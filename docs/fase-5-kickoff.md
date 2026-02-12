# Fase 5 — kickoff (limpeza UI / hardening)

## Objetivo
Mover responsabilidades de regra da camada de UI para domínio/arquitetura de suporte, deixando a `MainActivity` mais focada em composição e fluxo de tela.

## Lista explícita completa da Fase 5 (escopo atual)
1. Identificar helpers de regra ainda acoplados ao arquivo `MainActivity.kt`.
2. Extrair instruções dinâmicas de uso para um use case puro.
3. Extrair helpers genéricos de domínio (formatação de dado e regra de estágio por slot) para arquivos dedicados fora da Activity.
4. Atualizar a UI para consumir exclusivamente os componentes extraídos.
5. Cobrir os itens novos com teste unitário (use case).
6. Atualizar documentação de revisão/checkpoint com status da Fase 5.

## Execução
- [x] Item 1 concluído.
- [x] Item 2 concluído (`BuildUsageInstructionsUseCase`).
- [x] Item 3 concluído (`DiceExtensions.kt`, `ProgressionSlotRules.kt`).
- [x] Item 4 concluído (MainActivity delega e não mantém helpers antigos).
- [x] Item 5 concluído (`BuildUsageInstructionsUseCaseTest`).
- [x] Item 6 concluído (`docs/revisao-fases-0-4.md` e `docs/plano-modernizacao-arquitetura.md`).

## Resultado
- `MainActivity.kt` deixou de carregar helpers utilitários/regras de domínio que não pertencem à composição da tela.
- Regras reutilizáveis agora estão em componentes dedicados e testáveis.
