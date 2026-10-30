execute store result score @s cfb_q_tmp run clear @s farmersdelight:beef_stew 0
execute if score @s cfb_q_tmp matches ..15 run return run tellraw @s [{"text":"特級レーションの備蓄: beef_stew 16個が必要です","color":"red"}]
give @s coinsje:gold_coin 3
experience add @s 300 points
give @s minecraft:shulker_box 1
tellraw @s [{"text":"[クエスト達成] 特級レーションの備蓄 → gold_coinx3 / XP+300 / shulker_boxx1","color":"green"}]
