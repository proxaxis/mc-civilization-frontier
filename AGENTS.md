# AGENTS.md

## Markdown 記述ルール

このリポジトリ内の Markdown ファイルを書く・編集する時は、以下のルールに従うこと.

* 太字 `**` は基本使わない
* 中黒 `・` は `、` や `または` で代用する
* 句点 `。` は使用せず `. ` を使う
* 鉤括弧 `「」` は使用せず `""` を使う
* 閉じ括弧 `）` の後に `.` は使わない

## Minecraft クライアントの MCP 操作

ユーザーの Windows マシンで動作する Minecraft クライアントを、MCP ツール `mcp__minecraft__*` 経由で遠隔操作できる.

### 構成

* クライアント側: BCN mod (`bcn-0.1.0.jar`) がクライアント起動時に HTTP 制御エンドポイントを `0.0.0.0:25580` で待受する (`POST /call` に `{"tool": ..., "args": ...}` を投げる. `GET /ping` で疎通確認). サーバー側では動作しない
* ブリッジ: `/ws/tools/mc_bridge.py` が stdio MCP サーバーとして `.devin/mcp_config.local.json` に登録済み. 各ツール呼出を `http://192.168.2.248:25580/call` へ転送する
* 対象ホストは環境変数 `MC_HOST`, `MC_PORT` で変更可. `MC_TOKEN` とクライアント側 JVM 引数 `-Dbcn.mcp.token` を一致させれば簡易認証も可
* 無効化はクライアント側 JVM 引数 `-Dbcn.mcp=false`

### 利用可能ツール

* `status`: 座標, 視点, HP, 視線先ブロックまたはエンティティ, gamemode, 接続先サーバーを返す
* `screenshot`: 現在の視点を PNG で返す. 見た目確認の主手段
* `say`: チャット送信. `/` 始まりならコマンドとして実行される (`/give`, `/tp`, `/gamemode` 等)
* `look`: `yaw` (0=南, 90=西, -90=東, ±180=北) と `pitch` (-90=上, 90=下) で視点を設定
* `press`: キーを `ms` ミリ秒押して離す. 対象は `forward`, `back`, `left`, `right`, `jump`, `sneak`, `sprint`, `use`, `attack`, `drop`, `swap_offhand`, `pick_block`
* `key`: キーの押下状態を `down` 真偽値で明示制御. 連続移動向き
* `hotbar`: ホットバースロット 0 から 8 を選択
* `inventory`: インベントリ内容と選択スロットを返す

### 使い方の注意

* クライアントが未起動または未接続だとツールは `client unreachable` を返す
* ブロック設置は `hotbar` で対象を選び, `look` で狙い, `press` で `use` を押す手順
* 採掘は `attack` を長め (`ms` 数百以上) に押す
* GUI 操作 (チェストの中身を開く等) は未対応. コマンドは `say` で代替する
* 前提としてユーザー側で Windows ファイアウォールの受信 TCP 25580 許可が必要
