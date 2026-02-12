# Fase 4 — kickoff (Strategy por cenário)

## Objetivo
Reduzir regras espalhadas por `if/else` no `CriadorViewModel` criando estratégias de cenário (`GameRules`) e um resolver central.

## Lista explícita desta rodada
1. Expandir `GameRules` para cobrir defaults de cenário além de dinheiro inicial.
2. Cobrir default de ancestralidade via estratégia de cenário.
3. Cobrir vantagens mandatórias e coração inicial de Crystal Heart via estratégia.
4. Aplicar o resolver no fluxo real de reset do personagem.
5. Atualizar testes do resolver com os novos contratos.

## Execução
- [x] Item 1 concluído.
- [x] Item 2 concluído.
- [x] Item 3 concluído.
- [x] Item 4 concluído.
- [x] Item 5 concluído.

## Resultado
- `GameRules` agora define:
  - `startingResources()`
  - `defaultAncestralidade()`
  - `mandatoryAdvantageIds()`
  - `defaultCrystalHeartId()`
- `RulesResolver` ganhou suporte explícito a `compendioCrystalHeartAtivo`.
- `CriadorViewModel.resetStateParaNovoPersonagem(...)` usa `selectedRules` para:
  - ancestralidade inicial,
  - dinheiro/carteira inicial,
  - vantagens mandatórias por cenário,
  - coração inicial no Crystal Heart.
