package app.preach.gospel.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 節別情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Data
@EqualsAndHashCode(callSuper = false)
public final class PhraseDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 4733931779755040721L;

	/*
	 * 章節ID
	 */
	private String chapterId;

	/*
	 * ID
	 */
	private String id;

	/*
	 * 名称
	 */
	private String name;

	/*
	 * 内容
	 */
	private String textEn;

	/*
	 * 日本語内容
	 */
	private String textJp;
}
