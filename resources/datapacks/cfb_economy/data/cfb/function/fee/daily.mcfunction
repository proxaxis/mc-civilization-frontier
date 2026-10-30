scoreboard players set #tick cfb_days 0
tellraw @a [{"text":"[税務署] 領地維持費の徴収日です. EFP: ","color":"red"},{"score":{"name":"#efp","objective":"cfb_efp"}}]
# 未納(EFP高)ほど災害に近づく
execute as @a run function cfb:fee/collect
scoreboard players add @a cfb_feepaid 1
