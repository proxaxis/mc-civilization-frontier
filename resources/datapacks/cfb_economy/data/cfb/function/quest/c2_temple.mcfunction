execute unless entity @s[advancements={minecraft:adventure/hero_of_the_village=true}] run tellraw @s [{"text":"森の寺院: 対象の進捗が未達です","color":"red"}]
give @s coinsje:iron_coin 30
experience add @s 3 levels
tellraw @s [{"text":"[クエスト達成] 森の寺院 → MD+30 / XP lv+3","color":"green"}]
