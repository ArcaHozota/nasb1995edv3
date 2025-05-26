package app.preach.gospel.handler;

import java.io.Serial;

import org.apache.struts2.ActionContext;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.dispatcher.DefaultActionSupport;
import org.jooq.exception.DataAccessException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * ホームページハンドラ
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@Setter
@Controller
@Scope("prototype")
public class HomePageHandler extends DefaultActionSupport implements ServletRequestAware {

	@Serial
	private static final long serialVersionUID = -171237033831060185L;

	/**
	 * 賛美歌サービスインターフェス
	 */
	@Resource
	private IHymnService iHymnService;

	/**
	 * JSONリスポンス
	 */
	private transient Object responseJsonData;

	/**
	 * リクエスト
	 */
	private transient HttpServletRequest servletRequest;

	/**
	 * インデクスへ移動する
	 *
	 * @return String
	 */
	public String toHomePage() {
		final CoResult<Long, DataAccessException> totalCounts = this.iHymnService.getTotalCounts();
		if (!totalCounts.isOk()) {
			throw totalCounts.getErr();
		}
		ActionContext.getContext().put(ProjectConstants.ATTRNAME_RECORDS, totalCounts.getData());
		return SUCCESS;
	}

	/**
	 * 一覧表へ移動する
	 *
	 * @return String
	 */
	public String toIchiranhyo() {
		return toHomePage();
	}

	@Override
	public void withServletRequest(final HttpServletRequest request) {
		this.servletRequest = request;
	}

}
