execute store result score @s cfb_q_tmp run clear @s farmersdelight:cutting_board 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"厨房の夜明け: まな板x1が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s farmersdelight:cooking_pot 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"厨房の夜明け: 調理鍋x1が必要です","color":"red"}]
give @s coinsje:iron_coin 5
experience add @s 100 points
give @s farmersdelight:iron_knife 1
give @s farmersdelight:onion 8
give @s farmersdelight:tomato_seeds 8
tellraw @s [{"text":"[クエスト達成] 厨房の夜明け → 鉄貨x5 / XP+100 / 鉄の包丁x1 / 玉ねぎx8 / トマトの種x8","color":"green"}]
