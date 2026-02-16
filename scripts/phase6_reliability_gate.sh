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

search_any() {
  local pattern="$1"
  shift

  if command -v rg >/dev/null 2>&1; then
    rg -n "$pattern" "$@" >/dev/null
  else
    grep -R -n -E "$pattern" "$@" >/dev/null
  fi
}

# 1) Artefatos obrigatórios do plano unificado
require_file "docs/plano-unificado-execucao-confiabilidade.md"
require_file "app/src/main/java/com/example/swadebuilder/model/usecase/BuildUsageInstructionsUseCase.kt"
require_file "app/src/main/java/com/example/swadebuilder/DiceExtensions.kt"
require_file "app/src/main/java/com/example/swadebuilder/ProgressionSlotRules.kt"
require_file "app/src/test/java/com/example/swadebuilder/model/usecase/BuildUsageInstructionsUseCaseTest.kt"
require_file "app/src/test/java/com/example/swadebuilder/DiceExtensionsTest.kt"
require_file "app/src/test/java/com/example/swadebuilder/ProgressionSlotRulesTest.kt"

# 2) Garantias de não-regressão da extração de helpers da Activity
if search_any "fun buildUsageInstructions\\(" app/src/main/java/com/example/swadebuilder/MainActivity.kt; then
  fail "MainActivity voltou a declarar buildUsageInstructions inline"
fi
pass "MainActivity não contém helper inline buildUsageInstructions(...)"

# 3) Garantias de wiring do use-case e resources exigidos
search_any "BuildUsageInstructionsUseCase" app/src/main/java/com/example/swadebuilder/MainActivity.kt \
  || fail "MainActivity não referencia BuildUsageInstructionsUseCase"
pass "MainActivity referencia BuildUsageInstructionsUseCase"

search_any "sw_supers_book_title|sw_monsters_book_title" app/src/main/res/values/strings.xml \
  || fail "Strings obrigatórias de livros não encontradas em strings.xml"
pass "Strings de livros Supers/Monstros presentes"

# 4) Garantia de cobertura mínima do cenário de monstro no teste do use-case
search_any "modoMonstroAtivo[[:space:]]*=[[:space:]]*true|Livro de Monstros|Monstros" \
  app/src/test/java/com/example/swadebuilder/model/usecase/BuildUsageInstructionsUseCaseTest.kt \
  || fail "Teste de instruções não cobre explicitamente cenário de modo monstro"
pass "Teste do use-case cobre modo monstro"

# 5) Baseline continua executável (rastreabilidade de evolução)
./scripts/phase0_baseline_metrics.sh >/dev/null
pass "Baseline da Fase 0 executou com sucesso"

# 6) Guard-rails adicionais de regressão conhecidos
if search_any "Categoria\.SOCIAL\b" app/src/test/java app/src/main/java; then
  fail "Encontrada referência inválida a Categoria.SOCIAL (use Categoria.SOCIAIS)"
fi
pass "Sem referência inválida a Categoria.SOCIAL"

if command -v rg >/dev/null 2>&1; then
  data_loader_hits="$(rg -n "DataLoader\." app/src/main/java/com/example/swadebuilder -g'*.kt' | grep -v "model/GameDataRepository.kt" || true)"
else
  data_loader_hits="$(grep -R -n -E "DataLoader\." app/src/main/java/com/example/swadebuilder --include='*.kt' | grep -v "model/GameDataRepository.kt" || true)"
fi

if [[ -n "${data_loader_hits}" ]]; then
  fail "Uso direto de DataLoader fora do repositório detectado"
fi
pass "Sem uso direto de DataLoader fora do repositório"


# 7) Drift control (strict): sem variáveis globais mutáveis de domínio na Activity
GLOBAL_MUTABLE_PATTERN='^var[[:space:]]+(lista|mapa|racial|arcano)[^[:space:]]+[[:space:]]+by[[:space:]]+mutableStateOf<(List|Map)<'
if command -v rg >/dev/null 2>&1; then
  global_mutable_count="$((rg -n "${GLOBAL_MUTABLE_PATTERN}" \
    app/src/main/java/com/example/swadebuilder/MainActivity.kt || true) | wc -l | tr -d ' ')"
else
  global_mutable_count="$((grep -n -E "${GLOBAL_MUTABLE_PATTERN}" \
    app/src/main/java/com/example/swadebuilder/MainActivity.kt || true) | wc -l | tr -d ' ')"
fi

if [[ "${global_mutable_count}" -ne 0 ]]; then
  fail "Encontradas variáveis globais mutáveis de domínio em MainActivity (${global_mutable_count} != 0)"
fi
pass "Sem variáveis globais mutáveis de domínio em MainActivity"

criador_state_lines="$(wc -l app/src/main/java/com/example/swadebuilder/CriadorState.kt | awk '{print $1}')"
CRIADOR_STATE_WARN_THRESHOLD=5100
if [[ "${criador_state_lines}" -gt "${CRIADOR_STATE_WARN_THRESHOLD}" ]]; then
  echo "[phase6][WARN] CriadorState.kt acima do limiar de atenção (${criador_state_lines} > ${CRIADOR_STATE_WARN_THRESHOLD})" >&2
else
  pass "CriadorState.kt dentro do limiar de atenção (${criador_state_lines} <= ${CRIADOR_STATE_WARN_THRESHOLD})"
fi

# 8) Presença de testes críticos de contrato
require_file "app/src/test/java/com/example/swadebuilder/model/GameDataRepositorySanitizationTest.kt"
require_file "app/src/test/java/com/example/swadebuilder/model/CriadorViewModelGameDataSnapshotTest.kt"
require_file "app/src/test/java/com/example/swadebuilder/model/rules/RulesResolverTest.kt"


# 9) Extinção de legado: arquivos removidos não podem reaparecer
[[ ! -f "app/src/main/java/com/example/swadebuilder/GameDataGlobals.kt" ]] || fail "Arquivo legado reapareceu: GameDataGlobals.kt"
[[ ! -f "app/src/main/java/com/example/swadebuilder/AppData.kt" ]] || fail "Arquivo legado reapareceu: AppData.kt"
pass "Arquivos legados GameDataGlobals.kt/AppData.kt ausentes"

echo "[phase6] Reliability gate concluído com sucesso."
