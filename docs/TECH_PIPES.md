> [ガイド](./GUIDE.md) > [科学技術ガイド](./GUIDE_TECH.md) > 科学技術の輸送パイプ

# 科学技術の輸送パイプ

アイテム・液体・エネルギーの輸送パイプとパイプ周辺部品.

### Wooden Transport Pipe / 木の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Wooden Transport Pipe / 日本語: 木の輸送パイプ / Origin: `-` / New: `bcn:wooden_transport_pipe`
* レシピ: `Glass` x1 + `Wooden Planks` x2 -> `Wooden Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 搬出パイプ. 使用時には木エンジン必須. 入力がある度に、隣接した"内部にアイテム置き場を有するブロック"から アイテムを1個吸い出して、パイプラインに乗せる作用を持つ. レンチで右クリックすると、搬出口の切り替えが可能.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftWoodenTransportPipe.gif)

### Cobblestone Transport Pipe / 丸石の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Cobblestone Transport Pipe / 日本語: 丸石の輸送パイプ / Origin: `-` / New: `bcn:cobblestone_transport_pipe`
* レシピ: `Glass` x1 + `Cobblestone` x2 -> `Cobblestone Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ノーマルパイプその1. これといって特別な効果を持たない. 焼石パイプとは互いに連結せず、使い分けると複雑な配管が可能.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftCobblestoneTransportPipe.gif)

### Stone Transport Pipe / 焼石の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Stone Transport Pipe / 日本語: 焼石の輸送パイプ / Origin: `-` / New: `bcn:stone_transport_pipe`
* レシピ: `Glass` x1 + `Stone` x2 -> `Stone Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ノーマルパイプその2. 少しだけ速度保持効果が上. 金パイプで加速されたアイテムの速度を、丸石パイプの2倍の距離保つ. が、それでも金パイプから16ブロック進んだ所で減速が開始、 徐々に速度が落ちていき、60ブロック進んだ所で完全に元の速度に戻る.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftStoneTransportPipe.gif)

### Sandstone Transport Pipe / 砂岩の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Sandstone Transport Pipe / 日本語: 砂岩の輸送パイプ / Origin: `-` / New: `bcn:sandstone_transport_pipe`
* レシピ: `Glass` x1 + `Sandstone` x2 -> `Sandstone Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ノーマルパイプその3. 加速保持性能は丸石パイプと同等. アイテムパイプとだけ連結し、それ以外とは一切連結しないパイプ. 隣のチェストやかまどにパイプを繋げたくない時に使用するが、 アイテムパイプ全種と結合してしまうため、複雑な配管には向かない.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftSandstoneTransportPipe.gif)

### Iron Transport Pipe / 鉄の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Iron Transport Pipe / 日本語: 鉄の輸送パイプ / Origin: `-` / New: `bcn:iron_transport_pipe`
* レシピ: `Glass` x1 + `Iron Ingot` x2 -> `Iron Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 一方通行パイプ. 搬送ラインの分岐点ないし合流点で、多方向から入ってきたアイテムを 一つの方向にだけ送り出す作用を持つ. 設置時に覆いが無いのが出口側で、覆いの掛かった残り全てが入口側. レンチで右クリックする毎に、覆いの掛かっていない側の向きを変わる. また、レッドストーン入力すると搬出方向が切り替わる. これによって複雑なパイプラインが構築できる.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftIronTransportPipe.gif)

### Golden Transport Pipe / 金の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Golden Transport Pipe / 日本語: 金の輸送パイプ / Origin: `-` / New: `bcn:golden_transport_pipe`
* レシピ: `Glass` x1 + `Gold Ingot` x2 -> `Golden Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 加速パイプ. 通過アイテムの搬送速度を加速する. ただしある程度進むと徐々に減速、やがて元に戻ってしまうので、 高速を維持させたい場合は一定間隔で金パイプを設置する必要がある. v3.2.0以降はレッドストーン入力不要、通過アイテムを常に加速させる. また最遅から最速まで加速させるには3個並べたのを通す必要がある.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftGoldenTransportPipe.gif)

### Obsidian Transport Pipe / 黒曜石の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Obsidian Transport Pipe / 日本語: 黒曜石の輸送パイプ / Origin: `-` / New: `bcn:obsidian_transport_pipe`
* レシピ: `Glass` x1 + `Obsidian` x2 -> `Obsidian Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 吸引パイプ. 地面に落ちているアイテムを吸引し、搬送ラインに乗せる作用を持つ. 通常状態でも、設置マスに落ちてきたドロップアイテムは吸引する. 木エンジンの入力を受ける毎、僅かに吸引効果がアップ(範囲+1) 石＆鉄エンジンから入力を受けると、大幅に吸引効果アップ(範囲+4)
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftObsidianTransportPipe.gif)

### Emerald Transport Pipe / エメラルドの輸送パイプ / BC

* 実装状態: [ ]
* 名称: Emerald Transport Pipe / 日本語: エメラルドの輸送パイプ / Origin: `-` / New: `bcn:emerald_transport_pipe`
* レシピ: `Glass` x1 + `Emerald` x2 -> `Emerald Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 通常の木パイプにダイヤパイプのフィルター機能を足したようなパイプ. 右クリックで専用のGUIを開く事ができ、Filter欄に置いたアイテムのみを、 通常木パイプ同様、対象チェストの左上から順に搬出していく. Filter欄には9種類までアイテムを置く事が可能.
* 専用GUI: 有 — 搬出フィルタ設定
* 実装方法: 

![](assets/tech_items/bc_CraftEmeraldTransportPipe.gif)

### Diamond Transport Pipe / ダイヤモンドの輸送パイプ / BC

* 実装状態: [ ]
* 名称: Diamond Transport Pipe / 日本語: ダイヤモンドの輸送パイプ / Origin: `-` / New: `bcn:diamond_transport_pipe`
* レシピ: `Glass` x1 + `Diamond` x2 -> `Diamond Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 仕分けパイプ. 他のパイプに接して置くと、接合部位の色が全六色のどれかに変わる. 設置後のパイプを右クリックすると、色分けされた特殊なGUIが開く. あらかじめここで仕分けしたいブロックを指定して置くと、 通過アイテムが対応する色側出口へとそれぞれ自動で仕分けされる. アイテム未指定の色側出口には、指定外の全アイテムが流される.
* 専用GUI: 有 — 色別仕分けフィルタ
* 実装方法: 

![](assets/tech_items/bc_CraftDiamondTransportPipe.gif)

### Void Transport Pipe / 虚空の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Void Transport Pipe / 日本語: 虚空の輸送パイプ / Origin: `-` / New: `bcn:void_transport_pipe`
* レシピ: `Glass` x1 + `Ink Sac` x1 + `Redstone` x1 -> `Void Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ゴミ箱パイプ. このパイプの中央に入ったアイテムは全てデリートされる. 捌き切れない余剰アイテムの処分用に使う.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftVoidTransportPipe.gif)

### Stripes Transport Pipe / ストライプの輸送パイプ / BC

* 実装状態: [ ]
* 名称: Stripes Transport Pipe / 日本語: ストライプの輸送パイプ / Origin: `-` / New: `bcn:stripes_transport_pipe`
* レシピ: `Glass` x1 + `Ink Sac` x1 + `Yellow Dye` x1 / `Glass` x1 + `Gold Gear` x2 -> `Stripes Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設置 or 撤去回収パイプ. ブロックやアイテムが流れてくると、目の前の空間に設置や使用を試みる. 使用後のアイテムや、設置できないブロックはラインアウト. また、エンジン入力があると、自身の正面1マスにあるブロックを破壊し、 ドロップしたアイテムを搬送ラインに乗せる作用も持つ.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftStripesTransportPipe.png)

### Quartz Transport Pipe / 水晶の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Quartz Transport Pipe / 日本語: 水晶の輸送パイプ / Origin: `-` / New: `bcn:quartz_transport_pipe`
* レシピ: `Glass` x1 + `Nether Quartz Block` -> `Quartz Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ノーマルパイプその4. 石パイプより速度保持が上. 丸石、石パイプと接続できない.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftQuartzTransportPipe.gif)

### Lazuli Transport Pipe / ラピスラズリの輸送パイプ / BC

* 実装状態: [ ]
* 名称: Lazuli Transport Pipe / 日本語: ラピスラズリの輸送パイプ / Origin: `-` / New: `bcn:lazuli_transport_pipe`
* レシピ: `Glass` x1 + `Lapis Lazuli Block` x2 -> `Lazuli Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: このパイプを通過したアイテムに色を付ける. レンチ右クリック（Shift+右クリック）で付けられる色が変わる. 全16色
* 専用GUI: 有 — 色指定の振り分け
* 実装方法: 

![](assets/tech_items/bc_CraftLazuliTransportPipe.gif)

### Daizuli Transport Pipe / ダイズリの輸送パイプ / BC

* 実装状態: [ ]
* 名称: Daizuli Transport Pipe / 日本語: ダイズリの輸送パイプ / Origin: `-` / New: `bcn:daizuli_transport_pipe`
* レシピ: `Glass` x1 + `Lapis Lazuli Block` x1 + `Diamond` x1 -> `Daizuli Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設定した色のアイテムを通す. レンチ右クリックでパイプの向き変更、Shift+右クリックで色変更
* 専用GUI: 有 — 色指定の振り分け
* 実装方法: 

![](assets/tech_items/bc_CraftDaizuliTransportPipe.gif)

### Emzuli Transport Pipe / エメズリの輸送パイプ / BC

* 実装状態: [ ]
* 名称: Emzuli Transport Pipe / 日本語: エメズリの輸送パイプ / Origin: `-` / New: `bcn:emzuli_transport_pipe`
* レシピ: `Glass` x1 + `Lapis Lazuli Block` x1 + `Emerald` x1 -> `Emzuli Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: アイテムに色を付けつつインベントリから搬出する. インベントリへ設置後のパイプを右クリックするとGUIが開き、4種類の設定が可能. 設定後、パイプに設定した色と同じ色をゲート出力しながらエンジン出力を受けると、対応するアイテムに色を付けつつ搬出する. エンジンでの搬出はできない.
* 専用GUI: 有 — 搬出フィルタ設定
* 実装方法: 

![](assets/tech_items/bc_CraftEmzuliTransportPipe.gif)

### Clay Transport Pipe / 粘土の輸送パイプ / BC

* 実装状態: [ ]
* 名称: Clay Transport Pipe / 日本語: 粘土の輸送パイプ / Origin: `-` / New: `bcn:clay_transport_pipe`
* レシピ: `Glass` x1 + `Clay Block` x2 -> `Clay Transport Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 隣接した空きのあるインベントリを持ったブロックに優先的に挿入するパイプ. インベントリが埋まっている場合は通過する.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftClayTransportPipe.gif)

### Pipe Sealant / パイプの防水材 / BC

* 実装状態: [ ]
* 名称: Pipe Sealant / 日本語: パイプの防水材 / Origin: `buildcrafttransport:waterproof` / New: `bcn:pipe_sealant`
* レシピ: `Sealant` x1 -> `Pipe Sealant` x1
* レシピ配置図: `[不定形]`
* 概要: 作成に必要な緑の染料はサボテンを精製することで得られる. ただ従来のレシピでは砂漠バイオームが遠くにある人にとってキツいものだったのか、 BC6.4.0からコンフィグでスライムボールレシピを追加することが可能になった(ただしBC6.4.4まで正常に動作しない模様) 通常パイプから液体専用の各種パイプを作るのに必要.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftPipeSealant.png)

### Wooden Fluid Pipe / 木の防水パイプ / BC

* 実装状態: [ ]
* 名称: Wooden Fluid Pipe / 日本語: 木の防水パイプ / Origin: `-` / New: `bcn:wooden_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Wooden Transport Pipe` x1 -> `Wooden Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 基本的な使い方はアイテム用と同じで、木エンジン必須. もっぱら内部に液体貯蔵領域を持つブロックに対して使用する. （例：液体タンク、リファイナリー、鉄エンジン） ただしポンプには液体が自動搬出される関係で必要ない. 輸送速度:10mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftWoodenFluidPipe.gif)

### Cobblestone Fluid Pipe / 丸石の防水パイプ / BC

* 実装状態: [ ]
* 名称: Cobblestone Fluid Pipe / 日本語: 丸石の防水パイプ / Origin: `-` / New: `bcn:cobblestone_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Cobblestone Transport Pipe` x1 -> `Cobblestone Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 液体用のノーマルパイプその1. 輸送速度:10mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftCobblestoneFluidPipe.gif)

### Stone Fluid Pipe / 焼石の防水パイプ / BC

* 実装状態: [ ]
* 名称: Stone Fluid Pipe / 日本語: 焼石の防水パイプ / Origin: `-` / New: `bcn:stone_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Stone Transport Pipe` x1 -> `Stone Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 液体用のノーマルパイプその2. 丸石パイプの倍輸送が可能. 輸送速度:20mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftStoneFluidPipe.gif)

### Sandstone Fluid Pipe / 砂岩の防水パイプ / BC

* 実装状態: [ ]
* 名称: Sandstone Fluid Pipe / 日本語: 砂岩の防水パイプ / Origin: `-` / New: `bcn:sandstone_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Sandstone Transport Pipe` x1 -> `Sandstone Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: アイテム用の砂岩パイプと全く同じ特性＆使い方. 輸送速度:20mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftSandstoneFluidPipe.gif)

### Iron Fluid Pipe / 鉄の防水パイプ / BC

* 実装状態: [ ]
* 名称: Iron Fluid Pipe / 日本語: 鉄の防水パイプ / Origin: `-` / New: `bcn:iron_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Iron Transport Pipe` x1 -> `Iron Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: アイテム用の鉄パイプと全く同じ特性＆使い方. 輸送性能も丸石パイプと何ら変わらない. 輸送速度:40mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftIronFluidPipe.gif)

### Golden Fluid Pipe / 金の防水パイプ / BC

* 実装状態: [ ]
* 名称: Golden Fluid Pipe / 日本語: 金の防水パイプ / Origin: `-` / New: `bcn:golden_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Golden Transport Pipe` x1 -> `Golden Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 輸送速度が速いパイプ. 使う場合は全区間金パイプにする必要があり、 途中に一箇所でも木パイプや鉄パイプや砂岩パイプを使うと、 そこで液体が渋滞するので、結局液体を加速させた意味がなくなる. 輸送速度:80mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftGoldenFluidPipe.gif)

### Emerald Fluid Pipe / エメラルドの防水パイプ / BC

* 実装状態: [ ]
* 名称: Emerald Fluid Pipe / 日本語: エメラルドの防水パイプ / Origin: `-` / New: `bcn:emerald_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Emerald Transport Pipe` x1 -> `Emerald Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 使い方は木パイプと同じだが、液体の輸送速度が速い. 輸送速度:40mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftEmeraldFluidPipe.gif)

### Void Fluid Pipe / 虚空の防水パイプ / BC

* 実装状態: [ ]
* 名称: Void Fluid Pipe / 日本語: 虚空の防水パイプ / Origin: `-` / New: `bcn:void_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Void Transport Pipe` x1 -> `Void Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: アイテム用の虚空パイプと全く同じ特性＆使い方. この中に運び込まれた液体は全て除去される. 輸送速度:10mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftVoidFluidPipe.gif)

### Quartz Fluid Pipe / 水晶の防水パイプ / BC

* 実装状態: [ ]
* 名称: Quartz Fluid Pipe / 日本語: 水晶の防水パイプ / Origin: `-` / New: `bcn:quartz_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Quartz Transport Pipe` x1 -> `Quartz Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 基本液体パイプの最上位互換. 輸送速度:40mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftQuartzFluidPipe.gif)

### Diamond Fluid Pipe / ダイヤモンドの防水パイプ / BC

* 実装状態: [ ]
* 名称: Diamond Fluid Pipe / 日本語: ダイヤモンドの防水パイプ / Origin: `-` / New: `bcn:diamond_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Diamond Transport Pipe` x1 -> `Diamond Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: アイテム用ダイヤパイプの液体版. 何気に輸送速度が金の液体パイプと同じである. 輸送速度:80mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftDiamondFluidPipe.gif)

### Clay Fluid Pipe / 粘土の防水パイプ / BC

* 実装状態: [ ]
* 名称: Clay Fluid Pipe / 日本語: 粘土の防水パイプ / Origin: `-` / New: `bcn:clay_fluid_pipe`
* レシピ: `Pipe Sealant` x1 + `Clay Transport Pipe` x1 -> `Clay Fluid Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: アイテム用粘土パイプの液体版. 輸送速度:40mB/t
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftClayFluidPipe.gif)

### Wooden Conductive Pipe / 木の伝導パイプ / BC

* 実装状態: [ ]
* 名称: Wooden Conductive Pipe / 日本語: 木の伝導パイプ / Origin: `-` / New: `bcn:wooden_conductive_pipe`
* レシピ: `Redstone` x1 + `Wooden Transport Pipe` x1 -> `Wooden Conductive Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 石、鉄エンジンからエネルギーを受け取ると、 エネルギーをムラなく均分化し、安定した配給をする効果を持つパイプ. ただしこれ単体はエンジンからの受取口、入口としてのみ作用し、 エネルギーラインの始端にしか使えず、道中 or 末端部には使用不可. 320RF/tまで対応
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

<!-- 画像なし -->

### Cobblestone Kinesis Pipe / 丸石の伝導パイプ / BC

* 実装状態: [ ]
* 名称: Cobblestone Kinesis Pipe / 日本語: 丸石の伝導パイプ / Origin: `-` / New: `bcn:cobblestone_kinesis_pipe`
* レシピ: `Redstone` x1 + `Cobblestone Transport Pipe` x1 -> `Cobblestone Kinesis Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 木パイプからのエネルギーを各マシンに送り届ける役目のパイプ. エネルギーパイプラインの道中 or 末端部分に使用する. 80RF/tまで対応
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftCobblestoneKinesisPipe.png)

### Stone Kinesis Pipe / 焼石の伝導パイプ / BC

* 実装状態: [ ]
* 名称: Stone Kinesis Pipe / 日本語: 焼石の伝導パイプ / Origin: `-` / New: `bcn:stone_kinesis_pipe`
* レシピ: `Redstone` x1 + `Stone Transport Pipe` x1 -> `Stone Kinesis Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 木パイプからのエネルギーを各マシンに送り届ける役目のパイプ. エネルギーパイプラインの道中 or 末端部分に使用する. 160RF/tまで対応
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftCobblestoneKinesisPipe.png)

### Quartz Kinesis Pipe / 水晶の伝導パイプ / BC

* 実装状態: [ ]
* 名称: Quartz Kinesis Pipe / 日本語: 水晶の伝導パイプ / Origin: `-` / New: `bcn:quartz_kinesis_pipe`
* レシピ: `Redstone` x1 + `Quartz Transport Pipe` x1 -> `Quartz Kinesis Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: エネルギーパイプラインの道中 or 末端部分に使用する. 640RF/tまで対応
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftQuartzKinesisPipe.png)

### Iron Kinesis Pipe / 鉄の伝導パイプ / BC

* 実装状態: [ ]
* 名称: Iron Kinesis Pipe / 日本語: 鉄の伝導パイプ / Origin: `-` / New: `bcn:iron_kinesis_pipe`
* レシピ: `Redstone` x1 + `Iron Transport Pipe` -> `Iron Kinesis Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: エネルギーパイプラインの分岐 or末端に使用. 流す最大MJ/tを設定出来る. レンチ右クリックで変更 20RF/t～1280RF/tまで対応.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftIronKinesisPipe.png)

### Golden Kinesis Pipe / 金の伝導パイプ / BC

* 実装状態: [ ]
* 名称: Golden Kinesis Pipe / 日本語: 金の伝導パイプ / Origin: `-` / New: `bcn:golden_kinesis_pipe`
* レシピ: `Redstone` x1 + `Golden Transport Pipe` x1 -> `Golden Kinesis Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 木パイプからのエネルギーを各マシンに送り届ける役目のパイプ. エネルギーパイプラインの道中 or 末端部分に使用する. 2560RF/tまで対応
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

<!-- 画像なし -->

### Diamond Kinesis Pipe / ダイヤモンドの伝導パイプ / BC

* 実装状態: [ ]
* 名称: Diamond Kinesis Pipe / 日本語: ダイヤモンドの伝導パイプ / Origin: `-` / New: `bcn:diamond_kinesis_pipe`
* レシピ: `Redstone` x1 + `Diamond Transport Pipe` x1 -> `Diamond Kinesis Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 木パイプからのエネルギーを各マシンに送り届ける役目のパイプ. エネルギーパイプラインの道中 or 末端部分に使用する. 10240RF/tまで対応
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_CraftDiamondKinesisPipe.png)

### Cobblestone Structure Pipe / 丸石の構造パイプ / BC

* 実装状態: [ ]
* 名称: Cobblestone Structure Pipe / 日本語: 丸石の構造パイプ / Origin: `-` / New: `bcn:cobblestone_structure_pipe`
* レシピ: `Cobblestone` x2 + `Gravel` x1 -> `Cobblestone Structure Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 各種ゲート設置用のダミーパイプであり、アイテム搬送能力はない. どのパイプとも接続しないが、このパイプに取り付けたパイプワイヤーは、 他のどのパイプのパイプワイヤーとも接続する特性を持つ. また、精密作業台で外装パネルを作成する場合、大量に必要になる.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: 

![](assets/tech_items/bc_Cobblestone_Structure_Pipe.png)

### Pipe Plug / パイプ栓 / BC

* 実装状態: [ ]
* 名称: Pipe Plug / 日本語: パイプ栓 / Origin: `-` / New: `bcn:pipe_plug`
* レシピ: `Cobblestone Structure Pipe` x1 -> `Pipe Plug` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 正確にはパイプではなく、外装板のようにパイプに取り付けるアイテム. 外装板と共存可能. この栓が取り付けられた方向へは、どんなパイプ、マシンとも接続しない. 外装板同様、パイプ栓を持った状態でShift+右クリックすれば撤去可能だが、 本来接続可能なものが既に置かれている方向へは設置、撤去共に行えない.
* 専用GUI: 無
* 実装方法: 

![](assets/tech_items/bc_CraftPipePlug.png)

### Lens / レンズ / BC

* 実装状態: [ ]
* 名称: Lens / 日本語: レンズ / Origin: `-` / New: `bcn:lens`
* レシピ: `Dye` x1 + `Glass` x1 -> `Lens` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: パイプの接続部分に取り付けることによってラピスラズリパイプと同様にアイテムに色を付けることができる. アイテムパイプであれば何にでも取り付けることが可能なため、細かい配管に有利である.
* 専用GUI: 有 — 透過フィルタ設定
* 実装方法: 

<!-- 画像なし -->

### Filter / フィルター / BC

* 実装状態: [ ]
* 名称: Filter / 日本語: フィルター / Origin: `-` / New: `bcn:filter`
* レシピ: `Dye` x1 + `Glass` x1 + `Iron Bars` x1 -> `Filter` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: パイプの接続部分に取り付けることによってダイズリパイプと同様に適応した色のついたアイテムしか通過させなくなる. アイテムパイプであれば何にでも取り付けることが可能なため、細かい配管に有利である.
* 専用GUI: 有 — フィルタ条件設定
* 実装方法: 

<!-- 画像なし -->

### Filtered Buffer / 選別緩衝器 / BC

* 実装状態: [ ]
* 名称: Filtered Buffer / 日本語: 選別緩衝器 / Origin: `buildcrafttransport:filtered_buffer` / New: `bcn:filtered_buffer`
* レシピ: `Wooden Planks` x6 + `Pipe Diamond Item` x1 + `Chest` x1 + `Piston` x1 -> `Filtered Buffer` x1
* レシピ配置図: `ABA` / `ACA` / `ADA`
* 概要: v3.7.1から追加 アイテムの一斉搬入をしたい場合に使用する. ゲートが必要.
* 専用GUI: 有 — 通過フィルタの設定
* 実装方法: 

![](assets/tech_items/bc_CraftFilteredBuffer.png)

### Red Pipe Wire / 赤ワイヤー / BC

* 実装状態: [ ]
* 名称: Red Pipe Wire / 日本語: 赤ワイヤー / Origin: `-` / New: `bcn:red_pipe_wire`
* レシピ: `Red Pipe Wire` x8 + `Iron Ingot` x1 + `Red Dye` x1 -> `Red Pipe Wire` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 1つのパイプに4種類取り付けられるワイヤー. パイプの四隅に敷設する形で、ゲートと併用しての信号伝達に使用する. これら4色のワイヤー信号は対ゲート限定の信号であり、 ドア、ピストン、マシンのような、ゲート以外の装置への動力には使えない. 赤のワイヤーは鉄ゲート以上、青のワイヤーは金ゲート以上、 緑＆黄のワイヤーはダイヤゲート以上でないと送受信を取り扱えない.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: style='vertical-align:MIDDLE;'|赤石粉1 + 鉄インゴット1 + 赤の染料1 = 赤ワイヤー85000RF（6.25秒/青1本） [gate] / style='vertical-align:MIDDLE;'|赤石チップ1 + 赤ワイヤー1 = 赤石ゲート1100000RF（250秒/青1本）特定条件が満たされた場合のトリガー機構として機能する検知ゲート。パイプを設置した後、更にそれに被せるようにして取り付ける。アイテム用・液体用・エネルギー用、全てのパイプに対し [gate]

![](assets/tech_items/bc_AssemRedWire.png)

### Blue Pipe Wire / 青ワイヤー / BC

* 実装状態: [ ]
* 名称: Blue Pipe Wire / 日本語: 青ワイヤー / Origin: `-` / New: `bcn:blue_pipe_wire`
* レシピ: `Blue Pipe Wire` x8 + `Iron Ingot` x1 + `Lapis Lazuli` x1 -> `Blue Pipe Wire` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 1つのパイプに4種類取り付けられるワイヤー. パイプの四隅に敷設する形で、ゲートと併用しての信号伝達に使用する. これら4色のワイヤー信号は対ゲート限定の信号であり、 ドア、ピストン、マシンのような、ゲート以外の装置への動力には使えない. 赤のワイヤーは鉄ゲート以上、青のワイヤーは金ゲート以上、 緑＆黄のワイヤーはダイヤゲート以上でないと送受信を取り扱えない.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: style='vertical-align:MIDDLE;'|赤石粉1 + 鉄インゴット1 + ラピスラズリ1 = 青ワイヤー85000RF（6.25秒/青1本） [gate] / style='vertical-align:MIDDLE;'|水晶チップ1 + 赤ワイヤー1 + 青ワイヤー1 + 緑ワイヤー1 = エメラルドゲート1600000RF（1500秒/青1本）設定できる条件の数が2個と非常に少ないが、エメラルドほどではないものの条件が細かく設定できるゲート。 [gate]

![](assets/tech_items/bc_AssemBlueWire.png)

### Green Pipe Wire / 緑ワイヤー / BC

* 実装状態: [ ]
* 名称: Green Pipe Wire / 日本語: 緑ワイヤー / Origin: `-` / New: `bcn:green_pipe_wire`
* レシピ: `Green Pipe Wire` x8 + `Iron Ingot` x1 + `Green Dye` x1 -> `Green Pipe Wire` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 1つのパイプに4種類取り付けられるワイヤー. パイプの四隅に敷設する形で、ゲートと併用しての信号伝達に使用する. これら4色のワイヤー信号は対ゲート限定の信号であり、 ドア、ピストン、マシンのような、ゲート以外の装置への動力には使えない. 赤のワイヤーは鉄ゲート以上、青のワイヤーは金ゲート以上、 緑＆黄のワイヤーはダイヤゲート以上でないと送受信を取り扱えない.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: style='vertical-align:MIDDLE;'|赤石粉1 + 鉄インゴット1 + 緑の染料1 = 緑ワイヤー85000RF（6.25秒/青1本） [gate] / style='vertical-align:MIDDLE;'|水晶チップ1 + 赤ワイヤー1 + 青ワイヤー1 + 緑ワイヤー1 = エメラルドゲート1600000RF（1500秒/青1本）設定できる条件の数が2個と非常に少ないが、エメラルドほどではないものの条件が細かく設定できるゲート。 [gate]

![](assets/tech_items/bc_AssemGreenWire.png)

### Yellow Pipe Wire / 黄ワイヤー / BC

* 実装状態: [ ]
* 名称: Yellow Pipe Wire / 日本語: 黄ワイヤー / Origin: `-` / New: `bcn:yellow_pipe_wire`
* レシピ: `Yellow Pipe Wire` x8 + `Iron Ingot` x1 + `Yellow Dye` x1 -> `Yellow Pipe Wire` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 1つのパイプに4種類取り付けられるワイヤー. パイプの四隅に敷設する形で、ゲートと併用しての信号伝達に使用する. これら4色のワイヤー信号は対ゲート限定の信号であり、 ドア、ピストン、マシンのような、ゲート以外の装置への動力には使えない. 赤のワイヤーは鉄ゲート以上、青のワイヤーは金ゲート以上、 緑＆黄のワイヤーはダイヤゲート以上でないと送受信を取り扱えない.
* 専用GUI: 無 — 輸送のみ(一部は接続フィルタ設定あり)
* 実装方法: style='vertical-align:MIDDLE;'|赤石粉1 + 鉄インゴット1 + 黄の染料1 = 黄ワイヤー85000RF（6.25秒/青1本） [gate] / style='vertical-align:MIDDLE;background-color:lightblue;'|黄ワイヤー信号を出力(Yellow Pipe Signal) [gate]

![](assets/tech_items/bc_AssemYellowWire.png)

### Facade: xxxx / 外装パネル / BC

* 実装状態: [ ]
* 名称: Facade: xxxx / 日本語: 外装パネル / Origin: `-` / New: `bcn:facade_xxxx`
* レシピ: `Facade: xxxx` x6 + `Cobblestone Structure Pipe` x3 -> `Facade: xxxx` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: パイプを覆い隠す為のハリボテパネル. 様々なブロックで作成可能. 厚さが1/16だがパイプの各面にしか取り付けられない. v3.2.0pre9で本物のブロックと同じ実体＆当たり判定も獲得. Shiftキーを押しながら同じパネルを持ってパイプをクリックすると、パネルだけを外せる.
* 専用GUI: 無
* 実装方法: 

<!-- 画像なし -->

### Mining Pipe / 採掘用パイプ / IC2

* 実装状態: [ ]
* 名称: Mining Pipe / 日本語: 採掘用パイプ / Origin: `-` / New: `bcn:mining_pipe`
* レシピ: `Refined Iron` x6 + `Treetap` x1 -> `Mining Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 掘削機に入れて使う. 再回収も可能.
* 専用GUI: 無
* 実装方法: 

![](assets/tech_items/ic2_blockMiningPipe.png)

### Filter / 選別配送器、フィルター / PR

* 実装状態: [ ]
* 名称: Filter / 日本語: 選別配送器、フィルター / Origin: `-` / New: `bcn:filter`
* レシピ: `Piston` + `Cobblestone` x5 + `Gold Ingot` x2 + `Infused Silicon` -> `Filter` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 配送器(フィルター機能付き) ※レシピは金 設置時は受取口とプレイヤーが向き合う形で、全6方向を向かせて設置可能. 上記の配送器(Transposer)の完全上位互換で、 右クリック時のGUIを持ち、フィルター機能を有す以外に1スタック一度に引き出すことができ、また取り出したアイテムに色をつけることもできる. ただしこちらの方が作成コストが割高で量産しにくい. 詳細はインターフェイスの項を参照のこと.
* 専用GUI: 無
* 実装方法: 

![](assets/tech_items/pr_CraftFilter.png)

### Basic Logistics Pipe / 基本パイプ / LP

* 実装状態: [ ]
* 名称: Basic Logistics Pipe / 日本語: 基本パイプ / Origin: `logisticspipes:pipe_basic` / New: `bcn:basic_logistics_pipe`
* レシピ: `Chip Fpga` x1 + `Pipe Transport Basic` x1 -> `Basic Logistics Pipe` x1
* レシピ配置図: `A` / `B`
* 概要: 最も基本的なロジパイプ. 他のロジパイプの素材にもなる. GUIでアイテム指定しておく事でそのアイテムの回収ポイントになり、 隣接して回収物搬入用のインベントリ内包ブロックを設置しておくと、 抽出モジュールの発送物や、加工パイプの余剰物など、 発送側に宛先指定されなかったアイテムの受け皿として機能する.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: ロジスティクスパイプの中でも特に「基本パイプ」としか接続しないが、ネットワークのどこか一箇所に設置されていれば、ネットワーク内のロジスティクスパイプ全体にLPを供給する。 [detail] / 変換機は、「エネルギーパイプ」および「ケーブル」とは6面全てで連結し、どの面からでもEU(MJ)を受け取れる。「基本パイプ」とは側面4面でしか連結できず、上面および下面では繋がらない。「基本パイプ以外の残り全てのロジスティクスパイプ」とは連結すらしない。物流制御パイプMk2でも駄目。 [detail]

![](assets/tech_items/lp_BasicPipe.gif)

### Provider Logistics Pipe / 供給パイプ / LP

* 実装状態: [ ]
* 名称: Provider Logistics Pipe / 日本語: 供給パイプ / Origin: `-` / New: `bcn:provider_logistics_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Glowstone Dust` x1 / `Basic Logistics Pipe` x1 + `Gold Gear` x1 + `Redstone` x1 / `Basic Logistics Pipe` x1 + `Golden Chipset` x1 + `Redstone Torch` x1 -> `Provider Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: このパイプに隣接するインベントリ内包ブロックが、加工、加工補佐、 要求、補給パイプからアイテム取寄先として認識される. 逆に言えば、チェストetc.のインベントリ内包ブロックは、 このパイプに隣接していないと内容物がネットワークに登録されない. 内部にアイテム置き場を有する全ブロックが認識対象になる. Dev571にてコストが増加、大量生産には手間がかかるように.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: style='background-color:lightblue;'|供給パイプ Mk1 [detail] / style='background-color:lightblue;'|供給パイプ Mk2 [detail]

![](assets/tech_items/lp_ProviderPipe.gif)

### Provider Logistics Pipe Mk2 / 供給パイプMk2 / LP

* 実装状態: [ ]
* 名称: Provider Logistics Pipe Mk2 / 日本語: 供給パイプMk2 / Origin: `-` / New: `bcn:provider_logistics_pipe_mk2`
* レシピ: `Provider Logistics Pipe` x1 + `Diamond Chipset` x1 -> `Provider Logistics Pipe Mk2` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 6tick毎に1回、16個ずつ（秒間50個）の速さでアイテムを発送する. BuildCraft3.2.0以前ではデフォルト設定では無印の供給パイプと性能が変わらず作るだけ無駄だった. ただしBuildCraft3.2.0以前であってもBCのConfigでI:pipes.maxItems=100の値を1000以上に書き換えると、 （6tickに1回、最大16個ずつのアイテムをスタック発送、秒間50個） の高速モードでアイテムを発送するようになる.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: style='vertical-align:MIDDLE;background-color:lightblue;'|供給パイプ供給パイプMk2供給モジュール供給モジュールMk2 [detail] / style='vertical-align:MIDDLE;background-color:lightblue;'|供給パイプMk2(Provider Logistics Pipe Mk2) [detail]

![](assets/tech_items/lp_ProviderPipeMk2.gif)

### Request Logistics Pipe / 要求パイプ / LP

* 実装状態: [ ]
* 名称: Request Logistics Pipe / 日本語: 要求パイプ / Origin: `-` / New: `bcn:request_logistics_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Golden Chipset` x2 / `Basic Logistics Pipe` x1 + `Golden Chipset` x1 + `Iron Gear` x1 -> `Request Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ネットワーク全体から、アイテムを任意で取り寄せるのに使う. （ただし事前に供給、加工パイプetc.を使っての取寄先の登録が必要） 任意のアイテムを任意の個数、プレイヤーの思い通りに引き出せる一方、 要求の毎にいちいちレンチで右クリックしてGUIを開き、 要求アイテムの種類＆個数を指定しなければならない煩雑さがある.
* 専用GUI: 有 — 要求画面
* 実装方法: 

![](assets/tech_items/lp_RequestPipe.gif)

### Request Logistics Pipe Mk2 / 要求パイプMk2 / LP

* 実装状態: [ ]
* 名称: Request Logistics Pipe Mk2 / 日本語: 要求パイプMk2 / Origin: `-` / New: `bcn:request_logistics_pipe_mk2`
* レシピ: `Request Logistics Pipe` x1 + `Diamond Chipset` x1 / `Request Logistics Pipe` x1 + `Diamond Gear` x1 + `Redstone` x1 -> `Request Logistics Pipe Mk2` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 上記"要求パイプ"の強化版. GUIを開くのにレンチが要らず、右クリックするだけで開けるようになった. また、後述する"リクエストディスク"の利用も可能となり、 複数アイテムの取り寄せetc.の煩雑な作業を一気に簡略化可能となった. 更にThaumcraft3が導入されている場合、選択したアイテムの相がGUI左上に表示されるようになる. Dev571より、歯車使用レシピでは赤石粉も必要に.
* 専用GUI: 有 — 要求画面
* 実装方法: 

![](assets/tech_items/lp_RequestPipeMk2.gif)

### Remote Orderer Logistics Pipe / 遠隔要求パイプ / LP

* 実装状態: [ ]
* 名称: Remote Orderer Logistics Pipe / 日本語: 遠隔要求パイプ / Origin: `-` / New: `bcn:remote_orderer_logistics_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Ender Pearl` x1 / `Basic Logistics Pipe` x1 + `Ender Pearl` x1 + `Redstone` x1 -> `Remote Orderer Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: リモートオーダーで右クリックすると、その遠隔要求パイプを記憶する. 記憶済みリモートオーダーで何も無い空間を右クリックすると、 どこからでもその遠隔要求パイプに対して取り寄せ実行命令を出せる. 普通にプレイする範囲ではこれを作る必要も使う出番もない. が、遠隔共有チェスト系のMODと組み合わせると反則的な便利さとなる.
* 専用GUI: 有 — 遠隔発注画面
* 実装方法: 

<!-- 画像なし -->

### Supplier Logistics Pipe / 補給パイプ / LP

* 実装状態: [ ]
* 名称: Supplier Logistics Pipe / 日本語: 補給パイプ / Origin: `-` / New: `bcn:supplier_logistics_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Lapis Lazuli` x2 -> `Supplier Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: GUI内でアイテムとその個数を設定すると、 隣接するインベントリ内包ブロックに対し、指定アイテムが指定個数分、 常にストックされるようなアイテムの自動補給が行われる. が、2つ以上のインベントリに同時に隣接していると正常動作しないので、 必ず"1インベントリに1補給パイプ"の比率で配管する必要がある. 上記の補給パイプの液体版. BCの鉄エンジン、IC2の地熱発電機、FFMのバイオマス発酵槽など、 容器を使った液体投入スロットを持つ装置の隣に設置、 バケツ、ガラス瓶、セル、カン、カプセルを使っての液体自動補給を行う. 補給は、液体が常にバケツ(指定個数)杯以上になるように行われる. BCのビルダー専用の補給パイプ. ロジパイプの中では珍しく、レンチで右クリックした時に開くGUIを持たない. ブループリントを読み込ませたビルダーに隣接させると、 ビルダーが要求するアイテムをネットワーク内から自動で取り寄せる. （要するに加工は行わない） Dev632の時点ではビルダー凍結に伴い?消滅しており、その後ビルダー復帰後も復活していない.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

![](assets/tech_items/lp_SupplierPipe.png)

### Crafting Logistics Pipe / 加工パイプ / LP

* 実装状態: [ ]
* 名称: Crafting Logistics Pipe / 日本語: 加工パイプ / Origin: `-` / New: `bcn:crafting_logistics_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Glowstone Dust` x2 / `Basic Logistics Pipe` x1 + `Redstone` x1 + `Stone Gear` -> `Crafting Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: バニラのかまど、BCの全自動作業台、IC2の粉砕機などに隣接設置. 原材料のみから加工を経つつ、加工後のアイテムを直接取り寄せられる. かまどのように燃料を同時搬入する必要のある装置や、水バケツetc.のスタック不可アイテムを原材料に使った加工を行うには、後述の"加工補佐パイプ"との併用が必須になる. BCの全自動作業台の仕様変更とロジスティクス作業台の登場により、 スタック付加アイテムを原材料に使う場合でも大量発注しない限り 加工補佐パイプは必要なくなった. それでもかまどでの作業などでは加工補佐パイプとの併用を行った方が良いことが多々ある.
* 専用GUI: 有 — クラフト設定
* 実装方法: 

<!-- 画像なし -->

### Crafting Logistics Pipe Mk2 / 加工パイプMk2 / LP

* 実装状態: [ ]
* 名称: Crafting Logistics Pipe Mk2 / 日本語: 加工パイプMk2 / Origin: `-` / New: `bcn:crafting_logistics_pipe_mk2`
* レシピ: `Crafting Logistics Pipe` x1 + `Golden Chipset` x1 / `Crafting Logistics Pipe` x1 + `Gold Gear` x1 + `Redstone` x1 -> `Crafting Logistics Pipe Mk2` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 上記"加工パイプ"の強化版. 全自動作業台と併用時、 無印の加工パイプが作成されたアイテムを1個ずつ発送するのに対し、 こちらは作成されたアイテムを作成毎にスタックされたまま発送する. 全自動作業台以外には使えない上、正直あまり差を感じられない.
* 専用GUI: 有 — クラフト設定
* 実装方法: 

<!-- 画像なし -->

### Crafting Logistics Pipe Mk3 / 加工パイプMk3 / LP

* 実装状態: [ ]
* 名称: Crafting Logistics Pipe Mk3 / 日本語: 加工パイプMk3 / Origin: `-` / New: `bcn:crafting_logistics_pipe_mk3`
* レシピ: `Crafting Logistics Pipe` x21 + `Nano Hopper` x1 + `Diamond Chipset` x1 + `Iron Ingot` x1 -> `Crafting Logistics Pipe Mk3` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 32スロット分の隠しインベントリ(視認不可)を内蔵する加工パイプ. そこで一時待機させる事で、かまど等の原料欄が1スロットの装置でも、 1種64個以上が同時に流れ込んだ際のアイテム溢れが起こらなくなる. 継続して加工パイプMk2のスタック単位での搬出機能も持つ.
* 専用GUI: 有 — クラフト設定
* 実装方法: 

<!-- 画像なし -->

### Satellite Logistics Pipe / 加工補佐パイプ / LP

* 実装状態: [ ]
* 名称: Satellite Logistics Pipe / 日本語: 加工補佐パイプ / Origin: `-` / New: `bcn:satellite_logistics_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Redstone` x2 -> `Satellite Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: "加工パイプ"、"加工パイプMk2"の子機として働くパイプ. 子機側で設定したIDを、親機側からも指定してやる事で初めて動作する. 親機にあたる"加工パイプ"へのリクエストが発生した際、指定アイテムを指定個数、連動して隣接するインベントリへと取り寄せる. （全自動作業台が隣接チェストの中身も材料と認識する仕組みを利用） 全自動作業台のリメイクとロジスティクス作業台の追加により必要な場面は減ったが、かまどへの燃料の同時供給や大量精錬時にホッパーへ材料を送り込むなどまだまだ活躍の場はある.
* 専用GUI: 有 — 衛星先設定
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Basic Pipe / 液体基本パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Basic Pipe / 日本語: 液体基本パイプ / Origin: `-` / New: `bcn:logistics_liquid_basic_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Pipe Sealant` x6 + `Empty Bucket` x2 / `Basic Logistics Pipe` x1 + `Pipe Sealant` x1 + `Empty Bucket` x1 -> `Logistics Liquid Basic Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: GUIにて液体を指定すると、その液体を回収して隣接する機械、タンクに搬入する液体系の基本パイプ. 内部ストレージは存在しないが、機械、タンクとの間が通常の液体パイプ扱いなのか、1方向につき4杯分まで液体を残留させられる. デフォルトルート的なものは存在せず、指定無しの場合はどの液体も回収しない. 他液体系パイプの材料にもなる. 通常の基本パイプと違い、LP変換機とは接続しない.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Connector Pipe / 液体接続パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Connector Pipe / 日本語: 液体接続パイプ / Origin: `-` / New: `bcn:logistics_liquid_connector_pipe`
* レシピ: `Golden Fluid Pipe` x1 + `Gold Ingot` x2 + `Redstone Torch` x2 + `Glass` x4 / `Logistics Liquid Basic Pipe` x1 + `Golden Fluid Pipe` x1 + `Gold Ingot` x2 + `Glass` x2 -> `Logistics Liquid Connector Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 通常の液体パイプと液体系ロジパイプを接続するためのパイプ. 動作自体は通常の液体パイプと何ら変わりない（アップグレーダーやネットワークモニターも使用不可） 液体系ロジパイプは液体をアイテムとして輸送するため通常パイプ扱いであり、通常の液体パイプとは接続できない. そのため、液体パイプと接続するには液体パイプとも液体系ロジパイプとも接続できるこのパイプが必須となる. ほぼ液体挿入パイプ専用.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Insertion Pipe / 液体挿入パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Insertion Pipe / 日本語: 液体挿入パイプ / Origin: `-` / New: `bcn:logistics_liquid_insertion_pipe`
* レシピ: `Logistics Liquid Basic Pipe` x1 + `Glass Bottle` x4 -> `Logistics Liquid Insertion Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 流れてきた液体をアイテム化して輸送するパイプ. 別方向からの搬入であれば複数の液体を同時に扱えるため、液体管理が容易に. 液体の輸送先がない場合は各方向からの接続パイプ内に液体が残留し、その方向のパイプが破壊された場合は内部に蓄積される（同時に1種類のみ） ほぼ液体接続パイプ必須.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Provider Pipe / 液体供給パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Provider Pipe / 日本語: 液体供給パイプ / Origin: `-` / New: `bcn:logistics_liquid_provider_pipe`
* レシピ: `Logistics Liquid Basic Pipe` x1 + `Glass Bottle` x1 -> `Logistics Liquid Provider Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 供給パイプの液体版. 用途も、リクエストがあった際に隣接する機械、タンクから液体を搬出と通常の供給パイプ同様である.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Request Pipe / 液体要求パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Request Pipe / 日本語: 液体要求パイプ / Origin: `-` / New: `bcn:logistics_liquid_request_pipe`
* レシピ: `Logistics Liquid Basic Pipe` x1 + `Glass Bottle` x2 -> `Logistics Liquid Request Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 要求パイプの液体版. 用途も、隣接する機械、タンクへ液体を取り寄せと通常の要求パイプ同様である. 取り寄せ量は1mbから選択可能で、＋＋（－－）ボタンは1000mb単位の、 ＋＋＋（－－－）ボタンは16000mb（タンク1基分）単位の選択となる.
* 専用GUI: 有 — 要求画面
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Extractor Pipe / 液体抽出パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Extractor Pipe / 日本語: 液体抽出パイプ / Origin: `-` / New: `bcn:logistics_liquid_extractor_pipe`
* レシピ: `Logistics Liquid Insertion Pipe` + `Wooden Fluid Pipe` x1 -> `Logistics Liquid Extractor Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 抽出パイプの液体版. 用途も、隣接する機械、タンクから液体を抽出と通常の抽出パイプ同様である.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### Logistics Liquid Satellite Pipe / 液体補佐パイプ / LP

* 実装状態: [ ]
* 名称: Logistics Liquid Satellite Pipe / 日本語: 液体補佐パイプ / Origin: `-` / New: `bcn:logistics_liquid_satellite_pipe`
* レシピ: `Logistics Liquid Basic Pipe` x1 + `Redstone` x2 -> `Logistics Liquid Satellite Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 加工補佐パイプの液体版. なのだが、現状SatelliteIDを指定する液体ロジパイプが他に存在しない模様（~~通常の加工パイプとは連携しない~~） 通常の加工パイプ(モジュール)には液体加工補佐チップをつけることで使える.
* 専用GUI: 有 — 衛星先設定
* 実装方法: 

<!-- 画像なし -->

### Logistics Chassis Pipe Mk1 / 物流制御パイプ Mk1 / LP

* 実装状態: [ ]
* 名称: Logistics Chassis Pipe Mk1 / 日本語: 物流制御パイプ Mk1 / Origin: `-` / New: `bcn:logistics_chassis_pipe_mk1`
* レシピ: `Basic Logistics Pipe` x1 + `Redstone` x8 / `Redstone Chipset` x3 / `Basic Logistics Pipe` x1 + `Iron Ingot` x2 + `Redstone` x3 / `Redstone Chipset` x1 -> `Logistics Chassis Pipe Mk1` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設置後に下に挙げた様々なモジュール(＝ソフトウェア)をセットする事で、 様々な機能を組み合わせつつ複合的に持たせる事ができるが、 これ自体は単なるモジュールの容れ物(＝ハードウェア)に過ぎない. Mk1からMk5まで存在し、 Mk1～4はそれぞれ1～4個のモジュールを同時にセットできる. Mk5のみ8つ同時セットが可能だが、コスト的に使う機会はほぼ無い. Dev571のレシピ変更にて、全体的にコストが低減され作りやすくなった（特にMk5）
* 専用GUI: 有 — モジュール装着スロット
* 実装方法: 

![](assets/tech_items/lp_ChassisPipeMk1.png)

### Logistics Chassis Pipe Mk2 / 物流制御パイプ Mk2 / LP

* 実装状態: [ ]
* 名称: Logistics Chassis Pipe Mk2 / 日本語: 物流制御パイプ Mk2 / Origin: `-` / New: `bcn:logistics_chassis_pipe_mk2`
* レシピ: `Basic Logistics Pipe` x1 + `Iron Ingot` x8 / `Iron Chipset` x3 / `Basic Logistics Pipe` x1 + `Iron Ingot` x2 + `Iron Ingot` x3 / `Iron Chipset` x1 -> `Logistics Chassis Pipe Mk2` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設置後に下に挙げた様々なモジュール(＝ソフトウェア)をセットする事で、 様々な機能を組み合わせつつ複合的に持たせる事ができるが、 これ自体は単なるモジュールの容れ物(＝ハードウェア)に過ぎない. Mk1からMk5まで存在し、 Mk1～4はそれぞれ1～4個のモジュールを同時にセットできる. Mk5のみ8つ同時セットが可能だが、コスト的に使う機会はほぼ無い. Dev571のレシピ変更にて、全体的にコストが低減され作りやすくなった（特にMk5）
* 専用GUI: 有 — モジュール装着スロット
* 実装方法: 

![](assets/tech_items/lp_ChassisPipeMk2.gif)

### Logistics Chassis Pipe Mk3 / 物流制御パイプ Mk3 / LP

* 実装状態: [ ]
* 名称: Logistics Chassis Pipe Mk3 / 日本語: 物流制御パイプ Mk3 / Origin: `-` / New: `bcn:logistics_chassis_pipe_mk3`
* レシピ: `Basic Logistics Pipe` x1 + `Gold Ingot` x8 / `Golden Chipset` x3 / `Basic Logistics Pipe` x1 + `Iron Ingot` x2 + `Iron Ingot` x6 / `Iron Chipset` x2 -> `Logistics Chassis Pipe Mk3` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設置後に下に挙げた様々なモジュール(＝ソフトウェア)をセットする事で、 様々な機能を組み合わせつつ複合的に持たせる事ができるが、 これ自体は単なるモジュールの容れ物(＝ハードウェア)に過ぎない. Mk1からMk5まで存在し、 Mk1～4はそれぞれ1～4個のモジュールを同時にセットできる. Mk5のみ8つ同時セットが可能だが、コスト的に使う機会はほぼ無い. Dev571のレシピ変更にて、全体的にコストが低減され作りやすくなった（特にMk5）
* 専用GUI: 有 — モジュール装着スロット
* 実装方法: 

![](assets/tech_items/lp_ChassisPipeMk3.gif)

### Logistics Chassis Pipe Mk4 / 物流制御パイプ Mk4 / LP

* 実装状態: [ ]
* 名称: Logistics Chassis Pipe Mk4 / 日本語: 物流制御パイプ Mk4 / Origin: `-` / New: `bcn:logistics_chassis_pipe_mk4`
* レシピ: `Basic Logistics Pipe` x1 + `Diamond` x8 / `Diamond Chipset` x3 / `Basic Logistics Pipe` x1 + `Iron Ingot` x2 + `Gold Ingot` x3 + `Golden Chipset` x1 -> `Logistics Chassis Pipe Mk4` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設置後に下に挙げた様々なモジュール(＝ソフトウェア)をセットする事で、 様々な機能を組み合わせつつ複合的に持たせる事ができるが、 これ自体は単なるモジュールの容れ物(＝ハードウェア)に過ぎない. Mk1からMk5まで存在し、 Mk1～4はそれぞれ1～4個のモジュールを同時にセットできる. Mk5のみ8つ同時セットが可能だが、コスト的に使う機会はほぼ無い. Dev571のレシピ変更にて、全体的にコストが低減され作りやすくなった（特にMk5）
* 専用GUI: 有 — モジュール装着スロット
* 実装方法: 

![](assets/tech_items/lp_ChassisPipeMk4.gif)

### Logistics Chassis Pipe Mk5 / 物流制御パイプ Mk5 / LP

* 実装状態: [ ]
* 名称: Logistics Chassis Pipe Mk5 / 日本語: 物流制御パイプ Mk5 / Origin: `-` / New: `bcn:logistics_chassis_pipe_mk5`
* レシピ: `Basic Logistics Pipe` x1 + `Gold Block` x4 + `Diamond Block` x4 / `物流制御パイプMk` x41 + `Diamond Chipset` x1 -> `Logistics Chassis Pipe Mk5` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 設置後に下に挙げた様々なモジュール(＝ソフトウェア)をセットする事で、 様々な機能を組み合わせつつ複合的に持たせる事ができるが、 これ自体は単なるモジュールの容れ物(＝ハードウェア)に過ぎない. Mk1からMk5まで存在し、 Mk1～4はそれぞれ1～4個のモジュールを同時にセットできる. Mk5のみ8つ同時セットが可能だが、コスト的に使う機会はほぼ無い. Dev571のレシピ変更にて、全体的にコストが低減され作りやすくなった（特にMk5）
* 専用GUI: 有 — モジュール装着スロット
* 実装方法: 

![](assets/tech_items/lp_ChassisPipeMk5.png)

### Bee Analyzer Logistics Pipe / 蜂鑑定パイプ / LP

* 実装状態: [ ]
* 名称: Bee Analyzer Logistics Pipe / 日本語: 蜂鑑定パイプ / Origin: `-` / New: `bcn:bee_analyzer_logistics_pipe`
* レシピ: `Redstone` x3 + `Propolis` x4 + `Iron Chipset` x1 / `Basic Logistics Pipe` x1 / `Liquid Honey 1500mB` / `MJ` x640 -> `Bee Analyzer Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: FFMの蜂限定でだが、回収と抽出、両方の機能を発揮する. 配管内に存在する全ての未鑑定蜂を回収し、隣接インベントリに格納. 更に隣接インベントリ内に鑑定済みの蜂が置かれていて、 かつ配管内にその蜂の回収ポイントがあるなら、5秒に1回1個搬出する. 隣が設置型分析機でなくとも、インベントリならば機能する.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### BeeSink Logistics Pipe / 蜂回収パイプ / LP

* 実装状態: [ ]
* 名称: BeeSink Logistics Pipe / 日本語: 蜂回収パイプ / Origin: `-` / New: `bcn:beesink_logistics_pipe`
* レシピ: `Redstone` x4 + `Propolis` x4 / `Basic Logistics Pipe` x1 / `Liquid Honey 1500mB` / `MJ` x640 -> `BeeSink Logistics Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 養蜂パイプのロジスティクス版で、GUIで指定した条件の蜂に関し、 配管内にあるものを回収、隣接インベントリに格納する. 自然にいない蜂は、交配で作り出した後でないと品種指定できない. FFMの養蜂パイプと違い、蜂以外のアイテムは回収指定できない.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

<!-- 画像なし -->

### Logistics System Entrance Pipe / 入口パイプ / LP

* 実装状態: [ ]
* 名称: Logistics System Entrance Pipe / 日本語: 入口パイプ / Origin: `-` / New: `bcn:logistics_system_entrance_pipe`
* レシピ: `Provider Logistics Pipe` x1 + `Green Dye` x1 -> `Logistics System Entrance Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: このパイプ内を通過したアイテムは、 ペアとなる周波数カードがセットされた出口パイプへとオートで送られる. （途中の分岐点にはきちんとロジパイプが使われている必要がある） ただ、他のロジパイプからリクエストを受けて移動中のアイテムや、 既に特定の回収ポイントに向かって移動中のアイテムには効果がなく、 実質木パイプや黒曜石パイプ等からパイプ内に入ってきた、 ロジパイプ外からの侵入アイテムに対してしか効果がなく使いにくい.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

![](assets/tech_items/lp_SystemEntrancePipe.png)

### Logistics System Destination Pipe / 出口パイプ / LP

* 実装状態: [ ]
* 名称: Logistics System Destination Pipe / 日本語: 出口パイプ / Origin: `-` / New: `bcn:logistics_system_destination_pipe`
* レシピ: `Provider Logistics Pipe` x1 + `Red Dye` x1 -> `Logistics System Destination Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ペアとなる周波数カードがセットされた入口パイプから 送られてきたアイテムにとっての、目的地として作用するパイプ. 上記の入口パイプで述べた性質により、使いにくい.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

![](assets/tech_items/lp_SystemDestinationPipe.png)

### Logistics Firewall Pipe / ファイアーウォールパイプ / LP

* 実装状態: [ ]
* 名称: Logistics Firewall Pipe / 日本語: ファイアーウォールパイプ / Origin: `-` / New: `bcn:logistics_firewall_pipe`
* レシピ: `Basic Logistics Pipe` x1 + `Iron Transport Pipe` x3 + `Nether Brick` x4 + `Diamond Chipset` x1 + `Iron Ingot` x1 / `Basic Logistics Pipe` x1 + `Nether Brick` x4 + `Diamond Chipset` x1 + `Iron Ingot` x1 -> `Logistics Firewall Pipe` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: ネットワーク同士を分離したまま、一部要素を共有することができるパイプ. このパイプと繋がっているネットワーク間での、指定アイテムのみの共有、共有禁止の設定が可能な他、 供給、加工、高速分別について共有の可否がそれぞれ設定可能. 以前はLPを別なネットワークへ通すことはなかったが、Dev427よりLPの供給可否も設定できるようになった. これにより、"ネットワークは分割したままLPのみ一括管理"という配管も可能に.
* 専用GUI: 有 — フィルタ設定
* 実装方法: 

![](assets/tech_items/lp_FirewallPipe.png)

### Unrouted Transport Pipe / 不定経路輸送パイプ / LP

* 実装状態: [ ]
* 名称: Unrouted Transport Pipe / 日本語: 不定経路輸送パイプ / Origin: `logisticspipes:pipe_transport_basic` / New: `bcn:unrouted_transport_pipe`
* レシピ: `Iron Ingot` x4 + `Redstone` x2 + `Block Glass` x2 -> `Unrouted Transport Pipe` x8
* レシピ配置図: `ABA` / `C0C` / `ABA`
* 概要: LPを消費してパイプ内のアイテム輸送速度を他のロジパイプと同じ速度に保つパイプ. パイプにのみ接続し、一切のコンテナに接続しない. BCのパイプ同様乱数に基づいた確率によって分岐するため分岐点にも使えない. ただ、コストが安いのでBCのパイプの代わりに利用するとよいかもしれない. 現在、どのバージョンから追加されたのか捜索中.
* 専用GUI: 有 — ロジスティクスレンチによる設定画面
* 実装方法: 

![](assets/tech_items/lp_UnroutedTransportPipe.png)

### Reset Pipe / パイプの初期化 / LP

* 実装状態: [ ]
* 名称: Reset Pipe / 日本語: パイプの初期化 / Origin: `-` / New: `bcn:reset_pipe`
* レシピ: なし
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 
* 専用GUI: 有 — ロジスティクスレンチによる設定画面
* 実装方法: 

![](assets/tech_items/lp_ResetPipe.gif)

### Logistics Pipe Controller / パイプコントローラー / LP

* 実装状態: [ ]
* 名称: Logistics Pipe Controller / 日本語: パイプコントローラー / Origin: `-` / New: `bcn:logistics_pipe_controller`
* レシピ: `Gold Gear` x1 + `Gold Ingot` x3 -> `Logistics Pipe Controller` x1
* レシピ配置図: `[000]` / `[000]` / `[000]`
* 概要: 以前のネットワークモニターとアップグレーダーの機能を併せ持つ.
* 専用GUI: 有 — ロジスティクス設定画面
* 実装方法: 

![](assets/tech_items/lp_PipeController.gif)
