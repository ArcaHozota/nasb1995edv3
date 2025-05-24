package app.preach.gospel.dto;

import java.io.Serializable;

/**
 * 章節情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public record ChapterDto(

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
		String nameJp,

		/*
		 * 書別ID
		 */
		String bookId) implements Serializable {
}
