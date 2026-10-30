scoreboard players operation @s cfb_q_tmp = @s cfb_k_soulbound
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_soulboundb
execute if score @s cfb_q_tmp matches ..4 run return run tellraw @s [{"text":"誓約の剣士: 達成数 5 未達です","color":"red"}]
scoreboard players add @s cfb_k_soulboundb 5
give @s coinsje:iron_coin 15
experience add @s 250 points
give @s minecraft:iron_sword 1
tellraw @s [{"text":"[クエスト達成] 誓約の剣士 → 鉄貨x15 / XP+250 / 鉄の剣x1","color":"green"}]
