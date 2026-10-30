execute if entity @s[tag=qd_c1_magic] run return run tellraw @s [{"text":"初めての術式: 達成済みです","color":"gray"}]
execute unless score @s cfb_hasjob matches 1.. run return run tellraw @s [{"text":"初めての術式: 要件が未達です","color":"red"}]
execute unless score @s cfb_spellbound matches 1.. run return run tellraw @s [{"text":"初めての術式: 要件が未達です","color":"red"}]
give @s coinsje:copper_coin 10
experience add @s 100 points
give @s minecraft:lapis_lazuli 8
tag @s add qd_c1_magic
tellraw @s [{"text":"[クエスト達成] 初めての術式 → copper_coinx10 / XP+100 / lapis_lazulix8","color":"green"}]
