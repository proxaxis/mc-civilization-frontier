#!/usr/bin/env python3
"""cfb クエスト生成: 章立て(納品/討伐/探索)+報酬(MD硬貨・スキルポイント).

生成物: data/cfb/function/quest/*.mcfunction
- quest/menu.mcfunction: 章別メニュー
- quest/<id>.mcfunction: 条件チェック→消費/達成→報酬付与
形式: 納品=アイテムN個消費 / 討伐=スコア閾値(クライアント報告式) / 探索=advancement キー
報酬: coinsje 硬貨 + puffish_skills points add(職業ポイント)
"""
import os

OUT = "/ws/resources/datapacks/cfb_economy/data/cfb/function/quest"
os.makedirs(OUT, exist_ok=True)

# (id, 章, 日本語名, 種別, 条件, MD額面(鉄貨換算), XP lv)
Q = [
 # 第一章: 開拓と生存
 ("c1_wood",   1, "開拓の第一歩",   "deliver", ("minecraft:oak_log",64),      5, 1),
 ("c1_stone",  1, "石積みの基盤",   "deliver", ("minecraft:cobblestone",64),  5, 1),
 ("c1_wheat",  1, "交易への道",     "deliver", ("minecraft:wheat",64),        8, 1),
 ("c1_hunt",   1, "夜の猟",         "kill",    ("minecraft:zombie",10),      8, 2),
 ("c1_village",1, "交易民の発見",   "advance", "minecraft:adventure/trade",   10,2),
 # 第二章: 工業化
 ("c2_iron",   2, "鉄の製錬",       "deliver", ("minecraft:iron_ingot",32),   15,2),
 ("c2_copper", 2, "導管の銅",       "deliver", ("minecraft:copper_ingot",32), 15,2),
 ("c2_red",    2, "動力の血",       "deliver", ("minecraft:redstone",32),     15,2),
 ("c2_pillager",2,"拠点防衛",       "kill",    ("minecraft:pillager",10),     20,2),
 ("c2_temple", 2, "森の寺院",       "advance", "minecraft:adventure/hero_of_the_village",30,3),
 # 第三章: 深淵と大災厄
 ("c3_blaze",  3, "炎の証",         "deliver", ("minecraft:blaze_rod",16),    40,3),
 ("c3_ender",  3, "虚空の瞳",       "deliver", ("minecraft:ender_pearl",16),  40,3),
 ("c3_nether", 3, "焦土の征伐",     "kill",    ("minecraft:wither_skeleton",5),60,4),
 ("c3_dragon", 3, "終焉の龍",       "advance", "minecraft:end/kill_dragon",   100,5),
 ("c3_elite",  3, "古の守護者",     "kill",    ("minecraft:elder_guardian",3),80,4),
]

def md_coins(iron):
    """鉄貨 n 枚を最適硬貨構成に分解(上位から 64:1)"""
    units=[("netherite_coin",64**4),("diamond_coin",64**3),("emerald_coin",64**2),("gold_coin",64**1),("iron_coin",1)]
    out=[]
    rem=iron
    for name,v in units:
        c,rem=divmod(rem,v)
        if c: out.append(f"give @s coinsje:{name} {c}")
    return out

for qid,ch,nj,kind,cond,md,pt in Q:
    lines=[]
    if kind=="deliver":
        item,n=cond
        lines+=[f'execute if entity @s[nbt={{Inventory:[{{id:"{item}",count:{n}}}]}}] run clear @s {item} {n}',
                f'execute unless entity @s[nbt={{Inventory:[{{id:"{item}"}}]}}] run tellraw @s [{{"text":"{nj}: {item.split(":")[1]} {n}個が必要です","color":"red"}}]']
    elif kind=="kill":
        ent,n=cond
        lines+=[f'execute if score @s cfb_quest matches {n}.. run scoreboard players set @s cfb_quest 0',
                f'execute if score @s cfb_quest matches ..{n-1} run tellraw @s [{{"text":"{nj}: {ent.split(":")[1]} 討伐数 {n} 未達です","color":"red"}}]']
    elif kind=="advance":
        lines+=[f'execute unless entity @s[advancements={{{cond}=true}}] run tellraw @s [{{"text":"{nj}: 対象の進捗が未達です","color":"red"}}]']
    lines+=md_coins(md)
    lines+=[f'experience add @s {pt} levels',
            f'tellraw @s [{{"text":"[クエスト達成] {nj} → MD+{md} / XP lv+{pt}","color":"green"}}]']
    open(f"{OUT}/{qid}.mcfunction","w").write("\n".join(lines)+"\n")

# メニュー: 章別に並べる
lines=['tellraw @s [{"text":"=== クエスト ===","color":"gold"}]']
for ch in (1,2,3):
    lines.append(f'tellraw @s [{{"text":"--- 第{ch}章 ---","color":"gray"}}]')
    for qid,c,nj,kind,cond,md,pt in Q:
        if c!=ch: continue
        lines.append(f'tellraw @s [{{"text":"{nj} ","color":"white"}},{{"text":"[申請]","color":"aqua","click_event":{{"action":"run_command","command":"/function cfb:quest/{qid}"}}}}]')
open(f"{OUT}/menu.mcfunction","w").write("\n".join(lines)+"\n")
print(len(Q),"quests")
