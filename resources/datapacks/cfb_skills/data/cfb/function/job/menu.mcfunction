tellraw @s [{"text":"=== 職業選択 ===","color":"gold","bold":false}]
tellraw @s [{"text":"いずれかの職業を選択してください. 選択後は他職業のツリーは使用できません.","color":"gray"}]
tellraw @s [{"text":"[守護者] ","color":"aqua","clickEvent":{"action":"run_command","value":"/trigger cfb_job set 1"}},{"text":"[暗殺者] ","color":"dark_purple","clickEvent":{"action":"run_command","value":"/trigger cfb_job set 2"}},{"text":"[魔道士] ","color":"blue","clickEvent":{"action":"run_command","value":"/trigger cfb_job set 3"}},{"text":"[聖職者] ","color":"yellow","clickEvent":{"action":"run_command","value":"/trigger cfb_job set 4"}}]
