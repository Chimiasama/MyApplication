# Plano Unificado de Execução — Confiabilidade e Modernização Arquitetural

## 1) Objetivo único
Centralizar neste documento **todo o plano histórico + ajustes + próximos passos** para eliminar dispersão e acelerar execução com rastreabilidade.

---

## 2) Histórico consolidado (o que já foi feito)

### Fase 0 — Baseline e diagnóstico inicial
- Métricas base de arquivos críticos, testes e acoplamentos estabelecidas.
- Scripts de baseline e inventário operacional criados.
- Resultado: ponto de partida mensurável para monitorar regressões.

### Fase 1 — Repositório/Snapshot de dados
- Introdução de `GameDataRepository`, `AssetGameDataRepository`, `GameDataSnapshot`, `GameDataStore`.
- Carga de dados com sanitização e validações de integridade.
- Resultado: fronteira de dados explícita e mais testável.

### Fase 2 — Kickoff de desacoplamento
- Início da separação de responsabilidades entre UI/State/Domain.
- Preparação de trilhas para extrações incrementais com rollback simples.

### Fase 3 — Kickoff de extrações de domínio
- Extrações iniciais para use-cases focados, com testes de contrato.
- Redução de lógica crítica em blocos monolíticos de estado.

### Fase 4 — Strategy por cenário (concluída)
- `GameRules` e `RulesResolver` cobrindo políticas de cenário.
- Defaults e visibilidade por compêndio migrados para domínio.
- Resultado: regras de cenário centralizadas e previsíveis.

### Fase 5 — Limpeza UI/Activity (concluída)
- Helpers de regra removidos da `MainActivity` e movidos para domínio/utilitários:
  - `BuildUsageInstructionsUseCase`
  - `DiceExtensions`
  - `ProgressionSlotRules`
- Resultado: camada de UI mais fina e com menos risco de regressão funcional.

### Fase 6 — Gate de confiabilidade (concluída)
- `scripts/phase6_reliability_gate.sh` como guard-rail de regressão.
- Baseline e inventário operacional integrados ao fluxo.
- Resultado: verificação rápida e automatizada para impedir retrocessos conhecidos.

---

## 3) Execução incremental do hotspot crítico (`aplicarAncestralidade`)

### E4.1 → E4.6 (concluídas)
- Extrações progressivas para use-cases de transição humana, atributos, pacote racial, complicações automáticas e validação de inválidas.

### E4.7 (concluída — fechamento)
Extração da orquestração completa para coordinator dedicado, com subetapas:
- **E4.7a**: `ResolveAncestryTransitionBootstrapUseCase`
- **E4.7b**: `ResolveAncestryRacialPackageUseCase`
- **E4.7c**: `ResolveAncestryInvalidAdvantagesUseCase`
- **E4.7d**: `ResolveAncestryComplicationsSnapshotUseCase`
- **E4.7e (fechamento)**: `ApplyAncestryChangeCoordinatorUseCase` consolidando o fluxo fim-a-fim.

### Resultado técnico da E4.7
- `CriadorState.aplicarAncestralidade(...)` passou a **consumir um contrato único de domínio** para quase toda a lógica de negócio.
- O state mantém somente aplicação de side-effects inevitáveis (estado mutável/UI-driven).
- Testes de contrato adicionados para cada bloco extraído + coordinator final.

---

## 4) Ajustes adicionais já incorporados no ciclo
- Correções de origem de conteúdo do compêndio Sol a Vapor no JSON de vantagens, alinhando exibição por origem canônica.
- Evolução contínua dos testes de contrato dos use-cases novos.
- Atualizações incrementais de documentação de execução e marcos.

---

## 5) Estado atual (resumo executivo)
- ✅ Fases 0–6: concluídas e estabilizadas.
- ✅ Hotspot #1 (`aplicarAncestralidade`): concluído com coordinator completo (E4.7 fechado).
- ✅ Fase 7 (`rebuildAllPericiaStacks`): concluída (`RebuildSkillStacksUseCase`).
- ✅ Fase 8 (`podeSelecionar`): concluída (`ValidateSelectionUseCase`).
- ✅ Fase 9 (`GameDataStore`): concluída.
- ✅ Fase 10 (Hardening de CI): concluída.

---

## 6) Plano futuro (o que falta, por que e como)

## Fase 7 — Extração do hotspot #2 (`rebuildAllPericiaStacks`) (CONCLUÍDA)
- **Status:** Entregue via `RebuildSkillStacksUseCase`.
- A lógica de cálculo de custos, stacks e limites de pool foi extraída para um UseCase puro.
- `CriadorState` agora delega a reconstrução para o domínio.

## Fase 8 — Extração do hotspot #3 (`podeSelecionar`) (CONCLUÍDA)
- **Status:** Entregue via `ValidateSelectionUseCase`.
- A validação de vantagens foi quebrada em validadores granulares (`ValidateRequirementsUseCase`, `ValidatePrerequisiteUseCase`, etc.).
- `podeSelecionar` no state agora é um wrapper fino.

## Fase 9 — Convergência de fonte de verdade (B2/B3) (CONCLUÍDA)
- **Status:** Entregue via refatoração de UI e `GameDataStore`.
- `DataLoader` agora é puro e retorna snapshot.
- `UnifiedScreen` e ViewModels consomem dados injetados via `GameDataStore`.
- Uso direto de globais removido da camada de UI principal.

## Fase 10 — Hardening de CI e alertas de warnings (CONCLUÍDA)
- **Status:** Concluído.
- Correção de warnings de compilador (Unnecessary safe call, Elvis operator, Deprecated APIs).
- Remoção de plugins e flags Gradle obsoletos.
- Atualização do Gate de Confiabilidade para prevenir regressão de arquitetura.

---

## 7) Ordem de execução recomendada (curto prazo)
1. Fase 9 (convergência de fonte de verdade) - **CONCLUÍDA**
2. Fase 10 (hardening de warnings) - **CONCLUÍDA**

---

## 8) Regras de operação daqui pra frente
1. Mudanças em lotes consistentes por fase (não micro-fragmentar sem necessidade).
2. Cada fase deve sair com:
   - código,
   - teste de contrato,
   - atualização deste documento,
   - execução do gate.
3. Este é o **único documento oficial de plano** do repositório.

---

## 9) Status vivo
- Última atualização: Conclusão Fases 9 e 10. Projeto finalizado.

## 10) Conclusão e Próximos Passos
O plano de refatoração para confiabilidade e modernização arquitetural foi executado com sucesso.
- **Confiabilidade:** Gate de regressão implementado e ativo no CI.
- **Arquitetura:** Lógica de negócio extraída de `CriadorState` para UseCases testáveis (`RebuildSkillStacks`, `ValidateSelection`, `ApplyAncestry`).
- **Dados:** Fonte de verdade unificada em `GameDataStore`/`Snapshot`, desacoplando a UI de variáveis globais.
- **Qualidade:** Redução significativa de warnings e limpeza de código legado.

**Recomendação de Manutenção:**
- Monitorar novos warnings em atualizações de dependências.
- Manter o script `phase6_reliability_gate.sh` no pipeline de CI.
- Continuar a migração gradual de quaisquer referências legadas remanescentes (se houver) para `GameDataStore`.
