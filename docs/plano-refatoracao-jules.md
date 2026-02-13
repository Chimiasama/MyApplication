# Plano de Refatoração e Hardening (Branch Jules_corrigindo)

## Objetivo
Alcançar alta confiabilidade na arquitetura do app, eliminando o acoplamento via variáveis globais, decompondo o "God Object" `CriadorState` e garantindo cobertura de testes para regras críticas.

## Estratégia Geral
A refatoração será dividida em fases incrementais, onde cada fase deve manter o app compilável e funcional. O foco é migrar a "fonte da verdade" das variáveis globais (`GameDataGlobals.kt`/`MainActivity.kt`) para o `GameDataStore` (via `CriadorViewModel`), e extrair lógica de negócio complexa para Use Cases testáveis.

---

## Fase 1: Unificação e Isolamento do Carregamento de Dados

**Objetivo:** Eliminar efeitos colaterais no `DataLoader` e garantir que o app leia dados apenas de uma fonte confiável (`GameDataRepository`).

1.  **Refatorar `DataLoader`:**
    -   Alterar métodos `loadCore` e `updateActiveModules` para retornarem `GameDataSnapshot` diretamente.
    -   Remover atribuições a variáveis globais (ex: `listaPericias = ...`) de dentro desses métodos.
    -   Criar um objeto de resultado que encapsule todas as listas carregadas.

2.  **Atualizar `AssetGameDataRepository`:**
    -   Adaptar para consumir o novo retorno do `DataLoader`.
    -   Manter a validação de integridade (`ValidateGameDataSnapshotIntegrityUseCase`) operando sobre o snapshot retornado.

3.  **Depreciar Globais:**
    -   Marcar variáveis em `GameDataGlobals.kt` como `@Deprecated`.
    -   Identificar todos os usos de `listaPericias`, `listaVantagens`, etc., no código.

4.  **Migrar Consumidores:**
    -   Alterar `CriadorState` e `CriadorViewModel` para lerem dados exclusivamente de `GameDataStore` (injetado ou acessado via ViewModel).
    -   Na `MainActivity`, substituir referências globais por observações do `CriadorViewModel.state` ou `GameDataStore`.

**Verificação:**
-   O app deve compilar sem erros.
-   Ao trocar de módulo (ex: ativar Crystal Heart), os dados devem atualizar corretamente na UI.
-   Nenhuma variável global deve ser modificada após a inicialização.

---

## Fase 2: Decomposição do `CriadorState` (O "God Object")

**Objetivo:** Extrair lógica de negócio complexa para Use Cases puros e testáveis, reduzindo o tamanho e responsabilidade de `CriadorState`.

1.  **Extração de `ApplyAncestryUseCase`:**
    -   Mover a lógica de `aplicarAncestralidade` (cálculo de atributos, traits raciais, ajuste de PV) para uma classe dedicada.
    -   **Input:** Estado atual (atributos, perícias, vantagens), Nova Ancestralidade, Regras de Cenário.
    -   **Output:** Novo estado (mapas atualizados, mensagens de feedback).
    -   **Teste:** Cobrir casos de troca Humano -> Não-Humano -> Humano e aplicação de modificadores raciais.

2.  **Extração de `RebuildSkillsUseCase`:**
    -   Mover `rebuildAllPericiaStacks` e lógica de custo de perícias.
    -   **Input:** Perícias, Atributos, Pontos Disponíveis, Regras de Custo.
    -   **Output:** Stacks de custo recalculadas, Pontos Restantes.
    -   **Teste:** Verificar recálculo correto ao mudar atributo base (d4 -> d6 -> d8).

3.  **Extração de `ValidateSelectionUseCase`:**
    -   Mover `podeSelecionar` e `podeSelecionarComplicacao`.
    -   **Input:** Item a selecionar, Estado atual (vantagens/complicações já selecionadas), Regras de Cenário.
    -   **Output:** Booleano (pode/não pode) + Motivo (se bloqueado).
    -   **Teste:** Validar pré-requisitos (Rank, Atributo, Edge) e exclusões mútuas.

4.  **Extração de `SnapshotUseCase`:**
    -   Mover `toSnapshot` e `restoreFromSnapshot` para um `PersistCharacterUseCase`.
    -   Isolar a lógica de conversão DTO <-> Estado.

**Verificação:**
-   Testes unitários para cada Use Case cobrindo cenários de sucesso e borda.
-   `CriadorState` deve delegar essas operações para as novas classes.

---

## Fase 3: Limpeza da `MainActivity.kt`

**Objetivo:** Tornar a Activity puramente uma camada de visualização e composição, sem lógica de negócio ou definição de dados.

1.  **Remover Definições de Dados:**
    -   Mover `listaComplicacoes`, `listaAtributos`, etc., que estão na Activity para `GameDataGlobals.kt` (se ainda forem necessárias temporariamente) ou eliminar de vez em favor do `GameDataStore`.

2.  **Mover Helpers:**
    -   Identificar funções auxiliares (ex: formatação, cálculos de UI) e movê-las para `CriadorViewModel` ou objetos utilitários (`ViewUtils.kt`).

**Verificação:**
-   `MainActivity.kt` deve conter apenas código Compose e configuração de ViewModel/Navegação.

---

## Fase 4: Hardening e Testes de Regressão

**Objetivo:** Garantir que as mudanças não quebraram funcionalidades existentes e cobrir fluxos críticos.

1.  **Testes de "Round-Trip" (Salvar/Carregar):**
    -   Criar personagem -> Adicionar itens complexos (Arcano, Perícias, Equipamento) -> Salvar -> Carregar.
    -   Verificar se o estado carregado é *idêntico* ao original.

2.  **Testes de Cenário (Crystal Heart / Arte da Guerra):**
    -   Verificar regras específicas (ex: "Canalizar Cristal" obrigatório, slots de Jutsu).
    -   Garantir que regras de um cenário não vazam para outro.

3.  **Verificação de Memória e Performance:**
    -   Monitorar criação de objetos durante a troca de cenários.
    -   Garantir que não há leaks de `Context` nos ViewModels ou UseCases.

---

## Riscos e Mitigação

-   **Risco:** Quebra de referências em UIs legadas que dependem de globais.
    -   **Mitigação:** Manter as globais como *proxies* (leitura apenas) apontando para o `GameDataStore` durante a transição, marcadas como `@Deprecated`.

-   **Risco:** Regressão na lógica de Ancestralidade (complexa e cheia de *edge cases*).
    -   **Mitigação:** Criar testes de "caracterização" (snapshot tests) da lógica atual antes de refatorar, para garantir que o comportamento novo seja idêntico.

-   **Risco:** `CriadorState` ficar dessincronizado do `GameDataStore`.
    -   **Mitigação:** O `CriadorState` deve ser a *única* fonte de verdade para o estado *mutável* do personagem, enquanto `GameDataStore` é a fonte para dados *imutáveis* (regras/listas).

---

## Próximos Passos (Execução)

1.  Criar branch `refactor/data-loader-isolation`.
2.  Implementar Fase 1.
3.  Validar e mergear.
4.  Criar branch `refactor/state-decomposition`.
5.  Implementar Fase 2 (um Use Case por vez).
6.  Validar e mergear.
7.  Seguir para Fases 3 e 4.
