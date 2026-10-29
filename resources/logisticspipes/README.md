# LogisticsPipes アセット参照メモ

LogisticsPipes の移植を検討するための参考資料として、公式リポジトリから取得したアセットの出典と構成、注意点をまとめたメモ.

## 1. 出典

- リポジトリ: https://github.com/RS485/LogisticsPipes.git
- ブランチ: デフォルトブランチ (shallow clone で取得)
- 取得元パス: `resources/assets/` と `resources_raw/`
- ライセンス: `LICENSE.md` を同梱. 個人使用の参照目的で取得したため、配布物への同梱はライセンス要確認

## 2. ディレクトリ構成

- `assets/logisticspipes/blockstates/` — ブロックステート定義 JSON
- `assets/logisticspipes/models/` — モデル定義 JSON
- `assets/logisticspipes/textures/` — パイプ、モジュール、アップグレード、HUD眼鏡、GUI 等の PNG
- `assets/logisticspipes/recipes/` — レシピ JSON
- `assets/logisticspipes/lang/` — 言語ファイル
- `assets/logisticspipes/book/`、`fonts/` — ガイドブック、フォント関連
- `resources_raw/` — 生成前の元素材 (solid_block 等のベース素材)

## 3. 注意点 (知見)

- LogisticsPipes は BuildCraft 前提のアドオンで、基本パイプとモジュール (ソフトウェア) を差し込む物流制御パイプ (ハードウェア) という構成になっている. テクスチャもパイプ本体とモジュール装着面が分かれているので、ポート時は重ね合わせの構造を意識する
- LP の電力単位は LP で、MJ/RF/EU から LP変換機で変換して使う (IC2 との連携点)
- アイテム一覧は同ディレクトリの `ITEMS.md` (Minecraft Japan Wiki から抽出) を参照. Dev570/Dev571 のレシピ差分も記載済み
- `resources_raw/` は最終成果物ではないが、パイプやブロックの見た目を作る際のベース素材として参照できる

## 4. 関連 MOD

- BuildCraft: `../buildcraft/` に ITEMS.md、README.md、公式リポジトリ (8.0.x-1.12.2 ブランチ) のアセットを収録済み
- IndustrialCraft²: `../industrialcraft/` に ITEMS.md と README.md (用語集、ソースコードの所在) を収録済み
