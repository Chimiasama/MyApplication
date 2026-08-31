# Índice de Criação de Personagem — Arte da Guerra (ADG)

Este índice cobre **apenas** o conteúdo de criação de personagem dos dois arquivos-fonte do
livro "Arte da Guerra": `docs/swade_adg` (corpo principal, 15112 linhas) e
`docs/swade_adg_diario_do_kui` (suplemento "Diário do Kui", 1474 linhas). O cenário substitui
a perícia Lutar por **Jutsu** (uma perícia por categoria de arma, via Especialização de
Perícia) e substitui Antecedente Arcano/magia por **Técnicas Chi**, ativadas com a nova
perícia Foco (ou Transição, para Elementalistas). Um personagem escolhe uma
**Ancestralidade** e depois um **Tropo** — a lista compilada de "arquétipos" (Artista
Marcial, Bu Xista, Elementalista, Kui, Protagonista, Samurai, Shinobi, Youxia, e o Mon do
Diário do Kui) que concede perícias, vantagens grátis e Técnicas Chi iniciais. Todo o
conteúdo abaixo foi conferido contra `app/src/main/assets/*.json` filtrando pela tag de livro
`ARTE_DA_GUERRA`; **tudo que foi conferido está [OK]** — nenhum item de criação de personagem
ficou de fora dos dados do app. Não há [FALTA] nesta revisão.

## Ancestralidades

Todas as 11 ancestralidades do cenário existem em `ancestralidades.json` com a tag
`ARTE_DA_GUERRA` (arquivo não usa campo `id` próprio; a chave é o campo `nome`). As primeiras
sete são "Kemonomimi" (humanoides com traços de animal) e todas elas recebem Visão no Escuro.

- **Akaimimi (Panda Vermelho)** — [OK] — `ancestralidades.json: "Akaimimi (Panda Vermelho)"` — Sábios estoicos; Complicação Peculiaridade (Pedância), Convenção d6 (máx. d12+1), Conhecimento Geral d6 — docs/swade_adg linha ~4770
- **Araiguma (Guaxinim)** — [OK] — `"Araiguma (Guaxinim)"` — Curiosos e gulosos; Provocar d4, imune a veneno/doença/náusea por comida, Furtividade d6, Hábito+Peculiaridade — docs/swade_adg linha ~4843
- **Inumimi (Cão)** — [OK] — `"Inumimi (Cão)"` — Leais; +1 Resistência, mordida For+d4 (Prender/Enredar), Perceber d6, Complicação Leal (matilha) — docs/swade_adg linha ~4928
- **Kitsunemimi (Raposa)** — [OK] — `"Kitsunemimi (Raposa)"` — Intrigantes/burocratas; Complicação Cauteloso, perícia inicial à escolha d4, Vantagem Cativar o Ambiente grátis — docs/swade_adg linha ~5011
- **Nekomimi (Gato)** — [OK] — `"Nekomimi (Gato)"` — Ligados à sorte; Jogar d6, Bene extra por sessão, Complicação Azarado — docs/swade_adg linha ~5107
- **Tanukimimi (Tanuki)** — [OK] — `"Tanukimimi (Tanuki)"` — Camponeses tenazes, "medianos em tudo" (perfil equilibrado) — docs/swade_adg linha ~5186
- **Usagimimi (Coelho)** — [OK] — `"Usagimimi (Coelho)"` — Comerciantes; perícia à escolha d6, +2 Movimentação e corrida +1 dado, -2 em Provocar/Intimidar — docs/swade_adg linha ~5269
- **Terracota** — [OK] — `"Terracota"` — Estátua animada por um espírito com dever/pacto; +3 Resistência, imune a veneno/doença/afogamento, não regenera após a "Hora de Ouro" (precisa de Consertar), Movimentação 5/corrida d4, Chi inicial -1, Voto ou Obrigação (Maior), Forasteiro (menor) — docs/swade_adg linha ~5362
- **Umvee (Filhos da Lua)** — [OK] — `"Umvee (Filhos da Lua)"` — Ancestrais protetores da natureza, ligados aos Dons do Ápice (berserker) — docs/swade_adg linha ~5515
- **Feral** — [OK] — `"Feral"` — Meio-Umvee/meio-besta; Sobrevivência d6, Vantagem Furioso + Complicação Sanguinário, Dons do Ápice, d6 em For/Agi/Vig, Astúcia máx. d6 na criação, não pode aprender Técnicas Chi, Forasteiro (menor) — docs/swade_adg linha ~5721
- **Onigem (Sangue Oni)** — [OK] — `"Onigem (Sangue Oni)"` — Descendentes mistos de Oni; +1 Medo/Absorção/venenos/náusea, Ancestralidade Infame (-2 Persuadir com desconhecidos), Força inicial d6/máx. d12+1, Complicação Suspeitoso (menor) — docs/swade_adg_diario_do_kui linha ~44

## Tropos

O Tropo é escolhido após a Ancestralidade e concede: perícias/dados iniciais, vantagens
gratuitas e o número de **Técnicas Chi** iniciais do personagem (regra em
docs/swade_adg linha ~5762). Todos os 9 tropos do livro batem exatamente com os 9 registros
de `adg_tropos.json` (ids `tropo_*`).

- **Artista Marcial** — id: `tropo_artista_marcial` — [OK] — 0 Técnicas Chi; Jutsu (Desarmado) d6 (ou d4+d4 outro Jutsu), Vantagens Artista Marcial + Estudante de Artes Marciais, +1 Atributo físico à escolha com bônus associado, 3 "Treinamentos Únicos" ativados por Chi — docs/swade_adg linha ~5788
- **Bu Xista (Monge)** — id: `tropo_buxista` — [OK] — 2 Técnicas Chi; Mente sobre a Matéria (transe via Foco), Convenção/Ocultismo d4, escolhe uma seita/caminho (Equilibrado, Círculo, Exterior etc.) — docs/swade_adg linha ~5944
- **Elementalista** — id: `tropo_elementalista` — [OK] — 0 Técnicas Chi (só Manifestações do próprio elemento); Jutsu (Desarmado) d4 + Transição d4, Vantagem Lutador Elemental, escolhe 1 dos 6 elementos (Ar/Fogo/Metal/Rocha/Água/Madeira) com Manobras próprias — docs/swade_adg linha ~6123
- **Kui** — id: `tropo_kui` — [OK] — 2 Técnicas Chi; habilidade Exorcizar (Foco vs Espírito do alvo), caçador de espíritos/oni ligado à Ordem Noite Sem Lua — docs/swade_adg linha ~6316
- **Protagonista** — id: `tropo_protagonista` — [OK] — Rola em 5 tabelas (Técnicas de Chi d4, Perícia Básica d6, Vantagem d8, Qualidades de Herói d10, Habilidades d12) para gerar um pacote aleatório de bônus — docs/swade_adg linha ~6502
- **Samurai** — id: `tropo_samurai` — [OK] — 1 Técnica Chi; Conhecimento Batalha d6, Jutsu ou Atirar d6, Vantagem Comando (ou Vantagem de Combate), Propriedade (Domínio), Estoico, escolhe 2 Posturas (Asa da Garça, Bico do Galo, Carapaça da Tartaruga, Cauda do Macaco, Casco do Cavalo, Enxame de Ratos, Presas de Águia, Presas do Javali) — docs/swade_adg linha ~6628
- **Shinobi** — id: `tropo_shinobi` — [OK] — 2 Técnicas Chi; escolhe 1 Talento Shinobi (Alteração, Pés Leves, Místico, Passo das Sombras), Caçador Silencioso (+1d6 dano na Finalização), escolhe Tipo de Treinamento (Infiltrador/Batedor/Espião) — docs/swade_adg linha ~6759
- **Youxia** — id: `tropo_youxia` — [OK] — 2 Técnicas Chi; Kensai (Arma Predileta + Jutsu associado +1 dado), Protetor (desvia ataque gastando Chi), arma Kensai com traço especial (Ancestral/Carregada/Penetrante/Afiada) — docs/swade_adg linha ~6925
- **Mon** — id: `tropo_mon` — [OK] — Companheiro animal vinculado (Carta Selvagem sem avanço próprio); Técnica Chi Forma Bestial restrita ao Mon, Vínculo Bestial (Umvee), Sentidos Compartilhados, qualifica para Senhor das Feras/avanços de companheiro — docs/swade_adg_diario_do_kui linha ~116

## Sistema de Jutsu

Jutsu é a perícia (Agilidade) que substitui Lutar neste cenário: cada categoria de arma
escolhida é uma **perícia separada** (regra de Especialização de Perícia aplicada
exclusivamente a Jutsu); usar uma arma fora do grupo conhecido dá -2. A descrição completa
da regra e das 9 categorias-exemplo está embutida como texto no próprio registro `Lutar`
(tag `ARTE_DA_GUERRA`) de `pericias.json` — **[OK]**, o texto da regra bate com o livro.

- **Mecânica Jutsu** — id existente: perícia `Lutar` (tag ARTE_DA_GUERRA) — [OK] — Cada grupo de Jutsu = 1 perícia; -2 fora do grupo conhecido; aprender novo grupo = aprender nova perícia — docs/swade_adg linha ~9000
- **Jutsu (Concussão)** — categoria-exemplo — [OK, texto embutido] — bastão de 3 partes, chui, pá do monge, nunchaku, tetsubo, tonfa, martelo de guerra — docs/swade_adg linha ~9221
- **Jutsu (Corrente)** — categoria-exemplo — [OK, texto embutido] — dardo com corda, kusarigama, kyoketsu-shogi, manrikigusari, martelo meteoro, cabelo — docs/swade_adg linha ~9229
- **Jutsu (Leve)** — categoria-exemplo — [OK, texto embutido] — faca, kama, tessen, jitte, sai, espada borboleta, nunchaku, pincel de ferro, tekko kagi — docs/swade_adg linha ~9238
- **Jutsu (Massivo)** — categoria-exemplo — [OK, texto embutido] — armas Massivas, sem penalidade de manejo — docs/swade_adg linha ~9246
- **Jutsu (Passivo)** — categoria-exemplo — [OK, texto embutido] — bastão-bo, pincel de ferro, jitte, nunchaku, sai, tessen — docs/swade_adg linha ~9250
- **Jutsu (Haste)** — categoria-exemplo — [OK, texto embutido] — bastão-bo, alabarda, lança, machado longo, naginata, yari — docs/swade_adg linha ~9254
- **Jutsu (Samurai)** — categoria-exemplo — [OK, texto embutido] — katana, naginata, nodachi, tanto, tessen, wakizashi — docs/swade_adg linha ~9263
- **Jutsu (Espada)** — categoria-exemplo — [OK, texto embutido] — dao, jian, katana, nodachi, shuang gou, wakizashi — docs/swade_adg linha ~9269
- **Jutsu (Desarmado)** — categoria-exemplo — [OK, texto embutido] — punho, pé, cabeçada, ombros, pernas, cotovelos, joelhos, dedos — docs/swade_adg linha ~9276

**Nota de implementação:** o app (`CriadorState.kt`, `isJutsuPericia`/`jutsuSlotRegex`) trata
"Lutar" + slots livres "Jutsu 1", "Jutsu 2"... como perícias Jutsu quando o compêndio Arte da
Guerra está ativo, com o nome da categoria digitado livremente pelo usuário (placeholder
"Ex: Espada, Leve, Desarmado..." em `PericiasSection.kt`/`TroposSection.kt`). Isso é
[OK] e corresponde à intenção do livro, que explicitamente diz que as 9 categorias acima são
"exemplos" e que grupos e narradores podem criar novas categorias — não há uma lista fechada
para travar em um dropdown.

### Estilos de Luta (Jutsu Desarmado)

12 estilos de combate desarmado (2 por elemento), concedendo bônus em Aparar/dano/manobras
nos níveis Discípulo e Mestre; usados pela Vantagem `estudante_artes_marciais`. Confirmados
como opções embutidas em `vantagens.json` (array de estilos na vantagem Estudante de Artes
Marciais) — [OK], todos os 12 batem.

- **Asa da Garça de Qinlang (Ar)** — [OK] — docs/swade_adg linha ~9724
- **Cauda Vermelha (Ar)** — [OK] — docs/swade_adg linha ~9762
- **Templo da Lua Reflexiva (Água)** — [OK] — docs/swade_adg linha ~9784
- **Rio Divisor (Água)** — [OK] — docs/swade_adg linha ~9804
- **Bico de Basabasa (Fogo)** — [OK] — docs/swade_adg linha ~9831
- **Garra do Tigre (Fogo)** — [OK] — docs/swade_adg linha ~9850
- **Pata da Pantera Caçadora (Madeira)** — [OK] — docs/swade_adg linha ~9879
- **Pata Macia do Macaco (Madeira)** — [OK] — docs/swade_adg linha ~9912
- **Forjado no Aço (Metal)** — [OK] — docs/swade_adg linha ~9943
- **Escola das Três Presas (Metal)** — [OK] — docs/swade_adg linha ~9969
- **Couro de Ferro (Rocha)** — [OK] — docs/swade_adg linha ~9988
- **Ferroada do Escorpião (Rocha)** — [OK] — docs/swade_adg linha ~10034

## Antecedente Arcano — Sistema de Chi (Técnicas / Poderes)

O cenário bane Antecedente Arcano, Resistência Arcana e todas as Vantagens de Poder
tradicionais (docs/swade_adg linha ~7375). Em seu lugar, todo personagem pode ter uma
**Reserva de Chi** (concedida por Tropo/Vantagens) e aprender **Técnicas Chi**, ativadas com
uma rolagem de Foco (ou Transição, exclusiva de Elementalistas) — regras completas em
docs/swade_adg linha ~11340-11605. As Técnicas usam a mesma estrutura de Poderes do SWADE
(Custo, Duração, Distância, Requisito, Manifestações, Modificadores) e várias são "Poderes
Alterados" com nome trocado (indicado entre parênteses). As 40 Técnicas + 9 "Manobras de
Maestria" (usadas por Elementalistas via a Vantagem Maestria na Transição) somam os **49**
registros com tag `ARTE_DA_GUERRA` em `poderes.json` — **todas presentes, [OK]**.

- **Aljava de Porco-Espinho** — id: `chi_aljava` — [OK] — manifesta flechas/virotes/projéteis do nada — docs/swade_adg linha ~11605
- **Areia de Bolso** — id: `chi_areia_bolso` — [OK] — docs/swade_adg linha ~11636
- **Asa da Garça** — id: `chi_asa_garca` — [OK] — -2 em ataques corpo a corpo contra o herói — docs/swade_adg linha ~11668
- **Ataque Chi Corpo a Corpo** — id: `chi_ataque_corporal` — [OK] — +1d6 em ataque/dano corpo a corpo — docs/swade_adg linha ~11685
- **Ataque Chi à Distância** — id: `chi_ataque_distancia` — [OK] — +1d6 em ataque/dano à distância — docs/swade_adg linha ~11710
- **Aura de Chi** — id: `chi_aura_chi` — [OK] — docs/swade_adg linha ~11746
- **Casco de Tartaruga** — id: `chi_casco_tartaruga` — [OK] — docs/swade_adg linha ~11762
- **Clone** — id: `chi_clone` — [OK] — clone com Destemido — docs/swade_adg linha ~11784
- **Descanso de Mikaboshi (Sono)** — id: `chi_descanso_mikaboshi` — [OK] — Poder Alterado: Sono — docs/swade_adg linha ~11838
- **Descanso Eterno** — id: `chi_descanso_eterno` — [OK] — coloca mortos-vivos/espíritos para descansar — docs/swade_adg linha ~11855
- **Detectar Chi** — id: `chi_detectar_chi` — [OK] — docs/swade_adg linha ~11888
- **Explosão de Chi** — id: `chi_explosao_chi` — [OK] — docs/swade_adg linha ~11911
- **Fissura** — id: `chi_fissura` — [OK] — docs/swade_adg linha ~11933
- **Forma Bestial** — id: `chi_forma_bestial` — [OK] — usada pelo Tropo Mon — docs/swade_adg linha ~11972
- **Fuga Ninja** — id: `chi_fuga_ninja` — [OK] — docs/swade_adg linha ~12007
- **Goku** — id: `chi_goku` — [OK] — docs/swade_adg linha ~12036
- **Grande Muralha (Barreira)** — id: `chi_grande_muralha` — [OK] — Poder Alterado: Barreira — docs/swade_adg linha ~12053
- **Grito de Fei** — id: `chi_grito_fei` — [OK] — docs/swade_adg linha ~12087
- **Hadouken (Raio)** — id: `chi_hadouken` — [OK] — Poder Alterado: Raio — docs/swade_adg linha ~12122
- **Inundação** — id: `chi_inundacao` — [OK] — docs/swade_adg linha ~12137
- **Lâmina Oculta** — id: `chi_lamina_oculta` — [OK] — docs/swade_adg linha ~12161
- **Lições de Koi** — id: `chi_licoes_koi` — [OK] — movimento sobre líquidos — docs/swade_adg linha ~12191
- **Máscara Kabuki (Disfarce)** — id: `chi_mascara_kabuki` — [OK] — Poder Alterado: Disfarce — docs/swade_adg linha ~12224
- **Memento (Confusão)** — id: `chi_memento` — [OK] — Poder Alterado: Confusão — docs/swade_adg linha ~12243
- **Muco do Caracol** — id: `chi_muco_caracol` — [OK] — docs/swade_adg linha ~12261
- **Negar o Chi** — id: `chi_negar_chi` — [OK] — docs/swade_adg linha ~12295
- **Névoa Noturna** — id: `chi_nevoa_noturna` — [OK] — docs/swade_adg linha ~12320
- **Ordem do Guerreiro (Ferir)** — id: `chi_ordem_guerreiro` — [OK] — Poder Alterado: Ferir — docs/swade_adg linha ~12349
- **Passos de Joro** — id: `chi_passos_joro` — [OK] — docs/swade_adg linha ~12365
- **Poção da Fang (Reduzir Característica)** — id: `chi_pocao_fang` — [OK] — Poder Alterado: Reduzir Característica — docs/swade_adg linha ~12389
- **Picada do Escorpião** — id: `chi_picada_escorpiao` — [OK] — docs/swade_adg linha ~12410
- **Poof!** — id: `chi_poof` — [OK] — teleporte modificado — docs/swade_adg linha ~12436
- **Projeção de Chi** — id: `chi_projecao` — [OK] — docs/swade_adg linha ~12461
- **Raposa Prateada** — id: `chi_raposa_prateada` — [OK] — docs/swade_adg linha ~12519
- **Reflexos Fotográficos** — id: `chi_reflexos_fotograficos` — [OK] — docs/swade_adg linha ~12538
- **Reorientação** — id: `chi_reorientacao` — [OK] — docs/swade_adg linha ~12568
- **Salto do Coelho Lunar** — id: `chi_salto_coelho_lunar` — [OK] — docs/swade_adg linha ~12592
- **Sopro de Dragão (Rajada)** — id: `chi_sopro_dragao` — [OK] — Poder Alterado: Rajada — docs/swade_adg linha ~12623
- **Tentáculos!** — id: `chi_tentaculos` — [OK] — docs/swade_adg linha ~12639
- **Visão Clara** — id: `chi_visao_clara` — [OK] — docs/swade_adg linha ~12683
- **Manobras de Maestria (9)** — ids: `manobra_armadura`, `manobra_barreira`, `manobra_cavar`, `manobra_cura`, `manobra_desequilibrar`, `manobra_impacto`, `manobra_nadar`, `manobra_planar`, `manobra_raio` — [OK] — desbloqueadas pela Vantagem Maestria na Transição, exclusivas de Elementalistas — docs/swade_adg linha ~9414, ~7941

### Talismãs (regra de criação de item — fora do escopo de compra fixa)

A criação de talismãs (10145) é uma sub-regra de **fabricação de item mágico** (custo em Chi
por Estágio + modificadores, rolagem de Ocultismo/Ofício) — enquadra-se na exclusão de
"criação de itens mágicos/artefatos" do escopo desta tarefa. Os talismãs **comprados prontos**
(catálogo fixo) já existem em `equipamentos.json` sob "Itens Especiais / Talismãs":
Talismã Explosivo, Talismã de Papel Ilusório, Talismã de Papel Protetor, Talismã de Papel do
Véu — [OK].

## Vantagens

35 vantagens novas/renomeadas do corpo principal + 5 do Diário do Kui, todas com a tag
`ARTE_DA_GUERRA` em `vantagens.json` — **[OK]** em todos os casos (ids entre parênteses).

**Vantagens de Antecedente** (docs/swade_adg linha ~7390):
- **Aristocrata\*** (`aristocrata`) — [OK] — linhagem nobre de Clã — linha ~7391
- **Domínio** (`dominio`) — [OK] — possuir terras, 4 PC iniciais (+3 por repetição) — linha ~7401
- **Ferimento Extra** (`ferimento_extra`) — [OK] — -1 adicional por nível de Ferimento — linha ~7415
- **Legado** (`legado`) — [OK] — +20 de Reputação inicial — linha ~7426
- **Linhagem Temível** (`linhagem_temivel`) — [OK] — mordida For+d6 (Kemonomimi) — linha ~7437
- **Mentor** (`mentor`) — [OK] — benefícios por Estágio (rerrolagem, perícia não treinada, recuperação) — linha ~7449
- **Rico\*** (`rico`) — [OK] — 2.000 ki iniciais — linha ~7494

**Vantagens de Chi** (docs/swade_adg linha ~7508):
- **23 Passos** (`vinte_e_tres_passos`) — [OK] — mover em qualquer superfície por 1 Chi — linha ~7512
- **Absorver** (`absorver`) — [OK] — gasta Chi para rerrolar/ampliar Absorção — linha ~7523
- **Canalização\*** (`canalizacao`) — [OK] — ampliação em Foco reduz custo de Técnica — linha ~7531
- **Concentração\*** (`concentracao`) — [OK] — versão SW aplicada a Técnicas Chi — linha ~7539
- **Espírito de Ferro** (`espirito_de_ferro`) — [OK] — recupera de Atordoado/Náusea por 1 Chi — linha ~7544
- **Espírito de Ferro Aprimorado** (`espirito_de_ferro_aprimorado`) — [OK] — supera Medo/paralisia automaticamente — linha ~7554
- **Explosão Exterior** (`explosao_exterior`) — [OK] — +1d6 de dano por ponto de Chi — linha ~7564
- **Meditação de Chi** (`meditacao_chi`) — [OK] — sem penalidade para recuperar Chi meditando — linha ~7571
- **Músculo Gyuki** (`musculo_gyuki`) — [OK] — +1 dado de Força por 1 rodada — linha ~7580
- **Nova Técnica** (`nova_tecnica`) — [OK] — concede 1 Técnica Chi extra (ou nova Manifestação) — linha ~7586
- **Pontos de Chi** (`pontos_de_chi`) — [OK] — +4 na Reserva Máxima de Chi, 1x/Estágio — linha ~7602
- **Salto Duplo** (`salto_duplo`) — [OK] — salto ampliado por 1 Chi — linha ~7609
- **Um Polegar** (`um_polegar`) — [OK] — empurrão via Ataque de Toque — linha ~7626

**Vantagens de Combate** (docs/swade_adg linha ~7635):
- **Agitador\*** (`agitador`) — [OK] — linha ~7636
- **Grito de Guerra** (`grito_de_guerra`) — [OK] — expande Agitador — linha ~7644
- **Abraço Esmagador** (`abraco_esmagador`) — [OK] — dano extra ao Apertar em um agarrão — linha ~7648
- **Contra-Jutsu** (`contra_jutsu`) — [OK] — +2 Aparar contra categoria de Jutsu enfrentada — linha ~7660
- **Estudante de Artes Marciais** (`estudante_artes_marciais`) — [OK] — escolhe 1 dos 12 estilos de luta — linha ~7670
- **Discípulo das Artes Marciais** (`discipulo_artes_marciais`) — [OK] — nível Discípulo do estilo — linha ~7688
- **Mestre em Artes Marciais** (`mestre_artes_marciais`) — [OK] — nível Mestre do estilo — linha ~7694
- **Saltador de Paredes** (`saltador_de_paredes`) — [OK] — escalada mais barata em combate — linha ~7703
- **Tiro Rastreador** (`tiro_rastreador`) — [OK] — mover durante Mirar — linha ~7711

**Vantagens Profissionais** (docs/swade_adg linha ~7722):
- **Acrobata\*** (`acrobata`) — [OK] — rerrolagem em Atletismo ou Acrobacia — linha ~7723
- **Atleta** (`atleta`) — [OK] — linha ~7731
- **Matador de Demônios** (`matador_demonios`) — [OK] — bônus vs. demônio/oni/shamni escolhido, repetível — linha ~7740
- **Detetive** (`detetive`) — [OK] — +2 Perceber/Persuadir/Pesquisar em investigação — linha ~7767
- **Embaixador de Clã** (`embaixador_de_cla`) — [OK] — +2 Intimidar/Persuadir representando um clã menor — linha ~7787
- **Pupila** (`pupila`) — [OK] — recrutas do Orgulho de Gion (Clã Kitsune) — linha ~7814
- **Sabotador** (`sabotador`) — [OK] — -4 Dureza de estruturas/objetos atacados — linha ~7846

**Vantagens de Tropo** (docs/swade_adg linha ~7862):
- **Ordem-Unida** (`ordem_unida`) — [OK] — Samurai; melhora uma Vantagem de Liderança — linha ~7867
- **Domínio de Postura** (`dominio_de_postura`) — [OK] — Samurai; +1 a uma Postura — linha ~7880
- **Dupla Postura** (`dupla_postura`) — [OK] — Samurai; funde 2 Posturas — linha ~7889
- **Especialista em Sabotagem** (`especialista_em_sabotagem`) — [OK] — Shinobi — linha ~7900
- **Espírito Maior** (`espirito_maior`) — [OK] — Kui; melhora o espírito vinculado — linha ~7916
- **Histórico Adicional** (`historico_adicional`) — [OK] — Youxia; Histórico extra na arma Kensai — linha ~7935
- **Maestria na Transição** (`maestria_na_transicao`) — [OK] — Elementalista; desbloqueia Manobra de Maestria — linha ~7941
- **Mente Superior** (`mente_superior`) — [OK] — Bu Xista; Mente sobre a Matéria sem rolagem — linha ~7949
- **Modelo Ideal** (`modelo_ideal`) — [OK] — Protagonista; rola +1x nas tabelas de Perícia/Qualidades — linha ~7955
- **Nova Postura** (`nova_postura`) — [OK] — Samurai; nova postura conhecida — linha ~7961
- **Proeza Física** (`proeza_fisica`) — [OK] — Artista Marcial; +1 Treinamento Único — linha ~7968
- **Protetor Absoluto** (`protetor_absoluto`) — [OK] — Youxia; usa Bene em vez de Chi para Protetor — linha ~7975
- **Talento Extra** (`talento_extra`) — [OK] — Shinobi; +1 Talento Shinobi — linha ~7982
- **Talismã Instantâneo** (`talisma_instantaneo`) — [OK] — Kui; talismã como ação — linha ~7988
- **Talismã Longevo** (`talisma_longevo`) — [OK] — Kui; talismã dura 2d6 dias — linha ~7998

**Vantagens Estranhas** (docs/swade_adg linha ~8006):
- **Danificar a Roupa** (`danificar_a_roupa`) — [OK] — sacrifica roupa para absorver Ferimentos — linha ~8007

**Vantagens do Diário do Kui** (docs/swade_adg_diario_do_kui linha ~179):
- **Chifres** (`chifres`) — [OK] — arma natural For+d4 — linha ~181
- **Chifres Grandes** (`chifres_grandes`) — [OK] — +1 dado de dano do chifre — linha ~185
- **Chifres Pontudos** (`chifres_pontudos`) — [OK] — chifre com PA 2 — linha ~189
- **Foco de Chi (Chi)** (`foco_chi_chi`) — [OK] — ativa Técnicas sem Mãos Livres/Sem Fala via chifre — linha ~194
- **Transformação** (`transformacao`) — [OK] — Akaimimi/Kitsunemimi/Tanukimimi; age como Mudança de Forma por 2 Chi — linha ~206

## Complicações

18 complicações novas/modificadas, todas presentes com tag `ARTE_DA_GUERRA` em
`complicacoes.json` — **[OK]**.

- **Além das Fronteiras (Maior)** (`alem_das_fronteiras`) — [OK] — -2 Convenção/Conh. Geral/Perceber em normas culturais — docs/swade_adg linha ~7053
- **Amnésia (Menor/Maior)** (`amnesia`) — [OK] — linha ~7066
- **Apático (Menor/Maior)** (`apatico`) — [OK] — insensibilidade emocional — linha ~7082
- **Azarado (Maior)** (`azarado`) — [OK] — perde quase todos os Benes em Falha Crítica — linha ~7103
- **Bloqueio Interno (Menor/Maior)** (`bloqueio_interno`) — [OK] — Reserva de Chi inicial reduzida — linha ~7110
- **Delinquente Juvenil (Menor/Maior)** (`delinquente_juvenil`) — [OK] — combina com Jovem — linha ~7119
- **Desonrado (Menor)** (`desonrado`) — [OK] — -5 Reputação, reações Não Cooperativas no clã — linha ~7134
- **Dorminhoco (Menor/Maior)** (`dorminhoco`) — [OK] — linha ~7148
- **Endividado (Menor)** (`endividado`) — [OK] — inicia só com 200 ki — linha ~7162
- **Exilado do Clã (Maior)** (`exilado_do_cla`) — [OK] — -15 Reputação, banido do território — linha ~7172
- **Gêmeo Maligno (Menor/Maior)** (`gemeo_maligno`) — [OK] — linha ~7195
- **Infame (Menor)** (`infame`) — [OK] — -5 Reputação, -2 Persuadir com estranhos — linha ~7222
- **Prisioneiro Espiritual (Menor/Maior)** (`prisioneiro_espiritual`) — [OK] — pacto unilateral com espírito/Oni — linha ~7233
- **Pudor (Menor)** (`pudor`) — [OK] — linha ~7266
- **Roupas Rasgadas (Menor)** (`roupas_rasgadas`) — [OK] — Dado de Roupas ligado a gasto de Chi — linha ~7280
- **Sangramento Nasal (Menor/Maior)** (`sangramento_nasal`) — [OK] — linha ~7315
- **\*Sem-Noção (Menor/Maior, versão ADG)** (`sem_nocao_adg`) — [OK] — versão modificada da Complicação padrão — linha ~7350
- **Sem Clã (Menor)** (`sem_cla`) — [OK] — -2 Persuadir/Intimidar com membros de Clãs Maiores — linha ~7355
- **Suspeitoso (Menor/Maior)** (`suspeitoso`) — [OK] — do Diário do Kui; -2 furtar/fugir em Complicação Maior — docs/swade_adg_diario_do_kui linha ~170

## Perícias

6 perícias novas do cenário (mais a redefinição de "Lutar"→Jutsu, já coberta acima),
todas com tag `ARTE_DA_GUERRA` em `pericias.json` — **[OK]**.

- **Acrobacia (Agilidade)** — [OK] — escalada sem equipamento, fuga de agarrões, quedas — docs/swade_adg linha ~9027
- **Convenção (Astúcia)** — [OK] — etiqueta social, rituais, reconhecer significados ocultos — docs/swade_adg linha ~9137
- **Foco (Espírito)** — [OK] — ativa Técnicas Chi, recupera Chi, alternativa ao Espírito contra Medo — docs/swade_adg linha ~9190
- **Jutsu (Agilidade)** — [OK] — ver seção "Sistema de Jutsu" acima — docs/swade_adg linha ~9199
- **Ofício (Astúcia)** — [OK] — substitui perícia de profissional para criação de obras/ofícios — docs/swade_adg linha ~9177
- **Transição (Vigor)** — [OK] — exclusiva de Elementalistas, ativa Manobras/Técnicas elementais — docs/swade_adg linha ~9283

## Equipamento

Armas e armaduras orientais/wuxia do capítulo "Armas" (docs/swade_adg linha ~8494) — todas
catalogadas em `equipamentos.json` sob os grupos "Armas Corpo a Corpo / Arte da Guerra",
"Armas de Combate à Distância / Arte da Guerra" e "Armaduras / Arte da Guerra" — **[OK]**
(os itens não têm campo `id` individual; identificados por `nome` dentro do grupo).

**Armas corpo a corpo (33, todas [OK]):** Cabelo (Corrente), Bastão-bo (Haste/Concussão/Passivo),
Chui (Concussão), Dao (Espada), Emeici (Leve), Pincel de Ferro (Leve/Passivo), Espada
Borboleta (Leve), Faca (Leve), Guandao (Haste), Jian (Espada/Samurai), Jitte
(Leve/Concussão/Passivo), Kama (Leve), Katana (Espada/Samurai), Kunai (Leve), Kusarigama
(Corrente), Kyoketsu-shoge (Corrente), Manrikigusari (Corrente), Martelo Meteoro
(Corrente/Concussão), Maidao (Espada), Naginata (Haste/Samurai), Nodachi (Espada/Samurai),
Nunchaku (Leve/Concussão/Passivo), Dardo com Corda (Corrente), Sai (Leve/Concussão/Passivo),
Shuang Gou (Espada), Tanto (Samurai), Tekko Kagi, Tessen, Tetsubo (Concussão), Bastão de Três
Partes, Tonfa, Wakizashi (Samurai/Espada), Yari (Haste) — docs/swade_adg linha ~8646-8823

**Armas à distância (5, todas [OK]):** Daikyu (Arco Longo), Ovo de Fumaça, Kunai, Shuriken,
Yumi (Arco) — docs/swade_adg linha ~8612-8624

**Armaduras (18, todas [OK]):** Roupas Acolchoadas, conjuntos Pele de Naga / Espelhada /
Lamelar / Lamelar Pesada / Superior (Braçadeira, Couraça, Grevas, Elmo, 4 peças cada) —
docs/swade_adg (equipamento inicial de Tropo em linha ~8072-8082, tabela de armaduras
referenciada no capítulo de Equipamento em linha ~8131)

**Notas de arma orientais (glossário descritivo, sem stats próprios — [OK] cobertas pelas
armas acima):** Bastão Bo, Espada Borboleta, Cabelo, Chui, Dao, Daikyu, Emeici, Faca, Pincel
de Ferro, Guandao, Jitte, Jian, Kama, Katana, Kunai, Kusarigama, Kyoketsu-shogi,
Manrikigusari, Martelo Meteoro, Naginata, Nodachi, Nunchaku, Dardo com Corda, Sai, Shuang
Gou, Tekko Kagi, Tessen, Tetsubo, Bastão de Três Partes — docs/swade_adg linha ~8502-8776

## Poderes

Ver seção **"Antecedente Arcano — Sistema de Chi (Técnicas / Poderes)"** acima: neste
cenário não existem "poderes" separados de Antecedente Arcano — as 40 Técnicas Chi + 9
Manobras de Maestria listadas ali **são** os "poderes novos" do livro, todos com tag
`ARTE_DA_GUERRA` em `poderes.json` — [OK].

## Fora de escopo

Conteúdo lido e identificado, mas deliberadamente **excluído** deste índice por não afetar a
criação/build de personagem, conforme instrução da tarefa:

- **Amorai (continente/lore de cenário)** — docs/swade_adg linha ~2021 — geografia e história do mundo, sem mecânica de build.
- **Bestiário completo** — docs/swade_adg_diario_do_kui, seções "Habilidades Especiais" (~213), "Animais" (~228), "Sobrenatural" (~620) e "Desmortos" (~1079) — estatísticas de criaturas/monstros, exceto as 5 vantagens e a Complicação Suspeitoso já extraídas de dentro dessas seções (conferido: `grep REQUISITOS` no arquivo não achou mais nenhuma vantagem fora das já listadas).
- **"O Prato Favorito de Poco"** — docs/swade_adg_diario_do_kui, final do arquivo — conto/short story, sem mecânica.
- **Regras de Duelo** — docs/swade_adg linha ~10845 — subsistema de resolução de duelos em mesa, sem impacto em build.
- **Reputação (subsistema completo)** — docs/swade_adg linha ~11102 — mecânica social de fama (Pontos de Fama, gasto em rolagens); os *valores iniciais* concedidos por Vantagens/Complicações (Legado +20, Desonrado -5, Exilado do Clã -15, Infame -5) já estão descritos dentro das entradas de Vantagens/Complicações acima, então o subsistema em si (sem "id" comprável) foi tratado como regra de ambientação, não como entrada de catálogo.
- **Talismãs — regra de criação** — docs/swade_adg linha ~10145 — fabricação de item mágico personalizado (excluído por instrução explícita da tarefa); os talismãs prontos do catálogo já estão em Equipamento acima.
- **Ofício — Criações Mundanas / Arrecadando Fundos / Pausa** — docs/swade_adg linha ~10224 — regras de economia/crafting/downtime (fabricar itens com desconto, gerar renda), análogas a criação de itens — não afeta build, só economia pós-criação.
- **Domínio (subsistema de propriedade/feudo)** — docs/swade_adg linha ~10314 — regras estendidas de gerenciamento de terras/feudo ligadas à Vantagem Domínio (já catalogada); o subsistema em si é conteúdo de downtime/campanha.
- **Regras de Ambientação gerais de combate** — docs/swade_adg linha ~9050-9701 (Empurrão de Chi, Fanáticos, Heróis Sem Armadura, Limite de Ferimentos etc.) — mecânicas de mesa/combate, não de build, exceto "Nasce um Herói" (ignora requisitos de Estágio na criação) e a regra de recuperação de Chi via Bene, que são apenas notas de contexto já mencionadas na introdução deste índice.
- **Locais/facções do cenário** — Terra dos Tigres (~12717), Feudo das Serpentes (~13012), Domínio dos Kitsune (~13133), Cidade Imperial (~13432), Galeria (~14130) — lore de cenário não-mecânico.
