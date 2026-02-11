#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$repo_root"

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
rg -n "@Test" app/src/test/java | wc -l | awk '{print "tests_unitarios=" $1}'
printf "\n"
printf "## Pontos de acoplamento por globais\n"
rg -n "var lista(Pericias|Vantagens|Complicacoes|Poderes|Equipamentos) by mutableStateOf" app/src/main/java/com/example/swadebuilder/MainActivity.kt | wc -l | awk '{print "globais_lista_mutaveis=" $1}'
