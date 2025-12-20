package app.preach.gospel.service.impl;

import static app.preach.gospel.jooq.Routines.similarity;
import static app.preach.gospel.jooq.Tables.HYMNS;
import static app.preach.gospel.jooq.Tables.HYMNS_WORK;
import static app.preach.gospel.jooq.Tables.STUDENTS;
import static org.jooq.impl.DSL.val;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.exception.ConfigurationException;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.DataChangedException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;
import com.github.benmanes.caffeine.cache.Cache;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.DocKey;
import app.preach.gospel.dto.HymnDto;
import app.preach.gospel.dto.IdfKey;
import app.preach.gospel.dto.TokKey;
import app.preach.gospel.dto.VecKey;
import app.preach.gospel.jooq.Keys;
import app.preach.gospel.jooq.tables.records.HymnsWorkRecord;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.CoSortsUtils;
import app.preach.gospel.utils.CoStringUtils;
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
		return CoStringUtils.isEmpty(keyword) ? CoStringUtils.HANKAKU_PERCENTSIGN
				: CoStringUtils.HANKAKU_PERCENTSIGN.concat(keyword).concat(CoStringUtils.HANKAKU_PERCENTSIGN);
	}

	/**
	 * セリフの全角スペースを削除する
	 *
	 * @param serif セリフ
	 * @return トリムドのセリフ
	 */
	private static @NotNull String trimSerif(final @NotNull String serif) {
		final var zenkakuSpace = "\u3000";
		final var replace = serif.replace(zenkakuSpace, CoStringUtils.EMPTY_STRING);
		return replace.trim();
	}

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	/**
	 * キャシュー
	 */
	@Qualifier("nlpCache")
	private final Cache<Object, Object> nlpCache;

	@Transactional(readOnly = true)
	@Override
	public CoResult<Integer, DataAccessException> checkDuplicated(final String id, final String nameJp) {
		try {
			if (CoStringUtils.isDigital(id)) {
				final var checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
						.and(HYMNS.ID.ne(Long.parseLong(id))).and(HYMNS.NAME_JP.eq(nameJp)).fetchSingle()
						.into(Integer.class);
				return CoResult.ok(checkDuplicated);
			}
			final var checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.eq(nameJp)).fetchSingle().into(Integer.class);
			return CoResult.ok(checkDuplicated);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<Integer, DataAccessException> checkDuplicated2(final String id, final String nameKr) {
		try {
			if (CoStringUtils.isDigital(id)) {
				final var checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
						.and(HYMNS.ID.ne(Long.parseLong(id))).and(HYMNS.NAME_KR.eq(nameKr)).fetchSingle()
						.into(Integer.class);
				return CoResult.ok(checkDuplicated);
			}
			final var checkDuplicated = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.NAME_KR.eq(nameKr)).fetchSingle().into(Integer.class);
			return CoResult.ok(checkDuplicated);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	// 3) TF-IDF ベクトルキャッシュ
	private double[] computeTfIdfVector(final String lang, final String hymnVersion, final long hymnId,
			final String text, final Map<String, Double> idf) {
		final var key = new VecKey(lang, hymnVersion, hymnId, this.hash(text));
		final var cached = (double[]) this.nlpCache.getIfPresent(key);
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
		this.nlpCache.put(key, vec);
		return vec;
	}

	/**
	 * イメージからPDFへ変換する
	 *
	 * @param img            イメージ
	 * @param pdfDiscernment タイプ
	 * @return byte[]
	 */
	private byte[] convertCenteredImage(final byte[] img, final String pdfDiscernment) {
		if (CoStringUtils.isEqual(MediaType.APPLICATION_PDF_VALUE, pdfDiscernment)) {
			return img;
		}
		try (final var doc = new PDDocument()) {
			final var page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			final BufferedImage image = (CoStringUtils.isEqual(MediaType.IMAGE_JPEG_VALUE, pdfDiscernment))
					? CoSortsUtils.readAndNormalizeOrientation(img)
					: ImageIO.read(new ByteArrayInputStream(img));
			// A4 の幅・高さ（ポイント: 1pt = 1/72 inch）
			final float pageWidth = page.getMediaBox().getWidth();
			final float pageHeight = page.getMediaBox().getHeight();
			// 画像の元サイズ（ピクセル）。PDFBox はピクセルをそのままポイントとして扱っても OK
			final float imgWidth = image.getWidth();
			final float imgHeight = image.getHeight();
			// 描画サイズ計算
			float drawWidth;
			float drawHeight;
			// 画像が A4 より小さい → 拡大しない
			if (imgWidth <= pageWidth && imgHeight <= pageHeight) {
				drawWidth = imgWidth;
				drawHeight = imgHeight;
			} else {
				// 画像が A4 より大きい → A4 に収まるように縮小
				final float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
				drawWidth = imgWidth * scale;
				drawHeight = imgHeight * scale;
			}
			// 画像オブジェクト作成
			final PDImageXObject pdfImage = LosslessFactory.createFromImage(doc, image);
			// 中央配置用の座標計算（原点は左下）
			final float x = (pageWidth - drawWidth) / 2f;
			final float y = (pageHeight - drawHeight) / 2f;
			try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
				contentStream.drawImage(pdfImage, x, y, drawWidth, drawHeight);
			}
			final var out = new ByteArrayOutputStream();
			doc.save(out);
			return out.toByteArray();
		} catch (final IOException e) {
			e.printStackTrace();
			return ProjectConstants.EMPTY_ARR;
		} catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 最も似てる三つの賛美歌を取得する
	 *
	 * @param target   目標テキスト
	 * @param elements 賛美歌リスト
	 * @return List<HymnsRecord>
	 */
	private List<HymnDto> findTopThreeMatches(final HymnDto target, final List<HymnDto> elements) {
		final String corpusVersion = this.getCorpusVersion();
		final var hymnsStream = elements.stream().map(e -> this.tokenize(KR, "KOMORAN", e.lyric()));
		final Map<String, Double> idf = this.getIdf(target.updatedTime().toString(), hymnsStream);
		final double[] targetVector = this.computeTfIdfVector(KR, corpusVersion, Long.valueOf(target.id()),
				target.lyric(), idf);
		final var elementVectors = elements.stream()
				.map(item -> this.computeTfIdfVector(KR, corpusVersion, Long.valueOf(item.id()), item.lyric(), idf))
				.toList();
		final var maxHeap = new PriorityQueue<Map.Entry<HymnDto, Double>>(
				Comparator.comparing(Map.Entry<HymnDto, Double>::getValue).reversed());
		for (int i = 0; i < elements.size(); i++) {
			final double similarity = HymnServiceImpl.cosineSimilarity(targetVector, elementVectors.get(i));
			maxHeap.add(new AbstractMap.SimpleEntry<>(elements.get(i), similarity));
		}
		return maxHeap.stream().limit(3).map(Map.Entry::getKey).toList();
	}

	@Transactional(readOnly = true)
	private String getCorpusVersion() {
		// 1. MAX(updated_at) を取得
		final var maxUpdatedAt = this.dslContext.select(DSL.max(HYMNS.UPDATED_TIME)).from(HYMNS).fetchOne(0,
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
			final var hymnsRecord = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).and(HYMNS.ID.eq(id))
					.fetchSingle();
			final var hymnsWorkRecord = this.dslContext.selectFrom(HYMNS_WORK).where(HYMNS_WORK.WORK_ID.eq(id))
					.fetchSingle();
			final var studentsRecord = this.dslContext.selectFrom(STUDENTS).where(StudentServiceImpl.COMMON_CONDITION)
					.and(STUDENTS.ID.eq(hymnsRecord.getUpdatedUser())).fetchSingle();
			final var zonedDateTime = hymnsRecord.getUpdatedTime().atZoneSameInstant(ZoneOffset.ofHours(9));
			final var hymnDto = new HymnDto(hymnsRecord.getId().toString(), hymnsRecord.getNameJp(),
					hymnsRecord.getNameKr(), hymnsRecord.getLyric(), hymnsRecord.getLink(), hymnsWorkRecord.getScore(),
					hymnsWorkRecord.getBiko(), studentsRecord.getUsername(),
					FORMATTER.format(zonedDateTime.toLocalDateTime()), null);
			return CoResult.ok(hymnDto);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<Pagination<HymnDto>, DataAccessException> getHymnsInfoByPagination(final Integer pageNum,
			final String keyword) {
		try {
			final var totalRecords = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION).fetchSingle()
					.into(Long.class);
			final int offset = (pageNum - 1) * ProjectConstants.DEFAULT_PAGE_SIZE;
			final int margrave = (int) ((offset + ProjectConstants.DEFAULT_PAGE_SIZE) > totalRecords ? totalRecords
					: offset + ProjectConstants.DEFAULT_PAGE_SIZE);
			final var docKey = new DocKey(keyword, this.getCorpusVersion(), totalRecords);
			@SuppressWarnings("unchecked")
			final var nlpedHymnDtos = (List<HymnDto>) this.nlpCache.getIfPresent(docKey);
			if (nlpedHymnDtos != null) {
				final var subList = nlpedHymnDtos.subList(offset, margrave);
				final var pagination = Pagination.of(subList, totalRecords, pageNum,
						ProjectConstants.DEFAULT_PAGE_SIZE);
				return CoResult.ok(pagination);
			}
			if (CoStringUtils.isEmpty(keyword)) {
				final var hymnDtos = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).orderBy(HYMNS.ID.asc())
						.fetch(rd -> new HymnDto(rd.getId().toString(), rd.getNameJp(), rd.getNameKr(), rd.getLyric(),
								rd.getLink(), null, null, rd.getUpdatedUser().toString(),
								rd.getUpdatedTime().toString(), LineNumber.SNOWY));
				final var pagination = Pagination.of(hymnDtos.subList(offset, margrave), totalRecords, pageNum,
						ProjectConstants.DEFAULT_PAGE_SIZE);
				this.nlpCache.put(docKey, hymnDtos);
				return CoResult.ok(pagination);
			}
			for (final String starngement : STRANGE_ARRAY) {
				if (keyword.toLowerCase().contains(starngement) || keyword.length() >= 100) {
					log.warn("怪しいキーワード： " + keyword);
					final var hymnDtos = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
							.orderBy(HYMNS.ID.asc())
							.fetch(rd -> new HymnDto(rd.getId().toString(), rd.getNameJp(), rd.getNameKr(),
									rd.getLyric(), rd.getLink(), null, null, rd.getUpdatedUser().toString(),
									rd.getUpdatedTime().toString(), LineNumber.SNOWY));
					final var pagination = Pagination.of(
							hymnDtos.subList(offset, offset + ProjectConstants.DEFAULT_PAGE_SIZE), totalRecords,
							pageNum, ProjectConstants.DEFAULT_PAGE_SIZE);
					this.nlpCache.put(docKey, hymnDtos);
					return CoResult.ok(pagination);
				}
			}
			final String[] splits = keyword.split("&");
			if (splits.length > 1) {
				final String searchStr1 = getHymnSpecification(splits[0]);
				final String searchStr2 = getHymnSpecification(splits[1]);
				final Field<Float> smlField1 = similarity(HYMNS.NAME_JP, val(splits[0]));
				final Field<Float> smlField2 = similarity(HYMNS.NAME_KR, val(splits[0]));
				final Field<Float> smlField3 = similarity(HYMNS.NAME_JP, val(splits[1]));
				final Field<Float> smlField4 = similarity(HYMNS.NAME_KR, val(splits[1]));
				final Condition condition1 = HYMNS.NAME_JP.like(searchStr1).and(HYMNS.NAME_JP.like(searchStr2));
				final Condition condition2 = HYMNS.NAME_KR.like(searchStr1).and(HYMNS.NAME_KR.like(searchStr2));
				final Condition condition3 = smlField1.gt(0.33f).and(smlField3.gt(0.33f));
				final Condition condition4 = smlField2.gt(0.33f).and(smlField4.gt(0.33f));
				final var withNameLike = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
						.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
						.and(condition1.or(condition2).or(condition3).or(condition4))
						.fetch(rd -> new HymnDto(rd.get(HYMNS.ID).toString(), rd.get(HYMNS.NAME_JP),
								rd.get(HYMNS.NAME_KR), rd.get(HYMNS.LYRIC), rd.get(HYMNS.LINK), null, null,
								rd.get(HYMNS.UPDATED_USER).toString(), rd.get(HYMNS.UPDATED_TIME).toString(),
								LineNumber.BURGUNDY));
				final var withNameLikeIds = withNameLike.stream().map(HymnDto::id).toList();
				final String detailKeyword1 = CoStringUtils.getDetailKeyword(splits[0]);
				final String detailKeyword2 = CoStringUtils.getDetailKeyword(splits[1]);
				final var tokenizer = new Tokenizer();
				final var sBuilder1 = new StringBuilder();
				final var sBuilder2 = new StringBuilder();
				final List<Token> tokens1 = tokenizer.tokenize(splits[0]);
				final List<Token> tokens2 = tokenizer.tokenize(splits[1]);
				tokens1.forEach(ab -> {
					sBuilder1.append(ab.getPronunciation());
				});
				final String detailKeyword3 = CoStringUtils.getDetailKeyword(sBuilder1.toString());
				tokens2.forEach(ab -> {
					sBuilder2.append(ab.getPronunciation());
				});
				final String detailKeyword4 = CoStringUtils.getDetailKeyword(sBuilder2.toString());
				final var withRandomFive = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
						.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
						.and((HYMNS.LYRIC.like(detailKeyword1).and(HYMNS.LYRIC.like(detailKeyword2))).or(
								HYMNS_WORK.FURIGANA.like(detailKeyword3).and(HYMNS_WORK.FURIGANA.like(detailKeyword4))))
						.fetch(rd -> {
							final String hymnId = rd.get(HYMNS.ID).toString();
							if (withNameLikeIds.contains(hymnId)) {
								return null;
							}
							return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR),
									rd.get(HYMNS.LYRIC), rd.get(HYMNS.LINK), null, null,
									rd.get(HYMNS.UPDATED_USER).toString(), rd.get(HYMNS.UPDATED_TIME).toString(),
									LineNumber.NAPLES);
						});
				withRandomFive.removeIf(a -> a == null);
				final var withRandomFiveIds = withRandomFive.stream().map(HymnDto::id).toList();
				final var otherHymns = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).orderBy(HYMNS.ID.asc())
						.fetch(rd -> {
							final String hymnId = rd.get(HYMNS.ID).toString();
							if (withNameLikeIds.contains(hymnId) || withRandomFiveIds.contains(hymnId)) {
								return null;
							}
							return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR),
									rd.get(HYMNS.LYRIC), rd.get(HYMNS.LINK), null, null,
									rd.get(HYMNS.UPDATED_USER).toString(), rd.get(HYMNS.UPDATED_TIME).toString(),
									LineNumber.SNOWY);
						});
				otherHymns.removeIf(a -> a == null);
				final var hymnDtos = new ArrayList<HymnDto>(withNameLike);
				hymnDtos.addAll(withRandomFive);
				hymnDtos.addAll(otherHymns);
				final var sortedHymnDtos = hymnDtos.stream()
						.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList();
				final var pagination = Pagination.of(sortedHymnDtos.subList(offset, margrave), totalRecords, pageNum,
						ProjectConstants.DEFAULT_PAGE_SIZE);
				this.nlpCache.put(docKey, sortedHymnDtos);
				return CoResult.ok(pagination);
			}
			final var withName = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
					.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.eq(keyword).or(HYMNS.NAME_KR.eq(keyword)))
					.fetch(rd -> new HymnDto(rd.get(HYMNS.ID).toString(), rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR),
							rd.get(HYMNS.LYRIC), rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
							rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.CADMIUM));
			final var hymnDtos = new ArrayList<HymnDto>(withName);
			final var withNameIds = withName.stream().map(HymnDto::id).toList();
			final String searchStr = getHymnSpecification(keyword);
			final Field<Float> smlField1 = similarity(HYMNS.NAME_JP, val(keyword));
			final Field<Float> smlField2 = similarity(HYMNS.NAME_KR, val(keyword));
			final var withNameLike = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
					.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.like(searchStr).or(HYMNS.NAME_KR.like(searchStr)).or(smlField1.gt(0.33f))
							.or(smlField2.gt(0.33f)))
					.fetch(rd -> {
						final String hymnId = rd.get(HYMNS.ID).toString();
						if (withNameIds.contains(hymnId)) {
							return null;
						}
						return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR), rd.get(HYMNS.LYRIC),
								rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
								rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.BURGUNDY);
					});
			withNameLike.removeIf(a -> a == null);
			final var withNameLikeIds = withNameLike.stream().map(HymnDto::id).toList();
			final String detailKeyword = CoStringUtils.getDetailKeyword(keyword);
			final var tokenizer = new Tokenizer();
			final var sBuilder = new StringBuilder();
			final List<Token> tokens = tokenizer.tokenize(keyword);
			tokens.forEach(ab -> {
				sBuilder.append(ab.getPronunciation());
			});
			final String detailKeyword2 = CoStringUtils.getDetailKeyword(sBuilder.toString());
			final var withRandomFive = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
					.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.LYRIC.like(detailKeyword).or(HYMNS_WORK.FURIGANA.like(detailKeyword2))).fetch(rd -> {
						final String hymnId = rd.get(HYMNS.ID).toString();
						if (withNameIds.contains(hymnId) || withNameLikeIds.contains(hymnId)) {
							return null;
						}
						return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR), rd.get(HYMNS.LYRIC),
								rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
								rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.NAPLES);
					});
			withRandomFive.removeIf(a -> a == null);
			final var withRandomFiveIds = withRandomFive.stream().map(HymnDto::id).toList();
			final var otherHymns = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).orderBy(HYMNS.ID.asc())
					.fetch(rd -> {
						final String hymnId = rd.get(HYMNS.ID).toString();
						if (withNameIds.contains(hymnId) || withNameLikeIds.contains(hymnId)
								|| withRandomFiveIds.contains(hymnId)) {
							return null;
						}
						return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR), rd.get(HYMNS.LYRIC),
								rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
								rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.SNOWY);
					});
			otherHymns.removeIf(a -> a == null);
			hymnDtos.addAll(withNameLike);
			hymnDtos.addAll(withRandomFive);
			hymnDtos.addAll(otherHymns);
			final var sortedHymnDtos = hymnDtos.stream()
					.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList();
			final var pagination = Pagination.of(sortedHymnDtos.subList(offset, margrave), totalRecords, pageNum,
					ProjectConstants.DEFAULT_PAGE_SIZE);
			this.nlpCache.put(docKey, sortedHymnDtos);
			return CoResult.ok(pagination);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<List<HymnDto>, DataAccessException> getHymnsInfoByRandom(final String keyword) {
		try {
			for (final String starngement : STRANGE_ARRAY) {
				if (keyword.toLowerCase().contains(starngement) || keyword.length() >= 100) {
					final var hymnDtos = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
							.orderBy(HYMNS.ID.asc()).limit(ProjectConstants.DEFAULT_PAGE_SIZE)
							.fetch(rd -> new HymnDto(rd.getId().toString(), rd.getNameJp(), rd.getNameKr(),
									rd.getLyric(), rd.getLink(), null, null, rd.getUpdatedUser().toString(),
									rd.getUpdatedTime().toString(), LineNumber.SNOWY));
					log.warn("怪しいキーワード： " + keyword);
					return CoResult.ok(hymnDtos);
				}
			}
			if (CoStringUtils.isEmpty(keyword)) {
				final var totalRecords = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
						.fetch(rd -> new HymnDto(rd.getId().toString(), rd.getNameJp(), rd.getNameKr(), rd.getLyric(),
								rd.getLink(), null, null, rd.getUpdatedUser().toString(),
								rd.getUpdatedTime().toString(), LineNumber.SNOWY));
				final List<HymnDto> hymnDtos = this.randomFiveLoop2(totalRecords);
				return CoResult.ok(hymnDtos);
			}
			final var withName = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
					.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.eq(keyword).or(HYMNS.NAME_KR.eq(keyword)))
					.fetch(rd -> new HymnDto(rd.get(HYMNS.ID).toString(), rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR),
							rd.get(HYMNS.LYRIC), rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
							rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.CADMIUM));
			final var hymnDtos = new ArrayList<HymnDto>(withName);
			final var withNameIds = withName.stream().map(HymnDto::id).toList();
			final String searchStr = getHymnSpecification(keyword);
			final Field<Float> smlField1 = similarity(HYMNS.NAME_JP, val(keyword));
			final Field<Float> smlField2 = similarity(HYMNS.NAME_KR, val(keyword));
			final var withNameLike = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
					.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.NAME_JP.like(searchStr).or(HYMNS.NAME_KR.like(searchStr)).or(smlField1.gt(0.33f))
							.or(smlField2.gt(0.33f)))
					.fetch(rd -> {
						final String hymnId = rd.get(HYMNS.ID).toString();
						if (withNameIds.contains(hymnId)) {
							return null;
						}
						return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR), rd.get(HYMNS.LYRIC),
								rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
								rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.BURGUNDY);
					});
			withNameLike.removeIf(a -> a == null);
			hymnDtos.addAll(withNameLike);
			final var withNameLikeIds = withNameLike.stream().map(HymnDto::id).toList();
			if (hymnDtos.size() >= ProjectConstants.DEFAULT_PAGE_SIZE) {
				final List<HymnDto> randomFiveLoop = this.randomFiveLoop(withName, withNameLike);
				return CoResult.ok(randomFiveLoop.stream()
						.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList());
			}
			final String detailKeyword = CoStringUtils.getDetailKeyword(keyword);
			final var tokenizer = new Tokenizer();
			final var sBuilder = new StringBuilder();
			final List<Token> tokens = tokenizer.tokenize(detailKeyword);
			tokens.forEach(ab -> {
				sBuilder.append(ab.getPronunciation());
			});
			final var withRandomFive = this.dslContext.select(HYMNS.fields()).from(HYMNS).innerJoin(HYMNS_WORK)
					.onKey(Keys.HYMNS_WORK__HYMNS_WORK_HYMNS_TO_WORK).where(COMMON_CONDITION)
					.and(HYMNS.LYRIC.like(detailKeyword).or(HYMNS_WORK.FURIGANA.like(sBuilder.toString())))
					.fetch(rd -> {
						final String hymnId = rd.get(HYMNS.ID).toString();
						if (withNameIds.contains(hymnId) || withNameLikeIds.contains(hymnId)) {
							return null;
						}
						return new HymnDto(hymnId, rd.get(HYMNS.NAME_JP), rd.get(HYMNS.NAME_KR), rd.get(HYMNS.LYRIC),
								rd.get(HYMNS.LINK), null, null, rd.get(HYMNS.UPDATED_USER).toString(),
								rd.get(HYMNS.UPDATED_TIME).toString(), LineNumber.NAPLES);
					});
			withRandomFive.removeIf(a -> a == null);
			hymnDtos.addAll(withRandomFive);
			if (hymnDtos.size() >= ProjectConstants.DEFAULT_PAGE_SIZE) {
				final var hymnDtos2 = new ArrayList<HymnDto>();
				hymnDtos2.addAll(withName);
				hymnDtos2.addAll(withNameLike);
				final List<HymnDto> randomFiveLoop = this.randomFiveLoop(hymnDtos2, withRandomFive);
				return CoResult.ok(randomFiveLoop.stream()
						.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList());
			}
			final var totalRecords = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.fetch(rd -> new HymnDto(rd.getId().toString(), rd.getNameJp(), rd.getNameKr(), rd.getLyric(),
							rd.getLink(), null, null, rd.getUpdatedUser().toString(), rd.getUpdatedTime().toString(),
							LineNumber.SNOWY));
			final List<HymnDto> randomFiveLoop = this.randomFiveLoop(hymnDtos, totalRecords);
			return CoResult.ok(randomFiveLoop.stream()
					.sorted(Comparator.comparingInt(item -> item.lineNumber().getLineNo())).toList());
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	// 2) IDF キャッシュ（コーパススナップショットで）
	private Map<String, Double> getIdf(final String corpusVersion, final Stream<List<String>> allDocs) {
		final IdfKey key = new IdfKey(corpusVersion);
		@SuppressWarnings("unchecked")
		final var cached = (Map<String, Double>) this.nlpCache.getIfPresent(key);
		if (cached != null) {
			return cached;
		}
		final Map<String, Integer> df = new HashMap<>();
		final var list = allDocs.toList();
		for (final var docs : list) {
			docs.stream().distinct().forEach(term -> df.merge(term, 1, Integer::sum));
		}
		final long totalDocs = list.size();
		final var idf = df.entrySet().stream().collect(
				Collectors.toMap(Map.Entry::getKey, e -> Math.log((totalDocs + 1.0) / (e.getValue() + 1.0)) + 1.0));
		this.nlpCache.put(key, idf);
		return idf;
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<List<HymnDto>, DataAccessException> getKanumiList(final Long id) {
		try {
			final var hymnsRecord = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).and(HYMNS.ID.eq(id))
					.fetchSingle();
			final List<HymnDto> hymnDtos = new ArrayList<>();
			hymnDtos.add(new HymnDto(hymnsRecord.getId().toString(), hymnsRecord.getNameJp(), hymnsRecord.getNameKr(),
					hymnsRecord.getLyric(), hymnsRecord.getLink(), null, null, null, null, LineNumber.BURGUNDY));
			final var list = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).and(HYMNS.ID.ne(id))
					.fetch(rd -> new HymnDto(rd.getId().toString(), rd.getNameJp(), rd.getNameKr(), rd.getLyric(),
							rd.getLink(), null, null, rd.getUpdatedUser().toString(), rd.getUpdatedTime().toString(),
							LineNumber.NAPLES));
			final var topThreeMatches = this.findTopThreeMatches(hymnDtos.get(0), list);
			hymnDtos.addAll(topThreeMatches);
			return CoResult.ok(hymnDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<Long, DataAccessException> getTotalCounts() {
		try {
			final var totalRecords = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION).fetchSingle()
					.into(Long.class);
			return CoResult.ok(totalRecords);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * ハッシュ化する
	 *
	 * @param text テキスト
	 * @return String
	 */
	private String hash(final String text) {
		final var b = SHA256.get().digest(text.getBytes(CoStringUtils.CHARSET_UTF8));
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
			final var hymnsRecord = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION).and(HYMNS.ID.eq(id))
					.fetchSingle();
			hymnsRecord.setVisibleFlg(Boolean.FALSE);
			this.dslContext.deleteFrom(HYMNS_WORK).where(HYMNS_WORK.WORK_ID.eq(id)).execute();
			hymnsRecord.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_DELETED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public CoResult<Integer, DataAccessException> infoStorage(final @NotNull HymnDto hymnDto) {
		try {
			final var hymnsRecord = this.dslContext.newRecord(HYMNS);
			final var trimedSerif = trimSerif(hymnDto.lyric());
			hymnsRecord.setId(SnowflakeUtils.snowflakeId());
			hymnsRecord.setNameJp(hymnDto.nameJp());
			hymnsRecord.setNameKr(hymnDto.nameKr());
			hymnsRecord.setLink(hymnDto.link());
			hymnsRecord.setLyric(trimedSerif);
			hymnsRecord.setVisibleFlg(Boolean.TRUE);
			hymnsRecord.setUpdatedUser(Long.parseLong(hymnDto.updatedUser()));
			hymnsRecord.setUpdatedTime(OffsetDateTime.now());
			hymnsRecord.insert();
			final var hymnsWorkRecord = this.dslContext.newRecord(HYMNS_WORK);
			final var tokenizer = new Tokenizer();
			final var sBuilder = new StringBuilder();
			final List<Token> tokens = tokenizer.tokenize(trimedSerif);
			if (!tokens.isEmpty()) {
				tokens.forEach(ab -> {
					sBuilder.append(ab.getAllFeatures());
				});
			}
			final int count = this.dslContext.selectCount().from(HYMNS_WORK).fetchSingle().into(Integer.class);
			hymnsWorkRecord.setId(Long.valueOf(count + 1L));
			hymnsWorkRecord.setWorkId(hymnsRecord.getId());
			hymnsWorkRecord.setFurigana(sBuilder.toString());
			hymnsWorkRecord.setUpdatedTime(OffsetDateTime.now());
			hymnsWorkRecord.insert();
			final var totalRecords = this.dslContext.selectCount().from(HYMNS).where(COMMON_CONDITION).fetchSingle()
					.into(Long.class);
			final int discernLargestPage = CoStringUtils.discernLargestPage(totalRecords);
			return CoResult.ok(discernLargestPage);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public CoResult<String, DataAccessException> infoUpdate(final @NotNull HymnDto hymnDto) {
		try {
			final var hymnsRecord = this.dslContext.newRecord(HYMNS);
			hymnsRecord.setId(Long.valueOf(hymnDto.id()));
			hymnsRecord.setNameJp(hymnDto.nameJp());
			hymnsRecord.setNameKr(hymnDto.nameKr());
			hymnsRecord.setLink(hymnDto.link());
			hymnsRecord.setLyric(hymnDto.lyric());
			hymnsRecord.setVisibleFlg(Boolean.TRUE);
			final var hymnsRecord2 = this.dslContext.selectFrom(HYMNS).where(COMMON_CONDITION)
					.and(HYMNS.ID.eq(hymnsRecord.getId())).fetchSingle();
			final var updatedTime1 = hymnDto.updatedTime();
			final var updatedTime2 = FORMATTER
					.format(hymnsRecord2.getUpdatedTime().atZoneSameInstant(ZoneOffset.ofHours(9)).toLocalDateTime());
			hymnsRecord2.setUpdatedTime(null);
			hymnsRecord2.setUpdatedUser(null);
			if (CoStringUtils.isEqual(hymnsRecord, hymnsRecord2)) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_STRING_NO_CHANGE));
			}
			if (CoStringUtils.isNotEqual(updatedTime1, updatedTime2)) {
				return CoResult.err(new DataChangedException(ProjectConstants.MESSAGE_OPTIMISTIC_ERROR));
			}
			final var trimedSerif = trimSerif(hymnsRecord.getLyric());
			final var tokenizer = new Tokenizer();
			final var sBuilder = new StringBuilder();
			final List<Token> tokens = tokenizer.tokenize(trimedSerif);
			tokens.forEach(ab -> {
				sBuilder.append(ab.getAllFeatures());
			});
			final HymnsWorkRecord hymnsWorkRecord = this.dslContext.selectFrom(HYMNS_WORK)
					.where(HYMNS_WORK.WORK_ID.eq(hymnsRecord2.getId())).fetchSingle();
			hymnsWorkRecord.setFurigana(sBuilder.toString());
			hymnsRecord2.setNameJp(hymnsRecord.getNameJp());
			hymnsRecord2.setNameKr(hymnsRecord.getNameKr());
			hymnsRecord2.setLink(hymnsRecord.getLink());
			hymnsRecord2.setLyric(trimedSerif);
			hymnsRecord2.setUpdatedUser(Long.parseLong(hymnDto.updatedUser()));
			hymnsRecord2.setUpdatedTime(OffsetDateTime.now());
			hymnsWorkRecord.update();
			hymnsRecord2.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
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
		final var ids = hymnsRecords.stream().map(HymnDto::id).distinct().toList();
		// 既に含まれていないレコード候補
		final var filteredRecords = totalRecords.stream().filter(item -> !ids.contains(item.id())).toList();
		// 結果リストを初期化
		final var result = new ArrayList<>(hymnsRecords);
		// 足りない分をランダム補充
		while (result.stream().distinct().count() < ProjectConstants.DEFAULT_PAGE_SIZE && !filteredRecords.isEmpty()) {
			final int indexOf = RANDOM.nextInt(filteredRecords.size());
			result.add(filteredRecords.get(indexOf));
		}
		// 最終的に distinct して返す
		return result.stream().distinct().toList();
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
		final var concernList2 = concernList1.stream().distinct().toList();
		if (concernList2.size() == ProjectConstants.DEFAULT_PAGE_SIZE) {
			return concernList2;
		}
		return this.randomFiveLoop(concernList2, hymnsRecords);
	}

	@Override
	public CoResult<String, DataAccessException> scoreStorage(final @NotNull byte[] file, final Long id) {
		try {
			final var hymnsWorkRecord = this.dslContext.selectFrom(HYMNS_WORK).where(HYMNS_WORK.WORK_ID.eq(id))
					.fetchSingle();
			final var tika = new Tika();
			final String pdfDiscernment = tika.detect(file);
			final byte[] centeredImage = this.convertCenteredImage(file, pdfDiscernment);
			if (Arrays.equals(hymnsWorkRecord.getScore(), file)
					|| Arrays.equals(hymnsWorkRecord.getScore(), centeredImage)) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_STRING_NO_CHANGE));
			}
			if (Arrays.equals(ProjectConstants.EMPTY_ARR, centeredImage)) {
				hymnsWorkRecord.setBiko(pdfDiscernment);
				hymnsWorkRecord.setScore(file);
				hymnsWorkRecord.setUpdatedTime(OffsetDateTime.now());
				hymnsWorkRecord.update();
				return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
			}
			hymnsWorkRecord.setBiko(MediaType.APPLICATION_PDF_VALUE);
			hymnsWorkRecord.setScore(centeredImage);
			hymnsWorkRecord.setUpdatedTime(OffsetDateTime.now());
			hymnsWorkRecord.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
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
		if (CoStringUtils.isEmpty(koreanText)) {
			return new ArrayList<>();
		}
		final var key = new TokKey(lang, tokenizer, this.hash(koreanText));
		@SuppressWarnings("unchecked")
		final var cached = (List<String>) this.nlpCache.getIfPresent(key);
		if (cached != null) {
			return cached;
		}
		final var tokens = KOMORAN.analyze(koreanText).getTokenList().stream().map(t -> t.getMorph()) // 正規化前
				.toList();
		this.nlpCache.put(key, tokens);
		return tokens;
	}

}
