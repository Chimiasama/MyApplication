# Mapeamento de conteúdo dos livros (criação de personagem)

## Objetivo

Este documento é o índice mestre de referência entre os livros do SWADE
(`docs/swade_*`, dumps de texto puro extraídos dos PDFs, sem numeração de
página) e os catálogos estruturados que o app usa (`app/src/main/assets/*.json`
+ código de domínio em `app/src/main/java/.../model`).

Serve para duas coisas:

1. **Pesquisa rápida.** Quando surgir dúvida sobre uma regra ("qual é o custo
   de X", "esse Antecedente Arcano existe no livro Y?"), em vez de abrir o
   dump de texto do zero, consulte primeiro o índice do livro correspondente
   em `docs/reports/book_index/<book_id>.md` — ele tem a localização
   aproximada (linha) de cada entrada no dump original.
2. **Base para limpeza de hardcode.** Cada entrada mapeada tem um `id`
   canônico sugerido (snake_case), que é o único identificador que o código
   Kotlin deveria usar para reconhecer aquela vantagem/complicação/perícia/
   ancestralidade/poder — nunca o nome exibido (`nome`), que é texto
   traduzido e pode variar entre livros/edições.

## Escopo: o que É mapeado

Somente conteúdo que afeta **criação e evolução de personagem jogável**:

- Atributos (regras de criação, novos atributos se houver)
- Perícias (lista, regras de vínculo a atributo, variantes tipo Jutsu)
- Vantagens / Edges (incluindo Antecedente Arcano e todos os equivalentes
  "místicos" de cada gênero — Magia, Milagres, Psiônicos, Ciência Estranha,
  Dom, Jutsu/Chi, Antecedente Arcano por Estágio, Superpoderes, etc.)
- Complicações / Hindrances
- Ancestralidades / Raças jogáveis (incluindo templates de "Monstro
  Heroico" quando jogáveis, ex.: Horror)
- Equipamento comprável (armas, armaduras, itens gerais, veículos, itens
  arcanos/relíquias prontas — catálogo fixo, não sistema de fabricação)
- Poderes (lista de poderes arcanos) e Super Poderes (sistema próprio do
  Compêndio de Superpoderes)
- Subsistemas de build específicos de cenário quando funcionam como opção
  de personagem (Tropos, Corações, Cibernéticos, Mechas, Jutsus)

## Escopo: o que NÃO é mapeado (fora de escopo)

- **Bestiário** — criaturas/NPCs como antagonistas de mestre
- **Criação de fortaleza/castelo/reino/base** — sistemas de mesa, não de
  ficha individual
- **Criação de item mágico/artefato** — sistema de fabricação (o catálogo
  fixo de itens prontos para comprar continua dentro do escopo)
- Conselhos de mestre, lore de cenário não-mecânico, regras de combate em
  massa/batalha em massa, sistemas de nau/base como "personagem coletivo"

Cada índice por livro tem uma seção final "Fora de escopo" listando o que
foi identificado e propositalmente não detalhado.

## Esquema de identificadores (IDs e tags)

### BookId — identifica o arquivo-fonte físico

| BookId | Arquivo (`docs/`) | Linhas | Livro |
|---|---|---:|---|
| `BASICO` | `swade_basico` | ~18149 | Livro Básico do SWADE (core) |
| `FANTASIA` | `swade_fantasia` | ~22667 | Compêndio de Fantasia |
| `SCIFI` | `swade_scifi` | ~23412 | Compêndio de Ficção Científica |
| `HORROR` | `swade_horror` | ~17317 | Compêndio de Horror |
| `SUPERPODERES` | `swade_superpoderes` | ~15732 | Compêndio de Superpoderes |
| `PATHFINDER_BASICO` | `swade_pathfinder_basico` | ~20058 | Pathfinder para Savage Worlds — livro básico (Golarion) |
| `PATHFINDER_COMPENDIO` | `swade_pathfinder_compendio` | ~21539 | Compêndio de Golarion |
| `DEADLANDS_BASICO` | `swade_deadlands` | ~16884 | Deadlands — livro básico |
| `DEADLANDS_COMPENDIO` | `swade_deadlands_compendio` | ~8606 | Deadlands — compêndio |
| `ADG_BASICO` | `swade_adg` | ~15112 | Arte da Guerra |
| `ADG_DIARIO_KUI` | `swade_adg_diario_do_kui` | ~1474 | Arte da Guerra — Diário do Kui |
| `CRYSTAL_HEART` | `swade_crystal_heart` | ~18469 | Crystal Heart |
| `CRYSTAL_HEART_MUITOS_CORACOES` | `swade_crystal_heart_muitos_coracoes` | ~734 | Crystal Heart — Muitos Corações |
| `WISEGUYS` | `swade_wiseguys_jogador` | ~5583 | Wiseguys — livro do jogador |
| `CSV_LIVRO_CRIADOR` | `swade_csv_livro_do_criador` | ~9886 | Cidade do Sol a Vapor — Livro do Criador |
| `CSV_LIVRO_MORTAIS` | `swade_csv_livro_dos_mortais` | ~10190 | Cidade do Sol a Vapor — Livro dos Mortais |
| `CSV_MOVIMENTO_VERMELHO` | `swade_csv_movimento_vermelho` | ~814 | Cidade do Sol a Vapor — O Movimento Vermelho |

### ModuleId — tag grosseira já usada em `livros: [...]` nos JSONs

Definida em `app/src/main/java/.../model/ids/DomainIds.kt` (`ModuleIds`).
Vários BookIds mapeiam para o mesmo ModuleId (livro básico + compêndio do
mesmo cenário compartilham a tag):

| ModuleId | BookIds que alimentam |
|---|---|
| _(sem tag / universal)_ | `BASICO` |
| `FANTASIA` | `FANTASIA` |
| `SCI_FI` (alias `FC`, `SCIFI`) | `SCIFI` |
| `HORROR` | `HORROR` |
| `SUPER` | `SUPERPODERES` |
| `PATHFINDER` | `PATHFINDER_BASICO`, `PATHFINDER_COMPENDIO` |
| `DEADLANDS` | `DEADLANDS_BASICO`, `DEADLANDS_COMPENDIO` |
| `ARTE_DA_GUERRA` | `ADG_BASICO`, `ADG_DIARIO_KUI` |
| `CRYSTAL_HEART` | `CRYSTAL_HEART`, `CRYSTAL_HEART_MUITOS_CORACOES` |
| `CIDADE_SOL_VAPOR` | `CSV_LIVRO_CRIADOR`, `CSV_LIVRO_MORTAIS`, `CSV_MOVIMENTO_VERMELHO` |
| `WISEGUYS` | `WISEGUYS` |

### Status por entrada (usado dentro de cada índice por livro)

- `[OK]` — já existe no asset JSON correspondente e parece correto
- `[FALTA]` — não encontrado em nenhum asset; candidato a criar
- `[CONFERIR]` — existe, mas custo/requisito/efeito precisa ser confirmado
  linha a linha contra o livro

## Índices por livro

Todos os 11 índices concluídos. Contagem por status de cada entrada mapeada
(`[OK]` já existe corretamente no app / `[FALTA]` não existe / `[CONFERIR]`
existe mas precisa validação linha a linha):

| BookId | Índice | OK | FALTA | CONFERIR |
|---|---|--:|--:|--:|
| `BASICO` | [`book_index/basico.md`](reports/book_index/basico.md) | 319 | 1 | 4 |
| `FANTASIA` | [`book_index/fantasia.md`](reports/book_index/fantasia.md) | 158 | 1 | 7 |
| `SCIFI` | [`book_index/scifi.md`](reports/book_index/scifi.md) | 169 | 0 | 9 |
| `HORROR` | [`book_index/horror.md`](reports/book_index/horror.md) | 179 | 0 | 10 |
| `SUPERPODERES` | [`book_index/superpoderes.md`](reports/book_index/superpoderes.md) | 125 | 3 | 3 |
| `PATHFINDER_BASICO` / `PATHFINDER_COMPENDIO` | [`book_index/pathfinder.md`](reports/book_index/pathfinder.md) | 67 | 1 | 2 |
| `DEADLANDS_BASICO` / `DEADLANDS_COMPENDIO` | [`book_index/deadlands.md`](reports/book_index/deadlands.md) | 126 | 0 | 4 |
| `ADG_BASICO` / `ADG_DIARIO_KUI` | [`book_index/adg.md`](reports/book_index/adg.md) | 171 | 1 | 0 |
| `CRYSTAL_HEART` / `CRYSTAL_HEART_MUITOS_CORACOES` | [`book_index/crystal_heart.md`](reports/book_index/crystal_heart.md) | 116 | 0 | 8 |
| `CSV_LIVRO_CRIADOR` / `CSV_LIVRO_MORTAIS` / `CSV_MOVIMENTO_VERMELHO` | [`book_index/csv.md`](reports/book_index/csv.md) | 76 | 0 | 6 |
| `WISEGUYS` | [`book_index/wiseguys.md`](reports/book_index/wiseguys.md) | 99 | 1 | 0 |
| **Total** | | **1605** | **8** | **53** |

**Leitura rápida:** todos os 11 livros mapeados estão praticamente fechados —
0-1 `[FALTA]` cada, exceto Superpoderes (3), que já eram considerados baixa
prioridade desde a varredura original. Não sobra nenhuma lacuna de conteúdo
volumosa; o que resta é revisar os 53 `[CONFERIR]` espalhados pelos índices
(item 6 da lista de prioridades, abaixo).

**Sci-Fi resolvido (2026-08-31, item 5 da lista de prioridades):** dos 56 itens
`[FALTA]` reais do relatório, 9 foram marcados `[FORA DE ESCOPO]` (7 "Robôs
Padrão" — blocos de stats de aliado contratável, mesma categoria de bestiário —
mais os 2 sistemas de montagem "Construção de Robôs"/"Sistema de Veículos
Customizados", equivalentes a "criação de item", ambos fora do que você pediu
pra mapear) e os **47 restantes foram adicionados** a
`app/src/main/assets/equipamentos.json`: 21 veículos prontos (`Veículos /
Customizados (Sci-Fi)`, incl. um "Submarino de Ataque" que a varredura original
não tinha achado) e 26 itens de `Itens Especiais / Relíquias e Artefatos` (1
regra + 25 relíquias nomeadas, incl. "Fragmento de Ka'han" que também não
constava na varredura original). Conteúdo transcrito por agente a partir do
texto de `docs/swade_scifi` e conferido manualmente (spot-check linha a linha
contra o livro) antes de mesclar — ver `docs/reports/book_index/scifi.md` pra
cada id e localização.

**Horror resolvido (2026-08-31, item 5 da lista de prioridades):** os 7
`[FALTA]` do Compêndio de Horror eram todos regras de resolução em jogo, não
opções compráveis na criação — categoricamente diferentes dos itens
resolvidos nos outros livros (Vantagens/Equipamento/Poderes/Ancestralidade).
Marcados `[FORA DE ESCOPO]`: a regra "Criando Itens Alquímicos" (sistema de
crafting, mesma categoria já excluída em toda a sessão — os itens resultantes
já estavam cadastrados) e o Sistema de Medo e Trauma completo (Tabela Efeitos
de Medo D20, Tabela Psicoses Aleatórias D12, regra de Colapso/Surto Psicótico,
regras de tratamento de Psicoses, e o recurso RAIVA do Monstro Heroico, que
depende desse mesmo sistema) — todos são sorteios/rastreamento conduzidos
pelo Mestre durante a campanha, não dados de ficha na criação; as
Complicações que a tabela de Psicoses pode gerar já existem todas
individualmente em `complicacoes.json`. Nenhuma mudança de código ou catálogo
neste item — só documentação. Ver `docs/reports/book_index/horror.md`.

**CSV resolvido (2026-08-31, item 5 da lista de prioridades):** os 14 `[FALTA]`
do cenário Cidade do Sol a Vapor eram um mix de dados de catálogo e de código.
Dados: a ancestralidade **Anjo** (`anc_anjo_csv`, com os traços raciais "Asas
de Anjo" e "Recluso"), que já era pré-requisito de 3 Vantagens cadastradas mas
inexistente como ancestralidade selecionável; **8 Vantagens** de organizações/
sociedades secretas da Teia (Irmandade das Seis Chaves, Clapper Branco,
Clapper do Carvão, Culto do Eclipse, Sociedade do Noroeste, A Pena do
Albatroz, Adepto da Ordem do Albatroz, Cavaleiro de São Germain); e **4 itens
de equipamento** do suplemento Movimento Vermelho (Rebitadora, Rebites,
Broca Compacta, Tabaco "Hora Extra"). Código: os Antecedentes Arcanos
(Tecnomagia) e (Anjo) tinham listas de poderes por Estágio documentadas no
livro mas não implementadas — `ArcaneConfig.kt` só reconhecia `MILAGRES`,
`FEITICEIRO` e `DEMONIO`. Adicionado `SOL_VAPOR_TECNOMAGIA_POWERS_BY_STAGE`
(27 poderes em 3 Estágios) e um novo caso `"ANJO" -> SOL_VAPOR_MILAGRES_POWERS_BY_STAGE`
(Anjo reaproveita a lista dos Abençoados, mas sem exigir Guerreiro do
Senhor/Ira do Senhor — resolvido de graça, já que `getStageBasedPowerRequirement`
nunca teve entrada pra `"ANJO"`), com `usaPoderesPorEstagio: true` marcado nas
duas Vantagens em `vantagens.json`. Achado incidental corrigido: `aa_tecnomagia`
e `aa_milagres` tinham uma cópia duplicada byte a byte em `vantagens.json`
(mesmo id, mesmo livro, mesmo texto — diferente do padrão normal de repetir o
mesmo id uma vez por livro); removida a duplicata de cada uma. Cobertura de
teste nova em `ArcaneConfigStageBasedTest.kt`. Ver
`docs/reports/book_index/csv.md` pra cada id, localização e a ressalva sobre
"Explosão" aparecer duas vezes na lista de poderes de Tecnomagia no livro.

**Achado incidental não corrigido (fora do escopo de CSV):** durante a
limpeza de duplicatas acima, uma varredura geral encontrou que a Vantagem
`aristocrata` tem 2 cópias com `livros: ["ARTE_DA_GUERRA"]` e texto
**diferente** entre si (521 vs. 319 caracteres) — provável divergência de
conteúdo, não simples duplicata. Não investigado nem corrigido por ser do
livro Arte da Guerra, não Cidade do Sol a Vapor; registrado aqui como
candidato a follow-up se o time quiser uma auditoria geral de duplicatas do
catálogo.

**Crystal Heart resolvido (2026-08-31, item 5 da lista de prioridades):** os
33 Cristais faltantes do "Apêndice A: Mais Cristais" (`docs/swade_crystal_heart`,
linhas 16641–18287) foram transcritos e mesclados em
`app/src/main/assets/crystal_coracoes.json` (38 → 71 registros). A determinação
do Estágio (Novato/Experiente/Veterano/Heroico) de cada Cristal por posição no
texto (entre os rodapés de seção do apêndice) foi cruzada com o capítulo de
Agentes-exemplo do livro (linhas 3400–3900), que cita o Estágio de vários
Cristais nomeados em prosa direta. Isso revelou que a seção do apêndice tem um
artefato real de extração em duas colunas (já visível no texto de Distorção/
Nada, cujos poderes foram impressos intercalados) e que 4 Cristais
(`heart_ausencia`, `heart_fera`, `heart_reverte`, `heart_aurora`) tinham Estágio
diferente do que a posição no texto sugeria — corrigidos para bater com a
citação direta ("Jordan carrega Ausência (Veterano)", "Marc carrega Besta [=
Fera] (Veterano)", "Reverte (Heroico) permite a Ivan...", "Aurora (Heroico), o
Cristal que Yurhant usa..."). Distribuição final: 3 Novato, 11 Experiente, 13
Veterano, 6 Heroico. Ver `docs/reports/book_index/crystal_heart.md` para cada
id, localização e a justificativa completa de cada correção de Estágio
(campo `_duvida_estagio` no JSON).

## Auditoria de hardcode (nome-texto em vez de ID)

Relatórios separados, cobrindo o código Kotlin de domínio (leitura/análise
apenas, nenhum código foi alterado):

| Relatório | Escopo | Achados |
|---|---|--:|
| [`reports/hardcode_audit_vantagens_complicacoes.md`](reports/hardcode_audit_vantagens_complicacoes.md) | Vantagens, Complicações, Antecedente Arcano (incl. variantes por livro) | 39 (21 ALTA, 13 MÉDIA, 5 BAIXA) |
| [`reports/hardcode_audit_atributos_pericias_equipamento_ancestralidade.md`](reports/hardcode_audit_atributos_pericias_equipamento_ancestralidade.md) | Atributos, Perícias, Equipamento, Ancestralidade | 47 (27 ALTA, 17 MÉDIA, 3 BAIXA) |

**Achado estrutural (não é só um bug de código):** `pericias.json` e
`equipamentos.json` não têm campo `id` nenhum — só `nome`. Isso significa que
parte do hardcode nessas duas categorias não pode ser corrigida só trocando
a comparação no Kotlin; o catálogo JSON precisa ganhar um `id` estável
primeiro, do mesmo jeito que `vantagens.json`/`complicacoes.json` já têm.

### Padrão-alvo confirmado (exemplo de referência)

`DerivedAttributesCalculator.kt::valorAparar` calcula o Aparar como
`2 + (Lutar / 2) + bônus`, e os bônus (Bloquear, Bloquear Aprimorado,
Superpoder Aparar) já existem — mas o "Bloquear" é resolvido comparando
`it.nome.keyify() == Constants.EDGE_BLOCK.keyify()` (nome de exibição
normalizado) em vez do campo `id` estável da `Vantagem`. Isso é o padrão
exato que os relatórios de auditoria acima devem catalogar em todo o
domínio de Vantagens/Complicações/Perícias/Atributos/Equipamento/
Ancestralidade: qualquer decisão mecânica que hoje lê `nome`/texto livre
deveria passar a ler `id`.

## Prioridades sugeridas

Ordem sugerida de ataque, do que parece mais valioso/barato para o mais caro:

1. ✅ **Terminar migrações "meio feitas"** — feito (commit `9293f4e`). 14
   novas `Constants.ID_*` (conferidas contra `vantagens.json`/
   `complicacoes.json`) e toda comparação por nome restante trocada por `id`
   para Bloquear, Bloquear Aprimorado, Ligeiro, Musculoso, Brutamontes,
   Brigão (+ alias Pugilista), Soldado, Profissional, Especialista, Idoso,
   Lento, Obeso, Pequeno — em `ModifierEngine.kt`, `DerivedAttributesCalculator.kt`,
   `RequirementValidator.kt`, `CriadorState.kt`, `PericiaRules.kt`,
   `ResumoSection.kt`, `UnifiedScreen.kt`, e o `calcAparar` duplicado de
   `SummaryUtils.kt`/`ResumoPdfReferenciador.kt` (achados que a auditoria
   original não tinha coberto, pois esses arquivos não estavam na lista dos
   agentes de auditoria). Build não pôde ser verificado neste sandbox (AGP
   inacessível pela rede) — revisão só manual, recomendo rodar
   `./gradlew build`/testes antes de dar como fechado.
2. ✅ **Bug de "O Melhor Que Há"** — feito (commit `4423102`). Achado real,
   não só estilo: a regra comparava `v.nome.keyify()` (`"O MELHOR QUE HA"`)
   contra a constante em formato de id (`"o_melhor_que_ha"`) — nunca batia,
   então a vantagem podia ser comprada sem nenhum investimento em Superpoder
   e durante progressão, ao contrário de toda outra checagem da mesma
   vantagem no resto do código. Corrigido nos **dois** lugares onde o mesmo
   bug existia: `RequirementValidator.canSelect` (usado no diálogo de
   progressão) e `ValidateSpecialRulesUseCase.execute` (usado por
   `ValidateSelectionUseCase`, o validador real da seleção de vantagens
   durante a criação de personagem).
   - **Achado novo durante a investigação:** existe uma segunda camada de
     validação inteira em `app/src/main/java/.../model/usecase/`
     (~37 arquivos `Validate*UseCase`/`Resolve*UseCase`) que nenhuma das
     duas auditorias de hardcode cobriu, porque essa pasta não estava na
     lista de arquivos passada aos agentes. `ValidateSpecialRulesUseCase`
     era quase uma cópia de `RequirementValidator.canSelect` com o mesmo
     bug.
3. ✅ **Auditoria da pasta `model/usecase/`** — feita
   ([`reports/hardcode_audit_usecase_layer.md`](reports/hardcode_audit_usecase_layer.md)).
   27 achados Tipo A (hardcode por nome, mesmo padrão de sempre) + **6
   achados Tipo B (duplicação/divergência com `model/`)**. O mais grave: o
   mapa de incompatibilidades entre Vantagens/Complicações existe em
   **3 cópias** (`ValidateConflictsUseCase.kt` com 20 entradas,
   `RequirementValidator.kt` com só 7, `CriadorState.kt` com 20 iguais às do
   UseCase mas usada só pra mensagem de erro) — e as duas cópias que
   **decidem** algo (não só exibem mensagem) **já divergem hoje**: dá pra
   pegar Antecedente Arcano (Milagres) tendo Alma Penhorada/Vendida durante
   a progressão de nível (`ProgressosDialog`), embora isso seja bloqueado
   na criação do personagem. Mesmo padrão achado na regra de
   Cavaleiro/Obrigação Maior e na exceção de Estágio de Liderança do
   Samurai (Arte da Guerra) — ambas existem só no validador de criação, não
   no de progressão. Causa raiz: **criação de personagem e progressão de
   nível usam dois validadores diferentes e vivos**
   (`ValidateSelectionUseCase` vs. `RequirementValidator.canSelect`), que
   precisam ser sincronizados manualmente a cada mudança de regra — foi
   exatamente essa duplicação que já causou o bug do "O Melhor Que Há"
   (item 2 acima).
   - **(a) feito** (commit `ccea1dc`): as 3 cópias viraram uma fonte única,
     `model/IncompatibilityRules.kt`, usada por `ValidateConflictsUseCase`,
     `RequirementValidator` e `CriadorState.mensagemConflitoPara*`. Ao
     conferir cada entrada linha a linha contra o catálogo e contra o texto
     real de Cidade do Sol a Vapor (`docs/swade_csv_livro_dos_mortais`),
     descobri que o problema era maior do que o relatório apontou: **5 das
     10 duplas de conflito nunca funcionaram em NENHUMA das 3 cópias**
     (Alma Penhorada/Vendida × Antecedente Arcano Milagres, Maldição do
     Gremlin × Antecedente Arcano Tecnomagia, Tecnofobia × Tarô
     Engenheiro/Mestre das Caldeiras/Mecânico Cego) — as chaves usavam
     prefixo `"COMP "` e texto `"ANTECEDENTE ARCANO X"` com espaço, que
     nunca batem contra os ids reais (`comp_alma_penhorada`,
     `antecedente_arcano_milagres`, snake_case) nem contra os nomes reais
     (que têm parênteses, e `keyify()` não remove parênteses). Ou seja, não
     era só risco de divergência futura — metade das regras do livro já
     estavam silenciosamente sem efeito em todo o app, não só na
     progressão. A correção também incluiu as duas variantes de Crystal
     Heart (`lento_ch`, `inimigo_ch`) que nenhuma das 3 cópias cobria.
     Isso já resolve **(c)** também (a entrada morta `"TARO ENGENHEIRO"`
     virou o id real `taro_engenheiro`). Testes novos em
     `IncompatibilityRulesTest.kt` cobrindo as duas direções de cada par.
   - **(d) feito** (commit `d592185`): `scifiVariantDrivenKeys` (21 ids)
     virou `AncestryVariantRegistry.scifiVariantDrivenKeys` (público), em
     vez de cópias idênticas em `CriadorState.kt` e
     `ResolveAncestrySpecificAdjustmentsUseCase.kt`.
   - **(b) parcialmente feito** (commit `d592185`): investiguei unificar
     `ProgressosDialog` com o validador de criação e decidi não fazer a
     fusão completa às cegas. `RequirementValidator` também é chamado por
     `strictRequirementsOk(v, estIndex)`, que faz uma checagem própria de
     "essa vantagem ficaria disponível no Estágio X que estou só
     pré-visualizando" comparando `estIndex` direto — diferente do jeito
     que `RequirementValidator`/`podeSelecionar` checam estágio (só sabem o
     Estágio atual real do personagem, ou o override específico de compra
     via XP). Trocar `RequirementValidator` por `state.podeSelecionar()`
     puro faria essa checagem de estágio passar a usar sempre o Estágio
     atual — não dá pra confirmar se isso quebra a pré-visualização de
     Estágios futuros sem rodar o app de verdade, o que não é possível
     neste sandbox. Em vez disso, portei as 2 regras que estavam faltando e
     são seguras de adicionar (não mexem em lógica de estágio): Cavaleiro
     exige Obrigação (Maior), e Tiro Duplo Aprimorado exige Tiro Duplo com
     perícia escolhida em d10+. Ficou de fora, documentado como risco a
     investigar com o app rodando antes de mexer: a exceção de Estágio de
     Liderança do Samurai (mexe direto na lógica de estágio que estou
     evitando tocar às cegas), e todo o conteúdo de
     `ValidateScenarioRulesUseCase` (bloqueios específicos de Crystal
     Heart/Fantasia/Pathfinder) que `RequirementValidator` nunca teve —
     nenhum dos dois afeta a seleção de verdade hoje (o gate real já usa
     `state.podeSelecionar()` em paralelo), só a precisão da
     pré-visualização nas abas de Estágio.
4. ✅ **Dar `id` estável a `pericias.json` e `equipamentos.json`** — feito
   (commit `c43a382`). Id gerado como slug (minúsculo, sem acento,
   `snake_case`) do `nome`, mesmo padrão de `vantagens.json`/
   `complicacoes.json`: 66 perícias → 42 ids únicos (zero colisão), 3243
   itens de equipamento em 376 categorias → 1609 ids únicos (as 23
   "colisões" eram todas o mesmo item com capitalização/acento
   inconsistente entre impressões de livro — ex. "Cavalo de guerra" vs
   "Cavalo de Guerra" — então compartilhar id ali é o comportamento
   correto, não um bug). Conferido programaticamente que remover o campo
   `id` reproduz o JSON original byte a byte (diff é só uma linha nova por
   registro). Adicionado `id: String = ""` (com valor padrão, no fim da
   lista de parâmetros pra não quebrar nenhuma chamada por posição) em
   `EquipamentoItem`, `Pericia`, `PericiaJson` e no `PericiaFonte` privado
   do `DataLoader`, com o id passando pelo pipeline
   Fonte→PericiaJson→Pericia. **Só adiciona o campo — nenhum código
   consumidor foi migrado para usar `.id` em vez de `.nome` ainda**; isso
   é o próximo passo natural (as fórmulas de Aparar/Movimentação/Tamanho
   racial, armas naturais etc. continuam comparando por nome/regex até
   alguém fazer essa migração, agora que o id existe pra elas usarem).
5. ✅ **Fechar os `[FALTA]` de conteúdo**, por ordem de volume: ~~Sci-Fi (57)~~,
   ~~Deadlands (36)~~, ~~Crystal Heart (33)~~, ~~CSV (14)~~ e ~~Horror (7)~~ —
   todos feitos (ver acima e abaixo).

**Deadlands resolvido (2026-08-31):** os 36 `[FALTA]` eram todas Vantagens de
árvores de arquétipo (Abençoado, Agente, Atormentado ×2 seções, Cientista
Louco, Mascate, Mestre do Chi, Patrulheiro Territorial, Xamã) + 1 poder novo +
1 modificador de poder — sem ambiguidade de escopo (tudo build de personagem,
nada de bestiário/crafting). Adicionadas 34 Vantagens a `vantagens.json`, o
poder `abrir_portal` a `poderes.json`, e o modificador "Visão Espiritual (+5)"
ao array `modificadores` do poder `detectar_ocultar_arcano` (cópia tagueada
DEADLANDS). Ao ler o livro, o agente corrigiu vários dados que a varredura
original tinha errado (ex.: Fé Verdadeira é Novato, não Veterano; Pessoa de
Mil Faces é Experiente, não Heroico; Rebanho e Favor do Espírito são
repetíveis) e confirmou que `implacavel_atormentado` tem mecânica realmente
diferente de `implacavel` (HORROR), então o id próprio estava certo. Conferi
manualmente (Fé Verdadeira, Abrir Portal e Visão Espiritual) contra o texto
antes de mesclar — bateu tudo. Ver `docs/reports/book_index/deadlands.md` pra
cada id e localização.
6. Só depois, revisar os `[CONFERIR]` (custo/requisito/efeito a bater linha a
   linha com o livro) — risco menor que os itens acima, mas ainda vale para
   fechar o ciclo de confiabilidade.

Nenhuma dessas ações foi executada nesta rodada — são só recomendações. Qual
delas atacar primeiro é decisão sua.

## Como este documento evolui

Depois que os índices por livro e as auditorias de hardcode estiverem
prontos, o próximo passo é transformar os achados `[FALTA]`/`[CONFERIR]`
em itens de trabalho concretos (parecido com
`docs/racial_trait_migration_plan.md`, mas para Vantagens/Complicações/
Perícias/Atributos/Equipamento) — cada item vira um PR pequeno que troca
comparação por nome por comparação por `id`, sem mudar comportamento
visível, com testes de regressão antes/depois.
