#!/usr/bin/env python3
"""BCN crops/飲料系リソース生成: テクスチャ PNG + モデル/blockstate/items/recipe/loot JSON"""
import zlib, struct, os, json

BASE = "/ws/app/src/main/resources"
T_ITEM = f"{BASE}/assets/bcn/textures/item"
T_BLOCK = f"{BASE}/assets/bcn/textures/block"
M_ITEM = f"{BASE}/assets/bcn/models/item"
M_BLOCK = f"{BASE}/assets/bcn/models/block"
ITEMS = f"{BASE}/assets/bcn/items"
BS = f"{BASE}/assets/bcn/blockstates"
REC = f"{BASE}/data/bcn/recipe"
LOOT = f"{BASE}/data/bcn/loot_table/blocks"
for d in (T_ITEM, T_BLOCK, M_ITEM, M_BLOCK, ITEMS, BS, REC, LOOT):
    os.makedirs(d, exist_ok=True)

def png(rows, palette):
    out = []
    for y in range(16):
        row = b"\x00"
        line = rows[y] if y < len(rows) else ""
        for x in range(16):
            ch = line[x] if x < len(line) else "."
            row += bytes(palette.get(ch, (0, 0, 0, 0)))
        out.append(row)
    raw = b"".join(out)
    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d))
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))

def tex(name, rows, palette, folder=T_ITEM):
    with open(f"{folder}/{name}.png", "wb") as f:
        f.write(png(rows, palette))
    print("tex", name)

def j(path, obj):
    with open(path, "w") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print("json", os.path.basename(path))

WOOD = (139, 94, 44, 255)
WOOD_D = (90, 60, 25, 255)
GRAY = (90, 90, 100, 255)
GRAY_L = (150, 150, 160, 255)
GREEN = (60, 190, 80, 255)
GREEN_D = (30, 110, 40, 255)
RED = (200, 60, 60, 255)
BROWN = (120, 85, 45, 255)
BROWN_L = (165, 125, 75, 255)
BROWN_D = (80, 55, 28, 255)
TAN = (210, 185, 130, 255)
PURPLE = (150, 80, 180, 255)
BLUE_D = (45, 55, 95, 255)
STONE = (125, 125, 130, 255)
STONE_D = (80, 80, 85, 255)

# --- アイテムテクスチャ ---

tex("crop_stick", [
"................",
".XX..........XX.",
"..XX........XX..",
"...XX......XX...",
"....XX....XX....",
".....XX..XX.....",
"......XXXX......",
".......XX.......",
".......XX.......",
"......XXXX......",
".....XX..XX.....",
"....XX....XX....",
"...XX......XX...",
"..XX........XX..",
".XX..........XX.",
"................",
], {"X": WOOD})

tex("crop_analyzer", [
"................",
"..XXXXXXXXXX....",
"..XGGGGGGGGX....",
"..XGXXXXXXGX....",
"..XGGGGGGGGX....",
"..XXXXXXXXXX....",
"..X........X....",
"..X...RRR..X....",
"..X...R.R..X....",
"..X...RRR..X....",
"..X........X....",
"..XXXXXXXXXX....",
".....X....X.....",
"................",
], {"X": GRAY, "G": GREEN, "R": RED})

tex("crop_seed_bag", [
"................",
"......XXXX......",
".....X....X.....",
"....X......X....",
"...X........X...",
"..XXXXXXXXXXXX..",
"..XFFFFFFFFFFX..",
"..XFSSFSSFSSFX..",
"..XFSSFSSFSSFX..",
"..XFFFFFFFFFFX..",
"..XFFFFFFFFFFX..",
"..XFFFFFFFFFFX..",
"...XFFFFFFFFX...",
"....XXXXXXXX....",
"................",
"................",
], {"X": BROWN_D, "F": BROWN_L, "S": BROWN_D})

tex("fertilizer", [
"................",
"................",
"....XX...XX.....",
"...XXXX.XXXX....",
"...XXXX.XXXX....",
"....XX...XX.....",
"..XX....XX..XX..",
".XXXX..XXXX.XX..",
".XXXX..XXXX.XX..",
"..XX....XX......",
"....XX......XX..",
"...XXXX....XXXX.",
"...XXXX....XXXX.",
"....XX......XX..",
"................",
"................",
], {"X": TAN})

tex("hydrated_coal", [
"................",
"................",
"......XXX.......",
"....XXBXBXX.....",
"...XBXXXXBXX....",
"..XBXXXXXXXX....",
"..XXXXXBXXXX....",
".XBXBXXXXXBXX...",
".XXXXXXXXXXBX...",
".XXBXXXXBXXXX...",
"..XXXXXBXXXBX...",
"..XBXXXXXBXXX...",
"...XXXXXXXXX....",
".....XXXXXX.....",
"................",
"................",
], {"X": BLUE_D, "B": (70, 90, 150, 255)})

tex("weed_ex", [
"................",
".......XXX......",
"......X...X.....",
"......X...X..XX.",
"......XXXXX.X...",
"......XGGGX.....",
"......XGGGX.....",
"......XGGGX.....",
"......XG.GX.....",
"......XGGGX.....",
"......XGGGX.....",
"......XGGGX.....",
"......XXXXX.....",
"................",
"................",
"................",
], {"X": GRAY_D if (GRAY_D := (60, 60, 65, 255)) else GRAY, "G": GREEN})

tex("grin_powder", [
"................",
"................",
"................",
"........X.......",
".......XXX......",
"....X..XXX..X...",
"...XXX.XXXX.XX..",
"..XXXXXXXXXXXX..",
".XXXXXXXXXXXXXX.",
".XXXXXXXXXXXXXX.",
"..XXXXXXXXXXXX..",
"....XXXXXXXX....",
"................",
"................",
"................",
"................",
], {"X": PURPLE})

tex("hops", [
"................",
".......GG.......",
"......GGGG......",
".....GGGGGG.....",
"....GGGGGGGG....",
".....GGGGGG.....",
"......GGGG......",
".....GGGGGG.....",
"....GGGGGGGG....",
".....GGGGGG.....",
"......GGGG......",
".......GG.......",
"........X.......",
"................",
"................",
"................",
], {"G": GREEN, "X": GREEN_D})

tex("coffee_beans", [
"................",
"................",
"....XX....XX....",
"...XXXX..XXXX...",
"..XXDXX.XXDXX...",
"..XXDXX.XXDXX...",
"...XXXX..XXXX...",
"....XX....XX....",
".......XX.......",
"......XXXX......",
".....XXDXXX.....",
".....XXDXXX.....",
"......XXXX......",
".......XX.......",
"................",
"................",
], {"X": BROWN, "D": BROWN_D})

tex("coffee_powder", [
"................",
"................",
"................",
"................",
"........X.......",
".......XXX......",
"......XXXXX.....",
"....X.XXXXX.X...",
"...XXXXXXXXXXX..",
"..XXXXXXXXXXXX..",
".XXXXXXXXXXXXXX.",
".XXXXXXXXXXXXXX.",
"................",
"................",
"................",
"................",
], {"X": BROWN_D})

STONE_MUG_ROWS = [
"................",
"................",
"...XXXXXXXXX....",
"...XSSSSSSSX.X..",
"...XSSSSSSSX.X..",
"...XSSSSSSSX.X..",
"...XLLLLLLLX.X..",
"...XLLLLLLLX.X..",
"...XLLLLLLLX.X..",
"...XLLLLLLLX.X..",
"...XLLLLLLLX.X..",
"...XLLLLLLLX....",
"...XSSSSSSSX....",
"...XXXXXXXXX....",
"................",
"................",
]

def mug(name, liquid):
    tex(name, STONE_MUG_ROWS, {"X": STONE_D, "S": STONE, "L": liquid})

mug("stone_mug", (0, 0, 0, 0))
mug("mug_rum", (190, 120, 40, 255))
mug("mug_rum_aged", (140, 80, 20, 255))
mug("mug_youngster", (235, 220, 150, 255))
mug("mug_beer", (230, 185, 60, 255))
mug("mug_ale", (190, 130, 30, 255))
mug("mug_black_stuff", (25, 20, 25, 255))
mug("mug_cold_coffee", (110, 85, 65, 255))
mug("mug_dark_coffee", (55, 35, 22, 255))
mug("mug_coffee", (95, 60, 35, 255))

# --- ブロックテクスチャ ---

tex("crop_stick", [
"XX.XXX.XX.XXX.XX",
"X.XX.X.X.X.X.XX.",
"XX.XXX.XX.XXX.XX",
"X.X.X.XX.X.X.X.X",
"XX.XXX.XX.XXX.XX",
".XX.X.XX.X.X.X.X",
"XX.XXX.XX.XXX.XX",
"X.XX.X.X.X.X.XX.",
"XX.XXX.XX.XXX.XX",
"X.X.X.XX.X.X.X.X",
"XX.XXX.XX.XXX.XX",
".XX.X.XX.X.X.X.X",
"XX.XXX.XX.XXX.XX",
"X.XX.X.X.X.X.XX.",
"XX.XXX.XX.XXX.XX",
"X.X.X.XX.X.X.X.X",
], {"X": WOOD}, T_BLOCK)

def crop_stage(name, height):
    rows = []
    for y in range(16):
        line = ""
        for x in range(16):
            ch = "."
            if 16 - y <= height:
                if x == 8:
                    ch = "G"
                elif height >= 8 and 16 - y <= height - 3 and x in (6, 10):
                    ch = "G"
                elif height >= 12 and 16 - y <= height - 6 and x in (5, 7, 9, 11):
                    ch = "G"
            line += ch
        rows.append(line)
    tex(name, rows, {"G": GREEN}, T_BLOCK)

for s, h in (("crop_stage0", 4), ("crop_stage1", 8), ("crop_stage2", 12), ("crop_stage3", 16)):
    crop_stage(s, h)

tex("booze_barrel_side", [
"DDDDDDDDDDDDDDDD",
"WWWWWWWWWWWWWWWW",
"DDDDDDDDDDDDDDDD",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"WWWWWWWWWWWWWWWW",
"DDDDDDDDDDDDDDDD",
"WWWWWWWWWWWWWWWW",
], {"W": WOOD, "D": WOOD_D}, T_BLOCK)

tex("booze_barrel_top", [
"DDDDDDDDDDDDDDDD",
"DWWWWWWWWWWWWWWD",
"DWWWWWWWWWWWWWWD",
"DWWDDDDDDDDDDWWD",
"DWWDDDDDDDDDDWWD",
"DWWDDWWWWWWDWWWD".replace("W", "D", 0),
"DWWDWWWWWWWDDWWD",
"DWWDWWWWWWWDDWWD",
"DWWDWWWWWWWDDWWD",
"DWWDWWWWWWWDDWWD",
"DWWDWWWWWWWDDWWD",
"DWWDDDDDDDDDDWWD",
"DWWDWWWWWWWDWWWD",
"DWWWWWWWWWWWWWWD",
"DWWWWWWWWWWWWWWD",
"DDDDDDDDDDDDDDDD",
], {"W": WOOD, "D": WOOD_D}, T_BLOCK)

# --- ブロックモデル ---

def stick_elements(extra):
    els = [
        {"from": [7, 0, 0], "to": [9, 12, 16],
         "faces": {f: {"texture": "#stick"} for f in ("north", "south", "east", "west", "up", "down")}},
        {"from": [0, 0, 7], "to": [16, 12, 9],
         "faces": {f: {"texture": "#stick"} for f in ("north", "south", "east", "west", "up", "down")}},
    ]
    els += extra
    return els

def cross_elements():
    els = []
    for fr, to in (([7, 0, 0], [9, 12, 16]), ([0, 0, 7], [16, 12, 9])):
        els.append({"from": fr, "to": to,
                    "rotation": {"origin": [8, 6, 8], "axis": "y", "angle": 45, "rescale": True},
                    "faces": {f: {"texture": "#stick"} for f in ("north", "south", "east", "west", "up", "down")}})
    return els

def crop_overlay(height, tex_name):
    uv_lo = 16 - height
    return [
        {"from": [0.8, 0, 8], "to": [15.2, height, 8],
         "rotation": {"origin": [8, height / 2, 8], "axis": "y", "angle": 45, "rescale": True},
         "shade": False,
         "faces": {"north": {"uv": [0, uv_lo, 16, 16], "texture": "#crop"},
                   "south": {"uv": [0, uv_lo, 16, 16], "texture": "#crop"}}},
        {"from": [8, 0, 0.8], "to": [8, height, 15.2],
         "rotation": {"origin": [8, height / 2, 8], "axis": "y", "angle": 45, "rescale": True},
         "shade": False,
         "faces": {"west": {"uv": [0, uv_lo, 16, 16], "texture": "#crop"},
                   "east": {"uv": [0, uv_lo, 16, 16], "texture": "#crop"}}},
    ]

for s, h in ((0, 4), (1, 8), (2, 12), (3, 16)):
    j(f"{M_BLOCK}/crop_stick_s{s}.json", {
        "textures": {"stick": "bcn:block/crop_stick", "crop": f"bcn:block/crop_stage{s}",
                     "particle": "bcn:block/crop_stick"},
        "elements": stick_elements(crop_overlay(h, f"crop_stage{s}")),
    })
    j(f"{M_BLOCK}/crop_stick_cross_s{s}.json", {
        "textures": {"stick": "bcn:block/crop_stick", "crop": f"bcn:block/crop_stage{s}",
                     "particle": "bcn:block/crop_stick"},
        "elements": stick_elements(cross_elements() + crop_overlay(h, f"crop_stage{s}")),
    })

j(f"{M_BLOCK}/booze_barrel.json", {
    "parent": "minecraft:block/cube_column",
    "textures": {"end": "bcn:block/booze_barrel_top", "side": "bcn:block/booze_barrel_side"},
})

# --- blockstates ---

variants = {}
for cross in ("false", "true"):
    for s in range(4):
        variants[f"cross={cross},growth={s}"] = {
            "model": f"bcn:block/crop_stick_{'cross_' if cross == 'true' else ''}s{s}"}
j(f"{BS}/crop_stick.json", {"variants": variants})
j(f"{BS}/booze_barrel.json", {"variants": {"": {"model": "bcn:block/booze_barrel"}}})

# --- アイテムモデル + items 定義 ---

SIMPLE_ITEMS = ["crop_analyzer", "crop_seed_bag", "fertilizer", "hydrated_coal", "weed_ex",
                "grin_powder", "hops", "coffee_beans", "coffee_powder", "stone_mug",
                "mug_rum", "mug_rum_aged", "mug_youngster", "mug_beer", "mug_ale",
                "mug_black_stuff", "mug_cold_coffee", "mug_dark_coffee", "mug_coffee",
                "crop_stick"]

for name in SIMPLE_ITEMS:
    j(f"{M_ITEM}/{name}.json", {"parent": "item/generated",
                                "textures": {"layer0": f"bcn:item/{name}"}})
    j(f"{ITEMS}/{name}.json", {"model": {"type": "minecraft:model",
                                         "model": f"bcn:item/{name}"}})

j(f"{ITEMS}/booze_barrel.json", {"model": {"type": "minecraft:model",
                                           "model": "bcn:block/booze_barrel"}})
j(f"{M_ITEM}/booze_barrel.json", {"parent": "bcn:block/booze_barrel"})

# --- レシピ ---

def recipe(name, obj):
    j(f"{REC}/{name}.json", obj)

recipe("crop_stick", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": ["S S", "S S"], "key": {"S": "minecraft:stick"},
    "result": {"id": "bcn:crop_stick", "count": 4}})
recipe("fertilizer", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["minecraft:bone_meal", "minecraft:rotten_flesh"],
    "result": {"id": "bcn:fertilizer", "count": 2}})
recipe("hydrated_coal", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["minecraft:coal", "minecraft:water_bucket"],
    "result": {"id": "bcn:hydrated_coal"}})
recipe("grin_powder", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["minecraft:spider_eye"],
    "result": {"id": "bcn:grin_powder"}})
recipe("weed_ex", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["minecraft:redstone", "bcn:grin_powder"],
    "result": {"id": "bcn:weed_ex"}})
recipe("crop_analyzer", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": [" G ", "RCR", "R R"],
    "key": {"G": "minecraft:glass", "R": "minecraft:redstone", "C": "minecraft:copper_ingot"},
    "result": {"id": "bcn:crop_analyzer"}})
recipe("stone_mug", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": ["S S", "S S", "SSS"],
    "key": {"S": "minecraft:stone"},
    "result": {"id": "bcn:stone_mug"}})
recipe("booze_barrel", {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": [" P ", " L ", " P "],
    "key": {"P": "#minecraft:planks", "L": "#minecraft:logs"},
    "result": {"id": "bcn:booze_barrel"}})
recipe("coffee_powder", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["bcn:coffee_beans"],
    "result": {"id": "bcn:coffee_powder"}})
recipe("mug_cold_coffee", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["bcn:stone_mug", "bcn:coffee_powder", "minecraft:water_bottle"],
    "result": {"id": "bcn:mug_cold_coffee"}})
recipe("mug_dark_coffee", {
    "type": "minecraft:smelting", "cookingtime": 100, "experience": 0.1,
    "ingredient": "bcn:mug_cold_coffee",
    "result": {"id": "bcn:mug_dark_coffee"}})
recipe("mug_coffee", {
    "type": "minecraft:crafting_shapeless", "category": "misc",
    "ingredients": ["bcn:mug_dark_coffee", "minecraft:sugar", "minecraft:milk_bucket"],
    "result": {"id": "bcn:mug_coffee"}})

# --- loot tables ---

for name in ("crop_stick", "booze_barrel"):
    j(f"{LOOT}/{name}.json", {
        "type": "minecraft:block",
        "pools": [{"entries": [{"type": "minecraft:item", "name": f"bcn:{name}"}], "rolls": 1}],
        "random_sequence": f"bcn:blocks/{name}"})

print("done")
