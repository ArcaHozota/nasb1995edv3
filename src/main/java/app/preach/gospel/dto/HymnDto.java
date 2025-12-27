package app.preach.gospel.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import app.preach.gospel.utils.LineNumber;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 賛美情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
@Getter
@Setter
@NoArgsConstructor
public final class HymnDto implements Serializable {

	@Serial
	private static final long serialVersionUID = 8503008003122740312L;

	/*
	 * 備考
	 */
	private String biko;

	/*
	 * ID
	 */
	private String id;

	/*
	 * LINENUMBER
	 */
	private LineNumber lineNumber;

	/*
	 * ビデオリンク
	 */
	private String link;

	/*
	 * 歌詞
	 */
	private String lyric;

	/*
	 * 日本語名称
	 */
	private String nameJp;

	/*
	 * 韓国語名称
	 */
	private String nameKr;

	/*
	 * 楽譜
	 */
	private byte[] score;

	/*
	 * 更新時間
	 */
	private String updatedTime;

	/*
	 * 更新者
	 */
	private String updatedUser;

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof final HymnDto other)) {
			return false;
		}
		return Objects.equals(this.biko, other.biko) && Objects.equals(this.id, other.id)
				&& (this.lineNumber == other.lineNumber) && Objects.equals(this.link, other.link)
				&& Objects.equals(this.nameJp, other.nameJp) && Objects.equals(this.nameKr, other.nameKr)
				&& Arrays.equals(this.score, other.score) && Objects.equals(this.lyric, other.lyric)
				&& Objects.equals(this.updatedTime, other.updatedTime)
				&& Objects.equals(this.updatedUser, other.updatedUser);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(this.score);
		return (prime * result) + Objects.hash(this.biko, this.id, this.lineNumber, this.link, this.nameJp, this.nameKr,
				this.lyric, this.updatedTime, this.updatedUser);
	}

	@Override
	public @NotNull String toString() {
		return "HymnDto [id=" + this.id + ", nameJp=" + this.nameJp + ", nameKr=" + this.nameKr + ", serif="
				+ this.lyric + ", link=" + this.link + ", score=" + Arrays.toString(this.score) + ", biko=" + this.biko
				+ ", updatedUser=" + this.updatedUser + ", updatedTime=" + this.updatedTime + ", lineNumber="
				+ this.lineNumber + "]";
	}

}
