scoreboard players operation @s cfb_q_tmp = @s cfb_q_guardian
scoreboard players operation @s cfb_q_tmp -= @s cfb_q_gb
execute if score @s cfb_q_tmp matches ..2 run return run tellraw @s [{"text":"古の守護者: 討伐数 3 未達です","color":"red"}]
scoreboard players add @s cfb_q_gb 3
give @s coinsje:gold_coin 1
give @s coinsje:iron_coin 16
experience add @s 4 levels
tellraw @s [{"text":"[クエスト達成] 古の守護者 → MD+80 / XP lv+4","color":"green"}]
