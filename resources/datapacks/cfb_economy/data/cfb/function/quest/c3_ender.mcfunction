execute if entity @s[nbt={Inventory:[{id:"minecraft:ender_pearl",count:16}]}] run clear @s minecraft:ender_pearl 16
execute unless entity @s[nbt={Inventory:[{id:"minecraft:ender_pearl"}]}] run tellraw @s [{"text":"虚空の瞳: ender_pearl 16個が必要です","color":"red"}]
give @s coinsje:iron_coin 40
experience add @s 3 levels
tellraw @s [{"text":"[クエスト達成] 虚空の瞳 → MD+40 / XP lv+3","color":"green"}]
