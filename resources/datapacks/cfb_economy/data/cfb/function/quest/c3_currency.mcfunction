execute store result score @s cfb_q_tmp run clear @s coinsje:copper_coin 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"通貨の流通: 銅貨x1が必要です","color":"red"}]
give @s coinsje:iron_coin 10
experience add @s 100 points
give @s minecraft:bundle 1
tellraw @s [{"text":"[クエスト達成] 通貨の流通 → 鉄貨x10 / XP+100 / バンドルx1","color":"green"}]
