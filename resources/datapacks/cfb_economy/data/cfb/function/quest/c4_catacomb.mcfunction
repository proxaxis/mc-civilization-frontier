scoreboard players operation @s cfb_q_tmp = @s cfb_m_spawner
scoreboard players operation @s cfb_q_tmp -= @s cfb_m_spawnerb
execute if score @s cfb_q_tmp matches ..2 run return run tellraw @s [{"text":"カタコンベの深淵: 達成数 3 未達です","color":"red"}]
scoreboard players add @s cfb_m_spawnerb 3
give @s coinsje:iron_coin 20
experience add @s 350 points
give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}] 3
give @s minecraft:trial_key 1
tellraw @s [{"text":"[クエスト達成] カタコンベの深淵 → iron_coinx20 / XP+350 / potionx3 / trial_keyx1","color":"green"}]
