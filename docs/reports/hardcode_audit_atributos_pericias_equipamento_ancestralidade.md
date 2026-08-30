# Auditoria de hardcode por nome/texto — Atributos, Perícias, Equipamento, Ancestralidade

Data: 2026-08-30
Escopo: leitura/análise apenas, nenhuma alteração de código.
Objetivo: localizar todo lugar onde a lógica decide algo comparando `.nome`/`.name`/`.contains(...)`/texto solto de `Constants.kt`, em vez do `id` estável do catálogo JSON correspondente — seguindo o padrão de migração já aplicado em traços raciais (`docs/racial_trait_migration_plan.md`).

## Resumo

**Total de achados catalogados: 47** (agrupando ocorrências idênticas repetidas linha a linha; várias linhas listadas representam também outras ocorrências irmãs no mesmo bloco, indicadas na coluna Observações).

### Por severidade
| Severidade | Qtde |
|---|---|
| ALTA | 27 |
| MÉDIA | 17 |
| BAIXA | 3 |

### Por arquivo
| Arquivo | Qtde |
|---|---|
| CriadorState.kt | 17 |
| ModifierEngine.kt | 14 |
| DerivedAttributesCalculator.kt | 9 |
| DataLoader.kt | 5 |
| Constants.kt | 1 |
| model (Atributos.kt / Pericia.kt / Pericias.kt / EquipamentoModels.kt) | 3 (estrutural) |
| AncestryVariantSystem.kt | 2 |
| EquipamentoFormatters.kt / MonstroTemplate.kt / RacialModifier.kt / AnaoCiberTraits.kt / AncestryVariantRegistry.kt | 0 (ver notas) |

### Por categoria
| Categoria | Qtde |
|---|---|
| Ancestralidade/Raça | 24 |
| Perícia | 9 |
| Equipamento | 8 |
| Atributo | 6 |

### Achados-chave confirmados contra o JSON
- `vantagens.json`: `PROFISSIONAL`→`profissional`, `ESPECIALISTA`→`especialista`, `BLOQUEAR`→`bloquear`, `BLOQUEAR APRIMORADO`→`bloquear_aprimorado`, `LIGEIRO`→`ligeiro`, `MUSCULOSO`→`musculoso`, `BRUTAMONTES`→`brutamontes` **já têm `id` estável**, mas o código compara por `nome.keyify()`.
- `complicacoes.json`: `IDOSO`→`idoso`, `LENTO`→`lento`, `OBESO`→`obeso`, `PEQUENO`→`pequeno` **já têm `id`** (o `ModifierEngine` já usa `comp.id` para Complicações — correto; mas `DerivedAttributesCalculator` mistura `id` e `.name.keyify()` para os mesmos traços).
- `pericias.json` (66 registros) e `geral_atributos.json` (5 registros): **nenhum registro tem campo `id`** — gap estrutural no catálogo, não só no código consumidor.
- `equipamentos.json` (3243 itens em 376 categorias): **nenhum item tem campo `id`** — todo o app é forçado a casar equipamento por `nome.keyify()`/`contains()`.
- `ancestralidades.json` (120 registros): apenas **28/120** têm campo `id` — a maioria das raças só é endereçável pelo nome, o que explica (mas não justifica) o uso disseminado de `ancestralidade.keyify() == "X"` em `CriadorState.kt`.

---

## Tabela de achados (ordenada por severidade)

| # | Severidade | Arquivo:Linha | Trecho | Compara (texto) | Deveria comparar (id) | Observações |
|---|---|---|---|---|---|---|
| 1 | ALTA | DerivedAttributesCalculator.kt:51 | `.desvantagens?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }` | busca de substring livre dentro de descrições raciais | id de traço via `RacialTraitPointCatalog` (já existe para Movimentação Reduzida no `ModifierEngine`) | Exemplo já confirmado pelo dono do projeto; pior padrão do arquivo |
| 2 | ALTA | DerivedAttributesCalculator.kt:33 | `it.nome.keyify() == Constants.EDGE_BLOCK.keyify()` | nome da Vantagem "Bloquear" | `it.id == "bloquear"` (id confirmado em vantagens.json) | |
| 3 | ALTA | DerivedAttributesCalculator.kt:35 | `it.nome.keyify() == Constants.EDGE_IMPROVED_BLOCK.keyify()` | nome "Bloquear Aprimorado" | `it.id == "bloquear_aprimorado"` (id confirmado) | |
| 4 | ALTA | DerivedAttributesCalculator.kt:87 | `state.vantagensSelecionadas.any { it.nome.keyify() == Constants.EDGE_FLEET_FOOTED.keyify() }` | nome "Ligeiro" | `it.id == "ligeiro"` (id confirmado) | Inconsistente com as linhas 58/67/80 do mesmo arquivo, que já têm fallback por id |
| 5 | ALTA | ModifierEngine.kt:117 | `if (ancestryKey == "AQUARIANOS" && hasSemiAquatico)` | nome da raça | `anc.id` (Aquarianos não está entre os 28 com id hoje — mas o padrão de comparar por nome de raça é o que o dono do projeto pediu para eliminar) | |
| 6 | ALTA | ModifierEngine.kt:124-126 | `isAvianosAveRapina = ancestryKey == "AVIANOS" && allTraitKeys.any{it.contains("FORMA ALIENIGENA")} && allTraitKeys.any{it.contains("HABITANTE DE GRAVIDADE")}` | nome de raça + `.contains` duplo sobre nomes de traço | raça por id + traço por id (`RacialAbility.id`) | Duplo `.contains` livre é o padrão mais frágil da função |
| 7 | ALTA | ModifierEngine.kt:135-140 | `isCentauxGazela = ... ancestryKey == "CENTAUX" && ...resolveSciFiVariantSelectionFor(...).equals("Gazela", ignoreCase = true)` | nome de raça + nome de variante | raça/variante por id (`AncestryVariantRegistry` já teria "centaux"/"gazela") | |
| 8 | ALTA | ModifierEngine.kt:156-157 | `sources.firstOrNull { it.contains("TAMANHO", ignoreCase=true) && !it.keyify().startsWith("DIMINUTO") }` | busca de substring "TAMANHO" em toda lista de nomes de traço/vantagem/desvantagem | id de traço com efeito `RacialTraitEffect` de Tamanho | |
| 9 | ALTA | ModifierEngine.kt:159-160 | `isDiminutoAncestry = ... \|\| (state.compendioSciFiAtivo && anc.nome.keyify() == "FERAIS")` | nome de raça "Ferais" | `anc.id` (Ferais tem id? conferir) ou id de traço "DIMINUTO" | Repetido também na linha 249 |
| 10 | ALTA | ModifierEngine.kt:162-176 | `racialSizeFromText` — `Regex("""TAMANHO\s*([+-]\s*\d+)""")` sobre `it.descricao` das habilidades da raça | regex extraindo valor numérico de texto narrativo livre | campo estruturado equivalente a `MonstroHabilidade.armasNaturais`/`RacialTraitEffect` para Tamanho | Mesmo tipo de padrão do achado #1 (extrair mecânica de descrição em prosa), mas com regex numérico — ainda mais frágil a mudanças de texto/tradução |
| 11 | ALTA | ModifierEngine.kt:184 | `sources.any { it.keyify() == "PEQUENOS" \|\| it.keyify() == "PEQUENO" }` | nome de traço "Pequeno(s)" | id de traço de Tamanho | |
| 12 | ALTA | ModifierEngine.kt:220,240-247 | `diminutoSource = sources.firstOrNull { it.keyify().startsWith("DIMINUTO") }`; `when { k.contains("TAMANHO -2") -> -2; ... }` | prefixo de string + `.contains` de valor numérico embutido no nome | id de traço + valor estruturado | |
| 13 | ALTA | ModifierEngine.kt:249 | `state.compendioSciFiAtivo && anc.nome.keyify() == "FERAIS"` | nome de raça | `anc.id` | Duplicado do achado #9 |
| 14 | ALTA | ModifierEngine.kt:312 | `if (state.compendioSciFiAtivo && anc.nome.keyify() == "MIMICOS")` | nome de raça "Mímicos" | `anc.id` | |
| 15 | ALTA | ModifierEngine.kt:336-398 | Bloco "Generic Parsing": `Regex("""RESISTENCIA\s*([+-])\s*(\d+)""")`, `Regex("""ARMADURA(.*?)\+(\d+)""")`, `Regex("""MOVIMENTACAO\s*\+(\d+)""")`, `Regex("""APARAR\s*([+-])\s*(\d+)""")` aplicados sobre `sources` (nomes de vantagem/desvantagem/habilidade concatenados) | 4 regexes extraindo valores numéricos de strings de nome/descrição | campos estruturados por traço (mesmo padrão de `RacialTraitEffect`) | Bloco mais extenso do arquivo; o próprio código reconhece a fragilidade com checks `alreadyAdded`/`alreadyReduced` para não duplicar contagem |
| 16 | ALTA | ModifierEngine.kt:479 | `state.compendioArteDaGuerraAtivo && state.ancestralidade.keyify().contains("HUMANO")` | substring "HUMANO" no id de raça | `state.ancestralidade == "humanos"` (exato, ou melhor, id real) | `.contains` é mais frágil que `==`; risco de casar com variantes futuras cujo nome contenha "Humano" |
| 17 | ALTA | CriadorState.kt:4608-4613 | `vantagensSelecionadas.count { it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave }` (também 4611-4612 para "ESPECIALISTA") | nome literal duplicando `Constants.EDGE_PROFESSIONAL`/`EDGE_EXPERT`, que já existem mas não são usados aqui | `it.id == "profissional"` / `it.id == "especialista"` (ids confirmados em vantagens.json) | Cálculo de teto de Atributo (`atributoMaxRaw`) — núcleo da categoria Atributo |
| 18 | ALTA | CriadorState.kt:4649-4653 | Mesmo padrão de #17, dentro de `periciaCapRaw` | nome literal "PROFISSIONAL"/"ESPECIALISTA" | `it.id` | Duplica #17 para teto de Perícia — 4 ocorrências totais no arquivo (2+2) |
| 19 | ALTA | CriadorState.kt:4642-4644 | `compendioPathfinderAtivo && ancestralidade.keyify().contains("MEIO-ORC") && per.nome.keyify() == "INTIMIDAR"` | nome de raça (substring) + nome de perícia | id de raça + id/nome canônico de perícia (perícia não tem id no JSON — ver achado estrutural #40) | Caso especial "Meio-Orc Buscatrilha" só descoberto por nome |
| 20 | ALTA | CriadorState.kt:4494-4521 | `if (ancKey == "DRAKENS") { ... }` / `if (ancKey == "ELEMENTAIS") { ... }` dentro de `atributoBaseRacial` | nome de raça, comentário no próprio código admite: *"ainda não têm o traço... continuam hardcoded por nome de raça por enquanto"* | traço estruturado tipo `RacialTraitEffect.AtributoStep` (mesmo padrão já usado para outras raças no mesmo método) | Débito técnico já reconhecido pelos próprios devs no comentário; núcleo do cálculo de Atributo |
| 21 | ALTA | CriadorState.kt:4536-4548 | `compendioArteDaGuerraAtivo && ancestralidade.keyify().contains("HUMANO")` + `sign.equals("Boi", true)`/`"Dragão"` | substring de raça + nome de "Signo" | id de raça + id de signo | Bônus de Atributo por Signo (Arte da Guerra) resolvido por texto duplo |
| 22 | ALTA | CriadorState.kt:1476-1497 | `findItem()`: `listaEquipamentos.firstOrNull { it.nome.keyify().contains("CAVALO") && ...contains("GUERRA") }`, idem para "LANCA", "ESCUDO"+"MEDIO" | busca de equipamento por substring de nome | id de equipamento (não existe no JSON — ver achado estrutural #41) | Vantagem "Cavaleiro" concede equipamento; sem id no catálogo, `.contains` pode casar item errado (ex.: qualquer item com "Lança" no nome) |
| 23 | ALTA | CriadorState.kt:1877-2021 | Bloco de detecção/injeção de armas naturais: `.contains("TOQUE ARREPIANTE")`, `"FERRAO"`, `"GARRA"`, `"MORDIDA"`, `"CHIFRE"`, `"CASCO"`, `"CABECA DURA"` + `Regex("""(For\|Str\|Força\|Strength)(\s*\+\s*)?d\d+""")` e `Regex("""PA\s*\d+""")` extraindo dano/PA de `desc` (texto de habilidade/vantagem) | casamento por palavra-chave em nome + regex de dano sobre descrição em prosa | `ArmaNatural` estruturado (o próprio `RacialAbility.armasNaturais`/`MonstroHabilidade.armasNaturais` já existe para isso, mas este bloco ainda é o "fallback" de texto) | Maior bloco de hardcode do arquivo; mesmo padrão do achado #1, em escala muito maior (14 chaves de keyword + 2 regexes) |
| 24 | ALTA | CriadorState.kt:2023 | `val isInsectoid = ancestralidade.keyify().contains("INSETOIDE")` | substring de nome de raça | id/traço de raça | |
| 25 | ALTA | CriadorState.kt (padrão recorrente, ~14 ocorrências) — ex.: linhas 663,748,4777,4888,5193,5314,5330,5702 | `.contains("MEIO-ELFO")`, `.contains("ANOES")`, `.contains("UMVEE")`, `.contains("USAGIMIMI")`, `.contains("DEMONIOS")` etc. sobre `ancestralidade.keyify()` | substring de nome de raça | id de raça exato | `.contains` é mais frágil que `==`; evidência da fragilidade: linhas 524/663/4777 precisam de exclusão manual `&& !key.contains("PATHFINDER")` para não casar "Meio-Elfo (Pathfinder)" com "Meio-Elfo" — sintoma direto do problema |
| 26 | ALTA | CriadorState.kt:902 (comentário próprio do código) | `if (key.contains("HUMANO")) { if (variant.equals("Baixa Gravidade"...))... }` | substring de raça + nome de variante | id de raça + id de variante (`AncestryVariantRegistry` já cobre "HUMANOS") | O comentário da própria função já descreve isto como o mesmo tipo de problema ("bônus de atributo virava hardcode de nome de raça") mas ainda decide a ENTRADA do bloco por `.contains("HUMANO")` |
| 27 | ALTA | ModifierEngine.kt:47-50 vs. equipamentos.json (sem campo de categoria estruturado) | `item.subtipo?.uppercase()?.let { s -> s.contains("VEICULO") \|\| s.contains("VEÍCULO") \|\| s.contains("CHASSIS") \|\| s.contains("MECHA") }` | substring sobre `subtipo` (texto livre) | campo de categoria estruturado (ex.: enum `tipo`) — hoje não existe no JSON | Determina se um item de armadura conta ou não para Armor; variação de acento ("VEICULO" vs "VEÍCULO") já é sintoma de fragilidade |
| 28 | MÉDIA | DerivedAttributesCalculator.kt:25 | `state.isJutsuPericia(it) \|\| it.nome.keyify() == Constants.SKILL_FIGHTING` | nome de perícia "Lutar" | perícia não tem `id` no JSON (ver achado estrutural #40) — `Constants.SKILL_FIGHTING` já centraliza o texto | Centralizado em Constants, mas ainda por nome porque o catálogo de Perícia não tem id |
| 29 | MÉDIA | DerivedAttributesCalculator.kt:49 | `state.listaAncestralidadesJson.firstOrNull { it.nome.keyify() == state.ancestralidade }` | nome de raça | `id` (só 28/120 raças têm) | `state.ancestralidade` já É a forma keyify(nome) usada como chave em todo o app — consistente com a convenção atual, mas seria mais robusto se resolvido por id quando disponível |
| 30 | MÉDIA | DerivedAttributesCalculator.kt:58,67,80 | `it.name.keyify() == Constants.EDGE_ELDERLY/SLOW/OBESE.keyify() \|\| it.id.keyify().endsWith(...)` | mistura nome OU id (com `.endsWith`, não `==`) | apenas `it.id == "idoso"/"lento"/"obeso"` (ids confirmados em complicacoes.json) | Já tenta usar id, mas mantém o fallback por nome e usa `.endsWith` em vez de igualdade exata no id — parcialmente migrado |
| 31 | MÉDIA | ModifierEngine.kt:321 | `if (variant == "Resistente" && !hasResistenciaMaisUm)` dentro do bloco MIMICOS | nome de variante | id de opção de variante (`VariantOption.id`) | |
| 32 | MÉDIA | ModifierEngine.kt:429-446 | `if (key == "MUSCULOSO")`, `key == "BRUTAMONTES" \|\| key == "BRAWNY"`, `key == "BRIGAO" \|\| key == "PUGILISTA"`, `key == "LIGEIRO"`, `key == "BLOQUEAR"`, `key == "BLOQUEAR APRIMORADO"` (`key = vant.nome.keyify()`) | nome de Vantagem | `vant.id` (ids "musculoso"/"brutamontes"/"ligeiro"/"bloquear"/"bloquear_aprimorado" confirmados) — o mesmo bloco já usa `vant.id` corretamente em outras linhas (426,448,451,454) | Padrão misto no mesmo `forEach`: algumas checagens usam id, outras nome — inconsistência interna |
| 33 | MÉDIA | CriadorState.kt:2077 | `compendioPathfinderAtivo && ancestralidade.keyify() == "ANAO"` | nome de raça, e com grafia diferente ("ANAO", singular) de outras ~12 ocorrências no mesmo arquivo que usam "ANOES" (plural) para a mesma raça | id de raça único e consistente | Risco real de bug: se "ANAO" e "ANOES" não colidirem no `keyify()` da mesma forma em todo contexto, esta checagem pode nunca disparar |
| 34 | MÉDIA | CriadorState.kt (recorrente) — ex.: 2270,2495,2528,3644,3661,4030,4116,4139,4234,4981,4993,5372,5375,5423 | `ancestralidade.keyify() == "TRANSMORFOS"/"DEMONIOS"/"TERRACOTA"/"GOBLINS"/"FERAL"/"ORACULOS"` (igualdade exata) | nome de raça exato | id de raça (`ancestralidade` já funciona como pseudo-id keyify(nome) por convenção do app, mas nem toda raça tem `id` real no JSON) | Menos frágil que o achado #25 (`.contains`) por ser igualdade exata, mas ainda depende do nome de exibição nunca mudar |
| 35 | MÉDIA | DataLoader.kt:502-514 | `localRacialAttrMinMap`/`localRacialSkillStartMap` chaveados por `rm.nome.keyify()` | nome de raça | `rm.id` quando presente (28/120 já têm) | Os mapas centrais de mínimo de atributo/perícia por raça ignoram o `id` já existente nos registros que o têm |
| 36 | MÉDIA | DataLoader.kt:244,249 | `cat.origem?.equals("super", ignoreCase = true)` | string livre "super" no campo `origem` | enum/flag estruturado de categoria | Separa equipamento "de Super" do restante só por comparação de texto no campo de origem do livro |
| 37 | MÉDIA | DataLoader.kt:580,600-601,688,697-722 | Deduplicação/merge de equipamento via `equipamentoKey()` = concatenação de `nome.keyify()` + ~20 outros campos | chave composta por nome + todos os campos de exibição | `id` estável de item (não existe no JSON — ver achado estrutural #41) | Funciona hoje, mas qualquer edição de texto num item (ex.: correção de descrição) muda a chave e pode duplicar o item em vez de atualizá-lo |
| 38 | MÉDIA | Constants.kt (arquivo inteiro) | Todas as constantes `SKILL_*`/`ATTR_*`/`EDGE_*` são apenas o texto em maiúsculas do nome de exibição (ex.: `SKILL_FIGHTING = "LUTAR"`) | texto canônico do nome, não um id do catálogo | Para `EDGE_*`, os ids reais já existem em vantagens.json (ver Constants.ID_* que já existe para outros casos) e deveriam substituir o padrão `EDGE_*` | Já é "centralizado" (reduz duplicação de string literal), mas continua sendo comparação por nome — próximo passo lógico da migração já em andamento é trocar `EDGE_*` por `ID_*` |
| 39 | MÉDIA | CriadorState.kt:5977-5981 | `val key = it.nome.keyify(); ...; equipamentosComprados.add(kitItem)` (busca de item de kit por nome) | nome de equipamento | id de equipamento (não existe) | |
| 40 | MÉDIA | model/Pericia.kt, model/Pericias.kt (PericiaJson) — estrutural | `data class Pericia(val nome: String, ...)` / `data class PericiaJson(val nome: String, ...)` sem campo `id` | Todo o catálogo de Perícia (66 registros em pericias.json) não tem `id` | Adicionar `id` estável ao catálogo de Perícias, espelhando o que já está em andamento para Ancestralidade | Gap estrutural na base — a causa raiz de várias comparações por `nome.keyify()` de perícia no restante do código (achados #17-19, #28) |
| 41 | MÉDIA | model/EquipamentoModels.kt — estrutural | `data class EquipamentoItem(val nome: String, ...)` sem campo `id`; confirmado 0/3243 itens de equipamentos.json têm `id` | Todo o catálogo de Equipamento não tem `id` | Adicionar `id` estável a cada item do catálogo de Equipamento | Gap estrutural mais impactante do relatório — é a causa raiz de praticamente todos os achados de Equipamento (#22,#23,#27,#36,#37,#39) |
| 42 | MÉDIA | AncestryVariantSystem.kt:57-59 | `tracosParaRemoverPorNome: List<String>` (comentário: "Nomes de traços/vantagens automáticas da raça base que esta opção substitui/revoga") | campo do modelo é explicitamente por nome, por design | campo irmão por id já existe (`tracosParaRemoverPorId`) mas este campo paralelo continua por nome | Já documentado como exceção pontual no próprio arquivo; risco baixo mas seria consistente unificar com o campo por id existente |
| 43 | MÉDIA | AncestryVariantSystem.kt:60-62 | `desvantagensParaRemover: List<String>` (Complicações raciais removidas por Variante, comparadas por nome) | nome da Complicação | `Complicacao.id` (existe e é usado em outras partes do app) | Mesmo padrão do achado #42, mas para Complicações em vez de Vantagens |
| 44 | BAIXA | model/Atributos.kt — estrutural | `data class AtributoJson(val nome: String, ...)` sem campo `id` | 5 atributos fixos do sistema (Agilidade/Astúcia/Espírito/Força/Vigor), improváveis de mudar de nome | id estável, se algum dia necessário | Risco baixo dado o conjunto fechado e estável de 5 atributos do SWADE |
| 45 | BAIXA | DataLoader.kt:301-302,353-359 | `localListaAtributos`/`localMapaAtributosDisplay`/`localMapaAtributosDescricao` chaveados por `it.nome.keyify()` | nome de atributo | idem acima — não há id no JSON de atributos | Consequência direta do achado #44; risco baixo pelo mesmo motivo |
| 46 | BAIXA | CriadorState.kt:220 | `this.ancestryMap = this.listaAncestralidadesJson.groupBy { it.nome.keyify() }` | nome de raça como chave de agrupamento central | `id` quando presente, nome como fallback | Estrutura central usada por toda a tela de Ancestralidade; hoje consistente com a convenção "keyify(nome) = chave" do app, mas seria o primeiro lugar a atualizar se/quando `ancestralidades.json` ganhar `id` em 100% dos registros |
| 47 | BAIXA | AncestryVariantRegistry.kt:29-55,68 (nota, não é um problema em si) | `ancestralidadeId = "RAKASHANOS"` etc. — o campo chamado "id" é na verdade `keyify(nome)`, não um `id` real do JSON | comentário do próprio arquivo já admite: "indexado por id estável (mesmo valor de `ancestralidade.keyify()`)" | usar `RacialModifier.id` quando existir, com fallback para `keyify(nome)` | Listado apenas para registro — é o padrão mais bem documentado do módulo e não deve ser tratado como bug, mas evidencia que "id" e "nome keyificado" estão sendo tratados como sinônimos em todo o app por falta de `id` real na maioria das raças |

---

## Arquivos sem achados relevantes

- **model/MonstroTemplate.kt**: já usa `id` estruturado (`MonstroHabilidade.id`, `ArmaNatural` estruturado) — referência de bom padrão, comentários no próprio arquivo descrevem a migração de regex-sobre-texto para dado estruturado.
- **model/RacialModifier.kt**: `RacialCaracteristicasResolver` já lê por id/campo estruturado (`RacialTraitPointCatalog.efeitoDe(id)`), não por nome — bom padrão, exceto o filtro pontual `filterNot { it.keyify() == Constants.ID_AA_AGENT_SYN.keyify() }` (linha 183), que já é por id (`ID_AA_AGENT_SYN`, não `EDGE_*`), então não entrou na tabela.
- **model/AnaoCiberTraits.kt**: catálogo já 100% por `id`; a única exceção documentada é a necessidade de a `injecaoMecanica` produzir uma string de texto ("Aparar -1") para o `ModifierEngine` ler via regex genérico — sintoma indireto do achado #15/#23 do `ModifierEngine`, não um hardcode próprio deste arquivo.
- **ui/sections/EquipamentoFormatters.kt**: puramente formatação de exibição a partir de campos estruturados (`JsonElement`) — nenhuma decisão de lógica por nome/texto.
- **model/Atributos.kt, Pericia.kt, Pericias.kt, EquipamentoModels.kt**: ver achados estruturais #40, #41, #44 (ausência de `id`), listados na tabela.

## Nota metodológica

`CriadorState.kt` tem ~7000 linhas; a varredura foi feita por `Grep` com os termos indicados no pedido (`.nome.keyify()`, `ancestralidade ==`, `.contains(`, `Constants.SKILL/EDGE/ATTR`, etc.), seguida de leitura de contexto ao redor de cada agrupamento de ocorrências. Dado o volume (~150+ ocorrências brutas de `ancestralidade.keyify()`/`.nome.keyify()` só neste arquivo), a tabela acima consolida ocorrências do mesmo padrão em uma linha representativa por bloco/técnica, citando a linha mais ilustrativa e apontando na coluna "Observações" onde o mesmo padrão se repete. Isso prioriza cobertura dos *padrões* de hardcode (o que o dono do projeto pediu para eliminar) sobre uma listagem exaustiva de cada uma das dezenas de linhas quase-idênticas.
