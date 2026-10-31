# BuildCraft アセット参照メモ

BuildCraft の Fabric 移植を検討するための参考資料として、公式リポジトリから取得したアセットの出典、構成、注意点をまとめたメモ.

## 1. 出典

- リポジトリ: https://github.com/BuildCraft/BuildCraft.git
- ブランチ: `8.0.x-1.12.2` (BuildCraft 7.99.x 系、Minecraft 1.12.2 対応の最終安定世代)
- 取得時コミット: `7c86626d09c4569fb2e0f458a16aaf8c3148ebaf`
- 取得元パス: `buildcraft_resources/` 以下の `assets/` と `data/`、さらに `misc/`、`changelog/`、`versions.txt`、`mcmod.info`
- 取得方法: `git clone --depth 1 --filter=blob:none --sparse` で sparse clone して対象パスをコピー
- ライセンス: `LICENSE.BUILDCRAFT` (MMPL 1.0.1) を同梱. 詳細は後述

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

トップレベルのその他のファイル:

- `misc/unused_textures/` — 上流で未使用の没テクスチャ 12 枚 (ギア、ポンプ上面、フィルター、GUI 等の代替デザイン)
- `misc/buildcraft.qm` — 上流のクエリモデルファイル
- `changelog/` — バージョン別チェンジログ
- `versions.txt` — MC バージョンと BC バージョンの対応表
- `mcmod.info` — mod メタデータ
- `ITEMS.md` — Minecraft Japan Wiki から抽出したアイテム一覧
- `LICENSE.BUILDCRAFT` — MMPL 1.0.1 ライセンス本文

収録規模: PNG 997 (assets 985 + unused 12)、JSON 414、mcmeta 24、OBJ+MTL 1組、lang 1件、計 1,602 ファイル.

## 4. 注意点 (知見)

- BC8 系はパイプ本体のテクスチャをコード側で動的生成しているため、パイプの PNG は少なめ. 移植時は `buildcrafttransport` のモデル定義と生成側コードの参照が必要
- ロボットステーションは `buildcraftrobotics/models/pluggables/robot_station_base.obj` + `.mtl` の OBJ モデルになっている
- ゲートのテクスチャは `buildcraftsilicon/textures/gates/` にあり、素材別 (`gate_material_*`)、AND/OR 別、点灯状態別 (`_lit` / `_dark`) に分割されている
- アニメーションは `.png.mcmeta` で定義されている (例: `gate_expansion_fader.png.mcmeta`)
- レシピ JSON は `recipes/` 以下にあるが、MOD バージョンによりレシピが異なる点に注意. wiki 記載の Dev570/Dev571 や BC6/BC7 の差分は `ITEMS.md` のレシピ欄と照合するのに使える
- アイテム名、説明、旧バージョンのレシピは同ディレクトリの `ITEMS.md` (Minecraft Japan Wiki から抽出した一覧) を参照

## 5. ライセンス知見

- `buildcraft_resources/` (このディレクトリのアセットの出所) は MMPL 1.0.1 (Minecraft Mod Public License) 配下. `LICENSE.BUILDCRAFT` に本文あり
- MMPL は複製物または改変物の再配布を許可するが、その部分は MMPL のまま配布する必要があり、バイナリ配布時はソース一式を無償で入手できる手段の提供が必須
- BCN に PNG をそのまま取り込んで配布する場合、取り込んだアセット部分は MMPL 継承になる. 配布しない私的利用や、絵柄の参考だけに留める場合は問題にならない
- 上流コード側の新しいライセンス (`LICENSE-NEW`) は MPL 2.0. コードを参照する場合はそちらが適用される
- リポジトリにはこの他、旧世代向けの `common_old_license`、`src_old_license` も存在する

## 6. テクスチャ分類の早見表 (PNG 997 枚の内訳)

| カテゴリ | 概数 | 代表パス |
|---|---|---|
| パイプ | 262 | `buildcrafttransport/textures/pipes/` (アイテム、液体、伝導 × 材質別) |
| 建築機械 | 178 | `buildcraftbuilders/textures/blocks/` (フィラー、ビルダー、製図机、製図棚) |
| トリガーアイコン | 158 | `*/textures/triggers/` (エンジン熱量、液体量、レッドストーン等) |
| ロボット | 90 | `buildcraftrobotics/textures/entities/` |
| エンジン | 31 | `buildcraftenergy|core|lib/textures/blocks/engine/` (材質別 side/back + chamber/trunk 部品) |
| クァーリー | 15 | `buildcraftbuilders/textures/blocks/quarry/` (normal/connected + drill, laser) |
| 採掘、ポンプ、タンク | ~30 | `buildcraftfactory/textures/blocks/` (mining_well, pump, tank, flood_gate, distiller, refinery) |
| 歯車、工具 | ~25 | `buildcraftcore/textures/items/` (gear 5 素材、wrench、paintbrush) |
| ゲート、チップ | ~40 | `buildcraftsilicon/textures/gates/`、`items/redstone_chipset/` |
| 流体 | 12 | `buildcraftenergy/textures/blocks/fluids/` (oil, fuel, redplasma + heat 段階) |
| 没テクスチャ | 12 | `misc/unused_textures/` |
| その他 | ~150 | GUI 背景、マーカー、レーザー、レンズ、ガイドブック等 |

## 7. 絵柄上の注意

- 1.12.2 時代の 16px 旧式テクスチャなので、26.3 現行バニラとは絵柄のテイストが異なる. そのまま使うとクラシック BC の見た目になる
- エンジンやクァーリーは面別 (side/back/top/front) に分割されているため、現行モデル形式への移植時は面マッピングの再構成が必要
- トリガーアイコンはゲート GUI 用の小アイコンなので、アイテムアイコンとしてはそのまま使いにくいものが多い
