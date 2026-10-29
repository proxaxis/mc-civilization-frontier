#!/usr/bin/env python3
"""Spell Engine 用のアクティブスキル(spell)JSON を生成する.

形式: data/cfb/spell/<id>.json
対象: 4職業 x 15アクティブ = 60スペル
school は実績のある spell_power:arcane/fire/frost/healing に限定.
impacts は DAMAGE / HEAL / STATUS_EFFECT / なし の4型.
"""
import json, os

OUT = "/ws/resources/datapacks/cfb_skills/data/cfb/spell"

# id: (school, tier, cast秒, cd秒, exhaust, rune, impact)
# rune: None=素材不要 / impact: ("damage",coef) ("heal",coef) ("effect",effect_id,dur) ("none",)
S = {
 "guardian": {
  "bulwark":      ("arcane", 3, 0.5, 30,  0.4, None, ("effect","minecraft:resistance",8)),
  "charge":       ("arcane", 1, 0.0, 20,  0.3, None, ("damage",1.0)),
  "barrage":      ("arcane", 1, 0.0, 15,  0.2, None, ("damage",0.8)),
  "roar":         ("arcane", 2, 0.5, 45,  0.4, None, ("none",)),
  "resolve":      ("arcane", 2, 0.5, 45,  0.3, None, ("effect","minecraft:resistance",8)),
  "bastion":      ("arcane", 2, 0.5, 50,  0.4, None, ("effect","minecraft:resistance",15)),
  "wall":         ("arcane", 2, 0.5, 40,  0.4, "runes:protection_stone", ("none",)),
  "cover":        ("arcane", 3, 0.5, 35,  0.4, None, ("effect","minecraft:resistance",8)),
  "flesh_roar":   ("arcane", 2, 0.5, 50,  0.4, None, ("effect","minecraft:strength",10)),
  "second_wind":  ("arcane", 3, 1.0, 70,  0.5, None, ("heal",3.0)),
  "seismic":      ("arcane", 3, 0.5, 25,  0.4, None, ("damage",1.5)),
  "def_field":    ("arcane", 3, 0.5, 60,  0.5, None, ("effect","minecraft:resistance",12)),
  "vow":          ("arcane", 4, 0.5, 120, 0.6, None, ("effect","minecraft:fire_resistance",1)),
  "aegis":        ("arcane", 4, 0.5, 60,  0.6, "runes:protection_stone", ("effect","minecraft:absorption",15)),
  "unbreakable":  ("arcane", 5, 0.5, 150, 0.8, None, ("effect","minecraft:resistance",6)),
 },
 "assassin": {
  "flash":        ("arcane", 1, 0.0, 15,  0.3, None, ("damage",1.0)),
  "dash":         ("arcane", 1, 0.0, 12,  0.2, None, ("effect","minecraft:speed",3)),
  "hamstring":    ("arcane", 1, 0.0, 20,  0.3, None, ("effect","minecraft:slowness",6)),
  "blood_trail":  ("arcane", 1, 0.0, 30,  0.2, None, ("none",)),
  "afterimage":   ("arcane", 2, 0.0, 30,  0.3, None, ("effect","minecraft:speed",8)),
  "smoke":        ("arcane", 2, 0.5, 40,  0.4, "runes:protection_stone", ("none",)),
  "shadow_strike":("arcane", 2, 0.0, 30,  0.4, None, ("damage",1.5)),
  "needle":       ("arcane", 2, 0.0, 35,  0.4, "runes:arcane_stone", ("effect","minecraft:slowness",4)),
  "abyss":        ("arcane", 3, 0.5, 40,  0.4, None, ("effect","minecraft:strength",5)),
  "taunt":        ("arcane", 2, 0.5, 45,  0.3, None, ("none",)),
  "stealth":      ("arcane", 3, 0.5, 60,  0.5, None, ("effect","minecraft:invisibility",8)),
  "deathblow":    ("arcane", 4, 0.0, 50,  0.5, None, ("damage",3.0)),
  "vanish":       ("arcane", 3, 0.0, 70,  0.4, None, ("effect","minecraft:invisibility",3)),
  "clone":        ("arcane", 4, 0.5, 80,  0.5, None, ("none",)),
  "burial":       ("arcane", 5, 0.5, 180, 0.8, "runes:arcane_stone", ("damage",5.0)),
 },
 "mage": {
  "firebolt":     ("fire",   1, 0.0, 8,   0.2, "runes:fire_stone", ("damage",1.2)),
  "frostbolt":    ("frost",  1, 0.0, 10,  0.2, "runes:frost_stone", ("damage",1.0)),
  "blink":        ("arcane", 1, 0.0, 20,  0.3, None, ("none",)),
  "cache":        ("arcane", 2, 0.5, 60,  0.3, None, ("none",)),
  "mana_shield":  ("arcane", 2, 0.5, 45,  0.4, None, ("effect","minecraft:absorption",10)),
  "inferno":      ("fire",   2, 0.5, 20,  0.4, "runes:fire_stone", ("damage",1.8)),
  "frost_domain": ("frost",  2, 0.5, 40,  0.5, "runes:frost_stone", ("effect","minecraft:slowness",8)),
  "flare":        ("arcane", 2, 0.0, 25,  0.4, None, ("damage",0.8)),
  "surge":        ("arcane", 2, 0.5, 50,  0.4, None, ("effect","minecraft:haste",10)),
  "chain":        ("arcane", 3, 0.5, 30,  0.5, "runes:arcane_stone", ("damage",1.5)),
  "shockbolt":    ("arcane", 3, 0.0, 12,  0.3, "runes:arcane_stone", ("damage",1.3)),
  "attune":       ("arcane", 3, 0.5, 70,  0.5, None, ("effect","minecraft:strength",15)),
  "gravity":      ("arcane", 4, 0.5, 50,  0.6, "runes:arcane_stone", ("none",)),
  "burst":        ("fire",   4, 1.0, 25,  0.6, "runes:fire_stone", ("damage",2.5)),
  "final":        ("arcane", 5, 2.0, 200, 1.0, "runes:arcane_stone", ("damage",5.0)),
 },
 "paladin": {
  "quick":        ("healing",1, 0.0, 8,   0.2, "runes:healing_stone", ("heal",0.8)),
  "heal":         ("healing",1, 0.5, 15,  0.3, "runes:healing_stone", ("heal",1.5)),
  "purify":       ("healing",2, 0.5, 30,  0.4, None, ("none",)),
  "rally":        ("healing",2, 0.5, 40,  0.4, None, ("effect","minecraft:speed",12)),
  "consecrate":   ("healing",1, 0.5, 45,  0.4, None, ("damage",0.8)),
  "blessing":     ("healing",2, 0.5, 60,  0.5, None, ("effect","minecraft:strength",20)),
  "ward":         ("healing",2, 0.5, 45,  0.5, None, ("effect","minecraft:absorption",10)),
  "regen":        ("healing",2, 0.5, 30,  0.4, None, ("effect","minecraft:regeneration",20)),
  "hymn":         ("healing",3, 0.5, 60,  0.5, None, ("effect","minecraft:strength",15)),
  "martyr":       ("healing",3, 0.5, 55,  0.5, None, ("none",)),
  "circle":       ("healing",3, 0.5, 50,  0.5, "runes:healing_stone", ("heal",2.0)),
  "eruption":     ("healing",3, 0.5, 35,  0.4, None, ("damage",1.2)),
  "sanctuary":    ("healing",4, 1.0, 90,  0.6, "runes:healing_stone", ("effect","minecraft:regeneration",15)),
  "repose":       ("healing",4, 1.5, 120, 0.7, "runes:healing_stone", ("heal",4.0)),
  "grand":        ("healing",5, 2.0, 180, 0.9, "runes:healing_stone", ("effect","minecraft:regeneration",20)),
 },
}

def make_spell(job, sid, v):
    school, tier, cast, cd, exh, rune, imp = v
    cost = {"exhaust": exh * 4.0, "cooldown": {"group": "weapon", "duration": cd, "proportional": True}}
    if rune:
        cost["item"] = {"id": rune}
    spell = {
        "school": f"spell_power:{school}",
        "range": 16.0,
        "tier": tier,
        "group": f"cfb_{job}",
        "learn": {},
        "active": {
            "cast": {
                "duration": cast,
                "animation": {"id": "spell_engine:one_handed_area_charge"},
            }
        },
        "release": {
            "animation": {"id": "spell_engine:one_handed_area_release"},
        },
        "impacts": [],
        "cost": cost,
    }
    kind = imp[0]
    if kind == "damage":
        spell["target"] = {"type": "AIM", "aim": {"sticky": True}}
    elif kind == "heal":
        spell["target"] = {"type": "AIM", "aim": {"sticky": True, "use_caster_as_fallback": True}}
    elif kind == "effect":
        spell["target"] = {"type": "AREA", "area": {"include_caster": True}}
    if kind == "damage":
        spell["impacts"] = [{"action": {"type": "DAMAGE", "damage": {"spell_power_coefficient": imp[1], "knockback": 0.3}}}]
    elif kind == "heal":
        spell["impacts"] = [{"action": {"type": "HEAL", "heal": {"spell_power_coefficient": imp[1]}}}]
    elif kind == "effect":
        spell["impacts"] = [{"action": {"type": "STATUS_EFFECT", "status_effect": {"effect_id": imp[1], "duration": float(imp[2])}}}]
    return spell

os.makedirs(OUT, exist_ok=True)
n = 0
for job, spells in S.items():
    for sid, v in spells.items():
        with open(os.path.join(OUT, f"{job}_{sid}.json"), "w", encoding="utf-8") as f:
            json.dump(make_spell(job, sid, v), f, ensure_ascii=False, indent=2)
        n += 1
print(f"{n} spells")
