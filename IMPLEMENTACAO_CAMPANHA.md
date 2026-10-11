# Implementação da campanha principal ThreeEras

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
