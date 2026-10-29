execute store result score @s cfb_q_tmp run clear @s minecraft:ender_pearl 0
execute if score @s cfb_q_tmp matches ..15 run return run tellraw @s [{"text":"虚空の瞳: ender_pearl 16個が必要です","color":"red"}]
clear @s minecraft:ender_pearl 16
give @s coinsje:iron_coin 40
experience add @s 3 levels
tellraw @s [{"text":"[クエスト達成] 虚空の瞳 → MD+40 / XP lv+3","color":"green"}]
