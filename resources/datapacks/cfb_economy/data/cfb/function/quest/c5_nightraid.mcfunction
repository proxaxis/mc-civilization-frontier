scoreboard players operation @s cfb_q_tmp = @s cfb_k_hordezomb
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_hordezombb
execute if score @s cfb_q_tmp matches ..9 run return run tellraw @s [{"text":"周期夜襲の防衛線: 達成数 10 未達です","color":"red"}]
scoreboard players add @s cfb_k_hordezombb 10
give @s coinsje:diamond_coin 1
experience add @s 500 points
give @s minecraft:splash_potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}] 4
give @s minecraft:golden_apple 2
tellraw @s [{"text":"[クエスト達成] 周期夜襲の防衛線 → diamond_coinx1 / XP+500 / splash_potionx4 / golden_applex2","color":"green"}]
