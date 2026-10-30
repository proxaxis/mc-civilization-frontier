execute store result score @s cfb_q_tmp run clear @s coinsje:copper_coin 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"通貨の流通: copper_coin 1個が必要です","color":"red"}]
give @s coinsje:iron_coin 10
experience add @s 100 points
give @s minecraft:bundle 1
tellraw @s [{"text":"[クエスト達成] 通貨の流通 → iron_coinx10 / XP+100 / bundlex1","color":"green"}]
