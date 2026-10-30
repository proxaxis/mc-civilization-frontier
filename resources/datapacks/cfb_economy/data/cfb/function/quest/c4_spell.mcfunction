execute if entity @s[tag=qd_c4_spell] run return run tellraw @s [{"text":"呪文書の結合: 達成済みです","color":"gray"}]
execute unless score @s cfb_spellbound matches 1.. run return run tellraw @s [{"text":"呪文書の結合: 要件が未達です","color":"red"}]
give @s coinsje:iron_coin 10
experience add @s 200 points
give @s minecraft:lapis_lazuli 16
give @s runes:fire_stone 8
tag @s add qd_c4_spell
tellraw @s [{"text":"[クエスト達成] 呪文書の結合 → iron_coinx10 / XP+200 / lapis_lazulix16 / fire_stonex8","color":"green"}]
