# Relatório de Análise da Branch

## Resumo Executivo
A análise profunda do código e dos arquivos de dados revelou um **problema crítico de integridade de dados** nos arquivos JSON do Deadlands e Crystal Heart. Devido a inconsistências nas chaves de JSON, diversos pré-requisitos de vantagens estão sendo **ignorados silenciosamente** pelo sistema.

## Detalhes dos Problemas Encontrados

### 1. Inconsistência de Chaves em `deadlands_vantagens.json`
O arquivo `deadlands_vantagens.json` utiliza incorretamente a chave `"vantagens"` dentro do objeto `requisitos` para listar pré-requisitos de outras vantagens.
O código Kotlin (`Requisito.kt`) espera a chave serializada como `"vantagens_previas"`.

**Impacto:**
Como o `DataLoader` está configurado com `ignoreUnknownKeys = true`, o parser ignora a chave desconhecida `"vantagens"`. Consequentemente, **os requisitos não são carregados**.
Exemplos de vantagens afetadas (permitem compra sem ter o pré-requisito):
*   `antecedente_arcano_mestre_do_chi` (Deveria exigir "Artista Marcial")
*   `veterano_do_oeste_estranho` (Deveria exigir "Carta Selvagem")
*   `batedor` (Deveria exigir "Mateiro")
*   `atormentado` (Deveria exigir "Carta Selvagem")
*   `determinacao` (Deveria exigir "Coragem")
*   `mao_direita_do_diabo`, `rapido_como_um_raio`, entre outras.

### 2. Inconsistência de Casing em `crystal_vantagens.json`
O arquivo `crystal_vantagens.json` apresenta mistura de convenções de nomenclatura.
*   Alguns itens usam `"vantagensPrevias"` (camelCase), ex: `focus_aprimorado_ch`.
*   O código espera `"vantagens_previas"` (snake_case).

**Impacto:**
Assim como no Deadlands, requisitos definidos com camelCase são ignorados. O requisito `focus_ch` para a vantagem `FOCUS APRIMORADO` não será aplicado.

### 3. Endereçamento `aa_agente_syn` (Crystal Heart)
A lógica para o Antecedente Arcano "Agente da SYN" (`aa_agente_syn`) está implementada corretamente no `CriadorViewModel.kt` e `CriadorState.kt`.
*   O ID é verificado explicitamente para adicionar a vantagem automaticamente.
*   O ID está correto em `crystal_vantagens.json`.
*   *Observação:* A dependência de IDs hardcoded (`"aa_agente_syn"`) em meio à lógica de negócio torna o sistema frágil a mudanças de nome de arquivo ou refatorações de dados, mas funcionalmente está correto no momento.

### 4. DataLoader e Tratamento de Erros
A classe `DataLoader.kt` utiliza `Json { ignoreUnknownKeys = true }` e blocos `runCatching` dentro de loops de carregamento.
*   **Ponto Positivo:** Evita que o app feche (crash) se um JSON estiver malformado.
*   **Ponto Negativo:** Mascara erros de digitação (como `vantagens` vs `vantagens_previas`), fazendo com que o jogo rode com regras quebradas sem aviso.

### 5. Estrutura de Código
As classes `CriadorViewModel` e `CriadorState` são excessivamente grandes ("God Classes"), misturando lógica de UI, regras de negócio complexas e manipulação de dados. Isso dificulta a auditoria e aumenta o risco de regressões ao corrigir os problemas acima.

## Recomendações Imediatas (Para Discussão)
1.  **Padronização de JSON:** Realizar um "find & replace" massivo nos arquivos `deadlands_vantagens.json` e `crystal_vantagens.json` para corrigir as chaves para `"vantagens_previas"`.
2.  **Validação:** Adicionar um teste unitário ou script de verificação que falhe se encontrar chaves não mapeadas em `requisitos`, para evitar reincidência.
