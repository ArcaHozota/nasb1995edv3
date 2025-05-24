package app.preach.gospel.dto;

/**
 * 節別情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public record PhraseDto(

		/*
		 * ID
		 */
		Long id,

		/*
		 * 名称
		 */
		String name,

		/*
		 * 内容
		 */
		String textEn,

		/*
		 * 日本語内容
		 */
		String textJp,

		/*
		 * 章節ID
		 */
		Integer chapterId) {
}
