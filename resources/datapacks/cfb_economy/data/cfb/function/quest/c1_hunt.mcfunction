execute if score @s cfb_q_zombie matches 10.. run scoreboard players set @s cfb_q_zombie 0
execute if score @s cfb_q_zombie matches ..9 run tellraw @s [{"text":"夜の猟: zombie 討伐数 10 未達です","color":"red"}]
give @s coinsje:iron_coin 8
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 夜の猟 → MD+8 / XP lv+2","color":"green"}]
