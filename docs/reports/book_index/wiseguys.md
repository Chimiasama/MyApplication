# Índice de Criação de Personagem — Wiseguys

Levantamento do conteúdo de `docs/swade_wiseguys_jogador` relevante para a **criação de personagem** no cenário noir/máfia Wiseguys, cruzado com os dados já cadastrados em `app/src/main/assets/*.json` sob a tag de livro `WISEGUYS`. O cenário é puramente humano e sem magia: não há novas ancestralidades além de Humano e não há Antecedente Arcano (o próprio livro exclui explicitmente Antecedente Arcano, Resistência Arcana, Aristocrata, Campeão, Chi, Matador de Gigantes e todas as Vantagens de Poder — linha ~3855-3862). A varredura em `vantagens.json`, `complicacoes.json`, `pericias.json`, `ancestralidades.json` e `equipamentos.json` mostra que praticamente todo o conteúdo mecânico do livro já está cadastrado com a tag `WISEGUYS`; a única lacuna encontrada é uma modificação de veículo (vidros peliculados) mencionada em texto corrido, sem entrada própria de item.

## Ancestralidades

- **Humano (única ancestralidade jogável)** — id: `HUMANOS` (genérico, não específico de Wiseguys) — [OK] — Wiseguys usa apenas Humano como ancestralidade padrão; todo personagem recebe uma Vantagem grátis conforme a regra padrão de Savage Worlds — linha ~3706 de docs/swade_wiseguys_jogador
- Nota: o livro permite ao Mestre liberar outras ancestralidades apenas se ele estiver usando Wiseguys como "kit de ferramentas" para outra campanha — isso é orientação de mesa, não uma ancestralidade nova a cadastrar (linha ~3712-3716).

## Antecedente Arcano

- **Não existe conteúdo arcano em Wiseguys** — [OK/CONFIRMADO] — o livro proíbe explicitamente Antecedente Arcano, Resistência Arcana, Aristocrata, Campeão, Chi, Matador de Gigantes e todas as Vantagens de Poder; Assassino de Sangue-Frio substitui a Vantagem Corajoso — linha ~3855-3862 de docs/swade_wiseguys_jogador. Perícias de Antecedente Arcano também são explicitamente excluídas da lista de perícias disponíveis (linha ~3809-3811). Nenhuma entrada em `poderes.json` está marcada com a tag `WISEGUYS`, confirmando a ausência de poderes no cenário.

## Complicações

Todas as 15 Complicações novas do Capítulo 3 já estão cadastradas em `complicacoes.json` com a tag `WISEGUYS`.

- **Arauto da Morte (Menor)** — id: `comp_arauto_morte` — [OK] — Extras aliados incapacitados perto dela sofrem -2 em testes de Vigor para sobreviver — linha ~4015
- **Brincalhão (Menor)** — id: `comp_brincalhao` — [OK] — piadas involuntárias dão -2 quando um colega tenta Intimidar/Provocar perto dele — linha ~4028
- **Chamativo (Menor)** — id: `chamativo` — [OK] — maneirismos marcantes dão -2 em Furtividade para se misturar e facilitam reconhecimento por testemunhas — linha ~4044
- **Cidadão Íntegro (Maior)** — id: `comp_cidadao_integro` — [OK] — -2 em qualquer teste de característica ao cometer um crime — linha ~4066
- **Cugine (Menor)** — id: `comp_cugine` — [OK] — membro mais novo/recente do grupo, encarregado das tarefas menos desejáveis — linha ~4078
- **Dívida (Menor/Maior)** — id: `comp_divida` — [OK] — alguém cobra favores/chantageia o personagem indefinidamente; ao ser "quitada" deve ser substituída por outra Complicação — linha ~4085
- **Dedo Leve (Menor)** — id: `comp_dedo_leve` — [OK] — -1 em Persuadir (exceto sob ameaça de violência); em 1 natural ao intimidar com arma em punho, ela dispara sozinha — linha ~4110
- **Em Condicional (Maior)** — id: `comp_condicional` — [OK] — compra carta de Ação a cada sessão; Curinga significa violação da condicional e prisão iminente — linha ~4121
- **Fardo (Menor/Maior)** — id: `comp_fardo` — [OK] — atrai a ira de superiores; carta com face em cada sessão reduz privilégios/aumenta taxas, Curinga pode significar execução — linha ~4140
- **Má Reputação (Menor)** — id: `comp_ma_reputacao` — [OK] — -2 em Persuadir contra não-criminosos e em usos da Rede de Contatos — linha ~4175
- **Na Lista Negra (Menor)** — id: `comp_lista_negra` — [OK] — banido de cassinos/estabelecimentos de jogo, é escoltado para fora se identificado — linha ~4184
- **O Suspeito (Maior)** — id: `comp_suspeito` — [OK] — alvo constante de vigilância e interrogatório policial, envolvido ou não no crime — linha ~4193
- **Pé-Frio (Menor)** — id: `comp_pe_frio` — [OK] — ele e quem está perto não podem gastar Bennies em rolagens de Jogar — linha ~4207
- **Peso na Consciência (Menor/Maior)** — id: `comp_peso_consciencia` — [OK] — remorso após crimes; na versão Maior, o Mestre ganha um Bene para usar contra ela após um ato hediondo — linha ~4219
- **Relação Disfuncional (Menor/Maior)** — id: `comp_relacao_disfuncional` — [OK] — parente/amigo problemático prejudica a reputação do personagem — linha ~4237

Nota: **Qualidades Redentoras** (linha ~3975-4013) não é uma Complicação nova — é orientação de criação de personagem que sugere reaproveitar Complicações já existentes (Heroico, Código de Honra, Leal, Desejo de Morrer, Peso na Consciência, Segredo, Pacifista) como "traço redentor"; não requer entrada própria.

## Perícias

- **Lei (Astúcia)** — id/nome: `Lei` — [OK] — usada para saber o que pode ser feito sem consequência jurídica, proteger interesses legais e defender alguém em tribunal — linha ~4285 de docs/swade_wiseguys_jogador

Nota: "Novos usos para as Perícias" (Jogar contra a casa, linha ~4294-4327) é uma regra de mesa/mecânica de jogo (não uma perícia nova nem afeta criação de personagem diretamente) — ver Fora de escopo.

## Vantagens

Todas as 33 Vantagens novas (Capítulo 3) já estão cadastradas em `vantagens.json` com a tag `WISEGUYS`.

### Vantagens de Antecedente
- **Assassino Impiedoso** — id: `assassino_impiedoso` — [OK] — Novato, Espírito d8+, Sem Escrúpulos (Maior); imune à variante Náusea de Medo e resistente a polígrafo — linha ~4330
- **Bom Companheiro** — id: `bom_companheiro` — [OK] — Novato, homem, herança italiana; concede Manha, benefícios de membro oficial da Cosa Nostra, mas impõe Voto (Maior — Omertà) — linha ~4343
- **Nascido nas Ruas** — id: `nascido_ruas` — [OK] — Novato; ignora até 2 pontos de penalidade de segurança ao cometer crimes — linha ~4388
- **Notório** — id: `notorio` — [OK] — Carta Selvagem, Novato; rerrolagem grátis em Intimidação contra o submundo e dobra ganhos de trambiques — linha ~4397

### Vantagens de Combate
- **Artista Gun-Fu** — id: `artista_gunfu` — [OK] — Experiente, Atirar d8+, Lutar d8+, Artista Marcial; atira com precisão em combate corpo a corpo — linha ~4435
- **Beijo da Morte** — id: `beijo_morte` — [OK] — Novato, Atraente; Imunidade a Veneno e aplica Veneno Leve por toque/surpresa — linha ~4446
- **Beijo da Morte Aprimorado** — id: `beijo_morte_aprimorado` — [OK] — Experiente, Beijo da Morte; acesso a veneno Nocauteador/Letal/Paralisante, acumulável — linha ~4463
- **Guarda-Costas** — id: `guarda_costas` — [OK] — Novato, Perceber d8+; estende Defender/Cobertura Média a um aliado adjacente — linha ~4472
- **Fanfarrão** — id: `fanfarrao` — [OK] — Experiente, Astúcia d8+, Consertar d6+, Ladinagem d8+; cria distrações com Modelo Pequeno de Explosão — linha ~4480
- **Lutadora de Patins** — id: `lutadora_patins` — [OK] — Novato, Atletismo d6+, Lutar d6+; luta usando patins como arma não desarmável — linha ~4504
- **Manobrar e Atirar** — id: `manobrar_atirar` — [OK] — Novato, Atirar d6+, Dirigir d6+; reduz em 2 a penalidade de Ações Múltiplas ao atirar dirigindo — linha ~4517
- **Sequestrador** — id: `sequestrador` — [OK] — Novato, Intimidar d8+; regras de refém como escudo/proteção — linha ~4526
- **Telecatch** — id: `telecatch` — [OK] — Experiente, Lutar d8+, Provocar d6+, Artista Marcial; benefícios extras em Desafios de luta livre — linha ~4553

### Vantagens de Liderança
- **Líder de Time** — id: `lider_time` — [OK] — Experiente, Espírito d8+, Intimidar d6+, Comando; compartilha Benes com Aliados mesmo sem contato — linha ~4587
- **Rebaixar** — id: `rebaixar` — [OK] — Veterano, Intimidar d8+, Comando; amplia alcance de Vantagens de Liderança a Extras na mesma cidade — linha ~4600

### Vantagens Profissionais
- **Cozinheiro** — id: `cozinheiro` — [OK] — Novato, Ciência d8+; produz drogas/álcool ilegal com laboratório e auxiliar, dobra o lucro de trambiques — linha ~4612
- **Especialista em Explosivos** — id: `especialista_explosivos` — [OK] — Novato, Consertar d8+, Ciência d6+; +2 Consertar/+1 Perceber para explosivos e modificações (potência, perfuração, área, ocultação) — linha ~4626
- **Falsificador** — id: `falsificador` — [OK] — Novato, Astúcia d8+, Ladinagem d8+; rerrolagem grátis de Ladinagem para falsificar itens — linha ~4669
- **Limpador** — id: `limpador` — [OK] — Novato, Curar d8+, Perceber d6+; +2 e metade do tempo para limpar cena de crime, usa Curar para achar pistas — linha ~4679
- **Mestre da Fuga** — id: `mestre_fuga` — [OK] — Novato, Atletismo d6+, Ladinagem d8+; +2 Atletismo para escapar/passar por aberturas, +2 Ladinagem para se soltar de algemas — linha ~4693
- **Mestre do Disfarce** — id: `mestre_disfarce` — [OK] — Novato, Furtividade d6+, Performance d6+, Persuadir d6+; rerrolagem grátis de Performance ao usar disfarce — linha ~4716
- **Motorista Agressivo** — id: `motorista_agressivo` — [OK] — Novato, Dirigir d8+; Forçar/Abalroar a uma carta de distância — linha ~4730
- **Motorista de Fuga** — id: `motorista_fuga` — [OK] — Novato, Astúcia d6+, Dirigir d8+; foge de perseguição com uma carta a menos de distância — linha ~4740
- **Trambiqueiro** — id: `trambiqueiro` — [OK] — Novato, Manha; +1 em três esquemas de Conhecimento Geral escolhidos — linha ~4751
- **Trapaceiro** — id: `trapaceiro` — [OK] — Novato, Astúcia d8+, Jogar d8+; +4 (em vez de +2) em Jogar ao trapacear, rerrola Falha Crítica com Bene, usa Jogar em vez de Ladinagem para truques de mão — linha ~4759

### Vantagens Sociais
- **Acima da Lei** — id: `acima_da_lei` — [OK] — Heroico; não pode ser detido/acusado exceto em flagrante (ou custódia federal) — linha ~4776
- **Capanga** — id: `capanga` — [OK] — Carta Selvagem, Experiente; mantém Seguidor leal por estágio, pode virar Resiliente/Tenente — linha ~4786
- **Insistente** — id: `insistente` — [OK] — Experiente, Espírito d8+, Persuadir d8+; +1 Contato adicional por sessão — linha ~4860
- **Persistente** — id: `persistente` — [OK] — Veterano, Insistente; +2 Contatos adicionais por sessão — linha ~4871
- **Amigo Meu (Um Amigo Meu)** — id: `um_amigo_meu` — [OK] — Experiente, Manha, Persuadir d8+; 1x/sessão pede Favor sem gastar Bene, e o conhecido vira Contato permanente — linha ~4878

### Vantagens Estranhas
- **Dama da Sorte** — id: `dama_sorte` — [OK] — Carta Selvagem, Novato; Bene extra só para Jogar, compartilhável, +1 Persuadir com Rede de Contatos via joia da sorte — linha ~4902

### Vantagens Lendárias
- **Em Outro Patamar** — id: `em_outro_patamar` — [OK] — Carta Selvagem, Lendário; Dado Selvagem vira d8 (ou d12 com Mestre) em testes não-combate da especialidade — linha ~4928
- **Intocável** — id: `intocavel` — [OK] — Lendário, Acima da Lei; imunidade prática a prisão/processo, mesmo federal — linha ~4954

## Equipamento

### Armas corpo a corpo novas
- **Garrote (corda de piano)** — nome: `Garrote` — [OK] — For+d4 quando agarrado, requer Surpresa+Agarrar, +2 na resistida de Força para sufocar — linha ~5006
- **Facão** — nome: `Facão` — [OK] — For+d6, PA 1, $25 — linha ~5020
- **Nunchaku** — nome: `Nunchaku` — [OK] — For+d4, requer Agilidade d8+, ignora escudos, $50 — linha ~5025
- **Sai** — nome: `Sai` — [OK] — For+d4, Aparar +1, +1 para Desarmar, tipicamente em pares, $20 — linha ~5030
- **Shuriken** — nome: `Shuriken` — [OK] — For+1, alcance 3/6/12, $5 — linha ~5035

### Armas especiais novas
- **Coquetel Molotov** — nome: `Coquetel Molotov` — [OK] — 2d10, MPE, pode incendiar alvos inflamáveis, $10 — linha ~5065
- **Explosivos Improvisados** — nome: `Explosivos Improvisados` — [OK] — 4d6, MME, Arma Pesada, $50 — linha ~5073
- **Explosivos Militares** — nome: `Explosivos Militares` — [OK] — 4d6, PA 4, MGE, Arma Pesada, +1d6 por carga, $1.500 — linha ~5080

### Acessórios de trapaça
- **Dados Adulterados** — nome: `Dados Adulterados` — [OK] — $20, favorece o resultado em jogos de dados — linha ~5144
- **Moedas Iô-Iô** — nome: `Moedas Iô-Iô` — [OK] — $0,25, trapaça em caça-níqueis via linha de pesca — linha ~5154
- **Descanso** — nome: `Descanso` — [OK] — $25, trava a alavanca de pagamento de caça-níqueis — linha ~5160
- **Dispositivo Óptico de Luz** — nome: `Dispositivo Óptico de Luz` — [OK] — $200, cega o sensor da máquina para pagar prêmio máximo — linha ~5166
- **Patas de Macaco** — nome: `Patas de Macaco` — [OK] — $50, manipula a alavanca interna do caça-níqueis — linha ~5173

(Todos os 5 concedem +2 em Ladinagem ou Jogar dependendo do uso; Falha Crítica expõe o trapaceiro — linha ~5139-5143.)

### Equipamento mundano (Ferramentas da Profissão)
- **Máscara Balaclava** — [OK] — esconde identidade, $10 — linha ~5213
- **Maçarico** — [OK] — aplica chama/calor, $8 — linha ~5219
- **Maleta** — [OK] — aparência profissional/carrega papelada, $100 — linha ~5223
- **Saco de Estopa** — [OK] — cobre a cabeça de um refém, $2 — linha ~5228
- **Laboratório Químico** — [OK] — instalação fixa para fabricar químicos, $10.000 — linha ~5232
- **Kit de Química** — [OK] — versão portátil, coleta/analisa evidências, $200 — linha ~5237
- **Aparador de Charutos** — [OK] — símbolo de status, $10 — linha ~5243
- **Kit de Limpeza** — [OK] — remove evidências de cena de crime, $100 — linha ~5247
- **Pente** — [OK] — item de vaidade, $1 — linha ~5253
- **Equipamento de Contra-vigilância** — [OK] — detecta dispositivos eletrônicos num Modelo Médio, $1.500 — linha ~5258
- **Kit de Disfarce** — [OK] — perucas, próteses, maquiagem, $150 — linha ~5267
- **Mochila** — [OK] — carrega ferramentas/armas, $40 — linha ~5271
- **Chapéu** — [OK] — disfarce leve/vaidade, $80 — linha ~5277
- **Clipe de Dinheiro** — [OK] — substitui carteira, $5 — linha ~5280
- **Estojo de Instrumento Musical** — [OK] — esconde arma (-2 Perceber), $50 — linha ~5284
- **Envelope de Papel** — [OK] — transporte de suborno/contribuições — linha ~5291
- **Baralho de Cartas** — [OK] — jogo improvisado/trapaça, $2 — linha ~5335
- **Scanner de Polícia** — [OK] — monitora frequência policial, $50 — linha ~5342
- **Polígrafo** — [OK] — +2 Perceber mentiras, $2.000 — linha ~5347
- **Tapete** — [OK] — esconde corpo enrolado, $100 — linha ~5354
- **Kit de Arrombamento de Cofres** — [OK] — brocas, C-4, detonadores, scanner, $2.500 — linha ~5359
- **Supressor** — [OK] — -2 Perceber som de disparo, específico por arma, $100 — linha ~5363
- **Equipamento de Vigilância** — [OK] — câmeras/microfones/gravadores, $2.000 — linha ~5370
- **Soro da Verdade** — [OK] — veneno leve, teste de Vigor ou Distraído/Fatigado (Exausto em Falha Crítica), $200 — linha ~5404
- **Saco de Papel Marrom Amassado** — [OK] — esconde dinheiro/arma/bebida — linha ~5412
- **Isqueiro Zippo** — [OK] — estiloso, $20 — linha ~5416

## Veículos

- **Caminhão Blindado** — nome: `Caminhão Blindado` — [OK] — Grande, armadura pesada, $50-100K+ — linha ~5429
- **Limusine** — nome: `Limusine` — [OK] — Grande, artigo de luxo, $60-200K+ — linha ~5431
- **Carro Luxuoso** — nome: `Carro luxuoso` — [OK] — Grande, artigo de luxo (Cadillac Eldorado, Lincoln Continental), $40-80K+ — linha ~5433
- **Caminhão Monstro** — nome: `Caminhão Monstro` — [OK] — Grande, 4x4, $200K+ — linha ~5438
- **Muscle Car** — nome: `Muscle car` — [OK] — Grande (Pontiac GTO, Plymouth GTX, Dodge Challenger), $20-80K+ — linha ~5439
- **Picape** — nome: `Picape` — [OK] — Grande, 4x4, $20-48K+ — linha ~5441
- **Carro de Polícia** — nome: `Carro de Polícia` — [OK] — Grande, $20-40K+ — linha ~5443
- **Patins** — nome: `Patins` — [OK] — dobra Movimentação/dado de corrida do patinador, destruídos em acerto crítico, $100 — linha ~5444
- **Carro Recreativo** — nome: `Carro Recreativo` — [OK] — Grande, veículo de lazer para família, $100K+ — linha ~5454
- **Van** — nome: `Van` — [OK] — Grande, entrega/vigilância, $10-40K+ — linha ~5457
- **Vidros Peliculados (modificação)** — id sugerido: `mod_vidros_peliculados` — [FALTA] — trata o interior do veículo como Penumbra/Escuridão/Escuro como Breu (níveis I-III) para visibilidade — linha ~5463

## Poderes

- **Nenhum poder novo** — [OK/CONFIRMADO] — Wiseguys não usa nenhuma Vantagem de Poder do Savage Worlds nem introduz poderes próprios; `poderes.json` não tem nenhuma entrada com a tag `WISEGUYS` — linha ~3855-3856 de docs/swade_wiseguys_jogador

## Fora de escopo

- **Lore de cenário não-mecânico**: Introdução, Estilo de Vida do Mafioso, Tropos e Temas, história da Máfia Ítalo-Americana, Las Vegas (linhas ~618-2251) — contexto narrativo puro, sem impacto em ficha.
- **Regras de ambientação como recurso de grupo/organização**: Traição, Consenso, Favores, Nossos Amigos/Suborno, A Cosa Nostra, O Serviço do Safári, Assaltos e Gerador de Assalto (linhas ~2254-3074) — mecânicas de campanha/equipe, não Vantagens/Complicações individuais.
- **Bestiário**: inexistente no cenário (confirmado — não há capítulo de adversários/criaturas no sumário nem no texto).
- **Arquétipos de personagem** (Acrobata, Agiota, Andarilho, Apostador Profissional, Arrombador de Cofres, Artista, Caipira, Desperado, Dublê, Ex-Proprietário de Cassino, Executor, Fabricante de Bombas, Falsificador, Gatuno, Grande Apostador, Ladrão, Limpador, Matador, Matriarca, Médico de Rua, Patinadora Roller Derby, Protetor, Taxista, Trambiqueiro, Trapaceiro, Veterano de Guerra) — linhas ~3274-3651: são sugestões de conceito/inspiração narrativa, não entidades mecânicas próprias a cadastrar.
- **Gerador de Antecedente aleatório** (nascimento, infância, ficha criminal por naipe/carta) — linhas ~3744-3792: tabela de geração de história, não afeta atributos/perícias diretamente.
- **Interlúdios Expandidos** (opção "Fonte de renda" por naipe) — linhas ~3913-3961: variação de tabela de Interlúdio, mecânica de jogo contínuo, não de criação inicial.
- **"Jogar contra a Casa" e regra de Trambiques via Conhecimento Geral** — linhas ~4294-4327 e ~4258-4281: regras de resolução de mesa, não itens de ficha.
- **"Eu Preciso de um Advogado"** (contratar advogado via regra de Riqueza) — linhas ~5384-5403: serviço/regra de compra, não vantagem/equipamento cadastrável.
- **Armas improvisadas (lista de exemplos genéricos)** — linhas ~5086-5106: exemplos de flavor que usam as regras padrão de arma improvisada do Savage Worlds, sem estatísticas próprias a cadastrar.
- **Glossário e Índice remissivo** — linhas ~5467 em diante: apoio de leitura, sem conteúdo mecânico.
