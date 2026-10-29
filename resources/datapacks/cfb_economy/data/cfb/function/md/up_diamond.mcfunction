execute store result score @s cfb_q_tmp run clear @s coinsje:emerald_coin 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"emerald_coin が64枚必要です","color":"red"}]
clear @s coinsje:emerald_coin 64
give @s coinsje:diamond_coin 1
tellraw @s [{"text":"64 emerald_coin → 1 diamond_coin に両替しました","color":"gray"}]
