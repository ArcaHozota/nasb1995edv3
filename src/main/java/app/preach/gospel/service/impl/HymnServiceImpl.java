package app.preach.gospel.service.impl;

import static app.preach.gospel.jooq.Tables.HYMNS;
import static app.preach.gospel.jooq.Tables.HYMNS_WORK;
import static app.preach.gospel.jooq.Tables.STUDENTS;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.ConfigurationException;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.DataChangedException;
import org.springframework.stereotype.Service;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.HymnDto;
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
import kr.co.shineware.nlp.komoran.model.Token;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HymnServiceImpl implements IHymnService {

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
	 * ランドム選択
	 */
	private static final Random RANDOM = new Random();

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
	 * コーパスサイズ
	 */
	private int corpusSize;

	/**
	 * 計算マップ2
	 */
	private final Map<String, Integer> docFreq = new LinkedHashMap<>();

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	/**
	 * 計算マップ1
	 */
	private final Map<String, Integer> termToIndex = new LinkedHashMap<>();

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

	/**
	 * TF-IDFベクターを計算する
	 *
	 * @param originalText 生のストリング
	 * @return double[]
	 */
	private double[] computeTFIDFVector(final String originalText) {
		final Map<String, Integer> termFreq = this.tokenizeKoreanTextWithFrequency(originalText);
		final int totalTerms = termFreq.values().stream().mapToInt(Integer::intValue).sum();
		final double[] vector = new double[this.termToIndex.size()];
		Arrays.fill(vector, 0.00);
		termFreq.forEach((term, count) -> {
			if (this.termToIndex.containsKey(term)) {
				final int index = this.termToIndex.get(term);
				// 计算TF
				final double tf = (double) count / totalTerms;
				// 计算IDF
				final int df = this.docFreq.getOrDefault(term, 0);
				final double idf = Math.log((double) this.corpusSize / (df + 1));
				vector[index] = tf * idf;
			}
		});
		return vector;
	}

	/**
	 * 最も似てる三つの賛美歌を取得する
	 *
	 * @param target   目標テキスト
	 * @param elements 賛美歌リスト
	 * @return List<HymnsRecord>
	 */
	private List<HymnsRecord> findTopTwoMatches(final String target, final List<HymnsRecord> elements) {
		final List<String> texts = elements.stream().map(HymnsRecord::getSerif).toList();
		this.preprocessCorpus(texts);
		final double[] targetVector = this.computeTFIDFVector(target);
		final List<double[]> elementVectors = elements.stream().map(item -> this.computeTFIDFVector(item.getSerif()))
				.toList();
		final PriorityQueue<Entry<HymnsRecord, Double>> maxHeap = new PriorityQueue<>(
				Comparator.comparing(Entry<HymnsRecord, Double>::getValue).reversed());
		for (int i = 0; i < elements.size(); i++) {
			final double similarity = HymnServiceImpl.cosineSimilarity(targetVector, elementVectors.get(i));
			maxHeap.add(new AbstractMap.SimpleEntry<>(elements.get(i), similarity));
		}
		return maxHeap.stream().limit(3).map(Entry::getKey).toList();
	}

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
			final HymnDto hymnDto = new HymnDto(hymnsRecord.getId().toString(), hymnsRecord.getNameJp(),
					hymnsRecord.getNameKr(), hymnsRecord.getSerif(), hymnsRecord.getLink(), hymnsWorkRecord.getScore(),
					hymnsWorkRecord.getBiko(), studentsRecord.getUsername(),
					FORMATTER.format(zonedDateTime.toLocalDateTime()), null);
			return CoResult.ok(hymnDto);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

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
			final List<HymnsRecord> topTwoMatches = this.findTopTwoMatches(hymnsRecord.getSerif(), hymnsRecords);
			final List<HymnDto> list = this.mapToDtos(topTwoMatches, LineNumber.NAPLES);
			hymnDtos.addAll(list);
			return CoResult.ok(hymnDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

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
	 * コーパスを取得する
	 *
	 * @param originalTexts
	 */
	private void preprocessCorpus(final List<String> originalTexts) {
		this.termToIndex.clear();
		this.docFreq.clear();
		this.corpusSize = originalTexts.size();
		int index = 0;
		// 第一遍：建立文档频率
		for (final String doc : originalTexts) {
			final Map<String, Integer> termFreq = this.tokenizeKoreanTextWithFrequency(doc);
			termFreq.keySet().forEach(term -> this.docFreq.put(term, this.docFreq.getOrDefault(term, 0) + 1));
		}
		// 第二遍：建立词汇表索引
		for (final String term : this.docFreq.keySet()) {
			this.termToIndex.put(term, index++);
		}
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
		final List<String> ids = hymnsRecords.stream().map(HymnDto::id).distinct().toList();
		final List<HymnDto> filteredRecords = totalRecords.stream().filter(item -> !ids.contains(item.id())).toList();
		final List<HymnDto> concernList1 = new ArrayList<>(hymnsRecords);
		if (hymnsRecords.size() < ProjectConstants.DEFAULT_PAGE_SIZE) {
			final int sagaku = ProjectConstants.DEFAULT_PAGE_SIZE - hymnsRecords.size();
			for (int i = 1; i <= sagaku; i++) {
				final int indexOf = RANDOM.nextInt(filteredRecords.size());
				final HymnDto hymnsRecord = filteredRecords.get(indexOf);
				concernList1.add(hymnsRecord);
			}
		}
		final List<HymnDto> concernList2 = concernList1.stream().distinct().toList();
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
			final HymnDto hymnsRecord = hymnsRecords.get(indexOf);
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

	/**
	 * テキストによって韓国語単語を取得する
	 *
	 * @param originalText テキスト
	 * @return Map<String, Integer>
	 */
	private Map<String, Integer> tokenizeKoreanTextWithFrequency(final @NotNull String originalText) {
		final String regex = "\\p{IsHangul}";
		final StringBuilder builder = new StringBuilder();
		for (final char ch : originalText.toCharArray()) {
			if (Pattern.matches(regex, String.valueOf(ch))) {
				builder.append(ch);
			}
		}
		final String koreanText = builder.toString();
		if (CoProjectUtils.isEmpty(koreanText)) {
			return new LinkedHashMap<>();
		}
		final List<Token> tokenList = KOMORAN.analyze(koreanText).getTokenList();
		return tokenList.stream().collect(Collectors.toMap(Token::getMorph, t -> 1, Integer::sum));
	}

	/**
	 * セリフの全半角スペースを削除する
	 *
	 * @param serif セリフ
	 * @return トリムドのセリフ
	 */
	private @NotNull String trimSerif(final @NotNull String serif) {
		final String zenkakuSpace = "\u3000";
		final String replace = serif.replace(zenkakuSpace, CoProjectUtils.EMPTY_STRING);
		final String hankakuSpace = "\u0020";
		return replace.replace(hankakuSpace, CoProjectUtils.EMPTY_STRING);
	}

}
