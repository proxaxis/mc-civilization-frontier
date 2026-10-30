execute if entity @s[tag=qd_c1_shelter] run return run tellraw @s [{"text":"荒野の生命線: 達成済みです","color":"gray"}]
execute unless entity @s[advancements={minecraft:adventure/sleep_in_bed=true}] run return run tellraw @s [{"text":"荒野の生命線: 対象の進捗が未達です","color":"red"}]
give @s coinsje:copper_coin 5
experience add @s 50 points
give @s minecraft:bread 8
give @s minecraft:charcoal 16
tag @s add qd_c1_shelter
tellraw @s [{"text":"[クエスト達成] 荒野の生命線 → copper_coinx5 / XP+50 / breadx8 / charcoalx16","color":"green"}]
