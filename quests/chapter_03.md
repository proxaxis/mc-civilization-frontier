# 第 3 章 - 💰経済

## 説明

産業が興れば, 次に必要となるのは流通と交易の力だ. 高低差に広がる立体都市や沿岸の港湾集落と交易路を結び, 余剰資源や工業中間素材を硬貨へと換金せよ. チャンクをクレームして領地を保護し, 維持費を納めつつ, 敷設した鉄道網によって物流の動脈を世界中へと張り巡らせるのだ.

## 目的

共通通貨 MD を通じた交易ネットワークを構築し, 領地保護の維持管理, および連結鉄道による長距離物流網を完成させる.

## クエスト一覧

### 通貨の流通

- id: c3_currency
- 説明: 本ワールドの経済基準となる通貨 Minecraft Dollar です. 銅・鉄・金・エメラルド・ダイヤ・ネザライト規格が存在し, ボスの討伐や中間素材の売却で入手できます.
- 要件:
  - have:coinsje:copper_coin x1 ※いずれかの硬貨は or 条件が組めないため銅貨に限定
- 報酬:
  - coinsje:iron_coin x10
  - xp:100
  - minecraft:bundle x1 ※硬貨入れポーチ→bundle代替

### 交易都市への参入

- id: c3_trade
- 説明: 余剰な木材・石材・作物を換金し, 自前では調達困難な希少資材やエンチャントを入手します. 他プレイヤーとの商流を築く拠点としても活用できます.
- 要件:
  - advancement:minecraft:adventure/trade ※都市構造の到達判定は構造ID未定のため交易進捗に代替
- 報酬:
  - coinsje:gold_coin x1
  - xp:150
  - minecraft:emerald x4 ※商人の引換券→エメラルド代替

### 領地の保護と納税

- id: c3_claim
- 説明: 露天掘り機などを設置した科学拠点は, 定期的な維持費を支払う必要があります. 未納が続くと保護が切れ, 破壊や略奪のリスクに晒されます.
- 要件:
  - flag:fee_paid ※維持費納付の検出機構がないため fee 側に納付実績スコアを追加予定
- 報酬:
  - coinsje:gold_coin x2
  - xp:200
  - minecraft:golden_shovel x1 ※領地境界杭→金シャベル(Flan標準)代替

### 鉄路の敷設

- id: c3_rail
- 説明: 大量物資の遠隔輸送を担う連結列車網です. 駅やレッドストーン制御を整え, 過酷な荒野を繋ぐ輸送ラインを完成させましょう.
- 要件:
  - have:minecraft:furnace_minecart x1 ※運行実績は追跡不可のため車両所持に代替
  - have:minecraft:minecart x1
- 報酬:
  - coinsje:gold_coin x2
  - xp:250
  - minecraft:powered_rail x16
  - minecraft:rail x64
