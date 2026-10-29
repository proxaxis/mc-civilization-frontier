# EFP閾値判定 — 低階級から順に判定し1回につき1件だけ発火(消費で下の階級へ落ちても再評価されない順序)
execute if score #efp cfb_efp matches 12..29 run function cfb:disaster/dusk
execute if score #efp cfb_efp matches 12..29 run scoreboard players set #disaster_cd cfb_days 6000
execute if score #efp cfb_efp matches 12..29 run scoreboard players remove #efp cfb_efp 12

execute if score #efp cfb_efp matches 30..59 run function cfb:disaster/threat
execute if score #efp cfb_efp matches 30..59 run scoreboard players set #disaster_cd cfb_days 6000
execute if score #efp cfb_efp matches 30..59 run scoreboard players remove #efp cfb_efp 30

execute if score #efp cfb_efp matches 60.. run function cfb:disaster/raid
execute if score #efp cfb_efp matches 60.. run scoreboard players set #disaster_cd cfb_days 6000
execute if score #efp cfb_efp matches 60.. run scoreboard players remove #efp cfb_efp 60

# 閾値未満なら短い周期で再監視(200tick=10秒)
execute if score #disaster_cd cfb_days matches ..0 run scoreboard players set #disaster_cd cfb_days 200
