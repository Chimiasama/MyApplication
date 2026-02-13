# Relatório definitivo — modernização Fases 1 a 6

## Resumo executivo
Este relatório consolida, em alta confiabilidade, o que foi implementado nas Fases 1–6, impactos técnicos/funcionais, riscos residuais e pontos de atenção operacional.

Conclusão: **não há pendência crítica aberta das Fases 1–5** no recorte de arquitetura definido no plano. A Fase 6 também está operacional, com gate automatizado e integração em CI.

---

## Fase 1 — Repository / Snapshot / Store
### O que foi feito
- Contrato explícito de carregamento (`GameDataRepository`) e snapshot imutável (`GameDataSnapshot`).
- Implementação `AssetGameDataRepository` para centralizar carregamento e validar integridade.
- Introdução de `GameDataStore` para acesso por snapshot com fallback controlado.

### Impacto
- Reduz acoplamento de UI/estado com loader global.
- Cria fronteira testável de dados.

### Risco / inconsistência possível
- Fallback legado ainda existe por transição (esperado).
- Mitigação: gate da Fase 6 impede regressão óbvia de acoplamento em pontos críticos.

---

## Fase 2 — Extração de domínio para use-cases
### O que foi feito
- Extração de regras críticas para use-cases puros (validação de investimento, cálculos e resoluções de dependências).
- `CriadorViewModel` passou a orquestrar comportamento usando esses use-cases.

### Impacto
- Menor complexidade procedural no ViewModel/State.
- Melhor testabilidade isolada de regras de negócio.

### Risco / inconsistência possível
- Pode haver regras residuais ainda no estado legado fora do escopo inicial.
- Mitigação: checklist incremental e cobertura de testes por fluxo crítico.

---

## Fase 3 — Redução de magic strings
### O que foi feito
- Catálogo de IDs de domínio (`DomainIds`).
- Validações de integridade para detectar problemas de IDs cedo.

### Impacto
- Menor risco de regressões por typo/string solta.

### Risco / inconsistência possível
- Catálogo pode não cobrir 100% de literais históricas ainda não migradas.
- Mitigação: evolução incremental + validações de integridade.

---

## Fase 4 — Strategy por cenário
### O que foi feito
- `GameRules` + implementações por cenário + `RulesResolver`.
- Políticas de default e visibilidade por cenário migradas para estratégia.

### Impacto
- Evolução de cenários sem espalhar condicionais em muitos pontos.
- Comportamento mais previsível e mais fácil de validar.

### Risco / inconsistência possível
- Ordem de precedência entre módulos precisa permanecer explícita e testada.
- Mitigação: testes do resolver + centralização no contrato.

---

## Fase 5 — Limpeza UI e hardening
### O que foi feito
- Helpers removidos da `MainActivity` para artefatos dedicados (`BuildUsageInstructionsUseCase`, `DiceExtensions`, `ProgressionSlotRules`).
- UI ajustada para consumir componentes extraídos.

### Impacto
- Activity focada em composição/fluxo.
- Reuso e testes unitários mais simples.

### Risco / inconsistência possível
- Reintrodução acidental de helper inline na Activity.
- Mitigação: gate da Fase 6 com regra de não regressão.

---

## Fase 6 — Confiabilidade operacional
### O que foi feito
- Gate `scripts/phase6_reliability_gate.sh` com fail-fast.
- Baseline robusto sem dependência obrigatória de `rg`.
- CI alinhada com JDK 21 e execução do gate antes do build.
- Guard-rails adicionais:
  - bloqueio de `Categoria.SOCIAL` (enum inválido) em favor de `Categoria.SOCIAIS`;
  - bloqueio de uso direto de `DataLoader` fora do repositório.

### Impacto
- Regressões recorrentes passam a falhar cedo no pipeline.
- Menor dependência de inspeção manual para problemas já conhecidos.

### Risco / inconsistência possível
- Gate é intencionalmente enxuto (não substitui a suíte Gradle completa).
- Mitigação: manter build/test completo no workflow após gate.

---

## Incidente Supers (crash) — análise e correção
### Causa raiz
- Duplicados legítimos de conteúdo por sobreposição de módulos (ex.: perícias/vantagens/poderes) estavam sendo tratados como erro fatal de integridade.

### Correção aplicada
- Sanitização no `AssetGameDataRepository` antes da validação:
  - dedupe por chave normalizada (`keyify`)
  - política "prefer last" para preservar override de módulo específico
  - reconstrução de `mapaPericias` coerente com lista sanitizada

### Efeito
- Evita crash no carregamento ao abrir Supers quando há sobreposição esperada de dados.

---

## Confiabilidade atual (estado final)
- Fases 1–5: **sem pendência crítica aberta** dentro do escopo do plano.
- Fase 6: **implementada e operante** com gate e integração em CI.
- Risco residual principal: warnings/deprecações não bloqueantes do ecossistema (AGP/Kotlin/Compose/AndroidX) que não impedem build atual, mas exigem backlog técnico para médio prazo.

## Recomendação final
1. Manter gate da Fase 6 como obrigatório em PR.
2. Tratar backlog de warnings/deprecações por lotes (sem misturar com bugfix funcional).
3. Continuar removendo fallback legado conforme cobertura de testes cresça.
