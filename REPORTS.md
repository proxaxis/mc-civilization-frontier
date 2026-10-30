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

## task-201 BCN 油田仕上げ(流体レンダラ)

* 結果: 実装済み — クライアント描画の既知不具合を修正
* 変更点:
  * `BcnClient` (ClientModInitializer) を src/client/java に新設 — splitEnvironmentSourceSets のため client ソースセット必須
  * `FluidRenderingRegistry.register(OIL, FLOWING_OIL, FluidModel$Unbaked)` で oil_still/oil_flow テクスチャを登録
  * fabric.mod.json に client エントリポイント追加
  * このバージョンの API は `FluidRenderingRegistry.register(Fluid, Fluid, FluidModel$Unbaked)` で、Material は単一 Identifier コンストラクタを取る
* 未解決: クライアントでの実際の描画確認は実機待ち(サーバー側では検証不可)

## BCN アセット配線検証(アイテム/テクスチャ/モデル)

* 結果: 成功 — アセットパイプラインは完備、新規要素モデルブロックの通し検証も登録・配置まで確認
* 変更点:
  * `BcnItemGroup` 新設 — bcn 名前空間の全アイテムを自動収集するクリエイティブタブ(fabric.mod.json lang `itemGroup.bcn.main`)
  * `engine_frame` 検証用ブロック — 5 要素のカスタム 3D モデル・blockstate・アイテムモデル・テクスチャ・lang を追加し登録
* 検証済み: 登録 18 件 vs items.json 29 件の網羅差分は流体のみで正常、`bcn:engine_frame` の give + setblock 動作確認
* 未検証(クライアント目視): カスタムモデルの見た目・タブ表示・oil 流体レンダラ

## BCN パイプ実装(アイテム輸送)

* 結果: 実装済み — multipart 3 状態接続モデル
* 変更点:
  * `BcnPipeBlock` — 6 方向 Conn 列挙型(NONE/PIPE/BLOCK)、自動接続
  * 接続条件: 同種パイプ=PIPE(連続管)、BlockEntity 持ち(容器/機械)=BLOCK(終端フランジ)、その他=NONE(土・壁には生えない)
  * モデル: 中心コア+シャフト+終端フランジを分離、ガラス調透過テクスチャ(render_type: translucent)
  * 26.3 の差分対応: EnumProperty(StringRepresentable)、updateShape 新シグネチャ(LevelReader+ScheduledTickAccess)
* 未解決: クライアントでの透過・接続見た目の最終目視、流れるアイテムの BER 実装は task 204

## task-207 実装1 - BCN 重火器(大砲・攻城兵器)+ EFP 連携

* 結果: 実装・実機検証済み(設置/RS発射/着弾爆発/EFP加算まで動作確認)
* 変更点:
  * `entity/BcnShellEntity` + `CannonballEntity` + `SiegeShotEntity` — ThrowableItemProjectile 継承の砲弾. 大砲弾は高速直進(威力2.0)、巨石は放物線(威力3.5)
  * `block/BcnArtilleryBlock` 基底 + `BcnCannonBlock` + `BcnTrebuchetBlock` — facing/loaded/triggered 状態. 弾薬 useItemOn 装填、素手 useWithoutItem 手動発射、neighborChanged の RS 立ち上がり発射
  * `BcnEntities` — EntityType 登録 + `hasShellCapacity`(同時上限48, 超過は発射拒否)
  * 低負荷設計: `noSave()`(アンロードで滞留しない)、`maxAge` で生存 tick 上限(200/300)、`updateInterval(10)`、`fireImmune`
  * EFP 連携: 発射 `BcnEfp.add(server, 4/8)`、着弾 `+6/12`. API は task-202 側の `energy.BcnEfp`(cfb_efp スコアボード直接書込)を利用
  * `BcnClient` に ThrownItemRenderer 登録(アイテムスプライト描画)
  * リソース一式: blockstate/model/items/texture 生成, loot, recipe(cannon/trebuchet/cannonball/siege_shot), mineable タグ, lang en/ja
* 検証済み(RCON):
  * setblock で cannon/trebuchet 設置 → redstone_block 供給で発射 → EFP +4(砲撃)/+8(投石) 加算を確認
  * 下向き大砲で着弾爆発 +6 加算、投石機の飛行エンティティと Motion 実測
  * `bcn:cannonball`/`bcn:siege_shot` の summon + `getEntities` カウント動作
* 判明したこと:
  * 26.3 はプレイヤー不在だと発射物がシミュレーション範囲外で凍結する(Dummy 合流で再開・着弾加算も確認). vanilla 仕様
  * 26.3 の `useItemOn` は既定が `TRY_WITH_EMPTY_HAND` 返しで、`PASS` を返すと `useWithoutItem` が呼ばれない → 手動発射に必須
  * `Direction.getNormal()` は `getUnitVec3i()/getUnitVec3()` に改名
* 未解決・要判断:
  * 爆風の地形破壊係数/弾速/EFP 係数はバランス要実測(完了条件の調整対象)
  * `useItemOn` 装填の実機目視は本テストの RCON 手順では未完了(ロジックはバイトコード確認済み)
  * 他タスク(energy/railway)の WIP が src 全体を頻繁に壊すため、デプロイ jar は broken ファイル除外の自前ビルド(energy/block・railway 系は除外、MachineBlockEntities は部分スタブで代替)

## task-204 BCN パイプ輸送ネットワーク(アイテム+防水液体)

* 結果: 実装済み・実機検証済み — アイテム輸送・液体輸送ともにテストサーバで動作確認
* 変更点:
  * `ItemPipeBlockEntity` — 搬送中アイテムを Transit リスト(stack/from/to/progress/turned)で保持する BlockEntity. WorldlyContainer 実装の仮想1スロットでホッパー・他機械からの投入を受け付け(投入面=進入面として搬送開始)
  * 輸送: 10tick/ブロック定速. 中央通過時に接続面(PIPE/BLOCK)からランダムに出口選択、行き止まりは反転して往復(BC式バウンス). 端で Container/WorldlyContainerHolder へ `HopperBlockEntity.addItem` 相当で放出、満杯・拒否は残量をバウンス、搬送中に接続先が消えた開放端は ItemEntity として排出
  * `BcnPipeBlock` — EntityBlock 化, `fluid` フラグで系統判別(異系統パイプは接続しない), `property(Direction)` ヘルパ, 防水側は useItemOn で `FluidStorageUtil.interactWithFluidStorage`(バケツ右クリックで注/汲)
  * `FluidPipeBlockEntity` — FluidVariant+量(mB)のブロブ搬送. `FluidStorage.SIDED` で外部からの圧入を受付(Transaction は addOuterCloseCallback でコミット時のみ生成). 隣接 FluidStorage から ~25mB/t 自動抽出+隣接ソースブロック汲み上げ(1秒間隔, 簡易ポンプ相当で将来ポンプ機械に移行予定)
  * `ItemPipeRenderer` + `ItemPipeRenderState` — 26.3 新 submit パイプラインの BER. updateForTopItem で ItemStackRenderState を作り進行度+partialTick で位置補間して描画(エンティティ不用)
  * 登録は既存 `MachineBlockEntities` に集約(FabricBlockEntityTypeBuilder), `waterproof_pipe` の blockstate/モデル/アイテムモデル/loot/mineable/lang を追加, テクスチャは pipe_body/pipe_cap の青緑 tint 版を PIL 生成
  * 接続先判定: `getContainerAt` で WorldlyContainerHolder(堆肥桶等)・コンテナ付エンティティも終端として扱う
* 実機検証(testserver, RCON):
  * ホッパー→item_pipe×3→チェストで cobblestone×8, stone×64, dirt×4 が到達
  * transit NBT(`p`/`f`/`t`/`s`)で搬送中アイテムが可視, サーバ再起動をまたいで永続化を確認
  * チェスト撤去後はアイテムがネットワーク内を往復(デッドエンドバウンス), 水源は防水パイプが汲み上げて 81000mB ブロブがパイプ間を移動
* 実機検証で発覚した環境起因の障害(対処済み):
  * `BcnGateBlock` — createBlockStateDefinition が super() 内で hasMemory 未初期化を読み MEM 未定義で起動クラッシュ(並行作業側で同内容に修正済み)
  * `recipe/rail_signal.json` — `minecraft:redstone_dust` の無効IDで Registry ロード失敗 → `minecraft:redstone` に修正済み
* 未解決・要判断:
  * ghost item の実際の見た目(スケール 0.375, FIXED 表示)はクライアント目視待ち
  * 防水パイプ内の液体ブロブ描画は未実装(NBTでは可視). 必要なら半透明キューブの BER 追加
  * 分岐選択・抽出ゲート・選択管・パイプワイヤー連動は発展項目(task 200/206 系)
  * 液体の汲み上げは無料(エンジン不要). MJ 必須化は task-203 系の発電系統確立後
  * 防水パイプの終端が FluidStorage 非対応(バニラ容器含む)の場合は往復滞留のみ. 開放端への流出は未実装

## task-208 BCN 農業自動化 (自動栽培・収穫設備)

* 結果: 実装済み・実機検証済み — 自動栽培機が種まき・育成待ち・収穫・搬出を稼働確認
* 変更点:
  * `PlanterBlockEntity` — 自身中心の 5x5 (自ブロックを除く 24 区画) を巡回. 10tick 間隔で成熟作物を収穫・空き耕地へ種まき. 種 9 + 収穫 9 の 18 スロット, WorldlyContainer 実装 (上=種投入, 下/横=収穫物) でホッパー連携
  * 作物判定: CropBlock (isMaxAge), NetherWartBlock, BonemealableBlock+age 汎用 (Farmer's Delight 作物・ココア・スイートベリーも対象), 植付可否は `canSurvive` (FarmlandBlock 系・FD の rich soil 含む)
  * 収穫ドロップの種は種スロットへ優先回収して自動再栽培ループ化. 収穫物は `HopperBlockEntity.getContainerAt`+`addItem` で隣接 Container へ自動放出 (チェスト, ホッパー, task-204 のパイプ BE=WorldlyContainer も対象)
  * MJ: `energy.MjReceiver` 実装, 許容圧 MV (51 MJ-P). 内部バッファ 1000, 種まき 10 / 収穫 20 MJ 消費
  * EFP: task-202 の `energy.BcnEfp.add` 経由で発電機と同率 (2.8e-6/tick 換算 = 作業 1 回 2.8e-5) を `efp_buffer` に蓄積し整数分を `cfb_efp` スコアボードへ反映
  * `MjSourceBlock`/`MjSourceBlockEntity` — デバッグ用無限 MJ 源, 毎 tick 全面へ LV パケット送出 (発電機実装までの検証用)
  * `BcnCommands` — `/bcn_mj <pos> <amount>` (Receiver へ分割給電), `/bcn_efp <amount>` (加算検証)
  * リソース: blockstate/planter(active 2状態), model, items, loot, mineable タグ, lang en/ja, テクスチャは既存 engine 系の暫定流用
* 実機検証 (testserver, RCON):
  * 5x5 耕地+中央 planter+上部 mj_source で `mj: 0→723` 給電確認 (MjNetwork.sendPacket→receive 経路)
  * wheat_seeds×64 投入 → 種まき・自然成長・成熟収穫の自動ループ. 種は種スロットへ還流 (64+1 に回復)
  * 隣接チェストへ小麦搬出 18→50 個と累積確認. `efp_buffer: 0.00154` (~55 作業分) 蓄積確認
  * `bcn_efp` で cfb_efp スコアボード書込み実測 (46→49), `bcn_mj` で Receiver 判定・給電動作確認
* 判明したこと:
  * 26.3 では `CommandSourceStack.hasPermission(int)` が廃止 → `permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)` へ移行
  * 26.3 では gamerule 名が名前空間化 (`minecraft:random_tick_speed`, `minecraft:spawn_mobs`)
  * `time query daytime` は 26.3 に無い (timeline 系に変更), RCON で `say`/`echo` は応答に出ない → `scoreboard players set` マーカーで観測
  * randomTickSpeed 加速環境では無水耕地が即 dirt 化する → 試験畑は周囲モート給水が必須
  * `execute if block` は RCON で結果を返さないため検証は data get/scoreboard 併用が必要
* 未解決・要判断:
  * planter 側の ACTIVE 表示切替は稼働瞬間のみ (作業間隔で点滅). 常時点灯ポリシーに変えるかは演出調整時に判断
  * ホッパーからの種自動供給・パイプ直入れは WorldlyContainer 契約上動作する設計 (チェスト放出で実証済) だが, パイプ終端までの E2E は task-204 側成果物との組み合わせ未検証
  * GUI は未実装 (種投入は右クリック挿入/ホッパー, 状態は素手右クリック表示). 管理 UI が必要なら追加
  * 自動栽培機のクラフトレシピ未整備 (設計は TECH_ITEMS.md/精密作業台系の確定待ち)

## task-209 BCN 鉄道高度化 (時刻表ダイヤ・信号システム)

* 結果: 実装済み — 信号閉塞・時刻表ダイヤ・自律運行を testserver で実機確認
* 実装 (com.civilizationfrontier.bcn.railway):
  * `bcn:rail_signal` — ガードレール脇に立てる閉塞信号ブロック (FACING 4 方向 + ASPECT clear/stop). BE は 20tick 毎にレールグラフを `RailState` で BFS 走査して閉塞区間を構成 (他信号のガードレールを境界, 上限 128)
  * 占有判定: 区間内にトロッコがあれば STOP. 停止は "区間方向へ動く先頭車両" のみ対象なので出区間列車は赤でも通過でき単線でもデッドロックしない. 保持時は motion とかまど push を保存→ゼロ化→位置固定、解放時に復帰
  * RS 連動: STOP で全方向に強度 15 を出力 (側面ランプ等の駆動用). ガードレール未検出時は fails-closed で STOP
  * 時刻表: `RailwayData` SavedData に路線 (名前・周期 period・停車場リスト). 停車場は `at <offset>` (周期内発車時刻) または `dwell <ticks>` (停車時間). 列車割当は `im.root` → 路線名
  * `TimetableManager` — IM の駅停車 (zone 1/4, `im.wait` 減算) に乗り、残り発車時刻を毎 tick 書き込む. 時計は 26.3 の WorldClock (overworld) を使用
  * `/bcn rail` コマンド: line create/delete/list/info, stop add(at|dwell)/remove, assign/unassign, trains, time
* 検証 (testserver, 26.3):
  * 区間内駐車車両で両方向信号が aspect=stop、解放で clear 復帰 + 保持車両が保存 motion で再発車
  * 反対方向の通過は赤でも止まらない (方向性確認)
  * 駅停車中の列車の `im.wait` が発車時刻まで減算され予定時刻に発車 (offset 指定で実発車確認)
* デプロイ: bcn-0.1.0.jar → resources/mods (停止→上書き→起動の手順. 稼働中上書きは zip 破損を招く注意を追記)
* 未解決・要判断:
  * 分岐ポイント自動切替は未対応 (経路は路線の停車場リスト管理)
  * 未管理単発トロッコは車両単位制御、ガードレール上の先客への追突は物理的に防げない
  * 詳細仕様は docs/RAILWAY.md に記載

## task-202 BCN エネルギー基盤 MJ (生成・伝送・消費 API とケーブル)

* 結果: 実装済み — 生成 → ケーブル伝送 → 消費の往復、MJ-P ランク、変圧・蓄電、EFP 加算 API を testserver で実機確認
* 実装 (com.civilizationfrontier.bcn.energy):
  * `MjPressure` — 5 ランク ULV/LV/MV/HV/EV = 2/13/51/205/819 MJ-P (旧 EU-P ×0.4 整数化)
  * `MjReceiver` — パケット受信インターフェース `receive(packetEnergy, face)`. パケットのエネルギー量自体が圧値
  * `MjCable` — `maxPacket()` (許容圧) + `lossPerBlock()` (損失/ブロック). ケーブルは BlockEntity を持たずブロック特性として定義
  * `MjNetwork.sendPacket` — 出力面からパケット 1 個を送出. 隣接 Receiver へ直送 or ケーブル網を BFS 探索して最短ホップの受信先へ配送. 網シミュレーション無し (パケット都度探索) の軽量設計
  * `BcnEfp` — `cfb_efp` スコアボード (#efp ホルダ) へ `add(server, long)`. objective 未存在時は DUMMY で自動作成
* ブロック (全12種, MachineBlockEntities 登録):
  * ケーブル5種: tin(ULV, 0.01) / copper(LV, 0.08) / gold(MV, 0.16) / glass_fiber(HV, 0.01) / hv(EV, 0.32). 損失は IC2 源流値 ×0.4. BcnPipeBlock の 3 状態 multipart 流用, `getClass()` 一致条件でアイテム管/液管との混線を防止
  * `mj_generator` — 検証用発電機 4 MJ/t. TIER 状態で ULV〜EV 切替, 定格フルパケットを溜め送出, 稼働中 EFP 2.8e-6/t 蓄積 (100 台×3h → 閾値 60 の設計値)
  * `mj_consumer` — 全量吸収シンク. `total_received` が累計計測値
  * `transformer_lv/mv/hv` — 降圧変圧器 (MV→LV, HV→MV, EV→HV). 出力面以外で入力ランク以下を受け, 出力ランクで最大 4 pkt/t 送出 (大1→小4)
  * `battery_box` (LV, 16000 MJ) / `mfe` (MV, 1.6M MJ) — 出力面以外で充電, FACING 面からランクパケット放出
* 実機検証 (testserver, RCON, `/data get block` 観測):
  * 発電機(LV) → 銅線3 → 消費機: 受信 433.84 = 34pkt × 12.76 (13 - 3×0.08 損失を正確に確認)
  * ULV(2MJ) 発電 → スズ線 → 消費機: 85pkt × 1.98 (2 - 2×0.01) で通過
  * LV(13MJ) → スズ線(許容2): ケーブル蒸発 (`IS_AIR` 確認), MV(51MJ) → 銅線(許容13): 同様に蒸発
  * 蓄電器: 発電 → 銅線 → battery_box 充電 (received 335.92) → 出力面から消費機へ 335.84 放電, 残量 0.08
  * 変圧器: tier=2 発電機 → 金線 → transformer_lv(MV→LV) → 銅線 → 消費機. 50.84 入力 → 406.64 出力 (8pkt 変換, 残 0.08)
  * EFP: `/bcn_efp 5` で 49→54 加算, 発電機の `efp_buffer` 小数蓄積を NBT で確認. `/bcn_mj` 直接給電 100MJ 確認
  * BE の NBT (mj/total_received/total_emitted/efp_buffer) は再起動を跨いで永続化確認
* 判明したこと:
  * `BlockEntityType` 生成は `FabricBlockEntityTypeBuilder` + `MachineBlockEntities` 登録が既存流儀 (BE ctor は (pos,state) で型内埋め)
  * 26.3 の NBT は `saveAdditional/loadAdditional` (ValueOutput/ValueInput). `/data get block` で検証可能
  * ケーブルは BE 無し (BFS がブロックを辿る). `newBlockEntity` → null で問題なく動作
  * `BcnPipeBlock` 継承で `hasBlockEntity()==true` になるため隣接パイプは BLOCK キャップ表示になる (見た目のみ)
* 未解決・要判断:
  * パケットは最短ホップ1受信先のみに届く (均等分配なし). 複数受信先への分配ポリシーは機械実装時に要検討
  * 変圧器は入力ランク以下のパケットを許容 (小パケットは素通し的に変換). 逆変換 (昇圧) は仕様上省略
  * 受信機が許容圧超過パケットを受けた場合は拒否のみ (IC2 の機械爆発は未実装)
  * BFS 探索はパケット毎の都度走査. 巨大ケーブル網での負荷は規模が出てから要測定
  * EFP 係数は設計値 (2.8e-6/t). 実バランスは稼働機械数の実測で再調整

## task-206 BCN 制御回路 (壁面 RS ワイヤー+小型論理素子)

* 結果: 成功 (実機検証済み)
* 変更点:
  * `BcnWireBlock` 新設 — FaceAttachedHorizontalDirectionalBlock で床/壁/天井に貼付可能. Conn 3 状態 (none/wire/block) の multipart 接続モデルは item_pipe 踏襲. 信号伝播は独自のコンポーネント評価: 面6+辺12 の近傍で flood し、外部ソースの OR を powered へ一括反映 (強度なしのブール式, 距離減衰なし)
  * `BcnGateBlock` 新設 — 同一クラスで 10 種 (not/buffer/or/nor/and/nand/xor/xnor/rs_latch/t_latch). ローカル端子 (front/back/left/right) を AttachFace+FACING からワールド方向へ解決. 入力は `level.getSignal(neighbor, dir)` で vanilla 準拠に読み、出力側のみ弱+強信号を返す. 評価は `scheduleTick(2)` で行いラッチは MEM プロパティに状態保持. トグルラッチは右クリックでも反転
  * `BcnEngineBlock` — neighborChanged/getStateForPlacement で `hasNeighborSignal` → ACTIVE を同期 (RS 信号オンオフ経路)
  * アセット: blockstates(redwire=6面×6接続+lit, gate=10種×6向き+out点灯)・モデル・テクスチャ・items・loot・shapeless レシピ・lang を生成 (約190ファイル)
  * mineable/pickaxe に redwire + gate 全種を追加
* 検証 (ローカル Fabric サーバ + RCON):
  * 床ワイヤー: レバー→ワイヤー連鎖で powered 伝播を確認
  * 壁ワイヤー: 床→壁→上部壁への登り伝播を確認 (edge 近傍で角回り込み動作)
  * OR ゲート: 入力ワイヤー ON → out_east=true → 出力ワイヤー → engine_stone active=true の全経路を確認
  * NOT ゲート: 無入力で out_north=true, AND ゲート: 片入力のみで off (負例も正しい)
* 判明したこと:
  * 26.3 の `SignalGetter.getSignal(pos,dir)` は conductor に対して `getDirectSignalTo` を畳み込むため、powered 固体ブロックを素通りして直接信号を読める (diode 互換の入力検出)
  * ワイヤーが弱信号のみを出す設計にすると外部読み取りは直接信号のみで自己保持しない (ダストは読み対象から除外してフィードバック防止)
  * サーバーの `pause-when-empty-seconds` が tick 駆動の検証を止める → 検証時は -1 必須
  * setblock の `lever[powered=true]` は位置によって稀に lever が残らない事象あり (ハーネス側の問題、本コード非依存)
* 未解決・要判断:
  * `data/bcn/recipe/rail_signal.json` が `minecraft:redstone_dust` を参照し datapack ロード失敗 → 正しくは `minecraft:redstone` (railway 担当の要修正, 検証時は jar 側を仮パッチ)
  * 絶縁色違いワイヤー・ケーブル束・ジャケット被覆は未実装 (PR 仕様の拡張余地)
  * ゲートのクライアント目視 (pin 点灯・壁貼り見た目) はクライアント起動待ち

## task-203 BCN エンジン + リファイナリー (燃料→MJ・2段階燃料)

* 結果: 実装済み — 固体/流体燃料エンジンの発電・リファイナリー精製・稼働中 EFP 加算を testserver で実機確認
* 燃料体系: 黒オイル (bcn:oil, 3000 MJ/B) → リファイナリー精製 → 金オイル (bcn:fuel, 12000 MJ/B) の2段階
* 実装:
  * `FuelFluid` + `BcnFluids.FUEL/FLOWING_FUEL` + FUEL 液ブロック + `fuel_bucket` (金オイル, 描画登録済)
  * `BcnEngineBlock` — `EngineKind` (STONE: 固体燃料 1 MJ/t LV 出力 / COMBUSTION: 流体燃料 6 MJ/t MV 出力) 化し MjMachineBlock 継承 + EntityBlock 化
  * `EngineBlockEntity` — RS 信号稼働, 燃焼時間消費, 定格フルパケット送出, 稼働中 EFP 2.8e-6/t. 固体燃料は WorldlyContainer 1 スロット (ホッパー搬入可) + 右クリック投入, 流体燃料は FluidStorage.SIDED 経由 (パイプ/バケツ) で内部タンク 4B
  * `MjRefineryBlock`/`RefineryBlockEntity` — 入力タンク (oil 専用 8B) → 405 droplets/t・4 MJ/t 消費で変換 → 出力タンク (fuel 8B). FluidStorage.SIDED で注入=oil のみ/取出=fuel のみの片道窓口, バケツ操作も対応
* 実機検証 (testserver, RCON/data get):
  * engine_stone: burn_time 消費で 1 MJ/t 発電 → 銅線 → 消費機へ 12.92/pkt で到達 (active=稼働中)
  * engine_combustion: タンク 81000 droplets を消費し 6 MJ/t で 9000 MJ 生成 → 金線経由で 8998.68 到達
  * refinery: oil 81000→54675, fuel 0→26325 (405/t) 変換, MJ 260 消費
  * 稼働中 `efp_buffer` が蓄積し整数分は cfb_efp へ反映
* 判明したこと:
  * `data modify block` で文字列流体 ID を書くと loadAdditional の getValue が EMPTY を返す事象を確認 (空は null 扱いの防御実装済み). 実経路は fluidStorage.insert がインスタンス直接保持するため影響なし
  * 燃料タンクの save/load 経路 (data modify 注入等) は実流体搬入経路 (防水パイプ→fluidStorage) での再検証を推奨
* 未解決・要判断:
  * 燃料対応表はかまど近似の固定テーブル (CookingFuel コンポーネント参照への移行は別タスク候補)
  * エンジン GUI 未実装 (燃料投入は右クリック/ホッパー/パイプ). 管理 UI は別途
  * 核/蒸気等の上位 tier は EngineKind 拡張で追加可能 (今回 STONE/COMBUSTION のみ)
  * engine_combustion テクスチャは engine_stone の色変え暫定

## task-205 BCN クァーリー (地上→岩盤の自動採掘)

* 結果: 実装済み — MJ 供給で直下 5x5 を層状自動採掘, ドロップを隣接コンテナへ搬出, Flan 保護判定と未ロード待機を実機確認
* 実装 (`MjQuarryBlock`/`QuarryBlockEntity`):
  * MjStorageBlockEntity 継承の MJ 消費機 (LV 以下受電, 容量 128, 1 ブロック 8 MJ, 4 tick 間隔)
  * 採掘範囲: クァーリー直下 5x5, y-1 から層状 (x->z->y 下降) に岩盤まで走査. 空気/流体/採掘不可 (岩盤等) はスキップ
  * 採掘は設置者を記録し `[BCN-Quarry]` FakePlayer で `Block.getDrops` 発行. Flan `ClaimHandler` をリフレクション解決: クレーム有り → `getOwner` が設置者と一致 or `canInteract(BREAK)` が許可なら採掘, 判定不能クレームは保守的にスキップ
  * 未ロード位置はカーソルを進めず待機 (常駐チャンクロード無しの設計準拠)
  * ドロップは隣接 Container (チェスト/WorldlyContainer 準拠の item_pipe) へ搬出, 溢れ分は上面へ自然ドロップ. 稼働中 EFP 加算
* 実機検証 (testserver, RCON):
  * mj_source 供供給で採掘進行 (received 584 MJ, 57 ブロック掘削, カーソル cy 下降), 丸石 56 個がチェストへ搬出
  * `efp_buffer` 蓄積 + `total_emitted` が掘削数カウンタとして記録
* 未解決・要判断:
  * 範囲は固定 5x5 (BC 式の枠/マーカー拡張は未実装). 掘削速度・消費は要バランス調整
  * 保護判定はクレーム存在チェック + owner 一致 + canInteract(BREAK) の 3 段リフレクション. Flan のクレーム作成検証は要実地テスト
  * GUI/枠表示未実装 (稼働表示は ACTIVE ブロック状態)

## hotfix 災害連鎖発火の修正 (cfb_economy)

* 障害: EFP が閾値(12/30/60)を超えると economy_tick が毎 tick 災害を発火し続け, threat が evoker/illusioner を毎 tick 召喚. エンティティ集中で SpellcasterIllager.pushEntities が詰まりサーバー watchdog クラッシュの連鎖原因になっていた
* 修正:
  * disaster/check.mcfunction を新設: 発火時に該当階級分を EFP から消費し #disaster_cd=6000(5分) を設定. 低階級から順に判定して 1 回 1 件のみ発火
  * disaster/decay.mcfunction を新設: 1200tick 毎に EFP -1 の自然減衰(旧実装は remove 0 で無効だった)
  * economy_tick から閾値直接判定を削除し CD 経由に変更
  * threat の召喚位置を @r(ランダムプレイヤー位置)に変更(関数呼び出し位置への定点召喚を解消)
* 検証: EFP=46 設定で threat が 1 回発火し 30 消費(46→16), CD 6000 カウントダウン確認. 連続発火メッセージ 0 件

## task-210 BCN builder 系 (landmark/filler/blueprint/architect/builder)

* 結果: 実装完了, 実機検証は兄弟タスクのビルド破綻で未実施
* 変更点:
  * `builder/LandmarkBlock` — 青マーカー + 緑マーカー (path フラグで兼用). 右クリックで active 切替, 軸方向に MAX_DISTANCE=64 内のマーカーを直線スキャンして連結, 到達可能マーカー群の包絡直方体を findVolume で返す (path マーカーは範囲形成に非参加). active 時は lightLevel 12
  * `builder/FillerBlock` + `FillerBlockEntity` — 27 slot WorldlyContainer (ホッパー供給可, BlockItem のみ投入可). 空の手で右クリックで pattern 切替 (fill/clear/flatten/stairs=外殻). 4 tick 毎に 1 ブロック処理, 1 ブロックあたり 1 MJ 消費. 隣接 landmark の findVolume で範囲取得, `level.isLoaded` でロード済みチャンクのみ稼働
  * `builder/SnapshotItem` — template/blueprint/map_location/list の 4 kind. blueprint は volume 内の全セルをパレット+インデックスで完全保存, template は素材のみ. データは `DataComponents.CUSTOM_DATA` の CompoundTag 格納. map_location は useOn でクリック座標を marks 配列に追記
  * `builder/ArchitectTableBlock` — blueprint/template を持って右クリックすると隣接 landmark 範囲を capture してアイテムへ書き込み (GUI は未実装の右クリック式)
  * `builder/BuilderBlock` + `BuilderBlockEntity` — slot0=blueprint, 1-27=素材. blueprint アイテムで右クリックで装着, 空の手で排出. 4 tick 毎にセルを走査しパレット一致ブロックを消費して配置, 1 ブロック 1 MJ. 設置原点は機械直上 (origin タグ読み取りは次版)
  * アセット: blockstates(landmark/path_mark active 2 態, 機械 3 種), モデル, テクスチャ, items, loot, レシピ, lang (en/ja) 生成
  * 登録は他エージェントが先行で入れていた FILLER/BUILDER エントリと重複するため整理 (最終的に BcnBlocks/BcnItems/MachineBlockEntities 全て 1 定義)
* 判明したこと:
  * 26.3 では `displayClientMessage` は無く `Player.sendSystemMessage(Component)` を使う
  * `BlockEntityType.Builder.of` は存在せず, Fabric API の `FabricBlockEntityTypeBuilder.create` を使う
  * `Containers.dropItemStack` でインベントリ溢れ分を床に出せる
  * 共有レジストリファイル (BcnBlocks/BcnItems/MachineBlockEntities) は複数エージェントが同時編集しており短時間で内容が変わる
* 未解決・要判断:
  * ビルドは兄弟エージェントの in-flight コード (EnvGenerator/Geothermal/Reactor/WaterGenerator/SolarGenerator/TankBlockEntity/BcnWrenchItem) のコンパイルエラーで失敗中. 本タスクのファイルは単体 javac でコンパイル済み
  * 実機検証 (landmark 3 点→filler 充填, blueprint 保存→builder 再現, MJ 消費) は全体ビルド復旧後に要実施
  * landmark の赤ビーム表示・architect 本 GUI・builder の複数ブロック/tick 化は未実装の拡張余地

## task-210 BCN IC2 発電機 (地熱/水力/風力/ソーラー/原子炉)

* 結果: 実装済み, testserver 実機検証済み
* 実装 (energy/block, energy/block/entity):
  * `Ic2GeneratorBlock` — 5種共通ブロック (FACING 出力面, ACTIVE 発電表示, UI 無し). useItemOn で溶岩バケツ投入 (FluidStorageUtil) / ウランインゴット燃料投入
  * `EnvGeneratorBlockEntity` — 環境発電機の共通基底. 20tick 毎に環境再評価, クリーン電力は EFP 微減算 (-1e-6/t)
  * `generator_water` — 3x3x3 の水ブロック数に応じて最大 0.1 MJ/t (ULV)
  * `generator_wind` — y64-144 の高度係数 x 天候 (雨 1.2, 雷 1.5) で最大 2 MJ/t (ULV). 要見空
  * `generator_solar` — SKY_LIGHT_FACTOR 環境属性 x 天候 (雨 0.4, 雷 0.2) で最大 0.4 MJ/t (ULV). 要見空
  * `GeothermalBlockEntity` — 溶岩タンク 4B, 溶岩バケツ/防水パイプ受入 (FluidStorage.SIDED 登録), 8 MJ/t (LV)
  * `ReactorBlockEntity` — ウランインゴット 1 個 = 6000tick, 2 MJ/t (LV). 稼働で熱蓄積, 停止で冷却, 熱>=1200 でメルトダウン (半径8爆発+ドロップ+EFP+500). 燃料切れは熱スパイク+400 で高温時に臨界誘発. パルス運転で安全運用可能な設計
* リソース: blockstate/model/texture(生成)/item/loot/recipe/lang/pickaxe タグを 5 ブロック分追加
* 検証 (testserver, RCON):
  * solar: produced_total 8.06 MJ, efp_buffer -8.4e-5 (クリーン微減算)
  * wind: produced_total 8.1 MJ (高度係数動作)
  * water: produced_total 5.3 MJ (周囲水ブロック検出)
  * geothermal: 溶岩タンク注入後 produced_total 56.0 MJ = 8 MJ/t x 7t, 溶岩消費 81 droplet/t
  * reactor: 空スロットで熱スパイク1回 (+400) のみ, 冷却動作, メルトダウンせず
  * reactor メルトダウン: heat+燃料で臨界, 爆発でブロック破壊, EFP 938 -> 1438 (+500)
* 修正: 燃料切れ熱スパイクが毎 tick 加算され空設置数秒で爆発するバグを fuelOutSpiked フラグで1回化
* 備考: 並行作業 (crop/processor 系) のコンパイルエラーのため、jar は ReactorBlockEntity.class と tin_can レシピを直接パッチした検証用ビルド. ソースは修正済みなので次回フルビルドに反映される

## task-21x 実装1 - BCN IC2 加工機 (macerator/extractor/compressor/canner/alloy_smelter)

* 結果: 実装・ビルド済み (実機起動検証は並行タスクの datapack 破損で保留)
* 変更点:
  * `energy/BcnProcessingRecipes` — `data/*/processing/<machine>.json` を SimpleSynchronousResourceReloadListener で読む機種別レシピローダー. datapack 追加でレシピ拡張可能
  * `energy/block/MjProcessorBlock` — MjMachineBlock (FACING+ACTIVE) 継承の共通加工ブロック. useItemOn=入力投入, useWithoutItem=出力回収
  * `energy/block/entity/ProcessorBlockEntity` — WorldlyContainer+MjReceiver. 入力2+出力1スロット, レシピ解決→progress→MJ消費 (4/tick, LV圧, 260MJ バッファ), ACTIVE=稼働中, EFP 微加算, hopper は上面/側面=入力・下面=出力
  * `BcnBlocks` に5機械登録, `MachineBlockEntities` に共通 PROCESSOR 型, `BcnItems` に機械5種+資材11種 (dust4, bronze/steel ingot, coal_chunk, rubber, tin_can, fuel_can, canned_food)
  * レシピ: 粉砕=鉱石→粉2x/インゴット→粉/砂・火打石派生, 抽出=slime→rubber 等, 圧縮=coal_chunk→diamond 等, 缶詰=tin_can+fuel_bucket/cooked_beef, 合金炉=copper+tin→bronze4, iron+coal→steel
  * アセット: 5機械の orientable + active 前面モデル, 機種色アクセント, アイテム11種テクスチャ
* 判明したこと:
  * 26.3 の ResourceManagerReloadListener メソッド名は `onResourceManagerReload` (reload ではない)
  * FabricBlockEntityTypeBuilder は1型で複数ブロックを束ねられる (PROCESSOR 型が5機械共有, 機種は blockstate の block から解決)
* 未解決・要判断:
  * 実機検証は testserver が別タスクの datapack 破損で起動不可のため未実施 (javac/gradle build では全ソースコンパイル済み)
  * レシピ ticks/MJ 係数は仮値 (400t, 4MJ/t). バランス要実測

## task-210 BCN IC2 電力周辺(絶縁/検出/分割ケーブル, batbox/mfsu, バッテリー)

* 結果: 実装済み・実機検証済み
* 変更点:
  * `MjCable` に `routesFrom(level,pos,entry)` (送出方向フィルタ) と `onPacketPass` (通過フック) を追加. `MjNetwork` BFS は各ケーブルの侵入面を追跡して利用. 既定は従来どおり全方向で既存ケーブル互換
  * `cable_detector` — MjCableBlock 派生 (EV, 損失0.32). onPacketPass で BE が 4tick パルス発光, isSignalSource/getSignal/getDirectSignal で RS15 出力
  * `cable_splitter` — FACING=指定入力面. 入力面からのパケットは `CableSplitterBlockEntity.nextLane` が送出可能面 (ケーブル/Receiver 隣接) をラウンドロビン巡回して振り分け. 入力面以外からは通常ケーブルとして全方向通過
  * 絶縁派生 — cable_gold/cable_hv を被覆なし (0.2/0.4 MJ 損失, 強度0.5) とし, 2x/4x 絶縁を追加 (損失 0.18/0.16, 0.36/0.32, 強度2.0). ランクは据え置き (IC2 源流 0.5/0.45/0.4, 1.0/0.9/0.8 EU の MJ 換算)
  * `batbox` (LV, 16000MJ) / `mfsu` (EV, 4000000MJ) を追加し battery_box(LV)/mfe(MV) と合わせて 3 段蓄電. BATTERY BE タイプは 4 ブロック共有
  * `BcnComponents.MJ_STORED` (int コンポーネント) + `MjBatteryItem` — アイテム内 MJ 保持 (re_battery 4000/LV, su_battery 400/LV 使い捨て, energy_crystal 400000/MV, lapotron_crystal 4000000/EV, batpack 24000, lappack 120000). 耐久バーで残量表示, pack は胸スロット装備可
  * `BatteryBlockEntity` — WorldlyContainer の機械スロット 2 個 (slot0=充電:バッファ→充電式, slot1=放電:バッテリー→バッファ, SU は空で消滅). ホッパー/パイプからの搬入出も可
* 実機検証 (testserver, RCON):
  * splitter: source→splitter(facing=down)→2 consumer で両枝 608.64 MJ 同量受信 (ラウンドロビン分流)
  * detector: パケット通過で隣接 redstone_lamp が lit=true
  * batbox/mfsu: 側面 mj_source から 715 MJ 充電確認 (上面=出力面への入力は正しく拒否)
  * バッテリー: charge スロットの re_battery に mj_stored=741 充電, su_battery は放電スロットで消費→消滅
* 未解決・要判断:
  * 絶縁ケーブル/検出/分割のテクスチャは素ケーブル流用 (専用絶縁テクスチャは後続で差替可)
  * batbox と battery_box は同スペックの重複 (battery_box は先行実装の残置, 将来 batbox に統合して battery_box 削除検討)
  * batpack/lappack の装備中ツール給電 (IC2 のツール連携) は未実装, バッテリーとしては充放電可
  * 被覆なし銅ケーブルの独立アイテムは未追加 (copper_cable は従来通り 0.08)

## task-212 BCN マシン群 (tank/pump/chute/mining_well 等)

* 結果: 実装済み・実機検証済み — 優先4機種は testserver で動作確認, 残り3機種は登録/配置まで確認
* 実装 (全て `block/` + `block/entity/`, 登録は BcnBlocks/BcnItems/MachineBlockEntities):
  * `tank` — ガラスタンク. 1段16B, 縦積み連結をカラム集約の Storage として露出(下から充填/上から排出). `FluidStorage.SIDED` + バケツ右クリック対応, 透過描画
  * `pump` — MjStorageBlockEntity 消費機. 直下64ブロックを下降スキャンし最初の流体ソースを 10MJ で汲み上げ, 内部4B バッファ→隣接 FluidStorage (防水パイプ等) へ送出
  * `chute` — MJ 不要のチェスト+ホッパー. 18スロット, 8tick毎に上の容器から1個引き込み下へ押し出し
  * `mining_well` — Mj 消費 8MJ/ブロックで直下を1列掘削(クァーリー単幅版). FakePlayer+Flan 保護準拠, ドロップは FACING 背面の容器へ排出(なければ上にドロップ), 設置者記録
  * `auto_workbench` — グリッド9スロット+出力1. 20tick毎に `RecipeManager.getRecipeFor(CRAFTING)` でレシピ判定し自動クラフト(残存素材は getRemainingItems で処理). WorldlyContainer で素材投入/成果取出
  * `filtered_buffer` — フィルター9ゴースト+バッファ18スロット. 一致品のみを8tick毎に隣接容器へ押し出し(空フィルタ=全通過)
  * `flood_gate` — 4B バッファ. 20tick毎に隣接の空気/置換可能セルへソースブロックを拡散配置(タンク逆動作)
* アセット: BC 原本テクスチャ(tank/pump/mining_well/chute/auto_workbench/flood_gate/filtered_buffer)を流用, orientable+cube_column モデル, lang/loot/mineable 登録
* 実機検証 (testserver, forceload 必須):
  * pump — 隣接 mj_source 受電 (mj:260) で水源を汲み上げ amt:81000, `fl:minecraft:water` 確認
  * mining_well — 受電で直下を cy:150→93 まで掘削進行, 消費 MJ 減少
  * auto_workbench — oak_planks×4 を2x2に並べて crafting_table 自動生成 (出力スロット9に2個)
  * chute — 上位チェストの apple×3 が下位チェストへ到達
* 検証中の環境起因トラブル:
  * 並行作業の `bcn:tin_can` 未登録アイテム参照でサーバが Registry ロード失敗→クラッシュループ(並行側で TIN_CAN 登録後に解消, 再デプロイ済み)
  * 初期検証で機械が mj:0 だった原因は (a) 機械ブロックのチャンクが forceload 対象外, (b) mj_source を非隣接(+2)に設置した試行ミス — 隣接受電で解決
* 未解決・要判断:
  * tank の液面可視レンダリングは未実装(BER で半透明流体面を描く案)
  * filtered_buffer のフィルタースロットは NBT/`item replace` で設定する形 (GUI 未実装のため)
  * auto_workbench のレシピ固定(テンプレート保持)は現状グリッド内容をそのまま使う簡易版 (BC 原本はレシピをテンプレとして保持)
  * flood_gate の拡散範囲は隣接1セル/回のみ (広域拡散アルゴリズムは未実装)
  * クライアントでのモデル見た目(前面LED等)は目視未確認

## task-206 BCN 部品 (歯車・レンチ・絵筆)

* 結果: 実装済み — 5 歯車・レンチ・絵筆 17 色を give/クラフト可能, レンチで facing 回転を実機確認
* 実装:
  * `gear_wood/stone/iron/gold/diamond` — BC 定型の段階クラフト (中央=下位歯車, 十字=素材4). `c:gears/{wooden,stone,iron,golden,diamond}` + `c:gears` conventional タグ登録
  * `BcnWrenchItem` (`wrench`) — 石歯車+鉄3. 右クリックで任意ブロックの `facing` EnumProperty<Direction> を順送り回転 (バニラ機械・BCN 機械共通)
  * `paintbrush` + 染色版16色 (paintbrush+染料→着色). 着色機能はスコープ外でアイテム/レシピのみ
* 実機検証 (testserver, RCON/carpet fake player):
  * `/give` で全品入手, `/recipe give` で gear_iron/wrench/paintbrush_red のレシピ登録を確認
  * レンチ: furnace north→south 回転, engine_stone north→east 連続回転を確認
* 判明したこと:
  * carpet fake player は tp 後に落下するため検証時は足場設置が必須. `use once` は単発で外れる場合があり `use continuous` が確実
  * レシピ読込エラーは並行作業の `bcn:rail_signal`/`bcn:tin_can`/`bcn:processing/*` のみで部品レシピは全正常
* 未解決・要判断:
  * 絵筆の対象ブロック着色機能は未実装 (スコープ外, 別タスク)
  * 歯車の段階クラフト中間物としての c:gears タグ活用は後続レシピで拡張
  * 並行作業のレシピエラー (tin_can, rail_signal, processing/*) は別タスクで要修正

## task-210 BCN 残りエンジン (redstone/creative)

* 結果: 実装済み — redstone エンジンの RS 駆動微弱発電と creative エンジンの常時大出力を実機確認
* EngineKind 拡張: `FuelMode` enum (SOLID/FLUID/NONE_POWERED/NONE_ALWAYS) + rate + outputRank + addsEfp のテーブル化. tier 追加は enum 1 行 + ブロック/アイテム登録で可能
  * `REDSTONE` — 燃料不要・RS 駆動・0.2 MJ/t・ULV 出力 (BC redstone engine 相当の微弱 tier)
  * `CREATIVE` — 燃料不要・常時発電・200 MJ/t・EV 出力・EFP 除外 (クリエイティブ専用, クラフト不可)
* 実装詳細: `BcnEngineBlock(EngineKind)` で engine_redstone/engine_creative を登録, `EngineBlockEntity` は fuelMode で燃焼処理を分岐 (NONE 系は produce() 直接), RS ゲートは needsPower() で判定, 燃料投入は SOLID のみ許可 (右クリック/ホッパー両方)
* 実機検証 (testserver):
  * engine_redstone + redstone_block → 0.2 MJ/t で 60.2 MJ 生成 → スズ線経由 59.7 MJ 到達 (ULV 2MJ パケット). efp_buffer 蓄積確認
  * engine_creative (RS 無し) → 200 MJ/t で 61000 MJ 生成 → HV 線経由 60576.4 MJ 到達 (EV 819MJ パケット). efp_buffer=0 で除外確認
  * `/recipe give` で engine_redstone レシピ登録確認 (ガラス1+板材3+木歯車2+ピストン1)
* 未解決・要判断:
  * creative レシピは意図的に無し (クリエイティブタブ限定). redstone engine の素材は BC 準拠の仮置き
  * 既存 engine_stone/combustion のレシピは別タスク (材料は歯車系で確定済み)

## task-219 BCN Project:Red (配線/IC/モーター/blulectricity)

* 結果: 実装済み — 色別絶縁ワイヤー・ケーブル束・ic_gate・手ノコ/マイクロブロック・フレームモーターを testserver で実機確認
* 配線系 (task-206 の redwire を拡張):
  * `BcnInsulatedWireBlock` — 16 色 (DyeColor 全色, `insulated_wire_<color>`). 同色絶縁ワイヤーとケーブル束にのみ接続し、ネットワークは色別に完全分離. 外部ソースの信号読み/弱信号出力は redwire 同様
  * `BcnBundledCableBlock` + `BundledCableBlockEntity` — 16 チャネルの信号を1本で運ぶ. チャネル状態は BE の 16bit `channels` マスクに保持
  * `ColoredWireNetwork` — 評価エンジン. チャネル c のハイパーコンポーネント (bundled 全体 ∪ 色 c の絶縁ワイヤー群) を flood し、メンバの外部信号 OR で一括 powered 確定. 異色ワイヤーは同一ケーブル束上で混線しない. redwire とは別ネット (絶縁→redwire の一方向読み取りのみ許可しループ防止)
* `ic_gate` — `BcnIcGateBlock` + `IcGateBlockEntity`. 内部に最大4個の論理セル {t,in,out,src} を記憶し1ブロックで複数論理を実行. セルは各ゲート種のデフォルト端子配置で登録 (ゲートアイテム右クリック追加/ドライバーで削除/素手で構成表示). `src` 参照で先行セル出力を入力に連鎖させ多段論理も可能 (NBT 編集で設定). ラッチ系はセル別 mem を保持
* 中間素材 — TECH_ITEMS.md の PR 系を登録: plate 系 4種/陽極・陰極・指針/シリコンチップ 2種/アセンブリ基板/赤合金・青合金インゴット/シリコンウェハー系 4種 (boule はブロック)/モーター/コイル 3種/帆布・帆/化合物 6種/絞り板/ドライバー. 全種レシピ付き
* `frame`/`frame_motor` — フレームは面貼り付き骨組み (ホストブロックを掴む). モーターは RS 信号中 4 tick 毎に、接するフレーム網 + ホストブロック群を FACING 方向へ 1 ブロック瞬間移動 (BE は NBT 引継ぎ, 上限512, 行き先塞がり/移動不能時は中断)
* blulectricity — MJ ブリッジとして最小実装: `bt_battery` (MjBatteryItem, 24000MJ/64MJ-t 充電式), `bt_battery_box` (MjBatteryBlock LV 24000, 既存蓄電スロット機構を共用), `solar_panel` (Ic2GeneratorBlock SOLAR 共用で昼間発電), `multimeter` (MJ 蓄積表示ツール)
* `handsaw` + `microblock` — 手ノコ5種 (鉄 + 宝石4種). フルブロックを HALF に置換 + 残り半分をアイテム化, マイクロブロックは HALF→PANEL×2→COVER×2 に分割. 宝石ノコで boule ブロック → シリコン×8. マイクロブロック素材は BE 保持, アイテムは BLOCK_STATE + BLOCK_ENTITY_DATA コンポーネントで搬送 (通常 BlockItem 設置経路で反映)
* 実機検証 (testserver, RCON):
  * レバー → 赤ワイヤー powered=true, 隣接青/非接触赤は powered=false で色分離を確認
  * レバー → 緑ワイヤー → bundled ×2 → 緑ワイヤー: 末端 powered=true, BE `channels: 8192` (bit13=緑). 同色中継 + 他色は無影響
  * ic_gate: cells=[and(in12→out1), not(in2→out8)] 設定で out_north/out_east 両立, 南入力投入で out_east 消灯を確認 (複数論理1ブロック)
  * 手ノコ (carpet fake player): stone → HALF マイクロブロック置換 + 半分アイテム取得, HALF→PANEL 分割で {block_state:{size:panel}, block_entity_data:{material}} コンポーネント付きアイテム取得
  * frame_motor: レッドストーンブロック駆動で cobblestone + 天井フレームが 1 ブロック東へ移動
* 判明したこと:
  * 26.3 で `displayClientMessage` は `sendOverlayMessage` に改名, PushReaction の移動不能は `IMMOVEABLE`
  * FaceAttachedHorizontalDirectionalBlock 派生は FACE/FACING を自分で `createBlockStateDefinition` 登録必須 (忘れると登録時に getShape で死ぬ)
  * レシピ category に "tools" は存在しない (equipment を使う)
  * 稼働中サーバーへの jar 上書きは zip 破損 (invalid LOC header) でクラッシュ → 必ず停止→差替→起動
* 未解決・要判断:
  * bundled cable の絶縁ワイヤーは外部ソースを持たないと読めない設計 (ケーブル束自体は信号源に非ず, PR 準拠). redwire/bundled 間は直接ブリッジせず絶縁経由のみ
  * ic_gate セル編集 UI は無し (ゲート投入/ドライバー削除 + NBT 直接編集). 破壊時セルは消散
  * 絶縁ワイヤーのモデルは全色同形状で色テクスチャ差替のみ. 見た目のゲート構成表示は無し
  * blulectric 専用機器 (かまど/風力等) はスコープ外, MJ 共用実装のみ
  * マイクロブロックのマルチパート同居 (同一セル複数パーツ) は未対応
  * tools/gen_pr_assets.py でアセット約1350ファイルを生成 (再生成可)

## task-220 BCN LogisticsPipes (物流パイプ網)

* 結果: 実装済み — provider→request の網内自動搬送, モジュール挙動, remote_orderer 遠隔要求, 補給, 溶接台を testserver で実機確認
* 構成: `com.civilizationfrontier.bcn.lp` パッケージ. item_pipe の Transit に宛先/経路を持たせる拡張方式
  * `ItemPipeBlockEntity.Transit` — dest(BlockPos)/destFace/route を追加し chooseExit/handOff で宛先配送. destFace>=6 はパイプ内部バッファ配送 (加工材料)
  * `BcnPipeBlock.conn` — 同クラス限定から同流体種へ緩和し、item_pipe と物流パイプ、防水管と液体系パイプが接続するように
  * `LogisticsNetwork` — BFS で網列挙/最短経路/シンク解決/要求発行 (MjNetwork と同じ都度探索方針)
* 実装ブロック (全て `bcn:` 接頭辞, WorldlyContainer なしの NBT 駆動設定)
  * `pipe_basic_logistics` — フィルタ設定で回収ポイント (優先度30), 未設定なら Default Route (1)
  * `pipe_provider` — 隣接コンテナを供給源登録
  * `pipe_request` — `flt` NBT で発注. 網内供給源から destFace の隣接コンテナへ配送, 到達で発注残を減算
  * `pipe_supplier` — `flt` のアイテムを指定個数まで隣接コンテナに維持
  * `pipe_crafting` — 隣接バニラ Crafter のグリッドをレシピとして解決し、材料を網から取寄せて成果物を配送
  * `pipe_chassis_mk1..5` — モジュール 1,2,3,4,8 スロットの受け皿
  * `pipe_liquid_supplier` — 防水管網内の他タンクから指定液体を維持量まで配送
  * `soldering_station` — 材料9+燃料(鉄)+完成1 スロット, MJ 消費で nano_hopper/item_card/HUD 部品を組立
* モジュール (item) — item_sink/polymorphic/terminus/passive_supplier/provider/extractor/quick_sort/active_supplier/crafting/blank. chassis へ右クリック挿入, レンチで取出. フィルタはモジュールの custom_data `flt`
* `remote_orderer` — useOn で物流パイプに紐付け (custom_data `pipe`), 空中 use で反対側の手のアイテム or 保存 `req` を遠隔要求
* 動力: 全ロジパイプ/溶接台は MjReceiver (バッファ128/1024MJ). 発送2MJ/個, クラフト16MJ/回, 液体補給4MJ/回, 溶接320MJ/回
* 実機検証 (RCON): provider 64→56, request チェストへ8配送で発注完了 (16MJ消費). extractor モジュール挿しで 32→24 抽出→basic シンクへ配送. orderer で 4 個遠隔要求 (56→52, 17→21). supplier で鉄16維持 (64→48). 溶接台で hopper8+鉄1+320MJ→nano_hopper
* 判明したこと:
  * ItemPipeBlockEntity が WorldlyContainer のため HopperBlockEntity.getContainerAt が隣接パイプをコンテナ扱いする → attachedContainer は BcnPipeBlock を除外必須 (要求が常に0になる障害の原因だった)
  * 26.3 の displayClientMessage は sendOverlayMessage(Component) に改名, Recipe.assemble は引数1個 (registryAccess 不要)
  * 他タスクの進行中レシピ (handsaw/multimeter 系6件) がレジストリロードを落とす → 検証は jar から該当レシピのみ除去した仮パッチで実施 (ソースは未改変, 再ビルドで復活)
* 未解決・要判断:
  * LP 用 GUI 未実装 (要求/フィルタ/モジュール設定は NBT と右クリック投入で代用)
  * 物流パイプ BE に検証用 dbg/dbgs カウンタが残っている (デバッグ用途, 次回除去候補)
  * 加工パイプの衛星パイプ/速度チップ/LP変換機・ファイアウォール等の周辺要素は未実装
  * `data/bcn/recipe/rail_signal.json` の minecraft:redstone_dust 誤記 (task-201 からの既知件) は依然要修正

## task-218 BCN IC2 crops (支柱育種・農薬・飲料系)

* 結果: 実装済み — 支柱の作物成長・交差育種・酒樽発酵の tick 動作を testserver で実機確認 (プレイヤー操作パスはコードのみ)
* 構成: `com.civilizationfrontier.bcn.crop` パッケージ
  * `CropStickBlock` (crop_stick) — 耕地上限定 (canSurvive=FarmlandBlock). 空支柱へ支柱使用で cross=true の二重支柱. バニラ種や種袋で植え付け, 成熟時は素手右クリック収穫 (stage-3 に戻り持続), それ以外は簡易鑑定表示. 破壊で支柱ドロップ + BE 側から種袋 (未成熟40%/成熟25%) と収穫物
  * `CropStickBlockEntity` — crop/stage/GGR (growth,gain,resistance 1-31) + grow_progress + 栄養/保水/除草ストック (0-100, 20 tick 毎に減衰). 5 パラメータは 光量=sky light, 通気=周囲 3x3x3 空気率, 栄養=ストック+耕地湿潤, 保水=ストック+湿潤+水源数, 除草=ストック を各 0-20 で算出
  * 交差育種 — cross 支柱が隣接 4 方の成熟した通常支柱 2 株を親に子を発生 (0.15/20tick). 種は親継承 60% / 親最大 tier+1 以下への突然変異 25% / 雑草 15%. GGR は親平均±1 で 1-31
  * 雑草 — 栄養>=40 かつ除草 0 の空支柱に自然発生, 耐性<=20 かつ除草 0 の隣作物へ拡散. weed_ex 使用で枯死
  * `BcnCropType` — 12 種 (weed/wheat/pumpkin/carrot/potato/melon/sugarcane/beetroot/cocoa/nether_wart/coffee/hops). バニラ種をそのまま植え付け可 (GGR=1/1/1)
* アイテム
  * `crop_seed_bag` — 作物種+GGR+scanned をデータコンポーネント保持 (item.BcnComponents に crop_id/crop_growth/crop_gain/crop_resistance/crop_scanned を追加). 未鑑定はツールチップでステータス非表示
  * `crop_analyzer` — 支柱右クリックで種名/stage/GGR/5 パラメータを表示, 手に持って使用で反対手の種袋を鑑定
  * `fertilizer`(+栄養50) `hydrated_coal`(+保水50) `weed_ex`(+除草50, 雑草枯死) `grin_powder` `hops` `coffee_beans` `coffee_powder` `stone_mug`
* 飲料系
  * `BoozeBarrelBlock`+BE — 水バケツ注水後, 砂糖 3 でラム, 小麦 2+ホップ 1 で麦酒を仕込み (4 杯分). ferment_ticks を tick 毎加算し, 汲み出し時の熟度で産物分岐: ラム <24000=未発酵/以上=熟成, 麦酒 <6000 若い酒/<18000 ビール/<36000 エール/以上 暗黒物質X
  * マグ系 9 種 — Consumable コンポーネントで飲用効果 (未発酵rum/暗黒物質X は確率付き複数効果の近似, 熟成rum は耐性2+火炎耐性+力, コーヒー系は速度/加速に加え slowness/mining_fatigue 解除の覚醒効果), usingConvertsTo で空マグ返却
  * コーヒー加工線 — coffee_beans→coffee_powder (不定形), stone_mug+粉+水バケツ→コールドコーヒー, 精錬でブラックホットコーヒー, +砂糖+ミルクでホットコーヒー
* 実機検証 (testserver RCON, NBT 注入):
  * 水張り耕地上に setblock → BE 生成と NBT 正常を確認. 親株 wheat (4/3/2) と carrot (6/8/10) を成熟化すると cross 支柱に子 wheat (G6/G6/R6) が発生し stage 5 まで成長
  * growth 20 の小麦が約 2 分で stage 0→7 に成熟, ブロックステート growth=3 へ反映
  * 酒樽に brew=beer/servings=4 を注入 → ferment_ticks が tick 連動で増加 (2 分で 2411)
  * give で mug_beer/crop_analyzer/crop_seed_bag/fertilizer を取得 (lang 名も解決), `give Tester bcn:crop_seed_bag[bcn:crop_id="coffee",...]` でコンポーネントの往復保存を確認
  * loot spawn で crop_stick/booze_barrel のブロックルートを確認
* 判明したこと:
  * 本パックでは乾燥かつ未植付の耕地が dirt に自然復帰する → 支柱の検証は水張り耕地 (moisture>0) が必須. 乾燥耕地に支柱を置くと耕地が dirt 化して支柱もポップする
  * `minecraft:water_bottle` はアイテム ID として存在しない (ポーションは minecraft:potion+components) → コールドコーヒーの材料は water_bucket に変更
  * 26.3 の PushReaction は PUSH_PULL/PUSH/POPPED/IMMOVEABLE/IGNORE_ENTITY (DESTROY なし, 作物には POPPED)
  * ランダム飲用効果は ApplyStatusEffectsConsumeEffect の probability を効果毎に複数登録して "いずれか 1 つ" を近似できる
* 未解決・要判断:
  * プレイヤー必須パス (分析機の右クリック, 収穫, マグ汲み出し, 飲用効果) は RCON で実行不能なため未実機確認. コードパスとコンポーネント往復のみ検証済み
  * 共有ワークツリー上の他タスク WIP コード (lp/PR 系の未完成参照) で全体 gradle build が間欠的に失敗する. 検証 jar は WIP ファイルの一時退避でビルドし対象を復帰済み (jar 内の壊れた BcnGateBlock 中間版は javac 単体差替で修正)
  * rail_signal レシピの redstone_dust 誤記はソース側修正済みのため再ビルドで解消. 他タスクの壊れたリソース (handsaw 系 recipe の "tools" カテゴリ) はソース側も equipment に修正済み
  * GUI なし (鑑定表示はチャット, 樽状態は右クリック表示). crop_matron (農薬散布機), treetap, 足場, ラムの糖蜜工程 (extractor/canner), 麦酒の品質接頭語 (Watery/Strong 等) は未実装

## task-211 BCN silicon (laser/assembly/chipset/gate/robot)

* 結果: 実装済み — レーザー→精密作業台→チップ製造・BC 式ゲートの RS 制御抽出・ロボット最小 dock を実機確認
* 実装:
  * `assembly_table`/`AssemblyTableBlockEntity` — レーザー専用受電 (CAP 10000), 素材 12 スロット, `BcnProcessingRecipes.find("assembly_table", slots)` のデータ駆動レシピで energy>=cost 時に製作, 隣接コンテナへ搬出 (右クリック投入可)
  * `laser`/`LaserBlockEntity` — MjStorageBlockEntity (LV 受電・容量 500), 半径 5 の作業台へ ≤4 MJ/t 分配. `LaserBeamRenderer` で照射ライン描画 (26.3 render-state API)
  * チップセット 8 種 + `data/bcn/processing/assembly_table.json` で 8 レシピ定義 (JSON データ駆動)
  * `GateItem` (AND/OR ×5 材) — パイプ右クリックで装着, `ItemPipeBlockEntity.gate` 保存. 10 tick 毎に AND=信号ON時のみ/OR=信号OFF時のみに隣接コンテナから 1 抽出 (redwire 系とは別系統)
  * 派生作業台 4 種 (integration/charging/gate_copier/advanced_crafting) + `docking_station` は登録のみ (機能は別タスク)
  * `robot`/`RobotEntity` — dock 右クリックで配置, 最小の待機エンティティ (AI 無し). gate_mod 4 種 + redstone_board/crystal も登録
* 実機検証 (testserver, RCON):
  * laser 受電 760 MJ → 作業台へ 4 MJ/t 供給 → redstone×4 + 800 MJ で `chipset_redstone` 生成→チェスト搬出 (crafted_total=1)
  * gate=and のパイプは RS 無しで搬出 0 → redstone_block で cobble 搬出開始を確認
  * `summon bcn:robot` + dock ブロック配置で実体化を確認
* 判明したこと: 26.3 描画 API は `BlockEntityRenderer<T,S>` + `createRenderState/extractRenderState/submit` + `SubmitNodeCollector.submitCustomGeometry`, RenderType 定数は `rendertype.RenderTypes` 複数形
* 未解決・要判断:
  * ゲートの素材差別化 (条件スロット数・トリガ種別) 未実装 — 現状 AND=電源有 / OR=電源無の単一条件のみ
  * ゲート改造 4 種はアイテム登録のみ (装着/動作は別タスク). 派生作業台もスタブ
  * ロボットは登録+ dock 配置のみ. ビーム描画はクライアント側で未目視 (render 構造のみ実機確認不可)
