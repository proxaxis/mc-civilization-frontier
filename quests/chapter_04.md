# 第 4 章 - ⚔️征伐

## 説明

地底深く脈打つ多層迷宮, 外海を漂う幽霊船団, そして怨嗟が渦巻く呪われた教会群. 失われた超古代文明の遺構には, 強力なアンデッドや防衛機構が侵入者を阻んでいる. 職業固有のスキルや魔導の術式を解き放ち, 迷宮の最深部に巣食うボスたちを討ち破って, 新たな力となる武具と鍵を奪還せよ.

## 目的

専用呪文書を作成して職業アクティブスキルを行使可能にし, 地下迷宮や幽霊船団を踏破して伝説武器の獲得を目指す.

## クエスト一覧

### 呪文書の結合

- id: c4_spell
- 説明: 職業ごとのアクティブスキルを行使するために不可欠な書物です. 経験値とラピスラズリを捧げ, 呪文書スロットに装備してスペルホットバーを展開しましょう.
- 要件:
  - flag:spell_bound ※呪文書の結合・装備検出は trinket スロットのため専用判定(実装: スクロール消費時にフラグ)
- 報酬:
  - coinsje:iron_coin x10
  - xp:200
  - minecraft:lapis_lazuli x16
  - runes:fire_stone x8 ※初級ルーン石→fire_stone 代替

### カタコンベの深淵

- id: c4_catacomb
- 説明: 狭い坑道ではグールの多段突撃やアイアン・ヴァンガードの盾が脅威となります. タンクが前線を維持し, 各個撃破で通路の安全を確保してください.
- 要件:
  - mine:minecraft:spawner x3 ※迷宮第2層の判定は構造識別不可のためスポナー破壊数に集約
- 報酬:
  - coinsje:iron_coin x20
  - xp:350
  - minecraft:potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}] x3
  - minecraft:trial_key x1 ※迷宮の鍵→trial_key 代替

### 幽霊船団の主

- id: c4_cornelia
- 説明: 船内は水没しており, 満腹度と隠し満腹度を急速に削る浸水フィールドが展開されます. 無限に湧き出す水兵亡霊を退けつつ, 速攻で主を仕留めましょう.
- 要件:
  - flag:boss_cornelia ※Ghost Cornelia(remnants:ancient_vessel, tag cfb_boss_cornelia)討伐→player_killed_entity 進捗で検出
- 報酬:
  - coinsje:gold_coin x5
  - xp:500
  - minecraft:turtle_helmet x1 ※深海コーラルアーマーの兜→代替
  - minecraft:potion[minecraft:potion_contents={potion:"minecraft:water_breathing"}] x2 ※酸素のポーション→代替

### 伝説の覚醒

- id: c4_legend
- 説明: 攻略報酬限定の固有能力を持つ伝説兵装です. 各職業の潜在能力を極限まで引き上げ, 次なる災厄の主たちとの決戦に備えましょう.
- 要件:
  - have:minecraft:netherite_sword x1 ※「装備して発動」は検出不可のため伝説級武器の所持に代替
- 報酬:
  - coinsje:diamond_coin x1
  - xp:600
  - minecraft:netherite_scrap x2 ※武器研磨石→代替
  - minecraft:writable_book x1 ※スキルリセットの巻物→代替

### 蠢く墓地

- id: c4_ghoul
- 説明: 墓地から湧き出す腐敗の民. 集団突撃を捌き, 数を減らせ.
- 要件:
  - kill:graveyard:ghoul x10
- 報酬:
  - coinsje:iron_coin x10
  - xp:200
  - minecraft:rotten_flesh x32

### 異端の信徒

- id: c4_acolyte
- 説明: 満腹度を削る祈祷を使う信徒たち. 接近戦を許す前に仕留めよう.
- 要件:
  - kill:graveyard:acolyte x5
- 報酬:
  - coinsje:iron_coin x10
  - xp:200
  - minecraft:bone_block x8

### 鋼の盾兵

- id: c4_vanguard
- 説明: 重盾を掲げて歩く遺構の番人. 足止めと背後斬りで崩せ.
- 要件:
  - kill:remnants:rusted_guardian x5
- 報酬:
  - coinsje:iron_coin x15
  - xp:250
  - minecraft:shield x1

### 誓約の剣士

- id: c4_knight
- 説明: 魂を縛られた高機動剣士. 迎え撃つなら距離管理を徹底せよ.
- 要件:
  - kill:remnants:soulbound_knight x5
- 報酬:
  - coinsje:iron_coin x15
  - xp:250
  - minecraft:iron_sword x1
