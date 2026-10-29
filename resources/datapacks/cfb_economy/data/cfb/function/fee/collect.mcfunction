execute store result score @s cfb_q_tmp run clear @s coinsje:iron_coin 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"[税務署] 鉄貨を持っていないため維持費の徴収を免除しました(未納扱い)","color":"red"}]
clear @s coinsje:iron_coin 1
scoreboard players set @s cfb_fee 0
tellraw @s [{"text":"[税務署] 維持費として鉄貨1枚を徴収しました","color":"gray"}]
