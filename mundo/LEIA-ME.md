# mundo/ — save do servidor (protótipo medieval)

## O que é

Cópia de trabalho do mundo **medieval** do ThreeEras, recebida em
12/10/2026 no pacote `ThreeEras-Mapa-Expandido-Etapa-2-Minefort-v2.zip`.

| Item | Valor |
|---|---|
| Nome da pasta do mundo | `ThreeErasWorld` (exato — usar este nome no servidor) |
| Seed | `-1369628266` (informada pelo pacote; **não** alterar) |
| Tamanho | ~12 MB, 4 regiões (`r.0.0`, `r.-1.-1`, `r.-1.0`, `r.0.-1`) + entities/poi/data |
| Formato | dimensões em `dimensions/minecraft/overworld/` ( layout do MC moderno) |
| Datapack incluído | `threeeras_prototipo` — constrói as regiões por funções na primeira carga |
| Estado | **protótipo**: não foi carregado em Minecraft/Minefort por mim |

## Regiões (coordenadas do pacote — provisórias, servem ao protótipo)

| Região | Aprox. | Conteúdo |
|---|---|---|
| Vila Inicial | 62, -28 | praça, salão, casas, armazém, paliçada |
| Capital Medieval | 62, -720 | praça, salão de mercado, quartel, torre, muralhas, 4 portões |
| Arquivos Enterrados | 150, -700 | ruína de entrada e câmara subterrânea |
| Território das Raízes | -220, -735 | abrigo de patrulha e observatório |
| Floresta dos Monstros | -440, -955 | mata, clareira, ruína |
| Fronteira das Fortalezas | 500, -980 | bastião e 2 torres |
| Terras Distantes | 100, -1500 | posto de retorno; fim da rota segura |
| Etapa 2 | várias | fazenda de rota (125,-400), pátio de artesãos (165,-720), refúgio florestal (-530,-1060), 2ª linha da Fronteira (780,-1170), rota de reconhecimento até (160,-2250) |

Docs originais: `MAPA-EXPANDIDO.md`, `MAPA-EXPANDIDO-ETAPA-2.md`,
`LEIA-ME-IMPORTACAO.txt`, `LEIA-ME-ETAPA-2.txt`.

## Privacidade (importante)

A cópia versionada aqui **não contém** `players/` nem `session.lock`: o
pacote original trazia dados de um jogador real (UUID, stats, advancements),
que não devem ir a repositório público. O arquivo completo está com você e
no servidor.

## Como usar no Minefort

1. Pare o servidor e faça backup do mundo atual.
2. Extraia `ThreeErasWorld.zip` **no computador** (não no painel).
3. Envie a pasta `ThreeErasWorld` por SFTP/FileZilla para a raiz do
   servidor, preservando a estrutura (`level.dat`, `dimensions/`,
   `datapacks/`, `region/`...).
4. No painel, selecione `ThreeErasWorld` como mundo ativo (level-name).
5. Inicie e aguarde a mensagem do datapack de conclusão da construção.
6. Confira as estruturas in-game antes de tornar este o mundo de produção.

## Como o plugin usa este mundo

`ThreeEras/src/main/resources/eras.yml` aponta a era Medieval para
`ThreeErasWorld`. As outras duas eras seguem sem mundo até você criar.

Para posicionar NPCs e arenas, use os comandos no jogo
(`/threeeras npc criar <id>`, `/threeeras boss criar <id>`,
`/threeeras estrutura construir <id>`) — as coordenadas das regiões acima
indicam onde; o plugin não fixa coordenadas definitivas.
