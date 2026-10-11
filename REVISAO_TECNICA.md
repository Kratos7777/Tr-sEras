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
