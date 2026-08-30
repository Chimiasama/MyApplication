# Índice de Criação de Personagem — Deadlands

Levantamento do conteúdo de `docs/swade_deadlands` (livro básico do cenário, ~16884 linhas) e `docs/swade_deadlands_compendio` (~8606 linhas) relevante para a **criação de personagem** no cenário de horror/velho-oeste Deadlands: O Oeste Estranho, cruzado com os dados já cadastrados em `app/src/main/assets/*.json` sob a tag de livro `DEADLANDS`. Deadlands é um cenário majoritariamente humano (só há a ancestralidade Humano) mas com sete subtipos distintos de Antecedente Arcano próprios do cenário (Abençoado, Mascate, Cientista Louco, Xamã, Mestre do Chi — no livro básico — e Voduísmo e Bruxa — no compêndio), além do status especial "Atormentado" (morto-vivo, funciona como uma raça/template extra escolhida na criação). A varredura mostra que Complicações, Perícias, Ancestralidades e o catálogo de Equipamentos/Dispositivos Infernais já estão essencialmente completos e tagueados `DEADLANDS`; a lacuna real está nas Vantagens específicas de cada Antecedente Arcano/Atormentado (boa parte das vantagens "de grupo" de cada AB não foi cadastrada, apenas o próprio Antecedente Arcano) e em dois poderes do compêndio.

## Ancestralidades

- **Humano (única ancestralidade jogável)** — id: `anc_humano_deadlands` — [OK] — Deadlands não tem raças próprias; humanos recebem uma Vantagem grátis de estágio Novato cujos pré-requisitos sejam atendidos — linha ~765 de docs/swade_deadlands

## Antecedente Arcano (sistemas de magia)

Sete subtipos de Antecedente Arcano específicos de Deadlands (cinco no livro básico, dois no compêndio), todos já cadastrados como a própria Vantagem "Antecedente Arcano (X)":

- **Antecedente Arcano (Abençoado)** — id: `antecedente_arcano_abencoado` — [OK] — Fé (Espírito); milagres cristãos/religiosos genéricos; Choque de Retorno e regra de Pecado — linha ~1185 (resumo) / ~3653 (detalhe completo) de docs/swade_deadlands
- **Antecedente Arcano (Mascate)** — id: `antecedente_arcano_mascate` — [OK] — Conjurar (Astúcia); magia "Hoyle"/pôquer com manitus, mecânica própria "Barganhar com o Diabo" — linha ~1217 / ~4729 de docs/swade_deadlands
- **Antecedente Arcano (Cientista Louco)** — id: `antecedente_arcano_cientista_louco` — [OK] — Ciência Estranha (Astúcia); "Nova Ciência" movida a rocha fantasma, Tabela de Mau Funcionamento — linha ~1232 / ~4502 de docs/swade_deadlands
- **Antecedente Arcano (Xamã)** — id: `antecedente_arcano_xama` — [OK] — Fé (Espírito); magia dos espíritos da natureza dos povos indígenas, ligação com o Juramento dos Velhos Costumes — linha ~1246 / ~5528 de docs/swade_deadlands
- **Antecedente Arcano (Mestre do Chi)** — id: `antecedente_arcano_mestre_do_chi` — [OK] — Foco (Espírito); artes marciais místicas, poderes só afetam a si mesmo (benéficos) ou por Toque (prejudiciais) — linha ~1203 / ~5105 de docs/swade_deadlands
- **Antecedente Arcano (Vuduísmo)** — id: `antecedente_arcano_vuduismo` — [OK] — Fé (Espírito); houngans/mambos convocam loas (rada/petro) para "montar" seu corpo; exige bolsa gris-gris — linha ~1123 de docs/swade_deadlands_compendio
- **Antecedente Arcano (Bruxa)** — id: `antecedente_arcano_bruxa` — [OK] — Conjurar (Astúcia); "Magia das Trevas" tirada diretamente dos Campos de Caça, com sistema de Corrupção (ver seção Manias abaixo) — linha ~1536 de docs/swade_deadlands_compendio
- **Mago do Metal** — id: `mago_do_metal` — [OK] — não é um Antecedente Arcano novo, mas uma Vantagem híbrida exigindo Antecedente Arcano (Cientista Louco): permite Barganhar com o Diabo como um mascate para fabricar dispositivos infernais improvisados — linha ~785/832 de docs/swade_deadlands_compendio
- Nota (fora do escopo de criação de personagem): **Antecedente Arcano (Magia das Trevas)** descrito em "Regras do Xerife" (linha ~6433-6489 de docs/swade_deadlands) é uma variante de uso exclusivo de vilões/NPCs controlados pelo Mestre, não uma opção de criação de heróis — ver Fora de escopo.

## Complicações

Todas as 10 Complicações novas do Capítulo "Criando Heróis e Heroínas" já estão cadastradas em `complicacoes.json` com a tag `DEADLANDS`.

- **Doente (Menor ou Maior)** — id: `doente` — [OK] — penalidade em resistir Fadiga; Falha Crítica pode evoluir para morte com Convicção ao grupo — linha ~952 de docs/swade_deadlands
- **Imã de Problemas (Menor ou Maior)** — id: `ima_de_problemas` — [OK] — Falhas Críticas pioram sutilmente (Menor) ou o personagem é sempre o "alvo aleatório" escolhido pelo Mestre (Maior) — linha ~988
- **Juramento dos Velhos Costumes (Menor)** — id: `juramento_dos_velhos_costumes` — [OK] — rerrolagem grátis em Espírito por recusar tecnologia moderna; penalidade se usar item movido a rocha fantasma — linha ~1007
- **Maldição (Maior)** — id: `maldicao` — [OK] — o Mestre ganha um Bene adicional por causa de um mal do passado do personagem — linha ~1022
- **Olhos Mentirosos (Menor)** — id: `olhos_mentirosos` — [OK] — –1 em Intimidar/Persuadir ao mentir, inclusive blefando em Jogar — linha ~1033
- **Pele Fina (Maior)** — id: `pele_fina` — [OK] — –1 adicional em todas as ações por Ferimento; incompatível com a Vantagem Não Me Irrite! — linha ~1048
- **Servo Sombrio da Morte (Maior)** — id: `servo_sombrio_da_morte` — [OK] — +1 em dano, mas Falha Crítica em ataque acerta o aliado mais próximo — linha ~1059
- **Sono Pesado (Menor)** — id: `sono_pesado` — [OK] — penalidades para acordar/ficar acordado — linha ~1095
- **Talismã (Menor ou Maior)** — id: `talisma` — [OK] — só para Antecedentes Arcanos; penalidade em rolagens arcanas sem o item físico (cientistas loucos não são elegíveis) — linha ~1138
- **Terrores Noturnos (Maior)** — id: `terrores_noturnos` — [OK] — –1 em todas as rolagens de Espírito por pesadelos constantes — linha ~1165

## Perícias

- **Ofício (Astúcia)** — id/nome: `Ofício` — [OK] — substitui Eletrônica/Hackear; cobre tarefas de negócio (ferreiro, dono de saloon etc.) e permite levantar fundos como Performance — linha ~829 de docs/swade_deadlands
- Nota: as perícias arcanas usadas pelos Antecedentes Arcanos de Deadlands (Fé, Foco, Conjurar, Ciência Estranha) já são perícias-base do sistema e já estão tagueadas `DEADLANDS` em `pericias.json` — [OK], nenhuma entrada nova necessária.

## Vantagens

### De Antecedente / genéricas (Criando Heróis e Heroínas)
- **Humor Ácido** — id: `humor_acido` — [OK] — usa Provocar em vez de Espírito em testes de Medo — linha ~1257
- **Veterano do Oeste Estranho** — id: `veterano_do_oeste_estranho` — [OK] — começa como Experiente, mas sacar carta na "Tabela de Azar" — linha ~1274

### De Combate
- **Duelista** — id: `duelista` — [OK] — linha ~1302
- **Martelar o Cão** — id: `martelar_o_cao` — [OK] — linha ~1311
- **Martelar o Cão Aprimorado** — id: `martelar_o_cao_aprimorado` — [OK] — linha ~1340
- **Não Me Irrite!** — id: `nao_me_irrite` — [OK] — linha ~1346
- **Saque Rápido** — id: `saque_rapido` — [OK] — linha ~1356

### Profissionais
- **Agente** — id: `agente` — [OK] — linha ~1370
- **Batedor** — id: `batedor` — [OK] — linha ~1384
- **Contador de Histórias** — id: `contador_de_historias` — [OK] — linha ~1404
- **Coragem** — id: `coragem` — [OK] — linha ~1463
- **Nascido na Sela** — id: `nascido_na_sela` — [OK] — linha ~1470
- **Patrulheiro Territorial** — id: `patrulheiro_territorial` — [OK] — linha ~1485
- **Soldado (Deadlands)** — id: `soldado_deadlands` — [OK] — linha ~1504
- **Trapaceiro** — id: `trapaceiro` — [OK] — linha ~1551
- **Delegado Federal** — id: `delegado_federal` — [OK] — versão estadual do Patrulheiro Territorial — linha ~1086 de docs/swade_deadlands_compendio

### Sociais
- **Reputação** — id: `reputacao` — [OK] — linha ~1564 de docs/swade_deadlands

### Estranhas
- **Atormentado** — id: `atormentado` — [OK] — status de morto-vivo (só pode ser escolhida na criação); concede acesso às Vantagens de Atormentado — linha ~1577 (resumo) / ~4027 (regras completas) de docs/swade_deadlands
- **Determinação** — id: `determinacao` — [OK] — linha ~1598
- **Talento** — id: `talento` — [OK] — vantagem com 7 sub-opções escolhidas na criação (Bastardo, Nascido na Véspera de Todos os Santos, Nascido no Natal, Parto Invertido, Sétimo Filho, Estrela Cadente, Nascido numa Tempestade) — linha ~1605

### Lendárias
- **Condenado** — id: `condenado` — [OK] — linha ~1684
- **Determinação Verdadeira** — id: `determinacao_verdadeira` — [OK] — linha ~1709
- **Eis um Cavalo Amarelo...** — id: `eis_um_cavalo_amarelo` — [OK] — linha ~1719
- **Mão Direita do Diabo** — id: `mao_direita_do_diabo` — [OK] — linha ~1737
- **Rápido como um Raio** — id: `rapido_como_um_raio` — [OK] — linha ~1763

### De Abençoado
- **Fé Verdadeira** — id sugerido: `fe_verdadeira_abencoado` — [FALTA] — Veterano, Espírito d10+, AA (Abençoado), Fé d6+; rerrolagem grátis em Fé — linha ~3736 de docs/swade_deadlands
- **Rebanho** — id sugerido: `rebanho` — [FALTA] — Veterano, Persuadir d8+; concede 5 seguidores (estatísticas de Cidadão) — linha ~3742

### De Agente
- **Pessoa de Mil Faces** — id sugerido: `pessoa_de_mil_faces` — [FALTA] — Heroico, AA Agente, Persuadir d8+; disfarce/infiltração aprimorados — linha ~3988

### De Atormentado (livro básico — habilidades "de tronco")
- **Assustar** — id sugerido: `assustar_atormentado` — [FALTA] — força teste de Medo –2 num alvo — linha ~4332
- **Atributo Sobrenatural** — id sugerido: `atributo_sobrenatural` — [FALTA] — melhora um atributo em 2 tipos de dado, repetível por atributo — linha ~4350
- **Devorador de Almas** — id sugerido: `devorador_de_almas` — [FALTA] — cura Ferimento/Fadiga ao ferir com as mãos nuas — linha ~4361
- **Fantasma** — id sugerido: `fantasma_atormentado` — [FALTA] — forma incorpórea à vontade — linha ~4378
- **Fogo do Inferno** — id sugerido: `fogo_do_inferno` — [FALTA] — ataque de fogo em Modelo de Cone, 3d6 — linha ~4394
- **Frio da Sepultura** — id sugerido: `frio_da_sepultura` — [FALTA] — Modelo Grande de Explosão deixa alvos Vulneráveis — linha ~4404
- **Garras** — id sugerido: `garras_atormentado` — [FALTA] — For+d6 em combate — linha ~4413
- **Garras Aprimoradas** — id sugerido: `garras_aprimoradas_atormentado` — [FALTA] — For+d8, PA 2 — linha ~4419
- **Implacável (Atormentado)** — id sugerido: `implacavel_atormentado` — [FALTA] — nome colide com `implacavel` já cadastrado para o livro HORROR (mecânica diferente: Ferimento extra antes de Incapacitar); precisa de id próprio — linha ~4424
- **Infestar** — id sugerido: `infestar` — [FALTA] — controla um Bando de insetos — linha ~4437
- **Murchar** — id sugerido: `murchar` — [FALTA] — reduz Força (e Vigor com ampliação) da vítima por toque — linha ~4464
- **Olhos de Gato** — id sugerido: `olhos_de_gato` — [FALTA] — nega penalidades de penumbra/escuridão — linha ~4479
- **Olhos de Gato Aprimorados** — id sugerido: `olhos_de_gato_aprimorados` — [FALTA] — visão perfeita no escuro — linha ~4485
- **Remendar** — id sugerido: `remendar` — [FALTA] — cura natural diária consumindo carne — linha ~4491
- **Remendar Aprimorado** — id sugerido: `remendar_aprimorado` — [FALTA] — cura natural a cada hora — linha ~4498

### De Atormentado (compêndio — habilidades adicionais)
- **Aranha** — id sugerido: `aranha_atormentado` — [FALTA] — anda em superfícies verticais/invertidas — linha ~274 de docs/swade_deadlands_compendio
- **Arame Espiritual** — id: `arame_espiritual` — [OK] — linha ~298
- **Cavar** — id: `cavar` — [OK] (nota: mesmo id existe também como poder em `poderes.json`; conferir se não há conflito de namespace) — linha ~313
- **Chamado dos Mortos** — id: `chamado_dos_mortos` — [OK] — linha ~336
- **Contorcer** — id: `contorcer` — [OK] — linha ~353
- **Corcel Infernal** — id: `corcel_infernal` — [OK] — linha ~383
- **Falar com os Mortos** — id: `falar_com_os_mortos` — [OK] — linha ~419
- **Hálito da Sepultura** — id: `halito_da_sepultura` — [OK] — linha ~452
- **Hoste Profana** — id: `hoste_profana` — [OK] — linha ~474
- **Imitar** — id: `imitar` — [OK] — linha ~509
- **Mão de Defunto** — id: `mao_de_defunto` — [OK] — linha ~532
- **Máscara da Morte** — id: `mascara_da_morte` — [OK] — linha ~610
- **Pesadelos** — id: `pesadelos` — [OK] — linha ~629
- **Possessão** — id: `possessao` — [OK] — linha ~576
- **Rápido como a Morte** — id: `rapido_como_a_morte` — [OK] — linha ~593
- **Rastro de Dentes** — id: `rastro_de_dentes` — [OK] — linha ~602
- **Rastro de Dentes Aprimorado** — id: `rastro_de_dentes_aprimorado` — [OK] — linha ~675
- **Rigor Mortis** — id: `rigor_mortis` — [OK] — linha ~680
- **Silencioso como um Cadáver** — id: `silencioso_como_um_cadaver` — [OK] — linha ~697
- **Sono dos Mortos** — id: `sono_dos_mortos` — [OK] — linha ~710
- **Toque Diabólico** — id: `toque_diabolico` — [OK] — linha ~727
- **Visão Espiritual** — id: `visao_espiritual` — [OK] — linha ~740

### De Cientista Louco
- **Alquimia** — id sugerido: `alquimia` — [FALTA] — cria até 3 poções/elixires investindo Pontos de Poder — linha ~4626 de docs/swade_deadlands
- **Devorador de Minério** — id sugerido: `devorador_de_minerio` — [FALTA] — +5 Pontos de Poder por comer rocha fantasma; risco de Febre da Rocha Fantasma — linha ~4662
- **Gênio Verdadeiro** — id sugerido: `genio_verdadeiro` — [FALTA] — gasta Bene para forçar nova rolagem nas tabelas de Mau Funcionamento/Loucura — linha ~4685
- **Ligação com o Ferro** — id sugerido: `ligacao_com_o_ferro` — [FALTA] — começa com até $2.000 em dispositivos infernais, desconto de 25% — linha ~4703

### De Mascate
- **Bruxeiro** — id sugerido: `bruxeiro` — [FALTA] — ganha o poder enfeitiçar munição e pode gravar armas com runas — linha ~5010
- **Conhecedor** — id sugerido: `conhecedor` — [FALTA] — pode descartar até 3 cartas ao Barganhar com o Diabo — linha ~5048
- **Grande Apostador** — id sugerido: `grande_apostador` — [FALTA] — carta extra ao Barganhar com o Diabo — linha ~5056
- **Grande Apostador Aprimorado** — id sugerido: `grande_apostador_aprimorado` — [FALTA] — duas cartas extras — linha ~5062
- **Sangue dos Whateley** — id sugerido: `sangue_dos_whateley` — [FALTA] — sofre Fadiga/Ferimento para ganhar Pontos de Poder; –1 em Persuadir — linha ~5066

### De Mestre do Chi
- **Kung Fu Superior** — id sugerido: `kung_fu_superior` — [FALTA] — escolhe um estilo entre 6 (Bêbado, Garra de Águia, Louva-a-Deus, Macaco, Shuai Jiao, Tantui, Wing Chun); repetível — linha ~5211
- **Kung Fu Celestial** — id sugerido: `kung_fu_celestial` — [FALTA] — permite manter dois estilos simultâneos — linha ~5296

### De Patrulheiro Territorial
- **Como um Carvalho** — id sugerido: `como_um_carvalho` — [FALTA] — aliados próximos negam 2 pontos de penalidade de Medo — linha ~5498
- **Promoção de Patrulheiro** — id sugerido: `promocao_de_patrulheiro` — [FALTA] — repetível até 3x (Sargento/Tenente/Capitão), concede o Capítulo 13 — linha ~5512

### De Xamã
- **Favor do Espírito** — id sugerido: `favor_do_espirito` — [FALTA] — lança um poder escolhido sem penalidade de Ação Múltipla — linha ~5680
- **Fetiche** — id sugerido: `fetiche` — [FALTA] — rerrolagem grátis em Fé enquanto portar o fetiche — linha ~5697

### De Delegado Federal / Mago do Metal
- Nenhuma Vantagem adicional própria além da Vantagem "base" já listada acima (Delegado Federal, Mago do Metal) — [OK]

### De Voduísta
- **Esteve na Encruzilhada** — id: `esteve_na_encruzilhada` — [OK] — linha ~1272 de docs/swade_deadlands_compendio
- **Favorecido** — id: `favorecido` — [OK] — linha ~1289

### De Bruxa
- **Bruxa de Wichita** — id: `bruxa_de_wichita` — [OK] — linha ~1706
- **Familiar** — id: `familiar` — [OK] — linha ~1737

## Poderes (Novos Poderes)

### Livro básico
- **Banir** — id: `banir` (tag DEADLANDS já existe) — [CONFERIR] — a entrada cadastrada traz apenas a descrição genérica de Savage Worlds; falta a nota específica de Deadlands sobre manitus dentro de Atormentados ficarem "inertes" por uma hora — linha ~5716 de docs/swade_deadlands
- **Bugigangas** — id: `bugigangas_2` — [OK] — linha ~5740
- **Caminhada Selvagem** — id: `caminhada_selvagem` — [OK] — linha ~5771
- **Enfeitiçar Munição** — id: `enfeiticar_municao` — [OK] — só para Bruxeiro; 8 efeitos de munição diferentes — linha ~5795
- **Entorpecimento** — id: `entorpecimento` — [OK] — linha ~5847
- **Fantoche (modificador Passageiro da Mente)** — id: `fantoche_2` — [OK] — linha ~5866
- **Maldição** — id: `maldicao_3` — [OK] — linha ~5875
- **Santificar** — id: `santificar` — [OK] — linha ~5902
- **Símbolo Sagrado** — id: `simbolo_sagrado_2` — [OK] — linha ~5941

### Compêndio
- **Pane** (Mago do Metal) — id: `pane` — [OK] — linha ~942 de docs/swade_deadlands_compendio
- **Reparar** (Mago do Metal) — id: `reparar` — [OK] — linha ~979
- **Aspecto do Loa Rada** (Voduísta) — id: `aspecto_do_loa_rada` — [OK] — linha ~1314
- **Fúria do Loa Petro** (Voduísta) — id: `furia_do_loa_petro` — [OK] — linha ~1437
- **Iludir** (Bruxa) — id: `iludir` — [OK] — linha ~1797
- **Pavor** (Bruxa) — id: `pavor` — [OK] — linha ~1834
- **Transformar** (Bruxa) — id: `transformar` — [OK] — linha ~1853
- **Abrir Portal** — id sugerido: `abrir_portal` — [FALTA] — Heroico, 20 PP, disponível para todos os Antecedentes Arcanos menos Mestre do Chi; abre uma fenda entre o mundo físico e os Campos de Caça — linha ~5080
- **Detectar/Ocultar Arcano — opção "Visão Espiritual" (+5)** — [FALTA] — nova opção de poder do compêndio (só Estágio Heroico) que não aparece nos modificadores de `detectar_ocultar_arcano` tagueados DEADLANDS — linha ~5057

## Manias / Corrupção (magia negra ligada à ficha)

Deadlands não usa um sistema único de "tiques"; o conceito mais próximo que afeta diretamente a ficha do personagem é a **Corrupção das Bruxas**, complementado por duas tabelas de "efeito colateral" ligadas a magia/ciência sombria que também geram Complicações:

- **Corrupção (Antecedente Arcano Bruxa)** — [CONFERIR] — cada Falha Crítica em Conjurar dá 1 ponto de Corrupção; cada ponto força uma nova Complicação Menor (ou upgrade de Menor para Maior); se a Corrupção igualar o Espírito, a bruxa vira PNJ controlada pelo Mestre; reduzida participando de reduções de Nível de Medo ou gastando Progresso — mecânica de acompanhamento (contador), não um item de catálogo; verificar se o app tem algum campo de "pontos de corrupção" por personagem — linha ~1609 de docs/swade_deadlands_compendio
- **Tabela de Loucura (Cientistas Loucos)** — [CONFERIR] — Falha Crítica em Ciência Estranha pode conceder Complicações como Sem Noção, Delirante (Menor), Peculiaridade (Menor), Fobia, Impulsivo, Invejoso (Menor), ou uma "mania" crescente de perseguir inimigos imaginários ("Atos Diabólicos") — todas as Complicações referenciadas já existem no catálogo genérico; só a tabela de sorteio em si não é um dado cadastrável — linha ~6785 de docs/swade_deadlands
- **Tabela de Tiro pela Culatra (Mascates)** — [CONFERIR] — Falha Crítica ao Barganhar com o Diabo pode causar Loucura (reaproveita a Tabela de Loucura acima), redução permanente de Conjurar, dano, ou deixar o mascate Distraído/Atordoado/Vulnerável — linha ~6503 de docs/swade_deadlands

## Equipamento

O catálogo de equipamentos de Deadlands já está muito bem coberto em `equipamentos.json` (24 grupos, tag `DEADLANDS`), incluindo tanto os itens "de época" quanto os Dispositivos Infernais de ciência maluca. Contagem por grupo cruzada linha a linha com o livro confirma paridade completa — **[OK] em todas as categorias abaixo**:

- Roupas comuns (armadura nativa, escudos nativos, casacos etc.) — 15 itens — linha ~1886 de docs/swade_deadlands
- Comida e bebida — 10 itens — linha ~1917
- Equipamento geral (kits, ferramentas, matéria-prima) — 34 itens — linha ~1929
- Acessórios para armas e munição — 15 itens — linha ~1998
- Serviços, chapéus e bebidas — 15 itens — linha ~2027
- Transporte (montarias, bilhetes) — 7 itens — linha ~2058
- Armas corpo a corpo (época) — 14 itens, incl. modificador "Arma de Aço Fantasma" — linha ~2102
- Armas de fogo Gatling, derringers, revólveres, carabinas, rifles, espingardas — 33 itens — linha ~2113-2254
- Armas à distância diversas (arco, boleadera, arremesso) — 8 itens — linha ~2255
- Explosivos (dinamite, nitroglicerina, acessórios) — 5 itens — linha ~2270
- Veículos terrestres e aquáticos (mundanos) — 9 itens — linha ~2299
- Dispositivos Infernais (roupas/acessórios, elixires e tônicos, diversos, armas e armaduras, veículos infernais) — 42 itens — linha ~2372-2702

Nota: **Equipamento Barato / Equipamento Mixuruca** (regra de compra de item usado por metade do preço, com risco de quebrar em Falha Crítica) é uma regra econômica geral, não um item de catálogo — não requer entrada própria — linha ~1802-1863 de docs/swade_deadlands.

## Fora de escopo

- **Bestiário** ("Patifes, Vermes e Criaturas", linha ~161 do índice / conteúdo a partir de ~ linha correspondente de docs/swade_deadlands; "Patifes Famosos" e "O Devorador de Cavalos" em docs/swade_deadlands_compendio) — mortos-vivos, monstros do Oeste Macabro e vilões nomeados, usados como antagonistas.
- **Manitu / Manitu Maior** (estatísticas, linha ~5226-5310 de docs/swade_deadlands_compendio) — perfis de criatura/espírito, não uma opção de personagem.
- **Antecedente Arcano (Magia das Trevas)** para vilões (linha ~6433-6489 de docs/swade_deadlands) — variante de uso exclusivo do Mestre para PNJs, não uma opção de criação de heróis.
- **Lore não-mecânica**: "No Oeste Estranho" (introdução, história), "A Vida no Oeste Estranho", "O Julgamento", "Encontros", geografia regional completa ("O Oeste Estranho", "A Grande Bacia", "O Grande Labirinto" etc.), "Uma Breve História de Deadlands", "A História Até Agora", "Mestrando Deadlands", "Danças Espirituais" e "Enxame de Manitus" (mecânicas de campanha/região, não de ficha individual).
- **Regras de Ambientação gerais** (Duelando, Enforcamento, Debandadas — linha ~3290-3538 de docs/swade_deadlands) — regras de mesa/combate, não opções de criação de personagem.
- **Relíquias** (itens mágicos/artefatos, linha ~1931-1930+ de docs/swade_deadlands_compendio, ~30 relíquias catalogadas) — catálogo de tesouro concedido pelo Mestre conforme a narrativa, não algo comprável na criação; mantido fora do escopo por instrução explícita.
- **Regras do Xerife de bastidor**: Campo de Batalha, Rocha Fantasma (verdade macabra), Superstição, Telégrafos, Nível de Medo regional — ferramentas de mestre de jogo, não opções de ficha.
