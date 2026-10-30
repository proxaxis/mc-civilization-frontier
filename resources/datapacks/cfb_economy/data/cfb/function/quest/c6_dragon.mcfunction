execute if entity @s[tag=qd_c6_dragon] run return run tellraw @s [{"text":"終焉の龍と新時代の夜明け: 達成済みです","color":"gray"}]
execute unless entity @s[advancements={minecraft:end/kill_dragon=true}] run return run tellraw @s [{"text":"終焉の龍と新時代の夜明け: 対象の進捗が未達です","color":"red"}]
give @s coinsje:netherite_coin 1
experience add @s 2000 points
give @s minecraft:dragon_egg 1
give @s minecraft:dragon_head 1
tag @s add qd_c6_dragon
tellraw @s [{"text":"[クエスト達成] 終焉の龍と新時代の夜明け → ネザライト貨x1 / XP+2000 / ドラゴンの卵x1 / ドラゴンの頭x1","color":"green"}]
