execute if entity @s[nbt={Inventory:[{id:"minecraft:redstone",count:32}]}] run clear @s minecraft:redstone 32
execute unless entity @s[nbt={Inventory:[{id:"minecraft:redstone"}]}] run tellraw @s [{"text":"動力の血: redstone 32個が必要です","color":"red"}]
give @s coinsje:iron_coin 15
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 動力の血 → MD+15 / XP lv+2","color":"green"}]
