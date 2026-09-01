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
