execute store result score @s cfb_q_tmp run clear @s minecraft:oak_log 0
execute if score @s cfb_q_tmp matches ..31 run return run tellraw @s [{"text":"原始の道具と身体拡張: oak_log 32個が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s minecraft:stone_pickaxe 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"原始の道具と身体拡張: stone_pickaxe 1個が必要です","color":"red"}]
clear @s minecraft:oak_log 32
give @s coinsje:copper_coin 5
experience add @s 50 points
give @s minecraft:cooked_cod 8
give @s minecraft:torch 32
tellraw @s [{"text":"[クエスト達成] 原始の道具と身体拡張 → copper_coinx5 / XP+50 / cooked_codx8 / torchx32","color":"green"}]
