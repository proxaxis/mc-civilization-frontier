execute if entity @s[tag=qd_c3_claim] run return run tellraw @s [{"text":"領地の保護と納税: 達成済みです","color":"gray"}]
execute unless score @s cfb_feepaid matches 1.. run return run tellraw @s [{"text":"領地の保護と納税: 要件が未達です","color":"red"}]
give @s coinsje:gold_coin 2
experience add @s 200 points
give @s minecraft:golden_shovel 1
tag @s add qd_c3_claim
tellraw @s [{"text":"[クエスト達成] 領地の保護と納税 → gold_coinx2 / XP+200 / golden_shovelx1","color":"green"}]
