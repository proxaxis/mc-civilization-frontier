# REPORTS

このファイルは、各タスクを担当したエージェントが作業結果をコンパクトに報告するための台帳です. 新しい報告は常に末尾へ追記し、過去の報告は改変しないこと.

## 記述ルール

* 1 報告 = 1 コミット. 報告の見出しにコミット ID(短縮 SHA)とタスク番号を付ける
* 差分だけを書く. 変更していない部分・自明な内容は省略する
* 「判明したこと」「未解決・要判断」を分けて書く. 次のエージェントが読んで状態を把握できる密度で
* Markdown 記述ルールは `/ws/AGENTS.md` に従う
* 書式:
  ```
  ## <short-sha> - <task番号> <タイトル>
  * 結果: 成功/一部成功/失敗/中断
  * 変更点: 何をどう変えたか(コンパクトに)
  * 判明したこと: 調査・検証で分かった事実
  * 未解決・要判断: 残課題、ユーザー判断が要る事項
  ```

## 報告ログ

## d477c6b - 初期: 全体構成の確定

* 結果: 成功
* 変更点:
  * `resources/mods/` に Minecraft `26.3` + Fabric 向け 78 jar を収集・配置(依存解決済み)
  * `docs/MOD_LIST.md` を実導入構成+除外・代替対応表に更新、`STRUCTURES.md`・`ENEMIES.md`・`GUIDE.md`・`README.md` を現状整合
  * `docs/STRUCTURES.md` は MOD カタログ形式(○/△/✕ の導入状況)に差し替え
  * `tasks/100.md` に実装タスク一覧、`101.md`〜`105.md`・`200.md` にエージェント向け自己完結プロンプトを作成
  * `AGENTS.md` に Markdown 記述ルールを定義
* 判明したこと:
  * `26.3` 非対応で除外: FTB Quests、YUNG's 全般、When Dungeons Arise 系、Aquamirae、IDAS、Dungeon Crawl、Masquerade、CTOV、Simply Swords
  * Better Nether は Incendium と生成系が競合するため不採用(対応版は存在)
  * 鉄道は Improvement Minecarts のみ対応、重火器・農業自動化・本格クエスト・黄昏/脅威イベントは既存 MOD なしで自作領域
  * The Graveyard 等の一部 jar は CurseForge から取得(Modrinth は旧版)
* 未解決・要判断:
  * 起動テスト未実施(task 101 の最初の作業)
  * ENEMIES.md の独自敵は実モブでないため、自作か既存割当の方針決定が必要

## 5afab43 - task 101 環境構築・起動検証

* 結果: 成功
* 変更点:
  * `docker-compose.yml` testserver に `${WORKSPACE_HOST_PATH:-.}/resources/mods:/data/mods:ro` を追加. 本環境の Docker daemon は別ホスト(実パス `/home/me/devserver/mc-civilization-frontier`)にあるため `WORKSPACE_HOST_PATH` 環境変数で上書きする形にした. 同様に `resources/datapacks/cfb_fixes` を `/data/world/datapacks/cfb_fixes:ro` にマウント
  * `MEMORY=6G` を追加(78 MOD でデフォルト 1G は不足)
  * `resources/datapacks/cfb_fixes/` 新設: `improvement_minecarts:riding` predicate を 26.3 形式(`minecraft:entity_type`)に上書きする修正 datapack. pack_format=121
  * コンテナ内 `/data/server.properties` の `pause-when-empty-seconds` を `-1` に変更(検証用、ボリューム側のみ)
* 判明したこと:
  * イメージの Java は 25.0.4.1 で `java>=25` 要件を満たす. タグ差し替え不要
  * 起動成功: 169 mod ロード、依存エラー・クラッシュ・mixin 起因の起動失敗なし、`Done` 到達. `file/cfb_fixes (world)` が有効化され 65 datapack 稼働
  * improvement_minecarts の `riding.json` が廃止済み `entity_sub_predicate_type: minecraft:type` を使い初回起動が失敗. datapack での上書きで解消(jar は無変更)
  * 構造物 locate 実績: ネザーで Incendium 5 種全て成功(forbidden_castle/sanctum/infernal_altar/piglin_village/nether_reactor、368〜2016 blocks)、オーバーワールドで nova_structures/graveyard/structory_towers/towns_and_towers/mvs 成功、エンドで nullscape/mes(=Moog's End Structures) 成功、`remnant_bosses:eclipsed_realm` 次元で hollow_citadel 成功
  * Lootr は実地検証済み: loot table 付き chest が `id: lootr:chest` + `LootrId` 付きで個別ルート化される. 設定は `config/lootr-common.jsonc` で変換有効・次元制限なし
  * Undead Nights 現在設定: `config/undeadnights.json5`(夜襲有効、猶予0日、距離70〜75、上限80体、夜襲中は睡眠不可、自動難易度進行ON)、`undeadnights_difficulty_config.json`(Level1: 5日周期・発生率100・追加波ありクールダウン45)、`undeadnights_horde_mobs_config.json`(波最大15、elite 3、demolition 6/TNT5)
  * 生成密度: repurposed_structures の mineshafts 系は spacing=1(バニラ式)、dungeons+ は spacing=8 でやや密だがパックの意図的構成と判断. 過密を示す設定ミスは見当たらず
  * 警告(起動は阻害しない): spell_engine と adpother の `player` メソッド mixin overwrite 競合(adpother 優先、spell_engine 側スキップ)、flan の未導入 MOD 連携データ(mekanism/create/AE2等)の parse エラー、Katters 翻訳キー欠落、初回のみ RPG Series loot 解決エラー(再起動で解消)
* 未解決・要判断:
  * ワールドは MOD 未搭載の初回起動で一度生成済みのため、スポーン周辺チャンクはバニラ生成. 全コンテンツをスポーン近辺で確認したい場合は world ボリュームの作り直しが必要(要ユーザー判断)
  * `WORKSPACE_HOST_PATH` は現環境固有. 別環境では unset(`.` 既定)で動くが、リモート daemon 環境では設定が必要

## task-102 - JOB_*.md 4職業ガイド作成

* 結果: 成功
* 変更点:
  * `docs/JOB_GUARDIAN.md`・`JOB_ASSASSIN.md`・`JOB_MAGE.md`・`JOB_PALADIN.md` を `JOB_GUIDE_TEMPLATE.md` 構成(特徴/パッシブ/アクティブ/装備品)で新規作成
  * 各職業にパッシブ3種・アクティブ3種・対応MOD武具(Paladins/Rogues/Wizards/Runes/Arsenal)を記述
* 判明したこと:
  * 性能値・クールダウン・消費量は全て (仮) 置き。タスク 103 のスキルツリー・スペル定義と整合させる前提
  * Guardian=Paladins パラディン系、Paladin(聖職者)=同 MOD のプリースト系として分担記述
* 未解決・要判断:
  * 数値確定は 103 の実装側との擦り合わせ待ち

## task-102(改訂) - JOB_*.md を新テンプレートで書き直し

* 結果: 成功
* 変更点:
  * 4職業を新テンプレート(パッシブ5種×5レベル、アクティブ15種、凡例アイコン式⬆️⬇️↔️🔄🍖⚡💎📖)で全面書き直し
  * パッシブはLv1〜5の効果+xpコスト、アクティブは凡例形式で効果・時間・CD・消費を統一記述
* 判明したこと:
  * 新テンプレートはスキル取得で経験値消費・死亡時スキル継続の記述を含む(スキルシステムの仕様に直結)
  * 凡例形式のため数値は装備品と同じ⬆️⬇️↔️🔄で統一記述可能
* 未解決・要判断:
  * xpコスト・効果量は依然(仮)。103 の実装時に最終調整が必要

## task-102修正 - マナ表記の実リソース置き換え

* 結果: 成功
* 変更点:
  * `JOB_MAGE.md` のパッシブ(深き魔力→隠し満腹度最大値、魔力循環→術満腹度コスト軽減)と魔力障壁(被ダメージ→隠し満腹度相殺)、装備品(マナ効率→術コスト効率)を実在リソース表現へ修正
* 判明したこと:
  * 本パックにマナプールは存在しない。コスト体系は 🍖満腹度・⚡隠し満腹度・💎ルーン で構成
* 未解決・要判断: なし

## 7fd02c4 - 106 ワールド生成系コンテンツの先行実装(鉱石・油田)

* 結果: 成功
* 変更点:
  * `/ws/app` に Fabric Mod `bcn` 0.1.0 を新規実装(gradle 9.8.2 + loom 1.19.3 + JDK25, mojmap 直結で `mappings` 行不要)
  * 鉱石ブロック 17 種: 銅・錫・銀・ウラニウム・ルビー・サファイア・ペリドット・エレクトロタイン(=ニコライト)の石/深層バリアント+大理石. RedStoneOreBlock で電気石の lit を再現
  * アイテム: raw 3 種・インゴット 3 種・宝石 3 種・electrotine_dust・oil_bucket・各ブロックアイテム. 精錬レシピ・採掘レベル/`c:` コンベンションタグ付き
  * オイル流体(oil/flowing_oil)+オイルブロック+油源泉ブロック. 無限水源なし・水流より遅く浅く(3 マス)・エンダーマンとブレイズにダメージ・強い沈降力・炎/溶岩隣接で着火・`WATER_EVAPORATES` 次元で設置 1 秒後に爆発
  * worldgen: `worldgen/feature/`+`placed_feature/` JSON(26.3 の新形式, `config` なし)+`BiomeModifications.addFeature` で `#c:is_desert`/`#c:is_ocean`/`foundInOverworld` へ注入. 油田は独自 `bcn:oil_field` Feature(codec を FEATURE_TYPE へ登録)
  * loot_table は 26.3 形式(`condition`/`modifier`/`tool/can_silk_touch`). ウラニウムは幸運ボーナスなしの raw_uranium 1 個, ニコライトは 3-5 個+fortune, 宝石は 1-2 個+fortune
  * `improvement_minecarts:riding` predicate が旧形式で新規ワールド作成をクラッシュさせていたため、jar 内の該当 JSON を `minecraft:vehicle`+`minecraft:entity_type` 形式に修正(`cfb_fixes` には既に修正版あり)
  * 生成 jar を `resources/mods/bcn-0.1.0.jar` に配置
* 判明したこと:
  * 26.3 は非難読化(mojmap 実行時名, intermediary/yarn なし). worldgen は `worldgen/feature` へ移動し `Feature` が config 統合インタフェース化, loot は `condition`/`modifier` 単数キー化, `ResourceLocation` は `Identifier`, `EnderMan` は `Enderman`, `PushReaction.DESTROY` は `POPPED`, `ultraWarm()` は `environmentAttributes().getValue(WATER_EVAPORATES,pos)` へ移行. region のパレットキーは `Name` ではなく `''`
  * 既存 JSON 準拠で生成: tin count8 trapz -24..56 size8, silver count6 -64..32 size9, 宝石各 count1 -80..80 size8-10, electrotine count4 -32..32 size8, marble count1 uniform -64..64 size64, 銅は spec より count8 trapz 0..64, ウラニウム count3 above_bottom 0..127 size3
  * 検証はローカル Fabric サーバ(fabric-api+bcn のみ)で実施: `/locate`+`/forceload` でチャンク生成→リージョン NBT スキャンで全鉱石が想定高度にあることを確認(例: 錫はセクション -1..3, ウラニウム -1..3+深層). 油田は砂漠/海洋の地表セクション 4-6 にプール+噴出柱+地下ポケット, 油源泉もポケット底に生成確認(6 箇所中 2 箇所で spring_chance 0.5 と整合)
  * `minecraft:lake` は fluid を任意 BlockStateProvider で指定可能で油溜まりに流用可. `minecraft:ore` で液体ブロックを鉱脈化可能(地下オイルポケット)
  * testserver 起動テストで roguelike-dungeons の CisternRoom 生成が 60 秒を超え Watchdog クラッシュした(既存 mod 側の問題, 本件とは無関係だが大量生成時に再現しやすい)
  * `cfb_fixes` 自体は新形式に修正済みだったが、クラッシュしていたのは mod 同梱ファイルの方だった
* 未解決・要判断:
  * イリジウムは仕様どおり未実装(クラフト限定の注記のみ)
  * 油田のレア度は仮値(池 1/80, 砂漠田 1/200, 海洋田 1/250). 実機プレイで要バランス調整
  * オイルの着火は簡易実装(源→油流+直上着火), 「油流が燃え続ける」挙動や溶岩→黒曜石化は未実装. クライアント側の流体描画属性(FluidVariantRenderHandler 相当)もサーバ検証範囲外のため未確認
  * testserver のワールドはクラッシュ後に再生成済みだが、roguelike-dungeons の重生成は task 5.4 側で要対応. `/ws/resources/mods` の improvement_minecarts jar はローカル修正済み(上流バグ, 後続タスクでも要認識)
  * クリエイティブタブへの登録・`oil` のクライアント描画確認は task 200 の範囲として残した

## task-102追補 - スキルツリー設計図 SKILL_TREE.md 作成

* 結果: 成功
* 変更点:
  * `docs/SKILL_TREE.md` を新規作成。4職業×(パッシブ5×5Lv=25 + アクティブアンロック15 + ゲート)の40-45ノード構成を設計
  * Tier1-4の階層構造(基礎/中堅/熟練/奥義)、各アクティブの前提パッシブレベルを全職業で定義
* 判明したこと:
  * 1職業コンプは仮コストで約12,500xp(レベル100-120相当)。Tier3/4ゲートは討伐実績・クエストを条件想定
* 未解決・要判断:
  * 数値は103実装時にJOB_*.mdと照合して最終調整が必要

## task-103(着手) - スキルツリー datapack 生成

* 結果: 一部完了(継続中)
* 変更点:
  * `tools/gen_skills.py` を新設し、`resources/datapacks/cfb_skills/` に4職業×40ノード(パッシブ5×5Lv+アクティブ15)の category.json を生成。合計160ノード
  * 各職業 = `cfb:{job}` カテゴリ、パッシブは attribute reward(multiply_total)、アクティブは親パッシブLv前提のアンロックノード
* 未解決・要判断:
  * puffish_skills の JSON スキーマ(category.json の形式・keys)は実機での検証待ち(task101環境で読み込み確認が必要)
  * アイコンを全て book 仮置き。職業・スキル別に要差し替え
  * 残作業: Spell Engine スペル定義、空腹度コストフック、職業選択UI/排他ロック

## task-103検証 - cfb_skills datapack 実機検証

* 結果: 成功
* 変更点:
  * `tools/gen_skills.py` を公式スキーマ準拠に修正: `categories/<job>/` 配下へ `category.json`/`definitions.json`/`skills.json`/`connections.json`/`experience.json` の5分割、`config.json`(version 3、categories一覧)を追加
  * skills は `{"definition": <ローカルid>, "x": int, "y": int, "root": bool}`、connections は `[["idA","idB"],...]` 配列、definitions は `rewards` 複数形、experience は `expression` + `puffish_skills:` 名前空間付き source
  * testserver へ datapack を配置し `/reload` で `Data pack cfb loaded successfully` を確認
  * 副次修正: `bcn-0.1.0.jar` の zip 構造不良(`invalid LOC header` でレシピ読込失敗)を標準 deflate で再パックして解消
* 判明したこと:
  * puffish_skills 0.19.2 のカテゴリ構造は5ファイル分割式(統合json不可)、definition 参照はローカルid、`cost` は skill に存在しない(警告のみ)
  * 有効な xp source type は `puffish_skills:kill_entity` 等の名前空間付き。source は `variables`(operations 連鎖)+`experience`(式)+`anti_farming` 構成
  * docker cp の再配置は既存 /tmp コピーを消してから行わないと入れ子化する
  * リロード失敗時は他MOD(flan等)のエラーも混在するため `cfb:` プレフィクスで絞って確認すると判別しやすい
* 未解決・要判断:
  * ツリー内のスキルアンロック条件(経験値→ポイント交換)は experience.json の式で仮設定。バランスは要実プレイ調整
  * アクティブスキルはアンロックノードのみ。実際の発動アイテム・効果は Spell Engine 定義と連携する残作業あり
  * アイコンは book/paper の仮置き

## task-103(続) - Spell Engine スペル定義と実機検証

* 結果: 成功
* 変更点:
  * `tools/gen_spells.py` を新設し `data/cfb/spell/` に4職業x15=60スペル JSON を生成(school/tier/cast/cooldown/exhaust/rune/impact)
  * cost は `exhaust`(満腹度消費) + `cooldown.duration` + 必要に応じ `item`(runes:*_stone) で実リソース表現
  * impact は DAMAGE(spell_power_coefficient)/HEAL/STATUS_EFFECT/なし の4型で近似実装
  * testserver へ配置し `/reload` で `Data pack cfb loaded successfully`・パースエラー 0 を確認
* 判明したこと:
  * Spell Engine の spell は `data/<ns>/spell/<id>.json` に配置。`school` は `spell_power:arcane/fire/frost/healing` 等
  * cost の `exhaust` フィールドが満腹度(空腹)コストとして機能するため、別途自作フックなしで実現可能と判明
  * `cooldown.group` は武器別ではなく duration 直指定も可
  * スペル側の learn/active/impact は詳細だが、本実装は骨格(DAMAGE/HEAL/STATUS_EFFECT)で近似。SUMMON/PROJECTILE 等の高度挙動は未実装
* 未解決・要判断:
  * スペルと職業ツリーの連携(ツリーアンロック→実スペル発動)は puffish unlock と spell_engine 側のアイテム/スペルプール結合の設計が必要
  * 職業選択の排他ロック(他職業カテゴリ不可)は別途実装が必要
  * 奥義・召喚・瞬間移動など一部スペルは impact 骨格のみで実挙動は未実装

## task-103(続2) - 職業選択・排他ロック datapack 実装

* 結果: 一部完了(実機プレイヤー検証は未実施)
* 変更点:
  * `data/cfb/advancement/on_join.json`: 初回参加時に `cfb:job/menu` を実行する進捗
  * `data/cfb/function/job/menu.mcfunction`: tellraw クリック式の4職業選択メニュー
  * `data/cfb/function/job/{guardian,assassin,mage,paladin}.mcfunction`: 選択職業を unlock、他3職業を lock + 確認メッセージ
  * `/reload` で `Data pack cfb loaded successfully` を再確認
* 判明したこと:
  * `puffish_skills category lock/unlock` コマンドが存在し datapack function から実行可能な設計にできる
  * `minecraft:tick` トリガーの進捗 + rewards.function で初回参加時の自動メニューが実装できる
  * rcon 経由ではプレイヤー引数コマンドの検証ができない(lock/unlock の実挙動はプレイヤーログイン時要確認)
* 未解決・要判断:
  * `category lock <players> <category>` の引数順は仮定(@s 前提)。プレイヤー参加時に要検証
  * スキルツリーアンロックとスペル発動の実連携(puffish unlock→spell 使用可否)は別設計が必要
  * on_join メニューは初回のみ。再選択手段(リセット)の設計が必要

## task-103検証補足 - ボット検証環境の構築とプロトコル制約

* 結果: 一部成功(bot 接続は未達)
* 変更点:
  * `docker-compose.yml` の testserver を `ONLINE_MODE=FALSE` に変更(検証用)
  * `resources/datapacks/cfb_skills` もコンテナへマウント追加(cfb_fixes と同様)
  * mineflayer ベースの検証 bot(HTTP API 付き)を /tmp/mcmcp に作成し接続を試行
* 判明したこと:
  * docker のポート公開はこの環境の netns からは未解決で、bot はコンテナ IP `172.18.0.2` 直接接続が必要だった
  * minecraft-data / minecraft-protocol が対応するのは `26.1` までで、サーバーの 26.3 要求に合わず接続時に `incompatible 26.3` でキックされた
  * Modrinth に 26.3 対応の Fabric 用 MCP 操作 mod は見当たらない(検索範囲では未確認)
  * rcon の `puffish_skills category lock/unlock` は `<players> <category>` 引数が必要で、サーバーコンソール単独では実行対象を得られないため live 検証はプレイヤー必須
* 未解決・要判断:
  * bot/クライアント経由の実機検証は minecraft-data の 26.3 対応 or Fabric 側 MCP mod 待ち
  * 現時点で可能なのは datapack ロード検証まで(実機動作は未確認)
