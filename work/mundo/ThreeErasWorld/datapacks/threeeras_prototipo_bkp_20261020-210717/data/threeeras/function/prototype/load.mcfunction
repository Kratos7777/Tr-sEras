execute unless data storage threeeras:prototype state{generated:1b} run function threeeras:prototype/start
execute unless data storage threeeras:prototype state{expanded:1b} run function threeeras:prototype/expansion/start
execute if data storage threeeras:prototype state{expanded:1b} unless data storage threeeras:prototype state{expanded2:1b} run function threeeras:prototype/expansion2/start
