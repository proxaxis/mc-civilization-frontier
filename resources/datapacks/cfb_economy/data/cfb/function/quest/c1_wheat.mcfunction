execute if entity @s[nbt={Inventory:[{id:"minecraft:wheat",count:64}]}] run clear @s minecraft:wheat 64
execute unless entity @s[nbt={Inventory:[{id:"minecraft:wheat"}]}] run tellraw @s [{"text":"交易への道: wheat 64個が必要です","color":"red"}]
give @s coinsje:iron_coin 8
experience add @s 1 levels
tellraw @s [{"text":"[クエスト達成] 交易への道 → MD+8 / XP lv+1","color":"green"}]
