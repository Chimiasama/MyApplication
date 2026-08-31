# Índice de Criação de Personagem — Crystal Heart

Cobre os dois arquivos-fonte do cenário **Crystal Heart** (livro principal, `docs/swade_crystal_heart`, 18469 linhas, e a expansão `docs/swade_crystal_heart_muitos_coracoes`, 734 linhas), restrito ao que afeta a criação de Agentes da Syn: origem/Terra Natal, o sistema de Cristal/Coração (o "Antecedente Arcano" próprio do cenário), Vantagens e Complicações novas ou alteradas, perícias específicas, equipamento temático e o catálogo de Corações iniciais. O cenário usa "Cristal" e "Coração" como sinônimos mecânicos: ao remover o coração de carne, o Agente insere um Cristal em um suporte cirúrgico, e esse Cristal passa a funcionar como coração — por isso o código já modela isso como `CrystalHeartIds`/`CrystalHeart.kt` e o catálogo `crystal_coracoes.json`. Os dados existentes (`crystal_coracoes.json`, `crystal_tropos.json`, e as entradas com tag `CRYSTAL_HEART` em `vantagens.json`, `complicacoes.json`, `pericias.json`, `ancestralidades.json`, `equipamentos.json`) cobrem bem o núcleo do livro principal, mas há uma lacuna grande: o Apêndice A do livro principal ("Mais Cristais", 33 Cristais adicionais) não está representado no app, e o sistema de "Tropos" do app não corresponde a nenhuma mecânica nomeada assim no texto-fonte (ver observações nas seções abaixo).

## Sistema de Coração (Cristal / Suporte / Canalizar Cristal)

Mecânica central do cenário, descrita no capítulo "Cristais e Corações" (`docs/swade_crystal_heart`, linhas 3922–6160):

- **Como escolhe (criação):** Passo 6 da criação (linha 1469) — o jogador escolhe (ou rola 1d20) um Cristal **Novato** da lista "Cristais Iniciais" (linha 4752). Pode gastar 1 ponto de Requisição para pegar um Cristal **Experiente** em vez de um Novato (linha 5576).
- **Antecedente Arcano (Canalizar Cristal)** (linha 1016): todo Agente recebe de graça; não usa Pontos de Poder — ativa-se o poder com uma rolagem de **Canalizar Cristal** (perícia de Espírito, começa em d4) com penalidade igual à metade do custo em PP do poder, arredondado para cima, mais a diferença de Estágio entre Agente e Cristal (linha 4193).
- **Anatomia de um Cristal** (linha 3998): cada Cristal tem **Estágio** (Novato/Experiente/Veterano/Heroico/Lendário — só 5 Cristais Lendários existem, exclusivos do Conselho da Syn), **Tema** (o conceito, usado para Truques e usos casuais), **Benefício** (uma Vantagem ou evolução de atributo sempre ativa) e **Disposição** (uma Complicação induzida pelo Cristal, geralmente Menor em Novatos e Maior em Experientes+).
- **Duplamente Vantajoso / Problemas Duplos** (linhas 4104–4167): se o Cristal concede uma Vantagem/Complicação que o Agente já tem, ela vira a versão aprimorada (ou os valores numéricos sobem em 1).
- **Truques** (linha 4336): gastando um Bene, o Agente ativa temporariamente qualquer poder compatível com o tema do Cristal (mesmo que não listado); duas ampliações na rolagem "fixam" o poder permanentemente para aquele Cristal.
- **Encaixar um Cristal** (linha 4380) e **Dessincronização** (linha 4300): trocar de Cristal fora de combate normalmente não exige rolagem; em estresse, rola-se Curar. Falha crítica em Canalizar Cristal ou ao encaixar causa dessincronização (o suporte "desliga", o Agente sufoca).
- **O Suporte** (linha 4412): dispositivo cirúrgico que substitui o coração; só ele permite acesso total e seguro aos poderes de um Cristal (usar um Cristal sem suporte é possível mas Limitado, linha 4509).
- **Substituindo Cristais** (linha 1479): ao longo da campanha, o Agente troca de Cristal ao encontrar novos ou gastando Requisição para "comprar" da Syn.

Ver também as Vantagens ligadas diretamente a este sistema na seção **Vantagens** abaixo (Focus, Conceder Poderes, Reter Poder, Especialista em um Cristal, O Cristal Certo) e as Complicações (Compulsivo, Dessintonia, Disposição Persistente).

### Catálogo de Corações/Cristais — cobertos em `crystal_coracoes.json` [OK — 38/38 do catálogo-base]

Todos os 38 registros do JSON (1 placeholder `heart_starter` + 37 Cristais reais) foram conferidos e batem com o texto-fonte: os 21 Novatos + 5 Experientes de "Cristais Iniciais" no livro principal, mais os 11 Cristais da expansão "Muitos Corações" (2 Novatos, 6 Experientes incluindo as duas versões de Tangente, 3 Veteranos).

- **Amigo** — id: `heart_amigo` — [OK] — Apoio de equipe; +2/+3 em Suporte, -3 na falha — linha ~4764 de swade_crystal_heart
- **Consciência** — id: `heart_consciencia` — [OK] — Vibrações mentais/telepáticas — linha ~4809 de swade_crystal_heart
- **Pateta** — id: `heart_pateta` — [OK] — Sorte e acaso — linha ~4844 de swade_crystal_heart
- **Iluminado** — id: `heart_iluminado` — [OK] — Manipulação de luz/brilho — linha ~4876 de swade_crystal_heart
- **Dançarino** — id: `heart_dancarino` — [OK] — Movimentos elegantes e graciosos — linha ~4902 de swade_crystal_heart
- **Espirituoso** — id: `heart_espirituoso` — [OK] — Tema social/humor (ver poderes no JSON) — linha ~4928 de swade_crystal_heart
- **Jingle** — id: `heart_jingle` — [OK] — Musical/sonoro — linha ~4965 de swade_crystal_heart
- **Retalho** — id: `heart_retalho` — [OK] — Entendimento intuitivo de mecanismos — linha ~5018 de swade_crystal_heart
- **Rali** — id: `heart_rali` — [OK] — Liderança pelo exemplo, reunir aliados — linha ~5061 de swade_crystal_heart
- **Rascal** — id: `heart_rascal` — [OK] — Travessura/trapaça (ver poderes no JSON) — linha ~5098 de swade_crystal_heart
- **Ricochete** — id: `heart_ricochete` — [OK] — Pontaria e trajetórias impossíveis — linha ~5142 de swade_crystal_heart
- **Inclinado** — id: `heart_inclinado` — [OK] — Cima/baixo subjetivos, gravidade/inclinação — linha ~5175 de swade_crystal_heart
- **Pensamento Silencioso** — id: `heart_pensamento_silencioso` — [OK] — Telepatia/silêncio mental — linha ~5220 de swade_crystal_heart
- **Destaque** — id: `heart_destaque` — [OK] — Chamar atenção/carisma — linha ~5262 de swade_crystal_heart
- **Forte** — id: `heart_forte` — [OK] — Manifestação física de força/braços — linha ~5304 de swade_crystal_heart
- **Sobrevivente** — id: `heart_sobrevivente` — [OK] — Resistência a qualquer condição adversa — linha ~5349 de swade_crystal_heart
- **Verbalista** — id: `heart_verbalista` — [OK] — Palavras como armas, oratória — linha ~5380 de swade_crystal_heart
- **Visão** — id: `heart_visao` — [OK] — Visão acima da média — linha ~5443 de swade_crystal_heart
- **Choque** — id: `heart_choque` — [OK] — Campos eletromagnéticos/choque — linha ~5469 de swade_crystal_heart
- **Lobo** — id: `heart_lobo` — [OK] — Transformação lupina, faro — linha ~5498 de swade_crystal_heart
- **Bolha** — id: `heart_bolha` — [OK, mas ver nota] — Bolhas rosa translúcidas, proteção/arremesso — linha ~5532 de swade_crystal_heart
- **Sombra** — id: `heart_sombra` — [OK] — Sombra corpórea — linha ~5613 de swade_crystal_heart
- **Veloz** — id: `heart_veloz` — [OK] — Velocidade — linha ~5645 de swade_crystal_heart
- **Fogo** — id: `heart_fogo` — [OK] — Explosões de fogo — linha ~5655 de swade_crystal_heart
- **Translucido** — id: `heart_translucido` — [OK] — Intangibilidade/transparência — linha ~5691 de swade_crystal_heart
- **Infusão** — id: `heart_infusao` — [OK] — Energia vital, cura — linha ~5734 de swade_crystal_heart
- **Cabelo** — id: `heart_cabelo` — [OK] — Crescimento de cabelo como arma/utilidade — linha ~39 de swade_crystal_heart_muitos_coracoes
- **Coração de Ouro** — id: `heart_coracao_de_ouro` — [OK] — Tema de generosidade/valor — linha ~83 de swade_crystal_heart_muitos_coracoes
- **Oásis** — id: `heart_oasis` — [OK] — Água/cura ambiental — linha ~134 de swade_crystal_heart_muitos_coracoes
- **Pia** — id: `heart_pia` — [OK] — Lixo/objetos empilhados — linha ~167 de swade_crystal_heart_muitos_coracoes
- **Pílula** — id: `heart_pilula` — [OK] — Veneno/gases tóxicos — linha ~225 de swade_crystal_heart_muitos_coracoes
- **Salto** — id: `heart_salto` — [OK] — Saltos/impacto — linha ~272 de swade_crystal_heart_muitos_coracoes
- **Tangente (Experiente)** — id: `heart_tangente_experiente` — [OK] — Versão Experiente do Cristal Tangente — linha ~324 de swade_crystal_heart_muitos_coracoes
- **Tangente (Heroico)** — id: `heart_tangente_heroico` — [OK] — Versão Heroica do mesmo Cristal — linha ~375 de swade_crystal_heart_muitos_coracoes
- **Destino (Meta)** — id: `heart_destino` — [OK] — Tema de "destino"/sorte narrativa — linha ~465 de swade_crystal_heart_muitos_coracoes
- **Domínio** — id: `heart_dominio` — [OK] — Controle/autoridade sobre outros — linha ~532 de swade_crystal_heart_muitos_coracoes
- **Elegância** — id: `heart_elegancia` — [OK] — Graça/duelo — linha ~592 de swade_crystal_heart_muitos_coracoes

**[CONFERIR]** `heart_bolha`: o JSON marca `estagio: "Experiente"`, mas no texto Bolha é o **último Cristal da lista de Novatos** (linha 5532), antes do cabeçalho "Cristais Experientes" (linha 5575) que introduz Veloz/Fogo/Sombra/Translucido/Infusão. Vale confirmar se o app pretendia isso de propósito (ex.: rebalanceamento) ou se é erro de captura do estágio.

### Cristais adicionais do Apêndice A "Mais Cristais" [OK — 33/33, resolvido em 2026-08-31]

O livro principal tem um apêndice inteiro de Cristais extras (linhas 16641–18287) no mesmo formato dos já catalogados (Descrição/Tema/Benefício/Disposição/Poderes), pensado para o Cristal evoluir/ser trocado ao longo da campanha. Os 33 foram transcritos e mesclados em `crystal_coracoes.json` (agora com 71 registros: 38 do catálogo-base + 33 do Apêndice A):

- **Tranquilo** — id: `heart_tranquilo` — [OK] — Novato — Rápido, mas Astúcia reduzida; pensamento simples — linha ~16643 de swade_crystal_heart
- **Linguarudo** — id: `heart_linguarudo` — [OK] — Novato — Revela verdades/segredos ocultos — linha ~16690 de swade_crystal_heart
- **Alizarina** — id: `heart_alizarina` — [OK] — Novato — Absorve impacto e libera com fúria — linha ~16723 de swade_crystal_heart
- **Bam-Bam** — id: `heart_bam_bam` — [OK] — Experiente — Energiza matéria inorgânica por contato — linha ~16766 de swade_crystal_heart
- **Imortal** — id: `heart_imortal` — [OK] — Experiente — Toca almas diretamente — linha ~16816 de swade_crystal_heart
- **Caçador** — id: `heart_cacador` — [OK] — Experiente — Perseguição e captura de alvos — linha ~16864 de swade_crystal_heart
- **Lembrança** — id: `heart_lembranca` — [OK] — Experiente — Memória perfeita — linha ~16897 de swade_crystal_heart
- **Demônio da Corda** — id: `heart_demonio_da_corda` — [OK] — Experiente — Corda infinita/unidimensional — linha ~16928 de swade_crystal_heart
- **Escrutinador** — id: `heart_escrutinador` — [OK] — Experiente — Análise minuciosa, esquemas — linha ~16990 de swade_crystal_heart
- **Escorregadio** — id: `heart_escorregadio` — [OK] — Experiente — Nada gruda; deslizar — linha ~17047 de swade_crystal_heart
- **Viajante** — id: `heart_viajante` — [OK] — Experiente — Peregrinação, sempre retorna ao lar — linha ~17085 de swade_crystal_heart
- **Vaso** — id: `heart_vaso` — [OK] — Experiente — Consciência como água em recipientes — linha ~17127 de swade_crystal_heart
- **Distorção** — id: `heart_distorcao` — [OK] — Experiente — Distorce distância/espaço — linha ~17182 de swade_crystal_heart
- **Nada** — id: `heart_nada` — [OK] — Experiente — Fraqueza/incapacidade — linha ~17198 de swade_crystal_heart
- **Ausência** — id: `heart_ausencia` — [OK] — Veterano — Espaços negativos/vazio — linha ~17253 de swade_crystal_heart
- **Fera** — id: `heart_fera` — [OK] — Veterano — Manifestação de besta primal — linha ~17295 de swade_crystal_heart
- **Rugido** — id: `heart_rugido` — [OK] — Veterano — Som extremamente alto — linha ~17332 de swade_crystal_heart
- **Garra** — id: `heart_garra` — [OK] — Veterano — Dor aguda e penetrante — linha ~17368 de swade_crystal_heart
- **Língua Seca** — id: `heart_lingua_seca` — [OK] — Veterano — Torna coisas secas/salgadas — linha ~17422 de swade_crystal_heart
- **Consistente** — id: `heart_consistente` — [OK] — Veterano — Movimento perpétuo — linha ~17454 de swade_crystal_heart
- **Ego** — id: `heart_ego` — [OK] — Veterano — Multiplicação do próprio ego/self — linha ~17517 de swade_crystal_heart
- **Alma** — id: `heart_alma` — [OK] — Veterano — Acessa memórias nos corações alheios — linha ~17563 de swade_crystal_heart
- **Jato** — id: `heart_jato` — [OK] — Veterano — Explosões de energia pressurizada — linha ~17610 de swade_crystal_heart
- **Intenso** — id: `heart_intenso` — [OK] — Veterano — Força ao custo de tudo mais — linha ~17671 de swade_crystal_heart
- **Boca** — id: `heart_boca` — [OK] — Veterano — Aniquilar e consumir — linha ~17707 de swade_crystal_heart
- **Pedra** — id: `heart_pedra` — [OK] — Veterano — Corpo vira rocha — linha ~17766 de swade_crystal_heart
- **Espectro** — id: `heart_espectro` — [OK] — Veterano — Presença dividida em vários lugares — linha ~17799 de swade_crystal_heart
- **Reverte** — id: `heart_reverte` — [OK] — Heroico — Reverte objetos feitos pelo homem ao natural — linha ~17896 de swade_crystal_heart
- **Aurora** — id: `heart_aurora` — [OK] — Heroico — Conexão com a natureza/frio — linha ~17918 de swade_crystal_heart
- **Escudo** — id: `heart_escudo` — [OK] — Heroico — Proteção — linha ~17988 de swade_crystal_heart
- **Esboço** — id: `heart_esboco` — [OK] — Heroico — Manifesta desenhos como construtos reais — linha ~18090 de swade_crystal_heart
- **Empatia** — id: `heart_empatia` — [OK] — Heroico — Emoções como objetos físicos — linha ~18151 de swade_crystal_heart
- **Marés** — id: `heart_mares` — [OK] — Heroico — Ciclos de subida/queda — linha ~18227 de swade_crystal_heart

**Nota sobre Estágios corrigidos por citação cruzada:** a determinação do Estágio de cada Cristal por posição no texto (entre os rodapés "Cristais Novatos/Experientes/Veteranos/Heroicos", linhas 16720/16765/17329/17988) é a fonte primária, mas essa seção do apêndice tem um artefato comprovado de extração em duas colunas (o texto de Distorção e Nada foi impresso intercalado, exigindo reconstrução por conteúdo temático — ambos confirmados como Experiente, sem mudança de estágio). Para 4 Cristais (`heart_ausencia`, `heart_fera`, `heart_reverte`, `heart_aurora`) a posição no texto conflitava com citações diretas e inequívocas do capítulo de Agentes-exemplo (linhas 3400–3900: "Jordan carrega Ausência (Veterano)", "Marc carrega Besta [= Fera, ver poder 'Besta Interior'] (Veterano)", "Reverte (Heroico) permite a Ivan...", "Aurora (Heroico), o Cristal que Yurhant usa..."). Nesses 4 casos a citação direta prevaleceu sobre a inferência posicional; o JSON registra a justificativa completa no campo `_duvida_estagio` de cada entrada, para conferência futura contra o livro físico/PDF se necessário.

## Tropos

**[CONFERIR — mecânica não localizada no texto-fonte]** `crystal_tropos.json` tem 4 registros (`treino_schultz`, `treino_mira`, `treino_yara`, `treino_leighmya`), cada um dando +1 em um atributo e duas perícias/edges "ganhas ao comprar" (ex.: `treino_schultz` → Vigor+1, Consertar, Brutamontes). O livro **não** usa a palavra "Tropo" em nenhum lugar (única ocorrência textual é o falso cognato "claustropos", um monstro). O mais próximo que existe é a lista de dez Supervisores/Mentores do Apêndice B (linha 18288 de swade_crystal_heart), da qual **Schultz, Trabalhador Incansável** (linha ~18455) e **Mira, a Sutil** (linha ~18443) realmente aparecem — mas o texto não lhes atribui bônus de atributo nem perícias fixas; isso é coberto mecanicamente pela Vantagem **Mentor** (veja Vantagens abaixo), que dá +1 perícia à escolha e +2 Requisição, não um pacote fixo por mentor. Os nomes **Yara** e **Leighmya** não aparecem em nenhum dos dois arquivos-fonte (nem entre os 10 mentores do Apêndice B, nem entre os "Doze Agentes Heroicos", que terminam em Yurhant). Recomenda-se checar se `crystal_tropos.json` foi extraído de uma fonte adicional (ex.: "Vivendo com a Syn", citado na introdução do livro como suplemento não incluído nestes dois arquivos) antes de tratar como conteúdo homebrew do app.

- **Discípulo de Schultz (O Trabalhador)** — id: `treino_schultz` — [CONFERIR] — Vigor+1, Consertar, Brutamontes; mentor "Schultz" existe (Apêndice B) mas sem esse pacote no texto — linha ~18455 de swade_crystal_heart
- **Discípulo de Mira (A Sutil)** — id: `treino_mira` — [CONFERIR] — Espírito+1, Carismático, Ladrão; mentora "Mira, a Sutil" existe (Apêndice B) mas sem esse pacote no texto — linha ~18443 de swade_crystal_heart
- **Discípulo de Yara (A Caçadora)** — id: `treino_yara` — [CONFERIR] — Agilidade+1, Mateiro, Atento; nome "Yara" não encontrado em nenhum dos dois arquivos-fonte — sem linha correspondente
- **Discípulo de Leighmya (A Analista)** — id: `treino_leighmya` — [CONFERIR] — Astúcia+1, Erudito, Investigador; nome "Leighmya" não encontrado em nenhum dos dois arquivos-fonte — sem linha correspondente

## Ancestralidades / Terra Natal

Passo 1 da criação (linha 541 de swade_crystal_heart): todos os Agentes são humanos (ganham 1 Vantagem grátis) e escolhem uma das Cinco Terras, que dá +1 dado num atributo (com limite d12+1) e tópicos extras de Conhecimento Geral. Todas as 5 estão em `ancestralidades.json` com tag `CRYSTAL_HEART`.

- **Bogovia** — id: `anc_bogovia` — [OK] — Força começa em d6; espírito de luta direta — linha ~572 de swade_crystal_heart
- **Fjordstad** — id: `anc_fjordstad` — [OK] — Astúcia começa em d6; abordagem de "quebra-cabeça" — linha ~575 de swade_crystal_heart
- **As Ilhas** — id: `anc_as_ilhas` — [OK] — Vigor começa em d6; fortitude e paciência — linha ~579 de swade_crystal_heart
- **Maseia** — id: `anc_maseia` — [OK] — Agilidade começa em d6; reação rápida — linha ~582 de swade_crystal_heart
- **Zingama** — id: `anc_zingama` — [OK] — Espírito começa em d6; visão de conjunto — linha ~586 de swade_crystal_heart

## Vantagens novas ou modificadas (Passo 3, linha 844)

Todas as 19 Vantagens novas ("Novas Vantagens", linha 1012) e as 4 Vantagens com uso alterado (Antecedente Arcano, Aristocrata, Arma Predileta, Comando, Conexões — linha 965) estão presentes em `vantagens.json` com tag `CRYSTAL_HEART`. **Nota**: `vantagens.json` também marca ~112 edges genéricos do SWADE básico (Ambidestro, Atraente, Acrobata, etc.) com a tag `CRYSTAL_HEART` — isso é esperado, pois o cenário reaproveita a lista padrão do SWADE (ver "Vantagens não usadas", linha 1007, para as poucas exceções: Vantagens de Poder, Campeão, Chi, Linguista, Resistência Arcana, Rico), então não foram listadas individualmente aqui.

- **Antecedente Arcano: Canalizar Cristal** — id: `aa_agente_syn` — [OK] — Concede acesso a Canalizar Cristal; grátis para todo Agente — linha ~1016 de swade_crystal_heart
- **Feroz** — id: `feroz` — [OK] — Aumenta o Dado Selvagem quando algo em que crê está em jogo — linha ~1025 de swade_crystal_heart
- **Mentor** — id: `mentor` — [OK] — +1 perícia relevante e +2 Requisição — linha ~1047 de swade_crystal_heart
- **Bogoviano - Determinado** — id: `bogoviano_determinado` — [OK] — Cultural de Bogovia; +2 após sair de Abalado — linha ~1084 de swade_crystal_heart
- **Fjordstadiano - Sondagem** — id: `fjordstadiano_sondagem` — [OK] — Cultural de Fjordstad; Noção de Perigo via Astúcia — linha ~1095 de swade_crystal_heart
- **Dieta da Ilha** — id: `dieta_da_ilha` — [OK] — Cultural das Ilhas; resistência a fome e refeições reconfortantes — linha ~1117 de swade_crystal_heart
- **Maseiano - Ritual de Passagem** — id: `maseiano_ritual` — [OK] — Cultural de Maseia; influencia a história sem gastar Bene 1x/sessão — linha ~1139 de swade_crystal_heart
- **Zingamaiano - Nacionalismo** — id: `zingamaiano_nacionalismo` — [OK] — Cultural de Zingama; conta como Obstinado e Corajoso — linha ~1153 de swade_crystal_heart
- **Focus** — id: `focus_ch` — [OK] — Ignora até 2 de penalidade em Canalizar Cristal parado — linha ~1167 de swade_crystal_heart
- **Focus Aprimorado** — id: `focus_aprimorado_ch` — [OK] — Rerrolagem em Canalizar Cristal — linha ~1174 de swade_crystal_heart
- **Conceder Poderes** — id: `conceder_poderes_ch` — [OK] — Transfere um poder do Cristal para um objeto usável por outros — linha ~1179 de swade_crystal_heart
- **Reter Poder** — id: `reter_poder_ch` — [OK] — Ignora penalidade de manter um poder — linha ~1203 de swade_crystal_heart
- **Especialista na Era Antiga** — id: `especialista_era_antiga` — [OK] — +2 Conhecimento Acadêmico/Engenharia com tech antiga; sabe Idioma Antigo — linha ~1209 de swade_crystal_heart
- **Cristaleiro** — id: `cristaleiro` — [OK] — +2 Conhecimento Acadêmico/Curar sobre Cristais; conhece temas/poderes catalogados — linha ~1217 de swade_crystal_heart
- **Especialista em um Cristal** — id: `especialista_cristal` — [OK] — Novo poder/Vantagem num Cristal específico; ignora 2 de penalidade nele — linha ~1232 de swade_crystal_heart
- **Especialista Aprimorado** — id: `especialista_aprimorado_cristal` — [OK] — Mais um poder no Cristal especializado — linha ~1254 de swade_crystal_heart
- **O Cristal Certo** — id: `o_cristal_certo` — [OK] — Permite carregar um segundo Cristal e trocar rapidamente — linha ~1260 de swade_crystal_heart
- **Invasor de Tumbas** — id: `invasor_tumbas` — [OK] — +1 Perceber/Ladinagem em ruínas; Conhecimento Geral sobre mitos — linha ~1287 de swade_crystal_heart
- **Assinatura** — id: `assinatura` — [OK] — Golpe/movimento assinatura com preparação+execução — linha ~1303 de swade_crystal_heart
- **Segundo Fôlego** — id: `segundo_folego_ch` — [OK] — Nível extra de Fadiga antes de Incapacitado — linha ~1345 de swade_crystal_heart
- **Aristocrata (uso alterado)** — id: `aristocrata_ch` — [OK] — Bônus pode cair para +1 entre Terras diferentes — linha ~978 de swade_crystal_heart
- **Arma Predileta (uso alterado)** — id: `arma_predileta_ch` — [OK] — Arma trazida de casa; sem rolagem de Requisição — linha ~987 de swade_crystal_heart
- **Comando (uso alterado)** — id: `comando_ch` — [OK] — Vantagens de Liderança para Extras também valem para PPs — linha ~995 de swade_crystal_heart
- **Conexões (uso alterado)** — id: `conexoes_ch` — [OK] — Não vale para membros da Syn (usa Requisição/Mentor) — linha ~1001 de swade_crystal_heart

**[CONFERIR]** Cinco Vantagens em `vantagens.json` com tag `CRYSTAL_HEART` não têm correspondência textual em nenhum dos dois arquivos-fonte: `sintonizacao_cristal` (SINTONIZAÇÃO COM CRISTAL), `troca_rapida` (TROCA RÁPIDA (Coração)), `resiliencia_cristalina` (RESILIÊNCIA CRISTALINA), `sobrecarga_segura` (SOBRECARGA SEGURA) e `arma_predileta_aprimorada` (ARMA PREDILETA APRIMORADA). Parecem extensões homebrew do app para o sistema de Coração — vale confirmar a origem antes de apresentá-las como conteúdo oficial do livro.

## Complicações novas ou modificadas (linha 856)

- **Forasteiro (uso alterado)** — id: `forasteiro_ch` — [OK] — Versão Menor para não-Ilhéus; Maior vetada por respeito legal da Syn — linha ~875 de swade_crystal_heart
- **Inimigo (uso alterado)** — id: `inimigo_ch` — [OK] — Normalmente um Agente rival ou pessoa da organização — linha ~888 de swade_crystal_heart
- **Lento (uso alterado)** — id: `lento_ch` — [OK] — Syn fornece prótese (Menor) ou cadeira ultraleve (Maior) — linha ~892 de swade_crystal_heart
- **Procurado (uso alterado)** — id: `procurado_ch` — [OK] — Maior só se autoridades caçam o Agente ativamente — linha ~904 de swade_crystal_heart
- **Um Braço Só (uso alterado)** — id: `um_braco_so_ch` — [OK] — Sempre Menor; -2 em tarefas de duas mãos — linha ~911 de swade_crystal_heart
- **Compulsivo (Maior)** — id: `compulsivo` — [OK] — Ao sacar Copas, deve gastar uma ação satisfazendo a disposição do Cristal — linha ~920 de swade_crystal_heart
- **Dependente (Menor)** — id: `dependente` — [OK] — Começa com 1 Requisição a menos; paga Requisição periodicamente — linha ~929 de swade_crystal_heart
- **Dessintonia (Maior)** — id: `dessintonia` — [OK] — Precisa re-rolar Canalizar Cristal a cada 10min para manter poder — linha ~938 de swade_crystal_heart
- **Disposição Persistente (Menor)** — id: `disposicao_persistente` — [OK] — Mantém uma Complicação Menor do Cristal anterior ao trocar — linha ~949 de swade_crystal_heart

**[CONFERIR]** Três Complicações em `complicacoes.json` com tag `CRYSTAL_HEART` não foram localizadas em nenhum dos dois textos-fonte: `comp_dessorna` ("Dessorna (Doença do Cristal)"), `comp_sensibilidade_obsidiana` ("Sensibilidade à Obsidiana") e `comp_inimigo_syn` ("Inimigo da Syn"). Parecem adições do app (a última é redundante com `inimigo_ch` acima).

## Perícias específicas (Passo 2, linha 610)

Lista fechada de 23 perícias (as demais "não existem em Crystal Heart", linha 615); todas presentes em `pericias.json` com tag `CRYSTAL_HEART`. A única perícia nova do cenário é **Canalizar Cristal**; as demais são o conjunto padrão do SWADE restrito e, em alguns casos, fundidas (Conhecimento Acadêmico absorve Ciência; Engenharia funde Eletrônica+Consertar).

- **Canalizar Cristal** — [OK] — Perícia arcana do Antecedente Arcano do cenário (Espírito); todo Agente começa em d4 — linha ~633 de swade_crystal_heart
- Atirar, Atletismo, Cavalgar, Conhecimento Acadêmico, Conhecimento Batalha, Conhecimento Geral, Curar, Dirigir, Engenharia, Furtividade, Idiomas, Intimidar, Ladinagem, Lutar, Navegar, Perceber, Performance, Persuadir, Pesquisar, Pilotar, Provocar, Sobrevivência — [OK] todas (22) — linhas ~616–826 de swade_crystal_heart

## Antecedente Arcano / sistema de poder próprio

Já detalhado em "Sistema de Coração" acima — o Antecedente Arcano do cenário é único (**Canalizar Cristal**, id `aa_agente_syn`) e não usa Pontos de Poder; os "poderes" ficam embutidos em cada registro de Coração (campo `poderes: List<String>` em `crystal_coracoes.json`), não num catálogo genérico separado. Isso explica por que `poderes.json` (usado por outros livros do app) não tem nenhuma entrada com tag `CRYSTAL_HEART` — **[CONFERIR]**: confirme que essa é uma decisão de design intencional (poderes como texto associado ao Coração) e não uma lacuna de modelagem.

## Equipamento temático (capítulo Equipamentos, linha 2758)

Catálogo bem coberto em `equipamentos.json` (21 grupos com tag `CRYSTAL_HEART`, incluindo réplicas retaggeadas dos grupos genéricos do SWADE básico para uso neste livro). Itens exclusivos do cenário conferidos: [OK] em todos os grupos abaixo.

- **Armamento Syn e Regional** — [OK] — Faca Syn, Espada Syn, Clava Charmosa Bogoviana, Bastão Bogoviano, Morsa das Ilhas, Corte Maseiano, Picada Zingamaiana, Dardo Zingamaiano, Chicote Zingamaiano, Apito das Ilhas, Arco Maseiano, Buzzer Fjordstadiano, Palmspring Fjordstadiano, Pistola Syn — linhas ~2882–2957 de swade_crystal_heart
- **Trajes da Agência e Proteção** — [OK] — Traje Syn, Traje Reforçado, Traje Ambiental, Armadura de Combate, Capacete Tático, Escudo Syn — linhas ~2846–2954 de swade_crystal_heart
- **Equipamento Syn Básico** — [OK] — Mochila Syn, Cobertor Térmico, Lanterna, Luvas de Manuseio, Recipiente de Cristal, Prótese/Cadeira de Rodas Syn — linha ~1364 de swade_crystal_heart (Passo 4)
- **Veículos Syn** — [OK] — O Tanque, Autoplano Padrão, Autoplanos (O Mamute), Barco Syn, Moto de Neve — linhas ~3315–3369 de swade_crystal_heart
- **Ferramentas Especializadas (Kits de perícia)** — [OK] — Kit Acadêmico, Química, Escalada, Disfarce, Mergulho, Primeiros Socorros, Fechaduras, Mecânico, Performance, Cavalgar, Navegação, Exploração, Esqui, Furtividade, Sobrevivência, Perícia de Ponta — linha ~3245 de swade_crystal_heart
- **Explosivos Regionais** — [OK] — Bomba de Fumaça/Ácido Zingamaiana, Dreck da Ilha, Estátua/Vaso Maseiano, Flashflash Fjordstadiano, Páprica Bogoviana — não lido em detalhe (fora do trecho revisado), status inferido da presença no JSON com nomes regionais coerentes

## Fora de escopo

Os seguintes trechos foram identificados mas **não** entram no índice de criação de personagem, conforme escopo pedido:

- **Arquétipos** (linha 1505 de swade_crystal_heart) — fichas de NPC pré-feitas por Terra (ex.: Habitante do Pântano, Acólito, Camponês, Mateiro, Engenheiro...), remetidas ao capítulo de Adversários — exemplos, não opções mecânicas novas.
- **Os Doze Agentes Heroicos** (linha 3419) — NPCs icônicos (Bach, Garridan, Ivan, Jordan, Kelly, Marc, Nui, Nyama, Tokpela, Tuhinga, Yurhant...) com estatísticas completas no capítulo de Adversários — bestiário/antagonistas.
- **Apêndice B: Supervisores e Mentores** (linha 18288) — lista de 10 NPCs mentores (Arx, Besouro, Calaway, Sra. Frodeliani, Iyani, Jamil, Hilda, Liha, Mira, Schultz) — lore de NPC, não opção de criação em si (mecânica coberta pela Vantagem Mentor).
- **Corações Humanos** (linha 6161) — lore não-mecânico sobre a fisiologia dos corações de carne substituídos.
- **Criando Novos Cristais** (linha ~5734–6057) — regras para o Mestre desenhar Cristais/Corações originais — excluído por instrução explícita (criação de item mágico/artefato; o catálogo comprável já está coberto acima).
- **Fragmentos** (linha 6059) — pedaços de Cristal encontrados em aventura, usáveis por qualquer um — conteúdo de loot/aventura, não escolha de criação.
- **Manifestações Selvagens** (swade_crystal_heart_muitos_coracoes, linha ~637) — sugestões de como cada Cristal aparece "selvagem" no mundo — conteúdo de aventura/GM.
- **Os Cristais / O Mundo / As Cinco Terras / A Era Antiga** (linhas 207–369) — lore ambiental sem mecânica de criação direta (a mecânica de atributo por Terra já está coberta em Ancestralidades).
- **Syn: Responsabilidades, Estações Syn, Hierarquia, Requisição (regra geral)** (linhas 2237–2757) — estrutura organizacional; a única parte com efeito direto na criação (pontos iniciais de Requisição) já foi citada em Sistema de Coração/Equipamento.
- **Aventuras, Contos Selvagens, Campanha: Mudança Sísmica, Adversários e Desafios, Manifestações dos Cristais (bestiário)** — conteúdo de mestre/bestiário, fora de escopo por instrução explícita.
