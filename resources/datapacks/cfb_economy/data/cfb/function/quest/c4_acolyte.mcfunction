scoreboard players operation @s cfb_q_tmp = @s cfb_k_acolyte
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_acolyteb
execute if score @s cfb_q_tmp matches ..4 run return run tellraw @s [{"text":"異端の信徒: 達成数 5 未達です","color":"red"}]
scoreboard players add @s cfb_k_acolyteb 5
give @s coinsje:iron_coin 10
experience add @s 200 points
give @s minecraft:bone_block 8
tellraw @s [{"text":"[クエスト達成] 異端の信徒 → 鉄貨x10 / XP+200 / 骨ブロックx8","color":"green"}]
