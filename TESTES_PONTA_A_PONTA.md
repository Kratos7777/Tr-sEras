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
