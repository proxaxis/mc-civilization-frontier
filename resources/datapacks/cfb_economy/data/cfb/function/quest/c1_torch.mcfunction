execute store result score @s cfb_q_tmp run clear @s minecraft:torch 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"灯火の確保: torch 64個が必要です","color":"red"}]
clear @s minecraft:torch 64
give @s coinsje:copper_coin 5
experience add @s 50 points
give @s minecraft:coal 16
tellraw @s [{"text":"[クエスト達成] 灯火の確保 → copper_coinx5 / XP+50 / coalx16","color":"green"}]
