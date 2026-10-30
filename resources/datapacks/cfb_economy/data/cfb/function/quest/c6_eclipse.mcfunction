execute if entity @s[tag=qd_c6_eclipse] run return run tellraw @s [{"text":"蝕界の深淵: 達成済みです","color":"gray"}]
scoreboard players operation @s cfb_q_tmp = @s cfb_k_hollowsov
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_hollowsovb
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"蝕界の深淵: 討伐が確認できません","color":"red"}]
scoreboard players add @s cfb_k_hollowsovb 1
give @s coinsje:diamond_coin 2
experience add @s 1000 points
give @s minecraft:heart_of_the_sea 1
give @s minecraft:recovery_compass 1
tag @s add qd_c6_eclipse
tellraw @s [{"text":"[クエスト達成] 蝕界の深淵 → diamond_coinx2 / XP+1000 / heart_of_the_seax1 / recovery_compassx1","color":"green"}]
