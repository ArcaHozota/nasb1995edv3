package app.preach.gospel.service.impl;

import static app.preach.gospel.jooq.Tables.HYMNS;
import static app.preach.gospel.jooq.Tables.HYMNS_WORK;
import static app.preach.gospel.jooq.Tables.STUDENTS;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.ConfigurationException;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.DataChangedException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.HymnDto;
import app.preach.gospel.dto.IdfKey;
import app.preach.gospel.dto.TokKey;
import app.preach.gospel.dto.VecKey;
import app.preach.gospel.jooq.Keys;
import app.preach.gospel.jooq.tables.records.HymnsRecord;
import app.preach.gospel.jooq.tables.records.HymnsWorkRecord;
import app.preach.gospel.jooq.tables.records.StudentsRecord;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoProjectUtils;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.LineNumber;
import app.preach.gospel.utils.Pagination;
import app.preach.gospel.utils.SnowflakeUtils;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 賛美歌サービス実装クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Log4j2
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class HymnServiceImpl implements IHymnService {

	/**
	 * 共通検索条件
	 */
	protected static final Condition COMMON_CONDITION = HYMNS.VISIBLE_FLG.eq(Boolean.TRUE);

	/**
	 * 日時フォマーター
	 */
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * KOMORAN-API
	 */
	private static final Komoran KOMORAN = new Komoran(DEFAULT_MODEL.FULL);

	/**
	 * Korean Language
	 */
	private static final String KR = "Korean";

	/**
	 * ランドム選択
	 */
	private static final Random RANDOM = new Random();

	/**
	 * ダイジェストメソッド
	 */
	private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	});

	/**
	 * 怪しいキーワードリスト
	 */
	private static final String[] STRANGE_ARRAY = { "insert", "delete", "update", "create", "drop", "#", "$", "%", "&",
			"(", ")", "\"", "\'", "@", ":", "select" };

	/**
	 * コサイン類似度を計算する
	 *
	 * @param vectorA ベクターA
	 * @param vectorB ベクターB
	 * @return コサイン類似度
	 */
	private static double cosineSimilarity(final double @NotNull [] vectorA, final double[] vectorB) {
		double dotProduct = 0.00;
		double normA = 0.00;
		double normB = 0.00;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		if ((normA == 0) || (normB == 0)) {
			return 0; // 避免除0
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	/**
	 * 通常検索条件を取得する
	 *
	 * @param keyword キーワード
	 * @return Specification<Hymn>
	 */
	private static @NotNull String getHymnSpecification(final String keyword) {
		return CoProjectUtils.isEmpty(keyword) ? CoProjectUtils.HANKAKU_PERCENTSIGN
				: CoProjectUtils.HANKAKU_PERCENTSIGN.concat(keyword).concat(CoProjectUtils.HANKAKU_PERCENTSIGN);
	}

	/**
	 * キャシュー
	 */
	@Qualifier("nlpCache")
	private final Cache<Object, Object> cache;

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	@Transactional(readOnly = true)
	@Override
	public CoResult<Integer, DataAccessException> checkDuplicated(final String id, final String nameJp) {
		try {
			if (CoProjectUtils.isDigital(id)) {
				final Integer checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
						.and(HYMNS.ID.ne(Long.parseLong(id))).and(HYMNS.NAME_JP.eq(nameJp)).fetchSingle()
						.into(Integer.class);
				return CoResult.ok(checkDuplicated);
			}
			final Integer checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.eq(nameJp)).fetchSingle().into(Integer.class);
			return CoResult.ok(checkDuplicated);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<Integer, DataAccessException> checkDuplicated2(final String id, final String nameKr) {
		try {
			if (CoProjectUtils.isDigital(id)) {
				final Integer checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
						.and(HYMNS.ID.ne(Long.parseLong(id))).and(HYMNS.NAME_KR.eq(nameKr)).fetchSingle()
						.into(Integer.class);
				return CoResult.ok(checkDuplicated);
			}
			final Integer checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.NAME_KR.eq(nameKr)).fetchSingle().into(Integer.class);
			return CoResult.ok(checkDuplicated);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	// 3) TF-IDF ベクトルキャッシュ
	private double[] computeTfIdfVector(final String lang, final String hymnVersion, final long hymnId,
			final String text, final Map<String, Double> idf) {
		final var key = new VecKey(lang, hymnVersion, hymnId, this.hash(text));
		final var cached = (double[]) this.cache.getIfPresent(key);
		if (cached != null) {
			return cached;
		}
		final var tokens = this.tokenize(lang, "KOMORAN", text);
		final var tf = new HashMap<String, Integer>();
		tokens.forEach(t -> tf.merge(t, 1, Integer::sum));
		final var vec = new double[idf.size()];
		final Map<String, Integer> termIndex = this.indexOf(idf.keySet()); // term -> position の固定順序マップを作る
		tf.forEach((term, cnt) -> {
			final var i = termIndex.get(term);
			if (i != null) {
				vec[i] = cnt * idf.getOrDefault(term, 0.0);
			}
		});
		this.cache.put(key, vec);
		return vec;
	}

	/**
	 * 最も似てる三つの賛美歌を取得する
	 *
	 * @param target   目標テキスト
	 * @param elements 賛美歌リスト
	 * @return List<HymnsRecord>
	 */
	private List<HymnsRecord> findTopThreeMatches(final HymnsRecord target, final List<HymnsRecord> elements) {
		final String corpusVersion = this.getCorpusVersion();
		final Stream<List<String>> hymnsStream = elements.stream().map(e -> this.tokenize(KR, "KOMORAN", e.getSerif()));
		final Map<String, Double> idf = this.getIdf(target.getUpdatedTime().toString(), hymnsStream);
		final double[] targetVector = this.computeTfIdfVector(KR, corpusVersion, target.getId(), target.getSerif(),
				idf);
		final List<double[]> elementVectors = elements.stream()
				.map(item -> this.computeTfIdfVector(KR, corpusVersion, item.getId(), item.getSerif(), idf)).toList();
		final PriorityQueue<Entry<HymnsRecord, Double>> maxHeap = new PriorityQueue<>(
				Comparator.comparing(Entry<HymnsRecord, Double>::getValue).reversed());
		for (int i = 0; i < elements.size(); i++) {
			final double similarity = HymnServiceImpl.cosineSimilarity(targetVector, elementVectors.get(i));
			maxHeap.add(new AbstractMap.SimpleEntry<>(elements.get(i), similarity));
		}
		return maxHeap.stream().limit(3).map(Entry::getKey).toList();
	}

	@Transactional(readOnly = true)
	private String getCorpusVersion() {
		// 1. MAX(updated_at) を取得
		final OffsetDateTime maxUpdatedAt = this.dslContext.select(DSL.max(HYMNS.UPDATED_TIME)).from(HYMNS).fetchOne(0,
				OffsetDateTime.class);
		// 2. null の場合（レコードなし）は現在時刻を代替
		if (maxUpdatedAt == null) {
			return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		}
		// 3. ISO 文字列に変換
		return maxUpdatedAt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<HymnDto, DataAccessException> getHymnInfoById(final Long id) {
		try {
			final HymnsRecord hymnsRecord = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.ID.eq(id)).fetchSingle();
			final HymnsWorkRecord hymnsWorkRecord = this.dslContext.selectFrom(HYMNS_WORK)
					.where(HYMNS_WORK.WORK_ID.eq(id)).fetchSingle();
			final StudentsRecord studentsRecord = this.dslContext.selectFrom(STUDENTS)
					.where(StudentServiceImpl.COMMON_CONDITION).and(STUDENTS.ID.eq(hymnsRecord.getUpdatedUser()))
					.fetchSingle();
			final ZonedDateTime zonedDateTime = hymnsRecord.getUpdatedTime().atZoneSameInstant(ZoneOffset.ofHours(9));
			final var hymnDto = new HymnDto(hymnsRecord.getId().toString(), hymnsRecord.getNameJp(),
					hymnsRecord.getNameKr(), hymnsRecord.getSerif(), hymnsRecord.getLink(), hymnsWorkRecord.getScore(),
					hymnsWorkRecord.getBiko(), studentsRecord.getUsername(),
					FORMATTER.format(zonedDateTime.toLocalDateTime()), null);
			return CoResult.ok(hymnDto);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<Pagination<HymnDto>, DataAccessException> getHymnsByKeyword(final Integer pageNum,
			final String keyword) {
		try {
			final String searchStr = getHymnSpecification(keyword);
			final Long totalRecords = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.like(searchStr).or(HYMNS.NAME_KR.like(searchStr))).fetchSingle()
					.into(Long.class);
			final int offset = (pageNum - 1) * ProjectConstants.DEFAULT_PAGE_SIZE;
			final List<HymnsRecord> hymnsRecords = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.like(searchStr).or(HYMNS.NAME_KR.like(searchStr))).orderBy(HYMNS.ID.asc())
					.limit(ProjectConstants.DEFAULT_PAGE_SIZE).offset(offset).fetchInto(HymnsRecord.class);
			final List<HymnDto> hymnDtos = this.mapToDtos(hymnsRecords, LineNumber.SNOWY);
			final Pagination<HymnDto> pagination = Pagination.of(hymnDtos, totalRecords, pageNum,
					ProjectConstants.DEFAULT_PAGE_SIZE);
			return CoResult.ok(pagination);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<List<HymnDto>, DataAccessException> getHymnsRandomFive(final String keyword) {
		try {
			for (final String starngement : STRANGE_ARRAY) {
				if (keyword.toLowerCase().contains(starngement) || keyword.length() >= 100) {
					final List<HymnDto> hymnDtos = this.mapToDtos(
							this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).orderBy(HYMNS.ID.asc())
									.limit(ProjectConstants.DEFAULT_PAGE_SIZE).fetchInto(HymnsRecord.class),
							LineNumber.SNOWY);
					log.error("怪しいキーワード： " + keyword);
					return CoResult.ok(hymnDtos);
				}
			}
			if (CoProjectUtils.isEmpty(keyword)) {
				final List<HymnDto> totalRecords = this.mapToDtos(
						this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).fetchInto(HymnsRecord.class),
						LineNumber.SNOWY);
				final List<HymnDto> hymnDtos1 = this.randomFiveLoop2(totalRecords);
				return CoResult.ok(hymnDtos1);
			}
			final List<HymnDto> withName = this.mapToDtos(this.dslContext.select(HYMNS.fields()).from(HYMNS)
					.innerJoin(HYMNS_WORK).onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.eq(keyword).or(HYMNS.NAME_KR.eq(keyword))
							.or(HYMNS_WORK.NAME_JP_RATIONAL.like("%[".concat(keyword).concat("]%"))))
					.fetchInto(HymnsRecord.class), LineNumber.CADMIUM);
			final List<HymnDto> hymnDtos = new ArrayList<>(withName);
			final List<String> withNameIds = withName.stream().map(HymnDto::id).toList();
			final String specification = getHymnSpecification(keyword);
			final List<HymnDto> withNameLike = this.mapToDtos(
					this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
							.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
							.and(HYMNS.NAME_JP.like(specification).or(HYMNS.NAME_KR.like(specification))
									.or(HYMNS_WORK.NAME_JP_RATIONAL.like(specification)))
							.fetchInto(HymnsRecord.class).stream()
							.filter(a -> !withNameIds.contains(a.getId().toString())).toList(),
					LineNumber.BURGUNDY);
			hymnDtos.addAll(withNameLike);
			final List<String> withNameLikeIds = withNameLike.stream().map(HymnDto::id).toList();
			if (hymnDtos.stream().distinct().toList().size() >= ProjectConstants.DEFAULT_PAGE_SIZE) {
				final List<HymnDto> randomFiveLoop = this.randomFiveLoop(withName, withNameLike);
				return CoResult.ok(randomFiveLoop.stream()
						.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList());
			}
			final String detailKeyword = CoProjectUtils.getDetailKeyword(keyword);
			final List<HymnDto> withRandomFive = this
					.mapToDtos(
							this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
									.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
									.and(HYMNS.NAME_JP.like(detailKeyword).or(HYMNS.NAME_KR.like(detailKeyword))
											.or(HYMNS_WORK.NAME_JP_RATIONAL.like(detailKeyword)
													.or(HYMNS.SERIF.like(detailKeyword))))
									.fetchInto(HymnsRecord.class).stream()
									.filter(a -> !withNameIds.contains(a.getId().toString())
											&& !withNameLikeIds.contains(a.getId().toString()))
									.toList(),
							LineNumber.NAPLES);
			hymnDtos.addAll(withRandomFive);
			if (hymnDtos.stream().distinct().toList().size() >= ProjectConstants.DEFAULT_PAGE_SIZE) {
				final List<HymnDto> hymnDtos2 = new ArrayList<>();
				hymnDtos2.addAll(withName);
				hymnDtos2.addAll(withNameLike);
				final List<HymnDto> randomFiveLoop = this.randomFiveLoop(hymnDtos2, withRandomFive);
				return CoResult.ok(randomFiveLoop.stream()
						.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList());
			}
			final List<HymnDto> totalRecords = this.mapToDtos(
					this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).fetchInto(HymnsRecord.class),
					LineNumber.SNOWY);
			final List<HymnDto> randomFiveLoop = this.randomFiveLoop(hymnDtos, totalRecords);
			return CoResult.ok(randomFiveLoop.stream()
					.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList());
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	// 2) IDF キャッシュ（コーパススナップショットで）
	private Map<String, Double> getIdf(final String corpusVersion, final Stream<List<String>> allDocs) {
		final IdfKey key = new IdfKey(corpusVersion);
		@SuppressWarnings("unchecked")
		final Map<String, Double> cached = (Map<String, Double>) this.cache.getIfPresent(key);
		if (cached != null) {
			return cached;
		}
		final Map<String, Integer> df = new HashMap<>();
		final List<List<String>> list = allDocs.toList();
		for (final List<String> docs : list) {
			docs.stream().distinct().forEach(term -> df.merge(term, 1, Integer::sum));
		}
		final long totalDocs = list.size();
		final var idf = df.entrySet().stream().collect(
				Collectors.toMap(Map.Entry::getKey, e -> Math.log((totalDocs + 1.0) / (e.getValue() + 1.0)) + 1.0));
		this.cache.put(key, idf);
		return idf;
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<List<HymnDto>, DataAccessException> getKanumiList(final Long id) {
		try {
			final HymnsRecord hymnsRecord = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.ID.eq(id)).fetchSingle();
			final List<HymnDto> hymnDtos = new ArrayList<>();
			hymnDtos.add(new HymnDto(hymnsRecord.getId().toString(), hymnsRecord.getNameJp(), hymnsRecord.getNameKr(),
					hymnsRecord.getSerif(), hymnsRecord.getLink(), null, null, null, null, LineNumber.BURGUNDY));
			final List<HymnsRecord> hymnsRecords = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.ID.ne(id)).fetchInto(HymnsRecord.class);
			final List<HymnsRecord> topTwoMatches = this.findTopThreeMatches(hymnsRecord, hymnsRecords);
			final List<HymnDto> list = this.mapToDtos(topTwoMatches, LineNumber.NAPLES);
			hymnDtos.addAll(list);
			return CoResult.ok(hymnDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<Long, DataAccessException> getTotalCounts() {
		try {
			final Long totalRecords = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION).fetchSingle()
					.into(Long.class);
			return CoResult.ok(totalRecords);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	/**
	 * ハッシュ化する
	 *
	 * @param text テキスト
	 * @return String
	 */
	private String hash(final String text) {
		final var b = SHA256.get().digest(text.getBytes(CoProjectUtils.CHARSET_UTF8));
		final var sb = new StringBuilder();
		for (final byte x : b) {
			sb.append(String.format("%02x", x));
		}
		return sb.toString();
	}

	/**
	 * インデクスを取得する
	 *
	 * @param terms リスト
	 * @return Map<String, Integer>
	 */
	private Map<String, Integer> indexOf(final Collection<String> terms) {
		final var map = new HashMap<String, Integer>(terms.size() * 2);
		int i = 0;
		for (final var t : terms) {
			map.put(t, i++);
		}
		return map;
	}

	@Override
	public CoResult<String, DataAccessException> infoDeletion(final Long id) {
		try {
			final HymnsRecord hymnsRecord = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.ID.eq(id)).fetchSingle();
			hymnsRecord.setVisibleFlg(Boolean.FALSE);
			this.dslContext.deleteFrom(HYMNS_WORK).where(HYMNS_WORK.WORK_ID.eq(id)).execute();
			hymnsRecord.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_DELETED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<Integer, DataAccessException> infoStorage(final @NotNull HymnDto hymnDto) {
		try {
			final HymnsRecord hymnsRecord = this.dslContext.newRecord(HYMNS);
			final String trimedSerif = this.trimSerif(hymnDto.serif());
			hymnsRecord.setId(SnowflakeUtils.snowflakeId());
			hymnsRecord.setNameJp(hymnDto.nameJp());
			hymnsRecord.setNameKr(hymnDto.nameKr());
			hymnsRecord.setLink(hymnDto.link());
			hymnsRecord.setSerif(trimedSerif);
			hymnsRecord.setVisibleFlg(Boolean.TRUE);
			hymnsRecord.setUpdatedUser(Long.parseLong(hymnDto.updatedUser()));
			hymnsRecord.setUpdatedTime(OffsetDateTime.now());
			hymnsRecord.insert();
			final HymnsWorkRecord hymnsWorkRecord = this.dslContext.newRecord(HYMNS_WORK);
			hymnsWorkRecord.setWorkId(hymnsRecord.getId());
			hymnsWorkRecord.setUpdatedTime(OffsetDateTime.now());
			hymnsWorkRecord.insert();
			final Long totalRecords = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION).fetchSingle()
					.into(Long.class);
			final int discernLargestPage = CoProjectUtils.discernLargestPage(totalRecords);
			return CoResult.ok(discernLargestPage);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<String, DataAccessException> infoUpdation(final @NotNull HymnDto hymnDto) {
		try {
			final HymnsRecord hymnsRecord = this.dslContext.newRecord(HYMNS);
			hymnsRecord.setId(Long.valueOf(hymnDto.id()));
			hymnsRecord.setNameJp(hymnDto.nameJp());
			hymnsRecord.setNameKr(hymnDto.nameKr());
			hymnsRecord.setLink(hymnDto.link());
			hymnsRecord.setSerif(hymnDto.serif());
			hymnsRecord.setVisibleFlg(Boolean.TRUE);
			final HymnsRecord hymnsRecord2 = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.ID.eq(hymnsRecord.getId())).fetchSingle();
			final String updatedTime1 = hymnDto.updatedTime();
			final String updatedTime2 = FORMATTER
					.format(hymnsRecord2.getUpdatedTime().atZoneSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime());
			hymnsRecord2.setUpdatedTime(null);
			hymnsRecord2.setUpdatedUser(null);
			if (CoProjectUtils.isEqual(hymnsRecord, hymnsRecord2)) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_STRING_NO_CHANGE));
			}
			if (CoProjectUtils.isNotEqual(updatedTime1, updatedTime2)) {
				return CoResult.err(new DataChangedException(ProjectConstants.MESSAGE_OPTIMISTIC_ERROR));
			}
			final String trimedSerif = this.trimSerif(hymnsRecord.getSerif());
			hymnsRecord2.setNameJp(hymnsRecord.getNameJp());
			hymnsRecord2.setNameKr(hymnsRecord.getNameKr());
			hymnsRecord2.setLink(hymnsRecord.getLink());
			hymnsRecord2.setSerif(trimedSerif);
			hymnsRecord2.setUpdatedUser(Long.parseLong(hymnDto.updatedUser()));
			hymnsRecord2.setUpdatedTime(OffsetDateTime.now());
			hymnsRecord2.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	/**
	 * DTOへ変換する
	 *
	 * @param hymns      賛美歌リスト
	 * @param lineNumber 行番号
	 * @return List<HymnDto>
	 */
	private List<HymnDto> mapToDtos(final @NotNull List<HymnsRecord> hymns, final LineNumber lineNumber) {
		return hymns.stream()
				.map(hymnsRecord -> new HymnDto(hymnsRecord.getId().toString(), hymnsRecord.getNameJp(),
						hymnsRecord.getNameKr(), hymnsRecord.getSerif(), hymnsRecord.getLink(), null, null, null, null,
						lineNumber))
				.toList();
	}

	/**
	 * ランドム選択ループ1
	 *
	 * @param hymnsRecords 選択したレコード
	 * @param totalRecords 総合レコード
	 * @return List<HymnDto>
	 */
	private @NotNull List<HymnDto> randomFiveLoop(final @NotNull List<HymnDto> hymnsRecords,
			final @NotNull List<HymnDto> totalRecords) {
		final var ids = hymnsRecords.stream().map(HymnDto::id).distinct().toList();
		final var filteredRecords = totalRecords.stream().filter(item -> !ids.contains(item.id())).toList();
		final var concernList1 = new ArrayList<>(hymnsRecords);
		if (hymnsRecords.size() < ProjectConstants.DEFAULT_PAGE_SIZE) {
			final int sagaku = ProjectConstants.DEFAULT_PAGE_SIZE - hymnsRecords.size();
			for (int i = 1; i <= sagaku; i++) {
				final int indexOf = RANDOM.nextInt(filteredRecords.size());
				final var hymnsRecord = filteredRecords.get(indexOf);
				concernList1.add(hymnsRecord);
			}
		}
		final var concernList2 = concernList1.stream().distinct().toList();
		if (concernList2.size() == ProjectConstants.DEFAULT_PAGE_SIZE) {
			return concernList2;
		}
		return this.randomFiveLoop(concernList2, filteredRecords);
	}

	/**
	 * ランドム選択ループ2
	 *
	 * @param hymnsRecords 選択したレコード
	 * @return List<HymnDto>
	 */
	private @NotNull List<HymnDto> randomFiveLoop2(final List<HymnDto> hymnsRecords) {
		final List<HymnDto> concernList1 = new ArrayList<>();
		for (int i = 1; i <= ProjectConstants.DEFAULT_PAGE_SIZE; i++) {
			final int indexOf = RANDOM.nextInt(hymnsRecords.size());
			final var hymnsRecord = hymnsRecords.get(indexOf);
			concernList1.add(hymnsRecord);
		}
		final List<HymnDto> concernList2 = concernList1.stream().distinct().toList();
		if (concernList2.size() == ProjectConstants.DEFAULT_PAGE_SIZE) {
			return concernList2;
		}
		return this.randomFiveLoop(concernList2, hymnsRecords);
	}

	@Override
	public CoResult<String, DataAccessException> scoreStorage(final @NotNull byte[] file, final Long id) {
		try {
			final HymnsWorkRecord hymnsWorkRecord = this.dslContext.selectFrom(HYMNS_WORK)
					.where(HYMNS_WORK.WORK_ID.eq(id)).fetchSingle();
			if (Arrays.equals(hymnsWorkRecord.getScore(), file)) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_STRING_NO_CHANGE));
			}
			hymnsWorkRecord.setScore(file);
			hymnsWorkRecord.setUpdatedTime(OffsetDateTime.now());
			hymnsWorkRecord.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	// 1) 形態素解析キャッシュ
	private List<String> tokenize(final String lang, final String tokenizer, final String text) {
		final var regex = "\\p{IsHangul}";
		final var builder = new StringBuilder();
		for (final var ch : text.toCharArray()) {
			if (Pattern.matches(regex, String.valueOf(ch))) {
				builder.append(ch);
			}
		}
		final var koreanText = builder.toString();
		if (CoProjectUtils.isEmpty(koreanText)) {
			return new ArrayList<>();
		}
		final var key = new TokKey(lang, tokenizer, this.hash(koreanText));
		@SuppressWarnings("unchecked")
		final var cached = (List<String>) this.cache.getIfPresent(key);
		if (cached != null) {
			return cached;
		}
		final var tokens = KOMORAN.analyze(koreanText).getTokenList().stream().map(t -> t.getMorph()) // 正規化前
				.toList();
		this.cache.put(key, tokens);
		return tokens;
	}

	/**
	 * セリフの全角スペースを削除する
	 *
	 * @param serif セリフ
	 * @return トリムドのセリフ
	 */
	private @NotNull String trimSerif(final @NotNull String serif) {
		final var zenkakuSpace = "\u3000";
		final var replace = serif.replace(zenkakuSpace, CoProjectUtils.EMPTY_STRING);
		return replace.trim();
	}

}
