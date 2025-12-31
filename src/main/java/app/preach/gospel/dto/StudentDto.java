package app.preach.gospel.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 奉仕者情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Data
@EqualsAndHashCode(callSuper = false)
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
