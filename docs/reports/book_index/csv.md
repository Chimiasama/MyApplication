# Índice de Criação de Personagem — A Cidade do Sol a Vapor (CSV)

Este índice cobre os três textos-fonte do cenário steampunk "A Cidade do Sol a Vapor" (Odyssey
Publicações / Retropunk, tag de livro `CIDADE_SOL_VAPOR`): **O Livro do Criador**
(`docs/swade_csv_livro_do_criador`, ~9886 linhas — livro do Mestre, contém o capítulo "Criando
Personagens: Demônios e Anjos" com o Antecedente Arcano (Demônio) e (Anjo)), **A Cidade do Sol a
Vapor — Livro dos Mortais** (`docs/swade_csv_livro_dos_mortais`, ~10190 linhas — livro dos
jogadores, dividido em Livro dos Homens / Livro dos Demônios / Livro dos Anjos / Apêndice, com a
maior parte das Vantagens, Complicações, Equipamentos e Poderes novos) e **O Movimento
Vermelho** (`docs/swade_csv_movimento_vermelho`, ~814 linhas — suplemento pequeno sobre os
sindicatos trabalhistas "Jaquetas Vermelhas"). Cada entrada abaixo foi conferida contra
`app/src/main/assets/*.json` (busca por nome e pela tag `"CIDADE_SOL_VAPOR"` em `livros`) e marcada
[OK] (já cadastrada), [FALTA] (ausente, id sugerido em snake_case) ou [CONFERIR] (existe mas com
alguma divergência/nuance que vale checar).

## Ancestralidades / Origens

- **Humano** — id: `anc_humano_csv` — [OK] — Origem padrão de personagens, ganha uma Vantagem de Novato extra grátis (Adaptável) — linha ~2252 (Antecedente Arcano exige "Humano") de docs/swade_csv_livro_dos_mortais
- **Demônios** — id: `anc_demonio_csv` — [OK] — Habitantes do Abismo, -2 em Resistência (Frágil), começam com Antecedente Arcano (Demônio) — linha ~2000-2034 de docs/swade_csv_livro_do_criador
- **Meio-Demônio** — id: `anc_meio_demonio_csv` — [OK] — Herança mista, Vantagem Novato extra e opção de comprar AA (Demônio) com Disfarce Demoníaco atrasado para o Estágio Experiente (custo 2 PP) — linha ~2169-2204 de docs/swade_csv_livro_do_criador
- **Anjo** — id: `anc_anjo_csv` — [OK, resolvido em 2026-08-31] — Habitantes alados do Céu; racial "Asas de Anjo" (voo 12, usa Atletismo, não pode nadar, -1 em movimentação com asas, teto de altitude reduzido no Limbo) e "Recluso" (-2 em Conhecimento Geral); agora satisfaz o requisito das três Vantagens já cadastradas (`aa_anjo`, `anjo_cinza`, `guerreiro_celestial`) — linha ~2205-2236 de docs/swade_csv_livro_do_criador

## Vantagens

### De Antecedente / Origem

- **Acordo com Demônios** — id: `acordo_com_demonios` — [OK] — 4x o dinheiro inicial e salário maior em troca de Fobia (Menor — demônios) — linha ~2149 de docs/swade_csv_livro_dos_mortais
- **Parrudo** — id: `parrudo` — [OK] — Sem penalidade ao mover e usar Tiro Rápido; permite operar Arma Estacionária de armadura a vapor como Tiro Rápido — linha ~2170 de docs/swade_csv_livro_dos_mortais
- **Cavalheiro Completo** — id: `cavalheiro_completo` — [OK] — +2 em Lutar, Atirar e dano em duelos 1x1 — linha ~2179 de docs/swade_csv_livro_dos_mortais

### De Combate

- **Atirador Perceptivo** — id: `atirador_perceptivo` — [OK] — Ignora Mau Funcionamento de arma de ar com 10+ ao diagnosticar — linha ~2190 de docs/swade_csv_livro_dos_mortais
- **Atirador Olímpico** — id: `atirador_olimpico` — [OK] — Metade da penalidade por ações múltiplas de Atirar — linha ~2200 de docs/swade_csv_livro_dos_mortais
- **Atirador Urbano** — id: `atirador_urbano` — [OK] — Atirar corpo a corpo não causa Vulnerável nem sofre Aparar — linha ~2216 de docs/swade_csv_livro_dos_mortais
- **Caçador de Pombos** — id: `cacador_de_pombos` — [OK] — Espectador Inocente só em Falha Crítica ao atirar — linha ~2224 de docs/swade_csv_livro_dos_mortais
- **Caçador de Tigres** — id: `cacador_de_tigres` — [OK] — +d6/+d8 de dano ao mirar com rifle de ar — linha ~2235 de docs/swade_csv_livro_dos_mortais

### De Poder (base dos Antecedentes Arcanos — ver seção dedicada)

- **Antecedente Arcano (Magia Negra)** — id: `aa_magia_negra` — [OK] — Novato, Humano, Ocultismo d6+ — linha ~2250 de docs/swade_csv_livro_dos_mortais
- **Antecedente Arcano (Milagres)** — id: `aa_milagres` — [OK] — Novato, Humano, Fé d6+ — linha ~2262 de docs/swade_csv_livro_dos_mortais
- **Antecedente Arcano (Tecnomagia)** — id: `aa_tecnomagia` — [OK] — Novato, Humano, Tecnomagia d6+ — linha ~2270 de docs/swade_csv_livro_dos_mortais
- **Antecedente Arcano (Demônio)** — id: `aa_demonio` — [OK] — Novato, Demônio ou Meio-Demônio; Conjurar; Disfarce Demoníaco + 3 poderes; 10 PP — linha ~2036 de docs/swade_csv_livro_do_criador
- **Antecedente Arcano (Anjo)** — id: `aa_anjo` — [OK, mas ver CONFERIR abaixo] — Experiente, Anjo; Fé; 2 poderes iniciais; 15 PP — linha ~2238 de docs/swade_csv_livro_do_criador
- **Magomecânico** — id: `magomecanico` — [OK] — Experiente, AA (Tecnomagia), Consertar/Ciência/Tecnomagia d8+; permite fabricar invenções tecnomágicas — linha ~7053 de docs/swade_csv_livro_dos_mortais
- **Molda-Carne** — id: `molda_carne` — [OK] — Veterano, AA (Tecnomagia), Curar d8+, Tecnomagia d10+; próteses tecnomágicas — linha ~7073 de docs/swade_csv_livro_dos_mortais
- **Poder da Mente** — id: `poder_da_mente` — [OK] — Novato, AA (Tecnomagia); trocar poder na unidade de programação em 1 rodada — linha ~7088 de docs/swade_csv_livro_dos_mortais
- **Irmão da Noite** — id: `irmao_da_noite` — [OK] — Experiente, AA (Magia Negra); Visão no Escuro constante — linha ~7308 de docs/swade_csv_livro_dos_mortais
- **Poder do Sangue** — id: `poder_do_sangue` — [OK] — Novato, AA (Magia Negra); +5 PP por sacrifício — linha ~7315 de docs/swade_csv_livro_dos_mortais
- **Vontade Sombria** — id: `vontade_sombria` — [OK] — Novato, AA (Magia Negra); +1 feitiço guardado sem penalidade — linha ~7324 de docs/swade_csv_livro_dos_mortais
- **Guerreiro do Senhor** — id: `guerreiro_do_senhor` — [OK] — Experiente, AA (Milagres); libera poderes de combate dos abençoados — linha ~7490 de docs/swade_csv_livro_dos_mortais
- **Ira do Senhor** — id: `ira_do_senhor` — [OK] — Veterano, Guerreiro do Senhor; libera poderes de combate mais fortes — linha ~7501 de docs/swade_csv_livro_dos_mortais
- **Poder do Espírito** — id: `poder_do_espirito` — [OK] — Experiente, AA (Milagres), Espírito d8+, Fé d8+; +10 PP por Fadiga da Determinação Religiosa — linha ~7509 de docs/swade_csv_livro_dos_mortais
- **Anjo Cinza** — id: `anjo_cinza` — [OK, ancestralidade Anjo agora existe] — Novato, Anjo; imune a ares do Limbo, calor, frio, doenças e venenos — linha ~2256 de docs/swade_csv_livro_do_criador
- **Guerreiro Celestial** — id: `guerreiro_celestial` — [OK, ancestralidade Anjo agora existe] — Experiente, Anjo; lança em si mesmo proteção/aumentar característica/velocidade/dádiva do guerreiro usando o Atributo — linha ~2263 de docs/swade_csv_livro_do_criador

### Profissionais

- **Mecânico Cego** — id: `mecanico_cego` — [OK] — +2 em Consertar, sem penalidade para conserto às cegas — linha ~2288 de docs/swade_csv_livro_dos_mortais
- **Mestre das Caldeiras** — id: `mestre_das_caldeiras` — [OK] — Evita Mau Funcionamento de mecanismo a vapor com sucesso em Astúcia — linha ~2299 de docs/swade_csv_livro_dos_mortais

### Sociais

- **Elegante** — id: `elegante` — [OK] — Cria traje a partir de trapos, +1 em Persuadir — linha ~2311 de docs/swade_csv_livro_dos_mortais

### Estranhas

- **Me Chamo Igor** — id: `me_chamo_igor` — [OK] — Assistente com Consertar/Curar que evolui com o Estágio — linha ~2329 de docs/swade_csv_livro_dos_mortais
- **Seguidor de Nietzsche** — id: `seguidor_de_nietzsche` — [OK] — Ganha Convicção em Falha Crítica — linha ~2345 de docs/swade_csv_livro_dos_mortais
- **Tarô da Nova Era** (22 cartas, uma escolha única) — ids: `taro_peregrino`, `taro_charlatao`, `taro_espiritualista`, `taro_confusao`, `taro_sentinela`, `taro_engenheiro`, `taro_portao`, `taro_voo`, `taro_pacto`, `taro_expedicao`, `taro_fortuna`, `taro_mecanismo`, `taro_sem_alma`, `taro_morte`, `taro_conhecimento`, `taro_demonio`, `taro_estrela_morta`, `taro_eter`, `taro_vazio`, `taro_luz`, `taro_galvanismo`, `taro_cidade` — [OK] — todas as 22 cartas do Arcano Maior conferidas 1:1 — linhas ~2385-2726 de docs/swade_csv_livro_dos_mortais

### De Organizações / Sociedades Secretas (cidadania na Teia) [OK — 8/8, resolvido em 2026-08-31]

- **Irmandade das Seis Chaves** — id: `irmandade_das_seis_chaves` — [OK] — Veterano, Magomecânico ou Consertar/Ciência d10+; concede um "animal de estimação" magomecânico fixo (Anansi, Hanuman, Hugin ou Sleipnir — estatísticas prontas transcritas na descrição da Vantagem, não é sistema de criação) — linha ~8648 de docs/swade_csv_livro_dos_mortais
- **Clapper Branco** — id: `clapper_branco` — [OK] — Novato; +2 em Persuadir com trabalhadores de fábrica — linha ~8925 de docs/swade_csv_livro_dos_mortais
- **Clapper do Carvão** — id: `clapper_do_carvao` — [OK] — Novato; sabota mecanismos, causando Mau Funcionamento programado — linha ~8934 de docs/swade_csv_livro_dos_mortais
- **Culto do Eclipse** — id: `culto_do_eclipse` — [OK] — Novato; 1x/sessão convoca 3 combatentes do Culto para ajudar (statblocks dos combatentes ficam fora do escopo, mesma categoria de bestiário) — linha ~8991 de docs/swade_csv_livro_dos_mortais
- **Sociedade do Noroeste** — id: `sociedade_do_noroeste` — [OK] — Novato, Perceber d8+; +2 em Perceber nas ruas, nunca se perde na Teia — linha ~9222 de docs/swade_csv_livro_dos_mortais
- **A Pena do Albatroz** — id: `pena_do_albatroz` — [OK] — Novato; 1x/sessão pede ajuda a um membro da Força Expedicionária — linha ~9280 de docs/swade_csv_livro_dos_mortais
- **Adepto da Ordem do Albatroz** — id: `adepto_ordem_do_albatroz` — [OK] — Novato, Conhecimento Acadêmico d6+; Conexões com a Ordem + recall de informação sobre o Limbo — linha ~9301 de docs/swade_csv_livro_dos_mortais
- **Cavaleiro de São Germain** — id: `cavaleiro_de_sao_germain` — [OK] — Novato, Complicação Código de Honra (Ordem de São Germain); +2 Resistência contra magia demoníaca e +2 de dano contra demônios — linha ~9490 de docs/swade_csv_livro_dos_mortais

### Movimento Vermelho (suplemento)

- **Força Direcionada** — id: `forca_direcionada` — [OK] — Novato, Força d8+, Vigor d6+, Musculoso; +2 em rolagens de Força — linha ~125 de docs/swade_csv_movimento_vermelho
- **Sindicalizado** — id: `sindicalizado` — [OK] — Novato, Conhecimento Geral d6+; Conexões (Sindicato), descontos e regra de Sindicato — linha ~133 de docs/swade_csv_movimento_vermelho
- **Couro Calejado** — id: `couro_calejado` — [OK] — Novato, Vigor d8+; +2 Resistência contra armas de controle de tumulto — linha ~147 de docs/swade_csv_movimento_vermelho
- **Ferramenta Letal** — id: `ferramenta_letal` — [OK] — Experiente, Lutar/Astúcia d6+, Lutador Improvisador; +2 dano/+1 Aparar com ferramentas — linha ~156 de docs/swade_csv_movimento_vermelho
- **Pegada Forte** — id: `pegada_forte` — [OK] — Experiente, Força/Atletismo d8+, Brutamontes; +2 em Agarrar e resistir Desarme — linha ~163 de docs/swade_csv_movimento_vermelho
- **Líder de Manifestação** — id: `lider_de_manifestacao` — [OK] — Experiente, Comando, Conhecimento de Batalha d8+, Performance d6+ — linha ~182 de docs/swade_csv_movimento_vermelho
- **Vox Populi** — id: `vox_populi` — [OK] — Veterano, Astúcia d8+, Comando, Conhecimento de Batalha d8+ — linha ~189 de docs/swade_csv_movimento_vermelho
- **Desmobilizar** — id: `desmobilizar` — [OK] — Veterano; Desafio contra comandante rival em Batalha em Massa — linha ~200 de docs/swade_csv_movimento_vermelho
- **Socializar Magias** — id: `socializar_magias` — [OK] — Veterano, AA (Tecnomagia), Poder da Mente, Magomecânico; compartilha poderes configurados entre tecnomagos próximos — linha ~219 de docs/swade_csv_movimento_vermelho
- **"Hacker"** — id: `hacker` — [OK] — Experiente, Astúcia/Consertar d8+, Senhor Conserta Tudo; hackeia autômatos — linha ~249 de docs/swade_csv_movimento_vermelho
- **Meu Camarada!** — id: `meu_camarada` — [OK] — Experiente, Sindicalizado; melhora reação de colegas de trabalho não sindicalizados — linha ~277 de docs/swade_csv_movimento_vermelho

## Complicações

- **Alma Penhorada (Menor)** — id: `comp_alma_penhorada` — [OK] — Dobro do dinheiro inicial, dívida de 5000 sóis, -2 em Conhecimento Geral — linha ~2033 de docs/swade_csv_livro_dos_mortais
- **Alma Vendida (Maior)** — id: `comp_alma_vendida` — [OK] — Rolagem de Espírito no início da aventura ou ganha Fadiga — linha ~2049 de docs/swade_csv_livro_dos_mortais
- **Face Nobre (Menor)** — id: `comp_face_nobre` — [OK] — Reação pior em distritos da classe trabalhadora — linha ~2064 de docs/swade_csv_livro_dos_mortais
- **Gosto Sofisticado (Maior)** — id: `comp_gosto_sofisticado` — [OK] — Precisa gastar 20 sóis/dia em comida ou sofre Fadiga — linha ~2080 de docs/swade_csv_livro_dos_mortais
- **Maldição do Gremlin (Menor ou Maior)** — id: `comp_maldicao_gremlin` — [OK] — Aumenta a PMF de tecnologia nas mãos (Menor) ou ao redor (Maior) do personagem — linha ~2092 de docs/swade_csv_livro_dos_mortais
- **Paciente de Sanatório (Maior)** — id: `comp_paciente_sanatorio` — [OK] — -1 em Espírito e reação inicial reduzida — linha ~2113 de docs/swade_csv_livro_dos_mortais
- **Tecnofobia (Maior)** — id: `comp_tecnofobia` — [OK] — Precisa de rolagem de Espírito para usar tecnologia a vapor/tecnomágica — linha ~2129 de docs/swade_csv_livro_dos_mortais

## Perícias

- **Tecnomagia** (Astúcia) — [OK] — perícia arcana que combina tecnologia e magia, usada pelos tecnomagos (AA Tecnomagia) — linha ~6820 de docs/swade_csv_livro_dos_mortais

Todas as demais perícias citadas no cenário (Atirar, Ciência, Conhecimento de Batalha, Ocultismo,
Fé, Consertar, Curar, etc.) são perícias-padrão de Savage Worlds já cadastradas como `BASICO` — não
há perícias novas além de Tecnomagia.

## Antecedente Arcano por Estágio

O cenário substitui a progressão livre de poderes por **listas fechadas por Estágio** para quatro
Antecedentes Arcanos (um quinto, Anjo, reaproveita a lista de Milagres). No código, isso é
implementado em `app/src/main/java/com/example/swadebuilder/model/ArcaneConfig.kt`
(`getStageBasedPowersByStage`) e sinalizado por `usaPoderesPorEstagio: true` em
`vantagens.json`/`Vantagem.kt`.

### Antecedente Arcano (Magia Negra) — "Feiticeiro" — id `aa_magia_negra`, chave interna `FEITICEIRO`

[OK] — 30 poderes distribuídos em 4 Estágios, conferidos 1:1 contra `ArcaneConfig.SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE` — linha ~7333 de docs/swade_csv_livro_dos_mortais.

| Estágio | Poderes (livro) |
|---|---|
| Novato | Andar nas Paredes, Atordoar, Cegar, Confusão, Deflexão, Detectar/Ocultar Arcano, Devastação, Enredar, Ferir, Ilusão, Medo, Obscurecer (Aspecto), Proteção Arcana, Proteção, Raio, Rajada, Reduzir Característica (Aspecto), Som/Silêncio, Visão Sombria |
| Experiente | Campo de Dano, Disfarce, Explosão, Invisibilidade, Morosidade (Aspecto), Sono, Visão Distante |
| Veterano | Fantoche, Limpeza Mental |
| Heroico | Adivinhação, Intangibilidade |

Feiticeiros usam sacrifício humano (10 PP por sacrifício) em vez de recuperação normal de PP e não
podem comprar a Vantagem Novos Poderes — regra de recurso, fora do escopo do mapeamento de
poderes por estágio, mas relevante para a UI de Pontos de Poder.

### Antecedente Arcano (Milagres) — "Abençoado" — id `aa_milagres`, chave interna `MILAGRES`

[OK, com uma nuance CONFERIR] — 26 entradas de poder no livro (com Aspectos e variantes que
recolhem para 21 ids únicos), conferidas contra `ArcaneConfig.SOL_VAPOR_MILAGRES_POWERS_BY_STAGE` —
linha ~7525 de docs/swade_csv_livro_dos_mortais.

| Estágio | Poderes (livro) |
|---|---|
| Novato | Ajuda, Aumentar Característica (Aspecto), Cura, Deflexão, Detectar Arcano (Aspecto), Enredar, Iluminar (Aspecto), Proteção Ambiental, Proteção Arcana, Proteção, Som/Silêncio, Visão Sombria |
| Experiente | Atordoar*, Campo de Dano*, Cegar*, Confusão*, Devastação*, Dissipar, Invisibilidade, Morosidade*, Raio*, Reduzir Característica* (Aspecto), Sono, Velocidade (Aspecto) |
| Veterano | Dádiva do Guerreiro†, Explosão†, Ferir†, Medo†, Rajada† |
| Heroico | Adivinhação, Ressurreição |

`*` = requer a Vantagem **Guerreiro do Senhor** para ser usado; `†` = requer **Ira do Senhor**.
`ArcaneConfig.SOL_VAPOR_MILAGRES_POWER_REQUIREMENTS` reproduz 11 desses gates (6 de Guerreiro do
Senhor: atordoar, campo_de_dano, cegar, confusao, devastacao, raio; 5 de Ira do Senhor:
dadiva_do_guerreiro, explosao, ferir, medo, rajada). **[OK, limitação estrutural confirmada em
2026-08-31]**: o livro também amarra "Morosidade" e "Reduzir Característica" a Guerreiro do Senhor,
mas como esses Aspectos compartilham o mesmo id de poder com a variante livre ("Velocidade"/
"Aumentar Característica", sem exigência), o app — que só tem um id por poder — não tem como
aplicar o gate nesse nível de detalhe (mesma limitação de "um id por poder, não um id por Aspecto"
documentada para o Transmorfo em `docs/reports/book_index/scifi.md`); hoje `morosidade_velocidade`
e `aumentar_reduzir_caracteristica` ficam liberados sem
Guerreiro do Senhor. É uma limitação estrutural do modelo de dados, não um erro de digitação — vale
uma decisão consciente do time (ignorar a nuance ou desdobrar o poder por Aspecto).

### Antecedente Arcano (Demônio) — id `aa_demonio`, chave interna `DEMONIO`

[OK, com uma ressalva de fonte] — usa a mesma lista do Feiticeiro (texto do livro: "a maioria dos
poderes disponíveis para demônios são os mesmos que os bruxos usam") somada a 7 poderes exclusivos
de demônio, batendo com `SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE + SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE`
— linha ~2036 de docs/swade_csv_livro_do_criador.

| Estágio | Poder exclusivo de demônio | Confirmação no texto |
|---|---|---|
| Novato | Disfarce Demoniaco (poder inicial, 0 PP) | Explícito: "Estágio: Novato" (linha ~2066) |
| Novato | Elo Mental | [OK, confirmado em 2026-08-31] — ver nota abaixo |
| Experiente | Telecinese | [OK, confirmado em 2026-08-31] — ver nota abaixo |
| Veterano | Voar | [OK, confirmado em 2026-08-31] — ver nota abaixo |
| Veterano | Leitura Mental | Explícito: "Estágio: Veterano" (linha ~2148) |
| Veterano | Limpeza Mental | [OK, confirmado em 2026-08-31] — ver nota abaixo |
| Heroico | Drenar Pontos de Poder | Explícito: "Estágio: Heroico" (linha ~2105) |

Apenas 3 dos 7 poderes exclusivos têm o rótulo "Estágio:" plenamente visível na extração de texto
puro do PDF (Disfarce Demoníaco=Novato, Leitura Mental=Veterano, Drenar Pontos de Poder=Heroico).
Dos outros 4, três ("Elo Mental", "Limpeza Mental", "Telecinese") têm apenas o texto "Como em
Savage Worlds" — sem descrição própria nenhuma — o que, bem lido, já é a resposta: o poder é
idêntico em tudo ao livro básico, **inclusive o Estágio**, e por isso não haveria um rótulo próprio
para repetir. Conferido contra `poderes.json` (entradas `livros: ["BASICO"]`): `elo_mental` =
Novato, `telecinese` = Experiente, `limpeza_mental` = Veterano — os três batem exatamente com os
valores já implementados em `SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE`. O quarto, "Voar", tem
descrição própria (regras de altitude do Limbo) mas nenhum rótulo de Estágio customizado — mesma
lógica de "usa o Estágio padrão salvo indicação em contrário" se aplica, e `poderes.json` confirma
`voar` (`BASICO`) = Veterano, batendo com o valor já implementado. Os 4 valores em `ArcaneConfig`
estão corretos — nenhuma mudança de código necessária.

O livro também documenta uma tabela de Casta → Estágio (Ímpio/Demônio do solo = Novato;
Supervisor/Mercador = Experiente; Ceifador/Guerreiro = Veterano; Mago/Conselheiro = Heroico; Lorde
= Lendário) — é lore de status social do demônio, não um dado usado pelo `ArcaneConfig` (que já
tem estágio Lendário sem poderes associados) — incluída aqui só como contexto, não requer ação.

### Antecedente Arcano (Tecnomagia) — id `aa_tecnomagia` — **[OK, resolvido em 2026-08-31]**

O livro apresenta uma lista de poderes por Estágio para tecnomagos tão detalhada quanto a dos
Feiticeiros/Abençoados (Novato, Experiente, Veterano — não há lista de Heroico) — linha ~7104 de
docs/swade_csv_livro_dos_mortais:

| Estágio | Poderes (livro) |
|---|---|
| Novato | Ajuda, Andar nas Paredes (Ressonância), Atordoar, Aumentar/Diminuir Característica (Ressonância), Cegar, Confusão, Deflexão (Ressonância), Devastação, Detectar Arcano (Óculos, Aspecto), Enredar, Explosão, Iluminar (Aspecto), Medo, Ocultar o Arcano (Aspecto), Proteção (Ressonância), Proteção Arcana (Ressonância), Raio, Som/Silêncio, Visão Sombria (Óculos) |
| Experiente | Campo de Dano (Ressonância), Dissipar, Ferir, Invisibilidade (Ressonância), Morosidade/Velocidade (Ressonância), Sono, Visão Distante (Óculos) |
| Veterano | Limpeza Mental, Sobrecarga |

**Nota:** "Explosão" aparece listada tanto em Novato (linha ~7148) quanto em Experiente (linha
~7201) no texto do livro, sem explicação para a repetição — mantida como Novato (a ocorrência mais
cedo/permissiva) no novo `SOL_VAPOR_TECNOMAGIA_POWERS_BY_STAGE` (`ArcaneConfig.kt`), já que um
`linkedMapOf` só pode ter um valor por chave. Vale conferir contra o PDF original se possível.

Adicionado `SOL_VAPOR_TECNOMAGIA_POWERS_BY_STAGE` em `ArcaneConfig.kt`, um novo caso
`"TECNOMAGIA" -> SOL_VAPOR_TECNOMAGIA_POWERS_BY_STAGE` em `getStageBasedPowersByStage()`, e
`"usaPoderesPorEstagio": true` na Vantagem `aa_tecnomagia` (`vantagens.json`) — o app agora
restringe os poderes de tecnomago por Estágio como o livro exige.

**Achado incidental corrigido:** `aa_tecnomagia` e `aa_milagres` tinham uma entrada **duplicada
byte a byte** em `vantagens.json` (mesmo id, mesmo `livros`, mesma descrição — diferente do padrão
normal do catálogo, em que o mesmo id se repete uma vez por livro onde a Vantagem é reimpressa).
Removida a cópia extra de cada uma.

### Antecedente Arcano (Anjo) — id `aa_anjo` — **[OK, resolvido em 2026-08-31]**

O livro afirma que Anjos usam exatamente a mesma lista de poderes (por Estágio) dos Abençoados
(Milagres), com a diferença de que **não precisam** das Vantagens Guerreiro do Senhor/Ira do Senhor
para acessar os poderes de combate marcados com essas exigências — linha ~2238 de
docs/swade_csv_livro_do_criador ("o conjunto de poderes disponíveis para os anjos e seus efeitos
são os mesmos que o dos abençoados [...] e anjos sem vantagens adicionais podem usar poderes que
abençoados só podem usar com as Vantagens Guerreiro do Senhor ou Ira do Senhor").

Adicionado um novo caso `"ANJO" -> SOL_VAPOR_MILAGRES_POWERS_BY_STAGE` em
`getStageBasedPowersByStage()` (reaproveitando a lista dos Abençoados, sem duplicar dados) e
`"usaPoderesPorEstagio": true` na Vantagem `aa_anjo`. Como `getStageBasedPowerRequirement()` só tem
entradas pra chave `"MILAGRES"` (nunca `"ANJO"`), o comportamento padrão (sem exigência) já
reproduz corretamente a regra do livro — não precisou de código extra além do case acima. Combinado
com a nova ancestralidade Anjo (ver seção de Ancestralidades), Antecedente Arcano (Anjo) agora tem
sustentação mecânica completa no app.

## Equipamento

O catálogo de equipamentos do cenário já está amplamente coberto em `equipamentos.json` sob a tag
`CIDADE_SOL_VAPOR` (armas medievais/modernas padrão de SWADE reaproveitadas, mais as categorias
exclusivas marcadas "(Livro dos Homens)" no arquivo): armas mecânicas (Mini Besta, Besta de Mola),
pistolas e rifles de ar comprimido ("Brisa", "Boreas", "Siroco", "Brownie", "Pixie", "Chinnok",
"Garmsil", "Simoom", "Pampeiro", "Leprechaum"), armas a vapor de mercado negro (Rifle a vapor,
Lança-vapores), as 3 Pistolas Tecnomágicas (Nível 1-3), munição/cilindros de gás, Enxofre (puro,
mistura para caldeira, mistura para armas/armaduras), armaduras (Sobretudo de lã, Peitoral e
Pickelhaube de aço vermelho, Armadura a vapor, Máscara/Conjunto de proteção, Armadura da Força
Expedicionária), armas brancas exclusivas (Faca/Espada/Sabre/Espada-bengala, normais e de aço
vermelho), baterias tecnomágicas (Pequena a Estacionária) e itens do dia a dia da Teia (transporte,
comida, aluguel, salários). Todos [OK].

Itens do suplemento **Movimento Vermelho** [OK — 4/4, resolvido em 2026-08-31]:

- **Rebitadora** — id: `rebitadora` — [OK] — arma de pressão (10/20/40, 2d6, PA2, CdT3, PMF3, Força Mín. d8, 30 tiros), usa bateria tecnomágica média, só no mercado negro — linha ~338 de docs/swade_csv_movimento_vermelho
- **Rebites (100)** — id: `municao_rebites` — [OK] — munição da Rebitadora, custo 25, peso 1 — linha ~359 de docs/swade_csv_movimento_vermelho
- **Broca Compacta** — id: `broca_compacta` — [OK] — ferramenta/arma improvisada (For+d10, PA2, PMF2, Força Mín. d10, mercado negro), alimentada por caldeira a vapor nas costas — linha ~314 de docs/swade_csv_movimento_vermelho
- **Tabaco "Hora Extra"** — id: `tabaco_hora_extra` — [OK] — item consumível (3 sóis); +1 passo em Força/Vigor por 3h, ignora 1 ponto de penalidade de Ferimento/Fadiga, risco de Complicação Hábito (Maior) — adicionado à categoria já existente "Itens Diários (Livro dos Homens)" — linha ~289 de docs/swade_csv_movimento_vermelho

## Poderes

Os poderes-padrão de Savage Worlds usados pelos Antecedentes Arcanos por Estágio (Andar nas
Paredes, Atordoar, Cegar, etc.) já existem em `poderes.json` como poderes genéricos — a nuance de
cada Antecedente está apenas na *lista de quais poderes cada Estágio libera* (ver seção acima), não
em poderes novos. Os poderes exclusivos de Demônio, todos [OK] em `poderes.json`:

- **Disfarce Demoníaco** — id: `disfarce_demoniaco` — [OK] — ilusão de aparência, tabela de tempo/duração/detecção por Estágio — linha ~2065 de docs/swade_csv_livro_do_criador
- **Sobrecarga** — id: `sobrecarga` — [OK] — quebra mecanismos por overload de energia (também poder Veterano de tecnomago) — linha ~7221 de docs/swade_csv_livro_dos_mortais
- **Drenar Pontos de Poder (Demônio)** — id: `drenar_pontos_de_poder_demonio` — [OK] — rolagem resistida de perícia arcana para roubar PP — linha ~2104 de docs/swade_csv_livro_do_criador
- **Voar (Demônio)** — id: `voar_demonio` — [OK] — voo com limites de altitude dos ventos do Limbo — linha ~2137 de docs/swade_csv_livro_do_criador
- **Elo Mental (Demônio)** — id: `elo_mental_demonio` — [OK] — linha ~2145 de docs/swade_csv_livro_do_criador
- **Leitura Mental (Demônio)** — id: `leitura_mental_demonio` — [OK] — rolagem resistida Arcana x Astúcia — linha ~2147 de docs/swade_csv_livro_do_criador
- **Limpeza Mental (Demônio)** — id: `limpeza_mental_demonio` — [OK] — linha ~2164 de docs/swade_csv_livro_do_criador
- **Telecinese (Demônio)** — id: `telecinese_demonio` — [OK] — linha ~2166 de docs/swade_csv_livro_do_criador

O "Novo Modificador de Poder: Duração (+1)" (linha ~6809 de docs/swade_csv_livro_dos_mortais) é uma
regra genérica de modificador de poder, não um poder novo — não há entrada correspondente em
`poderes.json` e provavelmente não precisa de uma (é regra de UI/cálculo, fora do escopo de
catálogo de poderes).

## Fora de escopo

- **Bestiário e antagonistas**: Guardiões Mecânicos/Autômatos Ancestrais, PNJs Comuns (Anjos,
  Criminosos, Demônios, Guardas, Habitantes do Limbo), Personagens Excepcionais por facção, insetos
  gigantes, Homo Odonatus, Oráculos, Prometeico, Combatente/Assassino/Mago das Trevas/Ladrão do
  Culto do Eclipse, Jaqueta Vermelha e Líder de Sindicato (statblocks do Movimento Vermelho).
- **Lore não-mecânica**: Genesis, história do mundo, Guerra do Pecado, Êxodo, Guerra do
  Esquecimento, Salões do Criador, Coletores de Almas, Hierarquia dos Demônios, política da cidade
  (Chanceler, Custodes Umbra, Culto do Eclipse enquanto organização, Embaixada de Ferro, Erin
  Kug-Mush, Governos Mundiais), Ermos do Limbo (fauna/geografia), Arqueologia da cidade, os três
  casos/aventuras completos (Testamento Perdido, Mecanismo Misterioso, Estátua Alada, Casa dos
  Esquecidos), recomendações de mestre, exemplos de grupo e os 10 "Tipos de Personagens" pré-prontos
  (Aventureira, Bandido, Detetive, Erudito, Espiritualista, Inventor, Guarda, Médico, Operário de
  Fábrica, Tecnomago) — são arquétipos combinando Vantagens/Perícias já existentes, não novas
  entradas de catálogo.
- **Sistema de crafting/invenções**: todo o capítulo "Oficina" (Criação de invenções
  magomecânicas, montagem de núcleo de energia, núcleos padrão, consumo de combustível, Armas/
  Transporte/Próteses inventáveis, "Criando as suas próprias modificações", "Criando invenções
  únicas", Autômato-Assistente/Autômato-Secretário, a "Base dos núcleos" com engrenagens, caldeiras,
  plantas tecnomágicas, componentes mecânicos/a vapor/tecnomágicos e conectores) — por definição do
  escopo desta tarefa, o catálogo comprável fica fixo e não modela esse sistema de montagem.
- **Tesouros de Masmorra, Armadilhas, Salas com Desafios** (Livro do Criador) — ferramentas de
  Mestre para masmorras genéricas, não criação de personagem.
- **Regras de Sindicato / Manifestações** (Movimento Vermelho, Apoio Popular/Conflito/Acordo) — são
  regras de cena de Interlúdio e Batalha em Massa ligadas à Vantagem Sindicalizado (já [OK]), não
  entradas de catálogo por si.
