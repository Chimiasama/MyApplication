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
