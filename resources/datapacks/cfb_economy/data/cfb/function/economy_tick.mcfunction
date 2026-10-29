# 20分=24000tickを1日として維持費徴収+災害判定
scoreboard players add #tick cfb_days 1
execute if score #tick cfb_days matches 24000.. run function cfb:fee/daily
# 災害クールダウン(6000tick=5分)
scoreboard players remove #disaster_cd cfb_days 1
execute if score #disaster_cd cfb_days matches ..0 run function cfb:disaster/check
# 自然減衰: 稼働停止なら汚染は徐々に下がる(1200tickごとに-1)
scoreboard players add #decay cfb_days 1
execute if score #decay cfb_days matches 1200.. run function cfb:disaster/decay
