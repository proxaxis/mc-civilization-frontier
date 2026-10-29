# 20分=24000tickを1日として維持費徴収+災害判定
scoreboard players add #tick cfb_days 1
execute if score #tick cfb_days matches 24000.. run function cfb:fee/daily
# EFP閾値判定(常時監視)
execute if score #efp cfb_efp matches 60.. run function cfb:disaster/raid
execute if score #efp cfb_efp matches 30..59 run function cfb:disaster/threat
execute if score #efp cfb_efp matches 12..29 run function cfb:disaster/dusk
# 自然減衰: 稼働停止なら汚染は徐々に下がる
scoreboard players remove #efp cfb_efp 0
