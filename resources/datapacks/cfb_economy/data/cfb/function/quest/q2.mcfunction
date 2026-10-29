scoreboard players add @s cfb_quest 0
execute if score @s cfb_quest matches 10.. run give @s coinsje:iron_coin 8
execute if score @s cfb_quest matches 10.. run tellraw @s [{"text":"[クエスト達成] 鉱山の脅威 → 鉄貨8枚","color":"green"}]
execute if score @s cfb_quest matches 10.. run scoreboard players set @s cfb_quest 0
execute if score @s cfb_quest matches ..9 run tellraw @s [{"text":"ゾンビ討伐数が足りません(cfb_questスコアでカウント)","color":"red"}]
