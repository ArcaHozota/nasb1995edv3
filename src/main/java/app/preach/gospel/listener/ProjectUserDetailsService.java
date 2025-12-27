package app.preach.gospel.listener;

import static app.preach.gospel.jooq.Tables.AUTHORITIES;
import static app.preach.gospel.jooq.Tables.ROLE_AUTH;
import static app.preach.gospel.jooq.Tables.STUDENTS;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.StudentDto;
import app.preach.gospel.jooq.Keys;
import app.preach.gospel.jooq.tables.records.AuthoritiesRecord;
import app.preach.gospel.jooq.tables.records.StudentsRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * ログインコントローラ(SpringSecurity関連)
 *
 * @author ArkamaHozota
 * @since 2.00
 */
@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectUserDetailsService implements UserDetailsService {

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final StudentsRecord studentsRecord = this.dslContext.selectFrom(STUDENTS)
				.where(STUDENTS.VISIBLE_FLG.eq(Boolean.TRUE))
				.and(STUDENTS.LOGIN_ACCOUNT.eq(username).or(STUDENTS.EMAIL.eq(username))).fetchOne();
		if (studentsRecord == null) {
			throw new DisabledException(ProjectConstants.MESSAGE_SPRINGSECURITY_LOGINERROR1);
		}
		final List<AuthoritiesRecord> authoritiesRecords = this.dslContext.select(AUTHORITIES.fields()).from(ROLE_AUTH)
				.innerJoin(AUTHORITIES).onKey(Keys.ROLE_AUTH__ROLE_AUTH_AUTH_ID)
				.where(ROLE_AUTH.ROLE_ID.eq(studentsRecord.getRoleId())).fetchInto(AuthoritiesRecord.class);
		if (authoritiesRecords.isEmpty()) {
			throw new AuthenticationCredentialsNotFoundException(ProjectConstants.MESSAGE_SPRINGSECURITY_LOGINERROR3);
		}
		final var studentDto = new StudentDto();
		studentDto.setId(studentsRecord.getId().toString());
		studentDto.setLoginAccount(studentsRecord.getLoginAccount());
		studentDto.setUsername(studentsRecord.getUsername());
		studentDto.setPassword(studentsRecord.getPassword());
		studentDto.setEmail(studentsRecord.getEmail());
		studentDto.setDateOfBirth(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(studentsRecord.getDateOfBirth()));
		studentDto.setRoleId(studentsRecord.getRoleId().toString());
		final List<SimpleGrantedAuthority> authorities = authoritiesRecords.stream()
				.map(item -> new SimpleGrantedAuthority(item.getName())).collect(Collectors.toList());
		return new SecurityAdmin(studentDto, authorities);
	}

}
