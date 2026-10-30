# 第 1 章 - ⛏️開拓

## 説明

大災厄の爪痕が残る未踏の荒野に降り立ったあなたが, まず成すべきは生き延びることだ. 己の背中を預ける職業を定め, 身を守るための初期武具を整えよ. 街道沿いに点在する酒場や要塞化された村を探し出し, 暖炉とベッドのある安全な拠点を確保することが, 長き開拓史の最初の礎となる.

## 目的

超古代文明の廃墟と徘徊する怪物たちに囲まれた過酷な地表環境において, 初日の安全な避難所を確保し, 初期装備と基礎的な採掘基盤を確立する.

## 要件の書式

- `deliver:item_id xN` — アイテム N 個を消費して納品
- `kill:entity_id xN` — 累計討伐数 N 体
- `advancement:advancement_id` — 指定進捗を達成
- `have:item_id xN` — アイテム N 個を所持(消費しない)
- `flag:name` — カスタム条件(flag:job_selected=職業選択済, flag:skill_learned=スキル習得済)

## クエスト一覧

### 運命の選択

- id: c1_choice
- 説明: この世界で生き抜くための役割を決定します. 前線で耐える守護者, 影から急所を刺す暗殺者, 広域属性術を操る魔道士, 治癒と加護を司る聖職者から, 自身の適性に合った道を選んでください.
- 要件:
  - flag:job_selected
- 報酬:
  - coinsje:copper_coin x5
  - xp:100
  - minecraft:shield x1 ※職業別武具は盾で共通代替
  - coinsje:copper_coin x5

### 荒野の生命線

- id: c1_shelter
- 説明: 地表の夜は危険に満ちています. 街道沿いに点在する酒場にはベッド, 暖炉, ビール樽, 初期食料が整っており, 最初の夜を安全にやり過ごすためのシェルターとして機能します.
- 要件:
  - advancement:minecraft:adventure/sleep_in_bed ※「酒場到達」は構造ID未定のため就寝進捗に代替
- 報酬:
  - coinsje:copper_coin x5
  - xp:50
  - minecraft:bread x8
  - minecraft:charcoal x16

### 原始の道具と身体拡張

- id: c1_tools
- 説明: 過酷な開拓を支援するアシスト機能が標準搭載されています. 原木の一括伐採や, 同種鉱石・土砂の一括掘削を駆使して, 効率的に初期資源を集めましょう.
- 要件:
  - deliver:minecraft:oak_log x32 ※一括伐採利用は検出不可のため納品に代替
  - have:minecraft:stone_pickaxe x1
- 報酬:
  - coinsje:copper_coin x5
  - xp:50
  - minecraft:cooked_cod x8
  - minecraft:torch x32

### 初陣の備え

- id: c1_armed
- 説明: 祠やチェストから基本防具を整え, 獲得した経験値で最初のパッシブスキルを解放しましょう. 習得済みのパッシブスキルは死亡しても消滅しません.
- 要件:
  - have:minecraft:iron_chestplate x1 ※「祠チェストからの入手」は由来追跡不可のため鉄装備所持に代替
  - flag:skill_learned ※スキル習得検出は要 puffish_skills 連携(未実装, 後続タスク)
- 報酬:
  - coinsje:copper_coin x10
  - xp:100
  - minecraft:iron_ingot x3
  - minecraft:golden_apple x1 ※包帯→金リンゴ代替

### 灯火の確保

- id: c1_torch
- 説明: 夜を越えるには光が命綱. 湧き潰しと道標を兼ねた松明を量産しよう.
- 要件:
  - deliver:minecraft:torch x64
- 報酬:
  - coinsje:copper_coin x5
  - xp:50
  - minecraft:coal x16

### 初めての術式

- id: c1_magic
- 説明: 職業に応じた呪文書を呪文結合台で結合しましょう. スクロールとラピスラズリを捧げて, 初めての術式を紡ぎます.
- 要件:
  - flag:job_selected
  - flag:spell_bound ※呪文書の結合検出は専用判定
- 報酬:
  - coinsje:copper_coin x10
  - xp:100
  - minecraft:lapis_lazuli x8
