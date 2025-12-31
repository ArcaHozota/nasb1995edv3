package app.preach.gospel.listener;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.struts2.util.StrutsTypeConverter;

/**
 * Struts2タイプコンバーター
 *
 * @author ArkamaHozota
 * @since 2.54
 */
@SuppressWarnings("rawtypes")
public class Base64ToByteArrayConverter extends StrutsTypeConverter {

	@Override
	public Object convertFromString(final Map context, final String[] values, final Class toClass) {
		if (values == null || values.length == 0) {
			return null;
		}
		String s = values[0];
		if (s == null) {
			return null;
		}
		s = s.trim();
		if (s.isEmpty()) {
			return null;
		}
		// JSON interceptor 経由でも values[0] に base64 が入ってくる想定
		// DataURL が来る可能性があるならここで削る（あなたは split(",")[1] してるので本来不要）
		final int comma = s.indexOf(',');
		if (comma >= 0 && s.startsWith("data:")) {
			s = s.substring(comma + 1);
		}
		// base64 は改行が混じるケースもあるので MIME decoder が安全
		return Base64.getMimeDecoder().decode(s.getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public String convertToString(final Map context, final Object o) {
		if (o == null) {
			return null;
		}
		if (!(o instanceof final byte[] bytes)) {
			return null;
		}
		return Base64.getEncoder().encodeToString(bytes);
	}

}
