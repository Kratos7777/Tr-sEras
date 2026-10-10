# SEVEN_SINS — Os Sete Pecados Capitais

> Todas as identidades, fenômenos e mecânicas abaixo são **PROPOSTAS** pendentes de aprovação. A associação com os pecados capitais é tratada como **interpretação humana** (rótulo de cronistas), nunca como verdade cósmica confirmada.

## Natureza (protegida — não decidir)

- Se encarnam os pecados metafisicamente: **em aberto**.
- Sua natureza ontológica (Abstrata/Concreta/outra): **em aberto**.
- Sua origem (Rei Escarlate? dissociação? outra causa?): **em aberto** — é vedado afirmar qualquer uma sem aprovação.

## Requisitos de design (cânone de design)

Cada criatura precisa de: silhueta e anatomia próprias; comportamento reconhecível; presença ambiental distinta; fenômeno anômalo característico; relação temática com o pecado; forma específica de interação com os jogadores; mistérios não resolvidos na primeira aparição.

## As sete criaturas

### 1. Soberba — "O que não se curva"
- **Silhueta:** massa vertical assimétrica; coroa de estruturas fundidas no topo; sem rosto; membros em número contestável.
- **Fenômeno:** dentro de seu domínio, oposição direta se degenera — golpes perdem força, movimento custa.
- **Mecânica:** pilares de sustentação espalhados; enquanto há pilar de pé, a criatura é invulnerável e jogadores próximos sofrem lentidão e fraqueza.
- **Mistério:** se comanda os outros Sete ou é apenas a primeira encontrada.

### 2. Avareza — "O Cofre Vivo"
- **Silhueta:** massa orgânica recoberta por metais, objetos e escombros incorporados.
- **Fenômeno:** absorve itens e partes do ambiente; quanto mais acumula, mais se cura.
- **Mecânica:** itens dropados num raio de 10 blocos são absorvidos (curam a criatura); o grupo precisa proteger o próprio loot.
- **Mistério:** se escolhe o que absorve; para onde vai o que desaparece.

### 3. Ira — "O Coração da Ruína"
- **Silhueta:** anatomia violenta, estrutura torácica externa, centro pulsante de natureza desconhecida.
- **Fenômeno:** escalada por resistência — quanto mais é ferida, mais violenta.
- **Mecânica:** cada dano recebido aumenta sua Força (até nível IV).
- **Mistério:** a escalada é intenção ou reflexo.

### 4. Preguiça — "A Coisa que Permanece"
- **Silhueta:** imóvel, parcialmente enterrada; corpo confundido com paredes, raízes e pilares.
- **Fenômeno:** o entorno perde movimento — mecanismos param, criaturas passivas "param de existir".
- **Mecânica:** lentidão III + exaustão em área; animais da região desaparecem periodicamente.
- **Mistério:** dorme, morre ou aguarda algo específico.

### 5. Inveja — "O Reflexo Incompleto"
- **Silhueta:** superfícies espelhadas, membros duplicados, rostos que não correspondem a indivíduos conhecidos.
- **Fenômeno:** reprodução imperfeita do que observa.
- **Mecânica:** cria manifestações falsas (cópias com 20% da vida, brilhantes); **a criatura real não brilha**; atacar uma cópia a desfaz.
- **Mistério:** copia o que vê ou o que acredita ver.

### 6. Gula — "A Boca sem Fundo"
- **Silhueta:** sem limite corporal identificável; abertura colossal cercada por estruturas dentárias.
- **Fenômeno:** consumo do espaço — partes do ambiente desaparecem.
- **Mecânica:** a cada 10 segundos, blocos do terreno ao redor são consumidos; a arena muda continuamente.
- **Mistério:** para onde vai o consumido; algo já retornou.

### 7. Luxúria — "A Presença Desejada"
- **Silhueta:** aparência instável; relatos divergentes; todos descrevem o incômodo de reconhecer algo que não deveria ser familiar.
- **Fenômeno:** percepção divergente — observadores diferentes percebem manifestações incompatíveis.
- **Mecânica:** cada jogador recebe pistas diferentes de posição/distância; a cada 3 ciclos, uma fascinação puxa o jogador em direção à criatura.
- **Mistério:** qual manifestação é verdadeira — ou se existe uma.
- **Nota de interpretação:** luxúria aqui abrange desejo, fascinação, obsessão, dependência e idealização — **não** é interpretada exclusivamente como sexualidade.

## Estado de implementação

As sete criaturas estão implementadas no plugin (ver `LORE_IMPLEMENTATION.md`), cada uma com sua regra anômala isolada. Os números (vida, raios, intervalos) são parâmetros de `bosses.yml`, ajustáveis sem recompilar.
