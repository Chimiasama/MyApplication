# Fase 2 — kickoff (separação de domínio)

Com a Fase 1 funcionalmente concluída, iniciamos a Fase 2 com uma extração pequena e segura de regra para caso de uso de domínio.

## Entrega deste kickoff

- Regra extraída do `CriadorViewModel` para `EnsureDefaultSpecializationsUseCase`.
- O ViewModel agora apenas orquestra entradas/saídas e aplica o resultado no estado.
- Testes unitários puros do use case adicionados.

## Objetivo atingido

- Reduzir lógica de negócio dentro do ViewModel.
- Criar padrão de extração incremental para próximas regras críticas (ex.: ancestralidade, rebuild de perícias, validações de progresso).

## Próximos passos da Fase 2

1. Extrair `SkillPointRebuildUseCase` (regras de custo/normalização de perícia).
2. Extrair `ApplyAncestryUseCase` (ajustes raciais e reembolso).
3. Isolar validações em `ValidationUseCase` com testes por cenário.
4. Manter estratégia de migração incremental com comparação de comportamento.
