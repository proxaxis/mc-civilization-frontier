execute if entity @s[tag=qd_c1_armed] run return run tellraw @s [{"text":"初陣の備え: 達成済みです","color":"gray"}]
execute store result score @s cfb_q_tmp run clear @s minecraft:iron_chestplate 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"初陣の備え: iron_chestplate 1個が必要です","color":"red"}]
execute unless score @s cfb_skillok matches 1.. run return run tellraw @s [{"text":"初陣の備え: 要件が未達です","color":"red"}]
give @s coinsje:copper_coin 10
experience add @s 100 points
give @s minecraft:iron_ingot 3
give @s minecraft:golden_apple 1
tag @s add qd_c1_armed
tellraw @s [{"text":"[クエスト達成] 初陣の備え → copper_coinx10 / XP+100 / iron_ingotx3 / golden_applex1","color":"green"}]
