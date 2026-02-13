# Reliability Inventory

- Date (UTC): 2026-02-13T20:26:44Z
- Commit: 4b5e174
- Branch: work

## Critical file sizes
  4902 app/src/main/java/com/example/swadebuilder/CriadorState.kt
  1180 app/src/main/java/com/example/swadebuilder/MainActivity.kt
   505 app/src/main/java/com/example/swadebuilder/model/DataLoader.kt
  1439 app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt
  8026 total

## Global mutable declarations (listas + mapas de domínio)
global_list_count=11
global_map_count=3
app/src/main/java/com/example/swadebuilder/MainActivity.kt:125:var arcanoInfo by mutableStateOf<Map<String, Triple<Int, Int, String>>>(emptyMap())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1137:var listaComplicacoes by mutableStateOf<List<Complicacao>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1139:var listaCoracoesCrystal by mutableStateOf<List<CrystalHeart>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1151:var listaAncestralidadesJson by mutableStateOf<List<RacialModifier>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1152:var listaMonstroTemplates by mutableStateOf<List<MonstroTemplate>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1154:var racialAttrMinMap by mutableStateOf<Map<String, Map<String,Int>>>(emptyMap())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1155:var racialSkillStartMap by mutableStateOf<Map<String, Map<String,Int>>>(emptyMap())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1157:var listaAtributos by mutableStateOf<List<String>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1158:var mapaAtributosDisplay by mutableStateOf<Map<String, String>>(emptyMap())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1160:var listaPericias by mutableStateOf<List<Pericia>>(emptyList())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1161:var mapaPericias by mutableStateOf<Map<String, Pericia>>(emptyMap())
app/src/main/java/com/example/swadebuilder/MainActivity.kt:1162:var mapaAtributosDescricao by mutableStateOf<Map<String, String>>(emptyMap())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:12:var listaVantagens by mutableStateOf<List<Vantagem>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:13:var listaTropos by mutableStateOf<List<Tropo>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:14:var listaEquipamentos by mutableStateOf<List<EquipamentoItem>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:15:var listaPoderes by mutableStateOf<List<Poder>>(emptyList())
app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt:20:var listaSuperPoderes by mutableStateOf<List<SuperPoder>>(emptyList())

## Direct DataLoader usage outside repository

## CriadorState hotspots (line anchors)
2640:    fun podeSelecionar(v: Vantagem): Boolean {
3163:    fun aplicarAncestralidade(anc: String, feedbackMessages: MutableList<String>) {
4331:    fun rebuildAllPericiaStacks(

## Key architectural markers
GameDataRepository=1
GameDataStore=1
RulesResolver=1
UseCases=26

## Test inventory
tests_unitarios=123
