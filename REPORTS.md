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
