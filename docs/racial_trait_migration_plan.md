# Plano de Migração: Traits Raciais Tipados (v1)

## Objetivo
Eliminar ambiguidade entre vantagens, complicações e traços raciais (positivos/negativos), substituindo heurísticas por um pacote racial final tipado e determinístico.

## Princípios
- Um único identificador canônico por traço (`trait_id`).
- Variantes sempre explícitas: `replace_trait_ids` + `add_trait_ids`.
- Cálculo mecânico e resumo só leem o pacote final resolvido.
- Texto narrativo não define mecânica.

## Etapas

### PR 1 — Infra de domínio (sem quebrar legado)
- Adicionar DTOs tipados para trait catalog e ancestry package.
- Carregar `docs/racial_trait_catalog_seed.json` como fonte de classificação base.
- Criar resolvedor `ResolveTypedAncestryPackageUseCase` com contrato:
  - input: ancestralidade + variante selecionada
  - output: `resolvedTraitIds`, `resolvedTraitObjects`

**Critério de aceite**
- Sem mudança visual/mecânica em produção.
- Testes de contrato para IDs duplicados e variantes inválidas.

### PR 2 — Compat layer dos JSONs atuais
- Mapear `habilidades` legadas -> `trait_id` usando tabela canônica.
- Mapear `desvantagens` e `vantagensGratis` para categorias corretas.
- Reportar ambiguidades em log de diagnóstico (somente debug).

**Critério de aceite**
- Todos os ancestrais atuais resolvem um pacote tipado sem crash.

### PR 3 — Variantes declarativas
- Definir para Sci-Fi (primeiro lote):
  - Aquarianos Semi-aquáticos
  - Avianos Ave de Rapina
  - Elfos Comunitário
  - Humanos Baixa Gravidade/Minerador
- Remover fallbacks ad-hoc por nome nesses casos.

**Critério de aceite**
- `base - replaced + adds` validado por testes unitários de pacote.

### PR 4 — ModifierEngine usa efeitos tipados
- Trocar inferência por string por aplicação de `effects` do catálogo.
- Manter fallback temporário somente para traits ainda não mapeados.

**Critério de aceite**
- Casos de `Resistência` e `Frágil` 100% determinados por pacote tipado.

### PR 5 — Summary/PDF usa pacote final tipado
- Exibir blocos por categoria:
  - vantagens raciais
  - complicações raciais
  - traits positivos
  - traits negativos
  - notas
- Eliminar duplicidade/vazamento de traços substituídos.

**Critério de aceite**
- Resumo e PDF consistentes com pacote final.

### PR 6 — Editor de raça (preparação)
- Reutilizar catálogo tipado para construir raça custom por pontos.
- Permitir regras por cenário (cap de pontos opcional).

## Matriz mínima de testes
1. Variante substitui traço base e não vaza em UI.
2. Variante substitui traço base e efeito mecânico some.
3. Seleção de variante não cai em fallback indevido.
4. Pacote final não contém `trait_id` duplicado.
5. Toda `effect` referenciando atributo/perícia valida domínio.

## Riscos e mitigação
- **Risco:** divergência de nomenclatura entre livros.
  - **Mitigação:** normalização por `trait_id` e aliases no catálogo.
- **Risco:** regressão silenciosa em cenários antigos.
  - **Mitigação:** modo compat + testes snapshot por ancestralidade.

## Entregáveis já disponíveis
- `docs/racial_trait_catalog_seed.json` (seed inicial de classificação)
- `docs/racial_trait_schema_v1.json` (schema de referência)
