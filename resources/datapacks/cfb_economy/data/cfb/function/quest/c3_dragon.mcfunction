execute unless entity @s[advancements={minecraft:end/kill_dragon=true}] run tellraw @s [{"text":"終焉の龍: 対象の進捗が未達です","color":"red"}]
give @s coinsje:gold_coin 1
give @s coinsje:iron_coin 36
experience add @s 5 levels
tellraw @s [{"text":"[クエスト達成] 終焉の龍 → MD+100 / XP lv+5","color":"green"}]
