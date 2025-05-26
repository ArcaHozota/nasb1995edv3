package app.preach.gospel.listener;

import org.jetbrains.annotations.NotNull;
import org.jooq.exception.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 共通エラー処理コントローラ
 *
 * @author ArkamaHozota
 * @since 4.00
 */
@ControllerAdvice
public class ProjectExceptionListener {

	/**
	 * データ永続化例外処理
	 *
	 * @param ex エラー
	 * @return ResponseEntity<String>
	 */
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<String> handleDataAccessException(final @NotNull DataAccessException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

}
