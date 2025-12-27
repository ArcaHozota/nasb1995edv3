package app.preach.gospel.dto;

import java.io.Serial;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

/**
 * JSONオブジェクトマッパー
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public final class JacksonObjectMapper extends ObjectMapper {

	@Serial
	private static final long serialVersionUID = 3882120239622401371L;

	/** デフォルト日フォーマット */
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	/** デフォルト日時フォーマット */
	private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/** デフォルト時フォーマット */
	private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	/**
	 * コンストラクタ
	 */
	JacksonObjectMapper() {
		super();
		// 不明な属性を受信しても例外は報告されません。
		this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// デシリアライズ時、互換性のある処理が存在しないプロパティ。
		this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// シリアライザーとデシリアライザーを設定する。
		final SimpleModule simpleModule = new SimpleModule()
				.addDeserializer(LocalDateTime.class,
						new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
				.addDeserializer(LocalDate.class,
						new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
				.addDeserializer(LocalTime.class,
						new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))
				.addSerializer(BigInteger.class, ToStringSerializer.instance)
				.addSerializer(Long.class, ToStringSerializer.instance)
				.addSerializer(LocalDateTime.class,
						new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
				.addSerializer(LocalDate.class,
						new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
				.addSerializer(LocalTime.class,
						new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
		// 機能モジュールを登記する。
		this.registerModule(simpleModule);
	}

}
