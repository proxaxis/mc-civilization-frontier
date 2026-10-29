execute if score @s cfb_q_guardian matches 3.. run scoreboard players set @s cfb_q_guardian 0
execute if score @s cfb_q_guardian matches ..2 run tellraw @s [{"text":"古の守護者: elder_guardian 討伐数 3 未達です","color":"red"}]
give @s coinsje:gold_coin 1
give @s coinsje:iron_coin 16
experience add @s 4 levels
tellraw @s [{"text":"[クエスト達成] 古の守護者 → MD+80 / XP lv+4","color":"green"}]
