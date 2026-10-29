puffish_skills category erase @s cfb:guardian
puffish_skills category erase @s cfb:assassin
puffish_skills category erase @s cfb:mage
puffish_skills category erase @s cfb:paladin
puffish_skills category lock @s cfb:guardian
puffish_skills category lock @s cfb:assassin
puffish_skills category lock @s cfb:mage
puffish_skills category lock @s cfb:paladin
scoreboard players set @s cfb_hasjob 0
tellraw @s {"text":"職業をリセットしました. 再度選択してください.","color":"gold"}
function cfb:job/menu
