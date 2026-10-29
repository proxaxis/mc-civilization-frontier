execute store result score @s cfb_q_tmp run clear @s coinsje:diamond_coin 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"diamond_coin が64枚必要です","color":"red"}]
clear @s coinsje:diamond_coin 64
give @s coinsje:netherite_coin 1
tellraw @s [{"text":"64 diamond_coin → 1 netherite_coin に両替しました","color":"gray"}]
