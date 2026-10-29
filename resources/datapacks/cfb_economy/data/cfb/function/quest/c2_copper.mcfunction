execute if entity @s[nbt={Inventory:[{id:"minecraft:copper_ingot",count:32}]}] run clear @s minecraft:copper_ingot 32
execute unless entity @s[nbt={Inventory:[{id:"minecraft:copper_ingot"}]}] run tellraw @s [{"text":"導管の銅: copper_ingot 32個が必要です","color":"red"}]
give @s coinsje:iron_coin 15
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 導管の銅 → MD+15 / XP lv+2","color":"green"}]
