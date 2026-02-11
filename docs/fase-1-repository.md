# Fase 1 — isolamento de dados globais com Repository

Implementação da Fase 1 evoluída em duas etapas incrementais:

## Etapa 1 (bridge de carregamento)

1. **Contrato de repositório**
   - `GameDataRepository` com método `load(context, activeModules)`.
   - `GameDataSnapshot` como DTO imutável de dados carregados.

2. **Implementação concreta**
   - `AssetGameDataRepository` usando `DataLoader` internamente.
   - Mantém compatibilidade com o fluxo atual enquanto centraliza o ponto de acesso a dados.

3. **Bridge no DataLoader**
   - `DataLoader.loadCore` e `DataLoader.updateActiveModules` retornam `GameDataSnapshot`.
   - As globais legadas continuam sendo preenchidas para compatibilidade.

4. **Orquestração via ViewModel**
   - `CriadorViewModel` recebe `GameDataRepository` (injeção por construtor com default).
   - API `carregarDadosDeJogo(context, activeModules)` centraliza carregamento no ViewModel.

5. **UI sem DataLoader direto**
   - `MainActivity` e `TelaInicial` chamam ViewModel para carregar dados.

## Etapa 2 (redução de leitura direta de globais)

No `CriadorViewModel`, pontos críticos passaram a ler preferencialmente do snapshot carregado (`loadedGameData`) com fallback para globais durante transição:

- listas de perícias, vantagens, complicações e corações;
- mapa de perícias (`mapaPericias`) usado em validações/efeitos;
- atualização de Crystal Hearts customizados sincronizando também o snapshot em memória.

Esse passo reduz acoplamento da camada de regra à fonte global mutável sem quebrar o legado.

## Resultado desta iteração

- O acoplamento da UI com `DataLoader` foi removido.
- O ponto de entrada para dados agora é um contrato (`GameDataRepository`).
- O ViewModel já começa a consumir fonte de dados carregada explicitamente (snapshot) em vez de depender somente de globais.
- Migração segue incremental (Branch by Abstraction), sem rewrite.

## Próximo passo sugerido (Fase 1.2)

- Extrair um `GameDataStore` interno ao ViewModel/State para substituir gradualmente leituras globais restantes.
- Encapsular writes de conteúdo customizado (ex.: Crystal Hearts) nesse store para eliminar sincronização manual.
- Adicionar testes unitários do ViewModel com `FakeGameDataRepository`.
