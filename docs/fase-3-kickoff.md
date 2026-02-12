# Fase 3 — kickoff (remoção de magic strings)

Com os principais fluxos de domínio da Fase 2 extraídos para use-cases, iniciamos a Fase 3 focada em reduzir strings mágicas em regras críticas.

## Primeira entrega da Fase 3

- Catálogo inicial de IDs criado em `model/ids/DomainIds.kt`:
  - `PowerIds` (ex.: ARMOR/RESISTANCE/SUPER_FEITICARIA/SUPER_CIENCIA)
  - `SkillIds` (ex.: OCULTISMO/CIENCIA)
- Regras críticas migradas para usar constantes tipadas em vez de literais:
  - `ValidatePowerInvestmentUseCase`
  - `ValidateSpecialPowerRequirementsUseCase`
  - `ResolveDependentPowerRemovalUseCase`
  - trechos de `CriadorViewModel` que resolvem dependências de poderes/perícias

## Próximos passos da Fase 3

1. Expandir catálogo de IDs para vantagens/complicações usadas em regras centrais.
2. Trocar literais restantes em fluxos críticos (`CriadorViewModel`/`CriadorState`) por constantes.
3. Adicionar validação de integridade de IDs no carregamento de dados (falha rápida em dev).
4. Evoluir para tipos de domínio onde viável (`sealed`/`enum`) mantendo compatibilidade com JSON dinâmico.
