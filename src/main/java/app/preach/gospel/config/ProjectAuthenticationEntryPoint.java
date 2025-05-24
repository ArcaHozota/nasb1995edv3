package app.preach.gospel.config;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.common.ProjectURLConstants;
import app.preach.gospel.utils.CoProjectUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

/**
 * ログインエラーコントローラ
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Log4j2
@Component
public class ProjectAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(final HttpServletRequest request, final @NotNull HttpServletResponse response,
			final AuthenticationException authException) throws IOException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		response.sendRedirect(ProjectURLConstants.URL_CATEGORY_NAMESPACE.concat(CoProjectUtils.SLASH)
				.concat(ProjectURLConstants.URL_TO_LOGIN_WITH_ERROR));
		log.warn(ProjectConstants.MESSAGE_STRING_NOT_LOGIN);
	}

}
