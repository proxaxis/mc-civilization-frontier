#!/usr/bin/env python3
# /ws/quests/chapter_*.md -> cfb_economy datapack クエスト生成
# 使い方: python3 /ws/tools/gen_quests_v2.py
#
# 生成物:
#   quest/<id>.mcfunction    各クエストの申請関数
#   quest/sheet<N>.mcfunction 章ごとのクエストシート(written_book)
#   quest/book.mcfunction    マスタークエストブック
#   quest/route.mcfunction   cfb_qmenu trigger の振り分け
#   quest/gen_load.mcfunction 追加 scoreboard
#   quest/efpcheck.mcfunction EFP 確認用
import re, os, glob

BASE = "/ws/resources/datapacks/cfb_economy/data/cfb"
QDIR = f"{BASE}/function/quest"
os.makedirs(QDIR, exist_ok=True)

# ---- 討伐統計 scoreboard(entity -> (統計objective, ベースラインobjective)) ----
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

# ---- flag -> (種別, キー) ----
FLAG_MAP = {
    "job_selected": ("score", "cfb_hasjob"),
    "efp_checked": ("score", "cfb_efpseen"),
    "fee_paid": ("score", "cfb_feepaid"),
    "skill_learned": ("score", "cfb_skillok"),
    "spell_bound": ("score", "cfb_spellbound"),
    "boss_cornelia": ("kill", "remnants:ancient_vessel"),
    "boss_lich": ("kill", "graveyard:lich"),
    "boss_flagship": ("kill", "remnants:hollow_sovereign"),
    "boss_eclipse": ("kill", "remnants:hollow_sovereign"),
}

# ---- 表示名辞書 ----
NAME_JA = {
    "coinsje:copper_coin":"銅貨","coinsje:iron_coin":"鉄貨","coinsje:gold_coin":"金貨",
    "coinsje:emerald_coin":"緑貨","coinsje:diamond_coin":"ダイヤ貨","coinsje:netherite_coin":"ネザライト貨",
    "minecraft:oak_log":"オークの原木","minecraft:stone_pickaxe":"石のツルハシ",
    "minecraft:iron_chestplate":"鉄のチェストプレート","minecraft:torch":"松明",
    "minecraft:bread":"パン","minecraft:charcoal":"木炭","minecraft:cooked_cod":"焼きタラ",
    "minecraft:coal":"石炭","minecraft:coal_block":"石炭ブロック","minecraft:iron_ingot":"鉄インゴット",
    "minecraft:golden_apple":"金のリンゴ","minecraft:lapis_lazuli":"ラピスラズリ","minecraft:shield":"盾",
    "minecraft:chest":"チェスト","minecraft:diamond":"ダイヤモンド","minecraft:oak_sapling":"オークの苗木",
    "minecraft:bundle":"バンドル","minecraft:emerald":"エメラルド","minecraft:golden_shovel":"金のシャベル",
    "minecraft:powered_rail":"パワードレール","minecraft:rail":"レール",
    "minecraft:furnace_minecart":"かまど付きトロッコ","minecraft:minecart":"トロッコ",
    "minecraft:blaze_rod":"ブレイズロッド","minecraft:ender_pearl":"エンダーパール",
    "minecraft:dragon_egg":"ドラゴンの卵","minecraft:dragon_head":"ドラゴンの頭",
    "minecraft:obsidian":"黒曜石","minecraft:netherite_ingot":"ネザライトインゴット",
    "minecraft:netherite_sword":"ネザライトの剣","minecraft:netherite_scrap":"ネザライトの欠片",
    "minecraft:writable_book":"書き込み可能な本","minecraft:turtle_helmet":"カメの甲羅",
    "minecraft:echo_shard":"残響の欠片","minecraft:beacon":"ビーコン",
    "minecraft:totem_of_undying":"不死のトーテム","minecraft:heart_of_the_sea":"マリンの心臓",
    "minecraft:recovery_compass":"回生のコンパス","minecraft:rotten_flesh":"腐った肉",
    "minecraft:bone_block":"骨ブロック","minecraft:iron_sword":"鉄の剣",
    "minecraft:cooked_beef":"ステーキ","minecraft:bone_meal":"骨粉",
    "minecraft:shulker_box":"シュルカーボックス","minecraft:trial_key":"トライアルキー",
    'minecraft:potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}]':"強力な治癒のポーション",
    'minecraft:potion[minecraft:potion_contents={potion:"minecraft:water_breathing"}]':"水中呼吸のポーション",
    'minecraft:splash_potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}]':"治癒の聖水",
    "bcn:engine_stone":"石エンジン","bcn:item_pipe":"輸送パイプ","bcn:quarry":"露天掘り機",
    "bcn:landmark":"ランドマーク","bcn:cable_copper":"銅ケーブル","bcn:wrench":"レンチ",
    "bcn:gear_iron":"鉄歯車","bcn:fertilizer":"肥料",
    "runes:fire_stone":"火のルーン",
    "farmersdelight:cutting_board":"まな板","farmersdelight:cooking_pot":"調理鍋",
    "farmersdelight:iron_knife":"鉄の包丁","farmersdelight:onion":"玉ねぎ",
    "farmersdelight:tomato_seeds":"トマトの種","farmersdelight:cabbage":"キャベツ",
    "farmersdelight:tomato":"トマト","farmersdelight:rice":"米",
    "farmersdelight:rich_soil":"肥沃な土","farmersdelight:beef_stew":"ビーフシチュー",
    "minecraft:zombie":"ゾンビ","minecraft:pillager":"ピリジャー","minecraft:guardian":"ガーディアン",
    "minecraft:wither_skeleton":"ウィザースケルトン","minecraft:blaze":"ブレイズ","minecraft:spawner":"スポナー",
    "graveyard:ghoul":"グール","graveyard:acolyte":"アコライト",
    "remnants:rusted_guardian":"アイアン・ヴァンガード","remnants:soulbound_knight":"マスクド・デュエリスト",
    "remnants:memory_wraith":"ファントム・ダンサー","remnants:hollow_sovereign":"蝕界の眷属",
    "remnants:ancient_vessel":"幽霊船団の主","undeadnights:horde_zombie":"大群ゾンビ","graveyard:lich":"腐敗の司教",
}
FLAG_JA = {
    "job_selected":"職業を選択する","spell_bound":"呪文書を結合する",
    "skill_learned":"パッシブスキルを習得する","efp_checked":"環境負荷指数を確認する",
    "fee_paid":"維持費を納付する",
    "boss_cornelia":"幽霊船団の主を討伐","boss_lich":"腐敗の司教を討伐",
    "boss_flagship":"天空の覇王を討伐","boss_eclipse":"蝕界の王を討伐",
}
ADV_JA = {
    "minecraft:adventure/sleep_in_bed":"ベッドで眠る",
    "minecraft:adventure/trade":"村人と交易する",
    "minecraft:end/kill_dragon":"エンダードラゴンを討伐する",
}
TYPE_JA = {"deliver":"納品","have":"所持","kill":"討伐","mine":"破壊"}
CH_EMOJI = {1: "⛏", 2: "⚙", 3: "💰", 4: "⚔", 5: "🍲", 6: "👑"}

def esc(s): return s.replace('"', '\\"')
def jname(item): return NAME_JA.get(item, item.split("[")[0].split(":")[-1])
def req_ja(t, tg, n):
    if t == "flag": return FLAG_JA.get(tg, tg)
    if t == "advancement": return "進捗 " + ADV_JA.get(tg, tg.split("/")[-1]) + " を達成"
    return f"{TYPE_JA[t]}: {jname(tg)}x{n}"
def hover(iid):
    if ":" not in iid or "[" in iid: return ""
    return ',hover_event:{action:"show_item",id:"' + iid + '"}'
def E(ch):  # 絵文字 component(白指定で本来の色に)
    return '{"text":"' + ch + '","font":"cfb:emoji","color":"white"}'
def S(text, color=""):  # 小さめフォント component
    c = f',"color":"{color}"' if color else ""
    return '{"text":"' + esc(text) + '","font":"cfb:small"' + c + '}'

# ---- 章ファイルのパース ----
chapters = []
for path in sorted(glob.glob("/ws/quests/chapter_*.md")):
    txt = open(path).read()
    title = re.search(r"# 第\s*(\d+)\s*章\s*-\s*(\S+)", txt)
    cnum = int(title.group(1))
    ctitle = re.sub(r"^[^ぁ-んァ-ヶ一-龥]+", "", title.group(2))  # 先頭絵文字を除去
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
                lines.append(f'execute if score @s cfb_q_tmp matches ..{n-1} run return run tellraw @s [{{"text":"{esc(q["name"])}: {jname(target)}x{n}が必要です","color":"red"}}]')
            elif t in ("kill", "mine"):
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
                rew_txt.append(f"{jname(r[0])}x{r[1]}")
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
    em = CH_EMOJI.get(i, "★")
    cn = f"{ch['num']:02d}"
    pages = []
    pages.append('["",' + E("📜") + ',{"text":" > ' + str(ch["num"]) + ' - "},' + E(em) + ',{"text":"' + esc(ch["title"]) + '\\n\\n"},' + S(ch["desc"]) + ']')
    pages.append('["",' + E("📜") + ',{"text":" > "},' + E(em) + ',{"text":" > 目的\\n\\n"},' + S(ch["goal"]) + ']')
    lst = '["",' + E("📜") + ',{"text":" > "},' + E(em) + ',{"text":" > リスト\\n\\n"}'
    for j, q in enumerate(ch["quests"], 1):
        lst += ',{"text":"□ ' + f"{j:02d}" + '. ' + esc(q["name"]) + '\\n","font":"cfb:small"}'
    pages.append(lst + "]")
    for j, q in enumerate(ch["quests"], 1):
        code = q["code"]
        rewcomps = []
        for r in q["rews"]:
            if r[0] == "xp":
                rewcomps.append(E("✨") + ',{"text":"' + r[1] + ' XP\\n","font":"cfb:small"}')
            elif "coinsje:" in r[0]:
                rewcomps.insert(0, E("🪙") + ',{"text":"' + esc(jname(r[0])) + 'x' + r[1] + '\\n","font":"cfb:small"' + hover(r[0]) + '}')
            else:
                rewcomps.append(E("💎") + ',{"text":"' + esc(jname(r[0])) + 'x' + r[1] + '\\n","font":"cfb:small"' + hover(r[0]) + '}')
        reqparts = []
        for t, tg, n in q["reqs"]:
            h = hover(tg) if t in ("deliver", "have") else ""
            reqparts.append(E("🔶") + ',{"text":"' + esc(req_ja(t, tg, n)) + '\\n","font":"cfb:small"' + h + '}')
        pages.append('["",' + E("📜") + ',{"text":" > "},' + E(em) + ',{"text":" > #' + f"{j:02d}" + '\\n"},{"text":"' + esc(q["name"]) + '\\n\\n","bold":true},{"text":"","extra":[' + ",".join(rewcomps) + ']},{"text":"\\n"},{"text":"","extra":[' + ",".join(reqparts) + ']},{"text":">> [申請]","color":"aqua",click_event:{action:"run_command",command:"/trigger cfb_qmenu set ' + str(code) + '"}}]')
        pages.append('["",' + E("📜") + ',{"text":" > "},' + E(em) + ',{"text":" > #' + f"{j:02d}" + ' > 詳細\\n"},{"text":"' + esc(q["name"]) + '\\n\\n","bold":true},' + S(q["desc"]) + ']')
    book = 'give @s minecraft:written_book[minecraft:written_book_content={title:"クエストシート - ' + esc(ch["title"]) + '",author:"Civilization Frontier",pages:[' + ",".join(pages) + ']}]'
    open(f"{QDIR}/sheet{i}.mcfunction", "w").write(book + f'\ntellraw @s [{{"text":"クエストシート 第{ch["num"]}章 を受け取りました.","color":"gray"}}]\n')

# ---- マスターブック ----
intro = '["",' + E("⚙") + ',{"text":" Civilization Frontier\\n\\n創世か, 終焉か ――\\n開拓, 産業, 経済, そして迫りくる強大な災厄 ―― 歯車を回せ! 剣を抜け!\\n新たな文明を創造するか, 破滅の露と消えるか, すべてはあなたの選択と手腕に委ねられている"}]'
menu = '["",' + E("⚙") + ',{"text":" > ブックメニュー\\n\\n"}'
for i, ch in enumerate(chapters, 1):
    menu += ',{"text":"' + f"{i:02d}" + ' >> ","font":"cfb:small"},' + E(CH_EMOJI.get(i, "★")) + ',{"text":"' + esc(ch["title"]) + ' ","font":"cfb:small"},{"text":"[取得]\\n","color":"aqua",click_event:{action:"run_command",command:"/trigger cfb_qmenu set ' + str(900 + i) + '"}}'
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
         "scoreboard objectives add cfb_m_spawnerb dummy",
         "scoreboard objectives add cfb_q_zombie minecraft.killed:minecraft.zombie",
         "scoreboard objectives add cfb_q_pillager minecraft.killed:minecraft.pillager",
         "scoreboard objectives add cfb_q_guardian minecraft.killed:minecraft.guardian",
         "scoreboard objectives add cfb_q_wither minecraft.killed:minecraft.wither_skeleton"]
open(f"{QDIR}/gen_load.mcfunction", "w").write("\n".join(load) + "\n")

# ---- EFP 確認関数 ----
open(f"{QDIR}/efpcheck.mcfunction", "w").write(
    'tellraw @s [{"text":"環境負荷指数 EFP: ","color":"gray"},{"score":{"name":"#efp","objective":"cfb_efp"}},{"text":" (12=黄昏 30=脅威 60=襲撃)","color":"gray"}]\n'
    'scoreboard players set @s cfb_efpseen 1\n')

nq = sum(len(c["quests"]) for c in chapters)
print(f"chapters={len(chapters)} quests={nq} kill_obj={len(KILL_OBJ)}")
