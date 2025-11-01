package app.preach.gospel.service.impl;

import static app.preach.gospel.jooq.Tables.BOOKS;
import static app.preach.gospel.jooq.Tables.CHAPTERS;
import static app.preach.gospel.jooq.Tables.PHRASES;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.BookDto;
import app.preach.gospel.dto.ChapterDto;
import app.preach.gospel.dto.PhraseDto;
import app.preach.gospel.service.IBookService;
import app.preach.gospel.utils.CoProjectUtils;
import app.preach.gospel.utils.CoResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 聖書章節サービス実装クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BookServiceImpl implements IBookService {

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	@Transactional(readOnly = true)
	@Override
	public CoResult<List<BookDto>, DataAccessException> getBooks() {
		try {
			final var bookDtos = this.dslContext.selectFrom(BOOKS).orderBy(BOOKS.ID.asc())
					.fetch(r -> new BookDto(r.get(BOOKS.ID).toString(), r.get(BOOKS.NAME), r.get(BOOKS.NAME_JP)));
			return CoResult.ok(bookDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<List<ChapterDto>, DataAccessException> getChaptersByBookId(final String id) {
		try {
			if (CoProjectUtils.isDigital(id)) {
				final var chapterDtos = this.dslContext.selectFrom(CHAPTERS)
						.where(CHAPTERS.BOOK_ID.eq(Short.valueOf(id))).orderBy(CHAPTERS.ID.asc())
						.fetch(r -> new ChapterDto(r.get(CHAPTERS.ID).toString(), r.get(CHAPTERS.NAME),
								r.get(CHAPTERS.NAME_JP), r.get(CHAPTERS.BOOK_ID).toString()));
				return CoResult.ok(chapterDtos);
			}
			final var chapterDtos = this.dslContext.selectFrom(CHAPTERS).where(CHAPTERS.BOOK_ID.eq(Short.valueOf("1")))
					.orderBy(CHAPTERS.ID.asc()).fetch(r -> new ChapterDto(r.get(CHAPTERS.ID).toString(),
							r.get(CHAPTERS.NAME), r.get(CHAPTERS.NAME_JP), r.get(CHAPTERS.BOOK_ID).toString()));
			return CoResult.ok(chapterDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public CoResult<String, DataAccessException> infoStorage(final @NotNull PhraseDto phraseDto) {
		final var id = Long.valueOf(phraseDto.id());
		final var chapterId = Integer.valueOf(phraseDto.chapterId());
		try {
			final var chaptersRecord = this.dslContext.selectFrom(CHAPTERS).where(CHAPTERS.ID.eq(chapterId))
					.fetchSingle();
			final var phrasesRecord = this.dslContext.newRecord(PHRASES);
			phrasesRecord.setId((chapterId * 1000) + id);
			final var fetchOne = this.dslContext.selectFrom(PHRASES).where(PHRASES.ID.eq(phrasesRecord.getId()))
					.fetchOne();
			if (fetchOne != null) {
				fetchOne.setName(chaptersRecord.getName().concat("\u003a").concat(id.toString()));
				fetchOne.setTextJp(phraseDto.textJp());
				fetchOne.setChapterId(chapterId);
				final var textEn = phraseDto.textEn();
				if (textEn.endsWith("#")) {
					fetchOne.setTextEn(textEn.replace("#", CoProjectUtils.EMPTY_STRING));
					fetchOne.setChangeLine(Boolean.TRUE);
				} else {
					fetchOne.setTextEn(phraseDto.textEn());
					fetchOne.setChangeLine(Boolean.FALSE);
				}
				fetchOne.update();
				return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
			}
			phrasesRecord.setName(chaptersRecord.getName().concat("\u003a").concat(id.toString()));
			phrasesRecord.setTextJp(phraseDto.textJp());
			phrasesRecord.setChapterId(chapterId);
			final var textEn = phraseDto.textEn();
			if (textEn.endsWith("#")) {
				phrasesRecord.setTextEn(textEn.replace("#", CoProjectUtils.EMPTY_STRING));
				phrasesRecord.setChangeLine(Boolean.TRUE);
			} else {
				phrasesRecord.setTextEn(phraseDto.textEn());
				phrasesRecord.setChangeLine(Boolean.FALSE);
			}
			phrasesRecord.insert();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_INSERTED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
