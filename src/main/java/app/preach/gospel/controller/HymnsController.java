package app.preach.gospel.controller;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.dto.HymnDto;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoProjectUtils;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.Pagination;
import jakarta.annotation.Resource;

/**
 * 賛美歌管理コントローラ
 *
 * @author ArkamaHozota
 * @since 1.10
 */
@Controller
@RequestMapping(ProjectURLConstants.URL_HYMNS_NAMESPACE)
public final class HymnsController {

	/**
	 * 賛美歌サービスインターフェス
	 */
	@Resource
	private IHymnService iHymnService;

	/**
	 * 名称重複チェック
	 *
	 * @param id     ID
	 * @param nameJp 日本語名称
	 * @return ResponseEntity<String>
	 */
	@GetMapping(ProjectURLConstants.URL_CHECK_NAME)
	@ResponseBody
	public ResponseEntity<String> checkDuplicated(@RequestParam final String id, @RequestParam final String nameJp) {
		final CoResult<Integer, DataAccessException> checkDuplicated = this.iHymnService.checkDuplicated(id, nameJp);
		if (!checkDuplicated.isOk()) {
			throw checkDuplicated.getErr();
		}
		final Integer checkDuplicatedOk = checkDuplicated.getOk();
		if (checkDuplicatedOk >= 1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProjectConstants.MESSAGE_HYMN_NAME_DUPLICATED);
		}
		return ResponseEntity.ok(CoProjectUtils.EMPTY_STRING);
	}

	/**
	 * 名称重複チェック2
	 *
	 * @param id     ID
	 * @param nameKr 韓国語名称
	 * @return ResponseEntity<String>
	 */
	@GetMapping(ProjectURLConstants.URL_CHECK_NAME2)
	@ResponseBody
	public ResponseEntity<String> checkDuplicated2(@RequestParam final String id, @RequestParam final String nameKr) {
		final CoResult<Integer, DataAccessException> checkDuplicated = this.iHymnService.checkDuplicated2(id, nameKr);
		if (!checkDuplicated.isOk()) {
			throw checkDuplicated.getErr();
		}
		final Integer checkDuplicatedOk = checkDuplicated.getOk();
		if (checkDuplicatedOk >= 1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProjectConstants.MESSAGE_HYMN_NAME_DUPLICATED);
		}
		return ResponseEntity.ok(CoProjectUtils.EMPTY_STRING);
	}

	/**
	 * ランダム五つを検索する
	 *
	 * @param keyword キーワード
	 * @return ResponseEntity<List<HymnDto>>
	 */
	@GetMapping(value = { ProjectURLConstants.URL_COMMON_RETRIEVE, ProjectURLConstants.URL_RANDOM_FIVE_RETRIEVE })
	@ResponseBody
	public @NotNull ResponseEntity<List<HymnDto>> commonRetrieve(@RequestParam final String keyword) {
		final CoResult<List<HymnDto>, DataAccessException> hymnsRandomFive = this.iHymnService
				.getHymnsRandomFive(keyword);
		if (!hymnsRandomFive.isOk()) {
			throw hymnsRandomFive.getErr();
		}
		final List<HymnDto> hymnDtos = hymnsRandomFive.getOk();
		return ResponseEntity.ok(hymnDtos);
	}

	/**
	 * 削除権限チェック
	 *
	 * @return ResponseEntity<String>
	 */
	@GetMapping(ProjectURLConstants.URL_CHECK_DELETE)
	@ResponseBody
	public @NotNull ResponseEntity<String> deletionCheck() {
		return ResponseEntity.ok(CoProjectUtils.EMPTY_STRING);
	}

	/**
	 * IDによって賛美歌情報を検索する
	 *
	 * @param hymnId ID
	 * @return ResponseEntity<HymnDto>
	 */
	@GetMapping(ProjectURLConstants.URL_GET_INFO)
	@ResponseBody
	public ResponseEntity<HymnDto> getInfoById(@RequestParam final Long hymnId) {
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService.getHymnInfoById(hymnId);
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		return ResponseEntity.ok(hymnInfoById.getOk());
	}

	/**
	 * 賛美歌情報を削除する
	 *
	 * @param deleteId 編集ID
	 * @return ResponseEntity<String>
	 */
	@DeleteMapping(ProjectURLConstants.URL_INFO_DELETION)
	@ResponseBody
	public @NotNull ResponseEntity<String> infoDeletion(@RequestParam final Long deleteId) {
		final CoResult<String, DataAccessException> infoDeletion = this.iHymnService.infoDeletion(deleteId);
		if (!infoDeletion.isOk()) {
			throw infoDeletion.getErr();
		}
		return ResponseEntity.ok(infoDeletion.getOk());
	}

	/**
	 * 賛美歌情報を保存する
	 *
	 * @param hymnDto 情報転送クラス
	 * @return ResponseEntity<Integer>
	 */
	@PostMapping(ProjectURLConstants.URL_INFO_STORAGE)
	@ResponseBody
	public @NotNull ResponseEntity<Integer> infoStorage(@RequestBody final HymnDto hymnDto) {
		final CoResult<Integer, DataAccessException> infoStorage = this.iHymnService.infoStorage(hymnDto);
		if (!infoStorage.isOk()) {
			throw infoStorage.getErr();
		}
		return ResponseEntity.ok(infoStorage.getOk());
	}

	/**
	 * 賛美歌情報を更新する
	 *
	 * @param hymnDto 情報転送クラス
	 * @return ResponseEntity<String>
	 */
	@PutMapping(ProjectURLConstants.URL_INFO_UPDATION)
	@ResponseBody
	public @NotNull ResponseEntity<String> infoUpdation(@RequestBody final HymnDto hymnDto) {
		final CoResult<String, DataAccessException> infoUpdation = this.iHymnService.infoUpdation(hymnDto);
		if (!infoUpdation.isOk()) {
			throw infoUpdation.getErr();
		}
		return ResponseEntity.ok(infoUpdation.getOk());
	}

	/**
	 * 金海氏の検索を行う
	 *
	 * @param hymnId ID
	 * @return ResponseEntity<List<HymnDto>>
	 */
	@GetMapping(ProjectURLConstants.URL_KANUMI_RETRIEVE)
	@ResponseBody
	public @NotNull ResponseEntity<List<HymnDto>> kanumiRetrieve(@RequestParam final Long hymnId) {
		final CoResult<List<HymnDto>, DataAccessException> kanumiList = this.iHymnService.getKanumiList(hymnId);
		if (!kanumiList.isOk()) {
			throw kanumiList.getErr();
		}
		return ResponseEntity.ok(kanumiList.getOk());
	}

	/**
	 * 情報一覧画面初期表示する
	 *
	 * @param pageNum ページナンバー
	 * @param keyword キーワード
	 * @return ResponseEntity<Pagination<HymnDto>>
	 */
	@GetMapping(ProjectURLConstants.URL_PAGINATION)
	@ResponseBody
	public @NotNull ResponseEntity<Pagination<HymnDto>> pagination(@RequestParam final Integer pageNum,
			@RequestParam(required = false, defaultValue = CoProjectUtils.EMPTY_STRING) final String keyword) {
		final CoResult<Pagination<HymnDto>, DataAccessException> hymnsByKeyword = this.iHymnService
				.getHymnsByKeyword(pageNum, keyword);
		if (!hymnsByKeyword.isOk()) {
			throw hymnsByKeyword.getErr();
		}
		final Pagination<HymnDto> pagination = hymnsByKeyword.getOk();
		return ResponseEntity.ok(pagination);
	}

	/**
	 * 賛美歌楽譜をダウンロードする
	 *
	 * @param scoreId 賛美歌ID
	 * @return ResponseEntity<byte[]>
	 */
	@GetMapping(ProjectURLConstants.URL_SCORE_DOWNLOAD)
	@ResponseBody
	public @NotNull ResponseEntity<byte[]> scoreDownload(@RequestParam final Long scoreId) {
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService.getHymnInfoById(scoreId);
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		final HymnDto hymnDto = hymnInfoById.getOk();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData("attachment", hymnDto.id() + ".pdf");
		return ResponseEntity.ok().headers(headers).body(hymnDto.score());
	}

	/**
	 * 楽譜の情報を保存する
	 *
	 * @param hymnDto 情報転送クラス
	 * @return ResponseEntity<String>
	 */
	@PostMapping(ProjectURLConstants.URL_SCORE_UPLOAD)
	@ResponseBody
	public @NotNull ResponseEntity<String> scoreUpload(@RequestBody final HymnDto hymnDto) {
		final CoResult<String, DataAccessException> scoreStorage = this.iHymnService.scoreStorage(hymnDto);
		if (!scoreStorage.isOk()) {
			throw scoreStorage.getErr();
		}
		return ResponseEntity.ok(scoreStorage.getOk());
	}

	/**
	 * 情報追加画面へ移動する
	 *
	 * @param pageNum ページナンバー
	 * @return ModelAndView
	 */
	@GetMapping(ProjectURLConstants.URL_TO_ADDITION)
	public @NotNull ModelAndView toAddition(@RequestParam final String pageNum) {
		final ModelAndView modelAndView = new ModelAndView("hymns-addition");
		modelAndView.addObject(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
		return modelAndView;
	}

	/**
	 * 情報更新画面へ移動する
	 *
	 * @param editId  編集ID
	 * @param pageNum ページナンバー
	 * @return ModelAndView
	 */
	@GetMapping(ProjectURLConstants.URL_TO_EDITION)
	public @NotNull ModelAndView toEdition(@RequestParam final Long editId, @RequestParam final Integer pageNum) {
		final ModelAndView modelAndView = new ModelAndView("hymns-edition");
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService.getHymnInfoById(editId);
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		final HymnDto hymnDto = hymnInfoById.getOk();
		modelAndView.addObject(ProjectConstants.ATTRNAME_EDITED_INFO, hymnDto);
		modelAndView.addObject(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
		return modelAndView;
	}

	/**
	 * 情報一覧画面へ移動する
	 *
	 * @param pageNum ページナンバー
	 * @return ModelAndView
	 */
	@GetMapping(ProjectURLConstants.URL_TO_PAGES)
	public @NotNull ModelAndView toPages(@RequestParam final String pageNum) {
		final ModelAndView modelAndView = new ModelAndView("hymns-pagination");
		if (CoProjectUtils.isDigital(pageNum)) {
			modelAndView.addObject(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
			return modelAndView;
		}
		modelAndView.addObject(ProjectConstants.ATTRNAME_PAGE_NUMBER, "1");
		return modelAndView;
	}

	/**
	 * 楽譜アプロード画面へ移動する
	 *
	 * @param pageNum ページナンバー
	 * @return ModelAndView
	 */
	@GetMapping(ProjectURLConstants.URL_TO_SCORE_UPLOAD)
	public @NotNull ModelAndView toScoreUpload(@RequestParam final Long scoreId, @RequestParam final Integer pageNum) {
		final ModelAndView modelAndView = new ModelAndView("hymns-score-upload");
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService.getHymnInfoById(scoreId);
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		final HymnDto hymnDto = hymnInfoById.getOk();
		modelAndView.addObject(ProjectConstants.ATTRNAME_EDITED_INFO, hymnDto);
		modelAndView.addObject(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
		return modelAndView;
	}

}
