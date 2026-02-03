# Proposta de UX para gasto de Pontos Bônus (PB)

## Como está hoje
- Cada seção de gasto (Atributos, Perícias, Vantagens e Recursos) exibe dois botões grandes "Usar PB" e "Desfazer uso de PB", mesmo quando o total de PB é zero. Eles ocupam largura total e duplicam o padrão em todas as seções, deixando a área visual carregada. Observa-se o layout nos cabeçalhos de Atributos e Perícias, por exemplo. 【F:app/src/main/java/com/example/swadebuilder/ui/sections/AtributosSection.kt†L78-L125】【F:app/src/main/java/com/example/swadebuilder/ui/sections/PericiasSection.kt†L107-L157】
- A lógica de habilitar/desabilitar depende apenas de `pcLivres` e dos stacks de gasto (`cpPaStack`, `cpSpStack` etc.), portanto os botões continuam renderizados mesmo para personagens que não ganharam PB (ex.: complicações raciais que não concedem pontos). 【F:app/src/main/java/com/example/swadebuilder/ui/sections/AtributosSection.kt†L52-L118】【F:app/src/main/java/com/example/swadebuilder/ui/sections/PericiasSection.kt†L120-L148】
- O texto explicativo sobre como obter PB aparece somente quando o total é zero, mas não reduz a sensação de espaço desperdiçado pelos botões vazios. 【F:app/src/main/java/com/example/swadebuilder/ui/sections/PericiasSection.kt†L151-L157】

## Objetivos de UX
1. Tornar a "carteira" de PB visível, mas compacta e consistente em todas as seções.
2. Mostrar ações de gasto/desfazer apenas quando o jogador realmente possui PB de complicações disponíveis para aquela etapa.
3. Reduzir clutter na área principal de cada seção, mantendo o foco nas listas/controles próprios (atributos, perícias, vantagens, recursos).

## Fluxo proposto
1. **Carteira de PB fixa no topo**: adicionar um componente reutilizável (ex.: `PbWalletBanner`) que apareça no cabeçalho das seções afetadas. Ele apresenta `PB livres / PB total` e um ícone de informação sobre a origem dos pontos. Quando `pcTotal == 0`, esconder o banner e não renderizar botões.
2. **Ações contextuais condicionalmente visíveis**:
   - Se `pcTotal > 0` e `pcLivres > 0`, exibir um botão compacto de ação primária (por exemplo, um `FilledTonalButton` com label curto tipo "Adicionar PB") e um botão secundário de "Desfazer" somente quando houver gasto registrado (`stack` não vazio). Sem PB: esconder ambos e deixar apenas o tooltip/label da carteira.
   - Para complicações que não geram PB (como de raça), a UI naturalmente se mantém no estado informativo porque `pcTotal` continua em zero.
3. **Menu flutuante de gasto**: em vez de dois botões lineares, usar um `AssistChip` ou botão icônico de ação flutuante alinhado à direita do cabeçalho da seção. Ao clicar, abrir um `DropdownMenu` ou `BottomSheet` com as opções "Usar PB" e "Desfazer". Isso mantém a seção compacta e evita ocupar a largura toda.
4. **Feedback de consumo**: ao confirmar o gasto, exibir um `Snackbar` ou mensagem inline "1 PB gasto em Perícias" com opção de desfazer. Para desfazer, reutilizar a mesma carteira/menu, mantendo a consistência.
5. **Resumo global**: no topo da tela ou na seção de Complicações, incluir um pequeno quadro "PB ativos" listando onde estão alocados (Atributos/Perícias/Vantagens/Recursos) usando os stacks existentes para compor o resumo. Se algum PB está travando a remoção de complicação, referenciar este quadro para indicar o motivo.

## Impacto técnico
- Criar um componente compartilhado de carteira (ex.: `ui/components/PbWalletBanner.kt`) que recebe `pcTotal`, `pcLivres` e callbacks de gasto/estorno por seção.
- Atualizar cada seção (Atributos, Perícias, Vantagens, Equipamento) para trocar o `Row` de dois `TextButton` por:
  - Renderizar o banner acima da lista.
  - Mostrar menu/ações apenas quando `pcTotal > 0` e `pcLivres > 0` (gasto) ou stack > 0 (desfazer).
- Manter a lógica de negócio intacta (`CriadorState` já expõe `pontosComplicacao` e stacks de gasto), ajustando apenas a apresentação e a visibilidade das ações.
- Opcional: centralizar a legenda explicativa em `PbWalletBanner` para que o texto "Para ganhar Pontos Bônus..." não precise ser duplicado em cada seção.

## Benefícios esperados
- Interface mais limpa: controles de PB ocupam pouco espaço e não "dominam" a tela quando o usuário ainda não tem pontos.
- Fluxo intuitivo: só aparecem opções de gasto quando há PB de fato; usuários com complicações sem PB não veem botões inúteis.
- Consistência: mesmo visual e microinterações em todas as seções, reduzindo curva de aprendizado.
