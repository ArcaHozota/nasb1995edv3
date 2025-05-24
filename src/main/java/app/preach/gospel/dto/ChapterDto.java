package app.preach.gospel.dto;

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
		Integer id,

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
		Short bookId) {
}
