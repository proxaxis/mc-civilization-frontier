execute store result score @s cfb_q_tmp run clear @s bcn:quarry 0
execute if score @s cfb_q_tmp matches ..0 run return run tellraw @s [{"text":"大地を穿つ爪: 露天掘り機x1が必要です","color":"red"}]
execute store result score @s cfb_q_tmp run clear @s bcn:landmark 0
execute if score @s cfb_q_tmp matches ..2 run return run tellraw @s [{"text":"大地を穿つ爪: ランドマークx3が必要です","color":"red"}]
give @s coinsje:iron_coin 15
experience add @s 300 points
give @s minecraft:diamond 2
give @s bcn:gear_iron 2
tellraw @s [{"text":"[クエスト達成] 大地を穿つ爪 → 鉄貨x15 / XP+300 / ダイヤモンドx2 / 鉄歯車x2","color":"green"}]
