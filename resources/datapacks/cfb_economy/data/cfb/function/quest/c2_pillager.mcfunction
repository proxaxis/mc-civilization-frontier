scoreboard players operation @s cfb_q_tmp = @s cfb_q_pillager
scoreboard players operation @s cfb_q_tmp -= @s cfb_q_pb
execute if score @s cfb_q_tmp matches ..9 run return run tellraw @s [{"text":"拠点防衛: 討伐数 10 未達です","color":"red"}]
scoreboard players add @s cfb_q_pb 10
give @s coinsje:iron_coin 20
experience add @s 2 levels
tellraw @s [{"text":"[クエスト達成] 拠点防衛 → MD+20 / XP lv+2","color":"green"}]
