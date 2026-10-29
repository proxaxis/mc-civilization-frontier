execute if entity @s[nbt={Inventory:[{id:"minecraft:wheat",count:64}]}] run clear @s minecraft:wheat 64
give @s coinsje:iron_coin 8
tellraw @s [{"text":"[クエスト達成] 交易への道 → 鉄貨8枚","color":"green"}]
