# Fase 2 — kickoff (separação de domínio)

Com a Fase 1 funcionalmente concluída, iniciamos a Fase 2 com uma extração pequena e segura de regra para caso de uso de domínio.

## Entrega deste kickoff

- Regra extraída do `CriadorViewModel` para `EnsureDefaultSpecializationsUseCase`.
- Regras de manutenção de Crystal Hearts customizados extraídas para `UpsertCrystalHeartUseCase` e `RemoveCrystalHeartUseCase`.
- Regra de geração de nome sequencial extraída para `GenerateSequentialNameUseCase`.
- Validações básicas de investimento de poder extraídas para `ValidatePowerInvestmentUseCase`.
- Validações específicas de poderes genéricos (Superfeitiçaria/Superciência) extraídas para `ValidateSpecialPowerRequirementsUseCase`.
- Validação de Super Vantagem extraída para `ValidateSuperAdvantageInvestmentUseCase`.
- Validação de Super Atributo extraída para `ValidateSuperAttributeInvestmentUseCase`.
- Aplicação/reversão de delta de Super Atributo extraída para `ApplySuperAttributeDeltaUseCase`.
- Cálculo de limite por poder favorecido extraído para `CalculatePerPowerLimitUseCase`.
- Cálculo de raw de perícia após reversão de Super Perícia extraído para `CalculateSuperSkillRawAfterRevertUseCase`.
- Resolução de remoção de poder dependente após queda de perícia extraída para `ResolveDependentPowerRemovalUseCase`.
- Ajuste de bônus não-negativo (aplicar/reverter) extraído para `AdjustNonNegativeBonusUseCase`.
- Orquestração de validações de investimento de poder extraída para `ValidatePowerInvestmentWorkflowUseCase`.
- Rebuild de stacks de perícia após mudanças de poder extraído para `RebuildSkillStacksUseCase`.
- Cálculo de passos atuais de Super Perícia extraído para `CalculateCurrentSuperSkillStepsUseCase`.
- Resolução de vantagem por ID (case-insensitive) extraída para `ResolveAdvantageByIdUseCase`.
- O ViewModel agora apenas orquestra entradas/saídas e aplica o resultado no estado.
- Testes unitários puros dos use cases adicionados.

## Objetivo atingido

- Reduzir lógica de negócio dentro do ViewModel.
- Criar padrão de extração incremental para próximas regras críticas (ex.: ancestralidade, rebuild de perícias, validações de progresso).

## Encerramento da Fase 2

A Fase 2 foi encerrada com a maior parte das regras críticas extraídas para use-cases puros e cobertura unitária correspondente.

Pendências remanescentes foram transferidas como backlog da Fase 3 (padronização de IDs e remoção de strings mágicas).
