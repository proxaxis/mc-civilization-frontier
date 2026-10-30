# 第 6 章 - 👑終焉

## 説明

空を切り裂く浮遊城塞の覇王, 死者を統べる腐敗の司教, そして世界の境界で待ち受ける終焉の龍. かつて超古代文明を呑み込んだ災厄の核へと挑む時が来た. 培った工業力, 張り巡らせた経済網, 鍛え上げた仲間との絆, そのすべてを懸けて決戦へ赴け. ――創世か, 終焉か. 時代の結末を刻むのは君だ.

## 目的

最上位の脅威度(Tier 4)を誇る災厄の主たちを各個撃破し, 超古代文明を崩壊させた大災厄の根源を断ち切ってフロンティアに真の創世をもたらす.

## クエスト一覧

### 腐敗の大聖堂

- id: c6_cathedral
- 説明: 満腹度を強制的に削り取る「飢餓の波浪」と, 周囲の骨塚(スポナー)による無敵障壁を展開する不死の司教です. 骨塚を速やかに破砕し, 特級レーションで飢えを凌ぎながら討ち果たしましょう.
- 要件:
  - flag:boss_lich ※Corrupted Lich(graveyard:lich, tag cfb_boss_lich)討伐→player_killed_entity 進捗で検出
- 報酬:
  - coinsje:gold_coin x10
  - xp:800
  - minecraft:echo_shard x4 ※魂の防壁の欠片→代替
  - minecraft:netherite_ingot x2

### 天空の覇王

- id: c6_skyking
- 説明: 甲板上は激しい強風が吹き荒れ, 絨毯爆撃のような掃射とノックバックによる地上への落下死が最大の脅威です. ノックバック耐性を極限まで高めて挑んでください.
- 要件:
  - flag:boss_flagship ※Flagship Core(remnants:hollow_sovereign, tag cfb_boss_flagship)討伐→player_killed_entity 進捗で検出
- 報酬:
  - coinsje:gold_coin x10
  - xp:800
  - minecraft:beacon x1 ※天空のコアブロック→代替
  - minecraft:totem_of_undying x1 ※風のトーテム→代替

### 蝕界の深淵

- id: c6_eclipse
- 説明: 被ダメージの蓄積に応じて最大体力を削り取る「蝕の侵食」を行使する異界の王です. 波状攻撃を仕掛ける眷属を排除しつつ, 聖職者の浄化と速攻の一撃で押し切る必要があります.
- 要件:
  - flag:boss_eclipse ※Eclipse Remnant(tag cfb_boss_eclipse)討伐→player_killed_entity 進捗で検出
- 報酬:
  - coinsje:diamond_coin x2
  - xp:1000
  - minecraft:heart_of_the_sea x1 ※蝕の心臓→代替
  - minecraft:recovery_compass x1 ※古代の遺物ボックス→代替

### 終焉の龍と新時代の夜明け

- id: c6_dragon
- 説明: 多段フェーズと広域爆発を操る災厄の究極形態です. 奈落への落下を防ぎつつ, 全工業・経済・兵站・戦闘技術の粋を集結させて撃破し, フロンティアの未来を切り拓いてください.
- 要件:
  - advancement:minecraft:end/kill_dragon ※強化進化形態の個体識別は不可のためバニラ討伐進捗に代替
- 報酬:
  - coinsje:netherite_coin x1
  - xp:2000
  - minecraft:dragon_egg x1 ※創世の核→ドラゴンの卵
  - minecraft:dragon_head x1 ※覇者のトロフィー→代替

### 眷属の排除

- id: c6_sovereign
- 説明: 蝕界の眷属は王の手足. 先に群れを削り, 王座への道を切り開け.
- 要件:
  - kill:remnants:hollow_sovereign x3
- 報酬:
  - coinsje:gold_coin x3
  - xp:400
  - minecraft:obsidian x16

### 炎の絨毯

- id: c6_blaze
- 説明: ネザーの炎は容赦ない. ブレイズの数を減らして進軍路を確保せよ.
- 要件:
  - kill:minecraft:blaze x10
- 報酬:
  - coinsje:gold_coin x3
  - xp:400
  - minecraft:blaze_rod x8

### 残響の舞姫

- id: c6_wraith
- 説明: 分身と残像で惑わす記憶の亡霊. 本体を見極め, 一撃で仕留めよ.
- 要件:
  - kill:remnants:memory_wraith x5
- 報酬:
  - coinsje:gold_coin x5
  - xp:500
  - minecraft:ender_pearl x8
