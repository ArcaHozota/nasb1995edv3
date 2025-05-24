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

	public static final String MESSAGE_SPRING_MAPPER = "静的リソースのマッピングが開始しました。";

	public static final String MESSAGE_SPRING_APPLICATION = "アプリは正常に起動されました。";

	public static final String ATTRNAME_EXCEPTION = "exception";

	public static final String ATTRNAME_EDITED_INFO = "arawaseta";

	public static final String ATTRNAME_PAGE_NUMBER = "pageNum";

	public static final String ATTRNAME_RECORDS = "totalRecords";

	public static final String ATTRNAME_TOROKU_MSG = "torokuMsg";

	public static final String ATTRNAME_KEYWORD = "keyword";

	public static final String ATTRNAME_PDF = "pdf";

	public static final String MESSAGE_STRING_NOT_LOGIN = "ログインしてください";

	public static final String MESSAGE_STRING_LOGIN_SUCCESS = "ログイン成功";

	public static final String MESSAGE_STRING_NO_CHANGE = "変更なし";

	public static final String MESSAGE_STRING_DELETED = "削除済み";

	public static final String MESSAGE_STRING_DELETION_ERROR = "削除できません";

	public static final String MESSAGE_STRING_INSERTED = "追加済み";

	public static final String MESSAGE_STRING_BOOKS = "更新または追加済み";

	public static final String MESSAGE_STRING_STORAGE_ERROR = "追加処理エラー";

	public static final String MESSAGE_STRING_UPDATED = "更新済み";

	public static final String MESSAGE_STRING_UPDATION_ERROR = "更新できません";

	public static final String MESSAGE_HYMN_NAME_DUPLICATED = "歌の名称がすでに存在します。";

	public static final String MESSAGE_STUDENT_NAME_DUPLICATED = "ユーザ名称がすでに存在します。";

	public static final String MESSAGE_STRING_FATAL_ERROR = "システムエラーが発生しました。";

	public static final String MESSAGE_STRING_UNEXPECTED_ERROR = "想定外のエラーが発生しました。管理員へご連絡ください。";

	public static final String MESSAGE_SPRINGSECURITY_REQUIRED_AUTH = "リクエスト拒否";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR1 = "当ユーザは存在しません。もう一度やり直してください";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR2 = "当ユーザの役割情報が存在しません。ログイン拒否";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR3 = "当ユーザの役割がありますが、役割権限がないのでログイン拒否";

	public static final String MESSAGE_SPRINGSECURITY_LOGINERROR4 = "入力したパスワードが正しくありません";

	public static final String MESSAGE_STUDENT_NOT_FOUND = "勉強者情報が見つかりませんでした";

	public static final String MESSAGE_HYMN_NOT_FOUND = "賛美歌情報が見つかりませんでした";

	public static final String MESSAGE_HYMNSWORK_NOT_FOUND = "賛美歌ワーク情報が見つかりませんでした";

	public static final String MESSAGE_BOOK_NOT_FOUND = "書別情報が見つかりませんでした";

	public static final String MESSAGE_CHAPTER_NOT_FOUND = "章節情報が見つかりませんでした";

	public static final String MESSAGE_SPRING_SECURITY = "スプリングセキュリティ作動中。";

	public static final String MESSAGE_OPTIMISTIC_ERROR = "オプティミスティックロックの競合、データは他のトランザクションによって変更されました。";
}
