# 強風: 3秒ごとに吹き飛ばし
execute if score #fx cfb_boss matches 30 run effect give @a[distance=..20] minecraft:levitation 1 2
execute if score #fx cfb_boss matches 30 run playsound minecraft:entity.breeze.charge master @a ~ ~ ~ 1 0.5
# 砲台掃射: 2.5秒ごと
execute if score #fx cfb_boss matches 20 run summon minecraft:arrow ~ ~6 ~ {Motion:[0.5,-0.6,0.3],damage:2.0}
execute if score #fx cfb_boss matches 70 run summon minecraft:arrow ~ ~6 ~ {Motion:[-0.4,-0.6,-0.3],damage:2.0}
