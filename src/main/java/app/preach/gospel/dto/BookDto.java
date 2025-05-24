package app.preach.gospel.dto;

/**
 * 書別情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public record BookDto(

		/*
		 * ID
		 */
		Short id,

		/*
		 * 名称
		 */
		String name,

		/*
		 * 日本語名称
		 */
		String nameJp) {
}
