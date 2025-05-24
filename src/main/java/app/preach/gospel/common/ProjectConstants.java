package app.preach.gospel.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * プロジェクトコンスタント
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectConstants {

	public static final Integer DEFAULT_PAGE_SIZE = 5;

	public static final Integer DEFAULT_TOKEN_EXPIRED = 1320;

	public static final String DEFAULT_PASSWORD = "00000000";

	public static final String MESSAGE_SPRING_MVCCONVERTOR = "拡張メッセージコンバーターの設置は完了しました。";

	public static final String MESSAGE_SPRING_MAPPER = "静的リソースのマッピングが開始しました。";

	public static final String MESSAGE_SPRING_APPLICATION = "アプリは正常に起動されました。";

	public static final String ATTRNAME_EXCEPTION = "exception";

	public static final String ATTRNAME_EDITED_INFO = "arawaseta";

	public static final String ATTRNAME_PAGE_INFO = "pageInfo";

	public static final String ATTRNAME_PAGE_NUMBER = "pageNum";

	public static final String ATTRNAME_RECORDS = "totalRecords";

	public static final String ATTRNAME_HYMNDTOS = "hymnDtos";

	public static final String MESSAGE_STRING_INVALIDATE = "入力した文字列は不規則。";

	public static final String MESSAGE_STRING_NOT_LOGIN = "ログインしてください";

	public static final String MESSAGE_STRING_LOGIN_SUCCESS = "ログイン成功";

	public static final String MESSAGE_STRING_NO_CHANGE = "変更なし";

	public static final String MESSAGE_STRING_DELETED = "削除済み";

	public static final String MESSAGE_STRING_DELETION_ERROR = "削除できません";

	public static final String MESSAGE_STRING_INSERTED = "追加済み";

	public static final String MESSAGE_STRING_STORAGE_ERROR = "追加処理エラー";

	public static final String MESSAGE_STRING_UPDATED = "更新済み";

	public static final String MESSAGE_STRING_UPDATION_ERROR = "更新できません";

	public static final String MESSAGE_HYMN_NAME_DUPLICATED = "歌の名称がすでに存在します。";

	public static final String MESSAGE_STUDENT_NAME_DUPLICATED = "ユーザ名称がすでに存在します。";

	public static final String MESSAGE_STRING_PROHIBITED = "ユーザは存在しません、もう一度やり直してください";

	public static final String MESSAGE_STRING_NOT_EXISTS = "役割は存在しません、もう一度やり直してください";

	public static final String MESSAGE_STRING_FORBIDDEN = "役割は利用されています、削除できません。";

	public static final String MESSAGE_STRING_FORBIDDEN2 = "権限付与にエラーが発生しました。";

	public static final String MESSAGE_STRING_ASSIGNED = "権限付与成功！";

	public static final String MESSAGE_STRING_FATAL_ERROR = "システムエラーが発生しました。";

	public static final String MESSAGE_SPRINGSECURITY_REQUIRED_AUTH = "リクエスト拒否";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR1 = "当ユーザは存在しません。もう一度やり直してください";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR2 = "当ユーザの役割情報が存在しません。ログイン拒否";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR3 = "当ユーザの役割がありますが、役割権限がないのでログイン拒否";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR4 = "入力したパスワードが正しくありません";

	public static final String MESSAGE_SPRING_SECURITY = "スプリングセキュリティ作動中。";

	public static final String MESSAGE_TOROKU_SUCCESS = "登録成功しました！メールとパスワードでログインしてください。";

	public static final String MESSAGE_TOROKU_FAILURE = "当メールは既に登録されまして直接ログインしてください。";

	public static final String MESSAGE_OPTIMISTIC_ERROR = "オプティミスティックロックの競合、データは他のトランザクションによって変更されました。";

	public static final String DEFAULT_LEDGER_NAME = "---------------------------------";
}
