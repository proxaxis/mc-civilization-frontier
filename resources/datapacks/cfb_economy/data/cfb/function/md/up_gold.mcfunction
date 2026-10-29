execute store result score @s cfb_q_tmp run clear @s coinsje:iron_coin 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"iron_coin が64枚必要です","color":"red"}]
clear @s coinsje:iron_coin 64
give @s coinsje:gold_coin 1
tellraw @s [{"text":"64 iron_coin → 1 gold_coin に両替しました","color":"gray"}]
