scoreboard players operation @s cfb_q_tmp = @s cfb_k_memorywra
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_memorywrab
execute if score @s cfb_q_tmp matches ..4 run return run tellraw @s [{"text":"残響の舞姫: 達成数 5 未達です","color":"red"}]
scoreboard players add @s cfb_k_memorywrab 5
give @s coinsje:gold_coin 5
experience add @s 500 points
give @s minecraft:ender_pearl 8
tellraw @s [{"text":"[クエスト達成] 残響の舞姫 → gold_coinx5 / XP+500 / ender_pearlx8","color":"green"}]
