# Análise de conteúdo — Compêndio de Ficção Científica e Compêndio de Horror

## Metodologia

Antes de sugerir qualquer adição, foi feito um levantamento do que já existe no app
(`app/src/main/assets/*.json` + código de domínio em `app/src/main/java/.../model`),
filtrando por `livros: ["SCI_FI"]` / `["HORROR"]` em cada catálogo. O resultado muda
bastante a expectativa inicial: **o app já implementa a grande maioria do conteúdo de
personagem dos dois livros**. O trabalho que falta é mais cirúrgico do que uma
importação do zero.

## O que já está implementado (confirmado nos dados)

| Catálogo | Sci-Fi | Horror |
|---|---|---|
| Ancestralidades (`ancestralidades.json`) | 28 | 10 (ancestralidades "mortais"; ver Monstros Heroicos abaixo) |
| Vantagens/Edges (`vantagens.json`) | 210 | 242 |
| Complicações (`complicacoes.json`) | 74 | 76 |
| Equipamento (`equipamentos.json`, por categoria) | ~68 itens em Armadura Energizada, Veículos, Armas de Longa Distância, Armas Veiculares, Ogivas etc. | ~39 itens gerais + **56 Itens Arcanos** em "Equipamento Mundano/Itens Arcanos" |
| Poderes (`poderes.json`) | 73 | 73 |
| Antecedentes Arcanos (dentro de `vantagens.json`, categoria `ANTECEDENTE`) | Capelão, Cavaleiro Estelar, Controlador de Luz Sólida, Cronomante, Dobrador, Gravcon, Místico, Pastor, Psionista, Tecnomante, Transmorfo | Alquimista, Bruxaria, Clérigo, Corrompido, Demonologista, Investigador Psíquico, Médium, Ocultista, Vidente, Voduísta |
| Cibernéticos (`scifi_ciberneticos.json`) | presente, dedicado | — |
| Mechas + armas/mods de mecha (`scifi_mechas.json`, `scifi_mecha_weapons.json`, `scifi_mecha_mods.json`) | presente, dedicado | — |
| **Monstros Heroicos (PJ monstro)** (`horror_monstros.json` + `MonstroTemplate.kt` + `TipoMonstroSection.kt`) | — | **Anjo, Demônio, Fantasma, Lobisomem, Monstro de Retalhos, Múmia, Revivido, Vampiro — já modelados como opção de personagem jogável**, com atributos bônus, habilidades e complicações próprias, e já plugado no fluxo de criação (`modoMonstroAtivo` / `aplicarTipoMonstro` em `CriadorState.kt`) |
| **Vantagens exclusivas de Monstro Heroico** (`vantagens.json`, categoria `MONSTRUOSAS`) | — | **65 vantagens** com `requisitos.template` travado no monstro certo (ex.: `LÂMINA DIVINA` exige `template: "anjo"`, `MORDIDA E GARRAS APRIMORADAS` exige `template: "lobisomem"`) — a pequena árvore de vantagens por tipo de monstro do livro já está modelada e corretamente restrita por pré-requisito |
| Relíquias/Artefatos (`equipamentos.json`, "Itens Especiais/Relíquias e Artefatos") | 7 itens (não segmentado por livro no momento) | — |

Ou seja: o pedido específico do usuário — "pj monstro eu quero no app" — **já está
atendido**. O que falta ali é aprofundamento, não criação do zero (ver abaixo).

## O que pode ficar de fora (bestiário/GM-only) — confirma a lógica pedida

Consistente com a distinção que você pediu (monstro-PJ ≠ monstro-inimigo) e com o fato
de o app ser um **criador de fichas de personagem solo**, não um kit de mestre:

- **Sci-Fi cap.13 "Bestiário"** (Criaturas do Cosmos, Inimigos dos Planetas Civilizados,
  Cidadãos Galáticos) e **Apêndice A "Impérios"** (frotas/NPCs de facção) — fora de
  escopo, sem equivalente no app hoje, e não recomendo criar: são blocos de estatística
  de NPC/facção, não opções de personagem.
- **Horror cap.6 "Criaturas"** e **Apêndice B (Mythos)** — mesmo raciocínio: bestiário
  de mestre, não personagem jogável. Ficam de fora.
- **Horror Apêndice A** (ensaio de como mestrar horror) — texto de orientação de mesa,
  não tem conteúdo mecânico para modelar.

## Lacunas reais (o que vale a pena melhorar)

### Sci-Fi

1. **Antecedente Arcano (Transmorfo)** e afins já existem, mas vale conferir se as
   listas de poderes liberados por antecedente arcano de sci-fi (`poderes_permitidos`)
   estão completas — não foi possível confirmar 1:1 com o livro nesta passada.
2. **Relíquias/Artefatos** (`Itens Especiais/Relíquias e Artefatos`, 7 itens) não estão
   marcadas com `livros: ["SCI_FI"]` — o catálogo do capítulo 12 (Artefatos) do
   Compêndio de Ficção Científica é maior no livro; vale auditar se os 7 itens batem
   com a lista do livro ou se é um subconjunto genérico multi-livro.
3. **Espaçonaves (cap.7)** como sistema próprio (cascos, módulos, tripulação mínima)
   não têm um arquivo dedicado equivalente ao de mechas — hoje "Veículos" cobre naves
   pequenas/aeronaves via `equipamentos.json`, mas não o sistema de espaçonave em
   escala maior do capítulo 7. Como o app é focado em ficha de PJ (não em gerenciar uma
   nave como "personagem" coletivo), isso é baixa prioridade — só vale a pena se algum
   dia o app expandir para veículos-PJ como já faz com mechas.
4. **Postos Avançados (cap.2) e Criador de Mundos (cap.10)** são sistemas de mesa/mestre
   (construção de base, geração de cenário) sem equivalente de "opção de personagem" —
   condizente com o escopo do app, recomendo não implementar.

### Horror

1. **Efeitos Expandidos de Medo** (psicose, tabela de medo expandida) — não há sistema
   dedicado no app hoje, só o texto avulso de "Tabela de Medo" citado em habilidades
   raciais (ex.: Tigre). Esse é o item de **maior valor** para adicionar: afeta
   diretamente o personagem jogador (efeitos de longo prazo tipo fobias/manias), então
   se encaixa na filosofia "conteúdo de PJ sim, bestiário não". Recomendo modelar como
   uma tabela de resultado (talvez reaproveitando o padrão de `Tropo`/`complicacoes`)
   em vez de mecânica automática de jogo.
2. **Refúgio** (safehouse/base de operações dos caçadores) — sistema de recurso de
   grupo. Como o app é de personagem individual, é opcional; se for modelado, faria
   mais sentido como uma lista de "upgrades" que o jogador pode anotar/comprar do que
   como uma mecânica de estado compartilhado.
3. **Proteções e Aprisionamentos** (círculos de proteção/aprisionamento) — hoje isso só
   aparece disperso dentro de descrições de poderes/itens individuais. Não há uma
   entrada organizada tipo "ritual de proteção" com custos e efeitos. Prioridade média:
   afeta jogadores que usam magia, mas é mais uma mecânica narrativa (o mestre valida)
   do que um dado de ficha.
4. **Regras de Massacre** (modo slasher letal) e **Sinais e Presságios** (presságios) —
   são variantes de estilo de jogo/ferramentas de mestre, não opções de personagem.
   Recomendo não implementar — fora do escopo de um criador de fichas.
5. **Conjuração Ritual** (magia em grupo com componentes aleatórios) — mecânica de mesa
   que envolve múltiplos personagens ao mesmo tempo; não se encaixa bem num app de
   ficha individual. Baixa prioridade.
6. **Itens Arcanos**: 56 itens já catalogados — vale uma auditoria item-a-item contra o
   capítulo 5 do livro (que tem bem mais de 56 entradas nomeadas, incluindo o
   subsistema de criação de itens alquímicos) para confirmar cobertura completa, mas a
   base já é sólida.

## Recomendação de prioridade

1. **Alta** — auditar minuciosamente os catálogos que já existem (vantagens, poderes,
   itens arcanos, cibernéticos) linha a linha contra os dois livros para achar
   entradas faltando ou com erros de custo/efeito — é onde há mais risco de gap
   silencioso, já que a cobertura estrutural está feita.
2. **Média** — as vantagens exclusivas por monstro já existem e estão corretamente
   restritas (`requisitos.template`); o que falta é só o recurso compartilhado de
   "Raiva"/corrupção (rastreamento de pontos, gatilhos, efeitos ao estourar) citado no
   livro — não encontrei esse rastreamento como mecânica de estado em `CriadorState.kt`
   nem em nenhum outro arquivo de modelo. Como é um recurso que afeta diretamente o
   personagem jogador, vale a pena adicionar, mas não é bloqueante: hoje o jogador pode
   anotar manualmente.
3. **Média** — Efeitos Expandidos de Medo (Horror) como tabela de referência de PJ.
4. **Baixa/opcional** — Refúgio, Proteções e Aprisionamentos, Espaçonaves como sistema
   dedicado.
5. **Não recomendado** — Bestiários (Sci-Fi cap.13, Horror cap.6 + Apêndice B), Impérios
   (Apêndice A Sci-Fi), Criador de Mundos, Postos Avançados, Regras de Massacre, Sinais
   e Presságios, Conjuração Ritual em grupo: todos GM-facing ou fora do escopo de um
   criador de fichas de personagem solo.
