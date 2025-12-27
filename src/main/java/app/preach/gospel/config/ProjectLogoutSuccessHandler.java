package app.preach.gospel.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;

import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.utils.CoStringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

/**
 * ログアウトコントローラ
 *
 * @author ArkamaHozota
 * @since 2.33
 */
@Log4j2
@Component
public class ProjectLogoutSuccessHandler implements LogoutSuccessHandler {

	/**
	 * キャッシュ
	 */
	private final Cache<Object, Object> nlpCache;

	/**
	 * コンストラクタ
	 *
	 * @param nlpCache キャッシュ
	 */
	public ProjectLogoutSuccessHandler(@Qualifier("nlpCache") final Cache<Object, Object> nlpCache) {
		this.nlpCache = nlpCache;
	}

	@Override
	public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException {
		// 🔥 清空缓存
		this.nlpCache.invalidateAll();
		// 如有需要可打印统计信息
		log.info("Caffeine stats: {}", this.nlpCache.stats());
		// 重定向到登录页
		response.sendRedirect(ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(CoStringUtils.SLASH)
				.concat(ProjectURLConstants.URL_TO_LOGIN));
	}

}
