#!/usr/bin/env python3
"""Spell Engine 用のアクティブスキル(spell)JSON を生成する.

形式: data/cfb/spell/<id>.json
対象: 4職業 x 15アクティブ = 60スペル
school は実績のある spell_power:arcane/fire/frost/healing に限定.

impact ヘルパー:
  dmg(coef,knock=0.3)      DAMAGE
  heal(coef)               HEAL
  eff(id,dur,lvl=0)        STATUS_EFFECT
  tp(mode)                 TELEPORT (FORWARD/BEHIND_TARGET)
  vel(x,y,z,frame="LOOK")  VELOCITY
  aggro()                  AGGRO
  fire(dur)                FIRE
target 種別: aim / aim_fb(回復,自己代替可) / area / caster / aim_req(狙い必須)
"""
import json, os

OUT = "/ws/resources/datapacks/cfb_skills/data/cfb/spell"

def dmg(c, k=0.3):
    return {"action": {"type": "DAMAGE", "damage": {"spell_power_coefficient": c, "knockback": k}}}
def heal(c):
    return {"action": {"type": "HEAL", "heal": {"spell_power_coefficient": c}}}
def eff(eid, dur, lvl=0):
    return {"action": {"type": "STATUS_EFFECT", "status_effect": {"effect_id": eid, "duration": float(dur), "amplifier": lvl}}}
def tp(mode):
    return {"action": {"type": "TELEPORT", "teleport": {"mode": mode, "intent": "HELPFUL", "forward": {"distance": 8}}}}
def vel(x, y, z, intent="HELPFUL"):
    return {"action": {"type": "VELOCITY", "velocity": {"frame": "LOOK", "push": {"x": x, "y": y, "z": z}, "reset_velocity": False, "intent": intent}}}
def aggro():
    return {"action": {"type": "AGGRO", "aggro": {"pull": True}}}
def fire(dur):
    return {"action": {"type": "FIRE", "fire": {"duration": float(dur)}}}
def summon(ent, ttl):
    return {"action": {"type": "SPAWN", "spawns": [{"entity_type_id": ent, "time_to_live_seconds": float(ttl)}]}}

# id: (school, tier, cast秒, cd秒, exhaust, rune, target, impacts)
S = {
 "guardian": {
  "bulwark":      ("arcane",3, 0.5, 30,  0.4, None, "caster", [eff("minecraft:resistance",8,1)]),
  "charge":       ("arcane",1, 0.0, 20,  0.3, None, "caster", [vel(0,0.3,1.6)]),
  "barrage":      ("arcane",1, 0.0, 15,  0.2, None, "area",   [dmg(0.8)]),
  "roar":         ("arcane",2, 0.5, 45,  0.4, None, "area",   [aggro(), eff("minecraft:weakness",6)]),
  "resolve":      ("arcane",2, 0.5, 45,  0.3, None, "caster", [eff("minecraft:resistance",8),eff("minecraft:strength",8)]),
  "bastion":      ("arcane",2, 0.5, 50,  0.4, None, "area",   [eff("minecraft:resistance",15)]),
  "wall":         ("arcane",2, 0.5, 40,  0.4, "runes:protection_stone", "area", [eff("minecraft:resistance",10,1)]),
  "cover":        ("arcane",3, 0.5, 35,  0.4, None, "area",   [eff("minecraft:resistance",8),eff("minecraft:absorption",8)]),
  "flesh_roar":   ("arcane",2, 0.5, 50,  0.4, None, "area",   [aggro(),eff("minecraft:strength",10)]),
  "second_wind":  ("arcane",3, 1.0, 70,  0.5, None, "caster", [heal(3.0),eff("minecraft:regeneration",5)]),
  "seismic":      ("arcane",3, 0.5, 25,  0.4, None, "area",   [dmg(1.5,1.2),eff("minecraft:slowness",3)]),
  "def_field":    ("arcane",3, 0.5, 60,  0.5, None, "area",   [eff("minecraft:resistance",12,1)]),
  "vow":          ("arcane",4, 0.5, 120, 0.6, None, "area",   [eff("minecraft:resistance",30,1),eff("minecraft:fire_resistance",30)]),
  "aegis":        ("arcane",4, 0.5, 60,  0.6, "runes:protection_stone", "area", [eff("minecraft:absorption",15,2),eff("minecraft:resistance",15)]),
  "unbreakable":  ("arcane",5, 0.5, 150, 0.8, None, "caster", [eff("minecraft:resistance",6,3),eff("minecraft:regeneration",6,1)]),
 },
 "assassin": {
  "flash":        ("arcane",1, 0.0, 15,  0.3, None, "aim",    [dmg(1.0)]),
  "dash":         ("arcane",1, 0.0, 12,  0.2, None, "caster", [vel(0,0.2,1.8)]),
  "hamstring":    ("arcane",1, 0.0, 20,  0.3, None, "aim",    [eff("minecraft:slowness",6,1),dmg(0.4)]),
  "blood_trail":  ("arcane",1, 0.0, 30,  0.2, None, "aim",    [eff("minecraft:glowing",15)]),
  "afterimage":   ("arcane",2, 0.0, 30,  0.3, None, "caster", [eff("minecraft:speed",8,1),eff("minecraft:jump_boost",8)]),
  "smoke":        ("arcane",2, 0.5, 40,  0.4, "runes:protection_stone", "area", [eff("minecraft:blindness",6),eff("minecraft:slowness",4)]),
  "shadow_strike":("arcane",2, 0.0, 30,  0.4, None, "aim_req",[tp("BEHIND_TARGET"),dmg(1.5)]),
  "needle":       ("arcane",2, 0.0, 35,  0.4, "runes:arcane_stone", "aim", [dmg(0.8),eff("minecraft:slowness",4,1),eff("minecraft:poison",4)]),
  "abyss":        ("arcane",3, 0.5, 40,  0.4, None, "caster", [eff("minecraft:strength",5,1),eff("minecraft:speed",5)]),
  "taunt":        ("arcane",2, 0.5, 45,  0.3, None, "area",   [aggro()]),
  "stealth":      ("arcane",3, 0.5, 60,  0.5, None, "caster", [eff("minecraft:invisibility",8),eff("minecraft:speed",8)]),
  "deathblow":    ("arcane",4, 0.0, 50,  0.5, None, "aim",    [dmg(3.0,0.6)]),
  "vanish":       ("arcane",3, 0.0, 70,  0.4, None, "caster", [eff("minecraft:invisibility",3),vel(0,0.3,1.0)]),
  "clone":        ("arcane",4, 0.5, 80,  0.5, None, "caster", [summon("minecraft:villager",10),eff("minecraft:invisibility",4),eff("minecraft:speed",4,1)]),
  "burial":       ("arcane",5, 0.5, 180, 0.8, "runes:arcane_stone", "aim", [dmg(5.0,1.0)]),
 },
 "mage": {
  "firebolt":     ("fire",   1, 0.0, 8,   0.2, "runes:fire_stone", "aim", [dmg(1.2),fire(2)]),
  "frostbolt":    ("frost",  1, 0.0, 10,  0.2, "runes:frost_stone", "aim", [dmg(1.0),eff("minecraft:slowness",4)]),
  "blink":        ("arcane", 1, 0.0, 20,  0.3, None, "caster", [tp("FORWARD")]),
  "cache":        ("arcane", 2, 0.5, 60,  0.3, None, "caster", [eff("minecraft:absorption",12,1)]),
  "mana_shield":  ("arcane", 2, 0.5, 45,  0.4, None, "caster", [eff("minecraft:absorption",10,2)]),
  "inferno":      ("fire",   2, 0.5, 20,  0.4, "runes:fire_stone", "area", [dmg(1.8),fire(4)]),
  "frost_domain": ("frost",  2, 0.5, 40,  0.5, "runes:frost_stone", "area", [eff("minecraft:slowness",8,1),dmg(0.5)]),
  "flare":        ("arcane", 2, 0.0, 25,  0.4, None, "aim",    [dmg(0.8),eff("minecraft:glowing",10)]),
  "surge":        ("arcane", 2, 0.5, 50,  0.4, None, "caster", [eff("minecraft:haste",10,1),eff("minecraft:speed",10)]),
  "chain":        ("arcane", 3, 0.5, 30,  0.5, "runes:arcane_stone", "aim", [dmg(1.5)]),
  "shockbolt":    ("arcane", 3, 0.0, 12,  0.3, "runes:arcane_stone", "aim", [dmg(1.3)]),
  "attune":       ("arcane", 3, 0.5, 70,  0.5, None, "caster", [eff("minecraft:strength",15),eff("minecraft:resistance",15)]),
  "gravity":      ("arcane", 4, 0.5, 50,  0.6, "runes:arcane_stone", "area", [eff("minecraft:slowness",6,2),eff("minecraft:levitation",2)]),
  "burst":        ("fire",   4, 1.0, 25,  0.6, "runes:fire_stone", "area", [dmg(2.5,1.0),fire(5)]),
  "final":        ("arcane", 5, 2.0, 200, 1.0, "runes:arcane_stone", "area", [dmg(5.0,1.5),eff("minecraft:slowness",5,1)]),
 },
 "paladin": {
  "quick":        ("healing",1, 0.0, 8,   0.2, "runes:healing_stone", "aim_fb", [heal(0.8)]),
  "heal":         ("healing",1, 0.5, 15,  0.3, "runes:healing_stone", "aim_fb", [heal(1.5)]),
  "purify":       ("healing",2, 0.5, 30,  0.4, None, "aim_fb", [heal(0.5),eff("minecraft:regeneration",5)]),
  "rally":        ("healing",2, 0.5, 40,  0.4, None, "area",   [eff("minecraft:speed",12),eff("minecraft:strength",8)]),
  "consecrate":   ("healing",1, 0.5, 45,  0.4, None, "area",   [dmg(0.8),heal(0.8)]),
  "blessing":     ("healing",2, 0.5, 60,  0.5, None, "area",   [eff("minecraft:strength",20),eff("minecraft:luck",20)]),
  "ward":         ("healing",2, 0.5, 45,  0.5, None, "aim_fb", [eff("minecraft:absorption",10,2),eff("minecraft:resistance",10)]),
  "regen":        ("healing",2, 0.5, 30,  0.4, None, "aim_fb", [eff("minecraft:regeneration",20)]),
  "hymn":         ("healing",3, 0.5, 60,  0.5, None, "area",   [eff("minecraft:strength",15,1),eff("minecraft:speed",15)]),
  "martyr":       ("healing",3, 0.5, 55,  0.5, None, "area",   [heal(1.5),eff("minecraft:resistance",10)]),
  "circle":       ("healing",3, 0.5, 50,  0.5, "runes:healing_stone", "area", [heal(2.0),eff("minecraft:regeneration",8)]),
  "eruption":     ("healing",3, 0.5, 35,  0.4, None, "area",   [dmg(1.2,0.8),heal(0.5)]),
  "sanctuary":    ("healing",4, 1.0, 90,  0.6, "runes:healing_stone", "area", [eff("minecraft:regeneration",15),eff("minecraft:resistance",15)]),
  "repose":       ("healing",4, 1.5, 120, 0.7, "runes:healing_stone", "aim_fb", [heal(4.0),eff("minecraft:regeneration",10)]),
  "grand":        ("healing",5, 2.0, 180, 0.9, "runes:healing_stone", "area", [heal(3.0),eff("minecraft:regeneration",20,1),eff("minecraft:resistance",20)]),
 },
}

TARGETS = {
    "aim":     {"type": "AIM", "aim": {"sticky": True}},
    "aim_fb":  {"type": "AIM", "aim": {"sticky": True, "use_caster_as_fallback": True}},
    "aim_req": {"type": "AIM", "aim": {"required": True, "sticky": True}},
    "area":    {"type": "AREA", "area": {"range": 6.0, "include_caster": True}},
    "caster":  {"type": "CASTER"},
}

def make_spell(job, sid, v):
    school, tier, cast, cd, exh, rune, target, impacts = v
    cost = {"exhaust": exh * 4.0, "cooldown": {"group": "weapon", "duration": round(cd * 0.6), "proportional": True}}
    if rune:
        cost["item"] = {"id": rune}
    return {
        "school": f"spell_power:{school}",
        "range": 16.0,
        "tier": tier,
        "group": f"cfb_{job}",
        "learn": {},
        "active": {"cast": {"duration": cast,
            "animation": {"id": "spell_engine:one_handed_projectile_charge" if target=="aim" else "spell_engine:one_handed_area_charge"}}},
        "release": {"animation": {"id": "spell_engine:one_handed_projectile_release" if target=="aim" else "spell_engine:one_handed_area_release"}},
        "target": TARGETS[target],
        "impacts": impacts,
        "cost": cost,
    }

os.makedirs(OUT, exist_ok=True)
n = 0
for job, spells in S.items():
    for sid, v in spells.items():
        d = make_spell(job, sid, v)
        with open(f"{OUT}/{job}_{sid}.json", "w") as f:
            json.dump(d, f, ensure_ascii=False, indent=2)
        n += 1
print(f"{n} spells generated")
