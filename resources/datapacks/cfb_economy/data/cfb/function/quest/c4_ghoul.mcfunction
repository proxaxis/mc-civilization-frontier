scoreboard players operation @s cfb_q_tmp = @s cfb_k_ghoul
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_ghoulb
execute if score @s cfb_q_tmp matches ..9 run return run tellraw @s [{"text":"蠢く墓地: 達成数 10 未達です","color":"red"}]
scoreboard players add @s cfb_k_ghoulb 10
give @s coinsje:iron_coin 10
experience add @s 200 points
give @s minecraft:rotten_flesh 32
tellraw @s [{"text":"[クエスト達成] 蠢く墓地 → 鉄貨x10 / XP+200 / 腐った肉x32","color":"green"}]
