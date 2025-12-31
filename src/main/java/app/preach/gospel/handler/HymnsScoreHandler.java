package app.preach.gospel.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serial;

import org.apache.struts2.ActionContext;
import org.apache.struts2.ModelDriven;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.dispatcher.DefaultActionSupport;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.NoDataFoundException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.HymnDto;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.CoStringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * 賛美歌楽譜管理ハンドラ
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@Setter
@Controller
@Scope("prototype")
public class HymnsScoreHandler extends DefaultActionSupport implements ModelDriven<HymnDto>, ServletRequestAware {

	@Serial
	private static final long serialVersionUID = 4949258675703419344L;

	/**
	 * 内容タイプ
	 */
	private String contentType;

	/**
	 * データ
	 */
	private byte[] fileData;

	/**
	 * ファイル名称
	 */
	private String fileName;

	/**
	 * 賛美歌サービスインターフェス
	 */
	@Resource
	private IHymnService iHymnService;

	/**
	 * 賛美歌情報転送クラス
	 */
	private transient HymnDto model = new HymnDto();

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
	 * 賛美歌楽譜の情報を保存する
	 *
	 * @return String
	 */
	@Override
	public String execute() {
		// // 获取 JSON 数据
		// final ObjectMapper mapper = new ObjectMapper();
		// @SuppressWarnings("unchecked")
		// final Object2ObjectOpenHashMap<String, String> data =
		// mapper.readValue(this.getServletRequest().getReader(),
		// Object2ObjectOpenHashMap.class);
		// // 获取参数
		// final String editId = data.get("id");
		// final String fileDataStr = data.get("score");
		// // 将 base64 文件数据解码并保存
		// final byte[] fileBytes = Base64.getDecoder().decode(fileDataStr);
		final CoResult<String, DataAccessException> scoreStorage = this.iHymnService
				.scoreStorage(this.getModel().getScore(), Long.valueOf(this.getModel().getId()));
		if (!scoreStorage.isOk()) {
			throw scoreStorage.getErr();
		}
		this.setResponseJsonData(scoreStorage.getData());
		return NONE;
	}

	/**
	 * ストリームを提供する
	 *
	 * @return InputStream
	 */
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.getFileData());
	}

	@Override
	public HymnDto getModel() {
		return this.model;
	}

	/**
	 * 賛美歌楽譜をダウンロードする
	 *
	 * @return String
	 */
	public String scoreDownload() {
		final Long scoreId = Long.valueOf(this.getModel().getId());
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService.getHymnInfoById(scoreId);
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		final HymnDto hymnDto = hymnInfoById.getData();
		final String biko = hymnDto.getBiko();
		if (CoStringUtils.isEmpty(biko)) {
			throw new NoDataFoundException(ProjectConstants.MESSAGE_STRING_FATAL_ERROR);
		}
		final int indexOf = biko.indexOf(CoStringUtils.SLASH) + 1;
		this.setContentType(biko);
		this.setFileName(hymnDto.getId() + CoStringUtils.DOT.concat(biko.substring(indexOf)));
		this.setFileData(hymnDto.getScore());
		return SUCCESS;
	}

	/**
	 * 楽譜アプロード画面へ移動する
	 *
	 * @return String
	 */
	public String toScoreUpload() {
		final String scoreId = this.getServletRequest().getParameter("scoreId");
		final String pageNum = this.getServletRequest().getParameter(ProjectConstants.ATTRNAME_PAGE_NUMBER);
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_PAGE_NUMBER, pageNum);
		final CoResult<HymnDto, DataAccessException> hymnInfoById = this.iHymnService
				.getHymnInfoById(Long.valueOf(scoreId));
		if (!hymnInfoById.isOk()) {
			throw hymnInfoById.getErr();
		}
		final HymnDto hymnDto = hymnInfoById.getData();
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_EDITED_INFO, hymnDto);
		return SUCCESS;
	}

	@Override
	public void withServletRequest(final HttpServletRequest request) {
		this.servletRequest = request;
	}

}
