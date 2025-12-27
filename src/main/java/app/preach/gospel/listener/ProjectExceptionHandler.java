package app.preach.gospel.listener;

import org.apache.struts2.dispatcher.DefaultDispatcherErrorHandler;
import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import com.alibaba.fastjson2.JSON;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.utils.CoStringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

/**
 * Struts2例外処理インターセプト
 *
 * @author ArkamaHozota
 * @since 2.98
 */
@Log4j2
public final class ProjectExceptionHandler extends DefaultDispatcherErrorHandler {

	@Override
	protected void sendErrorResponse(final HttpServletRequest request, final @NotNull HttpServletResponse response,
			final int code, final Exception exception) {
		try {
			// エラーメッセージ
			final var errorMessage = CoStringUtils.isNotEmpty(exception.getMessage()) ? exception.getMessage()
					: ProjectConstants.MESSAGE_STRING_FATAL_ERROR;
			log.error("Exception occurred during processing request: {}", errorMessage);
			// WW-1977: Only put errors in the request when code is a 500 error
			if (exception instanceof AccessDeniedException) {
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding(CoStringUtils.CHARSET_UTF8.name());
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.getWriter().print(JSON.toJSON(ProjectConstants.MESSAGE_SPRINGSECURITY_REQUIRED_AUTH));
				response.getWriter().close();
			} else if (exception instanceof DataAccessException) {
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding(CoStringUtils.CHARSET_UTF8.name());
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(JSON.toJSON(errorMessage));
				response.getWriter().close();
			} else {
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding(CoStringUtils.CHARSET_UTF8.name());
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().print(JSON.toJSON(errorMessage));
				response.getWriter().close();
				for (final var en : exception.getStackTrace()) {
					log.error(en.toString());
				}
			}
		} catch (final Exception e) {
			// Log illegal state instead of passing unrecoverable exception to calling
			// thread
			log.warn("Unable to send error response, code: {}; isCommited: {};", code, response.isCommitted(), e);
			for (final var en : e.getStackTrace()) {
				log.error(en.toString());
			}
		}
	}

}
