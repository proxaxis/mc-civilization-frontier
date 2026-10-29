execute store result score @s cfb_q_tmp run clear @s coinsje:iron_coin 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"iron_coin が1枚必要です","color":"red"}]
clear @s coinsje:iron_coin 1
give @s coinsje:copper_coin 64
tellraw @s [{"text":"1 iron_coin → 64 copper_coin に両替しました","color":"gray"}]
