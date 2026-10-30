scoreboard players operation @s cfb_q_tmp = @s cfb_k_hollowsov
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_hollowsovb
execute if score @s cfb_q_tmp matches ..2 run return run tellraw @s [{"text":"眷属の排除: 達成数 3 未達です","color":"red"}]
scoreboard players add @s cfb_k_hollowsovb 3
give @s coinsje:gold_coin 3
experience add @s 400 points
give @s minecraft:obsidian 16
tellraw @s [{"text":"[クエスト達成] 眷属の排除 → 金貨x3 / XP+400 / 黒曜石x16","color":"green"}]
