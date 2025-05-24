package app.preach.gospel.controller;

import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.service.IHymnService;
import app.preach.gospel.utils.CoResult;
import jakarta.annotation.Resource;

/**
 * ホームページコントローラ
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Controller
public final class HomePageController {

	/**
	 * 賛美歌サービスインターフェス
	 */
	@Resource
	private IHymnService iHymnService;

	/**
	 * 賛美歌一覧表画面へ移動する
	 *
	 * @return ModelAndView
	 */
	@GetMapping(ProjectURLConstants.URL_LEDGER)
	public @NotNull ModelAndView toIchiranhyo() {
		final ModelAndView modelAndView = new ModelAndView("index2");
		final CoResult<Long, DataAccessException> totalRecords = this.iHymnService.getTotalRecords();
		if (!totalRecords.isOk()) {
			throw totalRecords.getErr();
		}
		modelAndView.addObject(ProjectConstants.ATTRNAME_RECORDS, totalRecords);
		return modelAndView;
	}

	/**
	 * ホームページへ移動する
	 *
	 * @return ModelAndView
	 */
	@GetMapping(value = { ProjectURLConstants.URL_HOMEPAGE1, ProjectURLConstants.URL_HOMEPAGE2,
			ProjectURLConstants.URL_HOMEPAGE3, ProjectURLConstants.URL_HOMEPAGE4, ProjectURLConstants.URL_HOMEPAGE5 })
	public @NotNull ModelAndView toIndex() {
		final ModelAndView modelAndView = new ModelAndView("index");
		final CoResult<Long, DataAccessException> totalRecords = this.iHymnService.getTotalRecords();
		if (!totalRecords.isOk()) {
			throw totalRecords.getErr();
		}
		modelAndView.addObject(ProjectConstants.ATTRNAME_RECORDS, totalRecords);
		return modelAndView;
	}

	/**
	 * ログインページへ移動する
	 *
	 * @return ModelAndView
	 */
	@GetMapping("/category/loginWithError")
	public @NotNull ModelAndView toLoginPage() {
		final ModelAndView modelAndView = new ModelAndView("logintoroku");
		modelAndView.addObject("torokuMsg", ProjectConstants.MESSAGE_STRING_NOT_LOGIN);
		return modelAndView;
	}

	/**
	 * メインメニュへ移動する
	 *
	 * @return ModelAndView
	 */
	@GetMapping("/category/toMainmenuWithLogin")
	public @NotNull ModelAndView toMainmenuWithLogin() {
		final ModelAndView modelAndView = new ModelAndView("mainmenu");
		modelAndView.addObject("loginMsg", ProjectConstants.MESSAGE_STRING_LOGIN_SUCCESS);
		return modelAndView;
	}

}
