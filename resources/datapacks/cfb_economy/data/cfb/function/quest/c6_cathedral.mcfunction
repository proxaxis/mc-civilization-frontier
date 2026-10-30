execute if entity @s[tag=qd_c6_cathedral] run return run tellraw @s [{"text":"腐敗の大聖堂: 達成済みです","color":"gray"}]
scoreboard players operation @s cfb_q_tmp = @s cfb_k_lich
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_lichb
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"腐敗の大聖堂: 討伐が確認できません","color":"red"}]
scoreboard players add @s cfb_k_lichb 1
give @s coinsje:gold_coin 10
experience add @s 800 points
give @s minecraft:echo_shard 4
give @s minecraft:netherite_ingot 2
tag @s add qd_c6_cathedral
tellraw @s [{"text":"[クエスト達成] 腐敗の大聖堂 → gold_coinx10 / XP+800 / echo_shardx4 / netherite_ingotx2","color":"green"}]
