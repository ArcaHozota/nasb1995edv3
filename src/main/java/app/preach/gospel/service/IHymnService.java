package app.preach.gospel.service;

import java.util.List;

import org.jooq.exception.DataAccessException;

import app.preach.gospel.dto.HymnDto;
import app.preach.gospel.utils.CoResult;
import app.preach.gospel.utils.Pagination;

/**
 * 賛美歌サービスインターフェス
 *
 * @author ArkamaHozota
 * @since 1.00beta
 */
public interface IHymnService {

	/**
	 * 歌の名称の重複性をチェックする
	 *
	 * @param id     ID
	 * @param nameJp 日本語名称
	 * @return CoResult<Integer, DataAccessException>
	 */
	CoResult<Integer, DataAccessException> checkDuplicated(String id, String nameJp);

	/**
	 * 歌の名称の重複性をチェックする
	 *
	 * @param id     ID
	 * @param nameJp 韓国語名称
	 * @return CoResult<Integer, DataAccessDataAccessException>
	 */
	CoResult<Integer, DataAccessException> checkDuplicated2(String id, String nameKr);

	/**
	 * IDによって歌の情報を取得する
	 *
	 * @param id ID
	 * @return CoResult<HymnDto, DataAccessException>
	 */
	CoResult<HymnDto, DataAccessException> getHymnInfoById(Long id);

	/**
	 * キーワードによって賛美歌情報を取得する
	 *
	 * @param pageNum ページ数
	 * @param keyword キーワード
	 * @return CoResult<Pagination<HymnDto>, DataAccessException>
	 */
	CoResult<Pagination<HymnDto>, DataAccessException> getHymnsInfoByPagination(Integer pageNum, String keyword);

	/**
	 * ランドム選択の五つの賛美歌情報を取得する
	 *
	 * @param keyword キーワード
	 * @return CoResult<List<HymnDto>, DataAccessException>
	 */
	CoResult<List<HymnDto>, DataAccessException> getHymnsInfoByRandom(String keyword);

	/**
	 * 金海氏の検索によって賛美歌情報を取得する
	 *
	 * @param id ID
	 * @return CoResult<List<HymnDto>, DataAccessException>
	 */
	CoResult<List<HymnDto>, DataAccessException> getKanumiList(Long id);

	/**
	 * 賛美歌のレコード数を取得する
	 *
	 * @return CoResult<Long, DataAccessException>
	 */
	CoResult<Long, DataAccessException> getTotalCounts();

	/**
	 * 賛美情報を削除する
	 *
	 * @param id ID
	 * @return CoResult<String, DataAccessException>
	 */
	CoResult<String, DataAccessException> infoDeletion(Long id);

	/**
	 * 賛美情報を保存する
	 *
	 * @param hymnDto 賛美情報転送クラス
	 * @return CoResult<Integer, DataAccessException>
	 */
	CoResult<Integer, DataAccessException> infoStorage(HymnDto hymnDto);

	/**
	 * 賛美情報を更新する
	 *
	 * @param hymnDto 賛美情報転送クラス
	 * @return CoResult<String, DataAccessException>
	 */
	CoResult<String, DataAccessException> infoUpdate(HymnDto hymnDto);

	/**
	 * 賛美歌楽譜の情報を保存する
	 *
	 * @param file 楽譜ファイル
	 * @param id   ID
	 * @return CoResult<String, DataAccessException>
	 */
	CoResult<String, DataAccessException> scoreStorage(byte[] file, Long id);
}
