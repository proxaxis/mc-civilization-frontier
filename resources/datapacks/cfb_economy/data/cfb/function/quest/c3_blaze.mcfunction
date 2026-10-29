execute store result score @s cfb_q_tmp run clear @s minecraft:blaze_rod 0
execute if score @s cfb_q_tmp matches ..15 run return run tellraw @s [{"text":"炎の証: blaze_rod 16個が必要です","color":"red"}]
clear @s minecraft:blaze_rod 16
give @s coinsje:iron_coin 40
experience add @s 3 levels
tellraw @s [{"text":"[クエスト達成] 炎の証 → MD+40 / XP lv+3","color":"green"}]
