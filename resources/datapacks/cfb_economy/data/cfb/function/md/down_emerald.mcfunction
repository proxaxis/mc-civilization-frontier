execute store result score @s cfb_q_tmp run clear @s coinsje:diamond_coin 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"diamond_coin が1枚必要です","color":"red"}]
clear @s coinsje:diamond_coin 1
give @s coinsje:emerald_coin 64
tellraw @s [{"text":"1 diamond_coin → 64 emerald_coin に両替しました","color":"gray"}]
