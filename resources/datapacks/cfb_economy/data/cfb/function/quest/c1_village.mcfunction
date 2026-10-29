execute unless entity @s[advancements={minecraft:adventure/trade=true}] run tellraw @s [{"text":"交易民の発見: 対象の進捗が未達です","color":"red"}]
give @s coinsje:iron_coin 10
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 交易民の発見 → MD+10 / XP lv+2","color":"green"}]
