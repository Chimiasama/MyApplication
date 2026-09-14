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

## Nota de método a partir daqui

Antes de auditar o Fantasia percebi que dá pra economizar trabalho: como
várias vantagens do Básico são *reimpressas* nos livros-companion com a
mesma tag (`"livros": ["<BOOK>"]`), comparei programaticamente o
`requisitos`/`limite_compra`/`vinculado_pericia` de cada reimpressão contra
a cópia do Básico já auditada, em vez de reler o texto inteiro de cada
companion pra cada vantagem repetida. Resultado: FANTASIA, SCI_FI, HORROR,
DEADLANDS e CRYSTAL_HEART reimprimem todas as vantagens do Básico
byte-a-byte iguais (0 diferenças) — nada a corrigir aí além do que já foi
corrigido globalmente (Muito Duro na Queda, Drenar a Alma, Surto de Poder,
Pau Pra Toda Obra, que se aplicam a todas as reimpressões). Já
ARTE_DA_GUERRA, WISEGUYS, CIDADE_SOL_VAPOR, SUPER e PATHFINDER têm
reimpressões que DIVERGEM do Básico (esperado — esses cenários trocam
perícia-base, Antecedente Arcano específico etc.) — essas divergências
específicas serão conferidas contra o texto de cada um desses livros na
hora de auditá-los. Daqui pra frente, cada seção de livro cobre só (a) as
vantagens genuinamente novas daquele livro e (b) as reimpressões que
divergem do Básico.

## FANTASIA — 59 vantagens novas + 14 Antecedentes Arcanos + 29 vantagens de arquétipo

Conferidas as 102 entradas tageadas `["FANTASIA"]` que não são reimpressão
byte-a-byte do Básico, contra o texto "REQUISITOS:" de `docs/swade_fantasia`.

### Bugs confirmados — CORRIGIDOS

1. **Familiar (`familiar`) — estágio errado.** Livro (linha ~2294):
   "REQUISITOS: Novato, Antecedente Arcano (Bruxo, Diabolista, Druida,
   Elementalista, Feiticeiro, Mago, Necromante, Xamã)". JSON tinha
   `estagio: "Experiente"`. **Corrigido** para `"Novato"`.
2. **Mago de Sangue (`mago_de_sangue`) — estágio errado.** Livro (linha
   ~2406): "REQUISITOS: Novato, Antecedente Arcano (qualquer um), uma
   disposição maligna". JSON tinha `estagio: "Experiente"`. **Corrigido**
   para `"Novato"`.
3. **Tiro Duplo Aprimorado (`tiro_duplo_aprimorado`) — faltava o requisito
   de perícia d10+.** Livro (linha ~2239-2241): "Heroico, Tiro Duplo,
   Atletismo d10+ (arremesso) ou Atirar d10+ (arco)". O texto já estava
   corretamente descrito em `observacoes`, mas não havia campo estruturado
   pra isso — só `vantagens_previas: [tiro_duplo]`. Note que
   `CriadorState.kt` (~linha 5477) já tem um caso especial pra essa vantagem
   específica, checando via `choice` da Tiro Duplo base se a MESMA perícia
   escolhida (Atletismo ou Atirar) está em d10+ — só que isso só existe
   nessa cópia do validador dentro de `CriadorState`, não em
   `ValidateRequirementsUseCase.kt` (a classe genérica equivalente). **Corrigido**
   adicionando `"periciaMinOpcional": {"Atletismo": 10, "Atirar": 10}` — não
   quebra o caso especial existente (que continua rodando depois e é mais
   restrito, pois exige a MESMA perícia escolhida na base), e cobre o
   validador genérico que não tinha nenhuma checagem de perícia.

### Achados sem correção — precisam de decisão de produto ou engine

4. **Bando de Guerra (`bando_de_guerra`) — requisito "pelo menos duas outras
   Vantagens de Liderança" não é modelável hoje.** Livro (linha ~2797-2799):
   "Carta Selvagem, Lendário, Comando **e pelo menos duas outras Vantagens
   de Liderança**, Seguidores". O JSON só tem
   `vantagens_previas: ["comando", "seguidores"]` — falta a exigência de "2
   das outras 6 Vantagens de Liderança" (Presença de Comando, Estrategista,
   Mestre Estrategista, Fervor, Inspirar, Líder Nato, Mantenham a
   Formação!). O esquema atual de `vantagens_previas` só sabe expressar
   "TEM estas vantagens específicas" (lista fixa, AND), não "tem N de um
   grupo" — precisaria de um campo novo (tipo
   `categoriasCustomizadasRequeridas`, mas contando mínimo de N em vez de
   "pelo menos 1 de cada categoria"). Não mexi porque isso é feature nova de
   engine, não correção de dado — fica registrado pra você decidir se vale a
   pena implementar. Nota à parte: a `observacoes` atual ("Carta Selvagem,
   +2 Liderança") parece um resquício de anotação errada/confusa, não bate
   com a descrição real da vantagem (ganho de Resiliente para Seguidores) —
   sugiro pelo menos trocar o texto da observação por algo que descreva
   corretamente o requisito que falta modelar.
5. **Conjurador Silencioso — exclusão "exceto Bardo" só em texto.** Mesmo
   padrão de limitação já visto no Básico (Antecedente Arcano específico
   exigido só por texto): o requisito real é "qualquer Antecedente Arcano
   EXCETO Bardo", mas o JSON usa o id genérico `antecedente_arcano` (que
   aceita Bardo também) + uma nota em `observacoes`. Não há mecanismo de
   "excluir X" no schema atual. Registrado, não corrigido.
6. **Cavaleiro — "Obrigação (Maior)" só em texto.** Mesmo padrão: existe uma
   Complicação `obrigacao` no catálogo (sem variação estrutural de
   Menor/Maior), então dava pra reforçar com `vantagens_previas:
   ["obrigacao"]` pra pelo menos exigir ALGUMA Obrigação — mas isso não
   distinguiria Menor de Maior. Não mexi sem confirmar se vale a pena (viria
   com risco de travar personagens com Obrigação Menor que hoje passam
   livre pela falta de checagem nenhuma).

### Confirmado correto (nada a corrigir)

Todas as outras 96 vantagens (23 de Combate, 6 de Antecedente restantes, 7
de Poder restantes, 10 Profissionais, 1 Social, 2 Estranhas restantes, 3
Lendárias restantes, os 14 Antecedentes Arcanos por arquétipo e as 29
vantagens específicas de arquétipo) batem exatamente com o texto
"REQUISITOS:" do livro, incluindo casos que já usam mecanismos corretos
(`tags` para Golpe de Asa/Asas e Queimar/Arma de Sopro, `periciaMinOpcional`
para Inimigo Predileto/Tiro Duplo/Tiro Preciso/Envenenador). Algumas têm
grafia sem acento em `atributos`/`pericias` (ex.: `"Espirito"`, `"Forca"`,
`"Sobrevivencia"`, `"Astucia"`) — não são bugs funcionais, porque toda
comparação de nome no app passa por `keyify()`/`semAcentos()`, que ignora
acentuação; é só uma inconsistência cosmética no JSON-fonte, sem efeito no
app.

## SCI-FI — 26 vantagens novas + 3 de Cibernéticos + 11 Antecedentes Arcanos + 26 vantagens de arquétipo

### Bug confirmado — CORRIGIDO

1. **Poder Favorito (`poder_favorito`, tag SCI_FI) — mesmo bug da "perícia
   arcana genérica" do Básico.** Livro (linha ~1627-1629): "Experiente,
   Antecedente Arcano (qualquer um), perícia arcana d8+". O JSON usava
   `"pericias": {"Perícia Arcana": 8}` — de novo, uma perícia chamada
   "Perícia Arcana" não existe (o Sci-Fi tem 11 Antecedentes Arcanos, cada
   um com sua própria perícia arcana: Fé, Foco, Ciência, Psiônicos ou
   Ciência Estranha), então o requisito nunca era checado de verdade, pelo
   mesmo motivo já documentado pra Drenar a Alma/Surto de Poder. **Corrigido**
   trocando por `"periciaMinOpcional": {"Fé": 8, "Foco": 8, "Ciência": 8,
   "Psiônicos": 8, "Ciência Estranha": 8}` (as 5 perícias arcanas reais que
   aparecem nos Antecedentes Arcanos deste livro).

### Achados sem correção — limitação de schema, não erro de dado

2. **Sinfonia Celestial (`sinfonia_celestial`) e Drones (`drones`) — tetos
   de compra não capturados.** O livro diz explicitamente "pode ser
   adquirida até quatro vezes" pras duas — o JSON já documenta isso em
   `observacoes`, mas `limite_compra` está como `infinito` (sem teto). O
   enum de `limite_compra` hoje não tem um valor tipo "até N vezes" — só
   `uma_vez`/`infinito`/variantes "por sessão/encontro/estágio/característica".
   Não mexi porque precisaria de um valor novo no enum + suporte no motor de
   avanço pra realmente travar em 4 compras.
3. **Equipado (`equipado`) — restrição de "só na criação de personagem" não
   modelada.** Mesma categoria de limitação já vista antes (timing
   restrito, sem campo pra isso no schema).

### Confirmado correto (nada a corrigir)

Os outros 25 "novas" (Capitão, Habitante de Gravidade Intensa, Terreno
Favorito, Gerenciador de Munição, Oportunista, Saque Rápido, Líder de
Equipe, Poderes Místicos, Adaptação Atmosférica/Gravitacional, Ataque
Furtivo (+Aprimorado), Controle Fino, Exocientista, Fita Adesiva e
Chiclete, Manobras Evasivas, Hacker Sob Pressão, Piloto Atirador, Reflexos
Aprimorados, Tiro Preciso, Enganador, Imortal, Milagreiro, Salvador do
Universo), as 3 de Cibernéticos (Cibertolerância, Cibersamurai, Ciborgue —
que nem estavam na lista antiga do índice do livro, mas conferem 100% com
o texto), os 11 Antecedentes Arcanos por arquétipo e as 26 vantagens
específicas de arquétipo batem exatamente com "REQUISITOS:" do livro,
incluindo as exclusões mútuas texto-a-texto (Mago Estelar × Renegado
Estelar) e o caso "Harmonizado", onde o próprio livro não define um
Estágio explícito — o JSON já documentava essa ambiguidade em
`observacoes` antes desta auditoria, sem precisar de mudança.

## HORROR — 10 vantagens gerais + 65 Vantagens Monstruosas (8 templates) + 10 Antecedentes Arcanos + 19 vantagens de arquétipo

### Bug confirmado — CORRIGIDO

1. **Poder Favorito (`poder_favorito_horror`) — mesmo bug da "perícia arcana
   genérica", versão ainda pior (faltava por completo).** Livro (linha
   ~656-658): "Experiente, Antecedente Arcano (Corrompido, Ocultista,
   Bruxaria), perícia arcana d8+". O JSON já documentava a restrição de
   Antecedente Arcano específico em `observacoes`, mas não tinha NENHUM
   campo estruturado pra perícia — nem o "Perícia Arcana" genérico
   (inexistente) que pelo menos registrava a intenção em outros livros, só
   texto solto. **Corrigido** adicionando `"periciaMinOpcional": {"Conjurar":
   8, "Foco": 8}` (Conjurar é a perícia de Ocultista/Bruxaria, Foco é a de
   Corrompido — as únicas 2 perícias arcanas entre os 3 Antecedentes
   permitidos por esta Vantagem).

### Achado sem correção — mesma limitação de schema já registrada

2. **Servo (`servo`, Vampiro) — teto "até cinco vezes" não capturado.**
   Mesmo padrão do Sci-Fi (Sinfonia Celestial/Drones): `limite_compra` está
   como `infinito`, sem representar o teto de 5 compras do livro.

### Verificação extra: dois "sumiços" que na verdade não eram bugs

Durante a conferência dos Antecedentes Arcanos por arquétipo apareceram
dois alarmes falsos que vale documentar para não repetir o trabalho:

- **Sexto Sentido** (exclusiva do Vidente) parecia estar faltando no
  catálogo — na verdade existe com o id `sexto_sentido_vidente` (não
  `sexto_sentido`, que é como o índice antigo do livro nomeava); requisitos
  batem 100% com o texto (Veterano, Antecedente Arcano (Vidente)).
- **Esteve na Encruzilhada** e **Favorecido**, exclusivas do Voduísta,
  existem DUAS vezes cada uma: uma tageada `HORROR` (Antecedente Arcano
  `antecedente_arcano_vuduista`, requisitos Novato+Espírito d8+Fé d8) e
  outra tageada `DEADLANDS` (Antecedente Arcano `antecedente_arcano_vuduismo`,
  requisitos Experiente+Espírito d8+Fé d8). Parecia duplicata/mistagueamento
  à primeira vista, mas são duas Vantagens DIFERENTES: o Compêndio de
  Horror e o Compêndio de Deadlands (`docs/swade_deadlands_compendio`, não
  o Deadlands básico) trazem cada um sua própria versão do Antecedente
  Arcano Voduísta/Vuduísmo, com nomes e flavor quase idênticos (a mesma
  mitologia real de loa rada/petro, mambo/houngan) mas Estágio de
  Antecedente Arcano diferente — conferi as duas cópias linha a linha
  contra os dois livros-fonte e ambas batem exatamente com o texto do seu
  próprio livro. Nenhuma mudança necessária.

### Confirmado correto (nada a corrigir)

As outras 9 vantagens gerais, as 65 Vantagens Monstruosas dos 8 templates
de Monstro Heroico (Anjo, Demônio, Fantasma, Lobisomem, Monstro de
Retalhos, Múmia, Revivido, Vampiro — incluindo todas as cadeias de
pré-requisito entre elas, tipo Fogo Infernal→Queimar, Luz Sagrada→Rajada
Abrasadora, Resistência→Resistência Divina, Invocar Bando→Invocar Bando
Maior, Regeneração Lenta→Regeneração Rápida), os 10 Antecedentes Arcanos
por arquétipo e as 19 vantagens específicas de arquétipo restantes batem
exatamente com o texto "REQUISITOS:" do livro.

## SUPERPODERES — 6 vantagens novas

Livro pequeno pro escopo de vantagens (o sistema de Super Poderes em si
mora em `super_poderes.json`, catálogo separado). As 6 vantagens
específicas (`superpoderes`, `o_melhor_que_ha`, `aguenta_o_tranco`,
`lider_de_equipe`, `dupla_dinamica`, `parceiro`) foram conferidas contra
`docs/swade_superpoderes`.

### Bug confirmado — CORRIGIDO

1. **Parceiro (`parceiro`, tag SUPER) — atributo Espírito d8+ inventado,
   sem base no livro.** Livro (linha ~1030): "REQUISITOS: Carta Selvagem,
   Veterano" — só isso, sem menção a nenhum atributo em lugar nenhum do
   texto ao redor. O JSON tinha `"atributos": {"Espirito": 8}`, travando
   incorretamente heróis Veteranos com Espírito abaixo de d8 de pegar um
   parceiro. **Corrigido** removendo o atributo (`atributos: {}`), mantendo
   só Veterano + Carta Selvagem, que é o que o livro realmente pede.

### Confirmado correto (nada a corrigir)

As outras 5 vantagens (Superpoderes, O Melhor que Há, Aguenta o Tranco,
Líder de Equipe, Dupla Dinâmica) batem exatamente com o texto do livro.

## PATHFINDER (Básico + Compêndio) — ~200 vantagens

Este livro usa um schema de JSON mais antigo (várias entradas não têm
`limite_compra`/`vinculado_pericia`, alguns campos `requisitos` vêm mais
enxutos) e é um "cenário substituto": confirmei em
`ContentVisibility.kt` (`getActiveOrigins()`) que ativar o Compêndio de
Pathfinder **remove** `BASICO` do conjunto de livros ativos (mesma regra
para Deadlands, Crystal Heart, Arte da Guerra, Cidade do Sol a Vapor e
Wiseguys — são cenários que SUBSTITUEM o livro básico, não o estendem
como Fantasia/Sci-Fi/Horror/Supers). Ou seja, os ~112 ids que reaparecem
tageados `PATHFINDER` com conteúdo diferente do Básico **não são
duplicatas conflitantes** — é o livro básico inteiro sendo desligado e
substituído pelas versões próprias do Pathfinder, que valem sozinhas.
Por isso, toda comparação de conteúdo Pathfinder foi feita contra o
próprio texto de `docs/swade_pathfinder_basico`/`docs/swade_pathfinder_compendio`,
nunca contra o Básico.

### Achado sem correção — gap estrutural relevante, precisa de decisão de engine

1. **"Antecedente Arcano (qualquer um) OU Poderes Místicos (qualquer um)"
   não é validado em NENHUMA das ~13 vantagens que exigem isso.** O
   Pathfinder introduz "Poderes Místicos" como alternativa ao Antecedente
   Arcano tradicional (pacotes simplificados tipo Bárbaro/Guerreiro/Ladino
   dão poderes sem a Vantagem cheia), e o livro frequentemente permite "AA
   OU PM" como pré-requisito: Artífice, Canalização, Concentração,
   Guerreiro Sagrado/Profano, Novos Poderes, Pontos de Poder (Poder);
   Arqueiro Arcano, Cavaleiro Místico, Discípulo do Dragão, Trapaceiro
   Arcano, Agoureiro (Prestígio); Místico Teurgo (exige dois Antecedentes
   Arcanos diferentes, ainda mais específico). Nenhuma dessas tem
   `vantagens_previas` apontando pra `antecedente_arcano` (ao contrário do
   Básico/Fantasia/Sci-Fi/Horror, que pelo menos fixam o lado "Antecedente
   Arcano" do requisito) — só existe um texto solto em `observacoes`. Não
   dá pra simplesmente adicionar `vantagens_previas: ["antecedente_arcano"]`
   porque isso quebraria quem tem só Poderes Místicos (a Vantagem ficaria
   impossível pra metade de quem deveria poder pegá-la) — precisaria de um
   mecanismo novo tipo "qualquer um destes dois grupos", que não existe
   hoje. Documentando pra decisão de produto/engine, não mexi.

### Bug confirmado — CORRIGIDO

2. **Surto de Poder (`surto_de_poder`, tag PATHFINDER) — "Perícia Arcana"
   genérica quebrada ao contrário do resto: em vez de nunca bloquear (bug
   do Básico/Sci-Fi/Horror), aqui ela SEMPRE bloqueia.** Livro (linha
   ~5778-5781): "Carta Selvagem, Novato, Antecedente Arcano (qualquer um),
   perícia arcana d8+". O JSON usava `"periciaMinOpcional": {"Perícia
   Arcana": 8}` — só que como não existe perícia chamada "Perícia Arcana",
   e `periciaMinOpcional` usa lógica "OU" (precisa bater pelo menos uma),
   nenhuma perícia real bate NUNCA, então o requisito é impossível de
   cumprir — ninguém consegue comprar essa Vantagem, pro personagem
   nenhum. **Corrigido** trocando pelas 3 perícias arcanas reais que o
   Pathfinder realmente usa (conferido em `geral_arcano_info.json`, chaves
   `_PF`): `"periciaMinOpcional": {"Conjurar": 8, "Performance": 8, "Fé":
   8}` (Conjurar p/ Mago/Feiticeiro, Performance p/ Bardo, Fé p/
   Clérigo/Druida/Paladino).

### Confirmado correto (nada a corrigir)

Verifiquei nome-a-nome contra "REQUISITOS:" do livro: as 11 Vantagens de
Classe base (Bárbaro, Bardo, Clérigo, Druida, Feiticeiro, Guerreiro,
Ladino, Mago, Monge, Paladino, Patrulheiro) e suas 33 vantagens de
subclasse (3 cada, em cadeia Experiente→Veterano→Heroico requerendo só a
classe base), as 13 cadeias de Prestígio (10 do livro básico + 3 do
Compêndio: Arqueiro Arcano, Assassino, Cavaleiro Místico, Cronista
Desbravador, Dançarino das Sombras, Discípulo do Dragão, Duelista, Mestre
do Conhecimento, Místico Teurgo, Trapaceiro Arcano, Agoureiro, Cavaleiro
Infernal, Louva-a-Deus Vermelha), e uma amostra ampla das reimpressões que
divergem do Básico (Arma Predileta Aprimorada realmente é Veterano nesta
edição, não Experiente; Erudito/Investigador realmente usam Astúcia em
vez de Pesquisar; Drenar a Alma realmente usa Espírito d8+ em vez de
perícia arcana d10+ — 3 divergências reais confirmadas contra o texto,
não erros de dado). Único ponto de atenção sem ação: o requisito de Bardo
cita "Conhecimento Comum d6+", termo que não aparece em nenhum outro
lugar do livro — o JSON usa "Conhecimento Geral" (perícia real do
sistema), o que é quase certamente a leitura correta e a extração de
texto do PDF que está com um termo estranho, não o contrário.

### Nota especial: id `assassino` reaproveitado sem colisão

`assassino` no Pathfinder é uma Vantagem de Prestígio (Experiente,
habilidade Ataque Furtivo) completamente diferente da Vantagem
Profissional "Assassino" do Básico — mesmo id, conteúdo diferente. Cheguei
a suspeitar de colisão de id dentro do app, mas confirmei em
`ContentVisibility.kt` que isso nunca coexiste: como Pathfinder desliga
`BASICO` por completo (ver nota no topo desta seção), só uma das duas
versões de `assassino` fica visível de cada vez. Nenhuma ação necessária.

## DEADLANDS (Básico + Compêndio)

Cenário substituto (mesma regra do Pathfinder: `BASICO` é desligado quando
Deadlands está ativo — ver `ContentVisibility.kt`). O índice deste livro já
tinha uma rodada de auditoria bem recente e detalhada
(`docs/reports/book_index/deadlands.md`, marcas "[OK, resolvido em
2026-08-31]") que já corrigiu especificamente o Estágio de Fé Verdadeira
(Novato, não Veterano) e Pessoa de Mil Faces (Experiente, não Heroico) —
conferi as duas de novo contra o texto e ambas continuam corretas.

Fiz a conferência completa de "REQUISITOS:" pra todas as ~25 vantagens das
seções que ainda não tinham esse nível de detalhe registrado no índice (De
Antecedente, De Combate, Profissionais, Sociais, Estranhas, Lendárias — as
sem nota de "resolvido").

### Bug confirmado — CORRIGIDO

1. **Contador de Histórias (`contador_de_historias`) — faltava a opção
   "ou Persuadir".** Livro (linha ~1404-1405): "Novato, Performance ou
   Persuadir d8+". O JSON tinha `"pericias": {"Performance": 8}` — só
   Performance, travando quem tem Persuadir d8+ mas não Performance
   (contrariando o "ou" do livro). **Corrigido** trocando por
   `"periciaMinOpcional": {"Performance": 8, "Persuadir": 8}`.

### Confirmado correto (nada a corrigir)

As outras ~24 vantagens dessas seções (Humor Ácido, Veterano do Oeste
Estranho, Duelista, Martelar o Cão/Aprimorado, Não Me Irrite!, Saque
Rápido, Agente, Batedor, Coragem, Nascido na Sela, Patrulheiro Territorial,
Soldado, Trapaceiro, Delegado Federal, Reputação, Atormentado, Determinação/
Verdadeira, Talento, Condenado, Eis um Cavalo Amarelo..., Mão Direita do
Diabo, Rápido como um Raio) batem exatamente com o texto — incluindo casos
de perícia mínima incomum como Patrulheiro Territorial (Sobrevivência d4+,
não d6+, conferido e correto).

As ~70 vantagens restantes (Atormentado detalhado, Cientista Louco,
Mascate, Mestre do Chi, Patrulheiro Territorial avançado, Xamã, Voduísta,
Bruxa) já tinham passado por uma auditoria específica e recente registrada
no índice do livro (com correções já aplicadas onde precisava) — não
reabri cada uma do zero, mas as amostras que conferi (Fé Verdadeira,
Pessoa de Mil Faces, Delegado Federal) continuam batendo com o texto.

## ARTE DA GUERRA (+ Diário do Kui) — 35 + 5 vantagens novas

Cenário substituto (mesma regra de `ContentVisibility.kt`: desliga
`BASICO`). Conferidas as 40 vantagens novas/renomeadas contra
`docs/swade_adg` e `docs/swade_adg_diario_do_kui`.

### Bug confirmado — CORRIGIDO

1. **Aristocrata (`aristocrata`) duplicado dentro do próprio livro.**
   Achei DUAS entradas com `id: "aristocrata"` tageadas `ARTE_DA_GUERRA` ao
   mesmo tempo — não é o padrão são/esperado de reimpressão entre livros
   diferentes (tipo o `assassino` do Pathfinder, que nunca colide porque
   `BASICO` é desligado); aqui as duas conviviam na MESMA lista ativa,
   aparecendo duas vezes pro jogador. Uma tinha a descrição idêntica à
   Vantagem do livro básico ("Este indivíduo nasceu com privilégios...");
   a outra tinha a descrição própria do Arte da Guerra ("O Herói vem de
   uma linhagem nobre de um Clã...", linha ~7392-7401), que é a que
   realmente bate com o texto do livro. **Corrigido** removendo a cópia
   errada (a com texto do Básico) — sobrou só a versão com o texto correto
   do Arte da Guerra. Requisitos (`Novato`, sem outros) eram idênticos nas
   duas cópias, então isso não muda nenhum cálculo, só some com o card
   duplicado na lista de Vantagens.

### Confirmado correto (nada a corrigir)

As outras 39 vantagens (Domínio, Ferimento Extra, Legado, Linhagem
Temível, Mentor, Rico; as 12 Vantagens de Chi; as 9 de Combate; as 7
Profissionais; as 15 de Tropo, incluindo os requisitos condicionados a
escolha de Tropo/Ferramentas do Ofício tipo "Vínculo Espiritual"/"Talismãs";
Danificar a Roupa; e as 5 do Diário do Kui) batem exatamente com o texto.
Ordem-Unida repete a mesma limitação de schema já registrada no Fantasia
(Bando de Guerra): "quaisquer duas Vantagens de Liderança" fica só em
`observacoes`, sem checagem estrutural de "2 de um grupo".

## CRYSTAL HEART (+ Muitos Corações) — 19 vantagens novas + 4 de uso alterado

Cenário substituto (`BASICO` desligado quando ativo). Conferidas as 19
vantagens novas e as 4 com "uso alterado" (Aristocrata, Arma Predileta,
Comando, Conexões — o livro só reescreve o efeito de texto, sem mudar
requisito) contra `docs/swade_crystal_heart`. "Muitos Corações" é só
catálogo adicional de Cristais (`crystal_coracoes.json`), não introduz
vantagens novas.

### Confirmado correto (nada a corrigir)

Todas as 23 batem exatamente com "Requisitos:" do livro (aqui em
minúsculas, formato diferente dos outros livros — só variação de
digitação da fonte, sem efeito). Inclui os 5 traços culturais por Terra
Natal (Bogoviano, Fjordstadiano, Ilhéu, Maseiano, Zingamaiano), todos
corretamente modelados via `tags` em vez de `vantagens_previas` (aponta
pra origem/ancestralidade, não pra outra vantagem). O índice do livro já
tinha identificado 5 vantagens tageadas `CRYSTAL_HEART`
(`sintonizacao_cristal`, `troca_rapida`, `resiliencia_cristalina`,
`sobrecarga_segura`, `arma_predileta_aprimorada`) sem correspondência
textual nos dois arquivos-fonte disponíveis — não achei nada de novo pra
mudar essa conclusão, mantidas como estão.

## WISEGUYS — 33 vantagens novas

Cenário substituto (`BASICO` desligado quando ativo). Conferidas as 33
vantagens novas do Capítulo 3 contra `docs/swade_wiseguys_jogador`.

### Bugs confirmados — CORRIGIDOS

1. **Notório (`notorio`) — Intimidar d8+ inventado, sem base no livro.**
   Livro (linha ~4397-4398): "REQUISITOS: Carta Selvagem, Novato" — só
   isso. O JSON tinha `"pericias": {"Intimidar": 8}` — aparentemente um
   copia-e-cola do EFEITO da vantagem ("rerrolagem gratuita em testes de
   Intimidação...") pro campo de requisito, travando por engano quem não
   tem Intimidar d8+. **Corrigido**: `pericias` volta a `{}`.
2. **Motorista de Fuga (`motorista_fuga`) — faltava Astúcia d6+.** Livro
   (linha ~4740-4742): "Novato, Astúcia d6+, Dirigir d8+". O JSON só tinha
   `Dirigir: 8`. **Corrigido**: adicionado `"Astúcia": 6` em `atributos`.

### Confirmado correto (nada a corrigir)

As outras 31 vantagens (Assassino Impiedoso, Bom Companheiro, Nascido nas
Ruas, Artista Gun-Fu, Beijo da Morte/Aprimorado, Guarda-Costas, Fanfarrão,
Lutadora de Patins, Manobrar e Atirar, Sequestrador, Telecatch, Líder de
Time, Rebaixar, Cozinheiro, Especialista em Explosivos, Falsificador,
Limpador, Mestre da Fuga, Mestre do Disfarce, Motorista Agressivo,
Trambiqueiro, Trapaceiro, Acima da Lei, Capanga, Insistente/Persistente,
Amigo Meu, Dama da Sorte, Em Outro Patamar, Intocável) batem exatamente
com o texto.

## CIDADE DO SOL A VAPOR (3 livros: Livro do Criador, Livro dos Mortais, Movimento Vermelho)

### Bug estrutural grande, atravessando o livro inteiro — CORRIGIDO

Ao conferir a primeira vantagem nova deste livro (`cavalheiro_completo`),
percebi que o campo `requisitos` não era um objeto (`{estagio, atributos,
pericias, ...}`) como em todos os outros 1800+ registros do catálogo — era
uma **string única** (ex.: `"Novato, Aristocrata, Lutar d8+, Atirar d8+."`).
Fui conferir `Requisito.kt`/`RequisitoSerializer` pra entender como o app
lê isso, e o comportamento é sério: quando `requisitos` é uma string, o
deserializador (`RequisitoSerializer.deserialize`, ramo `is JsonPrimitive`)
só extrai o Estágio (primeiro token antes da vírgula, se bater com um dos
nomes conhecidos: Novato/Experiente/Veterano/Heroico/Lendário) e joga a
string inteira pra `observacoes` — **nenhum atributo, perícia ou vantagem
prévia é checado**, só o Estágio (e olhe lá: se o Estágio não vier em
primeiro na frase, tipo `"Anjo, Novato"`, nem isso é reconhecido, e a
Vantagem fica sem NENHUM requisito).

Contei quantas vantagens do catálogo inteiro (não só deste livro) usam esse
formato de string solta: **72, todas no `CIDADE_SOL_VAPOR`** — nenhum outro
livro usa esse formato. Ou seja, as 72 vantagens novas deste cenário
(Antecedentes Arcanos e suas vantagens exclusivas, vantagens de Combate/
Profissional/Social/Estranha do Livro dos Mortais, as 8 de Organizações/
Sociedades Secretas, as 22 cartas de Tarô da Nova Era, e as 11 do Movimento
Vermelho) estavam publicadas no app sem NENHUMA das checagens de atributo/
perícia/pré-requisito que o texto do livro realmente pede — só o Estágio
(quando reconhecido). Isso é bem mais sério que os bugs pontuais dos outros
livros: não é "uma vantagem com um requisito errado", é "72 vantagens sem
quase nenhum requisito aplicado".

**Corrigido**: reescrevi as 72 entradas para o formato estruturado normal
(`estagio`/`atributos`/`pericias`/`periciaMinOpcional`/`vantagens_previas`/
`observacoes`), lendo cada uma contra o texto original
(`docs/swade_csv_livro_do_criador`, `docs/swade_csv_livro_dos_mortais`,
`docs/swade_csv_movimento_vermelho`) pra confirmar atributo/perícia/dado
certos antes de estruturar — não confiei só na string já cadastrada, reli
o "Requisitos:" de cada uma no livro-fonte. Pontos de atenção durante a
conversão:

- **`aa_magia_negra` vs. `aa_magia_das_trevas`**: o livro nomeia o mesmo
  Antecedente Arcano de duas formas em lugares diferentes (resumo do
  Capítulo Um chama de "Magia Negra", a definição completa no Capítulo Sete
  chama de "Magia das Trevas") — e o catálogo tinha as DUAS como entradas
  separadas com descrição idêntica. Conferi qual delas é a realmente
  acessível pelo jogador: `VantagensSection.kt` (linha ~1292) usa
  `"Magia Negra" to "aa_magia_negra"` no seletor de Antecedente Arcano do
  cenário — `aa_magia_das_trevas` nunca aparece em nenhuma tela. Por isso,
  ao estruturar as 3 vantagens exclusivas (Irmão da Noite, Poder do Sangue,
  Vontade Sombria — o livro as descreve como dependentes de "Antecedente
  Arcano (Magia das Trevas)"), apontei `vantagens_previas` pra
  `aa_magia_negra` (a que o jogador realmente consegue ter), não pro nome
  literal do texto. Não apaguei `aa_magia_das_trevas` — é conteúdo órfão
  (existe no catálogo mas nenhuma tela leva a ele), fora do escopo desta
  auditoria de requisitos; fica registrado caso você queira limpar depois.
- **Requisitos sem estrutura possível hoje** (mesma categoria de limitação
  já documentada em outros livros — raça/ancestralidade exigida por nome,
  "não pode ter Vantagem X", "A ou (B e C)"): mantidos em `observacoes`, com
  o texto do livro preservado. Casos: `aa_demonio`/`aa_demonio_meio_demonio`/
  `aa_anjo`/`anjo_cinza`/`guerreiro_celestial` (exigem ancestralidade
  Demônio/Meio-Demônio/Anjo — não há campo de "requer esta raça" no schema,
  só o mecanismo de Monstro Heroico do Horror, que não se aplica aqui);
  `aa_tecnomagia`/`aa_magia_negra`/`aa_magia_das_trevas`/`aa_milagres`
  (exigem "Humano" — mesma limitação); `acordo_com_demonios` ("não pode ter
  a Vantagem Rico" — schema só sabe expressar "tem que ter", não "não pode
  ter"); `irmandade_das_seis_chaves` ("Magomecânico OU Consertar d10+ E
  Ciência d10+" — combinação OU/E que `vantagens_previas`/
  `periciaMinOpcional` não conseguem expressar sem arriscar ficar mais
  permissivo ou mais restritivo do que o livro pede); `taro_sem_alma`
  (exclusão cruzada com outra Vantagem/Antecedente Arcano/duas
  Complicações). Para `cavaleiro_de_sao_germain`, consegui uma checagem
  parcial: o livro exige a Complicação "Código de Honra (Ordem de São
  Germain)" especificamente, e o catálogo só tem `codigo_de_honra` genérico
  (sem variante por sabor) — usei `vantagens_previas: ["codigo_de_honra"]`
  (funciona citando complicações também, não só vantagens) para pelo menos
  exigir ALGUM Código de Honra, com o sabor exato só documentado em texto.
- **`Conhecimento Batalha` → `Conhecimento de Batalha`**: o livro (Líder de
  Manifestação, Vox Populi, Desmobilizar) escreve a perícia sem o "de", mas
  o nome real cadastrado em `pericias.json` para este livro é "Conhecimento
  de Batalha" — usei o nome real, senão a perícia nunca seria encontrada
  (mesma classe de bug do "Perícia Arcana" genérica already corrigida em
  outros livros).

### Confirmado correto após a conversão

As 72 reescritas foram conferidas uma a uma contra "Requisitos:" dos 3
livros-fonte (Antecedente Arcano de Demônio/Anjo/Tecnomagia/Magia Negra/
Milagres e suas 8 vantagens exclusivas; Acordo com Demônios, Parrudo,
Cavalheiro Completo; os 5 de Combate do Livro dos Mortais; Mecânico Cego,
Mestre das Caldeiras; Elegante; Me Chamo Igor, Seguidor de Nietzsche, as 22
cartas de Tarô; as 8 de Organizações; as 11 do Movimento Vermelho). Os
~127 ids restantes tageados `CIDADE_SOL_VAPOR` (reimpressões do Básico,
incluindo a exceção já conhecida de Novos Poderes usando `aa_tecnomagia`)
já tinham sido conferidos no início desta auditoria (comparação
programática contra o Básico) sem divergência nova.

Isso fecha a auditoria pedida: todos os livros do catálogo (Básico,
Fantasia, Sci-Fi, Horror, Superpoderes, Pathfinder, Deadlands, Arte da
Guerra, Crystal Heart, Wiseguys, Cidade do Sol a Vapor) foram conferidos
vantagem por vantagem contra o texto original.

## Rodada 2 — implementação das duas limitações estruturais (mesma data)

Depois do relatório acima, o dono do projeto pediu pra reconferir contra o
livro as duas limitações de schema documentadas ("N vantagens de um grupo"
e "vantagem A OU vantagem/perícias B") e implementar do jeito certo, sem
quebrar nada do que já existia.

### Reconferência contra o livro (achei imprecisões na Rodada 1)

- **"N de um grupo"**: só existem mesmo 2 casos no catálogo inteiro — Bando
  de Guerra (Fantasia, linha ~2797-2799: "Comando e pelo menos duas outras
  Vantagens de Liderança, Seguidores") e Ordem-Unida (Arte da Guerra, linha
  ~7868-7869: "Samurai, Comando, quaisquer duas Vantagens de Liderança").
  Busquei "pelo menos duas/três", "quaisquer duas/três", "dois Antecedentes"
  e "duas outras" em todos os 17 arquivos-fonte pra confirmar que não
  ficou nenhum caso parecido de fora — os outros resultados eram blocos de
  NPC ou efeitos de jogo (não requisitos).
- **"A OU B"**: reconferi cada uma das vantagens do Pathfinder que citei
  como "AA ou PM" na Rodada 1 e a lista **estava errada em 4 itens**. Lendo
  de novo linha por linha:
  - **Têm mesmo "AA ou PM"**: Drenar a Alma (+ Espírito d8+, não perícia
    arcana d10 como no Básico — o Pathfinder muda esse requisito de
    propósito), Guerreiro Sagrado/Profano (+ Voto — que eu tinha
    perdido na primeira leitura), Concentração, Pontos de Poder, Arqueiro
    Arcano (+ Atirar d8+), Cavaleiro Místico (+ Lutar d8+), Discípulo do
    Dragão (+ Ocultismo d6+), Trapaceiro Arcano (+ Ataque Furtivo,
    Ladinagem d8+), Agoureiro do Compêndio (+ Ocultismo d6+, Performance
    d6+). 9 vantagens, não 13.
  - **NÃO têm "ou PM"** (só Antecedente Arcano puro, eu tinha incluído
    errado): Artífice, Canalização, Novos Poderes. Essas ganharam o fix
    simples (`vantagens_previas: ["antecedente_arcano"]`), não o novo
    mecanismo de alternativas.
  - **Surto de Poder** também é só AA puro (sem "ou PM") — ganhou o mesmo
    fix simples, além da perícia arcana genérica já corrigida na Rodada 1.
  - **Místico Teurgo** é um caso à parte, não "A ou B": o requisito é
    "dois Antecedentes Arcanos com duas perícias arcanas diferentes" ao
    mesmo tempo — não implementei (é um terceiro padrão, "N distintos de
    um grupo dinâmico", mais raro e mais arriscado de encaixar no
    mecanismo genérico sem um caso especial só pra ele); fica documentado,
    registro devidamente sinalizado no código se quiser que eu faça depois.
  - Também achei, no meio da reconferência, que **Irmandade das Seis
    Chaves** (Cidade do Sol a Vapor) é o único caso de "vantagem OU
    combinação de perícias" (não "vantagem OU vantagem"): "Magomecânico OU
    Consertar d10+ e Ciência d10+" (linha ~8649-8651 de
    `docs/swade_csv_livro_dos_mortais`) — implementado com o mesmo
    mecanismo, numa alternativa só de perícias.

### Achado extra durante a reconferência: "Poderes Místicos" estava com o livro errado

Ao ir confirmar se a Vantagem `poderes_misticos` existia tageada
`PATHFINDER` pra poder ser referenciada nas alternativas acima, descobri
que ela só existia tageada **`FANTASIA`** — mas o texto da vantagem fala
em "Bárbaro (Força d8+)... Guerreiro (Lutar d8+)... Ladrão... Monge...
Paladino... Patrulheiro" — exatamente as 6 classes centrais do Pathfinder,
sem nenhuma relação com o Fantasia (que usa Antecedentes Arcanos por
arquétipo, não classes). Isso deixava Poderes Místicos invisível pra quem
realmente devia usá-la (Pathfinder) e presente por engano na lista do
Fantasia. **Corrigido**: `livros` trocado de `["FANTASIA"]` para
`["PATHFINDER"]`. Sem essa correção, o "ou Poderes Místicos" que acabei de
implementar não teria efeito prático nenhum pra ninguém jogando Pathfinder
— continuo achando que vale a pena registrar como nota pra você: essa
Vantagem não tem `choiceOptions` preenchido (ao contrário da versão do
Sci-Fi, que tem `["Guerreiro Estelar", "Telepata"]`) — pode valer a pena
adicionar `["Bárbaro", "Guerreiro", "Ladrão", "Monge", "Paladino",
"Patrulheiro"]` depois, se o app usa esse campo pra oferecer a escolha
numa tela; não mexi porque é outro assunto (falta de escolha interativa),
não requisito.

### Implementação (código)

Adicionei dois campos novos em `Requisito` (`model/Requisito.kt`), ambos
opcionais e com default vazio — **nenhuma das ~1900 vantagens existentes
muda de comportamento só por essa mudança de schema**, só as que eu
preenchi explicitamente:

- **`grupoMinimo: { opcoes: [ids], minimo: N }`** — "tem que ter pelo
  menos N destas opções", verificado JUNTO (E) com `vantagens_previas`,
  nunca no lugar dele.
- **`gruposAlternativos: [{ vantagens: [ids], pericias: {nome: mínimo} }, ...]`**
  — "basta UMA alternativa da lista bater por completo (E dentro dela)".
  Cada alternativa pode ter só vantagens (Antecedente Arcano OU Poderes
  Místicos), só perícias, ou os dois juntos.

A validação foi implementada nos dois lugares que hoje leem
`vantagens_previas` (achei que existiam DOIS, não um — `CriadorState.kt`
tem uma cópia inline da mesma lógica de
`model/usecase/ValidatePrerequisiteUseCase.kt`, usada em dois pontos
diferentes: `podeSelecionar` — o portão que decide se dá pra COMPRAR a
Vantagem — e `atendeRequisitosMantidos` — a revalidação de Vantagens já
compradas quando algo muda, tipo remover um pré-requisito depois). Extraí
a checagem "tem esta vantagem ou complicação" (que já existia, cobrindo
inclusive o caso especial de "Antecedente Arcano — qualquer variante") pra
uma função só, reaproveitada tanto pela checagem antiga de
`vantagens_previas` (comportamento 100% preservado) quanto pelas duas
checagens novas. Testes novos em
`ValidatePrerequisiteUseCaseTest.kt` cobrindo os dois mecanismos e
confirmando que uma vantagem sem nenhum dos dois campos novos se comporta
exatamente como antes.

**Não consegui rodar `./gradlew test`/build neste ambiente** (falha ao
resolver o Android Gradle Plugin por rede restrita no sandbox) — validei
manualmente linha por linha e escrevi os testes novos, mas recomendo
rodar a suíte completa antes de mesclar, especialmente
`ValidatePrerequisiteUseCaseTest`, `ValidateSelectionUseCaseTest` e
`RequisitoSerializerTest`.

### Vantagens corrigidas nesta rodada

- **`bando_de_guerra`** (Fantasia): ganhou `grupoMinimo` (7 opções de
  Liderança, mínimo 2); `observacoes` limpo de "+2 Liderança" (texto que
  não batia com a descrição real da vantagem).
- **`ordem_unida`** (Arte da Guerra): ganhou o mesmo `grupoMinimo`.
- **`irmandade_das_seis_chaves`** (Cidade do Sol a Vapor): ganhou
  `gruposAlternativos` (Magomecânico OU Consertar d10+/Ciência d10+).
- **`concentracao`, `drenar_a_alma`, `guerreiro_sagrado_profano`,
  `pontos_de_poder`, `arqueiro_arcano`, `cavaleiro_mistico`,
  `discipulo_do_dragao`, `trapaceiro_arcano`, `agoureiro`** (Pathfinder):
  ganharam `gruposAlternativos` (Antecedente Arcano OU Poderes Místicos);
  Guerreiro Sagrado/Profano também ganhou `vantagens_previas: ["voto"]`.
- **`artifice`, `canalizacao`, `novos_poderes`, `surto_de_poder`**
  (Pathfinder): ganharam `vantagens_previas: ["antecedente_arcano"]` (só
  precisavam do fix simples, sem alternativa nenhuma).
- **`poderes_misticos`**: livro corrigido de `FANTASIA` pra `PATHFINDER`.

### Pendente, não implementado

**Místico Teurgo** (Pathfinder) — "dois Antecedentes Arcanos com duas
perícias arcanas diferentes" — precisaria de um terceiro mecanismo (contar
Antecedentes Arcanos DISTINTOS, não apenas "tem pelo menos um") ou de um
caso especial hardcoded (mesmo padrão já usado pro Ameaçador/Tiro Duplo
Aprimorado). Como é uma vantagem só no catálogo inteiro, não implementei
pra não introduzir um mecanismo genérico só usado uma vez — me avise se
quiser que eu resolva esse caso também.
