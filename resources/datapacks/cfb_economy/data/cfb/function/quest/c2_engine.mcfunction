execute store result score @s cfb_q_tmp run clear @s bcn:engine_stone 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"鼓動する機関: 石エンジンx1が必要です","color":"red"}]
give @s coinsje:iron_coin 5
experience add @s 150 points
give @s minecraft:coal_block 4
give @s bcn:cable_copper 8
tellraw @s [{"text":"[クエスト達成] 鼓動する機関 → 鉄貨x5 / XP+150 / 石炭ブロックx4 / 銅ケーブルx8","color":"green"}]
