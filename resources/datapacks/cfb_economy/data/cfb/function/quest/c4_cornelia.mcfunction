execute if entity @s[tag=qd_c4_cornelia] run return run tellraw @s [{"text":"幽霊船団の主: 達成済みです","color":"gray"}]
scoreboard players operation @s cfb_q_tmp = @s cfb_k_ancientve
scoreboard players operation @s cfb_q_tmp -= @s cfb_k_ancientveb
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"幽霊船団の主: 討伐が確認できません","color":"red"}]
scoreboard players add @s cfb_k_ancientveb 1
give @s coinsje:gold_coin 5
experience add @s 500 points
give @s minecraft:turtle_helmet 1
give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:water_breathing"}] 2
tag @s add qd_c4_cornelia
tellraw @s [{"text":"[クエスト達成] 幽霊船団の主 → 金貨x5 / XP+500 / カメの甲羅x1 / 水中呼吸のポーションx2","color":"green"}]
