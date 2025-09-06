package app.preach.gospel.dto;

/**
 * ベクターキー
 */
public record VecKey(String lang, String hymnalVersion, long hymnId, String textHash) {
}
