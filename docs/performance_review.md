# Plano de performance (sem perda de funcionalidades)

## Escopo revisado
- Build e empacotamento Android (`app/build.gradle.kts`).
- Inicialização e carregamento de dados (`MainActivity`, `DataLoader`, `GameDataRepository`).
- Fluxos de persistência (`CharacterStorage`, auto-save na `UnifiedScreen`).
- Custo de recursos estáticos (assets, fontes, drawables).

## Plano de execução prático por PR

> Legenda de esforço por item:
> - **P** = 0,5–1 dia
> - **M** = 1–3 dias
> - **G** = 3–5 dias

### PR 1 — rápido, baixo risco (quick wins)
**Objetivo:** reduzir custo imediato de startup/interação com mudanças localizadas e alta previsibilidade.

1. **Instrumentação mínima de baseline (startup + jank)** — **M**
   - Adicionar medição reproduzível (cold/warm start, troca entre seções).
   - Registrar baseline inicial para comparar ganhos dos PRs seguintes.

2. **Auto-save com debounce + dirty-state** — **P**
   - Evitar salvar a cada mudança de aba.
   - Salvar apenas quando houver alteração real + manter save forçado ao sair/background.

3. **Índice dedicado para listagem de saves (`index.json`)** — **M**
   - Evitar parse de snapshot completo ao abrir lista de personagens.
   - Atualizar índice em save/delete e fallback seguro para migração de saves antigos.

4. **Higienização de fontes não usadas (sem alterar identidade visual)** — **P**
   - Reduzir peso de recursos removendo variantes redundantes.
   - Priorizar variable fonts já existentes.

5. **Checklist de validação funcional (sem regressão)** — **P**
   - Fluxos: criar personagem, progressão, salvar/carregar, exportar PDF.

**Esforço total estimado PR 1:** **4 a 8 dias**.

---

### PR 2 — médio, foco em runtime/UI
**Objetivo:** reduzir recomposição desnecessária e custo de processamento em uso real.

1. **Memoização forte em listas grandes do Compose** — **M**
   - Aplicar `remember`/`derivedStateOf` nas listas filtradas por seção.
   - Garantir `key` estável e `contentType` consistente nas `Lazy*`.

2. **Pré-cálculo de chaves normalizadas de string** — **M**
   - Evitar `keyify`/`uppercase`/`semAcentos` repetidos em loops de render e filtro.
   - Persistir campos normalizados nos modelos de runtime.

3. **Índices de snapshot já sanitizados para consulta rápida** — **M**
   - Evitar deduplicações e `associateBy` repetidos em caminhos críticos.
   - Reaproveitar estruturas prontas por combinação de módulos ativos.

4. **Cache in-memory por combinação de módulos (LRU pequena)** — **M**
   - Melhorar alternância entre compêndios sem recarregar tudo.

5. **Benchmark comparativo vs PR 1** — **P**
   - Atualizar números de startup/jank e validar melhoria real.

**Esforço total estimado PR 2:** **5 a 11 dias**.

---

### PR 3 — estrutural, maior impacto
**Objetivo:** atacar gargalos de arquitetura e distribuição para ganho sustentado.

1. **Lazy loading por domínio/seção (carregamento sob demanda)** — **G**
   - Carregar catálogos apenas ao entrar em seções (perícias, poderes, equipamentos etc.).
   - Manter compatibilidade com regras atuais e fluxo de módulos.

2. **Estratégia de pré-carregamento progressivo** — **M**
   - Inicializar com `BASICO` para liberar UI mais cedo.
   - Prefetch assíncrono de módulos opcionais após app ficar interativo.

3. **Pipeline de dados de leitura rápida (JSON fonte + formato otimizado em build)** — **G**
   - Avaliar materialização para formato binário de runtime (ex.: protobuf/cbor), mantendo JSON como origem canônica.

4. **Revisão de ABI por canal/flavor de release** — **P**
   - Remover ABIs não necessárias em produção para reduzir tamanho/instalação.

5. **Revisão de imagens pesadas (PNG → WebP/AVIF sem perda perceptível)** — **M**
   - Foco em assets com maior impacto de tamanho e decode.

6. **Revisão de dependências de alto custo (método/tamanho)** — **M**
   - Confirmar necessidade em runtime e remover/extrair o que for possível.

7. **Validação regressiva completa + baseline final** — **M**
   - Comparar com baseline PR 1 e consolidar resultado final.

**Esforço total estimado PR 3:** **12 a 22 dias**.

## Ordem recomendada de entrega
1. **PR 1** (baixo risco, retorno rápido, cria baseline).
2. **PR 2** (ganho de fluidez em uso contínuo).
3. **PR 3** (mudanças estruturais com maior janela de teste).

## Critérios de sucesso
- Reduzir **cold start em 20–35%** em dispositivos médios.
- Reduzir **jank >16ms** em rolagens/listas densas.
- Reduzir **uso de memória no startup** e tamanho instalado sem perda funcional.
- Manter 100% dos fluxos críticos atuais (criação, progressão, salvar/carregar, exportar PDF).
