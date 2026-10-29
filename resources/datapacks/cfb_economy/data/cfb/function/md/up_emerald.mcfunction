execute store result score @s cfb_q_tmp run clear @s coinsje:gold_coin 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"gold_coin が64枚必要です","color":"red"}]
clear @s coinsje:gold_coin 64
give @s coinsje:emerald_coin 1
tellraw @s [{"text":"64 gold_coin → 1 emerald_coin に両替しました","color":"gray"}]
