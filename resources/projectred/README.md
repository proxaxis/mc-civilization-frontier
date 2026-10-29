# Project:Red アセット参照メモ

ProjectRed の移植を検討するための参考資料として、公式リポジトリから取得したアセットと wiki 解説の出典をまとめたメモ.

## 1. 出典

- リポジトリ: https://github.com/MrTJP/ProjectRed.git
- ブランチ: デフォルトブランチ (NeoForge、MC 1.21.1 系)
- 取得元パス: 各モジュールの `src/main/resources/assets/` と `src/main/generated/assets/`、`src/main/(generated|resources)/data/`
- ライセンス: `LICENSE.md` を同梱. 個人使用の参照目的で取得したため、配布物への同梱はライセンス要確認
- wiki 解説: Minecraft Japan Wiki の "MOD解説/Project:Red" (Base、Integration、Mechanical、World、他モジュール)

## 2. 収録ドキュメント

- `ITEMS.md`: wiki の5サブページから抽出した追加ブロック、アイテム一覧 (178件)
  - Base: 中間素材 (ウェハー、基板、合金、コイル等)、ForgeMultiPart (ノコ、分割ブロック)
  - Integration: 配線 (赤合金ワイヤー、絶縁、ケーブル束、ジャケット系)、集積回路 (論理ゲート)、配列素子
  - Mechanical: ツール、ブロック操作、アイテム輸送、モーター、青電力 (Blulectricity) 関係装置
  - World: 追加ブロック (玄武岩系等)、一次生産品、一次加工品、ツール類
  - 他モジュール: Lighting (照明)、Exploration (探索)

## 3. アセット構成

`assets/` 以下は名前空間 (モジュール) 単位で分割.

| 名前空間 | 内容物 |
|---|---|
| `projectred_core` | コア素材 (ウェハー、基板等) |
| `projectred_integration` | 論理ゲート、回路 (テクスチャ量最大) |
| `projectred_transmission` | ワイヤー、フレーム配線 |
| `projectred_illumination` | 照明ブロック |
| `projectred_exploration` | 世界生成コンテンツ |
| `projectred_expansion` | ピストン、フレーム系 |
| `projectred_fabrication` | 自動化関連 |

- `assets/` = 各モジュールの `resources/assets` + datagen 生成の `generated/assets` をマージ (モデル、blockstates 等は generated 側)
- `data/` = 同様に `data` ディレクトリをマージ (recipes、tags、loot tables 等)
- datagen 生成物は上流では `./gradlew :<module>:runData` で再生成されるもの. 上流では `generated/` を直接編集しない運用だが、ここでは参照用のため問題ない

## 4. 関連 MOD

- BuildCraft: `../buildcraft/` に ITEMS.md、README.md、公式リポジトリ (8.0.x-1.12.2) のアセット
- IndustrialCraft²: `../industrialcraft/` に ITEMS.md と README.md (用語集、ソースコードの所在)
- LogisticsPipes: `../logisticspipes/` に ITEMS.md と GitHub リポジトリのアセット
