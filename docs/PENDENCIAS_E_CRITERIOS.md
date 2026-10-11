# Pendências e critérios de aceite para o Kilo Code

Esta lista converte o planejamento em tarefas organizáveis dentro do projeto Java. A prioridade é segurança dos dados e uma fatia pequena funcional, não preencher tudo de uma vez.

## Preparação

- [ ] Confirmar que o Kilo está no repositório correto e branch de trabalho; a pasta distribuída anteriormente não tinha `.git`.
- [ ] Revalidar Paper, Java, Maven, Geyser/Floodgate e configuração real. Não atualizar versões sem plano.
- [ ] Confirmar se o ambiente Minefort permite mundos adicionais, plugins necessários, SFTP e backups. Não guardar senhas no repositório.
- [ ] Criar backup/cópia antes de qualquer teste com mundo.

## Código

- [ ] Definir schema da campanha separado das rotas legadas, versionado e por UUID.
- [ ] Validador de conteúdo: IDs duplicados/ausentes, referências quebradas, ciclos de requisitos e finais mutuamente exclusivos.
- [ ] Serviço de viagem com validação de gate/destino/spawn, estado pendente, falhas recuperáveis e API Bukkit na thread correta.
- [ ] Uma implementação de gerenciamento de mundos, escolhida por teste; não combinar gerenciadores concorrentes.
- [ ] Uma rota/missão/decisão como vertical slice; salvar/recarregar/reiniciar.
- [ ] Prototipar um vínculo secundário e confirmar que recompensa não duplica em reconexão.
- [ ] Só então expandir M1–M3, A01–A18 e F01–F04 por etapas.

## Mapa

- [ ] Escolher mundo/seed de trabalho ou obter autorização de mapa externo e licença.
- [ ] Construir apenas Vila, corredor e entrada da Capital numa cópia.
- [ ] Medir caminhadas, validar marcos e alternativas, revisar spawn.
- [ ] Confirmar o mapa antes de converter a proposta em coordenadas.
- [ ] Gerar/transferir apenas região necessária; medir tamanho e desempenho antes de ampliar.

## Testes exigidos antes de cada implantação

- [ ] Build reprodutível e testes automatizados disponíveis.
- [ ] Paper inicia sem erro; configurações carregam e ids são validados.
- [ ] Mundos existentes/carregados, destino ausente e mundo offline.
- [ ] Spawn sem local seguro e teleport recusado.
- [ ] Jogador desconecta e servidor reinicia durante a viagem.
- [ ] Persistência e migração com dados antigos; isolamento de dois jogadores.
- [ ] Gate de rota, missão, escolha, recompensa e resultado parcial.
- [ ] Cliente Java e Bedrock em servidor de teste.
- [ ] Minefort: importação em cópia, restart, validação e procedimento de rollback.

## Informações não disponíveis localmente

- Versão/build atualmente instalado na Minefort.
- Acesso a SFTP/FTP e capacidade/limite de transferência do servidor do usuário.
- Seed e pasta do mundo existente, coordenadas e mapa aprovado.
- Permissão/plano para instalar plugins adicionais ou habilitar UDP/Bedrock.
- Aprovação de detalhes narrativos ainda marcados como abertos.

O Kilo pode concluir auditoria, arquitetura, configuração e testes locais sem essas informações. Deve parar antes de upload remoto, coordenadas finais, alterações destrutivas ou lore inventada.
