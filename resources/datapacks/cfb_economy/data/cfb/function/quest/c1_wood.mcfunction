execute if entity @s[nbt={Inventory:[{id:"minecraft:oak_log",count:64}]}] run clear @s minecraft:oak_log 64
execute unless entity @s[nbt={Inventory:[{id:"minecraft:oak_log"}]}] run tellraw @s [{"text":"開拓の第一歩: oak_log 64個が必要です","color":"red"}]
give @s coinsje:iron_coin 5
experience add @s 1 levels
tellraw @s [{"text":"[クエスト達成] 開拓の第一歩 → MD+5 / XP lv+1","color":"green"}]
