# LORE_IMPLEMENTATION — Como a narrativa vira jogo

## Mapa lore → sistema

| Elemento da lore | Sistema no plugin | Estado |
|---|---|---|
| 3 eras + mundos | `EraManager` + `/threeeras era` | Pronto |
| Dados dos jogadores | `PlayerManager` + `StorageManager` (gravação assíncrona) | Pronto |
| Entradas de lore (camadas 1–5, status, contradições) | `LoreManager` + `lore.yml` | Pronto (39 entradas) |
| NPCs com versões contraditórias | `NpcManager` + `npcs.yml` | Pronto (7 NPCs) |
| Artefatos das três eras | `ItemManager` + `itens.yml` | Pronto (11 itens) |
| Missões encadeadas | `QuestManager` + `quests.yml` | Pronto (6 missões) |
| Os sete chefes | `BossManager` + `bosses.yml` | Pronto (7 chefes) |
| Eventos agendados | `EventManager` + `eventos.yml` | Pronto (6 eventos) |
| Rotas narrativas (branching) | `RotaManager` + `rotas.yml` | Pronto (3 rotas, com entradas exclusivas) |

## As 9 dimensões de cada chefe

| Dimensão | Como é entregue |
|---|---|
| Descoberta | Entradas de lore por era + NPCs que mencionam a criatura |
| Investigação | Documentos que descrevem o comportamento (pilares, escalada, cópias, consumo) |
| Território | Arenas construídas no mundo; o ambiente reage (domínio, estase, consumo) |
| Anomalia | A regra do chefe (uma por criatura, isolada em classe própria) |
| Preparação | Missões que entregam os itens necessários; equipamento do grupo |
| Combate | Mecânica de grupo: papéis (derruba pilares / sustenta / ataca nas janelas) |
| Consequências | Vitória/derrota alteram o mundo (eventos, itens, descobertas) |
| Recompensas | Itens com lore — nunca trivializam o mistério |
| Legado | A criatura é lembrada nas eras seguintes (catálogos, brasões, zonas) |

## Restrições técnicas preservadas

- **Sem mods de cliente**: tudo via plugin, entidades vanilla, partículas, chat, itens. Funciona em Java e Bedrock (Geyser).
- **Thread principal**: eventos, comandos e spawns rodam na thread principal; gravação em disco é assíncrona com snapshot; o desligamento grava de forma síncrona.
- **Escala de 7 jogadores**: entidades limitadas por arena; verificações periódicas espaçadas; nada de spawns massivos.
- **Sem APIs inventadas**: todas as APIs Bukkit/Paper usadas foram verificadas contra o jar real da paper-api 26.2.build.124-stable.

## Fluxo de uma sessão de jogo (exemplo)

1. Jogador entra → dados carregados; era atual: medieval.
2. Lê `/threeeras lore ver fundacao-raizes` (ou encontra o registro no mundo).
3. Fala com o Cronista Anselmo → falas condicionais conforme descobertas.
4. Inicia `conta-que-nao-fecha` → entrega o fragmento do quarto marco.
5. Desbloqueia `faixa-no-estandarte` → encontra o estandarte → recebe a Amostra 7-4.
6. Desbloqueia `palavra-no-arquivo` → entrega a amostra → recebe o **Registro sem origem**.
7. Enfrenta os chefes na ordem definida pela progressão.
8. Eventos agendados (`sussurro-nas-ruinas`, `homunculos-residuais`) mantêm o mundo vivo entre as missões.
