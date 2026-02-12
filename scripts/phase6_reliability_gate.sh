#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "[phase6][FAIL] $1" >&2
  exit 1
}

pass() {
  echo "[phase6][OK] $1"
}

require_file() {
  local f="$1"
  [[ -f "$f" ]] || fail "Arquivo obrigatório ausente: $f"
  pass "Arquivo presente: $f"
}

# 1) Artefatos obrigatórios das fases 5 e 6
require_file "docs/fase-5-kickoff.md"
require_file "docs/fase-6-kickoff.md"
require_file "app/src/main/java/com/example/swadebuilder/model/usecase/BuildUsageInstructionsUseCase.kt"
require_file "app/src/main/java/com/example/swadebuilder/DiceExtensions.kt"
require_file "app/src/main/java/com/example/swadebuilder/ProgressionSlotRules.kt"
require_file "app/src/test/java/com/example/swadebuilder/model/usecase/BuildUsageInstructionsUseCaseTest.kt"
require_file "app/src/test/java/com/example/swadebuilder/DiceExtensionsTest.kt"
require_file "app/src/test/java/com/example/swadebuilder/ProgressionSlotRulesTest.kt"

# 2) Garantias de não-regressão da extração de helpers da Activity
if rg -n "fun buildUsageInstructions\(" app/src/main/java/com/example/swadebuilder/MainActivity.kt >/dev/null; then
  fail "MainActivity voltou a declarar buildUsageInstructions inline"
fi
pass "MainActivity não contém helper inline buildUsageInstructions(...)"

# 3) Garantias de wiring do use-case e resources exigidos
rg -n "BuildUsageInstructionsUseCase" app/src/main/java/com/example/swadebuilder/MainActivity.kt >/dev/null \
  || fail "MainActivity não referencia BuildUsageInstructionsUseCase"
pass "MainActivity referencia BuildUsageInstructionsUseCase"

rg -n "sw_supers_book_title|sw_monsters_book_title" app/src/main/res/values/strings.xml >/dev/null \
  || fail "Strings obrigatórias de livros não encontradas em strings.xml"
pass "Strings de livros Supers/Monstros presentes"

# 4) Garantia de cobertura mínima do cenário de monstro no teste do use-case
rg -n "modoMonstroAtivo\s*=\s*true|Livro de Monstros|Monstros" \
  app/src/test/java/com/example/swadebuilder/model/usecase/BuildUsageInstructionsUseCaseTest.kt >/dev/null \
  || fail "Teste de instruções não cobre explicitamente cenário de modo monstro"
pass "Teste do use-case cobre modo monstro"

# 5) Baseline continua executável (rastreabilidade de evolução)
./scripts/phase0_baseline_metrics.sh >/dev/null
pass "Baseline da Fase 0 executou com sucesso"

echo "[phase6] Reliability gate concluído com sucesso."
