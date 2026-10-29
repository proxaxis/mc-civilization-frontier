scoreboard objectives add cfb_days dummy
scoreboard objectives add cfb_quest dummy "クエスト"
scoreboard objectives add cfb_fee dummy
scoreboard players set #tick cfb_days 0
tellraw @a [{"text":"[CFB] 経済システムを読み込みました. MD交換は /function cfb:md/menu","color":"gray"}]
scoreboard objectives add cfb_q_zombie minecraft.killed:minecraft.zombie
scoreboard objectives add cfb_q_pillager minecraft.killed:minecraft.pillager
scoreboard objectives add cfb_q_wither minecraft.killed:minecraft.wither_skeleton
scoreboard objectives add cfb_q_guardian minecraft.killed:minecraft.elder_guardian
