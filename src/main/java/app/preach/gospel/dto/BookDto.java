package app.preach.gospel.dto;

import java.io.Serializable;

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
		String id,

		/*
		 * 名称
		 */
		String name,

		/*
		 * 日本語名称
		 */
		String nameJp) implements Serializable {
}
