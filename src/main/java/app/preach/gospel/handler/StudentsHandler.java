package app.preach.gospel.handler;

import java.io.Serial;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ModelDriven;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.dispatcher.DefaultActionSupport;
import org.jooq.exception.DataAccessException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson2.JSON;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.StudentDto;
import app.preach.gospel.service.IStudentService;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.CoStringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * 奉仕者管理ハンドラ
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@Setter
@Controller
@Scope("prototype")
public class StudentsHandler extends DefaultActionSupport implements ModelDriven<StudentDto>, ServletRequestAware {

	@Serial
	private static final long serialVersionUID = 1592265866534993918L;

	/**
	 * 奉仕者サービスインターフェス
	 */
	@Resource
	private IStudentService iStudentService;

	/**
	 * エラーリスポンス
	 */
	private transient String responseError;

	/**
	 * JSONリスポンス
	 */
	private transient Object responseJsonData;

	/**
	 * リクエスト
	 */
	private transient HttpServletRequest servletRequest;

	/**
	 * 奉仕者情報転送クラス
	 */
	private StudentDto studentDto = new StudentDto();

	/**
	 * アカウント重複チェック
	 *
	 * @return String
	 */
	public String checkDuplicated() {
		final CoResult<Integer, DataAccessException> checkDuplicated = this.iStudentService
				.checkDuplicated(this.getModel().getId(), this.getModel().getLoginAccount());
		if (!checkDuplicated.isOk()) {
			throw checkDuplicated.getErr();
		}
		final Integer checkDuplicatedOk = checkDuplicated.getData();
		if (checkDuplicatedOk >= 1) {
			ActionContext.getContext().getServletResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			this.setResponseError(ProjectConstants.MESSAGE_STUDENT_NAME_DUPLICATED);
			return ERROR;
		}
		this.setResponseJsonData(CoStringUtils.EMPTY_STRING);
		return NONE;
	}

	@Override
	public StudentDto getModel() {
		return this.getStudentDto();
	}

	/**
	 * 奉仕者情報を更新する
	 *
	 * @return String
	 */
	public String infoUpdation() {
		final CoResult<String, DataAccessException> infoUpdation = this.iStudentService.infoUpdation(this.getModel());
		if (!infoUpdation.isOk()) {
			throw infoUpdation.getErr();
		}
		this.setResponseJsonData(infoUpdation.getData());
		return NONE;
	}

	/**
	 * ユーザ情報初期化する
	 *
	 * @return String
	 */
	public String initial() {
		final String studentId = this.getServletRequest().getParameter("editId");
		final CoResult<StudentDto, DataAccessException> studentInfoById = this.iStudentService
				.getStudentInfoById(Long.valueOf(studentId));
		if (!studentInfoById.isOk()) {
			throw studentInfoById.getErr();
		}
		this.setResponseJsonData(JSON.toJSON(studentInfoById.getData()));
		return NONE;
	}

	/**
	 * ログイン時間記録
	 *
	 * @return String
	 */
	public String preLogin() {
		final CoResult<String, DataAccessException> preLoginUpdation = this.iStudentService
				.preLoginUpdate(this.getModel().getLoginAccount(), this.getModel().getPassword());
		if (!preLoginUpdation.isOk()) {
			throw preLoginUpdation.getErr();
		}
		this.setResponseJsonData(preLoginUpdation.getData());
		return NONE;
	}

	/**
	 * 情報更新画面へ移動する
	 *
	 * @return String
	 */
	public String toEdition() {
		final String editId = this.getServletRequest().getParameter("userId");
		final CoResult<StudentDto, DataAccessException> studentInfoById = this.iStudentService
				.getStudentInfoById(Long.valueOf(editId));
		if (!studentInfoById.isOk()) {
			throw studentInfoById.getErr();
		}
		final StudentDto studentDto = studentInfoById.getData();
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_EDITED_INFO, studentDto);
		return SUCCESS;
	}

	@Override
	public void withServletRequest(final HttpServletRequest request) {
		this.servletRequest = request;
	}

}
