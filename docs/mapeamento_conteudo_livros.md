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
| `SCIFI` | [`book_index/scifi.md`](reports/book_index/scifi.md) | 122 | **57** | 9 |
| `HORROR` | [`book_index/horror.md`](reports/book_index/horror.md) | 179 | 7 | 3 |
| `SUPERPODERES` | [`book_index/superpoderes.md`](reports/book_index/superpoderes.md) | 125 | 3 | 3 |
| `PATHFINDER_BASICO` / `PATHFINDER_COMPENDIO` | [`book_index/pathfinder.md`](reports/book_index/pathfinder.md) | 67 | 1 | 2 |
| `DEADLANDS_BASICO` / `DEADLANDS_COMPENDIO` | [`book_index/deadlands.md`](reports/book_index/deadlands.md) | 88 | **36** | 4 |
| `ADG_BASICO` / `ADG_DIARIO_KUI` | [`book_index/adg.md`](reports/book_index/adg.md) | 171 | 1 | 0 |
| `CRYSTAL_HEART` / `CRYSTAL_HEART_MUITOS_CORACOES` | [`book_index/crystal_heart.md`](reports/book_index/crystal_heart.md) | 83 | **33** | 8 |
| `CSV_LIVRO_CRIADOR` / `CSV_LIVRO_MORTAIS` / `CSV_MOVIMENTO_VERMELHO` | [`book_index/csv.md`](reports/book_index/csv.md) | 62 | 14 | 6 |
| `WISEGUYS` | [`book_index/wiseguys.md`](reports/book_index/wiseguys.md) | 99 | 1 | 0 |
| **Total** | | **1473** | **155** | **46** |

**Leitura rápida:** o Livro Básico, Fantasia, Arte da Guerra e Wiseguys estão
praticamente 100% cobertos (0-1 `[FALTA]`, os módulos mais antigos/maduros do
app). As maiores lacunas de conteúdo real estão em **Sci-Fi (57 faltando)**,
**Deadlands (36 faltando)** e **Crystal Heart (33 faltando)** — ver o motivo
resumido em cada arquivo de índice antes de decidir prioridade.

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
   (item 2 acima). Recomendação do relatório, em ordem: (a) unificar o mapa
   de incompatibilidades numa fonte única por id; (b) fazer
   `ProgressosDialog` usar `ValidateSelectionUseCase` em vez de
   `RequirementValidator` (ou o inverso, virando um wrapper fino do outro);
   (c) remover a entrada morta `"TARO ENGENHEIRO"` do mapa (não corresponde
   a nenhum id real); (d) consolidar a lista `scifiVariantDrivenKeys`
   duplicada entre `ResolveAncestrySpecificAdjustmentsUseCase.kt` e
   `CriadorState.kt`. Nada disso foi implementado ainda.
4. **Dar `id` estável a `pericias.json` e `equipamentos.json`** — pré-requisito
   estrutural antes de conseguir limpar boa parte do hardcode de
   Perícias/Equipamento/Ancestralidade (armas naturais, Aparar, Movimentação,
   Tamanho racial hoje dependem de regex sobre texto livre).
5. **Fechar os `[FALTA]` de conteúdo**, por ordem de volume: Sci-Fi (57),
   Deadlands (36), Crystal Heart (33), CSV (14), Horror (7).
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
