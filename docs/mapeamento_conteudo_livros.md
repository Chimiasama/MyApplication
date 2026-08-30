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

> Preenchido pelos agentes de mapeamento. Ao terminar cada um, a linha vira
> um link + um resumo de 1 linha (contagem de entradas / faltas).

| BookId | Índice | Status |
|---|---|---|
| `BASICO` | [`book_index/basico.md`](reports/book_index/basico.md) | em processamento |
| `FANTASIA` | [`book_index/fantasia.md`](reports/book_index/fantasia.md) | em processamento |
| `SCIFI` | [`book_index/scifi.md`](reports/book_index/scifi.md) | em processamento |
| `HORROR` | [`book_index/horror.md`](reports/book_index/horror.md) | em processamento |
| `SUPERPODERES` | [`book_index/superpoderes.md`](reports/book_index/superpoderes.md) | em processamento |
| `PATHFINDER_BASICO` / `PATHFINDER_COMPENDIO` | [`book_index/pathfinder.md`](reports/book_index/pathfinder.md) | em processamento |
| `DEADLANDS_BASICO` / `DEADLANDS_COMPENDIO` | [`book_index/deadlands.md`](reports/book_index/deadlands.md) | em processamento |
| `ADG_BASICO` / `ADG_DIARIO_KUI` | [`book_index/adg.md`](reports/book_index/adg.md) | em processamento |
| `CRYSTAL_HEART` / `CRYSTAL_HEART_MUITOS_CORACOES` | [`book_index/crystal_heart.md`](reports/book_index/crystal_heart.md) | em processamento |
| `CSV_LIVRO_CRIADOR` / `CSV_LIVRO_MORTAIS` / `CSV_MOVIMENTO_VERMELHO` | [`book_index/csv.md`](reports/book_index/csv.md) | em processamento |
| `WISEGUYS` | [`book_index/wiseguys.md`](reports/book_index/wiseguys.md) | em processamento |

## Auditoria de hardcode (nome-texto em vez de ID)

Relatórios separados, cobrindo o código Kotlin de domínio:

| Relatório | Escopo | Status |
|---|---|---|
| [`reports/hardcode_audit_vantagens_complicacoes.md`](reports/hardcode_audit_vantagens_complicacoes.md) | Vantagens, Complicações, Antecedente Arcano (incl. variantes por livro) | em processamento |
| [`reports/hardcode_audit_atributos_pericias_equipamento_ancestralidade.md`](reports/hardcode_audit_atributos_pericias_equipamento_ancestralidade.md) | Atributos, Perícias, Equipamento, Ancestralidade | em processamento |

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

## Como este documento evolui

Depois que os índices por livro e as auditorias de hardcode estiverem
prontos, o próximo passo é transformar os achados `[FALTA]`/`[CONFERIR]`
em itens de trabalho concretos (parecido com
`docs/racial_trait_migration_plan.md`, mas para Vantagens/Complicações/
Perícias/Atributos/Equipamento) — cada item vira um PR pequeno que troca
comparação por nome por comparação por `id`, sem mudar comportamento
visível, com testes de regressão antes/depois.
