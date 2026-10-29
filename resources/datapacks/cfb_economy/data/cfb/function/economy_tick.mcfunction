# 20分=24000tickを1日として維持費徴収判定
scoreboard players add #tick cfb_days 1
execute if score #tick cfb_days matches 24000.. run function cfb:fee/daily
