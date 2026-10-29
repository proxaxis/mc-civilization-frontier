# BCN 鉄道高度化 (時刻表ダイヤ・信号システム)

Improvement Minecarts (連結列車・かまど機関車・駅・RS 信号) の上に、時刻表ダイヤと閉塞信号による自律運行を提供する.

## 1. 信号システム

### ブロック `bcn:rail_signal`

ガードレールの脇に立てる閉塞信号. 信号は "facing" (列車の進入方向) と "aspect" (clear/stop) を持つ.

* 設置位置: 監視するレール(ガードレール)の真横・同じ高さ. facing = 列車が閉塞へ進入する向き
* ガードレール解決: facing から見て右側 (反時計回り) の隣接レールを優先. 無ければ左側
* 閉塞区間: ガードレールから facing 方向へレールを BFS 走査. 他の信号機に隣接するレールを区間境界とし最大 128 レール
* 占有判定: 区間内にいずれかのトロッコ(列車の一部でも)があれば STOP. 非在線なら CLEAR
* 方向性の停止: 赤の間にガードレール上を区間内方向へ動く先頭車両のみ停止する. 区間から出る列車は赤でも自由に通過するため、単線でもデッドロックしない
* 保持/解放: 停止時に車両の運動量とかまど車の推力を保存しゼロ化、位置をガードレールに固定する. 区間解放で保存値を復帰して再発車する
* RS 連動: STOP で全方向に信号強度 15 を出力. 側面のレッドストーンランプ等を直接駆動できる. ガードレール直下のランプへは給電しない (斜め位置のため). IM 素のランプ信号と併用する場合は全方向停止になる点に注意
* 状態は fails-closed. ガードレール未検出や区間ゼロの場合は STOP を表示する

### 実機確認済み

* 区間内駐車車両で両方向の信号が STOP になること
* 進入列車がガードレール上で保持され motion/push が保存・ゼロ化されること
* 区間解放で復帰発車すること、通過列車が区間内にいる間は追従列車へ STOP を維持すること
* 反対方向 (区間出側) の通過は赤でも止まらないこと

## 2. 時刻表ダイヤ

### モデル

* ワールド単位の `SavedData` (`bcn:railway`) に保持
* 路線 `TrainLine`: 名前、周期 period (デフォルト 2400 刻み)、停車場リスト
* 停車場 `Stop`: レール座標 + モード
  * `at <offset>`: 周期内の絶対発車時刻. 列車は次回の offset まで停車する
  * `dwell <ticks>`: 到着から ticks 後に発車する
* 列車割当: `im.root` (IM の列車 ID) から路線名への対応
* 時計: 26.3 の WorldClock (`world_clock/overworld`) の totalTicks. IM の駅停車は `im.wait` を予定発車までの残 tick に書き換える方式で、到発・復帰は IM 本家の処理に任せる

### コマンド `/bcn rail`

* `line create <name> [period]` / `line delete <name>` / `line list` / `line info <name>`
* `stop add <line> at <offset> [pos]` / `stop add <line> dwell <ticks> [pos]`
  * pos 省略時は近傍の IM 駅レール (直下が lapis/emerald/amethyst/gold/hopper) を自動検出
* `stop remove <line> <index>`
* `assign <line>` / `unassign`: 10 ブロック以内の最寄り先頭車両を対象にする
* `trains`: 管理列車の id・位置・路線・wait・zone・速度を列挙
* `time`: WorldClock の現在時刻表示

変更系コマンドは `Permissions.COMMANDS_GAMEMASTER` 要求.

## 3. 自律運行

* 推進は IM のかまど機関車 (furnace minecart) またはパワードレールに依存する. 信号・ダイヤ側は停止と発車のみを制御する
* 駅停車は IM 本体の zone 機構 (lapis/emerald/amethyst = 通常駅, gold = 終端駅, hopper+barrel = 貨物) を使う. ダイヤ管理対象は zone 1 と 4
* 割当列車が路線停車場に停まると `im.wait` を残り時間に固定し、発車時刻に 1 にして IM へ返す. 未登録の停車場は IM 標準の dwell で自然発車する
* IM の `im.cool` により発車直後の再検知は起きない

## 4. 実機検証の手順 (testserver)

RCON は `mc_civilization_frontier_testserver:25575` (devpassword).

* 区間占有: 信号間のレール上にトロッコを置くと両方向の signal aspect が `stop` になる
* 進入停止: `summon minecart ~ ~ ~ {Motion:[0.4,0,0]}` で進入方向から押すとガードレールで保持され BE の `held` に motion/push/pos が残る
* 解放: 占有車両を除去すると aspect が `clear` に戻り保持車両が発車する
* ダイヤ: `line create`、`stop add ... at <offset>`、`assign` の後、列車の `im.wait` が発車時刻まで減算されて発車する

## 5. 既知の限界

* ポイント (分岐) の自動切替は未対応. 経路は路線の停車場リストとして管理するレベル
* 未管理の単発トロッコは列車として扱われ、停止・発車は個別車両単位になる
* ガードレール上に先客が止まっている場合の追突は物理的に防げない (実鉄道同様、信号間隔の設計で回避する)
* `im.wait` 管理方式のため IM のスコアボード仕様に依存する
