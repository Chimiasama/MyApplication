#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_root"

count_matches() {
  local pattern="$1"
  shift

  if command -v rg >/dev/null 2>&1; then
    (rg -n "$pattern" "$@" || true) | wc -l
  else
    (grep -R -n -E "$pattern" "$@" || true) | wc -l
  fi
}

printf "# Baseline de métricas (Fase 0)\n"
printf "\n"
printf -- "- Data: %s\n" "$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
printf -- "- Commit: %s\n" "$(git rev-parse --short HEAD)"
printf "\n"
printf "## Tamanho de arquivos críticos\n"
wc -l \
  app/src/main/java/com/example/swadebuilder/CriadorState.kt \
  app/src/main/java/com/example/swadebuilder/MainActivity.kt \
  app/src/main/java/com/example/swadebuilder/model/DataLoader.kt
printf "\n"
printf "## Quantidade de testes unitários\n"
count_matches "@Test" app/src/test/java | awk '{print "tests_unitarios=" $1}'
printf "\n"
printf "## Pontos de acoplamento por globais\n"
count_matches "var lista(Pericias|Vantagens|Complicacoes|Poderes|Equipamentos) by mutableStateOf" app/src/main/java/com/example/swadebuilder/MainActivity.kt | awk '{print "globais_lista_mutaveis=" $1}'
