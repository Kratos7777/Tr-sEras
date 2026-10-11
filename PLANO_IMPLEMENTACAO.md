# Plano de implementação ThreeEras

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
