# THREEERAS — IMPLEMENTAÇÃO COMPLETA DA CAMPANHA NARRATIVA DAS TRÊS ERAS

## 1. Sua função e objetivo

Você atuará como desenvolvedor sênior de plugins Minecraft em Java, designer de sistemas narrativos ramificados e responsável pela integração da campanha ao projeto ThreeEras existente.

Sua tarefa é desenvolver a estrutura narrativa jogável da campanha principal de ThreeEras, composta por:

- 3 rotas iniciais na Era Medieval;
- 18 trajetórias distintas na Era Apocalíptica, sendo 6 para cada rota medieval;
- 3 finais principais compartilhados na Era Futurista;
- 1 final secreto, no qual o jogador não se torna apóstolo de nenhuma das três Entidades Concretas;
- um sistema híbrido de progressão narrativa, combinando escolhas explícitas, ações do jogador, resultados das missões e condições de desbloqueio;
- suporte para futuras atualizações, que poderão expandir as Entidades, as trajetórias, as dimensões, os personagens e as missões.

Não implemente somente textos de diálogo ou missões isoladas. O resultado precisa constituir uma campanha integrada, com progressão persistente, consequências, condições de desbloqueio e transições reais entre as três eras.

**Atenção:** não comece escrevendo código imediatamente. Primeiro inspecione o repositório, os documentos de cânone, os sistemas de missões e rotas já existentes e a arquitetura atual. Apresente um plano técnico curto, identificando o que será reutilizado, o que precisará ser criado e quais arquivos serão modificados. Depois, prossiga com a implementação, sem substituir sistemas existentes desnecessariamente.

---

## 2. Preservação obrigatória do cânone

Antes de implementar a campanha, leia os documentos de lore e cânone existentes, especialmente, se presentes:

- `docs/CANON_AND_PROPOSALS.md`
- `docs/LORE_MASTER.md`
- `docs/LORE_IMPLEMENTATION.md`
- `docs/THREE_ERAS_TIMELINE.md`
- `docs/KINGDOM_OF_ROOTS.md`
- `docs/SEVEN_SINS.md`
- `docs/HOMUNCULI.md`
- `docs/ANORMAL_CONNECTION.md`

Inspecione também os sistemas de quests, rotas, NPCs, eventos, itens, estruturas, eras, jogadores, persistência e comandos administrativos.

Os nomes dos documentos acima são referências ao projeto conhecido; confirme quais realmente existem antes de utilizá-los.

### Cânone obrigatório

1. ThreeEras possui três eras históricas: Medieval, Apocalíptica e Futurista. Elas pertencem à mesma continuidade histórica. Não as transforme automaticamente em dimensões independentes.

2. Na Era Medieval, existe o Reino das Raízes, envolvido no combate a monstros. O destino definitivo desse reino permanece desconhecido.

3. Na Era Apocalíptica, existem sete criaturas associadas aos Sete Pecados Capitais. Elas criam homúnculos violentos, inconscientes e cegamente leais.

4. A causa definitiva do apocalipse é desconhecida. Não a revele como consequência automática da ação de uma Entidade específica.

5. No cânone cosmológico do Requiemverse, Deus é a Causa Primeira absoluta, acima das Entidades Abstratas e Concretas. Entidades Abstratas são princípios fundamentais; Entidades Concretas manifestam parcialmente a autoridade de uma Fonte Abstrata. Heaven e Hell são ordens distintas, não Fontes.

6. O contexto de ThreeEras ocorre depois da dissociação entre Tempo e Destino. Tempo cria ramificações e novas raízes temporais; Destino retornou à sua função original. Ambos continuam intrinsecamente relacionados, embora separados.

7. ANORMAL poderá aparecer como referência misteriosa, por exemplo, em uma inscrição ou registro impossível. Não estabeleça que ANORMAL causou o apocalipse, criou os Sete Pecados ou provocou a dissociação entre Tempo e Destino.

8. Não determine a origem, a natureza completa, os objetivos finais ou o destino das Entidades Concretas além do que está definido neste documento.

9. Diferencie sempre o cânone confirmado das novas propostas narrativas. Documente qualquer nova interpretação como proposta até que seja validada.

10. Não transforme os finais da campanha atual em conclusões cosmológicas definitivas. O objetivo é permitir que atualizações futuras expandam a narrativa.

Se houver contradição entre uma ideia deste prompt e um fato já estabelecido nos documentos do projeto, preserve o cânone e documente a divergência antes de decidir como proceder.

---

## 3. Arquitetura geral da campanha

A campanha possui a seguinte estrutura:

**Era Medieval → Era Apocalíptica → Era Futurista.**

Na Era Medieval, o jogador escolhe ou conquista acesso a uma das três rotas de origem:

- M1 — O Legado das Raízes;
- M2 — A Guerra dos Condenados;
- M3 — Os Segredos Enterrados.

Cada rota medieval libera um conjunto de seis trajetórias apocalípticas.

A rota M1 permite A01–A06.

A rota M2 permite A07–A12.

A rota M3 permite A13–A18.

Cada trajetória deve ser uma sequência de missões interligadas, e não apenas uma variante de diálogo. As seis trajetórias de cada grupo devem apresentar objetivos, conflitos, NPCs, escolhas e consequências diferentes.

A Era Futurista reúne todos os jogadores elegíveis para os quatro finais. A rota anterior influencia os indícios descobertos, os recursos disponíveis, os personagens encontrados e os requisitos narrativos, mas não determina automaticamente o final.

O sistema deverá suportar:

- escolhas explícitas em diálogos ou interações;
- resultados das missões;
- decisões tomadas em momentos anteriores;
- qualidade dos objetivos cumpridos;
- evidências encontradas;
- personagens salvos, perdidos ou convencidos;
- relações entre facções;
- acesso a áreas e estruturas;
- condições especiais para desbloquear o final secreto.

Não utilize uma única variável para representar toda a história. Preserve as decisões relevantes de maneira individual e persistente.

---

# PARTE I — ERA MEDIEVAL

## 4. Rota M1 — O Legado das Raízes

### Premissa

O jogador entra em contato com vestígios da história do Reino das Raízes e precisa decidir o que merece ser preservado diante da ameaça dos monstros.

O conflito central é a relação entre memória, sobrevivência e responsabilidade. Preservar conhecimento pode exigir recursos necessários à população; proteger pessoas pode significar perder documentos irreparáveis.

Não revele o destino definitivo do Reino das Raízes.

### Missão M1.1 — Vestígios de um Reino

O jogador recebe uma missão de um NPC ligado à preservação da memória local.

Objetivos:

- encontrar vestígios do passado;
- explorar uma estrutura antiga;
- recolher registros, objetos ou testemunhos;
- retornar com informações verificáveis.

Inclua exploração, pelo menos um perigo real e uma descoberta narrativa.

### Missão M1.2 — O Arquivo em Perigo

Uma coleção de registros encontra-se ameaçada por monstros ou pela deterioração de uma estrutura.

O jogador deverá decidir como priorizar a operação:

- preservar os documentos;
- salvar os indivíduos ameaçados;
- tentar salvar ambos, enfrentando uma missão mais exigente.

O resultado precisa alterar os recursos, os NPCs disponíveis ou as evidências que serão acessíveis depois.

### Missão M1.3 — O Juramento das Raízes

Um personagem apresenta um juramento antigo cuja validade e importância são discutidas.

O jogador poderá:

- cumprir o juramento;
- reinterpretá-lo à luz das circunstâncias;
- recusá-lo em favor de uma necessidade imediata.

Não determine antecipadamente que uma das escolhas é moralmente correta.

### Missão M1.4 — O Preço da Preservação

A rota exige que o jogador utilize os recursos obtidos anteriormente.

Os recursos podem ser direcionados à recuperação dos registros, à proteção dos habitantes ou à preparação contra novas ameaças.

Faça as escolhas anteriores influenciarem as alternativas disponíveis. Não permita que todos os recursos sejam recuperados automaticamente.

### Missão M1.5 — O Último Registro

O jogador precisa recuperar ou autenticar uma evidência histórica importante.

A missão deve diferenciar um registro encontrado, um registro comprovado e uma interpretação feita a partir dele.

A evidência poderá ser incompleta ou contraditória. Não use a descoberta para explicar definitivamente o apocalipse ou o destino do Reino das Raízes.

### Missão M1.6 — O Legado que Permanece

O jogador precisa decidir o que será transmitido à geração seguinte.

Resultados possíveis:

- uma tradição documentada;
- uma comunidade protegida;
- um conjunto de registros incompletos;
- uma herança disputada por diferentes grupos.

A rota termina com uma consequência histórica persistente e uma transição narrativa para a Era Apocalíptica.

---

## 5. Rota M2 — A Guerra dos Condenados

### Premissa

O jogador é envolvido em uma guerra pela sobrevivência. A prioridade não é descobrir toda a história, mas decidir como enfrentar ameaças, organizar recursos e lidar com aliados de interesses conflitantes.

A rota deve destacar estratégia, sacrifício, liderança e as consequências de vencer batalhas sem necessariamente vencer a guerra.

### Missão M2.1 — A Primeira Linha

Uma região sofre ataques de monstros.

Objetivos:

- investigar a ameaça;
- preparar uma posição defensiva;
- reunir recursos;
- defender NPCs ou estruturas durante um ataque.

O resultado deve depender da preparação e das ações realizadas, não apenas de derrotar uma quantidade fixa de inimigos.

### Missão M2.2 — Civis ou Posição

Uma posição defensiva corre o risco de cair enquanto uma população precisa ser evacuada.

O jogador poderá priorizar a defesa, a evacuação ou uma tentativa de realizar ambas.

As consequências incluem mudanças no estado das estruturas, na população sobrevivente e nos recursos militares.

### Missão M2.3 — Os Sem-Bandeira

Um grupo independente oferece ajuda, mas exige condições.

O jogador poderá negociar, impor exigências ou aceitar uma cooperação temporária.

As relações devem persistir. Uma aliança não pode ser considerada estável apenas porque o jogador selecionou uma opção de diálogo.

### Missão M2.4 — A Marcha dos Condenados

O jogador precisa atravessar uma região hostil para alcançar um objetivo estratégico.

Disponibilize uma rota curta e perigosa e uma rota longa que exija mais preparação.

Considere recursos, suprimentos, ameaças e objetivos opcionais. O fracasso parcial deve gerar consequências sem necessariamente encerrar a campanha.

### Missão M2.5 — O Preço da Vitória

Após uma operação importante, o jogador deverá decidir como utilizar os recursos restantes.

Alternativas:

- reconstruir posições e infraestrutura;
- investigar o que ocorreu;
- proteger os sobreviventes e cobrar responsabilidades.

Cada decisão deve fornecer vantagens diferentes na Era Apocalíptica.

### Missão M2.6 — O Exército que Restou

O jogador precisa decidir o destino da força militar sobrevivente.

Possibilidades:

- reorganizar uma instituição defensiva;
- dispersar os combatentes para proteger comunidades;
- transformar o grupo em uma rede independente de proteção.

A escolha determina quais NPCs, serviços, recursos e relações estarão disponíveis posteriormente.

Não declare que a guerra explica a origem definitiva do apocalipse.

---

## 6. Rota M3 — Os Segredos Enterrados

### Premissa

O jogador investiga ruínas, arquivos e testemunhos contraditórios. O conflito central é a busca pela verdade quando as evidências são incompletas e divulgar uma descoberta pode colocar pessoas em perigo.

A rota deve privilegiar investigação, exploração e avaliação de evidências, sem transformar qualquer hipótese em verdade absoluta.

### Missão M3.1 — A Ruína Sem Nome

O jogador recebe informações sobre uma estrutura antiga.

Objetivos:

- localizar a estrutura;
- encontrar pistas;
- registrar símbolos, objetos ou testemunhos;
- retornar com material suficiente para iniciar uma investigação.

Não atribua automaticamente a estrutura a uma das Entidades Concretas.

### Missão M3.2 — Arquivos Contraditórios

Duas ou mais fontes apresentam versões diferentes de um acontecimento.

O jogador deverá comparar os relatos, encontrar evidências adicionais e decidir quais conclusões são sustentadas pelos fatos disponíveis.

Não resolva as contradições simplesmente declarando que uma das fontes mente.

### Missão M3.3 — O Testemunho Impossível

Um personagem relata um acontecimento difícil de conciliar com os demais registros.

O jogador poderá divulgar o testemunho, proteger sua fonte ou adiar a publicação até reunir evidências adicionais.

A decisão afeta relações, disponibilidade de informações e confiança entre NPCs.

### Missão M3.4 — O Mapa Incompleto

O jogador encontra referências a locais ainda não explorados.

Deverá escolher entre seguir uma pista promissora ou investigar alternativas menos conhecidas.

O sistema precisa registrar quais pistas foram verificadas e quais permanecem especulativas.

### Missão M3.5 — Os Observadores

O jogador descobre sinais de que alguém ou alguma coisa observava acontecimentos anteriores.

A missão consiste em identificar padrões, comparar registros e distinguir observação comprovada de interpretação.

Não confirme automaticamente a identidade, os poderes ou as intenções dos observadores.

### Missão M3.6 — A Verdade Inacabada

O jogador reúne as evidências disponíveis e precisa decidir como organizar o conhecimento obtido.

Possibilidades:

- adotar a hipótese mais bem sustentada;
- preservar hipóteses concorrentes;
- manter a investigação aberta.

A decisão influencia o acesso a informações futuras, sem revelar a causa definitiva do apocalipse.

---

# PARTE II — ERA APOCALÍPTICA

## 7. Regras gerais das 18 trajetórias

Cada trajetória deverá possuir:

1. Um identificador permanente, A01–A18.
2. Um título e uma premissa narrativa.
3. Uma condição de desbloqueio.
4. Uma missão de entrada que conecte a história medieval ao novo cenário.
5. Pelo menos três missões narrativamente distintas, com uma sequência principal bem definida.
6. Uma decisão explícita que altere a trajetória.
7. Uma consequência baseada em desempenho ou ações anteriores.
8. Pelo menos um objetivo opcional significativo.
9. NPCs, locais, estruturas ou acontecimentos associados.
10. Estados persistentes.
11. Condições de conclusão.
12. Uma transição para a Era Futurista.
13. Um gancho que possa ser utilizado em atualizações futuras.

A quantidade de missões pode aumentar quando a complexidade narrativa justificar, mas não reduza as trajetórias a tarefas repetitivas de coleta ou combate.

Cada trajetória deve ter início, desenvolvimento, clímax e consequência. Não é necessário que cada uma tenha um final completamente isolado: todas fazem parte da mesma continuidade histórica.

Os Sete Pecados e os homúnculos podem aparecer quando houver justificativa narrativa, respeitando o cânone existente. Não invente uma correspondência exclusiva entre cada trajetória e um pecado sem registrar isso como nova proposta.

## 8. Trajetória A01 — As Últimas Sementes

**Origem:** M1 — O Legado das Raízes.

**Tema:** preservação e sobrevivência.

O jogador encontra uma comunidade tentando preservar recursos raros enquanto enfrenta uma ameaça crescente.

Sequência:

1. Descobrir a comunidade e investigar a escassez.
2. Localizar sementes, registros ou materiais associados à herança medieval.
3. Escolher entre utilizar os recursos imediatamente ou preservá-los para o futuro.
4. Realizar uma missão final que determine o que será salvo.

Ações e resultados:

- preservar recursos aumenta as possibilidades de reconstrução;
- utilizar recursos imediatamente ajuda a comunidade, mas reduz reservas;
- objetivos opcionais podem permitir um resultado intermediário.

A conclusão deverá afetar os recursos e as opções de reconstrução disponíveis na Era Futurista.

Não transforme as sementes em prova da sobrevivência definitiva do Reino das Raízes.

## 9. Trajetória A02 — Os Herdeiros do Juramento

**Origem:** M1.

**Tema:** promessas, consequências e responsabilidade.

Um juramento medieval continua influenciando indivíduos ou grupos no presente apocalíptico.

Sequência:

1. Descobrir quem preservou o juramento.
2. Investigar sua origem e verificar as obrigações envolvidas.
3. Decidir se o juramento ainda deve ser cumprido.
4. Enfrentar as consequências da decisão.

O jogador pode cumprir a promessa, reinterpretá-la ou abandoná-la.

Ações anteriores determinam quais personagens confiam no jogador e quais alternativas são viáveis.

O final da trajetória deve preservar a tensão entre fidelidade e responsabilidade, sem declarar que qualquer promessa antiga é necessariamente sagrada ou inútil.

## 10. Trajetória A03 — O Reino Fragmentado

**Origem:** M1.

**Tema:** unidade, autonomia e legitimidade.

Comunidades sobreviventes discordam sobre como organizar sua defesa e seu futuro.

Sequência:

1. Encontrar comunidades isoladas.
2. Identificar suas necessidades e conflitos.
3. Mediar disputas ou apoiar uma liderança.
4. Concluir uma operação que teste a estabilidade dos acordos.

Possibilidades:

- criar uma coalizão;
- apoiar uma autoridade central;
- preservar a autonomia de cada comunidade.

As escolhas devem alterar relações entre facções, serviços disponíveis e apoio recebido em missões posteriores.

Não estabeleça que uma das comunidades representa automaticamente o destino do antigo Reino das Raízes.

## 11. Trajetória A04 — As Relíquias Perdidas

**Origem:** M1.

**Tema:** propriedade, custódia e significado.

O jogador descobre objetos antigos procurados por diferentes grupos.

Sequência:

1. Investigar pistas sobre a localização dos objetos.
2. Recuperar uma relíquia em uma estrutura perigosa.
3. Verificar sua autenticidade e relevância.
4. Decidir quem será responsável por sua custódia.

Possibilidades:

- entregar o objeto a uma facção;
- mantê-lo sob custódia independente;
- priorizar a investigação antes de decidir.

A decisão deverá modificar o acesso a NPCs, informações e recursos.

Não conceda poderes cósmicos às relíquias sem fundamento no cânone.

## 12. Trajetória A05 — O Peso da Memória

**Origem:** M1.

**Tema:** história oficial, testemunhos e memória coletiva.

O jogador descobre que diferentes grupos preservaram versões distintas de acontecimentos históricos.

Sequência:

1. Recolher testemunhos.
2. Comparar documentos e memórias.
3. Verificar contradições.
4. Decidir como os relatos serão preservados e divulgados.

Possibilidades:

- organizar uma versão consolidada;
- preservar relatos divergentes;
- divulgar o material completo, incluindo suas incertezas.

A escolha afeta a confiança entre grupos e a qualidade do arquivo histórico.

Não faça uma versão oficial se tornar verdadeira simplesmente porque recebeu mais apoio político.

## 13. Trajetória A06 — A Raiz que Permaneceu

**Origem:** M1.

**Tema:** legado, descoberta e incerteza.

O jogador encontra vestígios associados ao Reino das Raízes que não foram explicados pelos registros conhecidos.

Sequência:

1. Investigar um local protegido ou esquecido.
2. Encontrar sinais de atividade anterior.
3. Decidir entre proteger o local, estudá-lo ou abandoná-lo para cumprir uma necessidade urgente.
4. Registrar as consequências da decisão.

A trajetória deve criar uma oportunidade de investigação futura sem determinar o destino do reino.

Uma descoberta pode ampliar o mistério em vez de resolvê-lo.

## 14. Trajetória A07 — A Última Fortaleza

**Origem:** M2 — A Guerra dos Condenados.

**Tema:** defesa, evacuação e sacrifício.

Uma fortaleza precisa enfrentar uma ameaça enquanto os recursos se esgotam.

Sequência:

1. Avaliar as defesas.
2. Obter materiais e preparar posições.
3. Escolher entre reforçar a fortaleza, evacuar civis ou dividir recursos.
4. Defender a posição ou garantir uma retirada organizada.

O resultado depende da preparação, dos recursos e dos objetivos cumpridos.

Não permita que a derrota em combate elimine automaticamente todos os NPCs ou bloqueie a campanha. Resultados parciais devem existir.

## 15. Trajetória A08 — Os Sem-Bandeira

**Origem:** M2.

**Tema:** confiança, negociação e alianças instáveis.

O jogador encontra uma força independente disposta a cooperar mediante condições.

Sequência:

1. Estabelecer contato.
2. Investigar as motivações do grupo.
3. Negociar os termos de cooperação.
4. Cumprir ou romper as obrigações assumidas.

Possibilidades:

- estabelecer uma aliança duradoura;
- aceitar uma colaboração limitada;
- impor condições que provoquem resistência.

As decisões devem afetar a disponibilidade de combatentes, recursos e informações.

Não transforme o grupo em aliado permanente sem considerar o cumprimento dos acordos.

## 16. Trajetória A09 — A Marcha dos Condenados

**Origem:** M2.

**Tema:** estratégia, preparação e custo da travessia.

O jogador precisa atravessar uma região perigosa para alcançar um objetivo estratégico.

Sequência:

1. Reconhecer a região.
2. Escolher uma rota.
3. Administrar suprimentos e riscos.
4. Cumprir o objetivo final e registrar as perdas ou vantagens obtidas.

Uma rota curta pode oferecer maior risco. Uma rota longa pode exigir mais recursos e preparação.

O resultado deve depender das escolhas e do desempenho. Não utilize penalidades aleatórias impossíveis de antecipar como único fator de decisão.

## 17. Trajetória A10 — O Preço da Vitória

**Origem:** M2.

**Tema:** o que fazer depois de vencer.

Após uma operação importante, o jogador precisa lidar com danos, escassez e conflitos políticos.

Sequência:

1. Avaliar o estado das comunidades.
2. Identificar os recursos restantes.
3. Escolher entre reconstrução, investigação ou responsabilização.
4. Executar a prioridade escolhida.

Cada alternativa deverá abrir benefícios diferentes:

- reconstrução: infraestrutura e serviços;
- investigação: evidências e informações;
- responsabilização: mudanças nas relações políticas.

Nenhuma alternativa deve ser tratada como uma vitória universal. Cada decisão resolve problemas e cria novas limitações.

## 18. Trajetória A11 — A Guerra Silenciosa

**Origem:** M2.

**Tema:** informação, vigilância e intervenção.

O jogador encontra indícios de operações que não podem ser compreendidas apenas pelo confronto direto.

Sequência:

1. Recolher informações.
2. Observar uma operação ou investigar uma estrutura.
3. Escolher entre infiltrar-se, intervir ou continuar a observação.
4. Utilizar as informações para tomar uma decisão estratégica.

Alarmes, provas encontradas e objetivos opcionais determinam a qualidade das informações.

A investigação poderá produzir uma pista sobre acontecimentos aparentemente conectados, mas não deve confirmar que uma Entidade provocou esses eventos.

## 19. Trajetória A12 — O Exército que Restou

**Origem:** M2.

**Tema:** identidade, disciplina e transformação.

Os sobreviventes de uma força militar precisam decidir o que se tornarão.

Sequência:

1. Encontrar os sobreviventes.
2. Avaliar seus recursos e sua confiança mútua.
3. Decidir como reorganizar o grupo.
4. Cumprir uma operação que teste a decisão.

Possibilidades:

- reconstruir uma instituição militar;
- dispersar os combatentes para proteger comunidades;
- formar uma rede independente de proteção.

O resultado modifica os aliados e os serviços disponíveis futuramente.

Não transforme uma força sobrevivente em uma organização onipotente.

## 20. Trajetória A13 — Arquivos do Fim

**Origem:** M3 — Os Segredos Enterrados.

**Tema:** conhecimento, preservação e controle da informação.

O jogador encontra um arquivo cujo conteúdo pode ser perdido ou apropriado por uma facção.

Sequência:

1. Localizar o arquivo.
2. Restaurar ou recuperar documentos.
3. Verificar a consistência das informações.
4. Decidir como o conhecimento será preservado.

Possibilidades:

- manter um arquivo central;
- distribuir cópias;
- priorizar a recuperação de documentos essenciais.

A decisão afeta o acesso futuro a informações e o risco de perda ou manipulação.

Não transforme o arquivo em uma explicação definitiva para a cosmologia do Requiemverse.

## 21. Trajetória A14 — O Testemunho Impossível

**Origem:** M3.

**Tema:** credibilidade, proteção e incerteza.

O jogador encontra um testemunho difícil de conciliar com as evidências conhecidas.

Sequência:

1. Localizar a fonte.
2. Comparar o relato com registros independentes.
3. Decidir se deve divulgar, proteger ou investigar mais.
4. Resolver as consequências da decisão.

A qualidade das evidências deve depender das ações do jogador.

O relato pode continuar sem explicação definitiva. Não o declare verdadeiro ou falso apenas por parecer sobrenatural.

Essa trajetória pode produzir uma pista ligada à causalidade, mas a ligação com o Leviatã deverá permanecer indireta e opcional.

## 22. Trajetória A15 — As Ruínas que Respondem

**Origem:** M3.

**Tema:** exploração, observação e limites do conhecimento.

O jogador encontra uma estrutura que apresenta fenômenos inexplicáveis quando explorada.

Sequência:

1. Preparar a exploração.
2. Registrar as condições iniciais.
3. Investigar os fenômenos sem destruir evidências.
4. Decidir entre interromper a investigação, continuar com precauções ou recolher dados limitados.

As escolhas afetam o conhecimento obtido, os riscos e a possibilidade de retornar à estrutura.

Não explique os fenômenos por completo. Eles podem fornecer indícios de uma separação espacial incomum, sem confirmar que o Samurai Que Corta o Mundo é sua causa.

## 23. Trajetória A16 — O Mapa Incompleto

**Origem:** M3.

**Tema:** caminhos, exploração e possibilidades desconhecidas.

O jogador encontra um mapa que apresenta rotas para locais ainda não compreendidos.

Sequência:

1. Verificar a autenticidade do mapa.
2. Identificar os locais acessíveis.
3. Escolher entre seguir a pista principal ou explorar alternativas.
4. Registrar as áreas descobertas e as lacunas restantes.

As pistas devem ser verificáveis. Não permita que o mapa conceda acesso automático a dimensões ou locais que ainda não existem no projeto.

A trajetória poderá estabelecer um primeiro indício de conexões entre lugares aparentemente separados.

## 24. Trajetória A17 — Os Observadores

**Origem:** M3.

**Tema:** observação, autoria e interpretação.

O jogador encontra registros que sugerem a observação de acontecimentos por uma presença desconhecida.

Sequência:

1. Localizar os registros.
2. Identificar padrões recorrentes.
3. Comparar fontes independentes.
4. Decidir como preservar as evidências e conduzir a investigação.

O jogador pode divulgar os resultados, proteger os documentos ou manter a investigação em andamento.

Não confirme a identidade dos observadores, sua intenção ou sua relação com as Entidades Concretas.

## 25. Trajetória A18 — A Verdade Inacabada

**Origem:** M3.

**Tema:** conclusão provisória e limites das evidências.

O jogador reúne informações de sua trajetória e precisa decidir como organizar as hipóteses disponíveis.

Sequência:

1. Recuperar evidências anteriores.
2. Comparar fontes e identificar lacunas.
3. Escolher entre uma hipótese sustentada, a preservação de hipóteses concorrentes ou a continuidade da investigação.
4. Preparar um registro que poderá ser consultado na Era Futurista.

O resultado deve depender da qualidade, da diversidade e da verificação das evidências.

Essa trajetória pode estabelecer uma conexão temática com a causalidade, mas não deve provar que o Leviatã causou o apocalipse ou qualquer outro evento.

---

# PARTE III — ERA FUTURISTA

## 26. A função da Era Futurista

A Era Futurista reúne as consequências das rotas medievais e das trajetórias apocalípticas.

O jogador passa a investigar fenômenos que ultrapassam a compreensão imediata do mundo. As três Entidades Concretas não precisam aparecer fisicamente. A campanha pode apresentar sinais, efeitos, vestígios, registros e acontecimentos associados a elas.

A progressão futurista deverá considerar:

- a rota medieval;
- as trajetórias concluídas;
- as escolhas importantes;
- a qualidade das evidências;
- os NPCs e aliados preservados;
- os locais descobertos;
- as pistas especiais coletadas;
- as decisões sobre divulgar, preservar ou ocultar informações.

Nenhum dos três finais principais deve explicar completamente a Entidade correspondente.

O final representa o primeiro contato significativo e a possibilidade de o jogador tornar-se apóstolo, não o encerramento definitivo de sua história.

## 27. Final F01 — O Chamado do Leviatã

### Entidade

**Leviatã:** uma baleia azul que nada pelo universo sem nunca ser vista. Sua natureza é deliberadamente causal.

Não transforme essa descrição em uma explicação completa de seus poderes. Não estabeleça que o Leviatã controla todos os acontecimentos, conhece todos os futuros ou causou o apocalipse.

### Desenvolvimento

1. O jogador encontra indícios de acontecimentos cuja relação não é imediatamente compreensível.
2. Evidências obtidas em missões anteriores revelam conexões entre eventos aparentemente independentes.
3. Uma investigação futurista conduz o jogador a um fenômeno que sugere uma presença sem revelar diretamente a criatura.
4. O jogador recebe a oportunidade de aceitar ou recusar um vínculo com o Leviatã.
5. Caso aceite, torna-se seu apóstolo.

O contato deverá ser indireto. Não mostre necessariamente uma baleia azul, nem confirme que o jogador compreendeu a natureza da Entidade.

### Requisitos

Utilize evidências e resultados obtidos na campanha. As trajetórias A02, A05, A10, A11, A14 e A18 podem fornecer afinidades narrativas especiais com esse final, mas não devem ser requisitos exclusivos ou obrigatórios se isso impedir jogadores de outras rotas de alcançá-lo.

A elegibilidade deverá ser configurável.

### Gancho para atualizações

Termine com uma consequência cuja relação causal permaneça desconhecida. Uma evidência pode surgir antes de sua aparente causa, ou dois acontecimentos distantes podem apresentar um padrão inexplicável.

Não explique a anomalia. Deixe espaço para uma futura missão.

## 28. Final F02 — Os Mil e Um Caminhos

### Entidade

**Árvore dos Mil e Um Ramos:** uma árvore que conecta diferentes dimensões, com cada ramo possuindo uma dimensão própria aparentemente sem fim.

Não represente todos os ramos como dimensões que precisam ser criadas na versão atual. A estrutura deve suportar expansões.

### Desenvolvimento

1. O jogador reúne evidências de caminhos, estruturas ou fenômenos que não parecem pertencer a uma única região.
2. Descobre uma conexão que não pode ser explicada apenas por geografia convencional.
3. A investigação futurista conduz a um ramo ou vestígio da Árvore.
4. O jogador encontra uma oportunidade de aceitar ou recusar o vínculo.
5. Caso aceite, torna-se apóstolo da Árvore dos Mil e Um Ramos.

A Árvore não precisa aparecer integralmente. Um ramo, uma passagem ou um efeito pode bastar para estabelecer o contato.

### Requisitos

Utilize descobertas e resultados da campanha. As trajetórias A01, A03, A04, A06, A13 e A16 podem fornecer afinidades especiais, mas não devem monopolizar o acesso ao final.

O sistema deverá permitir adicionar novas dimensões em atualizações sem reescrever a campanha principal.

### Gancho para atualizações

O jogador descobre um ramo que conduz a uma dimensão ainda não implementada ou que não será explorada na campanha atual.

Registre a descoberta como um gancho narrativo persistente. Não gere uma dimensão infinita real nem tente carregar conteúdo ilimitado.

## 29. Final F03 — O Corte Impossível

### Entidade

**Samurai Que Corta o Mundo:** percorre os universos procurando algo que não possa cortar. Seu conceito está ligado à autoridade sobre o espaço e suas separações.

Não o reduza a um espadachim comum nem lhe conceda automaticamente poder absoluto sobre toda a existência. A natureza exata de sua autoridade permanece aberta.

### Desenvolvimento

1. O jogador reúne evidências de separações ou travessias que não podem ser explicadas pelos meios convencionais.
2. Investiga um fenômeno que parece atravessar uma fronteira espacial.
3. Encontra vestígios de um corte impossível, sem necessariamente ver quem o realizou.
4. Recebe a oportunidade de aceitar ou recusar o vínculo.
5. Caso aceite, torna-se apóstolo do Samurai Que Corta o Mundo.

O contato deve ser indireto e preservar o mistério da Entidade.

### Requisitos

As trajetórias A07, A08, A09, A12, A15 e A17 podem fornecer afinidades especiais. Entretanto, jogadores de outras trajetórias devem ter meios de alcançar esse final por meio de descobertas e missões opcionais.

### Gancho para atualizações

O jogador encontra uma separação espacial cujo limite não pode ser compreendido ou atravessado pelos métodos conhecidos.

A campanha termina antes de revelar o que existe além dela.

Não implemente poderes irrestritos de corte dimensional ou acesso universal a mundos nesta versão.

## 30. Final F04 — Aquele Que Não Se Curvou

### Natureza

Este é o final secreto. O jogador não se torna apóstolo de nenhuma das três Entidades.

Não deve ser apresentado como uma opção comum e evidente no início da campanha.

### Condições narrativas

1. O jogador precisa descobrir evidências suficientes para compreender que existem três caminhos distintos.
2. Deve ter oportunidade de investigar os chamados sem aceitar imediatamente qualquer vínculo.
3. Deve cumprir os requisitos especiais definidos pela investigação e pelas decisões anteriores.
4. No momento decisivo, deve recusar os três vínculos.
5. O sistema registra que nenhuma filiação foi aceita.

A dificuldade deverá vir da necessidade de descobrir e compreender as possibilidades, não de tarefas arbitrárias ou impossíveis.

Não exija obrigatoriamente concluir todas as 18 trajetórias em uma única campanha. O jogador deve poder descobrir as condições do final secreto por meio de sua trajetória, evidências opcionais e oportunidades de investigação coerentes.

### Consequência

O jogador conclui a campanha sem se tornar apóstolo.

Não interprete a recusa como superioridade sobre as Entidades, imunidade cósmica ou posse de uma autoridade maior. A condição significa apenas que o jogador não assumiu nenhuma das três filiações.

### Gancho para atualizações

O final deverá preservar o estado de não filiação e permitir que a história continue futuramente.

Não introduza uma quarta Entidade como recompensa automática pela recusa.

---

# PARTE IV — SISTEMA DE ESCOLHAS E CONSEQUÊNCIAS

## 31. Modelo híbrido de ramificação

Implemente um sistema que combine três elementos:

### A. Escolhas explícitas

O jogador seleciona uma decisão em um diálogo, interação, menu ou missão.

A escolha precisa ser registrada e influenciar o estado da campanha.

### B. Ações e desempenho

O sistema observa resultados como:

- objetivos cumpridos;
- objetivos opcionais concluídos;
- NPCs protegidos;
- recursos preservados;
- pistas encontradas;
- alarmes provocados;
- acordos cumpridos;
- evidências verificadas.

Essas ações podem liberar variantes de missão, recompensas ou oportunidades futuras.

### C. Condições narrativas

O sistema verifica as decisões e os resultados anteriores para determinar quais missões e finais estão disponíveis.

Uma escolha não deve ser anulada silenciosamente por uma variável genérica. Quando uma decisão é importante, registre seu significado e preserve seu resultado.

## 32. Estados persistentes sugeridos

Adapte os nomes à arquitetura já existente. Não crie duplicações desnecessárias.

Considere armazenar:

- rota medieval;
- trajetórias desbloqueadas;
- trajetória ativa;
- missões concluídas;
- escolhas importantes;
- objetivos opcionais concluídos;
- evidências encontradas e verificadas;
- relações entre facções;
- NPCs e comunidades preservados;
- recursos narrativos relevantes;
- pistas associadas às três Entidades;
- requisitos do final secreto;
- final aceito;
- condição de apóstolo;
- versão dos dados narrativos.

Use identificadores estáveis para missões, escolhas, evidências, finais e Entidades. Não utilize o texto do diálogo como identificador interno.

A persistência precisa sobreviver a reinicializações do servidor e reconexões do jogador.

Se o projeto já utiliza um sistema de armazenamento, reutilize-o. Não introduza outro banco de dados sem justificar a necessidade.

## 33. Elegibilidade dos finais

Os três finais principais precisam estar acessíveis por mais de uma trajetória.

A associação entre trajetórias e Entidades indica afinidade narrativa, não uma restrição absoluta.

O sistema deve permitir que:

- jogadores da mesma rota medieval obtenham finais diferentes;
- jogadores de rotas diferentes alcancem o mesmo final;
- decisões anteriores facilitem ou dificultem oportunidades;
- a falta de um objetivo opcional não bloqueie automaticamente toda a campanha;
- a recusa dos três vínculos permaneça possível;
- futuras atualizações adicionem requisitos e missões sem reescrever todas as trajetórias.

As condições exatas de elegibilidade deverão ficar documentadas e, quando apropriado, configuráveis.

---

# PARTE V — IMPLEMENTAÇÃO TÉCNICA

## 34. Inspeção obrigatória do projeto

Antes de modificar arquivos:

1. Identifique o ponto de entrada do plugin.
2. Inspecione os gerenciadores de quests, rotas, eras, NPCs, eventos, itens, jogadores e armazenamento.
3. Descubra como as missões são registradas e iniciadas.
4. Verifique como os objetivos e recompensas são processados.
5. Verifique como o estado do jogador é salvo e carregado.
6. Identifique os comandos administrativos existentes.
7. Verifique a compatibilidade com a versão de Paper utilizada pelo projeto.
8. Identifique os arquivos de configuração e documentação já existentes.
9. Determine se as 18 trajetórias podem ser adicionadas utilizando os sistemas atuais.
10. Apresente um plano conciso de implementação antes de executar mudanças estruturais.

Não crie um segundo sistema de missões ou uma segunda camada de persistência se o projeto já possuir recursos adequados.

## 35. Estrutura narrativa e arquivos

Organize as definições da campanha de forma consistente com a arquitetura atual.

Se o projeto utilizar YAML para conteúdo, considere separar:

- rotas medievais;
- trajetórias apocalípticas;
- missões;
- escolhas e consequências;
- evidências;
- finais;
- condições de desbloqueio.

Mantenha o conteúdo narrativo separado da lógica de execução sempre que isso for compatível com o projeto.

Não coloque toda a campanha em uma única classe Java.

Não gere configurações redundantes, missões duplicadas ou identificadores conflitantes.

## 36. Regras de execução

O sistema precisa garantir que:

- missões sejam iniciadas apenas quando os requisitos forem satisfeitos;
- objetivos tenham progresso verificável;
- escolhas sejam registradas;
- missões concluídas não sejam repetidas indevidamente;
- recompensas não possam ser exploradas por reinicializações ou reconexões;
- estados de NPCs e facções sejam consistentes;
- a progressão não seja perdida ao reiniciar o servidor;
- uma missão com falha parcial não corrompa a campanha;
- erros de configuração sejam identificados com mensagens claras;
- conteúdo futuro possa ser registrado sem alterar toda a arquitetura.

A implementação deve evitar bloqueios desnecessários no servidor, particularmente ao salvar dados ou processar eventos.

Não execute operações de Bukkit/Paper em threads inadequadas. Respeite as restrições de thread da API e a arquitetura já adotada.

## 37. Multiplayer

ThreeEras é um servidor de sobrevivência com suporte a jogadores Java e Bedrock por meio de Geyser e Floodgate.

A campanha deve considerar:

- progresso individual por jogador;
- escolhas individuais quando a narrativa exigir;
- objetivos cooperativos quando apropriado;
- proteção contra duplicação de recompensas;
- persistência individual após desconexão;
- compatibilidade das interações com clientes Java e Bedrock;
- tratamento claro de missões compartilhadas, quando existentes.

Não assuma que todos os jogadores precisam escolher a mesma rota ou final.

Se o projeto ainda não possuir um modelo de campanha coletiva, não o invente implicitamente. Documente a diferença entre progresso individual e progresso compartilhado e utilize o modelo mais compatível com o sistema atual.

---

# PARTE VI — TESTES E CRITÉRIOS DE ACEITAÇÃO

## 38. Testes narrativos

Verifique as três rotas medievais e todas as 18 trajetórias.

Para cada trajetória, confirme:

- condição de desbloqueio;
- sequência de missões;
- funcionamento das escolhas;
- persistência das consequências;
- objetivos principais e opcionais;
- transição para a Era Futurista;
- ausência de dependências impossíveis;
- ausência de bloqueios permanentes provocados por pequenas falhas.

Verifique também que trajetórias da mesma rota sejam realmente distintas.

## 39. Testes dos finais

Crie testes para os quatro finais:

1. Final do Leviatã: requisitos, escolha, estado de apóstolo e gancho de expansão.
2. Final da Árvore dos Mil e Um Ramos: requisitos, escolha, estado de apóstolo e gancho de expansão.
3. Final do Samurai Que Corta o Mundo: requisitos, escolha, estado de apóstolo e gancho de expansão.
4. Final secreto: requisitos especiais, recusa dos três vínculos, ausência de filiação e persistência do estado.

Verifique ainda que:

- aceitar um final não concede simultaneamente as outras duas filiações;
- recusar um vínculo não força o jogador a aceitar outro;
- jogadores de rotas diferentes podem alcançar o mesmo final;
- o final secreto não pode ser acionado acidentalmente por ausência de dados;
- reiniciar o servidor não apaga o resultado;
- atualizações futuras podem adicionar conteúdo sem invalidar os estados existentes.

## 40. Testes técnicos

Execute os testes e a compilação reproduzível utilizando as ferramentas e versões compatíveis com o projeto.

Teste, quando possível:

- inicialização do plugin;
- carregamento das configurações;
- validação dos identificadores;
- registro das missões;
- progressão e persistência;
- reinicialização do servidor;
- reconexão de jogadores;
- execução dos comandos administrativos;
- compatibilidade com Paper;
- comportamento com clientes Java e Bedrock, quando houver ambiente real de teste.

Não declare que um teste foi aprovado sem tê-lo executado.

Se não for possível testar algo dentro do Minecraft, documente exatamente o que não foi verificado.

Não substitua o artefato de distribuição por um JAR antigo ou por um arquivo que não corresponda ao código compilado.

---

# PARTE VII — DOCUMENTAÇÃO E ENTREGA

## 41. Documentação obrigatória

Crie ou atualize a documentação de forma coerente com o projeto, incluindo:

- visão geral da campanha;
- três rotas medievais;
- descrição completa das 18 trajetórias;
- sequência de missões de cada trajetória;
- escolhas e consequências;
- condições de transição entre as eras;
- descrição dos quatro finais;
- regras de elegibilidade;
- requisitos do final secreto;
- estados persistentes;
- decisões de implementação;
- limitações e pendências;
- distinção entre cânone confirmado e novas propostas.

Atualize o documento de cânone e propostas sem converter silenciosamente novas ideias em fatos definitivos do Requiemverse.

## 42. Entrega final

Ao concluir, apresente:

1. Resumo das mudanças realizadas.
2. Lista dos arquivos criados e modificados.
3. Descrição da implementação das 3 rotas e das 18 trajetórias.
4. Descrição de como os quatro finais funcionam.
5. Explicação da persistência e das condições de desbloqueio.
6. Resultados reais da compilação e dos testes.
7. Limitações que ainda exigem teste dentro do Minecraft.
8. Pendências necessárias para futuras atualizações.
9. Instruções objetivas para testar a campanha no servidor.

Não afirme que a campanha está completamente funcional se houver missões sem execução real, NPCs sem integração, objetivos não implementados ou finais que existam apenas como texto.

## 43. Critério final de qualidade

A campanha deve parecer uma história contínua, não um conjunto de tarefas independentes.

As escolhas precisam ter consequências perceptíveis. A exploração precisa produzir descobertas significativas. O fracasso parcial deve criar novas dificuldades, e não apenas interromper o jogo.

As três Entidades Concretas devem permanecer misteriosas. Tornar-se apóstolo significa iniciar uma relação que poderá ser explorada em atualizações futuras, não receber imediatamente toda a autoridade da Entidade.

O final secreto precisa ser uma escolha narrativa legítima: o jogador compreende os três caminhos, mas não aceita nenhum.

**Implemente a campanha de maneira modular, persistente, testável e extensível, preservando o cânone do Requiemverse e reutilizando a arquitetura existente do ThreeEras sempre que possível.**
