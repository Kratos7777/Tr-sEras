tellraw @a {"text":"[ThreeEras] Expandindo o mapa: Capital, Reino das Raizes, floresta, fronteira, arquivos e Terras Distantes.","color":"gold"}
function threeeras:prototype/expansion/capital/build
execute in minecraft:overworld positioned -220 0 -735 run function threeeras:prototype/expansion/roots/camp
execute in minecraft:overworld positioned -440 0 -955 run function threeeras:prototype/expansion/forest/ruins
execute in minecraft:overworld positioned 500 0 -980 run function threeeras:prototype/expansion/frontier/fortress
execute in minecraft:overworld positioned 435 0 -910 run function threeeras:prototype/expansion/frontier/watchtower
execute in minecraft:overworld positioned 575 0 -1040 run function threeeras:prototype/expansion/frontier/watchtower
execute in minecraft:overworld positioned 150 0 -700 run function threeeras:prototype/expansion/archive/entrance
execute in minecraft:overworld positioned 100 0 -1500 run function threeeras:prototype/expansion/distant/post
schedule function threeeras:prototype/expansion/roads/capital_00 2t replace
