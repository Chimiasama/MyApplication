# Relatório de Análise: Filtragem de Conteúdo Básico

Este relatório detalha as razões pelas quais o conteúdo do livro "Básico" (BASICO) continua aparecendo no aplicativo mesmo quando um cenário específico (como Pathfinder) está selecionado.

## Resumo
A lógica que decide se o conteúdo "Básico" deve ser ocultado está fragmentada e inconsistente através das diferentes seções do aplicativo. Algumas seções implementam a exclusão corretamente (Ancestralidades), enquanto outras usam lógica desatualizada que não reconhece os novos compêndios (Pathfinder, Sci-Fi, etc.).

## Detalhamento por Seção

### 1. Vantagens (Edges) - **Problemático**
- **Arquivo:** `model/ContentVisibility.kt`
- **Função:** `isVantagemVisible`
- **Problema:** A lógica para exibir conteúdo básico é:
  ```kotlin
  val isActive = (isBasico && (!compendioFantasiaAtivo || isGenericAB) && !compendioDeadlandsAtivo) || ...
  ```
  **Falha:** Esta verificação exclui o Básico *apenas* se Fantasia ou Deadlands estiverem ativos. Ela ignora completamente `compendioPathfinderAtivo`, `compendioSciFiAtivo`, etc.
- **Resultado:** Vantagens do Básico (incluindo Antecedentes Arcanos) aparecem misturadas com as do Pathfinder.

### 2. Equipamentos (Gear) - **Problemático**
- **Arquivo:** `ui/sections/EquipamentoSection.kt`
- **Problema:** O filtro de categorias usa uma lógica similarmente incompleta:
  ```kotlin
  (origem != "BASICO" || (!compendioFantasiaAtivo && !compendioSciFiAtivo && !state.modoSupers))
  ```
  **Falha:** Falta verificar `!compendioPathfinderAtivo` (e outros).
- **Resultado:** Categorias de equipamento do Básico (ex: "Armas Medievais") aparecem na lista. Embora exista um filtro secundário (`isItemAllowedByPathfinderRule`), ele atua dentro das categorias, mas a categoria em si pode vazar se contiver qualquer item permitido ou se a lógica falhar antes.

### 3. Poderes (Powers) - **Afetado Indiretamente**
- **Arquivo:** `ui/sections/PoderesSection.kt`
- **Funcionamento:** A lista de poderes exibida depende da *origem* do Antecedente Arcano (AA) selecionado.
- **Problema:** Como a seção de Vantagens (acima) permite selecionar AAs do Básico (ex: "Antecedente Arcano (Milagres)"), ao escolher um desses, o aplicativo carrega a lista de poderes do Básico (`basico_poderes.json`).
- **Solução:** Corrigir a seção de Vantagens resolverá o problema dos Poderes automaticamente, pois o usuário não conseguirá selecionar o AA errado.

### 4. Ancestralidades (Ancestries) - **Correto**
- **Arquivo:** `ui/sections/AncestralidadesSection.kt`
- **Lógica:** Esta seção constrói uma lista explícita de `activeOrigins`.
  ```kotlin
  if (!compendioFantasiaAtivo && ... && !compendioPathfinderAtivo) add("BASICO")
  ```
  **Sucesso:** Se *qualquer* compêndio estiver ativo, "BASICO" não é adicionado à lista. Isso explica por que essa aba funciona corretamente.

## Carregamento de Dados (Data Loading)
- **Arquivo:** `model/DataLoader.kt`
- **Estado:** O carregamento está correto. Os arquivos `pathfinder_*.json` são carregados separadamente. O problema reside inteiramente na lógica de exibição (UI) e não na estrutura dos dados.

## Recomendação
Centralizar a lógica de "Origens Ativas" em um único lugar (como `ContentVisibility.kt` ou `CriadorState`) e fazer com que todas as seções (Vantagens, Equipamentos, Perícias) consultem essa fonte única de verdade, em vez de repetir condicionais `if (!A && !B)` que ficam desatualizadas quando novos cenários são adicionados.
