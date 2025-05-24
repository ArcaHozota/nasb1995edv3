package app.preach.gospel.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * プロジェクトURLコンスタント
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectURLConstants {

	public static final String URL_CATEGORY_NAMESPACE = "/category";

	public static final String URL_BOOKS_NAMESPACE = "/books";

	public static final String URL_HYMNS_NAMESPACE = "/hymns";

	public static final String URL_STUDENTS_NAMESPACE = "/students";

	public static final String URL_INITIAL_TEMPLATE = "/initial";

	public static final String URL_CHECK_NAME = "/checkDuplicated";

	public static final String URL_CHECK_NAME2 = "/checkDuplicated2";

	public static final String URL_CHECK_EDITION = "/editionCheck";

	public static final String URL_CHECK_DELETE = "/deletionCheck";

	public static final String URL_TO_LOGIN = "/login";

	public static final String URL_TO_LOGIN_WITH_ERROR = "/loginWithError";

	public static final String URL_LOG_OUT = "/logout";

	public static final String URL_PRE_LOGIN = "/preLogin";

	public static final String URL_LOGIN = "/doLogin";

	public static final String URL_REGISTER = "/toroku";

	public static final String URL_TO_ERROR = "/toSystemError";

	public static final String URL_TO_MAINMENU = "/toMainmenu";

	public static final String URL_TO_MAINMENU_WITH_LOGIN = "/toMainmenuWithLogin";

	public static final String URL_TO_ADDITION = "/toAddition";

	public static final String URL_TO_EDITION = "/toEdition";

	public static final String URL_GET_INFO = "/getInfoById";

	public static final String URL_TO_PAGES = "/toPages";

	public static final String URL_PAGINATION = "/pagination";

	public static final String URL_INFO_DELETION = "/infoDeletion";

	public static final String URL_INFO_STORAGE = "/infoStorage";

	public static final String URL_INFO_UPDATION = "/infoUpdation";

	public static final String URL_MENU_INITIAL = "/menuInitial";

	public static final String URL_GET_CHAPTERS = "/getChapters";

	public static final String URL_TO_RANDOM_FIVE = "/toRandomFive";

	public static final String URL_RANDOM_FIVE_RETRIEVE = "/retrieveRandomFive";

	public static final String URL_COMMON_RETRIEVE = "/commonRetrieve";

	public static final String URL_KANUMI_RETRIEVE = "/kanumiRetrieve";

	public static final String URL_TO_SCORE_UPLOAD = "/toScoreUpload";

	public static final String URL_SCORE_UPLOAD = "/scoreUpload";

	public static final String URL_SCORE_DOWNLOAD = "/scoreDownload";

	public static final String URL_STATIC_RESOURCE = "/static/**";

	public static final String URL_HOMEPAGE1 = "/home";

	public static final String URL_HOMEPAGE2 = "/homePage";

	public static final String URL_HOMEPAGE3 = "/toHomePage";

	public static final String URL_HOMEPAGE4 = "/";

	public static final String URL_HOMEPAGE5 = "/index";

	public static final String URL_LEDGER = "/toIchiranhyo";
}
