package app.preach.gospel.dto;

/**
 * 学生情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public record StudentDto(

		/*
		 * ID
		 */
		Long id,

		/*
		 * アカウント
		 */
		String loginAccount,

		/*
		 * ユーザ名称
		 */
		String username,

		/*
		 * パスワード
		 */
		String password,

		/*
		 * メール
		 */
		String email,

		/*
		 * 生年月日
		 */
		String dateOfBirth,

		/*
		 * 役割ID
		 */
		Long roleId) {
}
