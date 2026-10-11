# Protótipo geográfico — Era Medieval

**Estado:** proposta de planejamento aprovada para desenvolvimento conceitual. Não é um mundo construído, não estabelece coordenadas definitivas e não altera o cânone. A queda do Reino das Raízes com sobreviventes permanece o fato já registrado em `CANON_AND_PROPOSALS.md`; o mapa não determina quando, como ou por quem ela ocorre.

## Objetivo desta primeira fatia

Validar a escala de exploração, a legibilidade dos caminhos e o contraste entre assentamento civil e infraestrutura militar antes de construir a Capital inteira ou gerar milhares de chunks. A fatia cobre a Vila Inicial, o corredor principal e um primeiro acesso à Capital. O restante do mundo fica reservado para fases posteriores.

## Esquema espacial (sem coordenadas)

Orientação é apenas uma convenção de leitura do mapa. Distâncias são estimativas de percurso a pé pela rota indicada; relevo, desvios e construções podem alterá-las.

```text
                              NORTE
                  Terras Distantes (expansão futura)
                              │ trilha antiga
        Floresta dos Monstros ─┼── Fronteira das Fortalezas
            │ trilha camuflada │          │ estrada militar
            │                 │          │
      Território do Reino das Raízes ── Capital Medieval
            │                         ╱       │
            │ caminho de patrulha    ╱        │ acesso controlado
            └────────── Vila Inicial ─── Arquivos Enterrados
                              SUL
```

### Regiões e função

| Região | Posição relativa proposta | Função de exploração e campanha | Identidade visual |
|---|---|---|---|
| Vila Inicial | Sul, em terreno habitável e próximo ao limite da área inicial | Tutorial, primeiros NPCs, abrigo e escolha de rota quando o sistema narrativo estiver aprovado | Construções de madeira e pedra local, paliçada reforçada, campos pequenos e sinais de reparos |
| Capital Medieval | Norte/nordeste da Vila, após o primeiro corredor | Centro de autoridade, comércio e missões principais | Muralhas em camadas, portões, torres de vigia, pátios de abastecimento e bairros protegidos |
| Território do Reino das Raízes | Faixa entre a Capital, a floresta e a fronteira | Fortificações, estradas de patrulha e assentamentos menores | Postos camuflados, depósitos, abrigos escavados e torres integradas a elevações naturais |
| Floresta dos Monstros | Oeste/noroeste, separada por vale, rio ou mata densa | Exploração de risco crescente, ruínas e trilhas alternativas | Dossel fechado, clareiras pequenas, ruínas cobertas e visibilidade curta |
| Fronteira das Fortalezas | Leste/nordeste, conectada à Capital por estrada militar | Defesa, movimentação de tropas e conflitos de fronteira | Muralhas escalonadas, torres em linha de visão, portões reforçados e terreno exposto |
| Arquivos Enterrados | Sob uma elevação próxima à Capital, com entrada secundária na estrada | Investigação, preservação de registros e acesso por etapas | Ruína de superfície discreta, corredores subterrâneos e áreas seladas; nada revela cosmologia por si só |
| Terras Distantes | Norte, além de uma barreira natural e da última estrada segura | Área reservada para expansões e transições narrativas | Biomas variados, marcos incompletos e rotas ainda não cartografadas |

## Conexões e ritmo

- **Vila → Capital:** estrada principal protegida, com um ponto de controle e um desvio mais longo por trilha. O caminho deve ensinar navegação e introduzir a escala do reino.
- **Vila → Território das Raízes:** caminho de patrulha, inicialmente opcional; permite encontrar abrigo e um primeiro posto de observação sem exigir combate obrigatório.
- **Capital → Fronteira:** estrada militar direta, com portões que podem ser fechados em eventos futuros; trilhas laterais não substituem a rota principal.
- **Território → Floresta:** acesso gradual por clareira e trilha camuflada. A floresta não deve ser um corredor obrigatório para chegar à Capital.
- **Capital → Arquivos:** entrada pública/administrativa e acesso subterrâneo restrito. O primeiro protótipo pode conter apenas o exterior e uma sala segura, mantendo o complexo maior bloqueado.
- **Capital → Terras Distantes:** estrada termina em posto avançado. A barreira natural dá um limite diegético à primeira área construída e permite expansão sem uma muralha artificial ao redor do mundo.

Como referência de ritmo, a travessia Vila–Capital pode ocupar aproximadamente **5–10 minutos a pé**; o trecho inicial construído pode cobrir cerca de **400–800 blocos de percurso**, sem impor dimensões fixas aos edifícios. A ida da Capital à Fronteira pode ser mais longa, em torno de **8–15 minutos**. São metas para teste de caminhada, não coordenadas ou compromisso de escala final.

## Composição do primeiro trecho jogável

1. Um espaço de chegada seguro na Vila, com cama/abrigo, orientação visual e NPCs de introdução.
2. Uma rua principal e uma pequena praça, sem concentrar todos os serviços no mesmo bloco.
3. Um segmento de estrada que sai da Vila e mostra a Capital ao longe apenas quando o relevo permitir.
4. Um posto de patrulha ou ponte como primeiro marco intermediário; ele oferece um abrigo, uma bifurcação e uma pista opcional.
5. A entrada sul da Capital com portão e área de recepção, sem construir ainda o castelo completo.
6. Um ponto de retorno seguro e sinalização consistente para jogadores que se percam.

A primeira versão deve usar terreno gerado naturalmente e construções manuais/modulares sobre uma cópia descartável do mundo. Não aplicar esquemáticos sobre construções existentes sem pré-visualização, backup e confirmação do espaço vazio.

## Plano de construção progressiva

- **M0 — levantamento local:** confirmar seed/mundo, versão Paper real, regras de terreno e espaço disponível. Ainda não editar o mundo Minefort.
- **M1 — Vila:** construir a praça, abrigo, caminhos e borda defensiva; validar spawn e orientação.
- **M2 — corredor:** conectar ponte/posto de patrulha à Vila e à entrada da Capital; medir tempo real de caminhada e revisar visibilidade.
- **M3 — portão da Capital:** construir somente o acesso inicial e entorno imediato; testar fluxo de NPCs, combate e rotas alternativas.
- **M4 — vertical slice:** testar onboarding, uma missão, uma bifurcação e retorno para a Vila com pelo menos dois jogadores.
- **M5 — expansão:** somente após aprovação do vertical slice, construir bairros da Capital, arquivos, fortificações e floresta em entregas separadas.

A dimensão global de aproximadamente 6.000 × 6.000 blocos permanece uma meta de reserva. Ela não significa gerar os 36 milhões de blocos agora. Usar exploração progressiva; pré-gerar apenas áreas de teste em uma cópia se o servidor e a ferramenta escolhida permitirem e se isso não pressionar armazenamento/memória.

## Regras de leitura visual

- Estruturas militares devem comunicar função: linha de visão, proteção, armazenamento, controle de passagem ou retirada.
- Camuflagem significa integração com relevo e vegetação, não esconder a entrada a ponto de se tornar injusta.
- Usar marcos visuais repetíveis (bandeiras, pedras, postes, lanternas) para guiar a rota principal; trilhas secundárias podem ser mais discretas.
- Variar biomas com transições naturais; reservar terreno aberto para muralhas e campos de visão, e vegetação densa para ocultar observatórios e rotas de retirada.
- Ruínas não devem confirmar causa do Apocalipse, origem dos Sete ou destino completo do Reino.

## Questões que ainda não fixam o mapa

- Seed e mundo de construção ainda não foram escolhidos.
- A posição relativa do castelo/centro político dentro da Capital continua aberta.
- A malha final, o estilo de blocos, a escala dos edifícios e o caminho exato dependem de revisão visual.
- Não se decidiu se o servidor usará mundo próprio ou mapa externo.
- O design não define coordenadas; convertê-lo em coordenadas é uma etapa posterior, após avaliar uma seed e aprovar um esboço no mundo local.

## Importação na Minefort — estratégia segura

A documentação da Minefort consultada orienta a importação por SFTP com FileZilla: parar/preparar o servidor, obter as credenciais do painel, enviar a pasta do mundo preservando `level.dat` e `region/`, selecionar o mundo ativo e reiniciar. O gerenciador de arquivos do painel permite envio, mas a ajuda informa que não permite baixar; manter backup local via SFTP/FTP ou usar o backup do painel. Procedimentos e credenciais podem variar conforme o painel/servidor; confirme no seu servidor antes da transferência.

1. Baixar um backup do mundo atual por SFTP/FTP e guardar cópia sem edição.
2. Construir o protótipo local em uma cópia, nunca no arquivo original.
3. Testar abrir/salvar/reabrir na mesma versão Paper que será usada em produção.
4. Exportar a pasta do mundo com sua raiz e dimensões corretas; evitar estrutura duplicada como `MeuMapa/MeuMapa/level.dat`.
5. Parar o servidor Minefort, enviar por SFTP e conferir os arquivos remotos.
6. Configurar primeiro o nome do mundo ativo pela Minefort; não alterar o mundo principal até validar o mapa importado.
7. Reiniciar e conferir logs, spawn, inventários/estruturas e salvamento; voltar ao backup se houver erro.

**Não foi feita importação nem teste dentro da Minefort.** Este documento é um procedimento de planejamento, não uma garantia de limites de tamanho, velocidade ou compatibilidade do mapa.

## Decisões aprovadas e propostas

- **Aprovado pelo usuário:** prosseguir com o plano de arquitetura e iniciar o protótipo geográfico Vila–Capital; preservar as rotas antigas separadas enquanto a campanha nova é construída.
- **Proposta de trabalho, não cânone:** orientação relativa das regiões, conexões e tempos/distâncias estimados neste documento.
- **Em aberto:** mundo/seed, coordenadas, mapa próprio ou externo, layout interno da Capital e detalhes biográficos além das premissas já fornecidas.
