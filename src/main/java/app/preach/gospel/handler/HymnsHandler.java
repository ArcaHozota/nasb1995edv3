package app.preach.gospel.handler;

import java.io.Serial;
import java.util.List;

import org.apache.struts2.ActionContext;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.dispatcher.DefaultActionSupport;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.alibaba.fastjson2.JSON;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.HymnDto;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoProjectUtils;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.Pagination;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * 賛美歌管理ハンドラ
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@Setter
@Controller
@Scope("prototype")
public class HymnsHandler extends DefaultActionSupport implements ServletRequestAware {

	@Serial
	private static final long serialVersionUID = -6535194800678567557L;

	/**
	 * ID
	 */
	private String id;

	/**
	 * 賛美歌サービスインターフェス
	 */
	@Resource
	private IHymnService iHymnService;

	/**
	 * ビデオリンク
	 */
	private String link;

	/**
	 * 日本語名称
	 */
	private String nameJp;

	/**
	 * 韓国語名称
	 */
	private String nameKr;

	/**
	 * エラーリスポンス
	 */
	private transient String responseError;

	/**
	 * JSONリスポンス
	 */
	private transient Object responseJsonData;

	/**
	 * セリフ
	 */
	private String serif;

	/**
	 * リクエスト
	 */
	private transient HttpServletRequest servletRequest;

	/**
	 * 更新時間
	 */
	private String updatedTime;

	/**
	 * 更新者
	 */
	private String updatedUser;

	/**
	 * 歌の名称の重複性をチェックする
	 *
	 * @return String
	 */
	public String checkDuplicated() {
		final CoResult<Integer, DataAccessException> checkDuplicated = this.iHymnService.checkDuplicated(this.getId(),
				this.getNameJp());
		if (!checkDuplicated.isOk()) {
			throw checkDuplicated.getErr();
		}
		final Integer checkDuplicatedOk = checkDuplicated.getData();
		if (checkDuplicatedOk >= 1) {
			ActionContext.getContext().getServletResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			this.setResponseError(ProjectConstants.MESSAGE_HYMN_NAME_DUPLICATED);
			return ERROR;
		}
		this.setResponseJsonData(CoProjectUtils.EMPTY_STRING);
		return NONE;
	}

	/**
	 * 歌の名称の重複性をチェックする
	 *
	 * @return String
	 */
	public String checkDuplicated2() {
		final CoResult<Integer, DataAccessException> checkDuplicated = this.iHymnService.checkDuplicated2(this.getId(),
				this.getNameKr());
		if (!checkDuplicated.isOk()) {
			throw checkDuplicated.getErr();
		}
		final Integer checkDuplicatedOk = checkDuplicated.getData();
		if (checkDuplicatedOk >= 1) {
			ActionContext.getContext().getServletResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			this.setResponseError(ProjectConstants.MESSAGE_HYMN_NAME_DUPLICATED);
			return ERROR;
		}
		this.setResponseJsonData(CoProjectUtils.EMPTY_STRING);
		return NONE;
	}

	/**
	 * ランダム五つを検索する
	 *
	 * @return String
	 */
	public String commonRetrieve() {
		final String keyword = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_KEYWORD);
		final CoResult<List<HymnDto>, DataAccessException> hymnsRandomFive = this.iHymnService
				.getHymnsRandomFive(keyword);
		if (!hymnsRandomFive.isOk()) {
			throw hymnsRandomFive.getErr();
		}
		final List<HymnDto> hymnDtos = hymnsRandomFive.getData();
		this.setResponseJsonData(JSON.toJSON(hymnDtos));
		return NONE;
	}

	/**
	 * 削除権限チェック
	 *
	 * @return String
	 */
	public String deletionCheck() {
		return NONE;
	}

	/**
	 * 賛美歌情報転送クラス
	 */
	@Contract(" -> new")
	private @NotNull HymnDto getHymnDto() {
		return new HymnDto(this.getId(), this.getNameJp(), this.getNameKr(), this.getSerif(), this.getLink(), null,
				null, this.getUpdatedUser(), this.getUpdatedTime(), null);
	}

	/**
	 * 賛美歌情報を取得する
	 *
	 * @return String
	 */
	public String getInfoById() {
		final String hymnId = this.getServletRequest().getParameter("hymnId");
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService
				.getHymnInfoById(Long.valueOf(hymnId));
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		this.setResponseJsonData(JSON.toJSON(hymnInfoById.getData()));
		return NONE;
	}

	/**
	 * 歌のレコード数を取得する
	 *
	 * @return String
	 */
	public String getRecords() {
		final CoResult<Long, DataAccessException> totalCounts = this.iHymnService.getTotalCounts();
		if (!totalCounts.isOk()) {
			throw totalCounts.getErr();
		}
		this.setResponseJsonData(totalCounts.getData());
		return NONE;
	}

	/**
	 * 賛美歌情報を削除する
	 *
	 * @return String
	 */
	public String infoDeletion() {
		final String deleteId = this.getServletRequest().getParameter("deleteId");
		final CoResult<String, DataAccessException> infoDeletion = this.iHymnService
				.infoDeletion(Long.valueOf(deleteId));
		if (!infoDeletion.isOk()) {
			throw infoDeletion.getErr();
		}
		this.setResponseJsonData(infoDeletion.getData());
		return NONE;
	}

	/**
	 * 賛美歌情報を保存する
	 *
	 * @return String
	 */
	public String infoStorage() {
		final CoResult<Integer, DataAccessException> infoStorage = this.iHymnService.infoStorage(this.getHymnDto());
		if (!infoStorage.isOk()) {
			throw infoStorage.getErr();
		}
		final Integer pageNum = infoStorage.getData();
		this.setResponseJsonData(pageNum);
		return NONE;
	}

	/**
	 * 賛美歌情報を更新する
	 *
	 * @return String
	 */
	public String infoUpdation() {
		final CoResult<String, DataAccessException> infoUpdation = this.iHymnService.infoUpdation(this.getHymnDto());
		if (!infoUpdation.isOk()) {
			throw infoUpdation.getErr();
		}
		this.setResponseJsonData(infoUpdation.getData());
		return NONE;
	}

	/**
	 * 金海氏の検索を行う
	 *
	 * @return String
	 * @throws DataAccessException 例外
	 */
	public String kanumiRetrieve() {
		final String hymnId = this.getServletRequest().getParameter("hymnId");
		final CoResult<List<HymnDto>, DataAccessException> kanumiList = this.iHymnService
				.getKanumiList(Long.valueOf(hymnId));
		if (!kanumiList.isOk()) {
			throw kanumiList.getErr();
		}
		try {
			Thread.sleep(90000);
		} catch (InterruptedException e) {
			this.setResponseError(e.getMessage());
			return ERROR;
		}
		final List<HymnDto> hymnDtos = kanumiList.getData();
		this.setResponseJsonData(JSON.toJSON(hymnDtos));
		return NONE;
	}

	/**
	 * 情報一覧画面初期表示する
	 *
	 * @return String
	 */
	public String pagination() {
		final String pageNum = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_PAGE_NUMBER);
		final String keyword = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_KEYWORD);
		final CoResult<Pagination<HymnDto>, DataAccessException> hymnsByKeyword = this.iHymnService
				.getHymnsByKeyword(Integer.valueOf(pageNum), keyword);
		if (!hymnsByKeyword.isOk()) {
			throw hymnsByKeyword.getErr();
		}
		final Pagination<HymnDto> pagination = hymnsByKeyword.getData();
		this.setResponseJsonData(JSON.toJSON(pagination));
		return NONE;
	}

	/**
	 * 情報追加画面へ移動する
	 *
	 * @return String
	 */
	public String toAddition() {
		final String pageNum = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_PAGE_NUMBER);
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
		return SUCCESS;
	}

	/**
	 * 情報更新画面へ移動する
	 *
	 * @return String
	 */
	public String toEdition() {
		final String editId = this.getServletRequest().getParameter("editId");
		final String pageNum = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_PAGE_NUMBER);
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService
				.getHymnInfoById(Long.valueOf(editId));
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		final HymnDto hymnInfoByIdOk = hymnInfoById.getData();
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_EDITED_INFO, hymnInfoByIdOk);
		return SUCCESS;
	}

	/**
	 * 情報一覧画面へ移動する
	 *
	 * @return String
	 */
	public String toPages() {
		final String pageNum = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_PAGE_NUMBER);
		if (CoProjectUtils.isDigital(pageNum)) {
			ActionContext.getContext().put(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
			return SUCCESS;
		}
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_PAGE_NUMBER, "1");
		return SUCCESS;
	}

	/**
	 * ランダム五つ画面へ移動する
	 *
	 * @return String
	 */
	public String toRandomFive() {
		return SUCCESS;
	}

	@Override
	public void withServletRequest(final HttpServletRequest request) {
		this.servletRequest = request;
	}

}
