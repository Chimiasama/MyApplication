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
- 🔄 Próxima prioridade: Hotspot #2 (`rebuildAllPericiaStacks`) e Hotspot #3 (`podeSelecionar`).

---

## 6) Plano futuro (o que falta, por que e como)

## Fase 7 — Extração do hotspot #2 (`rebuildAllPericiaStacks`)
### Por quê
- É núcleo de consistência de perícias e afeta múltiplos fluxos de progressão.
- Alto risco de regressão silenciosa quando alterado diretamente no state.

### O que fazer
1. Mapear subblocos determinísticos (cálculo base, bônus por origem, efeitos de complicação, ajustes de supers).
2. Criar coordinator de rebuild com contratos explícitos de entrada/saída.
3. Migrar lógica pura para use-cases e manter somente side-effects no state.
4. Cobrir com testes de contrato por subbloco + cenário integrado.

### Critério de pronto
- Método no state reduzido para adapter/orquestração mínima.
- Testes cobrindo feliz/borda/regressão conhecida.

## Fase 8 — Extração do hotspot #3 (`podeSelecionar`)
### Por quê
- É gate central de elegibilidade de vantagens; regressões impactam criação inteira.

### O que fazer
1. Separar validações por categoria: estágio, pré-requisitos, atributos/perícias, exceções por cenário.
2. Criar use-cases por domínio de validação + agregador de decisão final.
3. Remover branching espalhado e usar regras compostas.

### Critério de pronto
- Fluxo de elegibilidade totalmente testável fora do state.
- Redução de complexidade ciclomática no método original.

## Fase 9 — Convergência de fonte de verdade (B2/B3)
### Por quê
- Ainda há legado em globais; risco de drift entre snapshot/store e estado global.

### O que fazer
1. Migrar leituras remanescentes para `GameDataStore`.
2. Eliminar escrita tardia em globais (manter apenas mirror transitório quando inevitável).
3. Adicionar check de drift para impedir regressão da convergência.

### Critério de pronto
- Fluxos críticos sem dependência de globais como fonte primária.

## Fase 10 — Hardening de CI e alertas de warnings
### Por quê
- Warnings não bloqueantes acumulados podem virar erro em upgrades futuros.

### O que fazer
1. Criar backlog priorizado por risco de warnings (Kotlin/AGP/Compose/serialization).
2. Tratar por ondas pequenas sem misturar com bugfix funcional.
3. Evoluir gate para monitorar tendência (não necessariamente bloquear no início).

### Critério de pronto
- Tendência de warnings em queda + risco futuro controlado.

---

## 7) Ordem de execução recomendada (curto prazo)
1. Fase 7 (`rebuildAllPericiaStacks`)
2. Fase 8 (`podeSelecionar`)
3. Fase 9 (convergência de fonte de verdade)
4. Fase 10 (hardening de warnings)

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
- Última atualização: fechamento E4.7 + centralização documental.
- Próximo marco: iniciar Fase 7.
