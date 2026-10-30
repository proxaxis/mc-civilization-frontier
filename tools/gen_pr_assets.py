#!/usr/bin/env python3
"""Project:Red 系アセット生成 (task-219).

生成物:
- blockstates: insulated_wire_<16色>, bundled_cable, ic_gate, frame, frame_motor,
  microblock, boule, bt_battery_box, solar_panel
- models/block: iwire_<c>_*/bwire_* (wire_* をテクスチャ差替), frame_*, frame_motor,
  microblock_*, boule, bt_battery_box, solar_panel, iwire_<c>_item, bwire_item
- models/item + items/: 素アイテム一式
- textures: 上記に必要な生成 PNG
- data: recipes, loot_tables, tags(insulated_wires, mineable/pickaxe 追記)
- lang: en_us/ja_jp 追記
"""
import json, os, struct, zlib

RES = "/ws/app/src/main/resources"
A = f"{RES}/assets/bcn"
D = f"{RES}/data"

COLORS = {
    "white": (233, 236, 236), "orange": (234, 126, 53), "magenta": (189, 68, 179),
    "light_blue": (58, 175, 217), "yellow": (248, 197, 39), "lime": (112, 185, 25),
    "pink": (237, 141, 172), "gray": (62, 68, 71), "light_gray": (156, 157, 151),
    "cyan": (21, 137, 145), "purple": (121, 42, 172), "blue": (53, 57, 157),
    "brown": (114, 71, 40), "green": (84, 109, 27), "red": (161, 39, 34),
    "black": (20, 21, 25),
}

written = 0

def w(path, text):
    global written
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(text)
    written += 1

def wj(path, obj):
    w(path, json.dumps(obj, indent=2, ensure_ascii=False) + "\n")

def png16(path, pixels, colors):
    """pixels: 16文字x16行のシンボル -> colors: {ch:(r,g,b,a)} ('.' は透過推奨)"""
    rows = []
    for y in range(16):
        row = b"\x00"
        for x in range(16):
            ch = pixels[y][x] if y < len(pixels) and x < len(pixels[y]) else "."
            row += bytes(colors.get(ch, (0, 0, 0, 0)))
        rows.append(row)
    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(b"".join(rows))) + chunk(b"IEND", b""))
    global written
    written += 1

def solid(c, a=255):
    return (c[0], c[1], c[2], a)

def dark(c, f=0.6):
    return (int(c[0] * f), int(c[1] * f), int(c[2] * f), 255)

def lite(c, f=0.4):
    return (min(255, int(c[0] + (255 - c[0]) * f)),
            min(255, int(c[1] + (255 - c[1]) * f)),
            min(255, int(c[2] + (255 - c[2]) * f)), 255)

def simple_item_tex(name, main, accent=None):
    """16x16: 枠+内側フラット + 簡易模様 (accent があれば中央 4x4)"""
    p = ["................"] * 16
    px = [list(r) for r in p]
    for y in range(3, 13):
        for x in range(3, 13):
            px[y][x] = "M"
    for i in (2, 13):
        for j in range(2, 14):
            px[i][j] = px[j][i] = "B"
    if accent:
        for y in range(6, 10):
            for x in range(6, 10):
                px[y][x] = "A"
    cols = {"M": solid(main), "B": dark(main, 0.55)}
    if accent:
        cols["A"] = solid(accent)
    png16(f"{A}/textures/item/{name}.png", ["".join(r) for r in px], cols)

def wire_tex(name, c, on):
    """ワイヤーテクスチャ: 中央横筋 + 端ドット"""
    body = lite(c, 0.55) if on else solid(c)
    edge = lite(c, 0.85) if on else dark(c, 0.6)
    px = []
    for y in range(16):
        row = ""
        for x in range(16):
            if y in (6, 7, 8, 9):
                row += "M" if (x not in (0, 15)) else "B"
            else:
                row += "."
        px.append(row)
    png16(f"{A}/textures/block/{name}.png", px, {"M": body, "B": edge})

def block_tex(name, main, border_c=None, accent=None):
    px = []
    for y in range(16):
        row = ""
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                row += "B"
            elif accent and 5 <= x <= 10 and 5 <= y <= 10:
                row += "A"
            else:
                row += "M"
        px.append(row)
    cols = {"M": solid(main), "B": solid(border_c or dark(main)[:3])}
    if accent:
        cols["A"] = solid(accent)
    png16(f"{A}/textures/block/{name}.png", px, cols)

def frame_tex():
    px = []
    wood = (150, 105, 55)
    for y in range(16):
        row = ""
        for x in range(16):
            if x < 2 or x > 13 or y < 2 or y > 13:
                row += "M"
            else:
                row += "."
        px.append(row)
    png16(f"{A}/textures/block/frame.png", px, {"M": solid(wood)})

def simple_model(name):
    wj(f"{A}/models/item/{name}.json",
       {"parent": "item/generated", "textures": {"layer0": f"bcn:item/{name}"}})

def item_def(name, model):
    wj(f"{A}/items/{name}.json",
       {"model": {"type": "minecraft:model", "model": model}})

def block_item(name, model=None):
    item_def(name, model or f"bcn:block/{name}")

def item_entry(name, tex=None, accent=None):
    """素アイテム: テクスチャ + item model + items def"""
    simple_item_tex(name, tex or (200, 200, 200), accent)
    simple_model(name)
    item_def(name, f"bcn:item/{name}")

def loot(name, drop=None):
    wj(f"{D}/bcn/loot_table/blocks/{name}.json", {
        "type": "minecraft:block",
        "pools": [{"entries": [{"type": "minecraft:item", "name": drop or f"bcn:{name}"}], "rolls": 1}],
        "random_sequence": f"bcn:blocks/{name}"})

def recipe_shapeless(name, result_id, count, ingredients, category="misc"):
    wj(f"{D}/bcn/recipe/{name}.json", {
        "type": "minecraft:crafting_shapeless", "category": category,
        "ingredients": ingredients,
        "result": {"id": result_id, "count": count}})

def recipe_shaped(name, result_id, count, pattern, key, category="misc"):
    wj(f"{D}/bcn/recipe/{name}.json", {
        "type": "minecraft:crafting_shaped", "category": category,
        "pattern": pattern, "key": key,
        "result": {"id": result_id, "count": count}})

def recipe_smelting(name, result_id, ingredient, xp=0.1, category="misc"):
    wj(f"{D}/bcn/recipe/{name}.json", {
        "type": "minecraft:smelting", "category": category,
        "cookingtime": 200, "experience": xp, "ingredient": ingredient,
        "result": {"id": result_id}})

# ---------- 1. 絶縁ワイヤー / ケーブル束 ----------

redwire_bs = open(f"{A}/blockstates/redwire.json").read()
wire_models = [f for f in os.listdir(f"{A}/models/block") if f.startswith("wire_")]

for cname in COLORS:
    w(f"{A}/blockstates/insulated_wire_{cname}.json",
      redwire_bs.replace("bcn:block/wire_", f"bcn:block/iwire_{cname}_"))
    for mf in wire_models:
        src = open(f"{A}/models/block/{mf}").read()
        dst = mf.replace("wire_", f"iwire_{cname}_", 1)
        w(f"{A}/models/block/{dst}",
          src.replace("bcn:block/wire_off", f"bcn:block/iwire_{cname}_off")
             .replace("bcn:block/wire_on", f"bcn:block/iwire_{cname}_on"))
    wire_tex(f"iwire_{cname}_off", COLORS[cname], False)
    wire_tex(f"iwire_{cname}_on", COLORS[cname], True)
    block_item(f"insulated_wire_{cname}", f"bcn:block/iwire_{cname}_item")
    loot(f"insulated_wire_{cname}")
    recipe_shapeless(f"insulated_wire_{cname}", f"bcn:insulated_wire_{cname}", 1,
                     ["bcn:redwire", f"minecraft:{cname}_wool"], "redstone")

w(f"{A}/blockstates/bundled_cable.json",
  redwire_bs.replace("bcn:block/wire_", "bcn:block/bwire_"))
for mf in wire_models:
    src = open(f"{A}/models/block/{mf}").read()
    dst = mf.replace("wire_", "bwire_", 1)
    w(f"{A}/models/block/{dst}",
      src.replace("bcn:block/wire_off", "bcn:block/bwire_off")
         .replace("bcn:block/wire_on", "bcn:block/bwire_on"))
wire_tex("bwire_off", (90, 90, 105), False)
wire_tex("bwire_on", (90, 90, 105), True)
block_item("bundled_cable", "bcn:block/bwire_item")
loot("bundled_cable")
recipe_shapeless("bundled_cable", "bcn:bundled_cable", 3,
                 ["#bcn:insulated_wires", "#bcn:insulated_wires", "#bcn:insulated_wires"], "redstone")
recipe_shapeless("bundled_cable_color", "bcn:bundled_cable", 1,
                 ["bcn:bundled_cable", "#minecraft:wool"], "redstone")

# ---------- 2. ic_gate ----------

w(f"{A}/blockstates/ic_gate.json", open(f"{A}/blockstates/gate_or.json").read())
block_item("ic_gate", "bcn:block/gate_or_floor")
loot("ic_gate")
recipe_shapeless("ic_gate", "bcn:ic_gate", 1,
                 ["bcn:plate", "bcn:silicon_chip", "bcn:bundled_plate"], "redstone")

# ---------- 3. frame / frame_motor ----------

def plate_model(name, attach, t0, t1, tex):
    """attach 側から t0..t1 の板モデル"""
    boxes = {
        "down":  ([0, t0, 0], [16, t1, 16]), "up": ([0, 16 - t1, 0], [16, 16 - t0, 16]),
        "north": ([0, 0, t0], [16, 16, t1]), "south": ([0, 0, 16 - t1], [16, 16, 16 - t0]),
        "west":  ([t0, 0, 0], [t1, 16, 16]), "east": ([16 - t1, 0, 0], [16 - t0, 16, 16]),
    }
    fr, to = boxes[attach]
    wj(f"{A}/models/block/{name}.json", {
        "textures": {"0": tex, "particle": tex},
        "elements": [{"from": fr, "to": to, "faces": {
            d: {"texture": "#0"} for d in
            ("down", "up", "north", "south", "west", "east")}}]})

ATTACHES = ("down", "up", "north", "south", "west", "east")

frame_tex()
for a in ATTACHES:
    plate_model(f"frame_{a}", a, 0, 4, "bcn:block/frame")

def attach_for(face, facing):
    if face == "floor":
        return "down"
    if face == "ceiling":
        return "up"
    return {"north": "north", "south": "south", "west": "west", "east": "east"}[facing]

for name in ("frame", "frame_motor"):
    pass

frame_variants = {}
for face in ("floor", "ceiling", "wall"):
    for facing in ("north", "south", "east", "west"):
        a = attach_for(face, facing)
        frame_variants[f"face={face},facing={facing}"] = {"model": f"bcn:block/frame_{a}"}
wj(f"{A}/blockstates/frame.json", {"variants": frame_variants})
block_item("frame", "bcn:block/frame_down")
loot("frame")
recipe_shapeless("frame", "bcn:frame", 2,
                 ["minecraft:stick"] * 8 + ["minecraft:iron_ingot"], "redstone")

block_tex("frame_motor_front", (60, 60, 70), accent=(230, 140, 40))
block_tex("frame_motor_side", (90, 90, 100))
block_tex("frame_motor_top", (75, 75, 85))
wj(f"{A}/models/block/frame_motor.json", {
    "parent": "minecraft:block/orientable",
    "textures": {"side": "bcn:block/frame_motor_side",
                 "top": "bcn:block/frame_motor_top",
                 "front": "bcn:block/frame_motor_front"}})
fm_variants = {
    "facing=down,powered=false": {"model": "bcn:block/frame_motor", "x": 180},
    "facing=down,powered=true": {"model": "bcn:block/frame_motor", "x": 180},
    "facing=up,powered=false": {"model": "bcn:block/frame_motor"},
    "facing=up,powered=true": {"model": "bcn:block/frame_motor"},
    "facing=north,powered=false": {"model": "bcn:block/frame_motor", "x": 90},
    "facing=north,powered=true": {"model": "bcn:block/frame_motor", "x": 90},
    "facing=south,powered=false": {"model": "bcn:block/frame_motor", "x": 90, "y": 180},
    "facing=south,powered=true": {"model": "bcn:block/frame_motor", "x": 90, "y": 180},
    "facing=west,powered=false": {"model": "bcn:block/frame_motor", "x": 90, "y": 90},
    "facing=west,powered=true": {"model": "bcn:block/frame_motor", "x": 90, "y": 90},
    "facing=east,powered=false": {"model": "bcn:block/frame_motor", "x": 90, "y": 270},
    "facing=east,powered=true": {"model": "bcn:block/frame_motor", "x": 90, "y": 270},
}
wj(f"{A}/blockstates/frame_motor.json", {"variants": fm_variants})
block_item("frame_motor")
loot("frame_motor")
recipe_shapeless("frame_motor", "bcn:frame_motor", 1,
                 ["#minecraft:planks", "#minecraft:planks", "minecraft:iron_ingot",
                  "minecraft:redstone", "minecraft:redstone", "bcn:motor",
                  "minecraft:stone", "minecraft:stone", "bcn:electrotine_ingot"], "redstone")

# ---------- 4. microblock ----------

block_tex("microblock", (140, 140, 140), border_c=(95, 95, 95))
for size, t in (("half", 8), ("panel", 4), ("cover", 2)):
    for a in ATTACHES:
        plate_model(f"microblock_{size}_{a}", a, 0, t, "bcn:block/microblock")
mb_variants = {}
for size in ("half", "panel", "cover"):
    for face in ("floor", "ceiling", "wall"):
        for facing in ("north", "south", "east", "west"):
            a = attach_for(face, facing)
            mb_variants[f"face={face},facing={facing},size={size}"] = {
                "model": f"bcn:block/microblock_{size}_{a}"}
wj(f"{A}/blockstates/microblock.json", {"variants": mb_variants})
item_def("microblock", "bcn:block/microblock_half_down")

# ---------- 5. boule / bt_battery_box / solar_panel ----------

block_tex("boule", (200, 200, 205), border_c=(150, 150, 160), accent=(235, 235, 240))
wj(f"{A}/models/block/boule.json",
   {"parent": "minecraft:block/cube_all", "textures": {"all": "bcn:block/boule"}})
wj(f"{A}/blockstates/boule.json", {"variants": {"": {"model": "bcn:block/boule"}}})
block_item("boule")
loot("boule")

block_tex("bt_battery_front", (45, 70, 140), accent=(120, 190, 255))
wj(f"{A}/models/block/bt_battery_box.json", {
    "parent": "minecraft:block/orientable",
    "textures": {"side": "bcn:block/machine_side", "top": "bcn:block/machine_top",
                 "front": "bcn:block/bt_battery_front"}})
wj(f"{A}/blockstates/bt_battery_box.json",
   json.loads(open(f"{A}/blockstates/batbox.json").read().replace(
       "bcn:block/batbox", "bcn:block/bt_battery_box")))
block_item("bt_battery_box")
loot("bt_battery_box")
recipe_shapeless("bt_battery_box", "bcn:bt_battery_box", 1,
                 ["bcn:bt_battery", "bcn:bt_battery", "bcn:bt_battery", "bcn:bt_battery",
                  "#minecraft:planks", "minecraft:iron_ingot", "minecraft:iron_ingot",
                  "minecraft:iron_ingot", "bcn:electrotine_ingot"], "misc")

block_tex("solar_panel", (35, 45, 110), accent=(80, 130, 220))
wj(f"{A}/models/block/solar_panel.json", {
    "parent": "minecraft:block/orientable",
    "textures": {"side": "bcn:block/machine_side", "top": "bcn:block/solar_panel",
                 "front": "bcn:block/solar_panel"}})
wj(f"{A}/blockstates/solar_panel.json",
   json.loads(open(f"{A}/blockstates/batbox.json").read().replace(
       "bcn:block/batbox", "bcn:block/solar_panel")))
block_item("solar_panel")
loot("solar_panel")
recipe_shapeless("solar_panel", "bcn:solar_panel", 1,
                 ["bcn:electrotine_ingot"] + ["bcn:electro_silicon"] * 8, "misc")

# ---------- 6. 中間素材アイテム ----------

ITEMS = {
    "plate": (150, 150, 150, None), "conductive_plate": (150, 150, 150, (200, 40, 40)),
    "wired_plate": (150, 150, 150, (180, 20, 20)), "bundled_plate": (150, 150, 150, (90, 90, 110)),
    "anode": (160, 160, 160, (220, 60, 60)), "cathode": (160, 160, 160, (200, 200, 60)),
    "pointer": (170, 170, 170, (240, 120, 60)),
    "silicon_chip": (120, 120, 140, (60, 200, 120)),
    "energized_silicon_chip": (120, 120, 140, (240, 220, 80)),
    "plate_assembly": (140, 140, 150, (160, 80, 200)),
    "red_ingot": (200, 50, 50, None), "silicon": (160, 160, 180, None),
    "infused_silicon": (160, 160, 180, (220, 60, 60)),
    "energized_silicon": (160, 160, 180, (240, 220, 80)),
    "electro_silicon": (160, 160, 180, (80, 140, 240)),
    "motor": (120, 120, 125, (200, 120, 50)),
    "copper_coil": (190, 110, 60, (120, 60, 30)),
    "iron_coil": (170, 170, 170, (100, 100, 100)),
    "gold_coil": (230, 190, 60, (160, 120, 30)),
    "woven_cloth": (220, 210, 170, (170, 150, 100)),
    "sail": (230, 225, 195, None),
    "red_iron_comp": (140, 60, 50, None), "sand_coal_comp": (190, 170, 120, None),
    "red_silicon_comp": (150, 120, 130, None), "glow_silicon_comp": (210, 190, 120, None),
    "electrotine_ingot": (90, 140, 220, None),
    "electrotine_iron_comp": (110, 120, 160, None),
    "electrotine_silicon_comp": (120, 140, 190, None),
    "screwdriver": (60, 80, 180, (200, 200, 200)),
    "draw_plate": (110, 110, 120, (190, 190, 200)),
    "multimeter": (50, 55, 60, (80, 230, 90)),
    "handsaw": (170, 170, 175, (150, 105, 55)),
    "diamond_handsaw": (170, 170, 175, (90, 220, 215)),
    "ruby_handsaw": (170, 170, 175, (220, 60, 80)),
    "sapphire_handsaw": (170, 170, 175, (60, 90, 220)),
    "peridot_handsaw": (170, 170, 175, (120, 220, 90)),
    "bt_battery": (60, 110, 210, (140, 200, 255)),
}
for name, (r, g, b, accent) in ITEMS.items():
    item_entry(name, (r, g, b), accent)

# ---------- 7. レシピ (中間素材) ----------

recipe_smelting("plate", "bcn:plate", "minecraft:smooth_stone")
recipe_shapeless("conductive_plate", "bcn:conductive_plate", 1, ["bcn:plate", "minecraft:redstone"])
recipe_shapeless("wired_plate", "bcn:wired_plate", 1, ["bcn:plate", "bcn:redwire"])
recipe_shapeless("bundled_plate", "bcn:bundled_plate", 1, ["bcn:plate", "bcn:bundled_cable"])
recipe_shapeless("anode", "bcn:anode", 1, ["bcn:plate"] * 3 + ["minecraft:redstone"] * 4)
recipe_shapeless("cathode", "bcn:cathode", 1, ["bcn:plate", "minecraft:redstone_torch"])
recipe_shapeless("pointer", "bcn:pointer", 1,
                 ["minecraft:stone", "minecraft:redstone_torch", "bcn:plate"])
recipe_shapeless("silicon_chip", "bcn:silicon_chip", 1,
                 ["bcn:infused_silicon"] + ["bcn:plate"] * 3)
recipe_shapeless("energized_silicon_chip", "bcn:energized_silicon_chip", 1,
                 ["bcn:energized_silicon"] + ["bcn:plate"] * 3)
recipe_shapeless("plate_assembly", "bcn:plate_assembly", 1,
                 ["bcn:plate"] * 3 + ["minecraft:stick"] * 2 + ["bcn:wired_plate"] * 2)
recipe_shapeless("red_iron_comp", "bcn:red_iron_comp", 1,
                 ["minecraft:iron_ingot"] + ["minecraft:redstone"] * 8)
recipe_shapeless("sand_coal_comp", "bcn:sand_coal_comp", 1,
                 ["minecraft:coal_block"] + ["minecraft:sand"] * 8)
recipe_shapeless("red_silicon_comp", "bcn:red_silicon_comp", 1,
                 ["bcn:silicon"] + ["minecraft:redstone"] * 8)
recipe_shapeless("glow_silicon_comp", "bcn:glow_silicon_comp", 1,
                 ["bcn:silicon"] + ["minecraft:glowstone_dust"] * 8)
recipe_shapeless("electrotine_iron_comp", "bcn:electrotine_iron_comp", 1,
                 ["minecraft:iron_ingot"] + ["bcn:electrotine_dust"] * 8)
recipe_shapeless("electrotine_silicon_comp", "bcn:electrotine_silicon_comp", 1,
                 ["bcn:silicon"] + ["bcn:electrotine_dust"] * 8)
recipe_smelting("red_ingot", "bcn:red_ingot", "bcn:red_iron_comp", 0.3)
recipe_smelting("boule", "bcn:boule", "bcn:sand_coal_comp", 0.5)
recipe_smelting("infused_silicon", "bcn:infused_silicon", "bcn:red_silicon_comp", 0.3)
recipe_smelting("energized_silicon", "bcn:energized_silicon", "bcn:glow_silicon_comp", 0.3)
recipe_smelting("electrotine_ingot", "bcn:electrotine_ingot", "bcn:electrotine_iron_comp", 0.3)
recipe_smelting("electro_silicon", "bcn:electro_silicon", "bcn:electrotine_silicon_comp", 0.3)
recipe_shaped("motor", "bcn:motor", 1, [" i ", "csc", "rir"], {
    "i": "minecraft:iron_ingot", "c": "bcn:copper_coil",
    "s": "minecraft:stone", "r": "minecraft:redstone"})
recipe_shapeless("copper_coil", "bcn:copper_coil", 1, ["minecraft:copper_ingot"] * 3)
recipe_shapeless("iron_coil", "bcn:iron_coil", 1, ["minecraft:iron_ingot"] * 3)
recipe_shapeless("gold_coil", "bcn:gold_coil", 1, ["minecraft:gold_ingot"] * 3)
recipe_shapeless("woven_cloth", "bcn:woven_cloth", 1, ["minecraft:string"] * 8 + ["minecraft:stick"])
recipe_shapeless("sail", "bcn:sail", 1, ["bcn:woven_cloth"] * 6)
recipe_shapeless("screwdriver", "bcn:screwdriver", 1,
                 ["minecraft:iron_ingot"] * 3 + ["minecraft:blue_dye"] * 2)
recipe_shapeless("draw_plate", "bcn:draw_plate", 1,
                 ["minecraft:iron_block", "minecraft:diamond"])
recipe_shapeless("multimeter", "bcn:multimeter", 1,
                 ["bcn:red_ingot"] * 2 + ["minecraft:black_dye"] * 2
                 + ["minecraft:red_dye", "minecraft:glowstone_dust"], "tools")
recipe_shapeless("bt_battery", "bcn:bt_battery", 1,
                 ["bcn:tin_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot"]
                 + ["bcn:electrotine_dust"] * 6)
recipe_shapeless("handsaw", "bcn:handsaw", 1,
                 ["minecraft:stick"] * 3 + ["minecraft:iron_ingot"] * 2, "tools")
recipe_shapeless("diamond_handsaw", "bcn:diamond_handsaw", 1,
                 ["minecraft:stick"] * 3 + ["minecraft:iron_ingot"] * 2 + ["minecraft:diamond"] * 2, "tools")
recipe_shapeless("ruby_handsaw", "bcn:ruby_handsaw", 1,
                 ["minecraft:stick"] * 3 + ["minecraft:iron_ingot"] * 2 + ["bcn:ruby"] * 2, "tools")
recipe_shapeless("sapphire_handsaw", "bcn:sapphire_handsaw", 1,
                 ["minecraft:stick"] * 3 + ["minecraft:iron_ingot"] * 2 + ["bcn:sapphire"] * 2, "tools")
recipe_shapeless("peridot_handsaw", "bcn:peridot_handsaw", 1,
                 ["minecraft:stick"] * 3 + ["minecraft:iron_ingot"] * 2 + ["bcn:peridot"] * 2, "tools")

# ---------- 8. タグ ----------

wj(f"{D}/bcn/tags/item/insulated_wires.json", {
    "replace": False, "values": [f"bcn:insulated_wire_{c}" for c in COLORS]})

pick_path = f"{D}/minecraft/tags/block/mineable/pickaxe.json"
pick = json.load(open(pick_path))
new_blocks = ([f"bcn:insulated_wire_{c}" for c in COLORS]
              + ["bcn:bundled_cable", "bcn:ic_gate", "bcn:frame", "bcn:frame_motor",
                 "bcn:microblock", "bcn:boule", "bcn:bt_battery_box", "bcn:solar_panel"])
for b in new_blocks:
    if b not in pick["values"]:
        pick["values"].append(b)
wj(pick_path, pick)

# ---------- 9. lang ----------

EN = {
    **{f"block.bcn.insulated_wire_{c}": f"{c.replace('_', ' ').title()} Insulated Wire" for c in COLORS},
    "block.bcn.bundled_cable": "Bundled Cable",
    "block.bcn.ic_gate": "IC Gate",
    "block.bcn.frame": "Support Frame",
    "block.bcn.frame_motor": "Frame Motor",
    "block.bcn.microblock": "Microblock",
    "block.bcn.boule": "Silicon Boule",
    "block.bcn.bt_battery_box": "Battery Box (Blulectric)",
    "block.bcn.solar_panel": "Solar Panel",
    "item.bcn.microblock": "Microblock",
    "item.bcn.plate": "Circuit Plate",
    "item.bcn.conductive_plate": "Conductive Plate",
    "item.bcn.wired_plate": "Wired Plate",
    "item.bcn.bundled_plate": "Bundled Plate",
    "item.bcn.anode": "Anode", "item.bcn.cathode": "Cathode",
    "item.bcn.pointer": "Pointer",
    "item.bcn.silicon_chip": "Silicon Chip",
    "item.bcn.energized_silicon_chip": "Energized Silicon Chip",
    "item.bcn.plate_assembly": "Plate Assembly",
    "item.bcn.red_ingot": "Red Alloy Ingot",
    "item.bcn.silicon": "Silicon Wafer",
    "item.bcn.infused_silicon": "Infused Silicon",
    "item.bcn.energized_silicon": "Energized Silicon",
    "item.bcn.electro_silicon": "Electro Silicon",
    "item.bcn.motor": "Motor",
    "item.bcn.copper_coil": "Copper Coil",
    "item.bcn.iron_coil": "Iron Coil",
    "item.bcn.gold_coil": "Gold Coil",
    "item.bcn.woven_cloth": "Woven Cloth", "item.bcn.sail": "Sail",
    "item.bcn.red_iron_comp": "Red Iron Compound",
    "item.bcn.sand_coal_comp": "Sandy Coal Compound",
    "item.bcn.red_silicon_comp": "Red Silicon Compound",
    "item.bcn.glow_silicon_comp": "Glowing Silicon Compound",
    "item.bcn.electrotine_ingot": "Electrotine Alloy Ingot",
    "item.bcn.electrotine_iron_comp": "Electrotine Iron Compound",
    "item.bcn.electrotine_silicon_comp": "Electrotine Silicon Compound",
    "item.bcn.screwdriver": "Screwdriver",
    "item.bcn.draw_plate": "Draw Plate",
    "item.bcn.multimeter": "Multimeter",
    "item.bcn.handsaw": "Handsaw",
    "item.bcn.diamond_handsaw": "Diamond Handsaw",
    "item.bcn.ruby_handsaw": "Ruby Handsaw",
    "item.bcn.sapphire_handsaw": "Sapphire Handsaw",
    "item.bcn.peridot_handsaw": "Peridot Handsaw",
    "item.bcn.bt_battery": "BT Battery",
}
JA = {
    **{f"block.bcn.insulated_wire_{c}": f"{c.replace('_', ' ')}色の絶縁ワイヤー" for c in COLORS},
    "block.bcn.bundled_cable": "ケーブル束",
    "block.bcn.ic_gate": "集積回路ゲート",
    "block.bcn.frame": "サポートフレーム",
    "block.bcn.frame_motor": "フレームモーター",
    "block.bcn.microblock": "マイクロブロック",
    "block.bcn.boule": "高純度シリコン",
    "block.bcn.bt_battery_box": "蓄電器 (青電力)",
    "block.bcn.solar_panel": "ソーラーパネル",
    "item.bcn.microblock": "マイクロブロック",
    "item.bcn.plate": "ストーンウェハー",
    "item.bcn.conductive_plate": "導線基板",
    "item.bcn.wired_plate": "ワイヤー基板",
    "item.bcn.bundled_plate": "ケーブル束基板",
    "item.bcn.anode": "陽極基板", "item.bcn.cathode": "陰極基板",
    "item.bcn.pointer": "指針基板",
    "item.bcn.silicon_chip": "シリコンチップ基板",
    "item.bcn.energized_silicon_chip": "不純物入りシリコンチップ基板",
    "item.bcn.plate_assembly": "アセンブリ基板",
    "item.bcn.red_ingot": "赤合金インゴット",
    "item.bcn.silicon": "シリコンウェハー",
    "item.bcn.infused_silicon": "レッドウェハー",
    "item.bcn.energized_silicon": "不純物入りシリコンウェハー",
    "item.bcn.electro_silicon": "ブルーウェハー",
    "item.bcn.motor": "モーター",
    "item.bcn.copper_coil": "銅製のコイル",
    "item.bcn.iron_coil": "鉄製のコイル",
    "item.bcn.gold_coil": "金製のコイル",
    "item.bcn.woven_cloth": "帆布", "item.bcn.sail": "帆",
    "item.bcn.red_iron_comp": "赤合金化合物",
    "item.bcn.sand_coal_comp": "砂石炭化合物",
    "item.bcn.red_silicon_comp": "レッドシリコン化合物",
    "item.bcn.glow_silicon_comp": "不純物シリコン化合物",
    "item.bcn.electrotine_ingot": "青合金インゴット",
    "item.bcn.electrotine_iron_comp": "青合金化合物",
    "item.bcn.electrotine_silicon_comp": "ブルーシリコン化合物",
    "item.bcn.screwdriver": "ドライバー",
    "item.bcn.draw_plate": "絞り板",
    "item.bcn.multimeter": "検流計",
    "item.bcn.handsaw": "手ノコ",
    "item.bcn.diamond_handsaw": "ダイヤ手ノコ",
    "item.bcn.ruby_handsaw": "ルビー手ノコ",
    "item.bcn.sapphire_handsaw": "サファイア手ノコ",
    "item.bcn.peridot_handsaw": "ペリドット手ノコ",
    "item.bcn.bt_battery": "青電力電池",
}
for fname, entries in (("en_us.json", EN), ("ja_jp.json", JA)):
    p = f"{A}/lang/{fname}"
    lang = json.load(open(p))
    lang.update(entries)
    wj(p, lang)

print(written, "files")
