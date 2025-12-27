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
import org.springframework.transaction.annotation.Transactional;

import app.preach.gospel.common.ProjectConstants;
import app.preach.gospel.dto.StudentDto;
import app.preach.gospel.service.IStudentService;
import app.preach.gospel.utils.CoBeanUtils;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.CoStringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 奉仕者サービス実装クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentServiceImpl implements IStudentService {

	/**
	 * 共通検索条件
	 */
	protected static final Condition COMMON_CONDITION = STUDENTS.VISIBLE_FLG.eq(Boolean.TRUE);

	/**
	 * 共通検索条件
	 */
	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(BCryptVersion.$2A, 7);

	/**
	 * 日時フォマーター
	 */
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	/**
	 * 共通リポジトリ
	 */
	private final DSLContext dslContext;

	@Transactional(readOnly = true)
	@Override
	public CoResult<Integer, DataAccessException> checkDuplicated(final String id, final String loginAccount) {
		try {
			if (CoStringUtils.isDigital(id)) {
				final var checkDuplicated = this.dslContext.selectCount().from(STUDENTS).where(COMMON_CONDITION)
						.and(STUDENTS.ID.ne(Long.parseLong(id))).and(STUDENTS.LOGIN_ACCOUNT.eq(loginAccount))
						.fetchSingle().into(Integer.class);
				return CoResult.ok(checkDuplicated);
			}
			final var checkDuplicated = this.dslContext.selectCount().from(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.LOGIN_ACCOUNT.eq(loginAccount)).fetchSingle().into(Integer.class);
			return CoResult.ok(checkDuplicated);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public CoResult<StudentDto, DataAccessException> getStudentInfoById(final Long id) {
		try {
			final var studentsRecord = this.dslContext.selectFrom(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.ID.eq(id)).fetchSingle();
			final var studentDto = new StudentDto();
			studentDto.setId(studentsRecord.getId().toString());
			studentDto.setLoginAccount(studentsRecord.getLoginAccount());
			studentDto.setUsername(studentsRecord.getUsername());
			studentDto.setPassword(studentsRecord.getPassword());
			studentDto.setEmail(studentsRecord.getEmail());
			studentDto.setDateOfBirth(FORMATTER.format(studentsRecord.getDateOfBirth()));
			return CoResult.ok(studentDto);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CoResult<String, DataAccessException> infoUpdation(final @NotNull StudentDto studentDto) {
		try {
			final var studentsRecord = this.dslContext.newRecord(STUDENTS);
			studentsRecord.setId(Long.valueOf(studentDto.getId()));
			studentsRecord.setLoginAccount(studentDto.getLoginAccount());
			studentsRecord.setUsername(studentDto.getUsername());
			studentsRecord.setDateOfBirth(LocalDate.parse(studentDto.getDateOfBirth(), FORMATTER));
			studentsRecord.setEmail(studentDto.getEmail());
			studentsRecord.setVisibleFlg(Boolean.TRUE);
			final var studentsRecord2 = this.dslContext.selectFrom(STUDENTS).where(COMMON_CONDITION)
					.and(STUDENTS.ID.eq(studentsRecord.getId())).fetchSingle();
			final var password = studentsRecord2.getPassword();
			final var updatedTime = studentsRecord2.getUpdatedTime();
			studentsRecord2.setPassword(null);
			studentsRecord2.setUpdatedTime(null);
			boolean passwordDiscernment;
			if (CoStringUtils.isEqual(studentDto.getPassword(), password)) {
				passwordDiscernment = true;
			} else {
				passwordDiscernment = ENCODER.matches(studentDto.getPassword(), password);
			}
			if (CoStringUtils.isEqual(studentsRecord, studentsRecord2) && passwordDiscernment) {
				return CoResult.err(new ConfigurationException(ProjectConstants.MESSAGE_STRING_NO_CHANGE));
			}
			CoBeanUtils.copyNullableProperties(studentsRecord, studentsRecord2);
			studentsRecord2.setUpdatedTime(updatedTime);
			if (passwordDiscernment) {
				studentsRecord2.setPassword(password);
			} else {
				studentsRecord2.setPassword(ENCODER.encode(studentDto.getPassword()));
			}
			studentsRecord2.update();
			return CoResult.ok(ProjectConstants.MESSAGE_STRING_UPDATED);
		} catch (final DataAccessException e) {
			return CoResult.err(e);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CoResult<String, DataAccessException> preLoginUpdate(final String loginAccount, final String password) {
		try {
			final var studentsRecord = this.dslContext.selectFrom(STUDENTS).where(COMMON_CONDITION)
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
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
