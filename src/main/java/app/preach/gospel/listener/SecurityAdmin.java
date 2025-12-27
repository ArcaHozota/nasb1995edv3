package app.preach.gospel.listener;

import java.io.Serial;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import app.preach.gospel.dto.StudentDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * User拡張クラス(SpringSecurity関連)
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public final class SecurityAdmin extends User {

	@Serial
	private static final long serialVersionUID = 3827955098466369880L;

	/**
	 * 社員管理DTO -- GETTER -- getter for originalAdmin
	 */
	private final transient StudentDto originalAdmin;

	/**
	 * コンストラクタ
	 *
	 * @param admin       社員管理DTO
	 * @param authorities 権限リスト
	 */
	SecurityAdmin(final @NotNull StudentDto admin, final Collection<SimpleGrantedAuthority> authorities) {
		super(admin.getLoginAccount(), admin.getPassword(), true, true, true, true, authorities);
		this.originalAdmin = admin;
	}

}
