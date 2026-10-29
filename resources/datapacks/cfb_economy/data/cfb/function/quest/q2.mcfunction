scoreboard players operation @s cfb_q_tmp = @s cfb_q_zombie
scoreboard players operation @s cfb_q_tmp -= @s cfb_q_zb
execute if score @s cfb_q_tmp matches ..9 run return run tellraw @s [{"text":"ゾンビ討伐数が足りません","color":"red"}]
scoreboard players add @s cfb_q_zb 10
give @s coinsje:iron_coin 8
tellraw @s [{"text":"[クエスト達成] 鉱山の脅威 → 鉄貨8枚","color":"green"}]
