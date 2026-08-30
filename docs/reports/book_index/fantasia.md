# Índice — Compêndio de Fantasia (SWADE)

Índice estruturado do conteúdo de **criação de personagem** em `docs/swade_fantasia` (22.667 linhas), cruzado com `app/src/main/assets/*.json`. Conclusão geral: a cobertura de dados já é **quase completa** — praticamente todo o conteúdo mecânico de personagem deste livro (ancestralidades, complicações, vantagens, antecedentes arcanos por arquétipo, domínios, poderes e catálogo de equipamento/itens mágicos) já está presente nos JSONs com a tag `"FANTASIA"` em `livros`. Os poucos pontos sem "id" formal são subsistemas de regra (não itens de catálogo) — ver seção "Antecedente Arcano — Subsistemas". Status: **[OK]** = já existe no JSON com tag FANTASIA; **[FALTA]** = não encontrado; **[CONFERIR]** = existe parcialmente ou é regra (não entrada de catálogo), checar manualmente.

## Ancestralidades (raças) — `ancestralidades.json`

29 ancestralidades novas/reimpressas, todas já presentes com `"livros": ["FANTASIA"]`. Precedidas por texto de orientação ao Mestre (Escolhas Individuais l.465, Culturas Malignas l.518, Poderes Inerentes l.537, Nomes l.542, Pacotes Culturais l.566 — 4 exemplos de "pacote cultural" sem ficha própria) — conselhos, não entradas de catálogo, ver Fora de escopo.

- **Anões** — id: `anoes` — [OK] — ancestralidade anã clássica — linha ~703
- **Aquarianos** — id: `aquarianos` — [OK] — povo anfíbio/aquático — linha ~738
- **Avianos** — id: `avianos` — [OK] — humanoides alados — linha ~766
- **Celestiais** — id: `celestiais` — [OK] — descendentes de seres celestiais — linha ~797
- **Centauros** — id: `centauros` — [OK] — torso humanoide sobre corpo equino — linha ~814
- **Descendente Elemental** — id: `descendente_elemental` — [OK] — herança de um dos quatro elementos — linha ~847
- **Draconianos** — id: `draconianos` — [OK] — humanoides dracônicos — linha ~885
- **Elfos** — id: `elfos` — [OK] — linha ~923
- **Fadas** — id: `fadas` — [OK] — pequenos seres feéricos alados — linha ~958
- **Gnomos** — id: `gnomos` — [OK] — linha ~987
- **Goblins** — id: `goblins` — [OK] — linha ~1016
- **Golens** — id: `golens` — [OK] — construtos animados — linha ~1045
- **Humanos** — id: `humanos` — [OK] — linha ~1072
- **Infernais** — id: `infernais` — [OK] — descendentes de demônios/diabos — linha ~1080
- **Insetoides** — id: `insetoides` — [OK] — humanoides com traços de inseto — linha ~1113
- **Meio-Elfos** — id: `meio_elfos` — [OK] — linha ~1153
- **Meio-Gigantes** — id: `meio_gigantes` — [OK] — linha ~1172
- **Meio-Orcs** — id: `meio_orcs` — [OK] — linha ~1197
- **Minotauros** — id: `minotauros` — [OK] — linha ~1221
- **Ogros** — id: `ogros` — [OK] — linha ~1257
- **Orcs** — id: `orcs` — [OK] — linha ~1282
- **Pequeninos** — id: `pequeninos` — [OK] — linha ~1314
- **Povo Ratazana** — id: `povo_ratazana` — [OK] — linha ~1344
- **Povo Rato** — id: `povo_rato` — [OK] — linha ~1377
- **Povo Serpente** — id: `povo_serpente` — [OK] — linha ~1404
- **Rakashanos** — id: `rakashanos` — [OK] — humanoides felinos — linha ~1438
- **Renascidos** — id: `renascidos` — [OK] — mortos que retornaram à vida — linha ~1472
- **Sáurios** — id: `saurios` — [OK] — humanoides reptilianos — linha ~1517
- **Transmorfos** — id: `transmorfos` — [OK] — metamorfos/mudapeles — linha ~1543

Também há uma tabela genérica de "Novas Habilidades de Ancestralidade" (custos para montar raças customizadas, l.613-702) — reaproveita/atualiza a mesma tabela do livro básico; já coberta por `basico_habilidades_raciais.json` (115 entradas, origem BASICO) — **[OK]** (conteúdo idêntico ao básico, sem tag específica de FANTASIA necessária).

## Nova Perícia e regras de perícia

- **Alquimia (Astúcia)** — id: `alquimia` — [OK] — perícia arcana dos alquimistas, também usada para criar itens alquímicos — linha ~1822 (confirmado em `pericias.json`)
- **Manejo de Animais** — [CONFERIR/Fora de escopo] — não é perícia nova; é uma regra que reaproveita Intimidar/Persuadir para comandar animais — linha ~1829 (não requer entrada própria em `pericias.json`)

## Complicações Novas — `complicacoes.json`

13 complicações novas, todas presentes com tag FANTASIA:

- **Altruísta (Menor/Maior)** — id: `altruista` — [OK] — sacrifica-se pelos outros — linha ~1591
- **Amaldiçoado (Maior)** — id: `amaldicoado` — [OK] — -2 em poderes benéficos que o afetam — linha ~1603
- **Apaixonado (Menor)** — id: `apaixonado` — [OK] — penalidade extra a Desafios de Atraente/Muito Atraente — linha ~1618
- **Componentes Materiais (Maior)** — id: `componentes_materiais` — [OK] — só para Antecedente Arcano; -4 em perícias arcanas sem componentes — linha ~1627
- **Condenado (Maior)** — id: `condenado` — [OK] — -2 em Absorção — linha ~1650
- **Corrupção (Maior)** — id: `corrupcao` — [OK] — só para Antecedente Arcano; Falha Crítica gera complicações progressivas — linha ~1657
- **Desajeitado (Maior)** — id: `desajeitado` — [OK] — Falha Crítica sempre que o dado de perícia central for 1 — linha ~1679
- **Idealista (Menor)** — id: `idealista` — [OK] — visão preto-no-branco em dilemas morais — linha ~1687
- **Interferência de Armadura (Menor/Maior)** — id: `interferencia_de_armadura` — [OK] — só para Antecedente Arcano; -4 em perícia arcana usando armadura média/pesada — linha ~1696
- **Chauvinista (Menor/Maior)** — id: `chauvinista` — [OK] — penalidades sociais com outras culturas — linha ~1711
- **Sensibilidade Arcana (Menor/Maior)** — id: `sensibilidade_arcana` — [OK] — -2/-4 para resistir a poderes — linha ~1726
- **Sombrio (Menor)** — id: `sombrio` — [OK] — reage a provocações como se tivesse sido Provocado — linha ~1738
- **Talismã (Menor/Maior)** — id: `talisma` — [OK] — só para Antecedente Arcano; depende de item físico para conjurar — linha ~1753

## Vantagens Novas — `vantagens.json`

### De Antecedente (7)
- **Escolhido** — id: `escolhido` — [OK] — Convicção dura até o fim do encontro; ganha Inimigo (Maior) e marca indelével — linha ~1869
- **Herança** — id: `heranca` — [OK] — concede item mágico de 10.000 PO — linha ~1893
- **Inimigo Predileto** — id: `inimigo_predileto` — [OK] — rerrolagem grátis contra um tipo de inimigo — linha ~1910
- **Resistência Arcana** — id: `resistencia_arcana_fc` — [OK] — -2 em poderes/dano mágico contra o personagem — linha ~1930
- **Resistência Arcana Aprimorada** — id: `resistencia_arcana_aprimorada_fc` — [OK] — versão -4 — linha ~1947
- **Sangue Feérico** — id: `sangue_feerico` — [OK] — rerrolagem grátis ao resistir a poderes — linha ~1953
- **Terreno Predileto** — id: `terreno_predileto` — [OK] — bônus em Sobrevivência/Perceber e carta extra em terreno escolhido — linha ~1961

### De Combate (24)
- **Aguento o Tranco** — id: `aguento_o_tranco` — [OK] — rerrolagem em Absorção/Vigor vs. nocaute — linha ~1984
- **Ataque Furtivo** — id: `ataque_furtivo` — [OK] — bônus de Assassino vira d6 — linha ~1995
- **Ataque Furtivo Aprimorado** — id: `ataque_furtivo_aprimorado` — [OK] — aplica-se também a alvo Distraído — linha ~2001
- **Carga** — id: `carga` — [OK] — +2 dano ao correr antes de atacar — linha ~2007
- **Combate Próximo** — id: `combate_proximo` — [OK] — bônus contra oponentes muito próximos — linha ~2016
- **Combate Próximo Aprimorado** — id: `combate_proximo_aprimorado` — [OK] — melhora o alcance do bônus — linha ~2073
- **Defensor** — id: `defensor` — [OK] — compartilha bônus de escudo com aliado — linha ~2078
- **Deflexão de Projéteis** — id: `deflexao_de_projeteis` — [OK] — usa Aparar contra ataques à distância — linha ~2086
- **Fender** — id: `fender` — [OK] — +d6 de dano ao quebrar objetos — linha ~2097
- **Flexibilidade Marcial** — id: `flexibilidade_marcial` — [OK] — empresta temporariamente uma Vantagem de Combate — linha ~2108
- **Golpe Atordoador** — id: `golpe_atordoador` — [OK] — atordoa com armas contundentes — linha ~2116
- **Golpe de Asa** — id: `golpe_de_asa` — [OK] — ataque em cone com asas — linha ~2127
- **Guerreiro Treinado** — id: `guerreiro_treinado` — [OK] — melhora bônus de Agrupar — linha ~2144
- **Parede de Escudos** — id: `parede_de_escudos` — [OK] — bônus de Aparar em formação — linha ~2154
- **Lutador Sujo** — id: `lutador_sujo` — [OK] — +2 em Desafios com Lutar — linha ~2168
- **Lutador Realmente Sujo** — id: `lutador_realmente_sujo` — [OK] — ampliação em Desafio gera Finalização — linha ~2175
- **Oportunista** — id: `oportunista` — [OK] — Curinga dá +4 em vez de +2 — linha ~2183
- **Queimar** — id: `queimar` — [OK] — melhora dano de arma de sopro — linha ~2189
- **Rugir** — id: `rugir` — [OK] — Desafio de Intimidar em cone — linha ~2198
- **Selvageria** — id: `selvageria` — [OK] — Ataque Selvagem causa +4 em vez de +2 — linha ~2207
- **Tiro Duplo** — id: `tiro_duplo` — [OK] — dois projéteis num só ataque — linha ~2220
- **Tiro Duplo Aprimorado** — id: `tiro_duplo_aprimorado` — [OK] — Tiro Duplo duas vezes por turno — linha ~2238
- **Tiro Preciso** — id: `tiro_preciso` — [OK] — Desafio à distância resistido por Astúcia — linha ~2244
- **Reflexos Aprimorados** — id: `reflexos_aprimorados` — [OK] — melhora Evasão contra área — linha ~2251

### De Poder (10)
- **Artífice** — id: `artifice` — [OK] — cria Dispositivos Arcanos e itens mágicos — linha ~2266
- **Artífice Mestre** — id: `artifice_mestre` — [OK] — remove limite de progresso de criação — linha ~2279
- **Conjurador Silencioso** — id: `conjurador_silencioso` — [OK] — conjura sem falar — linha ~2286
- **Familiar** — id: `familiar` — [OK] — companheiro mágico pequeno — linha ~2293
- **Maestria Épica** — id: `maestria_epica` — [OK] — acesso a Modificadores Épicos de Poder — linha ~2377
- **Mago de Batalha** — id: `mago_de_batalha` — [OK] — conjura em Extras/tropas (ver Magia de Batalha) — linha ~2394
- **Mago de Sangue** — id: `mago_de_sangue` — [OK] — recupera PP ao causar Ferimento — linha ~2405
- **Poder Favorito** — id: `poder_favorito` — [OK] — ignora penalidades ao ativar um poder escolhido — linha ~2428
- **Poderes Místicos** — id: `poderes_misticos` — [OK] — pacotes simplificados (Bárbaro/Guerreiro/Ladrão/Monge/Paladino/Patrulheiro) — linha ~2438
- **Transferência** — id: `transferencia` — [OK] — transfere PP para outro conjurador — linha ~2490

### Profissionais (10)
- **Aptidão com Pedras** — id: `aptidao_com_pedras` — [OK] — Perceber grátis para armadilhas/portas em pedra — linha ~2502
- **Batedor** — id: `batedor` — [OK] — detecta perigos em viagem primeiro — linha ~2514
- **Caçador de Tesouros** — id: `cacador_de_tesouros` — [OK] — avalia tesouros/itens mágicos — linha ~2532
- **Cavaleiro** — id: `cavaleiro` — [OK] — vinculado a um senhor; ganha equipamento inicial — linha ~2549
- **Envenenador** — id: `envenenador` — [OK] — cria venenos mais rápido e duradouro — linha ~2579
- **Explorador** — id: `explorador` — [OK] — cartas extras e -10% tempo de viagem — linha ~2586
- **Montaria** — id: `montaria` — [OK] — companheiro montaria que progride (inclui Montaria Especial/Lendária) — linha ~2600 (rule box "A Vantagem Montaria" ~2640)
- **Nascido na Sela** — id: `nascido_na_sela` — [OK] — bônus de Cavalgar/Movimentação da montaria — linha ~2698
- **Pressentir Armadilhas** — id: `pressentir_armadilhas` — [OK] — detecta/evita/desarma armadilhas — linha ~2708
- **Trovador** — id: `trovador` — [OK] — bônus de Conhecimento Geral, usa Performance por Conhecimento Batalha — linha ~2732

### Sociais (1)
- **Enganador** — id: `enganador` — [OK] — escolhe resistência (Astúcia/Espírito) em Desafios — linha ~2745

### Estranhas (3)
- **Aura de Coragem** — id: `aura_de_coragem` — [OK] — bônus de Medo para aliados próximos — linha ~2757
- **Falar com Animais** — id: `falar_com_animais` — [OK] — comunicação com classes de animais — linha ~2764
- **Mudança Rápida** — id: `mudanca_rapida` — [OK] — licantropos mudam de forma como ação limitada — linha ~2784

### Lendárias (4)
- **Bando de Guerra** — id: `bando_de_guerra` — [OK] — Seguidores ganham Resiliente — linha ~2796
- **Imparável** — id: `imparavel` — [OK] — habilidade monstruosa Imparável — linha ~2805
- **Lar, Doce Lar** — id: `lar_doce_lar` — [OK] — recupera todos os PP uma vez por encontro em território natal — linha ~2815
- **Relíquia** — id: `reliquia` — [OK] — escolhe um item mágico do livro — linha ~2845

## Antecedente Arcano — Sistema de Magia deste livro

**Achado notável**: o livro substitui a "Vantagem Mago" genérica do livro básico por **14 Antecedentes Arcanos especializados por arquétipo** (nota explícita "A Vantagem Mago" l.2309-2333: não é usada aqui, para reforçar especialização). Cada arquétipo tem sua própria vantagem-base `Antecedente Arcano (X)` com perícia arcana, poderes iniciais e regras próprias, mais 2-3 vantagens exclusivas. Todos os 14 arquétipos + 29 vantagens específicas já constam em `vantagens.json` (tag FANTASIA) e em `geral_arcano_info.json` (slots/PP/perícia foco):

- **Alquimista** — id: `antecedente_arcano_alquimista` — [OK] — perícia Alquimia — linha ~6220 — vantagens: Mestre Alquimista (`mestre_alquimista`, l.6342), Químico (`quimico`, l.6356)
- **Bardo** — id: `antecedente_arcano_bardo` — [OK] — perícia Performance, conjura cantando/recitando — linha ~6364 — vantagens: Inspirar Heroísmo (`inspirar_heroismo`, l.6406), Instrumento (`instrumento`, l.6439), Marcha Fúnebre (`marcha_funebre`, l.6457)
- **Bruxo** — id: `antecedente_arcano_bruxo` — [OK] — linha ~6468 — vantagens: A Hora das Bruxas (`a_hora_das_bruxas`, l.6573), Mau-Olhado (`mau_olhado`, l.6584)
- **Clérigo** — id: `antecedente_arcano_clerigo` — [OK] — linha ~6604 — vantagens: Destruir Mortos-Vivos (`destruir_mortos_vivos`, l.6646), Misericórdia (`misericordia`, l.6670)
- **Diabolista** — id: `antecedente_arcano_diabolista` — [OK] — linha ~6680 — vantagens: Armadura Infernal (`armadura_infernal`, l.6748), Fúria Infernal (`furia_infernal`, l.6758)
- **Druida** — id: `antecedente_arcano_druida` — [OK] — linha ~6769 — vantagens: Cajado do Coração da Mata (`cajado_do_coracao_da_mata`, l.6826), Forma Verdadeira (`forma_verdadeira`, l.6849)
- **Elementalista** — id: `antecedente_arcano_elementalista` — [OK] — linha ~6868 — vantagens: Absorção Elemental (`absorcao_elemental`, l.6935), Maestria Elemental (`maestria_elemental`, l.6942)
- **Engenhoqueiro** — id: `antecedente_arcano_engenhoqueiro` — [OK] — linha ~6956 — vantagens: Armadura de Engenhoqueiro (`armadura_de_engenhoqueiro`, l.7032), Familiar Construto (`familiar_construto`, l.7060)
- **Feiticeiro** — id: `antecedente_arcano_feiticeiro` — [OK] — linha ~7102 — vantagens: Grande Poder (`grande_poder`, l.7169), Poder Fenomenal (`poder_fenomenal`, l.7181)
- **Ilusionista** — id: `antecedente_arcano_ilusionista` — [OK] — linha ~7195 — vantagens: Ilusão Mortal (`ilusao_mortal`, l.7229), Mestre da Ilusão (`mestre_da_ilusao`, l.7234)
- **Invocador** — id: `antecedente_arcano_invocador` — [OK] — linha ~7239 — vantagens: Barda Arcana (`barda_arcana`, l.7289), Grande Invocação (`grande_invocacao`, l.7296), Invocação Feroz (`invocacao_feroz`, l.7306)
- **Mago** — id: `antecedente_arcano_mago_fantasia` — [OK] — linha ~7317 — vantagens: Grimório (`grimorio`, l.7375), Inspiração Arcana (`inspiracao_arcana`, l.7386)
- **Necromante** — id: `antecedente_arcano_necromante` — [OK] — linha ~7401 — vantagens: Familiar Morto-Vivo (`familiar_morto_vivo`, l.7442), Recipiente da Alma (`recipiente_da_alma`, l.7454)
- **Xamã** — id: `antecedente_arcano_xama` — [OK] — linha ~7476 — vantagens: Fetiche Sagrado (`fetiche_sagrado`, l.7536), Magia Primordial (`magia_primordial`, l.7551)

### Subsistemas de regra do capítulo Arcano (não são "itens" de catálogo — lógica de app, não entrada JSON)
- **Múltiplos Antecedentes Arcanos** — [CONFERIR] — regra de multiclasse arcana (reserva de PP compartilhada) — linha ~6139
- **Truques** — [CONFERIR] — magias menores/cantrips sem custo de PP baseadas em poder já conhecido — linha ~7893
- **Dispositivos Arcanos** — [CONFERIR] — itens mágicos temporários feitos com a vantagem Artífice — linha ~7965
- **Magia de Batalha** — [CONFERIR] — regra para conjurar contra/com tropas em massa (liga-se à vantagem Mago de Batalha) — linha ~8049
- **Poderes Preparados** — [CONFERIR] — feitiços pré-preparados com aprimoramentos próprios — linha ~8106
- **Magia Ritualística** — [CONFERIR] — conjuração ritual longa com aprimoramentos próprios — linha ~8183

### Domínios (divindades/portfólios para Clérigos e afins) — `fantasia_dominios.json`
11 domínios, todos presentes:
- **Conhecimento** — [OK] — l.8307 · **Frio** — [OK] — l.8323 · **Ladinagem** — [OK] — l.8331 · **Lua** — [OK] — l.8353 · **Mar** — [OK] — l.8373 · **Guerra** — [OK] — l.8393 · **Justiça** — [OK] — l.8408 · **Morte** — [OK] — l.8437 · **Natureza** — [OK] — l.8463 · **Sol** — [OK] — l.8486 · **Vida** — [OK] — l.8499

## Poderes — `poderes.json` (69 novos/reimpressos com tag FANTASIA, todos [OK])

Adivinhação (l.8600) · Ajuda (l.8673) · Amigo das Feras (l.8719) · Âncora Planar (l.8756) · Andar nas Paredes (l.8833) · Atordoar (l.8853) · Aumentar/Reduzir Característica (l.8873) · Banir (l.8916) · Barreira (l.8945) · Bênção (l.8991) · Campo de Dano (l.9018) · Cavar (l.9048) · Cegar (l.9082) · Confusão (l.9108) · Conjurar Aliado (l.9136) · Conjurar Animal (l.9199) · Conjurar Item (l.9305) · Conjurar Monstro (l.9402) · Conjurar Morto-Vivo (l.9464) · Crescimento/Encolhimento (l.9509) · Cura (l.9559) · Dádiva do Guerreiro (l.9608) · Deflexão (l.9632) · Desejo (l.9659) · Detectar/Ocultar Arcano (l.9779) · Devastação (l.9854) · Disfarce (l.9895) · Dissipar (l.9920) · Drenar Pontos de Poder (l.9992) · Elo Mental (l.10022) · Empatia (l.10064) · Enredar (l.10104) · Explosão (l.10133) · Falar Idioma (l.10155) · Fantoche (l.10178) · Ferir (l.10218) · Iluminar/Obscurecer (l.10245) · Ilusão (l.10292) · Intangibilidade (l.10360) · Intervenção Mística (l.10398) · Invisibilidade (l.10462) · Leitura de Objeto (l.10493) · Leitura Mental (l.10521) · Limpeza Mental (l.10542) · Localizar (l.10582) · Maldição (l.10612) · Manipulação Elemental (l.10654) · Medo (l.10726) · Morosidade/Velocidade (l.10751) · Parar o Tempo (l.10820) · Mudança de Forma (l.10839) · Proteção (l.10885) · Proteção Arcana (l.10909) · Proteção Ambiental (l.10938) · Raio (l.10967) · Rajada (l.11004) · Ressurreição (l.11033) · Santuário (l.11071) · Som/Silêncio (l.11106) · Sono (l.11153) · Telecinese (l.11178) · Teleporte (l.11231) · Trancar/Destrancar (l.11300) · Viagem Planar (l.11349) · Vidência (l.11396) · Visão Distante (l.11436) · Visão Sombria (l.11460) · Voar (l.11483) · Zumbi (l.11502)

Todos confirmados em `poderes.json` com `"livros": ["FANTASIA"]` — **69/69 [OK]**. Nenhum poder novo faltando.

## Equipamento — `equipamentos.json` (29 grupos com tag FANTASIA, todos [OK])

- **Equipamento Geral** (Aventura e Utilidades, 55 itens; Extras do Básico, 5 itens) — [OK] — linha ~3266
- **Animais** (Montarias e Bestas, 8 itens) — [OK] — linha ~3409
- **Consumíveis** (Comida e Bebida, 12 itens) — [OK] — linha ~3419
- **Kits** (Kits de Classe, 9 itens) — [OK] — linha ~3447
- **Vestuário** (Roupas, 6 itens) — [OK] — linha ~3519
- **Materiais Especiais** (Modificadores de Material, 9 itens; inclui obras-primas) — [OK] — linha ~3601
- **Veículos** (Transporte terrestre 1 + Embarcações 3) — [OK] — linha ~3568
- **Itens Especiais → Venenos** (11 itens; regra "Criando Venenos" l.3788 é subsistema de fabricação, fora de escopo) — [OK] — linha ~3692
- **Armadura** (Proteção e Escudos, 32 itens) — [OK] — linha ~3836
- **Armas Corpo a Corpo** (Medievais, 42 itens) — [OK] — linha ~3964
- **Munição** (Projéteis, 9 itens) — [OK] — linha ~4023
- **Armas à Distância** (Medievais, 20 itens) — [OK] — linha ~4050
- **Armas de Pólvora** (Pólvora Negra, 3 itens) — [OK] — linha ~4092
- **Armas Especiais** (Cerco e Canhões, 8 itens; regra "Criando Itens Alquímicos" l.4142 é fabricação, fora de escopo) — [OK] — linha ~4103
- **Itens Especiais → Alquimia** (itens alquímicos comprável, 10 itens) — [OK] — linha ~4186

### Catálogo de itens mágicos prontos (Capítulo Cinco: Tesouro) — comprável/encontrável, mantido no escopo
- **Armaduras Mágicas** (3 itens) — [OK] — Tabela A, linha ~11942
- **Escudos Mágicos** (2 itens) — [OK] — Tabela B, linha ~11929
- **Armas Corpo a Corpo Mágicas** (3 itens) — [OK] — Tabela C, linha ~11963
- **Armas à Distância Mágicas** (3 itens) — [OK] — Tabela D, linha ~12012
- **Munição Mágica** (Flechas/Virotes, 4 itens) — [OK] — Tabela E, linha ~12052
- **Joias Mágicas** (2 itens) — [OK] — Tabela F, linha ~12085
- **Poções Mágicas** (12 itens) — [OK] — Tabela G, linha ~12131
- **Pergaminhos Mágicos e Tomos** (2 itens) — [OK] — Tabelas H/I, linhas ~12184/~12239
- **Itens Maravilhosos** (105 itens) — [OK] — Tabela J, linha ~12252
- **Relíquias e Artefatos** (7 itens, exemplos prontos: Cinzas de Ashur, Estátua da Fera, Ícone da Ruína, Matadora de Titãs, A Muralha, Tambores de Oon) — [OK] — linha ~13427
- **Geração Aleatória de Tesouro** (tabela de rolagem, 2 refs) — [OK] — linha ~11844
- **"Regras de Criação de Itens Mágicos"** — presente como 1 entrada de referência em `equipamentos.json`, mas o **subsistema de fabricação em si é fora de escopo** (ver abaixo) — linha ~13526

## Fora de escopo (apenas citado, não detalhado)

- **Bestiário** (Capítulo Seis) — centenas de criaturas/antagonistas e habilidades monstruosas de bestiário (Ataque Dilacerante, Atropelar, Drenar Energia, etc.) — linhas ~13636-21072
- **Fortaleza** (Capítulo, domínio/reino como sistema de jogo: aquisição, manutenção, melhorias de cômodos, cercos, armadilhas de masmorra) — linhas ~4228-5182
- **Criação de Itens Mágicos/Relíquias** (requisitos, processo, tabela de custo por oficina) e **Criando Venenos/Itens Alquímicos** (fabricação) — regras de fabricação, não catálogo — linhas ~3788, ~4142, ~13526-13684
- **Regras de Ambientação / Estilos de Fantasia** (Ações Limitadas, Traição, Cura Difícil, atividades de Pausa, Oponentes Gigantes, Convicção Vilanesca, e os 10 estilos: Tradicional, Sombria, Alta, Baixa, Histórica, Náutica, Superheroico, Espada e Feitiçaria, Tecnofantasia, Urbana) — regras/conselhos opcionais de Mestre por campanha, não catálogo de personagem — linhas ~5183-6107
- **Criação de Mundo (Apêndice A)** e **Planos de Existência (Apêndice B)** — lore de cenário e conselhos de worldbuilding, não mecânica de personagem — linhas ~21073-22376
- **Culturas Malignas, Nomes, Pacotes Culturais** (l.465-612) — conselhos de customização de ancestralidade, sem ficha própria a catalogar
