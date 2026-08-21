# Plano de execução — Melhorias de UX, estilo, funcionalidades e qualidade

## 1. Objetivo

Este documento transforma a análise de melhorias do app SWADEbuilder em um plano de execução incremental, rastreável e separável por frentes de trabalho. A intenção é permitir que cada melhoria seja implementada, validada e entregue sem bloquear as demais, reduzindo risco de regressões em regras de personagem, dados de jogo, saves e exportações.

## 2. Princípios do plano

1. **Entregas pequenas e verificáveis**: cada fase deve gerar mudanças demonstráveis, testáveis e reversíveis.
2. **Separação entre experiência, regra e infraestrutura**: mudanças visuais não devem alterar regras de criação sem testes dedicados.
3. **Compatibilidade com Lite e Full**: toda melhoria deve considerar os flavors existentes e as flags de conteúdo.
4. **Proteção de dados do usuário**: mudanças em save, importação, exportação e histórico devem ter migração segura e fallback.
5. **Acessibilidade desde o início**: novos componentes devem nascer com semântica, contraste e tamanho de toque adequados.
6. **Observabilidade antes de automação complexa**: antes de assistentes e templates avançados, instrumentar validações, diagnósticos e erros.
7. **Documentação junto com código**: cada fase deve atualizar documentação técnica e, quando necessário, instruções para o usuário.

## 3. Visão macro das frentes

| Frente | Tema | Resultado esperado |
|---|---|---|
| F0 | Preparação e qualidade base | Ambiente confiável, lint/testes alinhados e backlog higienizado |
| F1 | UX de criação | Tela inicial mais clara, onboarding e presets explicáveis |
| F2 | Navegação e validação | Progresso de criação, pendências e correção guiada |
| F3 | Busca, filtros e listas | Conteúdo longo mais navegável e comparável |
| F4 | Estilo, temas e acessibilidade | Identidade visual refinada, TalkBack e contraste melhores |
| F5 | Exportação, resumo e compartilhamento | Saídas úteis para mesa, backup e transferência |
| F6 | Progressão e histórico | Evolução de personagem segura e auditável |
| F7 | Conteúdo customizado e campanha | Extensões para mesas caseiras e gerenciamento de grupo |
| F8 | Assistente e templates | Criação acelerada por arquétipos e recomendações |

## 4. Dependências importantes

- **F0 deve vir primeiro**, pois garante que build, lint e testes sejam confiáveis antes das mudanças grandes.
- **F2 depende parcialmente de F1**, porque o onboarding define melhor quais pendências devem aparecer para cada fluxo.
- **F3 pode correr em paralelo com F1**, desde que não altere regras de validação.
- **F4 pode ser incremental**, mas acessibilidade dos componentes novos deve ser obrigatória desde F1.
- **F5 e F6 dependem de cuidado com persistência**, especialmente se histórico/versionamento alterar o formato de snapshot.
- **F7 e F8 são fases maiores**, recomendadas após estabilizar UX, validação e exportação.

## 5. Fase 0 — Preparação técnica e higiene

### 5.1 Objetivos

- Garantir que a base esteja pronta para mudanças grandes.
- Evitar que regressões visuais ou de regra passem despercebidas.
- Resolver pontos já identificados em auditorias anteriores.

### 5.2 Entregáveis

1. Revisão da estratégia de JDK/toolchain.
2. Confirmação de lint ativo por padrão, com opção de desativação apenas explícita.
3. Revisão de dependências não utilizadas ou mal configuradas.
4. Remoção de artefatos gerados versionados, se ainda existirem.
5. Checklist de testes mínimos por PR.
6. Documento curto de setup local/CI.

### 5.3 Tarefas técnicas

- Validar `./gradlew test` e `./gradlew lint` em ambiente com JDK suportado.
- Conferir se a estratégia Java 21 está documentada e reproduzível.
- Auditar dependências declaradas e uso real.
- Adicionar ou revisar `.gitignore` para APK/AAB, relatórios temporários e arquivos `.orig`.
- Definir matriz mínima:
  - `testLiteDebugUnitTest`;
  - `testFullDebugUnitTest`;
  - `lintLiteDebug` ou lint equivalente;
  - build debug de pelo menos um flavor.

### 5.4 Critérios de aceite

- O projeto possui comandos oficiais de validação documentados.
- Lint não fica desativado silenciosamente.
- Dependências sem uso óbvio são removidas ou justificadas.
- Artefatos gerados não são confundidos com fonte de verdade.

### 5.5 Riscos

- Mudanças em toolchain podem quebrar CI ou builds locais.
- Remover dependência aparentemente não usada pode afetar código gerado ou uso indireto.

### 5.6 Mitigação

- Fazer remoções em PR separado.
- Conferir build Lite e Full.
- Registrar decisão técnica no documento de toolchain.

## 6. Fase 1 — Tela inicial e onboarding de criação

### 6.1 Objetivos

- Reduzir complexidade inicial para novos usuários.
- Explicar escolhas antes de criar personagem.
- Preservar modo avançado para usuários experientes.

### 6.2 Proposta de fluxo

1. **Boas-vindas**
   - Criar personagem.
   - Carregar personagem.
   - Configurações.
2. **Escolha de modo**
   - Rápido/iniciante.
   - Avançado.
3. **Escolha de livro ou cenário**
   - Básico.
   - Fantasia.
   - Horror.
   - Sci-Fi.
   - Pathfinder.
   - Deadlands.
   - Crystal Heart.
   - Arte da Guerra.
   - Cidade Sol Vapor.
   - Wiseguys.
   - Supers.
4. **Regras opcionais**
   - Mostrar apenas regras relevantes ao cenário escolhido.
5. **Resumo antes de iniciar**
   - Módulo selecionado.
   - Regras ativas.
   - Recursos disponíveis no flavor atual.

### 6.3 Entregáveis

- Novo modelo de dados para opções da tela inicial.
- Componente de etapa/stepper ou fluxo paginado.
- Modo avançado preservando a seleção detalhada atual.
- Textos curtos de ajuda por opção.
- Estado visual para módulos indisponíveis em Lite.

### 6.4 Tarefas técnicas

- Extrair dados de módulos para uma lista declarativa, evitando flags espalhadas.
- Criar `CreationPreset` ou equivalente para centralizar defaults por livro.
- Separar estado da tela inicial da renderização.
- Adicionar testes unitários para presets.
- Adicionar testes Compose para fluxo básico de criação.

### 6.5 Critérios de aceite

- Usuário iniciante consegue iniciar personagem básico em poucos passos.
- Usuário avançado ainda consegue ativar regras opcionais manualmente.
- Presets geram os mesmos flags esperados atualmente.
- Estado sobreviverá a rotação/troca de configuração.

### 6.6 Riscos

- Alterar tela inicial pode mudar combinações válidas de módulos.
- Presets incorretos podem afetar regras de criação.

### 6.7 Mitigação

- Snapshot dos flags antes/depois para cada preset.
- Testes por cenário.
- Lançar primeiro sem remover o fluxo antigo, usando feature flag se necessário.

## 7. Fase 2 — Progresso de criação e painel de pendências

### 7.1 Objetivos

- Mostrar claramente o que falta para finalizar o personagem.
- Guiar o usuário para corrigir pendências.
- Separar erro bloqueante, aviso e sugestão.

### 7.2 Modelo de pendência

Cada pendência deve conter:

- `id` estável;
- severidade: `erro`, `aviso`, `info`;
- seção relacionada;
- mensagem curta;
- explicação longa opcional;
- ação sugerida;
- se bloqueia exportação/finalização.

### 7.3 Entregáveis

1. Barra ou painel de progresso por seção.
2. Lista de pendências clicável.
3. Indicadores nas abas/seções.
4. Mensagens de validação consistentes.
5. Botão “ir para correção”.

### 7.4 Tarefas técnicas

- Criar agregador de validações por seção.
- Mapear seções existentes para status: completo, incompleto, erro, opcional, bloqueado.
- Reaproveitar validações atuais do estado/modelo quando possível.
- Criar componente `CreationProgressPanel`.
- Integrar com navegação para alterar `activeSection`.

### 7.5 Critérios de aceite

- Ao abrir o resumo, o usuário vê pendências restantes.
- Clicar em uma pendência navega até a seção correta.
- Exportação/finalização avisa quando há erro bloqueante.
- Avisos não impedem continuar, mas ficam visíveis.

### 7.6 Riscos

- Validações duplicadas podem divergir das regras reais.
- Mensagens em excesso podem poluir a interface.

### 7.7 Mitigação

- Validação deve ser centralizada no domínio sempre que possível.
- Permitir recolher/expandir o painel.
- Priorizar até três pendências críticas visíveis por padrão.

## 8. Fase 3 — Busca, filtros, ordenação e comparação

### 8.1 Objetivos

- Facilitar navegação em listas grandes.
- Reduzir tempo para encontrar vantagens, poderes, perícias e equipamentos.
- Permitir comparar escolhas antes de selecionar.

### 8.2 Entregáveis

1. Campo de busca avançado com limpeza rápida.
2. Chips de filtro com contagem.
3. Ordenação por nome, origem, requisito, custo ou rank quando aplicável.
4. Estado “sem resultados” com sugestões.
5. Comparador lado a lado para itens compatíveis.
6. Destaque do termo buscado no resultado.

### 8.3 Tarefas técnicas

- Criar modelo comum de filtros por domínio.
- Padronizar normalização de texto sem acento e case-insensitive.
- Adicionar `LazyRow` ou layout rolável para chips quando houver muitos filtros.
- Persistir filtros por seção durante a sessão.
- Implementar comparação inicialmente para uma seção piloto, como Vantagens.

### 8.4 Critérios de aceite

- Busca funciona sem acento.
- Filtros não quebram ao trocar seção.
- Listas longas continuam performáticas.
- Comparador exibe diferenças úteis e não altera seleção sem confirmação.

### 8.5 Riscos

- Filtros podem causar recomposição excessiva.
- Comparador genérico pode ficar complexo demais.

### 8.6 Mitigação

- Começar por uma seção piloto.
- Medir recomposição/performance em listas grandes.
- Reutilizar memoização já aplicada em áreas críticas.

## 9. Fase 4 — Estilo, temas e acessibilidade

### 9.1 Objetivos

- Melhorar acabamento visual.
- Tornar temas mais reconhecíveis e consistentes.
- Garantir acessibilidade básica em componentes interativos.

### 9.2 Entregáveis

1. Revisão dos cards de módulo.
2. Badges por tipo, edição ou disponibilidade.
3. Preview de temas nas configurações.
4. Tema automático sugerido por cenário.
5. Semântica TalkBack nos principais componentes.
6. Checklist de contraste por tema.

### 9.3 Tarefas técnicas

- Adicionar `contentDescription` contextual em cards e botões.
- Garantir tamanho mínimo de toque.
- Revisar contraste `surface`, `surfaceVariant`, `primaryContainer` e texto.
- Criar dados de tema com nome amigável, descrição e preview.
- Adicionar badges reutilizáveis.
- Avaliar screenshots de referência para temas principais.

### 9.4 Critérios de aceite

- Cards selecionados/desabilitados são entendidos por TalkBack.
- Todos os temas principais mantêm contraste aceitável.
- Configurações mostram preview claro do tema.
- Mudanças visuais não alteram regras nem saves.

### 9.5 Riscos

- Temas muito estilizados podem prejudicar legibilidade.
- Animações podem incomodar ou impactar performance.

### 9.6 Mitigação

- Respeitar configurações de redução de animação quando possível.
- Manter tema minimalista como fallback de alta legibilidade.
- Testar em claro e escuro.

## 10. Fase 5 — Resumo, exportação, backup e compartilhamento

### 10.1 Objetivos

- Tornar o personagem fácil de usar na mesa.
- Facilitar backup e transferência entre dispositivos.
- Separar exportação de impressão, compartilhamento e backup.

### 10.2 Tipos de saída

1. **PDF compacto**: informações essenciais para jogo.
2. **PDF completo**: detalhes, descrições e referências.
3. **Markdown/texto**: compartilhável em chat ou notas.
4. **JSON de backup**: importável sem perda de dados.
5. **Imagem/resumo visual**: opcional, para compartilhamento rápido.
6. **Pacote `.swadebuilder`**: formato futuro com snapshot, metadados e retrato.

### 10.3 Entregáveis

- Tela de exportação com seleção de formato.
- Preview de resumo antes de exportar.
- Importação segura de backup.
- Validação de checksum/versão do arquivo importado.
- Share sheet Android para compartilhar exportações.

### 10.4 Tarefas técnicas

- Separar DTO de exportação do snapshot interno.
- Adicionar versionamento explícito ao formato exportável.
- Criar validadores de importação.
- Implementar permissões/contratos de arquivo via Storage Access Framework.
- Criar testes de round-trip: exporta/importa e compara snapshot essencial.

### 10.5 Critérios de aceite

- Exportar PDF não altera personagem.
- Importar arquivo inválido não corrompe saves existentes.
- JSON exportado pode ser reimportado com integridade.
- Usuário recebe mensagem clara em caso de erro.

### 10.6 Riscos

- Alterações em snapshot podem quebrar saves antigos.
- Exportação PDF pode gerar diferenças difíceis de testar.

### 10.7 Mitigação

- Versionar formatos.
- Manter migrações pequenas.
- Testar snapshots reais anonimizados.

## 11. Fase 6 — Progressão, histórico e desfazer

### 11.1 Objetivos

- Tornar evolução de personagem auditável.
- Permitir entender o que mudou a cada avanço.
- Reduzir risco de erro irreversível em campanhas longas.

### 11.2 Entregáveis

1. Linha do tempo de avanços.
2. Registro de mudanças por avanço.
3. Preview antes/depois.
4. Desfazer último avanço quando seguro.
5. Snapshots automáticos por marco.

### 11.3 Tarefas técnicas

- Criar modelo `CharacterRevision` ou equivalente.
- Armazenar metadados: data, motivo, seção, resumo da alteração.
- Definir regras para reversão segura.
- Integrar com modo de progressão existente.
- Criar migração para saves sem histórico.

### 11.4 Critérios de aceite

- Avanço gera registro legível.
- Usuário pode ver antes/depois de uma progressão.
- Desfazer funciona para casos simples e avisa quando não for possível.
- Saves antigos continuam carregando.

### 11.5 Riscos

- Histórico pode aumentar tamanho dos saves.
- Reversão pode conflitar com regras derivadas.

### 11.6 Mitigação

- Limitar snapshots automáticos ou compactar histórico.
- Começar com “restaurar versão anterior” em vez de desfazer granular.
- Validar após restauração.

## 12. Fase 7 — Conteúdo customizado e modo campanha

### 12.1 Objetivos

- Permitir mesas caseiras.
- Agrupar personagens em campanhas.
- Separar conteúdo oficial, fanmade e pessoal.

### 12.2 Entregáveis de conteúdo customizado

1. Cadastro de vantagem customizada.
2. Cadastro de complicação customizada.
3. Cadastro de equipamento customizado.
4. Cadastro de poder customizado.
5. Importação/exportação de pacote de conteúdo.
6. Validação por schema.

### 12.3 Entregáveis de campanha

1. Criar campanha.
2. Vincular personagens.
3. Registrar sessões.
4. Aplicar XP/progressos em lote ou por personagem.
5. Exportar grupo.

### 12.4 Tarefas técnicas

- Definir schema versionado de conteúdo customizado.
- Criar namespace para evitar colisão com conteúdo oficial.
- Adicionar origem visível em listas e filtros.
- Criar armazenamento separado para campanhas.
- Projetar tela de campanhas sem impactar fluxo individual.

### 12.5 Critérios de aceite

- Conteúdo customizado inválido é recusado com mensagem clara.
- Conteúdo pessoal aparece identificado como pessoal/fanmade.
- Campanha não é obrigatória para usar o app.
- Exportar grupo não altera personagens.

### 12.6 Riscos

- Conteúdo customizado pode quebrar regras ou requisitos.
- Modo campanha pode aumentar muito o escopo.

### 12.7 Mitigação

- Lançar conteúdo customizado primeiro para domínios simples.
- Manter modo campanha como área separada.
- Validar requisitos customizados com subconjunto inicial de regras.

## 13. Fase 8 — Templates e assistente de criação

### 13.1 Objetivos

- Acelerar criação para usuários iniciantes.
- Oferecer arquétipos úteis sem esconder o controle manual.
- Recomendar opções com base no cenário escolhido.

### 13.2 Entregáveis

1. Catálogo de arquétipos.
2. Criação a partir de template.
3. Explicação do que o template escolheu.
4. Recomendações opcionais por seção.
5. Assistente de perguntas simples.

### 13.3 Templates iniciais sugeridos

- Guerreiro corpo a corpo.
- Atirador/pistoleiro.
- Investigador.
- Social/face.
- Conjurador básico.
- Curandeiro/suporte.
- Piloto/mecânico.
- Hacker/técnico.
- Monstro simples.
- Super-herói equilibrado.

### 13.4 Tarefas técnicas

- Definir formato de template declarativo.
- Validar template contra regras ativas.
- Gerar relatório de escolhas aplicadas.
- Permitir desfazer aplicação do template antes de salvar.
- Criar testes para cada template em cenário suportado.

### 13.5 Critérios de aceite

- Aplicar template gera personagem válido ou lista pendências claras.
- Usuário vê todas as escolhas feitas automaticamente.
- Template nunca força conteúdo indisponível no flavor atual.
- Usuário pode continuar editando manualmente.

### 13.6 Riscos

- Recomendações podem parecer “erradas” para mesas específicas.
- Templates exigem manutenção conforme regras mudam.

### 13.7 Mitigação

- Marcar templates como sugestões, não regra oficial.
- Permitir feedback/edição antes de confirmar.
- Manter templates pequenos e versionados.

## 14. Estratégia de testes por fase

| Fase | Testes mínimos |
|---|---|
| F0 | Gradle test, lint, build debug, auditoria de dependências |
| F1 | Unitários de presets, Compose do fluxo inicial |
| F2 | Unitários de validação, Compose de painel de pendências |
| F3 | Unitários de filtro/ordenação, performance básica de lista |
| F4 | Screenshot/manual visual, acessibilidade básica, contraste |
| F5 | Round-trip export/import, geração de PDF, arquivo inválido |
| F6 | Histórico, migração de save antigo, restauração/desfazer |
| F7 | Schema customizado, namespace, campanha sem personagem |
| F8 | Validação de templates por cenário/flavor |

## 15. Estratégia de versionamento e migração

1. Toda mudança em save deve incrementar versão de schema.
2. Migrações devem ser idempotentes.
3. Importação deve validar antes de gravar.
4. Backups automáticos devem ser considerados antes de migrações destrutivas.
5. Mudanças em exportação não devem depender diretamente de classes internas mutáveis.

## 16. Estratégia de documentação

Para cada fase, atualizar ou criar:

- notas técnicas da implementação;
- comandos de teste executados;
- limitações conhecidas;
- screenshots quando houver mudança perceptível de UI;
- instruções rápidas para usuário quando houver nova funcionalidade.

## 17. Roadmap recomendado

### Ciclo 1 — Estabilização e base de UX

1. F0 — Preparação técnica.
2. F1 — Onboarding inicial mínimo.
3. F2 — Painel simples de pendências.

### Ciclo 2 — Navegação e acabamento

1. F3 — Busca/filtros em seções prioritárias.
2. F4 — Acessibilidade e temas.
3. F5 — Exportação compacta e backup JSON.

### Ciclo 3 — Campanha e automação

1. F6 — Histórico de progressão.
2. F7 — Conteúdo customizado inicial.
3. F8 — Templates e assistente.

## 18. Backlog quebrado em épicos

### Épico A — Fundação técnica

- A1: Revisar toolchain e CI.
- A2: Validar lint e testes.
- A3: Limpar dependências e artefatos.
- A4: Criar checklist de PR.

### Épico B — Criação guiada

- B1: Extrair presets para modelo declarativo.
- B2: Criar fluxo iniciante.
- B3: Criar resumo pré-criação.
- B4: Preservar modo avançado.

### Épico C — Validação e progresso

- C1: Modelo de pendências.
- C2: Agregador de validações.
- C3: UI de progresso.
- C4: Navegação para correção.

### Épico D — Descoberta de conteúdo

- D1: Busca normalizada.
- D2: Filtros com contagem.
- D3: Ordenação.
- D4: Comparador piloto.

### Épico E — Estilo e acessibilidade

- E1: Badges e estados de cards.
- E2: Semântica TalkBack.
- E3: Preview de temas.
- E4: Revisão de contraste.

### Épico F — Exportação e dados

- F1: PDF compacto.
- F2: Markdown/texto.
- F3: Backup JSON versionado.
- F4: Importação segura.

### Épico G — Progressão

- G1: Timeline de avanços.
- G2: Snapshot por marco.
- G3: Preview antes/depois.
- G4: Restauração/desfazer.

### Épico H — Extensões

- H1: Conteúdo customizado simples.
- H2: Pacotes de conteúdo.
- H3: Campanhas.
- H4: Exportação de grupo.

### Épico I — Automação amigável

- I1: Templates básicos.
- I2: Aplicação com relatório.
- I3: Assistente por perguntas.
- I4: Recomendações por seção.

## 19. Definition of Done geral

Uma entrega só deve ser considerada concluída quando:

1. Código implementado e revisado.
2. Testes relevantes executados e registrados.
3. Lite e Full considerados.
4. Acessibilidade básica considerada para UI nova.
5. Documentação atualizada.
6. Mudanças em save/exportação versionadas.
7. Screenshots anexados quando houver alteração visual perceptível.
8. Riscos e limitações conhecidos documentados.

## 20. Próximo passo sugerido

Começar pela **Fase 0** e, em seguida, implementar um recorte pequeno da **Fase 1**:

1. Criar modelo declarativo de módulos/presets.
2. Adicionar testes dos presets atuais.
3. Introduzir resumo pré-criação sem remover a tela atual.
4. Validar com build/test/lint.

Esse recorte reduz risco, cria base para o onboarding completo e já prepara o terreno para o painel de pendências da Fase 2.
