# 第 5 章 - 🍲兵站

## 説明

どれほど強固な鎧を纏い, 強大な術式を紡ごうとも, 空腹に耐えかねた者は死地で露と消える. 大盾の構えも俊敏な奇襲も, すべての源となるのは開拓者自身の活力だ. 農業を広げて高カロリーな料理を調理し, 過酷な夜襲や遠征に耐えうる特級レーションのストックを整えることこそが, 命を繋ぐ最大の防壁となる.

## 目的

Farmer's Delight を軸とした高カロリー食料の量産ラインを整備し, スキル行使に必要な満腹度・隠し満腹度を確保して大規模夜襲を耐え抜く.

## クエスト一覧

### 厨房の夜明け

- id: c5_kitchen
- 説明: 作業台クラフトの簡素な食事から脱却し, 本格的な煮込み・焼き料理を作るための調理基盤を整えます. 包丁によるスライス加工も可能になります.
- 要件:
  - have:farmersdelight:cutting_board x1 ※設置検出不可のため所持に代替
  - have:farmersdelight:cooking_pot x1
- 報酬:
  - coinsje:iron_coin x5
  - xp:100
  - farmersdelight:iron_knife x1 ※鉄の包丁
  - farmersdelight:onion x8 ※玉ねぎの種→玉ねぎ代替
  - farmersdelight:tomato_seeds x8 ※トマトの種

### 肥沃なる大地

- id: c5_fertile
- 説明: 多彩な調理レシピには継続的な多品目収穫が欠かせません. 肥沃な土を活用して作物の成長速度を上げ, 安定した収穫サイクルを維持しましょう.
- 要件:
  - have:farmersdelight:cabbage x32
  - have:farmersdelight:tomato x32
  - have:farmersdelight:onion x32
  - have:farmersdelight:rice x32 ※4品目同時所持で「収穫」の代替
- 報酬:
  - coinsje:iron_coin x10
  - xp:150
  - farmersdelight:rich_soil x16 ※肥沃な土
  - minecraft:bone_meal x32

### 特級レーションの備蓄

- id: c5_ration
- 説明: Tier 3 以上の強敵は空腹デバフを多用します. 一口で大量の隠し満腹度を補給できるレーションを携帯することが, スキル発動と即死回避の絶対条件です.
- 要件:
  - have:farmersdelight:beef_stew x16 ※ごちそう料理16食の代替としてシチュー16個所持. 隠し満腹度充填は検出不可
- 報酬:
  - coinsje:gold_coin x3
  - xp:300
  - minecraft:shulker_box x1 ※保冷ランチボックス→代替

### 周期夜襲の防衛線

- id: c5_nightraid
- 説明: 通常のスポーン限界を超えて群れをなすアンデッドの総攻撃です. 防壁を固め, 食料を補給しながら広域術式と迎撃設備をフル稼働させて拠点を守り抜いてください.
- 要件:
  - kill:undeadnights:horde_zombie x10 ※「夜襲を迎撃し夜明けまで生還」は検出不可のため horde 討伐数に代替
- 報酬:
  - coinsje:diamond_coin x1
  - xp:500
  - minecraft:splash_potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}] x4 ※聖別の聖水→代替
  - minecraft:golden_apple x2

### 台所の充実

- id: c5_meal
- 説明: 調理基盤ができたら実際に料理を作ろう. 鍋料理の基本となる肉料理を量産せよ.
- 要件:
  - deliver:minecraft:cooked_beef x16
- 報酬:
  - coinsje:iron_coin x5
  - xp:100
  - minecraft:bread x16
