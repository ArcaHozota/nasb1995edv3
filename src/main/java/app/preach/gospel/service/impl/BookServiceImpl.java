package app.preach.gospel.service.impl;

import static app.preach.gospel.jooq.Tables.BOOKS;
import static app.preach.gospel.jooq.Tables.CHAPTERS;
import static app.preach.gospel.jooq.Tables.PHRASES;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.BookDto;
import app.preach.gospel.dto.ChapterDto;
import app.preach.gospel.dto.PhraseDto;
import app.preach.gospel.jooq.tables.records.BooksRecord;
import app.preach.gospel.jooq.tables.records.ChaptersRecord;
import app.preach.gospel.jooq.tables.records.PhrasesRecord;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookServiceImpl implements IBookService {

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	@Override
	public CoResult<List<BookDto>, DataAccessException> getBooks() {
		try {
			final List<BooksRecord> booksRecords = this.dslContext.selectFrom(BOOKS).orderBy(BOOKS.ID.asc())
					.fetchInto(BooksRecord.class);
			final List<BookDto> bookDtos = booksRecords.stream()
					.map(booksRecord -> new BookDto(booksRecord.getId().toString(), booksRecord.getName(),
							booksRecord.getNameJp()))
					.toList();
			return CoResult.ok(bookDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<List<ChapterDto>, DataAccessException> getChaptersByBookId(final String id) {
		try {
			if (CoProjectUtils.isDigital(id)) {
				final List<ChaptersRecord> chaptersRecords = this.dslContext.selectFrom(CHAPTERS)
						.where(CHAPTERS.BOOK_ID.eq(Short.valueOf(id))).orderBy(CHAPTERS.ID.asc())
						.fetchInto(ChaptersRecord.class);
				final List<ChapterDto> chapterDtos = chaptersRecords.stream()
						.map(chaptersRecord -> new ChapterDto(chaptersRecord.getId().toString(),
								chaptersRecord.getName(), chaptersRecord.getNameJp(),
								chaptersRecord.getBookId().toString()))
						.toList();
				return CoResult.ok(chapterDtos);
			}
			final List<ChaptersRecord> chaptersRecords = this.dslContext.selectFrom(CHAPTERS)
					.where(CHAPTERS.BOOK_ID.eq(Short.valueOf("1"))).orderBy(CHAPTERS.ID.asc())
					.fetchInto(ChaptersRecord.class);
			final List<ChapterDto> chapterDtos = chaptersRecords.stream()
					.map(chaptersRecord -> new ChapterDto(chaptersRecord.getId().toString(), chaptersRecord.getName(),
							chaptersRecord.getNameJp(), chaptersRecord.getBookId().toString()))
					.toList();
			return CoResult.ok(chapterDtos);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<String, DataAccessException> infoStorage(final @NotNull PhraseDto phraseDto) {
		final Long id = Long.valueOf(phraseDto.id());
		final Integer chapterId = Integer.valueOf(phraseDto.chapterId());
		try {
			final ChaptersRecord chaptersRecord = this.dslContext.selectFrom(CHAPTERS).where(CHAPTERS.ID.eq(chapterId))
					.fetchSingle();
			final PhrasesRecord phrasesRecord = this.dslContext.newRecord(PHRASES);
			phrasesRecord.setId((chapterId * 1000) + id);
			phrasesRecord.setName(chaptersRecord.getName().concat("\u003a").concat(id.toString()));
			phrasesRecord.setTextJp(phraseDto.textJp());
			phrasesRecord.setChapterId(chapterId);
			final String textEn = phraseDto.textEn();
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
		}
	}

}
