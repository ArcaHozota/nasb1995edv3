package app.preach.gospel.dto;

import java.io.Serializable;

/**
 * Docキー
 */
public record DocKey(String keyword, String corpusVersion) implements Serializable {
}
