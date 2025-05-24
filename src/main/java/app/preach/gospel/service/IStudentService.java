package app.preach.gospel.service;

import org.jooq.exception.DataAccessException;

import app.preach.gospel.dto.StudentDto;
import app.preach.gospel.utils.CoResult;

/**
 * 奉仕者サービスインターフェス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public interface IStudentService {

	/**
	 * アカウントの重複性をチェックする
	 *
	 * @param id           ID
	 * @param loginAccount アカウント
	 * @return CoResult<Integer, DataAccessException>
	 */
	CoResult<Integer, DataAccessException> checkDuplicated(String id, String loginAccount);

	/**
	 * IDによって奉仕者の情報を取得する
	 *
	 * @param id ID
	 * @return CoResult<StudentDto, DataAccessException>
	 */
	CoResult<StudentDto, DataAccessException> getStudentInfoById(Long id);

	/**
	 * 奉仕者情報を更新する
	 *
	 * @param studentDto 奉仕者情報転送クラス
	 * @return CoResult<String, DataAccessException>
	 */
	CoResult<String, DataAccessException> infoUpdation(StudentDto studentDto);

	/**
	 * ログイン時間記録
	 *
	 * @param loginAccount アカウント
	 * @param password     パスワード
	 * @return CoResult<String, DataAccessException>
	 */
	CoResult<String, DataAccessException> preLoginUpdation(String loginAccount, String password);
}
