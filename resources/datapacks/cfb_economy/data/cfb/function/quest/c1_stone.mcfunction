execute if entity @s[nbt={Inventory:[{id:"minecraft:cobblestone",count:64}]}] run clear @s minecraft:cobblestone 64
execute unless entity @s[nbt={Inventory:[{id:"minecraft:cobblestone"}]}] run tellraw @s [{"text":"石積みの基盤: cobblestone 64個が必要です","color":"red"}]
give @s coinsje:iron_coin 5
experience add @s 1 levels
tellraw @s [{"text":"[クエスト達成] 石積みの基盤 → MD+5 / XP lv+1","color":"green"}]
