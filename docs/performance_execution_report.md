# Relatório de execução do plano de performance

## Objetivo
Consolidar o que foi implantado desde o plano original, o impacto esperado, e o que ainda fica como evolução estrutural.

## Como era (antes do plano)
- Carregamento de dados sujeito a recargas repetidas ao alternar combinações de módulos.
- Auto-save acionado de forma agressiva em mudanças de seção, com risco de escrita redundante.
- Listagem de saves dependente de leitura/parse frequente de arquivos completos em cenários sem metadados rápidos.
- Seção de Perícias com custo maior de recomposição por filtros e normalizações repetidas.
- Sem trilha clara de execução por PR com priorização/estimativa.

## Como ficou (depois da implantação)

### 1) Dados de jogo e módulos
- Cache LRU por combinação de módulos ativos, com normalização de chave e reaproveitamento de snapshots sanitizados.
- Deduplicação de cargas concorrentes via in-flight join para evitar trabalho duplicado.
- Instrumentação de tempo (cache hit/join/load/fail) com relógio monotônico e logs protegidos por `isLoggable`.
- Pré-aquecimento progressivo após carga inicial:
  - baseline (`emptySet`),
  - transições prováveis de módulo único quando múltiplos módulos estão ativos.

### 2) Persistência e saves
- Índice `index.json` para listagem rápida de personagens.
- Escrita atômica de índice e snapshot via arquivo temporário + rename/copy fallback.
- Fallback de reconstrução do índice a partir dos arquivos existentes.
- Saneamento do índice na leitura (ordem, dedupe, existência/tamanho).
- Persistência do índice em modo best-effort para não bloquear fluxo principal de save/delete.

### 3) Auto-save
- Debounce de auto-save com atraso curto.
- Digest SHA-256 do snapshot serializado para detectar mudanças reais e evitar escrita redundante.

### 4) UI (Perícias)
- Uso de `remember` + `derivedStateOf` em lista/filtro visível.
- Pré-cálculo de chaves, origens e classificações de perícia (idioma/jutsu).
- Reaproveitamento de totais e chaves normalizadas para reduzir custo por recomposição.

### 5) Qualidade e governança
- Testes unitários adicionados para cache e normalização de chaves, além de reforço em sanitização.
- Documento de plano por PR (rápido/médio/estrutural) com estimativas e critérios de sucesso.

## O que mudou e por qual motivo
- **Cache + in-flight join**: reduzir latência e CPU ao evitar recargas duplicadas de dados pesados.
- **Index de saves**: reduzir I/O e parse recorrente na listagem de personagens.
- **Auto-save com digest**: manter segurança funcional de persistência, cortando gravações iguais.
- **Memoização em Perícias**: reduzir recomposição e churn de string em caminho quente de UI.
- **Instrumentação**: permitir medir ganho real em cenários de carga/cache.

## Pendências remanescentes (fase estrutural)
1. Lazy loading por domínio/seção (mudança arquitetural maior).
2. Pipeline de dados otimizado de leitura (avaliação de formato derivado de build).
3. Fechamento de validação comparativa final em ambiente de benchmark/dispositivo alvo, incluindo revisão de distribuição (ABI/assets/dependências) com números finais.

## Status consolidado
- Execução prática do plano: **majoritariamente concluída**.
- Pendências: **itens estruturais/finais** de maior porte, sem bloqueio para operação atual.
