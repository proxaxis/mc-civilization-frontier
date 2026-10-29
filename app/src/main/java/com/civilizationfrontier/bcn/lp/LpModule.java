package com.civilizationfrontier.bcn.lp;

/**
 * 物流制御パイプに挿入するモジュールの種別.
 * シンク系は宛先解決 (resolveSink) で消費され、能動系は chassis tick で動く.
 */
public enum LpModule {
	/** 無機能. 他モジュールの素材. */
	BLANK,
	/** 指定アイテムの回収ポイント (優先度高). */
	ITEM_SINK,
	/** 隣接コンテナ内の在庫種別と同じアイテムの回収ポイント. */
	POLYMORPHIC_SINK,
	/** 回収ポイント (優先度低). */
	TERMINUS_SINK,
	/** 指定アイテムを指定個数まで回収 (優先度最高). */
	PASSIVE_SUPPLIER,
	/** 隣接コンテナを供給源として網に登録. */
	PROVIDER,
	/** 隣接コンテナから定期的にアイテムを吸い出し網へ送る. */
	EXTRACTOR,
	/** 隣接コンテナの中身を回収ポイントに合わせて高速仕分け. */
	QUICK_SORT,
	/** 隣接コンテナへ指定アイテムを補充し続ける. */
	ACTIVE_SUPPLIER,
	/** 隣接クラフターのレシピで要求クラフト. */
	CRAFTING
}
