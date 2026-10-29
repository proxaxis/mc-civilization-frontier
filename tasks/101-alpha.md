# タスク: ワールド生成系コンテンツの先行実装(鉱石・油田)

あなたは Minecraft MOD パックプロジェクト "Civilization Frontier: Beyond the Cataclysm" の作業エージェントです. ワールド生成に関わるコンテンツは後から追加すると既生成チャンクに反映されないため、Build Craft Neo(BCN)本体の実装(タスク 200)より先に、鉱石ブロック・油田・生成定義だけを先行して実装します.

## 前提知識

* ワークスペース: `/ws`、Minecraft `26.3` + Fabric、実装言語は Java の Fabric Mod
* BCN は過去作(Build Craft / Industrial Craft 2 / Project Red / Logistics Pipes)の要素を現代に再現する自作 MOD
* 仕様の原本は `/ws/resources/*/ITEMS.md`. アセット(テクスチャ・モデル・loot_table・worldgen JSON)は `/ws/resources/` 以下に既に存在するので参照・流用すること
  * `/ws/resources/projectred/data/projectred_exploration/worldgen/`: configured_feature・placed_feature・biome_modifier(neoforge 形式)の JSON あり. 参考になる
  * `/ws/resources/buildcraft/assets/buildcraftenergy/`: オイル流体テクスチャ・バケツモデルあり
* 生成定義はなるべく既存の placed_feature JSON の高さ・回数・サイズに準拠する

## 実装対象(ワールド生成リスト)

### A. 鉱石ブロック(IC2 / ProjectRed 由来)

| 鉱石 | 生成条件(原本仕様) | 用途メモ |
| --- | --- | --- |
| 銅鉱石 | Y10〜70 / trapezoid 生成(count=8、-24〜56 の既存 JSON あり) | 赤合金・ブラス等の合金素材 |
| 錫鉱石 | Y0〜40 | ブラス・錫合板の素材. 銀と見分けが付きにくい |
| 銀鉱石 | Y31 以下、金よりやや多め | 青合金素材 |
| ウラニウム鉱石 | Y0〜63、単体で見つかることが多い、幸運エンチャ無効 | 核燃料素材 |
| ルビー鉱石 | Y53 以下、ダイヤ程度の比率 | 宝石(ツール素材) |
| サファイア鉱石 | 同上 | 同上 |
| グリーンサファイア鉱石(ペリドット) | 同上 | 同上+電動ドライバー素材 |
| ニコライト鉱石(=electrotine) | Y20 以下、レッドストーン程度の比率、3〜5個ドロップ | 青電力導体素材. ダイヤと見分けが付きにくい |
| マーブル | Y64 以下に巨大鉱脈として生成 | 建材. 1鉱脈が非常に大きい |
| イリジウム | 生成しない(クラフト限定) | 例外として明記 |

### B. 油田・オイル(BuildCraft 由来)

* 砂漠・海洋バイオーム中心に油田(オイル溜まり・油源泉)を生成する
* 大型油田の底には油源泉が存在し、稀に黒オイルが湧き出す
* 流体ブロックとしての性質: 無限水源は作れない、エンダーマンとブレイズはダメージ、溶岩を黒曜石にできる、ネザー設置で爆発
* オイル流体とバケツアイテムはテクスチャが `/ws/resources/buildcraft/assets/` に存在

## タスク詳細

1. 上記 A・B のブロック・流体・アイテムを登録する(ID は既存アセットの命名規則に揃える)
2. worldgen 定義(configured_feature / placed_feature / biome_modifier 相当)を Fabric で実装する. 既存 JSON の高さ範囲・count・サイズを準拠値とする
3. loot_table: 鉱石ブロックのドロップ(幸運対応の有無は原本仕様に従う)を定義する
4. テスト: 新規ワールド生成で各鉱石・油田が想定高度・バイオームに生成されることを `/locate` や実掘りで検証する

## 制約・注意点

* 本タスクは「ワールド生成に関わるもの」のみ. 機械・回路・パイプ等の機能実装はタスク 200 の範囲
* テクスチャ・モデル・loot_table・worldgen JSON は `/ws/resources/` の既存アセットを優先して流用し、新規作成は不足分のみ
* `26.3` + Fabric で動作すること. 既存 MOD との ID 衝突に注意

## 完了条件

* A・B の全ブロックが新規生成ワールドの指定高度・バイオームに正しく生成される
* 生成検証結果を `REPORTS.md` に追記する(コミット ID・変更点・判明したこと・未解決を記述ルールに従って)
