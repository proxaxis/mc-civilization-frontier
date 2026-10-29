tellraw @s [{"text":"=== 職業選択 ===","color":"gold","bold":false}]
tellraw @s [{"text":"いずれかの職業を選択してください. 選択後は他職業のツリーは使用できません.","color":"gray"}]
tellraw @s [{"text":"[守護者] ","color":"aqua","click_event":{"action":"run_command","command":"/trigger cfb_job set 1"}},{"text":"[暗殺者] ","color":"dark_purple","click_event":{"action":"run_command","command":"/trigger cfb_job set 2"}},{"text":"[魔道士] ","color":"blue","click_event":{"action":"run_command","command":"/trigger cfb_job set 3"}},{"text":"[聖職者] ","color":"yellow","click_event":{"action":"run_command","command":"/trigger cfb_job set 4"}}]
