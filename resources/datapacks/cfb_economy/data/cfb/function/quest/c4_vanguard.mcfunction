scoreboard players operation @s cfb_q_tmp = @s cfb_k_rustedgua
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_rustedguab
execute if score @s cfb_q_tmp matches ..4 run return run tellraw @s [{"text":"鋼の盾兵: 達成数 5 未達です","color":"red"}]
scoreboard players add @s cfb_k_rustedguab 5
give @s coinsje:iron_coin 15
experience add @s 250 points
give @s minecraft:shield 1
tellraw @s [{"text":"[クエスト達成] 鋼の盾兵 → 鉄貨x15 / XP+250 / 盾x1","color":"green"}]
