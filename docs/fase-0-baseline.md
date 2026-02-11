# Fase 0 — baseline e segurança

Este documento implementa a **Fase 0** do plano de modernização:

1. cobertura inicial de fluxos críticos com testes de regressão;
2. definição de cenários-ouro (fixtures) para comparação futura;
3. baseline de métricas para acompanhar evolução da refatoração.

## 1) Testes de regressão adicionados

Arquivo: `app/src/test/java/com/example/swadebuilder/phase0/Phase0CriticalFlowsTest.kt`

Cenários-ouro cobertos:
- **Round-trip de snapshot** preservando pilhas e seleções críticas de criação.
- **Rebuild de perícias com limite de pool** para garantir que `pontosPericia` não fica negativo.
- **Troca de ancestralidade** aplicando base racial de perícia (`HUMANOS` -> `ELFOS` para `ATLETISMO`).

Esses cenários servem como “golden tests” para comparar comportamento durante as próximas fases.

## 2) Baseline de métricas

Script: `scripts/phase0_baseline_metrics.sh`

Métricas iniciais registradas pelo script:
- linhas de arquivos críticos (`CriadorState`, `MainActivity`, `DataLoader`);
- quantidade total de testes unitários (`@Test`);
- contagem de globais mutáveis de listas de domínio em `MainActivity`.

## 3) Como executar

```bash
./gradlew testLiteDebugUnitTest --tests "com.example.swadebuilder.phase0.Phase0CriticalFlowsTest"
# opcional (full flavor)
./gradlew testFullDebugUnitTest --tests "com.example.swadebuilder.phase0.Phase0CriticalFlowsTest"
./scripts/phase0_baseline_metrics.sh
```

## 4) Critério de saída da Fase 0 (atendido)

- [x] Fluxos sensíveis cobertos por testes de regressão iniciais.
- [x] Cenários-ouro definidos para comparação de comportamento.
- [x] Baseline de métricas instrumentado e executável no repositório.
