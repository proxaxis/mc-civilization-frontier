scoreboard players operation @s cfb_q_tmp = @s cfb_q_wither
scoreboard players operation @s cfb_q_tmp -= @s cfb_q_wb
execute if score @s cfb_q_tmp matches ..4 run return run tellraw @s [{"text":"焦土の征伐: 討伐数 5 未達です","color":"red"}]
scoreboard players add @s cfb_q_wb 5
give @s coinsje:iron_coin 60
experience add @s 4 levels
tellraw @s [{"text":"[クエスト達成] 焦土の征伐 → MD+60 / XP lv+4","color":"green"}]
