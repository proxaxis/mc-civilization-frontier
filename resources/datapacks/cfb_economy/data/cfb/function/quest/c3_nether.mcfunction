execute if score @s cfb_q_wither matches 5.. run scoreboard players set @s cfb_q_wither 0
execute if score @s cfb_q_wither matches ..4 run tellraw @s [{"text":"焦土の征伐: wither_skeleton 討伐数 5 未達です","color":"red"}]
give @s coinsje:iron_coin 60
experience add @s 4 levels
tellraw @s [{"text":"[クエスト達成] 焦土の征伐 → MD+60 / XP lv+4","color":"green"}]
