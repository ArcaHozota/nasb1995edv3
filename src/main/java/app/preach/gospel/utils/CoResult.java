package app.preach.gospel.utils;

import lombok.Data;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 共通返却クラス
 *
 * @param <T> データ
 * @param <E> エラー
 */
@Data
public final class CoResult<T, E> {

	/**
	 * 異常系を返却する。
	 *
	 * @param <T> 正常
	 * @param <E> エラー
	 * @param err エラー
	 * @return Result<T, E>
	 */
	@Contract(value = "_ -> new", pure = true)
	public static <T, E> @NotNull CoResult<T, E> err(final E err) {
		return new CoResult<>(null, err, false);
	}

	/**
	 * インスタンスを返却する。
	 *
	 * @param <T> 正常
	 * @param <E> エラー
	 * @return Result<T, E>
	 */
	@Contract(value = " -> new", pure = true)
	public static <T, E> @NotNull CoResult<T, E> getInstance() {
		return new CoResult<>(null, null, false);
	}

	/**
	 * 正常系を返却する。
	 *
	 * @param <T>  正常
	 * @param <E>  エラー
	 * @param data データ
	 * @return Result<T, E>
	 */
	@Contract(value = "_ -> new", pure = true)
	public static <T, E> @NotNull CoResult<T, E> ok(final T data) {
		return new CoResult<>(data, null, true);
	}

	/**
	 * 正常的なデータ
	 */
	private T data;

	/**
	 * エラー
	 */
	private E err;

	/**
	 * 正常系あるかどうか -- GETTER -- 正常系あるかどうかを判断する。
	 * 
	 */
	private boolean ok;

	/**
	 * コンストラクタ
	 *
	 * @param data 正常
	 * @param err  エラー
	 * @param ok   正常系あるかどうか
	 */
	private CoResult(final T data, final E err, final boolean ok) {
		this.data = data;
		this.err = err;
		this.ok = ok;
	}

	/**
	 * 自分を返却する。
	 *
	 * @param self 自分
	 */
	public void setSelf(final @NotNull CoResult<T, E> self) {
		this.setData(self.getData());
		this.setErr(self.getErr());
		this.setOk(self.isOk());
	}

}
