# 魂の防壁: 柱が生きている限り強耐性
execute if entity @e[tag=cfb_soul_pill,distance=..30] run effect give @s minecraft:resistance 2 3 true
# 飢餓の波浪: 15秒周期(300tick)
execute if score #fx cfb_boss matches 0 run effect give @a[distance=..30] minecraft:hunger 12 40
execute if score #fx cfb_boss matches 0 run tellraw @a[distance=..30] [{"text":"飢餓の波浪が満腹度を蝕む!","color":"dark_red"}]
