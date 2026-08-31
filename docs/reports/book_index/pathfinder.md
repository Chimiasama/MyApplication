# Índice — Savage Pathfinder (Golarion)

Este índice cobre os dois livros de Pathfinder para Savage Worlds usados pelo SWADE Builder:
**docs/swade_pathfinder_basico** (~20.058 linhas — "Savage Pathfinder", livro básico do cenário Golarion) e
**docs/swade_pathfinder_compendio** (~21.539 linhas — "Compêndio de Golarion / Guia do Mar Interior"; o arquivo contém
o texto do livro duplicado integralmente uma vez, então as referências de linha abaixo apontam para a primeira
ocorrência). O escopo é estritamente o que afeta a **criação de personagem**: ancestralidades, vantagens,
complicações, perícias, o Antecedente Arcano/sistema de domínios divinos e o catálogo fixo de equipamento (incluindo
a moeda própria de Golarion). Ficaram de fora bestiário, lore de reinos/nações não-mecânico, gerador de aventura,
organizações (exceto pelas Vantagens de Prestígio que concedem), o plano cósmico (Grande Além) e qualquer sistema de
criação de itens mágicos/reino como território — ver "Fora de escopo" ao final. Os dados já existentes em
`app/src/main/assets/*.json` foram conferidos por nome e pela tag `"PATHFINDER"` em `livros`/`tags`.

## Ancestralidades

Savage Pathfinder define exatamente 7 ancestralidades "recomendadas" para criação de personagem (linha ~1461 de
docs/swade_pathfinder_basico: "Savage Pathfinder inclui sete ancestralidades recomendadas..."). O Compêndio
(docs/swade_pathfinder_compendio) não introduz novas ancestralidades jogáveis — seus capítulos tratam de
organizações, geografia, planos e geradores de aventura, e a única menção a uma raça de monstro (Kobolds) é uma
entrada de tabela de encontros aleatórios do bestiário (linha ~4473), fora de escopo.

- **Anão** — id: `anc_anaopathfinder` — [OK] — resistente, visão no escuro, aptidão com pedras, movimentação reduzida — linha ~1466 de docs/swade_pathfinder_basico
- **Elfo** — id: `anc_elfopathfinder` — [OK] — ágil, inteligente, magia élfica, sentidos apurados, esguio, visão na penumbra — linha ~1503 de docs/swade_pathfinder_basico
- **Gnomo** — id: `anc_gnomopathfinder` — [OK] — magia gnômica (truques), tamanho -1, resistente, sentidos apurados, visão na penumbra — linha ~1544 de docs/swade_pathfinder_basico
- **Halfling** — id: `anc_halflingpathfinder` — [OK] — ágil, pés-firmes, sentidos aguçados, Sorte, tamanho -1 — linha ~1576 de docs/swade_pathfinder_basico
- **Humano** — id: `anc_humanopathfinder` — [OK] — Adaptável (vantagem Novato grátis + d6 num atributo) — linha ~1607 de docs/swade_pathfinder_basico
- **Meio-Elfo** — id: `anc_meio_elfopathfinder` — [OK] — flexibilidade, magia élfica, visão na penumbra — linha ~1632 de docs/swade_pathfinder_basico
- **Meio-Orc** — id: `anc_meio_orcpathfinder` — [OK] — ferocidade orc, forasteiro (menor), forte, intimidante, visão no escuro — linha ~1656 de docs/swade_pathfinder_basico

Todas as 7 já existem em `ancestralidades.json` com tag `PATHFINDER` e conferem com a descrição do livro (nomes,
habilidades e granularidade batem).

## Antecedente Arcano / Sistema de Magia (Domínios)

- **Antecedente Arcano (Magia)** — id: `antecedente_arcano_magia_pf` — [OK] — magia estudada/acadêmica; perícia Conjurar (Astúcia) — linha ~2999 de docs/swade_pathfinder_basico
- **Antecedente Arcano (Milagres)** — id: `antecedente_arcano_milagres_pf` — [OK] — fé divina; perícia Fé (Espírito); exige escolher Domínio — linha ~2999 de docs/swade_pathfinder_basico
- **Tabela Divindades e Domínios** (20 divindades de Golarion x tendência/área/domínios) — [CONFERIR — sem dado estruturado equivalente] — resumo de qual divindade concede quais domínios — linha ~3661 de docs/swade_pathfinder_basico. Não há JSON de "divindades" no app (fora do escopo de mecânica pura de personagem, mas pode ser útil para uma futura tela de escolha de divindade); sugestão de id se for modelado futuramente: `divindades_pathfinder.json`.

### Domínios divinos (`pathfinder_dominios.json`)

O livro lista exatamente 15 domínios na seção "DOMÍNIOS" (linha ~3707 de docs/swade_pathfinder_basico), cada um com
uma lista de 9–10 poderes disponíveis. Comparando os 15 nomes e as listas de poderes de `pathfinder_dominios.json`
contra o texto (conferidos integralmente Civilização e Conhecimento, e por amostragem os demais 13 — todas as
contagens de poderes por domínio batem exatamente com o livro):

- **Civilização** (Comunidade, Nobreza) — [OK] — 10 poderes: barreira, conjurar aliado, disfarce, dádiva do guerreiro, elo mental, falar idioma, localizar, fantoche (marionete), proteção, proteção ambiental — linha ~3708
- **Conhecimento** — [OK] — 9 poderes: detectar/ocultar arcano, drenar pontos de poder, elo mental, leitura de objeto, leitura mental, localizar, proteção, proteção arcana, vidência — linha ~3716
- **Destruição** (Maligno, Loucura) — [OK] — 10 poderes — linha ~3725
- **Elemental** (Ar, Terra, Fogo, Água) — [OK] — 10 poderes — linha ~3734
- **Enganação** — [OK] — 10 poderes — linha ~3742
- **Força** — [OK] — 10 poderes — linha ~3751
- **Glória** (Bem, Cura) — [OK] — 10 poderes — linha ~3760
- **Guerra** — [OK] — 10 poderes — linha ~3769
- **Morte** — [OK] — 10 poderes — linha ~3777
- **Magia** (Runa) — [OK] — 10 poderes — linha ~3786
- **Natureza** (Animal, Planta, Clima) — [OK] — 11→10 poderes — linha ~3796
- **Proteção** — [OK] — 10 poderes — linha ~3804
- **Sol** — [OK] — 10 poderes — linha ~3812
- **Sorte** — [OK] — 10 poderes — linha ~3818 (nota: esta é a única entrada do JSON com o campo extra `"id": "Sorte"`, inconsistência cosmética, não afeta o conteúdo)
- **Viagem** — [OK] — 10 poderes — linha ~3826

**Conclusão:** `pathfinder_dominios.json` bate com o livro — 15/15 domínios presentes, nomes e listas de poderes
conferidos corretos.

## Vantagens (Edges)

`vantagens.json` já contém uma cobertura muito ampla e tagueada `PATHFINDER` (~200 entradas) para tudo que o livro
básico lista nas seções de sumário de vantagens (Antecedente, Classe, Combate, Liderança, Poder, Prestígio,
Profissionais, Sociais, Estranhas, Lendárias — linhas ~6919–7534 de docs/swade_pathfinder_basico) e para as 3
Vantagens de Prestígio exclusivas do Compêndio. Todas as entradas abaixo foram conferidas nome-a-nome contra o texto
e estão [OK].

### Vantagens de Classe (as 11 classes centrais de Golarion)

- **Bárbaro** — id: `classe_barbaro` — [OK] — Restrição de Armadura (Média), Rápido, Fúria — linha ~6976 de docs/swade_pathfinder_basico
  - Ataque Poderoso (`ataque_poderoso`), Olhar Intimidador (`olhar_intimidador`), Surto de Força (`surto_de_forca`) — [OK] — linhas ~6978–6981
- **Bardo** — id: `classe_bardo` — [OK] — Antecedente Arcano (Bardo), Interferência de Armadura leve, Língua Afiada — linha ~6982
  - Inspirar Heroísmo (`inspirar_heroismo`), Contra-Canção (`contra_cancao`), Marcha Fúnebre (`marcha_funebre`) — [OK] — linhas ~6988–6992
- **Clérigo** — id: `classe_clerigo` — [OK] — Antecedente Arcano (Clérigo), Canalização de Energia — linha ~6994
  - Destruir Mortos-Vivos (`destruir_mortos_vivos`), Poderes Favorecidos (`poderes_favorecidos_clerigo`), Maestria Divina (`maestria_divina_clerigo`) — [OK] — linhas ~6998–7006
- **Druida** — id: `classe_druida` — [OK] — Elo com a Natureza, Sentido Natural — linha ~7007
  - Forma Selvagem (`forma_selvagem`), Poderes Favorecidos (`poderes_favorecidos_druida`), Maestria Divina (`maestria_divina_druida`) — [OK] — linhas ~7011–7015
- **Feiticeiro** — id: `classe_feiticeiro` — [OK] — Antecedente Arcano (Feiticeiro), Linhagem — linha ~7016
  - Poderes Favorecidos (`poderes_favorecidos_feiticeiro`), Maestria Arcana (`maestria_arcana_feiticeiro`), Linhagem Avançada (`linhagem_avancada`) — [OK] — linhas ~7018–7024
- **Guerreiro** — id: `classe_guerreiro` — [OK] — Flexibilidade Marcial — linha ~7025
  - Golpe Mortal (`golpe_mortal`), Flexibilidade Marcial Aprimorada (`flexibilidade_marcial_aprimorada`), Proeza Marcial (`proeza_marcial`) — [OK] — linhas ~7027–7030
- **Ladino** — id: `classe_ladino` — [OK] — Restrição de Armadura leve, Ataque Furtivo — linha ~7034
  - Pressentir Armadilhas (`pressentir_armadilhas`), Reflexos Sobrenaturais (`reflexos_sobrenaturais`), Oportunista (`oportunista`) — [OK] — linhas ~7039–7049
- **Mago** — id: `classe_mago` — [OK] — Antecedente Arcano (Mago), Elo Arcano, Escola, Grimório — linha ~7053
  - Poderes Favorecidos (`poderes_favorecidos_mago`), Maestria Arcana (`maestria_arcana_mago`), Inspiração Mística (`inspiracao_mistica`) — [OK] — linhas ~7058–7065
- **Monge** — id: `classe_monge` — [OK] — Punho Atordoante, Golpe Desarmado — linha ~7067
  - Poderes Místicos Monge (`poderes_misticos_monge`), Grande Ki (`grande_ki`), Corpo Vazio (`corpo_vazio`) — [OK] — linhas ~7071–7084
- **Paladino** — id: `classe_paladino` — [OK] — Aura de Coragem, Código de Honra, Detectar o Mal, Punir o Mal — linha ~7086
  - Poderes Místicos Paladino (`poderes_misticos_paladino`), Misericórdia (`misericordia`), Montaria (`montaria_paladino`) — [OK] — linhas ~7094–7101
- **Patrulheiro** — id: `classe_patrulheiro` — [OK] — Inimigo Predileto, Terreno Predileto, Travessia de Floresta — linha ~7102
  - Presa (`presa`), Poderes Místicos Patrulheiro (`poderes_misticos_patrulheiro`), Mestre Caçador (`mestre_cacador`) — [OK] — linhas ~7108–7116

### Vantagens de Poder (arcanas gerais)

- **Armadura Arcana** (`armadura_arcana`), **Artífice** (`artifice`), **Canalização** (`canalizacao`),
  **Concentração** (`concentracao`), **Drenar a Alma** (`drenar_a_alma`), **Guerreiro Sagrado/Profano**
  (`guerreiro_sagrado_profano`), **Novos Poderes** (`novos_poderes`), **Pontos de Poder** (`pontos_de_poder`),
  **Surto de Poder** (`surto_de_poder`) — todas [OK] — linhas ~7264–7287 de docs/swade_pathfinder_basico

### Vantagens de Prestígio

Livro básico (linhas ~7288–7432 de docs/swade_pathfinder_basico), cada uma com estágios I/II/III:

- **Arqueiro Arcano** (`arqueiro_arcano`, `arqueiro_arcano_2`, `arqueiro_arcano_3`) — [OK] — linha ~7290
- **Assassino** (`assassino`, `assassino_2`, `assassino_3`) — [OK] — linha ~7305
- **Cavaleiro Místico** (`cavaleiro_mistico` I/II/III) — [OK] — linha ~7315
- **Cronista Desbravador** (`cronista_desbravador` I/II/III) — [OK] — linha ~7327
- **Dançarino das Sombras** (`dancarino_das_sombras` I/II/III) — [OK] — linha ~7344
- **Discípulo do Dragão** (`discipulo_do_dragao` I/II/III) — [OK] — linha ~7364
- **Duelista** (`duelista` I/II/III) — [OK] — linha ~7381
- **Mestre do Conhecimento** (`mestre_do_conhecimento` I/II/III) — [OK] — linha ~7389
- **Místico Teurgo** (`mistico_teurgo` I/II/III) — [OK] — linha ~7407
- **Trapaceiro Arcano** (`trapaceiro_arcano` I/II/III) — [OK] — linha ~7418

Compêndio (exclusivas da região do Mar Interior, linhas ~6530–6910 de docs/swade_pathfinder_compendio):

- **Agoureiro** (`agoureiro`, `agoureiro_2`, `agoureiro_3`) — [OK] — leitor místico de fortunas com baralho de Agouro — linha ~6605 de docs/swade_pathfinder_compendio
- **Cavaleiro Infernal** (`cavaleiro_infernal`, `cavaleiro_infernal_2`, `cavaleiro_infernal_3`) — [OK] — campeão da lei com armadura própria (ver Equipamento) — linha ~6640 de docs/swade_pathfinder_compendio
- **Louva-a-Deus Vermelha** (`louva_a_deus_vermelha`, `louva_a_deus_vermelha_2`, `louva_a_deus_vermelha_3`) — [OK] — assassinas da Ilha Mediogalti, arma predileta sabre serrilhado — linha ~6675 de docs/swade_pathfinder_compendio

### Vantagens Profissionais, Sociais, Estranhas, Lendárias, Antecedente e Combate/Liderança

Todas as vantagens dos sumários (linhas ~6919–7534 de docs/swade_pathfinder_basico) — Ambidestro, Aristocrata,
Atraente/Muito Atraente, Brutamontes, Carismático, Corajoso, Cura Rápida, Famoso/Muito Famoso, Impulso, Ligeiro,
Linguista, Musculoso, Prontidão, Resistência Arcana/Aprimorada, Sorte/Sorte Grande, Rápido (Antecedente); Arma
Predileta/Aprimorada, Atacar Primeiro/Aprimorado, Atirador, Bloquear/Aprimorado, Brigão, Pugilista, Calculista,
Contra-Ataque/Aprimorado, Corredor, Duro de Matar/Muito Duro de Matar, Esquiva/Aprimorada, Finta, Focado/Extremamente
Focado, Frenesi/Aprimorado, Golpe Poderoso, Guerreiro Treinado, Impiedoso, Instinto Assassino, Lutador Improvisador,
Lutar com Duas Armas, Mãos Firmes, Matador de Gigantes, Nervos de Aço/Aprimorados, Queixo de Ferro, Recarregamento
Rápido, Reflexos de Combate, Retirada/Aprimorada, Tiro Mortal, Tiro Rápido/Aprimorado, Varredura/Aprimorada
(Combate); Comando, Presença de Comando, Estrategista, Mestre Estrategista, Fervor, Inspirar, Líder Nato, Mantenham a
Formação! (Liderança); Acrobata, Combatente Acrobata, Ás, Conserta Tudo, Erudito, Investigador, Ladrão, Mateiro,
Nascido na Sela, Pau Pra Toda Obra, Soldado, Trovador (Profissionais); Agitador, Ameaçador, Cativar o
Ambiente/Multidão, Conexões, Confiável, Elevar o Moral, Elo Comum, Humilhar, Manha, Obstinado, Vontade de Ferro,
Provocador, Réplica (Sociais); Campeão, Curandeiro, Elo Animal, Noção do Perigo, Senhor das Feras, Sucateiro
(Estranhas); Duro na Queda/Muito Duro na Queda, Mestre de Arma/das Armas, Parceiro, Profissional, Especialista,
Mestre, Seguidores (Lendárias) — **[OK] todas presentes em `vantagens.json` com tag `PATHFINDER`**, ids em
snake_case já existentes conferem com os nomes do livro.

## Complicações (Hindrances)

O "Sumário de Complicações" (linha ~6836 de docs/swade_pathfinder_basico) lista 54 complicações nomeadas
(Almofadinha, Analfabeto, Anêmico, Arrogante, Atrapalhado, Boca Grande, Cauteloso, Cego, Código de Honra, Covarde,
Curioso, Deficiente Auditivo, Delirante, Desagradável, Desastrado, Desconfiado, Desejo de Morrer, Excesso de
Confiança, Feio, Fobia, Forasteiro, Ganancioso, Guiado, Hábito, Heroico, Hesitante, Idoso, Impulsivo, Inimigo,
Invejoso, Jovem, Leal, Lento, Língua Presa, Má Sorte, Mudo, Não Sabe Nadar, Obeso, Obrigação, Pacifista,
Peculiaridade, Pequeno, Pobreza, Procurado, Sanguinário, Segredo, Sem Escrúpulos, Sem Noção, Sensível, Teimoso, Um
Braço Só, Um Olho Só, Vergonha, Vingativo, Visão Ruim, Voto). Todas as 54 batem (por nome) com as **56 entradas**
tagueadas `PATHFINDER` em `complicacoes.json` (`almofadinha`, `analfabeto`, `anemico`, `arrogante`, `atrapalhado`,
`boca_grande`, `cauteloso`, `cego`, `codigo_de_honra`, `covarde`, `curioso`, `deficiente_auditivo`, `delirante`,
`desagradavel`, `desastrado`, `desconfiado`, `desejo_de_morrer`, `excesso_de_confianca`, `feio`, `fobia`,
`forasteiro`, `ganancioso`, `guiado`, `habito`, `heroico`, `hesitante`, `idoso`, `impulsivo`, `inimigo`, `invejoso`,
`jovem`, `leal`, `lento`, `lingua_presa`, `ma_sorte`, `mudo`, `nao_sabe_nadar`, `obeso`, `obrigacao`, `pacifista`,
`peculiaridade`, `pequeno`, `pobreza`, `procurado`, `sanguinario`, `segredo`, `sem_escrupulos`, `sem_nocao`,
`sensivel`, `teimoso`, `um_braco_so`, `um_olho_so`, `vergonha`, `vingativo`, `visao_ruim`, `voto`).

- **Todas as 54 complicações do sumário** — [OK] — linha ~6837 de docs/swade_pathfinder_basico

Não foi encontrada nenhuma complicação nova exclusiva do Compêndio de Golarion (o texto ali é predominantemente
lore/organizações/planos, sem uma seção de complicações).

## Perícias (Skills)

O "Sumário de Características" (linha ~6801 de docs/swade_pathfinder_basico) lista exatamente as 25 perícias
usadas em Savage Pathfinder, incluindo as duas perícias arcanas do cenário:

- **Atirar, Atletismo, Cavalgar, Ciência, Conhecimento Acadêmico, Conhecimento de Batalha, Conhecimento Geral,
  Conjurar, Consertar, Curar, Dirigir, Fé, Furtividade, Intimidar, Jogar, Ladinagem, Lutar, Navegar, Ocultismo,
  Perceber, Performance, Persuadir, Pilotar, Provocar, Sobrevivência** — [OK] todas as 25 — linha ~6802 de
  docs/swade_pathfinder_basico. `pericias.json` tem exatamente 25 entradas tagueadas `PATHFINDER`, contagem e nomes
  batem 1:1 (**Conjurar** é a perícia arcana de Antecedente Arcano (Magia); **Fé** é a de Antecedente Arcano
  (Milagres)).

## Equipamento (incluindo moeda de Golarion)

`equipamentos.json` tem 17 categorias/subtipos tagueados `PATHFINDER` (~200 itens): Equipamento de Aventura (Geral,
Kits e Ferramentas, Vestuário e Serviços, Itens Alquímicos), Animais e Arreios, Armaduras Medievais, Escudos
Medievais, Armas Medievais (Corpo a Corpo, Distância, Munição), Armas Especiais (Cerco), Veículos (Terrestres,
Aéreos, Aquáticos), Materiais Especiais e **Itens Especiais / Itens Mágicos (Mar Interior)** — este último cobre o
catálogo fixo (comprável) de 3 itens mágicos do Compêndio: Acha-Caminho (500 po), Máscara do Louva-a-Deus (6.000 po)
e Unguento da Revivificação (300 po) — [OK], compatível com a política de "catálogo fixo comprável" (não é um
sistema de criação de itens mágicos). Também confirmadas as peças de armadura "do Cavaleiro Infernal" (Placa de
Peito, Braçadeiras, Grevas, Elmo Pesado/Fechado) e o Sabre Serrilhado da Louva-a-Deus Vermelha, ambos ligados às
Vantagens de Prestígio do Compêndio.

### Moeda de Golarion

- **Peça de Cobre (pc)**, **Peça de Prata (pp)**, **Peça de Ouro (po)**, **Peça de Platina (pl)** — [OK] — linha
  ~7765 de docs/swade_pathfinder_basico ("MOEDA" / "MOEDAS E VALOR DE TROCA": 1 pl = 10 po = 100 pp = 1.000 pc; o
  padrão de custos da tabela de equipamentos é em po). O livro também lista nomes regionais de moedas por
  divindade/nação (linha ~594) — puramente narrativo/lore, sem efeito mecânico, portanto fora de escopo mecânico.
- **Materiais especiais** — [OK, resolvido em 2026-08-31] — o livro descreve 6 materiais na seção "MATERIAIS ESPECIAIS" (linhas
  ~7690–7810 de docs/swade_pathfinder_basico): **Adamante** (PA +1 em armas, ignora 2 PA em armaduras, +3.000 po),
  **Madeira Negra** (metade do peso, Dureza +2), **Couro de Dragão** (conta como obra-prima, +4 de armadura contra
  o elemento do dragão), **Ferro Frio** (Dureza +2, dobro do custo, eficaz contra fadas/demônios), **Mithral**
  (reduz Força Mínima em um dado, conta como prata) e **Prata Alquímica** (afeta criaturas com Fraqueza à prata,
  10x custo). Todos os 6 já existem em `equipamentos.json`, porém na categoria "Materiais Especiais / Modificadores
  de Material" **tagueada `FANTASIA`**, não `PATHFINDER` (ids: `Adamantina`/`Bronze`/`Couro de Dragão`/`Ferro
  Frio`/`Madeira Negra`/`Metal Sombrio`/`Mithral`/`Obsidiana`/`Prata Alquímica` — sem campo `id` próprio, apenas
  `nome`). A categoria "Materiais Especiais / Metais e Madeiras" que É tagueada `PATHFINDER` contém apenas 1 item
  (Adamante, 3.000 po) — duplicata parcial da entrada `FANTASIA` "Adamantina". Ou seja, o conteúdo mecânico já
  existe e bate com o livro, mas 5 dos 6 materiais (Madeira Negra, Couro de Dragão, Ferro Frio, Mithral, Prata
  Alquímica) não estavam marcados como válidos para o livro PATHFINDER. Adicionada uma cópia de cada um dos 5 na
  categoria "Materiais Especiais / Metais e Madeiras" já tagueada `PATHFINDER` (mesma onde já estava `adamante`),
  com `origem: "PATHFINDER"` — mesmo padrão de "uma entrada por livro em que o conteúdo é reimpresso" do resto do
  catálogo.

**`PathfinderCurrencyIds` no código** (`app/src/main/java/com/example/swadebuilder/model/ids/DomainIds.kt`, linha
48): `PL`, `PO`, `PP`, `PC` — **bate exatamente** com as 4 moedas do livro (Platina, Ouro, Prata, Cobre) e com a
ordem de conversão 1:10:100:1000 usada na tabela do livro.

## Poderes (Powers)

O Capítulo Cinco de docs/swade_pathfinder_basico ("PODERES", linha ~12385) descreve 64 poderes distintos
(Adivinhação, Ajuda, Amigo das Feras, Âncora Planar, Andar nas Paredes, Atordoar, Aumentar/Reduzir Característica,
Banir, Barreira, Campo de Dano, Cavar, Cegar, Confusão, Conjurar Aliado, Conjurar Item, Crescimento/Encolhimento,
Cura, Dádiva do Guerreiro, Deflexão, Detectar/Ocultar Arcano, Desejo, Devastação, Disfarce, Dissipar, Drenar Pontos
de Poder, Elo Mental, Empatia, Enredar, Explosão, Falar Idioma, Fantoche, Ferir, Ilusão, Iluminar/Obscurecer,
Intangibilidade, Invisibilidade, Leitura de Objeto, Leitura Mental, Limpeza Mental, Localizar, Maldição, Manipulação
Elemental, Medo, Morosidade/Velocidade, Mudança de Forma, Parar o Tempo, Polimorfia Perniciosa, Proteção, Proteção
Ambiental, Proteção Arcana, Raio, Rajada, Ressurreição, Santuário, Som/Silêncio, Sono, Telecinese, Teleporte, Viagem
Planar, Vidência, Visão Distante, Visão Sombria, Voar, Zumbi).

- **Todos os 64 poderes** — [OK] — presentes em `poderes.json` com sufixo `_pf` e tag `PATHFINDER` (ex.:
  `adivinhacao_pf`, `ancora_planar_pf`, `viagem_planar_pf`, `zumbi_pf` etc.) — contagem confere exatamente com o
  livro, linha ~12385 de docs/swade_pathfinder_basico (índice de poderes nas linhas ~15563–15685, "SUMÁRIO DE
  PODERES"). Note-se que vários desses poderes (os sem sufixo `_pf`) já existem como poderes "core" reutilizados por
  outros cenários — a versão `_pf` parece existir para casos onde a descrição/estágio difere ligeiramente por
  cenário; não foi feita uma auditoria linha-a-linha de cada descrição de poder individual (72 sub-blocos de texto),
  apenas a conferência de que a lista completa de nomes está coberta.
- **Dispositivos Arcanos** (regra de criação de itens mágicos ligados a poderes, linha ~12780) — **fora de
  escopo** (é um sistema de criação de itens mágicos, ver "Fora de escopo").
- **Modificadores de Poderes / Manifestações / Truques** (regras gerais de customização de poder, linha ~12680) —
  regra mecânica, não uma entrada de catálogo — não requer entrada própria em JSON.

## Fora de escopo

- **Bestiário** (Capítulo Oito de docs/swade_pathfinder_basico, linha ~18403) — estatísticas de monstros e a
  entrada "Ancestralidade" como marcador de bestiário (Goblins, arcontes, demônios, diabos etc.) — não são
  ancestralidades jogáveis de criação de personagem.
- **Lore de reinos/nações** — Capítulos Dois e Três de docs/swade_pathfinder_compendio (Avistan/Garund, Guia do Mar
  Interior) — geografia, história e cultura das nações de Golarion, sem efeito mecânico direto na ficha.
- **Gerador de Aventura** — Capítulo Quatro de docs/swade_pathfinder_compendio (linha ~4174) — ferramenta de Mestre
  para gerar aventuras, não pertence à criação de personagem.
- **Organizações/facções (texto narrativo)** — Capítulo Cinco de docs/swade_pathfinder_compendio (Consórcio Aspis
  etc., linha ~5062) e Capítulo Um (Sociedade Desbravadora, linha ~283) — apenas o texto de lore; as três Vantagens
  de Prestígio que essas organizações concedem (Agoureiro, Cavaleiro Infernal, Louva-a-Deus Vermelha) já foram
  incluídas na seção de Vantagens acima.
- **O Grande Além (planos)** — Capítulo Oito de docs/swade_pathfinder_compendio (linha ~9019) — cosmologia dos
  planos, relevante para lore/aventuras, não para a criação mecânica de personagem.
- **Baralho das Surpresas** — Capítulo Nove de docs/swade_pathfinder_compendio (linha ~9853) — ferramenta de
  Mestre (cartas de complicações narrativas de aventura), não uma criação de personagem.
- **Itens Mágicos — Capítulo Seis de docs/swade_pathfinder_basico** (linha ~15685) — a **criação**/enchantamento de
  itens mágicos (regras de "Criando Itens Mágicos", slots de corpo etc.) está fora de escopo por instrução explícita;
  mantido apenas o catálogo fixo comprável já coberto (itens do Compêndio na seção Equipamento acima). Os itens
  mágicos genéricos do Capítulo Seis do básico (anéis, varinhas, poções padrão de Savage Worlds) não foram
  extraídos individualmente pois normalmente já vêm do conjunto "core" do sistema, não são exclusivos de Golarion —
  se o app quiser um catálogo fixo desses também, vale conferir separadamente (fora do escopo desta tarefa).
- **Regras gerais de jogo (Capítulos Três, Quatro e Sete de docs/swade_pathfinder_basico)** — regras de teste de
  característica, ferramentas de aventura (batalhas em massa, conflito social, perigos) e o capítulo "Narrando o
  Jogo" — regras de mestre/sistema, não catálogo de criação de personagem.
- **Sistema de criação de reino/domínio como território** — não encontrado em nenhum dos dois arquivos (o termo
  "domínio" no corpus se refere exclusivamente aos domínios religiosos do Antecedente Arcano, já cobertos acima).

## Resumo de pendências

- **[FALTA]**: nenhuma entrada de ancestralidade, vantagem, complicação, perícia, domínio ou poder ficou sem
  correspondência em JSON — a base de dados já está com cobertura completa para os dois livros dentro do escopo de
  criação de personagem.
- **[FORA DE ESCOPO, resolvido em 2026-08-31]**: tabela "Divindades e Domínios" (linha ~3661-3705 de
  docs/swade_pathfinder_basico) — 20 divindades de Golarion, cada uma com Tendência/Áreas de Interesse/lista de
  Domínios sugeridos. É uma tabela de referência narrativa (que divindade combina com qual tema) para orientar a
  escolha de domínio, não uma entrada mecânica nova por si — os Domínios em si (Terra, Nobreza, Proteção etc.) já
  são cobertos como Vantagens do Antecedente Arcano Clérigo. Mesma categoria de tabela-de-lore já deixada de fora
  em outros livros desta sessão (ex.: Casta → Estágio do Demônio no CSV).
- **[OK, resolvido em 2026-08-31]**: os 5 materiais especiais sem tag `PATHFINDER` — ver detalhe na seção
  "Materiais especiais" acima.
