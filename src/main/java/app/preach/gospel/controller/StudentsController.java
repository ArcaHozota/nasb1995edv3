package app.preach.gospel.controller;

import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.dto.StudentDto;
import app.preach.gospel.service.IStudentService;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.CoProjectUtils;
import jakarta.annotation.Resource;

/**
 * 奉仕者管理コントローラ
 *
 * @author ArkamaHozota
 * @since 1.10
 */
@Controller
@RequestMapping(ProjectURLConstants.URL_STUDENTS_NAMESPACE)
public final class StudentsController {

	/**
	 * 奉仕者サービスインターフェス
	 */
	@Resource
	private IStudentService iStudentService;

	/**
	 * アカウント重複チェック
	 *
	 * @param id           ID
	 * @param loginAccount アカウント
	 * @return ResultDto<String>
	 */
	@GetMapping(ProjectURLConstants.URL_CHECK_NAME)
	@ResponseBody
	public ResponseEntity<String> checkDuplicated(@RequestParam final String id,
			@RequestParam final String loginAccount) {
		final CoResult<Integer, DataAccessException> checkDuplicated = this.iStudentService.checkDuplicated(id,
				loginAccount);
		if (!checkDuplicated.isOk()) {
			throw checkDuplicated.getErr();
		}
		final Integer checkDuplicatedOk = checkDuplicated.getData();
		if (checkDuplicatedOk >= 1) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ProjectConstants.MESSAGE_STUDENT_NAME_DUPLICATED);
		}
		return ResponseEntity.ok(CoProjectUtils.EMPTY_STRING);
	}

	/**
	 * 奉仕者情報を更新する
	 *
	 * @param studentDto 情報転送クラス
	 * @return ResultDto<String>
	 */
	@PutMapping(ProjectURLConstants.URL_INFO_UPDATION)
	@ResponseBody
	public @NotNull ResponseEntity<String> infoUpdation(@RequestBody final StudentDto studentDto) {
		final CoResult<String, DataAccessException> infoUpdation = this.iStudentService.infoUpdation(studentDto);
		if (!infoUpdation.isOk()) {
			throw infoUpdation.getErr();
		}
		return ResponseEntity.ok(infoUpdation.getData());
	}

	/**
	 * ログイン時間記録
	 *
	 * @param loginAccount アカウント
	 * @param password     パスワード
	 * @return ResultDto<String>
	 */
	@GetMapping(ProjectURLConstants.URL_PRE_LOGIN)
	@ResponseBody
	public @NotNull ResponseEntity<String> preLogin(@RequestParam final String loginAccount,
			@RequestParam final String password) {
		final CoResult<String, DataAccessException> preLoginUpdation = this.iStudentService
				.preLoginUpdation(loginAccount, password);
		if (!preLoginUpdation.isOk()) {
			throw preLoginUpdation.getErr();
		}
		return ResponseEntity.ok(preLoginUpdation.getData());
	}

	/**
	 * 情報更新画面へ移動する
	 *
	 * @param editId 編集ID
	 * @return ModelAndView
	 */
	@GetMapping(ProjectURLConstants.URL_TO_EDITION)
	public @NotNull ModelAndView toEdition(@RequestParam final Long editId) {
		final ModelAndView modelAndView = new ModelAndView("students-edition");
		final CoResult<StudentDto, DataAccessException> studentInfoById = this.iStudentService
				.getStudentInfoById(editId);
		if (!studentInfoById.isOk()) {
			throw studentInfoById.getErr();
		}
		final StudentDto studentDto = studentInfoById.getData();
		modelAndView.addObject(ProjectConstants.ATTRNAME_EDITED_INFO, studentDto);
		return modelAndView;
	}

}
