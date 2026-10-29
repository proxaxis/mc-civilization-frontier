> [ガイド](./GUIDE.md) > MOD 構成リスト

# MOD 構成リスト

これは、本プロジェクトのシステムを構築するために導入した MOD のリストです. すべて Fabric 環境を前提とし、サーバーとクライアント双方への導入を想定しています.

`26.3` 対応版が存在しない MOD は、代替 MOD で役割を補完しています. 除外と代替の対応は末尾の対応表を参照してください.

## 前提

* Minecraft Java Editon 本体バージョン: `26.3`
* Minecraft Server: Fabric for Minecraft 26.3
* Mod Loader: Fabric
* jar 配置場所: `resources/mods/`

## 1. 基盤・RPG・システム系

### 1.1. Fabric API

Fabric 環境の基幹 API. `0.165.0+26.3`

- Wiki: https://wiki.fabricmc.net/start
- Source (GitHub): https://github.com/FabricMC/fabric-api.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/fabric-api

### 1.2. Pufferfish's Skills

守護者、暗殺者、魔道士、聖職者の各タブ画面、ノード配線、レベル管理を行う土台. `0.19.2`

- Wiki: https://puffish.net/skillsmod/docs/
- Source (GitHub): https://github.com/pufmat/skillsmod.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/puffish-skills

### 1.3. Armor Model API

Renders Bedrock/GeckoLib geo armor models through the vanilla armor pipeline. `1.1.2+26.3`

- Source (GitHub): https://github.com/FabricExtras/ArmorModelAPI.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/armor-model-api

### 1.4. Runes

Craft runes to serve as ammo for spells. `1.3.2+26.3`

- Source (GitHub): https://github.com/ZsoltMolnarrr/Runes.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/rune-crafting

### 1.5. Spell Engine

キー入力でアニメーション、当たり判定、エフェクト、クールダウンを処理する戦闘エンジン. `1.10.9+26.3`

- Source (GitHub): https://github.com/ZsoltMolnarrr/SpellEngine.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/spell-engine

### 1.6. Better Combat

Easy, spectacular and fun melee combat system from Minecraft Dungeons. `3.2.2+26.3`

- Source (GitHub): https://github.com/ZsoltMolnarrr/BetterCombat.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/better-combat-by-daedelus

### 1.7. Paladins & Priests (RPG Series)

聖職者、守護者の聖別術式、大盾、メイス、回復、加護の提供. `3.1.4.1+26.3`

- Source (GitHub): https://github.com/ZsoltMolnarrr/Paladins.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/paladins-and-priests

### 1.8. Wizards (RPG Series)

魔道士の属性術式(火炎、凍結、雷撃)、杖、魔導ローブの提供. `3.1.4+26.3`

- Source (GitHub): https://github.com/ZsoltMolnarrr/Wizards.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/wizards

### 1.9. Rogues & Warriors (RPG Series)

暗殺者の短剣、クナイ、煙幕、ステルス、奇襲アクションの提供. `3.1.4.1+26.3`

- Source (GitHub): https://github.com/ZsoltMolnarrr/Rogues.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/rogues-and-warriors

### 1.10. Arsenal (RPG Series)

攻略でしか入手できない固有特殊能力を持つ伝説武器群の提供. `1.5.1+26.3`

- File (Modrinth): https://modrinth.com/mod/arsenal-rpg-series

### 1.11. Ranged Weapon API

Arsenal および RPG Series の遠距離武器処理ライブラリ. `4.0.0+26.3`

- File (Modrinth): https://modrinth.com/mod/ranged-weapon-api

### 1.12. AppleSkin

隠し満腹度(Saturation)とスキル消費時の疲労度バーを画面に表示する. `3.0.10+mc26.3`

- Source (GitHub): https://github.com/squeek502/AppleSkin.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/appleskin

### 1.13. JustQuests / Daily Quests

生活、開拓、兵站、ダンジョン踏破を管理するクエスト系. FTB Quests が `26.3` 非対応のため軽量クエスト MOD 2 本で代替. `0.4.2` / `4.0`

- File (Modrinth): https://modrinth.com/mod/justquests
- File (Modrinth): https://modrinth.com/mod/daily-quests

### 1.14. Lootr (Fabric)

マルチプレイ時におけるダンジョンチェストの個別ルート化(ルート共有、奪い合い防止). `1.25.42.124`

- Source (GitHub): https://github.com/noobanidus/Lootr.git
- File (CurseForge): https://www.curseforge.com/minecraft/mc-mods/lootr-fabric

## 2. 構造物生成系

### 2.1. 共通ライブラリ

* Resourceful Lib `6.0.1`: 一部ダンジョンと構造物 MOD の共通データハンドラ
* Cloth Config API `26.3.159`: 各種 MOD のクライアント、サーバー設定 UI 提供ライブラリ
* Moog's Structure Lib `3.4.2`: Moog's 系構造物 MOD の共通ライブラリ
* GeckoLib `5.5.7`: ボス、敵対モブのアニメーション基盤
* JaUML `3.0.0`: Remnants 系ボスのモデルライブラリ
* Collective `8.68`: Daily Quests の依存ライブラリ
* RPG 系依存ライブラリ: Player Animation Library、Spell Power、Structure Pool API、Trinkets `4.2.1`
* その他ライブラリ: Midnight Lib、Cristel Lib、VillagersTradingPlus、Bookshelf、Polymer、Forge Ender Tech、Fabric Language Kotlin、Forge Config API Port、Puzzles Lib

### 2.2. Dungeons and Taverns シリーズ(バニラ構造物改修)

YUNG's シリーズが `26.3` 非対応のため、同一作者の公式オーバーホール群でバニラ構造物改修を担う.

| MOD | 改修対象 | バージョン |
| --- | --- | --- |
| Dungeons and Taverns | 街道沿いの大型酒場、地下納骨堂、山岳砦 | `6.0.2` |
| D&T Stronghold Overhaul | エンド要塞の多層迷宮化 | `3.0.1` |
| D&T Nether Fortress Overhaul | ネザー要塞の立体拡張 | `3.2.1` |
| D&T Ocean Monument Overhaul | 海底神殿の立体化 | `2.3` |
| D&T Jungle Temple Overhaul | ジャングル寺院の迷宮化 | `2.2` |
| D&T Ancient City Overhaul | 古代都市の拡張 | `3.5.1` |
| D&T Woodland Mansion Overhaul | 森林邸宅の大規模化 | `2.2.1` |
| D&T Pillager Outpost Overhaul | ピリジャー前哨基地の要塞化 | `3.4.2` |
| D&T Swamp Hut Overhaul | 魔女の小屋の拡張 | `2.4` |

- Files: https://www.curseforge.com/minecraft/mc-mods/dungeons-and-taverns

### 2.3. Repurposed Structures

バイオーム派生構造物. ジャングル要塞、砂漠廃坑、エンド寺院など. `7.8.4+26.3`

- Source (GitHub): https://github.com/TelepathicGrunt/RepurposedStructures
- File (Modrinth): https://modrinth.com/mod/repurposed-structures

### 2.4. ダンジョン・迷宮生成

| MOD | 概要 | バージョン |
| --- | --- | --- |
| The Graveyard | 呪われた教会、巨大納骨堂、アンデッドボス | `4.0.0+mc26.3` |
| Grim Kingdoms: Lost Structures & Ruins | 城塞、砦、廃墟と独自モブ勢力 | `2.0.3` |
| Roguelike Dungeons | 地下深く続く多階層ローグライク迷宮 | `2.1.2+mc26.3` |
| DeCubed Dungeons | 複合ダンジョン群(旧 Dungeons+) | `1.13.1` |
| Katters Structures: Dungeon | 高密度ダンジョン構造物群 | `2.9` |
| ATi Structures | 複合ダンジョン、古代施設群 | `1.4.7` |
| AdoraBuild: Structures | バニラ調の砦、工房、住宅群 | `2.12.0` |
| Deadly Deadly Dungeon | 高難度大型ダンジョン | `1.0.6` |
| Structory | 街道沿いの廃墟、廃屋、橋梁 | `1.3.18+26.3` |
| Structory: Towers | 多層階の監視塔、石造城塞タワー | `1.0.19+26.3` |

### 2.5. 空中・海洋・次元構造物

| MOD | 概要 | バージョン |
| --- | --- | --- |
| MSS - Moog's Soaring Structures | 浮遊城塞、空中宮殿、浮島 | `2.2.0` |
| MVS - Moog's Voyager Structures | 巨大船舶、遠征隊キャンプ、交易港 | `5.1.3` |
| MES - Moog's End Structures | エンドの古代塔、浮島迷宮 | `2.1.1` |
| Incendium | ネザー全面改修、パイプライン城塞、大聖所 | `5.5.4+26.3` |
| Nullscape | エンド地形の大規模改修、虚空遺跡 | `2.0.1+26.3` |
| Hopo Better Underwater Ruins | 海底廃墟群の強化 | `1.2.9` |
| Hopo Better Ruined Portals | 荒廃ポータルの強化 | `1.5.2` |

### 2.6. 集落・都市

| MOD | 概要 | バージョン |
| --- | --- | --- |
| Towns and Towers | バイオーム固有の大型村、港湾都市、ピリジャー要塞 | `1.13.13` |
| Lios Overhauled Villages | 高低差に沿った立体改修村(CTOV 代替) | `0.1.2` |
| Tidal Towns | 沿岸の港湾都市、海洋集落 | `2.0` |
| MMV - Moog's Missing Villages | バイオーム未対応地域への追加村 | `2.2.0` |
| Villages&Pillages | ピリジャー勢力の村、前哨集落 | `2.0.0+mc26.3` |

### 2.7. 敵性勢力・ボス・イベント

| MOD | 概要 | バージョン |
| --- | --- | --- |
| Remnants: The Eclipsed Realm | 蝕界の眷属ボス群、討伐構造物 | `3.1.0` |
| Undead Nights | 周期的なアンデッド軍団の夜襲イベント | `2.2.6` |
| Dragonkind Evolved | エンダードラゴン戦の全面改修 | `1.15` |

## 3. 工業・経済・生活系

### 3.1. Build Craft Neo

自作 MOD. エネルギー伝送、パイプ輸送、自動採掘、精錬などの産業基盤.

### 3.2. Coins JE

`MD` 通貨の硬貨基盤. クエスト報酬、ダンジョン討伐、納品報酬としての硬貨流通を想定. `26.3.0.2`

- File (Modrinth): https://modrinth.com/mod/coins-je

### 3.3. Universal Shops

プレイヤー間交易店の提供. 経済循環の実装面を担う. `1.16.0+26.3`

- File (Modrinth): https://modrinth.com/mod/universal-shops

### 3.4. Flan

チャンククレームによる土地保護. 維持費の定期徴収は自作実装が前提. `1.12.8.b`

- File (Modrinth): https://modrinth.com/mod/flan

### 3.5. Pollution of the Realms (AdPother)

環境汚染の蓄積と世界への影響を実装する EFP 基盤 MOD. 重工業稼働、兵器使用との連携は自作設計が前提. `26.3.0.1`

- File (Modrinth): https://modrinth.com/mod/pollution-of-the-realms

### 3.6. 生活ユーティリティ

作業ルーチンを削減する一括採掘系 MOD 群.

* Tree Harvester `10.3`: 原木ブロック破壊で樹木一括伐採、高速落葉、自動植樹
* Veinminer `2.13.0`: 同種ブロックの鉱脈一括採掘(鉱石、土、砂、砂利)
* Leaves Be Gone `26.3.0`: 伐採後の高速落葉処理

### 3.7. Improvement Minecarts

チェーン連結列車、かまど機関車、駅、レッドストーン制御を提供する鉄道拡張. 列車ダイヤの完全自律化は自作実装が前提. `1.3.0`

- File (Modrinth): https://modrinth.com/mod/improvement-minecarts

### 3.8. Farmer's Delight Refabricated

料理と農業の拡張. 食料兵站のバリエーションと料理レーションの実装面を担う. `3.6.30`

- File (Modrinth): https://modrinth.com/mod/farmers-delight-refabricated

## 4. 除外 MOD と代替対応表

以下は当初の構成候補だったが `26.3` 対応版が存在しないため未導入の MOD と、その役割を引き継ぐ代替です.

| 未導入 MOD | 最新対応版 | 代替 |
| --- | --- | --- |
| FTB Quests | `26.1.2` | JustQuests、Daily Quests |
| YUNG's API | `26.1.2` | D&T Overhaul 群は共通ライブラリ不要のため代替不要 |
| YUNG's Better Strongholds | `26.1.2` | D&T Stronghold Overhaul |
| YUNG's Better Dungeons | `26.1.2` | DeCubed Dungeons、D&T 各種 Overhaul |
| YUNG's Better Nether Fortresses | `26.1.2` | D&T Nether Fortress Overhaul |
| YUNG's Better Mineshafts | `26.1.2` | Katters Structures: Dungeon |
| YUNG's Better Ocean Monuments | `26.1.2` | D&T Ocean Monument Overhaul |
| YUNG's Better Desert Temples | `26.1.2` | 代替なし(Repurposed Structures の砂漠派生構造物で一部補完) |
| YUNG's Better Jungle Temples | `26.1.2` | D&T Jungle Temple Overhaul |
| YUNG's Better End Island | `26.1.2` | Nullscape、MES、Dragonkind Evolved |
| When Dungeons Arise | `1.21.1` | MSS - Moog's Soaring Structures、AdoraBuild |
| When Dungeons Arise: Seven Seas | `26.1.2` | MVS - Moog's Voyager Structures |
| Aquamirae | `1.21.1` | Deadly Deadly Dungeon |
| IDAS | Fabric 版なし | ATi Structures、Katters Structures |
| Dungeon Crawl | Fabric 版なし | Roguelike Dungeons |
| Masquerade | `26.2` | Remnants: The Eclipsed Realm、D&T Woodland Mansion Overhaul |
| CTOV | `26.2` | Lios Overhauled Villages、Tidal Towns、MMV、Villages&Pillages |
| Simply Swords | `1.21.1` | Arsenal (RPG Series) |
| Better Nether | `26.3` 対応あり | Incendium との生成系競合のため不採用 |
