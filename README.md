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

Documentação adicional na raiz: `PLANO_IMPLEMENTACAO.md`, `QUEST_CATALOG.md`, `ENDINGS.md`, `TESTES_PONTA_A_PONTA.md` e `REVISAO_TECNICA.md`.

Preparação dos destinos, mapas candidatos, licenças e dependências externas: `docs/MUNDOS_E_DEPENDENCIAS.md`.

## Compilar e testar

```powershell
cd ThreeEras
mvn package
```

Copie `target/ThreeEras.jar` para `plugins/` do seu servidor Paper 26.2 (Java 25). Instruções completas em `docs/GUIA_INSTALACAO.md`.

## Sistemas presentes no código

| Sistema | Comando | Conteúdo |
|---|---|---|
| Eras e mundos | `/threeeras era` | 3 definições; viagem segura com requisito de missão configurável; só `world` está configurado |
| Jogadores + persistência | `/threeeras jogador` | gravação assíncrona em `dados.yml` |
| Lore e descobertas | `/threeeras lore` | 54 entradas, camadas 1–5 |
| NPCs | `/threeeras npc` | 3 NPCs com diálogo condicional |
| Itens | `/threeeras item` | 5 artefatos |
| Missões | `/threeeras quest` | 3 missões encadeadas entre eras |
| Chefes | `/threeeras boss` | 7 definições e efeitos reutilizáveis; encontros completos ainda pendentes |
| Eventos | `/threeeras evento` | 3 eventos agendados |
| Diário | `/threeeras diario` | resumo de era, rota, objetivos e descobertas |
| Tutorial | `/threeeras tutorial reiniciar <jogador>` | reapresentação administrativa sem duplicar kit/progresso |

## Princípios técnicos

- Sem código de cliente customizado; compatibilidade Bedrock via Geyser/Floodgate ainda exige validação num servidor real.
- Thread principal para eventos e comandos; gravação em disco assíncrona com snapshot; desligamento grava de forma síncrona.
- Compilação verificada contra Paper API 26.2; carregamento em servidor e crossplay ainda precisam de teste ponta a ponta.
- Conteúdo em YAML: ajustar narrativa e balanceamento sem recompilar.

## Campanha principal (especificação)

A trajetória narrativa completa proposta para M1–M3, A01–A18 e F01–F04 está em [`docs/CAMPANHA_PRINCIPAL.md`](docs/CAMPANHA_PRINCIPAL.md). O estado real da implementação e as etapas para torná-la jogável estão em [`docs/IMPLEMENTACAO_CAMPANHA.md`](docs/IMPLEMENTACAO_CAMPANHA.md). A especificação está integrada ao pacote; a campanha em si ainda requer as etapas de código, conteúdo e configuração de mundos descritas nesses documentos.

## Protótipo geográfico medieval

A primeira fatia de planejamento da Vila Inicial até a entrada da Capital está em [`docs/PROTOTIPO_GEOGRAFICO_MEDIEVAL.md`](docs/PROTOTIPO_GEOGRAFICO_MEDIEVAL.md). É um protótipo conceitual com regiões, ligações e etapas de validação; não inclui mapa pronto nem coordenadas finais. A aprovação registrada permite continuar esse desenho, preservando as rotas existentes.
