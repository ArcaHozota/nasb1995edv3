package app.preach.gospel.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import app.preach.gospel.utils.LineNumber;
import org.jetbrains.annotations.NotNull;

/**
 * 賛美情報転送クラス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public record HymnDto(

		/*
		 * ID
		 */
		String id,

		/*
		 * 日本語名称
		 */
		String nameJp,

		/*
		 * 韓国語名称
		 */
		String nameKr,

		/*
		 * セリフ
		 */
		String serif,

		/*
		 * ビデオリンク
		 */
		String link,

		/*
		 * 楽譜
		 */
		byte[] score,

		/*
		 * 備考
		 */
		String biko,

		/*
		 * 更新者
		 */
		String updatedUser,

		/*
		 * 更新時間
		 */
		String updatedTime,

		/*
		 * LINENUMBER
		 */
		LineNumber lineNumber) implements Serializable {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + Arrays.hashCode(this.score);
		return (prime * result) + Objects.hash(this.biko, this.id, this.lineNumber, this.link, this.nameJp, this.nameKr,
				this.serif, this.updatedTime, this.updatedUser);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof HymnDto other)) {
			return false;
		}
        return Objects.equals(this.biko, other.biko) && Objects.equals(this.id, other.id)
				&& (this.lineNumber == other.lineNumber) && Objects.equals(this.link, other.link)
				&& Objects.equals(this.nameJp, other.nameJp) && Objects.equals(this.nameKr, other.nameKr)
				&& Arrays.equals(this.score, other.score) && Objects.equals(this.serif, other.serif)
				&& Objects.equals(this.updatedTime, other.updatedTime)
				&& Objects.equals(this.updatedUser, other.updatedUser);
	}

	@Override
	public @NotNull String toString() {
		return "HymnDto [id=" + this.id + ", nameJp=" + this.nameJp + ", nameKr=" + this.nameKr + ", serif="
				+ this.serif + ", link=" + this.link + ", score=" + Arrays.toString(this.score) + ", biko=" + this.biko
				+ ", updatedUser=" + this.updatedUser + ", updatedTime=" + this.updatedTime + ", lineNumber="
				+ this.lineNumber + "]";
	}

}
