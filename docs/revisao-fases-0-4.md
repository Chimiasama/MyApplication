# Revisão consolidada: Fases 0 → 6

## Fase 0 — Baseline e segurança

### Lista de revisão
1. Confirmar que baseline script ainda roda.
2. Confirmar que o script segue no repositório para comparação futura.

### Execução
- [x] Item 1 concluído (`./scripts/phase0_baseline_metrics.sh`).
- [x] Item 2 concluído (script preservado e utilizado na rodada).

---

## Fase 1 — Repository / snapshot

### Lista de revisão
1. Garantir integridade mínima do snapshot no boundary de carregamento.
2. Falhar cedo ao detectar inconsistências críticas de IDs.

### Execução
- [x] Item 1 concluído com `ValidateGameDataSnapshotIntegrityUseCase`.
- [x] Item 2 concluído com validação no `AssetGameDataRepository.load(...)`.

---

## Fase 2 — Separação de estado e domínio

### Lista de revisão
1. Extrair mais uma regra de filtragem/seleção do estado para use case puro.
2. Cobrir a regra extraída com teste unitário.

### Execução
- [x] Item 1 concluído com `ResolveActiveAncestryCandidatesUseCase` e uso no `CriadorState`.
- [x] Item 2 concluído com `ResolveActiveAncestryCandidatesUseCaseTest`.

---

## Fase 3 — Remoção de magic strings

### Lista de revisão
1. Consolidar pontos restantes de normalização/IDs em fluxo crítico de dados.
2. Testar a validação de integridade adicionada para impedir regressão silenciosa.

### Execução
- [x] Item 1 concluído via validação de IDs no carregamento.
- [x] Item 2 concluído com `ValidateGameDataSnapshotIntegrityUseCaseTest`.

---

## Fase 4 — Strategy por cenário

### Lista de execução
1. Criar contrato inicial `GameRules` para regras de cenário.
2. Implementar estratégias base para recursos iniciais (Base/Fantasia/SciFi/Deadlands/Pathfinder).
3. Implementar `RulesResolver` com precedência de módulos.
4. Aplicar o resolver em fluxo real do app (`CriadorViewModel.resetStateParaNovoPersonagem`).
5. Cobrir o resolver com teste unitário.
6. Expandir estratégia de cenário para defaults de ancestralidade/vantagens mandatórias/coração inicial.
7. Expandir Strategy por cenário para políticas de visibilidade de vantagens (AA/lista proibida/categoria PODER).

### Execução
- [x] Item 1 concluído.
- [x] Item 2 concluído.
- [x] Item 3 concluído.
- [x] Item 4 concluído.
- [x] Item 5 concluído.
- [x] Item 6 concluído (continuação da Fase 4 em `docs/fase-4-kickoff.md`).
- [x] Item 7 concluído (continuação da Fase 4 em `docs/fase-4-kickoff.md`).


---

## Fase 5 — Limpeza UI e hardening

### Lista de execução
1. Identificar helpers de regra remanescentes em `MainActivity.kt`.
2. Extrair instruções de uso para use case puro.
3. Extrair helpers genéricos (dado/estágio por slot) para arquivos dedicados fora da Activity.
4. Integrar UI com os componentes extraídos.
5. Cobrir o novo use case com teste unitário.
6. Atualizar documentação de revisão/checkpoint com status final da fase.

### Execução
- [x] Item 1 concluído.
- [x] Item 2 concluído.
- [x] Item 3 concluído.
- [x] Item 4 concluído.
- [x] Item 5 concluído.
- [x] Item 6 concluído.

---

## Fase 6 — Confiabilidade operacional e gate de regressão

### Lista de execução
1. Definir checklist executável de confiabilidade mínima pós-Fase 5.
2. Implementar gate automatizado com falha rápida para regressões críticas.
3. Cobrir no gate: extrações da Activity, resources críticos e cobertura mínima do use-case de instruções.
4. Executar baseline da Fase 0 dentro do gate.
5. Atualizar checkpoint documental com status final da fase.

### Execução
- [x] Item 1 concluído.
- [x] Item 2 concluído (`scripts/phase6_reliability_gate.sh`).
- [x] Item 3 concluído.
- [x] Item 4 concluído.
- [x] Item 5 concluído.
