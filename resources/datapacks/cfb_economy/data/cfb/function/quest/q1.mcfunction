execute store result score @s cfb_q_tmp run clear @s minecraft:oak_log 0
execute if score @s cfb_q_tmp matches ..63 run return run tellraw @s [{"text":"開拓の第一歩: oak_log 64個が必要です","color":"red"}]
clear @s minecraft:oak_log 64
give @s coinsje:iron_coin 5
tellraw @s [{"text":"[クエスト達成] 開拓の第一歩 → 鉄貨5枚","color":"green"}]
