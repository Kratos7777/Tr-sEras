data modify storage threeeras:prototype state.expanded set value 1b
tellraw @a {"text":"[ThreeEras] Primeira expansao concluida.","color":"green"}
schedule function threeeras:prototype/expansion2/start 2t replace
