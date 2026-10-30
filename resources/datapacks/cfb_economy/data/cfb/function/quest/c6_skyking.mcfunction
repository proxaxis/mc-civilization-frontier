execute if entity @s[tag=qd_c6_skyking] run return run tellraw @s [{"text":"天空の覇王: 達成済みです","color":"gray"}]
scoreboard players operation @s cfb_q_tmp = @s cfb_k_hollowsov
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_hollowsovb
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"天空の覇王: 討伐が確認できません","color":"red"}]
scoreboard players add @s cfb_k_hollowsovb 1
give @s coinsje:gold_coin 10
experience add @s 800 points
give @s minecraft:beacon 1
give @s minecraft:totem_of_undying 1
tag @s add qd_c6_skyking
tellraw @s [{"text":"[クエスト達成] 天空の覇王 → 金貨x10 / XP+800 / ビーコンx1 / 不死のトーテムx1","color":"green"}]
