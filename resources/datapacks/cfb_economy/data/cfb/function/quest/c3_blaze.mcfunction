execute if entity @s[nbt={Inventory:[{id:"minecraft:blaze_rod",count:16}]}] run clear @s minecraft:blaze_rod 16
execute unless entity @s[nbt={Inventory:[{id:"minecraft:blaze_rod"}]}] run tellraw @s [{"text":"炎の証: blaze_rod 16個が必要です","color":"red"}]
give @s coinsje:iron_coin 40
experience add @s 3 levels
tellraw @s [{"text":"[クエスト達成] 炎の証 → MD+40 / XP lv+3","color":"green"}]
