# Plano de Correções Finais (Revisão Final)

Este documento detalha o plano de execução para as correções solicitadas na revisão final do aplicativo.

## 1. Ancestralidades

### Aquarianos e Avianos (Resistência)
*   **Problema:** Bônus de Resistência (+1 Aquarianos) e penalidade de Resistência (-1 Avianos/Frágil) não estão sendo aplicados no resumo/PDF.
*   **Solução:** Ajustar `ModifierEngine.kt` para garantir que o Regex ou a verificação de IDs capture corretamente as strings "Resistência +1" e "Frágil" (ou "Esguios") vindas do JSON. Verificar se os termos no JSON correspondem aos esperados pelo engine.

### Meio-Elfo (Pontos Fantasmas)
*   **Problema:** Ao trocar de Meio-Elfo para outra raça, o PV extra (Adaptável) não é removido se o jogador não escolheu a opção Ágil, gerando pontos fantasmas.
*   **Solução:**
    *   O `ResolveAncestryTransitionContextUseCase.kt` atualmente verifica apenas se a raça anterior era "HUMANOS" ou tinha "ADAPTAVEL" explícito no JSON.
    *   Alterar a lógica (via `ApplyAncestryChangeCoordinatorUseCase`) para considerar "MEIO-ELFO" como "Humano-like" se a flag `meioElfoAgil` estiver `false` (ou seja, se escolheu Adaptável). Isso garantirá que o sistema de transição remova a vantagem gratuita corretamente.

### Rakashanos (Garras Duplicadas)
*   **Problema:** O resumo exibe "Garras" e "Ataque Natural" (Desarmado), duplicando a informação.
*   **Solução:** Ajustar `CriadorState.extrairArmasNaturais` para filtrar/remover a entrada genérica "Ataque Natural" caso o personagem já possua uma arma natural específica (como "Garras") na lista de equipamentos/habilidades.

### Saurios (Sentidos Aguçados Duplicados)
*   **Problema:** "Sentidos Aguçados" aparece duas vezes no resumo.
*   **Solução:** Implementar deduplicação no `ResumoSection.kt` ou `SummaryUtils.kt` para evitar exibir a mesma habilidade vinda de fontes diferentes (Habilidade Racial vs Vantagem Grátis) se tiverem o mesmo nome.

### Pequeninos (Sorte e Tamanho)
*   **Problema 1 (Sorte):** A vantagem "Sorte" pode ser removida manualmente.
    *   **Solução:** No `VantagensSection.kt`, reforçar a verificação de `isRacialFree`. Garantir que "Sorte" esteja na lista de vantagens raciais/automáticas para que o botão de remover seja desabilitado.
*   **Problema 2 (Tamanho):** Exibe Tamanho -2 (devido à penalidade acumulada), mas o usuário deseja limitar visualmente a -1 (exceto para Diminutos).
    *   **Solução:** Em `ModifierEngine.sizeDisplay` (ou onde o valor é formatado para exibição), aplicar um `clamp` para não mostrar valor menor que -1, *a menos que* a raça possua a tag/característica "Diminuto" (Tamanho <= -2 base). O cálculo de Resistência continuará usando o valor real.

## 2. Complicações

### Cego (Bônus Gratuito)
*   **Problema:** Não concede a Vantagem Gratuita automaticamente.
*   **Solução:** Implementar um *hook* ao adicionar a complicação "Cego" (em `ComplicacoesSection.kt` ou `CriadorState`) que incremente `pontosVantagem` em +1. Ao remover, decrementar.

### Idoso (Requisito de Perícias)
*   **Problema:** Não valida o gasto de 5 pontos em perícias de Astúcia e não avisa ao remover.
*   **Solução:**
    *   Adicionar validação em `CriadorState` (provavelmente em `creationComplete` ou `validateSelection`) para impedir a finalização se `Idoso` estiver presente e o gasto em perícias de Astúcia < 5.
    *   Ao tentar gastar pontos em outras perícias sem ter os 5 de Astúcia, exibir feedback visual ou bloquear (se viável sem travar a UX).
    *   Ao remover "Idoso", verificar se o saldo de pontos ficaria negativo ou inválido e alertar o usuário.

## 3. Perícias

### Idiomas (Crash)
*   **Problema:** Crash ao remover o segundo idioma (ou manipular a lista do início).
*   **Solução:** Revisar `CriadorState.decreasePericia` e `trimIdiomaSlots`. Adicionar verificações de segurança (`index bounds`, `null checks`) ao acessar e remover itens das listas `idiomasExtras` e `spCostStackPorPericia` para evitar exceções de concorrência ou índice inválido.

## 4. Sistema

### Reembolso PB vs SP
*   **Problema:** Ao reduzir atributos, o sistema reembolsa SP (Pontos de Perícia) antes de PB (Pontos de Bônus), mesmo que PB tenha sido gasto por último.
*   **Solução:** Ajustar a lógica de `checkAndRefundResourcePb` ou a ordem de reembolso em `rebuildAllPericiaStacks`/`recalcularPontosAtributo` para priorizar a devolução de stacks de PB se eles existirem no topo da pilha de gastos.

## 5. Vantagens e Poderes

### Antecedente Arcano (Validação)
*   **Problema:** Permite finalizar sem escolher poderes.
*   **Solução:** Em `CriadorState.creationComplete()`, adicionar verificação: Se `temAntecedenteArcano()`, verificar se a quantidade de poderes escolhidos corresponde aos slots disponíveis. Se não, retornar `false` e impedir avanço/finalização.

### Arma Predileta (Aba de Poderes)
*   **Problema:** Selecionar "Arma Predileta" estaria abrindo a aba de Poderes ou causando confusão na navegação.
*   **Solução:** Verificar em `VantagensSection.kt` e `CriadorViewModel` se a seleção desta vantagem dispara acidentalmente `iniciarCompraArcanoViaXp` ou flag similar. Garantir que ela apenas abra o `ChoiceDialog` para escolha da perícia/arma e nada mais.

### UI (Sticky Header)
*   **Problema:** Necessidade de rolar muito para ver as vantagens adquiridas para remover.
*   **Solução:** Implementar `stickyHeader` (cabeçalho fixo) na lista de vantagens em `VantagensSection.kt`, agrupando as vantagens adquiridas no topo para fácil acesso e remoção.

### Riqueza (Podre de Rico)
*   **Problema:** Adiciona valor fixo incorreto (somando 1500 ao invés de multiplicar corretamente).
*   **Solução:** Ajustar `CriadorState.applyVantagemDinheiro`. "Podre de Rico" deve garantir que o total inicial seja 5x o base ($2500 no padrão). Se "Rico" já adicionou +$1000 (Total $1500), "Podre de Rico" deve adicionar apenas +$1000 (Total $2500), e não +$1500 (Total $3000). Ajustar a lógica para considerar o que já foi adicionado.
