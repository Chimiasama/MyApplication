# Plano de Correções - SWADE Builder

Este documento detalha as correções e ajustes planejados para atender à revisão final do aplicativo.

## 1. Ancestralidades

### Meio-Elfos (Correção de Lógica e Pontos Fantasmas)
*   **Problema:** Ao trocar de Meio-Elfo para outra raça, o bônus escolhido (Vantagem ou Atributo) às vezes persiste incorretamente.
*   **Solução:**
    *   Dividir a entrada JSON de `MEIO-ELFOS` em duas versões internas: `MEIO-ELFOS (Versátil)` e `MEIO-ELFOS (Ágil)`.
    *   `MEIO-ELFOS (Versátil)` terá a tag `ADAPTÁVEL` em `vantagensGratis`, permitindo que o sistema trate a transição como se fosse Humano (removendo automaticamente a vantagem extra).
    *   `MEIO-ELFOS (Ágil)` terá o atributo Agilidade d6 definido diretamente no JSON, removendo a necessidade de lógica complexa no `CriadorState`.
    *   A interface (UI) interceptará a seleção de "Meio-Elfos" e apresentará um diálogo para o usuário escolher entre as duas opções, mantendo a experiência transparente.

### Pequeninos (Sorte e Tamanho)
*   **Problema:** A vantagem "Sorte" podia ser removida manualmente. Tamanho visual incorreto ao pegar a complicação "Pequeno".
*   **Solução:**
    *   Adicionar "SORTE" à lista `vantagensGratis` no JSON. Isso a torna uma Vantagem Racial (fixa e não removível).
    *   Ajustar a lógica de exibição do Tamanho para que raças Pequenas (Tamanho -1) não mostrem -2 visualmente ao pegar a complicação "Pequeno", exceto se forem nativamente Diminutas.

### Aquarianos e Avianos (Resistência)
*   **Problema:** Bônus/Penalidades de Resistência (+1/-1) não aplicados.
*   **Solução:** Melhorar o `ModifierEngine` para detectar corretamente as descrições textuais ("Adicione +1...", "-1") ou adicionar verificações explícitas para essas ancestralidades.

### Rakashanos (Duplicidade no Resumo)
*   **Problema:** "Garra" e "Desarmado (Garra)" aparecendo duplicados.
*   **Solução:** Filtrar a geração automática de "Ataque Natural" genérico quando a raça já possui uma arma natural específica (Garras) definida.

## 2. Complicações

### Cego (Vantagem Gratuita)
*   **Problema:** Não concedia o ponto de vantagem extra.
*   **Solução:** Implementar lógica no `CriadorState` para incrementar `pontosVantagem` ao selecionar a complicação "CEGO".

### Idoso (Requisitos e Remoção)
*   **Problema:** Não validava o gasto de 5 SP em Astúcia e permitia remoção causando déficit.
*   **Solução:** Adicionar validação na remoção da complicação para impedir a ação se os pontos extras já estiverem gastos e não puderem ser pagos.

## 3. Perícias

### Idiomas (Crash)
*   **Problema:** Crash ao remover idiomas em ordem específica.
*   **Solução:** Corrigir a função `trimIdiomaSlots` para usar uma remoção segura (evitando erro de modificação concorrente de lista).

### Sistema de Reembolso (PB vs SP)
*   **Problema:** Ao recuperar pontos (ex: baixando atributo), o sistema devolvia SP genérico em vez de PB (Pontos de Bônus) gastos anteriormente.
*   **Solução:** Ajustar a lógica de `rebuildSkillStacks` para priorizar a devolução de PB (`cpSpStack`) quando houver excesso de pontos de perícia.

## 4. Vantagens

### Antecedente Arcano (Validação)
*   **Problema:** Permitia finalizar ficha sem escolher poderes.
*   **Solução:** Adicionar verificação em `creationComplete` exigindo a escolha de poderes se um Antecedente Arcano estiver ativo.

### Interface (Sticky Header)
*   **Problema:** Necessidade de rolar para o topo para ver pontos/vantagens selecionadas.
*   **Solução:** Implementar cabeçalho fixo (Sticky Header) na aba de Vantagens, similar ao da aba de Complicações.

### Riqueza (Podre de Rico)
*   **Problema:** Cálculo de dinheiro incorreto (somava fixo em vez de multiplicar).
*   **Solução:** Ajustar o valor adicionado por "PODRE DE RICO" para garantir que o total final seja 5x o inicial (considerando que "RICO" já adicionou uma parte).

### Arma Predileta
*   **Problema:** Abria aba de poderes incorretamente.
*   **Solução:** Corrigir o callback de seleção na UI para não acionar a navegação de poderes para esta vantagem.
