# GUIA DE INSTALAÇÃO — Servidor ThreeEras

## 1. Requisitos

| Item | Versão |
|---|---|
| Java | **Temurin 25** (o projeto compila com `--release 25`) |
| Paper | **26.2** (build.124-stable é a API declarada no `pom.xml`) |
| Maven | 3.9+ (apenas para compilar) |
| Geyser + Floodgate | builds compatíveis com Paper 26.2 (para jogadores Bedrock) |

## 2. Compilar o plugin

Na sua máquina (Windows), dentro da pasta `ThreeEras/`:

```powershell
cd ThreeEras
mvn package
```

Saída esperada: `BUILD SUCCESS` e o arquivo `ThreeEras/target/ThreeEras.jar`.

> Build verificado nesta cópia: `mvn package` compilou 55 arquivos Java com Java 25 e a paper-api `26.2.build.124-stable`. Isso confirma compilação e empacotamento; não substitui um teste com servidor Paper em execução. Não há suíte automatizada de testes no projeto.

## 3. Instalar

1. Copie `target/ThreeEras.jar` para a pasta `plugins/` do servidor.
2. (Opcional) Instale Geyser e Floodgate conforme a documentação deles.
3. Inicie o servidor uma vez — os arquivos de configuração são criados em `plugins/ThreeEras/`:
   - `config.yml` — `geral.debug`, `jogadores.era-inicial`, `armazenamento.autosave-segundos`
   - `eras.yml` — as três eras e seus mundos
   - `lore.yml` — 54 entradas de lore
   - `rotas.yml` — 3 rotas com entradas exclusivas
   - `npcs.yml` — NPCs e posições
   - `itens.yml` — artefatos
   - `quests.yml` — missões encadeadas
   - `bosses.yml` — os sete chefes
   - `eventos.yml` — eventos agendados
4. Pare o servidor, ajuste os YAMLs e inicie novamente (ou use `/threeeras reload` para config, mensagens, eras, lore, NPCs, itens, quests, chefes e eventos).
5. `/threeeras diario` mostra era, rota, progresso das missões e descobertas. A missão inicial é configurada por `jogadores.missao-inicial`.

## 4. Configurar as eras (mundos)

A era Medieval já aponta para o mundo padrão (`world`). Apocalíptica e Futurista estão sem destino. O plugin não cria mundos automaticamente. Crie/carregue os mundos com o painel/gerenciador escolhido e só então configure os nomes existentes em `eras.yml`:

```yaml
eras:
  medieval:
    mundo: world          # nome da pasta do mundo
  apocaliptica:
    mundo: ""             # configure após criar/carregar o mundo
  futurista:
    mundo: ""
```

Depois: `/threeeras era listar` deve mostrar `mundo: carregado`.

## 5. Permissões

| Nó | Efeito | Padrão |
|---|---|---|
| `threeeras.admin` | Comandos administrativos (reload, boss criar/remover, item dar, quest de terceiros, etc.) | `op` |

## 6. Referência rápida de comandos

```
/threeeras ajuda                                  # lista tudo
/threeeras era listar|info <id>|viajar <id>       # viagem normal; requisito em eras.yml
/threeeras era ir <id>                            # teleporte administrativo para teste
/threeeras diario                                 # progresso e próximo objetivo
/threeeras tutorial reiniciar <jogador>            # reapresenta tutorial, sem kit/progresso (admin)
/threeeras lore listar [era|camada|status]        # entradas de lore
/threeeras lore ver <id>                          # ler + registrar descoberta
/threeeras npc listar|criar <id>|remover <id>|ir <id>
/threeeras item listar|info <id>|dar <id> [qtd]|identificar
/threeeras quest listar|info <id>|iniciar <id>|abandonar <id>
/threeeras boss listar|info <id>|criar <id>|remover <id>|ir <id>
/threeeras evento listar|forcars <id>
/threeeras armazenamento|jogador [nick]|reload
```

## 7. Colocar o servidor no ar (checklist)

1. [ ] `mvn package` com sucesso.
2. [ ] JAR em `plugins/`; Geyser/Floodgate instalados e validados para a versão do servidor (não verificado nesta cópia).
3. [ ] Mundos das três eras criados e apontados em `eras.yml`.
4. [ ] `/threeeras era listar` com os três mundos "carregado".
5. [ ] NPCs posicionados: `/threeeras npc criar cronista-anselmo` (e os outros dois) nos locais construídos.
6. [ ] Arenas dos chefes construídas; testar `/threeeras boss criar soberba`.
7. [ ] Um jogador Bedrock entra pelo Geyser e testa `/threeeras ajuda` + `item identificar`.
8. [ ] Testar entrada inicial, `/threeeras diario` e ciclo de quest completo (ver `docs/LORE_IMPLEMENTATION.md` e `TESTES_PONTA_A_PONTA.md`).
9. [ ] `git` atualizado; backups de `plugins/ThreeEras/*.yml` e dos mundos.

## 8. Notas de manutenção

- **Dados dos jogadores**: `plugins/ThreeEras/dados.yml` — gravado de forma assíncrona a cada 5 minutos (configurável), ao sair e no desligamento.
- **Logs**: mensagens `[ThreeEras]` no console; avisos de YAML inválido apareem na carga.
- **Atualização**: substitua o JAR e reinicie; os YAMLs existentes são preservados (novos campos ficam com padrão).
