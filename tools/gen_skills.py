#!/usr/bin/env python3
"""Pufferfish's Skills 用スキルツリー datapack を生成する.

形式: data/cfb/puffish_skills/categories/<job>/ に
category.json / definitions.json / skills.json / connections.json / experience.json
を出力する. 出力先: resources/datapacks/cfb_skills/
"""
import json, os

ROOT = "/ws/resources/datapacks/cfb_skills/data/cfb/puffish_skills"
XP = {1: 100, 2: 200, 3: 400, 4: 700, 5: 1100}
ACTIVE_XP = 300
ULT_XP = 1000

ATTR = {
    "health": "minecraft:max_health",
    "armor": "minecraft:armor",
    "kb_resist": "minecraft:knockback_resistance",
    "speed": "minecraft:movement_speed",
    "luck": "minecraft:luck",
    "attack": "minecraft:attack_damage",
    "resist": "minecraft:armor_toughness",
}

JOBS = {
    "guardian": {
        "title": "守護者", "icon": "minecraft:shield",
        "passives": [
            ("physique", "health", "鋼の肉体", "Iron Physique", 0.04),
            ("plating", "armor", "重装甲適性", "Heavy Plating", 0.03),
            ("stance", "kb_resist", "不動の足場", "Stalwart Stance", 0.10),
            ("shield", "resist", "巨盾の心得", "Greatshield Mastery", 0.06),
            ("presence", "luck", "挑発の威圧", "Provoking Presence", 0.02),
        ],
        "actives": [
            ("bulwark", "鉄壁の構え", "Bulwark Stance", "shield", 1),
            ("charge", "盾突撃", "Shield Charge", "stance", 1),
            ("barrage", "楯の乱打", "Shield Barrage", "plating", 1),
            ("roar", "守護の咆哮", "Guardian's Roar", "presence", 1),
            ("resolve", "鉄の抵抗", "Iron Resolve", "physique", 1),
            ("bastion", "城塞の号令", "Bastion Call", "plating", 2),
            ("wall", "忍耐の壁", "Wall of Endurance", "stance", 2),
            ("cover", "覆い隠す盾", "Covering Shield", "shield", 2),
            ("flesh_roar", "肉壁の咆哮", "Flesh Wall Roar", "physique", 2),
            ("second_wind", "起死回生", "Second Wind", "physique", 3),
            ("seismic", "地震槌", "Seismic Slam", "plating", 4),
            ("def_field", "防戦結界", "Defensive Field", "presence", 3),
            ("vow", "不屈の誓い", "Vow of Indomitability", "physique", 4),
            ("aegis", "聖盾の加護", "Aegis Ward", "presence", 5),
            ("unbreakable", "断固たる防壁", "Unbreakable Bulwark", "shield", 5),
        ],
    },
    "assassin": {
        "title": "暗殺者", "icon": "minecraft:iron_sword",
        "passives": [
            ("step", "speed", "影の歩法", "Shadow Step", 0.03),
            ("eye", "luck", "急所の目", "Vital Eye", 0.02),
            ("measure", "attack", "暗殺の間合い", "Assassin's Measure", 0.04),
            ("aura", "resist", "沈黙の殺気", "Silent Killing Aura", 0.04),
            ("body", "luck", "軽業の体", "Acrobatic Body", 0.02),
        ],
        "actives": [
            ("flash", "瞬刃", "Flash Blade", "eye", 1),
            ("dash", "影走り", "Shadow Dash", "step", 1),
            ("hamstring", "断脚刃", "Hamstring", "measure", 1),
            ("blood_trail", "血痕の追跡", "Blood Trail", "aura", 1),
            ("afterimage", "残像", "Afterimage", "body", 1),
            ("smoke", "煙幕", "Smoke Veil", "aura", 2),
            ("shadow_strike", "影縫い", "Shadow Strike", "measure", 2),
            ("needle", "麻痺針", "Paralytic Needle", "eye", 2),
            ("abyss", "深淵の呼吸", "Abyssal Breath", "eye", 3),
            ("taunt", "挑発の影", "Shadow Taunt", "aura", 3),
            ("stealth", "ステルス", "Stealth", "aura", 4),
            ("deathblow", "致命打", "Deathblow", "measure", 4),
            ("vanish", "闇遁", "Vanish", "step", 4),
            ("clone", "影分身", "Shadow Clone", "body", 5),
            ("burial", "影葬", "Shadow Burial", "measure", 5),
        ],
    },
    "mage": {
        "title": "魔道士", "icon": "minecraft:blaze_rod",
        "passives": [
            ("mana", "luck", "深き魔力", "Deep Mana", 0.02),
            ("circulation", "resist", "魔力循環", "Mana Circulation", 0.05),
            ("amplify", "attack", "術式強化", "Spell Amplification", 0.04),
            ("chant", "speed", "詠唱熟達", "Chanting Mastery", 0.03),
            ("affinity", "luck", "属性適性", "Elemental Affinity", 0.02),
        ],
        "actives": [
            ("firebolt", "火炎弾", "Firebolt", "affinity", 1),
            ("frostbolt", "氷結弾", "Frostbolt", "affinity", 1),
            ("blink", "瞬間移動", "Blink", "mana", 1),
            ("cache", "術式保存", "Spell Cache", "chant", 1),
            ("mana_shield", "魔力障壁", "Mana Shield", "circulation", 1),
            ("inferno", "範囲火炎", "Inferno Wave", "amplify", 2),
            ("frost_domain", "氷結領域", "Frost Domain", "affinity", 2),
            ("flare", "魔力鮮明", "Mana Flare", "amplify", 2),
            ("surge", "魔道加速", "Arcane Surge", "chant", 2),
            ("chain", "雷鎖", "Chain Lightning", "affinity", 3),
            ("shockbolt", "雷撃弾", "Shockbolt", "affinity", 4),
            ("attune", "属性同調", "Elemental Attunement", "affinity", 4),
            ("gravity", "重力操作", "Gravity Well", "mana", 4),
            ("burst", "属性爆発", "Elemental Burst", "amplify", 4),
            ("final", "終末術式", "Final Incantation", "amplify", 5),
        ],
    },
    "paladin": {
        "title": "聖職者", "icon": "minecraft:golden_apple",
        "passives": [
            ("barrier", "resist", "聖なる結界", "Holy Barrier", 0.04),
            ("faith", "kb_resist", "信仰の耐性", "Faith Resilience", 0.05),
            ("mercy", "luck", "慈悲の供給", "Merciful Supply", 0.03),
            ("healing", "health", "回復術式熟達", "Healing Mastery", 0.05),
            ("breath", "luck", "清らかな息吹", "Pure Breath", 0.02),
        ],
        "actives": [
            ("quick", "迅速回復", "Quick Mend", "healing", 1),
            ("heal", "即時回復", "Heal", "barrier", 1),
            ("purify", "浄化", "Purify", "faith", 1),
            ("rally", "鼓舞の祈祷", "Rallying Prayer", "mercy", 1),
            ("consecrate", "聖別", "Consecrate", "healing", 1),
            ("blessing", "祝福", "Blessing", "mercy", 2),
            ("ward", "守護光", "Ward of Light", "faith", 2),
            ("regen", "再生の誓い", "Vow of Regeneration", "barrier", 2),
            ("hymn", "力の賛歌", "Hymn of Might", "mercy", 3),
            ("martyr", "苦痛の代償", "Martyr's Bond", "faith", 3),
            ("circle", "治癒の輪", "Circle of Healing", "healing", 4),
            ("eruption", "信仰の爆発", "Faith Eruption", "healing", 3),
            ("sanctuary", "聖域の展開", "Sanctuary", "breath", 4),
            ("repose", "魂の安寧", "Soul Repose", "barrier", 5),
            ("grand", "大聖域", "Grand Sanctuary", "healing", 5),
        ],
    },
}

def item_icon(item):
    return {"type": "item", "data": {"item": item}}

for job, spec in JOBS.items():
    cat_dir = os.path.join(ROOT, "categories", job)
    os.makedirs(cat_dir, exist_ok=True)

    # definitions.json: スキル定義(見た目・報酬・コスト)
    definitions = {}
    # skills.json: ツリー上のノード配置(definition 参照 + 位置)
    skills = {}
    # connections.json: ノード間の辺
    connections = []

    # パッシブ: key_lv1..5 の直鎖. x=列(0..4), y=レベル(0..4)
    for col, (key, attr, nj, ne, per) in enumerate(spec["passives"]):
        for lv in range(1, 6):
            did = f"{key}_lv{lv}"
            definitions[did] = {
                "title": f"{nj} Lv{lv} - {ne}",
                "icon": item_icon("minecraft:book"),
                "description": f"{ne} Lv{lv}(累計 {int(per*lv*100)}%%)",
                "rewards": [{
                    "type": "puffish_skills:attribute",
                    "data": {
                        "attribute": ATTR[attr],
                        "value": round(per * lv, 4),
                        "operation": "multiply_total",
                    },
                }],
            }
            skills[did] = {
                "definition": did,
                "x": col * 40,
                "y": lv * 40,
                "root": lv == 1,
            }
            if lv > 1:
                connections.append([f"{key}_lv{lv-1}", f"{key}_lv{lv}"])

    # アクティブ: 親パッシブの Lv ノードから枝出し. x=6以上の列
    pool = {"guardian": "rogues:spell_scroll/warrior", "assassin": "rogues:spell_scroll/rogue",
            "mage": "wizards:spell_scroll/arcane", "paladin": "paladins:spell_scroll/priest"}[job]
    for i, (key, nj, ne, pkey, plv) in enumerate(spec["actives"]):
        did = key
        cost = ULT_XP if plv == 5 else ACTIVE_XP
        sid = f"cfb:{job}_{key}"
        scroll = f'give @s spell_engine:spell_scroll[spell_engine:spell_container={{spell_ids:["{sid}"],pool:"{pool}"}}]'
        definitions[did] = {
            "title": f"{nj} - {ne}",
            "icon": item_icon("minecraft:paper"),
            "description": f"{ne} アンロック(巻物を獲得し、結合台で呪文書へ結合)",
            "rewards": [{"type": "puffish_skills:command", "data": {"command": scroll}}],
        }
        skills[did] = {
            "definition": did,
            "x": 240 + (i % 5) * 40,
            "y": (i // 5 + plv) * 40,
        }
        connections.append([f"{pkey}_lv{plv}", key])

    category = {
        "title": spec["title"],
        "icon": item_icon(spec["icon"]),
        "background": "minecraft:textures/block/stone.png",
        "exclusive_root": True,
    }
    experience = {
        "level_limit": 25,
        "reset_on_death": False,
        "experience_per_level": {
            "type": "expression",
            "data": {"expression": "min(level ^ 1.5 + 30, 300)"},
        },
        "sources": [
            {
                "type": "puffish_skills:kill_entity",
                "data": {
                    "variables": {
                        "dropped_xp": {
                            "operations": [
                                {"type": "get_dropped_experience"}
                            ]
                        },
                        "max_health": {
                            "operations": [
                                {"type": "get_killed_living_entity"},
                                {"type": "get_max_health"},
                            ]
                        },
                    },
                    "experience": "dropped_xp + max_health / 20",
                    "anti_farming": {
                        "limit_per_chunk": 15,
                        "reset_after_seconds": 300,
                    },
                },
            }
        ],
    }

    for fname, obj in (
        ("category.json", category),
        ("definitions.json", definitions),
        ("skills.json", skills),
        ("connections.json", connections),
        ("experience.json", experience),
    ):
        with open(os.path.join(cat_dir, fname), "w", encoding="utf-8") as f:
            json.dump(obj, f, ensure_ascii=False, indent=2)

    # 旧形式の category.json(単一ファイル)が残っていれば削除
    stale = os.path.join(cat_dir, "..")  # noop
    print(f"{job}: {len(skills)} nodes")

print("done")
