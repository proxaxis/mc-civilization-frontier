# BuildCraft アセット参照メモ

BuildCraft の Fabric 移植を検討するための参考資料として、公式リポジトリから取得したアセットの出典、構成、注意点をまとめたメモ.

## 1. 出典

- リポジトリ: https://github.com/BuildCraft/BuildCraft.git
- ブランチ: `8.0.x-1.12.2` (BuildCraft 7.99.x 系、Minecraft 1.12.2 対応の最終安定世代)
- 取得元パス: `buildcraft_resources/` 以下の `assets/` と `data/`
- 取得方法: `git clone --depth 1 --branch 8.0.x-1.12.2 --single-branch` で shallow clone して `assets/` と `data/` をコピー
- ライセンス: `LICENSE.BUILDCRAFT` を同梱. 個人使用の参照目的で取得したため、配布物への同梱はライセンス要確認

## 2. このブランチを選んだ理由

- 解説 wiki (Minecraft Japan Wiki の BuildCraft ページ) の記載内容、つまりクァーリー、ゲート、ロボット、外装パネル、製図系などが最も揃っている世代であり、テクスチャが揃う最終世代でもある
- `8.0.x` 系の最新ブランチ (`8.0.x-1.16.5`、`8.0.x-1.18.2`、`8.0.x-1.20.1` など) は実装の再設計が進行中で、コンテンツが削減されている
- その他の候補ブランチ:
  - `7.1.x`: MC 1.7.10 世代. wiki 記事の記述 (BC6系の記述を含む) に近いが、アセット形式が旧式
  - `7.2.x`: MC 1.8.9 世代
  - `8.0.x-1.16.5` 以降: 比較的新しいが収録コンテンツが少ない
  - `master`: デフォルトブランチ

## 3. ディレクトリ構成

`assets/` 以下はモジュール単位で分割されている.

| モジュール | 主な内容物 |
|---|---|
| `buildcraftcore` | エンジン、歯車、レンチ、マーカー、漏斗等の基幹要素 |
| `buildcrafttransport` | アイテム、液体、伝導の各パイプとパイプワイヤー |
| `buildcraftfactory` | ポンプ、リファイナリー、ボーリングマシン、液体タンク、水門等 |
| `buildcraftbuilders` | クァーリー、フィラー、ビルダー、製図机、製図棚、テンプレート等 |
| `buildcraftsilicon` | 精密作業台、レーザー、ゲート、チップセット、レンズ等 |
| `buildcraftrobotics` | ロボット、ドック、範囲指定機、アイテム要求機等 |
| `buildcraftenergy` | オイル、燃料、プラズマ関連 |
| `buildcraftlib` | 共通ライブラリ |
| `buildcraft` | 言語ファイル (`en_US.lang`) とロゴのみ |

各モジュール内のサブディレクトリ:

- `textures/` — ブロック、アイテム、GUI、トリガーアイコン等の PNG
- `models/` — block/item/pluggables のモデル定義 JSON と一部 OBJ/MTL
- `blockstates/` — ブロックステート定義 JSON
- `recipes/`、`advancements/` — レシピと進捗の JSON
- `gui/` — GUI 定義関連

収録規模: PNG 985、JSON 414、mcmeta 24、OBJ+MTL 1組、lang 1件、計 1,427 ファイル.

## 4. 注意点 (知見)

- BC8 系はパイプ本体のテクスチャをコード側で動的生成しているため、パイプの PNG は少なめ. 移植時は `buildcrafttransport` のモデル定義と生成側コードの参照が必要
- ロボットステーションは `buildcraftrobotics/models/pluggables/robot_station_base.obj` + `.mtl` の OBJ モデルになっている
- ゲートのテクスチャは `buildcraftsilicon/textures/gates/` にあり、素材別 (`gate_material_*`)、AND/OR 別、点灯状態別 (`_lit` / `_dark`) に分割されている
- アニメーションは `.png.mcmeta` で定義されている (例: `gate_expansion_fader.png.mcmeta`)
- レシピ JSON は `recipes/` 以下にあるが、MOD バージョンによりレシピが異なる点に注意. wiki 記載の Dev570/Dev571 や BC6/BC7 の差分は `ITEMS.md` のレシピ欄と照合するのに使える
- アイテム名、説明、旧バージョンのレシピは同ディレクトリの `ITEMS.md` (Minecraft Japan Wiki から抽出した一覧) を参照
- `buildcraft_resources/versions.txt` や `mcmod.info` はコピーしていない. 必要なら上流リポジトリを参照
