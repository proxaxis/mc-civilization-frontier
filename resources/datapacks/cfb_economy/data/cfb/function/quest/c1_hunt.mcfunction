scoreboard players operation @s cfb_q_tmp = @s cfb_q_zombie
scoreboard players operation @s cfb_q_tmp -= @s cfb_q_zb
execute if score @s cfb_q_tmp matches ..9 run return run tellraw @s [{"text":"夜の猟: 討伐数 10 未達です","color":"red"}]
scoreboard players add @s cfb_q_zb 10
give @s coinsje:iron_coin 8
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 夜の猟 → MD+8 / XP lv+2","color":"green"}]
