data modify storage threeeras:prototype state set value {generated:1b}
tellraw @a {"text":"[ThreeEras] Construindo o protótipo medieval automaticamente...","color":"gold"}
execute in minecraft:overworld positioned 62 0 -28 over motion_blocking_no_leaves run function threeeras:prototype/village
execute in minecraft:overworld positioned 62 0 -248 over motion_blocking_no_leaves run function threeeras:prototype/outpost
execute in minecraft:overworld positioned 62 0 -448 over motion_blocking_no_leaves run function threeeras:prototype/gate
schedule function threeeras:prototype/road/segment_00 2t replace
