package app.preach.gospel.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import app.preach.gospel.common.ProjectConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * プロジェクト共通ツール
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoProjectUtils extends StringUtils {

	/**
	 * UTF-8キャラセット
	 */
	public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

	/**
	 * 空文字列
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * 半角スペース
	 */
	public static final String HANKAKU_SPACE = "\u0020";

	/**
	 * 半角パーセント記号
	 */
	public static final String HANKAKU_PERCENTSIGN = "\u0025";

	/**
	 * スラッシュマーク
	 */
	public static final String SLASH = "\u002f";

	/**
	 * ドットマーク
	 */
	public static final String DOT = "\u002e";

	/**
	 * 半角コロン
	 */
	public static final String HANKAKU_COLON = "\u003a";

	/**
	 * 半角シャープ
	 */
	public static final String HANKAKU_SHARP = "\u0023";

	/**
	 * 全角半角変換マップ
	 */
	private static final BidiMap<String, String> ZENHANKAKU_CONVERTOR = new DualHashBidiMap<>();

	static {
		ZENHANKAKU_CONVERTOR.put("\uff01", "\u0021");
		ZENHANKAKU_CONVERTOR.put("\uff02", "\"");
		ZENHANKAKU_CONVERTOR.put("\uff03", "\u0023");
		ZENHANKAKU_CONVERTOR.put("\uff04", "\u0024");
		ZENHANKAKU_CONVERTOR.put("\uff05", HANKAKU_PERCENTSIGN);
		ZENHANKAKU_CONVERTOR.put("\uff06", "\u0026");
		ZENHANKAKU_CONVERTOR.put("\uff07", "\u0027");
		ZENHANKAKU_CONVERTOR.put("\uff08", "\u0028");
		ZENHANKAKU_CONVERTOR.put("\uff09", "\u0029");
		ZENHANKAKU_CONVERTOR.put("\uff0a", "\u002a");
		ZENHANKAKU_CONVERTOR.put("\uff0b", "\u002b");
		ZENHANKAKU_CONVERTOR.put("\uff0c", "\u002c");
		ZENHANKAKU_CONVERTOR.put("\uff0d", "\u002d");
		ZENHANKAKU_CONVERTOR.put("\uff0e", "\u002e");
		ZENHANKAKU_CONVERTOR.put("\uff0f", "\u002f");
		ZENHANKAKU_CONVERTOR.put("\uff10", "\u0030");
		ZENHANKAKU_CONVERTOR.put("\uff11", "\u0031");
		ZENHANKAKU_CONVERTOR.put("\uff12", "\u0032");
		ZENHANKAKU_CONVERTOR.put("\uff13", "\u0033");
		ZENHANKAKU_CONVERTOR.put("\uff14", "\u0034");
		ZENHANKAKU_CONVERTOR.put("\uff15", "\u0035");
		ZENHANKAKU_CONVERTOR.put("\uff16", "\u0036");
		ZENHANKAKU_CONVERTOR.put("\uff17", "\u0037");
		ZENHANKAKU_CONVERTOR.put("\uff18", "\u0038");
		ZENHANKAKU_CONVERTOR.put("\uff19", "\u0039");
		ZENHANKAKU_CONVERTOR.put("\uff1a", "\u003a");
		ZENHANKAKU_CONVERTOR.put("\uff1b", "\u003b");
		ZENHANKAKU_CONVERTOR.put("\uff1c", "\u003c");
		ZENHANKAKU_CONVERTOR.put("\uff1d", "\u003d");
		ZENHANKAKU_CONVERTOR.put("\uff1e", "\u003e");
		ZENHANKAKU_CONVERTOR.put("\uff1f", "\u003f");
		ZENHANKAKU_CONVERTOR.put("\uff20", "\u0040");
		ZENHANKAKU_CONVERTOR.put("\uff21", "\u0041");
		ZENHANKAKU_CONVERTOR.put("\uff22", "\u0042");
		ZENHANKAKU_CONVERTOR.put("\uff23", "\u0043");
		ZENHANKAKU_CONVERTOR.put("\uff24", "\u0044");
		ZENHANKAKU_CONVERTOR.put("\uff25", "\u0045");
		ZENHANKAKU_CONVERTOR.put("\uff26", "\u0046");
		ZENHANKAKU_CONVERTOR.put("\uff27", "\u0047");
		ZENHANKAKU_CONVERTOR.put("\uff28", "\u0048");
		ZENHANKAKU_CONVERTOR.put("\uff29", "\u0049");
		ZENHANKAKU_CONVERTOR.put("\uff2a", "\u004a");
		ZENHANKAKU_CONVERTOR.put("\uff2b", "\u004b");
		ZENHANKAKU_CONVERTOR.put("\uff2c", "\u004c");
		ZENHANKAKU_CONVERTOR.put("\uff2d", "\u004d");
		ZENHANKAKU_CONVERTOR.put("\uff2e", "\u004e");
		ZENHANKAKU_CONVERTOR.put("\uff2f", "\u004f");
		ZENHANKAKU_CONVERTOR.put("\uff30", "\u0050");
		ZENHANKAKU_CONVERTOR.put("\uff31", "\u0051");
		ZENHANKAKU_CONVERTOR.put("\uff32", "\u0052");
		ZENHANKAKU_CONVERTOR.put("\uff33", "\u0053");
		ZENHANKAKU_CONVERTOR.put("\uff34", "\u0054");
		ZENHANKAKU_CONVERTOR.put("\uff35", "\u0055");
		ZENHANKAKU_CONVERTOR.put("\uff36", "\u0056");
		ZENHANKAKU_CONVERTOR.put("\uff37", "\u0057");
		ZENHANKAKU_CONVERTOR.put("\uff38", "\u0058");
		ZENHANKAKU_CONVERTOR.put("\uff39", "\u0059");
		ZENHANKAKU_CONVERTOR.put("\uff3a", "\u005a");
		ZENHANKAKU_CONVERTOR.put("\uff3b", "\u005b");
		ZENHANKAKU_CONVERTOR.put("\uff3c", "\\");
		ZENHANKAKU_CONVERTOR.put("\uff3d", "\u005d");
		ZENHANKAKU_CONVERTOR.put("\uff3e", "\u005e");
		ZENHANKAKU_CONVERTOR.put("\uff3f", "\u005f");
		ZENHANKAKU_CONVERTOR.put("\uff40", "\u0060");
		ZENHANKAKU_CONVERTOR.put("\uff41", "\u0061");
		ZENHANKAKU_CONVERTOR.put("\uff42", "\u0062");
		ZENHANKAKU_CONVERTOR.put("\uff43", "\u0063");
		ZENHANKAKU_CONVERTOR.put("\uff44", "\u0064");
		ZENHANKAKU_CONVERTOR.put("\uff45", "\u0065");
		ZENHANKAKU_CONVERTOR.put("\uff46", "\u0066");
		ZENHANKAKU_CONVERTOR.put("\uff47", "\u0067");
		ZENHANKAKU_CONVERTOR.put("\uff48", "\u0068");
		ZENHANKAKU_CONVERTOR.put("\uff49", "\u0069");
		ZENHANKAKU_CONVERTOR.put("\uff4a", "\u006a");
		ZENHANKAKU_CONVERTOR.put("\uff4b", "\u006b");
		ZENHANKAKU_CONVERTOR.put("\uff4c", "\u006c");
		ZENHANKAKU_CONVERTOR.put("\uff4d", "\u006d");
		ZENHANKAKU_CONVERTOR.put("\uff4e", "\u006e");
		ZENHANKAKU_CONVERTOR.put("\uff4f", "\u006f");
		ZENHANKAKU_CONVERTOR.put("\uff50", "\u0070");
		ZENHANKAKU_CONVERTOR.put("\uff51", "\u0071");
		ZENHANKAKU_CONVERTOR.put("\uff52", "\u0072");
		ZENHANKAKU_CONVERTOR.put("\uff53", "\u0073");
		ZENHANKAKU_CONVERTOR.put("\uff54", "\u0074");
		ZENHANKAKU_CONVERTOR.put("\uff55", "\u0075");
		ZENHANKAKU_CONVERTOR.put("\uff56", "\u0076");
		ZENHANKAKU_CONVERTOR.put("\uff57", "\u0077");
		ZENHANKAKU_CONVERTOR.put("\uff58", "\u0078");
		ZENHANKAKU_CONVERTOR.put("\uff59", "\u0079");
		ZENHANKAKU_CONVERTOR.put("\uff5a", "\u007a");
		ZENHANKAKU_CONVERTOR.put("\uff5b", "\u007b");
		ZENHANKAKU_CONVERTOR.put("\uff5c", "\u007c");
		ZENHANKAKU_CONVERTOR.put("\uff5d", "\u007d");
		ZENHANKAKU_CONVERTOR.put("\uff5e", "\u007e");
		ZENHANKAKU_CONVERTOR.put("\u3002", "\uff61");
		ZENHANKAKU_CONVERTOR.put("\u300c", "\uff62");
		ZENHANKAKU_CONVERTOR.put("\u300d", "\uff63");
		ZENHANKAKU_CONVERTOR.put("\u3001", "\uff64");
		ZENHANKAKU_CONVERTOR.put("\u30fb", "\uff65");
		ZENHANKAKU_CONVERTOR.put("\u30a1", "\uff67");
		ZENHANKAKU_CONVERTOR.put("\u30a3", "\uff68");
		ZENHANKAKU_CONVERTOR.put("\u30a5", "\uff69");
		ZENHANKAKU_CONVERTOR.put("\u30a7", "\uff6a");
		ZENHANKAKU_CONVERTOR.put("\u30a9", "\uff6b");
		ZENHANKAKU_CONVERTOR.put("\u30e3", "\uff6c");
		ZENHANKAKU_CONVERTOR.put("\u30e5", "\uff6d");
		ZENHANKAKU_CONVERTOR.put("\u30e7", "\uff6e");
		ZENHANKAKU_CONVERTOR.put("\u30c3", "\uff6f");
		ZENHANKAKU_CONVERTOR.put("\u30fc", "\uff70");
		ZENHANKAKU_CONVERTOR.put("\u30a2", "\uff71");
		ZENHANKAKU_CONVERTOR.put("\u30a4", "\uff72");
		ZENHANKAKU_CONVERTOR.put("\u30a6", "\uff73");
		ZENHANKAKU_CONVERTOR.put("\u30a8", "\uff74");
		ZENHANKAKU_CONVERTOR.put("\u30aa", "\uff75");
		ZENHANKAKU_CONVERTOR.put("\u30ab", "\uff76");
		ZENHANKAKU_CONVERTOR.put("\u30ad", "\uff77");
		ZENHANKAKU_CONVERTOR.put("\u30af", "\uff78");
		ZENHANKAKU_CONVERTOR.put("\u30b1", "\uff79");
		ZENHANKAKU_CONVERTOR.put("\u30b3", "\uff7a");
		ZENHANKAKU_CONVERTOR.put("\u30b5", "\uff7b");
		ZENHANKAKU_CONVERTOR.put("\u30b7", "\uff7c");
		ZENHANKAKU_CONVERTOR.put("\u30b9", "\uff7d");
		ZENHANKAKU_CONVERTOR.put("\u30bb", "\uff7e");
		ZENHANKAKU_CONVERTOR.put("\u30bd", "\uff7f");
		ZENHANKAKU_CONVERTOR.put("\u30bf", "\uff80");
		ZENHANKAKU_CONVERTOR.put("\u30c1", "\uff81");
		ZENHANKAKU_CONVERTOR.put("\u30c4", "\uff82");
		ZENHANKAKU_CONVERTOR.put("\u30c6", "\uff83");
		ZENHANKAKU_CONVERTOR.put("\u30c8", "\uff84");
		ZENHANKAKU_CONVERTOR.put("\u30ca", "\uff85");
		ZENHANKAKU_CONVERTOR.put("\u30cb", "\uff86");
		ZENHANKAKU_CONVERTOR.put("\u30cc", "\uff87");
		ZENHANKAKU_CONVERTOR.put("\u30cd", "\uff88");
		ZENHANKAKU_CONVERTOR.put("\u30ce", "\uff89");
		ZENHANKAKU_CONVERTOR.put("\u30cf", "\uff8a");
		ZENHANKAKU_CONVERTOR.put("\u30d2", "\uff8b");
		ZENHANKAKU_CONVERTOR.put("\u30d5", "\uff8c");
		ZENHANKAKU_CONVERTOR.put("\u30d8", "\uff8d");
		ZENHANKAKU_CONVERTOR.put("\u30db", "\uff8e");
		ZENHANKAKU_CONVERTOR.put("\u30de", "\uff8f");
		ZENHANKAKU_CONVERTOR.put("\u30df", "\uff90");
		ZENHANKAKU_CONVERTOR.put("\u30e0", "\uff91");
		ZENHANKAKU_CONVERTOR.put("\u30e1", "\uff92");
		ZENHANKAKU_CONVERTOR.put("\u30e2", "\uff93");
		ZENHANKAKU_CONVERTOR.put("\u30e4", "\uff94");
		ZENHANKAKU_CONVERTOR.put("\u30e6", "\uff95");
		ZENHANKAKU_CONVERTOR.put("\u30e8", "\uff96");
		ZENHANKAKU_CONVERTOR.put("\u30e9", "\uff97");
		ZENHANKAKU_CONVERTOR.put("\u30ea", "\uff98");
		ZENHANKAKU_CONVERTOR.put("\u30eb", "\uff99");
		ZENHANKAKU_CONVERTOR.put("\u30ec", "\uff9a");
		ZENHANKAKU_CONVERTOR.put("\u30ed", "\uff9b");
		ZENHANKAKU_CONVERTOR.put("\u30ef", "\uff9c");
		ZENHANKAKU_CONVERTOR.put("\u30f2", "\uff66");
		ZENHANKAKU_CONVERTOR.put("\u30f3", "\uff9d");
		ZENHANKAKU_CONVERTOR.put("\u30ac", "\uff76\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30ae", "\uff77\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30b0", "\uff78\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30b2", "\uff79\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30b4", "\uff7a\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30b6", "\uff7b\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30b8", "\uff7c\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30ba", "\uff7d\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30bc", "\uff7e\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30be", "\uff7f\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30c0", "\uff80\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30c2", "\uff81\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30c5", "\uff82\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30c7", "\uff83\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30c9", "\uff84\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30d0", "\uff8a\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30d3", "\uff8b\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30d6", "\uff8c\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30d9", "\uff8d\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30dc", "\uff8e\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30d1", "\uff8a\uff9f");
		ZENHANKAKU_CONVERTOR.put("\u30d4", "\uff8b\uff9f");
		ZENHANKAKU_CONVERTOR.put("\u30d7", "\uff8c\uff9f");
		ZENHANKAKU_CONVERTOR.put("\u30da", "\uff8d\uff9f");
		ZENHANKAKU_CONVERTOR.put("\u30dd", "\uff8e\uff9f");
		ZENHANKAKU_CONVERTOR.put("\u30f4", "\uff73\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30f7", "\uff9c\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u30fa", "\uff66\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u309b", "\uff9e");
		ZENHANKAKU_CONVERTOR.put("\u309c", "\uff9f");
		ZENHANKAKU_CONVERTOR.put("\u3000", HANKAKU_SPACE);
	}

	/**
	 * ページングのインデクス改善
	 *
	 * @param totalRecords レコード数
	 * @return レコード数
	 */
	public static int discernLargestPage(final @NotNull Long totalRecords) {
		int largestPage;
		final int discernment = totalRecords.intValue() % ProjectConstants.DEFAULT_PAGE_SIZE;
		if (discernment != 0) {
			largestPage = (totalRecords.intValue() / ProjectConstants.DEFAULT_PAGE_SIZE) + 1;
		} else {
			largestPage = totalRecords.intValue() / ProjectConstants.DEFAULT_PAGE_SIZE;
		}
		return largestPage;
	}

	/**
	 * 現在のリクエストがAJAXリクエストであるかどうかを判断する
	 *
	 * @param request リクエスト
	 * @return true: ajax-request, false: no-ajax
	 */
	public static boolean discernRequestType(final @NotNull HttpServletRequest request) {
		// リクエストヘッダー情報の取得する
		final String acceptInformation = request.getHeader("Accept");
		final String xRequestInformation = request.getHeader("X-Requested-With");
		// 判断して返却する
		return ((acceptInformation != null) && (acceptInformation.length() > 0)
				&& acceptInformation.contains("application/json"))
				|| ((xRequestInformation != null) && (xRequestInformation.length() > 0)
						&& "XMLHttpRequest".equals(xRequestInformation));
	}

	/**
	 * 共通権限管理ストリーム
	 *
	 * @param stream 権限ストリーム
	 * @return List<String>
	 */
	public static List<String> getAuthNames(final @NotNull Stream<SimpleGrantedAuthority> stream) {
		return stream.map(SimpleGrantedAuthority::getAuthority).toList();
	}

	/**
	 * ファジークエリ用の検索文を取得する
	 *
	 * @param keyword 検索文
	 * @return ファジークエリ
	 */
	public static String getDetailKeyword(final String keyword) {
		if (CoProjectUtils.isEmpty(keyword)) {
			return HANKAKU_PERCENTSIGN;
		}
		final StringBuilder builder = new StringBuilder();
		builder.append(HANKAKU_PERCENTSIGN);
		for (final char aChar : keyword.toCharArray()) {
			final String charAt = String.valueOf(aChar);
			builder.append(charAt).append(HANKAKU_PERCENTSIGN);
		}
		return builder.toString();
	}

	/**
	 * 該当文字列はすべて半角かどうかを判断する
	 *
	 * @param hankaku 文字列
	 * @return true: すべて半角文字列, false: 全角文字も含める
	 */
	public static boolean isAllHankaku(final @NotNull String hankaku) {
		final List<String> zenkakuList = new ArrayList<>(ZENHANKAKU_CONVERTOR.keySet());
		for (final char aChar : hankaku.toCharArray()) {
			if (zenkakuList.contains(String.valueOf(aChar))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 該当文字列はすべて全角かどうかを判断する
	 *
	 * @param zenkaku 文字列
	 * @return true: すべて全角文字列, false: 半角文字も含める
	 */
	public static boolean isAllZenkaku(final @NotNull String zenkaku) {
		final List<String> hankakuList = new ArrayList<>(ZENHANKAKU_CONVERTOR.values());
		for (final char aChar : zenkaku.toCharArray()) {
			if (hankakuList.contains(String.valueOf(aChar))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * ある文字列はすべて数字であるかどうかを判断する
	 *
	 * @param string ストリング
	 * @return true: すべて数字, false: 文字も含める
	 */
	public static boolean isDigital(@Nullable final String string) {
		if (CoProjectUtils.isEmpty(string)) {
			return false;
		}
		return Pattern.compile("\\d*").matcher(string).matches();
	}

	/**
	 * 当ストリングは空かどうかを判断する
	 *
	 * @param str ストリング
	 * @return true: 空, false: 空ではない
	 */
	public static boolean isEmpty(@Nullable final String str) {
		return (str == null) || str.isEmpty() || str.isBlank();
	}

	/**
	 * 二つのロング数はイコールすることを判断する
	 *
	 * @param long1 ロング1
	 * @param long2 ロング2
	 * @return true: イコール, false: イコールしない
	 */
	public static boolean isEqual(@Nullable final Long long1, @Nullable final Long long2) {
		if ((long1 == null) && (long2 == null)) {
			return true;
		}
		if ((long1 == null) || (long2 == null)) {
			return false;
		}
		return long1.longValue() == long2.longValue();
	}

	/**
	 * 二つのオブジェクトはイコールすることを判断する
	 *
	 * @param obj1 オブジェクト1
	 * @param obj2 オブジェクト2
	 * @return true: イコール, false: イコールしない
	 */
	public static boolean isEqual(@Nullable final Object obj1, @Nullable final Object obj2) {
		return ((obj1 == null) && (obj2 == null)) || ((obj1 != null) && obj1.equals(obj2));
	}

	/**
	 * 二つのストリングはイコールすることを判断する
	 *
	 * @param str1 ストリング1
	 * @param str2 ストリング2
	 * @return true: イコール, false: イコールしない
	 */
	public static boolean isEqual(@Nullable final String str1, @Nullable final String str2) {
		if ((str1 == null) && (str2 == null)) {
			return true;
		}
		if ((str1 == null) || (str2 == null) || (str1.length() != str2.length())) {
			return false;
		}
		return str1.trim().equals(str2.trim());
	}

	/**
	 * 当ストリングは空ではないかどうかを判断する
	 *
	 * @param str ストリング
	 * @return true: 空ではない, false: 空
	 */
	public static boolean isNotEmpty(@Nullable final String str) {
		return !CoProjectUtils.isEmpty(str);
	}

	/**
	 * 二つのロング数はイコールしないことを判断する
	 *
	 * @param long1 ロング1
	 * @param long2 ロング2
	 * @return true: イコールしない, false: イコール
	 */
	public static boolean isNotEqual(@Nullable final Long long1, @Nullable final Long long2) {
		return !CoProjectUtils.isEqual(long1, long2);
	}

	/**
	 * 二つのオブジェクトはイコールしないことを判断する
	 *
	 * @param obj1 オブジェクト1
	 * @param obj2 オブジェクト2
	 * @return true: イコールしない, false: イコール
	 */
	public static boolean isNotEqual(@Nullable final Object obj1, @Nullable final Object obj2) {
		return !CoProjectUtils.isEqual(obj1, obj2);
	}

	/**
	 * 二つのストリングはイコールしないことを判断する
	 *
	 * @param str1 ストリング1
	 * @param str2 ストリング2
	 * @return true: イコールしない, false: イコール
	 */
	public static boolean isNotEqual(@Nullable final String str1, @Nullable final String str2) {
		return !CoProjectUtils.isEqual(str1, str2);
	}

	/**
	 * 全角から半角へ変換
	 *
	 * @param zenkaku 全角文字
	 * @return 半角文字
	 */
	public static String toHankaku(@Nullable final String zenkaku) {
		if (CoProjectUtils.isEmpty(zenkaku)) {
			return EMPTY_STRING;
		}
		final StringBuilder builder = new StringBuilder();
		final List<String> zenkakuList = new ArrayList<>(ZENHANKAKU_CONVERTOR.keySet());
		for (final char charAt : zenkaku.toCharArray()) {
			final String charAtString = String.valueOf(charAt);
			if (zenkakuList.contains(charAtString)) {
				builder.append(ZENHANKAKU_CONVERTOR.get(charAtString));
			} else {
				builder.append(charAtString);
			}
		}
		return builder.toString();
	}

	/**
	 * 半角から全角へ変換
	 *
	 * @param hankaku 半角文字
	 * @return 全角文字
	 */
	public static String toZenkaku(@Nullable final String hankaku) {
		if (CoProjectUtils.isEmpty(hankaku)) {
			return EMPTY_STRING;
		}
		final StringBuilder builder = new StringBuilder();
		final List<String> hankakuList = new ArrayList<>(ZENHANKAKU_CONVERTOR.values());
		for (final char charAt : hankaku.toCharArray()) {
			final String charAtString = String.valueOf(charAt);
			if (hankakuList.contains(charAtString)) {
				builder.append(ZENHANKAKU_CONVERTOR.inverseBidiMap().get(charAtString));
			} else {
				builder.append(charAtString);
			}
		}
		return builder.toString();
	}

}