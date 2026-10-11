# ThreeEras (Três Eras)

Servidor Survival Minecraft que conecta três eras sucessivas — **Medieval**, **Apocalíptica** e **Futurista** — em uma única linha temporal do universo ficcional **Requiemverse**. A história é descoberta em camadas por meio de ruínas, registros, NPCs, artefatos, chefes e eventos.

## Estrutura do repositório

```
ThreeEras/                 # projeto Maven do plugin
├── pom.xml                # Paper API 26.2.build.124-stable, Java 25
└── src/main/
    ├── java/br/com/threeeras/
    │   ├── ThreeErasPlugin.java      # ciclo de vida e ligação dos sistemas
    │   ├── config/                   # mensagens (messages.yml)
    │   ├── era/                      # eras e mundos (eras.yml)
    │   ├── lore/                     # entradas de lore e descobertas (lore.yml)
    │   ├── player/                   # dados dos jogadores
    │   ├── storage/                  # persistência assíncrona (dados.yml)
    │   ├── npc/                      # NPCs e diálogo (npcs.yml)
    │   ├── item/                     # itens personalizados (itens.yml)
    │   ├── quest/                    # missões encadeadas (quests.yml)
    │   ├── boss/                     # sete chefes e regras anômalas (bosses.yml)
    │   ├── evento/                   # eventos agendados (eventos.yml)
    │   └── command/                  # /threeeras <subcomando>
    └── resources/                    # plugin.yml + YAMLs de conteúdo
docs/                      # documentação narrativa (lore)
├── LORE_MASTER.md                 # premissa, cosmologia, linha do tempo
├── SEVEN_SINS.md                  # os sete chefes
├── HOMUNCULI.md                   # os homúnculos e o mistério da lealdade
├── KINGDOM_OF_ROOTS.md            # o reino e a guerra
├── THREE_ERAS_TIMELINE.md         # cronologia e contradições
├── ANORMAL_CONNECTION.md          # conexão com ANORMAL
├── LORE_IMPLEMENTATION.md         # lore → sistemas de jogo
├── CANON_AND_PROPOSALS.md         # cânone / propostas / questões em aberto
└── GUIA_INSTALACAO.md             # build, instalação e checklist
```

## Compilar e testar

```powershell
cd ThreeEras
mvn package
```

Copie `target/ThreeEras.jar` para `plugins/` do seu servidor Paper 26.2 (Java 25). Instruções completas em `docs/GUIA_INSTALACAO.md`.

## Sistemas implementados

| Sistema | Comando | Conteúdo |
|---|---|---|
| Eras e mundos | `/threeeras era` | 3 eras, viagem validada |
| Jogadores + persistência | `/threeeras jogador` | gravação assíncrona em `dados.yml` |
| Lore e descobertas | `/threeeras lore` | 16 entradas, camadas 1–5 |
| NPCs | `/threeeras npc` | 3 NPCs com diálogo condicional |
| Itens | `/threeeras item` | 5 artefatos |
| Missões | `/threeeras quest` | 3 missões encadeadas entre eras |
| Chefes | `/threeeras boss` | 7 chefes, uma regra anômala cada |
| Eventos | `/threeeras evento` | 3 eventos agendados |

## Princípios técnicos

- Sem mods de cliente: tudo funciona em Java e Bedrock (Geyser).
- Thread principal para eventos e comandos; gravação em disco assíncrona com snapshot; desligamento grava de forma síncrona.
- Todas as APIs Bukkit/Paper verificadas contra o jar real da paper-api 26.2.build.124-stable.
- Conteúdo em YAML: ajustar narrativa e balanceamento sem recompilar.
