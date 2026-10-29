scoreboard players set #tick cfb_days 0
tellraw @a [{"text":"[税務署] 領地維持費の徴収日です. 保有 MD の 1%(最低 1 鉄貨)を徴収します.","color":"red"}]
execute as @a[scores={cfb_fee=1..}] run function cfb:fee/collect
execute as @a run function cfb:fee/assess
