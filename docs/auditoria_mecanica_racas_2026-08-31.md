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

Os outros 32 batem com uma **Magia (Poder)** ou uma **Vantagem** de
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

Nesta rodada esses 32 saíram do seletor também, por decisão do dono do
projeto: junto com o próprio traço "Poder" (Antecedente Arcano + Magia
de Novato, ver rodada anterior), não faz sentido oferecer duas vezes o
mesmo conceito por dois mecanismos diferentes. Caso especial:
**Superciência** e **Superfeitiçaria** — mesmo sendo balanceados num
cenário de Supers (onde todo mundo é super poderoso), como Antecedente
Arcano alternativo pra criação de raça eles são, pela própria descrição,
versões bem mais fortes que um Antecedente Arcano normal; concentrado
numa raça só (em vez de espalhado entre super-heróis), isso desbalancearia
demais — então ficam de fora, mesmo custando a opção "legítima" que
tinha sido citada como ressalva na rodada anterior.

Somando aos 34 do Grupo A (+ Camaleão do Grupo B), o seletor de Super
Poderes racial ficou só com os **25 itens** que sobraram sem nenhuma
sobreposição encontrada (23 do levantamento da rodada anterior mais
Crescimento e Encolhimento, mantidos do Grupo B): Absorção, Anular,
Balançar, Campo de Força, Controlar Máquinas, Controle de Clima,
Controle de Energia, Controle de Matéria, **Crescimento**, Decompor,
Duplicação, **Encolhimento**, Escanear, Escudo Mental, Furacão, Gênio,
Infecção, Má Sorte, Mau Funcionamento, Não envelhece, Precisão mortal,
Servos, Terremoto, Transmissão, Veículo. Conferido por script (ver
Verificação) contra o conjunto de exclusão real do código — bate certo.

Implementado ampliando o mesmo conjunto `SUPER_PODERES_JA_COBERTOS_POR_TRACO_RACIAL`
em `SettingsDialog.kt` (agora com um segundo bloco de nomes, comentado
separadamente do Grupo A) — nenhuma outra mudança de código, o filtro em
`superPoderesCatalogParaTracoRacial` já cobria qualquer nome adicionado
ao conjunto.

### Verificação

`scripts/phase6_reliability_gate.sh` passou (mesmo WARN de sempre sobre
o tamanho de `CriadorState.kt`, não relacionado a esta mudança).
`SettingsDialog.kt` não compila no harness standalone (Compose UI) —
validado por balanceamento de chaves (0, igual antes e depois) e de
parênteses (+8, igual antes e depois — a mudança não introduziu
desbalanceamento novo), e por script Python conferindo os 67 nomes do
conjunto de exclusão contra `super_poderes.json` (sem typo, sem
duplicata, todos existentes) e contando os 25 que sobraram
selecionáveis. Fica pro CI confirmar a compilação.

## Vigésima sexta rodada — Modo Auditoria: "ID de traço" em Ver Detalhes

Pedido: na tela "Ver detalhes" de uma raça (aba Ancestralidades), não dava
pra distinguir de olho se o que aparece ali vem do id de traço com uma
definição de verdade, de uma reskinagem por raça (ex.: Draconianos
"Mal-Humorado" reaproveitado como "Arrogante" via `targetRef`, comentário
já existente em `RacialAbilityLite`), ou de sujeira de hardcode sem
nenhuma das duas coisas. Pedido explícito: um jeito de ligar/desligar essa
leitura, só pra quem audita o app (não pro jogador nem pra quem cria
raça), com a leitura normal (com skin) continuando padrão e reversível a
qualquer momento.

Implementado:

- **`model/RacialTraitAuditFormatter.kt`** (novo): pra cada `RacialAbility`
  de uma raça, formata uma linha só com id/traitId cru, `targetRef`/`vezes`/
  `invisivel`/`category`, e a DEFINIÇÃO OFICIAL — nesta ordem de fonte:
  1. entrada com o mesmo id em `basico_habilidades_raciais.json` (via
     `HabilidadeCriacao`, carregado à parte, é o catálogo de criação de
     raças que já existe no app) — nome e descrição oficiais, ignorando de
     propósito `RacialAbility.nome`/`descricao` (onde mora a skin);
  2. sem entrada no catálogo oficial mas com `RacialTraitPointCatalog.LABEL`
     cadastrado — mostra o rótulo e avisa "sem entrada em
     basico_habilidades_raciais.json, só em RacialTraitPointCatalog" (traço
     bem específico de uma raça, sem equivalente no livro de criação);
  3. sem LABEL mas com efeito mecânico resolvido (`RacialTraitPointCatalog
     .efeitoDe`) — mostra o efeito bruto (ex.: "Atributo VIGOR +1 passo");
  4. nem catálogo nem efeito — `"⚠ SEM CATÁLOGO nem efeito mecânico —
     possível sujeira de hardcode..."`, com o nome de exibição normal como
     referência. `GRANTED_EDGE`/`RACIAL_HINDRANCE` (Vantagem/Complicação
     concedida por raça) ganham um caso à parte: o conteúdo de verdade está
     no `targetRef`, não faz sentido procurar "GRANTED_EDGE" no catálogo de
     traços.
- **`AppPreferences.loadModoAuditoriaIdPuro`/`saveModoAuditoriaIdPuro`**
  (novo, mesmo padrão de `loadTutoriaisDesabilitados`): boolean persistido,
  padrão `false` (leitura normal).
- **`AncestralidadesSection.kt`**: um `Switch` compacto ("Auditoria: ID de
  traço (sem skin)") logo abaixo do cabeçalho da aba, visível só quando
  "Ver detalhes" existe (`allowLongTexts`). Dentro do painel expandido de
  cada raça, com o modo ligado, a Descrição normal e a lista
  "Características:" (via `RacialCaracteristicasResolver`, que já mistura
  LABEL com fallback pro `nome` da raça) são substituídas pela lista de
  auditoria; desligado, comportamento idêntico a antes — nenhum código do
  caminho normal foi alterado, só envolvido num `if (modoAuditoriaIdPuro)`.

### Verificação

5 testes novos (`RacialTraitAuditFormatterTest`), rodados de verdade no
harness standalone e passando: entrada com catálogo oficial (ignora nome
reskinado), entrada só com LABEL (avisa que falta catálogo), GRANTED_EDGE
(usa targetRef), id sem catálogo nem efeito (vira aviso de hardcode), e
traço empilhável (x vezes, pontos multiplicados). `scripts/phase6_reliability_gate.sh`
passou. `AncestralidadesSection.kt` não compila no harness (Compose UI) —
validado por balanceamento de chaves/parênteses (0/0 antes e depois, sem
mudança) e leitura cruzada dos pontos alterados (import, estado do
toggle, catálogo carregado, switch, branch dentro do painel "Ver
detalhes"). Fica pro CI confirmar a compilação.

## Vigésima sétima rodada — corrige falso-positivo de "hardcode" no Modo Auditoria e adiciona "exclusivo desta raça"

Pergunta do dono do projeto: os traços que ele mandou criar como
"invisíveis" — específicos de uma raça só, pra balancear ou dar um poder
sem equivalente em outro livro — ficam identificados na auditoria, ou
o modo novo confunde eles com sujeira de hardcode?

Investigando pra responder direito, achei duas coisas:

1. **`RacialAbility.invisivel` não é o campo que ele está descrevendo.**
   Toda ocorrência real de `invisivel: true` no código hoje (grep em
   `ancestralidades.json` + `*.kt`) é de outra natureza: uma entrada
   sintética escondida da UI porque já aparece em outro lugar (ex.:
   `ATTRIBUTE_BOOST`/`SKILL_BOOST` espelhando um bônus já mostrado na aba
   de Atributos/Perícias, ou `GRANTED_EDGE`/`RACIAL_HINDRANCE` sintéticos
   de uma Variante custom) ou um ajuste de orçamento sem narrativa própria
   (`AJUSTE_FORMA_DE_ENERGIA`). Nenhuma tem a ver com "traço de
   balanceamento exclusivo de uma raça" — não é o mesmo conceito.

2. **O conceito real ("só essa raça tem") existe nos dados, mas não
   estava marcado em lugar nenhum** — e pior: o Modo Auditoria que acabei
   de entregar tinha um bug de verdade por causa disso. Rodando um script
   contra `ancestralidades.json` (112 raças, 180 ids distintos de
   traço): **122 ids são usados por exatamente 1 raça** — a maioria são
   traços bem específicos (ex.: "Magia Gnômica" do Gnomo, "Carismático"
   dos Transmorfos, "Obsessivos" do Gnomo). Desses, **49 não têm entrada
   em `basico_habilidades_raciais.json` nem `RacialTraitPointCatalog
   .LABEL`/`EFEITOS`** — exatamente o padrão que o formatador da rodada
   anterior rotulava como "⚠ possível sujeira de hardcode". Só que
   **todos os 49, sem exceção, TÊM entrada em `RacialTraitPointCatalog
   .CUSTOS`** (custo calibrado à mão, alguns com comentário tipo
   `"CARISMATICO" to 2, // oficial: vantagem_racial` ou `"MAGIA_GNOMICA"
   to 1, // Gnomo (Pathfinder) fecha com pontosRaciaisEsperados=4 só com
   este valor`) — ou seja, são traços PROPOSITAIS e calibrados, não
   sujeira. Rodando o mesmo script checando também CUSTOS: **zero ids
   ficam realmente sem nenhum match** em todo o catálogo oficial. O
   formatador da rodada anterior geraria um falso-positivo de "hardcode"
   pra essas 49 entradas legítimas — bug corrigido nesta rodada, antes de
   virar confusão pro dono do projeto durante a auditoria de verdade.

Implementado em `RacialTraitAuditFormatter.kt`:

- Novo degrau na cadeia de definição, entre "efeito mecânico bruto" e o
  aviso de hardcode: se o id está em `RacialTraitPointCatalog.CUSTOS`
  (mesmo sem LABEL/catálogo oficial), mostra `"Traço específico desta
  raça, sem nome genérico reaproveitável — custo calibrado no catálogo
  interno (+X pts), sem equivalente direto no livro de criação"` em vez
  do aviso de sujeira. O aviso de hardcode agora só dispara quando o id
  não bate em NADA (catálogo oficial, LABEL, EFEITOS, nem CUSTOS) —
  situação que hoje não ocorre pra nenhuma raça oficial, mas fica de
  guarda pra qualquer coisa nova que entrar sem passar por
  `RacialTraitPointCatalog`.
- Nova função `calcularIdsExclusivos(todasAsRacas: List<RacialModifier>)`:
  varre `habilidades[].id` de todas as raças e retorna os ids usados por
  exatamente uma — é só contagem estrutural, não tenta adivinhar se
  existe lógica hardcoded amarrada ao id em outro arquivo Kotlin (isso
  exigiria buscar o id pelo resto do código-fonte, fora do escopo desta
  função). `formatar()` ganhou um parâmetro opcional `idsExclusivos` que,
  quando o id da habilidade bate, soma a etiqueta `exclusivo-desta-raça`
  no cabeçalho de auditoria — ao lado de, não substituindo, a etiqueta
  `invisível(UI)` (renomeada de `invisível` pra deixar claro que é sobre
  a UI, não sobre exclusividade).
- `AncestralidadesSection.kt`: `idsExclusivosPorRaca` computado uma vez
  (`remember(state.listaAncestralidadesJson)`) e passado pro formatador.

**Limitação que fica registrada, não resolvida**: "exclusivo desta raça"
é só contagem de dados — não confirma se existe uma Vantagem/poder de
verdade concedido por lógica hardcoded amarrada a esse id específico em
algum `UseCase`/`CriadorState.kt` (esses hardcodes tendem a checar o
NOME da raça, não o id do traço — ex.: o `ensureAdvantageNames =
listOf("CARISMÁTICO")` da rodada 23 fica dentro de um `if (raça ==
TRANSMORFOS)`, sem nenhuma referência ao id `CARISMATICO` em si). Não
existe hoje um jeito automático de responder "este id específico aciona
algum grant hardcoded em outro arquivo" — só busca manual por raça.

### Verificação

3 testes novos + os 5 anteriores (8 no total em
`RacialTraitAuditFormatterTest`), todos passando no harness standalone:
id real (`CARISMATICO`) com custo em CUSTOS mas sem LABEL/catálogo não
vira mais aviso de hardcode; etiqueta `exclusivo-desta-raça` aparece só
quando o id está no mapa passado; `calcularIdsExclusivos` só marca ids
usados por exatamente 1 das raças de entrada (testado com uma raça
com 2 traços, um repetido em outra raça — só o não-repetido é marcado).
`scripts/phase6_reliability_gate.sh` passou. `AncestralidadesSection.kt`
não compila no harness (Compose UI) — balanceamento de chaves/parênteses
conferido (0/0, sem mudança) e leitura cruzada do novo bloco. Fica pro CI
confirmar a compilação.

## Vigésima oitava rodada — projeto "traço genérico em vez de id por combinação": plano e Rodada 1 (Elementais)

Pedido do dono do projeto: parar de vez com traços de raça oficiais
resolvidos por `if` hardcoded de nome de raça/id fixo por combinação
(atributo aumentado, perícia aumentada, Toque Venenoso, Garras/Chifres/
Mordida, Diminuto, Vantagem Inata etc.) e migrar pra um modelo onde o
traço é genérico e a escolha de intensidade/variante vira dado, com o
custo calculado — não um id novo por combinação. Antes de tocar em
código, investiguei 4 pontos específicos levantados pelo dono do projeto:

- **VEZES_MAX (teto de compra de traço empilhável) já é respeitado.**
  Script conferindo as 112 raças oficiais contra `RacialTraitPointCatalog
  .VEZES_MAX`: zero violações. O picker de criação (`stackPickerTarget`,
  `SettingsDialog.kt`) já limita as opções ao `vezesMax` do catálogo —
  não dá pra escolher "Resistente x5" hoje, a lista só mostra até 3.
- **`ArmaNatural.escalavel` já modela corretamente a diferença Garra vs
  Mordida/Chifre com Artista Marcial/Brigão** (regra do livro: só armas
  de impacto tipo garra escalam). Conferido dado real: Centauros "Cascos"
  → `escalavel: true` (o próprio texto do traço já cita Artista Marcial);
  Minotauros/Infernais "Chifres", Drakens "Cabeça Dura", Insetoides
  "Mordida" → `escalavel: false`. Consumo genérico em `CriadorState.kt:
  2308` (`if (arma.escalavel && (hasMartialArtist || hasBrawler))`), sem
  checar nome de raça. Nada a corrigir aqui.
- **Toque Venenoso já tem a fórmula de composição documentada em
  comentário**, mas implementada como 10 ids fixos (produto cartesiano de
  4 severidades × 2 entregas) em vez de 2 escolhas combináveis: base
  Moderado(1)/Nocauteador(+1)/Paralisante(+2)/Letal(+3) + entrega Corpo a
  Corpo(+0)/Cuspir(+2) — Letal+Cuspidor fecha nos 6 pontos que o dono do
  projeto lembrava. Confirma o pedido: "escolher o grau de efeito, depois
  escolher se cospe à distância" — vira a Fase 32 do plano (grupos de
  escolha com custo).
- **Garra tem custo-base diferente de Mordida pro mesmo dano/PA** (Garra
  2 pts, Mordida 1 pt) — não é a mesma tabela reaproveitada com nome
  diferente; cada família de arma natural (Garra/Mordida/Chifre) tem sua
  própria tabela-base, confirmada contra o texto de cada uma antes de
  generalizar (não assumida).
- **Elementais (Sci-Fi) tinha hardcode real, confirmado com precisão.**
  `CriadorState.applyAncestryVariantAdjustments()` tinha um
  `if (key == "ELEMENTAIS" && variant != "Padrão")` construindo
  `RacialAbility` na mão pra trocar Muito Forte+Resistência por Forma de
  Energia — enquanto `AncestryVariantRegistry.elementaisScifi()` já
  existia com os 2 pacotes (`padrao`/`ar_fogo_ou_agua`) só que **vazios**
  de propósito, com comentário admitindo que não eram lidos. Descendente
  Elemental (Fantasia), o caso irmão, já fazia certo — pacotes preenchidos
  de verdade, resolvidos pelo caminho genérico.

### Implementado nesta rodada: Elementais (Sci-Fi) migrado pro registro

- **`AncestryVariantRegistry.elementaisScifi()`**: pacote `ar_fogo_ou_agua`
  preenchido com `tracosParaRemoverPorNome = listOf("MUITO FORTE",
  "RESISTÊNCIA +2")` e `tracosParaAdicionar` com Forma de Energia +
  "Ajuste de Orçamento (Forma de Energia)" (fecha os 2 pontos que faltam
  entre remover 6 e ganhar só 4 de Forma de Energia — mesmo valor de
  sempre, só que agora dado, não construído na hora).
- **`TraitAddition`** (`AncestryVariantSystem.kt`) ganhou dois campos
  opcionais, com default preservando 100% do comportamento anterior pras
  outras ~20 raças que já usam essa classe: `pontos: Int = 0` (override
  de custo pra traço de bookkeeping puro, sem entrada em
  RacialTraitPointCatalog) e `invisivel: Boolean = false`.
- **`CriadorState.kt`**: o `if (key == "ELEMENTAIS" && variant !=
  "Padrão")` virou um bloco que chama
  `resolveAncestryVariantPackageUseCase.resolve(ancestralidadeId=
  "ELEMENTAIS", livro="SCI_FI", ...)` — mesmo motor único já usado por
  `scifiVariantDrivenKeys` (Drakens e outras 18 raças) — e aplica
  `tracosParaRemoverPorNome`/`tracosParaAdicionar` do pacote resolvido.
  Zero `RacialAbility` construída na mão; a condição que sobra
  (`key == "ELEMENTAIS"`) só decide QUANDO ler o pacote "ar_fogo_ou_agua"
  em vez do "padrao", nunca QUAIS traços trocam.

### Verificação

2 testes novos (`CriadorStateElementaisVariantTest`): Padrão mantém Muito
Forte/Resistência sem Forma de Energia; "Ar, Fogo ou Água" troca os dois
por Forma de Energia (visível) + Ajuste de Orçamento (invisível, 2 pts,
`resolvedPontos()` confirmado). Os 19 testes já existentes de
`ScifiAncestryVariantSyncTest` (Drakens, Centaux, Aquarianos, Ferais,
Mímicos, Avianos, Umvee, Elementais Padrão etc.) continuam passando sem
nenhuma mudança — zero regressão nas 18 outras raças que passam pelo
mesmo motor. `scripts/phase6_reliability_gate.sh` passou.

### Plano completo (fases seguintes, não implementadas ainda)

Documento de trabalho combinado com o dono do projeto ao vivo no chat —
resumo das fases pendentes: (29) mesma varredura de hardcode-por-nome
pra Umvee/Meio-Demônio; (30-31) migrar ids de Atributo/Perícia Aumentada
(`RESISTENTE`/`AGIL`/`FORTE`/`MUITO_*` etc.) pro par genérico
`ATTRIBUTE_BOOST`/`SKILL_BOOST`+`targetRef`+`value`, Básico primeiro
depois Fantasia — cuidado identificado: `RacialCaracteristicasResolver`
tem 2 loops que resolvem efeito por caminhos diferentes
(`resolvedTraitId()` vs `hab.id` direto), migrar sem marcar a entrada
`invisivel=true` (convenção já usada por toda entrada sintética
`ATTRIBUTE_BOOST` existente) duplicaria a linha em "Características";
(32) desenhar `RacialTraitEffect`-like "grupos de escolha com custo" pra
Toque Venenoso (fórmula já mapeada: severidade + entrega); (33) migrar
Garras/Mordida/Chifre pra esse sistema, respeitando que cada família tem
tabela-base própria (não confirmada ainda: Chifres pode ter uma terceira
tabela diferente de Garra/Mordida); (34) auditoria completa do sistema de
Variantes, listando toda raça que ainda usa exceção nomeada tipo a lista
em `CriadorState.kt:807` (`!key.contains("UMVEE") && ... && key !=
"ELEMENTAIS" && key != "DRAKENS"...`).

## Vigésima nona rodada — `ArmaNatural.escalavel` deixa de ser booleano solto

Revisão do dono do projeto na Rodada 28: "Garra escala com Artista
Marcial/Brigão, Chifre não" estava certo como REGRA, mas errado como
IMPLEMENTAÇÃO — `escalavel` era um campo `Boolean` livre, setado por
instância em cada `armasNaturais[]` de `ancestralidades.json`/
`horror_monstros.json` (28 + 3 ocorrências). Nada garantia que o valor
batesse com o tipo real da arma — Centauros "Cascos" tinha
`escalavel: true` vinculado à RAÇA, quando deveria vir do id `GARRAS_
SEM_PA` (a mesma família de Garras de qualquer outra raça), exatamente
como o dono do projeto apontou: "nada que vincule na raça... é o traço
que tem que definir".

Conferido antes de mexer: as 28+3 ocorrências já estavam 100%
consistentes por id (`GARRAS*` sempre `true`, `CHIFRES*`/`MORDIDA*`/
`CANINOS` sempre `false`) — não havia bug de dado hoje, só risco
estrutural (nada impede uma raça nova errar o valor).

Implementado:

- **`ArmaNatural`** (`MonstroTemplate.kt`) perdeu o parâmetro `escalavel:
  Boolean` do construtor — ganhou `id: String?` (o id do traço/Vantagem
  que concedeu a arma) e `escalavel` virou uma `val` computada:
  `get() = RacialTraitPointCatalog.armaNaturalEscalavel(id)`. Sem campo
  pra setar, não tem como setar errado.
- **`RacialTraitPointCatalog.armaNaturalEscalavel(id)`** (novo): única
  fonte de verdade — família `GARRAS`/`GARRAS_SEM_PA`/`GARRAS_MAIORES`/
  `GARRAS_MAIORES_SEM_PA` = escalável; qualquer outro id (ou `null`) =
  não escalável.
- **`ancestralidades.json`/`horror_monstros.json`**: removidas as 28+3
  ocorrências de `"escalavel": true/false` de dentro de `armasNaturais[]`
  (script Python com regex, validado por `json.loads()` antes/depois de
  salvar — diff conferido linha a linha, só as linhas de `escalavel`
  saíram). Como os objetos `armasNaturais[]` do JSON não repetem o id do
  traço (ele mora só no objeto pai), `CriadorState.extrairArmasNaturais()`
  ganhou um fallback nos dois pontos que leem `hab.armasNaturais`
  (raça e Monstro Heroico): `arma.copy(id = arma.id ?: hab.id)`.
- Os 3 `ArmaNatural(...)` construídos direto em Kotlin (Umvee "Ápice",
  Insetoides "Padrão" via `AncestryVariantRegistry`, Insetoides "Vespa")
  trocaram `escalavel = true/false` por `id = "GARRAS_SEM_PA"`/`"GARRAS"`/
  nada (Ferrão não é família Garra, comportamento idêntico ao de antes).

### Verificação

5 testes novos: `armaNaturalEscalavel()` cobrindo Garra/Chifre/Mordida/
null; Cascos do Centauro (id `GARRAS_SEM_PA`, sem nenhum booleano
declarado na raça) escalam de For+d4 pra For+d6 com Artista Marcial;
Chifres do Minotauro (id `CHIFRES_MAIORES`) permanecem For+d6 mesmo com
Artista Marcial. Os 3 testes de `CriadorStateArmaDeSoproTest` (rodada
anterior a esta, também passa por `extrairArmasNaturais()`) continuam
passando sem nenhuma mudança — confirma que o fallback `hab.id` não
quebrou a Arma de Sopro dos Draconianos. `scripts/phase6_reliability_gate
.sh` passou. Os dois JSONs re-parseados com sucesso depois da edição.

## Trigésima rodada — CI vermelho: 2 testes presos no comportamento antigo dos Elementais

O CI do commit da Rodada 28 (Elementais) deu `build failure` de verdade —
não achei isso no harness local porque só rodei `ScifiAncestryVariantSyncTest`
(o arquivo que eu já conhecia); não procurei por TODOS os arquivos de
teste que mencionam "ELEMENTAIS" antes de considerar a rodada fechada.
Lição: daqui pra frente, `grep -rl` pelo nome da raça em `app/src/test`
inteiro antes de fechar qualquer rodada que mexa em resolução de raça.

Os 2 testes que quebraram (`ResolveAncestrySpecificAdjustmentsUseCaseTest`
e `ResolveAncestryVariantPackageUseCaseTest`) tinham nome e comentário
dizendo explicitamente "elementais... nao injeta traco por aqui" —
fixavam o comportamento ANTIGO (pacotes vazios de propósito) que a
Rodada 28 corrigiu por decisão do dono do projeto. Não é regressão: a
falha confirma que o pacote passou a alimentar `ensureAutomaticAdvantages`
de verdade, exatamente como pretendido.

Investigado antes de mexer nos testes: existe um SEGUNDO caminho, já
existente antes da Rodada 28 e não tocado por ela, que também lê
`AncestryVariantRegistry` — `ResolveAncestrySpecificAdjustmentsUseCase
.buildResultFromVariantRegistry()` (usado pelas 19 raças de
`scifiVariantDrivenKeys`, ex. Drakens) monta `ensureAutomaticAdvantages
= resolved.vantagensGratisParaAdicionar + resolved.tracosParaAdicionar`
— **exatamente o mesmo padrão** que o bloco dedicado dos Elementais já
usava (linha ~270 do mesmo arquivo, escrito numa rodada anterior a
esta auditoria, preparado esperando o registro ser populado). Confirmei
que isso não é duplicação: `ensureAutomaticAdvantages`/`racialTraitIds`
alimenta `ModifierEngine` direto (efeito mecânico numérico), enquanto
`CriadorState.applyAncestryVariantAdjustments` mutando `habilidades[]`
alimenta a exibição (Resumo/PDF/Ver Detalhes/Modo Auditoria) — os dois
caminhos já coexistem pras 19 raças de `scifiVariantDrivenKeys` sem
problema (os 19 testes de `ScifiAncestryVariantSyncTest` continuam
passando), Elementais só passou a seguir o mesmo padrão.

Atualizados os 2 testes pra afirmar o valor novo e correto
(`[TraitAddition(Forma de Energia, FORMA_DE_ENERGIA), TraitAddition
(Ajuste de Orçamento..., AJUSTE_FORMA_DE_ENERGIA, pontos=2,
invisivel=true)]`), com comentário explicando a ligação com o padrão
de `scifiVariantDrivenKeys`. Nenhuma mudança de código de produção
nesta rodada — só os 2 testes.

### Verificação

Os 2 arquivos de teste inteiros rodados no harness (45 testes no total
entre os dois, não só os 2 que mudaram) — todos passando.
`grep -rl "ELEMENTAIS"` em todo `app/src/test` confirma que não sobra
nenhum outro teste pendente sobre a raça. `scripts/phase6_reliability_gate
.sh` passou.

## Trigésima primeira rodada

Rodada de verificação/investigação a pedido do usuário, em resposta ao
relatório de "wipe inteiro" de hardcode-por-nome entregue na rodada
anterior. O usuário corrigiu, caso a caso, o que realmente precisa de
ação — a maior parte do que eu tinha listado como "hardcode a
resolver" já está corretamente resolvido pelo sistema genérico; o
problema real estava em outro lugar. Registrado aqui item por item,
como pedido ("nem que a gente desenhe no documento a regra sendo
isso").

### Terracota (Arte da Guerra): confirmado, NÃO é como o Anão Ciber

Hipótese do usuário: Terracota escolheria entre "defeito negativo tipo
1 ou tipo 2", parecido com o catálogo orçado (`BUDGETED_CATALOG`) do
Anão Ciber. Conferido contra o dado real do livro embutido no app
(`ancestralidades.json`, entrada `anc_terracota_adg`): o campo
`"opcoes"` da raça é literalmente `["Voto (Maior)", "Obrigação
(Maior)"]` — duas Complicações Maiores nomeadas, não uma lista aberta
de traços negativos de menor porte. Isso bate exatamente com o que já
está implementado em `AncestryVariantRegistry.terracota()`: uma
`SelectionDef` única (`terracota_complicacao`) do tipo `FIXED_PACKAGE`
com 2 `FixedPackageOption` (voto/obrigacao), cada uma concedendo a
Complicação certa via `TraitAddition`. **Terracota já está correto —
não precisa de nenhuma mudança.** O único hardcode-por-nome que resta
aqui é o `if (ancKey.contains("TERRACOTA"))` de despacho em
`ResolveAncestrySpecificAdjustmentsUseCase` — mas isso é só "qual
registro buscar", mesmo padrão usado por Umvee/Elementais/Humanos
Fantasia (ver seção de Humanos abaixo).

### Akaimimi (Arte da Guerra): `forceArmorZero=true` não é especial, é o padrão

O usuário não lembrava a razão de o app zerar armadura natural do
Akaimimi. Conferido: `forceArmorZero=true` é o valor usado por
**quase toda raça** no `when`/cadeia de `if` de
`ResolveAncestrySpecificAdjustmentsUseCase` — Saurios, Golens,
Draconianos, Insetoides, Pequeninos, Celestiais, Descendente Elemental,
Transmorfos, Demônio (Abismo), o `else` genérico, e o bloco de
`buildResultFromVariantRegistry()` usado pelas 19 raças de
`scifiVariantDrivenKeys`, todos usam `forceArmorZero=true`. Não é um
comportamento específico do Akaimimi. E o JSON real do Akaimimi (`anc_
akaimimi`) confirma que a raça não tem nenhum traço de armadura entre
suas 4 habilidades (Bom Conselheiro, Dicas Culturais, Conhecimento
Geral, Visão no Escuro) — então `naturalArmorFromRace=0,
forceArmorZero=true` está correto: reseta qualquer armadura racial
"perdida" de uma raça anterior, e não define nenhuma nova porque
Akaimimi realmente não tem armadura natural no livro. **Nenhuma
mudança necessária.**

### DRAKENS na exceção de "candidato único": não era vestigial — o problema era o oposto

Pergunta do usuário: por que Drakens continua na lista de exceção do
curto-circuito de "candidato único" (`CriadorState.kt`, dentro de
`getAncestralidadeDef()`) se já é resolvido pelo caminho genérico?
Investigando a fundo: Drakens **precisa** dessa exceção — sem ela, com
apenas o compêndio Sci-Fi ativo (o caso normal, Drakens só existe nesse
livro), o candidato único faz o código sair antes de
`applyAncestryVariantAdjustments`, que é onde mora o bloco genérico
`if (key in AncestryVariantRegistry.scifiVariantDrivenKeys)` que lê o
`grupoVariante` do registro e troca FORTE por Arma de Sopro (Fogo) na
opção "Dragão". Cair fora antes disso deixa a troca de Variante sem
efeito na exibição.

Só que, ao investigar isso, apareceu um achado bem maior: **das 19
raças em `scifiVariantDrivenKeys`, só Drakens e Elementais* estavam na
lista de exceção — as outras ~12 raças exclusivas do Sci-Fi (sem
entrada em nenhum outro livro, então sempre "candidato único" no caso
normal de personagem) não estavam: Centaux, Ferais, Florans,
Gelatinoides, Mímicos, Mineradores Genéticos, Oráculos, Possessores,
Robôs, Seres Sintéticos, Soldados Genéticos, Yetis.** (*Elementais na
verdade usa um mecanismo próprio — `selecoes`/`FIXED_PACKAGE`, não
`grupoVariante` — resolvido por um bloco dedicado `key == "ELEMENTAIS"`
que fica fora do conjunto `scifiVariantDrivenKeys` de propósito.)

Conferido contra o registro: várias dessas raças têm troca de traço de
verdade até na opção "Padrão" (ex.: Centaux "Padrão" define
MOVIMENTAÇÃO vezes=1 pra poder virar vezes=2 em "Gazela"; Possessores
"Padrão" remove NOÇÃO DO PERIGO; Mineradores Genéticos "Padrão" injeta
FORTE que "Zero G" depois troca por Adaptação Gravitacional). Com o bug,
todas essas trocas silenciosamente não aconteciam na camada de exibição
(Resumo/PDF/Ver Detalhes) sempre que só o compêndio Sci-Fi estava
ativo — que é o caso normal de uso.

**Correção aplicada**: a lista de exceção deixou de enumerar nomes de
raça um por um e passou a checar pertencimento ao próprio registro
(`key in AncestryVariantRegistry.scifiVariantDrivenKeys`), a mesma
fonte de verdade que já era usada no bloco de aplicação genérica logo
abaixo — sem checagem de nome, sem `.keyify()` como "solução", só
verificação de que a raça está cadastrada no lote genérico. Como
Elfos/Humanos/Aquarianos/Avianos/Rakashanos/Insetoides também
pertencem a esse conjunto mas existem em vários livros (Básico,
Fantasia, etc.), a checagem ficou condicionada a `origem` ser
`SCI_FI`/`FC` (mesmo guard já usado logo abaixo, na branch de múltiplos
candidatos) — sem isso, um Elfo do Básico com candidato único caía por
engano na config Sci-Fi do registro e perdia o traço Ágil da raça base
(pego por `CriadorStateFullFlowTest` durante a verificação, corrigido
antes de fechar a rodada).

### Verificação

Rodei no harness (`/tmp/ktbig`) toda a bateria de testes que toca
`CriadorState`/resolução de raça — 19 arquivos, ~150 testes no total
(`ScifiAncestryVariantSyncTest` 19, `CriadorStateFullFlowTest` 3,
`CriadorStateElementaisVariantTest` 2, `CriadorStateArmaNaturalEscalavelTest`
3, `CriadorStateArmaDeSoproTest` 3, `CriadorStateDemonioArcanoTest` 4,
`CriadorStateKirinSignTest` 11, `CriadorStateRacialTraitDrivenAttributesTest`
11, `CriadorStateTransmorfosPoderTest` 2, `CriadorStateDiminutoEquipmentTest`
6, `CriadorStateDiminutoRaceSwitchTest` 3, `ModifierEngineAdgAncestryTest`
4, `ModifierEngineCidadeSolVaporTest` 1, `Phase0CriticalFlowsTest` 5,
`ResolveAncestrySpecificAdjustmentsUseCaseTest` 33,
`ResolveAncestryVariantPackageUseCaseTest` 12, `SummaryUtilsTest` 17,
`RacialTraitAuditFormatterTest` 8, `ValidateRequirementsUseCaseTagsTest`
5, `EquipamentoFormattersDiminutoTest` 5) — todos passando depois do
ajuste do guard de origem. `scripts/phase6_reliability_gate.sh` passou
(só o WARN pré-existente de tamanho de arquivo).

### Humanos "Pacotes Culturais" (Fantasia): já está certo — o relatório da rodada anterior citou o lugar errado

Reconferido o código: `CriadorState.applyAncestryVariantAdjustments()`,
bloco `canonicalOriginKey(base.origem) == "FANTASIA" && key.contains
("HUMANO")`, já **não** constrói os traços de Povo do Mar/Senhores dos
Cavalos na mão — ele lê `AncestryVariantRegistry.get("HUMANOS",
"FANTASIA")?.grupoVariante` e resolve via
`resolveAncestryVariantPackageUseCase.resolve(...)`, mesmo mecanismo
genérico de Terracota/Umvee/Elementais (comentário no próprio código já
registra isso: "Variante de verdade... não mais um subsistema
dedicado"). O que eu tinha listado no wipe como "hardcode" era só a
condição de despacho `key.contains("HUMANO")` — que decide *qual*
entrada do registro buscar, não como resolver os traços. Esse mesmo
formato de despacho por nome (`if (ancKey.contains("RAÇA"))` levando a
uma leitura genérica do registro) se repete em todo canto — Terracota,
Umvee, Elementais, Quadroides, Humanos Sci-Fi Minerador — não é
exclusivo de Humanos e não é o problema que o usuário apontou.
**Nenhuma mudança de mecânica necessária aqui**; o único item pendente
de verdade é a ideia, ainda maior, de trocar esses despachos por nome
por uma busca direto no registro por `(id, livro)" — ver seção "Próximos
passos" abaixo.

### Descoberta: o "traço de alvo escolhido pelo jogador" já existe, genérico, para Força/Vigor/Agilidade

Investigando o pedido do usuário de generalizar Meio-Orc (Força-ou-
Vigor à escolha) usando o mesmo desenho do Signo do Humano Arte da
Guerra, encontrei que **esse mecanismo genérico já existe e já
funciona** — só não documentado como tal. Em `CriadorState`
(atributoBaseRacial, por volta da linha 5402): qualquer raça cujo
`habilidades[]` contenha um dos ids `ENDURECIDO`, `PRIMITIVO` ou
`MINERADOR_ATRIBUTO` ativa um bloco que lê um state compartilhado
(`humanoMineradorAtributo`) pra saber qual atributo (Força/Vigor, ou
Força/Vigor/Agilidade se o traço for `PRIMITIVO`) o jogador escolheu, e
aplica o dado d6 no atributo certo. Esse único mecanismo já cobre **3
raças de livros diferentes**: Meio-Orc (Fantasia, id `ENDURECIDO`),
Feral (Arte da Guerra, id `PRIMITIVO`), Humano "Minerador" (Sci-Fi, id
`MINERADOR_ATRIBUTO`) — disparado por id do traço, não por nome de
raça, com uma validação extra pra evitar que uma escolha "Agilidade"
sobreviva à troca de Feral pra Meio-Orc/Minerador (que não têm essa
opção).

Dois pontos a corrigir, de baixo risco, quando essa frente for
retomada: (1) o nome do campo de state (`humanoMineradorAtributo`) é
enganoso — sugere que é exclusivo do Humano Minerador, mas é
compartilhado pelas 3 raças; merece um nome genérico (ex.:
`atributoEscolhidoRaca`). (2) `isFeralAdgSelecionado()` (usado pela UI
pra decidir quando mostrar o seletor de 3 opções) checa por nome
(`ancestralidade.keyify() == "FERAL"`) em vez de checar a presença do
traço `PRIMITIVO` em `habilidades[]`, que seria consistente com o resto
do mecanismo. Nenhuma das duas é um bug — são só limpeza de nome/
consistência, adiadas pra não misturar com a investigação desta rodada.

Esse achado é a base concreta pra atender o pedido do usuário de
generalizar Herança (Meio-Elfo), a troca Adaptável/Antecedente Arcano
(Meio-Demônio) e os Signos de Nascença (Humano Arte da Guerra) num
único mecanismo "seleção que vive na raça" — ver "Próximos passos".

### Monstro Heroico (Horror) e Tropos (Arte da Guerra): já são "templates" separados de raça, não hardcode a corrigir

O usuário havia levantado a hipótese de que eu estivesse confundindo
Monstro Heroico com raça/variante, e pediu pra conferir a definição no
livro de Horror. Não há PDF dos livros neste repositório — a fonte de
verdade disponível é o próprio dado que o app já usa
(`horror_monstros.json`), e ele confirma exatamente o que o usuário
descreveu: cada monstro (Anjo, Demônio, Fantasma, Lobisomem, Monstro de
Retalhos, Múmia, Revivido, Vampiro) só carrega `atributos_bonus` (passos
extras sobre os atributos que a raça de base já tem) e `habilidades`/
`complicacoes` adicionais — nenhuma entrada tem `categoria`,
`pontosRaciaisEsperados`, `livros` ou `especieId`, os campos que toda
raça de `ancestralidades.json` tem. Ou seja, o próprio schema já marca
Monstro Heroico como algo estruturalmente diferente de raça — não tem
orçamento racial próprio nem é selecionável como ancestralidade.

E, no código, essa separação já está implementada corretamente:
`tipoMonstroSelecionado` é um campo de state **separado** de
`ancestralidade` (`CriadorState.kt`, ~579) — o jogador escolhe uma
ancestralidade normalmente e, se `modoMonstroAtivo`, escolhe também um
`MonstroTemplate` por cima. O mesmo padrão já existe para Tropos:
`tropoSelecionado` também é um campo separado de `ancestralidade`, com
alguns comportamentos específicos por id do tropo (`tropo_protagonista`,
`tropo_samurai`, `tropo_elementalista` — checados por id, não por nome,
já seguindo a regra que o usuário pediu). **A arquitetura de "template
aplicado sobre uma raça" já existe e já é usada por Monstro Heroico e
Tropo** — não é um hardcode a eliminar.

O único item pendente de fato nessa área (baixa prioridade, mesmo
balde do "Fases 30-31" de Atributo/Perícia Aumentada genérico já
registrado em rodadas anteriores): `monstroAtributoTraitIds()`
(`CriadorState.kt`, ~5337) traduz `atributosBonus` do Monstro pra ids
fixos por combinação (`"FORCA" -> if (passos>=2) "MUITO_FORTE" else
"FORTE"`) — não é hardcode de nome de raça/monstro, é o mesmo padrão de
"um id por combinação de atributo+intensidade" que já existe em outros
lugares do catálogo (`RacialTraitPointCatalog.EFEITOS`) e no criador de
raça customizada. Fica registrado como candidato a generalizar junto
com a migração de Atributo/Perícia Aumentada, não como algo urgente.

A ideia do usuário de unificar Monstro Heroico e Tropo num sistema
único de "Template" é uma proposta de arquitetura válida (os dois já
têm a mesma forma: um id selecionado à parte da raça, que injeta
bônus/traços por cima) — mas, como os dois já funcionam corretamente e
já são id-driven, isso é uma refatoração de organização de código, não
uma correção de bug. Registrado como possível trabalho futuro, não
teve prioridade nesta rodada.

### Próximos passos (registrados, não implementados nesta rodada)

1. **Generalizar a "seleção que vive na raça"**: usar o mesmo desenho
   já validado pra Meio-Orc/Feral/Minerador (id do traço decide que a
   raça tem escolha; a escolha em si mora num campo de state genérico)
   para: Signos de Nascença do Humano Arte da Guerra (13 opções + "sem
   Signo" — e, junto disso, consertar o bug real já identificado em
   `totalSpPool`, que hoje faz `ancestralidade.keyify().contains
   ("HUMANO")` pra conceder os +3 pontos de perícia do "sem Signo" em
   vez de ler o `PERICIA_POINTS_BONUS` genérico que já existe pra
   qualquer raça nova), Herança do Meio-Elfo (Atributo Aumentado ou
   Adaptável), e a troca Adaptável/Antecedente Arcano (Demônio) do
   Meio-Demônio. Provavelmente é onde `SelectionType.TARGET_ATTRIBUTE_
   OR_SKILL` (hoje sem nenhuma raça usando) finalmente ganha uma
   implementação real.
2. **Renomear `humanoMineradorAtributo`** pra um nome que não sugira
   exclusividade do Humano Minerador (ex.: `atributoEscolhidoRaca`), e
   trocar `isFeralAdgSelecionado()` por uma checagem do traço
   `PRIMITIVO` em vez do nome da raça.
3. **Risco de colisão de id entre livros** (raça com o mesmo nome em
   livros diferentes, ex.: Humano em Básico/Fantasia/Sci-Fi/Horror,
   Elfo do Pathfinder com mecânica diferente dos outros Elfos): o
   registro já resolve isso na prática via chave composta
   `(ancestralidadeId, livro)` em `AncestryVariantRegistry.get()` — não
   por `.keyify()` de nome. O que falta é levar essa mesma composição
   pros pontos de despacho em `CriadorState`/`ResolveAncestrySpecific
   AdjustmentsUseCase`, que hoje decidem "qual raça é essa" por um
   `if (key.contains("NOME"))` e só depois buscam no registro pelo id —
   nenhuma colisão real encontrada até agora (os `if` são checados em
   sequência com `return` antecipado), mas a estrutura ficaria mais
   robusta buscando direto por `(id, livro ativo)` em vez de por
   substring de nome. Não implementado nesta rodada — é uma
   refatoração maior que toca todos os despachos de uma vez, arriscada
   demais pra fazer sem checar raça por raça.
4. Migração de Atributo/Perícia Aumentada pra id genérico
   (`ATTRIBUTE_BOOST`/`targetRef`/`value`) — já registrada em rodadas
   anteriores, ainda pendente. `monstroAtributoTraitIds()` do Monstro
   Heroico entra no mesmo balde.

### Adendo (mesmo dia): `forceArmorZero` removido — era campo morto

Pergunta do usuário, direto sobre o achado do Akaimimi: já que toda
raça começa com armadura zero, por que existiria um campo pra "forçar"
esse zero? Boa pergunta — a resposta estava num comentário já presente
no próprio `CriadorState.kt` (linha ~5931, escrito numa rodada
anterior): `forceArmorZero` resetava um `armadura` mutável que existia
antes de uma refatoração anterior, mas ninguém nunca escrevia outro
valor nele (sempre 0), então o reset já era um no-op *para todas as
raças*, não só Akaimimi. Depois que `armadura` virou uma propriedade
computada a partir de `equipamentosComprados`, não sobrou nem estado
pra resetar. Confirmei que o campo é lido e repassado entre dois
`Result` (`ResolveAncestrySpecificAdjustmentsUseCase` →
`ResolveAncestryRacialPackageUseCase`) e teve valor testado em ~8
asserts, mas **nunca é lido em nenhum lugar depois disso** — puramente
morto.

Removido por completo (campo do `data class Result` nas duas classes,
as 21 atribuições `forceArmorZero = true/false,` em
`ResolveAncestrySpecificAdjustmentsUseCase`, a atribuição em
`ResolveAncestryRacialPackageUseCase`, o comentário morto em
`CriadorState.kt`, e os 8 asserts nos dois arquivos de teste que só
verificavam esse valor sem checar nada mais). `naturalArmorFromRace`
sozinho já é a fonte de verdade de armadura racial. Nenhuma mudança de
comportamento — só remoção de estado sem consumidor. 152 testes
rodados no harness (mesma bateria da seção anterior + os 2 arquivos de
`usecase` completos) — todos passando. `scripts/phase6_reliability_gate.sh`
passou.

## Trigésima segunda rodada

Continuação direta da Trigésima primeira: terminar a generalização de
Signo (Humano Arte da Guerra) + Herança (Meio-Elfo) + Meio-Demônio antes
de partir para o desenho do sistema de Template (Monstro Heroico/Tropo),
por pedido explícito do usuário.

### Descoberta inicial: o "mecanismo genérico" de Signo/Herança já existe — faltava só terminar de aplicá-lo

Antes de desenhar algo novo, mapeei o que já está correto:

- A maior parte da mecânica de Signo (Boi/Força, Dragão/Espírito, Macaco/
  Astúcia, Urso/Vigor, Lebre/Cura, Garça, Serpente) já compara por `signId`
  (o id do Signo, via `signoIdFromNome()`), não por nome de raça.
- Herança (Meio-Elfo) e a escolha do Meio-Demônio já são id-driven no ponto
  de decisão principal (`base.habilidades.any { it.id == "HERANCA" }`).
- O que sobrava de hardcode-por-nome era a **guarda** ao redor desses
  blocos — `ancestralidade.keyify().contains("HUMANO")` repetido em 7
  lugares diferentes de `CriadorState.kt` (mais 2 na UI), decidindo *se*
  o bloco de Signo deveria rodar, não *qual* Signo aplicar.

Achei o marcador certo já pronto no próprio JSON do livro: Humano
(Império San) carrega, sempre, um traço `ADAPTAVEL_OU_SIGNO` ("Você pode
optar por não escolher um signo de nascença... começar como um humano
padrão com Adaptável") — e `temAdaptavel()` já usava esse id pra decidir
se a raça tem Adaptável disponível. Bastou reaproveitar esse mesmo id
como guarda em todo o resto do mecanismo, em vez de reinventar algo.

### O que foi corrigido

**Guardas de Signo (Humano Arte da Guerra)** — trocado
`ancestralidade.keyify().contains("HUMANO")`/`ancKey.contains("HUMANO")`
por `habilidades.any { it.id?.keyify() == "ADAPTAVEL_OU_SIGNO" }` em:
`totalSpPool` (o bug real de verdade: os +3 pontos de perícia do Signo
"Nenhum"), `reservaChi` (bônus de Chi do Kirin), as duas cópias de
`kirinSorteAutomatica` (`isVantagemAutomatica`/`podeRemoverVantagem`),
o loop de perícia (`periciaStartRawInternal`) e o de atributo
(`atributoBaseRacial`), e o gate do picker de Signo na UI
(`AncestralidadesSection.kt`).

Sobre o `totalSpPool`: investiguei se dava pra ir além e religar o
próprio traço `PONTOS_DE_PERICIA` ao mecanismo genérico
`bonusPontosPericia`/`PericiaPoolBonus` (que já existe e já é usado por
outras raças) — mas descobri que isso quebraria a contabilidade de
pontos da raça: `PONTOS_DE_PERICIA` (custo 1), `ADAPTAVEL_OU_SIGNO`
(custo 2) e `SIGNOS_DE_NASCENCA` (custo 0, "placeholder de Seleção")
somam juntos os 3 pontos que `pontosRaciaisEsperados=3` espera, e ficam
**sempre presentes** em habilidades[] independente do Signo escolhido —
é assim que o livro calibra o valor da raça, não uma coisa que deveria
oscilar por escolha do jogador. Se eu removesse/reinjetasse esse traço
dinamicamente (como faço com HERANCA), o "valor de livro" da raça usado
pelo editor de Variante custom (`ResolveVariantPointBudgetUseCase.
valorTotalDe`) oscilaria entre 2 e 3 pontos dependendo do Signo ativo —
uma inconsistência nova, pior que a que eu estaria corrigindo. Por isso
o cálculo do bônus de +3 continua sendo uma conta à parte de
`bonusPontosPericia` (não duplica — cada um cobre um caso diferente),
só que agora decidida por id do traço, não por nome da raça.

**Meio-Demônio (Cidade do Sol a Vapor)**: essa raça não tinha nenhum
traço-marcador próprio — a JSON só carregava `"id": "ADAPTAVEL"`, o
mesmo id genérico usado por dezenas de outras raças, sem como distinguir
"Meio-Demônio precisa da escolha AA/Adaptável" de "qualquer outra raça
com Adaptável simples". Segui o mesmo padrão já usado pelo livro pra
Humano ADG: troquei o id desse traço, só para Meio-Demônio, para
`ADAPTAVEL_OU_ANTECEDENTE_ARCANO_DEMONIO` (nome de exibição continua
"Adaptável" — só o id interno mudou) e cadastrei o custo (2, igual
Adaptável) em `RacialTraitPointCatalog.CUSTOS`. Com o marcador
existindo, troquei por id em: o despacho de
`applyAncestryVariantAdjustments` (que resolve a escolha de verdade),
a exceção de "candidato único" e o gatilho `withVariant` em
`getAncestralidadeDef()` (novo helper `temEscolhaMeioDemonio()`, mesmo
padrão do já existente `ehMeioElfoComHeranca()`), o picker da UI em
`AncestralidadesSection.kt`, e o ajuste de exibição de custo de Poder em
`PoderesSection.kt`. `ValidateScenarioRulesUseCase` (regra que bloqueia
comprar manualmente o Antecedente Arcano de sangue puro) ficou de fora
de propósito — essa função só recebe o nome da raça como `String`, não a
`RacialModifier` inteira, e mudar a assinatura pra carregar habilidades[]
seria um refactor maior do que o escopo desta rodada pede.

**Limpeza correlata**: `ehMeioElfoComHeranca()` tinha uma checagem de
nome (`key.contains("MEIO-ELFOS")`) redundante com a checagem do traço
"HERANCA" — conferido contra `ancestralidades.json` que esse id nunca
aparece em nenhuma raça além de Meio-Elfo (Básico/Fantasia/Horror/
Super), então a checagem de nome nunca mudava o resultado. Removida.

### Por que isso importa de verdade (não é só estética)

Achei um caso onde a checagem por nome já tinha ficado **desatualizada**
por conta própria: `atributoMaxRawNaCriacao()` (traço `MENTE_PRIMITIVA`
do Feral) já é checado por id, do lado dele, há algum tempo — prova de
que o padrão "id, não nome" já era seguido em partes do arquivo antes
desta rodada; só faltava terminar de aplicá-lo no que sobrou de Signo/
Meio-Demônio.

### Verificação

Dois testes precisavam de ajuste — não porque o comportamento mudou,
mas porque construíam o `CriadorState` sem passar pela ancestralidade
mockada de verdade (só setavam `ancestralidade = "HUMANOS"` como string
solta, sem `habilidades[]`), então o novo guard por id não tinha o que
checar:

- `CriadorStateKirinSignTest.kt` ("kirin trata sorte como vantagem
  automatica do signo") — passou a montar um `RacialModifier` com
  `ADAPTAVEL_OU_SIGNO`, igual ao teste "signo nenhum" que já existia no
  mesmo arquivo (mesmo padrão, só replicado pro primeiro teste).
- `CriadorStateMeioDemonioTest.kt` (5 testes) — o mock `meioDemonio()`
  trocou `id = "ADAPTAVEL"` por `id = "ADAPTAVEL_OU_ANTECEDENTE_ARCANO_
  DEMONIO"`, batendo com a mudança real no JSON.

`grep -rl` em `app/src/test` por `signoAdgSelecionado`, `MEIO-DEMONIO`,
`meioDemonioAA`, `totalSpPool` e `reservaChi` confirmou não sobrar mais
nenhum outro teste dependente do comportamento antigo (os que sobraram
— `ApplyAncestryChangeCoordinatorUseCaseTest`, `RebuildSkillStacksUseCaseTest`
— não instanciam `CriadorState`, então não são afetados).

Rodei no harness (`/tmp/ktbig`) os 22 arquivos de teste relevantes —
157 testes no total — todos passando, incluindo os dois recém-corrigidos.
`scripts/phase6_reliability_gate.sh` passou (só o WARN pré-existente de
tamanho de arquivo). As duas mudanças de UI (`AncestralidadesSection.kt`,
`PoderesSection.kt`) não são compiláveis no harness (Compose) — validadas
por leitura + balanceamento de chaves/parênteses, e ficam pro CI confirmar
a compilação de verdade.

### O que ficou de fora desta rodada (registrado, não esquecido)

- **Umvee** ("Dons da Natureza") continua com despacho por nome
  (`key.contains("UMVEE")`) — não tem traço-marcador equivalente a
  `ADAPTAVEL_OU_SIGNO`/`HERANCA` hoje. Mesmo padrão de generalização
  poderia se aplicar, não foi pedido nesta rodada.
- **`ValidateScenarioRulesUseCase`** (bloqueio de compra manual do AA de
  sangue puro por Meio-Demônio) continua por nome — `Input` só carrega
  `ancestralidade: String`, não a raça resolvida inteira.
- A migração de Atributo/Perícia Aumentada pra id genérico
  (`ATTRIBUTE_BOOST`) e a unificação Monstro Heroico/Tropo continuam
  pendentes das rodadas anteriores.

## Trigésima terceira rodada (planejamento) — desenho de "opções validadas" + Variante por opção

Discussão com o usuário sobre por que religar `PONTOS_DE_PERICIA` ao
mecanismo genérico de bônus quebraria a contabilidade de pontos do
Humano (Império San) (ver rodada anterior) levou a um redesenho maior,
puxado pelo próprio usuário. Registrado aqui antes de começar a
implementar, como pedido.

### O problema de fundo

O sistema de Seleção (`AncestryVariantConfig`/`SelectionDef`/
`VariantOption`) já sabe modelar "a raça oferece um menu de opções,
cada uma com seu pacote de traços" — é o que já funciona pra Terracota
(Voto/Obrigação), Umvee (6 Dons da Natureza), Elementais (elemento),
Quadroides (Habilidoso). O que **não existe** é validação por opção: o
único cálculo de orçamento (`ResolveVariantPointBudgetUseCase`) soma
`habilidades[]` como uma lista plana e permanente, sem noção de "isso é
o pacote da opção A" vs "isso é o pacote da opção B". É por isso que
tentar religar `PONTOS_DE_PERICIA` ao mecanismo genérico quebrava: o
traço ficava sempre presente na lista plana, então a soma nunca refletia
"só a opção ativa".

### O desenho acordado (3 peças, nessa ordem)

1. **Validador por opção**: para cada opção (`VariantOption`/
   `FixedPackageOption`) de uma raça, resolver o pacote completo daquela
   opção (traços comuns da raça, se houver, + o que a opção
   adiciona/remove) e comparar a soma contra `pontosRaciaisEsperados` —
   reaproveitando `ResolveVariantPointBudgetUseCase.resolve()`, que já
   faz exatamente essa conta pro editor de Variante custom, só que
   alimentado pelos itens de CADA opção em vez das escolhas manuais de
   um mestre. Se uma opção específica não fechar, o aviso aponta só
   ela — nunca a raça inteira.

2. **Migrar Signo (Humano Arte da Guerra), Herança (Meio-Elfo) e a
   escolha do Meio-Demônio pro sistema de Seleção de verdade**, com um
   conceito único de "id da opção ativa agora" (hoje cada um guarda a
   escolha do jogador do seu próprio jeito — `signoAdgSelecionado`,
   `meioElfoAgil`, `meioDemonioAA` — sem um formato comum). Depois da
   migração, `PONTOS_DE_PERICIA` (custo 1, valor fixo +3 — ver nota
   abaixo) só existe dentro do pacote da opção "Nenhum", nunca nas
   outras 13; o leitor genérico de bônus de perícia passa a funcionar
   sem precisar de nenhuma guarda por id de "a raça tem a escolha",
   porque o traço simplesmente não está presente fora da opção certa.
   Fecha o histórico da Sigla usando o mesmo motor que já resolve
   Terracota/Umvee/Elementais, eliminando os `if (signId == "X")`
   espalhados em `CriadorState.kt` que hoje ainda constroem o efeito na
   mão em vez de ler do registro.

   Nota sobre o traço de +3: confirmado com o usuário que ele **não**
   deve usar o mecanismo genérico parametrizável (`PERICIA_POINTS_
   BONUS` + `value` livre, pensado pro criador de conteúdo escolher
   qualquer valor) — o +3 é uma regra fixa do livro, não um parâmetro.
   Continua como um id próprio e fixo (mesmo efeito por baixo,
   `PericiaPoolBonus`, mas com o valor cravado no catálogo pelo id, não
   editável).

3. **Variante Customizada escopada por uma opção específica**: hoje uma
   Variante custom de raça (`CustomAncestryVariant`) enxerga a raça
   como uma coisa só — não tem como um mestre criar uma Variante que
   sobrescreve só o Signo Dragão, por exemplo, mantendo as outras 13
   opções oficiais. O desenho: a Variante custom ganha um campo
   opcional "id da opção que ela sobrescreve"; vazio = comportamento
   atual (aplica em cima da raça toda, como hoje); preenchido = só se
   aplica quando a opção ativa bate com esse id, e as demais opções
   continuam 100% oficiais. Só fica barato de construir depois da peça
   2, porque precisa de um jeito único de perguntar "qual é a opção
   ativa agora" que funcione pra qualquer raça de Seleção, não um
   mecanismo à parte por raça.

### Ordem de execução

1. Validador por opção (pequeno, testável isolado).
2. Migrar Herança (Meio-Elfo, 2 opções) e a escolha do Meio-Demônio
   (2 opções) pro formato de Seleção — mecânica de cada lado já está
   correta hoje, só falta empacotar como `SelectionDef`/`VariantOption`.
   Baixo risco, prova o desenho de ponta a ponta.
3. Migrar Signo (Humano Arte da Guerra, 14 opções — 13 signos + Nenhum)
   pro mesmo formato — trabalho grande, cada opção precisa ser
   conferida contra o texto do livro (já embutido em
   `ancestralidades.json`), comparável ou maior que a rodada dos
   Elementais (que teve só 2 opções).
4. Variante Customizada escopada por opção — depois que 1-3 estiverem
   testados e no ar.

Itens mais antigos do backlog (migração de Atributo/Perícia Aumentada
genérico, unificação Monstro Heroico/Tropo, robustecer o despacho por
`(id, livro)` em vez de substring de nome) continuam registrados nas
rodadas anteriores, sem prioridade nesta.

### Peça 2 (parcial): Meio-Elfo migrado pro registro; Meio-Demônio só cadastrado

Cadastrado `AncestryVariantRegistry.meioElfoHeranca(livro)` (4 entradas,
uma por livro — Básico/Fantasia/Horror/Super — todas geradas pela mesma
função, pra não deixar as cópias saírem de sincronismo) e
`AncestryVariantRegistry.meioDemonio()` (Cidade do Sol a Vapor).
Conferido contra `ancestralidades.json`: as 4 entradas de Meio-Elfo têm
`habilidades[]` idênticas (Forasteiro Menor -1, Herança 2, Visão no
Escuro 1 = 2, o orçamento padrão), então uma função só realmente serve
às quatro sem perder nada.

`CriadorState.applyAncestryVariantAdjustments()` migrado de verdade pro
Meio-Elfo: em vez de construir "Ágil"/"Adaptável" na mão, agora chama
`resolveAncestryVariantPackageUseCase.resolve()` com a resposta de
Seleção (`meio_elfo_heranca` → `agil` ou `adaptavel`), mesmo padrão já
usado por Terracota/Umvee/Elementais.

**Meio-Demônio ficou só cadastrado no registro, sem migrar a
aplicação** — decisão deliberada, não esquecimento. Investigando a
fundo descobri que o Antecedente Arcano dele é concedido de verdade por
um caminho diferente de todo o resto: `ApplyAncestryChangeCoordinatorUseCase`
lê `resolvedVantagensGratis()` direto de `habilidades[]` (o traço com
`traitId=GRANTED_EDGE` + `targetRef="aa_demonio_meio_demonio"`) pra
alimentar `ancestryGrantedAdvantages` — não existe nenhum bloco pra
Meio-Demônio em `ResolveAncestrySpecificAdjustmentsUseCase` (confirmado
por grep), então esse é o ÚNICO ponto que concede a Vantagem de
verdade. O laço genérico que já uso pra outras raças
(`vantagensGratisParaAdicionar` → `addIfAbsent(traco, "racial_edge")`)
não seta `targetRef`, só `id` — usar ele aqui trocaria silenciosamente
o alvo da concessão de "aa_demonio_meio_demonio" (o id real da
Vantagem) pra "ANTECEDENTE_ARCANO_DEMONIO_MEIO" (o id do traço, que não
existe no catálogo de Vantagens), quebrando a concessão real sem
nenhum teste acusar na hora — só na prática, com o jogador vendo a
Vantagem não aparecer. Preferi manter a construção manual, já correta
e coberta pelos 5 testes de `CriadorStateMeioDemonioTest`, e deixar só
o cadastro no registro (que já serve o validador) — migrar a aplicação
de verdade exigiria primeiro resolver esse descompasso entre
`vantagensGratisParaAdicionar`/`vantagensGratisIds` e o par
id+targetRef que `resolvedVantagensGratis()` espera, o que é maior que
o escopo desta rodada.

### Verificação

- Novo teste (`CriadorStateRacialTraitDrivenAttributesTest`): nenhum
  teste existente cobria de verdade o ramo `meioElfoAgil=true` — só o
  `false`/Adaptável. Fechado agora, confirmando Agilidade d6 pela
  leitura do registro.
- 2 novos testes em `ValidateAncestryOptionBudgetsUseCaseTest` usando o
  conteúdo REAL do registro (não sintético) pra Meio-Elfo e
  Meio-Demônio — confirmam que as duas opções de cada um fecham contra
  o orçamento padrão (2), pegando de quebra que
  `ANTECEDENTE_ARCANO_DEMONIO_MEIO` já tinha custo cadastrado (2) desde
  a rodada anterior.
- Suite completa rodada no harness (24 arquivos, 170 testes) — todos
  passando. `scripts/phase6_reliability_gate.sh` passou.

### Peça 3: Humano (Império San, Arte da Guerra) — as 14 opções de Signo de Nascença migradas

Pedido explícito do usuário: migrar as 14 opções (13 Signos + Nenhum)
pro mesmo formato de Seleção da Peça 2, no mesmo espírito de
Terracota/Umvee/Elementais/Meio-Elfo — sem hardcode por nome de raça, e
sem fabricar efeitos mecânicos que o app não modela ainda só pra fechar
o orçamento de pontos "bonito".

**Raiz do bug original** (motivo de toda a Peça 1/discussão): o Humano
do Arte da Guerra vinha em `ancestralidades.json` com DOIS traços
permanentes em `habilidades[]` — `ADAPTAVEL_OU_SIGNO` (0 pts,
placeholder) e `PONTOS_DE_PERICIA` (1 pt, sempre presente,
concedendo +3 pontos de perícia). O `+3` era só concedido de verdade
por um `if` avulso em `CriadorState.totalSpPool` checando
`signoIdFromNome(...) == "NENHUM"` — ou seja, o traço "sempre presente"
e o `if` avulso tinham que concordar entre si pra não vazar pontos, e
religar `PONTOS_DE_PERICIA` ao mecanismo genérico de bônus (que lê
direto de `habilidades[]`) teria somado +3 pontos de perícia pras
OUTRAS 13 opções também, já que o traço nunca saía da lista.

**Correção na raiz**: removidos `ADAPTAVEL_OU_SIGNO` e
`PONTOS_DE_PERICIA` de `ancestralidades.json` — a raça agora só carrega
o marcador `SIGNOS_DE_NASCENCA` (0 pts, o card de referência dos 13
Signos). `AncestryVariantRegistry.humanoArteDaGuerraSignos()`
(`ancestralidadeId="HUMANOS"`, `livro="ARTE_DA_GUERRA"`, mesma
convenção de literal compartilhado que `humanoFantasia()` já usava —
não é derivado de `nome.keyify()`) registra um `SelectionDef` único
(`id="signo_de_nascenca"`, `FIXED_PACKAGE`) com as 14
`FixedPackageOption`s. `CriadorState.applyAncestryVariantAdjustments()`
ganhou um bloco gated por
`base.habilidades.any { it.id?.keyify() == "SIGNOS_DE_NASCENCA" }`
(nunca por nome de raça) que resolve a opção ativa
(`signoIdFromNome(signoAdgSelecionado)`) via
`resolveAncestryVariantPackageUseCase.resolve()` e injeta os traços
reais da opção em `habilidades[]`, substituindo o marcador — agora
`PONTOS_DE_PERICIA` só existe na lista quando "Nenhum" é a opção ativa,
e o `PericiaPoolBonus(3)` genérico soma certo sem `if` avulso.

**4 blocos de hardcode em `CriadorState.kt` colapsados nos laços
genéricos já existentes** (em vez de removidos sem substituição — o
efeito continua existindo, só que lido do catálogo/registro em vez de
craveted em `if`):
- `periciaStartRawInternal`: Lebre/Garça viravam `if (signId == "LEBRE")`/
  `"GARCA"` — removidos, cobertos pelo laço genérico de `PericiaStep`.
  Só sobrou o ajuste de Serpente (efeito de escolha do jogador, sem
  gancho mecânico modelado — fica ad hoc de propósito, ver abaixo).
- `atributoBaseRacial`: bloco de `if (signId == "BOI"/"DRAGAO"/"MACACO"/
  "URSO")` removido — coberto pelo laço genérico de `AtributoStep`
  (mesmo padrão que já cobria "Povo da Montanha").
- `totalSpPool`: `if (temEscolhaDeSigno && signoIdFromNome(...) ==
  "NENHUM") 15 else 12` removido — agora é só
  `12 + bonusPontosPericia` (genérico), porque `PONTOS_DE_PERICIA` só
  está em `habilidades[]` quando "Nenhum" está ativo.
- `reservaChi`: `if (signo == "KIRIN") +1` avulso trocado por um laço
  genérico que soma `RacialTraitEffect.ChiReserveBonus` (tipo NOVO no
  `sealed class`, criado nesta rodada — junta-se a `PericiaPoolBonus`/
  `AtributoPoolBonus` como efeito lido direto por uma propriedade
  computada do `CriadorState`, sem gerar `Modifier` — por isso também
  precisou de um branch `Unit` em `ModifierEngine.aplicarEfeito` e um
  branch de texto em `RacialTraitAuditFormatter.formatEfeito`, os dois
  só pra manter os `when` exaustivos).
- `temAdaptavel()`: branch especial "Arte da Guerra: 'Nenhum' concede
  Adaptável" removido — agora Adaptável só aparece quando a opção
  "Nenhum" injeta o traço `ADAPTAVEL` de verdade em `habilidades[]`.

**Calibração das 14 opções** (cada uma comparada contra o texto oficial
em `ancestralidades.json`/livro, sem inventar custo pra fechar em 3
"bonito" — anotada via `anotacoes` quando um efeito do livro não tem
gancho mecânico no app ainda):
- `nenhum`: Adaptável (2) + Pontos de Perícia (1) = **3**, fecha.
- `basabasa`: Atraente/Vantagem concedida (2) + Perícia +1 Provocar-ou-
  Intimidar (1) = **3**, fecha.
- `boi`: Força d6 (2) + Perícia +1 Atletismo situacional (1) = **3**,
  fecha (anotação: falta a interação com Brutamontes).
- `tigre`: **0** — nenhum dos 3 efeitos do livro tem gancho mecânico no
  app ainda (achado real, documentado, não fabricado).
- `lebre`: Cura d6 (2) — **2** (anotação: falta o "Bene extra na Hora
  de Ouro").
- `garca`: Aparar +1 (1) + Acrobacia d4 (1) + Atletismo d6 (2) = **4**
  — acima do orçamento de propósito: os 3 efeitos já têm gancho
  mecânico pronto no catálogo, e o livro não parece calibrar os Signos
  entre si com o mesmo rigor de Terracota/Meio-Elfo. Confirmado por
  teste (`saldo=4`, `dentroDoOrcamento=false`), não escondido.
- `serpente`: **0** — efeito é escolha do jogador a cada uso (troca de
  perícia), sem representação estática em `habilidades[]`; mecanismo
  ad hoc mantido em `CriadorState` (fora do escopo desta migração).
- `dragao`: Espírito d6 (2) + Perícia +1 Conhecimento Geral situacional
  (1) = **3**, fecha.
- `kirin`: Sorte/Vantagem concedida (2) + Reserva de Chi +1 (1) = **3**,
  fecha — o "Bene extra por sessão" do livro já é coberto de verdade
  pela Vantagem Sorte real (concedida via `SIGNO_VANTAGENS_AUTOMATICAS`,
  ver abaixo).
- `macaco`: Astúcia d6 (2) — **2** (anotação: falta o bônus de d4+1 em
  testes sem perícia).
- `raposa`: Elevar o Moral/Vantagem concedida (2) + Perícia +1 Persuadir
  (1) = **3**, fecha (anotação: falta o bônus de Reação).
- `lobo`: Elo Comum/Vantagem concedida (2) — **2** (anotação: falta o
  bônus de Reação Inicial).
- `tartaruga`: Resistência +1 (1) — **1** (anotação: falta a penalidade
  de Finalização — o maior gap conhecido do conjunto).
- `urso`: Vigor d6 (2) — **2** (anotação: falta a redução de Exausto).

**`SIGNO_VANTAGENS_AUTOMATICAS` (CriadorState, mapa Signo→Vantagem pra
Basabasa/Raposa/Lobo/Kirin) deixado intocado de propósito** — já é
id-driven (não hardcode por nome), e roteá-lo pela nova injeção em
`habilidades[]` arriscaria conceder a mesma Vantagem duas vezes (uma
pelo mapa, outra pelo `resolvedVantagensGratis()` lendo o traço
injetado) sem nenhum teste pegar isso na hora — mesma lógica de
cautela já aplicada ao Meio-Demônio na Peça 2.

**Achado registrado, não corrigido** (fora do escopo): `SummaryUtils.kt`
(`calcAparar()`, pipeline separado de geração de resumo/PDF, opera em
cima de `MeuPersonagem`, não em `CriadorState`/`ModifierEngine`) tem sua
PRÓPRIA checagem `CriadorState.signoIdFromNome(personagem
.signoAdgSelecionado) == "GARCA"` pra aplicar o mesmo Aparar +1 da
Garça. Já é id-based (não viola a regra de "sem hardcode por nome"),
mas é uma implementação duplicada e paralela que pode dessincronizar da
versão em `CriadorState`/`ModifierEngine` se uma das duas mudar sem a
outra. `SummaryUtilsTest` já cobre esse caminho e continuou passando
(pipeline isolado, não afetado pelas mudanças desta rodada). Anotado
aqui como debt conhecido, não mexido.

**Limpeza**: removida a entrada morta `"ADAPTAVEL_OU_SIGNO" to 2` de
`RacialTraitPointCatalog.CUSTOS` (confirmado por busca: 0 raças em
`ancestralidades.json` ou testes ainda referenciam esse id depois da
migração).

### Verificação (Peça 3)

- Novo arquivo de teste (`CriadorStateSignoDeNascencaTest`, 7 testes):
  Nenhum concede Adaptável + 15 pontos de perícia; Boi não concede
  Adaptável/pontos extra e sobe Força pra d6; trocar Boi→Dragão não
  deixa Força vazando e sobe Espírito corretamente; Kirin soma +1 na
  Reserva de Chi vs. baseline sem Signo; Lebre começa com Curar d6;
  Garça fecha Acrobacia d4/Atletismo d6/tem o traço `APARAR`; Tigre não
  quebra nada mesmo sem efeito numérico modelado.
- 1 novo teste em `ValidateAncestryOptionBudgetsUseCaseTest` usando o
  conteúdo REAL do registro: confirma as 14 opções, `nenhum`=3,
  `kirin`=3, `garca`=4 (acima, de propósito), `tartaruga`=1, `tigre`=0,
  `serpente`=0.
- 2 mocks desatualizados em `CriadorStateKirinSignTest` (ainda usavam o
  marcador antigo `adaptavel_ou_signo`) corrigidos pro novo
  `SIGNOS_DE_NASCENCA`.
- Suite completa rodada no harness (24 arquivos, 194 testes) — todos
  passando, incluindo os 3 testes de `CriadorStateFullFlowTest` que só
  resolvem `ancestralidades.json` de verdade quando rodados a partir da
  raiz do repo. `scripts/phase6_reliability_gate.sh` passou (mesmo WARN
  pré-existente de tamanho de `CriadorState.kt`, sem regressão nova).

### Pendente pra próxima rodada

- **Peça 4** (Variante Customizada escopada a uma única opção dentro de
  uma raça — ex.: customizar só o Signo Dragão mantendo os outros 13
  oficiais) — ainda só desenho, não implementada.

## Trigésima quarta rodada — mecanismo genérico de Seleção pra QUALQUER raça: `resolveMarkedSelection`, `TARGET_ATTRIBUTE_OR_SKILL` implementado, Meio-Orc/Feral/Minerador migrados

Pedido explícito do usuário: em vez de continuar copiando o padrão
"if de gate por id + bloco de resolução manual" pra cada raça nova
(como as rodadas anteriores fizeram pra Herança/Signo), construir de
verdade **um mecanismo único, usável por qualquer raça, mas só
ativado nas que registram uma Seleção** — "Uma raça que não tenha isso
não vai ter seletor de opção... Uma raça que tenha variações... vai
mostrar dados diferentes no seletor." Aproveitado pra fechar o item
pendente da rodada anterior: generalizar a escolha de atributo
(Meio-Orc, Feral, Minerador Genético) pro mesmo formato de Seleção,
implementando `SelectionType.TARGET_ATTRIBUTE_OR_SKILL` de verdade
pela primeira vez.

### O mecanismo genérico

**Modelo** (`AncestryVariantSystem.kt`): `TraitAddition` ganhou
`traitId`/`targetRef` opcionais — antes só existia pra pacotes de
efeito FIXO (um id já sabe seu próprio efeito); agora uma Seleção pode
injetar um traço cujo efeito mecânico depende do que o jogador
escolheu (`traitId="ATTRIBUTE_BOOST"` + `targetRef=<atributo
escolhido>`, mesmo par `traitId`/`targetRef` que `RacialAbility` já
usa em outros lugares do app — ex.: bônus de atributo de Monstro
Heroico). `SelectionDef` ganhou `marcadorTraitId` (declara qual id, em
`habilidades[]` da raça base, sinaliza "esta Seleção está ativa aqui"
— antes cada raça migrada precisava de um `if` de gate próprio
escrito à mão em `CriadorState`; agora é um campo do registro) e
`manterMarcadorVisivel` (Signo mantém o card de referência dos 13
Signos depois de resolvido; Herança/Endurecido/Primitivo não, o
marcador é só um placeholder que desaparece) e `defaultTargetChoice`
(cada Seleção declara seu próprio default quando o jogador não
escolheu nada ainda — antes cada bloco em `CriadorState`/na UI tinha
o próprio `?: "Vigor"`/`?: "Força"` espalhado e duplicado).

**Resolução** (`ResolveAncestryVariantPackageUseCase.resolveTargetAttributeOrSkill`,
NOVO): recebe a resposta do jogador (`SelectionAnswer.targetChoice`),
valida contra `targetOptions`, cai no `defaultTargetChoice` da própria
Seleção se a resposta for nula/inválida, e injeta UM `TraitAddition`
com `traitId=ATTRIBUTE_BOOST`/`SKILL_BOOST` + `targetRef` + `pontos`
explícito (2 pra atributo, 1 pra perícia — não confia em
`RacialTraitPointCatalog.CUSTOS[id]`, porque o `id` do traço é único
por Seleção, não um id genérico reaproveitável entre raças, então o
custo tem que vir junto no próprio `TraitAddition`, igual ao padrão
"traço calibrado à mão" que já existia pra casos como as armas de
Draconianos).

**`CriadorState.resolveMarkedSelection()`** (NOVO, privado): UMA função
que qualquer raça com marcador pode chamar — resolve o pacote via
`resolveAncestryVariantPackageUseCase.resolve()`, remove (ou mantém,
conforme `manterMarcadorVisivel`) o marcador de `habilidades[]`, injeta
os traços resolvidos (repassando `traitId`/`targetRef`/`pontos`, e
escolhendo `category` certo — `racial_edge`/`racial_hindrance`/
`racial_trait_positive` — a partir do `traitId` do traço, não mais
fixo). Os blocos de Herança (Meio-Elfo) e Signo (Humano Arte da
Guerra), que antes tinham ~25 linhas cada construindo `RacialAbility`
na mão, viraram uma chamada de ~10 linhas cada — prova de que o
mecanismo generaliza sem perder nada. Meio-Orc (marcador `ENDURECIDO`)
e Feral (marcador `PRIMITIVO`) usam a MESMA função.

**Meio-Demônio continua fora desta migração** (Peça 2, decisão já
documentada): agora que `traitId`/`targetRef` passam pelo pipeline
genérico, o bloqueio original (perder o `targetRef` da Vantagem real ao
usar `addIfAbsent`) não existe mais — mas migrar essa raça especificamente
não foi pedido nesta rodada, e reabrir esse risco sem necessidade não
está no escopo. Fica anotado como via livre pra uma rodada futura, se
pedido.

### Meio-Orc, Feral e Humano Sci-Fi "Minerador" migrados

- `AncestryVariantRegistry.meioOrc()` (MEIO-ORCS/FANTASIA): Seleção
  `TARGET_ATTRIBUTE_OR_SKILL`, `targetOptions=[Força,Vigor]`,
  `defaultTargetChoice="Vigor"` (livro não define um padrão; preserva
  o comportamento de antes), `marcadorTraitId="ENDURECIDO"`.
- `AncestryVariantRegistry.feralArteDaGuerra()` (FERAL/ARTE_DA_GUERRA):
  `targetOptions=[Força,Vigor,Agilidade]`, `defaultTargetChoice="Força"`,
  `marcadorTraitId="PRIMITIVO"`.
- `AncestryVariantRegistry.humanos()` (HUMANOS/SCI_FI): a `VariantOption`
  "minerador" ganhou uma Seleção ANINHADA (mesmo padrão já usado por
  `humanoFantasia()`'s Povo do Mar/Senhores dos Cavalos) —
  `targetOptions=[Força,Vigor]`. O marcador fixo `MINERADOR_ATRIBUTO`
  (que só sinalizava a escolha, sem custo cadastrado — a raça toda
  nunca passava pelo validador de orçamento por opção) foi removido; o
  traço real resolvido entra no lugar.
- `atributoBaseRacial()`: bloco especial
  `habilidadeIds.contains("ENDURECIDO" || "PRIMITIVO" || "MINERADOR_ATRIBUTO")`
  removido inteiramente — o laço genérico de `AtributoStep` (que já lê
  `traitId`/`targetRef` de qualquer traço) cobre os três agora, mesmo
  padrão que já cobria Boi/Dragão/Macaco/Urso desde a rodada do Signo.
- `precisaPassarPorAjusteDeVariante`/`withVariant` (gate que decide se
  `applyAncestryVariantAdjustments` roda): ganharam
  `temEscolhaDeAtributoRacial()`, mesmo padrão de
  `temSignoDeNascenca()`/`temEscolhaMeioDemonio()` — sem isso, o
  candidato único de Meio-Orc/Feral nunca chegava a resolver a Seleção
  (achado real via teste, não hipotético: `CriadorStateRacialTraitDrivenAttributesTest`
  e `ScifiAncestryVariantSyncTest` quebraram com o marcador presente
  mas sem o traço resolvido substituindo).

### UI: um seletor genérico em vez de 3 blocos quase idênticos

`AncestralidadesSection.kt` tinha 3 blocos de `OutlinedButton`+
`DropdownMenu` praticamente idênticos (Minerador, Feral, Meio-Orc),
cada um com sua lista de opções e rótulo escritos à mão. Substituídos
por `atributoEscolhidoSelectionDefFor()` (acha a `SelectionDef`
aplicável — top-level via `marcadorTraitId`, ou aninhada dentro da
`VariantOption` ativa) + `AtributoEscolhidoPicker()` (um Composable só,
rótulo e opções lidos do próprio `SelectionDef`). `isFeral`/`isMeioOrc`
(flags por nome/id só usadas por esses blocos) removidas junto —
ficaram redundantes.

### Achado e correção: bloco genérico já resolvia a Seleção aninhada, "capenga"

O bloco genérico de `scifiVariantDrivenKeys` (que já resolve toda
Variante Sci-Fi de 2 opções) roda ANTES do bloco dedicado de Minerador,
pra QUALQUER `VariantOption` — inclusive "minerador", que agora tem uma
Seleção aninhada. Só que aquele bloco genérico passa
`selectionAnswers=emptyList()` (não sabe de resposta de jogador, só de
Variante) e usa um `addIfAbsent` que não repassa `traitId`/`targetRef` —
então ele MESMO já resolvia (e injetava, capenga, sem o par
`traitId`/`targetRef`) o traço de atributo, caindo sempre no
`defaultTargetChoice` (Força), ANTES do bloco dedicado rodar. O dedup
por id (`newHabilidades.none { it.id == traco.id }`) do bloco dedicado
então via o id já presente e silenciosamente MANTINHA a versão errada
— a escolha real do jogador (`humanoMineradorAtributo`) nunca chegava
a valer pro Minerador. Pego por teste real
(`CriadorStateAtributoEscolhidoTest`, não hipotético — as 2 primeiras
tentativas falharam consistentemente com "Vigor" caindo pra "Força"),
corrigido trocando o dedup por `removeAll` antes de adicionar: o bloco
dedicado agora sempre substitui a versão capenga do genérico pela
resolvida de verdade.

### Verificação

- Novo arquivo de teste (`CriadorStateAtributoEscolhidoTest`, 8
  testes): Meio-Orc sem escolha usa Vigor (default), com Força
  escolhida sobe Força; Feral sem escolha usa Força (default), com
  Agilidade escolhida sobe Agilidade; Humano Sci-Fi Minerador sem
  escolha usa Força, com Vigor escolhido sobe Vigor; Baixa Gravidade
  (a outra opção da mesma raça) não ganha bônus de Força/Vigor;
  trocar Meio-Orc de Vigor pra Força não deixa Vigor vazando.
- Suite completa rodada no harness (25 arquivos, 202 testes) — todos
  passando, incluindo os 3 testes de `CriadorStateFullFlowTest`
  (rodados a partir da raiz do repo).
  `scripts/phase6_reliability_gate.sh` passou (mesmo WARN pré-existente
  de tamanho de `CriadorState.kt`, sem regressão nova).

### Pendente pra próxima rodada

- **Peça 4** (Variante Customizada escopada a uma única opção dentro de
  uma raça) — segue só desenho, não implementada.
- Migrar Meio-Demônio pro `resolveMarkedSelection` genérico — agora
  tecnicamente seguro (o bloqueio original não existe mais), mas não
  pedido nesta rodada.

### Correção de CI: gap na verificação da rodada 33 (Signo)

O CI do PR pegou um teste real que o harness local (`/tmp/ktbig`) não
cobre — `AncestralidadeCatalogBudgetTest`, que varre TODAS as raças do
catálogo estático (`ancestralidades.json` puro, sem resolver Seleção
nenhuma) e confere que cada uma fecha sozinha contra seu próprio
`pontosRaciaisEsperados`. A remoção de `ADAPTAVEL_OU_SIGNO`/
`PONTOS_DE_PERICIA` do Humano (Império San) na rodada 33 (Peça 3) é
CORRETA pro app em runtime (o orçamento fecha depois que
`AncestryVariantRegistry` resolve o Signo ativo — já confirmado pelos
testes de `ValidateAncestryOptionBudgetsUseCaseTest`), mas deixa o
catálogo ESTÁTICO somando 0 em vez de 3 — exatamente a mesma situação
já documentada pra Ferais/Florans/Gelatinoides/Insetoides/Mímicos/Umvee
(raças cujo total só fecha com a Variante ativa). Faltava só adicionar
"Humano (Império San)"/ARTE_DA_GUERRA à lista de exceções documentada
(`racasComTracosInjetadosDinamicamente`) do teste — feito agora.
Achado real de CI, não hipotético (`soma=0, esperado=3` no log da
Actions), gap de cobertura no meu processo de verificação local (esse
teste específico nunca tinha sido copiado pro harness `/tmp/ktbig`) —
corrigido e a suíte completa (25 arquivos + este, 200 testes) rodou de
novo, incluindo este teste especificamente contra o JSON real, sem
falhas.

## Trigésima quinta rodada — Peça 4: Variante Customizada escopada a uma opção de Seleção

Última peça do plano original (ver rodada 33, "Ordem de execução",
item 4): até aqui, uma Variante Customizada de raça
(`CustomAncestryVariant`) só sabia sobrescrever a raça INTEIRA — não
tinha como o mestre criar uma Variante que muda só o Signo Dragão do
Humano Arte da Guerra, mantendo os outros 13 oficiais. Implementado
exatamente como desenhado na rodada 33: campo opcional "id da opção
que ela sobrescreve"; vazio = comportamento de sempre (raça inteira);
preenchido = só se aplica quando a opção ativa bate com esse id.

### Modelo e mecanismo

`CustomAncestryVariant.opcaoAlvoId: String? = null` (NOVO) — o id de
uma `FixedPackageOption` de `AncestryVariantRegistry` (ex.: `"dragao"`,
`"voto"`). `CriadorState.currentSelectionOptionId(base)` (NOVO,
privado): responde "qual é o id da opção ATIVA agora nesta raça" —
Meio-Elfo/Signo/Meio-Demônio (campo de estado próprio, não
compartilhado via registro) despachados por nome aqui, único lugar
desta rodada onde isso acontece — não decide comportamento MECÂNICO
por nome (isso continua proibido), só qual campo ler pra montar um id
de UI; as demais raças com Seleção `FIXED_PACKAGE` (Terracota, Umvee,
Elementais, e qualquer outra cadastrada no futuro) resolvidas
genericamente casando o texto de `resolveSciFiVariantSelectionFor`
contra os nomes das opções do registro, sem nome de raça nenhum.
`applyCustomAncestryVariantIfSelected()` ganhou um gate de uma linha:
`if (variant.opcaoAlvoId != null && variant.opcaoAlvoId != currentSelectionOptionId(base)) return base`
— a Variante simplesmente não se aplica quando a opção ativa não bate,
sem precisar o jogador desmarcá-la ao trocar de opção.

### UI (`SettingsDialog.kt`, criação da Variante)

Quando a raça base escolhida tem uma Seleção `FIXED_PACKAGE`
cadastrada, aparece um seletor "Escopo desta Variante:" (Toda a raça,
ou uma das opções nomeadas). Escopada a uma opção, a lista "Remover da
raça base" passa a incluir os traços da PRÓPRIA opção (ex.: "Espírito
d6 (Dragão)"), não só os da raça estática — sem isso não dava pra
sobrescrever nada que a opção concede, só a raça base como um todo
(que pro Humano Arte da Guerra é só o marcador `SIGNOS_DE_NASCENCA`,
0 pontos). O orçamento de pontos da Variante também passa a partir do
saldo RESOLVIDO da opção (reaproveitando
`ValidateAncestryOptionBudgetsUseCase`, o mesmo cálculo que já valida
cada opção isolada — ver Peça 1), não do total plano da raça base:
pro Dragão isso são 3 pontos de partida, não 0. `AncestralidadesSection.kt`
ganhou um aviso ("Escopo: só se aplica quando 'Dragão' estiver
ativo.") sob o seletor de Variante Custom, pra o mestre não se
surpreender ao trocar de Signo e ver a Variante "sumir" sem
desmarcá-la.

### Verificação

- Novo `CriadorStateCustomVariantScopedOptionTest` (4 testes): Variante
  escopada ao Dragão aplica quando Dragão está ativo; NÃO aplica
  quando outro Signo (Boi) está ativo, e a opção Boi continua 100%
  oficial (Força d6 do signo, sem interferência); uma Variante SEM
  escopo (`opcaoAlvoId=null`) continua aplicando pra qualquer opção,
  igual sempre foi (regressão coberta); trocar de Dragão pra outro
  Signo desativa a Variante escopada automaticamente, sem precisar
  desmarcá-la.
- `SettingsDialog.kt`/`AncestralidadesSection.kt` não entram no
  harness local (dependem de Compose/Material3 real, que o harness
  puro-JVM não tem) — revisadas manualmente linha a linha (imports,
  tipos, balanceamento de chaves) em vez de compiladas; o mecanismo em
  si (`CriadorState`/modelo), que É testável, tem cobertura completa.
- Suite completa (27 arquivos, 204 testes) e
  `scripts/phase6_reliability_gate.sh` passando (mesmo WARN
  pré-existente de tamanho de `CriadorState.kt`, sem regressão nova).

Com isso, as 4 peças do plano original da rodada 33 estão completas.
Pendente, não pedido em nenhuma rodada: migrar Meio-Demônio pro
`resolveMarkedSelection` genérico (tecnicamente seguro desde a rodada
34, nunca pedido).

## Trigésima sexta rodada — auditoria de TODAS as raças do app por Seleção não migrada; 2 bugs reais achados no caminho

Pedido explícito, distinto de "Variante" (mestre reconfigura a raça
pro cenário): "as opções disponíveis delas" — toda raça que oferece
uma escolha ao próprio jogador na criação (perícia/atributo à escolha,
traço A-ou-B etc.), migrada pro sistema de Seleção genérico da rodada
34, não só o Humanoide da Guerra citado como exemplo. Varredura
completa das 112 raças de `ancestralidades.json` + todo campo de
estado `*Escolhida`/`*Selecionado` de `CriadorState.kt`.

### Achados que já estavam corretos (não é gap)

- Humano/Meio-Elfo Pathfinder "Flexibilidade": +1 Ponto de Atributo
  livre pro jogador gastar (evita o caso de borda de um atributo já
  em d12 não ter como subir mais um passo) — mecanismo
  deliberadamente diferente de um traço-marcador fixo, não uma
  Seleção do mesmo tipo. Deixado como está.
- Rakashanos (Inimigo Racial/Ancestral) e o "Adaptável" de Humano
  (Vantagem de Estágio Novato à escolha, várias edições) — mecanismos
  próprios já servidos por UI dedicada, fora do escopo desta rodada
  (escolha de Vantagem/raça-inimiga, não perícia/atributo).

### Bug real #1 — Descendente Elemental: escolha de elemento sem nenhum efeito mecânico

`applyAncestryVariantAdjustments` tinha um bloco
`when (descendenteElementalSelecionado) { ... }` de verdade — só que
posicionado DEPOIS de um `return base` que já disparava sempre pra
essa raça (`opcoes` vazio). Confirmado ao vivo: escolher "Terra" não
mudava Vigor (continuava d4) nem injetava Sólido como Rocha. Bug de
produção anterior a esta sessão, só exposto agora pela varredura
sistemática. Corrigido: novo bloco gateado por
`habilidadeIds.contains("ELEMENTO_ANCESTRAL")`, roteado por
`resolveMarkedSelection` (marcador `ELEMENTO_ANCESTRAL`,
`FixedPackageOption` por elemento em
`AncestryVariantRegistry.descendenteElemental()`), posicionado ANTES
do `return base` do bloco Sci-Fi. O código morto também removia
incondicionalmente `RESISTENCIA_AMBIENTAL` — confirmado por
orçamento de pontos (1+2-1=2, bate com `pontosRaciaisEsperados`) que
esse traço é permanente, não ligado à escolha de elemento; não
carregado pra versão nova.

### Bug real #2 — Descendente Elemental (Fogo): Vantagem "Rápido" nunca era concedida

Mesmo depois do bug #1 corrigido, escolher "Fogo" ainda não dava a
Vantagem Rápido prometida pelo traço. Duas causas: (a)
`resolveMarkedSelection` só processava `tracosParaAdicionar`
(traços/habilidades), nunca `vantagensGratisParaAdicionar`; (b)
`ResolveAncestrySpecificAdjustmentsUseCase` já tinha um bloco
"DESCENDENTE ELEMENTAL" — mas só com lógica de REMOÇÃO
(`automaticAdvantagesToRemove`), nunca de concessão, mecanismo
totalmente separado do primeiro. Corrigido estendendo
`resolveMarkedSelection` com um `adicionarTraco()` compartilhado que
processa os dois tipos de traço e roteia `GRANTED_EDGE` pro mecanismo
`resolvedVantagensGratis()` (que já funciona pra outras raças); o
`TraitAddition("RÁPIDO", ...)` do registro ganhou
`targetRef = "rapido"` (id real de `vantagens.json`).

### Bug real #3 — fórmula de custo de SKILL_BOOST (latente, nunca exercitada)

Ao migrar Kitsunemimi/Gnomo (perícia à escolha, primeira vez que
`TARGET_ATTRIBUTE_OR_SKILL` resolve uma PERÍCIA em vez de um
atributo), os testes novos falharam: perícia escolhida vinha d6 em
vez de d4. Causa raiz, em duas camadas:
- `RacialTraitPointCatalog.custoDe("SKILL_BOOST", value)` calculava
  `if (value >= 1) 2 else 1` — invertido; corrigido primeiro pra
  `value` (ainda errado, ver abaixo), depois pra `value + 1`.
- Semântica real de `passos`, confirmada lendo os DOIS loops de
  resolução ao vivo (`atributoBaseRacial`/`periciaStartRawInternal`,
  ambos usam `4 + passos*2`): ATRIBUTO sempre parte de d4 implícito,
  então `passos=1` sempre foi d6 (comportamento pré-existente, correto,
  do Meio-Orc/Feral/Minerador). PERÍCIA parte DESTREINADA — d4 é o
  PRIMEIRO patamar treinado, não "um passo acima" —, então
  `passos=0` é d4 e `passos=1` é d6. Calibração oficial confirmada
  em `basico_habilidades_raciais.json`:
  `pericia_racial_d4`=1pt, `pericia_racial_d6`=2pt — exatamente
  `value + 1` com `value=passos`. `SelectionDef.passos` ganhou
  default `= 1` e um comentário explicando a distinção; Kitsunemimi/
  Gnomo declaram `passos = 0` explicitamente (caso "começa
  destreinada").

### Kitsunemimi (Preparado) e Gnomo (Obsessivos) migrados

Mesmo padrão de Meio-Orc/Feral, mas `targetKind = SKILL`:
`AncestryVariantRegistry.kitsunemimiArteDaGuerra()` (5 perícias:
Conhecimento Acadêmico, Convenção, Intimidar, Pesquisar, Provocar) e
`gnomoPathfinder()` (11 perícias de Astúcia do Pathfinder, listadas
via `pericias.json`). `CriadorState.temEscolhaDeAtributoOuPericia`
(renomeada de `temEscolhaDeAtributoRacial`) estendida de
`{ENDURECIDO, PRIMITIVO}` pra incluir `PREPARADO`/`OBSESSIVOS`; os
`if` avulsos velhos dentro de `periciaStartRawInternal` que liam
`habilidadeIdsPericia.contains("OBSESSIVOS"/"PREPARADO")` removidos,
substituídos pelos blocos gateados por
`habilidadeIds.any { it.id?.keyify() == "PREPARADO"/"OBSESSIVOS" }` +
`resolveMarkedSelection`, igual ao resto da família.

### UI: `AtributoEscolhidoPicker` deixa de ser hardcoded

Achado ao preparar o seletor de Kitsunemimi/Gnomo: o composable
`AtributoEscolhidoPicker(def, state)` (rodada 34) lia/escrevia direto
`state.humanoMineradorAtributo`/`selecionarHumanoMineradorAtributo` —
reusar como estava pra Kitsunemimi/Gnomo escreveria no campo ERRADO
(o de Minerador/Feral/Meio-Orc), corrompendo os dois. Assinatura
trocada pra `AtributoEscolhidoPicker(def, valorAtual, onSelecionar)`;
os 3 chamadores existentes (Minerador, Feral, Meio-Orc) passam
explicitamente `state.humanoMineradorAtributo`/
`{ state.selecionarHumanoMineradorAtributo(it) }`; 2 chamadores novos
(Kitsunemimi/Gnomo) passam seus próprios campos
(`kitsunemimiPericiaEscolhida`/`gnomoPericiaEscolhida`), gateados
pelo `marcadorTraitId` da Seleção ativa, mesmo padrão do
Feral/Meio-Orc.

### Deliberadamente adiado: Usagimimi "Definido pelo Ofício"

Escolha de QUALQUER perícia (não uma lista fixa curta) — o picker
genérico atual (`AtributoEscolhidoPicker`) só sabe renderizar um
dropdown de poucas opções fixas, não um catálogo filtrado completo
como o seletor de perícia dos Anões Ciber
(`periciasFiltradasPorCompendio`); precisa de uma nova capacidade de
UI. Além disso tem um efeito colateral independente de
compatibilidade com Tropo (`isUsagimimiTransicaoRestrictionActive()`,
ligado a um valor-sentinela "Transição") que não pode ser perturbado
de passagem. Adiamento consciente e documentado, não esquecimento.

### Verificação

- 2 testes novos: `CriadorStateDescendenteElementalTest` (5 testes —
  Água/Terra/Fogo cada um com o traço/atributo certo, Fogo concede a
  Vantagem Rápido de verdade via `GRANTED_EDGE`/`targetRef` E
  `resolvedVantagensGratis()`, trocar de elemento não deixa o traço
  anterior vazando) e `CriadorStatePericiaEscolhidaTest` (3 testes —
  Kitsunemimi/Gnomo começam com a perícia escolhida em d4, trocar de
  perícia no Gnomo não deixa a anterior vazando).
- Suite completa (29 arquivos, 215 testes) e
  `scripts/phase6_reliability_gate.sh` passando (mesmo WARN
  pré-existente de tamanho de `CriadorState.kt`, sem regressão nova).
- `AncestralidadesSection.kt` revisado manualmente linha a linha
  (não compila no harness puro-JVM, sem Compose/Material3 real).

Pendente, explicitamente fora desta rodada: migração de Usagimimi
(ver acima, precisa de nova capacidade de UI).

## Trigésima sétima rodada — Usagimimi migrado (o adiamento da rodada 36 estava errado) + 2 bugs de UI achados ao revisar o próprio trabalho

Ao preparar a migração de Usagimimi que a rodada 36 tinha adiado,
reli a lista real de perícias da Arte da Guerra em `pericias.json`
em vez de confiar na memória do "picker de qualquer perícia" —
achado: não são "todas as perícias do jogo" (isso sim precisaria de
um catálogo filtrado tipo Anões Ciber), são só as da Arte da Guerra
menos Idiomas/Jutsu — **29 no total**, um conjunto perfeitamente
enumerável, exatamente igual ao que o dropdown antigo (dinâmico,
calculado de `state.listaPericias.filter{...}`) já mostrava. O
adiamento da rodada 36 estava errado nesse ponto específico; a
restrição de Tropo (`isUsagimimiTransicaoRestrictionActive`,
inalterada) nunca foi o bloqueio de verdade.

### Bug real #1 — UI duplicada pra Kitsunemimi e Gnomo

Revisando `AncestralidadesSection.kt` de novo antes de mexer em
Usagimimi, achei que os NOVOS seletores genéricos de Kitsunemimi/
Gnomo (adicionados na rodada 36) foram só ACRESCENTADOS — os blocos
antigos, com dropdown próprio (`if (item.nome.keyify().contains("KITSUNEMIMI"))`
etc.), continuavam lá, mais abaixo no arquivo. Resultado: as duas
raças mostrariam DOIS seletores de perícia idênticos na tela (ambos
escrevendo no mesmo campo de estado, então sem corromper dado — só
UI redundante/confusa). Achado ao reler o próprio código depois de
"terminado", não relatado por ninguém. Corrigido removendo os 2
blocos antigos por completo.

### Bug real #2 — lista de Gnomo (Obsessivos) faltando "Provocar"

Comparando o dropdown antigo do Gnomo (dinâmico:
`periciasFiltradasPorCompendio.filter { atributo=="ASTUCIA" && ... }`)
contra a lista estática nova da rodada 36, faltava "Provocar" — as
12 perícias de Astúcia do Pathfinder em `pericias.json` são
Conhecimento de Batalha/Ciência/Conhecimento Acadêmico/Conhecimento
Geral/Conjurar/Consertar/Curar/Jogar/Ocultismo/Perceber/**Provocar**/
Sobrevivência; a rodada 36 só cadastrou 11, escrita de memória em vez
de conferida contra o catálogo. Corrigido em
`AncestryVariantRegistry.gnomoPathfinder()`.

### Usagimimi migrado

Mesmo padrão de Kitsunemimi/Gnomo, mas com uma diferença mecânica
real: o traço concede d6 DIRETO (não "d4, perícia destreinada" como
os outros dois) — `passos=1` (o default de `SelectionDef`, nem
precisa declarar), confirmado batendo o orçamento de pontos
(`DEFINIDO_PELO_OFICIO`=2pt em `RacialTraitPointCatalog.CUSTOS`,
igual a `custoDe("SKILL_BOOST", passos=1)=2`, e a soma de
`pontosRaciaisEsperados=3` do Usagimimi em `ancestralidades.json`
bate com 2+2+1-2 dos 4 traços da raça). `AncestryVariantRegistry.usagimimiArteDaGuerra()`
com as 29 opções; `CriadorState` ganhou o bloco gateado por
`habilidadeIds.any { it.id?.keyify() == "DEFINIDO_PELO_OFICIO" }` +
`resolveMarkedSelection`, e o `if` avulso antigo em
`periciaStartRawInternal` foi removido, mesmo padrão de Kitsunemimi/
Gnomo. `temEscolhaDeAtributoOuPericia` estendida com
`DEFINIDO_PELO_OFICIO`. `selecionarPericiaUsagimimi()` (a função
pública que a UI chama) e `isUsagimimiTransicaoRestrictionActive()`
NÃO foram tocadas — a restrição de Tropo ligada à opção "Transição"
continua funcionando exatamente como antes, só a resolução MECÂNICA
do traço (o "que perícia ganha d6") passou a vir do sistema de
Seleção. Na UI, o dropdown antigo (dinâmico, calculado a cada
recomposição) virou o mesmo `AtributoEscolhidoPicker` genérico das
outras raças, gateado por `marcadorTraitId == "DEFINIDO_PELO_OFICIO"`.

### Verificação

- 3 testes novos em `CriadorStatePericiaEscolhidaTest` (agora 6 no
  total): Usagimimi com "Provocar" escolhido começa em d6 (não d4,
  confirma o `passos=1`); trocar de perícia não deixa a anterior
  vazando; escolher "Transição" ativa
  `isUsagimimiTransicaoRestrictionActive()` (confirma que a
  migração não tocou nesse mecanismo).
- Suite completa (29 arquivos, 218 testes) e
  `scripts/phase6_reliability_gate.sh` passando (mesmo WARN
  pré-existente de tamanho de `CriadorState.kt`, sem regressão nova).
- `AncestralidadesSection.kt` revisado manualmente linha a linha de
  novo (balanceamento de chaves conferido por script à parte, já que
  o arquivo não compila no harness puro-JVM).

Com isso, as 5 raças com "opções" (não Variante) identificadas na
varredura da rodada 36 — Meio-Orc, Feral, Kitsunemimi, Gnomo e
Usagimimi — estão todas no sistema de Seleção genérico. Não ficou
nada pendente desta frente.

## Trigésima oitava rodada — Meio-Demônio migrado (o bloqueio de rodadas atrás já tinha sido resolvido sem ninguém voltar pra fechar); 3 candidatos conferidos e confirmados como não-gap

Revisão do que ainda restava da varredura completa (rodada 36): 4
itens em aberto, nenhum deles pedido explicitamente por ninguém, mas
citados como pendentes na lista de tarefas.

### Meio-Demônio migrado

`CriadorState.applyAncestryVariantAdjustments()` tinha um bloco
`if (meioDemonioAA) {...} else {...}` construindo o traço/Vantagem na
mão, embora `AncestryVariantRegistry.meioDemonio()` já existisse,
cadastrado mas nunca chamado. O comentário no código (de uma rodada
bem anterior) explicava por quê: migrar pra `resolveMarkedSelection`
na época teria trocado silenciosamente o alvo da concessão de
"aa_demonio_meio_demonio" (id real da Vantagem, o que
`resolvedVantagensGratis()` espera em `targetRef`) para
"ANTECEDENTE_ARCANO_DEMONIO_MEIO" (id do TRAÇO, que não existe no
catálogo de Vantagens) — porque `resolveMarkedSelection` só lia
`vantagensGratisParaAdicionar.id`, nunca `.targetRef`. Conferindo o
código de hoje: a rodada 36 (bug #2 do Descendente Elemental, Vantagem
Rápido nunca concedida) já resolveu exatamente essa lacuna, threadando
`targetRef` de verdade em `TraitAddition`/`adicionarTraco`/
`resolveMarkedSelection` — só que ninguém voltou pra Meio-Demônio pra
aproveitar o conserto. Migrado agora: `AncestryVariantRegistry.meioDemonio()`
ganhou `targetRef = "aa_demonio_meio_demonio"` na opção
"antecedente_arcano" (removido o `vantagensGratisIds` que nunca foi
consumido por nada — não existe bloco de Meio-Demônio em
`ResolveAncestrySpecificAdjustmentsUseCase`, confirmado por grep, e
`resolveMarkedSelection` deliberadamente não lê esse campo); o bloco
manual em `CriadorState` virou uma chamada a `resolveMarkedSelection`,
mesmo padrão de Meio-Elfo/Kitsunemimi/Gnomo/Usagimimi.

### 3 candidatos conferidos e confirmados como não-gap (não precisam migrar)

- **Rakashanos "Inimigo Racial/Ancestral"**: conferido no código — o
  id só existe em `RacialTraitPointCatalog.CUSTOS` (custo -1, uma
  Complicação Menor) e em nenhum outro lugar. É puramente narrativo
  ("escolha uma ancestralidade rival do cenário", o -2 em Persuadir é
  situacional, aplicado pelo mestre em jogo, não algo que a ficha
  digital tem contexto pra calcular sozinha) — não existe efeito
  numérico de personagem pra migrar, nem campo de estado, nem UI. Não
  é uma "opção" no sentido que o sistema de Seleção resolve.
- **Humano "Adaptável" (Vantagem de Estágio Novato à escolha)**: já
  tem mecanismo próprio, funcional e testado
  (`vantagemAdaptavelSelecionadaId` + um seletor de Vantagem
  dedicado) — mas é um tipo de escolha estruturalmente diferente do
  que `TARGET_ATTRIBUTE_OR_SKILL`/`FIXED_PACKAGE` resolvem (escolher
  uma Vantagem inteira do catálogo do jogo, com elegibilidade
  própria, não um atributo/perícia/pacote fixo pequeno). Migrar isso
  pro sistema de Seleção exigiria um `SelectionType` novo inteiro —
  fora do escopo desta varredura, que era sobre ad hoc não migrado,
  não sobre substituir mecanismos já corretos.
- **Quadroides "Habilidoso" (traço negativo à escolha)**: conferido —
  só existe quando `effectiveScifiVariant == "Habilidoso"`, ou seja,
  é uma escolha ANINHADA DENTRO DE UMA VARIANTE (troca de cenário
  pelo mestre), não uma Seleção que a raça sempre oferece — mesma
  categoria de Anões Ciber/Terracota, que também têm seus próprios
  blocos dedicados dentro da Variante em vez de entrar no sistema de
  Seleção genérico. Exatamente o tipo de coisa que o usuário pediu
  pra EXCLUIR ("não estou falando de variante").

### Verificação

- Suite completa (29 arquivos, 218 testes, `CriadorStateMeioDemonioTest`
  incluído com seus 5 testes originais intactos — inclusive o que
  confere `vantagensSelecionadas.any { it.id == "aa_demonio_meio_demonio" }`,
  a prova de que a Vantagem continua sendo concedida de verdade depois
  da migração) e `scripts/phase6_reliability_gate.sh` passando (mesmo
  WARN pré-existente de tamanho de `CriadorState.kt`, sem regressão
  nova).

Com isso, a varredura completa de "opções" (não Variante) iniciada na
rodada 36 está fechada: todas as raças identificadas migraram pro
sistema de Seleção genérico, e os candidatos restantes foram
conferidos individualmente e confirmados como corretamente fora de
escopo, não esquecidos.

## Trigésima nona rodada — backlog antigo: migração de Atributo/Perícia Aumentada pro par genérico; 1 bug real achado (Araiguma "Brincalhão")

Item de backlog registrado desde a rodada 28/31 ("migrar ids de
Atributo/Perícia Aumentada pro par genérico `ATTRIBUTE_BOOST`/
`SKILL_BOOST`+`targetRef`+`value`, Básico primeiro depois Fantasia"),
nunca priorizado até agora. Escopo real acabou maior do que o
estimado na hora de perguntar pro usuário ("todos os livros" — 8
livros, dezenas de raças): 27 ids ao todo, não só os ~18 de Atributo.
Feito com o usuário confirmando escopo completo; a unificação
Monstro Heroico/Tropo (outro item do mesmo backlog) foi conferida e
descartada a pedido do usuário — os dois sistemas já funcionam
corretamente hoje, sem bug, então a reorganização não valia o risco.

### O que foi migrado

- **15 ids de Atributo** (`AtributoStep`): `AGIL`, `ASTUCIA`, `ASTUTO`,
  `DURAO`, `EM_FORMA`, `ESPIRITUAL`, `ESPIRITUOSO`,
  `FELIZES_POR_NATUREZA`, `FORCA_SOBRENATURAL`, `FORTE`,
  `INTELIGENCIA`, `MUITO_FORTE`, `MUITO_RESISTENTE`, `RESISTENTE`,
  `VIGOROSO` — 48 entradas em `ancestralidades.json`, 27 raças, todos
  os 8 livros. `id` mantido (identidade/auditoria/testes), ganharam
  `traitId="ATTRIBUTE_BOOST"` + `targetRef=<atributo>` +
  `value=<passos>`.
- **12 ids de Perícia** (`PericiaStep`): `CAES_DE_GUARDA`,
  `INTEGRADO_A_NATUREZA`, `PESFIRMES`, `SENTIDOS_AGUCADOS`,
  `SENTIDOS_APRIMORADOS`, `SENTIDOS_APURADOS`, `SORRATEIRO`,
  `TRAPALHOES_TRAVESSOS`, `CONHECIMENTO_GERAL`, `DICAS_CULTURAIS`,
  `BRINCANDO_COM_O_DESTINO`, `BRINCALHAO` — 18 entradas, 11 raças.
  Achado no caminho: várias custam só 1pt (não os 2pt padrão de
  `SKILL_BOOST` com `value=1`) porque o livro dá desconto quando a
  perícia concedida já é "Perícia Básica" (`pericias.json`) — Perceber/
  Atletismo/Furtividade/Conhecimento Geral começam de graça, então
  subir pra d6 vale menos que numa perícia não-básica. A fórmula
  genérica de `custoDe("SKILL_BOOST", ...)` não sabe disso (não olha
  se a perícia é básica) — as 8 entradas afetadas
  (`CAES_DE_GUARDA`/`PESFIRMES`/`SENTIDOS_*`/`SORRATEIRO`/
  `TRAPALHOES_TRAVESSOS`/`CONHECIMENTO_GERAL`) ganharam `pontos=1`
  explícito no JSON — mesmo escape-hatch (override) que
  `resolvedPontos()`/`custoDe()` já priorizam antes de cair na fórmula
  do id, usado antes só por Mordida/Garras com PA calibrado à mão.
- **6 `TraitAddition` injetados por Seleção/Variante**
  (`AncestryVariantRegistry.kt`): `SOLIDO_COMO_ROCHA` (Descendente
  Elemental), `POVO_MONTANHA_VIGOR`, `NOMADES_DESERTO_SOBREVIVENCIA`,
  `POVO_MAR_ATLETISMO`, `POVO_MAR_NAVEGAR`, `SENHORES_CAVALOS_CAVALGAR`
  (Humanos Fantasia, Pacotes Culturais) — mesmo tratamento.

### Deliberadamente fora desta rodada

- **Ids ligados a Signo de Nascença** (`LEBRE_CURA`, `GARCA_ACROBACIA`,
  `GARCA_ATLETISMO`, `KIRIN_CHI`, `PONTOS_DE_PERICIA`): o usuário pediu
  pra ver uma LISTA antes de mexer em Signos especificamente — não
  tocados aqui, ficam pro próximo pedido.
- **`FE`** ("Fé"): tem entrada no catálogo mas nenhuma raça usa esse id
  hoje (conferido contra `ancestralidades.json`) — nada pra migrar.
- **Limpeza do catálogo** (remover as entradas antigas de
  `EFEITOS`/`CUSTOS` depois de migrado): decidido NÃO fazer. 8 dos ids
  de Atributo (`FORTE`, `MUITO_FORTE`, `RESISTENTE`,
  `MUITO_RESISTENTE`, `AGIL`, `MUITO_AGIL`, `ESPIRITUAL`, `ASTUCIA`)
  continuam sendo emitidos como ids soltos por
  `monstroAtributoTraitIds()` (Monstro Heroico) — remover quebraria
  esse caminho, que é um consumidor legítimo e separado, não uma raça.
  Os outros ids migrados também ficaram com a entrada antiga viva:
  virou código morto pras raças reais (que agora resolvem pelo par
  genérico), mas `CriadorStateRacialTraitDrivenAttributesTest`
  constrói raças sintéticas com esses ids soltos de propósito (testa o
  mecanismo do catálogo em si) — remover exigiria reescrever esses
  testes pra zero ganho funcional.

### Bug real achado no caminho: Araiguma "Brincalhão" concedia Provocar d6, não d4

Ao migrar `BRINCALHAO`, o texto oficial embutido no JSON ("O Araiguma
recebe Provocar d4 (1)") e o custo já cadastrado (1pt = tier
`pericia_racial_d4`) não batiam com o efeito calculado: `EFEITOS`
tinha `PericiaStep("Provocar")` sem `passos=0` explícito, caindo no
default `passos=1` — o loop genérico (`4 + passos*2`) calculava d6,
não d4. Provocar não é Perícia Básica (conferido em `pericias.json`),
então não é caso do desconto explicado acima — era d4 mesmo, sem
ambiguidade, um bug de calibração pré-existente (não relacionado à
migração em si, só descoberto por ela). Corrigido (`passos = 0`
explícito) e coberto por teste novo
(`CriadorStateRacialTraitDrivenAttributesTest`, "brincalhao (Araiguma)
concede Provocar d4, nao d6") — sem isso, Araiguma dava metade a mais
de perícia inicial do que o livro concede.

### Fix conexo: `ResolveVariantPointBudgetUseCase.habilidadeComoItem` ignorava `traitId`/`targetRef`/`value`

Achado ao investigar por que migrar os ids não bastava: essa função
(orçamento do editor de Variante Customizada e de
`ValidateAncestryOptionBudgetsUseCase.itensRemovidosDoPacote`) chamava
`RacialTraitPointCatalog.efeitoDe(habilidade.id)` — só o `id` cru, sem
`resolvedTraitId()` nem `targetRef`/`value` — diferente do padrão já
correto em `RacialAbility.resolvedPontos()`/
`RacialTraitAuditFormatter`/`AncestralidadeCatalogBudgetTest`. Sem
corrigir, um traço migrado pro par genérico resolvia pra `Nenhum`
aqui assim que a entrada antiga do catálogo saísse de uso — o editor
de Variante mostraria custo 0 pra remover, por exemplo, "Ágil" de um
Elfo migrado. Corrigido pra usar `resolvedTraitId()` + passar
`targetRef`/`value` pra `efeitoDe()`/`custoDe()`, mesmo padrão do
resto do app; `habilidadeId` (usado pra casar remoção por id) continua
sendo o `id` cru, não o resolvido — só o CÁLCULO de efeito/custo
mudou. De quebra, isso também corrige (nunca exercitado até agora)
qualquer outro traço parametrizado com `value` não-padrão que passasse
por essa função — conferido que nenhuma raça hoje tinha esse caso
além das minhas próprias entradas novas.

### Verificação

- Novo teste em `CriadorStateRacialTraitDrivenAttributesTest` (agora
  13 testes): Araiguma/Brincalhão concede Provocar d4.
- Suite completa (29 arquivos, 219 testes) — `AncestralidadeCatalogBudgetTest`
  (varre a soma de TODAS as raças do catálogo real) continua fechando
  certo pras 38 raças/entradas tocadas, confirmando que os overrides de
  `pontos` preservaram exatamente os custos já calibrados.
  `ValidateAncestryOptionBudgetsUseCaseTest` continua passando,
  confirmando que o fix de `habilidadeComoItem` não alterou nenhum
  orçamento de Seleção/Variante já existente.
  `scripts/phase6_reliability_gate.sh` passou (mesmo WARN pré-existente
  de tamanho de `CriadorState.kt`, sem regressão nova).

Pendente, explicitamente fora desta rodada: os ids de Signo de
Nascença (ver acima) e o outro item de backlog ainda aberto
("robustecer despacho por `(id, livro)` em vez de substring de
nome").

## Quadragésima rodada — política sobre bônus situacionais de Signo (fechado, não implementar) + Garça migrada pro traço genérico de Aparar

Duas listas pedidas pelo usuário antes de mexer em código (rodada 39):
efeitos de Signo sem gancho mecânico, e a duplicação Garça/`SummaryUtils`.

### Política definida pelo usuário: bônus só-de-sessão não viram mecânica no app

Resposta direta às 2 listas: efeitos que só se aplicam "durante o jogo"
(bônus de um teste específico, bônus de Reação, penalidade numa
manobra específica etc.) **não devem ganhar implementação numérica no
app** — são coisas que o mestre/jogador aplicam na mesa, não algo que
a ficha calcula na criação do personagem. O traço continua existindo
(id + descrição, já é assim hoje via `anotacoes` em
`AncestryVariantRegistry`), só não precisa de um `RacialTraitEffect`
novo por trás.

Isso fecha a Lista 1 inteira (Tigre, Boi/Brutamontes, Lebre/Bene,
Macaco/d4+1, Raposa/Reação, Lobo/Reação Inicial, Tartaruga/Finalização,
Urso/Exausto, Serpente) como **corretas do jeito que estão** — não é
mais um "gap conhecido", é uma decisão de escopo do próprio dono do
projeto. Nenhuma mudança de código necessária.

### Garça: migrada pro traço genérico de Aparar (não mais checagem por Signo)

Confirmado o que o usuário pediu pra verificar: existe sim um traço
genérico de Aparar no catálogo (`RacialTraitPointCatalog.EFEITOS["APARAR"]
= ApararBonus(1)`, já consumido por `ModifierEngine` pra qualquer
outra raça) — Garça só não passava por ele porque `SummaryUtils.calcAparar()`
tinha um `if` dedicado (`signoIdFromNome(...) == "GARCA"`) em vez de ler o
traço de verdade.

Achado no caminho, investigando a fundo: `SummaryUtils.buildSummaryLines()`
**não é** um pipeline "só do PDF" como uma rodada anterior tinha
registrado — é a ÚNICA fonte do valor "Aparar" mostrado tanto no PDF
quanto na aba Resumo do app ao vivo (`ResumoSection.kt` chama
`rememberSummarySections` → `buildSummaryLines`). `ModifierEngine`'s
`ApararBonus`/`ModifierTarget.PARRY` (o caminho "certo") na verdade não
é lido por NENHUMA tela hoje — `SummaryUtils` sempre foi a única
implementação de verdade, com sua própria checagem paralela em vez de
usar o catálogo genérico.

**Corrigido**: `garcaParryBonus` (hardcoded) virou `racialTraitApararBonus`
— soma `RacialTraitPointCatalog.efeitoDe(hab.resolvedTraitId(), hab.targetRef, hab.value)`
sobre `(ancestralidadeAtual ?: ancestralidadeNomeObj)?.habilidades`,
somando `efeito.valor * vezes` pra qualquer `ApararBonus` encontrado —
sem checar Signo, sem checar raça, só o traço de verdade.

**Achado de quebra, corrigindo um segundo gap nunca notado**: Tanukimimi
(Arte da Guerra) tem "Aparar Baixo" (id=`APARAR_BAIXO`,
`category=racial_trait_negative`) — o `apararBaixoMod` já existente só
lê `desvantagensRaciais` (populada só por Complicações CONCEDIDAS, não
por traços simples negativos), então esse -1 nunca aparecia em nenhum
Aparar mostrado antes desta rodada. O novo scan genérico cobre positivo
E negativo pelo mesmo mecanismo, então corrige os dois de graça — sem
ter sido pedido especificamente, só uma consequência de fazer certo.

### Verificação

- Teste existente da Garça (`SummaryUtilsTest`) atualizado: em vez de
  simular a raça com `signoAdgSelecionado="Garça"` e nenhum traço em
  `habilidades[]` (testava o atalho hardcoded, não o mecanismo real),
  agora passa a raça já com o traço `id="APARAR"` — como
  `currentAncestryDef` traria de verdade depois da resolução do Signo.
- Teste novo pra Tanukimimi: prova que o mecanismo é genérico (funciona
  pra qualquer raça com um traço `ApararBonus`, positivo ou negativo),
  não só pra Garça.
- Suite completa (30 arquivos, 220 testes) e
  `scripts/phase6_reliability_gate.sh` passando (mesmo WARN
  pré-existente de tamanho de `CriadorState.kt`, sem regressão nova).

Único item de backlog ainda em aberto: "robustecer despacho por
`(id, livro)` em vez de substring de nome" (~25 pontos em
`CriadorState.kt`/`ResolveAncestrySpecificAdjustmentsUseCase.kt`),
adiado a pedido do usuário pra depois desta sessão.

## Quadragésima primeira rodada — bug real de verdade no Modo Auditoria: "exclusivo desta raça" calculado com o catálogo errado

Relatado pelo usuário testando o app de verdade (screenshot do build
mais recente da CI): no Modo Auditoria, Androides "Construto"
(`id=CONSTRUTO`, +8 pts) aparecia marcado `exclusivo-desta-raça`,
mesmo Golens (Fantasia) usando o mesmo id pro mesmo efeito mecânico —
o usuário desconfiou certo. **Minha primeira verificação (rodada
anterior a esta) deu falso negativo**: rodei `calcularIdsExclusivos`
contra o catálogo INTEIRO carregado direto do disco, que corretamente
não marca `CONSTRUTO` como exclusivo — mas isso não é o caminho que o
APP DE VERDADE usa. O usuário insistiu, com evidência (build fresco
da Action de CI, refeito do zero) — voltei a investigar a fundo em
vez de aceitar meu primeiro resultado.

### Causa raiz

`AncestralidadesSection.kt` chamava `calcularIdsExclusivos(state.listaAncestralidadesJson)`
— mas `state.listaAncestralidadesJson` **não é o catálogo inteiro**,
é só as raças dos livros LIGADOS NA SESSÃO ATUAL (o carregador de
dados filtra por `ancestryVisibleOrigins` antes de popular esse
campo — comportamento certo pra tudo mais que usa esse campo, ex.: a
lista de raças pra escolher). Com só o Básico ativo (sem Fantasia),
Golens nunca chega a ser carregado — então, do ponto de vista da
função (que está correta: só marca exclusivo quando exatamente 1
raça usa o id), `CONSTRUTO` realmente só aparecia numa raça NAQUELE
catálogo filtrado. O bug não é na função, é em qual catálogo ela
recebe: "exclusivo desta raça" é uma pergunta sobre o JOGO INTEIRO
(pra quem audita balanceamento entre raças), não sobre quais livros
estão ligados agora — variar com a sessão do jogador é o oposto do
que uma ferramenta de auditoria deveria fazer.

### Correção

`AncestralidadesSection.kt` ganhou um `catalogoAncestralidadesBruto`
carregado À PARTE, direto de `ancestralidades.json` via
`context.loadJsonAsset<List<RacialModifier>>(...)` (mesmo padrão já
usado ali mesmo pra `basico_habilidades_raciais.json`), sem nenhum
filtro de compêndio — só pra alimentar `calcularIdsExclusivos`.
`origem` fica sempre "BASICO" (valor default de `RacialModifier`,
já que o JSON usa `livros: List<String>` em vez de `origem: String`
neste carregamento direto) — irrelevante aqui, a função só olha
nome da raça + id do traço. Fallback pro catálogo filtrado
(`state.listaAncestralidadesJson`) se o load falhar por algum motivo,
em vez de quebrar o Modo Auditoria inteiro.

### Verificação

- Novo teste (`AncestralidadeCatalogBudgetTest`): confirma que
  Androides e Golens realmente compartilham `id=CONSTRUTO` no
  catálogo oficial, e que `calcularIdsExclusivos` alimentada com o
  catálogo completo nunca marca esse id como exclusivo — não cobre o
  wiring em si (fora do alcance de um teste puro-JVM sem Context/
  Compose), mas trava a premissa de dados que o fix depende, pra
  travar se algum dia os dois ids se desalinharem sem ninguém notar.
- `scripts/phase6_reliability_gate.sh`: pegou um falso-positivo real
  que eu mesmo introduzi no primeiro rascunho do comentário
  (mencionava "DataLoader." em prosa, casando com o grep de "uso
  direto de DataLoader fora do repositório") — reescrito sem esse
  padrão de texto; o gate é quem pegou, não uma revisão manual.
- Suite completa (30 arquivos, 221 testes) e o gate passando.

Lição registrada: minha primeira resposta ao usuário (rodada
anterior) testou a função certa com o catálogo errado e concluiu
"não é bug" — o usuário insistiu com evidência real em vez de aceitar
a primeira resposta, e estava certo. Quando o relato vem de teste
real no app (não hipotético), vale re-investigar o CAMINHO DE DADOS
de verdade (qual catálogo/estado o código de produção realmente usa),
não só a lógica da função isolada.

## Quadragésima segunda rodada — racial_hindrance/racial_edge: três bugs reais no caminho de Vantagem/Complicação automática

O usuário testou um Avianos no Modo Auditoria e viu `category=racial_hindrance`
pra "Não Sabe Nadar", e perguntou se isso era "legado do método antigo" —
`category="racial_hindrance"`/`"racial_edge"` (+ opcionalmente
`traitId="RACIAL_HINDRANCE"`/`"GRANTED_EDGE"` + `targetRef` quando o traço é
reskinado) **não é legado**: é o mecanismo ATUAL, o mesmo já usado por todo o
resto do app (RacialCaracteristicasResolver, ApplyAncestryChangeCoordinatorUseCase,
CriadorState.aplicarAncestralidade). Não existe um sistema "de traços" separado
que devesse substituí-lo — os traços SÃO esse mecanismo. Mas investigar a fundo,
a pedido do usuário, achou três bugs reais nesse caminho (confirmados por
inspeção de código, não hipotéticos) — os três corrigidos nesta rodada, mantendo
`category`/`severity` como mecanismo (decisão explícita do usuário: "vamos
continuar usando os racial_hindrance, então, severity, etc").

### Bug 1 — rótulo de auditoria perdia o LABEL/catálogo de um traço migrado pro par genérico

`RacialTraitAuditFormatter.formatarUm()` buscava `LABEL`/catálogo oficial pela
chave RESOLVIDA (`hab.resolvedTraitId()` — `traitId ?: id`). Efeito colateral
não percebido da migração de Atributo/Perícia Aumentada (rodada 39, que deu
`traitId="ATTRIBUTE_BOOST"/"SKILL_BOOST"` a ~27 ids, mantendo o `id` original só
pra identidade/auditoria): qualquer um desses traços perdia o match do seu
LABEL real (cadastrado sob o id ORIGINAL, ex. "SENTIDOS_AGUCADOS") porque a
busca agora ia por "ATTRIBUTE_BOOST"/"SKILL_BOOST", que não tem entrada
própria — caía no ramo "sem LABEL nem catálogo" mesmo tendo um label real.
Corrigido: `LABEL`/catálogo oficial/`CUSTOS` agora buscam por `hab.id` cru
(com fallback pro id resolvido só se `id` for nulo/vazio); `resolvedTraitId()`
continua sendo o que decide o EFEITO mecânico (`efeitoDe`) e o cabeçalho.

### Bug 2 — `resolvedDesvantagens()` nunca lia `targetRef`, ao contrário do irmão `resolvedVantagensGratis()`

`RacialModifier.resolvedVantagensGratis()`/`resolvedDesvantagens()` reimplementavam,
com uma cópia própria e divergente, a mesma leitura que as funções de topo de
arquivo `vantagensGratisEfetivas()`/`desvantagensEfetivas()` já faziam CORRETO
(inclusive com o dedup por `racialGrantDedupeKey()`, que a versão em método não
tinha). A cópia do método tinha uma assimetria: o ramo `category=="racial_edge"`
de `resolvedVantagensGratis()` já priorizava `targetRef` sobre `hab.nome`
(`hab.targetRef ?: hab.id ?: hab.nome`), mas o ramo equivalente de
`resolvedDesvantagens()` (`category=="racial_hindrance"`) nunca olhava
`targetRef`, só `hab.nome` — uma Complicação reskinada por `targetRef` (ex.:
Draconianos "Mal-Humorado" concedendo a Complicação real "Arrogante") virava a
desvantagem automática com o NOME DE EXIBIÇÃO da raça em vez do nome real do
catálogo de Complicações, quebrando silenciosamente o vínculo com
`complicacoes.json` — exatamente o risco que o usuário descreveu (traço sem
link explícito pro catálogo real). Esses dois métodos são o caminho REAL usado
por `CriadorState.aplicarAncestralidade()` e `ApplyAncestryChangeCoordinatorUseCase`
pra conceder Vantagem/Complicação automática — não é auditoria, é mecânica de
personagem de verdade. Corrigido fazendo os dois métodos DELEGAREM pras funções
de topo de arquivo já corretas, em vez de manter duas implementações divergentes.

### Bug 3 — GRANTED_EDGE cobrava sempre 2 pontos fixos, nunca o Estágio real da Vantagem

`RacialTraitPointCatalog.custoDe("GRANTED_EDGE", ...)` retornava `2` fixo,
ignorando que o custo de uma Vantagem de catálogo deveria escalar pelo Estágio
dela (Novato=2/Experiente=3/Veterano=4/Heroico ou Lendário=5) — fórmula que já
existia, correta, mas SÓ no editor de Variante custom
(`ResolveVariantPointBudgetUseCase.custoDeAdicionarVantagem`), nunca alimentada
de volta pro cálculo de ponto de uma raça OFICIAL. Mesmo achado, forma
hardcoded, na aba Ancestralidades: `RacialCaracteristicasResolver.resolver()`
tinha um `formatPts(2)` literal pra toda linha "Vantagem Racial:", em vez de
olhar o Estágio de verdade. **Hoje isso não muda nenhum número visível** (as 3
Vantagens concedidas por `GRANTED_EDGE` no catálogo atual — Kitsunemimi
"Cativar o Ambiente", Tanukimimi "Impulso", Demônios "aa_demonio" — são todas
Novato, cost=2 de qualquer jeito), mas é um bug real de cálculo, não uma
hipótese: uma raça futura concedendo uma Vantagem Experiente+ cobraria errado
sem essa correção. Extraída a fórmula pra um único lugar
(`RacialTraitPointCatalog.custoDeVantagem(vantagem: Vantagem)`), com
`ResolveVariantPointBudgetUseCase.custoDeAdicionarVantagem` agora delegando pra
lá em vez de manter uma segunda cópia. `custoDe()` ganhou três parâmetros
opcionais (`targetRef`, `nome`, `allVantagens`, todos com default seguro —
chamadas que não os passam mantêm o fallback fixo de 2, sem regressão) pra
resolver QUAL Vantagem foi concedida (por id primeiro — mais robusto contra
reskin/acento/pontuação, ex. `targetRef="aa_demonio"` — com fallback por nome)
e cobrar o Estágio real dela. Threaded o catálogo de Vantagens
(`state.listaVantagens`) até os pontos reais de cálculo que já tinham acesso a
ele: `RacialCaracteristicasResolver.resolver()` (aba Ancestralidades e Monstro
Heroico), `RacialTraitAuditFormatter.formatar()` (Modo Auditoria),
`ResolveVariantPointBudgetUseCase.itensRemoviveisDe/valorTotalDe/habilidadeComoItem`
e `ValidateAncestryOptionBudgetsUseCase.execute()` (editor de Variante custom,
orçamento de cada opção de raça com Seleção).

### Verificação

- `RacialModifierGrantsTest.kt` (novo): prova que `resolvedDesvantagens()`
  agora prioriza `targetRef` igual `resolvedVantagensGratis()` já fazia, e que
  sem `targetRef` o comportamento de antes (nome cru + severidade) continua
  intacto.
- `RacialTraitPointCatalogTest.kt`: `custoDeVantagem` escalando Novato(2) até
  Heroico/Lendário(5); `custoDe("GRANTED_EDGE", ...)` cobrando o custo real
  quando o catálogo de Vantagens é passado (por `targetRef`-como-id, por
  `targetRef`-como-nome e por fallback no `nome` do traço), e mantendo o
  fallback fixo de 2 quando não é passado (sem regressão nos ~205 ids já
  calibrados).
- `RacialTraitAuditFormatterTest.kt`: traço com `traitId="ATTRIBUTE_BOOST"` +
  `id` com LABEL real agora acha o label (antes caía em "sem LABEL nem
  catálogo"); `GRANTED_EDGE` com uma Vantagem Experiente no catálogo mostra
  "+3 pts", não "+2 pts".
- Suite completa (32 arquivos, ~230 testes) e
  `scripts/phase6_reliability_gate.sh` passando (mesmo WARN pré-existente de
  tamanho de `CriadorState.kt`, sem regressão nova).

Migração de `targetRef` explícito nos ~90 traços `racial_hindrance`/~13
`racial_edge` que hoje só têm `category` (sem `traitId`/`targetRef`, casando
com o catálogo real só via `hab.nome`) fica de fora desta rodada: hoje esses
traços já usam como `nome` o nome real da Complicação/Vantagem (não há skin
pra desfazer), então o link "explícito" já existe via nome — o risco
concreto que o usuário descreveu (link perdido por reskin) só se materializa
nos casos COM skin, que já têm `targetRef` ou foram cobertos pelos 3 bugs
acima. Backlog, não bug: migrar esses ids também pra `targetRef` deixaria a
identificação robusta contra futura mudança de nome de exibição, mas não
corrige nenhum comportamento errado hoje.

## Quadragésima terceira rodada (planejamento) — sistema de Tropo genérico, unificando Arte da Guerra e Monstro Heroico

Pedido do usuário: desistir do sistema de Tropo do Arte da Guerra "no formato
que está" e do sistema de Monstro Heroico "no formato que está", e construir
UM sistema de Tropo genérico, utilizável por qualquer livro, que os dois
migrem para. Registrado aqui antes de começar a implementar, como pedido —
nenhum código foi alterado nesta rodada além da limpeza de sujeira abaixo.

### O receio de fundo (e por que ele já está resolvido no motor)

O medo do usuário: raça dá d6 numa perícia, Tropo também bonifica a mesma
perícia, e o sistema de recálculo erra a conta (fica em d10 fantasma, ou
zera) quando o jogador troca de raça depois. Investigação confirma que isso
**não é um risco estrutural hoje**: `CriadorState.atributoBaseRacial()` e
`periciaStartRawInternal()` são funções PURAS, recalculadas do zero a cada
chamada, lendo simultaneamente `currentAncestryDef.habilidades`,
`getMonstroSelecionado()` e `tropoSelecionado` — nunca somam
incrementalmente nem guardam um valor combinado em cache. Um comentário no
próprio código confirma que esse exato cenário (raça dá d6, Protagonista
soma +1 tipo em cima) já foi resolvido e testado: "a pedido do usuário: o
bônus do tropo não é o mesmo que perícia de raça que pode aumentar o valor
máximo do teto dela" — ou seja, bônus de Tropo nunca estica o teto de
compra de pontos (`pisoSemTropo`/`includeTropo=false` alimenta só o teto;
o bônus de Tropo soma por cima só na exibição final). Esse é o contrato de
segurança que o novo sistema genérico PRECISA preservar exatamente como
está — não inventar um mecanismo novo, só parar de restringir esse mecanismo
já correto a `compendioArteDaGuerraAtivo`.

### Estado real dos dois sistemas hoje (achado por 2 agentes de pesquisa)

**Monstro Heroico já é quase o sistema genérico que o usuário quer.** Zero
`if` por id de monstro específico em todo o Kotlin — Vampiro, Lobisomem,
Anjo etc. não têm nenhuma lógica bespoke, tudo passa por `habilidades[]`/
`atributosBonus` genéricos (incluindo Vantagem grátis do Monstro de Retalhos,
já usando `category="racial_edge"`/`traitId="GRANTED_EDGE"`, o mesmo padrão
de raça). Não trava Ancestralidade (aditivo: raça normal + template por
cima — nunca houve gate em `isSectionEnabled`). Dois gaps reais pra caber
100% no modelo genérico: `atributosBonus` ainda é `Map<String,Int>` solto
(3 pontos de conversão manual no código hoje, fácil de dobrar em
`habilidades[]` de verdade) e `complicacoes` é texto livre sem vínculo com o
catálogo real de Complicações (mesmo problema que a rodada 42 resolveu pra
raça). Achado cruzado importante: 61 Vantagens em `vantagens.json`
(categoria Monstruosas) referenciam os 8 ids de template hoje via campo
`template`/`templatesRequired` — se os ids mudarem, esse vínculo precisa
migrar junto.

**Tropo (Arte da Guerra) é mais primitivo como dado E menos implementado do
que o livro sugere.** `Tropo.kt` ainda usa o padrão antigo
(`periciasGratuitas: Map<String,Int>`, `ganhaAoComprar: List<String>`, sem
`habilidades[]`). O "sistema" de verdade é ~50 `if (tropoSelecionado?.id ==
"tropo_X")` espalhados pelo `CriadorState.kt`. Mas ao ler CADA um dos 8
tropos linha por linha, boa parte do que o LIVRO descreve nunca virou
código — é só texto na UI (`TroposSection.kt`), sem nenhum efeito numérico:
Posturas de Combate do Samurai (8 opções, todas sem efeito), Talento Shinobi
(só "Místico" tem código; Alteração/Pés Leves/Passo das Sombras são só
texto), Ferramentas do Kui (as 3 são só texto — Kui é o Tropo menos
implementado dos 8, só concede slots de Técnica Chi de verdade), Caminho
Sagrado + transe do Bu Xista (texto only), Histórico da Arma do Youxia
(texto only), as 14 Técnicas do Artista Marcial (checklist sem efeito), e 9
dos 12 resultados da tabela de Habilidades do Protagonista. Migrar "tudo que
existe DE VERDADE" é bem menor que migrar "tudo que o livro descreve" — ver
decisão D1 abaixo.

O que ESTÁ implementado reduz a 3 mecanismos genéricos reutilizáveis,
compartilhados por vários Tropos:

1. **Desbloqueio de banco de poderes (slot) por Tropo** — "TECNICAS CHI"
   (Samurai/Shinobi/Youxia/Bu Xista/Kui) e "TECNICAS ELEMENTAIS"
   (Elementalista) são pseudo-Antecedentes-Arcanos que reaproveitam toda a
   infraestrutura de poderes já existente, com a contagem de slots vindo de
   uma fórmula pequena (`tecnicasIniciaisFromTropo`). Isso já é o mecanismo
   de MAIOR valor pra generalizar.
2. **Slot de Vantagem grátis filtrado por predicado** — o slot do
   Protagonista (filtra por categoria conforme o resultado do dado) e o
   slot de combate do Samurai são estruturalmente idênticos a DOIS
   mecanismos que já existem fora de Tropo (`pathfinderFreeSlotId`,
   `vantagemAdaptavelSelecionadaId`) — bom candidato a virar UM primitivo
   "FreeAdvantageSlot(predicado)" compartilhado pelos 4, não só um recurso
   de Tropo.
3. **Bônus de perícia/atributo "relativo" (+1 tipo em cima do que já tem)
   vs "piso fixo"** — já quase genérico: o mapa `periciasGratuitas` do
   JSON já é lido sem checar qual Tropo é (`periciaStartRawInternal`,
   trecho final da função). Só falta o schema conseguir expressar
   "relativo" vs "fixo" e "alvo escolhido pelo jogador" vs "alvo fixo" (esse
   último já existe pronto — é o mesmo `TARGET_ATTRIBUTE_OR_SKILL` que a
   Seleção de raça já usa pra Kitsunemimi/Usagimimi/Gnomo).

Dois Tropos ficam genuinamente bespoke mesmo depois de generalizar os 3
mecanismos acima:

- **Protagonista**: não é um "grant", é uma mini-mecânica própria — 5
  tabelas de rolagem independentes (Técnicas/Perícia/Vantagem/Qualidades de
  Herói/Habilidades), cada resultado despachando pra um sistema diferente
  (perícia, atributo, Vantagem, Passo, slot de poder). Fica como lógica
  dedicada por cima do motor genérico, do mesmo jeito que hoje um traço
  bem específico de uma raça (ex.: Draconianos) já fica como exceção
  pontual em cima do motor genérico de raça — não é regressão, é o mesmo
  padrão que o resto do app já usa pra uma raça com regra própria.
- **Samurai**: a regra de pular o pré-requisito de Estágio em Vantagens de
  Liderança quando Conhecimento de Batalha ≥ d8 (`shouldIgnoreLeadershipStage`)
  é uma exceção de regra de verdade, referenciada em 3 arquivos — fica como
  está, hardcoded por id, em cima do motor novo.

Achado à parte (não bloqueia o desenho, mas precisa de decisão): existe um
9º Tropo no JSON (`tropo_mon`, "forma um vínculo com um Mon...") sem
NENHUMA referência no Kotlin — dado morto/abandonado. E existe um SEGUNDO
arquivo de Tropo (`crystal_tropos.json`, do livro Crystal Heart, ids
`treino_schultz`/`treino_mira`/`treino_yara`/`treino_leighmya`) usando um
campo `atributos_bonus` que nem existe no `Tropo.kt` atual — hoje
provavelmente descartado silenciosamente pelo parser. O novo sistema
genérico precisa decidir o que fazer com os dois.

### Desenho proposto

**Tipo de dado único** (substitui `Tropo.kt` E `MonstroTemplate.kt`):
`Tropo(id, nome, categoria, origem, descricao, descricaoLite, habilidades:
List<RacialAbility>)` — o MESMO tipo `RacialAbility` (com
`category`/`traitId`/`targetRef`/`severity`) que raça já usa, não um tipo
paralelo. Isso é o que garante, por construção, que o motor de cálculo
usado por Tropo seja o MESMO motor já testado e correto de raça — não uma
cópia. Três acréscimos novos ao vocabulário de `RacialTraitEffect`/
`traitId` (usados por raça também, se algum dia fizer sentido, mas
motivados por Tropo):
- Um flag "relativo" no efeito de Atributo/Perícia (soma ao que já existe
  em vez de definir um piso).
- Um `traitId` novo tipo "ARCANE_SLOT_GRANT" (targetRef = chave do
  Antecedente Arcano pseudo, ex. "TECNICAS CHI"; value = fórmula/quantidade
  de slots).
- Um `traitId` novo tipo "FREE_ADVANTAGE_SLOT" (targetRef = filtro —
  categoria ou lista — de quais Vantagens o slot aceita), generalizando
  também `pathfinderFreeSlotId`/`vantagemAdaptavelSelecionadaId` pro mesmo
  mecanismo.

**Gate de ativação genérico**: substitui as ~50 checagens de
`compendioArteDaGuerraAtivo` em `isSectionEnabled`/`periciaStartRawInternal`/
`atributoBaseRacial`/`ModifierEngine` por uma flag computada
`modoTroposAtivo = compendioArteDaGuerraAtivo || tropoSistemaHabilitadoManualmente`
— a primeira metade nunca desliga (regra obrigatória do livro), a segunda é
um toggle de regra exposto pra qualquer livro (ex.: Wise Guys), independente
de Ancestralidade estar ativa ou não naquele livro. A trava de Ancestralidade
continua exatamente como hoje (só quando `tropoSelecionado != null`) — em
livros que já escondem a aba de Ancestralidade por padrão (Wise Guys), a
trava é automaticamente inócua, nada a fazer de especial aí.

**Monstro Heroico vira Tropo com `categoria="MONSTRO"`, `origem="HORROR"`** —
mesmos ids (`vampiro`, `lobisomem` etc., sem renomear) pra não quebrar os 61
vínculos `templatesRequired` em `vantagens.json`. Continua SEM travar
Ancestralidade (a trava de `isSectionEnabled` só liga quando o Tropo
selecionado pede — Monstro nunca pediu, e não precisa passar a pedir).

**Criação customizada**: novo tipo "Tropo" no dropdown de
`SettingsDialog.kt` (mesmo padrão já usado por "Traço Racial"/"Variante de
Raça" — armazenamento por livro via `CustomStorageManager`), reaproveitando
o MESMO editor de traço já existente pra montar `habilidades[]`. O exemplo
do usuário (Tropo "Trombadinha" pro Wise Guys) só precisa dos blocos mais
simples (Vantagem/Complicação/perícia grátis) — nenhum Tropo customizado
precisa dos mecanismos 1/2 acima pra funcionar, esses só existem pros 8
Tropos oficiais do livro.

### Fases (ordem de execução recomendada)

1. **Infraestrutura aditiva**: novo tipo `Tropo` unificado + os 3
   acréscimos ao vocabulário de efeito, SEM mudar nenhum comportamento
   visível ainda (dados antigos continuam funcionando em paralelo).
2. **Migrar Monstro Heroico** pro novo tipo — menor risco (já quase 100%
   compatível), sem mudar comportamento (continua aditivo, sem travar
   raça). Apaga `MonstroTemplate.kt`/`modoMonstroAtivo`/
   `tipoMonstroSelecionado` no final da fase, substituídos pelo
   `tropoSelecionado` unificado.
3. **Gate de ativação genérico** — `modoTroposAtivo`, toggle de regra pra
   qualquer livro. Nesta fase o toggle já existe pra todo livro, mas só
   Arte da Guerra e Horror têm Tropos de catálogo pra oferecer (os outros
   ficam com a lista vazia até ter conteúdo customizado).
4. **Migrar os 8 Tropos do Arte da Guerra** pros 3 mecanismos genéricos +
   as 2 exceções bespoke (Protagonista, Samurai) replugadas no novo motor.
   Apaga `Tropo.kt` antigo (`periciasGratuitas`/`ganhaAoComprar`) e os ~50
   `if (tropoSelecionado?.id == ...)` no final da fase.
5. **Criação customizada de Tropo** em `SettingsDialog.kt`.
6. **Regressão completa** (suite + `phase6_reliability_gate.sh`) + nova
   rodada de auditoria documentando o resultado final.

### Decisões que preciso da sua confirmação antes de começar a codificar

- **D1 — Paridade, não expansão**: pra tudo que o livro descreve mas nunca
  foi implementado (Posturas do Samurai, Talentos do Shinobi além de
  Místico, etc.), a migração carrega o TEXTO adiante sem criar mecânica
  nova nenhuma — mesmo estado de hoje (só descrição, sem efeito). Construir
  essas mecânicas de verdade viraria um projeto à parte, bem maior, depois
  desta migração fechar. Minha recomendação: sim, só paridade agora. Você
  concorda, ou quer que algum desses vire mecânica de verdade já nesta
  leva?
- **D2 — `tropo_mon` (dado morto) e `crystal_tropos.json` (livro Crystal
  Heart, schema incompatível)**: apago o `tropo_mon` órfão como parte da
  limpeza (nunca foi usado), e trato Crystal Heart como fora de escopo
  desta migração (fica pra depois, registrado como backlog)? Ou você quer
  que eu já cheque se dá pra encaixar os dois no desenho novo?
- **D3 — Confirmação do nome/mecanismo do toggle**: "sistema de Tropos"
  como opção de regra em QUALQUER livro (Resumo/Regras da tela de criação),
  independente da Ancestralidade estar ativa naquele livro ou não — é assim
  que você imaginou, ou você quer o toggle em outro lugar da UI?

Nenhuma edição de código feita nesta rodada além da limpeza de sujeira
registrada acima (SummaryUtils.kt/ModifierEngine.kt).
