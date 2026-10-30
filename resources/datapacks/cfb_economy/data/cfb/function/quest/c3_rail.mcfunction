execute store result score @s cfb_q_tmp run clear @s minecraft:furnace_minecart 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"鉄路の敷設: furnace_minecart 1個が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s minecraft:minecart 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"鉄路の敷設: minecart 1個が必要です","color":"red"}]
give @s coinsje:gold_coin 2
experience add @s 250 points
give @s minecraft:powered_rail 16
give @s minecraft:rail 64
tellraw @s [{"text":"[クエスト達成] 鉄路の敷設 → gold_coinx2 / XP+250 / powered_railx16 / railx64","color":"green"}]
