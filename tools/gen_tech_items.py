#!/usr/bin/env python3
"""Flatten TECH_ITEMS.md tables into per-item blocks per the legend format."""
import re, os, json, glob, unicodedata

DOC = '/ws/docs/TECH_ITEMS.md'
RES = '/ws/resources'
IMGDIR = 'assets/tech_items'

# ---------- name normalization helpers ----------
def norm_tokens(s):
    s = re.sub(r'[^0-9A-Za-z]+', ' ', s)
    return ' '.join(sorted(s.lower().split()))

def camel_split(s):
    s = re.sub(r'([a-z0-9])([A-Z])', r'\1 \2', s)
    return s.replace('_', ' ').replace('-', ' ')

def slug(s):
    s = s.lower()
    s = re.sub(r'&', ' and ', s)
    s = re.sub(r'[^a-z0-9]+', '_', s)
    return s.strip('_') or 'item'

def nfkc(s):
    return unicodedata.normalize('NFKC', s)

def strip_html(s):
    return re.sub(r'<[^>]+>', '', s)

# ---------- 1) ITEMS.md descriptions ----------
def parse_items_md(path):
    desc = {}   # norm_en -> (jp, en, desc)
    cur = None
    for line in open(path):
        m = re.match(r'^(?:#{3,5}\s*[\d.]+\s*|-\s+)([^-|][^(]*?)\s*\(([^()]+)\)\s*$', line.strip())
        if m:
            jp, en = m.group(1).strip(), m.group(2).strip()
            cur = {'jp': jp, 'en': en, 'desc': ''}
            desc[norm_tokens(en)] = cur
            continue
        d = re.match(r'^\s*-\s*説明[:：]\s*(.+)', line)
        if d and cur:
            cur['desc'] += ' ' + d.group(1).strip()
            cur['desc'] = cur['desc'].strip()
    return desc

ITEM_DESC = {'BC': {}, 'IC2': {}, 'PR': {}, 'LP': {}}
for mod, key in [('buildcraft','BC'), ('industrialcraft','IC2'), ('projectred','PR'), ('logisticspipes','LP')]:
    ITEM_DESC[key].update(parse_items_md(f'{RES}/{mod}/ITEMS.md'))

# ---------- 2) recipe JSONs -> {result_id: {pattern, key, count, shaped}} ----------
def key_name(v):
    """ingredient value -> display token"""
    if isinstance(v, str):
        return v
    if 'item' in v:
        return v['item']
    if 'tag' in v:
        return '#' + v['tag']
    if 'ore' in v:
        return 'ore:' + v['ore']
    if 'part' in v:
        return v['part']
    if 'components' in v:
        mm = v.get('components', {}).get('cb_microblock:micro_material', {}).get('material')
        if mm:
            return mm
        if 'items' in v:
            return v['items']
        return 'component_item'
    return str(v)

RECIPES = {}

def add_recipe(rid, pattern, keymap, count, shaped):
    RECIPES[rid] = {'pattern': pattern, 'key': keymap, 'count': count, 'shaped': shaped}

# BC (1.12 format)
for f in glob.glob(f'{RES}/buildcraft/assets/*/recipes/*.json'):
    try:
        j = json.load(open(f))
    except Exception:
        continue
    if not isinstance(j, dict):
        continue
    res = j.get('result', {})
    rid = res.get('item')
    if not rid:
        continue
    if res.get('data'):
        rid += '#' + str(res['data'])
    pat = j.get('pattern')
    key = {k: key_name(v) for k, v in j.get('key', {}).items()}
    if pat:
        add_recipe(rid, pat, key, res.get('count', 1), True)
    elif j.get('ingredients'):
        key2 = {}
        for i, ing in enumerate(j['ingredients']):
            key2[chr(65 + i)] = key_name(ing)
        add_recipe(rid, None, key2, res.get('count', 1), False)

# IC2 export: pattern + ordered ingredients list (char = first-appearance order)
ic2 = json.load(open(f'{RES}/industrialcraft/recipes_export.json'))['recipes']
for r in ic2:
    rid = r.get('result', {}).get('id')
    if not rid:
        continue
    pat = r.get('pattern')
    ings = r.get('ingredients', [])
    if pat:
        seen, order = [], []
        for row in pat:
            for ch in row:
                if ch not in (' ', '0') and ch not in seen:
                    seen.append(ch); order.append(ch)
        key = {}
        for i, ch in enumerate(order):
            if i < len(ings):
                key[ch] = ings[i]['item']
        add_recipe(rid, pat, key, r['result'].get('count', 1), True)
    elif ings:
        key = {chr(65 + i): ing['item'] for i, ing in enumerate(ings)}
        add_recipe(rid, None, key, r['result'].get('count', 1), False)

# PR (modern format)
for f in glob.glob(f'{RES}/projectred/data/*/recipe/*.json'):
    try:
        j = json.load(open(f))
    except Exception:
        continue
    if not isinstance(j, dict):
        continue
    res = j.get('result', {})
    rid = res.get('id') or res.get('item')
    if not rid:
        continue
    pat = j.get('pattern')
    key = {k: key_name(v) for k, v in j.get('key', {}).items()}
    if pat:
        add_recipe(rid, pat, key, res.get('count', 1), True)
    elif j.get('ingredients'):
        ings = j['ingredients']
        key2 = {chr(65 + i): (key_name(x) if isinstance(x, dict) else x) for i, x in enumerate(ings)}
        add_recipe(rid, None, key2, res.get('count', 1), False)

# LP (1.12-ish format in assets/logisticspipes/recipes)
for f in glob.glob(f'{RES}/logisticspipes/assets/logisticspipes/recipes/**/*.json', recursive=True):
    try:
        j = json.load(open(f))
    except Exception:
        continue
    if not isinstance(j, dict):
        continue
    res = j.get('result', {})
    rid = res.get('item') or res.get('id')
    if not rid:
        continue
    pat = j.get('pattern')
    key = {k: key_name(v) for k, v in j.get('key', {}).items()}
    if pat:
        add_recipe(rid, pat, key, res.get('count', 1), True)
    elif j.get('ingredients'):
        key2 = {chr(65 + i): key_name(x) for i, x in enumerate(j['ingredients'])}
        add_recipe(rid, None, key2, res.get('count', 1), False)

# ---------- 3) JP display names ----------
MANUAL = {
 'iron_ingot':'鉄インゴット','ingotiron':'鉄インゴット','gold_ingot':'金インゴット','ingotgold':'金インゴット',
 'copper_ingot':'銅インゴット','ingotcopper':'銅インゴット','copper':'銅','tin_ingot':'スズインゴット','ingottin':'スズインゴット','tin':'スズ',
 'bronze':'青銅','ingotbronze':'青銅インゴット','lead':'鉛','inglead':'鉛インゴット','ingotsilver':'銀インゴット','silver':'銀',
 'steel':'鋼','ingotsteel':'鋼インゴット','refined_iron':'精錬鉄','ingotrefinediron':'精錬鉄',
 'cobblestone':'丸石','stone':'焼石','stones':'焼石','glass':'ガラス','blockglasscolorless':'ガラス','glasscolorless':'ガラス',
 'piston':'ピストン','sticky_piston':'粘着ピストン','stickypiston':'粘着ピストン',
 'redstone':'赤石粉','dustredstone':'赤石粉','redstone_torch':'赤石トーチ','redstonetorch':'赤石トーチ','redstone_block':'レッドストーンブロック','blockredstone':'レッドストーンブロック',
 'stick':'棒','stickwood':'棒','planks':'木材','plankwood':'木材','log':'原木','logwood':'原木','chest':'チェスト','chests/wooden':'チェスト','chestswooden':'チェスト',
 'bucket':'バケツ','water_bucket':'水バケツ','lava_bucket':'溶岩バケツ','diamond':'ダイヤモンド','emerald':'エメラルド',
 'obsidian':'黒曜石','sandstone':'砂岩','lapis_lazuli':'ラピスラズリ','lapis':'ラピスラズリ','lazuli':'ラピスラズリ','lapislazuliblock':'ラピスラズリブロック','blocklapis':'ラピスラズリブロック',
 'quartz':'ネザー水晶','nether_quartz':'ネザー水晶','blockquartz':'ネザー水晶ブロック','ink_sac':'イカスミ','dye':'染料','dyes':'染料',
 'wool':'羊毛','blockwool':'羊毛','string':'糸','iron_bars':'鉄格子','ironpickaxe':'鉄つるはし','diamondpickaxe':'ダイヤつるはし',
 'crafting_table':'作業台','furnace':'かまど','iron_furnace':'鉄かまど','hopper':'ホッパー','paper':'紙','book':'本',
 'slime_ball':'スライムボール','slimeball':'スライムボール','slime':'スライムボール','gunpowder':'火薬','bone_meal':'骨粉','bonemeal':'骨粉','bone':'骨',
 'torch':'松明','rail':'線路','magma_block':'マグマブロック','clay':'粘土','clayball':'粘土玉','brick':'レンガ','flint':'火打石',
 'gravel':'砂利','sand':'砂','dirt':'土','wheat':'小麦','sugar_cane':'サトウキビ','sugarcanes':'サトウキビ','melon':'スイカ','pumpkin':'カボチャ',
 'cocoa_beans':'カカオ豆','nether_wart':'ネザーウォート','spider_eye':'クモの目','arrow':'矢','bowl':'ボウル','compass':'コンパス','clock':'時計',
 'lever':'レバー','ladder':'はしご','sign':'看板','ender_pearl':'エンダーパール','blaze_powder':'ブレイズパウダー','coal':'石炭','charcoal':'木炭',
 'glowstone_dust':'グロウストーンの粉','dustglowstone':'グロウストーンの粉','glowstone':'グロウストーン','netherrack':'ネザーラック','dyegreen':'緑の染料','greendye':'緑の染料',
 'gearwood':'木の歯車','gearstone':'石の歯車','geariron':'鉄の歯車','geargold':'金の歯車','geardiamond':'ダイヤの歯車',
 'water':'水','lava':'溶岩','water_cell':'水セル','cellwater':'水セル',
 'dustscoal':'石炭の粉','dustcoal':'石炭の粉','dusts/redstone':'赤石粉','dusts/glowstone':'グロウストーンの粉','ingots/bronze':'青銅インゴット','ingots/tin':'スズインゴット','ingots/copper':'銅インゴット','ingots/refined_iron':'精錬鉄','ingots/silver':'銀インゴット','ingots/lead':'鉛インゴット','ingots/steel':'鋼インゴット','ingots/iron':'鉄インゴット','ingots/electrotine_alloy':'エレクトロタイン合金インゴット','ingots/red_alloy':'赤合金インゴット','ingots/blue_alloy':'蒼合金インゴット','dusts/coal':'石炭の粉',
}

# JP ingredient term -> EN display name
MANUAL_JP2EN = {
 '鉄インゴット':'Iron Ingot','金インゴット':'Gold Ingot','銅インゴット':'Copper Ingot','スズインゴット':'Tin Ingot','錫インゴット':'Tin Ingot',
 '青銅インゴット':'Bronze Ingot','鉛インゴット':'Lead Ingot','銀インゴット':'Silver Ingot','鋼インゴット':'Steel Ingot','精錬鉄':'Refined Iron Ingot',
 '丸石':'Cobblestone','焼石':'Stone','石':'Stone','ガラス':'Glass','ピストン':'Piston','粘着ピストン':'Sticky Piston',
 '赤石粉':'Redstone','レッドストーン':'Redstone','赤石トーチ':'Redstone Torch','レッドストーントーチ':'Redstone Torch','レッドストーンブロック':'Redstone Block','赤石ブロック':'Redstone Block',
 '棒':'Stick','木の棒':'Stick','木材':'Wooden Planks','原木':'Log','チェスト':'Chest','バケツ':'Bucket','水バケツ':'Water Bucket','溶岩バケツ':'Lava Bucket',
 'ダイヤモンド':'Diamond','エメラルド':'Emerald','黒曜石':'Obsidian','砂岩':'Sandstone','ラピスラズリ':'Lapis Lazuli','ラピスラズリブロック':'Lapis Lazuli Block',
 'ネザー水晶':'Nether Quartz','ネザー水晶ブロック':'Nether Quartz Block','イカスミ':'Ink Sac','染料':'Dye','緑の染料':'Green Dye','黄の染料':'Yellow Dye','青の染料':'Blue Dye','赤の染料':'Red Dye','黒の染料':'Black Dye','茶の染料':'Brown Dye','白の染料':'White Dye',
 '羊毛':'Wool','糸':'String','鉄格子':'Iron Bars','鉄つるはし':'Iron Pickaxe','鉄のつるはし':'Iron Pickaxe','ダイヤつるはし':'Diamond Pickaxe','ダイヤのつるはし':'Diamond Pickaxe',
 '作業台':'Crafting Table','かまど':'Furnace','鉄かまど':'Iron Furnace','ホッパー':'Hopper','紙':'Paper','本':'Book','スライムボール':'Slimeball',
 '火薬':'Gunpowder','骨粉':'Bone Meal','骨':'Bone','松明':'Torch','線路':'Rail','マグマブロック':'Magma Block','粘土':'Clay','粘土玉':'Clay Ball','粘土ブロック':'Clay Block','レンガ':'Brick',
 '火打石':'Flint','砂利':'Gravel','砂':'Sand','土':'Dirt','小麦':'Wheat','サトウキビ':'Sugar Cane','スイカ':'Melon','カボチャ':'Pumpkin','カカオ豆':'Cocoa Beans',
 'ネザーウォート':'Nether Wart','クモの目':'Spider Eye','矢':'Arrow','ボウル':'Bowl','コンパス':'Compass','時計':'Clock','レバー':'Lever','はしご':'Ladder',
 '看板':'Sign','エンダーパール':'Ender Pearl','ブレイズパウダー':'Blaze Powder','石炭':'Coal','木炭':'Charcoal','グロウストーンの粉':'Glowstone Dust','グロウストーン':'Glowstone','ネザーラック':'Netherrack',
 '水':'Water','溶岩':'Lava','水セル':'Water Cell','溶岩セル':'Lava Cell','空のセル':'Empty Cell','セル':'Cell',
 '石炭の粉':'Coal Dust','樹液':'Sticky Resin','樹脂':'Rubber','ゴム':'Rubber','銅の粉':'Copper Dust','スズの粉':'Tin Dust','錫の粉':'Tin Dust',
 '青銅の粉':'Bronze Dust','鉄の粉':'Iron Dust','金の粉':'Gold Dust','鉛の粉':'Lead Dust','銀の粉':'Silver Dust','ウラニウム':'Uranium','精錬ウラン':'Refined Uranium','イリジウム':'Iridium',
 '電子回路':'Electronic Circuit','発展回路':'Advanced Circuit','マシン':'Machine Block','マシンブロック':'Machine Block','アドバンスドマシン':'Advanced Machine Block',
 '充電式電池':'RE-Battery','冷却セル':'Coolant Cell','合金板':'Alloy Plate','銅のケーブル':'Copper Cable','金のケーブル':'Gold Cable','錫のケーブル':'Tin Cable','ガラス繊維ケーブル':'Glass Fibre Cable',
 '木の歯車':'Wooden Gear','石の歯車':'Stone Gear','鉄の歯車':'Iron Gear','金の歯車':'Gold Gear','ダイヤの歯車':'Diamond Gear',
 '液体タンク':'Tank','防水素材':'Pipe Sealant','防水材':'Pipe Sealant','園芸用支柱':'Crop Sticks','ゴムの原木':'Rubber Log','ゴム原木':'Rubber Log',
 '赤石':'Redstone','青マーカー':'Land Mark','緑マーカー':'Path Mark','鉄ピストン':'Piston','金チップ':'Gold Chipset','鉄チップ':'Iron Chipset','赤石チップ':'Redstone Chipset','ダイヤチップ':'Diamond Chipset',
 '変圧機アップグレード':'Transformer Upgrade','赤色の染料':'Red Dye','黄色の染料':'Yellow Dye','青色の染料':'Blue Dye','緑色の染料':'Green Dye','黒色の染料':'Black Dye','茶色の染料':'Brown Dye','白色の染料':'White Dye',
 '赤の染料':'Red Dye','黄の染料':'Yellow Dye','青の染料':'Blue Dye','緑の染料':'Green Dye','黒の染料':'Black Dye','橙の染料':'Orange Dye','各色染料':'Any Dye','全16色':'All 16 Dye Colors','再染色':'Re-dye','染色':'Dye',
 '本棚':'Bookshelf','地図':'Map','ガラス瓶':'Glass Bottle','ダイヤ歯車':'Diamond Gear','ダイヤブロック':'Diamond Block','ダイヤ':'Diamond',
 'レッドストーンパウダー':'Redstone','グロウストーンダスト':'Glowstone Dust','光石粉':'Glowstone Dust','水入りバケツ':'Water Bucket','溶岩入りバケツ':'Lava Bucket',
 '木パイプ':'Wooden Transport Pipe','液体木パイプ':'Wooden Fluid Pipe','液体金パイプ':'Golden Fluid Pipe','BCレンチ':'Wrench (BC)','漏斗':'Chute','漏斗(BC':'Chute',
 'ネザーレンガ':'Nether Brick','レンガブロック':'Bricks','エンチャントの本':'Enchanted Book','キノコ':'Mushroom','ツル':'Vines','ブラスインゴット':'Brass Ingot',
 'グリーンサファイヤ':'Green Sapphire','各種宝石':'Gems','バッテリー':'Battery','チューブ':'Tube','レッドストーンチューブ':'Redstone Tube','フラットケーブル':'Flat Cable',
 '木材ブロック':'Wood Block','板ガラス':'Glass Pane','オークの原木 strip':'Stripped Oak Log','柱':'Post','鉄ブロック':'Iron Block',
 '鉄ゲート':'Iron Gate','金ゲート':'Golden Gate','ダイヤゲート':'Diamond Gate','エメラルドゲート':'Emerald Gate','水晶ゲート':'Quartz Gate',
 'ワイヤー全色':'Pipe Wires (all colors)','レッドストーンコンパレータ':'Redstone Comparator','日照センサー':'Daylight Sensor','木の感圧式スイッチ':'Wooden Pressure Plate',
 '石炭の粉)小さな鉄の粉':'Tiny Iron Dust','スズの粉':'Tin Dust','鉄の粉':'Iron Dust','小麦の種':'Wheat Seeds','各種葉':'Leaves','草':'Grass','各種苗':'Saplings',
 'アイテム差配器':'Item Allocator','アイテム検知器':'Item Detector','Mod指定回収モジュール':'Mod-Based Item Sink Module','分類器':'Classifier','プロポリス':'Propolis',
 '液体蜂蜜1500mB':'Liquid Honey 1500mB','気送管':'Pneumatic Tube','ブリッジ':'Bridge Tube','LEDパウダー':'LED Powder',
 '地の結晶片':'Earth Shard','水の結晶片':'Water Shard','火の結晶片':'Fire Shard','火打石と打ち金':'Flint and Steel','石のボタン':'Stone Button',
 '石炭ブロック':'Coal Block','赤砂':'Red Sand','空バケツ':'Empty Bucket','紫の染料':'Purple Dye','色付き':'Colored','緩衝器':'Buffer',
 '配送器':'Distributor','選別配送器':'Sorted Distributor','花粉':'Pollen','菌糸ブロック':'Mycelium','調整器':'Regulator',
 '鉄のインゴット':'Iron Ingot','鉄のチェストプレート':'Iron Chestplate','鉄パイプ':'Iron Transport Pipe','陰極基盤':'Stone Cathode',
 '雪玉':'Snowball','青電力電池':'Blulectric Battery','革':'Leather','革のチェストプレート':'Leather Chestplate','種':'Seeds',
 '物流制御パイプ':'Logistics Control Pipe','金ブロック':'Gold Block','金塊':'Gold Nugget','砂利パイプ':'Cobblestone Structure Pipe',
 '炭素繊維':'Raw Carbon Fiber','炭素メッシュ':'Raw Carbon Mesh','木炭':'Charcoal','石炭':'Coal','金インゴット':'Gold Ingot','鉄インゴット':'Iron Ingot','赤石粉':'Redstone',
 '風の結晶片':'Wind Shard','ホッパー':'Hopper','色付き':'Colored','染色':'Dye','再染色':'Re-dye','全16色':'All 16 Dye Colors',
}

# id/tag -> canonical EN name (keys normalized: lowercase, no separators)
MANUAL_EN = {
 'ingotiron':'Iron Ingot','ingotgold':'Gold Ingot','ingotcopper':'Copper Ingot','ingottin':'Tin Ingot','ingotbronze':'Bronze Ingot',
 'ingotsilver':'Silver Ingot','ingotlead':'Lead Ingot','ingotsteel':'Steel Ingot','ingotrefinediron':'Refined Iron Ingot',
 'ingotelectrotinealloy':'Electrotine Alloy Ingot','ingotredalloy':'Red Alloy Ingot','ingotbluealloy':'Blue Alloy Ingot',
 'iron_ingot':'Iron Ingot','gold_ingot':'Gold Ingot','copper_ingot':'Copper Ingot','tin_ingot':'Tin Ingot','bronze_ingot':'Bronze Ingot',
 'stickwood':'Stick','stick':'Stick','plankwood':'Wooden Planks','planks':'Wooden Planks','logwood':'Log','log':'Log',
 'blockglasscolorless':'Glass','glass':'Glass','dustredstone':'Redstone','redstone':'Redstone','dustglowstone':'Glowstone Dust',
 'dustcoal':'Coal Dust','blockredstone':'Redstone Block','blocklapis':'Lapis Lazuli Block','blockquartz':'Nether Quartz Block','blockwool':'Wool',
 'ingots/tin':'Tin Ingot','ingots/copper':'Copper Ingot','ingots/bronze':'Bronze Ingot','ingots/refined_iron':'Refined Iron Ingot',
 'ingots/silver':'Silver Ingot','ingots/lead':'Lead Ingot','ingots/steel':'Steel Ingot','ingots/iron':'Iron Ingot',
 'ingots/electrotine_alloy':'Electrotine Alloy Ingot','ingots/red_alloy':'Red Alloy Ingot','ingots/blue_alloy':'Blue Alloy Ingot',
 'dusts/redstone':'Redstone','dusts/glowstone':'Glowstone Dust','dusts/coal':'Coal Dust','chests/wooden':'Chest',
 'gearwood':'Wooden Gear','gearstone':'Stone Gear','geariron':'Iron Gear','geargold':'Gold Gear','geardiamond':'Diamond Gear',
 'water':'Water','lava':'Lava','cobblestone':'Cobblestone','stone':'Stone','stones':'Stone','piston':'Piston','sticky_piston':'Sticky Piston','stickypiston':'Sticky Piston',
 'diamond':'Diamond','emerald':'Emerald','obsidian':'Obsidian','sandstone':'Sandstone','lapis_lazuli':'Lapis Lazuli','lapis':'Lapis Lazuli','lazuli':'Lapis Lazuli',
 'nether_quartz':'Nether Quartz','quartz':'Nether Quartz','ink_sac':'Ink Sac','inksac':'Ink Sac','dye':'Dye','wool':'Wool','string':'String',
 'iron_bars':'Iron Bars','ironbars':'Iron Bars','iron_pickaxe':'Iron Pickaxe','ironpickaxe':'Iron Pickaxe','diamond_pickaxe':'Diamond Pickaxe','diamondpickaxe':'Diamond Pickaxe',
 'crafting_table':'Crafting Table','craftingtable':'Crafting Table','furnace':'Furnace','iron_furnace':'Iron Furnace','ironfurnace':'Iron Furnace',
 'hopper':'Hopper','paper':'Paper','book':'Book','slime_ball':'Slimeball','slimeball':'Slimeball','slime':'Slimeball','gunpowder':'Gunpowder',
 'bone_meal':'Bone Meal','bonemeal':'Bone Meal','bone':'Bone','torch':'Torch','rail':'Rail','magma_block':'Magma Block','magmablock':'Magma Block',
 'clay':'Clay','clay_ball':'Clay Ball','clayball':'Clay Ball','brick':'Brick','flint':'Flint','gravel':'Gravel','sand':'Sand','dirt':'Dirt',
 'wheat':'Wheat','sugar_cane':'Sugar Cane','sugarcanes':'Sugar Cane','melon':'Melon','pumpkin':'Pumpkin','cocoa_beans':'Cocoa Beans','cocoabeans':'Cocoa Beans',
 'nether_wart':'Nether Wart','netherwart':'Nether Wart','spider_eye':'Spider Eye','spidereye':'Spider Eye','arrow':'Arrow','bowl':'Bowl',
 'compass':'Compass','clock':'Clock','lever':'Lever','ladder':'Ladder','sign':'Sign','ender_pearl':'Ender Pearl','enderpearl':'Ender Pearl',
 'blaze_powder':'Blaze Powder','blazepowder':'Blaze Powder','coal':'Coal','charcoal':'Charcoal','glowstone_dust':'Glowstone Dust','glowstonedust':'Glowstone Dust',
 'glowstone':'Glowstone','netherrack':'Netherrack','water_bucket':'Water Bucket','waterbucket':'Water Bucket','lava_bucket':'Lava Bucket','lavabucket':'Lava Bucket','bucket':'Bucket','chest':'Chest',
 'redstone_torch':'Redstone Torch','redstonetorch':'Redstone Torch','redstone_block':'Redstone Block','redstoneblock':'Redstone Block',
 'dyes/blue':'Blue Dye','dyes/red':'Red Dye','dyes/green':'Green Dye','dyes/black':'Black Dye','dyes/brown':'Brown Dye','dyes/white':'White Dye','dyegreen':'Green Dye','greendye':'Green Dye',
}

TABLE_N2J = {}   # norm_en -> jp
TABLE_EN = {}    # norm_en -> en (canonical)
JP2EN = {}       # jp -> en

def ingr_name_en(token):
    """ingredient id/tag token -> EN display name"""
    t = token
    if t.startswith('ore:'):
        t = t[4:]
    if t.startswith('#'):
        t = t[1:]
    if ':' in t:
        t = t.split(':', 1)[1]
    t = re.sub(r'\{.*', '', t)
    tail = t.split('/')[-1]
    tl = t.lower()
    for k in (tl, tail.lower(), tl.replace('-', '_'), re.sub(r'[^a-z0-9]', '', tail.lower())):
        if k in MANUAL_EN:
            return MANUAL_EN[k]
    n = norm_tokens(camel_split(tail))
    if n in TABLE_EN:
        return TABLE_EN[n]
    toks = n.split()
    if len(toks) >= 2:
        for en_norm, en in TABLE_EN.items():
            et = en_norm.split()
            if len(toks) == len(et) and all(a.startswith(b) or b.startswith(a) for a, b in zip(toks, et)):
                return en
    # humanize: camel/underscore -> Title Case words
    h = camel_split(tail).strip()
    h = re.sub(r'\s+', ' ', h)
    return ' '.join(w.capitalize() for w in h.split()) or token

def jp_to_en(name):
    """JP ingredient term -> EN display name"""
    s = nfkc(name).strip()
    s = re.sub(r'^[^）)]*\)', '', s).strip() or s          # '1個)小麦' -> '小麦'
    s = re.sub(r'^[（(][^）)]*[）)]', '', s).strip() or s  # '(ウランセル)再濃縮...' -> '再濃縮...'
    s = re.sub(r'[（(][^）)]*[)）]$', '', s).strip() or s  # trailing '(※バニラのもの)'
    if s in JP2EN:
        return JP2EN[s]
    if s in MANUAL_JP2EN:
        return MANUAL_JP2EN[s]
    for k, v in JP2EN.items():
        if s.startswith(k) and len(k) >= 3:
            return v
    return name.strip()

# ---------- 4) GUI rules ----------
def gui_bc(en, desc):
    e = en.lower()
    rules = [
     ('combustion engine','有 — 燃料と冷却材の投入、稼働状態の表示'),('stirling engine','有 — 燃料投入と燃焼進行の表示'),
     ('creative engine','有 — 出力レベルの選択'),('redstone engine','無 — レバーで起動するのみ'),
     ('mj dynamo','有 — MJ 変換の設定'),('quarry','有 — 採掘範囲と回収物の確認'),('mining well','無 — エンジン直結で動作'),
     ('refinery','有 — 精製フィルタとタンク管理'),('distiller','有 — 蒸留処理の管理'),('tank','有 — 貯蔵液体と容量の確認'),
     ('pump','有 — 汲み上げと貯蔵の確認'),('flood gate','無 — 動力を繋ぐのみ'),
     ('filtered buffer','有 — 通過フィルタの設定'),('chute','有 — 緩衝スロット'),
     ('filler','有 — 充填パターンの指定'),('builder','有 — 製図/テンプレートの指定と素材供給'),
     ('architect','有 — 範囲設定と製図作成'),('blueprint library','有 — 製図/ライブラリ管理'),
     ('assembly table','有 — レシピ選択と素材投入'),('advanced crafting table','有 — レシピ登録と素材収納'),
     ('auto workbench','有 — 見本レシピの登録'),('integration table','有 — ゲートへの改造合成'),
     ('programming table','有 — ロボット用ボードの書き込み'),('charging table','有 — 対象アイテムの充電'),
     ('stamping','有 — 刻印設定'),('packager','有 — 梱包設定'),('replacer','有'),
     ('energy heater','有 — 熱変換'),('heat exchanger','有 — 熱交換'),
     ('zone planner','有 — 範囲のマップ描画'),('requester','有 — 要求アイテムの指定'),
     ('gate copier','有 — ゲート設定の写し'),('gate','有 — トリガーとアクションの設定'),
     ('diamond transport pipe','有 — 色別仕分けフィルタ'),('emerald transport pipe','有 — 搬出フィルタ設定'),
     ('emzuli','有 — 搬出フィルタ設定'),('daizuli','有 — 色指定の振り分け'),('lazuli','有 — 色指定の振り分け'),
     ('lens','有 — 透過フィルタ設定'),('filter','有 — フィルタ条件設定'),('list','有 — リスト編集'),
     ('guide book','有 — 閲覧'),('pipe plug','無'),('facade','無'),('docking station','無 — ドック経由でロボットへ接続'),
    ]
    for k, v in rules:
        if k in e:
            return v
    if 'pipe' in e:
        return '無 — 輸送のみ(一部は接続フィルタ設定あり)'
    return '無'

def gui_ic2(en, desc):
    e = en.lower()
    if re.search(r'transformer|cable|wire|battery|cell|upgrade|plate|dust|ingot|ingot|circuit|part|scaffold|fence|paint|dye|food|coffee|beer|rum|brew|crop|seed|stick|tool|sword|drill|chainsaw|wrench|hammer|cutter|scanner|meter|armor|helmet|boots|pack|jetpack|batpack|hazmat|rubber|resin|powder|nugget|block|ore|water|lava|air|weedex|fertilizer|hydration|grin|treetap|mug|barrel', e):
        mach = re.search(r'reactor|chamber|generator|mill|macerator|extractor|compressor|furnace|recycler|fabricator|canning|electrolyzer|miner|pump|terraformer|teleporter|matron|nalyzer|safe|o-mat|batbox|mfe|mfsu|cesu|chargepad|magnetizer|fermenter|distiller|replicator|sorter|detector|splitter', e)
        if mach:
            pass
        else:
            return '無'
    else:
        mach = True
    m2 = re.search(r'reactor chamber|cooling|plating|disperser|isotope|depleted|enriched|uranium cell|heat', e)
    if m2:
        return '無'
    rules = [
     ('nuclear reactor','有 — 燃料セル/冷却部品の配置と稼働管理'),('reactor','有 — 炉心の管理画面'),
     ('generator','有 — 燃料投入と発電状態の表示'),('geothermal','有 — 溶岩燃料と発電状態'),
     ('water mill','有 — 燃料(水)と発電状態'),('wind mill','有 — 発電状態の確認'),('solar','有 — 発電状態の確認'),
     ('batbox','有 — 蓄電量と入出力面の確認'),('mfe','有 — 蓄電量の確認'),('mfsu','有 — 蓄電量の確認'),('cesu','有 — 蓄電量の確認'),
     ('macerator','有 — 粉砕処理'),('extractor','有 — 抽出処理'),('compressor','有 — 圧縮処理'),
     ('electric furnace','有 — 電気精錬'),('recycler','有 — スクラップ化'),('matter fabricator','有 — UU物質生成'),
     ('mass fabricator','有 — UU物質生成'),('canning','有 — 缶詰/封入処理'),('electrolyzer','有 — 電気分解'),
     ('miner','有 — 自動採掘の管理'),('pump','有 — 汲み上げ管理'),('terraformer','有 — テラフォーム設定'),
     ('teleporter','有 — テレポート設定'),('crop-matron','有 — 農薬/肥料の散布管理'),('cropnaly','有 — 作物の鑑定'),
     ('cropnalyzer','有 — 作物の鑑定'),('personal safe','有 — 個人ロッカー'),('trade-o-mat','有 — 自動販売設定'),
     ('energy-o-mat','有 — 電力販売設定'),('chargepad','有 — 充電パッド管理'),('magnetizer','有'),
     ('fermenter','有 — 発酵管理'),('distiller','有 — 蒸留管理'),('replicator','有 — 複製管理'),
     ('fluid','有 — 液体処理画面'),('sorter','有 — 仕分け設定'),('detector','無 — 信号出力のみ'),('splitter','無 — 信号分岐のみ'),
     ('transformer','無 — 変圧のみで画面なし'),
    ]
    for k, v in rules:
        if k in e:
            return v
    if re.search(r'mill|macerator|compressor|fabricator|generator|reactor|furnace|recycler|electrolyzer|terraformer|teleporter|matron|canning|distiller|fermenter|batbox|mfsu|mfe|cesu|nalyzer|safe|mat|chargepad', e):
        return '有 — 機械操作画面'
    return '無'

def gui_pr(en, desc):
    e = en.lower()
    rules = [
     ('project bench','有 — レシピ登録と素材収納'),('auto crafter','有 — 自動クラフト設定'),
     ('battery box','有 — 蓄電量の確認'),('charging bench','有 — ツールの充電'),
     ('deployer','有 — 使用動作の設定'),('electrotine generator','有 — 発電状態の確認'),
     ('induction furnace','有 — 製錬処理'),('backpack','有 — 収納スロット'),
     ('packaging table','有 — IC設計のパッケージ化'),('plotting table','有 — IC回路の設計'),
     ('lithography','有 — 回路描画'),('ic workbench','有 — IC実装作業'),
     ('frame motor','不明 — フレーム駆動の制御方式'),('gate','無 — ドライバーでワールド内設定'),
    ]
    for k, v in rules:
        if k in e:
            return v
    return '無'

def gui_lp(en, desc):
    e = en.lower()
    none_kw = ['chip','upgrade','disk','paper','rod','part','guide book?']
    rules = [
     ('power junction','有 — LP 変換とエネルギー管理'),('power provider','有 — 給電範囲の設定'),
     ('soldering','有 — 溶接作業台GUI'),('remote order','有 — 遠隔発注画面'),('request','有 — 要求画面'),
     ('crafting','有 — クラフト設定'),('chassis','有 — モジュール装着スロット'),('satellite','有 — 衛星先設定'),
     ('firewall','有 — フィルタ設定'),('programmer','有 — プログラム設定'),('logistics','有 — ロジスティクス設定画面'),
    ]
    for k in none_kw:
        if k in e:
            return '無'
    for k, v in rules:
        if k in e:
            return v
    return '有 — ロジスティクスレンチによる設定画面'

GUI_FN = {'BC': gui_bc, 'IC2': gui_ic2, 'PR': gui_pr, 'LP': gui_lp}

# ---------- 5) image index ----------
IMGIDX = {}
for p in glob.glob(f'/ws/docs/{IMGDIR}/*.*'):
    b = os.path.basename(p)
    if b == 'MANIFEST.tsv':
        continue
    pre = b.split('_', 1)[0]
    IMGIDX.setdefault(pre, []).append(b)

def normimg(s):
    return re.sub(r'[^0-9A-Za-zぁ-んァ-ヶ一-鿿々ー]+', '', s).lower()

IMG_ALIAS = {
    ('bc', 'oil'): 'bc_BlockOil.png',
    ('bc', 'oilbucket'): 'bc_CraftingOilBucket.png',
    ('bc', 'stirlingengine'): 'bc_stoneengine.png',
    ('bc', 'redstoneengine'): 'bc_woodengine.png',
    ('bc', 'combustionengine'): 'bc_ironenjine.png',
    ('bc', 'creativeengine'): 'bc_CreativeEngine.png',
    ('bc', 'ironandgate'): 'bc_AssemblyTableIronGate.gif',
    ('bc', 'goldandgate'): 'bc_AssemblyTableGoldGate.gif',
    ('bc', 'diamondandgate'): 'bc_AssemblyTableDiamondGate.gif',
    ('bc', 'emeraldandgate'): 'bc_AssemblyTableEmeraldGate.gif',
    ('bc', 'quartzandgate'): 'bc_AssemblyTableQuartzGate.gif',
    ('bc', 'redpipewire'): 'bc_AssemRedWire.png',
    ('bc', 'bluepipewire'): 'bc_AssemBlueWire.png',
    ('bc', 'greenpipewire'): 'bc_AssemGreenWire.png',
    ('bc', 'yellowpipewire'): 'bc_AssemYellowWire.png',
    ('ic2', 'goldcableinsulated2x'): 'ic2_2xIns-Gold_Cable.gif',
    ('ic2', 'hvcableinsulated2x'): 'ic2_2xIns-HV_Cable.gif',
    ('ic2', 'hvcableinsulated4x'): 'ic2_4xIns-HV_Cable.gif',
    ('ic2', 'insulatedgoldcable'): 'ic2_Insulated_Gold_Cable.gif',
    ('ic2', 'insulatedhvcable'): 'ic2_Insurated_HV_Cable.gif',
    ('ic2', 'glassfibre'): 'ic2_glass_fiber4.png',
    ('ic2', 'rawcarbonfible'): 'ic2_炭素繊維.gif',
    ('lp', 'basiclogisticspipe'): 'lp_BasicPipe.gif',
    ('lp', 'providerlogisticspipe'): 'lp_ProviderPipe.gif',
    ('lp', 'providerlogisticspipemk2'): 'lp_ProviderPipeMk2.gif',
    ('lp', 'requestlogisticspipe'): 'lp_RequestPipe.gif',
    ('lp', 'requestlogisticspipemk2'): 'lp_RequestPipeMk2.gif',
    ('lp', 'supplierlogisticspipe'): 'lp_SupplierPipe.png',
    ('lp', 'remotepipe'): 'lp_RemotePipe.png',
    ('lp', 'logisticscraftingtable'): 'lp_LogisticsCraftTable.gif',
}

def find_image(prefix, en, jp):
    for v in re.split(r'[/,]| or ', en):
        key = (prefix, normimg(v))
        if key in IMG_ALIAS and os.path.exists(f'/ws/docs/{IMGDIR}/{IMG_ALIAS[key]}'):
            return IMG_ALIAS[key]
    files = IMGIDX.get(prefix, [])
    stems = [(f, normimg(re.sub(r'^[a-z0-9]+_', '', f).rsplit('.', 1)[0])) for f in files]
    cands = [normimg(v) for v in re.split(r'[/,]| or ', en) if v.strip()] + [normimg(jp)]
    best = None
    for c in cands:
        if not c:
            continue
        for f, st in stems:
            stx = st[5:] if st.startswith('craft') else st
            if st == c or stx == c or 'craft' + c == st:
                return f
    for c in cands:
        if len(c) < 4:
            continue
        for f, st in stems:
            stx = st[5:] if st.startswith('craft') else st
            if len(stx) >= 4 and (c in stx or stx in c) and len(c) >= 4:
                ratio = min(len(c), len(stx)) / max(len(c), len(stx))
                if ratio > 0.55 and (best is None or ratio > best[1]):
                    best = (f, ratio)
    return best[0] if best else None

# ---------- 6) parse TECH_ITEMS.md ----------
lines = open(DOC).read().split('\n')
head = lines[:19]  # through <!-- 凡例終了 -->
MODMAP = {'BuildCraft': 'BC', 'IndustrialCraft': 'IC2', 'Project:Red': 'PR', 'LogisticsPipes': 'LP'}

nodes = []  # ('mod',title) ('cat',title) ('div',text) ('item',dict) 
cur_mod = None
i = 19
while i < len(lines):
    ln = lines[i]
    m = re.match(r'^##\s+(.*)', ln)
    if m:
        t = m.group(1).strip()
        cur_mod = None
        for k, v in MODMAP.items():
            if k in t:
                cur_mod = v
        nodes.append(('mod', t)); i += 1; continue
    m = re.match(r'^###\s+(.*)', ln)
    if m:
        nodes.append(('cat', m.group(1).strip())); i += 1; continue
    if ln.startswith('|'):
        row = [c.strip().replace('\\|', '|') for c in re.split(r'(?<!\\)\|', ln.strip().strip('|'))]
        if len(row) >= 3 and not set(row[0]) <= set('-: ') and row[0] != '名称':
            jp, en, rc = row[0], row[1], row[2]
            if en == '' or (jp == '' and en == ''):
                i += 1; continue
            if norm_tokens(jp) and not re.search(r'[^ -~]', jp) and re.search(r'[^ -~]', en):
                jp, en = en, jp  # swapped cols
            en = en.replace('Ugprade', 'Upgrade')
            rc_clean = nfkc(strip_html(re.sub(r'<br\s*/?>', ' ', rc)))
            if rc_clean.strip() == '-':
                nodes.append(('div', f'{jp} ({en})' if en else jp))
            else:
                officials = re.findall(r'公式レシピ[:：]\s*(\([^)]*\))?\s*(.*?)\s*->\s*([^\s<]+)', rc_clean)
                text = re.sub(r'<br\s*/?>', '\n', rc)
                text = strip_html(text).split('公式レシピ')[0].strip()
                nodes.append(('item', {'jp': jp, 'en': en, 'recipe': nfkc(text), 'officials': officials, 'mod': cur_mod}))
        i += 1; continue
    i += 1

# ---------- 7) build name maps from table rows ----------
for kind, n in nodes:
    if kind == 'item':
        JP2EN.setdefault(n['jp'].strip(), re.split(r'[/,]| or ', n['en'])[0].strip())
        for v in re.split(r'[/,]| or ', n['en']):
            if v.strip():
                TABLE_N2J.setdefault(norm_tokens(v), n['jp'])
                TABLE_EN.setdefault(norm_tokens(v), v.strip())
# also JP2EN from ITEMS.md descriptions
for pool in ITEM_DESC.values():
    for dd in pool.values():
        JP2EN.setdefault(dd['jp'], dd['en'])

def find_recipe(officials, en):
    ids = []
    for kind, ings, res in officials:
        rid = re.sub(r'x\d+$', '', res).strip()
        ids.append(rid)
        if '#' in rid:
            ids.append(rid.split('#')[0])
    for rid in ids:
        if rid in RECIPES:
            return rid, RECIPES[rid]
    # slug match on last segment
    e_slugs = {slug(v) for v in re.split(r'[/,]| or ', en) if v.strip()}
    for rid in RECIPES:
        seg = rid.split(':')[-1].split('#')[0]
        if slug(seg) in e_slugs:
            return rid, RECIPES[rid]
    return None, None

def parse_text_recipe(txt):
    """returns list of recipe variants; each variant = list of rendered term strings"""
    t = re.split(r'[→➝]|(?<!-)->', txt)[0]
    t = t.replace('＋', '+')
    out = []
    for var in re.split(r'\|\||\s*/\s*', t):
        terms = []
        for p in var.split('+'):
            p = re.sub(r'【[^】]*】', '', p).strip()
            if not p or p in ('なし', '-', 'なし (クラフト不可)', 'なし(クラフト不可)'):
                continue
            slot = []
            for a in re.split(r'\s+or\s+|\s+または\s+|(?<=[\dぁ-んァ-ヶ一-鿿])or(?=[ぁ-んァ-ヶ一-鿿])', p):
                a = a.strip()
                if not a:
                    continue
                # strip RF-cost '(25,000RF)', 'ずつ', '=' notes, '(※...)' notes, parens
                a = re.sub(r'[（(]\s*[\d,]+\s*RF\s*[)）]', '', a)
                a = re.sub(r'ずつ\s*$', '', a)
                a = re.sub(r'\s*=.*$', '', a)
                a = re.sub(r'[（(]※[^）)]*[)）]', '', a)
                a = re.sub(r'[（(][^）)]*$', '', a)
                a = re.sub(r'[（(]([^）)]*)[)）]', r'\1', a)
                a = a.strip().rstrip(':：').strip().strip('（）()').strip()
                if not a or a in ('なし', '-'):
                    continue
                if re.search(r'[。、をにがへ]|個\)|バージョン|v1\.|追加|発生', a):
                    return []  # prose, not a recipe -> keep raw text
                m = re.match(r'^(.*?)\s*[x×:]?\s*(\d+)\s*個?\s*$', a)
                m2 = re.match(r'^(\d+)\s*([^\d].*)$', a)  # '4ダイヤブロック'
                if m2 and not m:
                    a = f'{m2.group(2)}{m2.group(1)}'
                    m = re.match(r'^(.*?)\s*[x×:]?\s*(\d+)\s*個?\s*$', a)
                if m and m.group(1):
                    nm = m.group(1).strip(' :（）()')
                    nm = re.sub(r'^\d+', '', nm).strip()
                    if nm and nm not in ('なし', '-'):
                        slot.append(f'`{jp_to_en(nm)}` x{int(m.group(2))}')
                elif a:
                    slot.append(f'`{jp_to_en(a)}`')
            if slot:
                terms.append(' / '.join(slot))
        if terms:
            out.append(terms)
    return out

# ---------- 8) emit ----------
out = list(head)
out.append('')
num = 0
used_ids = {}
stats = {'items': 0, 'grid': 0, 'desc': 0, 'img': 0, 'off': 0}
mod_counts = {}
gui_counts = {'有': 0, '無': 0, '不明': 0}

def emit_div(text):
    out.append(f'#### {text}')
    out.append('')

for kind, n in nodes:
    if kind == 'mod':
        out.append(f'## {n}')
        out.append('')
    elif kind in ('cat', 'div'):
        continue
    elif kind == 'item':
        num += 1
        stats['items'] += 1
        mod = n['mod'] or 'BC'
        jp, en, raw = n['jp'], n['en'], n['recipe']
        officials = n['officials']
        rid, rec = find_recipe(officials, en)
        origin = officials[0][2].strip() if officials else ''
        origin = re.sub(r'x\d+$', '', origin).replace('buildcraftcorere:', 'buildcraftcore:')
        if officials:
            stats['off'] += 1
        # new id
        en_first = re.split(r'[/,]| or ', en)[0].strip()
        s = slug(en_first)
        nid = f'bcn:{s}'
        if nid in used_ids and used_ids[nid] != en:
            k = 2
            while f'bcn:{s}_{k}' in used_ids:
                k += 1
            nid = f'bcn:{s}_{k}'
        used_ids[nid] = en
        # recipe + grid
        grid = None
        terms = []
        rcount = 1
        if rec:
            rcount = rec['count']
            if rec['shaped']:
                stats['grid'] += 1
                pat = rec['pattern']
                seen, order = [], []
                for row in pat:
                    for ch in row:
                        if ch not in (' ', '0') and ch not in seen:
                            seen.append(ch); order.append(ch)
                letters = {ch: chr(65 + idx) for idx, ch in enumerate(order)}
                grid = ['`' + ''.join(letters.get(c, '0') if c not in (' ', '0') else '0' for c in row) + '`' for row in pat]
                for ch in order:
                    cnt = sum(row.count(ch) for row in pat)
                    terms.append(f'`{ingr_name_en(rec["key"].get(ch, ch))}` x{cnt}')
            else:
                for ch, tok in rec['key'].items():
                    terms.append(f'`{ingr_name_en(tok)}` x1')
                grid = ['`[不定形]`']
        variant_lines = []
        if not terms:
            for vt in parse_text_recipe(raw):
                variant_lines.append(' + '.join(vt))
            my = re.search(r'[→➝]\s*(\d+)\s*個', raw)
            if my:
                rcount = int(my.group(1))
            grid = grid or ['`[000]`', '`[000]`', '`[000]`']
        # recipe line
        if terms:
            recipe_line = ' + '.join(terms) + f' -> `{en_first}` x{rcount}'
        elif variant_lines:
            recipe_line = ' / '.join(variant_lines) + f' -> `{en_first}` x{rcount}'
        else:
            txt = raw.strip()
            recipe_line = 'なし' if (txt in ('なし', '-', '', 'なし (クラフト不可)')) else txt
        # desc (same mod first, then others)
        d = ''
        pools = [ITEM_DESC.get(mod, {})] + [v for k, v in ITEM_DESC.items() if k != mod]
        for v in re.split(r'[/,]| or ', en):
            for pool in pools:
                dd = pool.get(norm_tokens(v))
                if dd and dd['desc']:
                    d = dd['desc']; break
            if d:
                break
        if d:
            stats['desc'] += 1
        # gui
        g = GUI_FN[mod](en, d)
        if g.startswith('有'):
            gui_counts['有'] += 1
        elif g.startswith('不明'):
            gui_counts['不明'] += 1
        else:
            gui_counts['無'] += 1
        # img
        img = find_image(mod.lower(), en, jp)
        if img:
            stats['img'] += 1
        # emit
        out.append(f'### [ ] #{num:03d} {en} / {jp} / {mod}')
        out.append('')
        out.append(f'* 名称: {en} / 日本語: {jp} / Origin: `{origin or "-"}` / New: `{nid}`')
        out.append(f'* レシピ: {recipe_line}')
        out.append(f'* レシピ配置図: ' + ' / '.join(grid))
        out.append(f'* 概要: {d}')
        out.append(f'* 専用GUI: {g}')
        out.append(f'* 実装方法:')
        out.append('')
        if img:
            out.append(f'![]({IMGDIR}/{img})')
        else:
            out.append('<!-- 画像なし -->')
        out.append('')

# append (N 項目) count at end of each mod section
final = []
cnt = None
for ln in out:
    if ln.startswith('## '):
        if cnt:
            final.append(f'({cnt} 項目)')
            final.append('')
        cnt = 0
    elif re.match(r'^### \[ \] #\d', ln):
        cnt += 1
    final.append(ln)
if cnt:
    final.append(f'({cnt} 項目)')
    final.append('')
open(DOC, 'w').write('\n'.join(final))
print('stats:', stats, 'gui:', gui_counts)
print('lines:', len(out))
