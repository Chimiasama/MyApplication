# Reliability Inventory

- Date (UTC): 2026-02-13T11:45:01Z
- Commit: 6698903
- Branch: codex/plano-confiabilidade-total

## Critical file sizes
  4976 app/src/main/java/com/example/swadebuilder/CriadorState.kt
  1180 app/src/main/java/com/example/swadebuilder/MainActivity.kt
   505 app/src/main/java/com/example/swadebuilder/model/DataLoader.kt
  1439 app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt
  8100 total

## Global mutable list declarations
count=5
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1137:var listaComplicacoes by mutableStateOf<List<Complicacao>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1160:var listaPericias by mutableStateOf<List<Pericia>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:12:var listaVantagens by mutableStateOf<List<Vantagem>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:14:var listaEquipamentos by mutableStateOf<List<EquipamentoItem>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:15:var listaPoderes by mutableStateOf<List<Poder>>(emptyList())

## Direct DataLoader usage outside repository

## Key architectural markers
GameDataRepository=1
GameDataStore=1
RulesResolver=1
UseCases=19

## Test inventory
tests_unitarios=97
