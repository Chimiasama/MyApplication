# Plano mestre de confiabilidade total (execução guiada)

## Objetivo
Criar um plano exaustivo para elevar a confiabilidade do app ao máximo viável, reduzindo regressões arquiteturais e funcionais com validações automáticas, critérios de aceite claros e execução incremental segura.

## Escopo
Este plano cobre:
- estabilização arquitetural pós-fases 1–6;
- eliminação progressiva de fontes duplicadas de verdade;
- redução de riscos no carregamento/sanitização de dados de domínio;
- cobertura de testes e guard-rails de CI;
- documentação operacional para execução rastreável.

Não cobre, nesta etapa, redesign de UI/UX.

---

## Premissas e restrições
1. O sistema está em estado híbrido (snapshot + globais legadas) e precisa de migração segura, sem big-bang.
2. Confiabilidade é priorizada sobre velocidade.
3. Cada melhoria deve ter:
   - risco conhecido,
   - mitigação,
   - rollback simples,
   - teste de aceite.

---

## Definição de “100% de confiabilidade” (prática)
Como 100% absoluto é inalcançável em software real, adotaremos “100% operacional” =
1. Nenhum crash conhecido em fluxos críticos cobertos.
2. Build CI estável e reproduzível.
3. Guard-rails bloqueando regressões já mapeadas.
4. Testes automatizados cobrindo contratos críticos de dados/regras.
5. Uma fonte de verdade predominante por fluxo (com legado minimizado e explicitamente rastreado).

---

## Inventário de riscos atuais (mapa)
### R1 — Duplicidade de fonte de verdade
- Sintoma: snapshot + globais legadas coexistindo.
- Impacto: risco de drift comportamental.
- Mitigação: migração por trilhas e auditoria contínua de acoplamento.

### R2 — Regressão de acoplamento com `DataLoader`
- Sintoma: chamadas diretas fora do repositório.
- Impacto: quebra da fronteira arquitetural.
- Mitigação: guard-rail no gate e revisão por diff.

### R3 — Regressão de enum/IDs de domínio
- Sintoma: valores inválidos como `Categoria.SOCIAL`.
- Impacto: falha de compilação/testes.
- Mitigação: guard-rail textual + testes.

### R4 — Regras críticas ainda em artefatos grandes
- Sintoma: complexidade de `CriadorState`.
- Impacto: dificuldade de testes e manutenção.
- Mitigação: extração incremental para use-cases com testes.

### R5 — Overlap de módulos com dados duplicados
- Sintoma: duplicidade de perícias/vantagens/poderes.
- Impacto: risco de crash/inconsistência se não sanitizar.
- Mitigação: sanitização central + testes de política “prefer last”.

---

## Plano de execução detalhado

## Trilha A — Observabilidade e auditoria contínua
### A1. Script de inventário arquitetural (automático)
- Objetivo: mapear pontos de acoplamento, chamadas diretas de loader e tamanho de arquivos críticos.
- Entregável: `scripts/reliability_inventory_audit.sh`.
- Aceite: script executa local/CI sem dependência rígida de `rg`.

### A2. Relatório de inventário versionado
- Objetivo: gerar snapshot textual para comparação entre PRs.
- Entregável: `docs/reports/reliability-inventory-latest.md`.
- Aceite: contém métricas mínimas e timestamp.

---

## Trilha B — Fonte de verdade e dados
### B1. Catalogar globais restantes por dono e uso
- Objetivo: saber o que ainda está em `MainActivity` e `GameDataGlobals`.
- Entregável: tabela de migração no documento.
- Aceite: 100% dos globais listados com destino alvo.

### B2. Desacoplar escrita global do caminho principal (planejado)
- Objetivo: concentrar leitura/escrita no repositório/store.
- Estratégia: introduzir feature flag de migração para validação segura.
- Aceite: fluxos críticos usam snapshot; legado somente fallback explícito.

### B3. Harden da sanitização de snapshot
- Objetivo: garantir política clara para duplicados por domínio.
- Aceite: testes para empate por ordem e consistência dos mapas derivados.

---

## Trilha C — Domínio e extração de regras
### C1. Priorizar 3 hotspots do `CriadorState`
- Critério: maior risco + maior churn + maior impacto em regressão.
- Candidatos iniciais:
  1. aplicação de ancestralidade,
  2. rebuild de stacks/perícias,
  3. validações de seleção.
- Aceite: cada extração com teste dedicado e sem mudança funcional observável.

### C2. Testes de contrato por use-case extraído
- Objetivo: impedir regressão sem depender de testes de UI.
- Aceite: cenário feliz + borda + regressão conhecida.

---

## Trilha D — CI/Gates e robustez operacional
### D1. Manter gate Fase 6 como obrigatório
- Aceite: gate roda antes do build em workflows.

### D2. Expandir gate com checks de drift controlados
- Itens planejados:
  - detecção de novos pontos de acoplamento global acima de limiar;
  - alerta quando `CriadorState` crescer além de delta aceitável;
  - validação de cobertura mínima para testes críticos por nome.
- Observação: usar alertas progressivos antes de bloquear.

### D3. Backlog técnico de warnings não bloqueantes
- Objetivo: tratar warnings por ondas sem misturar com bugfix funcional.

---

## Critérios de aceite globais
1. Nenhum fluxo crítico quebrado (carregamento, supers, salvar/carregar, progresso).
2. CI verde com gate + build.
3. Métricas de inventário estáveis ou melhorando.
4. Documentação de execução atualizada a cada etapa.

---

## Plano de rollback
- Todas as mudanças em passos pequenos por commit.
- Cada passo com validação local mínima.
- Reversão via commit único quando necessário.

---

## Execução iniciada (rodada atual)
### Etapa E0 (nesta branch)
- [x] Criar plano mestre detalhado.
- [x] Implementar script de inventário automático (Trilha A1).
- [x] Rodar inventário e registrar relatório inicial (Trilha A2).

Próxima etapa sugerida:
- E1: catalogar globais remanescentes com destino alvo (Trilha B1) e anexar no relatório.

## Evidências da execução inicial
- Inventário gerado em `docs/reports/reliability-inventory-latest.md`.
- Snapshot inicial mapeado:
  - `CriadorState.kt`: 4976 linhas;
  - `MainActivity.kt`: 1180 linhas;
  - `CriadorViewModel.kt`: 1439 linhas;
  - 5 declarações de listas globais mutáveis de domínio no escopo principal.


## Etapa E1 — Catálogo completo de globais remanescentes (Trilha B1)

| Global | Arquivo | Tipo | Dono atual | Uso principal atual | Destino alvo | Risco de migração |
|---|---|---|---|---|---|---|
| `listaComplicacoes` | `MainActivity.kt` | Lista mutável global | UI/escopo raiz | composições e regras em estado legado | `GameDataStore.complicacoes()` | médio (muitas leituras indiretas) |
| `listaCoracoesCrystal` | `MainActivity.kt` | Lista mutável global | UI/escopo raiz | regras de cenário Crystal Heart | `GameDataStore.coracoesCrystal()` | médio |
| `listaAncestralidadesJson` | `MainActivity.kt` | Lista mutável global | UI/escopo raiz | aplicação de ancestralidade | `GameDataStore.ancestralidades()` | alto (impacta build/racial pipeline) |
| `listaMonstroTemplates` | `MainActivity.kt` | Lista mutável global | UI/escopo raiz | modo monstro/templates | `GameDataStore.monstroTemplates()` | médio |
| `listaAtributos` | `MainActivity.kt` | Lista mutável global | UI/escopo raiz | UI e validações de atributos | `GameDataStore.atributos()` | médio |
| `listaPericias` | `MainActivity.kt` | Lista mutável global | UI/escopo raiz | árvore de perícias/stacking | `GameDataStore.pericias()` | alto (efeito cascata no state) |
| `mapaPericias` | `MainActivity.kt` | Mapa mutável global | UI/escopo raiz | lookup de perícia por nome | `GameDataStore.mapaPericias()` | alto |
| `mapaAtributosDisplay` | `MainActivity.kt` | Mapa mutável global | UI/escopo raiz | labels de atributo | `GameDataStore.atributosDisplay()` | baixo |
| `mapaAtributosDescricao` | `MainActivity.kt` | Mapa mutável global | UI/escopo raiz | descrição contextual | `GameDataStore.atributosDescricao()` | baixo |
| `racialAttrMinMap` | `MainActivity.kt` | Mapa mutável global | UI/escopo raiz | mínimos por ancestralidade | `GameDataStore.racialAttrMinMap()` | médio |
| `racialSkillStartMap` | `MainActivity.kt` | Mapa mutável global | UI/escopo raiz | baseline de perícias raciais | `GameDataStore.racialSkillStartMap()` | médio |
| `listaVantagens` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | seleção/validação de vantagens | `GameDataStore.vantagens()` | alto (alto acoplamento atual) |
| `listaTropos` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | regras de tropos/supers | `GameDataStore.tropos()` | médio |
| `listaEquipamentos` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | catálogo e filtros de equipamento | `GameDataStore.equipamentos()` | médio |
| `listaPoderes` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | poderes arcanos/seleção | `GameDataStore.poderes()` | alto |
| `equipamentoCategorias` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | categorização no UI | `GameDataStore.equipamentoCategorias()` | baixo |
| `superequipCategorias` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | categorização supers | `GameDataStore.superequipCategorias()` | baixo |
| `listaSuperPoderes` | `GameDataGlobals.kt` | Lista mutável global | globais de domínio | fluxo supers | `GameDataStore.superPoderes()` | alto |
| `arcanoInfo` | `MainActivity.kt` | Mapa mutável global | UI/escopo raiz | regras de antecedentes arcanos | `GameDataStore.arcanoInfo()` | médio |

### Política de migração (B2/B3)
1. **Trocar leitura primeiro**: todo novo código lê `GameDataStore`; globais ficam como mirror temporário.
2. **Bloquear crescimento**: gate falha se número de globais-lista aumentar acima do baseline atual.
3. **Remover escrita global tardia**: após convergência de leituras, remover side effects globais do pipeline principal.

## Etapa E2 — Drift controls no gate (Trilha D2)
- [x] Limiar bloqueante para crescimento de listas globais mutáveis (baseline atual: 11).
- [x] Limiar de atenção para crescimento de `CriadorState.kt` (warning operacional).
- [x] Verificação explícita de presença de testes críticos de contrato (sanitização/repositório/rules).

## Etapa E3 — Priorização de hotspots (Trilha C1)

Hotspots priorizados para extração incremental (ordem de execução):
1. `aplicarAncestralidade(...)` — linha ~3148 em `CriadorState.kt` (alto impacto de regras raciais + estado derivado).
2. `rebuildAllPericiaStacks(...)` — linha ~4405 em `CriadorState.kt` (núcleo de consistência das perícias).
3. `podeSelecionar(v: Vantagem)` — linha ~2625 em `CriadorState.kt` (gate central de elegibilidade de vantagens).

Critérios de extração por hotspot:
- manter assinatura pública e comportamento;
- criar use-case puro com testes de contrato (feliz + borda + regressão conhecida);
- migrar por adapter fino no `CriadorState` e validar com gate completo.

## Próxima etapa operacional (E4)
- Iniciar extração do hotspot #1 (`aplicarAncestralidade`) para use-case dedicado com bateria de testes de contrato antes de alterar os demais hotspots.

## Etapa E4 — Extração inicial aplicada (hotspot #1, parcial)
- [x] Extraído bloco de transição do bônus humano (+1 PV) para `ApplyHumanAncestryTransitionUseCase`.
- [x] `CriadorState.aplicarAncestralidade(...)` agora delega essa regra ao use-case mantendo o fluxo restante inalterado.
- [x] Testes de contrato adicionados para o use-case (remoção elegível, fallback de pontos, entrada em humano, no-op).
- [ ] Próximo subpasso E4.1: extrair subseção de complicações automáticas raciais para use-case dedicado.
