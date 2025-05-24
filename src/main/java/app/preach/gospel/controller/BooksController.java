package app.preach.gospel.controller;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.dto.BookDto;
import app.preach.gospel.dto.ChapterDto;
import app.preach.gospel.dto.PhraseDto;
import app.preach.gospel.service.IBookService;
import app.preach.gospel.utils.CoResult;
import jakarta.annotation.Resource;

/**
 * 聖書章節入力コントローラ
 *
 * @author ArkamaHozota
 * @since 1.10
 */
@Controller
@RequestMapping(ProjectURLConstants.URL_BOOKS_NAMESPACE)
public final class BooksController {

	/**
	 * 聖書章節サービスインターフェス
	 */
	@Resource
	private IBookService iBookService;

	/**
	 * 情報追加画面へ移動する
	 *
	 * @return String
	 */
	@GetMapping(ProjectURLConstants.URL_GET_CHAPTERS)
	@ResponseBody
	public @NotNull ResponseEntity<List<ChapterDto>> getChapters(@RequestParam final Short bookId) {
		final CoResult<List<ChapterDto>, DataAccessException> chaptersByBookId = this.iBookService
				.getChaptersByBookId(bookId);
		if (!chaptersByBookId.isOk()) {
			throw chaptersByBookId.getErr();
		}
		final List<ChapterDto> chapterDtos = chaptersByBookId.getData();
		return ResponseEntity.ok(chapterDtos);
	}

	/**
	 * 聖書節別情報を保存する
	 *
	 * @return String
	 */
	@PostMapping(ProjectURLConstants.URL_INFO_STORAGE)
	@ResponseBody
	public @NotNull ResponseEntity<String> infoStorage(@RequestBody final PhraseDto phraseDto) {
		final CoResult<String, DataAccessException> infoStorage = this.iBookService.infoStorage(phraseDto);
		if (!infoStorage.isOk()) {
			throw infoStorage.getErr();
		}
		return ResponseEntity.ok(infoStorage.getData());
	}

	/**
	 * 情報追加画面へ移動する
	 *
	 * @return String
	 */
	@GetMapping(ProjectURLConstants.URL_TO_ADDITION)
	public @NotNull ModelAndView toAddition() {
		final ModelAndView modelAndView = new ModelAndView("books-addition");
		final CoResult<List<BookDto>, DataAccessException> books = this.iBookService.getBooks();
		if (!books.isOk()) {
			throw books.getErr();
		}
		final CoResult<List<ChapterDto>, DataAccessException> chaptersByBookId = this.iBookService
				.getChaptersByBookId((short) 1);
		if (!chaptersByBookId.isOk()) {
			throw chaptersByBookId.getErr();
		}
		modelAndView.addObject("bookDtos", books.getData());
		modelAndView.addObject("chapterDtos", chaptersByBookId.getData());
		return modelAndView;
	}

}
