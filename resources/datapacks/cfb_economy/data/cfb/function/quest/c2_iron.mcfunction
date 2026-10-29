execute store result score @s cfb_q_tmp run clear @s minecraft:iron_ingot 0
execute if score @s cfb_q_tmp matches ..31 run return run tellraw @s [{"text":"鉄の製錬: iron_ingot 32個が必要です","color":"red"}]
clear @s minecraft:iron_ingot 32
give @s coinsje:iron_coin 15
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 鉄の製錬 → MD+15 / XP lv+2","color":"green"}]
