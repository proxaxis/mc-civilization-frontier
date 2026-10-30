execute if score @s cfb_hasjob matches 1 run return run tellraw @s [{"text":"既に職業を選択済みです. 変更するにはリセットしてください.","color":"red"}]
puffish_skills category lock @s cfb:assassin
puffish_skills category lock @s cfb:mage
puffish_skills category lock @s cfb:paladin
puffish_skills category unlock @s cfb:guardian
scoreboard players set @s cfb_hasjob 1
function cfb:quest/book
tellraw @s {"text":"職業を選択しました: guardian","color":"gold"}
