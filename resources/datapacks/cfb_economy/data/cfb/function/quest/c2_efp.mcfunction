execute if entity @s[tag=qd_c2_efp] run return run tellraw @s [{"text":"煤煙と代償: 達成済みです","color":"gray"}]
execute unless score @s cfb_efpseen matches 1.. run return run tellraw @s [{"text":"煤煙と代償: 要件が未達です","color":"red"}]
give @s coinsje:iron_coin 10
experience add @s 200 points
give @s bcn:fertilizer 4
give @s minecraft:oak_sapling 16
tag @s add qd_c2_efp
tellraw @s [{"text":"[クエスト達成] 煤煙と代償 → iron_coinx10 / XP+200 / fertilizerx4 / oak_saplingx16","color":"green"}]
