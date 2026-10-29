scoreboard objectives add cfb_days dummy
scoreboard objectives add cfb_quest dummy "クエスト"
scoreboard objectives add cfb_fee dummy
scoreboard players set #tick cfb_days 0
tellraw @a [{"text":"[CFB] 経済システムを読み込みました. MD交換は /function cfb:md/menu","color":"gray"}]
