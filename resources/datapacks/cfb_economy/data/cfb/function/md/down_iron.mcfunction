execute store result score @s cfb_q_tmp run clear @s coinsje:gold_coin 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"gold_coin が1枚必要です","color":"red"}]
clear @s coinsje:gold_coin 1
give @s coinsje:iron_coin 64
tellraw @s [{"text":"1 gold_coin → 64 iron_coin に両替しました","color":"gray"}]
