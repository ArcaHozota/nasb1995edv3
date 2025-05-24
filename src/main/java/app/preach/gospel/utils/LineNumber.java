package app.preach.gospel.utils;

import lombok.Getter;

/**
 * LINE_NUMBER表示クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
public enum LineNumber {

	BURGUNDY(2),

	CADMIUM(1),

	NAPLES(3),

	SNOWY(5);

	/**
	 * ラインナンバー
	 */
	private final Integer lineNo;

	/**
	 * コンストラクタ
	 *
	 * @param lineNo ラインナンバー
	 */
	LineNumber(final Integer lineNo) {
		this.lineNo = lineNo;
	}

}
