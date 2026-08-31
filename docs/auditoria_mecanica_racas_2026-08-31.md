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

## Pendente — precisa de decisão antes de corrigir

1. **Tamanho (Size) não lido por id, 21 raças**: `TAMANHO_MENOS_1`,
   `TAMANHO_MAIS_1/2/3`, `DIMINUTO`, `PEQUENOS`, `GRANDE` existem como id no
   JSON, mas `ModifierEngine` calcula Tamanho com regex sobre nome/descrição
   do traço, não lendo esses ids. Funciona hoje só porque o nome do traço
   embute o valor ("TAMANHO +1"). Afeta: Pequeninos (Básico/Fantasia/Horror/
   Super), Centauros, Fadas, Gnomos (Fantasia e Pathfinder), Goblins, Golens,
   Meio-Gigantes, Minotauros, Ogros, Orcs, Povo Ratazana/Rato, Halfling
   (Pathfinder), Aurax, Centaux, Elementais, Yetis (Sci-Fi).
2. **Movimentação racial positiva só por regex, 3 raças, sem rede de
   segurança**: Centauros (+4), Centaux (+2), Aurax (+2) — nenhuma tem
   `movimentacao` estruturado nem entrada em
   `ResolveAncestrySpecificAdjustmentsUseCase`; dependem 100% do regex
   genérico `MOVIMENTACAO\s*\+(\d+)` sobre o nome do traço.
3. **Resistência com valor variável só por regex, 2 raças**: Drakens (+2),
   Soldados Genéticos (+1) — ids `RESISTENCIA_2`/`RESISTENCIA_1` existem mas
   não estão em `EFEITOS` (só o id singular `RESISTENCIA`, valor fixo +1,
   está mapeado).
4. **Armadura Natural funciona, mas por nome da raça, não por id do
   traço**: Sáurios, Draconianos, Golens, Insetoides têm o valor certo via
   `ResolveAncestrySpecificAdjustmentsUseCase` (`when (ancKey) { "SAURIOS"
   -> ... }`), não pelo id do traço "Armadura +2" (`ARMADURA_2`, que também
   não está em `EFEITOS`). Resultado certo, mecanismo do jeito que se pediu
   pra eliminar.
5. **Hardcode residual por nome de raça**: `CriadorState.kt:4457-4459`
   (exceção Meio-Orc/Intimidar do Pathfinder) e `CriadorState.kt:4310,4329`
   (Drakens/Elementais, escolha de atributo por Variante) ainda comparam
   `ancestralidade.keyify().contains(...)` em vez de id.

## Como usar este arquivo

Cada item pendente é independente — dá pra atacar um de cada vez. O padrão
de correção do item 1 (Tamanho) é: adicionar um caso `TamanhoBonus` em
`RacialTraitEffect`, popular `EFEITOS` pros 7 ids de Tamanho, e trocar o
bloco de regex em `ModifierEngine.kt:153-256` pra ler o efeito por id (só
cai no regex/fallback por nome quando não há id — mesmo padrão já usado no
loop de `ResistenciaBonus/PassoBonus/ApararBonus`). Os itens 2 e 3 seguem o
mesmo padrão, só que criando entradas em `EFEITOS` pros ids que faltam em
vez de um case novo.
