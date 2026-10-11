# PROMPT PARA O KILO CODE — ORGANIZAR E IMPLEMENTAR THREEERAS

Você está no repositório Java existente do ThreeEras. Use os documentos desta pasta como briefing e compare-os com o código real antes de editar. Organize a documentação aprovada dentro do repositório (preferencialmente `docs/`, atualizando arquivos equivalentes em vez de duplicar). Não copie cegamente arquivos sobre versões mais recentes.

## Objetivo
Auditar, planejar e depois implementar ThreeEras em etapas pequenas: gerenciamento/viagem entre eras, persistência recuperável, campanha ramificada, relações NPC e protótipo Medieval. Preservar o projeto e deixar claro o que está realmente funcional.

## Fonte de verdade e limites
Leia `docs/RELATORIO_E_ARQUITETURA.md`, `docs/PENDENCIAS_E_CRITERIOS.md`, `docs/CANON_AND_PROPOSALS.md`, `docs/CAMPANHA_PRINCIPAL.md`, `docs/IMPLEMENTACAO_CAMPANHA.md` e `docs/PROTOTIPO_GEOGRAFICO_MEDIEVAL.md`.

- Cânone e lore: o criador decide. Não invente biografias, diálogos definitivos, mortes, revelações ou explicações cosmológicas para preencher lacunas.
- Mantenha `raizes`, `cinzas`, `sinal` e dados existentes. M1–M3 usam estado próprio/versionado; sem conversão automática.
- Não faça alterações remotas destrutivas, não envie para Minefort sem autorização explícita do usuário, não coloque credenciais no código.
- O mapa geográfico é conceitual: não invente coordenadas nem sobrescreva `world`.
- Use interfaces simples; uma implementação de gerenciamento de mundo por vez. Não instale Multiverse/WorldEdit/Geyser no projeto sem provar necessidade e compatibilidade.
- Mantenha APIs Bukkit/Paper na thread apropriada. Ações de I/O não devem bloquear ticks.

## Ordem de trabalho
1. Audite branch, fontes, configurações, testes, dependências e diff. Confirme versão real do servidor somente se acesso houver. Apresente breve plano e evidências.
2. Organize/atualize docs do repositório. Registre decisões aprovadas separadas de propostas.
3. Confirme build atual. Não atualize Java/Paper automaticamente.
4. Implemente schema versionado por UUID para campanha, sem mexer nas rotas legadas.
5. Implemente e valide uma única transição segura, incluindo mundo/spawn ausente, teleport falho, logout e restart.
6. Faça uma vertical slice da Medieval (uma rota, uma quest e uma escolha persistente). Reconnect/restart não pode duplicar prêmio nem apagar dados.
7. Prototipe um Social Link e teste o modelo antes de criar os nove arcos.
8. Ajude a construir o mapa apenas em mundo cópia; primeiro Vila–corredor–entrada da Capital. Não importe mundo Minefort sem confirmação e backup.
9. Expanda trajetórias e finais gradualmente, cada entrega compilável e com critérios de aceite.

Se um passo depende de informação ausente, avance o trabalho independente e registre o bloqueio específico. Não substitua uma validação real por afirmação.

## Testes e entrega
Compile; rode testes disponíveis; valide configuração e persistência; inicie Paper local compatível quando possível. Faça matriz de falhas de transição e teste Java/Bedrock apenas em ambiente real disponível. Informe o que não foi testado.

Revise `git diff`, proteja dados privados, faça commits pequenos em branch de trabalho e só envie ao GitHub depois de confirmar o remoto e a branch. Não faça deploy Minefort. Entregue resumo de arquivos, mudanças, comandos/testes executados, resultados, riscos e pendências.
