# Reliability Inventory

- Date (UTC): 2026-02-16T16:29:45Z
- Commit: 9496423
- Branch: work

## Critical file sizes
  4645 app/src/main/java/com/example/swadebuilder/CriadorState.kt
  1135 app/src/main/java/com/example/swadebuilder/MainActivity.kt
   501 app/src/main/java/com/example/swadebuilder/model/DataLoader.kt
  1440 app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt
  7721 total

## Global mutable declarations (listas + mapas de domínio)
global_list_count=0
global_map_count=0

## Direct DataLoader usage outside repository

## CriadorState hotspots (line anchors)
2731:    fun podeSelecionar(v: Vantagem): Boolean {
3021:    fun aplicarAncestralidade(anc: String, feedbackMessages: MutableList<String>) {
4118:    fun rebuildAllPericiaStacks(

## Key architectural markers
GameDataRepository=1
GameDataStore=1
RulesResolver=1
UseCases=37

## Test inventory
tests_unitarios=138
