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

## task-103検証3 - Carpet 偽プレイヤーによる実機検証

* 結果: 成功
* 変更点:
  * `fabric-carpet-26.3+v260915.jar` を `resources/mods/` に追加(検証用)
  * `/player verifier spawn` で偽プレイヤー参加、`execute as verifier run function cfb:job/guardian` を実行
  * `execute as verifier run puffish_skills category lock/unlock @s cfb:<job>` が実効することを確認(Locked/Unlocked の応答あり)
* 判明したこと:
  * mineflayer は 26.3 プロトコル非対応(最大 26.1)で接続不可→代替として Carpet の `/player` 偽プレイヤーが使えた
  * job 選択 function の lock/unlock は `@s` コンテキストで正しく動作する設計で問題ない
  * ONLINE_MODE=FALSE でローカル偽プレイヤー参加を検証できる
* 未解決・要判断:
  * スペルとアイテムの結合(spell pool/weapon_attributes)は未実装で、実際の発動検証はまだ
  * on_join メニューの実表示はクライアント確認が必要
  * carpet は検証目的で追加。本番構成から外すかは要判断

## task-103検証4 - スペル↔アイテム結合と実発動試行

* 結果: 一部完了(発動は未達)
* 変更点:
  * `data/wizards/tags/spell/weapon/staff_arcane.json` を cfb datapack に追加し、wizards:staff_arcane に mage スペル4種(firebolt/inferno/shockbolt/blink)を結合
  * carpet 偽プレイヤーに staff_arcane を持たせ `player verifier use` で発動を試行
* 判明したこと:
  * スペルの武器結合は `data/<ns>/tags/spell/weapon/<item_id>.json` のタグ形式(item id とタグ名を一致させる)
  * `weapon_attributes` は戦闘属性で、スペル結合には関係ない
  * 偽プレイヤーの `use` ではスペル発動を確認できず(foodLevel 20 のまま)。spell_engine の発動経路はアイテムの spell_container コンポーネント or 正しい操作タイミングが必要な可能性
  * `execute as` 経由の `cast` 系コマンドは存在しない(spell_engine に直接発動コマンドは無い模様)
* 未解決・要判断:
  * スペル実発動は spell_container を持つアイテム定義(weapon_attributes or 独自アイテム)または実クライアントでの操作が必要。偽プレイヤーでの発動検証は未達
  * cfb スペルの構造はロード済み。発動経路の結線は残課題

## task-103検証5 - 職業選択メニューのクリック不発を trigger 方式へ修正

* 結果: 修正済み(ユーザーリトライ待ち)
* 変更点:
  * メニューの clickEvent を `/function` 直接実行から `/trigger cfb_job set 1..4` へ変更(非 op でも実行可)
  * `cfb:tick`(minecraft:tick タグ)でスコアを監視し、該当 job function をサーバー権限で実行後リセット+再有効化
  * `cfb:load`(minecraft:load タグ)で `cfb_job` trigger objective を作成・全員有効化
  * `op YSK357` を付与
* 判明したこと:
  * tellraw の run_command はプレイヤー自身の権限で実行されるため、非 op では /function が動かない。trigger スコア経由が正攻法
  * tick function 内のコマンドは呼び出し元(サーバー)権限で実行されるため、puffish の lock/unlock が非 op でも機能する
* 未解決・要判断:
  * 実クリックでの動作はユーザーのリトライ確認待ち

## task-103検証6 - cfbスペル実発動の調査とjar同梱への移行

* 結果: 原因特定・対策適用(最終確認はクライアント jar 更新待ち)
* 変更点:
  * cfb スペル全60種に `target` 追加(damage=AIM, heal=AIM+fallback, effect=AREA include_caster) — 未定義で発動不可だった
  * `group` を実在グループ名に変更(arcane_sorcery等)、cooldown を既存形式に合わせる
  * スペル・タグ・puffish categories・functions を `bcn-0.1.0.jar` に同梱(data/cfb 他95エントリ)
  * 暫定リソースパック cfb_assets.zip に60種アイコン+lang(name/description)を生成
* 判明したこと:
  * 複数スペル運用は「呪文書(spell_book)+トリンケットスロット+Binding Table」が正しい形。杖は1スペル自己完結で拡張不可
  * Binding Table: バニラ本+ラピス→呪文書タイプ選択→レベル消費でスペル購入。巻物で追加スペルをバインド
  * スペルホットバーのスロットキー(L/U等)=そのまま発動キー
  * datapack のスペル定義はサーバー側のみではクライアントに届かずキャスト不可 → jar同梱が必須(cfb:test_arcane=arcane_missile完全コピーでも打てず、namespace/配布形態が原因と確定)
* 未解決・要判断:
  * クライアント jar 更新後の発動確認待ち。成功なら datapack の spell/関連を jar 側に寄せる運用に確定か

## task-103検証7 - cfbスペル実発動成功・assets jar同梱

* 結果: 成功
* 変更点:
  * クライアント側 bcn jar 更新で cfb スペルの実発動を確認(ホットバースロットキーで詠唱→発射)
  * `assets/cfb/`(スペルアイコン60+lang ja/en)を bcn jar に同梱、リソースパック配布を不要化
  * 検証用 `cfb:test_arcane` スペル削除
* 判明したこと:
  * datapack同梱コンテンツはクライアントへ届かないため、クライアント側で解決が必要な定義(スペル・スキルカテゴリ・タグ・アセット)は jar 同梱が必須 — 実機で証明済み
  * cfb_skills datapack と jar 同梱内容は現状二重登録だが動作に支障なし(要整理)
* 未解決・要判断:
  * exhaust の満腹度実消費・cooldown HUD・ルーン消費の定量検証(サバイバルで要実測)
  * 他59スペルは骨格定義のまま(firebolt は arcane_blast 複写) → 実装内容を JOB ガイド値に合わせる本実装が残る
  * cfb_skills datapack と jar の二重登録の整理(dataload 先をどちらに寄せるか)

## task-103検証8 - コスト実測(ルーン・CD・exhaust)

* 結果: 成功
* 判明したこと:
  * rune(item cost)は消費される(fire_stone 31→30 実測)
  * cooldown は実機で8秒制約を確認
  * exhaust は foodExhaustionLevel の蓄積を確認(値が小さいので目視減少は緩やか)
  * スロットキー=発動キー、右クリックは選択操作という操作系が確定
* 未解決・要判断:
  * 満腹度コストをもっと体感できる値(例:1発=半分等)にするかはバランス要相談
  * 残り59スペルの本実装(骨格→JOBガイド値)が次フェーズ

## task-103実装1 - 60スペル本実装(骨格→多様action)

* 結果: 実装・デプロイ(実機確認待ち)
* 変更点:
  * gen_spells.py を複合impact+多様action(TELEPORT/VELOCITY/AGGRO/FIRE/HEAL/STATUS_EFFECT)対応に全面書換
  * 全60スペルを職業コンセプトに合わせて本実装(例: blink=TELEPORT FORWARD, shadow_strike=背後TP+斬撃, roar/taunt=AGGRO, fire系=着弾+FIRE, 各種範囲buff/heal=AREA)
  * target 種別を整理(aim/aim_fb/aim_req/area/caster)、アニメーションを投射/範囲で出し分け
  * bcn jar に再同梱してサーバー再起動
* 未解決・要判断:
  * クライアント jar 再更新が必要(配布形態は前回同様)
  * 個別スペルの演出・数値の体感調整は実機フィードバック次第
  * スキルツリー未習得のキャストロックは未実装

## task-103実装2 - 全60スペルの取得経路を完備

* 結果: 実装・デプロイ済み
* 変更点:
  * cfb スペル全60種を spell_book/spell_scroll タグに職業別マージ
  * mage→wizards:spell_book/arcane、assassin→rogues:spell_book/rogue、guardian→rogues:spell_book/warrior、paladin→paladins:spell_book/priest
  * scroll 側にも同マージ(巻物経由の取得も可能に)
* 判明したこと:
  * 取得経路は「本+ラピス→Binding Table→呪文書タイプ選択→スペル購入」+「巻物+書→バインド」
  * 呪文書の max_spell_count が上限(追加は別書が必要)
* 未解決・要判断:
  * 本番の習得設計(ツリー連携で解禁か、常時購入可能か)は未決定

## task-103整理1 - datapack/jar 二重登録の整理

* 結果: 整理完了
* 変更点:
  * 役割分担を確定: world datapack `cfb_skills` = サーバーロジック(puffishカテゴリ・functions・進捗・minecraftタグ)、bcn jar = クライアント共有コンテンツ(スペル・武器/書タグ・assets)
  * jar からサーバー側分69エントリを除去、docker-compose の cfb_skills マウント撤去
  * pack.mcmeta を新形式(min_format/max_format)へ修正
  * 関数内コマンドを正しい `puffish_skills` ルートに修正
* 判明したこと:
  * puffish カテゴリは jar 同梱ではロードされない → world datapack が必須(一方でスペル・タグは jar が必須)
  * docker compose 再作成時に `WORKSPACE_HOST_PATH` 未指定だとリモート daemon 側に空 dir がマウントされ MOD が全く読み込まれない(「Loading 4 mods」が兆候)
  * コマンド公式構文: `puffish_skills category lock/unlock <player> <category>`、`skills`(経験値等)は別系

## task-103実装3 - 職業再選択(リセット)経路

* 結果: 実装・ロード確認済み
* 変更点:
  * `cfb:job/reset` を追加(全4カテゴリを `category erase` → メニュー再表示)
  * メニューに [職業をリセットする] を追加(trigger cfb_job=5)、tick が reset を実行
  * jar 側の functions は既に除去済みのため world datapack のみ更新
* 未解決・要判断:
  * erase はスキル・経験値・ポイントも消す設計(リセット=全部消去)。残したい場合は unlock のみに変更要
  * 未習得スペルのキャストロック・アイコン本実装は次フェーズ

## task-103実装4 - 未習得スペルの実質ロック(ツリー解放→巻物付与)

* 結果: 実装・デプロイ済み
* 変更点:
  * アクティブ解放ノードに `puffish_skills:command` reward を追加 → 解放時に対応スペル入り巻物を give
  * cfb スペルを spell_book タグから除去 → 直接購入不可。巻物(spell_scroll タグは維持)経由でのみ呪文書へ結合可能
  * jar から puffish/function/advancement/minecraft 系を除外し、スペル+タグのみに整理
* 判明したこと:
  * spell_engine の learn は経験値レベルゲートのみでスキル連携は無い → 「未習得は取得不可」で実質ロックとする設計
* 未解決・要判断:
  * スクロール渡し等で巻物を譲渡すれば未習得者も使える(厳密ロックではない)。完全ロックには spell_engine 側の拡張が必要
  * 実機での unlock→巻物入手→結合→発動の一連確認は未実施

## task-104実装1 - 経済/災害基盤(datapack実装+BCN Java設計)

* 結果: 骨格実装・デプロイ済み(Java連携はソースのみ)
* 変更点:
  * `cfb_economy` datapack 新設: MD両替メニュー(5階級双方向)、日次維持費徴収、クエスト3種、災害3種関数
  * `BcnEconomy.java` 新設: Flan CurrencyRegistry への MD 登録、AdPother 汚染監視→閾値で cfb:disaster/* 発火、日次徴収の設計実装
* 判明したこと:
  * Flan に CurrencyRegistry/CurrencyHandler API があり、Coins JE 硬貨を通貨登録すればクレーム売買・維持費が MD で動く(buyType=MONEY)
  * AdPother は ChunkPollution/EntityPollution/コールバック群を提供 → 汚染値の読取とフックが可能
  * datapack からは Flan クレーム/汚染値を読めない → Java 側必須の箇所が明確化
* 未解決・要判断:
  * BCN のビルド環境(Gradle/JDK)がこのシェルに無い → Java 部分はソースのみ、ビルドは後続環境で
  * 維持費の本実装(Flan クレーム参照)・EFP 読取・災害自動発火は Java 側待ち
  * クエスト/災害/両替は datapack で動作するが、完成度は骨格レベル(拡充は設計次第)

## task-104実装2 - BCNビルド環境確立+クエストエンジン拡充

* 結果: 実装・デプロイ済み
* 変更点:
  * Java25+Gradle9.8 で BCN を実際にビルド(`gradle build` 成功、実 jar 生成→modsにデプロイ)
  * BcnEconomy をリフレクション型連携に書換(Flan/AdPother mod jar をビルド依存せず BCN 単体でビルド可)
  * `gen_quests.py` 新設: 章立て3章15クエスト(納品/討伐/探索)+MD硬貨・XP報酬
  * 討伐クエストは `minecraft.killed:` 統計で自動カウント
  * Undead Nights に horde カウントダウン告知を有効化
* 判明したこと:
  * app/src/main/resources に assets/data を置けば Gradle ビルドで jar へ自動同梱される(手動zip注入は不要に)
  * 討伐カウントは `minecraft.killed:<entity>` スコアboard統計が正解(自前インクリメント不要)
* 未解決・要判断:
  * Flan ClaimHandler/AdPother の実 API メソッドはビルド時に未解決(リフレクションスタブ) → mod jar を modImplementation に追加すれば本実装可能
  * EFP閾値・維持費料率は仮値(バランス要実測)
  * クエストは骨格動作、ストーリー本文・連鎖ロックは未実装

## task-104実装3 - EFP自走化(スコアボード汚染指数→災害自動発火)

* 結果: 実装・デプロイ済み
* 変更点:
  * `cfb_efp` スコアボードで EFP を管理し、economy_tick が閾値(12/30/60)で黄昏/脅威/襲撃を自動発火
  * `disaster/trigger` に EFP 加算ボタン(重機稼働+10・兵器使用+50・リセット)を追加 → 手動検証可能
  * fee/daily の徴収文に EFP 値を表示
* 判明したこと:
  * datapack 側で EFP を自前管理すれば Java API 無しでも災害連携は自走する(AdPother 連携は精度向上用の後段)
* 未解決・要判断:
  * 重機稼働・兵器使用の実イベントからの EFP 加算は BCN Java 側の仕掛けが本実装
  * AdPother 実汚染値との同期(双方向)は mod jar を modImplementation に入れれば本実装可能

## task-105実装1 - 敵対応表+ボス4体とギミック(cfb_enemies datapack)

* 結果: 実装・召喚検証済み(cornelia/lich)
* 変更点:
  * `cfb_enemies` datapack 新設: boss/menu + 4ボス召喚関数 + tick ギミック
  * Ghost Cornelia: remnants:ancient_vessel + 浸水フィールド(鈍足・飢餓・採掘疲労)+水兵亡霊周期召喚
  * Corrupted Lich: graveyard:lich + 魂の防壁(revenant柱が生存中は耐性4)+飢餓波浪(15s周期の強制hunger)
  * Flagship Core: remnants:hollow_sovereign + 強風(levitation)+砲台掃射(矢を上から降らせる)
  * Eclipse Remnant: remnants:hollow_sovereign + 侵食(常時wither)+眷属周期召喚
  * ENEMIES.md に「8. 実装対応表」を追加(設計名↔実在モブの全対応)
* 判明したこと:
  * 実在 modid は `remnants:`(remnant_bosses ではない)/`graveyard:` — lang の entity.* エントリで確定
  * 26.3 の random_chance predicate は関数内で使いにくい → スコアカウンタ周期に切替
  * graveyard に acolyte/ghoul/lich の同名実在モブがあり、ENEMIES.md の命名とほぼ一致
* 未解決・要判断:
  * 構造物への自動出現は未配線 → 次は各構造物のスポナー配置か、構造物侵入時のトリガー設計
  * Tier 2 汎用敵の spawn 関数群(cfb:spawn/*)は未実装 — 対応表から生成すれば即作れる
  * Dragonkind Evolved ドラゴンは MOD 側実装なので設定確認のみ

## task-105実装2 - 汎用敵14種+構造物別セット出現

* 結果: 実装・全spawn検証済み
* 変更点:
  * `cfb:spawn/*` に Tier2/3 汎用敵14種を生成(対応表の実在モブに名前・tierタグ・PersistenceRequired付き)
  * 構造物別セット出現関数5種(catacomb/ship/fortress/mansion/eclipse)→遭遇時の一括召喚
* 未解決・要判断:
  * 構造物への自動トリガー(侵入検知や生成時スポナー置換)は未配線 — 現状は関数経由の手動/イベント呼び出し
  * spawn関数の配置先・密度のバランスは設計値待ち

## 5ca627a - resources: ITEMS.md 公式レシピ併記 + IC2C/LP 公式データ取得

* 結果: 成功(公式データ取得+ドキュメント併記 完了、push 済み)
* 変更点:
  * ITEMS.md 4件(BC/IC2/PR/LP)の各アイテムに `公式レシピ` 行を併記(マッチ数: IC2 161件, BC 30件, PR 73件, LP 13件)
  * `resources/industrialcraft/`: IC2Classic-1.19.2-2.1.3.4.jar から `assets/`(4521ファイル)+`data/` を展開配置、`recipes_export.json`(917件のクラフトレシピ機械抽出)を追加
  * `resources/logisticspipes/src/logisticspipes/recipes/`: JSON化されていないコード内登録レシピの公式ソースを収録
  * 両 README.md に取得元・方法を追記
* 判明したこと:
  * `TinyModularThings/IC2Classic` は API/バグトラッカーのみで実装ソース非公開. IC2C のクラフトレシピは `IC2Recipes.addRecipes` のコード内登録(`data/ic2/recipes` は空) → CFR 逆コンパイル+SRG→Mojmap マッピング(mcp_config)で `Items.f_XXXX_` 等を解決し抽出可能だった
  * ITEMS.md の wiki 記述は旧版ベースのため、現行公式データと名称が異なる項目は未マッチ(IC2C 版違い、PR の1.21 改名、LP/BC のコード内レシピ)
  * BC/LP では JSON にないレシピが多数(パイプ類、モジュール類はコード登録) → LP は公式ソースを src/ に収録して対応
* 未解決・要判断:
  * 未マッチ項目への対応方針(手作業エイリアス or 別版データ参照)は要検討
  * IC2C の機械レシピ(macerator 等)は `recipes_export.json` 対象外(コード内 TileEntity::loadRecipes で別途登録)
  * IC2 Experimental と IC2 Classic でレシピ体系が異なる点は BCN 実装時にどちら準拠とするか要決定

## 6c3c029 - resources+docs: 残課題対処 (IC2C機械レシピ抽出, TECH_ITEMS.md 新設)

* 結果: 成功 (push は環境の GitHub 認証が無く不可, ローカルコミットのみ)
* 変更点:
  * `resources/industrialcraft/machine_recipes_export.json` 新設 — IC2C jar の TileEntity::loadRecipes を逆コンパイルし機械レシピ188件を抽出
  * `resources/industrialcraft/README.md` に export 追加と Experimental 準拠の決定を追記
  * `docs/TECH_ITEMS.md` 新設 — 4MOD 全706項目のアイテム/レシピ一覧, EU→MJ 統一表, 実装考慮, EFP 連携方針
* 判明したこと:
  * IC2C の機械レシピは `addIC2XPRecipe`/`addIC2SimpleRecipe` 系の一様形式で抽出可能 (188件: macerator 60, extractor 36, compressor 33 等)
  * ユーザー決定: BCN の IC2 仕様は Experimental 準拠 (IC2C は近似参照)
* 未解決・要判断:
  * push はユーザー側の認証で実行が必要 (ローカルに 3 コミット未 push)
  * TECH_ITEMS.md の未マッチ項目のレシピ正本は wiki 記述または LP src/ コードを参照

## 26f0afc / dfa0e7 - task-106 E2E導線検証と実機確認

* 結果: 成功(重大不備を複数発見し修正済み)
* 検証環境: testserver(171 mods)、YSK357 実接続(carpet client)、RCON+data.dat(puffish永続データ)で検証
* 変更点:
  * cfb_economy: クエスト18件/MD両替10件/維持費の無条件報酬バグを `clear 0` カウント+`return run` ゲートで修正
  * 討伐クエスト: 消費ベースライン方式(cfb_q_zb等)に変更 — minecraft.killed スコアの set 非依存で確実動作
  * cfb_skills: `category erase` が全カテゴリを unlocked 状態へ戻す puffish 挙動を発見、reset に lock 全付与を追加、`cfb_hasjob` フラグで無断転職禁止、`job/first_join` で初回全ロック
  * cfbスペル60種: deliver(発射物)/visuals(パーティクル)/sound が皆無だった骨格状態を解消(school別マッピングで演出実装)
  * bcn jar 再ビルド: サーバー側コンテンツ除去(役割分担に適合)、test_arcane 削除、最新 spell/tag 同梱
* 判明したこと:
  * `puffish_skills:command` reward は @s=プレイヤー + function 権限で実行 → give は非opでも動作、isAction=true は解放時のみ(リセットでは巻物は再付与されない)
  * unlock→巻物give は実機で確認(frostbolt巻物がインベントリに出現)
  * 職業選択→対象のみunlock、リセット→全消去+全ロック+再選択可、選択後の無断転職不可 を data.dat で確認
  * クエスト/両替/討伐カウント/メニュー表示、汎用敵14種・構造物セット5種・ボス4体召喚、lich耐性ギミックを確認
  * 結合台経路: paladin_consecrate/assassin_dash 結合済み呪文書が実在(player インベントリ)しておりタグ経路は機能
* 未解決・要判断:
  * 結合→スロットキー発動の最終確認はクライアント操作待ち(発動自体は user 報告で確認済み、演出実装後の再確認を依頼中)
  * 購入封じは spell_book タグ空で設計上成立しているが、結合台UIでの非表示確認は未実施
  * 巻物の受け渡しで未習得者もスペル使用可能(完全ロックは spell_engine 側拡張が必要)
  * クライアント側 jar 差替えが必要(演出・購入封じの反映のため)
  * 構造物への敵自動出現トリガーは未配線(関数呼出しで代替中)
  * EFP閾値・維持費額は仮値のまま

## task-106 E2E導線検証と実機確認(続報)

* 結果: 成功 — cfbスペルの発動不具合を解消し、職業→スキル→魔法→戦闘の導線が実機で完走することを確認
* 変更点:
  * 攻撃系AIMスペル: `aim:{sticky}` のみで照準対象が無いと発動失敗していた問題を `aim:{}` へ修正、支援系は `use_caster_as_fallback` 追加
  * `cast.duration:0.0` の17スペルで release が発火しない問題をキー省略(即時発動)へ修正
  * `cfb:` スペル未登録の根本原因を特定: 稼働中の jar 上書きで zip 読み込みが破損(`invalid LOC header`)し、bcn パックの spell/tag 全件が未ロード状態だった。再起動で解消し、火球の発射・パーティクル・着弾まで実機確認
  * 検証で同形化した `mage_firebolt` を cfb 版へ復元、`spell_book/arcane` タグは購入封じのため空に戻す
* 判明したこと:
  * `wizards:fireball` は arcane 書・巻物・純正書いずれでも発動するのに `cfb:` だけ動かなかった → spell JSON ではなく登録側の問題と切り分け、jar 破損が真因と確定
  * jar 差替えは必ずサーバー停止後か再起動を伴うこと(稼働中の上書きは zip ハンドル破損を招く)
  * 巻物の `spell_engine:spell_container={spell_ids,pool}` 形式、書への結合、結合台経路は正常動作
* 未解決・要判断:
  * 全60スペルの発動確認は一部(firebolt 等)。残スペルも同型修正済みだが個別検証は未実施
  * 結合台UIでの購入封じ表示(spell_book タグ空による非表示)は設計上成立しているが目視未確認
  * 巻物譲渡で未習得者もスペル使用可能(完全ロックは spell_engine 拡張が必要)
  * 構造物への敵自動出現トリガーは未配線(関数呼出しで代替)
  * EFP閾値・維持費額は仮値のまま(実機バランス要調整)
