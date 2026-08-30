# Auditoria: comparação por nome vs. id em Vantagens, Complicações e Antecedente Arcano

Auditoria somente-leitura do código Kotlin relacionado a Vantagens (Edges), Complicações e
Antecedente Arcano (incluindo variantes de livro: Jutsu/Arte da Guerra, AA por estágio de
Cidade do Sol a Vapor, Superpoderes), procurando lógica que decide algo comparando
`.nome`/`.name` (texto exibido, traduzido) em vez do campo `id` estável do catálogo JSON.

Nenhum código foi alterado. Todos os `id` canônicos abaixo foram confirmados via grep direto em
`app/src/main/assets/vantagens.json` / `complicacoes.json`.

## Resumo

- **Total de achados: 39**
- Por severidade: **ALTA: 21** · **MÉDIA: 13** · **BAIXA: 5**
- Por arquivo:
  - `CriadorState.kt`: 16
  - `RequirementValidator.kt`: 8
  - `ModifierEngine.kt`: 6
  - `DerivedAttributesCalculator.kt`: 4 (já confirmados no prompt, incluídos para referência)
  - `SummaryUtils.kt`: 2
  - `SuperPoder.kt` / uso em `CriadorState.kt`: 1 (achado estrutural — modelo sem `id`)
  - `RacialGrantResolver.kt`: 1 (achado estrutural — padrão aceito, ver nota)
  - `AncestryVariantRegistry.kt`: 1 (achado estrutural — padrão aceito, ver nota)
  - `ArcaneConfig.kt`: 1 (chave sintética interna, baixo risco)

Arquivos auditados sem achados relevantes (já orientados a `id`): `Vantagem.kt`,
`complicacoes.kt`, `ArcanoInfo.kt`, `SuperInvestment.kt`, `RacialTraitPointCatalog.kt`,
`ResumoPdfReferenciador.kt`, `CriadorViewModel.kt`.

## Achados ALTA severidade

| # | Arquivo:linha | Trecho | Compara (nome/texto) | Deveria comparar (id) | id canônico |
|---|---|---|---|---|---|
| 1 | `DerivedAttributesCalculator.kt:33,35` | `state.vantagensSelecionadas.any { it.nome.keyify() == Constants.EDGE_BLOCK.keyify() }` (e `EDGE_IMPROVED_BLOCK`) | nome vs constante de texto | `it.id == "bloquear"` / `"bloquear_aprimorado"` | `bloquear`, `bloquear_aprimorado` |
| 2 | `DerivedAttributesCalculator.kt:51` | `.desvantagens?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }` | `.contains()` de texto livre numa lista de strings de descrição | id de traço racial (`RacialTraitPointCatalog` já tem `MOVIMENTACAO_REDUZIDA`) | `MOVIMENTACAO_REDUZIDA` (traço) |
| 3 | `DerivedAttributesCalculator.kt:87` | `state.vantagensSelecionadas.any { it.nome.keyify() == Constants.EDGE_FLEET_FOOTED.keyify() }` | nome vs constante | `it.id == "ligeiro"` | `ligeiro` |
| 4 | `RequirementValidator.kt:34,162` | `v.nome.contains(Constants.EDGE_POWER_POINTS, ignoreCase = true)` | `.contains()` de texto no nome, usado 2x pra travar limite de compra de PP | `v.id == "pontos_de_poder"` | `pontos_de_poder` |
| 5 | `RequirementValidator.kt:47,54` | `key.startsWith(Constants.EDGE_ARCANE_BACKGROUND)` / `it.nome.keyify().startsWith(...)` | prefixo de nome ("ANTECEDENTE ARCANO") pra detectar qualquer vantagem de AA | `v.grupoId == Constants.ID_AA_PREFIX` — campo `grupoId` já existe em **toda** entrada de AA no JSON (`"antecedente_arcano"`) exatamente pra isso | `grupoId = "antecedente_arcano"` |
| 6 | `RequirementValidator.kt:71,82` | `key == Constants.EDGE_PROFESSIONAL.keyify()` / `EDGE_EXPERT.keyify()` | nome vs constante | `v.id == "profissional"` / `"especialista"` | `profissional`, `especialista` |
| 7 | `RequirementValidator.kt:84` | `it.id == "profissional"` (comparação já por id, mas ao lado de checks por nome na mesma função — inconsistência local) | mista | manter id, mas alinhar linhas 71/82 ao mesmo padrão | `profissional` |
| 8 | `RequirementValidator.kt:238-244` | Mapa `incompatibilidades` chaveado por `Constants.EDGE_SLOW`, `EDGE_FLEET_FOOTED`, `EDGE_OBESE`, `EDGE_MUSCULAR`, `EDGE_POVERTY`, `EDGE_RICH`, `EDGE_FILTHY_RICH` (todos nomes) | nomes de Vantagem como chave de conflito | ids: `lento`(complicação), `ligeiro`, `obeso`(complicação), `musculoso`, `pobreza`(complicação), `rico`, `podre_de_rico` | ver lista |
| 9 | `RequirementValidator.kt:250-253` | `it.id.trim().uppercase() == Constants.EDGE_POVERTY` | compara o campo **id** de Complicação contra uma constante que é o **nome** (`"POBREZA"`) — só funciona porque `id.trim().uppercase()` de `"pobreza"` também dá `"POBREZA"`; frágil e conceitualmente errado (mistura namespace de id com namespace de nome) | usar `Constants.ID_*` dedicado ou `"pobreza"` direto | `pobreza` |
| 10 | `CriadorState.kt:1406-1407` | `vantagensSelecionadas.any { it.nome.keyify() == "PODRE DE RICO" }` / `"RICO"` (cálculo do dado de Riqueza) | literal de nome solto (nem passa por Constants) | `it.id == "podre_de_rico"` / `"rico"` | `podre_de_rico`, `rico` |
| 11 | `CriadorState.kt:1534-1536` | `vantagensSelecionadas.any { it.nome.keyify() == "CIBERTOLERANCIA" }` / `"CIBERSAMURAI"` / `"CIBORGUE"` (limite de Chi) | literal de nome solto | `it.id == "cibertolerancia"` / `"cibersamurai"` / `"ciborgue"` | `cibertolerancia`, `cibersamurai`, `ciborgue` |
| 12 | `CriadorState.kt:1676` | `vantagensSelecionadas.any { it.nome.keyify().contains("GARRA") }` (dano corpo-a-corpo) | `.contains()` de substring no nome — bate em qualquer Vantagem futura com "Garra" no nome | `it.id in setOf("garras_demonio", "garras_vampiro")` (ou flag de dado dedicada) | `garras_demonio`, `garras_vampiro` |
| 13 | `CriadorState.kt:2075-2076,2079-2082` | `vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }` / `"MUSCULOSO"` / `nk == "BRUTAMONTES" \|\| nk == "BRAWNY"` (carga máxima) | literal de nome, inclusive comparando contra nome em inglês (`"BRAWNY"`) que não existe no catálogo pt-br | `it.id == "soldado"` / `"musculoso"` / `"brutamontes"` | `soldado`, `musculoso`, `brutamontes` |
| 14 | `CriadorState.kt:2528` | `if (v.nome.keyify() == "CAVALEIRO")` (remove equipamento ao remover a vantagem) | literal de nome | `v.id == "cavaleiro"` | `cavaleiro` |
| 15 | `CriadorState.kt:3460-3468, 3479-3490` | `id == "RICO" \|\| nomeKey == "RICO"` e `id == "PODRE_DE_RICO" \|\| nomeKey == "PODRE DE RICO"` (ganho/remoção de dinheiro ao comprar/remover Vantagem) | duplica a checagem por nome ao lado da por id (redundante, e a por id já usa uppercase incorretamente — `id.keyify()` de `"rico"` vira `"RICO"`, então o literal deveria ser `"RICO"` minúsculo-normalizado, não uma constante própria) | usar só `v.id == "rico"` / `"podre_de_rico"` | `rico`, `podre_de_rico` |
| 16 | `CriadorState.kt:4203` | `vantagensSelecionadas.any { it.nome.keyify() == "CAVALEIRO" }` (trava remoção de Obrigação Maior) | literal de nome | `it.id == "cavaleiro"` | `cavaleiro` |
| 17 | `CriadorState.kt:4608-4613, 4649-4653` | `it.nome.keyify() == "PROFISSIONAL"` / `"ESPECIALISTA"` (2 funções: `atributoMaxRaw`, `periciaCapRaw` — calculam o teto de atributo/perícia) | literal de nome, duplicado 2x, mesmo padrão problemático de `RequirementValidator` | `it.id == "profissional"` / `"especialista"` | `profissional`, `especialista` |
| 18 | `CriadorState.kt:5892` | `listaVantagens.firstOrNull { it.id == edgeId \|\| it.nome.keyify() == edgeId.keyify() }` (concessão automática de Vantagem por elemento) | fallback por nome ao lado do id (a chamada já deveria estar passando ids reais) | remover o fallback por nome; garantir que `edgesToAdd` só contenha ids | — |
| 19 | `ModifierEngine.kt:425-447` | Bloco "4. Advantages": `key = vant.nome.keyify()`, depois `if (key == "MUSCULOSO")`, `"BRUTAMONTES" \|\| "BRAWNY"`, `"BRIGAO" \|\| "PUGILISTA"`, `"LIGEIRO"`, `"BLOQUEAR"`, `"BLOQUEAR APRIMORADO"` | 6 comparações por nome dentro do mesmo loop que já mistura checks por `vant.id` (couro_blindado, resistencia_lobo/anjo/divina) — inconsistência clara: metade do bloco já usa id, metade usa nome | trocar as 6 por `vant.id == "musculoso"` / `"brutamontes"` / `"brigao"` / `"ligeiro"` / `"bloquear"` / `"bloquear_aprimorado"` | `musculoso`, `brutamontes`, `brigao`, `ligeiro`, `bloquear`, `bloquear_aprimorado` |
| 20 | `ModifierEngine.kt:403-420` | Bloco "3. Complications": `key = comp.id.keyify()` seguido de `if (key == "PEQUENO")`, `"OBESO"`, `"IDOSO" \|\| key.endsWith("IDOSO")`, `"LENTO" \|\| key.endsWith("LENTO")` | tecnicamente já usa `comp.id`, mas o `.endsWith(...)` é sinal de id concatenado/namespaced ainda sendo tratado como texto — se dois ids diferentes terminarem na mesma palavra (ex.: um id customizado `"muito_idoso"`), colide | usar igualdade estrita de id (`key == "idoso"`, `key == "lento"`) sem `endsWith` | `idoso`, `lento`, `obeso`, `pequeno` |
| 21 | `DerivedAttributesCalculator.kt:58,67,80` | `.filterKeys { it.name.keyify() == Constants.EDGE_ELDERLY.keyify() \|\| it.id.keyify().endsWith(...) }` (Idoso/Lento/Obeso na Movimentação) | mistura `.name` (nome exibido de Complicação) com `.id.keyify().endsWith(...)` — o `.endsWith` tem o mesmo risco de colisão do achado #20, e o `.name` é redundante já que o `.id` cobre o caso | usar só `it.id == "idoso"` / `"lento"` / `"obeso"` | `idoso`, `lento`, `obeso` |

## Achados MÉDIA severidade

| # | Arquivo:linha | Trecho | Compara | Deveria comparar | id canônico |
|---|---|---|---|---|---|
| 22 | `Constants.kt:27-69` | ~25 constantes `EDGE_*` guardam **nome exibido** (`"BLOQUEAR"`, `"ANTECEDENTE ARCANO"`, `"RICO"` etc.), não id — o próprio nome do padrão (`EDGE_*`) sugere "constante de Vantagem", mas na prática é "constante de texto de nome" | centralizado, mas ainda por nome | renomear/duplicar como `ID_*` apontando pro id real, e migrar call-sites (achados 1,3-9,19) para eles | — |
| 23 | `CriadorState.kt:3465,3485` | `it.nome.keyify() == "RICO" \|\| it.id.keyify() == "RICO"` (dentro de `giveVantagemDinheiro`/`removeVantagemDinheiro`) | OR redundante nome-ou-id | só `it.id == "rico"` | `rico` |
| 24 | `CriadorState.kt:3585,3591,3598,3600` | `mensagemConflitoParaVantagem`/`mensagemConflitoParaComplicacao`: `keys = setOf(vantagem.nome.keyify(), vantagem.id.keyify())`, depois `vant.nome.keyify() in vantConfl \|\| vant.id.keyify() in vantConfl` | mapa `incompatibilidades` (linha 3578-3581, ver achado #8) é chaveado por nome — a função tenta compensar aceitando nome OU id | resolver na raiz: mapa por id, então só `vantagem.id in ...` | `rico`, `podre_de_rico`, `pobreza`, `escolhido`, `inimigo` |
| 25 | `CriadorState.kt:4778` | `vantagensSelecionadas.find { it.id == "heranca" \|\| it.nome.keyify() == "HERANCA" }` | já checa id primeiro; fallback por nome é redundante/desnecessário pro catálogo padrão | remover o fallback por nome | `heranca` |
| 26 | `CriadorState.kt:4959-4962` | `previousFreeAdvantageKeys.contains(removed.nome.keyify())` como uma das 4 condições OR pra decidir reembolso de PV | nome usado como chave de um `Set<String>` (`previousFreeAdvantageKeys`) que deveria conter só ids | garantir que `previousFreeAdvantageKeys` seja populado só com ids, remover a checagem por nome | — |
| 27 | `RequirementValidator.kt:28` | `if (key == Constants.ID_THE_BEST_THERE_IS)` onde `key = v.nome.keyify()` | compara **nome keyified** contra uma constante com formato de **id** (`"o_melhor_que_ha"`) — só funciona hoje porque `keyify()` não normaliza espaço->underscore, então na prática nunca bate (bug latente: o comentário do arquivo sugere que deveria ser `v.id`) | `v.id == Constants.ID_THE_BEST_THERE_IS` | `o_melhor_que_ha` |
| 28 | `RequirementValidator.kt:196` | `nome.uppercase().semAcentos().trim()` pra resolver nome de atributo em `atributoMin` do requisito | atributo (não Vantagem/Complicação), mas mesmo padrão de normalização de texto pra chave; incluído por completude | usar chave de atributo fixa (`Constants.ATTR_*`) | — |
| 29 | `RacialGrantResolver.kt` (arquivo inteiro) | `VANTAGENS_GRATIS`/`DESVANTAGENS` indexados por `texto.keyify()` (ex.: `"ADAPTÁVEL"`, `"Sorte"`, `"AZARADO"`) | por design: resolve texto solto de `ancestralidades.json` (`vantagensGratis`/`desvantagens: List<String>`) pro id real — a fonte de dados upstream ainda é texto, não um problema introduzido aqui | migrar a fonte (schema de `ancestralidades.json`) pra já trazer id, tornando este resolver desnecessário — fora do escopo desta auditoria de Vantagem/Complicação em si | (vários, já mapeados no arquivo) |
| 30-33 | `AncestryVariantRegistry.kt` (arquivo inteiro, ~676 linhas) | `tracosParaAdicionar`/`desvantagensParaAdicionar`/`tracosParaRemoverPorNome` em texto livre (ex.: `"SANGUINÁRIO (Maior)"`, `"FRÁGIL"`, `"DESASTRADO (Menor)"`) | mesmo padrão do achado #29: schema aceito (comentários do próprio arquivo dizem que é o padrão usado desde o lote piloto), a fonte upstream (`ancestralidades.json`) é texto | mesma observação — migração de schema, não bug pontual | — |
| 34 | `SuperPoder.kt` (data class inteira) | `data class SuperPoder(val nome: String, ...)` — **não tem campo `id`** | modelo inteiro sem identificador estável | adicionar `val id: String` ao catálogo `superpoderes.json` e à data class, como já feito em `Vantagem`/`Complicacao` | — |

## Achados BAIXA severidade

| # | Arquivo:linha | Trecho | Nota |
|---|---|---|---|
| 35 | `CriadorState.kt:203` (`addCustomSuperPoder`) | `listaSuperPoderes.none { it.nome.equals(superPoder.nome, ignoreCase = true) }` | decorre diretamente do achado #34 (sem `id` em `SuperPoder`, dedup só pode ser por nome); não é bug isolado, é sintoma do mesmo problema estrutural |
| 36 | `CriadorState.kt:3097-3116` (`isJutsuPericia`, `jutsuSlotIndex`, `jutsuSlotIndexFromName`) | `per.nome.equals("LUTAR", ignoreCase = true)` | é `Pericia` (perícia), não Vantagem/Complicação — fora do escopo estrito do pedido, mas usa literal `"LUTAR"` em vez de `Constants.SKILL_FIGHTING` (que já existe e vale exatamente `"LUTAR"`) — puramente estilístico, `Pericia` não tem `id` estável no modelo atual |
| 37 | `ModifierEngine.kt:479-488` | `sign.equals("Tartaruga", ignoreCase = true)` / `"Garça"` (Signos Arte da Guerra) | não é Vantagem/Complicação catalogada — é uma escolha de string (`signoAdgSelecionado`) sem catálogo com id por trás; risco real baixo pois é um conjunto fechado e pequeno (2 signos com efeito mecânico) |
| 38 | `ArcaneConfig.kt:210` | `"ELEMENTALISTA" -> ARTE_GUERRA_ELEMENTALISTA` no `when(arcaneKey)` | chave sintética interna (não é nome de livro/tradução, é convenção interna do próprio código, setada em `CriadorState.kt:4982` a partir de `tropoSelecionado.id == "tropo_elementalista"`) — baixo risco de quebrar por mudança de texto do livro, mas quebraria numa refatoração de naming sem aviso; poderia usar o id do tropo diretamente |
| 39 | `SummaryUtils.kt:747-757` (`complicationWithSeverity`) | `listaComplicacoes.firstOrNull { comp.name.keyify() == compKey \|\| comp.originalName?.keyify() == compKey }` | função de **exibição de texto** (formata linha do resumo), não afeta mecânica/pontuação; pior caso é severidade não aparecer entre parênteses no resumo — a fonte (`raw: String`) já é texto livre por natureza (vem de `desvantagens: List<String>` de ancestralidade), então comparar por nome aqui é uma consequência do achado #29/30, não um novo padrão |

## Observações gerais

1. **Padrão mais recorrente**: comparar `Vantagem.nome.keyify()` contra um literal de texto (às vezes via `Constants.EDGE_*`, às vezes solto) em vez de `Vantagem.id`. Isso aparece em pelo menos 4 arquivos (`DerivedAttributesCalculator`, `RequirementValidator`, `ModifierEngine`, `CriadorState`) e cerca de 20 vantagens distintas (Bloquear, Ligeiro, Rico, Podre de Rico, Cibertolerância, Cibersamurai, Ciborgue, Soldado, Musculoso, Brutamontes, Brigão, Cavaleiro, Profissional, Especialista, Herança, Pontos de Poder, Antecedente Arcano, Garras).
2. **`ModifierEngine.kt` é o caso mais claro de "meio migrado, meio não"**: no mesmo bloco (`// 4. Advantages`), 4 vantagens já usam `vant.id` (`couro_blindado`, `resistencia_lobo`, `resistencia_anjo`, `resistencia_divina`) e 6 ainda usam `key` derivado de `vant.nome.keyify()` — é o candidato mais direto pra continuar a campanha de migração já em andamento.
3. **`Constants.EDGE_ARCANE_BACKGROUND` ignora um campo que já resolve o problema**: `Vantagem.grupoId == "antecedente_arcano"` já existe em 100% das entradas de AA no JSON (verificado: alquimista, bardo, bruxo, clérigo, diabolista, druida, elementalista, engenhoqueiro, feiticeiro, ilusionista, invocador, mago, necromante, xamã, e variantes de Horror) — o `startsWith` de nome em `RequirementValidator.kt:47,54` é estritamente desnecessário.
4. **Um achado é bug latente, não só estilo**: `RequirementValidator.kt:28` (`key == Constants.ID_THE_BEST_THERE_IS`) compara nome-keyified contra formato de id; `keyify()` não converte espaço para underscore, então essa comparação provavelmente nunca é verdadeira hoje — a regra especial de "O Melhor Que Há" pode estar sendo pulada silenciosamente. Vale investigação funcional separada (fora do escopo somente-leitura desta auditoria).
5. **Achado estrutural mais profundo**: `SuperPoder` (Superpoderes) não tem `id` no data class nem no JSON — ao contrário de `Vantagem`/`Complicacao`, que já migraram. Qualquer lógica de Superpoderes que precisar de identidade estável terá que comparar por nome até esse campo ser adicionado ao catálogo.
