# Relatório consolidado e arquitetura proposta — ThreeEras

**Objetivo:** orientar a implementação incremental no repositório Java existente. Esta é uma auditoria estática da cópia fornecida, não um teste no Minefort. Não substituir o projeto por uma implementação nova.

## 1. Estado auditado

- Maven, plugin Java, `maven.compiler.release=25`, Paper API `26.2.build.124-stable` com escopo `provided`; `plugin.yml` declara `api-version: 26.2`.
- 55 fontes Java. O `pom.xml` declara apenas a Paper API como dependência runtime/provided. Não há `src/test`.
- Conteúdo identificado: 3 eras (`medieval`, `apocaliptica`, `futurista`), 3 rotas legadas (`raizes`, `cinzas`, `sinal`), 13 quests, 54 entradas de lore, 22 NPCs e 7 chefes.
- Era Medieval aponta para `world`; as outras duas não têm mundo associado. `EraManager` lê nomes e requisitos e consulta mundos já carregados. Não há serviço de ciclo de vida de mundos.
- `PlayerData` usa UUID e guarda era, descobertas, quests, rota legada e dados de onboarding. `StorageManager` salva dados YAML. Ainda não há escolha M1–M3, estado de trajetória A01–A18, Social Links, elegibilidade de final ou transição pendente.
- A última compilação registrada na sessão anterior passou com 55 fontes; não foi repetida durante a auditoria atual. O JAR distribuído tem timestamp 11/10/2026 18:54. Não houve inicialização de Paper real nesta auditoria.
- Esta cópia não contém `.git`; confirme o repositório/branch reais antes de commit/push.

### Classificação

**Já existe:** plugin Paper, comandos, leitura de configuração YAML, persistência por UUID, teleporte a mundo carregado com busca de spawn seguro, quests, rotas antigas, NPCs, lore, itens, dungeons, chefes e eventos.

**Parcial:** viagem (não carrega mundos); conteúdo das quests e rotas (não é a campanha nova); migração (compatibilidade de campos existentes, mas sem esquema da campanha); crossplay (sem teste real nem dependência API Geyser/Floodgate).

**Ainda não existe:** gerenciamento de mundo externo/configuração do ciclo de vida, transação de viagem recuperável após queda, campanhas M1–M3/A01–A18/F01–F04 jogáveis, estados narrativos persistentes novos, Social Links, mapas reais e testes em Minefort.

**Quebrado:** nenhum defeito de execução foi confirmado porque não foi iniciado servidor nesta auditoria. Não confundir falta de teste com prova de funcionamento.

## 2. Decisões técnicas recomendadas para reduzir pendências

Estas decisões são recomendações de engenharia, não decisões de lore:

1. Preservar IDs e progresso de `raizes`, `cinzas`, `sinal`. Não converter essas rotas automaticamente para M1–M3.
2. Manter os IDs atuais de Era (`medieval`, `apocaliptica`, `futurista`) como chaves internas; os IDs sugeridos `era_medieval` etc. não justificam migração.
3. Armazenar campanha nova individualmente por UUID em campos próprios e com `campaign-schema-version`; manter o YAML/StorageManager existente até que volume e concorrência provem necessidade de banco.
4. Evitar estado global por padrão. Missões cooperativas devem declarar explicitamente quais fatos são compartilhados.
5. Não escolher Multiverse-Core nesta fase. Primeiro testar mundos pré-importados e a integração Paper em ambiente local. Definir uma única implementação de ciclo de vida atrás de interface. Se o plugin carregar mundos por API, validar custo no thread principal; se usar Multiverse, comprovar a versão e formato de armazenamento aceitos no Paper/Minefort real. Não instalar dois gerenciadores concorrentes.
6. A primeira entrega de viagem pode exigir que o destino esteja previamente carregado. A evolução para carregamento sob demanda só após testar latência, espaço de pastas e suporte Minefort.
7. Não adicionar integração Geyser/Floodgate ao plugin para comandos/missões comuns. Usar API Bukkit neutra e testar cliente Bedrock. Só integrar APIs se uma função exclusiva exigir isso.
8. Manter criação de mundo, edição e importação separadas do runtime narrativo. O plugin não deve gerar nem sobrescrever o mapa automaticamente.

## 3. Componentes recomendados (sem criar classes antes de conferir equivalentes)

- `EraManager` atual: registro de IDs/nomes/requisitos. Não assumir que ele carrega mundos.
- `WorldLifecycle` (ou integração existente): descobrir, validar e, futuramente, carregar/descarregar um mundo. Uma implementação ativa de cada vez.
- `EraTravelService`: validar requisito, destino e ponto seguro; iniciar viagem; teletransportar; concluir ou recuperar viagem interrompida.
- `CampaignState`/`CampaignService`: estado narrativo por jogador, separado de rota legada; validar transições e requisitos.
- `CampaignContentLoader`/validador: ler conteúdo modular, IDs estáveis e referências; recusar conteúdo malformado com mensagens precisas.
- `RelationshipService`: vínculos individuais por UUID + NPC ID, níveis e recompensa já reivindicada.
- Integração com quests/NPCs: reutilizar eventos e progresso atuais; não criar uma engine paralela sem comprovar que a existente não atende.
- `StorageManager`: manter YAML para a escala inicial; separar gravações sensíveis ao ciclo de vida da viagem se necessário e usar versão/migração.

## 4. Modelo de persistência sugerido

Por jogador, chave UUID existente:

- `campaign.schema-version`
- `campaign.route` (M1/M2/M3 ou vazio)
- `campaign.active-trajectory`
- `campaign.completed-trajectories[]`
- `campaign.choices.<choice-id>: <option-id>`
- `campaign.evidence.<evidence-id>: found/verified` (sem usar texto como ID)
- `campaign.flags.<flag-id>: boolean/string`
- `campaign.affinities.<entity-id>` apenas se houver regra aprovada para isso
- `campaign.ending` + estado de filiação mutuamente exclusivo
- `relationships.<npc-id>.rank`, progresso/requisitos e recompensas já concedidas
- `travel.pending`: source era/posição, destination era e estágio; vazio quando resolvido

O esquema é proposta técnica. A implementação deve escolher o menor modelo capaz de satisfazer a campanha, validar IDs e preservar compatibilidade com jogadores existentes. Não gravar progresso por nome visível.

## 5. Transição temporal segura

1. Confirmar jogador online, destino existente/carregado e gate de campanha cumprido.
2. Encontrar spawn configurado e seguro; falhar sem mover/alterar o jogador se não existir.
3. Persistir intenção de viagem (origem e destino) e salvar; não marcar a era de destino como concluída ainda.
4. Teletransportar na thread correta da API Bukkit/Paper.
5. Confirmar sucesso; atualizar era atual e limpar intenção pendente.
6. Se o jogador desconectar/reiniciar durante o processo, resolver ao próximo login com destino válido ou retornar à origem segura, sem apagar missão/escolhas.
7. Registrar diagnóstico sem expor dados pessoais ou credenciais.

A durabilidade exata diante de crash precisa ser testada com o StorageManager e o padrão real de gravação YAML. A sequência não deve ser declarada atômica sem essa prova.

## 6. Mundo e ferramentas

O protótipo Medieval está em `PROTOTIPO_GEOGRAFICO_MEDIEVAL.md`: Vila ao sul, Capital ao norte/nordeste, corredor e posto intermediário; distâncias são metas de caminhada, sem coordenadas finais. Construir por regiões. Não gerar todos os chunks do objetivo 6.000×6.000.

Minefort publicou orientação de importação por SFTP/FileZilla: backup, upload preservando raiz e arquivos (`level.dat`, `region/`), definir mundo ativo e reiniciar. A ajuda do painel informa que o gerenciador web envia, mas não baixa arquivos. Confirmar fluxo, credenciais e capacidade no painel do usuário antes de produção.

WorldEdit é candidato para construir/editar em Paper; validar o JAR da versão Minecraft escolhida antes de instalar. A documentação WorldEdit 7.4 lista Paper e seu changelog registra atualização para MC 26.2 na 7.4.4. Schematic é estrutura, não mundo completo. Litematica pode ser ferramenta do cliente construtor, mas não está aprovada nem compatibilidade foi validada aqui. Multiverse-Core permanece não escolhido até teste de versão, formato de mundo e Minefort.

## 7. Plano verificável

### Fase 0 — confirmar ambiente
Paper/Java instalados, versão Minefort, permissões de plugin, armazenamento, SFTP/backup e nome dos mundos. Não enviar credenciais ao código ou ao GitHub.

### Fase 1 — auditável local
Compilar; iniciar Paper local compatível; confirmar carregamento; obter logs e referência de performance. Registrar versões reais.

### Fase 2 — mundo mínimo
Usar duas cópias descartáveis pequenas. Importar uma por vez; verificar spawn, persistência após restart, criação/carregamento e descarregamento se adotado. Decidir um único gerenciador.

### Fase 3 — transição
Implementar uma viagem mínima sem campanha nova; cobrir mundo ausente, spawn perigoso, teleport recusado, logout e restart no meio; verificar que o jogador não perde dados.

### Fase 4 — estado campanha
Criar schema versionado e migração somente aditiva; cobrir dados antigos sem conversão de rota.

### Fase 5 — campanha vertical
Um percurso Medieval, uma missão verificável e uma escolha persistente; reconexão/restart; depois ampliar.

### Fase 6 — mapa/relacionamentos
Construir Vila–corredor–entrada da Capital numa cópia; validar rota visual e duas alternativas. Prototipar um Social Link antes de escrever nove arcos.

### Fase 7 — trajetórias e implantação
Expandir por grupos, testar gates e finais; teste Java/Bedrock; importar em Minefort com backup e rollback documentados.

## 8. Aprovações e bloqueios verdadeiros

**Aprovado pelo usuário:** separar campanha nova das rotas legadas; prosseguir com protótipo conceitual Vila–Capital; desenvolvimento incremental; cânone preservado.

**Proposto, não cânone:** direção geográfica relativa, tempos de caminhada, formato de dados de Social Link, serviço de viagem e esquema de estado acima.

**Só o usuário/painel pode confirmar:** versão e limites do servidor Minefort atual, nome/seed do mundo real, método de acesso disponível, política de plugins/gerenciadores e mapa próprio ou externo. Não bloquear a implementação de schema/configuração por essas incógnitas, mas não fazer deploy nem finalizar coordenadas até confirmação.

**Lore em aberto:** biografias, diálogos finais, mortes/reviravoltas, causa do Apocalipse, origem/objetivos dos Sete, significado de “Maculado”, detalhes de Genevieve/Lilia e consequência cosmológica dos finais. Kilo deve implementar dados genéricos/IDs e aguardar validação criativa.

## 9. Critérios de aceite iniciais

- Código compila com versões do repositório sem atualização automática.
- Plugin inicia em Paper da mesma versão com logs sem erros do ThreeEras.
- Rotas legadas e `dados.yml` antigo continuam legíveis.
- Viagem nunca marca sucesso se mundo, spawn ou teleporte falhar.
- Desconexão/restart em transição recupera o jogador e não apaga campanha.
- Dados de duas contas permanecem isolados.
- Java e Bedrock conseguem usar as mesmas interações de campanha.
- Nenhuma edição de terreno acontece sem cópia/backup e alvo aprovado.
- Campanha só é anunciada como jogável para conteúdo realmente implementado e testado.
