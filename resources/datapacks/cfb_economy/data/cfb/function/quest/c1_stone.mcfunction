execute store result score @s cfb_q_tmp run clear @s minecraft:cobblestone 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"石積みの基盤: cobblestone 64個が必要です","color":"red"}]
clear @s minecraft:cobblestone 64
give @s coinsje:iron_coin 5
experience add @s 1 levels
tellraw @s [{"text":"[クエスト達成] 石積みの基盤 → MD+5 / XP lv+1","color":"green"}]
