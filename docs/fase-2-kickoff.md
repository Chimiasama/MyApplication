# Fase 2 — kickoff (separação de domínio)

Com a Fase 1 funcionalmente concluída, iniciamos a Fase 2 com uma extração pequena e segura de regra para caso de uso de domínio.

## Entrega deste kickoff

- Regra extraída do `CriadorViewModel` para `EnsureDefaultSpecializationsUseCase`.
- Regras de manutenção de Crystal Hearts customizados extraídas para `UpsertCrystalHeartUseCase` e `RemoveCrystalHeartUseCase`.
- Regra de geração de nome sequencial extraída para `GenerateSequentialNameUseCase`.
- Validações básicas de investimento de poder extraídas para `ValidatePowerInvestmentUseCase`.
- Validações específicas de poderes genéricos (Superfeitiçaria/Superciência) extraídas para `ValidateSpecialPowerRequirementsUseCase`.
- Validação de Super Vantagem extraída para `ValidateSuperAdvantageInvestmentUseCase`.
- O ViewModel agora apenas orquestra entradas/saídas e aplica o resultado no estado.
- Testes unitários puros dos use cases adicionados.

## Objetivo atingido

- Reduzir lógica de negócio dentro do ViewModel.
- Criar padrão de extração incremental para próximas regras críticas (ex.: ancestralidade, rebuild de perícias, validações de progresso).

## Próximos passos da Fase 2

1. Extrair `SkillPointRebuildUseCase` (regras de custo/normalização de perícia).
2. Extrair `ApplyAncestryUseCase` (ajustes raciais e reembolso).
3. Isolar validações em `ValidationUseCase` com testes por cenário.
4. Extrair próximas regras críticas de custo/progressão para casos de uso puros.
5. Migrar mais regras de seleção/efeitos do `CriadorViewModel` para casos de uso puros.
6. Manter estratégia de migração incremental com comparação de comportamento.
