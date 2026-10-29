execute if entity @s[nbt={Inventory:[{id:"minecraft:oak_log",count:64}]}] run clear @s minecraft:oak_log 64
execute if entity @s[nbt={Inventory:[{id:"minecraft:oak_log"}]}] unless entity @s[nbt={Inventory:[{id:"minecraft:oak_log",count:64}]}] run tellraw @s [{"text":"木材64個が必要です","color":"red"}]
give @s coinsje:iron_coin 5
tellraw @s [{"text":"[クエスト達成] 開拓の第一歩 → 鉄貨5枚","color":"green"}]
