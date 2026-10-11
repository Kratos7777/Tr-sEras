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
