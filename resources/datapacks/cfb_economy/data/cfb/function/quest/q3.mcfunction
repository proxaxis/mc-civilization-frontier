execute store result score @s cfb_q_tmp run clear @s minecraft:wheat 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"wheat 64個が必要です","color":"red"}]
clear @s minecraft:wheat 64
give @s coinsje:iron_coin 8
tellraw @s [{"text":"[クエスト達成] 交易への道 → 鉄貨8枚","color":"green"}]
