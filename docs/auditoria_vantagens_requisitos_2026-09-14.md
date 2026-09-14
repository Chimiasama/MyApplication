# Auditoria de requisitos de Vantagens, livro por livro (2026-09-14)

Contexto: pedido do dono do projeto para conferir, vantagem por vantagem e
livro por livro, se o que o livro descreve como REQUISITOS de cada Vantagem
bate com o que está estruturado em `app/src/main/assets/vantagens.json`
(campos `requisitos.estagio`, `.atributos`, `.pericias`, `.periciaMinOpcional`,
`.vantagens_previas`, `.observacoes` e `limite_compra`).

Método: para cada vantagem tagueada `"livros": ["<BOOK>"]`, comparei contra o
texto "REQUISITOS:" da descrição completa em `docs/swade_<book>` (não só a
tabela-resumo "SUMÁRIO DE VANTAGENS", que na extração de texto do Básico
omite pelo menos um requisito real — ver achado do Corredor abaixo — então a
descrição completa é a fonte de verdade, a tabela é só um atalho de
navegação). Também conferi contra o código que consome esses campos
(`ValidateRequirementsUseCase.kt`, `ValidatePrerequisiteUseCase.kt`,
`CriadorState.kt`) sempre que o requisito não é um simples atributo/perícia
fixo (ex.: perícia "à escolha", Antecedente Arcano específico vs. genérico).

## LIVRO BÁSICO — 139 vantagens conferidas

Todas as 139 entradas com `"livros": ["BASICO"]` foram conferidas contra o
texto "REQUISITOS:" de `docs/swade_basico`. 133 delas batem exatamente
(estágio, atributo, perícia, vantagem prévia e limite de compra). Os achados
abaixo são os que não batem ou merecem nota.

### Bugs confirmados — CORRIGIDOS

1. **Muito Duro na Queda (`muito_duro_na_queda`) — faltava Vigor d12+.**
   Livro (linha ~4531): "REQUISITOS: Lendário, Duro na Queda, **Vigor d12+**".
   O JSON tinha `atributos: {}` nas 9 reimpressões (BASICO, FANTASIA, HORROR,
   SCI_FI, CRYSTAL_HEART, WISEGUYS, ARTE_DA_GUERRA, CIDADE_SOL_VAPOR,
   DEADLANDS) — só a cópia do PATHFINDER já tinha `"Vigor": 12` certo (serviu
   de referência pro formato da correção). **Corrigido**: adicionado
   `"Vigor": 12` em `atributos` nas 9 instâncias que estavam faltando.

2. **Drenar a Alma (`drenar_a_alma`) e Surto de Poder (`surto_de_poder`) —
   requisito de perícia arcana nunca era checado, em nenhum livro.**
   Livro (Básico, linhas ~3765-3781 e ~3916-3922): exigem "perícia arcana
   d10+" e "perícia arcana d8+" respectivamente — ou seja, a perícia arcana
   específica do Antecedente Arcano da personagem (Fé, Conjurar, Foco,
   Psiônicos ou Ciência Estranha, dependendo de qual AA ela tem).
   O JSON representava isso com `"pericias": {"Arcana": 10}` /
   `{"Arcana": 8}` — só que **não existe nenhuma perícia chamada "Arcana"**
   em `pericias.json` (as perícias arcanas reais têm nome próprio cada uma).
   `ValidateRequirementsUseCase.execute()` e o equivalente em
   `CriadorState.kt` (~linha 5430) resolvem perícia por nome via
   `getBestPericia("Arcana")`, que não encontrava nada e retornava `null`;
   como `vinculado_pericia = false` para essas duas vantagens, o código caía
   no ramo "todas as perícias obrigatórias" (AND), e o padrão
   `getBestPericia(nome) ?: return@any false` tratava "perícia não
   encontrada" como "não é motivo de bloqueio" — ou seja, o requisito de
   perícia arcana ficava sempre satisfeito, não importa o valor real.
   Resultado prático: dava pra comprar Drenar a Alma ou Surto de Poder tendo
   Antecedente Arcano com a perícia arcana em d4. Isso se repetia em **9
   instâncias** (a vantagem é reimpressa em vários livros): `drenar_a_alma`
   em BASICO/FANTASIA/HORROR/SCI_FI e `surto_de_poder` em BASICO/FANTASIA/
   HORROR/SCI_FI/DEADLANDS.
   **Corrigido** trocando `"pericias": {"Arcana": N}` por
   `"periciaMinOpcional": {"Fé": N, "Conjurar": N, "Foco": N, "Psiônicos": N,
   "Ciência Estranha": N}` (as 5 perícias arcanas reais do jogo) nas 9
   instâncias — reaproveita o mecanismo de "qualquer uma destas perícias"
   que já existe no motor (mesmo campo usado por Arma Predileta/Atirador/
   Tiro Mortal) sem precisar mexer em código Kotlin: como a personagem só
   tem UMA perícia arcana de cada vez (a do seu Antecedente Arcano), o
   check "qualquer uma das 5 com o mínimo" acerta sozinho qual delas é a
   relevante. Não mexi no `PATHFINDER` (que já usa `periciaMinOpcional`,
   mas com uma chave `"Perícia Arcana"` que também não bate com nenhuma
   perícia real — fica para a auditoria específica do Pathfinder).

### Imprecisão de modelagem (não trava nada errado hoje, mas é "mais ou menos")

3. **Engenhoqueiro, Esforço Extra, Guerreiro Sagrado/Profano, Mago,
   Mentalista — exigem o Antecedente Arcano ESPECÍFICO, não "qualquer um".**
   O livro é explícito nessas 5 (diferente de Artífice/Canalização/
   Concentração/Drenar a Alma/Novos Poderes/Pontos de Poder/Recarga Rápida/
   Surto de Poder, que realmente pedem "Antecedente Arcano (qualquer um)"):
   - Engenhoqueiro: "Antecedente Arcano (**Ciência Estranha**), Ciência
     Estranha d6+" (linha ~3782)
   - Esforço Extra: "Antecedente Arcano (**Dom**), Foco d6+" (linha ~3825)
   - Guerreiro Sagrado/Profano: "Antecedente Arcano (**Milagres**), Fé d6+"
     (linha ~3836)
   - Mago: "Antecedente Arcano (**Magia**), Conjurar d6+" (linha ~3847)
   - Mentalista: "Antecedente Arcano (**Psiônicos**), Psiônicos d6+"
     (linha ~3866)

   No JSON, as 5 usam `vantagens_previas: ["antecedente_arcano"]` (o id
   genérico), não `antecedente_arcano_ciencia_estranha` /
   `antecedente_arcano_dom` / etc. Conferi se isso é inofensivo por causa da
   perícia arcana ser exclusiva de cada Antecedente Arcano (o que tornaria a
   checagem de AA específico redundante) — **não é**: procurei em todo
   `app/src/main/java` por algum bloqueio que impeça alguém sem Antecedente
   Arcano (Magia) de simplesmente subir a perícia Conjurar, e não existe
   nenhum (`PericiasSection.kt` não faz essa checagem, não há
   `periciasArcanasExclusivas` nem equivalente). Ou seja, hoje é possível
   (ainda que seja uma pegada meio artificial) ter Antecedente Arcano
   (Milagres) + subir Conjurar por fora e comprar Mago sem nunca ter
   Antecedente Arcano (Magia). Recomendo trocar o `vantagens_previas` dessas
   5 pro id específico do Antecedente Arcano correspondente.

### Achado sobre requisito "veja texto" — conferido, correto

4. **Ameaçador (`ameacador`) — "N, Veja Texto".** O texto (linha ~4128) diz
   "Novato, qualquer um dentre Sanguinário, Desagradável, Sem Escrúpulos ou
   Feio" — bate com o `vantagens_previas` do JSON (as 4 Complicações). A
   lógica em `ValidatePrerequisiteUseCase.atendePreviasPorComplicacaoParaAmeacador`
   trata isso como OR (basta ter uma das quatro), que é o comportamento
   certo. Only a título de nota: esse mesmo código também aceita "Sombrio"
   (Complicação do Fantasia) e "Sinistro" (Complicação do Superpoderes) como
   liberadoras extras — não é erro do Básico, é conteúdo vindo de outro
   livro; deve ser conferido quando eu auditar Fantasia/Superpoderes se o
   texto desses livros realmente diz algo como "conta como liberadora de
   Ameaçador".

### Bug de limite de compra — CORRIGIDO

5. **Pau Pra Toda Obra (`pau_pra_toda_obra`) — `limite_compra: infinito`
   estava errado.** O texto (linha ~4077-4097) descreve um bônus único
   (d4/d6) que se move de perícia em perícia conforme a personagem estuda
   coisas novas ("dura até que a personagem tente aprender um assunto
   diferente") — não há frase de repetibilidade como as que aparecem em
   Erudito ("pode ser escolhida diversas vezes"), Arma Predileta ("é
   permitido escolher esta Vantagem várias vezes"), Senhor das Feras,
   Conexões, Seguidores ou Novos Poderes. Confirmado com o dono do projeto:
   compra única, sem recompra. **Corrigido**: `limite_compra` mudado de
   `infinito` para `uma_vez` nas 9 instâncias do catálogo oficial (BASICO,
   FANTASIA, HORROR, SCI_FI, CRYSTAL_HEART, WISEGUYS, ARTE_DA_GUERRA,
   CIDADE_SOL_VAPOR, DEADLANDS). O `PATHFINDER` já não tinha o campo
   `limite_compra` definido — fica para a auditoria específica do Pathfinder
   conferir o valor padrão aplicado nesse caso.

### Nota menor — confirmado, sem mudança necessária

6. **Parceiro (`parceiro`) — `limite_compra: uma_vez`.** O texto (linha
   ~4499-4522) permite escolher a Vantagem de novo se o Parceiro morrer
   ("a menos que esta vantagem seja escolhida novamente"). Confirmado com o
   dono do projeto: também é compra única, sem recompra — `uma_vez` já está
   certo, nenhuma mudança necessária aqui.

### Falsos alarmes descartados durante a auditoria (documentando pra não
refazer o trabalho depois)

- **Arma Predileta, Atirador, Tiro Mortal** pareciam estar sem o requisito
  de perícia "à escolha"/"ou" (ex.: "Atletismo ou Atirar d8+"), mas na
  verdade isso está corretamente modelado no campo `periciaMinOpcional`
  (`{"Atletismo": 8, "Atirar": 8}` etc.) — só não aparece se você olhar
  apenas `atributos`/`pericias`. Conferido certo.
- **Corredor** parecia ter uma perícia extra (`Atletismo: 6`) inventada,
  não presente na tabela-resumo do livro ("N, Agi d8"). A tabela-resumo
  realmente omite esse requisito, mas a descrição completa (linha
  ~3358-3359) confirma "Novato, Agilidade d8+, **Atletismo d6+**" — o JSON
  está certo, é a tabela-resumo (extração de PDF) que está incompleta.

## Próximos livros

Ainda faltam: Fantasia, Sci-Fi, Horror, Superpoderes, Pathfinder (Básico +
Compêndio), Deadlands (Básico + Compêndio), Arte da Guerra (+ Diário do
Kui), Crystal Heart (+ Muitos Corações), Wiseguys, Cidade do Sol a Vapor (3
livros). Vou seguir na mesma ordem de `docs/reports/book_index/` e anexar
uma seção por livro neste mesmo arquivo.
