# 蝕の侵食: 常時ウィザー付与
effect give @a[distance=..24] minecraft:wither 3 0
# 眷属召喚: 10秒ごと
execute if score #fx cfb_boss matches 99 run summon remnants:gravebound_soldier ~ ~1 ~ {Tags:["cfb_minion"],CustomName:'[{"text":"蝕界の眷属"}]'}
