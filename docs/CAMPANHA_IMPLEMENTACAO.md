# CAMPANHA_IMPLEMENTACAO — especificação, plano, catálogo, finais e testes

Documento consolidado que substitui: `IMPLEMENTACAO_CAMPANHA.md`, `PLANO_IMPLEMENTACAO.md`, `QUEST_CATALOG.md`, `ENDINGS.md`, `REVISAO_TECNICA.md`, `TESTES_PONTA_A_PONTA.md` e `MUNDOS_E_DEPENDENCIAS.md`.

Referências: narrativa em [CAMPANHA_PRINCIPAL.md](CAMPANHA_PRINCIPAL.md) · controle de cânone em [CANON_AND_PROPOSALS.md](CANON_AND_PROPOSALS.md) · protótipo geográfico em [PROTOTIPO_GEOGRAFICO_MEDIEVAL.md](PROTOTIPO_GEOGRAFICO_MEDIEVAL.md).


---

# IMPLEMENTACAO_CAMPANHA

﻿# Implementação da campanha principal ThreeEras

Este documento acompanha [`CAMPANHA_PRINCIPAL.md`](CAMPANHA_PRINCIPAL.md), que contém a trajetória narrativa fornecida e é a referência de conteúdo para a campanha. A especificação preserva o cânone: os detalhes novos continuam propostas até validação do autor.

## Estado real nesta cópia

A campanha completa **ainda não está implementada no jogo**. O plugin compila e possui infraestrutura reutilizável de quests, rotas, eras, NPCs, itens, lore e persistência individual em YAML, mas a campanha especificada não é equivalente ao conteúdo atual.

- Há três eras configuradas, mas apenas `world` está associado a um mundo carregável.
- Há três rotas antigas (`raizes`, `cinzas`, `sinal`), atribuídas automaticamente por descobertas. Elas não correspondem a M1/M2/M3.
- O catálogo de quests existente contém missões demonstrativas e expedições; não contém os seis capítulos de cada rota nem as 18 trajetórias A01–A18.
- Escolhas, evidências verificadas, relações entre facções, elegibilidade de finais e filiação a apóstolos ainda não são estados de jogador.
- Os finais F01–F04 são descritos na especificação, mas não têm execução no plugin.
- A passagem para outras eras depende de mundos e conteúdo que não estão instalados/configurados. Não há mapas adicionados ao servidor.

## Plano técnico por etapas

| Etapa | Entrega | Base reutilizada | Critério de conclusão |
|---|---|---|---|
| C0 — Fundação e compatibilidade | Validar ids, versão do esquema narrativo, migração e relatório de conflitos com as rotas existentes | `PlayerData`, `PlayerManager`, `StorageManager`, `rotas.yml` | Dados antigos continuam legíveis; escolha de campanha não muda automaticamente por ordem de YAML |
| C1 — Estado narrativo | Persistir rota M, trajetória ativa/concluída, decisões, objetivos opcionais, evidências verificadas, aliados, afinidades e final | `PlayerData` + `dados.yml`; sem novo banco | Reconnect/restart preservam estados; ids técnicos estáveis; evolução versionada |
| C2 — Modelo de conteúdo | Registrar rotas e trajetórias de YAML modular, validar referências e gerar erros claros | loaders de `QuestManager`, `RotaManager`, `LoreManager` | catálogo integral 3/18 validado; requisitos sem ciclos ou ids ausentes |
| C3 — Capítulos medievais | Implementar M1.1–M3.6 como missões verificáveis; escolha explícita de rota; consequências persistentes | quests, objetivos, NPCs, lore, itens | cada rota tem seis missões e transição desbloqueável; rota antiga permanece compatível |
| C4 — 18 trajetórias apocalípticas | Adicionar missões distintas, ramificações, objetivos opcionais e resultados parciais | mesma engine de quests e estado C1 | todas A01–A18 têm início, clímax, conclusão e gate validáveis |
| C5 — Era futurista e finais | Conteúdo de investigação, elegibilidade configurável, aceitar/recusar vínculos, final secreto | `EraManager`, missões, estado narrativo | F01–F04 persistem; filiações mutuamente exclusivas; recusas não forçam outro caminho |
| C6 — mundo e cooperação | Destinos/mundos, spawn, proteção de estruturas, escolha individual e regras de objetivos compartilhados | eras, NPCs, setups, Geyser/Floodgate | integração real sem sobrescrever construções; testes em Paper e Java/Bedrock |
| C7 — validação e lançamento | Testes automatizados de estado/configuração + roteiro manual no servidor | compilação Maven e ambiente Paper | relatório separa testes executados de verificações que exigem servidor real |

## Compatibilidade e migração das rotas existentes

A campanha usa os ids narrativos estáveis `M1`, `M2`, `M3`, `A01`–`A18`, `F01`–`F04`. As rotas existentes têm ids e gatilhos distintos e continuam necessárias para não reinterpretar progresso de jogadores. A integração deve guardar um `campaign-version` e a rota nova em campos próprios, ou migrar jogadores somente após uma regra explícita e reversível. Não usar `rotaAtual` antigo como se significasse M1/M2/M3.

A campanha é individual por padrão, alinhada ao `PlayerData`. Objetivos compartilhados devem ser opt-in e especificados por missão, nunca presumidos como estado coletivo do servidor.

## Gatilhos e dependências em aberto

- Definir/confirmar os mundos e respectivos spawns para Apocalíptica e Futurista.
- Confirmar se M1/M2/M3 substituem a escolha de rota da lore antiga ou coexistem como uma campanha versionada. Até lá, manter separadas.
- Validar conteúdo proposto e nomes/personagens antes de apresentá-los como cânone.
- Projetar arenas de dungeon em área dedicada/instanciada antes de reutilizá-las para missões que possam danificar terreno de jogador.
- Integrar diálogos e escolhas a uma interface compatível com Java/Bedrock; o comando textual é fallback, não substituto de uma experiência narrativa final.

## Teste manual inicial depois da integração de código

1. Entre com uma conta nova e confirme que o onboarding atual continua funcionando.
2. Selecione uma rota e verifique que somente suas seis trajetórias ficam disponíveis.
3. Complete missões com resultado normal e parcial; compare escolhas, evidências e diário.
4. Reinicie o servidor e reconecte; confira persistência de rota, missão, escolhas e objetivos opcionais.
5. Teste jogadores com rotas diferentes e confirme isolamento do progresso individual.
6. Teste cada final, recusa de vínculo, final secreto e bloqueio de filiação simultânea.
7. Só depois valide viagens, spawn, NPCs, estruturas e clientes Bedrock em ambiente real.

Este roteiro descreve validação futura; não afirma que esses testes foram executados.


---

# PLANO_IMPLEMENTACAO

﻿# Plano de implementação ThreeEras

Este plano segue os prompts fornecidos e separa o que existe no código da campanha ainda especificada. O pacote recebido não contém histórico Git nem acesso ao ambiente Minefort; as alterações são locais.

## Inventário técnico

- Maven, Paper API `26.2.build.124-stable`, Java 25.
- Infraestrutura existente: dados individuais em YAML, eras, quests, rotas antigas, NPCs, itens, lore, dungeons, eventos e sete definições de chefes.
- Só a era Medieval aponta para um mundo configurado. Apocalíptica e Futurista não possuem mundo associado.
- O primeiro acesso, diário, rotas antigas, viagens seguras e gates de missão foram melhorados previamente.
- A nova campanha completa está especificada em [`docs/CAMPANHA_PRINCIPAL.md`](docs/CAMPANHA_PRINCIPAL.md); diferenças e sequência técnica em [`docs/IMPLEMENTACAO_CAMPANHA.md`](docs/IMPLEMENTACAO_CAMPANHA.md).
- Dungeon ainda pode alterar terreno existente; homúnculos e encontros com fases não estão implementados; servidor Paper/Geyser/Floodgate real não foi testado.

## Status

| Etapa | Status |
|---|---|
| Correções de inicialização, persistência, recompensas repetidas e objetivos de item | Implementadas e compiladas na rodada anterior |
| Onboarding, diário, reset de tutorial, viagem segura e gates atuais | Implementados e compilados na rodada anterior |
| Auditoria e documentação técnica/cânone | Documentada; riscos e limitações registrados |
| Nova especificação da campanha 3 rotas + 18 trajetórias + 4 finais | Incorporada em `docs/CAMPANHA_PRINCIPAL.md` |
| Plano de integração e conflito com rotas existentes | Incorporado em `docs/IMPLEMENTACAO_CAMPANHA.md` |
| Estado persistente para escolhas/evidências/finais da nova campanha | Pendente (C1) |
| Loader modular e implementação das rotas M1–M3 | Pendente (C2–C3) |
| Implementação jogável das trajetórias A01–A18 | Pendente (C4) |
| Finais funcionais F01–F04 | Pendente (C5) |
| Mundos, mapas, estruturas seguras e testes multiplayer | Pendente (C6–C7) |

## Decisão de compatibilidade adotada

A campanha nova não substitui silenciosamente `raizes`, `cinzas` e `sinal`. Os ids antigos e seus dados precisam continuar válidos. A rota M1/M2/M3 será um estado de campanha próprio, com versão/migração explícita, evitando converter progresso legado por inferência.

## Próximos passos

1. Fechar o modelo de estado narrativo individual e sua migração.
2. Definir formato dos arquivos de conteúdo e validador de referências.
3. Implementar seleção e seis missões de cada rota medieval.
4. Implementar trajetórias apocalípticas em grupos incrementais, sem tarefas genéricas repetidas.
5. Integrar os três finais compartilhados e o final secreto com filiação exclusiva.
6. Configurar mundos e só então testar a campanha dentro do Paper, incluindo Java/Bedrock.

## Próxima etapa aprovada — protótipo geográfico

O escopo aprovado agora é conceitual: Vila Inicial, estrada/corredor, posto intermediário e acesso sul da Capital. Ver `docs/PROTOTIPO_GEOGRAFICO_MEDIEVAL.md`. Não há coordenadas definitivas nem mundo construído. Antes de editar um mundo real, validar seed, versão Paper, cópia/backup e método de importação Minefort.


---

# QUEST_CATALOG

# Catálogo de missões atual

Este catálogo reflete `ThreeEras/src/main/resources/quests.yml`. Identificadores, requisitos e recompensas são conteúdo configurado; o estado de implementação abaixo descreve o código atual e não valida a experiência num servidor em execução.

## Missões lineares de investigação

| ID | Era | Pré-requisito | Objetivos configurados | Recompensa | Estado |
|---|---|---|---|---|---|
| `conta-que-nao-fecha` | Medieval | — | Descobrir `marcos-contagem`; entregar `fragmento-quarto-marco` | — | Iniciada automaticamente no primeiro acesso; fluxo ainda requer teste em servidor |
| `faixa-no-estandarte` | Apocalíptica | `conta-que-nao-fecha` | Descobrir `faccoes-sobreviventes`; entregar `estandarte-faixa-vermelha` | `amostra-7-4` | Implementada por YAML e manager |
| `palavra-no-arquivo` | Futurista | `faixa-no-estandarte` | Descobrir `catalogo-erebus-amostra-7-4`; entregar `amostra-7-4` | `registro-sem-origem` | Implementada por YAML e manager |

## Rotas de personagens e registros

| ID | Era | Pré-requisito | Objetivos configurados | Recompensa | Estado |
|---|---|---|---|---|---|
| `trilha-da-fronteira` | Medieval | — | Descobrir `guerra-cronologia`; entregar `espada-da-fronteira` | `diario-do-estrategista` | Implementada por YAML e manager |
| `mascara-e-verdade` | Apocalíptica | `trilha-da-fronteira` | Descobrir `culto-dos-cinzas`; entregar `mascara-do-culto` | `chave-de-contencao` | Implementada por YAML e manager |
| `arquivo-em-branco` | Futurista | `palavra-no-arquivo` | Descobrir `fenomeno-orfao` e `sinal-verdade` | `marca-do-herege` | Implementada por YAML e manager |
| `coroa-e-consequencia` | Medieval | — | Descobrir `rei-da-coroa`; entregar `marca-de-soldado` | `coroa-de-sete-pontas` | Implementada por YAML e manager |
| `estrada-que-mentia` | Apocalíptica | `mascara-e-verdade` | Descobrir `estrada-dos-marcos`; entregar `mapa-rachado` | `fragmento-de-lingua-morta` | Implementada por YAML e manager |
| `porta-selada-por-dentro` | Futurista | `arquivo-em-branco` | Descobrir `laboratorio-9`; entregar `cristal-de-leitura` | `gravacao-do-silencio` | Implementada por YAML e manager |

## Expedições

| ID | Era | Pré-requisito/rota | Objetivo | Recompensa | Estado |
|---|---|---|---|---|---|
| `expedicao-cripta` | Medieval | — | Concluir `cripta-do-rei` | `insignia-rachada` | Manager reconhece dungeon concluída; geração segura e conclusão pelo chefe ainda precisam integração |
| `expedicao-cinzas` | Apocalíptica | `expedicao-cripta`, rota `cinzas` | Concluir `labirinto-das-cinzas` | `marca-de-soldado` | Idem; rota e destino devem ser configurados |
| `expedicao-sinal` | Futurista | `mascara-e-verdade`, rota `sinal` | Concluir `nucleo-do-sinal` | `chave-de-contencao` | Idem; rota e destino devem ser configurados |
| `expedicao-catacumbas` | Medieval | rota `raizes` | Concluir `catacumbas-do-herege` | `coroa-de-sete-pontas` | Idem; rota e destino devem ser configurados |

## Observações de implementação

- Objetivos atuais: descoberta, possuir item, entregar item e concluir dungeon. Não há objetivos de visita a região, fabricar, decisão narrativa ou evento.
- A persistência de passos e recompensas é individual por jogador. Não há compartilhamento de objetivos para grupos.
- Missões com requisitos apontam para IDs existentes no YAML; as rotas ainda dependem de seleção/configuração durante o jogo.
- O código não implementa estados de falha, prazo ou repetibilidade das missões.
- A conclusão natural de expedições e a cadeia entre chefe, dungeon e recompensa requerem integração antes de declarar esses fluxos como ponta a ponta.


---

# ENDINGS

# Finais possíveis — propostas não canônicas

Este documento cumpre a etapa de explorar finais sem escolher um desfecho oficial. Nenhuma proposta está implementada no plugin ou aprovada como cânone. Os requisitos são rascunhos para orientar uma futura decisão do criador.

## A — Contenção

- **Ideia:** os jogadores limitam uma ameaça conhecida e mantêm as comunidades capazes de sobreviver.
- **Possíveis pré-requisitos:** evidências de pelo menos duas eras; decisão de preservar uma instalação; participação no confronto final.
- **Consequência possível:** regiões permanecem exploráveis, mas algumas manifestações continuam sem explicação.
- **Pós-campanha:** investigar ocorrências remanescentes e proteger rotas.
- **Estado:** proposta; causa do apocalipse e identidade da ameaça não definidas.

## B — Libertação

- **Ideia:** os jogadores interrompem um vínculo ou mecanismo de obediência cuja natureza ainda precisa ser definida.
- **Possíveis pré-requisitos:** registros independentes sobre homúnculos; escolha de não destruir uma fonte de evidência.
- **Consequência possível:** libertar indivíduos ou comunidades provoca efeitos difíceis de prever.
- **Pós-campanha:** acompanhar consequências locais e registros contraditórios.
- **Estado:** proposta; não revela por que os homúnculos obedecem.

## C — Transgressão

- **Ideia:** os jogadores usam uma descoberta perigosa para mudar as condições de sobrevivência.
- **Possíveis pré-requisitos:** acesso a tecnologia futurista e conhecimento obtido em eras anteriores.
- **Consequência possível:** ganhos imediatos vêm acompanhados de mudanças persistentes no mundo.
- **Pós-campanha:** explorar áreas transformadas e investigar efeitos colaterais.
- **Estado:** proposta; tecnologia, custo e resultado não definidos.

## D — Verdade incompleta

- **Ideia:** os jogadores reúnem evidências suficientes para reconstruir uma parte da história e aceitam que algumas perguntas não têm resposta conclusiva.
- **Possíveis pré-requisitos:** registros opcionais das três eras e comparação de relatos incompatíveis.
- **Consequência possível:** facções adotam interpretações distintas, sem uma explicação cosmológica universal.
- **Pós-campanha:** continuar explorando arquivos e missões opcionais.
- **Estado:** proposta; não estabelece origem dos Sete nem causa do apocalipse.

## Aprovação necessária antes de implementar

Escolher quais finais existem, seus pré-requisitos, consequências persistentes e se podem coexistir numa campanha cooperativa. Nenhuma das quatro opções é final verdadeiro, secreto ou superior neste documento.


---

# REVISAO_TECNICA

# Revisao tecnica — ThreeEras

## Escopo

Auditoria feita sobre a copia enviada em `Tr-sEras-main.zip`. O arquivo ZIP nao contem metadados Git; portanto, esta revisao e uma copia local independente e nao foi enviada ao GitHub.

## Correcoes aplicadas

- Resolvida a inicializacao prematura de dependencias de `PlayerManager` em listeners e gerenciadores de quest, rota e dungeon. Os componentes agora consultam o gerenciador apos a inicializacao do plugin.
- O salvamento agora remove progresso de quests abandonadas/concluidas e apaga a rota quando ela e removida, evitando que dados antigos reaparecam ao entrar novamente.
- Protegido o arquivo `dados.yml`: YAML invalido nao e carregado como vazio para depois ser sobrescrito. O encerramento tambem sincroniza gravacoes para impedir que um autosave antigo substitua os dados finais.
- O progresso de objetivos por itens agora e verificado apos a atualizacao do inventario e quando o jogador coleta itens do chao.
- A entrega do livro-guia nao substitui o item que o jogador estiver segurando; itens sem espaco sao deixados no chao.
- O recarregamento do armazenamento e bloqueado enquanto houver jogadores online, alteracoes pendentes ou gravacao em curso. Mensagens novas usam os valores padrao mesmo em instalacoes com `messages.yml` antigo.
- Reexecutar o setup nao entrega outro kit inicial a quem ja o recebeu e remove NPCs anteriores antes de recria-los.
- A conclusao de uma dungeon nao paga recompensas novamente para o mesmo jogador.

## Validacao

- O primeiro acesso inicia a missao configurada em `jogadores.missao-inicial` e agenda o teleporte para um local seguro perto do spawn do mundo carregado da era inicial. Mundo ausente nao e criado e o jogador nao e enviado para coordenada invalida.
- `/threeeras diario` exibe era, rota, contagem de descobertas, missoes ativas/concluidas, proximo objetivo e missoes liberadas; as mensagens funcionam via chat para Java e Bedrock.
- `/threeeras tutorial reiniciar <jogador>` permite repetir o livro/apresentacao no proximo login sem alterar kit ou progresso.
- `/threeeras era viajar <id>` agora permite viagem do jogador quando a missão de desbloqueio configurada foi concluída. O destino usa spawn seguro; mundos ausentes ou descarregados não são criados. Admin continua com `era ir` para testes.
- Foi documentado o estado real de compatibilidade Geyser/Floodgate e pesquisados candidatos de mapas com termos explícitos para servidores pequenos não comerciais. Nenhum JAR ou mapa externo foi instalado; Minefort e a atualização dos mapas precisam de validação manual.
- Foram adicionados o plano por dependencias, catalogo das missoes configuradas, propostas de finais sem canonizacao e checklist para validacao num servidor Paper.

- Compilacao Maven concluida com sucesso apos implementar viagem: 55 arquivos Java compilados com Java 25 e a versao Paper API declarada (`26.2.build.124-stable`).
- Os 12 arquivos YAML de recursos foram analisados com SnakeYAML sem erros.
- JAR atualizado em `dist/ThreeEras.jar`.
- O build foi de empacotamento (`package`) com testes ignorados. O projeto nao inclui suite automatizada de testes; nao houve teste de servidor em execucao. A checklist manual esta em `TESTES_PONTA_A_PONTA.md`.

## Riscos que permanecem

1. **Dungeon altera terreno existente:** `/threeeras dungeon entrar` gera salas na posicao atual do jogador e usa `setType` nos blocos sem verificar o que havia antes. Em servidor com construcoes, pode destruir terreno/estruturas. E necessario definir uma area dedicada ou um sistema de instancias antes de usar em producao.
2. **Chefe sem conclusao automatica:** `DungeonManager.chefeDerrotado` existe, mas nenhuma chamada o conecta ao evento de morte do chefe. A dungeon pode exigir conclusao administrativa manual e nao fecha o ciclo de jogo planejado.
3. **Setup altera o mundo:** `/threeeras setup` constrói estruturas diretamente no mundo e pode sobrescrever blocos em execucoes repetidas. Os NPCs e kits foram protegidos contra duplicacao, mas a construcao das estruturas nao tem pre-visualizacao, backup ou confirmacao.
4. **Cooldown do kit nao persiste:** o intervalo do comando de kit fica somente em memoria e reinicia junto com o servidor.

Esses pontos dependem de decisões de produto/mundo e nao foram alterados nesta rodada para evitar escolher coordenadas, comportamento de instancias ou regras de repetibilidade sem especificacao.


---

# TESTES_PONTA_A_PONTA

# Checklist de testes ponta a ponta

Os testes abaixo dependem de um servidor Paper 26.2 com Java 25 e, para crossplay, Geyser/Floodgate compatíveis. Não foram executados em um servidor nesta rodada.

## Build e inicialização

- [ ] `mvn package` termina sem erro; o JAR tem `plugin.yml` e todos os YAMLs.
- [ ] Paper inicia com Java 25, carrega ThreeEras e não registra exceções de inicialização.
- [ ] Configurações ausentes são criadas sem sobrescrever arquivos existentes.
- [ ] YAML inválido em `dados.yml` é preservado e impede gravação destrutiva.
- [ ] Desligamento grava os dados finais; autosave não sobrescreve um snapshot novo.

## Jogadores e progressão

- [ ] Jogador sem registro recebe estado inicial, kit uma vez e missão inicial.
- [ ] Relogin e reinício não repetem kit, tutorial concluído nem recompensas.
- [ ] `/threeeras tutorial reiniciar <jogador>` reapresenta o guia no próximo acesso sem dar outro kit nem alterar missões/descobertas.
- [ ] Descoberta de lore avança objetivo correspondente.
- [ ] Coleta e entrega de item avançam após a mudança do inventário; entrega consome a quantidade exata.
- [ ] Progresso e descobertas continuam após desconexão e reinício.
- [ ] Diário mostra era, rota, missões em andamento, próximo objetivo e descobertas.
- [ ] Jogador sem permissão não executa operações administrativas.

## Mundo e segurança

- [ ] Mundo de era não configurado ou indisponível produz mensagem clara e não teleporta.
- [ ] `/threeeras era viajar <id>` respeita `requisito-missao`; admin `era ir` mantém bypass para teste.
- [ ] Ponto de chegada tem chão sólido, espaço para o jogador e não é líquido/perigoso.
- [ ] Setup não é executado sobre construções sem confirmação/backup.
- [ ] Dungeon não substitui terreno de jogadores; encerramento, desconexão e repetição são seguros.
- [ ] Mundos existentes nunca são recriados por uma configuração incompleta.

## Combate, eventos e cooperação

- [ ] Chefe usa mecânica, apresentação, condição de vitória e recompensa uma única vez.
- [ ] Morte, fuga, desconexão e reinício durante encontro deixam o estado recuperável.
- [ ] Evento temporizado cancela tarefas ao fim e ao desabilitar plugin.
- [ ] Participantes recebem progresso e recompensas sem duplicação; entrada tardia e desconexão não bloqueiam o grupo.

## Java/Bedrock

- [ ] Repetir entrada, teleportes, comandos, NPCs, itens e mensagens em cliente Java e Bedrock.
- [ ] Verificar títulos, sons, partículas e menus com Geyser; manter alternativa em mensagens/comandos.
- [ ] Confirmar nomes de jogadores Floodgate e permissões sem confiar em prefixos como autorização.


---

# MUNDOS_E_DEPENDENCIAS

# Mundos, mapas e plugins externos

Pesquisa registrada em 11/10/2026. Nenhum mapa ou plugin externo foi baixado ou instalado nesta cópia; as escolhas de mundo e a hospedagem precisam ser confirmadas antes de preparar os destinos. O ThreeEras compila somente com Paper API em escopo `provided`.

## Hospedagem e crossplay

- A documentação oficial atual do Geyser lista Java 26.2 como versão suportada e descreve Geyser-Spigot para Paper. Floodgate é complemento opcional para autenticar contas Bedrock sem conta Java; não substitui Geyser.
- Em Paper, o plugin Geyser é a opção direta. A conexão Bedrock usa UDP; a porta precisa estar disponível/configurada no painel da hospedagem. O Floodgate exige configuração própria e decisões de autenticação.
- A lista oficial de provedores com instruções não confirma Minefort nesta verificação. Antes da instalação, confirme no painel Minefort se é permitido adicionar os JARs e expor/configurar a porta UDP. Sem esse acesso, crossplay permanece não validado.
- Multiverse-Core não é requisito do plugin atual; Paper pode carregar mundos já presentes na pasta do servidor. Não adicionar um gerenciador de mundos sem necessidade e compatibilidade confirmadas.
- WorldEdit/WorldGuard, LuckPerms, EssentialsX e CoreProtect não são dependências do ThreeEras. São opções operacionais do servidor, não instaladas aqui. O CoreProtect ou backup equivalente ajudaria a auditar alterações, mas não torna segura uma geração que sobrescreve blocos.
- Não foi adicionada integração de API Geyser/Floodgate ao plugin. Nenhuma função do jogo deve depender de uma tela exclusiva Bedrock; o diário usa mensagens de chat.

## Mapas candidatos encontrados

As páginas dos autores concedem uso não comercial em servidor pequeno (até 20 jogadores) com crédito ao link original. Isso parece compatível com o grupo pequeno descrito no prompt, desde que o servidor continue não comercial e o crédito seja mantido. São candidatos, não mapas aprovados/instalados.

| Era desejada | Candidato e fonte | Uso declarado pelo autor | Limites antes de usar |
|---|---|---|---|
| Medieval | [Medieval / Fantasy Castle Ruins](https://www.planetminecraft.com/project/medieval-castle-ruins/) | Página oferece downloads Java/Bedrock; autor permite servidor pequeno não comercial até 20 jogadores com crédito. | É um mundo survival com ruínas, não um reino medieval completo. A página indica 1.21+; testar cópia em Paper 26.2 e verificar chests/loot, datapacks e dimensões antes de migrar. |
| Apocalíptica | [Abandoned City — Apocalypse Survival World](https://www.planetminecraft.com/project/post-apocalyptic-abandoned-city-survival-compatible-download/) | Página oferece downloads Java/Bedrock e permite servidor pequeno não comercial até 20 jogadores com crédito. | É uma ilha/cidade nevada de survival, escolha estética que precisa aprovação. A página indica 1.21+; testar conversão em cópia e conferir datapacks/estruturas antes de produção. |
| Futurista | Nenhum candidato recomendado nesta rodada. | — | O resultado encontrado com tema futurista tinha restrição explícita contra uso em servidor sem permissão do autor; outros resultados eram antigos, pagos ou exigiam validação adicional. Melhor construir um distrito original ou pedir autorização para um mapa específico. |

As páginas e seus termos podem mudar. Releia os termos no momento de baixar; mantenha cópia do link, autoria, data, arquivo original e checksum. Não redistribua os arquivos de mapa dentro deste ZIP. Teste qualquer conversão em um mundo separado e preserve o mapa original.

## Decisões necessárias

1. Definir se o servidor usará mapas próprios ou os candidatos acima.
2. Confirmar os mundos existentes no Minefort e seus nomes reais antes de editar `eras.yml`.
3. Confirmar que a hospedagem permite Geyser/Floodgate e a porta UDP necessária.
4. Só então carregar mapas em mundos dedicados e configurar spawn; o plugin não cria nem substitui mundos automaticamente.

## Fontes técnicas primárias

- [Geyser — versões suportadas](https://geysermc.org/wiki/geyser/supported-versions/)
- [Geyser — configuração](https://geysermc.org/wiki/geyser/setup/)
- [Floodgate — visão geral](https://geysermc.org/wiki/floodgate/)
- [Geyser — provedores de hospedagem documentados](https://geysermc.org/wiki/geyser/supported-hosting-providers/)

## Atualização Minefort — verificação documental em 11/10/2026

Atualização à nota anterior: a lista de provedores da Geyser agora contém uma entrada para Minefort. Ela orienta ativar **Allow Bedrock** no painel (Connect Support) e indica as formas de conexão da rede Minefort. A própria lista ressalta que os dados de provedores são contribuições da comunidade, não verificação oficial da equipe Geyser; isso não comprova o estado deste servidor ou plano específico.

A central de ajuda da Minefort descreve importação de mundos usando SFTP/FileZilla e credenciais do painel. Também descreve o upload pelo gerenciador web; a central informa que esse gerenciador não permite baixar os arquivos, então manter backup local requer FTP/SFTP. Para importar, preservar a pasta raiz e verificar `level.dat` e `region/`; definir o mundo ativo no painel e reiniciar. Os passos exatos podem variar conforme o servidor/painel atual.

Fontes consultadas:
- Minefort, [Import world](https://help.minefort.com/en/article/import-world-ffvurt/).
- Minefort, [Accessing and uploading files](https://help.minefort.com/en/article/accessing-and-uploading-files-to-your-server-16sv2vl/).
- Minefort, [How to download my world](https://help.minefort.com/en/article/how-to-download-my-world-17yoart/).
- GeyserMC, [Supported Hosting Providers](https://geysermc.org/wiki/geyser/supported-hosting-providers/).

Não houve acesso ao painel do usuário, transferência ou validação de importação. A compatibilidade do mapa e o espaço/limite de upload permanecem pendentes de teste no próprio servidor.


---

# MUNDO REAL RECEBIDO (12/10/2026)

- Save: `ThreeErasWorld`, seed `-1369628266`, ~12 MB, 4 regioes
  (`r.0.0`, `r.-1.-1`, `r.-1.0`, `r.0.-1`), layout de dimensoes em
  `dimensions/minecraft/overworld/`.
- Datapack incluido: `threeeras_prototipo` (constroi as regioes por
  funcoes na primeira carga; aguardar mensagem de conclusao).
- Repositorio: `mundo/ThreeErasWorld.zip` (4,5 MB) **sem** dados de
  jogador; coordenadas das regioes e importacao no Minefort em
  `mundo/LEIA-ME.md`.
- `eras.yml`: era Medieval aponta para `ThreeErasWorld`.
- Coordenadas das regioes (provisorias, do pacote): Vila Inicial 62,-28;
  Capital Medieval 62,-720; Arquivos Enterrados 150,-700; Territorio das
  Raizes -220,-735; Floresta dos Monstros -440,-955; Fronteira das
  Fortalezas 500,-980; Terras Distantes 100,-1500. Etapa 2: fazenda
  125,-400; patio de artesaos 165,-720; refugio florestal -530,-1060;
  2a linha da Fronteira 780,-1170; rota de reconhecimento ate 160,-2250.
- Aviso: nao foi carregado em Minecraft/Minefort por mim. Validar in-game
  com backup antes de virar mundo de producao; nao alterar a seed nem
  mover a pasta.
