execute store result score @s cfb_q_tmp run clear @s bcn:item_pipe 0
execute if score @s cfb_q_tmp matches ..15 run return run tellraw @s [{"text":"物流の動脈: item_pipe 16個が必要です","color":"red"}]
give @s coinsje:iron_coin 5
experience add @s 150 points
give @s bcn:wrench 1
give @s minecraft:chest 4
tellraw @s [{"text":"[クエスト達成] 物流の動脈 → iron_coinx5 / XP+150 / wrenchx1 / chestx4","color":"green"}]
