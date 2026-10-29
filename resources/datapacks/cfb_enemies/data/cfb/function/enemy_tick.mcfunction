# ギミックタイマー(0-99 で 5 秒周期)
scoreboard players add #fx cfb_boss 1
execute if score #fx cfb_boss matches 100.. run scoreboard players set #fx cfb_boss 0
execute as @e[tag=cfb_boss_cornelia] at @s run function cfb:boss/cornelia_fx
execute as @e[tag=cfb_boss_lich] at @s run function cfb:boss/lich_fx
execute as @e[tag=cfb_boss_flagship] at @s run function cfb:boss/flagship_fx
execute as @e[tag=cfb_boss_remnant] at @s run function cfb:boss/remnant_fx
