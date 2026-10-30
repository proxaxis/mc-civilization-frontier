execute store result score @s cfb_q_tmp run clear @s minecraft:cooked_beef 0
execute if score @s cfb_q_tmp matches ..15 run return run tellraw @s [{"text":"台所の充実: ステーキx16が必要です","color":"red"}]
clear @s minecraft:cooked_beef 16
give @s coinsje:iron_coin 5
experience add @s 100 points
give @s minecraft:bread 16
tellraw @s [{"text":"[クエスト達成] 台所の充実 → 鉄貨x5 / XP+100 / パンx16","color":"green"}]
