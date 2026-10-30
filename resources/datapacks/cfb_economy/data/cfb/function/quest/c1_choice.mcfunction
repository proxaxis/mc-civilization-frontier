execute if entity @s[tag=qd_c1_choice] run return run tellraw @s [{"text":"運命の選択: 達成済みです","color":"gray"}]
execute unless score @s cfb_hasjob matches 1.. run return run tellraw @s [{"text":"運命の選択: 要件が未達です","color":"red"}]
give @s coinsje:copper_coin 5
experience add @s 100 points
give @s minecraft:shield 1
give @s coinsje:copper_coin 5
tag @s add qd_c1_choice
tellraw @s [{"text":"[クエスト達成] 運命の選択 → copper_coinx5 / XP+100 / shieldx1 / copper_coinx5","color":"green"}]
