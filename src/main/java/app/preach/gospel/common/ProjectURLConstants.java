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

	public static final String URL_CHECK_EDITION = "edition-check";

	public static final String URL_CHECK_DELETE = "deletion-check";

	public static final String URL_TO_LOGIN = "login";

	public static final String URL_TO_LOGIN_WITH_ERROR = "login-with-error";

	public static final String URL_PRE_LOGIN = "pre-login";

	public static final String URL_LOGOUT = "do-logout";

	public static final String URL_LOGIN = "do-login";

	public static final String URL_REGISTER = "toroku";

	public static final String URL_TO_ERROR = "to-system-error";

	public static final String URL_TO_MAINMENU_WITH_LOGIN = "to-mainmenu-with-login";

	public static final String URL_TO_ADDITION = "to-addition";

	public static final String URL_TO_EDITION = "to-edition";

	public static final String URL_STATIC_RESOURCE = "/static/**";
}
