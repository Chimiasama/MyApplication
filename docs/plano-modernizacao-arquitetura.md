# Plano de modernização arquitetural (MVVM + domínio + cenários)

## Contexto
A base atual entrega valor, mas concentra regras de negócio em arquivos grandes e com múltiplas responsabilidades (estado de UI, regras, persistência e orquestração no mesmo lugar). Esse desenho aumenta risco de regressão e reduz velocidade para adicionar novos cenários.

## Vale a pena implantar?
**Sim, mas de forma incremental.**

- **Vale implantar** se o produto continuará evoluindo (novos cenários, regras e conteúdo), porque o custo de manutenção atual tende a crescer mais rápido que o custo de refatoração.
- **Não vale um “big bang rewrite”**: o ideal é migrar em fatias pequenas com métricas e rollback simples.
- **ROI esperado**: menos bugs de regras cruzadas, menor tempo para adicionar cenário novo e maior confiança com testes de domínio.

## Estratégia recomendada (ordem de execução)

### Fase 0 — Baseline e segurança (1 sprint)
Objetivo: criar rede de proteção antes de mexer na arquitetura.

1. Mapear fluxos críticos em testes de integração (criação, troca de ancestralidade, compra/reembolso, avanço XP, supers).
2. Congelar um conjunto de “cenários-ouro” (fixtures) para comparar estado final antes/depois.
3. Instrumentar métricas simples:
   - tempo para adicionar uma regra;
   - arquivos tocados por feature;
   - regressões por release.

**Critério de saída**: testes cobrindo os fluxos mais sensíveis e baseline de métricas estabelecida.

---

### Fase 1 — Isolar dados globais em Repository (1–2 sprints)
Objetivo: parar de depender de variáveis globais mutáveis.

1. Criar `GameDataRepository` (interface) + implementação inicial (`AssetGameDataRepository`).
2. Encapsular listas carregadas (`perícias`, `vantagens`, `poderes`, etc.) em um `GameDataSnapshot` imutável.
3. Mudar `ViewModel` para receber o repositório (injeção por construtor/factory).
4. Manter ponte de compatibilidade temporária para reduzir risco durante migração.

**Critério de saída**: UI e regras leem do repositório; globais deixam de ser fonte de verdade.

---

### Fase 2 — Separar estado e domínio (2–3 sprints)
Objetivo: reduzir o “God Object”.

1. Definir fronteiras:
   - `CriadorUiState`: somente estado de tela/seleções;
   - Domínio: regras puras em casos de uso.
2. Extrair casos de uso prioritários:
   - `ApplyAncestryUseCase`;
   - `SkillPointRebuildUseCase`;
   - `PointBudgetCalculator`;
   - `ValidationUseCase`.
3. Fazer o `ViewModel` orquestrar casos de uso e publicar novo estado.
4. Cobrir casos de uso com testes unitários sem Android framework.

**Critério de saída**: regras críticas fora de `CriadorState`, com testes dedicados.

---

### Fase 3 — Remover “magic strings” (1 sprint, paralelo possível)
Objetivo: impedir falhas silenciosas por digitação e chaves inconsistentes.

1. Criar catálogo central de IDs (`enum class`/`sealed`/objeto de constantes por domínio).
2. Substituir strings literais em regras centrais por tipos/constantes.
3. Adicionar validação de integridade de IDs no load de JSON (falhar cedo em desenvolvimento).
4. Manter camada de mapeamento para conteúdo dinâmico onde enum não for viável.

**Critério de saída**: regras críticas sem strings literais soltas.

---

### Fase 4 — Estratégia por cenário (Strategy Pattern) (2 sprints)
Objetivo: adicionar/remover cenário sem espalhar `if` pelo app inteiro.

1. Definir contrato `GameRules` (ex.: perícias iniciais, vantagens permitidas, limites especiais).
2. Criar implementações por cenário (`BaseRules`, `CrystalHeartRules`, `PathfinderRules`, etc.).
3. Introduzir `RulesResolver` para selecionar estratégia ativa.
4. Migrar regras de cenário aos poucos, mantendo fallback para comportamento legado.

**Critério de saída**: novo cenário entra criando classe de regras, não editando dezenas de pontos.

---

### Fase 5 — Limpeza da camada UI e hardening (1 sprint)
Objetivo: finalizar separação de responsabilidades.

1. Mover helpers de regra restantes da `Activity` para domínio/ViewModel.
2. Remover código legado e pontes temporárias.
3. Revisar documentação de arquitetura e guias de contribuição.

**Critério de saída**: `Activity` focada em composição/observação de estado.

## Prioridade sugerida (se houver pouco tempo)
1. **Fase 0 (mínimo viável de testes)**
2. **Fase 1 (Repository)**
3. **Fase 2 (casos de uso para regras mais críticas)**
4. **Fase 3 (magic strings)**
5. **Fase 4 (strategy por cenário)**

> Essa sequência já reduz bastante risco sem exigir reescrita total.

## Sugestão “melhor” que um refactor amplo
Se o time for pequeno, adote **Strangler Fig + Branch by Abstraction**:

- Coloque abstrações novas (`Repository`, `UseCase`, `GameRules`) **sem apagar** o legado de início.
- Migre apenas um fluxo por vez (ex.: ancestralidade) com testes comparativos.
- Só remova caminho antigo quando a cobertura e telemetria confirmarem equivalência.

Isso tende a dar melhor resultado que “parar tudo para refatorar”.

## Riscos e mitigação
- **Risco:** queda de velocidade inicial.
  - **Mitigação:** WIP limitado e fases curtas com entregas mensais.
- **Risco:** divergência entre regra nova e antiga.
  - **Mitigação:** testes de regressão por fixture + comparação de snapshots.
- **Risco:** escopo inflar e virar rewrite.
  - **Mitigação:** metas por fase com critério de saída objetivo.

## Status atual (checkpoint)
- Fase 0: concluída (baseline + testes críticos iniciais).
- Fase 1: concluída (repositório/snapshot/store transitório em uso).
- Fase 2: concluída (extrações centrais para use-cases + testes puros).
- Fase 3: em andamento (catálogo expandido para módulos/arcano/moedas Pathfinder, remoção adicional de literais críticas e normalização de AA extraída para use-case testado).
- Fase 4: em andamento (strategy por cenário aplicada a recursos + defaults de ancestralidade/vantagens mandatórias/coração inicial no reset).

## Indicadores de sucesso
- Redução de arquivos tocados por feature de regra.
- Redução de regressões de cálculo por release.
- Tempo menor para adicionar novo cenário.
- Aumento de testes unitários de domínio com execução rápida.
