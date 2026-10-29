execute if score @s cfb_q_pillager matches 10.. run scoreboard players set @s cfb_q_pillager 0
execute if score @s cfb_q_pillager matches ..9 run tellraw @s [{"text":"拠点防衛: pillager 討伐数 10 未達です","color":"red"}]
give @s coinsje:iron_coin 20
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 拠点防衛 → MD+20 / XP lv+2","color":"green"}]
