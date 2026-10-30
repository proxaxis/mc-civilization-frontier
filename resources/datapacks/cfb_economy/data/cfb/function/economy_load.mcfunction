scoreboard objectives add cfb_days dummy
scoreboard objectives add cfb_quest dummy "クエスト"
scoreboard objectives add cfb_qmenu trigger
scoreboard objectives add cfb_died minecraft.custom:minecraft.deaths
scoreboard objectives add cfb_tsd minecraft.custom:minecraft.time_since_death
scoreboard players enable @a cfb_qmenu
scoreboard objectives add cfb_fee dummy
scoreboard objectives add cfb_q_tmp dummy
scoreboard objectives add cfb_q_zb dummy
scoreboard objectives add cfb_q_pb dummy
scoreboard objectives add cfb_q_wb dummy
scoreboard objectives add cfb_q_gb dummy
scoreboard players set #tick cfb_days 0
tellraw @a [{"text":"[CFB] 経済システムを読み込みました. MD交換は /function cfb:md/menu","color":"gray"}]
