#!/usr/bin/env python3
# /ws/quests/chapter_*.md -> cfb_economy datapack クエスト生成
# 使い方: python3 /ws/tools/gen_quests_v2.py
import re, os, glob

BASE = "/ws/resources/datapacks/cfb_economy/data/cfb"
QDIR = f"{BASE}/function/quest"
ADIR = f"{BASE}/advancement/boss"
os.makedirs(QDIR, exist_ok=True)
os.makedirs(ADIR, exist_ok=True)

# kill カウント用 scoreboard (既存の共有名を再利用)
KILL_OBJ = {
    "minecraft:zombie": ("cfb_q_zombie", "cfb_q_zb"),
    "minecraft:pillager": ("cfb_q_pillager", "cfb_q_pb"),
    "minecraft:guardian": ("cfb_q_guardian", "cfb_q_gb"),
    "minecraft:wither_skeleton": ("cfb_q_wither", "cfb_q_wb"),
}
def slug(e): return e.split(":")[-1].replace("_", "")[:9]
def kobj(e):
    if e not in KILL_OBJ: KILL_OBJ[e] = (f"cfb_k_{slug(e)}", f"cfb_k_{slug(e)}b")
    return KILL_OBJ[e]

FLAG_MAP = {
    "job_selected": ("score", "cfb_hasjob"),
    "efp_checked": ("score", "cfb_efpseen"),
    "fee_paid": ("score", "cfb_feepaid"),
    "skill_learned": ("score", "cfb_skillok"),
    "spell_bound": ("score", "cfb_spellbound"),
    "boss_cornelia": ("adv", "remnants:ancient_vessel"), "boss_lich": ("adv", "graveyard:lich"),
    "boss_flagship": ("adv", "remnants:hollow_sovereign"), "boss_eclipse": ("adv", "remnants:hollow_sovereign"),
}

chapters = []
for path in sorted(glob.glob("/ws/quests/chapter_*.md")):
    txt = open(path).read()
    title = re.search(r"# 第\s*(\d+)\s*章\s*-\s*(\S+)", txt)
    cnum = int(title.group(1))
    ctitle = re.sub(r"^[^\u3040-\u9fff]+", "", title.group(2))  # 先頭絵文字を除去
    desc = re.search(r"## 説明\n\n(.+)", txt)
    goal = re.search(r"## 目的\n\n(.+)", txt)
    quests = []
    for m in re.finditer(r"### (.+?)\n\n- id: (\S+)\n- 説明: (.+?)\n- 要件:\n((?:  - .+\n)+)- 報酬:\n((?:  - .+\n?)+)", txt):
        name, qid, qdesc, reqs_raw, rews_raw = m.groups()
        reqs, rews = [], []
        for line in reqs_raw.strip().split("\n"):
            item = line.strip("- ").split(" ※")[0].strip()
            mm = re.match(r"(deliver|have|kill|mine|advancement|flag):(\S+?)(?:\s*x(\d+))?$", item)
            if mm: reqs.append((mm.group(1), mm.group(2), int(mm.group(3) or 1)))
        for line in rews_raw.strip().split("\n"):
            item = line.strip("- ").split(" ※")[0].strip()
            mm = re.match(r"xp:(\d+)$", item) or re.match(r"(\S+)\s+x(\d+)$", item)
            if mm:
                if item.startswith("xp:"): rews.append(("xp", mm.group(1)))
                else: rews.append((mm.group(1), mm.group(2)))
        quests.append({"name": name, "id": qid, "desc": qdesc, "reqs": reqs, "rews": rews})
    chapters.append({"num": cnum, "title": ctitle,
                     "desc": desc.group(1) if desc else "", "goal": goal.group(1) if goal else "",
                     "quests": quests})

def esc(s): return s.replace('"', '\\"')

# ---- クエスト申請関数 ----
route_lines = ["execute as @a[scores={cfb_qmenu=1}] run function cfb:quest/book"]
once_types = {"flag", "advancement"}
for ch in chapters:
    for i, q in enumerate(ch["quests"], 1):
        code = ch["num"] * 100 + i
        q["code"] = code
        route_lines.append(f"execute as @a[scores={{cfb_qmenu={code}}}] run function cfb:quest/{q['id']}")
        lines = []
        guarded = any(t in once_types for t, _, _ in q["reqs"])
        if guarded:
            lines.append(f'execute if entity @s[tag=qd_{q["id"]}] run return run tellraw @s [{{"text":"{esc(q["name"])}: 達成済みです","color":"gray"}}]')
        for t, target, n in q["reqs"]:
            if t in ("deliver", "have"):
                lines.append(f"execute store result score @s cfb_q_tmp run clear @s {target} 0")
                lines.append(f'execute if score @s cfb_q_tmp matches ..{n-1} run return run tellraw @s [{{"text":"{esc(q["name"])}: {target.split(":")[-1]} {n}個が必要です","color":"red"}}]')
            elif t in ("kill", "mine"):
                stat = "killed" if t == "kill" else "mined"
                obj, base = kobj(target) if t == "kill" else (f"cfb_m_{slug(target)}", f"cfb_m_{slug(target)}b")
                lines.append(f"scoreboard players operation @s cfb_q_tmp = @s {obj}")
                lines.append(f"scoreboard players operation @s cfb_q_tmp -= @s {base}")
                lines.append(f'execute if score @s cfb_q_tmp matches ..{n-1} run return run tellraw @s [{{"text":"{esc(q["name"])}: 達成数 {n} 未達です","color":"red"}}]')
                lines.append(f"scoreboard players add @s {base} {n}")
            elif t == "advancement":
                lines.append(f'execute unless entity @s[advancements={{{target}=true}}] run return run tellraw @s [{{"text":"{esc(q["name"])}: 対象の進捗が未達です","color":"red"}}]')
            elif t == "flag":
                kind, key = FLAG_MAP[target]
                if kind == "score":
                    lines.append(f'execute unless score @s {key} matches 1.. run return run tellraw @s [{{"text":"{esc(q["name"])}: 要件が未達です","color":"red"}}]')
                else:
                    obj, base = kobj(key)
                    lines.append(f"scoreboard players operation @s cfb_q_tmp = @s {obj}")
                    lines.append(f"scoreboard players operation @s cfb_q_tmp -= @s {base}")
                    lines.append(f'execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{{"text":"{esc(q["name"])}: 討伐が確認できません","color":"red"}}]')
                    lines.append(f"scoreboard players add @s {base} 1")
        for t, target, n in q["reqs"]:
            if t == "deliver": lines.append(f"clear @s {target} {n}")
        rew_txt = []
        for r in q["rews"]:
            if r[0] == "xp":
                lines.append(f"experience add @s {r[1]} points")
                rew_txt.append(f"XP+{r[1]}")
            else:
                lines.append(f"give @s {r[0]} {r[1]}")
                rew_txt.append(f"{r[0].split(chr(91))[0].split(':')[-1]}x{r[1]}")
        if guarded:
            lines.append(f'tag @s add qd_{q["id"]}')
        lines.append(f'tellraw @s [{{"text":"[クエスト達成] {esc(q["name"])} → {" / ".join(rew_txt)}","color":"green"}}]')
        open(f"{QDIR}/{q['id']}.mcfunction", "w").write("\n".join(lines) + "\n")

for i, ch in enumerate(chapters, 1):
    route_lines.append(f"execute as @a[scores={{cfb_qmenu={900+i}}}] run function cfb:quest/sheet{i}")
route_lines += ["scoreboard players set @a[scores={cfb_qmenu=1..}] cfb_qmenu 0",
                "scoreboard players enable @a cfb_qmenu"]
open(f"{QDIR}/route.mcfunction", "w").write("\n".join(route_lines) + "\n")

# ---- 章シート本 ----
for i, ch in enumerate(chapters, 1):
    emoji = "◆"  # 絵文字はクライアントで豆腐になるため記号に統一
    pages = []
    pages.append('["",{"text":"第 ' + str(ch["num"]) + ' 章\\n\\n"},{"text":"' + esc(ch["title"]) + '\\n\\n","color":"dark_purple"},{"text":"' + esc(ch["desc"]) + '"}]')
    pages.append('["",{"text":"' + emoji + '0' + str(ch["num"]) + ' > 目的\\n\\n","color":"gray"},{"text":"' + esc(ch["goal"]) + '"}]')
    lst = '["",{"text":"' + emoji + '0' + str(ch["num"]) + ' > ▶クエスト\\n\\n","color":"gray"}'
    for j, q in enumerate(ch["quests"], 1):
        lst += ',{"text":"□ ' + f"{j:02d}" + ' ' + esc(q["name"]) + '\\n"}'
    pages.append(lst + "]")
    for j, q in enumerate(ch["quests"], 1):
        code = q["code"]
        reward = " ".join(f"◆{r[0].split(chr(91))[0].split(':')[-1]}x{r[1]}" if r[0] != "xp" else f"XP+{r[1]}" for r in q["rews"])
        reqtxt = "".join(f"■{t}:{tg.split(':')[-1]}x{n}\\n" for t, tg, n in q["reqs"])
        pages.append('["",{"text":"' + emoji + '0' + str(ch["num"]) + ' > ' + f"{j:02d}" + f"{j:02d}" + ' ' + esc(q["name"]) + '\\n","bold":true},{"text":">> ' + esc(reward) + '\\n","color":"gold"},{"text":"' + reqtxt + '\\n","color":"dark_red"},{"text":"[申請]\\n\\n","color":"aqua",click_event:{action:"run_command",command:"/trigger cfb_qmenu set ' + str(code) + '"}},{"text":"' + esc(q["desc"]) + '"}]')
    book = 'give @s minecraft:written_book[minecraft:written_book_content={title:"クエストシート - ' + esc(ch["title"]) + '",author:"Civilization Frontier",pages:[' + ",".join(pages) + ']}]'
    open(f"{QDIR}/sheet{i}.mcfunction", "w").write(book + f'\ntellraw @s [{{"text":"クエストシート 第{ch["num"]}章 を受け取りました.","color":"gray"}}]\n')

# ---- マスターブック ----
intro = '["",{"text":"★ Civilization Frontier\\n\\n―― 創世か, 終焉か.\\n大災厄によって旧文明が崩壊した世界.\\n頭上には浮遊城塞, 足元には無限の迷宮が広がり, 安息の地はどこにもない.\\n己の職業を定め, 街道の酒場を確保し, 最初の道具を作れ.\\n産業を興して資源を掌握し, 失われた遺産を取り戻す戦いが今始まる.\\n\\n歯車を回せ, 剣を抜け!\\n新たな時代を築くのは君だ!"}]'
menu = '["",{"text":"★ クエスト本 > ブックメニュー\\n\\n"}'
for i, ch in enumerate(chapters, 1):
    menu += ',{"text":"' + f"{i:02d}" + '. ","color":"gray"},{"text":"' + esc(ch["title"]) + ' "},{"text":"[取得]\\n","color":"aqua",click_event:{action:"run_command",command:"/trigger cfb_qmenu set ' + str(900 + i) + '"}}'
menu += ']'
book = 'give @s minecraft:written_book[minecraft:written_book_content={title:"クエストブック",author:"Civilization Frontier",pages:[' + intro + "," + menu + ']}]'
open(f"{QDIR}/book.mcfunction", "w").write(book + '\ntellraw @s [{"text":"クエストブックを受け取りました. 死亡しても再配布されます.","color":"gray"}]\n')

# ---- gen_load: 追加 scoreboard ----
load = ["# quest 生成スクリプトが追加する scoreboard",
        "scoreboard objectives add cfb_efpcheck trigger",
        "scoreboard objectives add cfb_efpseen dummy",
        "scoreboard objectives add cfb_feepaid dummy",
        "scoreboard objectives add cfb_skillok dummy",
        "scoreboard objectives add cfb_spellbound dummy",
        "scoreboard players enable @a cfb_efpcheck"]
for e, (obj, base) in sorted(KILL_OBJ.items()):
    if obj.startswith("cfb_q_"): continue
    load.append(f"scoreboard objectives add {obj} minecraft.killed:{e}")
    load.append(f"scoreboard objectives add {base} dummy")
load += ["scoreboard objectives add cfb_m_spawner minecraft.mined:minecraft.spawner",
         "scoreboard objectives add cfb_m_spawnerb dummy"]
open(f"{QDIR}/gen_load.mcfunction", "w").write("\n".join(load) + "\n")

# ---- EFP 確認関数 ----
open(f"{QDIR}/efpcheck.mcfunction", "w").write(
    'tellraw @s [{"text":"環境負荷指数 EFP: ","color":"gray"},{"score":{"name":"#efp","objective":"cfb_efp"}},{"text":" (12=黄昏 30=脅威 60=襲撃)","color":"gray"}]\n'
    'scoreboard players set @s cfb_efpseen 1\n')

nq = sum(len(c["quests"]) for c in chapters)
print(f"chapters={len(chapters)} quests={nq} kill_obj={len(KILL_OBJ)}")
