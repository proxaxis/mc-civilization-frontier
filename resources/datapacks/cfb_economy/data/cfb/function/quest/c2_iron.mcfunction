execute if entity @s[nbt={Inventory:[{id:"minecraft:iron_ingot",count:32}]}] run clear @s minecraft:iron_ingot 32
execute unless entity @s[nbt={Inventory:[{id:"minecraft:iron_ingot"}]}] run tellraw @s [{"text":"鉄の製錬: iron_ingot 32個が必要です","color":"red"}]
give @s coinsje:iron_coin 15
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 鉄の製錬 → MD+15 / XP lv+2","color":"green"}]
