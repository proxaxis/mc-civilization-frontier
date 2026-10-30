execute store result score @s cfb_q_tmp run clear @s bcn:engine_stone 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"鼓動する機関: engine_stone 1個が必要です","color":"red"}]
give @s coinsje:iron_coin 5
experience add @s 150 points
give @s minecraft:coal_block 4
give @s bcn:cable_copper 8
tellraw @s [{"text":"[クエスト達成] 鼓動する機関 → iron_coinx5 / XP+150 / coal_blockx4 / cable_copperx8","color":"green"}]
