# Fase 5 — kickoff (limpeza UI / hardening)

## Objetivo
Mover helpers de regra restantes da camada de UI (`MainActivity`) para domínio/ViewModel e manter a Activity focada em composição/orquestração.

## Lista explícita desta rodada
1. Identificar helper de regra ainda presente em `MainActivity`.
2. Extrair o helper para use case puro na camada de domínio.
3. Conectar a UI para consumir o use case extraído.
4. Cobrir o novo use case com teste unitário.

## Execução
- [x] Item 1 concluído (`buildUsageInstructions` identificado em `MainActivity`).
- [x] Item 2 concluído (`BuildUsageInstructionsUseCase`).
- [x] Item 3 concluído (UI passou a montar `Input` e delegar ao use case).
- [x] Item 4 concluído (`BuildUsageInstructionsUseCaseTest`).
