# Fase 6 — confiabilidade operacional e gate de regressão

## Objetivo
Consolidar a modernização das fases anteriores com um **gate de confiabilidade reprodutível** (rápido, local e CI-friendly), reduzindo risco de regressão silenciosa entre UI, use-cases e documentação de fases.

## Lista explícita completa da Fase 6
1. Definir checklist executável de confiabilidade mínima pós-Fase 5.
2. Implementar script de gate que falha cedo quando requisitos críticos não forem atendidos.
3. Cobrir no gate os contratos críticos de Fase 5 (extrações da Activity, testes dos helpers/use-case e resources obrigatórios).
4. Integrar baseline de métricas da Fase 0 no gate para manter rastreabilidade.
5. Atualizar os checkpoints de revisão/plano com o status da Fase 6.

## Execução
- [x] Item 1 concluído.
- [x] Item 2 concluído (`scripts/phase6_reliability_gate.sh`).
- [x] Item 3 concluído (validações de extração da Activity, testes/strings/chamadas críticas).
- [x] Item 4 concluído (execução de `scripts/phase0_baseline_metrics.sh` dentro do gate).
- [x] Item 5 concluído (`docs/revisao-fases-0-4.md` e `docs/plano-modernizacao-arquitetura.md`).

## Resultado
- A Fase 6 estabelece uma verificação objetiva e automatizada para impedir retrocessos nos pontos mais sensíveis da refatoração.
- O projeto passa a ter um comando único de auditoria rápida da modernização já entregue.
