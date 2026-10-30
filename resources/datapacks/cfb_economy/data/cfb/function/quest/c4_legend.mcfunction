execute store result score @s cfb_q_tmp run clear @s minecraft:netherite_sword 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"伝説の覚醒: netherite_sword 1個が必要です","color":"red"}]
give @s coinsje:diamond_coin 1
experience add @s 600 points
give @s minecraft:netherite_scrap 2
give @s minecraft:writable_book 1
tellraw @s [{"text":"[クエスト達成] 伝説の覚醒 → diamond_coinx1 / XP+600 / netherite_scrapx2 / writable_bookx1","color":"green"}]
