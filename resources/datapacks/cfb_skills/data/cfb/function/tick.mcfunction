execute as @a[scores={cfb_job=1}] run function cfb:job/guardian
execute as @a[scores={cfb_job=2}] run function cfb:job/assassin
execute as @a[scores={cfb_job=3}] run function cfb:job/mage
execute as @a[scores={cfb_job=4}] run function cfb:job/paladin
scoreboard players set @a[scores={cfb_job=1..}] cfb_job 0
scoreboard players enable @a cfb_job
