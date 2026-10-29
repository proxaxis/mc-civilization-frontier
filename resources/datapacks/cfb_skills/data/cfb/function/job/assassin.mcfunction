execute if score @s cfb_hasjob matches 1 run return run tellraw @s [{"text":"既に職業を選択済みです. 変更するにはリセットしてください.","color":"red"}]
puffish_skills category lock @s cfb:guardian
puffish_skills category lock @s cfb:mage
puffish_skills category lock @s cfb:paladin
puffish_skills category unlock @s cfb:assassin
scoreboard players set @s cfb_hasjob 1
tellraw @s {"text":"職業を選択しました: assassin","color":"gold"}
