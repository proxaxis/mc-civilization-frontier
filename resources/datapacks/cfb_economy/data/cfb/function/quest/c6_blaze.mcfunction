scoreboard players operation @s cfb_q_tmp = @s cfb_k_blaze
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_blazeb
execute if score @s cfb_q_tmp matches ..9 run return run tellraw @s [{"text":"炎の絨毯: 達成数 10 未達です","color":"red"}]
scoreboard players add @s cfb_k_blazeb 10
give @s coinsje:gold_coin 3
experience add @s 400 points
give @s minecraft:blaze_rod 8
tellraw @s [{"text":"[クエスト達成] 炎の絨毯 → gold_coinx3 / XP+400 / blaze_rodx8","color":"green"}]
