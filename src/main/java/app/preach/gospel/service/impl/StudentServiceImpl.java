package app.preach.gospel.service.impl;

import static app.preach.gospel.jooq.Tables.STUDENTS;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.jetbrains.annotations.NotNull;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.ConfigurationException;
import org.jooq.exception.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.StudentDto;
import app.preach.gospel.jooq.tables.records.StudentsRecord;
import app.preach.gospel.service.IStudentService;
import app.preach.gospel.utils.CoBeanUtils;
import app.preach.gospel.utils.CoProjectUtils;
import app.preach.gospel.utils.CoResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 奉仕者サービス実装クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StudentServiceImpl implements IStudentService {

	/**
	 * 共通検索条件
	 */
	protected static final Condition COMMON_CONDITION = STUDENTS.VISIBLE_FLG.eq(Boolean.TRUE);

	/**
	 * 日時フォマーター
	 */
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * 共通検索条件
	 */
	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(BCryptVersion.$2A, 7);

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	@Override
	public CoResult<Integer, DataAccessException> checkDuplicated(final String id, final String loginAccount) {
		try {
			if (CoProjectUtils.isDigital(id)) {
				final Integer checkDuplicated = this.dslContext.selectCount().from(STUDENTS).where(COMMON_CONDITION)
						.and(STUDENTS.ID.ne(Long.parseLong(id))).and(STUDENTS.LOGIN_ACCOUNT.eq(loginAccount))
						.fetchSingle().into(Integer.class);
				return CoResult.ok(checkDuplicated);
			}
			final Integer checkDuplicated = this.dslContext.selectCount().from(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.LOGIN_ACCOUNT.eq(loginAccount)).fetchSingle().into(Integer.class);
			return CoResult.ok(checkDuplicated);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<StudentDto, DataAccessException> getStudentInfoById(final Long id) {
		try {
			final StudentsRecord studentsRecord = this.dslContext.selectFrom(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.ID.eq(id)).fetchSingle();
			final StudentDto studentDto = new StudentDto(studentsRecord.getId(), studentsRecord.getLoginAccount(),
					studentsRecord.getUsername(), studentsRecord.getPassword(), studentsRecord.getEmail(),
					FORMATTER.format(studentsRecord.getDateOfBirth()), null);
			return CoResult.ok(studentDto);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<String, DataAccessException> infoUpdation(final @NotNull StudentDto studentDto) {
		try {
			final StudentsRecord studentsRecord = this.dslContext.newRecord(STUDENTS);
			studentsRecord.setId(studentDto.id());
			studentsRecord.setLoginAccount(studentDto.loginAccount());
			studentsRecord.setUsername(studentDto.username());
			studentsRecord.setDateOfBirth(LocalDate.parse(studentDto.dateOfBirth(), FORMATTER));
			studentsRecord.setEmail(studentDto.email());
			studentsRecord.setVisibleFlg(Boolean.TRUE);
			final StudentsRecord studentsRecord2 = this.dslContext.selectFrom(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.ID.eq(studentsRecord.getId())).fetchSingle();
			final String password = studentsRecord2.getPassword();
			final OffsetDateTime updatedTime = studentsRecord2.getUpdatedTime();
			studentsRecord2.setPassword(null);
			studentsRecord2.setUpdatedTime(null);
			boolean passwordDiscernment;
			if (CoProjectUtils.isEqual(studentDto.password(), password)) {
				passwordDiscernment = true;
			} else {
				passwordDiscernment = ENCODER.matches(studentDto.password(), password);
			}
			if (CoProjectUtils.isEqual(studentsRecord, studentsRecord2) && passwordDiscernment) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_STRING_NO_CHANGE));
			}
			CoBeanUtils.copyNullableProperties(studentsRecord, studentsRecord2);
			studentsRecord2.setUpdatedTime(updatedTime);
			if (passwordDiscernment) {
				studentsRecord2.setPassword(password);
			} else {
				studentsRecord2.setPassword(ENCODER.encode(studentDto.password()));
			}
			studentsRecord2.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

	@Override
	public CoResult<String, DataAccessException> preLoginUpdation(final String loginAccount, final String password) {
		try {
			final StudentsRecord studentsRecord = this.dslContext.selectFrom(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.LOGIN_ACCOUNT.eq(loginAccount).or(STUDENTS.EMAIL.eq(loginAccount))).fetchOne();
			if (studentsRecord == null) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_SPRINGSECURITY_LOGINERROR1));
			}
			final boolean passwordMatches = ENCODER.matches(password, studentsRecord.getPassword());
			if (!passwordMatches) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_SPRINGSECURITY_LOGINERROR4));
			}
			studentsRecord.setUpdatedTime(OffsetDateTime.now());
			studentsRecord.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_LOGIN_SUCCESS);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		}
	}

}
