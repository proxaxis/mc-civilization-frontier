effect give @a[distance=..24] minecraft:slowness 2 0
effect give @a[distance=..24] minecraft:hunger 2 0
effect give @a[distance=..24] minecraft:mining_fatigue 2 0
# 亡霊の号令: 5秒ごとに水兵亡霊を召喚
execute if score #fx cfb_boss matches 50 run summon minecraft:drowned ~ ~1 ~ {CustomName:'[{"text":"水兵の亡霊"}]',Tags:["cfb_crew"]}
