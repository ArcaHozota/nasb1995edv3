package app.preach.gospel.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 奉仕者情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@Setter
@NoArgsConstructor
public final class StudentDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 5227326799603233210L;

	/*
	 * 生年月日
	 */
	private String dateOfBirth;

	/*
	 * メール
	 */
	private String email;

	/*
	 * ID
	 */
	private String id;

	/*
	 * アカウント
	 */
	private String loginAccount;

	/*
	 * パスワード
	 */
	private String password;

	/*
	 * 役割ID
	 */
	private String roleId;

	/*
	 * ユーザ名称
	 */
	private String username;
}
