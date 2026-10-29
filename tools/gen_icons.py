#!/usr/bin/env python3
"""cfb スペルアイコン生成: 16x16 PNG (school色ベース + グリフシンボル)"""
import zlib, struct, os, re

OUT = "/ws/resources/assets/cfb/textures/spell"
os.makedirs(OUT, exist_ok=True)

SCHOOLS = {
 "fire":   ((70,20,10,255),(255,120,30,255)),
 "frost":  ((15,40,80,255),(150,210,255,255)),
 "arcane": ((45,20,70,255),(200,140,255,255)),
 "healing":((60,50,10,255),(255,230,140,255)),
}
GLYPHS = {
 "bolt":   ["......XX......",".....XX.......","....XX........","...XX.........","..XX..........",".XX...........","XXXXXXXXX.....",".....XXXX.....",".......XX.....","......XX......",".....XX.......","....XX........","...XX.........","..XX..........","..............",".............."],
 "burst":  ["....XX..XX....",".....XX.XX....","..X...XXX...X.","..X..XXXXX..X.","....XXXXXXX...","..XXXXXXXXXX..","X.XXXXXXXXX.X.","XXXXXXXXXXXXX.","X.XXXXXXXXX.X.","..XXXXXXXXXX..","....XXXXXXX...","..X..XXXXX..X.","..X...XXX...X.",".....XX.XX....","....XX..XX....",".............."],
 "shield": ["..XXXXXXXXX...",".XXXXXXXXXXX..",".XX.......XX..",".XX..X....XX..",".XX..X....XX..",".XX..X....XX..",".XX..X....XX..",".XX..X....XX..",".XX..X...XX...","..XX.X..XX....","..XX.X.XX.....","...XXXXX......","....XXXX......",".....XX.......","..............",".............."],
 "tele":   ["....X.....X...","....XX...XX...",".....XX.XX....","......XXX.....","......X.X.....","..XXXXXXX.XX..",".X..X...X...X.","..X.X...X.X...","...XXXXXXX....","....X...X.....",".....XXX.X....","....XX.XX.....","...XX...XX....","..............","..............",".............."],
 "dash":   ["..............","X.............","XX............","XXXXXXXX......","XXXXXXXXX.....","XXXXXXXXXX....","XXXXXXXXX.....","XXXXXXXX......","XX............","X.............","..............","....XX........",".....XX.......","......XX......","..............",".............."],
 "buff":   ["......XX......",".....XXXX.....","....XXXXXX....","...XX.XX.XX...","..X..XXXX..X..",".....XXXX.....",".....XXXX.....",".....XXXX.....","....XXXXXX....","...X......X...","..X........X..","..............","..............","..............","..............",".............."],
 "heal":   ["..............",".....XXXX.....",".....XXXX.....",".....XXXX.....","..XXXXXXXXXX..","..XXXXXXXXXX..","..XXXXXXXXXX..","..XXXXXXXXXX..",".....XXXX.....",".....XXXX.....",".....XXXX.....","..............","..............","..............","..............",".............."],
 "eye":    ["..............","..............","...XXXXXXXX...","..X........X..",".X..XXXXXX..X.","X..X.XX.X.X..X","X..X.XXX..X..X",".X..XXXXXX..X.","..X........X..","...XXXXXXXX...","..............","..............","..............","..............","..............",".............."],
 "aggro":  ["..............","....XXXXX.....","...X.....X....","..X..X.X..X...","..X..X.X..X...","..X..X.X..X...","..X..X.X..X...","...X.....X....","....XXXXX.....",".....X.X......","....XX.XX.....","...X..X..X....","..............","..............","..............",".............."],
 "flame":  [".....XX.......","....XXX.......","....XXX..X....","...XXXX.XX....","...XXXXXXXX...","..XXXXXXXXX...","..XXXXXXXXX...",".XXXXXXXXXXX..",".XXXXXXXXXXX..",".XXXXXXXXXXX..","..XXXXXXXXX...","...XXXXXXX....","....XXXXX.....","..............","..............",".............."],
 "slow":   ["..............","..XXXXXXX.....",".X.......X....",".X..X....X....",".X..X....X....",".X..X....X....",".X..X....X....",".X..X....X....",".X.......X....","..XXXXXXX.....","..............","..............","..............","..............","..............",".............."],
 "mark":   ["......XX......",".....X..X.....","....X.XX.X....","...X.XXXX.X...","..X.XX..XX.X..","..X.X....X.X..","...X.XXXX.X...","....X.XX.X....",".....X..X.....","......XX......","......XX......","......XX......","......XX......","..............","..............",".............."],
 "regen":  ["..............","..............","....XXXXXX....","...X......X...","..X...XX...X..","..X..XXXX..X..","..X...XX...X..","...X......X...","....XXXXXX....","......XX......",".....XXXX.....","..............","..............","..............","..............",".............."],
 "cleanse":["..............","..XX......XX..","...XX....XX...","....XX..XX....",".....XXXX.....","......XX......",".....XXXX.....","....XX..XX....","...XX....XX...","..XX......XX..","..............","..............","..............","..............","..............",".............."],
 "death":  ["....XXXXX.....","...X.....X....","..X.X...X.X...","..X.X.X.X.X...","..X.X...X.X...","..X..XXX..X...","...X.....X....","....XXXXX.....","......X.......","....XXXXX.....","......X.......","....X...X.....","..............","..............","..............",".............."],
}

SPELLS = {
 # mage
 "mage_firebolt":("fire","bolt"),"mage_frostbolt":("frost","bolt"),"mage_shockbolt":("arcane","bolt"),
 "mage_inferno":("fire","flame"),"mage_burst":("fire","burst"),"mage_final":("arcane","burst"),
 "mage_chain":("arcane","bolt"),"mage_flare":("arcane","mark"),"mage_blink":("arcane","tele"),
 "mage_cache":("arcane","shield"),"mage_mana_shield":("arcane","shield"),"mage_surge":("arcane","buff"),
 "mage_attune":("arcane","buff"),"mage_frost_domain":("frost","slow"),"mage_gravity":("arcane","slow"),
 # guardian
 "guardian_bulwark":("arcane","shield"),"guardian_bastion":("arcane","shield"),"guardian_wall":("arcane","shield"),
 "guardian_def_field":("arcane","shield"),"guardian_aegis":("arcane","shield"),"guardian_cover":("arcane","shield"),
 "guardian_unbreakable":("arcane","shield"),"guardian_vow":("arcane","buff"),"guardian_resolve":("arcane","buff"),
 "guardian_charge":("arcane","dash"),"guardian_barrage":("arcane","burst"),"guardian_seismic":("arcane","burst"),
 "guardian_roar":("arcane","aggro"),"guardian_flesh_roar":("arcane","aggro"),"guardian_second_wind":("arcane","heal"),
 # assassin
 "assassin_flash":("arcane","bolt"),"assassin_dash":("arcane","dash"),"assassin_vanish":("arcane","eye"),
 "assassin_stealth":("arcane","eye"),"assassin_clone":("arcane","eye"),"assassin_smoke":("arcane","eye"),
 "assassin_afterimage":("arcane","buff"),"assassin_abyss":("arcane","buff"),"assassin_taunt":("arcane","aggro"),
 "assassin_hamstring":("arcane","slow"),"assassin_needle":("arcane","bolt"),"assassin_blood_trail":("arcane","mark"),
 "assassin_shadow_strike":("arcane","tele"),"assassin_deathblow":("arcane","death"),"assassin_burial":("arcane","death"),
 # paladin
 "paladin_quick":("healing","heal"),"paladin_heal":("healing","heal"),"paladin_repose":("healing","heal"),
 "paladin_circle":("healing","heal"),"paladin_martyr":("healing","heal"),"paladin_purify":("healing","cleanse"),
 "paladin_regen":("healing","regen"),"paladin_sanctuary":("healing","regen"),"paladin_grand":("healing","regen"),
 "paladin_blessing":("healing","buff"),"paladin_hymn":("healing","buff"),"paladin_rally":("healing","buff"),
 "paladin_ward":("healing","shield"),"paladin_consecrate":("healing","burst"),"paladin_eruption":("healing","burst"),
}

def png(glyph, bg, fg):
    rows, data = [], []
    for y in range(16):
        row = b"\x00"
        for x in range(16):
            row += bytes(fg) if y < len(glyph) and x < len(glyph[y]) and glyph[y][x] == "X" else bytes(bg)
        rows.append(row)
    raw = b"".join(rows)
    def chunk(t, d): return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d))
    return (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b""))

n = 0
for sid, (school, glyph) in SPELLS.items():
    bg, fg = SCHOOLS[school]
    with open(f"{OUT}/{sid}.png", "wb") as f:
        f.write(png(GLYPHS[glyph], bg, fg))
    n += 1
print(n, "icons")
