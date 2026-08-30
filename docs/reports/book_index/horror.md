# Índice de Conteúdo — Compêndio de Horror (SWADE)

Índice granular do conteúdo de **criação de personagem** do Compêndio de Horror
(`docs/swade_horror`, ~17.317 linhas), cruzado entrada a entrada com os catálogos
JSON do app (`app/src/main/assets/*.json`, tag de livro `HORROR`). Complementa —
sem repetir — a análise prévia em
`docs/reports/scifi_horror_content_gap_analysis.md` (seção Horror), que já havia
mapeado o panorama geral; aqui cada entrada individual ganha localização por
linha e status próprio. Metodologia: `Grep` nos cabeçalhos de capítulo/seção do
livro-texto para localizar os trechos, `Read` só dos trechos relevantes, e
consulta pontual via `python3 -c "json.load(...)"` em cada catálogo JSON
filtrando por `"HORROR" in livros` (ou, para `horror_monstros.json` e as
Vantagens `MONSTRUOSAS`, por `requisitos.template`). Escopo: apenas conteúdo que
afeta a ficha do personagem jogador — ancestralidades/templates de monstro
heroico, vantagens, complicações, perícia nova, itens (incl. Itens Arcanos) e
poderes. Bestiário de antagonistas, conselhos de mestria e mecânicas de mesa
multi-personagem ficam listados em "Fora de escopo" ao final.

Status: **[OK]** presente e conferido no catálogo indicado — **[FALTA]** não
encontrado em nenhum catálogo, com id sugerido em `snake_case` — **[CONFERIR]**
presente no catálogo, mas com alguma divergência que merece checagem manual.

## Complicações Novas (Capítulo Um, p.8–10)

Todas em `complicacoes.json`, filtro `"HORROR" in livros`.

- **AMALDIÇOADO (Maior)** — id: `amaldicoado_horror` — [OK] — Poderes benéficos sofrem -2 quando o alvo é a personagem amaldiçoada; Falha Crítica atordoa quem conjurou. — linha ~352
- **AMOROSO (Menor)** — id: `amoroso` — [OK] — -2 para resistir a Desafios de personagens Atraentes/Muito Atraentes. — linha ~371
- **APETITE INCOMUM (Menor)** — id: `apetite_incomum` — [OK] — -2 em Persuadir com quem conhece os hábitos alimentares estranhos da personagem. — linha ~378
- **ASSUSTADO (Menor)** — id: `assustado` — [OK] — Testes de Medo extras a qualquer susto ou barulho repentino. — linha ~390
- **COMPONENTES MATERIAIS (Maior)** — id: `componentes_materiais_horror` — [OK] — Exclusiva de conjuradores; -4 em toda perícia arcana se ficar sem os materiais. — linha ~396
- **CONDENADO (Maior)** — id: `condenado_horror` — [OK] — -2 em rolagens de Absorção. — linha ~419
- **CORRUPÇÃO (Maior)** — id: `corrupcao_horror` — [OK] — Exclusiva de Antecedente Arcano; Falha Crítica na perícia arcana gera/agrava Complicações. — linha ~432
- **HEMOFÍLICO (Maior)** — id: `hemofilico` — [OK] — Sangramento contínuo após Ferimento não-Incapacitante. — linha ~455
- **HISTÉRICO (Menor/Maior)** — id: `histerico` — [OK] — Grito automático ao falhar teste de Medo. — linha ~469
- **IMÃ DE BALAS (Menor)** — id: `ima_de_balas` — [OK] — Atingido por disparos acidentais de Espectadores Inocentes. — linha ~483
- **PESSIMISTA (Menor)** — id: `pessimista` — [OK] — +2 no resultado de testes de Medo malsucedidos. — linha ~490
- **SONO PESADO (Menor)** — id: `sono_pesado` — [OK] — Penalidades para acordar/ficar acordado. — linha ~495
- **SUPERSTICIOSO (Menor)** — id: `supersticioso` — [OK] — -1 em Características se o foco/ritual pessoal for perturbado. — linha ~501
- **TERRORES NOTURNOS (Maior)** — id: `terrores_noturnos` — [OK] — -1 em rolagens de Espírito; atrapalha o sono de quem dorme perto. — linha ~509
- **VALENTÃO (Menor/Maior)** — id: `valentao` — [OK] — Menospreza ou parte para confronto físico com quem julga "diferente". — linha ~523
- **VÍTIMA (Maior)** — id: `vitima` — [OK] — Alvo preferencial em sorteios de alvo aleatório do Mestre. — linha ~532

## Nova Perícia (Capítulo Um, p.10)

- **ALQUIMIA (Astúcia)** — id: presente como `"nome": "Alquimia"` em `pericias.json` — [OK] — Perícia arcana de Alquimistas; também substitui Ciências para reações químicas e é usada para criar Itens Alquímicos. — linha ~544
- **OCULTISMO (referência)** — já cadastrada como perícia básica multi-livro (inclui `HORROR`) — [OK] — Conhecimento sobre o sobrenatural, usada em rituais, Proteções e Aprisionamentos. — não é conteúdo novo do livro, citada aqui por ser a perícia-chave do gênero.

## Vantagens Novas — Gerais (Capítulo Um, p.10–13)

Todas em `vantagens.json`, filtro `"HORROR" in livros`.

- **HUMOR ÁCIDO** — id: `humor_acido` (categoria `ANTECEDENTE`) — [OK] — Usa Provocar em vez de Espírito em testes de Medo; ampliação dá Suporte +1 a aliados. — linha ~563
- **IMPLACÁVEL** — id: `implacavel` — [OK] — Pode agir com uma ação e -2 mesmo em Estado Abalado. — linha ~606
- **VETERANO DO MUNDO SOMBRIO** — id: `veterano_do_mundo_sombrio` — [OK] — Começa Experiente com 4 Progressos, rolando na tabela de infortúnios do veterano. — linha ~579 (tabela) / ~617 (vantagem)
- **BRAVURA** (Liderança) — id: `bravura` — [OK] — Rerrolagem grátis de teste de Medo malsucedido para aliados no Raio de Comando. — linha ~639
- **CONJURADOR SILENCIOSO** (Poder) — id: `conjurador_silencioso_horror` — [OK] — Conjura sem falar (debaixo d'água, amordaçado etc). — linha ~648
- **PODER FAVORITO** (Poder) — id: `poder_favorito_horror` — [OK] — Ignora até 2 pontos de penalidade ao ativar um poder escolhido. — linha ~655
- **CAÇADOR DE MONSTROS** (Profissional) — id: `cacador_de_monstros` — [OK] — Imunidade a testes de Medo de um tipo de criatura; acumulável por tipo. — linha ~667
- **RAINHA/REI DO GRITO** (Profissional) — id: `rainha_rei_do_grito` — [OK] — Rerrola o 1º resultado na tabela de Efeitos de Medo (deve aceitar o 2º). — linha ~677
- **POÇO DE VIRTUDE** (Social) — id: `poco_de_virtude` — [OK] — Gasta um Bene por 5 Marcadores de Inspiração para distribuir a aliados. — linha ~687
- **VISÃO** (Estranha) — id: `visao` — [OK] — Visão premonitória 1x/sessão; usa o sistema de Sinais e Presságios (p.61). — linha ~721

## Antecedentes Arcanos (Capítulo Quatro, p.69–83)

Todos em `vantagens.json`, categoria `ANTECEDENTE`, filtro `"HORROR" in livros`.
Cada um traz lista própria de `PODERES DISPONÍVEIS` (conferida linha a linha
contra `poderes.json`, sem divergência encontrada).

- **ANTECEDENTE ARCANO (Alquimista)** — id: `antecedente_arcano_alquimista_horror` — [OK] — Perícia Alquimia; "conjura" poções/óleos/granadas com a Complicação Componentes Materiais. — linha ~4303
  - **DISCERNIMENTO** — id: `discernimento` — [OK] — Poderes de Duração 5 passam a Duração 8 em misturas. — linha ~4353
  - **QUÍMICO** — id: `quimico_horror` — [OK] — Misturas dadas a terceiros duram 1 semana em vez de 48h. — linha ~4359
- **ANTECEDENTE ARCANO (Bruxaria)** — id: `antecedente_arcano_bruxaria` — [OK] — Perícia Conjurar; ganha familiar; Complicações Componentes Materiais e Corrupção. — linha ~4364
  - **A HORA DA BRUXA** — id: `a_hora_da_bruxa` — [OK] — Sem Falhas Críticas e rerrolagem grátis de Absorção entre meia-noite e 1h. — linha ~4399
  - **PODER SOMBRIO** — id: `poder_sombrio` — [OK] — Gasta um Bene para conjurar qualquer poder da campanha como ritual, ignorando requisitos. — linha ~4436
- **ANTECEDENTE ARCANO (Clérigo)** — id: `antecedente_arcano_clerigo_horror` — [OK] — Perícia Fé; poderes iniciais fixos (cura, santuário); Voto (Maior). — linha ~4450
  - **AURA DE CORAGEM** — id: `aura_de_coragem` — [OK] — +1 em testes de Medo de aliados próximos; -1 na tabela de Efeitos de Medo. — linha ~4473
  - **MISERICÓRDIA** — id: `misericordia` — [OK] — Gasta 1 PP para remover Distraído/Vulnerável/Abalado de um aliado. — linha ~4477
- **ANTECEDENTE ARCANO (Corrompido)** — id: `antecedente_arcano_corrompido` — [OK] — Perícia Foco; acesso a Vantagens de qualquer Antecedente Arcano; Complicação Corrupção. — linha ~4487 — sem Vantagens exclusivas (usa as de outros Antecedentes).
- **ANTECEDENTE ARCANO (Demonologista)** — id: `antecedente_arcano_demonologista` — [OK] — Perícia Conjurar; invoca legionários/cães infernais/corcel demoníaco por Estágio. — linha ~4519
  - **ARMADURA INFERNAL** — id: `armadura_infernal` — [OK] — Armadura +2 acumulável, mas anula Furtividade. — linha ~4541
  - **FÚRIA INFERNAL** — id: `furia_infernal` — [OK] — +2 de dano em explosão/raio/rajada. — linha ~4547
- **ANTECEDENTE ARCANO (Investigador Psíquico)** — id: `antecedente_arcano_investigador_psiquico` — [OK] — Perícia Psiônicos. — linha ~4558
  - **ESCANEAR** — id: `escanear` — [OK] — Detecta mentes num raio de 10 quadros (ação limitada, fica Distraído). — linha ~4574
  - **TORRE DE DETERMINAÇÃO** — id: `torre_de_determinacao` — [OK] — +4 para resistir/recuperar de invasões mentais. — linha ~4584
- **ANTECEDENTE ARCANO (Médium)** — id: `antecedente_arcano_medium` — [OK] — Perícia Conjurar; sente incorpóreos como detectar arcano passivo. — linha ~4596
  - **A CASA ESTÁ LIMPA** — id: `a_casa_esta_limpa` — [OK] — +2 na perícia arcana ao conjurar banir. — linha ~4618
  - **AMIGO ESPIRITUAL** — id: `amigo_espiritual` — [OK] — Familiar espiritual com Habilidade Etéreo. — linha ~4622
- **ANTECEDENTE ARCANO (Ocultista)** — id: `antecedente_arcano_ocultista_horror` — [OK] — Perícia Conjurar; Complicação Corrupção. — linha ~4632
  - **INSPIRAÇÃO MÍSTICA** — id: `inspiracao_mistica` — [OK] — Gasta um Bene para conjurar qualquer poder do seu Estágio a partir do livro de feitiços. — linha ~4655
  - **PROTEÇÃO UNIVERSAL** — id: `protecao_universal` — [OK] — Símbolo de proteção próprio, funciona contra qualquer sobrenatural. — linha ~4664
- **ANTECEDENTE ARCANO (Vidente)** — id: `antecedente_arcano_vidente_horror` — [OK] — Perícia Conjurar; pode redistribuir Cartas de Ação de aliados 1x/rodada. — linha ~4673
  - **AVISO** — id: `aviso` — [OK] — Gasta um Bene para forçar oponente a redescartar sua Carta de Ação. — linha ~4692
  - **SEXTO SENTIDO** — id: `sexto_sentido` — [OK] — Gasta PP para melhorar Evasão ou reduzir dano recebido. — linha ~4700
- **ANTECEDENTE ARCANO (Voduísta)** — id: `antecedente_arcano_vuduista` — [OK] — Perícia Fé; bolsa gris-gris (Componentes Materiais); poderes aspecto de loa rada / fúria de loa petro. — linha ~4707
  - **ESTEVE NA ENCRUZILHADA** — id: `esteve_na_encruzilhada` — [OK] — Só na criação; rerrolagem grátis de Fé após sobreviver a quase-morte. — linha ~4754
  - **FAVORECIDO** — id: `favorecido` — [OK] — Aspecto de loa rada / fúria de loa petro custam 3 PP em vez de 5. — linha ~4762

## Monstros Heroicos — Ancestralidades "Mortais" / PJ Monstro (Capítulo Um, p.13–31)

Não há ancestralidades mortais novas separadas neste livro — os 8 templates de
Monstro Heroico abaixo **são** a opção de personagem "não-humana" do
Compêndio de Horror, substituindo a ancestralidade normal. Modelados em
`horror_monstros.json` (atributos_bonus + habilidades + complicações) e nas
Vantagens Monstruosas (`vantagens.json`, categoria `MONSTRUOSAS`,
`requisitos.template`). Cada template listado abaixo contou linha a linha com o
texto do livro; nenhuma habilidade ou Vantagem exclusiva ficou de fora.

- **ANJO** — id: `anjo` (`horror_monstros.json`) — [OK] — Fé/Força/Vigor bônus, Embelezar, Imune a Doenças/Venenos, Não Envelhece, Voo Mov.12, Voto (Maior). 11 Vantagens exclusivas (Ataque Alado, Imortalidade, Lâmina Divina, Línguas, Luz Sagrada, Rajada Abrasadora, Poderes Místicos, Resistência, Resistência Divina, Sentido Sobrenatural, Velocidade) todas em `MONSTRUOSAS`. — linhas ~952–1085
- **DEMÔNIO** — id: `demonio` — [OK] — Força/Vigor/Astúcia conforme texto, Corpóreo/Incorpóreo, Fraqueza (Água Benta), Sentido Sobrenatural. 11 Vantagens exclusivas (Asas, Couro Blindado, Demônio Verdadeiro, Fogo Infernal, Garras, Imortalidade Demoníaca, Incorpóreo, Mordida, Poderes Místicos, Queimar, Sentido Sobrenatural) em `MONSTRUOSAS`. — linhas ~1085–1272
- **FANTASMA** — id: `fantasma` — [OK] — Espírito Forte, Etéreo, Fraqueza (Sal), Imune a Doenças/Venenos, Não Envelhece, Não Respira, Visão Total no Escuro, Voo. 5 Vantagens exclusivas (Invisibilidade, Poderes Místicos, Rugir [partilhada com Monstro de Retalhos], Toque Arrepiante, Travessia). — linhas ~1272–1374
- **LOBISOMEM** — id: `lobisomem` — [OK] — Ferocidade, Fraqueza (Prata), Infravisão, Mordida/Garras, Não Pode Falar, Regeneração (Lenta), Transformação, Velocidade. 7 Vantagens exclusivas (Alfa, Andar nas Paredes [partilhada com Vampiro], Fala, Mordida e Garras Aprimoradas, Regeneração Rápida, Resistência, Uivo). — linhas ~1374–1470
- **MONSTRO DE RETALHOS** — id: `monstro_retalhos` — [OK] — Resistência Arcana, Sem Noção, Fobia (Maior)/Fraqueza a Fogo, Furioso, pacote Morto-Vivo, Partes (Força/Vigor +2 dados). 5 Vantagens exclusivas (Descarga, Flashbacks, Partes Destacáveis [partilhada com Revivido], Robusto, Rugir [partilhada com Fantasma]). — linhas ~1470–1595
- **MÚMIA** — id: `mumia` — [OK] — Força das Eras, Fraqueza (Fogo), Lento, pacote Morto-Vivo, Não Envelhece. 8 Vantagens exclusivas (Cavar, Invocar Bando, Invocar Bando Maior, Invocar Tempestade, Podridão da Múmia, Poderes Místicos, Regeneração Lenta, Regeneração Rápida). — linhas ~1595–1715
- **REVIVIDO** — id: `revivido` — [OK] — Força dos Mortos, pacote Morto-Vivo, Não Envelhece, Regeneração (Lenta, exige carne crua), Robusto, Voto (Maior — vingança). 6 Vantagens exclusivas (Devorador de Memórias, Fedor, Mestre dos Zumbis, Partes Destacáveis, Rastreador Implacável, Toque da Morte). — linhas ~1715–1825
- **VAMPIRO** — id: `vampiro` — [OK] — Fome (Hábito Maior), Força dos Condenados, Fraquezas tradicionais (convite, estaca, sol, símbolo sagrado, água benta/corrente), Mordida, pacote Morto-Vivo, Não Envelhece, Regeneração (Lenta), Visão Total no Escuro. 11 Vantagens exclusivas (Andarilho do Dia, Andar nas Paredes, Forma Animal, Forma de Névoa, Garras, Prole da Noite, Encantar/Saciar, Senhor, Servo, Regeneração Rápida). — linhas ~1825–2105

### Vantagens Monstruosas — genéricas (p.15–16)

- **DANO AGRAVADO** — id: `dano_agravado` — [OK] — Ataques inatos ferem qualquer criatura sobrenatural; regeneração de alvos afetados sofre -4. — linha ~899
- **MEDO (-2)** — id: `medo_monstro` — [OK] — Reduz em -2 os testes de Medo provocados pela própria criatura. — linha ~909
- **SELVAGERIA** — id: `selvageria_monstro` — [OK] — Ataque Selvagem da criatura causa +4 em vez de +2. — linha ~917
- **VELHO** — id: `velho` — [OK] — +2 em Astúcia e Conhecimento Geral. — linha ~924

### Sistemas do Monstro Heroico (p.14–15)

- **RAIVA** — id sugerido: `sistema_raiva_monstro_heroico` — [FALTA] — 1x/sessão gasta um Bene como Convicção para entrar em fúria; ao terminar, Espírito -2 ou ganha Psicose Maior; a 4ª Psicose (de Raiva ou Medo) vira Surto Psicótico. Não há rastreamento desse recurso em nenhum catálogo/estado do app (`horror_monstros.json` só traz atributos/habilidades/complicações fixas do template). — linhas ~822–855
- **PODERES MÍSTICOS** (regra genérica de referência) — [OK, via referência] — Base mecânica citada pelas 4 Vantagens "Poderes Místicos (X)" já catalogadas (Anjo/Demônio/Fantasma/Múmia); não é uma entrada compra própria, por isso sem id — apenas o texto de regra (10 PP dedicados, ativação automática por ação livre limitada). — linha ~856

## Novos Poderes (Capítulo Quatro, p.86–99)

Todos em `poderes.json`, filtro `"HORROR" in livros` (aparecem com sufixo `_2`/`_3` por já existir uma entrada-base multi-livro com o mesmo nome).

- **ASPECTO DE LOA RADA** — id: `aspecto_de_loa_rada` — [OK] — linha ~5030
- **CONJURAR ALIADO** — id: `conjurar_aliado_3` — [OK] — linha ~5218
- **CONJURAR DEMÔNIO** — id: `conjurar_demonio` — [OK] — linha ~5289
- **CONSAGRAR SOLO** — id: `consagrar_solo` — [OK] — linha ~5391
- **EXORCISMO** — id: `exorcismo` — [OK] — linha ~5422
- **FÚRIA DE LOA PETRO** — id: `furia_de_loa_petro` — [OK] — linha ~5490
- **HORRORES ILUSÓRIOS** — id: `horrores_ilusorios` — [OK] — linha ~5562
- **LOCALIZAR** — id: `localizar_3` — [OK] — linha ~5620
- **MALDIÇÃO** — id: `maldicao_2` — [OK] — linha ~5646
- **MORTALHA DA COVA** — id: `mortalha_da_cova` — [OK] — linha ~5693
- **PESADELOS** — id: `pesadelos` — [OK] — linha ~5726
- **RANCOR** — id: `rancor` — [OK] — linha ~5747
- **SANTUÁRIO** — id: `santuario_2` — [OK] — linha ~5767
- **SENTIR CADÁVER** — id: `sentir_cadaver` — [OK] — linha ~5805
- **SESSÃO ESPÍRITA** — id: `sessao_espirita` — [OK] — linha ~5828
- **SUPRIMIR TRANSFORMAÇÃO** — id: `suprimir_transformacao` — [OK] — linha ~5885
- **TRANCAR/DESTRANCAR** — id: `trancar_destrancar_3` — [OK] — linha ~5922
- **VIDÊNCIA** — id: `videncia_3` — [OK] — linha ~6010

## Equipamento de Caçador de Monstros (Capítulo Dois, p.34–36)

Todos em `equipamentos.json`, categoria `Equipamento Mundano`, filtro `"HORROR" in livros`. Contagem bate exatamente: 10 armas à distância + 3 corpo a corpo + 7 munições + 11 especiais = 31 itens no livro e 31 no JSON.

**Armas à Distância** (subtipo "Armas à Distância", 10 itens):
- **Água Benta, Granada** — [OK] — linha ~2110
- **Água Benta, Pistola** — [OK] — linha ~2114
- **Água Benta, Spray** — [OK] — linha ~2117
- **Besta de Gancho** — [OK] — linha ~2120
- **Minibesta** — [OK] — linha ~2127
- **Besta de Repetição** — [OK] — linha ~2129
- **Estaca, arremessável** — [OK] — linha ~2131
- **Granada de UV** — [OK] — linha ~2132
- **Mochila Atômica Fantasma** — [OK] — linha ~2136
- **Sinalizador** — [OK] — linha ~2145

**Armas Corpo a Corpo** (3 itens):
- **Desmontador de Corpos** — [OK] — linha ~2154
- **Estaca** — [OK] — linha ~2157
- **Estaca (Ponta de Prata)** — [OK] — linha ~2158

**Munição** (7 itens):
- **Balas de Alho** — [OK] — linha ~2161
- **Balas de Nitrato de Prata** — [OK] — linha ~2162
- **Balas de Prata** — [OK] — linha ~2163
- **Balas UV** — [OK] — linha ~2165
- **Virote (Besta de Guincho)** — [OK] — linha ~2166
- **Virote (Besta de Repetição)** — [OK] — linha ~2167
- **Virote (Minibesta)** — [OK] — linha ~2168

**Equipamento Especial** (11 itens):
- **Armadilha de Fantasma** — [OK] — linha ~2172
- **Câmera Kirlian (Imagem)** — [OK] — linha ~2177
- **Câmera Kirlian (Vídeo)** — [OK] — linha ~2180
- **Câmera com Sensor de Luz Estruturada (SLS)** — [OK] — linha ~2184
- **Crucifixo/Símbolo Sagrado** — [OK] — linha ~2190
- **Lanterna UV** — [OK] — linha ~2194
- **Medidor de Campos Eletromagnéticos** — [OK] — linha ~2198
- **Óculos Kirlian** — [OK] — linha ~2202
- **Protetor de Pescoço de Couro** — [OK] — linha ~2206
- **Protetor de Pescoço de Metal** — [OK] — linha ~2208
- **Sensor de Movimento** — [OK] — linha ~2210

Regra de referência **Fazendo Água Benta** (p.36, linha ~2216) não é um item catalogável — é um procedimento (rolagem de Fé + 1 PP) que usa a Vantagem Antecedente Arcano (Milagres) já existente; não requer entrada própria.

## Itens Arcanos (Capítulo Cinco, p.101–115)

Catálogo comprável em `equipamentos.json`, `tipo: "Equipamento Mundano"`,
`subtipo: "Itens Arcanos"`, filtro `"HORROR" in livros"` — **56 itens no JSON**.
Conferi cada item do capítulo no texto-fonte: **o capítulo contém exatamente 54
itens nomeados** (Anel de Proteção Mágica → Vela Sombria, alfabético). Os 54
batem 1:1 com o JSON. **Os outros 2 itens do JSON — "Algema da Inquisição" e
"Anel da Irmandade Eterna" — não aparecem em nenhum lugar de
`docs/swade_horror`** (busca por `Algema da Inquisição` e `Irmandade Eterna` no
arquivo completo não retorna nenhuma ocorrência). Resposta à pergunta do
levantamento anterior: **a cobertura do capítulo é completa, mas o catálogo do
app tem 2 itens a mais do que este texto-fonte** — provavelmente de outra
edição/impressão do livro, de outro produto Savage Worlds, ou um erro de
tag `HORROR`; vale confirmar contra a edição física/PDF antes de considerar
"extra" ou remover.

- **Anel de Proteção Mágica** — [OK] — Armadura +4 vs. dano arcano, +4 para resistir a poderes. — linha ~6110
- **Arma Matadora de Alma** — [OK] — Pistola que destrói o alvo em vez de causar dano. — linha ~6125
- **Bandagens de Anúbis** — [OK] — linha ~6145
- **Bengala de Lobo** — [OK] — linha ~6171
- **Bisturi de Jack** — [OK] — linha ~6191
- **Boneca Possuída** — [OK] — linha ~6226
- **Bonecos Vodu** — [OK] — linha ~6250
- **Braseiro da Conjuração** — [OK] — linha ~6274
- **Caixão Vampírico** — [OK] — linha ~6285
- **Cajado do Necromante** — [OK] — linha ~6304
- **Carranca de Navio** — [OK] — linha ~6315
- **Casa de Bonecas Assombrada** — [OK] — linha ~6353
- **Conhecimento Proibido** — [OK] — linha ~6408
- **Cota de Malha de Ferro Frio** — [OK] — linha ~6476
- **Cutelo Primordial** — [OK] — linha ~6485
- **Diadema Penumbral** — [OK] — linha ~6509
- **Diário de um Louco** — [OK] — linha ~6526
- **Dinheiro de Sangue** — [OK] — linha ~6547
- **Espelho de Jade** — [OK] — linha ~6574
- **Espelho do Doppelganger** — [OK] — linha ~6598
- **Elixir do Amor** — [OK] — linha ~6646
- **Escada de Bruxa** — [OK] — linha ~6665
- **Faca de Sacrifício** — [OK] — linha ~6708
- **Garras de Prata** — [OK] — linha ~6730
- **Giz de Proteção** — [OK] — +2 na rolagem de Ocultismo ao criar círculo de aprisionamento. — linha ~6740
- **Grimório** — [OK] — Concede poder bônus com a Vantagem Novos Poderes. — linha ~6748
- **Lâmina Infernal** — [OK] — linha ~6769
- **Laboratório de Frankenstein** — [OK] — linha ~6790
- **Lâmina Lunar** — [OK] — linha ~6893
- **Lâmina Templária** — [OK] — linha ~6903
- **Mão da Glória** — [OK] — linha ~6920
- **Mão do Destino** — [OK] — linha ~6845
- **Mangual de Carne Incurável** — [OK] — linha ~6856
- **Marca do Demônio** — [OK] — linha ~6864
- **Máscara Canibal** — [OK] — linha ~6939
- **Máscara de Cerâmica** — [OK] — linha ~6976
- **Máscara de Hóquei do Terror** — [OK] — linha ~7023
- **Música Infernalis** — [OK] — linha ~7052
- **O Necronomicon** — [OK] — linha ~7068
- **Optógrafo** — [OK] — linha ~7118
- **Pata do Macaco** — [OK] — Concede 3 desejos, sempre pervertidos. — linha ~7143
- **Pedra dos Pesadelos** — [OK] — linha ~7176
- **Pergaminho** — [OK] — Item genérico: qualquer poder inscrito, definido pelo Mestre. — linha ~7189
- **Poção** — [OK] — Item genérico com 5 variantes (Cura/Força/Poder/Rapidez/Visão Sombria). — linha ~7206
- **Pó de Cadáver** — [OK] — linha ~7219
- **Pó de Tumba** — [OK] — linha ~7232
- **Quebra-Cabeças** — [OK] — Invoca demônio aleatório (tabela D12 na p.115). — linha ~7254
- **Relógio de Bolso da Batida de Coração** — [OK] — linha ~7279
- **Relógio de Pêndulo** — [OK] — linha ~7303
- **Retrato da Imortalidade** — [OK] — linha ~7329
- **Sangue de Lobisomem** — [OK] — linha ~7397
- **Talismã de Proteção** — [OK] — linha ~7412
- **Vela da Alma** — [OK] — linha ~7421
- **Vela Sombria** — [OK] — linha ~7437
- **Algema da Inquisição** — [CONFERIR] — não localizada em `docs/swade_horror`; confirmar fonte (outra edição/produto?) antes de manter a tag `HORROR`.
- **Anel da Irmandade Eterna** — [CONFERIR] — mesma observação acima.

Tabela **Demônios Aleatórios** (D12, p.115, linha ~7364) e a lista **Exemplos de
Grimórios** (5 grimórios nomeados dentro da entrada Grimório, linha ~6797) são
flavor/sub-tabelas de apoio ao item pai, não entradas de catálogo próprias.

## Itens Alquímicos — Consumíveis (Capítulo Cinco, p.117–118)

Tabela final do capítulo, em `equipamentos.json`, `tipo: "Consumíveis"`,
`subtipo: "Alquímicos e Especiais"`, filtro `"HORROR" in livros"` — 7 itens, bate
exatamente com a tabela do livro.

- **Ácido, frasco** — [OK] — linha ~7480
- **Antitoxina, frasco** — [OK] — linha ~7484
- **Bastão de Fumaça** — [OK] — linha ~7486
- **Enredapés** — [OK] — linha ~7489
- **Fogo Alquímico** — [OK] — linha ~7493
- **Fósforos** — [OK] — linha ~7498
- **Pedra Explosiva** — [OK] — linha ~7501

- **Regra "Criando Itens Alquímicos"** — id sugerido: `regra_criacao_itens_alquimicos` — [FALTA] — procedimento de crafting (laboratório + metade do custo em componentes + tempo = custo em minutos + rolagem de Alquimia) tanto para os Itens Arcanos alquímicos (p.116) quanto para os consumíveis acima (p.117); os *resultados* (os itens) já estão cadastrados, mas a *regra de fabricação* em si não tem representação em nenhum catálogo/mecânica do app. Baixa prioridade — é regra de mesa aplicável manualmente. — linhas ~7379 e ~7471

## Sistema de Medo e Trauma (Capítulo Três, p.50–52)

Afeta diretamente a ficha do personagem (fobias/manias de longo prazo geradas
por falha em teste de Medo) — não há representação disso em nenhum
catálogo/estado do app hoje (busca por "Psicose"/"Efeitos de Medo" nos JSONs
não retorna nada; `complicacoes.json` só tem as Complicações fixas de criação,
não o gerador aleatório de Psicoses).

- **Testes de Medo e regra de Sanidade "in-line"** — [OK, conceitual] — Regra central: falha em teste de Medo (Espírito) manda para a tabela de Efeitos de Medo em vez de um contador de Sanidade numérico. — linhas ~2878–2893
- **Tabela Efeitos de Medo (D20)** — id sugerido: `tabela_efeitos_de_medo` — [FALTA] — 8 faixas de resultado (Surto de Adrenalina → Ataque Cardíaco/Surto Psicótico em 22+); referenciada por várias Vantagens já cadastradas (ex. Aura de Coragem, Rainha/Rei do Grito) mas a tabela em si não existe como dado. — linha ~2927
- **Tabela Psicoses Aleatórias (D12 Menor/Maior)** — id sugerido: `tabela_psicoses_aleatorias` — [FALTA] — gera uma Complicação Menor ou Maior temporária (todas já existem em `complicacoes.json` individualmente: Cauteloso, Delirante, Pessimista, Hábito, Hesitante, Fobia, Peculiaridade, Histérico, Teimoso, Supersticioso, Desconfiado, Sensível / Sanguinário, Amaldiçoado, Delirante, Condenado, Hábito, Mudo, Terrores Noturnos, Fobia, Sem Escrúpulos, Histérico, Desconfiado, Covarde) — falta só a tabela de sorteio e o rastreamento de "até 3 Psicoses" (regra Colapso). — linha ~2915
- **Colapso / Surto Psicótico** — id sugerido: `regra_colapso_surto_psicotico` — [FALTA] — limite de 3 Psicoses por personagem; a 4ª vira Surto Psicótico (personagem sob controle do Mestre). — linha ~2987
- **Tratando Efeitos de Medo (Descanso/Terapia, Triunfando sobre o Mal)** — id sugerido: `regra_tratamento_psicoses` — [FALTA] — formas de remover Psicoses geradas por Medo (via downtime "Pausa" ou derrotando um grande mal); Complicações escolhidas na criação continuam só removíveis por Progresso. — linha ~2995

## Fora de escopo

- **Refúgio (safehouse)** (Capítulo Dois, p.37–41, linhas ~2231–2488) — sistema de recurso **de grupo**: Vantagem/Complicação/Forma/Melhorias do QG dos caçadores de monstros, mais a tabela de **Encontros** (D20, linha ~2588) ligada a cada Melhoria comprada. Não é ficha de personagem individual — candidato de baixa prioridade caso o app um dia suporte recursos de grupo, mas não modelado em detalhe aqui, conforme instrução.
- **Regras Novas e Atualizadas** (Capítulo Três, linhas ~2649–2830): Ações Limitadas, Ataque Desesperado, Preso e Enredado (errata), Novo Perigo (Grandes Altitudes), Modelo de Raio, Baldes de Sangue, Cartas Selvagens (regra de ambientação), Convicção Vilanesca, Cura Difícil — regras de mesa/combate genéricas, sem recorte de personagem.
- **Proteções e Aprisionamentos** (linhas ~3413–3532) — mecânica de perseguir criaturas com símbolos/materiais e criar círculos de aprisionamento (usa a perícia Ocultismo já cadastrada); validada e resolvida pelo Mestre a cada cena, não é um dado de ficha comprável. Candidato de baixa prioridade, na linha do que a análise anterior já apontava.
- **Regras de Massacre** (linhas ~3534–3598) — variante de estilo de jogo letal ("funil"), não opção de personagem.
- **Sinais e Presságios** (linhas ~3606–3670+) — ferramenta de mestre para entregar pistas/profecias; referenciada pela Vantagem Visão (já cadastrada), mas o sistema em si é conduzido pelo Mestre.
- **Estilos de Horror / Jogando o Gênero** (final do Capítulo Três) — ensaio de orientação de campanha, sem conteúdo mecânico de personagem.
- **Magia Ritualística / Conjuração Ritual** (Capítulo Quatro, p.85, linhas ~4876–4943, com as tabelas de Componentes de Ritual Comuns/Exóticos nas linhas ~4792–4872) — mecânica de Tarefa Dramática com componentes sorteados e preparo coletivo; conforme instrução, tratada como mecânica de mesa multi-personagem e deixada de fora.
- **Capítulo Seis "Criaturas"** (linhas ~7565–15634) — bestiário completo de antagonistas (inclui Customizando/Destruindo Vampiros, Novas Habilidades Monstruosas como Dilacerar/Engolir/Imparável etc.), estatísticas de NPC para o Mestre usar contra o grupo — não são opções de personagem jogador.
- **Apêndice A "Contos Assustadores"** (linhas ~15634–16211) — conselhos de mestria para conduzir horror (ritmo, tom, uso do bestiário), sem conteúdo mecânico de personagem.
- **Apêndice B "Os Mythos de Cthulhu"** (linhas ~16211–fim) — bestiário de antagonistas do Mythos (Servos e Deuses), mesma lógica do Capítulo Seis.
