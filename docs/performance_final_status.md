# Status final de implantação — Plano de performance

## Situação
**Plano encerrado para operação atual**: os itens de maior retorno e menor/médio risco foram implantados e estabilizados, cobrindo cache, dedupe de carga concorrente, autosave inteligente, indexação de saves e otimizações de recomposição na UI de Perícias.

## Entregas concluídas
1. Cache LRU por combinação de módulos com normalização de chave.
2. Join de cargas concorrentes (in-flight dedupe) para evitar trabalho duplicado.
3. Instrumentação de tempo no repositório (cache hit/join/load/fail), com relógio monotônico e logs guardados por `isLoggable`.
4. Autosave com debounce + checksum/digest para evitar gravações redundantes.
5. Índice `index.json` para saves com escrita atômica, saneamento e fallback de rebuild.
6. Persistência de índice em modo best-effort para não bloquear save/delete.
7. Sanitização de snapshot com dedupe estável (última ocorrência) e otimizações de chaves.
8. Memoização de filtros e pré-cálculo de chaves/flags na seção de Perícias.
9. Prewarm assíncrono de baseline após carga inicial.
10. Prewarm de transições prováveis de módulo único.
11. Testes unitários adicionados/reforçados para cache, normalização e sanitização.
12. Plano e relatório de execução documentados.

## O que era antes
- Reprocessamento maior em trocas de módulos e risco de cargas duplicadas concorrentes.
- Auto-save potencialmente excessivo em navegação de seções.
- Listagem de saves dependente de parse frequente de arquivos completos em cenários sem metadado rápido.
- Filtros de Perícias com custo mais alto de recomposição/normalização.

## O que ficou depois
- Caminhos críticos com menos I/O e menos CPU redundante.
- Melhor previsibilidade em carregamento de dados e comportamento de cache sob concorrência.
- Save listing mais rápido com índice dedicado e fallback seguro.
- UI de Perícias mais estável sob recomposição.

## Backlog estrutural (opcional, próxima fase)
1. Lazy loading completo por domínio/seção (mudança arquitetural maior).
2. Pipeline de dados otimizado de build/runtime (ex.: formato derivado para leitura mais rápida).
3. Rodada final de benchmark comparativo em ambiente estável/dispositivo alvo e fechamento de distribuição (ABI/assets/deps) com números finais.

## Conclusão
Para o objetivo de **acelerar o app sem perder funcionalidade**, a implantação atual está **suficiente para produção incremental**. Os itens restantes são evoluções estruturais de segunda fase.
