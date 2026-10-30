execute store result score @s cfb_q_tmp run clear @s farmersdelight:cabbage 0
execute if score @s cfb_q_tmp matches ..31 run return run tellraw @s [{"text":"肥沃なる大地: cabbage 32個が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s farmersdelight:tomato 0
execute if score @s cfb_q_tmp matches ..31 run return run tellraw @s [{"text":"肥沃なる大地: tomato 32個が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s farmersdelight:onion 0
execute if score @s cfb_q_tmp matches ..31 run return run tellraw @s [{"text":"肥沃なる大地: onion 32個が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s farmersdelight:rice 0
execute if score @s cfb_q_tmp matches ..31 run return run tellraw @s [{"text":"肥沃なる大地: rice 32個が必要です","color":"red"}]
give @s coinsje:iron_coin 10
experience add @s 150 points
give @s farmersdelight:rich_soil 16
give @s minecraft:bone_meal 32
tellraw @s [{"text":"[クエスト達成] 肥沃なる大地 → iron_coinx10 / XP+150 / rich_soilx16 / bone_mealx32","color":"green"}]
