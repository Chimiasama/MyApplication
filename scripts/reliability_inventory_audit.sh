#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

search_count() {
  local pattern="$1"
  shift
  if command -v rg >/dev/null 2>&1; then
    rg -n "$pattern" "$@" | wc -l
  else
    grep -R -n -E "$pattern" "$@" | wc -l
  fi
}

search_lines() {
  local pattern="$1"
  shift
  if command -v rg >/dev/null 2>&1; then
    rg -n "$pattern" "$@" || true
  else
    grep -R -n -E "$pattern" "$@" || true
  fi
}

OUT="docs/reports/reliability-inventory-latest.md"

{
  echo "# Reliability Inventory"
  echo
  echo "- Date (UTC): $(date -u +'%Y-%m-%dT%H:%M:%SZ')"
  echo "- Commit: $(git rev-parse --short HEAD)"
  echo "- Branch: $(git branch --show-current)"
  echo
  echo "## Critical file sizes"
  wc -l \
    app/src/main/java/com/example/swadebuilder/CriadorState.kt \
    app/src/main/java/com/example/swadebuilder/MainActivity.kt \
    app/src/main/java/com/example/swadebuilder/model/DataLoader.kt \
    app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt
  echo
  echo "## Global mutable declarations (listas + mapas de domínio)"
  echo "global_list_count=$(search_count '^var[[:space:]]+lista[^[:space:]]+[[:space:]]+by[[:space:]]+mutableStateOf<List<' app/src/main/java/com/example/swadebuilder/MainActivity.kt)"
  echo "global_map_count=$(search_count '^var[[:space:]]+mapa[^[:space:]]+[[:space:]]+by[[:space:]]+mutableStateOf<Map<' app/src/main/java/com/example/swadebuilder/MainActivity.kt)"
  search_lines '^var[[:space:]]+(lista|mapa|racial|arcano)[^[:space:]]+[[:space:]]+by[[:space:]]+mutableStateOf<(List|Map)<' app/src/main/java/com/example/swadebuilder/MainActivity.kt
  echo
  echo "## Direct DataLoader usage outside repository"
  search_lines 'DataLoader\.' app/src/main/java/com/example/swadebuilder | grep -v 'model/GameDataRepository.kt' || true
  echo
  echo "## CriadorState hotspots (line anchors)"
  search_lines 'fun (podeSelecionar\(|aplicarAncestralidade\(|rebuildAllPericiaStacks\()' app/src/main/java/com/example/swadebuilder/CriadorState.kt
  echo
  echo "## Key architectural markers"
  echo "GameDataRepository=$(search_count 'interface GameDataRepository' app/src/main/java/com/example/swadebuilder/model/GameDataRepository.kt)"
  echo "GameDataStore=$(search_count 'class GameDataStore' app/src/main/java/com/example/swadebuilder/model/GameDataStore.kt)"
  echo "RulesResolver=$(search_count 'class RulesResolver' app/src/main/java/com/example/swadebuilder/model/rules/GameRules.kt)"
  echo "UseCases=$(find app/src/main/java/com/example/swadebuilder/model/usecase -maxdepth 1 -name '*UseCase.kt' | wc -l | tr -d ' ')"
  echo
  echo "## Test inventory"
  echo "tests_unitarios=$(search_count '@Test' app/src/test/java)"
} > "$OUT"

cat "$OUT"
