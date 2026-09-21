# Auditoria de mecânica racial por id/tag (2026-08-31)

Contexto: pedido do dono do projeto pra verificar, raça por raça e livro por
livro, se o que o livro descreve (Tamanho, Resistência, Voo etc.) realmente
vira número calculado por `id`/`category` no app, em vez de regex/nome-de-
traço em texto livre. Este arquivo lista o que já foi corrigido nesta rodada
e o que ainda precisa de decisão antes de mexer.

## Já corrigido nesta rodada

- **Voo sem tier**: id genérico `VOO` usado tanto pra Mov 6 (Fadas) quanto
  Mov 12 (Avianos/Celestiais) — custo de ponto errado pra Fadas, sem
  indicação de valor em lugar nenhum. Separado em `VOO_MOV_6`/`VOO_MOV_12`/
  `VOO_MOV_24` (ids, `ancestralidades.json`, `RacialTraitPointCatalog`
  CUSTOS/LABEL). `ASAS_DE_ANJO` (Anjo, Cidade do Sol a Vapor) também
  cadastrado (antes sem custo nem rótulo).
- **Rótulo cru no editor de Variante**: `ResolveVariantPointBudgetUseCase.
  habilidadeComoItem` mostrava `habilidade.nome` (ex.: "Resistente") em vez
  do efeito mecânico resolvido (ex.: "Atributo aumentado d6: Vigor"). Agora
  usa `RacialTraitPointCatalog.LABEL`/`EFEITOS` primeiro, com fallback pro
  nome cru só quando não há id reconhecido.
- **RECLUSO** (Anjo, CSV) sem entrada nenhuma em CUSTOS/LABEL — cadastrado
  (-2, tier de `penalidade_pericia_2`). Confirmado com o dono do projeto:
  perícia é teste de jogo, não dado de construção — não precisa de
  modificador calculado, só precisa que o custo/rótulo do traço fique
  registrado certinho (feito).
- **Escolha de perícia nos traços genéricos do catálogo oficial**:
  `bonus_pericia_1/2`/`penalidade_pericia_1/2` (basico_habilidades_raciais.
  json) não tinham nenhum jeito de registrar QUAL perícia foi escolhida ao
  montar uma raça/Variante customizada — o traço entrava com o nome genérico
  igual pra qualquer perícia. Adicionado um picker em `SettingsDialog.kt`
  (mesmo padrão já usado pro Super Poder racial): ao marcar um desses 4
  traços, abre "Escolher Perícia" e o traço final entra como, por exemplo,
  "Bônus de Perícia (+1): Intimidar" — id continua o mesmo
  (`bonus_pericia_1`), só o nome/descrição da instância ficam
  autoexplicativos. Não calcula o +1/-1 em teste nenhum, por decisão
  confirmada — só deixa registrado o que é.
- **Mordida/Garra/Chifre/Casco/Ferrão — já estava certo, conferido**: as 21
  entradas de arma natural em `ancestralidades.json` já têm
  `armasNaturais` estruturado (`dano`, `pa`, `escalavel`) e
  `CriadorState.kt:1764-1770` já lê direto desse campo — o bloco de regex/
  palavra-chave antigo (achado de auditoria anterior) já tinha sido
  substituído antes desta sessão. Nenhuma ação necessária.
- **Tag de "asas físicas"**: já existe um mecanismo funcionando —
  `requisitos.tags` de Vantagem (ex.: `golpe_de_asa`, Fantasia) é validado
  contra `RacialModifier.tags` da raça (`CriadorState.kt:4873-4875`,
  `ValidateRequirementsUseCase.kt:83-85`), e separadamente
  `requisitos.templatesRequired` (ex.: `asas_demonio`, `ataque_alado (Anjo)`,
  Horror) é validado contra o Monstro Heroico selecionado
  (`CriadorState.kt:4879-4881`) — os dois já funcionam certos hoje. O que
  achei de errado: Avianos (Básico/Horror/Super) e Celestiais
  (Básico/Super) tinham a mesma habilidade de Voo que suas cópias de
  Fantasia/Sci-Fi, mas SEM o `tags: ["asas"]` que essas cópias têm —
  corrigido (+ Anjo do Cidade do Sol a Vapor, que não tinha tag nenhuma).
  O Anjo/Demônio do Horror (Monstro Heroico) não precisam dessa tag: já são
  travados por `templatesRequired`, que é mais preciso (trava no monstro
  exato, não em "qualquer um com asas").

## Segunda rodada — Tamanho, Movimentação, Resistência variável e Armadura Natural (mesma data)

Os 4 itens que ficaram pendentes na primeira rodada foram todos corrigidos
pra ler por id, seguindo a regra que o dono do projeto fechou: Tamanho
mínimo -1 / máximo +3 pra raças normais, só raças com indicativo de
Minúsculo (Fadas, Povo Rato, Ferais) podem ir a -3/-4; e uma Complicação
como Pequeno pode empurrar a Resistência além do que o Tamanho exibido
sozinho sugeriria, sem isso precisar aparecer no Tamanho mostrado.

- **`RacialTraitEffect` ganhou 3 casos novos**: `TamanhoBonus(valor,
  minusculo)` (o `minusculo` é o que deixa a tela mostrar -3/-4 em vez de
  travar em -1 — mesmo mecanismo que já existia, só que agora ligado por id
  em vez de nome de raça), `ArmaduraBonus(valor)` e `Composite(efeitos)`
  (pra um traço com mais de um efeito numérico ao mesmo tempo — só usado
  por Despretensiosos e Barrigudos dos Tanukimimi, Aparar -1 + Movimentação
  -1 juntos).
- **`ModifierEngine`**: removido por completo o bloco "Size from Ancestry",
  o bloco "Diminuto" e o bloco "Generic Parsing" (Resistência/Armadura/
  Movimentação/Aparar) — todos regex sobre nome/descrição do traço. Um loop
  só, por id, cobre Resistência/Passo/Aparar/Tamanho agora (Armadura não
  entra nesse motor — ver abaixo).
- **Achado no meio do caminho**: várias raças (Centaux, Aurax, Drakens,
  Ferais, Mímicos, Umvee) recebem esses traços por Variante/Seleção como
  texto solto (`vantagensRaciais`/`desvantagensRaciais`), não como
  `RacialAbility` com id de verdade. Pra não perder esses casos ao tirar o
  regex, criei `String.autoTraitId()` (`util/StringExtensions.kt`) — a
  MESMA função que `CriadorState.addIfAbsent` já usava (só extraída pra um
  lugar só) — e o `ModifierEngine` agora também reconhece um traço por esse
  id derivado do texto, não só pelo id já anexado à habilidade. Ainda é só
  id (`"RESISTÊNCIA +2".autoTraitId() == "RESISTENCIA_2"`, comparação exata
  no mapa), não regex/`contains` sobre o texto.
- **Armadura Natural**: `ResolveAncestrySpecificAdjustmentsUseCase.execute`
  ganhou o parâmetro `racialAbilityIds` (ids de `habilidades[]` da raça já
  resolvida, calculados em `ApplyAncestryChangeCoordinatorUseCase` a partir
  de `targetAncestryDef.habilidades`) — Sáurios/Golens/Draconianos/
  Insetoides agora só ganham a Armadura +2 quando o id `ARMADURA_2`
  realmente está presente na raça resolvida, em vez de fixo por
  `ancKey == "SAURIOS"` etc. Não precisou de `ModifierTarget.ARMOR` (esse
  alvo do `ModifierEngine` já não é lido por ninguém no app — achado
  incidental, registrado abaixo).
- Ids cobertos: `TAMANHO_MENOS_1/MAIS_1/MAIS_2/3`, `PEQUENOS`, `DIMINUTO`,
  `DIMINUTO_TAMANHO_3/4` (Ferais), `RESISTENCIA_1/2`, `MOVIMENTACAO`/`_2`/`_4`,
  `ARMADURA`/`ARMADURA_2`.
- Testes existentes ajustados pra bater com os novos ids de `Modifier`
  (`racial_res_generic` → `racial_trait_RESISTENCIA_2_res`, etc.) em
  `ScifiAncestryVariantSyncTest.kt`, `ModifierEngineAdgAncestryTest.kt`,
  `ResolveAncestrySpecificAdjustmentsUseCaseTest.kt` (+ 1 teste novo,
  confirmando que Sáurios sem o id `ARMADURA_2` não ganha mais Armadura de
  graça) e `ResolveAncestryRacialPackageUseCaseTest.kt`.

## Achado incidental (não mexi, só documentando)

`ModifierTarget.ARMOR` do `ModifierEngine` — usado pelo bloco de Equipamento,
pela Vantagem "Couro Blindado" e (antes desta rodada) pelo regex de Armadura
— nunca é somado por ninguém no app (`ModifierEngine.sum(state,
ModifierTarget.ARMOR)` não aparece em lugar nenhum fora do próprio
`ModifierEngine`; a Armadura real do personagem é calculada à parte, em
`SummaryUtils.kt`/`ResumoPdfReferenciador.kt`, direto de
`naturalArmorFromRace`/equipamento). Ou seja, esse pedaço do motor já era
morto antes de eu mexer em qualquer coisa — não é uma regressão desta
rodada, mas fica registrado caso você queira limpar depois.

## Terceira rodada — eliminação completa do "tradutor" de texto (mesma data)

A segunda rodada corrigiu o cálculo, mas introduziu um `String.autoTraitId()`
que o `ModifierEngine` chamava em tempo real sobre texto solto de
`vantagensRaciais`/`desvantagensRaciais` pra "adivinhar" o id mecânico —
exatamente o tipo de "gambiarra" que o dono do projeto pediu pra eliminar por
completo (ele mesmo notou o problema: "esse sistema tá lendo texto e gerando
id em tempo real ao usar o app... isso é mais complexo do que ajustarmos na
mão"). Essa rodada removeu essa função por completo e ajustou à mão todas as
raças/Variantes que dependiam dela.

- **`TraitAddition(nome, id)`** (`AncestryVariantSystem.kt`): substitui
  `List<String>` por `List<TraitAddition>` nos três campos de
  `ResolvedTraitPackage` que injetam traço novo (`tracosParaAdicionar`,
  `vantagensGratisParaAdicionar`, `desvantagensParaAdicionar`). Cada uma das
  ~40 entradas nas 25 raças/Variantes de `AncestryVariantRegistry.kt` (e nas 6
  do Dom da Natureza do Umvee, e no catálogo de traços negativos do Anão
  Ciber em `AnaoCiberTraits.kt`) agora carrega um id mecânico ESCRITO À MÃO
  no código-fonte, nunca calculado a partir do texto de exibição em tempo de
  execução. Ids já existentes em `RacialTraitPointCatalog.EFEITOS`/`CUSTOS`
  foram reaproveitados quando o conceito é o mesmo (ex.: Drakens "Padrão"
  usa "RESISTENCIA_2", igual a qualquer outra raça com Resistência +2); ids
  novos só onde o traço é genuinamente novo/narrativo.
- **`state.racialTraitIdsFromVariants`** (`CriadorState.kt`): nova lista,
  paralela a `vantagensRaciais`/`desvantagensRaciais`, que recebe os ids reais
  vindos de `ResolveAncestryRacialPackageUseCase.Result.racialTraitIds` —
  populada no mesmo lugar (`aplicarAncestralidade`) e com o mesmo
  clear+addAll a cada troca de raça/Variante. `ModifierEngine` agora lê essa
  lista direto pra somar ao conjunto de ids reconhecidos, e o
  `autoIdKeys`/`String.autoTraitId()` foi removido por completo (a função
  não existe mais em `util/StringExtensions.kt`).
- **Achado ao converter (bug pré-existente, não desta rodada)**: Umvee
  "Pedregoso" concedia Resistência +1 por DOIS caminhos ao mesmo tempo — o
  `when(dom)` de `CriadorState.applyAncestryVariantAdjustments` (id
  `RESISTENCIA`, já existia) e o pacote fixo do `AncestryVariantRegistry`
  (que teria virado `RESISTENCIA_1` se eu tivesse só copiado o slug antigo).
  Dois ids diferentes pro mesmo efeito = Resistência contada em dobro pra
  quem escolhe Pedregoso. Corrigido usando o MESMO id (`RESISTENCIA`) nos
  dois lugares — não craqueei isso rodando o app (não dá nesta sandbox),
  conferi lendo os dois caminhos lado a lado.
- **Achado incidental de efeito faltando (corrigido)**: "Aparar +1" (Umvee,
  Pele Iluminada pela Lua) e "Aparar -1" (Anão Ciber, traço Aparar Baixo)
  cada um já tinha um id de verdade escrito à mão há tempos, só que nenhum
  dos dois tinha entrada em `RacialTraitPointCatalog.EFEITOS` — ou seja, o
  traço aparecia na ficha mas nunca somava nada no Aparar. Adicionados
  `APARAR_1` (+1) e `APARAR_MENOS_1` (-1, id próprio pra não colidir com
  `APARAR_BAIXO` que vale -2).
- **Monstros do Horror — conferido, já estava certo**: auditei
  `horror_monstros.json`/`MonstroTemplate.kt` procurando o mesmo padrão
  (texto solto com "Resistência"/"Tamanho"/"Movimentação"/"Armadura" fora de
  `habilidades[].id`). Achei só um caso — Múmia, Complicação "Lento:
  Movimentação reduzida em 1..." — e esse já funciona certo: o
  `ModifierEngine` só lê o rótulo antes dos ":" (`"Lento"`), que bate direto
  com a chave `LENTO` do catálogo por normalização de acento/maiúscula
  (`keyify()`), sem precisar de nenhum "tradutor"/regex. Todo o resto de
  Anjo/Demônio/Fantasma/Lobisomem/Monstro de Retalhos/Múmia/Revivido/Vampiro
  já usa `habilidades[].id` de verdade. Nenhuma mudança necessária.
- Testes ajustados: `ResolveAncestryVariantPackageUseCaseTest.kt`,
  `ResolveAncestrySpecificAdjustmentsUseCaseTest.kt` (reescrito por completo
  pros novos tipos, incluindo os dois casos de nota-pro-mestre que passaram a
  morar em `anotacoesToAdd` em vez de `ensureRacialDisadvantages` —
  Possessores Energia e Quadroides Habilidoso), `ScifiAncestryVariantSyncTest.kt`
  (injeta `racialTraitIdsFromVariants` nos testes que simulam Variante
  manualmente, no lugar do texto sozinho que o `autoTraitId()` removido
  reconhecia antes).

## Pendente — sem mudança nesta rodada

**Hardcode residual por nome de raça**: `CriadorState.kt` ainda tem a
exceção Meio-Orc/Intimidar do Pathfinder (linha ~4458) e a escolha de
atributo por Variante de Drakens/Elementais (linhas ~4310/4329) comparando
`ancestralidade.keyify().contains(...)`/`== "DRAKENS"` em vez de id. Não
mexi porque você não pediu esses dois desta vez — ficam pra quando você
quiser.

**Passo de atributo (Forte/Robusto etc.) via Variante ainda não chega em
`atributoBaseRacial()` pra várias raças Sci-Fi**: esse cálculo (Drakens
"Padrão" começar com Força d6, por exemplo) lê `currentAncestryDef.habilidades`
direto — não os ids novos de `state.racialTraitIdsFromVariants` que esta
rodada criou. Pra raça com um candidato só no JSON (a maioria das raças
Sci-Fi), `getAncestralidadeDef` nem chega a rodar
`applyAncestryVariantAdjustments`, então o "Forte" da Variante nunca entra em
`habilidades[]` — o dado de Força fica no valor base mesmo com o traço
"presente" na lista de vantagens. Isso já era assim antes desta rodada (não é
regressão: `autoTraitId()` nunca foi chamado por esse caminho, só pelo
`ModifierEngine`) — fica registrado porque apareceu enquanto eu confirmava
que os testes de atributo (`elementais scifi comecam com forca d8`, por
exemplo) continuavam passando por outro motivo (`racialAttrMinMap`, não o
traço). Resolver isso de vez pediria estender `atributoBaseRacial()` pra
também ler `racialTraitIdsFromVariants`, igual ao que já fiz no
`ModifierEngine` — não fiz porque não é o que você pediu desta vez, mas é o
mesmo tipo de buraco.

## Quarta rodada — traços empilháveis ("vezes") por indicação (N)/(S) do livro

Pedido do dono do projeto: ele releu o livro e percebeu que cada traço do
catálogo de criação de ancestralidade traz um indicador `(N)`/`(S)` — quantas
vezes pode ser comprado, com custo E efeito escalando linearmente por compra
(ex.: "Armadura (3): ... Armadura +2 cada vez que é comprada", até +6 em 3
compras) — mecânica que o app não modelava (a rodada anterior já tinha
resolvido o CÁLCULO de cada traço, mas sempre como compra única).

- **Fonte dos dados**: lidos direto de `docs/swade_basico`, `docs/swade_fantasia`
  e `docs/swade_scifi` (extratos em texto dos 3 livros), seção "Habilidades
  de Ancestralidade". Confirmado por citação exata: Básico "-1 Aparar Baixo
  (3): ... Aparar -1" — a pergunta original do dono do projeto (Aparar Baixo é
  -1 ponto e pode ser pego até 3x) estava certa; minha resposta anterior,
  dizendo que não achei repetibilidade nenhuma, tinha checado só o JSON do
  app, não o livro.
- **7 traços EMPILHÁVEIS identificados** (mesmo efeito, escala por compra):
  Armadura (até 3, +2/compra), Resistência (até 3, +1/compra), Aparar (até 3,
  +1/compra), Aparar Baixo (até 3, -1/compra), Tamanho +1 (até 3, +1/compra),
  Frágil (até 2, -1/compra), Movimentação (até 2, +2/compra). Traços de TIER
  único já cobertos corretamente antes (Diminuto/Minúsculo, Voo, Toque
  Venenoso, Sentidos Aguçados) foram conferidos contra o livro e não mudaram
  — cada compra ali dá um efeito DIFERENTE (não o mesmo efeito somado), então
  não são "empilháveis" no sentido desta rodada.
- **Modelo novo**: `RacialAbility.vezes`/`HabilidadeCriacao.vezes` (compra
  já feita) e `HabilidadeCriacao.vezesMax` (teto do catálogo), mais
  `RacialTraitPointCatalog.VEZES_MAX`/`vezesMaxDe()`/`labelComVezes()`. Os ~10
  ids sintéticos por VALOR FINAL que a rodada anterior tinha criado
  (`RESISTENCIA_1/_2`, `ARMADURA_2`, `TAMANHO_MAIS_1/_2/TAMANHO_3`,
  `MOVIMENTACAO_2/_4`, `APARAR_1/APARAR_MENOS_1`, `FRAGIL_MAIOR`) colapsaram
  em 7 ids base (`RESISTENCIA`, `APARAR`, `APARAR_BAIXO`, `TAMANHO_MAIS_1`,
  `FRAGIL`, `MOVIMENTACAO`, `ARMADURA`) + `vezes` — exatamente a duplicidade
  de id que o dono do projeto suspeitava estar causando parte da bagunça.
  `ModifierEngine` agora junta `vezes` de todas as fontes (habilidade da
  raça, Monstro Heroico, `racialTraitIdsFromVariants`) pelo MAIOR valor, nunca
  soma — mesma cautela que corrigiu o Tamanho duplicado de Fadas/Povo Rato na
  rodada anterior, aplicada de novo aqui pra não somar a mesma compra duas
  vezes quando o mesmo id aparece por mais de um caminho.
- **Dois bugs pré-existentes achados e corrigidos no caminho** (fora do
  escopo original do pedido, mas na mesma categoria de "id derivado de texto
  em tempo real" que a rodada anterior tinha como missão eliminar):
  1. `basico_habilidades_raciais.json` (catálogo de criação de raça customizada)
     e `RacialTraitPointCatalog` viviam em namespaces de id DIFERENTES pros
     mesmos 7 traços (ex.: `armadura_racial` vs `ARMADURA`) — corrigido
     renomeando os 7 ids do catálogo pra bater com o namespace mecânico.
  2. Ao adicionar um traço customizado numa Variante/Raça pelo Mestre
     (`CriadorState.kt` e `SettingsDialog.kt`, tela "Criar Raça"), o id final
     do `RacialAbility` era CALCULADO A PARTIR DO TEXTO DE EXIBIÇÃO em tempo
     de execução (`trait.nome.lowercase().replace(" ", "_")`/`toIdSlug()`) em
     vez de usar o `id` de verdade que o próprio catálogo já carregava — ou
     seja, nenhum traço adicionado por essa tela jamais teve efeito mecânico
     de verdade, empilhável ou não. Corrigido pra `id = trait.id ?:
     trait.nome.toIdSlug()` (o fallback só entra pra conteúdo bespoke sem id
     de catálogo).
- **UI**: novo picker "Quantas vezes?" em `SettingsDialog.kt` (mesmo padrão
  de `periciaTraitPickerTarget`) — ao marcar um traço com `vezesMax > 1` no
  seletor de Traços Raciais (tanto "Criar Raça" quanto Variante custom), abre
  a escolha de 1..vezesMax compras antes de adicionar, já mostrando o rótulo
  e custo final de cada opção via `labelComVezes`.
- **Migração de dados**: `ancestralidades.json` (21 entradas de habilidade em
  raças oficiais) e `basico_habilidades_raciais.json` (7 entradas do catálogo
  de criação) migrados pros ids/vezes novos; `AncestryVariantRegistry.kt` (9
  `TraitAddition`) e `AnaoCiberTraits.kt` (1 `injecaoId`) idem.
  `ResolveVariantPointBudgetUseCase.habilidadeComoItem` também ajustado —
  sem isso, remover um traço empilhável de 2+ compras da raça base no editor
  de Variante devolveria só o custo de 1 compra, fechando o orçamento errado.
- **Escopo confirmado com o dono do projeto**: migração completa, incluindo
  as raças oficiais (não só o construtor de raça customizada).
- Testes ajustados: `RacialTraitPointCatalogTest.kt` (`FRAGIL_MAIOR` não
  existe mais, `APARAR_BAIXO` agora é -1/compra não -2 fixo, teste novo de
  `vezesMaxDe`/`labelComVezes`), `ScifiAncestryVariantSyncTest.kt`
  (`racialTraitIdsFromVariants` passou de `List<String>` pra
  `List<RacialTraitStack>`), `ResolveAncestryVariantPackageUseCaseTest.kt`,
  `ResolveAncestrySpecificAdjustmentsUseCaseTest.kt`,
  `ResolveAncestryRacialPackageUseCaseTest.kt` e
  `ModifierEngineCidadeSolVaporTest.kt` (todos com ids sintéticos removidos
  trocados pelos 7 ids base + `vezes`).

## Quinta rodada — auditoria específica: Centauros (Fantasia)

Pedido do dono do projeto: conferir especificamente a raça Centauros
(livro Fantasia, `ancestralidades.json:596-651`) — quais ids as habilidades
usam, se cada id tem custo/efeito de verdade cadastrado no
`RacialTraitPointCatalog`, e se os valores batem com o texto do livro.

Habilidades cadastradas (5), na ordem do JSON:

| Nome no JSON | id | category | Custo (`CUSTOS`) | Efeito (`EFEITOS`) | Observação |
|---|---|---|---|---|---|
| CASCOS | `GARRAS_SEM_PA` | racial_trait_positive | +2 | sem entrada — arma natural lida direto do campo `armasNaturais` (`For+d4`, pa 0, escalável), não pelo `ModifierEngine` | id genérico reaproveitado por outras 7 raças com ataque natural For+d4 sem PA (Sáurios, Draconianos etc.) |
| DEPENDÊNCIA | `DEPENDENCIA` | racial_trait_negative | -2 | sem entrada — regra de Fadiga é condicional de jogo, não dado de construção | mesmo padrão já confirmado pro RECLUSO (Anjo, primeira rodada): custo/rótulo cadastrados, sem cálculo automático por decisão do dono do projeto |
| FORMA INCOMUM | `FORMA_ALIENIGENA` | racial_trait_negative | -1 | sem entrada — restrição de uso de equipamento é textual | mesmo id reaproveitado por Insetoides (Fantasia) "Formato Corporal Incomum"; cada raça mantém seu próprio `nome` de exibição no JSON |
| MOVIMENTAÇÃO +4 | `MOVIMENTACAO` | racial_trait_positive | 2/compra × `vezes: 2` = 4 | `PassoBonus(2)` × vezes → Movimentação 10, dado de corrida d10 | `VEZES_MAX["MOVIMENTACAO"] = 2`; `vezes: 2` no JSON já está no teto do livro |
| TAMANHO +1 | `TAMANHO_MAIS_1` | racial_trait_positive | 1 | `TamanhoBonus(1)` → emite `SIZE_DISPLAY` e `SIZE_TOUGHNESS` (+1 Tamanho exibido E +1 Resistência) | bate com o texto do livro ("Adicione +1 a sua Resistência") |

Soma de custos: 2 - 2 - 1 + 4 + 1 = **4**, igual ao `pontosRaciaisEsperados: 4`
já declarado no JSON. Nenhuma divergência.

Conferido também:
- Nenhum `LABEL` genérico cadastrado pros 3 ids sem efeito numérico
  (`GARRAS_SEM_PA`, `DEPENDENCIA`, `FORMA_ALIENIGENA`) — cai no fallback do
  nome cru da própria raça (`CASCOS`, `DEPENDÊNCIA`, `FORMA INCOMUM`), que já
  é o texto certo pra Centauros. O fallback só seria problema se o rótulo
  genérico do catálogo fosse mostrado no lugar errado, o que não acontece
  aqui (mesmo padrão documentado na rodada 1, item "Rótulo cru no editor de
  Variante").
- `MOVIMENTACAO` e `TAMANHO_MAIS_1` já constavam na lista de ids cobertos
  desde a segunda/quarta rodada (mesmo arquivo, acima) — nada novo precisou
  ser cadastrado.
- As "Ideias Variantes" do livro pra Centauros (Tamanho +2 e Musculoso pra
  centauros de guerra; Voo + Movimentação reduzida a 8/corrida d8 pra
  centauros-pégaso) não têm pacote jogável em `AncestryVariantRegistry.kt` —
  existem só como texto narrativo no campo `variantes` do JSON. Não é bug
  (nenhuma outra raça exige que toda Ideia Variante do livro vire Variante
  jogável no app), só fica registrado caso o dono do projeto queira
  transformar isso em Variante de verdade depois.

**Resultado: a raça já estava 100% correta antes desta rodada — nenhuma
mudança de código foi necessária.** Auditoria feita só pra conferir e
documentar, a pedido do dono do projeto.

## Sexta rodada — auditoria completa do livro Fantasia (28 raças restantes)

Pedido do dono do projeto: repetir a mesma auditoria da rodada anterior
(ids/custo/efeito por habilidade) pras outras 28 raças de `livros:
["FANTASIA"]` em `ancestralidades.json` (Centauros já tinha sido feita).

**Método**: script Python lendo `RacialTraitPointCatalog.CUSTOS`/`EFEITOS`/
`VEZES_MAX` do `.kt` e cruzando com cada `habilidades[]` de cada raça,
reproduzindo a MESMA lógica de `custoDe()` (não só um match direto no mapa):
normalização por `keyify()` (maiúsculo, sem acento) e o caso especial de
`FORASTEIRO`/`PACIFISTA`/`SEM_ESCRUPULOS`/`SENSIVEL`/`VOTO`, cujo custo real
depende do campo `severity` da própria habilidade (Menor -1 / Maior -2,
default Maior quando o campo falta) em vez do valor fixo do mapa. A primeira
tentativa, sem considerar `severity`, dava total errado pra Sáurios e outras
raças com Forasteiro (Menor) — corrigido antes de fechar o resultado.

**Verificado por raça**: soma de custo (`custo × vezes`) de cada habilidade
comparada com `pontosRaciaisEsperados` (default 2 quando o campo não existe
no JSON — é o valor padrão de `RacialModifier.pontosRaciaisEsperados` em
`DataLoader.kt`/`RacialModifier.kt`); todo id presente em `CUSTOS`; todo
`vezes > 1` coberto por `VEZES_MAX` e dentro do teto; sinal do custo batendo
com a `category` (`racial_trait_positive` só custo positivo, `_negative` só
negativo).

**Resultado: as 28 raças fecham exatamente em 2 pontos (padrão do livro,
igual ao Humano) — nenhuma delas tem `pontosRaciaisEsperados` explícito no
JSON porque nenhuma precisa (só Centauros foge do padrão, com 4). Nenhum id
sem custo cadastrado, nenhum `vezes` fora do teto do `VEZES_MAX`, nenhuma
categoria com sinal trocado.** Tabela completa (formato `Nome (id[,
severidade][×vezes]) custo`):

- **Anões** (2/2): Movimentação Reduzida (`MOVIMENTACAO_REDUZIDA`) -1;
  Robusto (`ROBUSTO`) +2; Visão no Escuro (`VISAO_NO_ESCURO`) +1
- **Aquarianos** (2/2): Aquático (`AQUATICO`) +2; Dependência
  (`DEPENDENCIA`) -2; Resistência (`RESISTENCIA`) +1; Visão no Escuro
  (`VISAO_NO_ESCURO`) +1
- **Avianos** (2/2): Frágil (`FRAGIL`) -1; Movimentação Reduzida
  (`MOVIMENTACAO_REDUZIDA`) -1; Não Sabe Nadar (`NAO_SABE_NADAR`,Menor) -1;
  Sentidos Aguçados (`SENTIDOS_AGUCADOS`) +1; Voo (`VOO_MOV_12`) +4
- **Celestiais** (2/2): Atraente (`ATRAENTE`) +2; Código de Honra
  (`CODIGO_DE_HONRA`,Maior) -2; Voo (`VOO_MOV_12`) +4; Voto (`VOTO`,Maior) -2
- **Descendente Elemental** (2/2): Resistência Ambiental
  (`RESISTENCIA_AMBIENTAL`) +1; Forasteiro (`FORASTEIRO`,Menor) -1; Elemento
  Ancestral (`ELEMENTO_ANCESTRAL`) +2
- **Draconianos** (2/2): Arma de Sopro (`ARMA_DE_SOPRO`) +2; Armadura +2
  (`ARMADURA`) +1; Fraqueza Ambiental/Frio (`FRAQUEZA_AMBIENTAL`) -1;
  Mal-Humorado (`ARROGANTE`,Maior) -2 (id reaproveitado — o próprio livro
  chama isso de Complicação Arrogante); Garras (`GARRAS`) +3; Mordida
  (`MORDIDA`) +1; Resistência Ambiental/Calor (`RESISTENCIA_AMBIENTAL`) +1;
  Sangue Frio (`SANGUE_FRIO`) -3
- **Elfos** (2/2): Desastrado (`DESASTRADO`,Menor) -1; Visão no Escuro
  (`VISAO_NO_ESCURO`) +1; Ágil (`AGIL`) +2
- **Fadas** (2/2): Boca Grande (`BOCA_GRANDE`,Menor) -1; Curioso
  (`CURIOSO`,Maior) -2; Desastrado (`DESASTRADO`,Menor) -1; Diminuto/Tamanho
  -4 (`DIMINUTO_TAMANHO_4`) +6; Impulsivo (`IMPULSIVO`,Maior) -2; Voo
  (`VOO_MOV_6`) +2
- **Gnomos** (2/2): Astúcia (`ASTUCIA`) +2; Movimentação Reduzida
  (`MOVIMENTACAO_REDUZIDA`) -1; Sentidos Aprimorados
  (`SENTIDOS_APRIMORADOS`) +1; Tamanho -1 (`TAMANHO_MENOS_1`) -1; Visão no
  Escuro (`VISAO_NO_ESCURO`) +1
- **Goblins** (2/2): Desagradável (`DESAGRADAVEL`,Menor) -1; Infravisão
  (`INFRAVISAO`) +1; Pequenos (`PEQUENOS`) -1; Sobrevivente (`ADAPTAVEL`) +2
  (id reaproveitado — texto do livro: "começam com uma Vantagem de Estágio
  Novato à escolha", igual ao Adaptável dos Humanos); Sorrateiro
  (`SORRATEIRO`) +1
- **Golens** (2/2): Armadura +2 (`ARMADURA`) +1; Construto (`CONSTRUTO`) +8;
  Desajeitado/Atletismo e Desajeitado/Furtividade
  (`PENALIDADE_PERICIA_1` ×2, uma entrada por perícia) -1-1; Grande
  (`VOLUMOSO`) -2; Movimentação Reduzida (`MOVIMENTACAO_REDUZIDA`) -1;
  Perícias Básicas Reduzidas (`PERICIAS_BASICAS_REDUZIDAS`×3) -3; Sem Noção
  (`SEM_NOCAO`) -2; Sem Órgãos Vitais (`SEM_ORGAOS_VITAIS`) +1; Tamanho +2
  (`TAMANHO_MAIS_1`×2) +2
- **Humanos** (2/2): Adaptável (`ADAPTAVEL`) +2
- **Infernais** (2/2): Chifres (`CHIFRES`) +1; Forasteiro
  (`FORASTEIRO`,Menor) -1; Fraqueza Ambiental/Frio (`FRAQUEZA_AMBIENTAL`) -1;
  Natureza Diabólica (`NATUREZA_DIABOLICA`) +1; Resistência Ambiental/Calor
  (`RESISTENCIA_AMBIENTAL`) +1; Visão Total no Escuro
  (`VISAO_TOTAL_NO_ESCURO`) +1
- **Insetoides** (2/2): Andar nas Paredes (`ANDAR_NAS_PAREDES`) +1; Armadura
  +2 (`ARMADURA`) +1; Ações Adicionais (`ACAO_ADICIONAL_FISICA`) +4;
  Forasteiro (`FORASTEIRO`,Menor) -1; Formato Corporal Incomum
  (`FORMA_ALIENIGENA`) -1 (mesmo id do "Forma Incomum" de Centauros); Mordida
  ou Garra (`MORDIDA`) +1; Mente de Colmeia maior (`GUIADO`,Maior) -2; Mente
  de Colmeia menor (`LEAL`) -1
- **Meio-Elfos** (2/2): Forasteiro (`FORASTEIRO`,Menor) -1; Herança
  (`HERANCA`) +2; Visão no Escuro (`VISAO_NO_ESCURO`) +1
- **Meio-Gigantes** (2/2): Analfabeto (`ANALFABETO`,Menor) -1;
  Cabeças-Duras (`CABECAS_DURAS`) -2; Forasteiro (`FORASTEIRO`,Maior) -2;
  Grande (`VOLUMOSO`) -2; Muito Forte (`MUITO_FORTE`) +4; Muito Resistente
  (`MUITO_RESISTENTE`) +4; Sem Noção (`SEM_NOCAO`) -2; Tamanho +3
  (`TAMANHO_MAIS_1`×3) +3
- **Meio-Orcs** (2/2): Endurecido (`ENDURECIDO`) +2; Forasteiro
  (`FORASTEIRO`,Menor) -1; Infravisão (`INFRAVISAO`) +1
- **Minotauros** (2/2): Chifres (`CHIFRES_MAIORES`) +2; Desagradável
  (`DESAGRADAVEL`,Menor) -1; Durão (`DURAO`) +2; Grande (`VOLUMOSO`) -2;
  Muito Forte (`MUITO_FORTE`) +4; Sem Instrução (`SEM_INSTRUCAO`) -2;
  Sensível (`SENSIVEL`,Maior) -2; Tamanho +1 (`TAMANHO_MAIS_1`) +1
- **Ogros** (2/2): Arrogante (`ARROGANTE`,Maior) -2; Desajeitado/Atletismo e
  Desajeitado/Furtividade (`PENALIDADE_PERICIA_1`×2) -1-1; Forasteiro
  (`FORASTEIRO`,Menor) -1; Grande (`VOLUMOSO`) -2; Muito Forte
  (`MUITO_FORTE`) +4; Muito Resistente (`MUITO_RESISTENTE`) +4; Robusto
  (`ROBUSTO`) +2; Sem Noção (`SEM_NOCAO`) -2; Tamanho +1
  (`TAMANHO_MAIS_1`) +1
- **Orcs** (2/2): Brutal (`SEM_INSTRUCAO`) -2; Forasteiro
  (`FORASTEIRO`,Maior) -2; Forte (`FORTE`) +2; Infravisão (`INFRAVISAO`) +1;
  Resistente (`RESISTENTE`) +2; Tamanho +1 (`TAMANHO_MAIS_1`) +1
- **Pequeninos** (2/2): Espirituoso (`ESPIRITUOSO`) +2; Movimentação
  Reduzida (`MOVIMENTACAO_REDUZIDA`) -1; Sorte (`SORTE`) +2; Tamanho -1
  (`TAMANHO_MENOS_1`) -1
- **Povo Ratazana** (2/2): Covarde (`COVARDE`,Maior) -2; Forasteiro
  (`FORASTEIRO`,Maior) -2; Ganancioso (`GANANCIOSO`,Menor) -1; Garras
  (`GARRAS_SEM_PA`) +2; Mordida (`MORDIDA`) +1; Resistência a Doenças e
  Resistência a Venenos (`IMUNE_A_DOENCAS_E_VENENOS` ×2, uma entrada por
  categoria) +1+1; Sucateiro (`SUCATEIRO`) +2; Tamanho -1
  (`TAMANHO_MENOS_1`) -1; Visão no Escuro (`VISAO_NO_ESCURO`) +1
- **Povo Rato** (2/2): Diminuto/Tamanho -4 (`DIMINUTO_TAMANHO_4`) +6; Fobia
  (`FOBIA`,Menor) -1; Forasteiro (`FORASTEIRO`,Maior) -2; Movimentação
  Reduzida (`MOVIMENTACAO_REDUZIDA`) -1; Visão no Escuro
  (`VISAO_NO_ESCURO`) +1; Almofadinha (`ALMOFADINHA`) -1
- **Povo Serpente** (2/2): Forasteiro (`FORASTEIRO`,Menor) -1; Fraqueza
  Ambiental (`FRAQUEZA_AMBIENTAL`) -1; Infravisão (`INFRAVISAO`) +1; Mordida
  (`MORDIDA`) +1; Mordida Venenosa (`TOQUE_VENENOSO`) +1; Movimentação
  (`MOVIMENTACAO`×2) +4; Sangue Frio (`SANGUE_FRIO`) -3
- **Rakashanos** (2/2): Garras (`GARRAS_SEM_PA`) +2; Inimigo Ancestral
  (`INIMIGO_ANCESTRAL`) -1; Mordida (`MORDIDA`) +1; Não Sabe Nadar
  (`NAO_SABE_NADAR`,Menor) -1; Sanguinário (`SANGUINARIO`,Maior) -2; Visão no
  Escuro (`VISAO_NO_ESCURO`) +1; Ágil (`AGIL`) +2
- **Renascidos** (2/2): Aversão Animal (`AVERSAO_ANIMAL`) -1; Bebedor de
  Sangue (`REGENERACAO`) +2 (id reaproveitado — cura ao beber sangue mapeia
  pro mecanismo genérico de Regeneração); Forasteiro (`FORASTEIRO`,Maior) -2;
  Força Sobrenatural (`FORCA_SOBRENATURAL`) +2; Mordida (`MORDIDA`) +1;
  Resistência ao Frio (`RESISTENCIA_AO_FRIO`) +1; Sensibilidade à Luz Solar
  (`SENSIBILIDADE_A_LUZ_SOLAR`) -2; Visão no Escuro (`VISAO_NO_ESCURO`) +1
- **Sáurios** (2/2): Armadura +2 (`ARMADURA`) +1; Forasteiro
  (`FORASTEIRO`,Menor) -1; Fraqueza Ambiental/Frio (`FRAQUEZA_AMBIENTAL`) -1;
  Mordida (`MORDIDA`) +1; Sentidos Aprimorados (`PRONTIDAO`) +2 (o próprio
  texto do livro diz "ganhando a Vantagem Prontidão")
- **Transmorfos** (2/2): Carismático (`CARISMATICO`) +2; Mudar de Forma
  (`ANTECEDENTE_ARCANO_PODER`) +2 (texto do livro: "Transmorfos têm
  Antecedente Arcano (Dom)"); Segredo (`SEGREDO`,Maior) -2

**Único achado — corrigido nesta rodada**: Povo Ratazana tinha
`"id": "SUCATEIRO"` gravado em minúsculo (`"sucateiro"`), único caso em
todo o livro Fantasia (todos os outros ~150 ids são maiúsculos). Não era um
bug funcional — `custoDe()` sempre normaliza por `.keyify()` antes de
comparar com o catálogo, então `sucateiro` já virava `SUCATEIRO` e casava
certo com `RacialTraitPointCatalog.kt:791` (custo +2) — mas ficava
inconsistente com o padrão do arquivo. Corrigido pra `"SUCATEIRO"` em
`ancestralidades.json` só por higiene; nenhum outro arquivo referenciava a
forma minúscula (conferido em `app/src/main/java` e `app/src/test`).

**Resultado: as 28 raças já estavam mecanicamente corretas — a única
mudança de código desta rodada foi o ajuste de capitalização do id do
Sucateiro (Povo Ratazana), sem efeito no comportamento do app.**

## Sétima rodada — Mal-Humorado (Draconianos) virando a Complicação Arrogante de verdade

Pedido do dono do projeto, depois de ver a rodada anterior: "Mal-Humorado"
(Draconianos) já tinha `category: "racial_hindrance"` e `severity: "Maior"`
— já contava como complicação racial de verdade no orçamento de pontos
(-2). O que faltava: a complicação REALMENTE concedida ao personagem usava
`hab.nome` como fallback (`RacialModifier.kt` — `resolvedDesvantagens()`/
`desvantagensEfetivas()`), então o personagem ganhava uma complicação
chamada literalmente "Mal-Humorado (Maior)" em vez da Complicação real do
livro, "Arrogante (Maior)" — apesar do `id: "ARROGANTE"` já apontar pro
conceito certo pro cálculo de custo.

- **Mecanismo usado**: `traitId: "RACIAL_HINDRANCE"` + `targetRef:
  "Arrogante"`, adicionados à habilidade "MAL-HUMORADO" de Draconianos
  (`ancestralidades.json`). Não é código novo — é o mesmo padrão já usado
  por Kitsunemimi "Excessivamente Detalhistas" (id
  `EXCESSIVAMENTE_DETALHISTAS`, `targetRef: "Cauteloso"`), citado nos
  próprios comentários de `RacialModifier.kt:206-213`/`226-247` como a forma
  correta de "nome é só skin do livro, a Vantagem/Complicação de verdade
  vem do `targetRef`".
- **Efeito**: `resolvedTraitId()` agora resolve pra `"RACIAL_HINDRANCE"`
  (traitId tem prioridade sobre `id`), então tanto
  `RacialModifier.resolvedDesvantagens()` quanto
  `desvantagensEfetivas()`/`RacialCaracteristicasResolver` passam a conceder
  "Arrogante (Maior)" em vez de "Mal-Humorado (Maior)" — a "MAL-HUMORADO"
  continua sendo só o nome de exibição da habilidade (flavor/skin), igual
  antes.
- **Custo inalterado**: `custoDe("RACIAL_HINDRANCE", severity="Maior")` cai
  no mesmo caso especial que já existia pra Forasteiro/Voto/etc. (Maior =
  -2), idêntico ao valor que vinha antes via `CUSTOS["ARROGANTE"] = -2`
  (`RacialTraitPointCatalog.kt:500`) — Draconianos continua fechando em 2/2
  pontos, sem mudança no orçamento racial.
- Mantive `id: "ARROGANTE"` na habilidade (documentação/consistência com o
  resto do catálogo), mesmo não sendo mais o que `resolvedTraitId()` usa —
  não é lido em nenhum outro lugar do código pra esse traço específico
  (conferido: nenhuma referência hardcoded a `"ARROGANTE"` ou `"DRACONIANOS"`
  fora deste arquivo e de `RacialTraitPointCatalog.kt:500`; a única outra
  ocorrência, `SummaryUtils.kt:593`, já era código morto — filtra por
  `it.keyify() == "ARROGANTE"` numa lista que só contém `nome`s, nunca
  "Arrogante" de verdade, e nunca filtrou nada mesmo antes desta mudança).
- Não rodei o app nem os testes JVM nesta rodada (sandbox sem acesso aos
  repositórios de plugin do Gradle) — validação foi só leitura cruzada do
  caminho de código (`RacialAbility.resolvedTraitId()` →
  `RacialTraitPointCatalog.custoDe()`/`RacialModifier.resolvedDesvantagens()`/
  `desvantagensEfetivas()`).

## Oitava rodada — Pequenos (Goblins) virando Tamanho -1 de verdade

Pedido do dono do projeto: conferir Goblins. "Sobrevivente" (id
`ADAPTAVEL`) já estava certo (confirmado na rodada anterior — mesmo texto
de Humanos "Adaptável": "Vantagem de Estágio Novato à escolha"). O achado
real foi em "Pequenos": tinha `id: "PEQUENOS"` próprio em vez do id
compartilhado `TAMANHO_MENOS_1` — diferente de Draconianos (rodada 7), aqui
não era um bug de comportamento (`PEQUENOS` já tinha sua própria entrada em
`EFEITOS`/`CUSTOS`, com o MESMO efeito — `TamanhoBonus(-1)` — e o MESMO
custo — -1 — que `TAMANHO_MENOS_1`, e nenhum dos dois tem `LABEL`
cadastrado, então a exibição já usava o `nome` cru "PEQUENOS" dos dois
jeitos). Era duplicidade de id pro mesmo conceito — exatamente o tipo de
coisa que a rodada 3 (mesmo arquivo, acima) já tinha eliminado pro resto do
catálogo ("Ids já existentes... foram reaproveitados quando o conceito é o
mesmo").

- **`ancestralidades.json`**: Goblins "PEQUENOS" passou de `id: "PEQUENOS"`
  pra `id: "TAMANHO_MENOS_1"` (mesmo id já usado por Pequeninos, Gnomos,
  Povo Ratazana, Povo Rato e Gnomo/Halfling do Pathfinder). `nome`
  continua `"PEQUENOS"` — é a skin de exibição, sem mudança.
- **`RacialTraitPointCatalog.kt`**: removidas as entradas `"PEQUENOS"` de
  `EFEITOS` (linha ~222) e `CUSTOS` (linha ~736), já mortas depois da
  migração — nenhuma outra raça/Variante/teste referenciava esse id
  (conferido em `app/src/main/assets`, `app/src/main/java` e
  `app/src/test`). Comentário de `TAMANHO_MENOS_1` atualizado pra citar
  Goblins entre as raças que usam o id.
- **Custo inalterado**: Goblins continua fechando em 2/2 pontos (-1
  Desagradável, +1 Infravisão, -1 Pequenos/Tamanho -1, +2 Sobrevivente/
  Adaptável, +1 Sorrateiro).
- Mesma limitação de sandbox da rodada anterior: sem acesso aos
  repositórios de plugin do Gradle aqui, não rodei o app/testes JVM —
  validação por leitura cruzada (`ancestralidades.json` → `EFEITOS`/
  `CUSTOS`/`LABEL`/`VEZES_MAX` de `RacialTraitPointCatalog.kt`).

## Nona rodada — Natureza Diabólica (Infernais) virando Bônus de Perícia de verdade

Pedido do dono do projeto: conferir "Natureza Diabólica" (Infernais). O
texto do livro é literal — "Adiciona +1 às rolagens de Intimidar" — e o
dono do projeto confirmou que esse +1 é só descritivo (não é um dado
subindo de tipo, é um bônus fixo numa rolagem, igual ao resto da família
"Bônus de Perícia"/"Penalidade em Perícia" já cadastrada no catálogo). O
traço tinha `id: "NATUREZA_DIABOLICA"` próprio, com custo 1 igual ao
genérico, mas duplicado.

- **Precedente já existente no próprio catálogo**: Usagimimi "Ariscos"
  (`RacialTraitPointCatalog.kt:493-496`) já faz exatamente isso — `nome:
  "Ariscos"` no JSON, `id: "PENALIDADE_PERICIA_2"` (o genérico, -2), com um
  comentário explícito documentando o padrão: "usa id=PENALIDADE_PERICIA_2,
  skin 'Ariscos' via `nome`". Segui o mesmo padrão, só que na família
  positiva: `id: "BONUS_PERICIA_1"` (que já existia no catálogo, cadastrado
  mas sem nenhuma raça oficial usando — comentário antigo dizia "nenhuma
  raça cadastrada usa isso hoje", agora desatualizado e corrigido).
- **`ancestralidades.json`**: Infernais "NATUREZA DIABÓLICA" passou de
  `id: "NATUREZA_DIABOLICA"` pra `id: "BONUS_PERICIA_1"`. `nome`/
  `descricao`/`descricaoLite` continuam os mesmos — a skin de exibição
  não muda, só o id mecânico por trás.
- **`RacialTraitPointCatalog.kt`**: removida a entrada `"NATUREZA_DIABOLICA"`
  de `CUSTOS` (agora morta — nenhuma outra raça/teste referenciava esse id,
  conferido em `app/src/main/assets`, `app/src/main/java` e `app/src/test`).
  Comentário de `BONUS_PERICIA_1` atualizado pra citar o uso real (Infernais)
  e apontar o precedente do Usagimimi.
- **Sem efeito mecânico calculado, por decisão confirmada**: nem
  `BONUS_PERICIA_1` nem `NATUREZA_DIABOLICA` (antes) tinham entrada em
  `EFEITOS` — o app não soma esse +1 em nenhum teste de Intimidar, só
  registra custo (1 pt) e mantém o texto explicando o que é. Mesma decisão
  já tomada pra `bonus_pericia_1`/`penalidade_pericia_1/2` na primeira
  rodada (RECLUSO/Anjo) e reconfirmada aqui.
- **Custo inalterado**: Infernais continua fechando em 2/2 pontos (+1
  Chifres, -1 Forasteiro Menor, -1 Fraqueza Ambiental, +1 Natureza
  Diabólica/Bônus de Perícia, +1 Resistência Ambiental, +1 Visão Total no
  Escuro — soma 2).
- Mesma limitação de sandbox: sem acesso aos repositórios de plugin do
  Gradle aqui, não rodei o app/testes JVM — validação por leitura cruzada
  do JSON contra `EFEITOS`/`CUSTOS`/`LABEL`/`VEZES_MAX`.

## Décima rodada — lote de achados testados pelo dono do projeto (2026-09-17/19)

O dono do projeto testou o app e anotou vários problemas. Cada um investigado e
corrigido nesta rodada (exceto os marcados como pendente de confirmação):

- **Tela "Ver detalhes" (`AncestralidadesSection.kt`) mostrava rótulo/pontos
  errados pra traço empilhável ou com skin via `targetRef`** — causa raiz:
  `RacialAbilityLite` (modelo "lite" usado só nessa tela) não carregava
  `traitId`/`targetRef`/`value`/`pontos`/`invisivel`/`vezes`, então toda
  habilidade chegava no `RacialCaracteristicasResolver` como se fosse 1
  compra sem targetRef, não importa o que o JSON dissesse. Dois sintomas
  relatados vinham daqui:
  - **Meio-Gigantes "Tamanho 1" nos detalhes** (correto no Resumo): Tamanho
    real é +3 (`TAMANHO_MAIS_1` × vezes=3), mas a tela mostrava a versão de
    1 compra.
  - **Povo Serpente com menos Movimentação que o real**: Movimentação real
    é +4 (`MOVIMENTACAO` × vezes=2, Movimentação 10), tela mostrava +2.
  - Corrigido adicionando os 6 campos que faltavam em `RacialAbilityLite` e
    propagando em todo ponto de conversão (`RacialModifierLite`/"Ver
    detalhes"/chamada do resolver). Também corrigido
    `RacialCaracteristicasResolver` pra usar `labelComVezes()` (já existia,
    só não era chamada aqui) em vez do `LABEL` fixo de 1 compra.
  - **Efeito colateral bom**: como esse mesmo caminho carrega `targetRef`
    agora, a tela "Ver detalhes" do Draconianos (rodada 7) também passa a
    mostrar a Complicação "Arrogante" de verdade em vez de "Mal-Humorado" —
    o JSON já estava certo, só essa tela achatava o dado antes de chegar no
    resolver.
- **Ogro "duplica Vigor com d8 e d6" nos detalhes**: achado — o id `ROBUSTO`
  no catálogo tinha `EFEITOS["ROBUSTO"] = AtributoStep("Vigor")` (Vigor +1
  passo), correto pro "Robusto" dos Anões (Fantasia) — que É um aumento de
  atributo (Vigor d6) — mas ERRADO pro "Robusto" dos Ogros, que é uma
  habilidade completamente diferente com o mesmo nome (confirmado no livro,
  `docs/swade_basico:1090`: "Robusto (1): Um segundo resultado Abalado não
  causa Ferimento" — sem nenhum aumento de atributo). Com o id
  compartilhado, Ogro ganhava DOIS `AtributoStep("Vigor")` (um de
  `MUITO_RESISTENTE`, +2 passos = d8; outro de `ROBUSTO`, +1 passo = d6) e
  a tela de características lista cada um numa linha própria — daí "Vigor
  d8 e Vigor d6" ao mesmo tempo. Corrigido: Anões (Fantasia) "Robusto"
  passou a usar `id: "RESISTENTE"` (mesmo id/efeito que o "Resistente" do
  Anão Básico — é literalmente a mesma habilidade, "Vigor d6, máximo
  d12+1", só com nome diferente por livro), liberando `ROBUSTO` pro
  conceito oficial de verdade (Ogros) — sem entrada em `EFEITOS` pra esse
  id agora (não é cálculo automático, custo continua 2). Nome de exibição
  "Robusto" não muda pra nenhuma das duas raças.
- **Povo Ratazana e Rakashanos — Garras registradas sem PA, livro diz que
  têm PA 2**: conferido em `docs/swade_fantasia` — Povo Ratazana (linha
  1365) e Rakashanos (linha 1448) têm a MESMA frase: "Suas garras causam
  For+d4 de dano, têm PA 2 e adicionam +2 às jogadas de Atletismo". O JSON
  das duas raças tinha essa cláusula de PA cortada da descrição e usava
  `id: "GARRAS_SEM_PA"` (custo 2) em vez de `id: "GARRAS"` (custo 3, base 2
  + 1 por PA 2 — regra oficial em `docs/swade_basico:1046`). Corrigido nas
  duas: `id`, `descricao`, `descricaoLite` e `armasNaturais[0].pa` (0→2).
  Como o custo de cada raça sobe de 2 pra 3, adicionei
  `pontosRaciaisEsperados: 3` explícito nas duas (antes usavam o default
  2, que ficaria errado pro orçamento do editor de Variante). Único outro
  livro/edição de Rakashanos com esse texto não conferido ainda (Básico/
  Horror/Sci-Fi/Super também usam `GARRAS_SEM_PA` — fica pendente, fora do
  escopo desta rodada que é só Fantasia).
- **Armadura comprada nunca somava na Resistência exibida** (relatado:
  "comprei um Corselete de Bronze, Resistência não mudou"): achado —
  `CriadorState.armadura` era um `var` manual (`mutableIntStateOf(0)`) que
  nenhum lugar do app nunca escrevia (só resetava pra 0 quando trocava de
  raça, via `forceArmorZero` — reset que já era um no-op, já que o valor já
  era sempre 0). `calcResistencia()` (usada tanto no Resumo quanto no PDF)
  lê esse campo como `armorBase`, então a Armadura de equipamento comprado
  nunca aparecia como bônus na Resistência total — só aparecia sozinha na
  lista separada "Armaduras". Corrigido: `armadura` virou uma propriedade
  computada, somando o campo `armadura` de cada item em
  `equipamentosComprados` (mesma leitura que a lista "Armaduras" já usa).
  Soma simples, sem modelar regra de empilhamento por local do corpo (o
  catálogo não guarda essa informação estruturada hoje — nenhuma raça/item
  testado tem mais de uma peça de armadura ao mesmo tempo neste momento).
  **Achado incidental, não mexido**: `forceArmorZero` (usado por ~30 raças,
  inclusive raças humanoides comuns via o `else` padrão do `when`) fica sem
  efeito nenhum agora — antes já não tinha efeito nenhum tampouco (resetava
  um campo que já era sempre 0), então não é uma regressão desta rodada,
  mas fica registrado: se o dono do projeto quiser mesmo impedir raças como
  Golens/Draconianos (que não podem vestir armadura humanoide) de ganhar
  esse bônus, precisa de um mecanismo novo — o antigo nunca funcionou.
- Não rodei o app/testes JVM nesta rodada (mesma limitação de sandbox das
  anteriores) — validação por leitura cruzada de código e conferência
  direta contra `docs/swade_basico`/`docs/swade_fantasia`.

## Décima primeira rodada — Armadura (regra real), Diminuto (Força Mínima + dano), Meio-Orc/Meio-Elfo

Resposta às dúvidas levantadas na rodada anterior, depois de mais contexto do
dono do projeto e conferência direta do livro Fantasia.

- **Armadura na Resistência não deve SOMAR peças, só pegar a melhor**:
  correção da rodada anterior (armadura comprada nunca somava na
  Resistência) estava certa na causa, mas a solução usava `sumOf` — errado
  pra regra oficial (peças no mesmo local do corpo não empilham). Corrigido
  pra pegar o maior valor entre as peças que cobrem Tronco/Corpo (lido do
  texto livre de `observacoes`, ex. "Tronco.") e, sem nenhuma peça de
  Tronco/Corpo, o maior valor entre as demais. Nunca soma duas peças.
  **Registrado, não implementado**: Resistência por local do corpo
  separado no PDF (braço/peitoral com valores próprios) — o catálogo não
  tem "local" como campo estruturado hoje (só texto livre, às vezes
  cobrindo vários locais na mesma peça, ex. "Tronco, braços."); vira
  estrutura de dado de verdade num pedido à parte.
- **Traço Diminuto (livro Fantasia, pág. 10) — duas regras que faltavam por
  completo**, confirmadas na íntegra em `docs/swade_fantasia` (linhas
  637-661): além do teto de Força por Tamanho (já implementado antes,
  `CriadorState.atributoMaxRaw`), o livro também manda:
  1. **"Reduza a Força Mínima de armaduras de Tamanho [X] em [2/3/4] tipos
     de dado (mínimo d4)"** — armadura feita sob medida pro corpo pequeno.
     Não existia nenhuma implementação. Adicionado
     `ForcaMinimaCalculator.minimoReduzidoPorDiminuto()`, usado tanto no
     `ModifierEngine` (penalidade de Movimentação) quanto no Resumo
     (penalidade/nota de Agilidade) antes de comparar com a Força do
     personagem.
  2. **"Subtraem [2/3/4] de... rolagens de dano (corpo a corpo, à
     distância, magia etc.)"** — penalidade FIXA em toda rolagem de dano,
     além do cap do dado da arma pela Força que já existia
     (`danoLimitadoPelaForca`). Também não existia. Adicionado
     `ForcaMinimaCalculator.danoComPenalidadeDiminuto()`, aplicado em
     ataques naturais, armas corpo a corpo e armas à distância no Resumo
     (ex.: Fada com Espada Longa For+d8 e Força d4 vira "For+d4-4").
  - Nova função `ModifierEngine.racialDiminutoPassos(state)` — lê direto
    `currentAncestryDef.habilidades` por um efeito `TamanhoBonus(minusculo
    = true)`, sem chamar `sizeRawDisplay()/collect()` (chamada de dentro
    do próprio `collect()`, na seção 1b — recursão infinita se usasse o
    caminho normal). Mesma função reutilizada no Resumo pra não divergir.
  - **Pendente, não coberto nesta rodada**: PDF (`ResumoPdfReferenciador.kt`)
    não tem NENHUMA das duas regras de Força Mínima (nem o cap antigo por
    Força, nem os dois novos de Diminuto) — ele lê `dano`/`forcaMin` direto
    do catálogo, sem passar por `ForcaMinimaCalculator`. Gap pré-existente,
    não introduzido nesta rodada; fica registrado pra outra rodada. Dano de
    Poderes/Magia (arcano) também não foi conferido — só armas/ataques
    naturais.
- **Meio-Orc (Endurecido) e Meio-Elfo (Herança) — não é confusão, é bug
  de verdade**: os dois têm texto de escolha ("d6 em Força OU Vigor";
  "Vantagem grátis OU d6 em Agilidade") e custo cadastrado (2 pts cada),
  mas **nenhum dos dois tem entrada em `EFEITOS`** — `efeitoDe("ENDURECIDO"
  ou "HERANCA", ...)` sempre devolve `RacialTraitEffect.Nenhum`. Ou seja,
  hoje essas duas raças NÃO recebem NENHUM benefício mecânico desses
  traços (nem o atributo sobe, nem a Vantagem grátis é concedida, nem
  existe escolha nenhuma na UI) — só gastam 2 pontos raciais à toa. O
  mecanismo "escolha de atributo/perícia" que já existe no app (picker de
  `SettingsDialog.kt`, usado hoje só pro construtor de raça customizada, e
  o sistema `traitId="ATTRIBUTE_BOOST"` + `targetRef` já cadastrado em
  `RacialAbility`/`RacialTraitPointCatalog.efeitoDe`) nunca foi ligado a
  essas duas raças oficiais. **Não corrigido nesta rodada** — precisa de
  UI nova (picker "Força ou Vigor?"/"Vantagem grátis ou Agilidade?" na
  tela de Ancestralidade, não só no construtor de raça customizada), fica
  pra próxima rodada, a combinar com o dono do projeto.
- Não rodei o app/testes JVM nesta rodada (mesma limitação de sandbox das
  anteriores).

**Itens da lista do dono do projeto ainda pendentes** (fora do escopo desta
rodada, Sci-Fi/UI — vão ficar pra rodadas seguintes): Mechas não adicionando
arma customizada de verdade ao PDF (hoje é só anotação de texto); Propulsores
sem custo de mods (precisa ler `docs/swade_scifi`); sugestão de UI "Wiseguys"
(habilitar troca de raça na criação de personagem desse livro, hoje sempre
Humano).

## Décima segunda rodada — Diminuto no PDF, Meio-Orc/Meio-Elfo por id de verdade, Wiseguys

Resposta direta às instruções da rodada anterior.

- **Diminuto no PDF**: as duas regras implementadas na rodada anterior só
  valiam pro Resumo dentro do app — o PDF (`ResumoPdfReferenciador.kt`) lê
  `dano`/`forcaMin` direto do catálogo, numa função separada
  (`buildWeaponAndArmorBlocks`) que nunca passava pelo `ForcaMinimaCalculator`
  (nem o cap de dado por Força do livro básico, que já era uma lacuna antes
  desta auditoria). Corrigido: `MeuPersonagem` ganhou o campo
  `passosDiminuto` (calculado uma vez em `toMeuPersonagem()`, já que o
  snapshot usado pelo PDF não carrega `habilidades[]` da raça pra recalcular
  do zero), e as 3 tabelas do PDF (Corpo a Corpo, à Distância, Armaduras)
  passaram a aplicar `danoLimitadoPelaForca`/`danoComPenalidadeDiminuto`/
  `minimoReduzidoPorDiminuto`, na mesma distinção que o Resumo já fazia
  (cap de dado pela Força só em Corpo a Corpo/Arremesso, nunca à Distância).
  **Dano de Poderes/Magia Arcana — avaliado, decisão do dono do projeto:
  não implementar**. Motivo técnico (fica registrado): diferente de
  equipamento, poderes (`poderes.json`) não têm um campo `dano` estruturado
  — "dano" ali é só uma TAG de categoria (`tags: [..., "dano"]`); o valor de
  dano de cada poder (ex.: "2d6") vive dentro do texto livre da própria
  descrição/modificador. Aplicar "-N" automaticamente exigiria processar
  texto solto por poder (arriscado, alto volume) em vez de um cálculo sobre
  campo estruturado. Confirmado com o dono do projeto que não vale a pena
  — não é mais pendência.
- **Meio-Orc/Meio-Elfo — não era confusão nem falta de implementação**: a
  rodada anterior errou ao dizer que os dois não tinham NENHUM efeito — só
  não tinham entrada em `RacialTraitPointCatalog.EFEITOS` (o catálogo
  genérico); existe, de fato, um segundo mecanismo próprio (picker de
  Força/Vigor/Agilidade em `AncestralidadesSection.kt`, ligado ao state
  `humanoMineradorAtributo`, lido dentro de `atributoBaseRacial()`) que já
  funciona hoje — o "sobe" que o dono do projeto lembrava de ter testado
  estava certo. O que ERA sujeira de verdade, como o dono do projeto
  suspeitou:
  1. `getAncestralidadeDef()` decidia trocar "Herança" por "Ágil"/"Adaptável"
     checando `key.contains("MEIO-ELFOS")` (nome da raça) em vez do traço
     "HERANCA" presente em `habilidades[]`. Corrigido pra checar só o id.
  2. As duas telas de escolha (picker "Endurecido" do Meio-Orc, radio
     "Herança" do Meio-Elfo) só apareciam quando `item.nome.keyify() ==
     "MEIO-ORCS"`/`"MEIO-ELFOS"` — também nome, não id. Corrigido pra
     `item.habilidades.any { it.id == "ENDURECIDO"/"HERANCA" }`.
  3. **Achado incidental, bug de verdade**: `humanoMineradorAtributo` é uma
     ÚNICA variável compartilhada pelas 3 raças com esse tipo de escolha
     (Meio-Orc, Feral, Humano Minerador Sci-Fi) — sem validação, uma escolha
     "Agilidade" feita num personagem Feral sobrevivia à troca pra Meio-Orc
     (que só tem Força/Vigor como opção) e cancelava o bônus racial por
     completo, silenciosamente (nem Força nem Vigor batiam contra
     "Agilidade"). Corrigido validando a escolha contra as opções válidas de
     cada raça antes de usar, caindo no padrão do livro quando inválida.
- **Wiseguys — checkbox "Habilitar Raças"**: adicionado na tela inicial
  (mesmo grupo de opções de "A Cosa Nostra"), controlando um novo campo
  `CriadorState.wiseguysHabilitaRacas`. Ligado, ele: (1) reabre a aba
  Ancestralidades (`UnifiedScreen.kt` — antes sempre escondida quando
  `compendioWiseguysAtivo`, mesma raiz do problema pro Deadlands, que não
  mexi por não ter sido pedido); (2) reintroduz o Livro Básico como origem
  de raça ativa (`getActiveOrigins()` em `ContentVisibility.kt` — Wiseguys é
  tratado como "cenário substituto" que normalmente exclui o Básico, só
  raça `HUMANOS` tem `livros: ["WISEGUYS"]` no catálogo). O checkbox
  "Variantes de Raça" (já existia, mas ficava visível sem ter nada pra
  mostrar) agora fica desabilitado (cinza, sem clique) enquanto Wiseguys
  estiver ativo e esta opção não estiver marcada — `SimpleCheckRow` ganhou
  um parâmetro `enabled` novo pra isso.
- Não rodei o app/testes JVM nesta rodada (mesma limitação de sandbox das
  anteriores) — validação por leitura cruzada de código.

**Ainda pendente, explicitamente adiado pelo dono do projeto pra depois**:
Mechas com arma customizada de mentirinha no PDF; Propulsores sem custo de
mods.

## Décima terceira rodada — `local` estruturado no catálogo de armadura

Pedido do dono do projeto: implementar o identificador de local do corpo
(Cabeça/Braços/Pernas/Tronco) como dado estruturado no catálogo — base pra
Resistência por local aparecer no PDF depois. Confirmado também: dano de
Poderes/Magia Arcana fica de fora por decisão dele (rodada anterior).

- **`equipamentos.json`**: novo campo `"local": [...]` em toda peça de
  armadura oficial (297 de 330 entradas — as 33 restantes são casos que não
  são armadura corporal de verdade: armadura de montaria/"Barda", Escudo
  Balístico, o add-on "Espinhos", "Manopla Travada" e os 3 chassis de Mecha
  "Estrutura: Tam N", nenhum deles com local de corpo pra marcar). Valores
  possíveis: `CABECA`, `TRONCO`, `BRACOS`, `PERNAS` (uma peça pode cobrir
  mais de um — ex.: "Manto": Tronco+Braços+Pernas) e `CORPO_INTEIRO` (trajes
  completos/armadura energizada — Traje Espacial, Estruturas Classe I/II
  etc., conta pros 4 locais ao mesmo tempo).
  - Inferido por script a partir do texto que já existia (nome entre
    parênteses quando presente, ex. "Braçadeira lamelar (Braços)" — tem
    prioridade por ser específico da peça; senão o texto de `observacoes`,
    ex. "Tronco, braços."). Cuidado real durante a extração: pra itens em
    "conjunto" (Armadura Espelhada/Lamelar/Superior — 4 peças com a MESMA
    descrição do conjunto completo repetida em cada uma, "cabeça, tronco,
    braços e pernas"), ler só `observacoes` cravava as 4 peças com os 4
    locais ao mesmo tempo; e pras peças do "Cavaleiro Infernal", a frase de
    referência cruzada ("veja Placa de Peito...") contaminava o local de
    peças que não são tronco. Os dois casos corrigidos dando prioridade ao
    nome entre parênteses da própria peça sobre o texto do conjunto/nota.
  - Cada entrada revisada individualmente antes de gravar (lista completa
    impressa e conferida) — não foi um "aplica e reza".
- **`EquipamentoModels.kt`**: `EquipamentoItem` ganhou o campo `local:
  List<String>? = null`.
- **`CriadorState.kt`**: nova função `armaduraPorLocal(): Map<String, Int>`
  — maior valor de Armadura por local (peças no mesmo local não empilham,
  `CORPO_INTEIRO` conta pros 4), lendo o campo estruturado novo. `armadura`
  (o valor único que já soma na Resistência da ficha desde a rodada
  anterior) passou a usar essa função: Tronco por convenção, com fallback
  pra melhor peça quando não há cobertura de Tronco, e um último fallback
  por heurística de texto só pra equipamento customizado do Mestre (sem
  `local`, o catálogo oficial já está 100% migrado). `LOCAIS_CORPO` virou
  constante top-level do arquivo (`CriadorState` já tinha um `companion
  object` — evitei duplicar).
  **Registrado, não implementado**: a UI/PDF de Resistência por local
  (Cabeça/Braços/Pernas/Tronco cada um com seu próprio valor) ainda não
  existe — só o dado (`armaduraPorLocal()`) está pronto pra ela consumir
  quando o dono do projeto quiser essa tela/seção de verdade.
- Não rodei o app/testes JVM nesta rodada — validação por leitura cruzada
  de código e conferência da lista completa das 144 peças únicas antes de
  gravar no catálogo.

**Ainda pendente**: Mechas com arma customizada de mentirinha no PDF;
Propulsores sem custo de mods.

## Décima quarta rodada — "vestir armadura sobre armadura"

Pedido do dono do projeto: as peças que cobrem mais de um local (ex.:
"Manto": Tronco+Braços+Pernas) já ocupam esses locais — confirmado, é
exatamente o que `armaduraPorLocal()` da rodada anterior já fazia. O pedido
de verdade era conferir a regra de empilhar duas peças no MESMO local, que
até agora só pegava a melhor e descartava a outra.

- **Regra oficial, confirmada em `docs/swade_basico` (Cap. 2 "Equipamento",
  seção "Armadura")**: "Armadura vestida também se soma com outra camada. A
  armadura mais leve adiciona metade do seu valor (arredondado para baixo)
  ao total e aumenta em um tipo de dado a penalidade por Força Mínima da
  armadura mais pesada." — exemplo do próprio livro: cota de malha (+3) por
  baixo de armadura de placas (+4) vira +5 total (4 + metade de 3,
  arredondado pra baixo) e a Força Mínima sobe um passo de dado (ex.: d10
  vira d12).
- **`ForcaMinimaCalculator.minimoComCamadaExtra()`**: novo, sobe a Força
  Mínima recebida em um passo de dado (reaproveita `paraRaw`/`passo`/
  `stepParaRaw` já existentes).
- **`CriadorState.armaduraPorLocal()`**: agora retorna `Map<String,
  ArmorLocalInfo>` (`valor` + `forcaMinima`) em vez de só `Map<String,
  Int>`. Com 2+ peças no mesmo local, ordena por valor de Armadura
  (peça de maior valor = "principal"/mais pesada), soma valor cheio da
  principal + metade arredondada pra baixo da segunda, e aplica
  `minimoComCamadaExtra` na Força Mínima da principal. Com 3+ peças no
  mesmo local (sem regra explícita no livro pra isso), só as 2 melhores
  contam. `armadura` (Resistência da ficha) não mudou de comportamento —
  continua pegando o valor de Tronco já resolvido.
- **`ModifierEngine` (penalidade de Movimentação por Força Mínima)**: antes
  somava a penalidade de CADA peça independente — com a regra de camada,
  isso contava a penalidade da cota de malha E da armadura de placas
  separadas, errado. Agora itera por LOCAL (usando a Força Mínima efetiva
  já ajustada por `armaduraPorLocal()`), uma penalidade por local, não por
  peça. Equipamento customizado sem `local` (Mestre) continua por peça,
  já que não dá pra saber com o que ele empilha.
- **`ResumoSection.kt`**: mesma correção pro total exibido, mais uma linha
  nova "TRONCO (camadas): Armadura +5 • Força Mínima efetiva d12" quando um
  local tem 2+ peças — sem isso, a soma dos valores individuais mostrados
  em cada peça não bateria com a Resistência real da ficha. Nota de "Força
  abaixo da Força Mínima desta peça" por item removida pra peças com
  `local` (a informação mora na linha de local agora, atribuí-la a "esta
  peça" especificamente ficaria ambíguo/errado com duas peças empilhadas).
- **PDF**: `MeuPersonagem` ganhou `armaduraForcaMinimaPorLocal` (mapa
  pré-calculado, mesmo motivo do `passosDiminuto`) — a coluna Força Mínima
  da tabela de Armaduras agora mostra o valor efetivo por local pra peças
  com `local`, em vez do valor cru da peça isolada.
- Não rodei o app/testes JVM nesta rodada — validação por leitura cruzada
  de código e conferência da regra contra o texto exato do livro.

**Ainda pendente**: exibição de Resistência por local (não só a Força
Mínima) no PDF em tabela própria — o dado (`armaduraPorLocal()`) já cobre
isso, só falta a tela/seção.

## Décima quinta rodada — arma customizada de Mecha e Propulsores

Pedido do dono do projeto: (1) reconsiderar como funciona a criação de arma
customizada na tela de Mecha (era só um campo de texto livre que virava
"arma" de graça, sem nenhum custo em MODs — testado digitando "teste" e o
app aceitou); (2) verificar a regra de Propulsores no livro de Sci-Fi (se
ocupa slot de Mod ou não — no app era só um checkbox sem custo).

**1) Arma customizada de Mecha**

- Investigado o fluxo: `MechasSection.kt` tinha um `OutlinedTextField`
  ("Arma Personalizada") que jogava a string digitada direto em
  `mecha.armas_equipadas` (`List<String>`). O custo em MODs vem de
  `matchWeaponFromCatalog()` casando esse texto contra o catálogo oficial
  (`scifi_mecha_weapons.json`, 18 armas com `mods_cost` correto); texto que
  não casa com nada retorna `null` → soma 0 MODs. Ou seja, qualquer string
  virava uma arma sem custo.
- Considerei reusar o criador de Equipamento Customizado genérico
  (`EquipamentoCreatorForm.kt`, usado em Configurações → Conteúdo
  Customizado) — mas é estruturalmente incompatível: aquele formulário
  produz um `EquipamentoItem` de escala de PERSONAGEM (dano "For+dX", custo
  em $ do bolso do jogador, sem campo de Mods), enquanto armas de Mecha são
  de escala de VEÍCULO (dano em dados grandes tipo "6d6", custo em Mods do
  chassi, preço só de referência). Conferido também que `equipamentos.json`
  não tem nenhum item ligado a Mecha (busca por "mecha" no catálogo não
  retornou nada) — os dois sistemas de equipamento (pessoal vs. Mecha)
  sempre foram completamente separados, então forçar a integração
  misturaria as duas escalas.
- **Decisão**: um diálogo de criação dedicado (`CreateCustomMechaWeaponDialog`,
  no mesmo padrão do `CreateCustomMechaDialog` que já existe pra criar
  Mecha do zero), que produz um `MechaWeaponItem` de verdade com Mods cost
  OBRIGATÓRIO (campo numérico, não opcional) — em vez de aceitar texto
  livre sem custo. Esse item fica guardado só naquele Mecha
  (`MechaItem.armasCustomizadas: List<MechaWeaponItem>`, novo campo — não
  polui o catálogo global) e entra na mesma conta de MODs que as armas
  oficiais (`weaponCatalog + mecha.armasCustomizadas` na hora de casar por
  nome em `matchWeaponFromCatalog`).
- Removido o campo de texto livre "Arma Personalizada". A tela agora tem
  dois botões lado a lado: "Catálogo de Armas" (oficial, como já era) e
  "Criar Arma" (abre o novo diálogo). Os chips de arma equipada passaram a
  mostrar o custo em MODs entre colchetes (ex.: "Lâmina de Braço (For+d12,
  PA 6) [2 MODs]"), e remover o chip também descarta a definição
  customizada quando é a última cópia equipada dela (senão duplicatas
  perderiam o Mods cost umas das outras).

**2) Propulsores**

- Confirmado no `docs/swade_scifi` (seção Mechas, MODS de LOCOMOÇÃO,
  ~linha 12589): "Z Propulsores: Jatos e propulsores de manobra permitem
  que o mecha voe com Classificação de Velocidade 8 (150 km/h)... Metade
  do Tam. (1) $25K × Tam." — ou seja, É um Mod real, com custo em MODs que
  escala com o Tamanho do chassi (metade, arredondado pra cima), igual a
  "Propulsores para Salto" (Mod separado, próprio slot de pernas/salto, já
  cadastrado também).
- **Achado**: os dois já existiam corretos no catálogo de Mods
  (`scifi_mecha_mods.json`: `mod_loc_propulsores_voo` e
  `mod_loc_propulsores_salto`, ambos com `escala_tamanho_divisor = 2` e
  `mods_cost` batendo com o livro) e já eram instaláveis pelo diálogo
  "Modificadores & Qualidades" normal, com custo contabilizado
  corretamente. O bug era só o toggle separado e redundante
  (`MechaCustomizacoes.propulsores: Boolean`, na seção "Customizações
  Rápidas") que ligava/desligava "Propulsores" de graça, sem nenhuma
  relação com o Mod de verdade — duplicava (errado, sem custo) o que o Mod
  já fazia certo.
- **Fix**: removido o campo `propulsores` de `MechaCustomizacoes` e o
  toggle correspondente na tela (junto com o composable `CircleToggle`,
  que só era usado ali). Removidas as duas linhas que citavam
  "Propulsores instalados"/"Propulsores" no resumo em texto
  (`SummaryUtils.kt`) e no bloco de PDF do Mecha
  (`ResumoPdfReferenciador.kt`) — informação que já aparece via
  `mods_instalados` (que agora inclui o Mod real quando comprado). Como
  todo o Json de personagem usa `ignoreUnknownKeys = true`
  (`CharacterStorage.kt`), fichas salvas antes desse fix com
  `"propulsores": true` continuam carregando normalmente, só ignoram o
  campo morto.
- Não rodei o app/testes JVM nesta rodada (o ambiente sandbox não tem
  acesso de rede ao repositório de plugins do Gradle/Android, então
  `./gradlew` não resolve nem localmente nem offline) — validação por
  leitura cruzada do código (todos os pontos que citavam `.propulsores` e
  `weaponInput` foram localizados via grep e conferidos um a um) e
  conferência da regra contra o texto exato do livro.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria (mesma pendência da rodada anterior).

## Décima sexta rodada — aviso de "Ancestralidade desbalanceada"

Pedido do dono do projeto: quando a contagem de pontos raciais de uma raça
passa do orçamento normal (2 pontos), mostrar um indicador de "raça forte".
Ressalva do próprio pedido: Pathfinder e Arte da Guerra têm raças
naturalmente mais fortes por design do livro (orçamento 4 e 3, não 2), então
a comparação não pode ser contra 2 fixo — tem que ser contra o orçamento
PRÓPRIO de cada raça. Conclusão do dono do projeto: um indicador sutil nas
Características da raça, tipo "Ancestralidade desbalanceada", só pra avisar
o jogador que a contagem não fecha certo.

- **Conferido antes de mexer em código**: `RacialModifier.pontosRaciaisEsperados`
  já existe (rodadas anteriores) e já está corretamente calibrado por raça —
  as 7 raças de Pathfinder têm `4`, as 12 de Arte da Guerra têm `3`, as 5 de
  Crystal Heart têm `4` (design do próprio livro, "+4 pontos em vez do +2
  habitual" — comentário já existente em `RacialModifier.kt`), Centauros/
  Povo Ratazana/Rakashanos (Fantasia) têm `3`/`4` de rodadas anteriores desta
  auditoria. Ou seja, a parte "bota o contador certo" já estava pronta —
  faltava só USAR esse valor pra comparar contra o total real de pontos da
  raça e mostrar algo na tela.
- **`RacialCaracteristicasResolver`/aba Ancestralidades → "Ver detalhes"**
  (`AncestralidadesSection.kt`): a lista "Características:" já resolvia os
  traços efetivos da raça (`habilidadesEfetivas`, já considerando uma
  eventual Variante custom selecionada) só que sem somar os pontos. Agora
  soma `RacialAbility.resolvedPontos()` de cada traço (mesma fórmula que
  `ResolveVariantPointBudgetUseCase` e a aba já usam em outro lugar) e
  compara com o orçamento da própria raça. Quando o total ULTRAPASSA o
  orçamento, mostra uma linha discreta (cor `tertiary`, `labelSmall`, com
  ⚠) logo depois da lista de Características: "Ancestralidade desbalanceada
  (traços somam N pontos raciais, acima do orçamento de M)". Como a
  comparação usa o orçamento da própria raça (2/3/4 conforme o caso), uma
  raça de Pathfinder fechando nos 4 pontos dela não aciona o aviso — só
  aciona se de fato passar do que essa raça específica prevê, cobrindo
  tanto um bug de raça oficial desbalanceada quanto uma Variante custom que
  o Mestre montou torta.
- `RacialModifierLite` (o tipo "leve" usado só nessa tela, sem o resto dos
  campos de `RacialModifier`) ganhou o campo `pontosRaciaisEsperados`
  (default 2, igual ao original), preenchido a partir do `RacialModifier`
  de origem no único ponto que constrói essa lista.
- Não tentei resolver o cenário hipotético de "jogador reatribui a
  ancestralidade de Pathfinder pro livro Básico" citado na conversa — não
  existe hoje nenhum fluxo de UI pra reatribuir o livro/origem de uma raça
  OFICIAL (isso só existe pra conteúdo Customizado, que é outra tela). O
  aviso implementado já cobre o caso real de qualquer desequilíbrio de
  pontos que apareça, incluindo se um dia esse cenário virar possível.
- Não rodei o app/testes JVM nesta rodada (mesma limitação de rede do
  ambiente sandbox das rodadas anteriores) — validação por leitura cruzada
  do código e conferência de que os 7+12+5+3 casos com
  `pontosRaciaisEsperados` != 2 já cadastrados no catálogo continuam
  corretos (não precisaram de nenhum ajuste nesta rodada).

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria (mesma pendência das rodadas anteriores).

## Décima sétima rodada — Variante de raça baseada em raça de outro livro

Pergunta do dono do projeto, revisitando o cenário da rodada anterior: e se
o jogador pega o Elfo do Pathfinder e cria uma Variante dele, mas na hora
de escolher o(s) livro(s) pra salvar essa Variante marca só "Básico" (não
marca Pathfinder)? Ele suspeitava que isso pudesse gerar confusão — a
Variante "vazar" e ser tratada como se fosse do Elfo do Básico — mas também
já desconfiava que talvez fossem "ideias completamente diferentes" e por
isso seguro.

- **Conferido em `CustomAncestryVariant.kt`/`SettingsDialog.kt`**: a
  suspeita do dono do projeto está certa. `CustomAncestryVariant` guarda
  `ancestralidadeId` (a raça-base MECÂNICA, de onde vêm os traços
  removíveis e o orçamento de pontos) separado das `tags` (só controla em
  quais livros essa Variante fica arquivada/visível). Na aplicação
  (`CriadorState.applyCustomAncestryVariantIfSelected`), já existe uma
  trava (`if (variant.ancestralidadeId != base.nome.keyify()) return base`)
  que impede uma Variante vazar pra raça errada — impossível a Variante do
  Elfo do Pathfinder ser tratada como Variante do Elfo do Básico, porque
  `nome` de um é "Elfo" e do outro é "ELFOS" (chaves diferentes).
- **Risco real encontrado (efeito colateral, não o que foi perguntado)**:
  nesse cenário (Variante baseada em raça de um livro, tags marcadas só
  pra outro), a Variante fica ÓRFÃ — salva, mas nunca aparece selecionável,
  porque ela só é oferecida junto da raça-base quando essa raça-base está
  sendo exibida na aba Ancestralidades (`AncestralidadesSection.kt`:
  `listaVariantesRaciaisCustom.filter { it.ancestralidadeId ==
  item.nome.keyify() }`), e isso exige o livro DA RAÇA-BASE ativo (aqui,
  Pathfinder), não o(s) livro(s) marcado(s) nas tags. Não implementado
  (não pedido nesta rodada) — decisão do dono do projeto foi só corrigir o
  item abaixo.
- **Investigação inicial equivocada, corrigida antes de mexer em código**:
  cheguei a reportar um suposto bug de "o seletor de Raça Base esconde
  raças duplicadas do mesmo nome entre livros diferentes, pegando uma
  arbitrariamente". Falso — `state.listaAncestralidadesJson`
  (`DataLoader.kt`) já chega deduplicada por nome ANTES do seletor, via
  `distinctByOriginPriority` (mesmo mecanismo usado no resto do app pra
  decidir qual versão prevalece quando dois livros ativos compartilham
  nome — livro de cenário/companheiro vence o Básico). Ou seja, nunca
  existem duas entradas com o mesmo nome pra esconder; o `distinctBy`
  redundante do seletor nunca tem o que fazer.
- **Fix aplicado** (o que sobrou de real depois da correção acima): o
  seletor "Raça Base da Variante" mostrava só o nome cru ("Elfos"), sem
  dizer de qual livro veio a versão que o `distinctByOriginPriority`
  escolheu — o Mestre montava a Variante sem saber se estava trabalhando
  em cima do Elfo do Horror, da Fantasia ou do Básico (dependendo de quais
  livros estão ativos ao mesmo tempo). Adicionado
  `RacialModifier.nomeComLivro()` ("Elfos (Fantasia)", "Elfo
  (Pathfinder)") e usado tanto nas linhas do seletor quanto no botão/rótulo
  que mostra a raça-base já escolhida.
- Não rodei o app/testes JVM nesta rodada (mesma limitação de rede do
  ambiente sandbox) — validação por leitura cruzada do código, seguindo a
  cadeia completa `ancestralidades.json` → `DataLoader.kt`
  (`distinctByOriginPriority`) → `state.listaAncestralidadesJson` →
  seletor, pra confirmar que a dedupe já acontecia antes do seletor.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria; Variante de raça-base de livro diferente das tags fica órfã (ver
acima — não corrigido, não pedido nesta rodada).

## Décima oitava rodada — CI quebrado + primeiros testes automatizados

Pedido do dono do projeto: (1) corrigir o CI, que ficou vermelho depois do
commit da rodada anterior; (2) implementar os testes automatizados
discutidos na conversa anterior sobre "testes exageradamente perfeitos"
(varredura de invariantes sobre o catálogo inteiro, em vez de só cenários
roteirizados um por um).

**1) CI quebrado**

O job `Android CI` falhava em `./scripts/phase6_reliability_gate.sh`, no
check "Uso direto de DataLoader fora do repositório" — esse gate faz um
grep de texto por `DataLoader.` em todo `app/src/main/java`, fora de
`GameDataRepository.kt` (regra de arquitetura: todo acesso a `DataLoader`
tem que passar pelo repositório). O comentário que eu adicionei na rodada
anterior em `SettingsDialog.kt` citava `DataLoader.kt` como referência de
onde vem `distinctByOriginPriority` — e essa citação batia no mesmo grep
ingênuo (é busca de texto, não AST), um falso positivo causado por mim
mesmo. Corrigido reescrevendo o comentário pra não conter mais o texto
`DataLoader.` literal. Rodei `scripts/phase6_reliability_gate.sh`
localmente (esse script não depende de Gradle/rede) pra confirmar que
passa antes de subir.

**2) Testes automatizados — varredura de catálogo**

Sem acesso de rede neste ambiente sandbox pra rodar `./gradlew test` (Fase
não resolve o plugin do Android nem em cache), então pra não repetir o
erro de subir código não verificado, montei um compilador/executor Kotlin
standalone só com jars já em disco (`kotlin-compiler-embeddable` +
`kotlin-stdlib` + `kotlinx-serialization` + `junit`/`hamcrest`, todos já
cacheados dentro da própria distribuição do Gradle em `/opt/gradle-*`) —
consegui compilar e RODAR de verdade os arquivos de teste novos contra o
catálogo real antes de commitar, incluindo com JUnit de verdade (não só
leitura de código).

- **`AncestralidadeCatalogBudgetTest.kt`** (novo): a varredura de
  invariante proposta na conversa anterior. Lê `ancestralidades.json`
  direto do disco (sem passar por `DataLoader`/`Context` — não dá pra usar
  em JVM pura mesmo, e o gate de confiabilidade não deixaria de qualquer
  jeito) e, pra cada uma das 112 raças, soma o custo de cada traço
  (`RacialTraitPointCatalog.custoDe(traitId ?: id, value, severity,
  pontos) * vezes` — a mesma fórmula exata de `RacialAbility
  .resolvedPontos()`) e confere que bate com `pontosRaciaisEsperados` da
  própria raça. Rodando pela primeira vez contra o catálogo real, achou **7
  raças fora do orçamento** — validando a ideia na prática, não só na
  teoria.
- **Investigação dos 7 achados**: 6 (Ferais, Florans, Gelatinoides,
  Insetoides e Mímicos do Sci-Fi, Umvee do Arte da Guerra) são falsos
  positivos de um teste que só olha o catálogo estático — essas raças
  têm traços injetados em tempo de execução por `AncestryVariantRegistry`
  (ex.: Ferais só ganham Diminuto/Tamanho -3 pela Variante "padrão" ativa
  por default, não pelo `habilidades[]` cru do JSON — confirmado num
  comentário já existente em `RacialTraitPointCatalog.kt` sobre
  `DIMINUTO_TAMANHO_3`). Documentado e excluído explicitamente no teste
  (por par raça+livro, não a raça inteira — a versão Fantasia de Insetoides,
  por exemplo, continua sendo verificada normalmente).
- **1 bug real encontrado e corrigido**: Demônios (Cidade do Sol a Vapor)
  somava 0 pontos (Antecedente Arcano grátis +2, Frágil ×2 = -2) mas
  `pontosRaciaisEsperados` não estava definido no JSON (caía no default
  2). Conferido contra `docs/swade_csv_livro_do_criador` (seção
  "Demônios", p. 42-43): o livro descreve exatamente esses dois traços e
  mais nenhum — os dados já estavam certos, só faltava registrar que essa
  raça fecha em 0, não 2. Adicionado `"pontosRaciaisEsperados": 0` no
  JSON (diff mínimo, só essa raça).
- **`AncestralidadeCatalogRegressionTest.kt`** (novo): 7 testes, um por
  bug específico já corrigido nas rodadas anteriores desta auditoria
  (Draconianos Mal-Humorado→Arrogante por id, Goblins Pequenos→
  TAMANHO_MENOS_1, Infernais Natureza Diabólica→BONUS_PERICIA_1, Anões
  Fantasia Robusto→RESISTENTE, Garras PA 2 de Povo Ratazana/Rakashanos
  *só* na versão Fantasia — as outras versões de Rakashanos mantêm PA 0,
  conferido explicitamente — e Povo Ratazana Sucateiro em maiúsculas).
  Trava esses fatos específicos no catálogo real pra nenhum voltar a
  quebrar silenciosamente.
- Os dois arquivos foram compilados E executados (não só lidos) contra o
  `ancestralidades.json` real desta branch, com JUnit de verdade, batendo
  100% verde antes de commitar — ver metodologia acima.
- **Não implementado nesta rodada** (deliberado, não esquecido): testes
  no nível de `CriadorState` (o "abre o app, escolhe a raça, distribui
  atributos" descrito na conversa anterior). `CriadorState.kt` tem ~7900
  linhas e depende de Compose runtime — grande demais pra montar um
  classpath mínimo e compilar isolado como fiz com
  `RacialTraitPointCatalog.kt`, e eu não tenho como verificar se esse
  código compila/passa neste ambiente sem rede pro Gradle. Prefiro não
  entregar teste não verificado (foi exatamente isso que quebrou o CI
  nesta mesma rodada) a arriscar outro round de correção. Fica como
  próximo passo natural, num ambiente com CI disponível pra iterar.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria; Variante de raça-base de livro diferente das tags fica órfã.

## Décima nona rodada — testes de CriadorState (o que ficou pendente na anterior)

Pedido do dono do projeto: implementar também os testes no nível de
`CriadorState` que ficaram de fora da rodada anterior por eu não conseguir
verificar compilação neste ambiente sandbox sem rede pro Gradle.

- **Resolvido o bloqueio técnico**: estendi o harness standalone (kotlinc +
  jars já em disco) da rodada anterior pra compilar `CriadorState.kt` de
  verdade, com todo o fechamento transitivo de dependências dele — 79
  arquivos reais do projeto (`ModifierEngine`, `AncestryVariantRegistry`,
  ~25 Use Cases de `model/usecase/`, etc.), ~18 mil linhas. Faltavam duas
  peças que não existem em disco neste sandbox: o artefato binário do
  `androidx.compose.runtime` (só havia metadados do catálogo de versões do
  Gradle, não o jar) e o plugin de COMPILADOR do kotlinx.serialization
  (só os jars de runtime, que não geram `.serializer()` em classes
  `@Serializable`). Contornado com: (1) um shim mínimo de
  `androidx.compose.runtime`/`.snapshots` (mutableStateOf/
  mutableStateListOf/mutableStateMapOf/mutableIntStateOf/derivedStateOf,
  com a mesma assinatura pública, só sem o sistema de snapshot/recomposição
  de verdade — irrelevante pra teste sem UI) e stubs pontuais pras poucas
  peças puramente de Android/infra que `CriadorState.kt` referencia sem
  usar de fato nos testes (`Context`/`SystemClock`/`Log`, `BuildConfig`,
  `DataLoader` só como referência de tipo, `AppPreferences` só pelo enum
  `ModoSelecaoPericia`, a constante `TAG_GERAL`); (2) em só um arquivo
  (`Requisito.kt`, numa cópia local, nunca no repositório real), troquei o
  corpo de um `KSerializer` customizado que dependia de `.serializer()`
  gerado por um `data class` interno por um stub que lança exceção — nunca
  chamado pelos testes, que só constroem `Requisito` pelo construtor normal.
- **Validação de que o harness não é vazio por engano**: rodei um
  "controle negativo" — troquei de propósito um valor esperado nos testes
  novos pra um valor errado, compilei e rodei, e o teste falhou como
  esperado (`expected:<7> but was:<6>`), confirmando que a checagem
  realmente executa a lógica de verdade e não passa à toa.
- **`CriadorStateFullFlowTest.kt`** (novo, 3 testes, todos rodados de
  verdade com JUnit antes de commitar):
  1. Fluxo sintético completo — o cenário descrito na conversa anterior:
     raça neutra (Humanos, sem regra de livro extra ligada), distribuição
     de pontos de atributo, compra de Vantagem e Complicação, tudo numa
     sequência só — confere que nada foi silenciosamente perdido/ignorado
     entre uma etapa e outra.
  2. Elfos do Básico **de verdade** (lido de `ancestralidades.json`, não
     fixture inventada) rodando pela resolução real de `CriadorState`
     (`getAncestralidadeDef`/`atributoBaseRacial`/`ModifierEngine`, não só
     soma de pontos como em `AncestralidadeCatalogBudgetTest`): confere
     que `atributoMinRaw("Agilidade")` fecha em 6 (traço Ágil, id=AGIL).
     Esse teste pega uma classe de bug que o teste de soma de pontos NÃO
     pegaria: um id que soma o valor certo mas está mapeado pro EFEITO
     errado em `RacialTraitPointCatalog.EFEITOS` (ex.: concede Astúcia
     em vez de Agilidade, mesmo custo, soma bate, mas o jogo aplica o
     bônus no atributo errado).
  3. Humanos do Básico de verdade: confere `temAdaptavel() == true` (traço
     Adaptável, id=ADAPTAVEL) pela mesma resolução real.
- Não expandi pra mais raças/livros nesta rodada (o harness agora permite,
  mas cada teste novo pede pensar no cenário certo) — os 3 testes cobrem o
  que a conversa anterior pediu como exemplo; dá pra crescer a lista depois
  seguindo o mesmo padrão.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria; Variante de raça-base de livro diferente das tags fica órfã.

## Vigésima rodada — critério certo pro aviso de "Ancestralidade desbalanceada" + desconto de equipamento do traço Diminuto

Duas coisas relatadas pelo dono do projeto ao testar com Centauros: (1) o
selo "Ancestralidade desbalanceada" (Décima sexta rodada) não estava
aparecendo em "Ver Detalhes" da aba Ancestralidades mesmo pra uma raça que
ele esperava ver marcada; (2) faltava a redução de peso/custo de
equipamento do traço Diminuto — "as ancestralidades menores têm redução de
custo e peso para equipamentos... se ela for pequena, menos 2, muito
pequena, menos 3, ou minúscula, menos 4".

### Parte 1 — critério do aviso redefinido

Perguntei o que ele esperava ver e a resposta mudou o critério: o selo não
deve mais comparar a SOMA dos traços da raça contra o orçamento DELA MESMA
(isso já é garantido automaticamente em CI por `AncestralidadeCatalogBudgetTest`,
não precisa de aviso na UI pra isso) — deve acender sempre que o
ORÇAMENTO da raça (`pontosRaciaisEsperados`) for diferente dos 2 pontos
padrão do livro (`ResolveVariantPointBudgetUseCase.DEFAULT_ORCAMENTO`),
pra cima ou pra baixo, sem julgar se isso deixa a raça "forte" ou "fraca"
("Forte e fraco pode ofender as pessoas"). Também pediu pra preferir
"Ancestralidade" a "raça" no texto (já era o termo usado no nome do selo,
mantido).

- `AncestralidadesSection.kt` ("Ver Detalhes"): trocada a condição de
  `pontosRaciaisTotais > item.pontosRaciaisEsperados` (soma dos traços vs.
  orçamento da própria raça) para
  `item.pontosRaciaisEsperados != ResolveVariantPointBudgetUseCase.DEFAULT_ORCAMENTO`
  (orçamento da raça vs. padrão do livro). Centauros (`pontosRaciaisEsperados = 4`,
  ver rodadas anteriores) agora aciona o aviso; Humanos (`2`, o padrão) não.
  A soma de pontos (`pontosRaciaisTotais`) foi removida do trecho por não
  ser mais usada aqui.

### Parte 2 — desconto de equipamento do traço Diminuto

Conferido no livro Fantasia (pág. 10, "Diminuto"): "Equipamentos feitos
para personagens Pequenas/Muito Pequenas/Minúsculas pesam e custam
metade/um quarto/um décimo do valor listado" — 3 tiers (Tamanho -2/-3/-4),
o mesmo limiar que `ForcaMinimaCalculator.diminutoPassos()`/
`ModifierEngine.racialDiminutoPassos()` já usavam pra Força Mínima e
penalidade de dano (Décima primeira/Décima segunda rodadas), agora
estendido pro custo/peso de equipamento comum.

- **Achado incidental ao mexer nisso**: `RacialTraitPointCatalog.EFEITOS`
  tinha `DIMINUTO_TAMANHO_3`/`_4` (Muito Pequeno/Minúsculo) mas faltava
  `DIMINUTO_TAMANHO_2` (Pequeno) — só existia em `CUSTOS`/`LABEL`. Nenhuma
  raça oficial usa esse tier ainda, mas sem essa entrada uma Variante/raça
  customizada que usasse Pequeno não ganharia Tamanho -2 nem contaria como
  Diminuto pra nenhum dos benefícios (Força Mínima, dano, agora também
  equipamento). Adicionado.
- **`ForcaMinimaCalculator.kt`**: duas funções novas, só matemática pura —
  `divisorEquipamentoDiminuto(passosReducao)` (2.0/4.0/10.0 pros tiers
  2/3/4, 1.0 sem Diminuto) e `custoInteiroReduzidoPorDiminuto(baseValue,
  passosReducao)` (aplica o divisor a um valor já em unidade-base de
  moeda, arredonda, nunca zera um item que já custava algo — usada só pra
  cálculo de saldo/orçamento, não pro texto exibido).
- **`CriadorState.kt`**: três funções novas — `pesoEquipamentoEfetivo(item)`
  e `custoEquipamentoEfetivo(item)` (peso/custo de um item já com o
  desconto, lendo o Diminuto do personagem via
  `ModifierEngine.racialDiminutoPassos(this)`) e `totalPesoEquipamentos()`
  (soma o peso já descontado de tudo em `equipamentosComprados` — substitui
  o cálculo de peso total que estava duplicado, sem desconto nenhum, em
  `EquipamentoSection.kt` e `ResumoSection.kt`).
- **`EquipamentoFormatters.kt`**: `toResumo()` ganhou o parâmetro
  `passosDiminuto` (default 0, não quebra quem já chamava sem argumento) e
  duas funções puras novas, `pesoTextoComDiminuto()`/
  `custoTextoComDiminuto()` — aplicam o divisor ao número do texto original
  (`"300"` → `"150"`, `"5 po"` → `"0.5 po"`) preservando a unidade,
  **sem mexer** em texto que não é um número puro (`"Variável"`, `"x2"`,
  `"-5K * Tam"` — preços especiais/multiplicadores de outros catálogos,
  não o preço direto de um item comum).
- **`ListItems.kt`** (`StandardEquipamentoItem`) e **`EquipamentoSection.kt`**:
  o card de cada item na lista de Equipamentos (busca e navegação por
  categoria) agora recebe `passosDiminuto` calculado uma vez
  (`ModifierEngine.racialDiminutoPassos(state)`) e mostra peso/custo já
  descontados; o filtro "Somente acessíveis" agora compara contra
  `state.custoEquipamentoEfetivo(item)` em vez do custo cheio; o total de
  peso da mochila (limite de Carga) usa `state.totalPesoEquipamentos()`.
  **Não** mexi no diálogo de item mágico da Herança (orçamento fixo de
  10.000 PO, peça mágica sob medida não listada por peso/custo de
  catálogo comum — desconto de Diminuto não se aplica a esse fluxo).
- **`ResumoSection.kt`**: mesmo total de peso trocado pra
  `state.totalPesoEquipamentos()`.
- **`ResumoPdfReferenciador.kt`**: `buildEquipamentosBlocks()` (tabela de
  equipamento geral do PDF) passa `personagem.passosDiminuto` (campo que já
  existia em `MeuPersonagem`, usado nas rodadas anteriores pra Força
  Mínima/dano) pro `toResumo()` e pro novo `pesoTextoComDiminuto()`.
- **Verificação**: compilei `CriadorState.kt`/`ForcaMinimaCalculator.kt`/
  `RacialTraitPointCatalog.kt`/`EquipamentoFormatters.kt` de verdade no
  harness standalone (mesmo usado na Décima nona rodada) e rodei 11 testes
  JVM novos — `CriadorStateDiminutoEquipmentTest` (matemática pura dos 3
  tiers, `pesoEquipamentoEfetivo`/`custoEquipamentoEfetivo`/
  `totalPesoEquipamentos` com uma raça sintética Minúscula de verdade
  passando por `CriadorState`, caso sem Diminuto continua com valor cheio,
  texto não numérico não quebra) e
  `EquipamentoFormattersDiminutoTest` (formatação de texto, preservação de
  unidade Pathfinder, textos especiais intocados) — todos os 11 passaram.
  Os arquivos de Compose UI tocados (`AncestralidadesSection.kt`,
  `ListItems.kt`, `EquipamentoSection.kt`, `ResumoSection.kt`) não têm como
  ser compilados neste sandbox (sem os artefatos reais de
  compose-ui/material3) — validados por revisão manual e ficam pra CI/
  Gradle real confirmar.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria; Variante de raça-base de livro diferente das tags fica órfã;
dinheiro do personagem não é deduzido automaticamente ao comprar
equipamento (não mexido nesta rodada, fora do escopo pedido).

## Vigésima primeira rodada — Robusto do Ogro (investigado, não é bug), equipamento devolvido ao trocar de tamanho, grupos recolhíveis em Equipamentos

Três pontos trazidos pelo dono do projeto depois de testar a rodada
anterior.

### Parte 1 — Robusto do Ogro: investigado, mantido como estava

Suspeita: o Robusto do Ogro (custo 2, id `ROBUSTO`) estaria errado —
deveria custar 4, vindo do traço genérico "Super Poderes" (2 pontos pelo
Antecedente Arcano + custo do poder do Compêndio de Superpoderes
escolhido, nesse caso "Robusto" a 2 pontos).

- **Conferido nos dois livros antes de mexer**: o livro básico (cap. 1,
  "Habilidades Raciais Positivas", pág. 20) lista **duas entradas
  diferentes**: "Robusto (1): Um segundo resultado Abalado não causa
  Ferimento" com custo **2**, direta, standalone; e "Super Poderes (1):
  Possui habilidades... tiradas do Compêndio de Super Poderes... custo é
  2... mais o custo do poder selecionado (X)" — um mecanismo GENÉRICO
  separado, pra raças que concedem qualquer poder do Compêndio de
  Superpoderes, não uma via alternativa pra montar o Robusto especificamente.
  São dois caminhos possíveis pro Mestre calibrar uma raça, coincidência de
  nome/efeito (e de custo base) entre os dois, não a mesma coisa.
- **Confirmado por matemática**: somei TODOS os traços do Ogro (`ancestralidades.json`)
  com as regras/custos já cadastrados (`ARROGANTE` -2, `PENALIDADE_PERICIA_1`
  x2 -1 cada, `FORASTEIRO` Menor -1, `VOLUMOSO` -2, `MUITO_FORTE` +4,
  `MUITO_RESISTENTE` +4, `ROBUSTO` +2, `SEM_NOCAO` -2, `TAMANHO_MAIS_1` +1)
  e o total bate **exatamente 2** — o orçamento padrão do livro
  (`pontosRaciaisEsperados` do Ogro não está sobrescrito no catálogo, fica
  no default 2). Se `ROBUSTO` valesse 4, o Ogro fecharia em 4, e o livro
  teria calibrado a raça com um orçamento maior (não é o caso: nenhuma
  entrada de "raças mais fortes por design" — Pathfinder/Arte da
  Guerra/Crystal Heart — cita Ogros). Isso confirma que o cálculo atual
  (`ROBUSTO` custo 2, direto, sem passar pelo Antecedente Arcano) é o que
  o próprio livro usou pra calibrar a raça.
- **Achado incidental relevante**: o mecanismo genérico "Super Poderes
  (2+X)" que o dono do projeto descreveu **já existe no app**, implementado
  numa rodada anterior — é o picker `superPoderRacialPickerTarget` em
  `SettingsDialog.kt` (fluxo de criação de Vantagem/Variante custom), que
  lista o catálogo real de `super_poderes.json`, calcula
  `custoTotal = 2 + custoPoder` pra cada opção e monta a `HabilidadeCriacao`
  final com esse custo. Já está corretamente citado/comentado no código
  ("Super Poderes (2+X): o traço racial em si custa 2 pontos, mais o custo
  do Super Poder do Compêndio de Super Poderes escolhido"). Não precisou
  implementar nada novo — só confirmar que já existe e que o Ogro (raça
  OFICIAL) não usa esse caminho porque não precisa: o livro já cadastra
  Robusto como habilidade direta própria.
- **Nenhuma mudança de código nesta parte** — decisão do dono do projeto
  após ver as evidências: manter o Ogro como está.

### Parte 2 — equipamento "devolvido" ao trocar de tamanho (Diminuto)

Preocupação: comprar equipamento pelo desconto de Diminuto (rodada
anterior) e depois trocar de raça poderia deixar o personagem com o item
"preso" no preço/peso baixo mesmo não sendo mais Diminuto.

- **Testado antes de mexer em qualquer coisa**: reproduzi o cenário exato
  descrito (comprar armadura de 300 por 30 como uma raça Minúscula
  sintética, trocar pra Humano) num teste JVM real. O preço/peso **já**
  recalculavam sozinhos pro valor cheio (300/peso cheio), porque
  `pesoEquipamentoEfetivo()`/`custoEquipamentoEfetivo()` (rodada anterior)
  nunca gravam o desconto NO item — recalculam ao vivo a partir da raça
  ATUAL do personagem toda vez que são chamados. Ou seja, o número exibido
  já nunca ficava "preso" no desconto.
- **O que realmente faltava**: o ITEM em si continuava fisicamente na
  mochila (`equipamentosComprados`) mesmo depois de trocar de raça — só o
  preço/peso exibido corrigia, não a presença do item. Decisão do dono do
  projeto: remover o item automaticamente, já que equipamento Diminuto é
  "feito sob medida" pro tamanho da ancestralidade (texto do livro Fantasia,
  pág. 10) e não serve mais num corpo de tamanho diferente.
- **`ModifierEngine.kt`**: extraí `racialDiminutoPassosDe(habilidades: List<RacialAbility>?)`
  a partir do corpo de `racialDiminutoPassos(state)` (que agora só delega
  pra essa função com `state.currentAncestryDef?.habilidades`) — permite
  calcular o tier de Diminuto de uma `RacialModifier` qualquer, não só da
  raça ATUAL do `CriadorState` (precisava comparar a raça ANTES/DEPOIS da
  troca, e a raça anterior já não é mais `state.currentAncestryDef` no
  momento da comparação).
- **`CriadorState.aplicarAncestralidade()`**: calcula `passosDiminutoAntes`/
  `passosDiminutoDepois` (raça anterior/nova, via `racialDiminutoPassosDe()`)
  logo no início da função, e logo depois de `ancestralidade = anc` — se os
  dois tiers forem diferentes (inclusive 0→Diminuto ou Diminuto→0) e a
  mochila não estiver vazia, limpa `equipamentosComprados` inteira e
  adiciona uma mensagem de feedback ("N equipamento(s) devolvido(s):
  o tamanho da Ancestralidade mudou..."), mesmo padrão de feedback já usado
  pros outros ajustes automáticos dessa função (pontos de atributo/perícia/
  Vantagem devolvidos, Vantagens removidas por requisito). Troca entre duas
  raças do MESMO tier (ex.: duas raças de tamanho normal, ou duas raças
  igualmente Muito Pequenas) não mexe na mochila.
- **Verificação**: 3 testes JVM novos no harness standalone (raça Minúscula
  → Humano devolve a mochila e avisa; duas raças de tamanho normal não
  mexem em nada; troca entre dois tiers diferentes de Diminuto — Minúsculo
  → Pequeno — também devolve), todos rodados de verdade e passando, mais
  os 9 testes das rodadas anteriores continuam passando sem quebrar nada
  (mesma assinatura pública de `racialDiminutoPassos(state)`, só ganhou um
  overload novo).

### Parte 3 — grupos recolhíveis na lista de Equipamentos

Pedido: no modo "Navegar" (sem busca) da aba Equipamentos, o SuperType
(ex.: "Armaduras") já era recolhível (`CollapsibleSection`, de uma rodada
anterior), mas ao expandir um SuperType grande a lista inteira de Grupos
(ex.: "Corpo", "Escudos"...) e todos os itens despejava de uma vez, sem
como recolher só um Grupo específico — obrigando a rolar a tela inteira
pra passar por uma lista grande.

- **`CriadorState.kt`**: novo `equipExpandedGroups = mutableStateMapOf<String, Boolean>()`,
  chaveado por `"SuperType/Grupo"` (ex.: `"Armaduras/Corpo"`) pra não
  colidir entre SuperTypes diferentes que reaproveitem o mesmo nome de
  grupo.
- **`EquipamentoSection.kt`** (modo "Navegar"): cada `groupName` (antes um
  `Text` fixo sempre visível) agora usa o MESMO componente
  `CollapsibleSection` já usado pro SuperType — clica pra expandir/recolher
  só aquele grupo, mesmo estilo visual (ícone +/-, borda arredondada) já
  padronizado na rodada dos "Filtros Avançados". Recolhido por padrão
  (mesmo padrão do SuperType), então abrir um SuperType grande agora mostra
  só os títulos dos grupos, sem despejar item nenhum até o jogador escolher
  qual grupo abrir. O nível de Subgrupo (dentro de cada Grupo, ex.:
  "Medievais"/"Pólvora Negra" dentro de "Ataque à Distância") continua como
  um cabeçalho simples, sem colapso — listas nesse nível costumam ser bem
  menores, não pareceu precisar de um terceiro nível de recolhimento.
- Não mexi no modo "Buscar" (lista plana filtrada por texto) — esse modo já
  não tem hierarquia de Grupo/Subgrupo pra recolher, é resultado de busca
  direto.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria; Variante de raça-base de livro diferente das tags fica órfã;
dinheiro do personagem não é deduzido automaticamente ao comprar
equipamento.

## Vigésima segunda rodada — Vantagem concedida duplicada no Resumo (Sáurios) + investigação do traço "Poder" (Transmorfos)

Dois pontos trazidos pelo dono do projeto.

### Parte 1 — "Sentidos Aguçados, Prontidão" duplicado no Resumo

Relato: na aba Resumo, a lista "Características Raciais" de Sáurios mostrava
"Sentidos Aguçados, Prontidão" — deveria mostrar só "Sentidos Aguçados" (o
skin do traço da própria raça); "Prontidão" já aparece certo, separadamente,
na seção "Vantagens".

- **Causa raiz**: toda habilidade com `category == "racial_edge"` concede
  uma Vantagem de verdade (`RacialModifier.vantagensGratisEfetivas()`) — o
  id/targetRef dela vai pra `personagem.vantagensRaciais` (lógica de
  requisito/automação) e o `nome` (skin) dela já vai separadamente pra
  `habilidadesRaciais`. `SummaryUtils.buildSummaryLines()` juntava as DUAS
  listas (`habilidadesRaciais + personagem.vantagensRaciais`) pra montar a
  linha "Características Raciais" — pra qualquer traço `racial_edge`, isso
  bota o MESMO traço duas vezes: uma pelo skin, outra pelo nome oficial da
  Vantagem concedida. Existia um hardcode (`especieIdAtual == "saurios" &&
  key == "PRONTIDAO" -> "Sentidos Aguçados"`) tentando mascarar exatamente
  esse sintoma pra Sáurios, mas não removia a duplicata da lista — só
  igualava o texto das duas entradas, dependendo de `especieIdAtual` bater
  exatamente; não bati com certeza em qual condição específica ele estava
  falhando pro relato do dono do projeto, mas a causa estrutural (a lista
  duplicada por baixo) é a mesma em qualquer cenário.
- **Correção estrutural** (não mais um hardcode por espécie):
  `SummaryUtils.kt` agora calcula `vantagensCobertasPorHabilidadeEstatica`
  — o conjunto de ids/targetRef de toda habilidade `racial_edge` da PRÓPRIA
  raça (mesma extração de `vantagensGratisEfetivas()`) — e filtra
  `personagem.vantagensRaciais` pra excluir qualquer entrada já coberta por
  essa habilidade estática ANTES de juntar com `habilidadesRaciais`. Só
  sobra em `vantagensRaciaisSemSkinEstatico` uma Vantagem concedida que NÃO
  vem de uma habilidade estática da raça (ex.: injetada em tempo de
  execução por uma Variante custom, sem entrada em `habilidades[]`) — essa
  continua aparecendo normal (sem skin pra usar mesmo). Isso corrige Sáurios
  E qualquer outra raça com o mesmo padrão (conferido no catálogo real:
  Kitsunemimi "Socialmente Sofisticados"→Cativar o Ambiente tinha o MESMO
  bug estrutural, sem hardcode nenhum tentando mascarar — corrigido de
  graça pela mesma correção).
- **Limpeza**: removidos dois hardcodes por nome de espécie que ficavam bem
  ao lado — o de Sáurios (agora redundante/inalcançável, a entrada
  duplicada nem chega mais nesse ponto do código) e o de Povo Rato/"Fobia"
  (já era código morto ANTES desta rodada: `category == "racial_hindrance"`
  nunca passa pelo filtro que monta `habilidadesRaciais`/`vantagensRaciais`,
  então essa condição nunca disparava).
- **PDF**: conferido que `ResumoPdfReferenciador.kt` NÃO chama
  `buildSummaryLines()` — a ficha em PDF não tem hoje nenhuma seção
  "Habilidades Raciais"/"Características Raciais" (só "Vantagens" e
  "Complicações", vindas de `personagem.vantagens`/`personagem.complicacoes`
  diretamente). Ou seja, o bug relatado só existia na tela de Resumo — no
  PDF não tinha como acontecer porque essa lista simplesmente não é
  desenhada lá. Não adicionei essa seção ao PDF nesta rodada (o dono do
  projeto só supôs que existia, "acredito eu que...", sem confirmar) —
  fica como pergunta em aberto se ele quer essa seção também no PDF.
- **Verificação**: adicionado teste novo em `SummaryUtilsTest.kt`
  reproduzindo o cenário exato (Sáurios com habilidade `racial_edge`
  id=PRONTIDAO + `vantagensRaciais=["PRONTIDAO"]`) — confirma que
  "Sentidos Aguçados" aparece exatamente uma vez e "Prontidão" não aparece
  na linha racial. Rodei TODA a suíte `SummaryUtilsTest` (17 testes,
  cobrindo Avianos/Elfos/Centaux/Tanukimimi/Feral/etc.) depois da mudança —
  nenhum regrediu.

### Parte 2 — traço "Poder" (Antecedente Arcano + poder específico) e Transmorfos

Pedido: verificar se o mecanismo de criação de raça do livro básico "Poder
(S)" — "a ancestralidade tem uma habilidade inata que funciona como um
poder... por 2 pontos, tem o Antecedente Arcano (Dom) e um poder que
reflete sua habilidade incomum" — está implementado e se Transmorfos usa
ele corretamente (a habilidade "Mudar de Forma" deles é justamente essa,
ligada ao poder Disfarce restrito a si mesmo).

Investigação (sem mudar código ainda — achados pra decisão do próximo
passo):

- **Transmorfos RECEBE o Antecedente Arcano (Dom) corretamente** — testei
  de verdade (`CriadorStateTransmorfosPoderTest`, via `aplicarAncestralidade`
  real) com só o Compêndio de Fantasia ativo (sem Horror/Sci-Fi, que são os
  únicos livros que têm a entrada específica "antecedente_arcano_dom" no
  catálogo): o personagem termina com exatamente 1 Vantagem "Antecedente
  Arcano" com a escolha "Dom" marcada. Isso funciona por causa de um
  fallback já existente em `ResolveAncestryRacialPackageUseCase` (concede a
  Vantagem genérica "antecedente_arcano" + escolha "DOM" quando a entrada
  específica do livro não está carregada) combinado com um bloco
  hardcoded por NOME DE RAÇA ("TRANSMORFOS") em
  `ResolveAncestrySpecificAdjustmentsUseCase.kt`.
- **O mecanismo GENÉRICO não existe** — e é isso que faz o hardcode acima
  ser necessário. Dois problemas achados:
  1. Existem DOIS ids diferentes pro mesmo conceito, sem ligação entre
     eles: a raça oficial Transmorfos usa `id: "ANTECEDENTE_ARCANO_PODER"`
     (`ancestralidades.json`); o catálogo de criação de raça customizada
     (`basico_habilidades_raciais.json`, usado em `SettingsDialog.kt`) tem
     uma entrada DIFERENTE, `id: "poder_racial"` ("Poder Inato", mesma
     descrição do livro). Nenhum dos dois aparece em
     `RacialTraitPointCatalog.EFEITOS` (só `ANTECEDENTE_ARCANO_PODER` tem
     entrada em `CUSTOS`, custo 2 — `poder_racial` não tem NENHUMA entrada
     no catálogo de pontos).
  2. Ao contrário do "Super Poderes (2+X)" (que tem um picker de verdade em
     `SettingsDialog.kt`, `superPoderRacialPickerTarget`, deixando o Mestre
     escolher o poder específico do Compêndio de Super Poderes e calcula
     2+X automaticamente), o "Poder Inato"/`poder_racial` NÃO tem picker
     nenhum — escolher esse traço ao criar uma raça customizada só adiciona
     um `HabilidadeCriacao` com o texto fixo do JSON, sem conceder o
     Antecedente Arcano de verdade nem deixar escolher o poder. É só texto
     decorativo.
- **Transmorfos NÃO recebe o poder específico (Disfarce, restrito a si
  mesmo) automaticamente** — busquei em todo o código por qualquer lugar
  que adicione um poder a `poderesSelecionados` automaticamente por causa
  de raça: não existe NENHUM (nem pra Transmorfos, nem pra nenhuma outra
  raça). O jogador tem o Antecedente Arcano (Dom) de verdade, mas precisa
  escolher/pagar o poder Disfarce manualmente entre os poderes iniciais
  normais da Vantagem — o livro diz que esse poder deveria vir de graça,
  além dos poderes iniciais normais ("Ela não aumenta os Pontos de
  Poder—use a vantagem Pontos de Poder pra isso", ou seja, é um poder A
  MAIS, não conta contra o total de PP).
- **Achado extra no caminho** (mesma categoria de bug): a habilidade
  "CARISMÁTICO" de Transmorfos ("Começam gratuitamente com a Vantagem
  Carismático") também NÃO concede a Vantagem de verdade —
  `ensureAdvantageNames`/`ensureAdvantageIds` do bloco hardcoded de
  Transmorfos não incluem Carismático, e o id não está em
  `RacialTraitPointCatalog.EFEITOS`. Travado num teste (`CriadorStateTransmorfosPoderTest`)
  que documenta o estado ATUAL (falha de propósito se alguém corrigir sem
  atualizar o teste, pra não regredir silenciosamente essa correção
  futura).
- **Nenhuma mudança de código nesta parte** — decisão de implementação
  (unificar os dois ids, construir um picker de poder pro "Poder Inato"
  igual ao de Super Poderes, conceder o poder Disfarce automático pra
  Transmorfos, corrigir Carismático) fica pro dono do projeto decidir o
  escopo/prioridade antes de eu mexer.

**Ainda pendente**: tudo da rodada anterior, mais a seção "Habilidades
Raciais" no PDF (se o dono do projeto quiser) e a implementação do
mecanismo genérico "Poder" (Antecedente Arcano + poder específico) — ver
Parte 2 acima.

## Vigésima terceira rodada — Habilidades Raciais no PDF, Carismático, Golpe de Asa/Queimar/Arma de Sopro, Poder Favorito e mecanismo genérico "Poder"

Retomando os itens em aberto da rodada anterior, mais três Vantagens novas
do livro Fantasia trazidas pelo dono do projeto (Golpe de Asa, Queimar,
Poder Favorito).

### Seção "Habilidades Raciais" no PDF

Extraída a lógica de "Características Raciais" (skin da raça, casos
especiais por espécie, dedupe de Vantagem concedida — rodada anterior) de
dentro de `SummaryUtils.buildSummaryLines()` para a função pública
`buildRacialTraitsList()`, reaproveitada agora também por
`ResumoPdfReferenciador.gerarFichaEmPdf()` como uma seção própria "Habilidades
Raciais" — mesma lista, mesma lógica, sem duplicar código. `ancestralidadeAtual`
(já resolvida, `state.currentAncestryDef`) passa a ser parâmetro de
`produzirEExibirFichaPdf()`/`gerarFichaEmPdf()`, threaded desde `MainActivity.kt`
(`PdfExportRequest`). Os 17 testes de `SummaryUtilsTest` continuam passando
sem nenhuma mudança de comportamento (só reorganização de onde o código mora).

### Carismático dos Transmorfos

A habilidade "CARISMÁTICO" ("Começam gratuitamente com a Vantagem
Carismático") nunca concedia a Vantagem de verdade — mesma classe de bug do
"Poder"/Antecedente Arcano investigada na rodada anterior. Adicionada ao
pacote racial hardcoded de Transmorfos (`ensureAdvantageNames`), protegida
da remoção por requisito não atendido (Espírito d8+, que um personagem
recém-criado não tem) pelo mesmo mecanismo que já protege qualquer outra
Vantagem concedida por raça.

### Golpe de Asa (requisito "Asas") e Queimar (requisito "Arma de Sopro")

Trocada a checagem da tag manual solta em `ancestralidades.json` pelo
traço de VERDADE da raça — `RacialTraitPointCatalog.temTracoVoo()`/
`temArmaDeSopro()`, novos, checam `habilidades[]` por id (`VOO_MOV_6`/
`VOO_MOV_12`/`VOO_MOV_24`/`ASAS_DE_ANJO` pra Voo; `ARMA_DE_SOPRO` pro
Sopro) — nos dois lugares que validam requisito de Vantagem
(`CriadorState.atendeRequisitosMantidos`, `ValidateRequirementsUseCase`,
usado por `RequirementValidator`/Progressão e por `ValidateSelectionUseCase`/
criação). Achado real ao fazer essa troca: Draconianos tinham a tag "asas"
cadastrada mesmo sem nenhum traço de Voo — o livro (Ideias Variantes de
Draconianos) deixa claro que voar é opcional pra essa raça, não o padrão —
corrigido também no JSON (removida a tag errada, mantida "arma_de_sopro",
que está correta).

### Ataque de Sopro exibido (dano + área) e Vantagem Queimar

A habilidade "Arma de Sopro" dos Draconianos (2d6 de dano em Modelo de
Cone ou linha de 12 quadros) nunca aparecia em lugar nenhum além do texto
narrativo do traço — sem representação estruturada, não tinha como a
Vantagem Queimar ("o dano... aumenta em um tipo de dado") alterar nada de
verdade. `CriadorState.extrairArmasNaturais()` agora adiciona um item
"Ataque de Sopro" (dano 2d6, ou 2d8 com Queimar, reaproveitando o mesmo
`upgradeDie()` já usado por Garras/Mordida aprimoradas) sempre que a raça
tiver o traço — mesma lista que já alimenta Resumo e PDF: no PDF entra na
tabela de Armas à Distância (tem `distancia` preenchido, não "Toque"); no
Resumo, na lista de Ataques Naturais (a área vai nas observações, já que
essa lista não tem coluna de alcance própria).

### Poder Favorito — já funcionava (Fantasia); faltava só a variante do Horror

Investigado antes de implementar qualquer coisa: o picker de "Poder
Favorito" (escolher um dos poderes já conhecidos, ficar registrado como
"Poder Favorito (Disfarce)" no nome, via o mesmo campo `Vantagem.choice`
que Antecedente Arcano/Conexões já usam pra mostrar a escolha) **já
estava implementado por completo** pra `id == "poder_favorito"` (Fantasia
e Sci-Fi, que compartilham esse id) — dialog de escolha
(`dialogMostrandoPoderFavorito`), exclusão de poderes já favoritados,
`.choice` setado na compra, exibição automática em Vantagens/Resumo/PDF
via o mecanismo genérico de `choice`. O único id que ficava de fora era
`poder_favorito_horror` (entrada própria do livro Horror, com requisito
diferente) — comprá-lo caía direto no fluxo genérico (sem escolher poder
nenhum, sem NUNCA ficar registrado qual poder é o favorito). Corrigido
nos 3 pontos onde o app checa `vant.id == "poder_favorito"` pra decidir
se abre o picker (mesmo trecho duplicado 3x em `VantagensSection.kt`,
achado ao mexer) e no filtro de "já favoritados" do próprio dialog.

### Mecanismo genérico "Poder" (Antecedente Arcano + poder específico)

Implementado o picker que faltava (achado real na rodada anterior:
"ANTECEDENTE_ARCANO_PODER", da raça oficial Transmorfos, e "poder_racial"/
"Poder Inato", do catálogo de criação de raça customizada, são dois ids
DESCONECTADOS pro mesmo conceito, e nenhum dos dois tinha picker — ao
contrário de "Super Poderes", que já tinha um totalmente funcional).
`SettingsDialog.kt` ganhou um picker análogo ao de Super Poderes
(`poderRacialPickerTarget`), mas com duas diferenças de propósito,
pedidas pelo dono do projeto:
- **Custo fixo de 2 pontos**, não "2+X" — o livro não soma custo do poder
  escolhido pro traço "Poder" (diferente de "Super Poderes", que soma o
  custo do Compêndio de Super Poderes de verdade).
- **Restrito a poderes de Estágio Novato** (`Poder.estagio == "Novato"`):
  o traço em si não diz isso no livro, mas sem essa trava um Mestre podia
  escolher um poder Lendário (ex.: Ressurreição) pelos mesmos 2 pontos —
  o próprio dono do projeto identificou esse desbalanceamento ao descrever
  o pedido, então a trava entrou de propósito, com um aviso no texto do
  dialog explicando o porquê.
- **Escopo confirmado por paridade com Super Poderes**: assim como
  escolher "Super Poderes (Voo)" num traço de raça customizada NÃO concede
  a Vantagem Antecedente Arcano (Super Poderes) de verdade quando um
  jogador depois seleciona essa raça (confirmado lendo o código: o traço
  vira `RacialAbility` com `category = "racial_trait_positive"`, nunca
  `"racial_edge"` — a categoria que de fato aciona a concessão automática
  de Vantagem em `vantagensGratisEfetivas()`), o novo picker "Poder"
  registra o traço (nome, custo, descrição) do mesmo jeito, sem conceder
  Antecedente Arcano nem o poder escolhido automaticamente ainda. Isso é
  uma limitação JÁ EXISTENTE de Super Poderes, não algo introduzido agora
  — implementar a concessão automática de verdade (tanto pra Super Poderes
  quanto pra Poder) é uma feature maior, à parte, que fica pra decisão
  futura do dono do projeto.
- Não consegui compilar/rodar `SettingsDialog.kt` (Compose UI, mesma
  limitação de sempre neste sandbox) — validado por leitura cruzada de
  todos os 3 pontos que precisavam da mesma mudança (2 blocos de seleção
  de traço + o novo dialog), balanceamento de chaves/parênteses, e
  conferência de que `HabilidadeCriacao`/`RacialAbility`/`Poder` têm os
  campos usados. Fica pro CI confirmar a compilação.

### Verificação

38 testes JVM novos nesta rodada + rodadas anteriores (41 no total do
grupo relevante), todos rodados de verdade no harness standalone e
passando: `SummaryUtilsTest` (17, incluindo o novo teste de dedupe da
rodada anterior), `CriadorStateTransmorfosPoderTest` (2, agora cobrindo
Carismático concedido de verdade), `ValidateRequirementsUseCaseTagsTest`
(5, novo — Golpe de Asa/Queimar por traço real), `CriadorStateArmaDeSoproTest`
(3, novo — dano/área do Ataque de Sopro com e sem Queimar), mais os testes
de Diminuto das rodadas anteriores.

**Ainda pendente**: exibição de Resistência por local no PDF em tabela
própria; Variante de raça-base de livro diferente das tags fica órfã;
dinheiro do personagem não é deduzido automaticamente ao comprar
equipamento; concessão automática de verdade (Antecedente Arcano + poder/
super poder) pra traços de raça customizada criados via "Super Poderes"/
"Poder" — hoje só registram o traço, não concedem nada ao personagem.

## Vigésima quarta rodada — Super Poderes redundantes com traços de raça

Pedido: comparar os 92 Super Poderes (`super_poderes.json`) com os 115
traços de `basico_habilidades_raciais.json` (catálogo usado tanto pelo
picker "Super Poderes (2+X)" quanto pelo novo "Poder") e tirar do
seletor de Super Poderes qualquer um que a raça já cubra por outro
traço — evitar pagar duas vezes pela mesma coisa (ex.: Super Poder
Resistência quando já existe o traço racial `RESISTENCIA`).

### Grupo A — excluído do seletor (34 itens, confirmados pelo texto do
livro, não só pelo nome)

| Super Poder | Traço racial equivalente |
|---|---|
| Ações Adicionais | Ação Adicional / Ações Adicionais (Maior) |
| Alcance | Alcance (+1) |
| Andar nas Paredes | Andar nas Paredes |
| Aparar | Aparar (+1) |
| Aquático | Aquático / Semi-Aquático |
| Armadura | Armadura (+2) |
| Ataque Corpo a Corpo | Garras / Mordida / Chifres |
| Ataque de Longa Distância | Arma de Sopro |
| Atordoar | Atordoar |
| Aumentar/Reduzir Característica | Aumento de Atributo |
| Bônus de Perícia | Bônus de Perícia (+1/+2) |
| Camaleão (Grupo B) | Camuflagem (Adaptável) — muda de cor pra se camuflar, mesmo efeito |
| Cavar | Cavar |
| Construto | Construto |
| Espacial | Espacial |
| Imune a Doenças/Venenos | Imunidade a Doenças ou Venenos |
| Interface | Interface |
| Invisibilidade | Invisibilidade (Translúcido/Total) |
| Membros Extras | Membros Extras |
| Morto-vivo | Morto-Vivo |
| Movimentação | Movimentação Aumentada (+2) |
| Mudança de Forma | Mudança de Forma |
| Não dorme | Redução de Sono — texto do Super Poder é literalmente "precisa de metade do tempo normal de sono", igual à descrição do traço |
| Não respira | Não Respira |
| Regeneração | Regeneração / Regeneração Maior |
| Resistência Ambiental | Resistência Ambiental |
| Resistência | Resistência (+1) |
| Robusto | Robusto |
| Salto | Saltador |
| Sem Órgãos Vitais | Sem Órgãos Vitais |
| Sentidos Aprimorados | Sentidos Aguçados (Visão/Audição/Olfato) |
| Supervantagem | Vantagem Inata (Novato/Experiente/Veterano/Heroico) — texto "garante uma Vantagem... independente do Estágio" é a mesma coisa |
| Telepatia | Telepatia |
| Veneno | Toque Venenoso (Moderado/Paralisante/Projetado/Letal) |
| Voo | Voo (Mov 6/12/24) |

Implementado em `SettingsDialog.kt`: novo conjunto top-level
`SUPER_PODERES_JA_COBERTOS_POR_TRACO_RACIAL` (nomes normalizados via
`keyify()`) e `superPoderesCatalogParaTracoRacial` (o catálogo já
filtrado), usado só no dialog do picker do traço "Super Poderes (2+X)".
Escopo restrito de propósito: não toca em `state.listaSuperPoderes`
(compra normal de Super Poderes por um personagem, `SuperPoderesSection.kt`)
nem em `superPoderesParaModificador` (gerenciamento de Modificador de
Poder do Mestre, que precisa continuar enxergando o catálogo completo
mesmo pros Super Poderes agora escondidos do picker racial).

### Grupo B — decisão do dono do projeto

- **Camaleão**: excluído (foi pro Grupo A acima) — mesmo efeito de
  `camuflagem_2`.
- **Crescimento** e **Encolhimento**: mantidos selecionáveis, de
  propósito. Mudar de Tamanho ativamente/temporariamente (ex.: um gnomo
  que vira uma versão grande de si mesmo) é mecânica e narrativamente
  diferente de já nascer com Tamanho +1 ou Diminuto — ambos os traços
  raciais são permanentes/passivos, os Super Poderes são um efeito que o
  personagem aciona.

### Grupo C — sem sobreposição com traço racial, revisado por outro
tipo de id (55 itens; segue selecionável, listado aqui só como
levantamento, nenhuma mudança de código)

A maioria (22 itens) não bate com nenhum outro id do app: Absorção,
Anular, Balançar, Campo de Força, Controlar Máquinas, Controle de
Clima, Controle de Energia, Controle de Matéria, Decompor, Duplicação,
Escanear, Escudo Mental, Furacão, Gênio, Infecção, Má Sorte, Mau
Funcionamento, Não envelhece, Precisão mortal, Servos, Terremoto,
Transmissão, Veículo.

Os outros 33 batem com uma **Magia (Poder)** ou uma **Vantagem** de
nome igual/parecido — o mesmo padrão que Campo de Dano, citado no
pedido:

| Super Poder | Bate com | Observação |
|---|---|---|
| Campo de Dano | Poder "Campo de Dano" | dano menor, área um pouco diferente, mesmo conceito — exemplo citado no pedido |
| Curar | Poder "Cura" | idem: um personagem com o traço "Poder" já pode escolher Cura como o poder concedido |
| Cegar | Poder "Cegar" | |
| Enredar | Poder "Enredar" | |
| Explodir | Poder "Explosão" | |
| Falar Idioma | Poder "Falar Idioma" | |
| Ilusão | Poder "Ilusão" | |
| Leitura de Objeto | Poder "Leitura de Objeto" | |
| Leitura Mental | Poder "Leitura Mental" | racial `telepatia` é diferente (comunicação, não ler pensamento) |
| Lentidão | Poder "Morosidade/Velocidade" | metade do poder (lado "morosidade") |
| Velocidade | Poder "Morosidade/Velocidade" | outra metade do mesmo poder (lado "velocidade") |
| Medo | Poder "Medo" | |
| Obscurecer | Poder "Iluminar/Obscurecer" | metade do poder |
| Telecinese | Poder "Telecinese" | |
| Teleporte | Poder "Teleporte" | |
| Intangibilidade | Poder "Intangibilidade" + parcialmente `forma_energia` (racial) | racial só cobre imunidade a dano físico/projétil, não atravessar paredes |
| Possessão | Poder "Fantoche" | efeito parecido (controlar outro corpo), nome diferente |
| Companheiro Animal | Poder "Amigo das Feras" | |
| Controle de Animal | Poder "Amigo das Feras" | mesmo Poder cobre os dois Super Poderes |
| Controle Mental | Poderes "Fantoche"/"Limpeza Mental" | parcial, nenhum dos dois é idêntico |
| Empurrar | Poder "Rajada" | parcial (Rajada também causa dano) |
| Forma Alternativa | Traço racial + Super Poder "Mudança de Forma" | mecânica de se transformar em algo específico, redundante com o par acima |
| Mimetismo | Traço racial "Camuflagem"/"Mudança de Forma" | parcial (imitar voz/aparência, não cor) |
| Destemido | Vantagem "Destemido" | e o traço racial "Vantagem Inata (Novato)" já permite conceder essa Vantagem de qualquer nome, Destemido incluso |
| Esquiva | Vantagem "Esquiva" | mesma lógica do item acima |
| Reflexos Aprimorados | Vantagem "Reflexos Rápidos" | |
| Não Come | Parcialmente coberto pelos traços Construto/Morto-Vivo/Robô | esses já embutem "não precisa comer" dentro de um pacote maior; não existe um traço isolado só pra isso |
| Perceptivo | Cluster racial Sentidos Aguçados/Infravisão/Visão 360° | cada traço racial é específico (um sentido, uma condição); o Super Poder é um bônus genérico de Perceber |
| Superatributo | Traço racial "Aumento de Atributo" | versão mais forte do mesmo conceito (excede o máximo normal) |
| Superperícia | Traço racial "Perícia Racial (d4/d6)" | versão mais forte do mesmo conceito (excede d12) |
| Superciência | Traços "Poder Inato"/"Super Poderes" (Antecedente Arcano) | é um Antecedente Arcano alternativo — não dá pra simplesmente excluir sem também remover uma opção legítima, precisa de decisão específica |
| Superfeitiçaria | idem | idem |

Nenhum desses foi tirado do seletor — ficam pra próxima decisão, caso
o dono do projeto queira que o traço "Poder" (que já restringe a
poderes de Novato) absorva parte desse papel em vez do Super Poder
equivalente aparecer solto no seletor de Super Poderes.

### Verificação

`scripts/phase6_reliability_gate.sh` passou (mesmo WARN de sempre sobre
o tamanho de `CriadorState.kt`, não relacionado a esta mudança).
`SettingsDialog.kt` não compila no harness standalone (Compose UI) —
validado por balanceamento de chaves (0, igual antes e depois) e de
parênteses (+8, igual antes e depois — a mudança não introduziu
desbalanceamento novo), além de leitura cruzada dos 3 pontos afetados
(constante, catálogo filtrado, ponto de uso no dialog). Fica pro CI
confirmar a compilação.
