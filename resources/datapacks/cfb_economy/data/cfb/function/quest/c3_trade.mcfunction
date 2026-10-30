execute if entity @s[tag=qd_c3_trade] run return run tellraw @s [{"text":"交易都市への参入: 達成済みです","color":"gray"}]
execute unless entity @s[advancements={minecraft:adventure/trade=true}] run return run tellraw @s [{"text":"交易都市への参入: 対象の進捗が未達です","color":"red"}]
give @s coinsje:gold_coin 1
experience add @s 150 points
give @s minecraft:emerald 4
tag @s add qd_c3_trade
tellraw @s [{"text":"[クエスト達成] 交易都市への参入 → 金貨x1 / XP+150 / エメラルドx4","color":"green"}]
