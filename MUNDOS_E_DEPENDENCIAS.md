# Mundos, mapas e plugins externos

Pesquisa registrada em 11/10/2026. Nenhum mapa ou plugin externo foi baixado ou instalado nesta cópia; as escolhas de mundo e a hospedagem precisam ser confirmadas antes de preparar os destinos. O ThreeEras compila somente com Paper API em escopo `provided`.

## Hospedagem e crossplay

- A documentação oficial atual do Geyser lista Java 26.2 como versão suportada e descreve Geyser-Spigot para Paper. Floodgate é complemento opcional para autenticar contas Bedrock sem conta Java; não substitui Geyser.
- Em Paper, o plugin Geyser é a opção direta. A conexão Bedrock usa UDP; a porta precisa estar disponível/configurada no painel da hospedagem. O Floodgate exige configuração própria e decisões de autenticação.
- A lista oficial de provedores com instruções não confirma Minefort nesta verificação. Antes da instalação, confirme no painel Minefort se é permitido adicionar os JARs e expor/configurar a porta UDP. Sem esse acesso, crossplay permanece não validado.
- Multiverse-Core não é requisito do plugin atual; Paper pode carregar mundos já presentes na pasta do servidor. Não adicionar um gerenciador de mundos sem necessidade e compatibilidade confirmadas.
- WorldEdit/WorldGuard, LuckPerms, EssentialsX e CoreProtect não são dependências do ThreeEras. São opções operacionais do servidor, não instaladas aqui. O CoreProtect ou backup equivalente ajudaria a auditar alterações, mas não torna segura uma geração que sobrescreve blocos.
- Não foi adicionada integração de API Geyser/Floodgate ao plugin. Nenhuma função do jogo deve depender de uma tela exclusiva Bedrock; o diário usa mensagens de chat.

## Mapas candidatos encontrados

As páginas dos autores concedem uso não comercial em servidor pequeno (até 20 jogadores) com crédito ao link original. Isso parece compatível com o grupo pequeno descrito no prompt, desde que o servidor continue não comercial e o crédito seja mantido. São candidatos, não mapas aprovados/instalados.

| Era desejada | Candidato e fonte | Uso declarado pelo autor | Limites antes de usar |
|---|---|---|---|
| Medieval | [Medieval / Fantasy Castle Ruins](https://www.planetminecraft.com/project/medieval-castle-ruins/) | Página oferece downloads Java/Bedrock; autor permite servidor pequeno não comercial até 20 jogadores com crédito. | É um mundo survival com ruínas, não um reino medieval completo. A página indica 1.21+; testar cópia em Paper 26.2 e verificar chests/loot, datapacks e dimensões antes de migrar. |
| Apocalíptica | [Abandoned City — Apocalypse Survival World](https://www.planetminecraft.com/project/post-apocalyptic-abandoned-city-survival-compatible-download/) | Página oferece downloads Java/Bedrock e permite servidor pequeno não comercial até 20 jogadores com crédito. | É uma ilha/cidade nevada de survival, escolha estética que precisa aprovação. A página indica 1.21+; testar conversão em cópia e conferir datapacks/estruturas antes de produção. |
| Futurista | Nenhum candidato recomendado nesta rodada. | — | O resultado encontrado com tema futurista tinha restrição explícita contra uso em servidor sem permissão do autor; outros resultados eram antigos, pagos ou exigiam validação adicional. Melhor construir um distrito original ou pedir autorização para um mapa específico. |

As páginas e seus termos podem mudar. Releia os termos no momento de baixar; mantenha cópia do link, autoria, data, arquivo original e checksum. Não redistribua os arquivos de mapa dentro deste ZIP. Teste qualquer conversão em um mundo separado e preserve o mapa original.

## Decisões necessárias

1. Definir se o servidor usará mapas próprios ou os candidatos acima.
2. Confirmar os mundos existentes no Minefort e seus nomes reais antes de editar `eras.yml`.
3. Confirmar que a hospedagem permite Geyser/Floodgate e a porta UDP necessária.
4. Só então carregar mapas em mundos dedicados e configurar spawn; o plugin não cria nem substitui mundos automaticamente.

## Fontes técnicas primárias

- [Geyser — versões suportadas](https://geysermc.org/wiki/geyser/supported-versions/)
- [Geyser — configuração](https://geysermc.org/wiki/geyser/setup/)
- [Floodgate — visão geral](https://geysermc.org/wiki/floodgate/)
- [Geyser — provedores de hospedagem documentados](https://geysermc.org/wiki/geyser/supported-hosting-providers/)

## Atualização Minefort — verificação documental em 11/10/2026

Atualização à nota anterior: a lista de provedores da Geyser agora contém uma entrada para Minefort. Ela orienta ativar **Allow Bedrock** no painel (Connect Support) e indica as formas de conexão da rede Minefort. A própria lista ressalta que os dados de provedores são contribuições da comunidade, não verificação oficial da equipe Geyser; isso não comprova o estado deste servidor ou plano específico.

A central de ajuda da Minefort descreve importação de mundos usando SFTP/FileZilla e credenciais do painel. Também descreve o upload pelo gerenciador web; a central informa que esse gerenciador não permite baixar os arquivos, então manter backup local requer FTP/SFTP. Para importar, preservar a pasta raiz e verificar `level.dat` e `region/`; definir o mundo ativo no painel e reiniciar. Os passos exatos podem variar conforme o servidor/painel atual.

Fontes consultadas:
- Minefort, [Import world](https://help.minefort.com/en/article/import-world-ffvurt/).
- Minefort, [Accessing and uploading files](https://help.minefort.com/en/article/accessing-and-uploading-files-to-your-server-16sv2vl/).
- Minefort, [How to download my world](https://help.minefort.com/en/article/how-to-download-my-world-17yoart/).
- GeyserMC, [Supported Hosting Providers](https://geysermc.org/wiki/geyser/supported-hosting-providers/).

Não houve acesso ao painel do usuário, transferência ou validação de importação. A compatibilidade do mapa e o espaço/limite de upload permanecem pendentes de teste no próprio servidor.
