# Índice — Compêndio de Ficção Científica (SWADE)

Índice estruturado do conteúdo de **criação de personagem** do Compêndio de Ficção
Científica (`docs/swade_scifi`, ~23.412 linhas), cruzado entrada a entrada com
`app/src/main/assets/*.json` (tag de livro `SCI_FI`). Cobre os 13 capítulos do livro,
mas só entra no índice o que um jogador escolhe ao montar uma ficha — ancestralidades,
vantagens/complicações novas, perícias, equipamento comprável (armas, armaduras,
veículos, naves pequenas), cibernéticos, mechas (como opção de build) e poderes.
Bestiário, Impérios, Postos Avançados, Criador de Mundos e o sistema de espaçonave em
escala de campanha (gerenciamento de nave como entidade coletiva) ficam em
"Fora de escopo" ao final. Metodologia: `Grep` nos cabeçalhos de capítulo/seção,
`Read` dos trechos relevantes, depois checagem por nome+id em cada catálogo JSON
filtrando por `SCI_FI`/`FC`/`SCIFI`. Status: **[OK]** presente e íntegro, **[FALTA]**
ausente do app, **[CONFERIR]** presente mas com alguma divergência ou dúvida que
merece checagem manual.

## Ancestralidades (Capítulo 1, "Ancestralidades e Culturas", ~l.359-1150)

Todas as 21 ancestralidades prontas do capítulo estão em `ancestralidades.json` com
`livros: ["SCI_FI"]`. Além delas, o capítulo traz um "kit" de ~30 habilidades de
ancestralidade genéricas (l.391-637, ex. Ação Adicional, Arma de Sopro, Casca, Robô,
Telepatia) para o Mestre montar espécies próprias — isso é conteúdo de referência para
o Mestre, não uma entrada de personagem isolada, por isso não é listado item a item.

- **CENTAUX** — id: `CENTAUX` — [OK] — corpo inferior quadrúpede, Estável, Grande, Movimentação +2, Tamanho +2 — l.~721
- **DRAKENS** — id: `DRAKENS` — [OK] — símile de dragão, Cabeça Dura (chifres), Forte, Lento, Resistência +2 — l.~747
- **ELEMENTAIS** (pedra) — id: `ELEMENTAIS` — [OK] — Cabeça-Dura, Forte, Grande, Movimentação Reduzida, Resistência +2 — l.~767
- **FERAIS** — id: `FERAIS` — [OK] — mamíferos pequenos tipo guaxinim/furão, Diminuto, Alta Tecnologia (Maior), Espirituoso — l.~790
- **FLORANS** — id: `FLORANS` — [OK] — humanoides-planta, Dependência (sol), Sem Sangue, Robusto — l.~814
- **GELATINOIDES** — id: `GELATINOIDES` — [OK] — corpo amorfo, Ciber-Resistência, Fraco, Gelatinoso, Regeneração — l.~836
- **INSETOIDES** — id: `INSETOIDES` — [OK] — colmeia, Armadura +2, Comunitário, Garras, Incapaz de Falar — l.~862
- **MÍMICOS** — id: `MÍMICOS` — [OK] — mudam de forma, Ciber-Resistência, Forasteiro (Menor), Sem Órgãos Vitais — l.~889
- **MINERADORES GENÉTICOS** — id: `MINERADORES GENÉTICOS` — [OK] — geneticamente adaptados a trabalho pesado, Dependência Atmosférica, Em Forma, Forte — l.~910
- **ORÁCULOS** — id: `ORÁCULOS` — [OK] — poderes psíquicos inatos, Frágil, Noção do Perigo, Telepatia — l.~928
- **POSSESSORES** — id: `POSSESSORES` — [OK] — parasitas/energia que habitam hospedeiros, Casca, Comunitário, Forasteiro (Menor) — l.~943
- **QUADROIDES** — id: `QUADROIDES` — [OK] — quatro braços, Ação Adicional, Frágil, Membros Extras, Sensível — l.~984
- **ROBÔS** — id: `ROBÔS` — [OK] — Circuitos de Asimov (Pacifista Maior), Programado (Maior), habilidade Robô — l.~1008
- **SERES SINTÉTICOS** — id: `SERES SINTÉTICOS` — [OK] — quase-biológicos, Espacial, Imune a Doenças e Venenos, Não Pode Curar, Programado — l.~1032
- **SOLDADOS GENÉTICOS** — id: `SOLDADOS GENÉTICOS` — [OK] — Nervos de Aço, Reflexos de Combate, Resistência +1, Sem Escrúpulos (Maior), Treinados para a Guerra — l.~1051
- **VAZIOS** — id: `VAZIOS` — [OK] — criaturas de energia, Espacial, Forma de Energia, Incapaz de Falar, Sem Sangue — l.~1077
- **YETIS** — id: `YETIS` — [OK] — Forte, Fraqueza Ambiental (Calor), Grande, Resistência Ambiental (Frio), Tamanho +2 — l.~1120
- **AURAX** — id: `AURAX` — [OK] — centaux com Baixa Tecnologia (Maior) e Forte — l.~1150
- **DEADERS (PARASTEEN)** — id: `DEADERS (PARASTEEN)` — [OK] — parasita em cadáver, Aparar Baixo, Calculista, Desajeitado, Fraco, Lento, Morto-Vivo — l.~1165
- **KALIANOS** — id: `KALIANOS` — [OK] — quadroides de quatro braços — l.~1204
- **SERRANOS** — id: `SERRANOS` — [OK] — precognitivos, Aparar +2, Fraco, Noção de Perigo — l.~1211

## Vantagens Novas (Capítulo 1, l.~1509-2011)

Todas as 26 vantagens do "Sumário de Vantagens" do capítulo (l.1874-2011) estão em
`vantagens.json` com `livros: ["SCI_FI"]`.

- **CAPITÃO** — id: `capitao` — [OK] — começa com nave grande o bastante para o grupo — l.~1514
- **EQUIPADO** — id: `equipado` — [OK] — 10x os fundos iniciais em posses — l.~1531
- **HABITANTE DE GRAVIDADE INTENSA/SUPERINTENSA** — id: `habitante_gravidade_intensa` — [OK] — Força como dado maior para Sobrecarga/Força Mínima — l.~1544
- **TERRENO FAVORITO (Sci-Fi)** — id: `terreno_favorito` — [OK] — rerrolagem de Sobrevivência/Perceber + carta de ação extra no terreno escolhido — l.~1554
- **GERENCIADOR DE MUNIÇÃO** — id: `gerenciador_municao` — [OK] — reduz à metade balas gastas por CdT — l.~1575
- **OPORTUNISTA** — id: `oportunista` — [OK] — +4 (em vez de +2) ao sacar Curinga — l.~1594
- **SAQUE RÁPIDO (Aprimorado)** — id: `saque_rapido` — [OK] — compra 2 cartas ao gastar Bene por carta de ação extra — l.~1600
- **LÍDER DE EQUIPE** — id: `lider_equipe` — [OK] — aliados no Raio de Comando trocam Benes como Elo Comum — l.~1614
- **PODER FAVORITO** — id: `poder_favorito` — [OK] — ignora até 2 pontos de penalidade num poder escolhido — l.~1626
- **PODERES MÍSTICOS** — id: `poderes_misticos` — [OK] — pacotes Guerreiro Estelar/Telepata, 10 PP automáticos — l.~1637
- **ADAPTAÇÃO ATMOSFÉRICA** — id: `adaptacao_atmosferica` — [OK] — l.~1678
- **ADAPTAÇÃO GRAVITACIONAL** — id: `adaptacao_gravitacional` — [OK] — l.~1687
- **ATAQUE FURTIVO** — id: `ataque_furtivo` — [OK] — l.~1695
- **ATAQUE FURTIVO APRIMORADO** — id: `ataque_furtivo_aprimorado` — [OK] — l.~1703
- **CONTROLE FINO** — id: `controle_fino` — [OK] — l.~1711
- **EXOCIENTISTA** — id: `exocientista` — [OK] — l.~1718
- **FITA ADESIVA E CHICLETE** — id: `fita_adesiva_chiclete` — [OK] — l.~1727
- **MANOBRAS EVASIVAS** — id: `manobras_evasivas` — [OK] — l.~1736
- **HACKER SOB PRESSÃO** — id: `hacker_sob_pressao` — [OK] — l.~1749
- **PILOTO ATIRADOR** — id: `piloto_atirador` — [OK] — l.~1762
- **REFLEXOS APRIMORADOS** — id: `reflexos_aprimorados` — [OK] — l.~1770
- **TIRO PRECISO** — id: `tiro_preciso` — [OK] — l.~1785
- **ENGANADOR** — id: `enganador` — [OK] — l.~1793
- **IMORTAL** — id: `imortal` — [OK] — Lendária — l.~1801
- **MILAGREIRO** — id: `milagreiro` — [OK] — Lendária — l.~1810
- **SALVADOR DO UNIVERSO** — id: `salvador_universo` — [OK] — Lendária — l.~1825

### [CONFERIR] Vantagens sem correspondência encontrada neste texto

Estas 3 vantagens estão em `vantagens.json` com `livros: ["SCI_FI"]`, mas uma busca
por seus nomes completos em `docs/swade_scifi` **não retornou nenhuma ocorrência** —
vale confirmar contra o livro físico/PDF se pertencem mesmo a este Compêndio (podem
ser de outra edição/suplemento e estar mistagueadas):

- **JOCKEY DE MECHA** — id: `jockey_mecha` — [CONFERIR] — usa Agilidade pessoal em vez da do Mecha nas manobras
- **ENGENHEIRO ESPACIAL** — id: `engenheiro_espacial` — [CONFERIR] — +2 Consertar naves estelares
- **ADAPTADO À GRAVIDADE ZERO** — id: `gravidade_zero` — [CONFERIR] — sem enjoo espacial, Movimentação normal em zero-G

## Antecedentes Arcanos de Ficção Científica (Capítulo 11, l.~13029-14113)

Onze Antecedentes Arcanos, cada um com Requisitos/Perícia Arcana/Poderes
Iniciais/Pontos de Poder/Poderes Disponíveis/habilidades especiais próprias, todos em
`vantagens.json` (categoria `ANTECEDENTE`, `grupoId: antecedente_arcano`) com um par
de vantagens exclusivas cada (categoria `PODER`). **Todas as 11 listas
`poderes_permitidos` foram lidas linha a linha contra o livro e batem 1:1 em
quantidade e conteúdo com a lista "PODERES DISPONÍVEIS" impressa** (a dúvida deixada
pela análise anterior está resolvida: não há gap aqui) — única ressalva: no Transmorfo
o livro restringe a "velocidade (mas não morosidade)" e o dado usa o poder combinado
`morosidade_velocidade` sem essa restrição de sentido único (ver [CONFERIR] abaixo).

- **CAPELÃO** — id: `antecedente_arcano_capelao` — [OK] — Fé (Espírito), 10 PP, Misericórdia, Voto (Maior), variante psiônica troca Espírito→Astúcia/Fé→Psiônicos; 26 poderes disponíveis conferem 1:1 — l.~13072
  - Vantagens: **LITANIA DA FÚRIA** `litania_da_furia` [OK] l.~13136 · **REPROVAÇÃO** `reprovacao` [OK] l.~13145
- **CAVALEIRO ESTELAR** — id: `antecedente_arcano_cavaleiro_estelar` — [OK] — Foco (Espírito), 10 PP, Arma Predileta automática (espada de energia), Interferência de Armadura, Código de Honra; 16 poderes conferem 1:1 — l.~13163
  - Vantagens: **APARAR LASER** `aparar_laser` [OK] l.~13209 · **REFLETIR LASER** `refletir_laser` [OK] l.~13224 · **REFLETIR LASER APRIMORADO** `refletir_laser_aprimorado` [OK] l.~13239 · **RENEGADO ESTELAR** `renegado_estelar` [OK] l.~13245 · **MAGO ESTELAR** `mago_estelar` [OK] l.~13264 · **ASCENSÃO ESTELAR** `ascensao_estelar` [OK] l.~13280
- **CONTROLADOR DE LUZ SÓLIDA** — id: `antecedente_arcano_luz_solida` — [OK] — Ciência (Astúcia), 15 PP, Encapsulado (Absorção via PP), Dependente de Tecnologia, Falha Crítica drena PP; 13 poderes conferem 1:1 — l.~13300
  - Vantagens: **MELHORIA DE EQUIPAMENTO** `melhoria_de_equipamento` [OK] l.~13348 · **INFUNDIDO** `infundido` [OK] l.~13356
- **CRONOMANTE** — id: `antecedente_arcano_cronomante` — [OK] — Foco (Espírito), 15 PP, tabela Efeito Borboleta em Falha Crítica, Dotado; 15 poderes conferem 1:1 — l.~13365
  - Vantagens: **PREMONIÇÃO** `premonicao` [OK] l.~13465 · **REORGANIZAR TEMPO** `reorganizar_tempo` [OK] l.~13485 · **MESTRE DO TEMPO** `mestre_do_tempo` [OK] l.~13506
- **DOBRADOR** — id: `antecedente_arcano_dobrador` — [OK] — Foco (Espírito), 15 PP, Dotado, Astronavegação +2, Hiperespaço (teleporte grátis 12"), Falha Crítica abre buraco negro; 16 poderes conferem 1:1 — l.~13522
  - Vantagens: **CANALIZAR DOBRA** `canalizar_dobra` [OK] l.~13571 · **SURTO DE DOBRA** `surto_de_dobra` [OK] l.~13585
- **GRAVCON** — id: `antecedente_arcano_gravcon` — [OK] — Foco (Espírito), 10 PP, Gerenciamento de Massa; 16 poderes conferem 1:1 (inclui buraco_negro→`gravidade` mapeado corretamente) — l.~13597
  - Vantagens: **MESTRE DA GRAVIDADE** `mestre_da_gravidade` [OK] l.~13646 · **LEVANTADOR** `levantador` [OK] l.~13654
- **MÍSTICO** — id: `antecedente_arcano_mistico` — [OK] — Foco (Espírito), 10 PP, Dotado, Ressonância Harmônica, Orientação Cósmica; 24 poderes conferem 1:1 — l.~13689
  - Vantagens: **HARMONIZADO** `harmonizado` [OK] l.~13749 · **SINFONIA CELESTIAL** `sinfonia_celestial` [OK] l.~13756
- **PASTOR** — id: `antecedente_arcano_pastor` — [OK] — Fé (Espírito), 10 PP, Milagres, Aura de Coragem, Voto (Maior); 30 poderes conferem 1:1 — l.~13780
  - Vantagens: **SOZINHO CONTRA OS LOBOS** `sozinho_contra_os_lobos` [OK] l.~13836 · **MISERICÓRDIA** `misericordia_pastor` [OK] l.~13847
- **PSIONISTA** — id: `antecedente_arcano_psionista` — [OK] — Psiônicos (Astúcia), 10 PP, Psiônicos (acesso a vantagens dedicadas); 40 poderes conferem 1:1 — l.~13876
  - Vantagens: **ESCANEAR** `escanear_psionista` [OK] l.~13912 · **TORRE DE DETERMINAÇÃO** `torre_de_determinacao_psionista` [OK] l.~13930
- **TECNOMANTE** — id: `antecedente_arcano_tecnomante` — [OK] — Ciência Estranha (Astúcia), 10 PP, Interface +2 Eletrônica/Hackear; 31 poderes conferem 1:1 — l.~13950
  - Vantagens: **DISJUNTOR** `disjuntor` [OK] l.~13983 · **DRONES** `drones` [OK] l.~13994
- **TRANSMORFO** — id: `antecedente_arcano_transmorfo` — [OK] — Foco (Espírito), 15 PP, Foco Interno (poderes só Pessoal), Regeneração Lenta, Dotado; 12 poderes conferem 1:1 em quantidade — l.~14038
  - Vantagens: **GELATINOSO** `gelatinoso` [OK] l.~14080 · **TRANSFORMAR** `transformar` [OK] l.~14096

### [CONFERIR] Nuance do Transmorfo

O livro diz que o Transmorfo só pode manifestar "velocidade (mas não morosidade)"
(l.14058-14059) dentro do poder combinado morosidade/velocidade — o dado
`poderes_permitidos` do antecedente lista `morosidade_velocidade` sem indicar essa
restrição de sentido único; hoje isso provavelmente só é reforçado na descrição em
texto, não na estrutura de dados.

## Complicações Novas (Capítulo 1, l.~1237-1485, + Cibernéticos l.~7963-7980)

Todas as 14 complicações do "Sumário de Complicações" (l.1843-1873) e as 3
complicações de cibernéticos estão em `complicacoes.json` com `livros: ["SCI_FI"]`.

- **ALTRUÍSTA** (Menor/Maior) — id: `altruista` — [OK] — coloca outros acima de si — l.~1237
- **APAIXONADO** (Menor) — id: `apaixonado` — [OK] — -2 para resistir a Desafios de Atraente/Muito Atraente — l.~1251
- **ALTA/BAIXA TECNOLOGIA** (Menor/Maior) — id: `alta_baixa_tecnologia` — [OK] — -2/-4 em Eletrônica e Hackear fora do próprio nível tecnológico — l.~1265
- **DEPENDÊNCIA ATMOSFÉRICA** (Menor/Maior) — id: `dependencia_atmosferica` — [OK] — l.~1283
- **DEPENDENTE** (Menor/Maior) — id: `dependente` — [OK] — l.~1297
- **DOENÇA DE GRAVIDADE ZERO** (Menor) — id: `doenca_gravidade_zero` — [OK] — l.~1327
- **DOENÇA SUPERLUMÍNICA** (Menor/Maior) — id: `doenca_superluminica` — [OK] — l.~1342
- **ENFERMIDADE** (Menor/Maior) — id: `enfermidade` — [OK] — chamada "Doença" no Sumário, "Enfermidade" no corpo do texto (mesma entrada) — l.~1360
- **EX-DRONE** (Maior) — id: `ex_drone` — [OK] — l.~1397
- **HABITANTE DE GRAVIDADE ZERO/BAIXA** (Maior) — id: `habitante_gravidade_zero_baixa` — [OK] — l.~1409
- **ÍMÃ DE BALAS** (Menor) — id: `ima_de_balas` — [OK] — l.~1422
- **ÍMÃ DE PROBLEMAS** (Menor/Maior) — id: `ima_de_problemas` — [OK] — l.~1429
- **PROGRAMADO** (Maior) — id: `programado` — [OK] — l.~1447
- **REBELDE** (Menor) — id: `rebelde` — [OK] — l.~1469
- **CIBER-RESISTÊNCIA** (Menor) — id: `ciber_resistencia` — [OK] — -2 no Limite de Tensão — l.~7963
- **CIBERSENSIBILIDADE** (Menor) — id: `cibersensibilidade` — [OK] — -2 na instalação de cibernéticos — l.~7967
- **EFEITO COLATERAL DE CIBERNÉTICOS** (Maior) — id: `efeito_colateral_de_ciberneticos` — [OK] — resultado permanente na tabela de efeitos colaterais — l.~7973

## Perícias

O capítulo 1 não introduz nenhuma perícia nova (nenhuma seção "Novas Perícias" no
livro). As duas perícias arcanas usadas pelos Antecedentes Arcanos deste livro —
**Ciência Estranha** (Tecnomante) e **Psiônicos** (Psionista) — já existem em
`pericias.json` com `SCI_FI` na lista de `livros`. Nenhuma ação necessária.

## Equipamento (Capítulo 2, l.~2141-4184 — exclui Postos Avançados, l.~4185-4897)

Cobertura auditada por categoria (não item a item, dado o volume — centenas de
entradas). Todas as categorias abaixo têm grupo correspondente em `equipamentos.json`
com `livros: ["SCI_FI"]` e contagem de itens compatível com as tabelas do livro;
duas amostras foram conferidas linha a linha e bateram exatamente: **Ogivas /
Modificadores de Munição** (10 no livro l.~3358-3409, 10 no json) e **Armadura
Energizada / Armaduras Padrão** (9 trajes prontos no livro l.~8437-8524, 10 no json,
incluindo 1 extra "Traje de Exploração de Águas Profundas").

- **Equipamento de Aventura/Vestuário/Eletrônicos/Comida/Meios de Locomoção/Medicinais/Itens de Defesa Pessoal** — [OK] — `Equipamento Geral`, `Eletrônicos`, `Consumíveis`, `Medicinais`, `Meios de Locomoção`, `Itens de Defesa Pessoal` em equipamentos.json — l.~2222-2237 e seguintes
- **Trajes Armadurados / Armaduras de Energia / Trajes Espaciais** (armaduras vestíveis comuns, distintas de Armadura Energizada do cap.5) — [OK] — `Armaduras/Trajes Armadurados`, `Armaduras/Armaduras de Energia`, `Armaduras/Trajes Espaciais` — l.~2933-2970
- **Glossário de Qualidades de Arma** (Arma de Energia, Arma de Plasma, Cauterizante, Dilacerante, Vibro etc.) — [OK] — refletido como `observacoes`/propriedades nos itens de arma — l.~2920-3357
- **Ogivas** (10 modificadores de munição) — id sugerido: n/a — [OK] confirmado 10=10 — l.~3358
- **Armas Corpo a Corpo** (Correntes, PEM, Energia, Moleculares, Psíquicas, Repulsoras, Atordoantes, Vibro) — [OK] — `Armas Corpo a Corpo/Geral` (22 itens) — l.~3411-3427
- **Armas de Longa Distância** (Blasters, Desintegradores, Estilhaço, Lança-Chamas, Flechetes, Granadas, Girofoguetes, Lasers, Lança-Mísseis, Plasma, Psíquicas, Pulso, Lança-Foguetes, Lança-Projéteis, Atordoantes) — [OK] — `Armas de Longa Distância/*` (14 subgrupos, ~58 itens) — l.~3443-1978(rel.)
- **Armas Veiculares/Montadas** (Canhões Automáticos, Bombas, Canhões, Canhões Gravitacionais, Lança-Granadas, Canhões de Íon, Condutores de Massa, Minas, Mísseis, Feixe/Canhões de Partículas, Torpedos, Raio Trator) — [OK] — `Armas Veiculares/Geral` (58 itens) — l.~3739(rel. dentro de 2077+)-2071(rel.)
- **Ciberdeck — Programas** (Acesso Remoto, Guardião, Assistente, Redirecionar, Embaralhador, Spammer, Vírus, Capítulo 3 l.~5544-5583) — id sugerido: `programa_ciberdeck_*` — [CONFERIR] — o item "Ciberdeck" existe em `Eletrônicos/Computadores e Vigilância`, mas não há confirmação de que os 7 programas-upgrade estejam modelados como sub-itens/mods compráveis separados

## Cibernéticos (Capítulo 4, l.~7879-8308)

- **Sistema de instalação/remoção, tabela de Efeitos Colaterais (d20), Ocultando** — [OK] — regras de mestre/mecânica, refletidas no fluxo de compra do app — l.~7886-7980
- **Implantes** (Corpo 20, Defensivo 11, Ofensivo 8, Locomoção 4) — [OK] — `scifi_ciberneticos.json` tem 43 implantes cobrindo as 4 categorias — l.~8025-8307
- **CIBERTOLERÂNCIA** — id: `cibertolerancia` — [OK] — +2 Limite de Tensão — l.~7985
- **CIBERSAMURAI** — id: `cibersamurai` — [OK] — +4 Limite de Tensão — l.~7990
- **CIBORGUE** — id: `ciborgue` — [OK] — +4 Limite de Tensão, $20K em implantes, não faz Cura Natural — l.~7995

## Armadura Energizada (Capítulo 5, l.~8308-8871)

- **Estruturas / Qualidades Negativas / Sistemas Centrais/Defensivos/Locomoção/Ofensivos** — [OK] — `Armadura Energizada/*` em equipamentos.json (3 estruturas + 8+3+13+7+6 = 37 mods) — l.~8460-8746
- **9 trajes prontos** (Carregador de Carga, Traje de Resgate, Traje de Ataque, Traje de Comando, Traje de Suporte de Ataque, Traje de Voo, Traje Passolargo, Traje de Batedor, Traje Zero-G) — [OK] — todos presentes em `Armadura Energizada/Armaduras Padrão` — l.~8707-8830

## Robôs (Capítulo 6, l.~8871-9536)

A ancestralidade jogável **ROBÔS** já está coberta acima. O restante do capítulo é um
sistema de **construção de robô** (companheiro/propriedade compráveis pelo
personagem, mesmo molde de Mechas/Veículos) que **não tem catálogo dedicado no app**
— não há `scifi_robos.json` equivalente a `scifi_mechas.json`.

**[FORA DE ESCOPO — decisão registrada em 2026-08-31]** Os 7 "Robôs Padrão" abaixo
são blocos de estatística completos (Atributos, Perícias, Movimentação/Aparar/
Resistência, Complicações, Vantagens, Habilidades Especiais) para um robô
**contratável como aliado/propriedade**, não uma opção de build do próprio
personagem — mesma categoria de conteúdo que bestiário, fora do escopo do app.
"Construção de Robôs" é o sistema de montagem (Estruturas + dezenas de Mods com
custo em pontos) por trás deles, equivalente a "criação de item" — também fora de
escopo. Nenhum dos dois foi implementado.

- **Construção de Robôs** (Estruturas Tam. -4 a 3 + Mods: Qualidades Negativas, Sistemas Centrais, Sistemas Defensivos, Locomoção, Sistemas Ofensivos) — [FORA DE ESCOPO] — sistema de montagem, não catálogo de personagem — l.~8953-9282
- **ROBÔ DIPLOMATA** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9296
- **ROBÔ ENGENHEIRO** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9316
- **ROBÔ DE GUERRA** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9345
- **ROBÔ MÉDICO** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9373
- **ROBÔ DE PRAZER** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9389
- **ROBÔ SENTINELA** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9415
- **ROBÔ TRABALHADOR** — [FORA DE ESCOPO] — bloco de stats de aliado contratável — l.~9457

## Veículos (Capítulo 8, l.~11175-12035)

O app cobre veículos genéricos do Savage Worlds básico reaproveitados no cenário
sci-fi (`Veículos/Terrestres Civis`, `Militares (WWII)`, `Militares Modernos e Sci-Fi`,
`Aeronaves`, `Embarcações` — 47 itens). **[OK — resolvido em 2026-08-31]** Os 21
veículos prontos exclusivos deste Compêndio (20 listados abaixo mais um
"Submarino de Ataque" achado ao conferir o texto completo, que não constava desta
lista original) foram adicionados em `app/src/main/assets/equipamentos.json`,
categoria `Veículos / Customizados (Sci-Fi)`, tag `SCI_FI`. Campos
Tamanho/Manobrabilidade/Velocidade Máx./Resistência/Tripulação mapeados para os
campos estruturados já existentes em `EquipamentoItem`; Ferimentos/Energia/Mods/
Locomoção/Armas (sem campo estruturado próprio) ficaram em `observacoes` como
texto. **O sistema de construção (Qualidades Negativas, Sistemas Centrais/
Defensivos, Locomoção e Energia, Sistemas Ofensivos) continua fora de escopo** —
é montagem por Mods com custo em pontos, equivalente a "criação de item", mesma
decisão dos Robôs acima.

- **Sistema de Veículos Customizados** (Estruturas + Mods) — [FORA DE ESCOPO] — sistema de montagem, não catálogo de personagem — l.~11316-12002
- **MOTO FLUTUANTE** (Classe I) — id: `veiculo_moto_flutuante_scifi` — [OK] — l.~11711
- **ROVER DE EXPLORAÇÃO** (Classe I) — id: `veiculo_rover_de_exploracao_scifi` — [OK] — l.~11718
- **LIMUSINE FLUTUANTE** (Classe I) — id: `veiculo_limusine_flutuante_scifi` — [OK] — l.~11726
- **CAMINHÃO FLUTUANTE** (Classe I) — id: `veiculo_caminhao_flutuante_scifi` — [OK] — l.~11732
- **RASTEJADOR DE EXPLORAÇÃO** (Classe II) — id: `veiculo_rastejador_de_exploracao_scifi` — [OK] — l.~11738
- **MINISSUBMARINO** (Classe I) — id: `veiculo_minissubmarino_scifi` — [OK] — l.~11749
- **CARRO ESPORTIVO DE DECOLAGEM E POUSO VERTICAIS** (Classe I) — id: `veiculo_carro_esportivo_decolagem_pouso_verticais_scifi` — [OK] — l.~11757
- **MOTO FLUTUANTE DE COMBATE** (Classe I) — id: `veiculo_moto_flutuante_de_combate_scifi` — [OK] — l.~11768
- **ESQUIFE FLUTUANTE** (Classe I) — id: `veiculo_esquife_flutuante_scifi` — [OK] — l.~11780
- **JIPE FLUTUANTE** (Classe II) — id: `veiculo_jipe_flutuante_scifi` — [OK] — l.~11791
- **BLINDADO** (Classe II) — id: `veiculo_blindado_scifi` — [OK] — l.~11802
- **TANQUE FLUTUANTE** (Classe III) — id: `veiculo_tanque_flutuante_scifi` — [OK] — l.~11814
- **TANQUE DE COMBATE** (Classe IV) — id: `veiculo_tanque_de_combate_scifi` — [OK] — l.~11827
- **BARCO DE PATRULHA** (Classe II) — id: `veiculo_barco_de_patrulha_scifi` — [OK] — l.~11842
- **DESTRÓIER NAVAL** (Classe VI) — id: `veiculo_destroier_naval_scifi` — [OK] — l.~11852
- **SUBMARINO DE ATAQUE** (Classe IV) — id: `veiculo_submarino_de_ataque_scifi` — [OK] — achado extra, não constava na varredura original — l.~11870 (aprox.)
- **ENCOURAÇADO** (Classe VII) — id: `veiculo_encouracado_scifi` — [OK] — l.~11885
- **TRANSPORTADOR LEVE** (Classe IV) — id: `veiculo_transportador_leve_scifi` — [OK] — l.~11901
- **AERONAVE DE PATRULHA DE DECOLAGEM VERTICAL** (Classe II) — id: `veiculo_aeronave_de_patrulha_decolagem_vertical_scifi` — [OK] — l.~11914
- **ATAQUE RÁPIDO DE DECOLAGEM VERTICAL** (Classe II) — id: `veiculo_ataque_rapido_decolagem_vertical_scifi` — [OK] — l.~11946
- **CAÇA FURTIVO ATMOSFÉRICO** (Classe I) — id: `veiculo_caca_furtivo_atmosferico_scifi` — [OK] — l.~11961

## Mechas (Capítulo 9, l.~12035-12819)

Opção de build de personagem (piloto de mecha), com sistema próprio de construção —
já coberto de forma dedicada e completa no app.

- **Sistema de Mechas Customizados** (Estruturas Grande/Enorme/Colossal + Mods: Qualidades Negativas, Sistemas Centrais/Defensivos, Locomoção, Sistemas Ofensivos) — [OK] — `scifi_mechas.json` (3 estruturas-base) + `scifi_mecha_mods.json` (49 modificadores) — l.~12291-12660
- **7 mechas prontas** (Mecha de Apoio de Infantaria, Batedor Leve, Mecha Variável Caça, Lutador Médio, Suporte de Fogo Pesado, Mecha de Ataque, Mecha de Infantaria) — [OK] — todas em `scifi_mechas.json` — l.~12661-12742
- **Armas de mecha** (18 armas) — [OK] — `scifi_mecha_weapons.json` — referenciadas ao longo do cap.9

## Poderes (Capítulo 11, "Novos Poderes", l.~14123-14839)

Os 11 pacotes de Antecedente Arcano estão detalhados acima. Esta seção cobre as
mecânicas dos poderes novos/atualizados em si, em `poderes.json`.

- **13 poderes inéditos deste livro** (sem marcador "F" de atualização): Aperto Telecinético, Buraco Negro, Contra-Ataque Mental, Controlar Máquina, Criar Item, Curto-Circuito, Fantasma na Máquina, Gravidade, Leitura de Objeto, Localizar, Parar o Tempo, Previsão, Trancar/Destrancar — [OK] existem em `poderes.json`, mas ver ressalva de tag abaixo — l.~14133-14744
- **5 poderes "F" (atualização de poder pré-existente do SWADE básico)**: Conjurar Aliado, Mudança de Forma, Telecinese, Teleporte, Vidência — [OK] existem, mesma ressalva de tag — l.~14020-14839

### [CONFERIR] Tag de livro dos poderes reaproveitados entre compêndios

`aperto_telecinetico`, `buraco_negro`, `contra_ataque_mental`, `controlar_maquina`,
`criar_item`, `curto_circuito`, `fantasma_na_maquina`, `gravidade` e `previsao` estão
corretamente com `livros: ["SCI_FI"]`. Mas **`conjurar_aliado`, `mudanca_de_forma`,
`telecinese` e `teleporte` estão tagueados só `["PATHFINDER"]`**, e **`leitura_de_objeto`,
`localizar`, `parar_o_tempo`, `trancar_destrancar` só `["FANTASIA"]`** — mesmo sendo
poderes deste Compêndio (a maioria marcada "F" no livro, ou seja, reaproveitados
entre vários Compêndios com o mesmo texto). Isso não quebra os Antecedentes Arcanos
(cujas listas `poderes_permitidos` apontam para o id certo independente da tag), mas
qualquer filtro de "poderes disponíveis no livro Sci-Fi" que dependa só do campo
`livros` do poder (em vez de `poderes_permitidos` do Antecedente) vai esconder esses
9 poderes.

## Artefatos / Relíquias (Capítulo 12, l.~14839-15750)

**[OK — resolvido em 2026-08-31].** A entrada "Itens Especiais/Relíquias e
Artefatos" já existente em `equipamentos.json` (7 itens: Cinzas de Ashur, Estátua
da Fera, Ícone da Ruína, Matadora de Titãs, A Muralha, Tambores de Oon) continua
`livros: ["FANTASIA"]` e pertence ao Compêndio de Fantasia — as relíquias deste
Compêndio de Ficção Científica foram adicionadas como uma categoria SEPARADA,
mesmo `tipo`/`subtipo`, tag `livros: ["SCI_FI"]` (o formato do catálogo já suporta
a mesma categoria aparecer mais de uma vez com tags de livro diferentes). Total: 1
item de regras (`reliquias_e_artefatos_scifi_regras`, explica que relíquias só
aparecem como prêmio de aventura via Tabela de Prêmios/Tabela de Relíquias/Tabela
de Relíquias Apex — as 3 tabelas viraram texto dentro deste item, não catálogo à
parte) + 25 relíquias nomeadas (19 da Tabela de Relíquias d20 + 6 da Tabela de
Relíquias Apex d6 — 1 a mais do que os 24 desta lista original: "Fragmento de
Ka'han", que não tinha sido catalogado na varredura inicial).

- **Tabela de Prêmios** (Saque/Salvamento/Tesouro/Resgate de um Rei/Relíquia/Grande Prêmio por carta) — [OK] — texto incorporado ao item de regras — l.~14869
- **Tabela de Relíquias (d20) e Relíquias Apex (d6)** — [OK] — texto incorporado ao item de regras — l.~14952, l.~14975
- **ADAPTADOR DE BURACO DE MINHOCA** — id: `relic_adaptador_buraco_minhoca_scifi` — [OK] — l.~14924
- **AMPLIFICADOR DE COMBATE** — id: `relic_amplificador_de_combate_scifi` — [OK] — l.~14944
- **ANIQUILADOR UNIVERSAL** — id: `relic_aniquilador_universal_scifi` — [OK] — l.~14996
- **CIDADE FRACTAL** — id: `relic_cidade_fractal_scifi` — [OK] — l.~15058
- **CONCHA CANÓPICA** — id: `relic_concha_canopica_scifi` — [OK] — l.~15094
- **CONSTRUTO ALFA** — id: `relic_construto_alfa_scifi` — [OK] — l.~15121
- **DIADEMA DO SOBERANO** — id: `relic_diadema_do_soberano_scifi` — [OK] — l.~15159
- **DRIVE ZERO** — id: `relic_drive_zero_scifi` — [OK] — l.~15179
- **EMISSOR DE MEMÓRIAS** — id: `relic_emissor_de_memorias_scifi` — [OK] — l.~15207
- **EMPUNHADURA DO JULGAMENTO** — id: `relic_empunhadura_do_julgamento_scifi` — [OK] — l.~15233
- **FAROL CÓSMICO** — id: `relic_farol_cosmico_scifi` — [OK] — l.~15258
- **FRAGMENTO DE KA'HAN** — id: `relic_fragmento_de_kahan_scifi` — [OK] — achado extra (Tabela Apex), não constava na varredura original — l.~15275 (aprox.)
- **JOIA DO VÓRTICE ESTELAR** — id: `relic_joia_do_vortice_estelar_scifi` — [OK] — l.~15324
- **LABIRINTO TRANSDIMENSIONAL** — id: `relic_labirinto_transdimensional_scifi` — [OK] — l.~15345
- **MALETA DE MATÉRIA EXÓTICA** — id: `relic_maleta_de_materia_exotica_scifi` — [OK] — l.~15385
- **MAPA DA HIPERESTRADA** — id: `relic_mapa_da_hiperestrada_scifi` — [OK] — l.~15413
- **MATRIZ DE RECONSTRUÇÃO** — id: `relic_matriz_de_reconstrucao_scifi` — [OK] — l.~15437
- **MUTAGÊNICO ZETA** — id: `relic_mutagenico_zeta_scifi` — [OK] — l.~15464
- **NODO DE CURA** — id: `relic_nodo_de_cura_scifi` — [OK] — l.~15499
- **OBELISCO DE NÊUTRON** — id: `relic_obelisco_de_neutron_scifi` — [OK] — l.~15535
- **PROJETOR HARMÔNICO** — id: `relic_projetor_harmonico_scifi` — [OK] — l.~15549
- **PROTOMASSA** — id: `relic_protomassa_scifi` — [OK] — l.~15578
- **SENSOR DE DIFUSÃO** — id: `relic_sensor_de_difusao_scifi` — [OK] — l.~15605
- **TETRACUBO** — id: `relic_tetracubo_scifi` — [OK] — l.~15631
- **VINGADORA** — id: `relic_vingadora_scifi` — [OK] — l.~15657

## Fora de escopo

- **Capítulo 3 — Regras de Ambientação** (l.4898-7879): regras situacionais de mesa
  (Atmosfera, Gravidade, Pressão, Hackear/Netrunning, Logística, Pilhagem e Comércio,
  Combate Veicular Tático) — mecânica de sessão, não opção de personagem. Os termos
  que viram Complicações (ex. Dependência Atmosférica) já foram capturados acima.
- **Capítulo 2 — Postos Avançados** (l.~4185-4897, dentro do capítulo de Equipamento):
  construção de base/posto avançado — sistema de Mestre.
- **Capítulo 6 — "Robôs Padrão"** (7 templates, marcados acima como [FORA DE ESCOPO]):
  são robôs prontos para contratar/comprar como PNJ/companheiro, mais perto de um
  "catálogo de aliados" do que de uma opção de ficha — decisão registrada em
  2026-08-31, não implementados.
- **Capítulo 7 — Espaçonaves** (l.9536-11175): sistema de nave em escala de campanha
  (Perfil Padrão, Combate, Perseguições/Dogfight, Colisões, Astronavegação, Viagem
  Superlumínica, Escâneres, além do catálogo "Embarcações Civis/Militares", que usa
  o mesmo formato de ficha coletiva de tripulação/energia da nave) — gerenciamento de
  nave como entidade coletiva, não veículo individual comprável; explicitamente fora
  de escopo por instrução da tarefa.
- **Capítulo 10 — Criador de Mundos** (l.12819-13015): gerador de cenário para Mestre.
- **Capítulo 13 — Bestiário** (l.15750-~22163): "Criaturas do Cosmos", "Inimigos dos
  Planetas Civilizados", "Cidadãos Galáticos" — blocos de estatística de NPC/inimigo,
  não opções de personagem jogável.
- **Apêndice A — Impérios** (l.~22164-fim): frotas e facções NPC.
