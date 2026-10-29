execute store result score @s cfb_q_tmp run clear @s coinsje:copper_coin 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"copper_coin が64枚必要です","color":"red"}]
clear @s coinsje:copper_coin 64
give @s coinsje:iron_coin 1
tellraw @s [{"text":"64 copper_coin → 1 iron_coin に両替しました","color":"gray"}]
