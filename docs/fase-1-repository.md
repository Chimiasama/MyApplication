# Fase 1 — isolamento de dados globais com Repository

Implementação inicial da Fase 1 concluída com foco em **introduzir abstração sem quebrar o legado**.

## O que foi implementado

1. **Contrato de repositório**
   - `GameDataRepository` com método `load(context, activeModules)`.
   - `GameDataSnapshot` como DTO imutável de dados carregados.

2. **Implementação concreta**
   - `AssetGameDataRepository` usando `DataLoader` internamente.
   - Mantém compatibilidade com o fluxo atual enquanto centraliza o ponto de acesso a dados.

3. **Bridge no DataLoader**
   - `DataLoader.loadCore` e `DataLoader.updateActiveModules` agora retornam `GameDataSnapshot`.
   - Continuidade garantida para o legado: as globais ainda são preenchidas, mas o carregamento também devolve snapshot explícito.

4. **Orquestração via ViewModel**
   - `CriadorViewModel` passa a receber `GameDataRepository` (injeção por construtor com default).
   - Nova API `carregarDadosDeJogo(context, activeModules)` para concentrar o carregamento no ViewModel.

5. **Camada de UI usando ViewModel (não DataLoader direto)**
   - `MainActivity` e `TelaInicial` passaram a chamar o ViewModel para carregar dados.

## Resultado desta iteração

- O app deixa de acoplar UI diretamente ao `DataLoader`.
- O ponto de entrada para dados agora é um contrato (`GameDataRepository`), preparando a troca futura das globais por fonte imutável/injetada.
- A migração é incremental (Branch by Abstraction), sem rewrite.

## Próximo passo sugerido (Fase 1.1)

- Migrar consumidores de listas globais para usar `GameDataSnapshot` diretamente no estado/viewmodel.
- Reduzir escrita em globais para uma camada de compatibilidade única, até remoção total.
