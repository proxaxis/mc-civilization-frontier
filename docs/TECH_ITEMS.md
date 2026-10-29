# 科学技術アイテムとレシピのカタログ

BCN (Build Craft Neo) 実装用に、BuildCraft / IndustrialCraft2 / Project:Red / LogisticsPipes の
鉱石、アイテム、機械、パイプ類とそのレシピを整理した一覧. レシピの正本は各 `resources/*/ITEMS.md` (wiki 仕様)
であり、`公式レシピ` 行は現行公式データ(IC2C 1.19.2 / BC 7.99 / PR 1.21.1 / LP 1.12.2)からの近似参照.

## 0. エネルギー単位の統一 (EU -> MJ)

旧 IC2 の電力 EU は BCN では MJ (BuildCraft 系) に統一する. 変換基準は以下を採用(実装時に要確定).

| 単位 | 換算 | 備考 |
|---|---|---|
| 1 EU | 0.4 MJ (=4RF) | IC2 伝統換算 1EU=4RF に準拠 |
| 1 MJ | 10 RF (2.5 EU) | BC6 系の MJ-RF 換算 |
| 1 LP | LogisticsPipes 内部単位 | LP変換機で MJ/EU から変換 (要 BCN 側 MJ) |

主な発電量の MJ 換算目安 (wiki 記載 EU 値を 0.4 倍):

| 装置 | 旧 EU/t | 換算 MJ/t |
|---|---|---|
| 火力発電機 | 10 | 4 |
| 地熱発電機 | 20 | 8 |
| ソーラーパネル | 1 | 0.4 |
| 水力発電 | 0.25 | 0.1 |
| 風力発電機 | 0-5 | 0-2 |
| 原子炉(ウラン1本) | 5 | 2 |
| 石エンジン(BC) | - | 1 |
| 鉄エンジン(BC) | - | 6 |

### 0.1. 圧力 (電圧) 概念の維持

単位は MJ に統一するが、IC2 の「EU-Packet による電圧段階」は BCN の MJ 送電設計にも引き継ぐ.

- MJ はパケットの塊として送電され、パケットサイズで電圧ランクが決まる. ケーブルには許容パケット圧があり、超過すると蒸発 (感電ではなくケーブル消失)
- 損失はパケット1個につき 1 ブロックあたりの固定量を減衰させる方式. パケットを大きくまとめた方が損失は少ない (=高圧送電の優位性)

| ランク | 旧IC2 (EU-P) | BCN 換算 (MJ-P) | 用途目安 |
|---|---|---|---|
| 超低圧 | 5 | 2 | 微弱発電 (ソーラー, スズ線) |
| 低圧 | 32 | 12.8 | 一般機械, 銅線 |
| 中圧 | 128 | 51.2 | 上位機械, 金線 |
| 高圧 | 512 | 204.8 | グラスファイバー級 |
| 超高圧 | 2048 | 819.2 | 長距離送電専用, HV線 |

- 変圧器でパケット分割/統合 (大 1 → 小 4 方向のみ基本) する仕組みを残し、蓄電器の入出力圧もランクに紐付ける
- BC の伝導パイプ側は無圧力の量搬送 (旧仕様) のままとし、「IC2 系の圧力管理」と「BC 系の流量管理」を使い分ける

## 1. 実装上の考慮事項

- ワールド生成コンテンツ(鉱石、大理石、油田)は既に bcn MOD で先行実装済み. 本表の残りはブロック/アイテム登録と機能実装(タスク 200)
- エネルギー基盤: Fabric では Team Reborn Energy API (RF 相当) が主流だが、MJ ネットワークは独自実装とし、RF 変換は変換機経由に留める (旧 BC の MJ 伝導パイプ損失モデルを再現)
- パイプ類: 輸送(アイテム)/防水(液体)/伝導(動力)の3系統は BC 仕様どおり分離する. 26.3 の BlockEntity + 独自パケット同期で実装. LP はこのパイプ上に乗るアドオン構造
- 機械類: TileEntity + 進行度 NBT + インベントリ自動搬入出対応 (ホッパー互換). クァーリーはチャンクロードと設置者権限の扱いが課題 (FakePlayer 運用)
- ゲート/チップセット: Assembly Table 系は RF 消費型の独自クラフト UI. 条件/トリガーはレッドストーン論理として設計
- IC2 系機械: macerator/extractor/compressor/canner/alloy smelter 等の処理レシピは `recipes_export.json`, `machine_recipes_export.json` を基にデータ駆動化
- ID 命名: `bcn:` 名前空間. アセットは `resources/` の既存物を優先流用 (textures/models/lang)
- 未対応項目: 公式レシピが取れなかった項目(パイプ多数、モジュール類)は LP `src/` コードまたは wiki 記述を正本として実装

## 2. EFP (環境負荷指数) との連携方針

EFP は重工業稼働で世界全体に蓄積する汚染指数 (`cfb_efp` スコアボードで datapack 側が管理済み, task-104 実装).

| 発生源 (想定) | EFP 加算 | 方針 |
|---|---|---|
| 火力/内燃機関の連続稼働 | 稼働時間比例 | tick 単位で scoreboard 加算 (mod から `/scoreboard players add` 相当を呼ぶ or datapack 側が機械ブロックの稼働検知) |
| クァーリー採掘 (環境破壊) | 掘削ブロック数比例 | 土地維持費 (MD 通貨) との併罰. 掘削範囲登録で二重課徴防止 |
| 原子炉/爆発事故 | 事故規模比例 | メルトダウン時に大量加算 |
| リファイナリー精製 | バケツ単位 | 少量継続加算 |
| 油田の汲み上げ | 源泉枯渇で加算 | 有限資源インセンティブ |
| 低減要素 | 植林/グリーン電力稼働で減算 | ソーラー/風力/水力は EFP ゼロか微減算でクリーン電力の優位性を作る |

実装方式の候補:
1. BCN (Java) 側から `cfb_efp` スコアボードを直接更新 (最も正確. mod 側で稼働状態が取れる)
2. datapack が稼働中機械を定期スキャンして加算 (Java 改修なしで先行実装可能)
3. 暫定は既存の手動加算ボタン運用 → 本実装で 1. に移行

### 2.1. Pollution of the Realms (AdPother) との役割分担

導入済みの AdPother (26.3.0.1) は「物理的汚染ブロック」(局所に漂う汚染物質, エンティティ影響, 換気)を担うMODで、EFP の「世界全体の累積指数→災害トリガー」とは別レイヤー. 併用を推奨.

- 排出源登録: BCN のエンジン, 各種炉, リファイナリー, クァーリー, 核施設を AdPother の設定 JSON で emitter 登録 → 視覚的な汚染と実害がコード無しで付く
- EFP 指数: `cfb_efp` スコアボードをグローバル指標として維持 (災害トリガーは datapack 実装済み)
- 同期: PotR jar を `modImplementation` に加えて汚染量 API を読み取り、`cfb_efp` に反映 (task-104 報告の方針と同じ. 実測値連動にしたい場合はこれ)

## 3. アイテム,レシピ一覧

以降は MOD 別の一覧. `ID` は公式データで確認できたレジストリ名 (無いものは `-`).
`レシピ` は wiki 仕様と公式データの両方を略記 (括弧内は出典マーク: 無印=wiki, [公式]=公式データ).



## BuildCraft (7.99 / MC1.12.2)


### 1. 部品、ツール

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| レンチ | Wrench | 石の歯車1 + 鉄インゴット3<br>公式レシピ: (定型) ore:ingotIronx3 + ore:gearStone -> buildcraftcore:wrench |
| 木の歯車 | Wooden Gear | 棒4<br>公式レシピ: (定型) ore:stickWoodx4 -> buildcraftcore:gear_wood |
| 石の歯車 | Stone Gear | 木の歯車1 + 丸石4<br>公式レシピ: (定型) ore:cobblestonex4 + ore:gearWood -> buildcraftcore:gear_stone |
| 鉄の歯車 | Iron Gear | 石の歯車1 + 鉄インゴット4<br>公式レシピ: (定型) minecraft:iron_ingotx4 + ore:gearStone -> buildcraftcore:gear_iron |
| 金の歯車 | Gold Gear | 鉄の歯車1 + 金インゴット4<br>公式レシピ: (定型) minecraft:gold_ingotx4 + ore:gearIron -> buildcraftcore:gear_gold |
| ダイヤの歯車 | Diamond Gear | 金の歯車1 + ダイヤモンド4<br>公式レシピ: (定型) minecraft:diamondx4 + ore:gearGold -> buildcraftcore:gear_diamond |
| 無色の絵筆 | Paintbrush | 木の歯車1 + 棒1 + 羊毛1 + 糸2 |
| 染色済みの絵筆 | Paintbrush | 無色の絵筆1 + 各色染料1 |

### 2. エンジン

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 木エンジン | Redstone Engine | ガラス1 + 木材3 + 木の歯車2 + ピストン1<br>公式レシピ: (定型) ore:plankWoodx3 + ore:blockGlassColorless + ore:gearWoodx2 + minecraft:piston -> buildcraftcore:engine#0 |
| 石エンジン | Stirling Engine | ガラス1 + 丸石3 + 石の歯車2 + ピストン1<br>公式レシピ: (定型) ore:cobblestonex3 + ore:blockGlassColorless + ore:gearStonex2 + minecraft:piston -> buildcraftcore:engine#1 |
| 鉄エンジン | Combustion Engine | ガラス1 + 鉄インゴット3 + 鉄の歯車2 + ピストン1<br>公式レシピ: (定型) minecraft:iron_ingotx3 + ore:blockGlassColorless + ore:gearIronx2 + minecraft:piston -> buildcraftcore:engine#2 |
| クリエイティブエンジン | Creative Engine | なし (クラフト不可) |
| 黒オイル | Oil | なし (クラフト不可) |
| 黒オイルバケツ | Oil Bucket | なし (クラフト不可) |
| 金オイルバケツ | Fuel Bucket | なし (クラフト不可) |

### 3. パイプ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| アイテムパイプ | 輸送パイプ | - |
| 木の輸送パイプ | Wooden Transport Pipe | ガラス1 + 木材2 |
| 丸石の輸送パイプ | Cobblestone Transport Pipe | ガラス1 + 丸石2 |
| 焼石の輸送パイプ | Stone Transport Pipe | ガラス1 + 焼石2 |
| 砂岩の輸送パイプ | Sandstone Transport Pipe | ガラス1 + 砂岩2 |
| 鉄の輸送パイプ | Iron Transport Pipe | ガラス1 + 鉄インゴット2 |
| 金の輸送パイプ | Golden Transport Pipe | ガラス1 + 金インゴット2 |
| 黒曜石の輸送パイプ | Obsidian Transport Pipe | ガラス1 + 黒曜石2 |
| エメラルドの輸送パイプ | Emerald Transport Pipe | ガラス1 + エメラルド2 |
| ダイヤモンドの輸送パイプ | Diamond Transport Pipe | ガラス1 + ダイヤモンド2 |
| 虚空の輸送パイプ | Void Transport Pipe | ガラス1 + イカスミ1 + 赤石粉1 |
| ストライプの輸送パイプ | Stripes Transport Pipe | 【v3.2.0 pre6以前】ガラス1 + イカスミ1 + 黄の染料1 / 【v6.0.2以降】ガラス1 + 金の歯車2 |
| 水晶の輸送パイプ | Quartz Transport Pipe | ガラス1 + ネザー水晶ブロック |
| ラピスラズリの輸送パイプ | Lazuli Transport Pipe | ガラス1 + ラピスラズリブロック2 |
| ダイズリの輸送パイプ | Daizuli Transport Pipe | ガラス1 + ラピスラズリブロック1 + ダイヤモンド1 |
| エメズリの輸送パイプ | Emzuli Transport Pipe | ガラス1 + ラピスラズリブロック1 + エメラルド1 |
| 粘土の輸送パイプ | Clay Transport Pipe | ガラス1 + 粘土ブロック2 |
| 液体パイプ | 防水パイプ | - |
| パイプの防水材 | Pipe Sealant | 緑の染料1 or スライムボール1<br>公式レシピ: (不定形) minecraft:dye#2 -> buildcrafttransport:waterproof |
| 木の防水パイプ | Wooden Fluid Pipe | 防水素材1 + 木の輸送パイプ1 |
| 丸石の防水パイプ | Cobblestone Fluid Pipe | 防水素材1 + 丸石の輸送パイプ1 |
| 焼石の防水パイプ | Stone Fluid Pipe | 防水素材1 + 焼石の輸送パイプ1 |
| 砂岩の防水パイプ | Sandstone Fluid Pipe | 防水素材1 + 砂岩の輸送パイプ1 |
| 鉄の防水パイプ | Iron Fluid Pipe | 防水素材1 + 鉄の輸送パイプ1 |
| 金の防水パイプ | Golden Fluid Pipe | 防水素材1 + 金の輸送パイプ1 |
| エメラルドの防水パイプ | Emerald Fluid Pipe | 防水素材1 + エメラルドの輸送パイプ1 |
| 虚空の防水パイプ | Void Fluid Pipe | 防水素材1 + 虚空の輸送パイプ1 |
| 水晶の防水パイプ | Quartz Fluid Pipe | 防水素材1 + 水晶の輸送パイプ1 |
| ダイヤモンドの防水パイプ | Diamond Fluid Pipe | 防水素材1 + ダイヤモンドの輸送パイプ1 |
| 粘土の防水パイプ | Clay Fluid Pipe | 防水素材1 + 粘土の輸送パイプ1 |
| エネルギーパイプ | 伝導パイプ | - |
| 木の伝導パイプ | Wooden Conductive Pipe | レッドストーン1 + 木の輸送パイプ1 |
| 丸石の伝導パイプ | Cobblestone Kinesis Pipe | レッドストーン 1 + 丸石の輸送パイプ1 |
| 焼石の伝導パイプ | Stone Kinesis Pipe | レッドストーン1 + 焼石の輸送パイプ1 |
| 水晶の伝導パイプ | Quartz Kinesis Pipe | レッドストーン1 + 水晶の輸送パイプ1 |
| 鉄の伝導パイプ | Iron Kinesis Pipe | レッドストーン1 + 鉄の輸送パイプ |
| 金の伝導パイプ | Golden Kinesis Pipe | レッドストーン1 + 金の輸送パイプ1 |
| ダイヤモンドの伝導パイプ | Diamond Kinesis Pipe | レッドストーン1 + ダイヤモンドの輸送パイプ1 |

### 3.4. その他のパイプ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 丸石の構造パイプ | Cobblestone Structure Pipe | 丸石2 + 砂利1 |
| パイプ栓 | Pipe Plug | 丸石の構造パイプ1 |

### 3.5. レンズ、フィルター

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| レンズ | Lens | 染料1 + ガラス1 |
| フィルター | Filter | 染料1 + ガラス1 + 鉄格子1 |

### 4. マシン（旧factory）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 漏斗 | Chute | 石の歯車1 + チェスト1 + 鉄インゴット5<br>公式レシピ: (定型) ore:ingotIronx5 + minecraft:chest + ore:gearStone -> buildcraftfactory:chute |
| 全自動作業台 | Auto Workbench | 作業台1 + 石の歯車2 |
| ボーリングマシン | Mining Well | 鉄の歯車1 + 鉄インゴット6 + 赤石粉1 + 鉄つるはし1<br>公式レシピ: (定型) ore:ingotIronx6 + ore:dustRedstone + ore:gearIron + minecraft:iron_pickaxe -> buildcraftfactory:mining_well |
| クァーリー | Quarry | 鉄の歯車3 + 赤石1 + 金の歯車2 + ダイヤ歯車2 + ダイヤつるはし1<br>公式レシピ: (定型) ore:gearIronx3 + ore:dustRedstone + ore:gearGoldx2 + ore:gearDiamondx2 + minecraft:diamond_pickaxe -> buildcraftbuilders:quarry |
| 液体タンク | Tank | ガラス8<br>公式レシピ: (定型) ore:blockGlassColorlessx8 -> buildcraftfactory:tank |
| ポンプ | Pump | 鉄インゴット4 + レッドストーン1 + 鉄の歯車1 + 液体タンク2 + バケツ1<br>公式レシピ: (定型) ore:ingotIronx4 + ore:dustRedstone + ore:gearIron + buildcraftfactory:tankx2 + minecraft:bucket -> buildcraftfactory:pump |
| リファイナリー | Refinery | 液体タンク3 + 赤石トーチ2 + ダイヤ歯車1 |
| 選別緩衝器 | Filtered Buffer | 木材6 + ダイヤモンドの輸送パイプ1 + チェスト1 + ピストン1<br>公式レシピ: (定型) ore:plankWoodx6 + buildcrafttransport:pipe_diamond_item#0 + minecraft:chest + minecraft:piston -> buildcrafttransport:filtered_buffer |
| 水門 | Flood Gate | 鉄インゴット4 + 鉄格子3 + 鉄の歯車1 + 液体タンク1<br>公式レシピ: (定型) ore:ingotIronx4 + ore:gearIron + minecraft:iron_barsx3 + buildcraftfactory:tank -> buildcraftfactory:flood_gate |

### 5. マシン（旧builder）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 青マーカー | Land Mark | ラピスラズリ1 + 赤石トーチ1 |
| フィラー | Filler | 作業台1 + イカスミ2 + 青マーカー1 + 黄の染料2 + 金の歯車2 + チェスト1 |
| テンプレート | Template | イカスミ + 紙8<br>公式レシピ: (定型) minecraft:paperx8 + ore:dyeBlack -> buildcraftbuilders:snapshot#0 |
| ブループリント | Blueprint | ラピスラズリ + 紙8<br>公式レシピ: (定型) minecraft:paperx8 + ore:gemLapis -> buildcraftbuilders:snapshot#2 |
| 範囲指定マップ | Map Location | 黄色の染料1 + 紙8 |
| リスト | List | 緑色の染料1 + 紙8 + レッドストーン1<br>公式レシピ: (定型) minecraft:paperx7 + ore:dustRedstone + minecraft:dye#2 -> buildcraftcore:list |
| 製図机 | Architect Table | 作業台1 + イカスミ2 + 青マーカー1 + 黄の染料2 + ダイヤ歯車2 + ブループリント1<br>公式レシピ: (定型) ore:dyeBlackx2 + buildcraftcore:marker_volume + ore:dyeYellowx2 + minecraft:crafting_table + ore:gearDiamondx2 + buildcraftbuilders:snapshot#2 -> buildcraftbuilders:architect |
| 製図棚 | Electronic Library | 本棚2 + ブループリント1 + 鉄インゴット4 + 鉄の歯車1 + レッドストーン1 |
| ビルダー | Builder | 作業台1 + イカスミ2 + 青マーカー1 + 黄の染料2 + ダイヤ歯車2 + チェスト1 |
| 緑マーカー | Path Mark | 緑の染料1 + 赤石トーチ1 |
| 建築マーカー | Construction Mark | 金の歯車1 + 赤石トーチ1 |
| アイテム要求機 | Requester | 鉄インゴット4 + 鉄の歯車2 + ピストン1 + チェスト1 + レッドストーン1 |
| 範囲指定機 | Zone Planner | 鉄インゴット4 + 金の歯車2 + ダイヤの歯車1 + 地図1 + レッドストーン1 |

### 6. 精密作業台＆ゲート関連

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ゲートコピー機 | Gate Copier | レンチ1 + 赤石チップ1 |
| レーザー | Laser | 黒曜石2 + ダイヤ2 + レッドストーン5<br>公式レシピ: (定型) ore:dustRedstonex5 + minecraft:obsidianx2 + ore:gemDiamondx2 -> buildcraftsilicon:laser |
| 精密作業台 | Assembly Table | 黒曜石6 + ダイヤ1 + レッドストーン1 + ダイヤ歯車1<br>公式レシピ: (定型) minecraft:obsidianx6 + ore:gemDiamond + ore:dustRedstone + ore:gearDiamond -> buildcraftsilicon:assembly_table |
| 全自動作業台、改 | Advanced Crafting Table | 黒曜石6 + チェスト1 + 作業台1 + 赤石チップ1<br>公式レシピ: (定型) minecraft:obsidianx6 + ore:craftingTableWood + ore:chestWood + buildcraftsilicon:redstone_chipset#0 -> buildcraftsilicon:advanced_crafting_table |
| レーザー合成台 | Integration Table | 黒曜石6 + 金インゴット1 + 赤石チップ1 + ダイヤ歯車1<br>公式レシピ: (定型) minecraft:obsidianx6 + ore:ingotGold + buildcraftsilicon:redstone_chipset#1 + ore:gearDiamond -> buildcraftsilicon:integration_table |
| スタンピングテーブル | Charging Table | 黒曜石6 + 作業台1 + 赤石チップ1 + 金の歯車1 |

### 6.7. パイプワイヤー

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 赤ワイヤー | Red Pipe Wire | 赤ワイヤー8 ＝ 赤石粉1 + 鉄インゴット1 + 赤の染料1（5,000RF） |
| 青ワイヤー | Blue Pipe Wire | 青ワイヤー8 ＝ 赤石粉1 + 鉄インゴット1 + ラピスラズリ1（5,000RF） |
| 緑ワイヤー | Green Pipe Wire | 緑ワイヤー8 ＝ 赤石粉1 + 鉄インゴット1 + 緑の染料1（5,000RF） |
| 黄ワイヤー | Yellow Pipe Wire | 黄ワイヤー8 ＝ 赤石粉1 + 鉄インゴット1 + 黄の染料1（5,000RF） |

### 6.8. チップ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 赤石チップ | Redstone Chipset | 赤石チップ1 ＝ 赤石粉1（100,000RF）<br>公式レシピ: (精密作業台) ore:dustRedstone -> buildcraftsilicon:redstone_chipset#0 (10000MJ) |
| 鉄チップ | Iron Chipset | 鉄チップ1 ＝ 赤石粉1 + 鉄インゴット1（200,000RF）<br>公式レシピ: (精密作業台) ore:dustRedstone + ore:ingotIron -> buildcraftsilicon:redstone_chipset#1 (20000MJ) |
| 金チップ | Golden Chipset | 金チップ1 ＝ 赤石粉1 + 金インゴット1（400,000RF）<br>公式レシピ: (精密作業台) ore:dustRedstone + ore:ingotGold -> buildcraftsilicon:redstone_chipset#2 (40000MJ) |
| ダイヤチップ | Diamond Chipset | ダイヤチップ1 ＝ 赤石粉1 + ダイヤ1（800,000RF）<br>公式レシピ: (精密作業台) ore:dustRedstone + ore:gemDiamond -> buildcraftsilicon:redstone_chipset#4 (80000MJ) |
| パルサー | Pulsating Chipset | パルサーチップ2 ＝ 赤石粉1 + エンダーパール1（400,000RF） |
| 水晶チップ | Quartz Chipset | 水晶チップ1 ＝ 赤石粉1 + ネザー水晶1（600,000RF）<br>公式レシピ: (精密作業台) ore:dustRedstone + ore:gemQuartz -> buildcraftsilicon:redstone_chipset#3 (60000MJ) |
| コンパレータチップ | Redstone Comp Chipset | コンパレータチップ1 ＝ 赤石粉1 + ダイヤ1（600,000RF） |
| エメラルドチップ | Emerald Chipset | エメラルドチップ1 ＝ 赤石粉1 + エメラルド1（1,200,000RF） |

### 6.9. ゲート

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 赤石ゲート | Gate | 赤石ゲート1 ＝ 赤石チップ1 + 赤ワイヤー1（100,000RF） |
| 鉄ゲート（AND/OR） | Iron AND Gate / Iron OR Gate | 鉄ゲート1 ＝ 鉄チップ1 + 赤ワイヤー1 + 青ワイヤー1（200,000RF） |
| 金ゲート（AND/OR） | Gold AND Gate / Gold OR Gate | 金ゲート1 ＝ 金チップ1 + 赤ワイヤー1 + 青ワイヤー1 + 緑ワイヤー1（400,000RF） |
| ダイヤゲート（AND/OR） | Diamond AND Gate / Diamond OR Gate | ダイヤゲート1 ＝ ダイヤチップ1 + ワイヤー全色1ずつ（800,000RF） |
| エメラルドゲート（AND/OR） | Emerald AND Gate / Emerald OR Gate | エメラルドゲート1 ＝ エメラルドチップ1 + ワイヤー全色1ずつ（1,200,000RF） |
| 水晶ゲート（AND/OR） | Quartz AND Gate / Quartz OR Gate | 水晶ゲート1 ＝ 水晶チップ1 + 赤ワイヤー1 + 青ワイヤー1 + 緑ワイヤー1（600,000RF） |

### 6.10. ゲートの改造

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| クロックタイマー | Clock Timer | クロックタイマー1 ＝ 各種ゲート1 + 水晶チップ1（25,000RF） |
| レッドストーンフェーダー | Redstone Fader | レッドストーンフェーダー1 ＝ 各種ゲート1 + レッドストーンコンパレータ1（25,000RF） |
| オートチックパルサー | Autarchic Pulsar | オートチックパルサー1 ＝ 各種ゲート1 + パルサーチップ1（25,000RF） |
| ライトセンサー | Light Sensor | ライトセンサー1 ＝ 各種ゲート1 + 日照センサー1（25,000RF） |

### 6.11. 外装パネル

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 外装パネル | Facade: xxxx | 外装パネル6 ＝ ブロック1 + 砂利パイプ3（8,000RF） |

### 7. ロボット関連

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| レッドストーンボード | Redstone Board | レッドストーン1 + 紙8 |
| レッドストーンクリスタル | Redstone Crystal | レッドストーンブロック1（10,000,000RF） |
| ロボット | Robot | 鉄インゴット5 + レッドストーンクリスタル1 + ダイヤチップ2 |
| ドック | Docking Station | 鉄のインゴット3 + 金チップ |

### 7.5. Programming Table

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| レーザー充電台 | Charging Table | 黒曜石6 + レッドストーン1 + 赤石チップ1 + 金の歯車1 |

(113 項目)

## IndustrialCraft² (wiki=Experimental 準拠, 公式=IC2 Classic 1.19.2)


### 1. 発電装置

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 火力発電機 | Generator | 充電式電池：１＋精錬鉄：３＋鉄のかまど：１ / 充電式電池：１＋マシン：１＋かまど：１<br>公式レシピ: (定型) ic2:re_battery + #forge:ingots/refined_ironx3 + ic2:iron_furnace -> ic2:generator<br>公式レシピ: (定型) ic2:re_battery + ic2:machine_block + minecraft:furnace -> ic2:generator |
| 地熱発電機 | Geothermal Generator | ガラス：４＋セル：２＋精錬鉄：２＋火力発電機：１<br>公式レシピ: (定型) #forge:glassx4 + ic2:cell_emptyx2 + #forge:ingots/refined_ironx2 + ic2:generator -> ic2:geothermal_generator |
| 水力発電機 | Water Mill | 木材：４＋木の棒：４＋火力発電機：１<br>公式レシピ: (定型) minecraft:stickx4 + #minecraft:planksx4 + ic2:generator -> ic2:water_millx2 |
| 風力発電機 | Wind Mill | 鉄インゴット：４＋火力発電機：１<br>公式レシピ: (定型) ic2:complex_circuit + ic2:transformer_hv + ic2:wind_turbine + ic2:iron_fence + ic2:advanced_machine_block -> ic2:windmill |
| ソーラーパネル | Solar Panel | ガラス：３＋石炭の粉：３＋電子回路：２＋火力発電機：１<br>公式レシピ: (定型) #ic2:coal_and_charcoalx3 + #forge:glassx3 + ic2:circuitx2 + ic2:generator -> ic2:solar_panel |
| 原子炉 | Nuclear Reactor | 発展回路：１＋チャンバー：３＋火力発電機：１<br>公式レシピ: (定型) ic2:advanced_circuit + ic2:reactor_chamberx3 + ic2:generator -> ic2:nuclear_reactor |

### 2.1. 送電線

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 被覆なし銅ケーブル | Uninsulated Cable | 銅インゴット：３／金インゴット：３／精錬鉄：３ |
| 銅のケーブル | Copper Cable | 銅インゴット：３＋ゴム：６ / 銅の被覆なしケーブル：１＋ゴム：１<br>公式レシピ: (定型) #forge:ingots/copperx3 -> ic2:copper_cable_itemx6 |
| 金のケーブル | Gold Cable | 金インゴット：１＋ゴム：４ / 金の被覆なしケーブル：１＋ゴム：１<br>公式レシピ: (定型) #forge:ingots/goldx3 -> ic2:gold_cable_itemx12 |
| 金の2倍絶縁ケーブル | Gold Cable Insulated 2x | 金の被覆なしケーブル：１＋ゴム：２ / 金のケーブル：１＋ゴム：１<br>公式レシピ: (不定形) ic2:gold_cable_item + ic2:rubber + #ic2:rubber -> ic2:gold_double_insulated_cable_item<br>公式レシピ: (不定形) ic2:gold_insulated_cable_item + #ic2:rubber -> ic2:gold_double_insulated_cable_item<br>公式レシピ: (定型) #ic2:rubberx8 + #forge:ingots/gold -> ic2:gold_double_insulated_cable_itemx4 |
| 高圧用のケーブル | HV Cable | 精錬鉄：１＋ゴム：４ / 精錬鉄の被覆なしケーブル：１＋ゴム：１ |
| 高圧用の2倍絶縁ケーブル | HV Cable Insulated 2x | 精錬鉄の被覆なしケーブル：１＋ゴム：２ / 高圧用のケーブル：１＋ゴム：１ |
| 高圧用の4倍絶縁ケーブル | HV Cable Insulated 4x | 精錬鉄の被覆なしケーブル：１＋ゴム：３ / 高圧用の2倍絶縁ケーブル：１＋ゴム：１ |
| グラスファイバー | Glass Fibre | ガラス：６＋レッドストーン：２＋ダイヤモンド：１ |
| 超低圧用のケーブル | Ultra-low-Current Cable | スズインゴット：３ |
| 検出ケーブル | Detector Cable | 電子回路：１＋レッドストーン：３＋高圧用の4倍絶縁ケーブル：１<br>公式レシピ: (定型) ic2:circuit + #forge:dusts/redstonex3 + ic2:iron_triple_insulated_cable_item -> ic2:detector_cable_item |
| 分割ケーブル | Splitter Cable | レバー：１＋レッドストーン：２＋高圧用の4倍絶縁ケーブル：２<br>公式レシピ: (定型) #forge:dusts/redstonex2 + ic2:iron_triple_insulated_cable_itemx2 + minecraft:lever -> ic2:splitter_cable_item |

### 2.2. 変圧装置

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 低圧変換装置 | LV Transformer | 木材：４＋銅インゴット：３＋銅のケーブル：２<br>公式レシピ: (定型) #minecraft:planksx4 + ic2:copper_insulated_cable_itemx2 + #forge:ingots/copperx3 -> ic2:transformer_lv |
| 中圧変換装置 | MV Transformer | マシン：１＋金の2倍絶縁ケーブル：２<br>公式レシピ: (定型) ic2:gold_double_insulated_cable_itemx2 + ic2:machine_block -> ic2:transformer_mv<br>公式レシピ: (定型) ic2:bronze_double_insulated_cable_itemx2 + ic2:machine_block -> ic2:transformer_mv |
| 高圧変換装置 | HV Transformer | 高圧用の4倍絶縁ケーブル：２＋エナジークリスタル：１＋中圧変換装置：１＋電子回路：１<br>公式レシピ: (定型) ic2:iron_triple_insulated_cable_itemx2 + ic2:circuit + ic2:transformer_mv + ic2:energy_crystal -> ic2:transformer_hv |

### 2.3. 蓄電器

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| バットボックス | BatBox | 木材：５＋充電式電池：３＋銅のケーブル：１<br>公式レシピ: (定型) #minecraft:planksx5 + ic2:copper_insulated_cable_item + ic2:re_batteryx3 -> ic2:batbox |
| MFEユニット | MFE Unit | マシン：１＋エナジークリスタル：４＋金の2倍絶縁ケーブル：４ |
| MFSユニット | MFS Unit | MFEユニット：１＋ラポトロンクリスタル：６＋発展回路：１＋アドバンスドマシン：１ |

### 2.4. バッテリー

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 充電式電池 | RE Battery | スズインゴット：４＋レッドストーン：２＋銅のケーブル：１<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:ingots/tinx4 + #forge:dusts/redstonex2 -> ic2:re_battery<br>公式レシピ: (不定形) ic2:industrial_belt -> ic2:re_batteryx3 |
| 使い捨て電池 | SU Battery | 銅のケーブル：１＋レッドストーン：１＋石炭の粉末or液状化石炭：１<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:dusts/redstone + #forge:dusts/coal -> ic2:su_batteryx6<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:dusts/coal + #forge:dusts/redstone -> ic2:su_batteryx6<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:dusts/redstone + ic2:dust_hydrated_coal -> ic2:su_batteryx8<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + ic2:dust_hydrated_coal + #forge:dusts/redstone -> ic2:su_batteryx8<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:dusts/redstone + #forge:dusts/charcoal -> ic2:su_batteryx4<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:dusts/charcoal + #forge:dusts/redstone -> ic2:su_batteryx4<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:dusts/redstone + ic2:dust_hydrated_charcoal -> ic2:su_batteryx6<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + ic2:dust_hydrated_charcoal + #forge:dusts/redstone -> ic2:su_batteryx6 |
| エナジークリスタル | Energy Crystal | ダイヤモンド：１＋レッドストーン：８<br>公式レシピ: (定型) #forge:dusts/redstonex8 + #forge:gems/diamond -> ic2:energy_crystal |
| バットパック | BatPack | スズインゴット：１＋電子回路：１＋充電式電池：６<br>公式レシピ: (定型) ic2:re_batteryx6 + ic2:circuit + #forge:ingots/tin -> ic2:batpack |
| ラップパック | LapPack | バットパック：１＋発展回路：１＋ラピスラズリブロック：６<br>公式レシピ: (定型) minecraft:lapis_blockx6 + ic2:advanced_circuit + ic2:batpack -> ic2:lappack |
| ラポトロンクリスタル | Lapotron Crystal | エナジークリスタル：１＋電子回路：２＋ラピスラズリ：６<br>公式レシピ: (定型) minecraft:lapis_lazulix6 + ic2:circuitx2 + ic2:energy_crystal -> ic2:lapatron_crystal |

### 3. 機械装置

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 鉄のかまど | Iron Furnace | 鉄インゴット：５＋かまど：１ / 鉄インゴット：８<br>公式レシピ: (定型) #forge:ingots/ironx8 -> ic2:iron_furnace<br>公式レシピ: (定型) #forge:ingots/ironx5 + minecraft:furnace -> ic2:iron_furnace |
| 電気炉 | Electric Furnace | 鉄のかまど：１＋レッドストーン：２＋電子回路：１<br>公式レシピ: (定型) ic2:circuit + #forge:dusts/redstonex2 + ic2:iron_furnace -> ic2:electric_furnace<br>公式レシピ: (定型) ic2:circuit + #forge:dusts/redstonex2 + minecraft:furnace + ic2:machine_block -> ic2:electric_furnace |
| 誘導加熱炉 | Induction Furnace | 銅インゴット：７＋アドバンスドマシン：１＋電気炉：１<br>公式レシピ: (定型) #forge:ingots/copperx7 + ic2:electric_furnace + ic2:advanced_machine_block -> ic2:induction_furnace |
| 圧縮機 | Compressor | 石：６＋マシン：１＋電子回路：１<br>公式レシピ: (定型) #forge:stonex6 + ic2:machine_block + ic2:circuit -> ic2:compressor |
| 抽出機 | Extractor | ツリータップ：４＋マシン：１＋電子回路：１<br>公式レシピ: (定型) ic2:treetapx4 + ic2:machine_block + ic2:circuit -> ic2:extractor |
| 粉砕機 | Macerator | 火打石：３＋丸石：２＋マシン：１＋電子回路：１<br>公式レシピ: (定型) #forge:ingots/refined_ironx7 + ic2:stone_macerator + ic2:circuit -> ic2:macerator<br>公式レシピ: (定型) minecraft:flintx3 + #forge:cobblestonex2 + ic2:machine_block + ic2:circuit -> ic2:macerator |
| 掘削機 | Miner | 採掘用パイプ：２＋マシン：１＋電子回路：２<br>公式レシピ: (定型) ic2:circuitx2 + ic2:machine_block + ic2:mining_pipe_shaftx2 -> ic2:miner |
| 電解水精製装置 | Electrolyzer | 銅のケーブル：４＋セル：２＋マシン：１＋電子回路：１<br>公式レシピ: (定型) ic2:copper_insulated_cable_itemx4 + ic2:circuit + ic2:cell_emptyx2 + ic2:machine_block -> ic2:electrolyzer |
| ポンプ装置 | Pump | セル：４＋ツリータップ：１＋採掘用パイプ：２＋マシン：１＋電子回路：１<br>公式レシピ: (定型) ic2:cell_emptyx4 + ic2:circuit + ic2:machine_block + ic2:mining_pipe_shaftx2 + ic2:treetap -> ic2:pump |
| 磁気発生装置 | Magnetizer | レッドストーン：６＋鉄の柵：２＋マシン：１<br>公式レシピ: (定型) #forge:dusts/redstonex6 + ic2:iron_fencex2 + ic2:machine_block -> ic2:magnetizer |
| リサイクル装置 | Recycler | 圧縮機：１＋土：３＋精錬鉄：２＋グロウストーンダスト：１<br>公式レシピ: (定型) minecraft:glowstone_dust + minecraft:dirtx3 + ic2:compressor + #forge:ingots/refined_ironx2 -> ic2:recycler |
| 缶詰製造機 | Canning Machine | スズインゴット：７＋マシン：１＋電子回路：１<br>公式レシピ: (定型) #forge:ingots/tinx7 + ic2:circuit + ic2:machine_block -> ic2:canner<br>公式レシピ: (定型) #forge:ingots/tinx7 + ic2:circuit + ic2:stone_canner -> ic2:canner |
| 物質製造機 | Mass Fabricator | グロウストーンダスト：４＋アドバンスドマシン：２＋発展回路：２＋ラポトロンクリスタル：１<br>公式レシピ: (定型) minecraft:glowstone_dustx4 + ic2:advanced_circuitx2 + ic2:stabilized_machine_blockx2 + ic2:lapatron_crystal -> ic2:mass_fabricator |
| ルミネータ | Luminator | ガラス：５＋超低圧用のケーブル：１＋精錬鉄：２＋銅のケーブル：１<br>公式レシピ: (定型) #forge:ingots/refined_ironx2 + ic2:copper_insulated_cable_item + #forge:glassx5 + ic2:tin_cable_item -> ic2:luminatorx8 |
| テラフォーマー | Terraformer | 土：３＋グロウストーンダスト：４＋アドバンスドマシン：１＋空のTFBP：１<br>公式レシピ: (定型) minecraft:glowstone_dustx4 + ic2:tfbp + minecraft:dirtx3 + ic2:stabilized_machine_block -> ic2:terraformer |
| 農薬散布機 | Crop-Matron | 電子回路：２＋チェスト：１＋マシン：１＋園芸用支柱：５<br>公式レシピ: (定型) ic2:circuitx2 + #forge:chests/wooden + ic2:crop_sticksx5 + ic2:machine_block -> ic2:crop_matron |

### 4. 新しいブロック

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| マシン | Machine | 精錬鉄：８ |
| アドバンスドマシン | Advanced Machine | 合金板：２＋カーボンプレート：２＋マシン：１ |
| 建築用建材 | Construction Foam | 粘土の粉：１＋石炭の粉：１＋レッドストーン：１＋水セル：１ |
| 足場ブロック | Scaffold | 木材：３＋木の棒：３ |
| 鉄製の足場ブロック | Iron Scaffold | 精錬鉄：３＋鉄の柵：３<br>公式レシピ: (定型) #forge:ingots/refined_ironx3 + ic2:iron_fencex3 -> ic2:scaffold_ironx16 |
| テスラコイル | Tesla Coil | レッドストーン：５＋精錬鉄：２＋電子回路：１＋中圧変換装置：１<br>公式レシピ: (定型) #forge:dusts/redstonex5 + ic2:transformer_mv + #forge:ingots/refined_ironx2 + ic2:circuit -> ic2:tesla_coil |
| 工業用TNT | IndustrialTNT | 火打石：６＋TNT：３ / ／銅のケーブル：１＋電子回路：１＋グロウストーンダスト：２＋TNT：３<br>公式レシピ: (定型) minecraft:flintx6 + minecraft:tntx3 -> ic2:itntx4<br>公式レシピ: (定型) minecraft:flintx6 + minecraft:tntx3 -> ic2:itntx4 |
| 核爆弾 | Nuke | なし<br>公式レシピ: (定型) minecraft:gunpowderx5 + #forge:ingots/uraniumx4 -> ic2:nuke |
| 鉄の柵 | Iron Fence | 精錬鉄：６<br>公式レシピ: (定型) #forge:ingots/refined_ironx6 -> ic2:iron_fencex12 |
| 採掘用パイプ | Mining Pipe | 精錬鉄：６＋ツリータップ：１ |
| 強化ガラス | Reinforced Glass | ガラス：７＋合金版：２<br>公式レシピ: (定型) #forge:glassx7 + ic2:plate_advanced_alloyx2 -> ic2:reinforced_glassx7<br>公式レシピ: (定型) #forge:glassx7 + ic2:plate_advanced_alloyx2 -> ic2:reinforced_glassx7 |
| 強化石材 | Reinforced Stone | 石：８＋合金版：１<br>公式レシピ: (定型) #forge:stonex8 + ic2:plate_advanced_alloy -> ic2:reinforced_stonex8 |
| 強化ドア | Reinforced Door | 強化石材：６<br>公式レシピ: (定型) ic2:reinforced_stonex6 -> ic2:reinforced_door |
| ラバーシート | Rubber Sheet | ゴム：６<br>公式レシピ: (定型) #ic2:rubberx6 -> ic2:rubber_sheetx3 |
| テレポーター | Teleporter | アドバンスドマシン：１＋周波数送信機：１＋ダイヤモンド：１＋グラスファイバー：２＋発展回路：４<br>公式レシピ: (定型) ic2:advanced_circuitx4 + ic2:frequency_transmitter + ic2:glassfiber_cable_itemx2 + ic2:stabilized_machine_block + #forge:gems/diamond -> ic2:teleporter |
| 個人チェスト | Personal Safe | 電子回路：１＋マシン：１＋チェスト：１ |
| トレード-O-Mat | Trade-O-Mat | レッドストーン：３＋マシン：１＋チェスト：２<br>公式レシピ: (定型) #forge:dusts/redstonex3 + #forge:chests/woodenx2 + ic2:machine_block -> ic2:trade_o_mat |

### 5. 鉱物

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 銅 | Copper | なし |
| スズ | Tin | なし |
| ブロンズ | Bornze | なし |
| 精錬鉄 | Refined Iron | なし |
| ウラニウム | Uranium | なし |
| 精製ウラン | Refined Uranium | なし |
| 合金インゴット | Mixed Metal Ingot | 精錬鉄：３＋ブロンズ：３＋錫インゴット：３<br>公式レシピ: (定型) #forge:ingots/refined_ironx3 + #forge:ingots/bronzex3 + #forge:ingots/aluminiumx3 -> ic2:ingot_advanced_alloyx2 |
| 合金板 | Advanced Alloy | なし |
| 銅板 | Dense Copper Plate | 銅インゴット：８ |
| イリジウム鉱石 | Iridium Ore | （イリジウム鉱石）マター：７ （イリジウムプレート）イリジウム鉱石：４＋合金板４＋ダイヤモンド１ |
| イリジウムプレート | Iridium Plate | なし<br>公式レシピ: (定型) ic2:ore_iridiumx4 + ic2:plate_advanced_alloyx4 + #forge:gems/diamond -> ic2:plate_iridium |

### 5.1. 粉末

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 小さな鉄の粉 | Small Pile of Iron Dust | （小さな鉄の粉）銅の粉：５＋スズの粉４/銅の粉：４＋スズの粉５ （鉄の粉）小さな鉄の粉：２ |
| 石炭の粉 | Coal Dust | なし |
| ブロンズの粉 | Bronze Dust | 銅の粉：３＋スズの粉：１<br>公式レシピ: (不定形) #forge:dusts/tin + #forge:dusts/copperx3 -> ic2:dust_bronzex4<br>公式レシピ: (不定形) ic2:bronze_dust_block -> ic2:dust_bronzex9 |
| 粘土の粉 | Clay Dust | なし |
| コーヒーの粉 | Coffee Powder | なし<br>公式レシピ: (不定形) ic2:coffee_beans -> ic2:coffee_powder |
| グリンパウダー | Grin Powder | なし |

### 5.2. 石炭から作るアイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 炭素繊維、炭素メッシュ | Raw Carbon Fible / Raw Carbon Mesh | （炭素繊維）石炭の粉：４ / （炭素メッシュ）炭素繊維：２ |
| カーボンプレート | Carbon Plate | なし |
| 石炭の玉 | Coal Ball | 石炭の粉：８＋火打石：１<br>公式レシピ: (定型) #forge:dusts/coalx8 + minecraft:flint -> ic2:coal_ball |
| 圧縮した石炭玉 | Compressed Coal Ball | なし |
| 石炭の塊 | Coal Chunk | 圧縮した石炭玉：８＋鉄ブロック：１or黒曜石：１orレンガブロック：１<br>公式レシピ: (定型) ic2:compressed_coal_ballx8 + minecraft:obsidian -> ic2:coal_chunk<br>公式レシピ: (定型) ic2:compressed_coal_ballx8 + minecraft:iron_block -> ic2:coal_chunk<br>公式レシピ: (定型) ic2:compressed_coal_ballx8 + minecraft:bricks -> ic2:coal_chunk<br>公式レシピ: (精錬) ic2:bio_chunk -> ic2:coal_chunk [furnace] |
| ダイヤモンド | Diamond | なし |

### 5.3. 液体燃料

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 液状化石炭 | Hydrated Coal Dust | (1個)水入りバケツor水セル：１＋石炭の粉：１ / (8個)水入りバケツor水セル：１＋石炭の粉：８<br>公式レシピ: (不定形) #forge:dusts/coal + fluid:water -> ic2:dust_hydrated_coal<br>公式レシピ: (定型) #forge:dusts/coalx8 + fluid:water -> ic2:dust_hydrated_coalx8 |
| 植物の玉 | Plantball | (1個)小麦or小麦の種orサトウキビor各種葉or草：８ / (2個)各種苗：８<br>公式レシピ: (定型) new ArrayInput(1, false, new Object[]{Items.f_41909_, Items.f_41982_, Items.f_42029_, Items.f_41864_, Items.f_42210_, ItemTags.f_13143_, Tags.Items.CROPS, IC2Items.HEMP, Tags.Items.MUSHROOMS, Tags.Items.SEEDS, Items.f_42410_, IC2Items.STICKY_RESIN, IC2Items.CROP_SEED, Items.f_41911_, ItemTags.f_13149_, Items.f_41910_, IC2Items.HOPS, Items.f_42260_, Items.f_42259_, Items.f_42780_, Items.f_151079_, Items.f_42533_})x8 -> ic2:plant_ball<br>公式レシピ: (定型) #minecraft:saplingsx8 -> ic2:plant_ballx2 |
| 圧縮した液化石炭 | H.Coal | なし |
| 圧縮した植物の玉 | Compressed Plants | なし |
| 空の燃料缶 | Fuel Can | スズインゴット：７<br>公式レシピ: (定型) #forge:ingots/tinx7 -> ic2:fuel_can |
| 燃料缶 | Fuel Can_Filled | なし |

### 6. 未分類

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ゴムの木 | Rubbertree Wood | なし |
| ゴム | Rubber | なし<br>公式レシピ: (精錬) ic2:sticky_resin -> ic2:rubber [furnace] |
| 電子回路 | Electronic Circuit | 精錬鉄：１＋レッドストーン：２＋銅のケーブル：６<br>公式レシピ: (定型) ic2:copper_insulated_cable_itemx6 + #forge:dusts/redstonex2 + #forge:ingots/refined_iron -> ic2:circuit<br>公式レシピ: (定型) ic2:copper_insulated_cable_itemx6 + #forge:dusts/redstonex2 + #forge:ingots/refined_iron -> ic2:circuit |
| 発展回路 | Advanced Circuit | 電子回路：１＋レッドストーン：４＋グロウストーンの粉：２＋ラピスラズリ：２<br>公式レシピ: (定型) #forge:dusts/redstonex4 + minecraft:glowstone_dustx2 + minecraft:lapis_lazulix2 + ic2:circuit -> ic2:advanced_circuit<br>公式レシピ: (定型) #forge:dusts/redstonex4 + minecraft:lapis_lazulix2 + minecraft:glowstone_dustx2 + ic2:circuit -> ic2:advanced_circuit |
| スクラップ | Scrap | なし |
| スクラップ箱 | Scrap Box | なし<br>公式レシピ: (定型) ic2:scrapx9 -> ic2:scrapbox |
| マター | Matter | なし |

### 7. 新しい装備

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| バットパック | BatPack | スズインゴット：１＋電子回路：１＋充電式電池：６<br>公式レシピ: (定型) ic2:re_batteryx6 + ic2:circuit + #forge:ingots/tin -> ic2:batpack |
| ラップパック | LapPack | バットパック：１＋発展回路：１＋ラピスラズリブロック：６<br>公式レシピ: (定型) minecraft:lapis_blockx6 + ic2:advanced_circuit + ic2:batpack -> ic2:lappack |
| CFパック | CF-Pack | スズインゴット：１：＋電子回路：１＋ＣＦスプレー：２＋空の燃料缶：４ |
| ジェットパック | Jetpack | 精錬鉄：４＋電子回路：１＋空の燃料缶：１＋レッドストーン：２ |
| 電動ジェットパック | Electric Jetpack | 精錬鉄：４＋発展回路：１＋バットボックス：１＋グロウストーンの粉：２<br>公式レシピ: (定型) #forge:ingots/refined_ironx4 + ic2:advanced_circuit + ic2:batbox + minecraft:glowstone_dustx2 -> ic2:jetpack_electric<br>公式レシピ: (不定形) ic2:jetpack_electric + ic2:magnet + new FlagModifier(IC2Items.JETPACK_ELECTRIC, "SpecialMode", false).setUsesInput() + RecipeMods.HIDDEN_RECIPE -> ic2:jetpack_electric |
| ゴムブーツ | Rubber Boots | ゴム：６＋羊毛：１<br>公式レシピ: (定型) #ic2:rubberx6 + #minecraft:wool -> ic2:armor_hazmat_boots |
| コンポジットベスト | Composite Vest | 革のチェストプレート：１＋鉄のチェストプレート：１＋合金板：６ |
| ブロンズアーマー | Bronze Armor | なし |
| HAZMATスーツ | Hazmat Suit | なし<br>公式レシピ: (定型) #ic2:rubberx6 + minecraft:orange_dyex2 -> ic2:armor_hazmat_chest |
| ナノアーマー | Nano Suits | なし |
| クアンタムアーマー | Quantum Suits | なし |
| ソーラーヘルメット | Solar Helmet | 鉄：５or鉄のヘルメット：１＋ソーラーパネル：１＋銅のケーブル：３<br>公式レシピ: (定型) #forge:ingots/ironx5 + ic2:solar_panel + ic2:copper_insulated_cable_itemx3 -> ic2:solar_helmet<br>公式レシピ: (定型) minecraft:iron_helmet + ic2:solar_panel + ic2:copper_insulated_cable_itemx3 -> ic2:solar_helmet |
| スタティックブーツ | Static Boots | 鉄：４or鉄のブーツ：１＋羊毛：１＋銅のケーブル：３<br>公式レシピ: (定型) #forge:ingots/ironx4 + #minecraft:wool + ic2:copper_insulated_cable_itemx3 -> ic2:static_boots<br>公式レシピ: (定型) minecraft:iron_boots + #minecraft:wool + ic2:copper_insulated_cable_itemx3 -> ic2:static_boots |

### 8. 新しい道具

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ブロンズ製工具 | Bronze Tools | なし |
| チェーンソー | Chain Saw | 電子回路：１＋精錬鉄：５：充電式電池：１<br>公式レシピ: (定型) #forge:ingots/refined_ironx5 + ic2:circuit + ic2:re_battery -> ic2:chainsaw |
| 採掘用ドリル | Mining Drill | 電子回路：１＋精錬鉄：５＋充電式電池：１ |
| ダイヤモンドドリル | Diamond Drill | 採掘用ドリル：１＋ダイヤモンド：３<br>公式レシピ: (定型) #forge:gems/diamondx3 + ic2:drill -> ic2:drill_diamond |
| 電動クワ | Electric Hoe | 充電式電池：１＋電子回路：１＋精錬鉄：２<br>公式レシピ: (定型) #forge:ingots/refined_ironx2 + ic2:circuit + ic2:re_battery -> ic2:electric_hoe |
| レンチ | Wrench | ブロンズ：６<br>公式レシピ: (定型) #forge:ingots/bronzex6 -> ic2:wrench |
| 電動レンチ | Electric Wrench | レンチ：１＋電子回路：１＋充電式電池：１<br>公式レシピ: (定型) ic2:wrench + ic2:circuit + ic2:re_battery -> electricWrench |
| EUリーダー | EU-Reader | 電子回路：１＋グロウストーンの粉：１：銅のケーブル：４<br>公式レシピ: (定型) minecraft:glowstone_dust + ic2:copper_insulated_cable_itemx4 + ic2:circuit -> ic2:eu_reader |
| CFスプレー | CF Sprayer | （CFスプレー）丸石：４＋木の棒：１ / （補充）CFスプレー：１＋CFペレット：１/CFスプレー：１＋CFペレット：８ |
| CFペレット | CF Pellet | なし |
| カッター | Inslation Cutter | 精錬鉄：３＋鉄インゴット：２ |
| ダイナマイト | Dynamite | 工業用TNT：１＋糸：１<br>公式レシピ: (不定形) ic2:itnt + minecraft:string -> ic2:dynamitex8 |
| 粘着ダイナマイト | Sticky Dynamite | ダイナマイト：８＋べたつく樹液：１<br>公式レシピ: (定型) ic2:dynamitex8 + ic2:sticky_resin -> ic2:sticky_dynamitex8 |
| 遠隔起爆装置 | Dynamite-O-Mote | 周波数送信機：１＋スズインゴット：２＋銅のケーブル：１＋ラピスラズリ：１ / 銅のケーブル：１＋電子回路：１＋グロウストーンダスト：２＋TNT：３<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + minecraft:glowstone_dustx2 + ic2:circuit + minecraft:tntx3 -> ic2:dynamite_remote<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:ingots/tinx2 + minecraft:lapis_lazuli + ic2:frequency_transmitter -> ic2:dynamite_remote |
| ODスキャナー | OD Scanner | 充電式電池：１＋電子回路：２＋グロウストーンの粉：１＋銅のケーブル：３<br>公式レシピ: (定型) minecraft:glowstone_dust + ic2:circuitx2 + ic2:re_battery + ic2:copper_insulated_cable_itemx3 -> ic2:scanner_od |
| OVスキャナー | OV Scanner | ODスキャナー：１＋発展回路：２＋グロウストーンの粉：３＋金の2倍絶縁ケーブル：２(注意、画像は銅ケーブル)<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:gold_double_insulated_cable_itemx2 + ic2:scanner_od -> ic2:scanner_ov<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:bronze_double_insulated_cable_itemx2 + ic2:scanner_od -> ic2:scanner_ov<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:gold_double_insulated_cable_itemx2 + ic2:re_battery -> ic2:scanner_ov<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:bronze_double_insulated_cable_itemx2 + ic2:re_battery -> ic2:scanner_ov |
| 採掘用レーザー | Mining Laser | エナジークリスタル：２＋発展回路：１＋合金板：４＋レッドストーン：１<br>公式レシピ: (定型) #forge:dusts/redstone + ic2:energy_crystalx2 + ic2:plate_advanced_alloyx5 -> ic2:mining_laser |
| スズ製の缶 | Tin Can | スズインゴット：５<br>公式レシピ: (定型) #forge:ingots/tinx5 -> ic2:tin_canx4<br>公式レシピ: (鍛冶台) #forge:ingots/tin + ic2:cutter -> ic2:tin_canx2 |
| 食物缶詰 | Food Can | なし |
| ツリータップ | Treetap | 木材：５<br>公式レシピ: (定型) #minecraft:planksx5 -> ic2:treetap |
| 電動ツリータップ | Electric Treetap | ツリータップ：１＋電子回路：１＋充電式電池：１<br>公式レシピ: (定型) ic2:treetap + ic2:circuit + ic2:re_battery -> ic2:electric_treetap |
| ナノセイバー | Nano Saber | エナジークリスタル：１＋合金板：２＋グロウストーンダスト：２＋カーボンプレート：２<br>公式レシピ: (定型) minecraft:glowstone_dustx2 + ic2:plate_advanced_alloyx2 + ic2:carbon_platex2 + ic2:energy_crystal -> ic2:nano_saber |
| ペインター | Painter | （ペインター）羊毛：３＋鉄インゴット：２ \|\| （色付き）ペインター：１＋各種染料：１<br>公式レシピ: (定型) #minecraft:woolx3 + #forge:ingots/ironx2 -> ic2:painter |
| 周波数送信機 | Frequency Transmitter | 電子回路：１＋銅のケーブル：１<br>公式レシピ: (不定形) ic2:circuit + ic2:copper_insulated_cable_item -> ic2:frequency_transmitter |
| コイン | Industrial Credit | 精錬鉄：４<br>公式レシピ: (不定形) ic2:coin_8x -> ic2:coinx8<br>公式レシピ: (定型) ic2:coin_4096xx8 -> ic2:coin_32768x<br>公式レシピ: (定型) ic2:coin_512xx8 -> ic2:coin_4096x<br>公式レシピ: (不定形) ic2:coin_32768x -> ic2:coin_4096xx8<br>公式レシピ: (定型) ic2:coin_64xx8 -> ic2:coin_512x<br>公式レシピ: (不定形) ic2:coin_4096x -> ic2:coin_512xx8<br>公式レシピ: (定型) ic2:coin_8xx8 -> ic2:coin_64x<br>公式レシピ: (不定形) ic2:coin_512x -> ic2:coin_64xx8<br>公式レシピ: (定型) ic2:coinx8 -> ic2:coin_8x<br>公式レシピ: (不定形) ic2:coin_64x -> ic2:coin_8xx8 |
| ツールボックス | Toolbox | チェストボックス：１＋精錬鉄：５<br>公式レシピ: (定型) #forge:ingots/refined_ironx5 + #forge:chests/wooden -> ic2:tool_box |
| マグカップ | Stone Mug | 石：７ |
| 酒樽 | Booze Barrel | ゴムの原木：１＋木材：２ |

### 9. セル

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 空のセル | Empty Cell | スズインゴット：４<br>公式レシピ: (定型) #forge:ingots/tinx4 -> ic2:cell_emptyx16 |
| 圧縮空気セル | Compressd Air Cell | なし |
| バイオセル | Bio Cell | 空のセル：１＋圧縮した植物の玉：１<br>公式レシピ: (不定形) ic2:cell_empty + ic2:compressed_plant_ball -> ic2:cell_bio |
| 液化石炭セル | H. Coal Cell | 空のセル：１＋圧縮した液化石炭：１ |
| バイオ燃料セル | Biofuel Cell | なし |
| 石炭燃料セル | Coalfuel Cell | なし |
| 溶岩セル | Lava Cell | 空のセル：１＋溶岩入りバケツ：１ |
| 水セル | Water Cell | 空のセル：１＋水入りバケツ：１ |
| 電解水セル | Electrolyzed Water Cell | なし |
| K冷却セル | 10K Coolant Cell | v1.103までは水セルを抽出器にかけると製作可能. / v1.106以後は水セル：１＋スズインゴット：４で製作可能.<br>公式レシピ: (定型) #forge:ingots/tinx4 + fluid:water -> ic2:heat_storage_single |

### 10. テラフォーマーブループリント

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 空のTFBP | Empty TFBP | 発展回路：１＋電子回路：１＋レッドストーン：２ |
| 寒冷化 | Chilling | 空のTFBP：１＋雪玉：４ |
| 砂漠化 | Desertification | 空のTFBP：１＋砂：４ |
| 平地化 | Flatificator | 空のTFBP：１＋土：４ |
| 培養 | Cultivation | 空のTFBP：１＋種：４ |
| 灌漑 | Irrigation | 空のTFBP：１＋水入りバケツ：４ |
| 榎茸 | Mushroom | 空のTFBP：１＋キノコ：４＋菌糸ブロック：４ |

### 11. アップグレードモジュール

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| オーバークロッカー | Overclocker upgrade | 電子回路：１＋冷却セル：３＋銅のケーブル：２<br>公式レシピ: (定型) ic2:heat_storage_singlex3 + ic2:copper_insulated_cable_itemx2 + ic2:circuit -> ic2:overclocker_upgrade<br>公式レシピ: (定型) ic2:heat_storage_triplex3 + ic2:copper_insulated_cable_itemx2 + ic2:circuit -> ic2:overclocker_upgradex3<br>公式レシピ: (定型) ic2:heat_storage_sixx3 + ic2:copper_insulated_cable_itemx2 + ic2:circuit -> ic2:overclocker_upgradex6<br>公式レシピ: (定型) ic2:heat_storage_singlex4 + ic2:upgrade_base -> ic2:overclocker_upgrade<br>公式レシピ: (定型) ic2:heat_storage_singlex8 + ic2:upgrade_base -> ic2:overclocker_upgradex2<br>公式レシピ: (定型) ic2:heat_storage_triplex4 + ic2:upgrade_base -> ic2:overclocker_upgradex4<br>公式レシピ: (定型) ic2:heat_storage_triplex8 + ic2:upgrade_base -> ic2:overclocker_upgradex8<br>公式レシピ: (定型) ic2:heat_storage_sixx4 + ic2:upgrade_base -> ic2:overclocker_upgradex8<br>公式レシピ: (定型) ic2:heat_storage_sixx8 + ic2:upgrade_base -> ic2:overclocker_upgradex16 |
| トランスフォーマー | Transformer upgrade | 中圧変換装置：１＋電子回路：１＋金の２倍絶縁ケーブル：２＋ガラス：５<br>公式レシピ: (定型) #forge:glassx5 + ic2:gold_double_insulated_cable_itemx2 + ic2:transformer_mv + ic2:circuit -> ic2:transformer_upgrade<br>公式レシピ: (定型) #forge:glassx7 + ic2:transformer_mv + ic2:upgrade_base -> ic2:transformer_upgrade |
| エナジーストレージ | Energy storage upgrade | 充電式電池：１＋電子回路：１＋銅のケーブル：２＋木材：５<br>公式レシピ: (定型) #minecraft:planksx5 + ic2:copper_insulated_cable_itemx2 + ic2:re_battery + ic2:circuit -> ic2:energy_storage_upgrade<br>公式レシピ: (定型) #minecraft:planksx7 + ic2:re_battery + ic2:upgrade_base -> ic2:energy_storage_upgrade |
| イジェクター | Ejecter Upgrade | ピストン：２＋電子回路：１＋銅のケーブル：２＋ホッパー：１(※バニラのもの) |

### 12. 既存アイテムの追加レシピ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 木材（ジャングル） | Jungle Wooden Planks | ゴム原木：１ |
| 原木（ジャングル） | Jungle Wood | なし |
| バケツ | Bucket | スズインゴット：３<br>公式レシピ: (定型) #forge:ingots/tinx3 -> minecraft:bucket |
| 線路 | Rail | 青銅インゴット：６＋棒：１<br>公式レシピ: (定型) #forge:ingots/bronzex6 + minecraft:stick -> minecraft:railx8 |
| ピストン | Piston | 木材：３＋丸石：４＋青銅インゴット：１＋レッドストーン：１<br>公式レシピ: (定型) #minecraft:planksx3 + #forge:cobblestonex4 + #forge:ingots/silver + #forge:dusts/redstone -> minecraft:piston<br>公式レシピ: (定型) #minecraft:planksx3 + #forge:cobblestonex4 + #forge:ingots/bronze + #forge:dusts/redstone -> minecraft:piston |
| 粘着ピストン | Sticky piston | ピストン：１＋樹液：１<br>公式レシピ: (不定形) minecraft:piston + ic2:sticky_resin + RecipeMods.HIDDEN_RECIPE -> minecraft:sticky_piston |
| 松明 | Torch | 樹液：１＋棒：１<br>公式レシピ: (定型) ic2:sticky_resin + minecraft:stick -> minecraft:torchx4 |
| グロウストーンの粉 | GrowStone Dusts | レッドストーン：５＋金の粉：４ |
| 火薬 | Gunpowder | レッドストーン：５＋石炭の粉：４<br>公式レシピ: (定型) #forge:dusts/redstonex5 + #forge:dusts/coalx4 -> minecraft:gunpowderx3 |
| 黒曜石 | Obsidian | 水セル：２＋溶岩セル：２<br>公式レシピ: (不定形) ic2:cell_waterx2 + ic2:cell_lavax2 -> minecraft:obsidian<br>公式レシピ: (不定形) ic2:cell_waterx2 + minecraft:magma_blockx2 -> minecraft:obsidian |

### 1. 鉱物

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 銅、スズ、ブロンズ | Copper / Tin / Bornze | なし |
| 精錬鉄 | Refined Iron | なし |
| ウラニウム | Uranium | なし |
| 精製ウラン | Refined Uranium | なし |
| 合金インゴット | Mixed Metal Ingot | 精錬鉄：３＋ブロンズ：３＋錫インゴット：３<br>公式レシピ: (定型) #forge:ingots/refined_ironx3 + #forge:ingots/bronzex3 + #forge:ingots/aluminiumx3 -> ic2:ingot_advanced_alloyx2 |
| 合金板 | Advanced Alloy | なし |
| 銅板 | Dense Copper Plate | 銅インゴット：８ |
| イリジウム鉱石、イリジウムプレート | Iridium Plate | （イリジウム鉱石）マター：７ / （イリジウムプレート）イリジウム鉱石：４＋合金板４＋ダイヤモンド１<br>公式レシピ: (定型) ic2:ore_iridiumx4 + ic2:plate_advanced_alloyx4 + #forge:gems/diamond -> ic2:plate_iridium |

### 1.1. 粉末

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 小さな鉄の粉 | Small Pile of Iron Dust | （小さな鉄の粉）銅の粉：５＋スズの粉４/銅の粉：４＋スズの粉５ / （鉄の粉）小さな鉄の粉：２ |
| 石炭の粉 | Coal Dust | なし |
| ブロンズの粉 | Bronze Dusts | 銅の粉：３＋スズの粉：１ |
| 粘土の粉 | Clay Dust | なし |
| コーヒーの粉 | Coffee Powder | なし<br>公式レシピ: (不定形) ic2:coffee_beans -> ic2:coffee_powder |
| グリンパウダー | Grin Powder | なし |

### 1.2. 石炭から作るアイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 炭素繊維、炭素メッシュ | Raw Carbon Fible / Raw Carbon Mesh | （炭素繊維）石炭の粉：４ / （炭素メッシュ）炭素繊維：２ |
| カーボンプレート | Carbon Plate | なし |
| 石炭の玉 | Coal Ball | 石炭の粉：８＋火打石：１<br>公式レシピ: (定型) #forge:dusts/coalx8 + minecraft:flint -> ic2:coal_ball |
| 圧縮した石炭玉 | Compressed Coal Ball | なし |
| 石炭の塊 | Coal Chunk | 圧縮した石炭玉：８＋鉄ブロック：１or黒曜石：１orレンガブロック：１<br>公式レシピ: (定型) ic2:compressed_coal_ballx8 + minecraft:obsidian -> ic2:coal_chunk<br>公式レシピ: (定型) ic2:compressed_coal_ballx8 + minecraft:iron_block -> ic2:coal_chunk<br>公式レシピ: (定型) ic2:compressed_coal_ballx8 + minecraft:bricks -> ic2:coal_chunk<br>公式レシピ: (精錬) ic2:bio_chunk -> ic2:coal_chunk [furnace] |
| ダイヤモンド | Diamond | なし |

### 1.3. 液体燃料

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 液状化石炭 | Hydrated Coal Dust | (1個)水入りバケツor水セル：１＋石炭の粉：１ / (8個)水入りバケツor水セル：１＋石炭の粉：８<br>公式レシピ: (不定形) #forge:dusts/coal + fluid:water -> ic2:dust_hydrated_coal<br>公式レシピ: (定型) #forge:dusts/coalx8 + fluid:water -> ic2:dust_hydrated_coalx8 |
| 植物の玉 | Plantball | (1個)小麦or小麦の種orサトウキビor各種葉or草：８ / (2個)各種苗：８<br>公式レシピ: (定型) new ArrayInput(1, false, new Object[]{Items.f_41909_, Items.f_41982_, Items.f_42029_, Items.f_41864_, Items.f_42210_, ItemTags.f_13143_, Tags.Items.CROPS, IC2Items.HEMP, Tags.Items.MUSHROOMS, Tags.Items.SEEDS, Items.f_42410_, IC2Items.STICKY_RESIN, IC2Items.CROP_SEED, Items.f_41911_, ItemTags.f_13149_, Items.f_41910_, IC2Items.HOPS, Items.f_42260_, Items.f_42259_, Items.f_42780_, Items.f_151079_, Items.f_42533_})x8 -> ic2:plant_ball<br>公式レシピ: (定型) #minecraft:saplingsx8 -> ic2:plant_ballx2 |
| 圧縮した液化石炭 | H.Coal | なし |
| 圧縮した植物の玉 | Compressed Plants | なし |
| 空の燃料缶 | Fuel Can | スズインゴット：７<br>公式レシピ: (定型) #forge:ingots/tinx7 -> ic2:fuel_can |
| 燃料缶 | Fuel Can_Filled | なし |

### 2. 未分類

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ゴムの木 | Rubbertree Wood | なし |
| ゴム | Rubber | なし<br>公式レシピ: (精錬) ic2:sticky_resin -> ic2:rubber [furnace] |
| 電子回路 | Electronic Circuit | 精錬鉄：１＋レッドストーン：２＋銅のケーブル：６<br>公式レシピ: (定型) ic2:copper_insulated_cable_itemx6 + #forge:dusts/redstonex2 + #forge:ingots/refined_iron -> ic2:circuit<br>公式レシピ: (定型) ic2:copper_insulated_cable_itemx6 + #forge:dusts/redstonex2 + #forge:ingots/refined_iron -> ic2:circuit |
| 発展回路 | Advanced Circuit | 電子回路：１＋レッドストーン：４＋グロウストーンの粉：２＋ラピスラズリ：２<br>公式レシピ: (定型) #forge:dusts/redstonex4 + minecraft:glowstone_dustx2 + minecraft:lapis_lazulix2 + ic2:circuit -> ic2:advanced_circuit<br>公式レシピ: (定型) #forge:dusts/redstonex4 + minecraft:lapis_lazulix2 + minecraft:glowstone_dustx2 + ic2:circuit -> ic2:advanced_circuit |
| スクラップ | Scrap | なし |
| スクラップ箱 | Scrap Box | なし<br>公式レシピ: (定型) ic2:scrapx9 -> ic2:scrapbox |
| マター | Matter | なし |

### 3. 新しい装備

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| バットパック | BatPack | スズインゴット：１＋電子回路：１＋充電式電池：６<br>公式レシピ: (定型) ic2:re_batteryx6 + ic2:circuit + #forge:ingots/tin -> ic2:batpack |
| ラップパック | LapPack | バットパック：１＋発展回路：１＋ラピスラズリブロック：６<br>公式レシピ: (定型) minecraft:lapis_blockx6 + ic2:advanced_circuit + ic2:batpack -> ic2:lappack |
| CFパック | CF-Pack | スズインゴット：１：＋電子回路：１＋ＣＦスプレー：２＋空の燃料缶：４ |
| ジェットパック | Jetpack | 精錬鉄：４＋電子回路：１＋空の燃料缶：１＋レッドストーン：２ |
| 電動ジェットパック | Electric Jetpack | 精錬鉄：４＋発展回路：１＋バットボックス：１＋グロウストーンの粉：２<br>公式レシピ: (定型) #forge:ingots/refined_ironx4 + ic2:advanced_circuit + ic2:batbox + minecraft:glowstone_dustx2 -> ic2:jetpack_electric<br>公式レシピ: (不定形) ic2:jetpack_electric + ic2:magnet + new FlagModifier(IC2Items.JETPACK_ELECTRIC, "SpecialMode", false).setUsesInput() + RecipeMods.HIDDEN_RECIPE -> ic2:jetpack_electric |
| ゴムブーツ | Rubber Boots | ゴム：６＋羊毛：１<br>公式レシピ: (定型) #ic2:rubberx6 + #minecraft:wool -> ic2:armor_hazmat_boots |
| コンポジットベスト | Composite Vest | 革のチェストプレート：１＋鉄のチェストプレート：１＋合金板：６ |
| ブロンズアーマー | Bronze Armor | なし |
| HAZMATスーツ | Hazmat Suit | なし<br>公式レシピ: (定型) #ic2:rubberx6 + minecraft:orange_dyex2 -> ic2:armor_hazmat_chest |
| ナノアーマー | Nano Suits | なし |
| クアンタムアーマー | Quantum Suits | なし |
| ソーラーヘルメット | Solar Helmet | 鉄：５or鉄のヘルメット：１＋ソーラーパネル：１＋銅のケーブル：３<br>公式レシピ: (定型) #forge:ingots/ironx5 + ic2:solar_panel + ic2:copper_insulated_cable_itemx3 -> ic2:solar_helmet<br>公式レシピ: (定型) minecraft:iron_helmet + ic2:solar_panel + ic2:copper_insulated_cable_itemx3 -> ic2:solar_helmet |
| スタティックブーツ | Static Boots | 鉄：４or鉄のブーツ：１＋羊毛：１＋銅のケーブル：３<br>公式レシピ: (定型) #forge:ingots/ironx4 + #minecraft:wool + ic2:copper_insulated_cable_itemx3 -> ic2:static_boots<br>公式レシピ: (定型) minecraft:iron_boots + #minecraft:wool + ic2:copper_insulated_cable_itemx3 -> ic2:static_boots |

### 4. 新しい道具

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ブロンズ製工具 | Bronze Tools | なし |
| チェーンソー | Chain Saw | 電子回路：１＋精錬鉄：５：充電式電池：１<br>公式レシピ: (定型) #forge:ingots/refined_ironx5 + ic2:circuit + ic2:re_battery -> ic2:chainsaw |
| 採掘用ドリル | Mining Drill | 電子回路：１＋精錬鉄：５＋充電式電池：１ |
| ダイヤモンドドリル | Diamond Drill | 採掘用ドリル：１＋ダイヤモンド：３<br>公式レシピ: (定型) #forge:gems/diamondx3 + ic2:drill -> ic2:drill_diamond |
| 電動クワ | Electric Hoe | 充電式電池：１＋電子回路：１＋精錬鉄：２<br>公式レシピ: (定型) #forge:ingots/refined_ironx2 + ic2:circuit + ic2:re_battery -> ic2:electric_hoe |
| レンチ | Wrench | ブロンズ：６<br>公式レシピ: (定型) #forge:ingots/bronzex6 -> ic2:wrench |
| 電動レンチ | Electric Wrench | レンチ：１＋電子回路：１＋充電式電池：１<br>公式レシピ: (定型) ic2:wrench + ic2:circuit + ic2:re_battery -> electricWrench |
| EUリーダー | EU-Reader | 電子回路：１＋グロウストーンの粉：１：銅のケーブル：４<br>公式レシピ: (定型) minecraft:glowstone_dust + ic2:copper_insulated_cable_itemx4 + ic2:circuit -> ic2:eu_reader |
| CFスプレー | CF Sprayer | （CFスプレー）丸石：４＋木の棒：１ / （補充）CFスプレー：１＋CFペレット：１/CFスプレー：１＋CFペレット：８ |
| CFペレット | CF Pellet | なし |
| カッター | Inslation Cutter | 精錬鉄：３＋鉄インゴット：２ |
| ダイナマイト | Dynamite | 工業用TNT：１＋糸：１<br>公式レシピ: (不定形) ic2:itnt + minecraft:string -> ic2:dynamitex8 |
| 粘着ダイナマイト | Sticky Dynamite | ダイナマイト：８＋べたつく樹液：１<br>公式レシピ: (定型) ic2:dynamitex8 + ic2:sticky_resin -> ic2:sticky_dynamitex8 |
| 遠隔起爆装置 | Dynamite-O-Mote | 周波数送信機：１＋スズインゴット：２＋銅のケーブル：１＋ラピスラズリ：１ / 銅のケーブル：１＋電子回路：１＋グロウストーンダスト：２＋TNT：３<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + minecraft:glowstone_dustx2 + ic2:circuit + minecraft:tntx3 -> ic2:dynamite_remote<br>公式レシピ: (定型) ic2:copper_insulated_cable_item + #forge:ingots/tinx2 + minecraft:lapis_lazuli + ic2:frequency_transmitter -> ic2:dynamite_remote |
| ODスキャナー | OD Scanner | 充電式電池：１＋電子回路：２＋グロウストーンの粉：１＋銅のケーブル：３<br>公式レシピ: (定型) minecraft:glowstone_dust + ic2:circuitx2 + ic2:re_battery + ic2:copper_insulated_cable_itemx3 -> ic2:scanner_od |
| OVスキャナー | OV Scanner | ODスキャナー：１＋発展回路：２＋グロウストーンの粉：３＋金の2倍絶縁ケーブル：２(注意、画像は銅ケーブル)<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:gold_double_insulated_cable_itemx2 + ic2:scanner_od -> ic2:scanner_ov<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:bronze_double_insulated_cable_itemx2 + ic2:scanner_od -> ic2:scanner_ov<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:gold_double_insulated_cable_itemx2 + ic2:re_battery -> ic2:scanner_ov<br>公式レシピ: (定型) minecraft:glowstone_dustx3 + ic2:advanced_circuit + ic2:bronze_double_insulated_cable_itemx2 + ic2:re_battery -> ic2:scanner_ov |
| 採掘用レーザー | Mining Laser | エナジークリスタル：２＋発展回路：１＋合金板：４＋レッドストーン：１<br>公式レシピ: (定型) #forge:dusts/redstone + ic2:energy_crystalx2 + ic2:plate_advanced_alloyx5 -> ic2:mining_laser |
| スズ製の缶 | Tin Can | スズインゴット：５<br>公式レシピ: (定型) #forge:ingots/tinx5 -> ic2:tin_canx4<br>公式レシピ: (鍛冶台) #forge:ingots/tin + ic2:cutter -> ic2:tin_canx2 |
| 食物缶詰 | Food Can | なし |
| ツリータップ | Treetap | 木材：５<br>公式レシピ: (定型) #minecraft:planksx5 -> ic2:treetap |
| 電動ツリータップ | Electric Treetap | ツリータップ：１＋電子回路：１＋充電式電池：１<br>公式レシピ: (定型) ic2:treetap + ic2:circuit + ic2:re_battery -> ic2:electric_treetap |
| ナノセイバー | Nano Saber | エナジークリスタル：１＋合金板：２＋グロウストーンダスト：２＋カーボンプレート：２<br>公式レシピ: (定型) minecraft:glowstone_dustx2 + ic2:plate_advanced_alloyx2 + ic2:carbon_platex2 + ic2:energy_crystal -> ic2:nano_saber |
| ペインター | Painter | （ペインター）羊毛：３＋鉄インゴット：２ \|\| （色付き）ペインター：１＋各種染料：１<br>公式レシピ: (定型) #minecraft:woolx3 + #forge:ingots/ironx2 -> ic2:painter |
| 周波数送信機 | Frequency Transmitter | 電子回路：１＋銅のケーブル：１<br>公式レシピ: (不定形) ic2:circuit + ic2:copper_insulated_cable_item -> ic2:frequency_transmitter |
| コイン | Industrial Credit | 精錬鉄：４<br>公式レシピ: (不定形) ic2:coin_8x -> ic2:coinx8<br>公式レシピ: (定型) ic2:coin_4096xx8 -> ic2:coin_32768x<br>公式レシピ: (定型) ic2:coin_512xx8 -> ic2:coin_4096x<br>公式レシピ: (不定形) ic2:coin_32768x -> ic2:coin_4096xx8<br>公式レシピ: (定型) ic2:coin_64xx8 -> ic2:coin_512x<br>公式レシピ: (不定形) ic2:coin_4096x -> ic2:coin_512xx8<br>公式レシピ: (定型) ic2:coin_8xx8 -> ic2:coin_64x<br>公式レシピ: (不定形) ic2:coin_512x -> ic2:coin_64xx8<br>公式レシピ: (定型) ic2:coinx8 -> ic2:coin_8x<br>公式レシピ: (不定形) ic2:coin_64x -> ic2:coin_8xx8 |
| ツールボックス | Toolbox | チェストボックス：１＋精錬鉄：５<br>公式レシピ: (定型) #forge:ingots/refined_ironx5 + #forge:chests/wooden -> ic2:tool_box |
| マグカップ | Stone Mug | 石：７ |
| 酒樽 | Booze Barrel | ゴムの原木：１＋木材：２ |

### 5. セル

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 空のセル | Empty Cell | スズインゴット：４<br>公式レシピ: (定型) #forge:ingots/tinx4 -> ic2:cell_emptyx16 |
| 圧縮空気セル | Compressd Air Cell | なし |
| バイオセル | Bio Cell | 空のセル：１＋圧縮した植物の玉：１<br>公式レシピ: (不定形) ic2:cell_empty + ic2:compressed_plant_ball -> ic2:cell_bio |
| 液化石炭セル | H. Coal Cell | 空のセル：１＋圧縮した液化石炭：１ |
| バイオ燃料セル | Biofuel Cell | なし |
| 石炭燃料セル | Coalfuel Cell | なし |
| 溶岩セル | Lava Cell | 空のセル：１＋溶岩入りバケツ：１ |
| 水セル | Water Cell | 空のセル：１＋水入りバケツ：１ |
| 電解水セル | Electrolyzed Water Cell | なし |
| K冷却セル | 10K Coolant Cell | v1.103までは水セルを抽出器にかけると製作可能. / v1.106以後は水セル：１＋スズインゴット：４で製作可能.<br>公式レシピ: (定型) #forge:ingots/tinx4 + fluid:water -> ic2:heat_storage_single |

### 6. テラフォーマーブループリント

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 空のTFBP | Empty TFBP | 発展回路：１＋電子回路：１＋レッドストーン：２ |
| 寒冷化 | Chilling | 空のTFBP：１＋雪玉：４ |
| 砂漠化 | Desertification | 空のTFBP：１＋砂：４ |
| 平地化 | Flatificator | 空のTFBP：１＋土：４ |
| 培養 | Cultivation | 空のTFBP：１＋種：４ |
| 灌漑 | Irrigation | 空のTFBP：１＋水入りバケツ：４ |
| 榎茸 | Mushroom | 空のTFBP：１＋キノコ：４＋菌糸ブロック：４ |

### 7. アップグレードモジュール

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| オーバークロッカー | Overclocker upgrade | 電子回路：１＋冷却セル：３＋銅のケーブル：２<br>公式レシピ: (定型) ic2:heat_storage_singlex3 + ic2:copper_insulated_cable_itemx2 + ic2:circuit -> ic2:overclocker_upgrade<br>公式レシピ: (定型) ic2:heat_storage_triplex3 + ic2:copper_insulated_cable_itemx2 + ic2:circuit -> ic2:overclocker_upgradex3<br>公式レシピ: (定型) ic2:heat_storage_sixx3 + ic2:copper_insulated_cable_itemx2 + ic2:circuit -> ic2:overclocker_upgradex6<br>公式レシピ: (定型) ic2:heat_storage_singlex4 + ic2:upgrade_base -> ic2:overclocker_upgrade<br>公式レシピ: (定型) ic2:heat_storage_singlex8 + ic2:upgrade_base -> ic2:overclocker_upgradex2<br>公式レシピ: (定型) ic2:heat_storage_triplex4 + ic2:upgrade_base -> ic2:overclocker_upgradex4<br>公式レシピ: (定型) ic2:heat_storage_triplex8 + ic2:upgrade_base -> ic2:overclocker_upgradex8<br>公式レシピ: (定型) ic2:heat_storage_sixx4 + ic2:upgrade_base -> ic2:overclocker_upgradex8<br>公式レシピ: (定型) ic2:heat_storage_sixx8 + ic2:upgrade_base -> ic2:overclocker_upgradex16 |
| トランスフォーマー | Transformer upgrade | 中圧変換装置：１＋電子回路：１＋金の２倍絶縁ケーブル：２＋ガラス：５<br>公式レシピ: (定型) #forge:glassx5 + ic2:gold_double_insulated_cable_itemx2 + ic2:transformer_mv + ic2:circuit -> ic2:transformer_upgrade<br>公式レシピ: (定型) #forge:glassx7 + ic2:transformer_mv + ic2:upgrade_base -> ic2:transformer_upgrade |
| エナジーストレージ | Energy storage upgrade | 充電式電池：１＋電子回路：１＋銅のケーブル：２＋木材：５<br>公式レシピ: (定型) #minecraft:planksx5 + ic2:copper_insulated_cable_itemx2 + ic2:re_battery + ic2:circuit -> ic2:energy_storage_upgrade<br>公式レシピ: (定型) #minecraft:planksx7 + ic2:re_battery + ic2:upgrade_base -> ic2:energy_storage_upgrade |
| イジェクター | Ejecter Upgrade | ピストン：２＋電子回路：１＋銅のケーブル：２＋ホッパー：１(※バニラのもの) |

### 8. 既存アイテムの追加レシピ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 木材（ジャングル） | Jungle Wooden Planks | ゴム原木：１ |
| 原木（ジャングル） | Jungle Wood | なし |
| バケツ | Bucket | スズインゴット：３<br>公式レシピ: (定型) #forge:ingots/tinx3 -> minecraft:bucket |
| 線路 | Rail | 青銅インゴット：６＋棒：１<br>公式レシピ: (定型) #forge:ingots/bronzex6 + minecraft:stick -> minecraft:railx8 |
| ピストン | Piston | 木材：３＋丸石：４＋青銅インゴット：１＋レッドストーン：１<br>公式レシピ: (定型) #minecraft:planksx3 + #forge:cobblestonex4 + #forge:ingots/silver + #forge:dusts/redstone -> minecraft:piston<br>公式レシピ: (定型) #minecraft:planksx3 + #forge:cobblestonex4 + #forge:ingots/bronze + #forge:dusts/redstone -> minecraft:piston |
| 粘着ピストン | Sticky piston | ピストン：１＋樹液：１<br>公式レシピ: (不定形) minecraft:piston + ic2:sticky_resin + RecipeMods.HIDDEN_RECIPE -> minecraft:sticky_piston |
| 松明 | Torch | 樹液：１＋棒：１<br>公式レシピ: (定型) ic2:sticky_resin + minecraft:stick -> minecraft:torchx4 |
| グロウストーンの粉 | GrowStone Dusts | レッドストーン：５＋金の粉：４ |
| 火薬 | Gunpowder | レッドストーン：５＋石炭の粉：４<br>公式レシピ: (定型) #forge:dusts/redstonex5 + #forge:dusts/coalx4 -> minecraft:gunpowderx3 |
| 黒曜石 | Obsidian | 水セル：２＋溶岩セル：２<br>公式レシピ: (不定形) ic2:cell_waterx2 + ic2:cell_lavax2 -> minecraft:obsidian<br>公式レシピ: (不定形) ic2:cell_waterx2 + minecraft:magma_blockx2 -> minecraft:obsidian |

### 1. 原子炉関連

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 原子炉 | Nuclear Reactor | 合金板：４＋発展回路：２＋チャンバー：２＋火力発電機：１<br>公式レシピ: (定型) ic2:advanced_circuit + ic2:reactor_chamberx3 + ic2:generator -> ic2:nuclear_reactor |
| チャンバー | Reactor Chamber | マシン：１＋ヒートディスペンサー：１＋リアクタープレート：３＋合金板：４<br>公式レシピ: (定型) ic2:plate_dense_copperx4 + ic2:machine_block -> ic2:reactor_chamber |
| ウランセル | Uranium Cell | 空のセル：１＋精錬ウラン：１ |
| 準劣化ウランセル | Near-Depleted Uranium Cell | 空のセル：８＋精錬ウラン：１ |
| 劣化ウラン同位体セル | Depleted Isotope Cell | 準劣化ウランセル：１＋石炭の粉：１ |
| 再濃縮ウランセル | Re-Enriched Uranium Cell | (ウランセル)再濃縮ウランセル：１＋石炭の粉：１ |
| 冷却セル | Cooling Cell | なし |
| リアクタープレート | Integrated Reactor Plating | 合金板：１＋銅インゴット：４ |
| ヒートディスペンサー | Integrated Heat Disperser | リアクタープレート：１＋発展回路：１＋銅インゴット：２＋冷却セル：２ |

### 1. 通常品種

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 雑草、Tier 0 | Weed | なし |
| 小麦、Tier 1 | Wheat | なし |
| カボチャ、Tier 1 | Pumpkin | なし |
| スイカ、Tier 2 | Melon | なし |
| カカオ豆、Tier 3 | Cocoa | <span style='color: red;'>栄養が豊富な支柱にしか植えられない. </span> |
| サトウキビ、Tier 2 | Sugar Canes | なし |
| 赤の花、Tier 2 | Rose | なし<br>公式レシピ: (不定形) ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.GREEN) + ColorMaps.CFOAM_BLOCKS.getBlock(DyeColor.PINK) -> ic2:rose |
| 黄の花、Tier 2 | Flower | なし |
| ネザーウォート、Tier 5 | Nether Wart | v1.90から追加 |

### 2. 支柱が持つ5つのパラメータ（光量、通気、栄養、保水、除草）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ．光量 | Light | - |
| ．通気 | Air | - |
| ．栄養 | Nutrition | - |
| ．保水 | Hydration | - |
| ．除草 | Herbicide | - |
| 肥料 | Fertilizer | 骨粉1 + スクラップ1<br>公式レシピ: (不定形) ic2:scrap + minecraft:bone_meal -> ic2:fertilizerx2<br>公式レシピ: (不定形) ic2:scrapx2 + ic2:fertilizer -> ic2:fertilizerx2 |
| 保水剤セル | Hydration Cell | 1.103以前:水セルを2回抽出機にかける 1.106以降:水セルを1回抽出機にかける |
| グリンパウダー | Grin Powder | クモの目を粉砕機にかける |
| 除草スプレー | Weed-EX | 空のセル1 + 毒の粉1 + レッドストーンパウダー1<br>公式レシピ: (定型) #forge:dusts/redstone + ic2:grin_powder + ic2:cell_empty -> ic2:weedex |
| 作物分析機 | Cropnalyzer | ガラス：１＋電子回路：１＋レッドストーン：４＋銅のケーブル：２ |
| 農薬散布機 | Crop-Matron | 電子回路2 + チェスト1 + マシン1 + 園芸用支柱5 → 1個<br>公式レシピ: (定型) ic2:circuitx2 + #forge:chests/wooden + ic2:crop_sticksx5 + ic2:machine_block -> ic2:crop_matron |

### 3. １．農薬散布機を設置する

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 農薬散布機 | Crop-Matron | なし<br>公式レシピ: (定型) ic2:circuitx2 + #forge:chests/wooden + ic2:crop_sticksx5 + ic2:machine_block -> ic2:crop_matron |

### 4. ３．作物分析機で作物を鑑定

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 作物分析機 | Cropnalyzer | ガラス1 + 電子回路1 + レッドストーン4 + 銅のケーブル2 |

### 7.4. ４．飲む

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 足場ブロック | Scaffold | 木材3 + 木の棒3 |
| 酒樽 | Booze Barrel | ゴムの原木1 + 木材2 |
| ツリータップ | Treetap | 木材5<br>公式レシピ: (定型) #minecraft:planksx5 -> ic2:treetap |
| マグカップ | Stone Mug | 焼石7 |

### 8. 糖蜜酒（ラム）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ラム、未発酵 | Rum | 以下の中からランダムで1つの効果が発生. |
| ラム、20分以上熟成 | Rum | なし |

### 9. 麦酒（ビール、エール）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 暗黒物質X | Black Stuff | 失敗作. 以下の中からランダムで1つの効果が発生. |
| 仕込液 | Brew | なし |
| 若い酒 | Youngster | なし |
| ビール | Beer | なし |
| エール | Ale | なし |
| 竜血酒 | Dragonblood | なし |
| Watery | 水っぽい | なし |
| Lite | 薄い | なし |
|  | 接頭語なし | なし |
| Strong | 強めの | なし |
| Thick | 濃厚な | なし |
| Stodge | 特濃の | なし |
| Soup | スープ | なし |
| Alcfree | ノンアルコール | なし |
| White | ホワイト | なし |
|  | 接頭語なし | なし |
| Dark | ダーク | なし |
| Full | フル | なし |
| Black | ブラック | なし |

### 10. コーヒー

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| コールドコーヒー | Cold Coffee | なし |
| ブラックホットコーヒー | Dark Coffee | なし |
| ホットコーヒー | Coffee | なし |

(334 項目)

## Project:Red (MC1.21.1)


### 1.1. 中間素材

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ストーンウェハー | Stone Wafer | なし |
| 導線基板 | Stone Wire | レッドストーン＋ストーンウェハー |
| ワイヤー基板 | Stone Redwire | 赤合金ワイヤー＋ストーンウェハー |
| ケーブル束基板 | Stone Bundle | ケーブル束＋ストーンウェハー |
| 陽極基板 | Stone Anode | レッドストーン4＋ストーンウェハー3 |
| 陰極基板 | Stone Cathode | レッドストーントーチ＋ストーンウェハー |
| 指針基板 | Pointer | 石＋レッドストーントーチ＋ストーンウェハー<br>公式レシピ: (定型) #c:stones + minecraft:redstone_torch + projectred_core:plate -> projectred_core:pointer |
| シリコンチップ基板 | Silicon Chip | ストーンウェハー3＋レッドウェハー<br>公式レシピ: (定型) projectred_core:infused_silicon + projectred_core:platex3 -> projectred_core:silicon_chip |
| 不純物入りシリコンチップ基板 | Energized Silicon Chip | ストーンウェハー3＋イエローウェハー<br>公式レシピ: (定型) projectred_core:energized_silicon + projectred_core:platex3 -> projectred_core:energized_silicon_chip |
| アセンブリ基板 | Plate Assembly | ストーンウェハー3＋棒2＋ワイヤー基板2 |
| 赤合金インゴット | Red Alloy Ingot | 赤鉄化合物を精錬<br>公式レシピ: (精錬) projectred_core:red_iron_comp -> projectred_core:red_ingot |
| 高純度シリコン | Silicon Boule | 砂石炭化合物を精錬<br>公式レシピ: (精錬) projectred_core:sand_coal_comp -> projectred_core:boule |
| シリコンウェハー | Silicon | [手ノコ]高純度シリコン<br>公式レシピ: (定型) cb_microblock:diamond_saw + projectred_core:boule -> projectred_core:siliconx8 |
| レッドウェハー | Infused Silicon | レッドシリコン化合物を製錬<br>公式レシピ: (精錬) projectred_core:red_silicon_comp -> projectred_core:infused_silicon |
| 不純物入りシリコンウェハー | Energized Silicon | 不純物シリコン化合物を製錬<br>公式レシピ: (精錬) projectred_core:glow_silicon_comp -> projectred_core:energized_silicon |
| モーター | Motor | 鉄インゴット+石2+銅製のコイル2+レッドストーン2<br>公式レシピ: (定型) #c:ingots/iron + #c:stonesx2 + projectred_core:copper_coilx2 + #c:dusts/redstonex2 -> projectred_core:motor |
| 銅製のコイル | Copper Coil | [絞り板]銅インゴット<br>公式レシピ: (定型) #c:ingots/copper + projectred_core:draw_plate -> projectred_core:copper_coil |
| 鉄製のコイル | Iron Coil | [絞り板]鉄インゴット<br>公式レシピ: (定型) #c:ingots/iron + projectred_core:draw_plate -> projectred_core:iron_coil |
| 金製のコイル | Gold Coil | [絞り板]金インゴット<br>公式レシピ: (定型) #c:ingots/gold + projectred_core:draw_plate -> projectred_core:gold_coil |
| 帆布 | Woven Cloth | 糸9+棒1<br>公式レシピ: (定型) #c:stringsx8 + #c:rods/wooden -> projectred_core:woven_cloth |
| 帆 | Sail | 帆布6<br>公式レシピ: (定型) projectred_core:woven_clothx6 -> projectred_core:sail |
| ルビー | Ruby | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_ruby_ore -> projectred_core:ruby<br>公式レシピ: (不定形) projectred_exploration:ruby_block -> projectred_core:rubyx9<br>公式レシピ: (精錬) projectred_exploration:ruby_ore -> projectred_core:ruby |
| サファイヤ | Sapphire | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_sapphire_ore -> projectred_core:sapphire<br>公式レシピ: (不定形) projectred_exploration:sapphire_block -> projectred_core:sapphirex9<br>公式レシピ: (精錬) projectred_exploration:sapphire_ore -> projectred_core:sapphire |
| ペリドット | Peridot | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_peridot_ore -> projectred_core:peridot<br>公式レシピ: (不定形) projectred_exploration:peridot_block -> projectred_core:peridotx9<br>公式レシピ: (精錬) projectred_exploration:peridot_ore -> projectred_core:peridot |
| 赤合金化合物 | Red Iron Compound | 鉄インゴット1+レッドストーン8<br>公式レシピ: (定型) #c:dusts/redstonex8 + #c:ingots/iron -> projectred_core:red_iron_comp |
| 砂石炭化合物 | Sandy Coal Compound | 石炭ブロック1+砂or赤砂8 |
| レッドシリコン化合物 | Red Silicon Compound | シリコンウェハー1+レッドストーン8<br>公式レシピ: (定型) #c:dusts/redstonex8 + projectred_core:silicon -> projectred_core:red_silicon_comp |
| 不純物シリコン化合物 | Glowing Silicon Compound | シリコンウェハー1+グロウストーン8<br>公式レシピ: (定型) #c:dusts/glowstonex8 + projectred_core:silicon -> projectred_core:glow_silicon_comp |
| 青合金インゴット | Electrotine Alloy Ingot | 青合金化合物を製錬<br>公式レシピ: (精錬) projectred_core:electrotine_iron_comp -> projectred_core:electrotine_ingot |
| 青合金化合物 | Electotine Iron Compound | ニコライトパウダー8＋鉄インゴット1 |
| ブルーシリコン化合物 | Electrotine Silicon Compound | シリコンウェハー1+ニコライトパウダー8<br>公式レシピ: (定型) #c:dusts/electrotinex8 + projectred_core:silicon -> projectred_core:electrotine_silicon_comp |
| ブルーウェハー | Electro Silicon | ブルーシリコン化合物を製錬 |
| ドライバー | Screw Driver | 鉄インゴット3＋ラピスラズリ2<br>公式レシピ: (定型) #c:ingots/ironx3 + #c:dyes/bluex2 -> projectred_core:screwdriver |
| 絞り板 | Draw Plate | 鉄ブロック Post4＋ダイヤモンドブロック Panel1<br>公式レシピ: (定型) neoforge:components:{"type": "neoforge:components", "componex5 -> projectred_core:draw_plate |
| 検流計 | Multimeter | 赤合金インゴット2＋黒の染料2＋赤の染料2＋エメラルド1＋グロウストーンダスト1<br>公式レシピ: (定型) #c:ingots/red_alloyx2 + #c:dyes/blackx2 + #c:dyes/green + #c:dyes/redx2 + #c:dusts/glowstone -> projectred_core:multimeter |

### 2.1. 手ノコ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 石の棒 | Stone Rod | 石2 |
| 石ノコ | Stone Saw | 棒2＋石の棒3＋火打石1 |
| 鉄ノコ | Iron Saw | 棒2＋石の棒3＋鉄インゴット1 |
| ダイヤノコ | Diamond Saw | 棒2＋石の棒3＋ダイヤモンド1 |

### 2.2. 1/2、1/4、1/8ブロック

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 穴空き板 | Hollow Cover / Hollow Panel / Hollow Slab | なし |

### 1.1. 配線

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 赤合金ワイヤー | Red Alloy Wire | 赤合金インゴット3<br>公式レシピ: (定型) #c:ingots/red_alloyx3 -> projectred_transmission:red_alloy_wirex12 |
| 絶縁ワイヤー | Insulated Wire | 赤合金インゴット3＋羊毛6 |
| 絶縁ワイヤー | 再染色 | なし |
| ケーブル束 | 染色 | なし |
| ジャケットワイヤー | Framed Red Alloy Wire | 赤合金ワイヤー＋オークの原木 strip<br>公式レシピ: (定型) #c:rods/woodenx8 + projectred_transmission:red_alloy_wire -> projectred_transmission:framed_red_alloy_wire |
| ジャケット絶縁ワイヤー | Framed Insulated Wire | 絶縁ワイヤー+オークの原木 strip8 |
| ジャケットケーブル | Framed Bundled Cable | ケーブル束＋オークの原木 strip8<br>公式レシピ: (定型) #c:rods/woodenx8 + projectred_transmission:neutral_bundled_wire -> projectred_transmission:neutral_framed_bundled_wire |
| 青合金ワイヤー | Blue Alloy Wire | 青合金インゴット3＋羊毛6 |
| 高圧ワイヤー | 10kV Wire | 青合金ワイヤー+羊毛 |
| ジャケット青合金ワイヤー | Jacketed Bluewire | 青合金インゴット＋オークの原木 strip8 |

### 1.2. 集積回路

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ORゲート | OR Gate | 陰極基板2＋導線基板3＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:cathodex2 + projectred_core:conductive_platex3 -> projectred_integration:or_gate |
| NORゲート | NOR Gate | 陰極基板＋陽極基板＋導線基板3＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:anode + projectred_core:conductive_platex3 + projectred_core:cathode -> projectred_integration:nor_gate |
| NOTゲート | NOT Gate | 陰極基板3＋陽極基板＋導線基板＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:cathodex3 + projectred_core:anode + projectred_core:conductive_plate -> projectred_integration:not_gate |
| ANDゲート | AND Gate | 陰極基板4＋陽極基板2＋導線基板＋ストーンウェハー2<br>公式レシピ: (定型) projectred_core:anodex2 + projectred_core:cathodex4 + projectred_core:platex2 + projectred_core:conductive_plate -> projectred_integration:and_gate |
| NANDゲート | NAND Gate | 陰極基板3＋陽極基板3＋導線基板＋ストーンウェハー2<br>公式レシピ: (定型) projectred_core:anodex3 + projectred_core:cathodex3 + projectred_core:platex2 + projectred_core:conductive_plate -> projectred_integration:nand_gate |
| XORゲート | XOR Gate | 陰極基板3＋陽極基板3＋導線基板3<br>公式レシピ: (定型) projectred_core:anodex3 + projectred_core:conductive_platex3 + projectred_core:cathodex3 -> projectred_integration:xor_gate |
| XNORゲート | XNOR Gate | 陰極基板4＋陽極基板3＋導線基板2<br>公式レシピ: (定型) projectred_core:anodex3 + projectred_core:cathodex4 + projectred_core:conductive_platex2 -> projectred_integration:xnor_gate |
| Bufferゲート | Buffer Gate | 陰極基板2＋陽極基板2＋導線基板3＋ストーンウェハー2<br>公式レシピ: (定型) projectred_core:anodex2 + projectred_core:cathodex2 + projectred_core:conductive_platex3 + projectred_core:platex2 -> projectred_integration:buffer_gate |
| マルチプレクサ回路 | Multiplexer | 陰極基板4＋陽極基板3＋導線基板1＋ストーンウェハー<br>公式レシピ: (定型) projectred_core:anodex3 + projectred_core:cathodex4 + projectred_core:plate + projectred_core:conductive_plate -> projectred_integration:multiplexer_gate |
| パルス回路 | Pulse Former | 陰極基板3＋陽極基板3＋導線基板2＋ストーンウェハー<br>公式レシピ: (定型) projectred_core:anodex3 + projectred_core:cathodex3 + projectred_core:conductive_platex2 + projectred_core:plate -> projectred_integration:pulse_gate |
| リピーター回路 | Repeater | 陰極基板2＋陽極基板2＋導線基板＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:cathodex2 + projectred_core:anodex2 + projectred_core:conductive_plate -> projectred_integration:repeater_gate |
| 乱数回路 | Randomizer | 不純物入りシリコンチップ基板3＋導線基板4＋ストーンウェハー2<br>公式レシピ: (定型) projectred_core:platex2 + projectred_core:energized_silicon_chipx3 + projectred_core:conductive_platex4 -> projectred_integration:randomizer_gate |
| RS NOR Latch回路 | RS Latch | 陰極基板2＋陽極基板2＋導線基板4＋ストーンウェハー<br>公式レシピ: (定型) projectred_core:anodex2 + projectred_core:cathodex2 + projectred_core:conductive_platex4 + projectred_core:plate -> projectred_integration:sr_latch_gate |
| Toggle Latch回路 | Toggle Latch | レバー＋陰極基板2＋＋導線基板2＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:cathodex2 + projectred_core:platex4 + projectred_core:conductive_platex2 + minecraft:lever -> projectred_integration:toggle_latch_gate |
| D Latch回路 | Transparent Latch | 陰極基板5＋陽極基板＋導線基板2＋ストーンウェハー<br>公式レシピ: (定型) projectred_core:anode + projectred_core:cathodex5 + projectred_core:conductive_platex2 + projectred_core:plate -> projectred_integration:transparent_latch_gate |
| 光センサー | Light Sensor | ラピスラズリ3＋導線基板＋ストーンウェハー5<br>公式レシピ: (定型) projectred_core:platex5 + #c:dyes/bluex3 + projectred_core:conductive_plate -> projectred_integration:light_sensor_gate |
| 雨センサー | Rain Sensor | スライムボール3＋導線基板＋ストーンウェハー5<br>公式レシピ: (定型) projectred_core:platex5 + #c:slimeballsx3 + projectred_core:conductive_plate -> projectred_integration:rain_sensor_gate |
| タイマー回路 | Redstone Timer | 指針基板＋陰極基板＋陽極基板2＋導線基板3＋ストーンウェハー2 |
| シーケンス回路 | Redstone Sequencer | 指針基板＋陰極基板4＋ストーンウェハー4 |
| カウンタ回路 | Counter | 指針基板＋陰極基板2＋導線基板2＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:cathodex2 + projectred_core:conductive_platex2 + projectred_core:pointer -> projectred_integration:counter_gate |
| State Cell回路 | State Cell | シリコンチップ基板＋指針基板＋陰極基板＋陽極基板＋導線基板2＋ストーンウェハー3<br>公式レシピ: (定型) projectred_core:platex3 + projectred_core:anode + projectred_core:cathode + projectred_core:conductive_platex2 + projectred_core:silicon_chip + projectred_core:pointer -> projectred_integration:state_cell_gate |
| 同期回路 | Synchronizer | シリコンチップ基板2＋陰極基板＋陽極基板＋導線基板5<br>公式レシピ: (定型) projectred_core:conductive_platex5 + projectred_core:cathode + projectred_core:silicon_chipx2 + projectred_core:anode -> projectred_integration:synchronizer_gate |
| トランシーババス | Bus Transceiver | ケーブル束基板6＋シリコンチップ基盤2＋ストーンウェハー<br>公式レシピ: (定型) projectred_core:bundled_platex6 + projectred_core:silicon_chipx2 + projectred_core:plate -> projectred_integration:bus_transceiver_gate |
| コンパレータ回路 | Comparator | 導線基板2＋陰極基盤＋ネザー水晶2＋ストーンウェハー2<br>公式レシピ: (定型) projectred_core:conductive_platex4 + projectred_core:cathode + #c:gems/quartzx2 + projectred_core:platex2 -> projectred_integration:comparator_gate |
| 乱数バス | Bus Randomizer | ケーブル束基板6＋導線基盤2＋不純物入りシリコンチップ基板<br>公式レシピ: (定型) projectred_core:bundled_platex6 + projectred_core:conductive_platex2 + projectred_core:energized_silicon_chip -> projectred_integration:bus_randomizer_gate |
| コンバータバス | Bus Converter | ケーブル束基板＋導線基盤3＋シリコンチップ基板＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:bundled_plate + projectred_core:conductive_platex3 + projectred_core:silicon_chip -> projectred_integration:bus_converter_gate |
| 挿入パネルバス | Bus Input Panel | ケーブル束基板7＋導線基盤＋任意の色のLEDパウダー<br>公式レシピ: (定型) projectred_core:bundled_platex7 + projectred_core:conductive_plate + #projectred_core:illumars -> projectred_integration:bus_input_panel_gate |
| 配列素子 | 旧Arrayのもの | - |
| 交差素子 | Null Cell | アセンブリ基盤+ワイヤー基板4＋ストーンウェハー4<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:wired_platex4 + projectred_core:platformed_plate -> projectred_integration:null_cell_gate |
| 反転交差素子 | Invert Cell | アセンブリ基板＋ワイヤー基板3＋ストーンウェハー4+陰極基盤<br>公式レシピ: (定型) projectred_core:platex4 + projectred_core:wired_platex3 + projectred_core:platformed_plate + projectred_core:cathode -> projectred_integration:invert_cell_gate |
| 連動交差素子 | Buffer Cell | アセンブリ基板＋ワイヤー基板3＋ストーンウェハー3+陰極基盤2<br>公式レシピ: (定型) projectred_core:platex3 + projectred_core:wired_platex3 + projectred_core:platformed_plate + projectred_core:cathodex2 -> projectred_integration:buffer_cell_gate |
| AND素子 | AND Cell | アセンブリ基板＋ワイヤー基板2＋ストーンウェハー+陰極基盤3＋導線基板2<br>公式レシピ: (定型) projectred_core:cathodex3 + projectred_core:conductive_platex2 + projectred_core:wired_platex2 + projectred_core:platformed_plate + projectred_core:plate -> projectred_integration:and_cell_gate |
| データ素子 | Stacking Latch | ワイヤー基板2＋ストーンウェハー3+陰極基盤4 |

### 1.1. ツール、中間素材

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 絞り板 | Diamond Drawplate | 1/4ダイヤブロック ＋ 1/4*1/4鉄ブロック4 |
| 電動ドライバー | Sonic Screwdriver | グリーンサファイヤ＋ブラスインゴット＋青電力電池 |
| 銅細線 | Fine Copper Wire | 銅インゴット＋絞り板 |
| 鉄細線 | Fine Iron Wire | 鉄インゴット＋絞り板 |
| 銅線コイル | Copper Coil | 鉄インゴット＋鉄格子4＋銅細線4<br>公式レシピ: (定型) #c:ingots/copper + projectred_core:draw_plate -> projectred_core:copper_coil |
| 青電力モーター | Blulectric Motor | 銅線コイル2＋鉄インゴット6＋青合金インゴット |
| 帆 | Wooden Sail | 帆布6＋棒2＋木材ブロック |

### 1.2. ブロック操作関係のアイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| アイテム差配器、デプロイヤー | Deployer | ピストン＋チェスト＋丸石6＋レッドストーン<br>公式レシピ: (定型) #c:cobblestonesx6 + minecraft:chest + minecraft:piston + #c:dusts/redstone -> projectred_expansion:deployer |
| ブロック粉砕器、ブロックブレイカー | Block Breaker | ピストン＋鉄のツルハシ＋丸石6＋レッドストーン<br>公式レシピ: (定型) #c:cobblestonesx6 + minecraft:iron_pickaxe + minecraft:piston + #c:dusts/redstone -> projectred_expansion:block_breaker |
| 着火装置、イグナイター | Igniter | アイテム差配器＋火打石と打ち金＋ネザーラック2＋丸石4＋レッドストーン |

### 1.3. アイテム輸送関係のアイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 気送管、ニューマティックチューブ | Pneumatic Tube | ガラス＋ブラスインゴット2<br>公式レシピ: (定型) #c:ingots/copperx6 + #c:glass_panesx3 -> projectred_expansion:pneumatic_tubex8 |
| 制限チューブ、リストリクションチューブ | Restriction Tube | 気送管＋鉄インゴット |
| 配送器、トランスポーザー | Transposer | ピストン＋丸石5＋木材2＋レッドストーン<br>公式レシピ: (定型) #minecraft:planksx3 + #c:cobblestonesx4 + minecraft:piston + #c:dusts/redstone -> projectred_expansion:transposer |
| 選別配送器、フィルター | Filter | ピストン＋丸石5＋金インゴット2＋レッドウェハー |
| アイテム検知器、アイテムディテクター | Item Detector | 木の感圧式スイッチ＋気送管2＋ブラスインゴット2＋レッドウェハー2＋木材2 |
| 取寄器、リトリーバー | Retriever | 選別配送器＋革＋ブラスインゴット2＋エンダーパール2＋鉄インゴット2＋青合金インゴット |
| 緩衝器、バッファー | Buffer | 木材4＋鉄格子4 |
| 分類器、ソーティングマシン | Sorting Machine | 選別配送器＋鉄インゴット5＋レッドウェハー2＋青合金インゴット |
| 管理器、マネージャー | Manager | 調整器＋分類器＋鉄インゴット2＋レッドウェハー2＋木材2＋青合金インゴット |
| 電子分類器、ソートロン | Sortron | 分類器＋鉄インゴット4＋レッドウェハー2＋ダイヤモンド＋フラットケーブル |
| 調整器、レギュレーター | Regulator | アイテム検知器＋緩衝器2＋ブラスインゴット2＋レッドウェハー2＋木材2 |
| 排出器、イジェクター | Ejector | 配送器＋緩衝器＋木材4＋丸石2＋レッドストーン |
| 中継器、リレー | Relay | 配送器＋緩衝器＋木材4＋丸石2＋レッドウェハー |

### 1.4. モーター関係のアイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| フレーム | Support Frame | ブラスインゴット＋棒8 |
| モーター | Frame Motor | 青電力モーター＋鉄インゴット5＋ブラスインゴット2＋青合金インゴット<br>公式レシピ: (定型) #minecraft:planksx2 + #c:ingots/iron + #c:dusts/redstonex2 + projectred_core:motor + #c:stonesx2 + #c:ingots/electrotine_alloy -> projectred_expansion:frame_motor |
| チューブ内蔵フレーム | Tube Frame | フレーム＋チューブ |
| RSチューブ内蔵フレーム | Redstone Tube Frame | フレーム＋レッドストーンチューブ / チューブ内蔵フレーム＋レッドストーンパウダー |

### 1.5. 青電力(Blulectricity)関係装置

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 青電力かまど | Blulectric Furnace | 粘土ブロック5＋鉄インゴット2＋青合金インゴット |
| 青電力合金かまど | Blulectric Alloy Furnace | レンガブロック5＋鉄インゴット2＋青合金インゴット |
| チャージングベンチ | Charging Bench | 黒曜石2＋青電力電池2＋木材ブロック2＋銅線コイル＋チェスト＋青合金インゴット<br>公式レシピ: (定型) #c:stonesx2 + projectred_core:copper_coil + #minecraft:planksx2 + projectred_expansion:battery + #c:ingots/ironx2 + #c:ingots/electrotine_alloy -> projectred_expansion:charging_bench |
| 青電力電池、BTバッテリー | BT Battery | 錫インゴット＋銅インゴット2＋ニコライトパウダー6 |
| 蓄電器、バッテリーボックス | BatteryBox | 鉄インゴット3＋木材＋青電力電池4＋青合金インゴット<br>公式レシピ: (定型) projectred_expansion:batteryx4 + #minecraft:planks + #c:ingots/ironx3 + #c:ingots/electrotine_alloy -> projectred_expansion:battery_box |
| 検流計、ボルトメーター | Voltmeter | ニコライトパウダー＋木材5＋銅インゴット3 |
| 変圧器、トランスフォーマー | Blulectric Transformer | 鉄インゴット5＋青合金インゴット2＋銅線コイル2 |
| ソーラーパネル | Solar Panel | 青合金インゴット＋ブルーウェハー8 |
| 温度差発電器、サーモパイル | Thermopile | 青合金インゴット3＋鉄インゴット2＋銅インゴット4＋ブルーウェハー2 |
| 動力発電機、キネティックジェネレータ | Kinetic Generator | 鉄インゴット6＋ブラスインゴット＋青電力モーター＋青合金インゴット |
| 風力タービン | Wooden Wind Turbine | 帆6＋鉄ブロック1/4柱3 |
| 風車 | Wooden Windmill | 帆4＋鉄ブロック1/4柱 |

### 1.1. 追加ブロック

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ルビー鉱石 | Ruby Ore | なし |
| サファイア鉱石 | Sapphire Ore | なし |
| グリーンサファイア鉱石 | Green Sapphire Ore | なし |
| 銅鉱石 | Copper Ore | なし |
| 錫鉱石 | Tin Ore | なし |
| 銀鉱石 | Silver Ore | なし |
| ニコライト鉱石 | Nikolite Ore | なし |
| 大理石 | Marble | なし |
| 大理石レンガ | Marble Brick | 大理石4<br>公式レシピ: (定型) projectred_exploration:marblex4 -> projectred_exploration:marble_brickx4 |
| 玄武岩レンガ | Basalt Brick | 玄武岩（焼石）4<br>公式レシピ: (定型) projectred_exploration:basaltx4 -> projectred_exploration:basalt_brickx4 |

### 1.3. 一次生産品

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ニコライトパウダー | Nikolite | なし |
| ルビー | Ruby | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_ruby_ore -> projectred_core:ruby<br>公式レシピ: (不定形) projectred_exploration:ruby_block -> projectred_core:rubyx9<br>公式レシピ: (精錬) projectred_exploration:ruby_ore -> projectred_core:ruby |
| サファイア | Sapphire | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_sapphire_ore -> projectred_core:sapphire<br>公式レシピ: (不定形) projectred_exploration:sapphire_block -> projectred_core:sapphirex9<br>公式レシピ: (精錬) projectred_exploration:sapphire_ore -> projectred_core:sapphire |
| グリーンサファイア | Green Sapphire | なし |

### 1.4. 一次加工品

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 銅インゴット | Copper Ingot | なし |
| 錫インゴット | Tin Ingot | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_tin_ore -> projectred_exploration:tin_ingot<br>公式レシピ: (不定形) projectred_exploration:tin_block -> projectred_exploration:tin_ingotx9<br>公式レシピ: (精錬) projectred_exploration:raw_tin -> projectred_exploration:tin_ingot<br>公式レシピ: (精錬) projectred_exploration:tin_ore -> projectred_exploration:tin_ingot |
| 銀インゴット | Silver Ingot | なし<br>公式レシピ: (精錬) projectred_exploration:deepslate_silver_ore -> projectred_exploration:silver_ingot<br>公式レシピ: (不定形) projectred_exploration:silver_block -> projectred_exploration:silver_ingotx9<br>公式レシピ: (精錬) projectred_exploration:raw_silver -> projectred_exploration:silver_ingot<br>公式レシピ: (精錬) projectred_exploration:silver_ore -> projectred_exploration:silver_ingot |
| 青合金インゴット | Blue Alloy Ingot | 銀インゴット＋ニコライトパウダー4 |
| 高純度シリコン | Silicon Boule | 砂8＋石炭8or木炭8<br>公式レシピ: (精錬) projectred_core:sand_coal_comp -> projectred_core:boule |
| シリコンウェハー | Silicon Wafer | ダイヤノコ＋高純度シリコン |
| レッドウェハー | Red-Doped Wafer | シリコンウェハー＋レッドストーン4 |
| ブルーウェハー | Blue-Doped Wafer | シリコンウェハー＋ニコライトパウダー4 |

### 1.5. ツール類

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 草刈り鎌 | Sickcle | なし |
| 宝石製の手ノコ | Gem Handsaws | 棒3＋鉄インゴット2＋各種宝石2 |
| ルビーツール各種 | Ruby Tools | なし |
| サファイアツール各種 | Sapphire Tools | なし |
| グリーンサファイアツール各種 | Green Sapphire Tools | なし |
| アサメ | Athame | 銀インゴット＋棒<br>公式レシピ: (定型) #c:ingots/silver + #c:rods/wooden -> projectred_exploration:athame |
| 紡毛カード | Wool Card | 鉄細線＋木材＋棒 |
| 【Lighting / Exploration】 | 他モジュール | - |
| LED照明ブロック | Lamp | LEDパウダー2＋板ガラス6＋レッドストーン |
| 反転LED照明ブロック | Inverted Lamp | LEDパウダー2＋板ガラス6＋レッドストーントーチ |
| LED飾り照明 | Fixture | LEDパウダー1＋板ガラス5＋ストーンウェハー2+導線基板1 |
| 反転LED飾り照明 | Inverted Fixture | LEDパウダー1＋板ガラス5＋ストーンウェハー2+陰極基板1 |
| LEDケージ照明 | Cage Lamp | LEDパウダー1＋ストーンウェハー2+鉄の柵3+導線基板 |
| 反転LEDケージ照明 | Inverted Cage Lamp | LEDパウダー1＋ストーンウェハー2+鉄の柵3+陰極基板 |
| LEDランタン | Lantern | LEDパウダー1＋板ガラス2＋ストーンウェハー4+レッドストーン1+金塊1 |
| 反転LEDランタン | Inverted Lantern | LEDパウダー1＋板ガラス2＋ストーンウェハー4+レッドストーントーチ1+金塊1 |
| FO.LED飾り照明 | Fallout Light | LEDパウダー1＋鉄格子5+金塊2+導線基板1 |
| 反転FO.LED飾り照明 | Inverted Fallout Light | LEDパウダー1＋鉄格子5+金塊2+陰極基板1 |
| LED照明ボタン | Illumar Button | LEDパウダー2＋石のボタン1 |
| FB.LED照明ボタン | Illumar Feedback Button | LED照明ボタン1＋レッドストーントーチ1 |

### 2. 【Exploration】で追加されるブロック、アイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 百合の花の種 | Deviating Lily Seeds | なし |

(165 項目)

## LogisticsPipes (MC1.12.2)


### 1. 基幹装置

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| LP変換機 | Logistics Power Junction | 【Dev570以前】基本パイプ1 + 金チップ1 + 赤石粉2 + 鉄インゴット5 \|\| 【Dev571以降】レッドストーンブロック1 + 赤石粉2 + 鉄インゴット6<br>公式レシピ: (定型) part:chip_basic + ore:dustRedstonex2 + logisticspipes:frame + ore:ingotIronx2 + minecraft:redstone_block -> logisticspipes:power_junction |
| RF変換機 | Logistics RF Power Provider | 【1.7.10-0.9.3.dev.125現在】レッドストーンブロック1 + 鉄ピストン1 + 金チップ2 + 鉄チップ1 + 紙4 |
| EU変換機 | Logistics EU Power Provider | 【1.7.10-0.9.3.dev.125現在】レッドストーンブロック1 + オーバークロッカーアップグレード2 + エナジーストレージアップグレード1 + 変圧機アップグレード1 + 紙4 |
| ロジスティクス作業台 | Logistics Crafting Table | 作業台1 + 石の歯車1 + チェスト1 + 木材4<br>公式レシピ: (定型) minecraft:crafting_table + ore:plankWoodx2 + logisticspipes:frame + minecraft:piston -> logisticspipes:crafting_table |
| 基本パイプ | Basic Logistics Pipe | 【Dev570以前】ダイヤパイプ1 + 金チップ（金の歯車）2 + 赤石トーチ2 + ガラス4 \|\| 【Dev571以降】ダイヤパイプ1 + 丸石パイプ2 + 金チップ（金の歯車）1 + 赤石トーチ1 + ガラス2<br>公式レシピ: (定型) logisticspipes:chip_fpga + logisticspipes:pipe_transport_basic -> logisticspipes:pipe_basic |
| ブランクモジュール | Blank module | 【Dev570以前】赤石粉2 + 金塊1 + 紙6 \|\| 【Dev571以降】赤石粉2 + 金塊1 + 紙2<br>公式レシピ: (定型) ore:dustRedstone + ore:paper + ore:nuggetGold -> logisticspipes:module_blank |
| ファジーロジスティクス作業台 | Logistics Fuzzy Crafting Table | ロジスティクス作業台1 + ネザー水晶1<br>公式レシピ: (定型) part:chip_basic + logisticspipes:crafting_table -> logisticspipes:crafting_table_fuzzy |

### 2.1. 供給系（プロバイダー系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 供給パイプ | Provider Logistics Pipe | 【Dev570以前】基本パイプ1 + 光石粉1 \|\| 【Dev571以降】基本パイプ1 + 金の歯車1 + 赤石粉1 \|\| 【Dev571以降】基本パイプ1 + 金チップ1 + レッドストーントーチ1 |
| 供給パイプMk2 | Provider Logistics Pipe Mk2 | 供給パイプ1 + ダイヤチップ（ダイヤの歯車）1 |

### 2.2. 要求系（リクエスト系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 要求パイプ | Request Logistics Pipe | 【Dev570以前】基本パイプ1 + 金チップ2 \|\| 【Dev571以降】基本パイプ1 + 金チップ（金の歯車）1 + 鉄の歯車1 |
| 要求パイプMk2 | Request Logistics Pipe Mk2 | 要求パイプ1 + ダイヤチップ1 \|\| 【Dev571以降のみ】要求パイプ1 + ダイヤの歯車1 + 赤石粉1 |
| リクエストディスク | Logistics Disk | 赤石粉1 + 金塊4 + イカスミ4<br>公式レシピ: (定型) ore:ingotIronx4 + ore:dustRedstonex4 + ore:nuggetGold -> logisticspipes:disk |
| 遠隔要求パイプ | Remote Orderer Logistics Pipe | 【Dev570以前】基本パイプ1 + エンダーパール1 \|\| 【Dev571以降】基本パイプ1 + エンダーパール1 + 赤石粉1 |

### 2.3. 補給系（サプライヤー系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 補給パイプ | Supplier Logistics Pipe | 基本パイプ1 + ラピスラズリ2 |

### 2.4. 加工系（クラフティング系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 加工パイプ | Crafting Logistics Pipe | 【Dev570以前】基本パイプ1 + 光石粉2 \|\| 【Dev571以降】基本パイプ1 + 赤石粉1 + 石の歯車 |
| 加工パイプMk2 | Crafting Logistics Pipe Mk2 | 加工パイプ1 + 金チップ1 \|\| 【Dev571以降のみ】加工パイプ1 + 金の歯車1 + 赤石粉1 |
| 加工パイプMk3 | Crafting Logistics Pipe Mk3 | 【溶接作業台で作成】 加工パイプ(Mk2)1 + ナノ漏斗1 + ダイヤチップ1 + (鉄インゴット1) |
| 加工補佐パイプ | Satellite Logistics Pipe | 基本パイプ1 + 赤石粉2 |

### 2.5. 液体系（リキッド系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 液体基本パイプ | Logistics Liquid Basic Pipe | 【Dev570以前】基本パイプ1 + 防水素材6 + 空バケツ2 \|\| 【Dev571以降】基本パイプ1 + 防水素材1 + 空バケツ1 |
| 液体接続パイプ | Logistics Liquid Connector Pipe | 【Dev570以前】液体金パイプ1 + 金インゴット2 + レッドストーントーチ2 + ガラス4 \|\| 【Dev571以降】液体基本パイプ1 + 液体金パイプ1 + 金インゴット2 + ガラス2 |
| 液体挿入パイプ | Logistics Liquid Insertion Pipe | 液体基本パイプ1 + ガラス瓶4 |
| 液体供給パイプ | Logistics Liquid Provider Pipe | 液体基本パイプ1 + ガラス瓶1 |
| 液体要求パイプ | Logistics Liquid Request Pipe | 液体基本パイプ1 + ガラス瓶2 |
| 液体抽出パイプ | Logistics Liquid Extractor Pipe | 液体挿入パイプ + 液体木パイプ1 |
| 液体補佐パイプ | Logistics Liquid Satellite Pipe | 【Dev571以降】液体基本パイプ1 + 赤石粉2 |
| 液体補給パイプ | Logistics Fluid Supplier | 【Dev571以降】液体基本パイプ1 + 金インゴット2 + ラピスラズリ2 |

### 3. モジュール（＋物流制御パイプ）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 物流制御パイプ Mk1 | Logistics Chassis Pipe Mk1 | 【Dev570以前】基本パイプ1 + 赤石粉8 or 赤石チップ3 \|\| 【Dev571以降】基本パイプ1 + 鉄インゴット2 + 赤石粉3 or 赤石チップ1 |
| 物流制御パイプ Mk2 | Logistics Chassis Pipe Mk2 | 【Dev570以前】基本パイプ1 + 鉄インゴット8 or 鉄チップ3 \|\| 【Dev571以降】基本パイプ1 + 鉄インゴット2 + 鉄インゴット3 or 鉄チップ1 |
| 物流制御パイプ Mk3 | Logistics Chassis Pipe Mk3 | 【Dev570以前】基本パイプ1 + 金インゴット8 or 金チップ3 \|\| 【Dev571以降】基本パイプ1 + 鉄インゴット2 + 鉄インゴット6 or 鉄チップ2 |
| 物流制御パイプ Mk4 | Logistics Chassis Pipe Mk4 | 【Dev570以前】基本パイプ1 + ダイヤモンド8 or ダイヤチップ3 \|\| 【Dev571以降】基本パイプ1 + 鉄インゴット2 + （鉄インゴット3 + 金インゴット3） or （鉄チップ1 + 金チップ1） |
| 物流制御パイプ Mk5 | Logistics Chassis Pipe Mk5 | 【Dev570以前】基本パイプ1 + 金ブロック4 + ダイヤブロック4 \|\| 【Dev571以降】物流制御パイプ（Mk4）1 + ダイヤチップ1 |

### 3.6. 回収系（アイテムシンク系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 回収モジュール | ItemSink module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + 緑の染料2 + 鉄チップ1 \|\| 【Dev571以降】ブランクモジュール1 + 緑の染料2 + 鉄チップ1 or （鉄の歯車1 + 赤石粉2） |
| 読取回収モジュール | Polymorphic ItemSink module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + 橙の染料2 + 鉄チップ1 \|\| 【Dev571以降】ブランクモジュール1 + 橙の染料2 + 鉄チップ1 or （鉄の歯車1 + 赤石粉2） |
| 終端回収モジュール | Terminus ItemSink module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + イカスミ1 + 紫の染料1 + 鉄チップ1 \|\| 【Dev571以降】ブランクモジュール1 + イカスミ1 + 紫の染料1 + 鉄チップ1 or （鉄の歯車1 + 赤石粉2） |
| 消極補給モジュール | Passive Supplier module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + 赤の染料4 + 鉄チップ1 \|\| 【Dev571以降】ブランクモジュール1 + 赤の染料2 + 鉄チップ1 or （鉄の歯車1 + 赤石粉2） |
| MOD指定回収モジュール | Mod Based ItemSink module | 回収モジュール1 + 金チップ1 |
| エンチャント回収モジュール | Enchantment Sink module | 回収モジュール1 + エンチャントの本1 |
| エンチャント回収モジュールMk2 | Enchantment Sink module Mk2 | エンチャント回収モジュール1 + 金チップ1 or 金の歯車1 |
| クリエイティブタブ指定回収モジュール | Creative Tab Based ItemSink module | 鉱石辞書回収モジュール1 + 金チップ1 or 金の歯車1 |
| 鉱石辞書回収モジュール | OreDict ItemSink module | Mod指定回収モジュール1 + 本1 |

### 3.7. 供給系（プロバイダー系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 供給モジュール | Provider module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + ラピスラズリ2 + 金チップ1 \|\| 【Dev571以降】ブランクモジュール1 + ラピスラズリ2 + 金チップ1 or （金の歯車1 + 赤石粉2） |
| 供給モジュールMk2 | Provider module Mk2 | 供給モジュール1 + ダイヤチップ1 |

### 3.8. 抽出系（エクストラクター系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 抽出モジュール | Extractor module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + ラピスラズリ2 + 鉄チップ1 \|\| 【Dev571以降】ブランクモジュール1 + ラピスラズリ2 + 鉄チップ1 or （鉄の歯車1 + 赤石粉2） |
| 抽出モジュールMk2 | Extractor Mk2 module | 抽出モジュール1 + 金チップ1 |
| 抽出モジュールMk3 | Extractor Mk3 module | 抽出モジュール(Mk2)1 + ダイヤチップ1 |
| 抽出モジュール改 | Advanced Extractor module | 抽出モジュール1 + 赤石粉1 |
| 抽出モジュール改Mk2 | Advanced Extractor Mk2 module | 抽出モジュール改1 + 金チップ1 |
| 抽出モジュール改Mk3 | Advanced Extractor Mk3 module | 抽出モジュール改(Mk2)1 + ダイヤチップ1 |
| 高速分別モジュール | QuickSort module | 【Dev570以前】ブランクモジュール1 + 赤石粉3 + ラピスラズリ2 + ダイヤチップ1 \|\| 【Dev571以降】ブランクモジュール1 + ラピスラズリ2 + ダイヤチップ1 or （ダイヤの歯車1 + 赤石粉2） |

### 3.9. 補給系（サプライヤー系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 積極補給モジュール | Active Supplier module | 消極補給モジュール1 + 金チップ1 or (金の歯車1 + 赤石粉2) |

### 3.10. 加工系（クラフティング系）

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 加工モジュール | Crafting module | ブランクモジュール1 + 赤の染料1 + ラピスラズリ1 + 鉄チップ1 or (赤石粉2 + 鉄の歯車1) |
| 加工モジュールMk2 | Crafting module Mk2 | 加工モジュール1 + 金チップ1 or 金の歯車 |
| 加工モジュールMk3 | Crafting module Mk3 | 供給モジュール1 + ダイヤチップ1 |

### 4.1. Forestry For Minecraft 導入時のみ作成可能

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 蜂鑑定パイプ | Bee Analyzer Logistics Pipe | 赤石粉3 + プロポリス4 + 鉄チップ1 \|\| 基本パイプ1 / 液体蜂蜜1500mB / 640MJ |
| 蜂回収パイプ | BeeSink Logistics Pipe | 赤石粉4 + プロポリス4 \|\| 基本パイプ1 / 液体蜂蜜1500mB / 640MJ |
| 蜂鑑定モジュール | Bee Analyzer module | 赤石粉3 + プロポリス4 + 鉄チップ1 \|\| ブランクモジュール1 / 液体蜂蜜1500mB / 640MJ |
| 蜂回収モジュール | BeeSink module | 赤石粉4 + プロポリス4 \|\| ブランクモジュール1 / 液体蜂蜜1500mB / 640MJ |
| 蜂再補充モジュール | Apiary Refiller module | 赤石粉2 + プロポリス2 + 花粉1 + 木パイプ1 \|\| ブランクモジュール1 / 液体蜂蜜1500mB / 640MJ |
| Drone終端モジュール | Drone Terminus module | 赤石粉3 + イカスミ2 + 紫の染料2 + 花粉1 \|\| ブランクモジュール1 / 液体蜂蜜1500mB / 640MJ |

### 4.2. Industrial Craft 2 導入時のみ作成可能

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 充電管理モジュール | Electric Manage module | ブランクモジュール1 + 赤石粉3 + 電子回路1 + バッテリー1 + 金チップ1 |
| 充電緩衝モジュール | Electric Buffer module | ブランクモジュール1 + 赤石粉3 + 発展回路2 + 金チップ1 |

### 4.3. Thaumcraft 3 導入時のみ作成可能

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 相指定回収モジュール | Thaumic AspectSink module | ブランクモジュール1 + 赤石粉3 + 火の結晶片1 + 水の結晶片1 + 風の結晶片1 + 地の結晶片1 + 鉄チップ1 |

### 4.4. 要、周波数カード

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| コネクタパイプ | Logistics Inventory System Connector | 【Dev570以前】基本パイプ1 + エンダーパール2 \|\| 【Dev571以降】基本パイプ1 + エンダーパール1 + 赤石粉2 |
| 入口パイプ | Logistics System Entrance Pipe | 供給パイプ1 + 緑の染料1 |
| 出口パイプ | Logistics System Destination Pipe | 供給パイプ1 + 赤の染料1 |

### 4.5. その他

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| ファイアーウォールパイプ | Logistics Firewall Pipe | 【溶接作業台で作成】 【Dev570以前】基本パイプ1 + 鉄パイプ3 + ネザーレンガ4 + ダイヤチップ1 + (鉄インゴット1) \|\| 【Dev571以降】基本パイプ1 + ネザーレンガ4 + ダイヤチップ1 + (鉄インゴット1) |
| 不定経路輸送パイプ | Unrouted Transport Pipe | 板ガラス1 + 鉄インゴット2 + レッドストーン1<br>公式レシピ: (定型) ore:ingotIronx4 + ore:dustRedstonex2 + ore:blockGlassx2 -> logisticspipes:pipe_transport_basicx8 |

### 5. 溶接作業台

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 溶接作業台 | Soldering Station | 作業台1 + 赤石粉1 + 鉄インゴット6 |
| ナノ漏斗 | Nano Hopper | 【溶接作業台で作成】 【?以前】漏斗（BC）8 + (鉄インゴット1) \|\| 【?以降】ホッパー8 + (鉄インゴット1) |
| 周波数カード | Logistics Item Card | 【溶接作業台で作成】 【Dev570以前】ブランクモジュール1 + エンダーパール4 + (鉄インゴット1) \|\| 【Dev571以降】ブランクモジュール2 + エンダーパール1 + 鉄チップ2 + (鉄インゴット1) |
| 眼鏡のツル | Logistics HUD Bow | 【溶接作業台で作成】 ダイヤ3 + ダイヤチップ1 + (鉄インゴット1) |
| 眼鏡のレンズ | Logistics HUD Glass | 【溶接作業台で作成】 ガラス1 + ダイヤチップ1 + 光石粉7 + (鉄インゴット1) |
| 眼鏡のブリッジ | Logistics HUD Nose | 【溶接作業台で作成】 ダイヤ2 + ダイヤチップ1 + (鉄インゴット1) |
| ロジスティクス眼鏡 | Logistics HUD Glasses | 【溶接作業台で作成】 ブリッジ1 + レンズ2 + ツル2 + (鉄インゴット1) |
| 要求作業台 | Logistics Request Table | 【溶接作業台で作成】 作業台1 + ホッパー1 + チェスト1 + 加工パイプ（Mk2）1 + 要求パイプ（Mk2）1 + ダイヤチップ2 + 石レンガ2 + (鉄インゴット1)<br>公式レシピ: (定型) logisticspipes:chip_advanced + logisticspipes:pipe_request_mk2 + logisticspipes:frame + logisticspipes:pipe_crafting + ore:chestWood -> logisticspipes:pipe_request_table |

### 6. その他アイテム

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| パイプの初期化 | Reset Pipe | なし |
| モジュールの初期化 | Reset Module | なし |
| サインクリエイター | Crafting Sign Creator | 看板1 + 金チップ2 + ダイヤチップ1 |
| リモートオーダー | Remote Orderer | ガラス4 + ダイヤチップ2<br>公式レシピ: (定型) part:chip_advancedx2 + ore:ingotGold + ore:dustRedstonex3 + ore:blockGlass + ore:gemLapisx2 -> logisticspipes:remote_orderer |
| リモートオーダー | 色付き/全16色 | 染料1 + リモートオーダー1 |
| リモートオーダー | 再染色/全16色 | 染料1 + 色付きリモートオーダー |
| ネットワークモニター | Network monitor | 【0.8.0.17にて削除】金チップ1 + 金インゴット3 |
| アップグレーダー | Upgrade Manager | 【0.8.0.17にて削除】ネットワークモニター1 + BCレンチ1 |
| パイプコントローラー | Logistics Pipe Controller | 【0.7.4.Dev.251より実装】(金チップ1 or 金の歯車1) + 金インゴット3 |
| セキュリティステーション | Logistic Security Station | 基本パイプ1 + 赤石粉2 + 鉄インゴット5 + ダイヤチップ1 |

### 6.11. アップグレードチップ

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 速度加速チップ | Item Speed Upgrade | 鉄チップ1 + 金インゴット4 + 紙4 |

### 6.11.7. 搬入偽装チップ(西) (Sneaky Upgrade)

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 搬入偽装組み合わせチップ | Sneaky Combination Upgrade | 鉄チップ1 + 赤石粉4 + 紙4 |

### 6.11.14. 連結禁止チップ(西) (Disconnection Upgrade)

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 発展型加工補佐チップ | Advanced Satellite Upgrade | 金チップ1 + 鉄インゴット4 + 紙4 |
| 液体加工補佐チップ | Fluid Crafting Ugprade | 金チップ1 + 赤石粉4 + ガラス瓶4 |
| 加工副産物排出チップ | Crafting Byproduct Extracting Ugprade | 鉄チップ1 + 赤石粉4 + 木の歯車4 |
| 補給配置規則チップ | Placement Rules Ugprade | 鉄チップ1 + 赤石粉2 + 鉄インゴット3 + ホッパー1 + 赤色の染料1 + 黄色の染料1 |
| ファジーチップ | Fuzzy Ugprade | 金チップ1 + 紙4 + ネザー水晶4 |
| 補足解説 | 同ページ後半の個別解説より | - |

### 7.6. リモートオーダー

| 名称 | EN名 | レシピ/入手 |
|---|---|---|
| 色付きリモートオーダー | 全16色 | - |

(94 項目)
