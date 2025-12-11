# Plano de salvamento e retomada da ficha

## Objetivo
Permitir que o jogador pause a construção do personagem e retome depois **com a mesma capacidade de desfazer, recalcular e reembolsar** pontos (PB, atributos, perícias, vantagens, supers e XP). O salvamento deve capturar não só os valores finais, mas também o histórico de compras que alimenta as automatizações existentes.

## O que precisa ser persistido

### Pilhas e pools de criação base
- **Histórico de PB/PA/PV/SP**: `cpPaStack`, `cpSpStack`, `cpPvStack`, `cpRecursosStack` e derivados de progresso (`paFromProgress`, `spFromProgress`, `pvFromXpOutstanding`) preservam de onde vieram os pontos gastos.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L657-L676】
- **Atributos**: valores atuais e `paCostStackPorAtributo`, que registra cada passo comprado e permite reduzir atributos ao mudar de ancestralidade ou ao desfazer gastos.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L966-L1004】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1531-L1556】
- **Perícias**: `baseIncsPorPericia`, `spCostStackPorPericia` e `compCostStackPorPericia` guardam quantos incrementos cada perícia recebeu e quanto custaram, permitindo reembolsos automáticos quando o atributo base muda ou faltam pontos.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L960-L1004】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1558-L1604】
- **Seleções**: listas de vantagens, complicações (com severidade), equipamentos e poderes precisam ser salvas junto com as escolhas associadas (ex.: `choice` de Vantagem especialista).

### Ancestralidade e reajustes automáticos
- A função `aplicarAncestralidade` remove/ajusta vantagens raciais, recalcula atributos dentro dos novos tetos e devolve pontos quando necessário, por isso os stacks e valores associados precisam estar íntegros ao restaurar para que os recálculos funcionem igual ao vivo.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1075-L1179】

### Progresso de XP
- **Histórico de avanços**: `advancementHistory` registra cada gasto (atributo, perícia, vantagem, remoção de complicação, reserva lendária) e é o que alimenta `revertLastAdvancement` ao desfazer slots.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1453-L1476】【F:app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt†L829-L915】
- **Estado em curso**: flags de avanço em andamento (`skillAdvancementInProgress`, `advantageAdvancementInProgress`, etc.) e snapshots auxiliares (ex.: `frozenSkillIncrements`, `attributeStacksBeforeAdvancement`) devem ser persistidos para reabrir um avanço parcialmente concluído.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1453-L1476】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1541-L1556】

### Supers
- **Investimentos e limites**: `superInvestments`, `gastosPorPoder`, `superPontosTotais`/`Disponiveis`, `superLimite`/`superLimitePorPoder` e bônus aplicados (aparar, resistência, armadura, movimento) precisam ir para o snapshot para que os efeitos contínuos de poderes sejam recalculados ao carregar.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L34-L63】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L640-L664】

## Ordem de hidratação sugerida
1. **Resetar estado**: usar o fluxo existente de criação nova para limpar Compose states.
2. **Restaurar metadados base**: tema, flags de modo, dinheiro, anotação, ancestralidade inicial.
3. **Reaplicar stacks de atributos** antes de acionar recálculos, garantindo que os tetos raciais sejam respeitados e que `pontosAtributo` seja recalculado corretamente.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1531-L1556】
4. **Restaurar stacks de perícia** e pools de SP/CP, então chamar `rebuildAllPericiaStacks` uma única vez como sanidade para alinhar custos ao estado atual.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1558-L1604】
5. **Repor seleções**: vantagens (com `choice`), complicações (níveis), equipamentos e poderes; em seguida deixar o recalculo de dependências (ex.: antecedentes arcanos) rodar.
6. **Aplicar progresso de XP**: reidratar `advancementHistory`, `xpSlots`, reservas lendárias e snapshots de compra em andamento; isso garante que desfazer após o load percorra exatamente as mesmas ações.【F:app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt†L829-L915】
7. **Supers**: reintroduzir `superInvestments` e limites, depois reexecutar o cálculo de bônus agregados para aparar, resistência e movimento.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L34-L63】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L640-L664】

## Cobertura dos exemplos citados
- **Exemplo 1 (vai e vem com PB, ancestralidade e recálculo de perícia)**: Ao salvar stacks de PA/PV/SP e `paCostStackPorAtributo`, o carregamento mantém o histórico que alimenta `aplicarAncestralidade` e `rebuildAllPericiaStacks`, permitindo que a troca para Humanos ajuste atributos, reverta custos de perícia e devolva pontos exatamente como na sessão original.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L657-L676】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1075-L1179】【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1558-L1604】
- **Exemplo 2 (desfazer avanços de XP após load)**: Persistindo `advancementHistory` e os snapshots auxiliares de avanço, `revertLastAdvancement` consegue recolocar complicações, remover vantagens compradas e reduzir atributos na ordem inversa dos slots gastos, mantendo o resumo coerente mesmo após reabrir o app.【F:app/src/main/java/com/example/swadebuilder/CriadorState.kt†L1453-L1476】【F:app/src/main/java/com/example/swadebuilder/model/CriadorViewModel.kt†L829-L915】

## Armazenamento sugerido
- **Formato**: JSON via `kotlinx.serialization` em um DTO dedicado (ex.: `PersonagemSnapshot`) que converte chaves de mapas para `String` e registra escolhas (vantagem, arcano, severidade de complicação).
- **Local**: diretório privado da aplicação (context.filesDir) com uma lista de saves versionada para evitar incompatibilidades futuras.

## Próximos passos práticos
- Criar os DTOs de snapshot cobrindo todos os campos listados.
- Implementar funções `toSnapshot()` e `restoreFromSnapshot()` no `CriadorViewModel` (ou helper), seguindo a ordem de hidratação acima.
- Expor na UI ações de salvar e carregar que chamem essas funções e atualizem o estado Compose atual.
