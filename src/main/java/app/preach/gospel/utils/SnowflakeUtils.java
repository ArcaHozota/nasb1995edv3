package app.preach.gospel.utils;

import java.util.Random;

import org.jetbrains.annotations.NotNull;

/**
 * 雪花のアルゴリズムID生成ツール
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public final class SnowflakeUtils extends SnowflakeIdGenerator {

	private static final Random RANDOM = new Random();

	/**
	 * 次の雪花アルゴリズムIDを取得
	 *
	 * @return long ID
	 */
	public static @NotNull Long snowflakeId() {
		final int nextInt1 = RANDOM.nextInt(31);
		final int nextInt2 = RANDOM.nextInt(31);
		return new SnowflakeUtils(nextInt1, nextInt2).nextId();
	}

	/**
	 * コンストラクタ
	 *
	 * @param workerId     ワークID(最大値は31)
	 * @param datacenterId データセンターID(最大値は31)
	 */
	private SnowflakeUtils(final long workerId, final long datacenterId) {
		super(workerId, datacenterId);
	}

}
