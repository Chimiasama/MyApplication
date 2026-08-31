# Auditoria: pasta `model/usecase/` — hardcode por nome e duplicação com `model/`

Auditoria somente-leitura dos ~37 arquivos de
`app/src/main/java/com/example/swadebuilder/model/usecase/` (padrão `XxxUseCase.execute(...)`),
não coberta pelas duas auditorias anteriores
(`hardcode_audit_vantagens_complicacoes.md`,
`hardcode_audit_atributos_pericias_equipamento_ancestralidade.md`).
Nenhum código foi alterado.

Todos os `id` canônicos foram confirmados via grep/parse direto em
`app/src/main/assets/vantagens.json` / `complicacoes.json`. A cadeia de chamadas "quem instancia
quem" foi confirmada via grep em `CriadorState.kt`, `ProgressosDialog.kt` e demais arquivos de
`ui/`.

## Resumo

**Tipo A (hardcode por nome): 27 achados** — ALTA: **14** · MÉDIA: **9** · BAIXA: **4**

**Tipo B (duplicação/divergência com `model/`): 6 pares/grupos**, dos quais **2 são duplicações
de 3 vias** (mesmo mapa/conjunto copiado em 3 arquivos diferentes) e **1 é uma divergência ativa
de regras** (mapa de conflitos com conteúdo diferente entre as cópias "vivas").

### Achado mais importante: dois validadores de seleção de Vantagem VIVOS ao mesmo tempo

`CriadorState.kt` (fluxo normal de criação/edição de ficha) usa
`ValidateSelectionUseCase` (em `usecase/`), que por sua vez chama
`ValidateConflictsUseCase`, `ValidateRequirementsUseCase`, `ValidatePrerequisiteUseCase`,
`ValidateScenarioRulesUseCase`, `ValidatePowerPointsLimitUseCase` e `ValidateSpecialRulesUseCase`
— confirmado em `CriadorState.kt:118,4250-4251` (`validateSelectionUseCase` e
`validationContext`).

`ui/dialogs/ProgressosDialog.kt` (fluxo de progressão/subida de nível) **não** usa
`ValidateSelectionUseCase` — chama diretamente `RequirementValidator.canSelect(v, state)`
(`ProgressosDialog.kt:75,234`), que é uma reimplementação quase completa das mesmas 14 regras,
mantida à mão em paralelo desde antes da extração dos UseCases.

Ou seja: **não há um caminho morto aqui — são dois caminhos vivos, para duas telas diferentes**,
que precisam ficar sincronizados manualmente. Foi exatamente essa duplicação que já causou o bug
real documentado no contexto desta tarefa (regra especial de "O Melhor Que Há" copiada com o
mesmo bug em `ValidateSpecialRulesUseCase`, hoje já corrigida em ambos os lados para comparar por
`id`). O restante desta auditoria mostra que a mesma dualidade se repete em outros pontos, com o
mapa de incompatibilidades sendo o caso mais grave porque as duas cópias **já divergem em
conteúdo hoje**, não só em risco futuro.

---

## Tipo A — hardcode por nome

| # | Arquivo:linha | Trecho | Compara (nome/texto) | Deveria comparar (id) | id canônico | Severidade |
|---|---|---|---|---|---|---|
| 1 | `ValidateConflictsUseCase.kt:14-35` | Mapa `incompatibilidades` inteiro chaveado por nomes literais (`"LENTO"`, `"LIGEIRO"`, `"COMP ALMA PENHORADA"`, `"TARO ENGENHEIRO"`, `"ESCOLHIDO"`...) | nome de Vantagem/Complicação em texto solto | ids: `lento`(comp), `ligeiro`, `comp_alma_penhorada`, `comp_alma_vendida`, `antecedente_arcano_milagres`/`aa_milagres`, `comp_maldicao_gremlin`, `aa_tecnomagia`, `comp_tecnofobia`, `mecanico_cego`, `mestre_das_caldeiras`, `pobreza`, `rico`, `podre_de_rico`, `inimigo`/`escolhido` | ALTA |
| 2 | `ValidateConflictsUseCase.kt:26` | `"TARO ENGENHEIRO"` como chave de conflito com `COMP TECNOFOBIA` | **não existe nenhuma Vantagem chamada "Taró Engenheiro" no catálogo** — os Tarós reais são `taro_peregrino`, `taro_charlatao`, `taro_espiritualista`, `taro_confusao`, `taro_sentinela`, etc. Esta entrada do mapa nunca dispara — regra morta desde sempre, não só frágil | — (id inexistente; regra provavelmente deveria referenciar outro edge de Taró, ou nunca deveria ter sido escrita assim) | ALTA |
| 3 | `ValidateConflictsUseCase.kt:38-39` | `input.vantagem.nome.keyify()` combinado com `input.vantagem.id.keyify()` para achar a chave no mapa (dupla tentativa nome-ou-id) | mistura namespace de nome com namespace de id no mesmo lookup | usar só `input.vantagem.id` como chave do mapa | ALTA |
| 4 | `ValidateConflictsUseCase.kt:43-49` | `val vantKey = input.vantagem.nome.trim().uppercase(); if (vantKey == "RICO" \|\| vantKey == "PODRE DE RICO")` e `it.id.trim().uppercase() == "POBREZA"` | nome vs. literal solto, e id comparado contra literal em formato de nome (`"POBREZA"` funciona só porque `id.uppercase()` de `pobreza` coincide) | `input.vantagem.id == "rico" \|\| input.vantagem.id == "podre_de_rico"`; `it.id == "pobreza"` | ALTA |
| 5 | `ValidateScenarioRulesUseCase.kt:17,23-24` | `key = input.vantagem.nome.keyify()`; `ancestralidadeKey.contains("MEIO-DEMONIO")` / `.contains("DEMONIO")` para decidir se AA(Demônio) é permitido | `.contains()` livre em nome de ancestralidade — combina com qualquer ancestralidade custom que contenha "demonio" na substring | usar id/ancestryOrigin estável da ancestralidade, ou checar contra o registro de ancestralidades ao invés de substring | ALTA |
| 6 | `ValidateScenarioRulesUseCase.kt:47-49` | `if (key.startsWith("ANTECEDENTE ARCANO")) { if (vId != "aa_agente_syn") return false }` | prefixo de nome pra detectar "é algum Antecedente Arcano" (mesmo padrão já sinalizado como achado #5 na auditoria anterior sobre `RequirementValidator`) | `v.grupoId == "antecedente_arcano"` (campo já existe no JSON) | MÉDIA |
| 7 | `ValidatePrerequisiteUseCase.kt:43-44` | `when (prevId.keyify().replace(" ", "_")) { "ANTECEDENTE_ARCANO", "ANTECEDENTE_ARCANO:*" -> ... }` | compara texto formatado à mão contra o resultado de `.keyify()` de um requisito | comparar contra `Constants.ID_AA_PREFIX` (ou equivalente) diretamente | MÉDIA |
| 8 | `ValidateRequirementsUseCase.kt:34` | `nome.uppercase().semAcentos().trim()` como chave de atributo, casada por `it.equals(chaveNorm, ignoreCase = true)` contra `valoresAtributos.keys` | nome de atributo normalizado na hora, sem passar por um id de atributo estável | usar as chaves de `listaAtributos`/id de atributo já carregadas do catálogo | BAIXA |
| 9 | `ValidateSpecialRulesUseCase.kt:44,53` | `key = v.nome.keyify()`; `if (key == "CAVALEIRO")` (regra de Obrigação Maior) | nome vs. literal | `v.id == "cavaleiro"` | ALTA |
| 10 | `ValidateSpecialRulesUseCase.kt:67,75-76` | `key.startsWith("ANTECEDENTE ARCANO")`; `it.nome.keyify().startsWith("ANTECEDENTE ARCANO")` | mesmo padrão de prefixo por nome do achado #6, repetido numa 3ª função | `v.grupoId == "antecedente_arcano"` | MÉDIA |
| 11 | `ValidateSpecialRulesUseCase.kt:93-129` | `if (key == "profissional" \|\| key == "especialista")` (bloco inteiro de Profissional/Especialista, ~35 linhas) usa `key` = `v.nome.keyify()` | nome vs. literal (na prática os ids `profissional`/`especialista` são estáveis em todo o catálogo, então funciona hoje, mas o padrão é o mesmo que já causou o bug de "O Melhor Que Há") | `v.id == "profissional" \|\| v.id == "especialista"` | MÉDIA |
| 12 | `ValidatePowerPointsLimitUseCase.kt:16` | `input.vantagem.nome.contains("Pontos de Poder", ignoreCase = true)` | `.contains()` de texto livre no nome | `input.vantagem.id == "pontos_de_poder"` | ALTA |
| 13 | `ResolveAncestryTransitionContextUseCase.kt:38-47` | `when { previousAncestryKey == "SAURIOS" -> setOf("Sentidos Aguçados", "Prontidão"); ... }` — 9 ramos por nome de ancestralidade, produzindo nomes de Vantagem/traço em texto livre (`"NOÇÃO DO PERIGO"`, 4 grafias diferentes da mesma coisa) | nome de ancestralidade + nomes de vantagem/traço concedidos, tudo em texto, com múltiplas variações de acentuação mantidas à mão para cobrir divergência de grafia | usar id de ancestralidade + ids de vantagem/traço do `RacialModifier`/registro | ALTA |
| 14 | `ResolveAncestrySpecificAdjustmentsUseCase.kt` (arquivo inteiro, 453 linhas) | Cadeia de `if (ancKey == "DEADERS")`, `if (ancKey == "ANOES")`, `.contains("UMVEE")`, `.contains("TERRACOTA")`, `.contains("AKAIMIMI")`, e um `when(ancKey)` final com >10 ramos (`"SAURIOS"`, `"GOLENS"`, `"DRACONIANOS"`, `"INSETOIDES"`, `"PEQUENINOS"`, `"CELESTIAIS"`, etc.) | nome de ancestralidade em texto, incluindo `.contains()` livre em 3 ramos | migrar progressivamente para `AncestryVariantRegistry` (o próprio arquivo já faz isso para as raças Sci-Fi via `scifiVariantDrivenKeys` — ver Tipo B #5); os ramos restantes são o resíduo ainda não migrado | ALTA (pelos `.contains()`) / MÉDIA (pelo `when` de igualdade) |
| 15 | `ResolveAncestryRacialPackageUseCase.kt:44-47` | `isLeakedDom = (it.id == "antecedente_arcano" && it.choice?.keyify() == "DOM" && "ANTECEDENTE ARCANO (DOM)" in params.previousFreeAdvantageKeys)` etc. | mistura correta (`it.id`) com literais de nome/choice em texto (`"DOM"`, `"TELEPATA"`, `"ANTECEDENTE ARCANO (DOM)"`) | usar id de choice também estável (se existir) em vez do texto de exibição | MÉDIA |
| 16 | `ResolveAncestryRacialPackageUseCase.kt:85-92` | `ancestrySpecificAdjustments.ensureAdvantageNames.forEach { advantageName -> params.allAdvantages.firstOrNull { it.nome.equals(advantageName, ignoreCase = true) } ... }` | resolve Vantagem-a-conceder por igualdade de nome (porta de entrada dos nomes hardcoded do achado #14) | preferir sempre `ensureAdvantageIds` (que já existe e é usado ao lado); tratar `ensureAdvantageNames` como legado a eliminar | MÉDIA |
| 17 | `NormalizeArcaneBackgroundChoiceUseCase.kt:9-16` | `when (choice?.trim()?.uppercase()) { ArcaneBackgroundChoices.DOM -> ... ; "PSIÔNICOS" -> ...; "CIÊNCIA ESTRANHA" -> ... }` | compara texto de escolha exibido contra constantes de texto (`ArcaneBackgroundChoices`), com 2 variações de acento hardcoded soltas fora da constante | ok como ponte de UI→id (é literalmente a função de normalização), mas os 2 literais soltos (`"PSIÔNICOS"`, `"CIÊNCIA ESTRANHA"`) deveriam estar dentro de `ArcaneBackgroundChoices`, não soltos aqui | BAIXA |
| 18 | `RemoveInvalidAdvantagesAfterAncestryChangeUseCase.kt:39` | `if (vantagem.id == "conexoes" && vantagem.choice?.equals("Máfia", ignoreCase = true) == true) continue` | id correto, mas `choice` comparado por texto de exibição | aceitável — `choice` não tem id próprio no modelo atual; mesmo padrão usado em toda a base | BAIXA |
| 19 | `ApplyAncestryChangeCoordinatorUseCase.kt:92-93,164-170` | `targetKey.contains("HUMANO")`; `params.targetAncestry == "CELESTIAIS"`; `targetKey != "MEIO-ELFOS"`; `resetAnoesScifi = !targetKey.contains("ANOES")`; `clearPericiaGnomo = !targetKey.contains("GNOMO")` | mistura de `==` e `.contains()` por nome de ancestralidade para decidir resets de estado | usar id/flag de ancestralidade dedicado ao invés de `.contains()` em nome | MÉDIA (`.contains()`) |
| 20 | `ApplyAncestryChangeCoordinatorUseCase.kt:183` | `variantKey.contains("BAIXA") && variantKey.contains("GRAVIDADE")` | `.contains()` duplo em texto de variante pra detectar "Baixa Gravidade" | usar id de opção de variante do `AncestryVariantRegistry` | ALTA |
| 21 | `ResolveGrantedAncestryAdvantagesUseCase.kt:24-32` | `featKey == "HERANCA"` (ignorado de propósito); depois `advantage.nome.keyify() == featKey \|\| advantage.id == featString \|\| advantage.id.keyify() == featKey` | resolve por nome OU id (fallback), com um caso especial de nome hardcoded pra evitar colisão com outra Vantagem de nome parecido — sintoma direto de o catálogo ter nomes duplicados com ids diferentes | migrar as fontes de `grantedAdvantageNamesOrIds` para ids puros e remover o fallback por nome | MÉDIA |
| 22 | `ResolveAncestryVariantUseCase.kt:27,58,77` | `it.keyify() == "BASICO" \|\| it.keyify() == "PADRAO"` (2x) para achar a opção "padrão" de uma lista de nomes de variante | nome de opção de variante em texto | as opções de variante ainda não têm id próprio nesse ponto do fluxo (vêm como `List<String>` de `RacialModifier.opcoes`) — risco aceito, mas registrado | BAIXA |
| 23 | `ResolveVariantPointBudgetUseCase.kt:102-105` | `when (vantagem.requisitos.estagio.trim().lowercase()) { "experiente" -> 3; "veterano" -> 4; "heroico", "lendário", "lendario" -> 5; ... }` | nome de estágio em texto, com grafias alternativas mantidas à mão (`"lendário"`/`"lendario"`) | usar o objeto `Estagio`/índice de estágio já usado em outros lugares (`listaDeEstagios.indexOf(...)`) | MÉDIA |
| 24 | `ResolveDependentPowerRemovalUseCase.kt:20-31` | `when (input.skillKey) { SkillIds.OCULTISMO -> ...; SkillIds.CIENCIA -> ... }` | já usa `SkillIds`/`PowerIds` (ids) — **sem achado**, listado aqui só para registro de cobertura | — | — |
| 25 | `ValidateScenarioRulesUseCase.kt:30-35,57-61` | Listas `forbiddenIds` já usam ids reais (`"campeao"`, `"antecedente_arcano_ciencia_estranha"` etc.) — **sem achado Tipo A**, listado para registro | — | — | — |
| 26 | `CalculateCurrentSuperSkillStepsUseCase.kt:17` | `it.skillKey.equals(input.targetSkillName, ignoreCase = true)` | compara "chave de perícia" por igualdade de texto (não é nome de Vantagem/Complicação, é infraestrutura de Super Perícias) — risco baixo pois `skillKey` já é tratado como um id interno estável no chamador | considerar migrar `skillKey`/`targetSkillName` para um tipo id dedicado em vez de `String` livre | BAIXA |
| 27 | `EnsureDefaultSpecializationsUseCase.kt:29-36` | `out[per.nome]` usa `per.nome` como chave do mapa de especializações | Perícia não tem outro id estável além do nome no modelo atual (`Pericia.nome` é a chave primária de fato) — risco aceito, mesmo padrão em toda a base de Perícias | — | BAIXA |

Arquivos lidos por completo e sem achado Tipo A relevante (já orientados a id, matemática pura,
ou fora do escopo de Vantagens/Complicações/Ancestralidade/Antecedente Arcano/Perícias/Atributos):
`AdjustAttributesForAncestryChangeUseCase.kt`, `AdjustNonNegativeBonusUseCase.kt`,
`ApplyHumanAncestryTransitionUseCase.kt`, `ApplySuperAttributeDeltaUseCase.kt`,
`CalculatePerPowerLimitUseCase.kt`, `CalculateSuperSkillRawAfterRevertUseCase.kt`,
`GenerateSequentialNameUseCase.kt`, `ManageCrystalHeartUseCases.kt` (Upsert/RemoveCrystalHeart),
`ResolveActiveAncestryCandidatesUseCase.kt` (usa `ModuleIds`, com um `.contains()` restrito a
"TRILHADOR"/Pathfinder — MÉDIA, já contabilizado indiretamente no achado #19 do padrão),
`ResolveAdvantageByIdUseCase.kt`, `ResolveAncestryComplicationsSnapshotUseCase.kt` (delega pra
`ResolveRacialAutomaticComplicationsUseCase`), `ResolveAncestryTransitionBootstrapUseCase.kt`,
`ResolveAncestryVariantPackageUseCase.kt`, `ResolveRacialAutomaticComplicationsUseCase.kt` (já
normaliza por id/token, não por nome cru), `ValidateGameDataSnapshotIntegrityUseCase.kt`,
`ValidatePowerInvestmentUseCase.kt`, `ValidatePowerInvestmentWorkflowUseCase.kt`,
`ValidateSpecialPowerRequirementsUseCase.kt`, `ValidateSuperAdvantageInvestmentUseCase.kt`,
`ValidateSuperAttributeInvestmentUseCase.kt`.

---

## Tipo B — duplicação/divergência com `model/`

| usecase/arquivo:linha | model/arquivo:linha equivalente | Idêntico ou divergente | Caminho ativo | Risco |
|---|---|---|---|---|
| `ValidateConflictsUseCase.kt:14-35` (mapa `incompatibilidades`, 20 entradas) | `RequirementValidator.kt:237-245` (mapa `incompatibilidades`, 7 entradas) | **DIVERGENTE hoje.** `RequirementValidator` só tem Lento/Ligeiro, Obeso/Musculoso, Pobreza/Rico/Podre-de-Rico. `ValidateConflictsUseCase` tem tudo isso **mais** os 4 conflitos de Antecedente Arcano×Complicação (Alma Penhorada/Vendida×Milagres, Maldição do Gremlin×Tecnomagia), Tecnofobia×3 edges técnicos, e Escolhido×Inimigo — 13 entradas a mais | `ValidateConflictsUseCase` roda em **criação/edição** (via `ValidateSelectionUseCase`, plugado em `CriadorState.kt:118`); `RequirementValidator` roda em **progressão** (`ProgressosDialog.kt:75,234`) | **ALTA** — hoje é possível pegar uma Vantagem de Antecedente Arcano (Milagres) tendo Alma Penhorada/Vendida (ou vice-versa) **durante a progressão de nível**, porque `RequirementValidator` não bloqueia essa combinação; a mesma combinação já é bloqueada na criação inicial. Divergência ativa, não hipotética. |
| `CriadorState.kt:3555-3576` (mapa `incompatibilidades`, privado, usado só em `mensagemConflitoParaVantagem`/`mensagemConflitoParaComplicacao`) | `ValidateConflictsUseCase.kt:14-35` | **Idêntico byte-a-byte** ao mapa do UseCase (mesmas 20 entradas, mesma ordem) | `CriadorState` usa sua cópia só para gerar a mensagem "Remova X para pegar Y" (chamada de `VantagensSection.kt`, `ComplicacoesSection.kt`, `ProgressosDialog.kt`); a decisão real de bloqueio usa a cópia do UseCase | MÉDIA — hoje sincronizado porque foi copiado junto, mas é uma 3ª cópia manual do mesmo dado; qualquer correção futura (ex.: remover a entrada morta de `"TARO ENGENHEIRO"`, achado A#2) precisa ser replicada em 2 lugares (`ValidateConflictsUseCase` + `CriadorState`) para não voltar a divergir da mensagem de erro exibida ao jogador |
| `ResolveAncestrySpecificAdjustmentsUseCase.kt:38-43` (`scifiVariantDrivenKeys`, 20 ids) | `CriadorState.kt:129-134` (`scifiVariantDrivenKeys`, 20 ids, comentário explícito "Mesmo conjunto de ids de ResolveAncestrySpecificAdjustmentsUseCase") | Duplicação **reconhecida no próprio código** (comentário em `CriadorState.kt:122-127` admite a cópia); conteúdo idêntico como conjunto (mesmos 20 nomes de raça, só 2 últimos itens em ordem diferente — irrelevante para um `Set`) | Ambas as cópias são ativas: a do UseCase decide o pacote de traços da raça; a de `CriadorState` decide se a raça usa o path "id de habilidade presente" (`PERICIAS_BASICAS_REDUZIDAS*`) em vez de comparação por nome pra perícias básicas — são usos diferentes do mesmo "quais raças já migraram pro registro" | MÉDIA — se uma raça sci-fi nova for migrada para `AncestryVariantRegistry` e só uma das duas listas for atualizada, o comportamento de exibição/cálculo de perícias básicas e o de concessão de traços racial ficam dessincronizados para aquela raça |
| `ValidateSelectionUseCase.kt` (orquestrador inteiro, 145 linhas) | `RequirementValidator.canSelect(v, state)` (262 linhas) | **Estruturalmente divergente por design** — `RequirementValidator` é uma função monolítica com as 14 regras inline (incluindo o mapa de conflitos acima); `ValidateSelectionUseCase` delega para 6 UseCases especializados. Regra a regra, a lógica é quase toda equivalente hoje (mesmas 14 checagens, na mesma ordem conceitual), **exceto** o mapa de conflitos (linha acima) e pequenas diferenças de acesso a estado (`ValidateRequirementsUseCase` recebe `getBestPericia` injetável para Jutsu/Arte da Guerra; `RequirementValidator` usa `state.mapaPericias[...]` direto, sem esse hook) | Criação usa `ValidateSelectionUseCase`; progressão usa `RequirementValidator` | **ALTA** — é o par-guarda-chuva de todos os outros achados desta seção; toda futura correção em qualquer uma das 14 regras (ex.: a próxima vez que um bug tipo "O Melhor Que Há" aparecer) precisa ser replicada nos dois lados manualmente até que um dos dois vire wrapper do outro |
| `ValidateRequirementsUseCase.kt` (10-99, requisitos de atributo/perícia/CS/tags/template) | `RequirementValidator.kt:194-232` (blocos 10-13) | Lógica equivalente linha a linha, mesma ordem de checagem; única diferença notável é o hook `getBestPericia` do UseCase (suporte a "melhor perícia equivalente" para Jutsu) que `RequirementValidator` não tem — `RequirementValidator` sempre usa `state.mapaPericias[perNome.keyify()]` direto | UseCase ativo na criação; `RequirementValidator` ativo na progressão | MÉDIA — se Arte da Guerra/Jutsu depender do hook `getBestPericia` para funcionar corretamente, um personagem em progressão (via `ProgressosDialog`) pode falhar a validar um requisito de perícia de Jutsu que a criação validaria com sucesso |
| `ValidateSpecialRulesUseCase.kt` (regras 1,1a,2a,3,4,5,8,9,13b) | `RequirementValidator.kt:24-68,89-192` (mesmas regras, numeradas igual nos comentários) | Equivalente regra-a-regra hoje, incluindo a correção recente da regra 1 ("O Melhor Que Há", ambas já em `v.id == "o_melhor_que_ha"` / `Constants.ID_THE_BEST_THERE_IS`). Únicas ausências: a regra **1a (CAVALEIRO/Obrigação Maior)** existe só no UseCase (`ValidateSpecialRulesUseCase.kt:53-58`) — **não encontrada em `RequirementValidator.kt`** — e a regra de **Samurai/Arte da Guerra ignorando estágio de Liderança** (`shouldIgnoreLeadershipStage`, linhas 134-141) também só existe no UseCase | UseCase ativo na criação; `RequirementValidator` ativo na progressão | **ALTA** — um personagem em progressão pode pegar uma Vantagem exclusiva de Cavaleiro sem ter Obrigação (Maior) selecionada (regra 1a ausente em `RequirementValidator`), e um Samurai de Arte da Guerra com Conhecimento de Batalha d8+ não tem o requisito de Estágio de Liderança ignorado durante a progressão como tem na criação |

### Confirmação da cadeia "quem chama quem" (via grep)

- `ValidateConflictsUseCase`, `ValidateRequirementsUseCase`, `ValidatePrerequisiteUseCase`,
  `ValidateScenarioRulesUseCase`, `ValidatePowerPointsLimitUseCase`, `ValidateSpecialRulesUseCase`
  só são instanciados dentro de `ValidateSelectionUseCase.kt` (construtor com defaults) — nenhum
  outro arquivo os chama diretamente.
- `ValidateSelectionUseCase` só é instanciado em `CriadorState.kt:118`, e usado via
  `validationContext` (`CriadorState.kt:4250`).
- `RequirementValidator.canSelect` só é chamado em `ProgressosDialog.kt:234`.
- Logo: **nenhum dos dois caminhos está morto** — são dois validadores vivos, cobrindo duas telas
  diferentes do mesmo app, exatamente o cenário de risco descrito no prompt desta auditoria.

---

## Recomendação

Sem implementar nada, por ordem de prioridade de consolidação:

1. **Unificar o mapa de incompatibilidades numa única fonte** (ex.: mover para
   `Constants.kt` ou um objeto novo `IncompatibilityRules`, chaveado por `id`, sem os textos de
   nome) e fazer `ValidateConflictsUseCase`, `RequirementValidator.canSelect` e
   `CriadorState.mensagemConflitoPara*` lerem dessa única fonte. É o achado de maior risco
   concreto (Tipo B, linha 1): a divergência já existe e já permite uma combinação inválida
   passar despercebida na progressão de personagem.
2. **Fazer `ProgressosDialog.kt` usar `ValidateSelectionUseCase` em vez de
   `RequirementValidator.canSelect`** (ou o inverso — extrair `RequirementValidator` como
   wrapper fino sobre os UseCases). Resolveria de uma vez as divergências de regra 1a
   (Cavaleiro/Obrigação Maior) e do hook Samurai/Arte da Guerra, e eliminaria a manutenção
   dupla de toda a árvore de validação de seleção de Vantagem.
3. **Remover a entrada morta `"TARO ENGENHEIRO"`** do mapa de incompatibilidades (aparece em
   `ValidateConflictsUseCase.kt:26` e `CriadorState.kt:3567` — não corresponde a nenhum id real
   do catálogo) e revisar se a intenção original era outro edge de Taró.
4. **Consolidar `scifiVariantDrivenKeys`** numa constante compartilhada (ex.: expor a partir de
   `AncestryVariantRegistry`) em vez de manter a lista duplicada em
   `ResolveAncestrySpecificAdjustmentsUseCase.kt` e `CriadorState.kt`.
5. Só depois disso vale atacar os achados Tipo A "isolados" (sem duplicata em `model/`) como o
   `when(ancKey)` de 453 linhas em `ResolveAncestrySpecificAdjustmentsUseCase.kt` — já está sendo
   migrado gradualmente para `AncestryVariantRegistry` pelo próprio time (comentários no arquivo
   confirmam isso), então o caminho natural é continuar essa migração em vez de reescrever tudo
   de uma vez.
