# Índice — Livro Básico (SWADE Core Rules)

`docs/swade_basico` (18.149 linhas) é o dump em texto puro do **Livro Básico do Savage Worlds Adventure Edition**, a base de todo o sistema usado pelo SWADE Builder. Estrutura macro do livro: Cap. 1 "Personagens" (conceito, ancestralidades, complicações, atributos, perícias, características derivadas, sumário de criação, sumário de vantagens — linhas ~354–5352); Cap. 2 "Equipamento" (armas, armaduras, veículos — ~5352–6693); Cap. 3 "Regras" (combate, dano, estados — ~6693–9014, fora de escopo); Cap. 4 "Ferramentas para Aventuras" (aliados, batalhas em massa, conflito social, medo, perigos, perseguições, riqueza, tarefas dramáticas e, ao final, Antecedentes Arcanos — ~9014–12457); Cap. 5 "Poderes" (os 5 Antecedentes Arcanos e a lista de 53 poderes — ~12457–15055); Cap. 6 "Bestiário" (~15055 em diante, fora de escopo). Cross-referência: **todo** o conteúdo de criação de personagem do livro básico (5 atributos, 32 perícias, 133 vantagens + 5 subtipos de Antecedente Arcano = 139, 57 complicações, 10 ancestralidades + 2 exemplos, 283 itens de equipamento em 32 grupos, 53 poderes) já está presente nos assets JSON do app, com boa fidelidade mecânica nos pontos amostrados. Nenhum item foi marcado **[FALTA]**.

## Atributos

Criação: 5 pontos para distribuir entre os 5 atributos (começam em d4), 1 ponto por passo de dado, limite d12 salvo bônus racial (pág. ~62, linha 4663).

- **Agilidade** — id sugerido: `agilidade` — [OK] — flexibilidade, destreza e coordenação — linha ~2050 de docs/swade_basico
- **Astúcia** — id sugerido: `astucia` — [OK] — intelecto bruto e velocidade de raciocínio; resiste a ataques mentais/sociais — linha ~2052 de docs/swade_basico
- **Espírito** — id sugerido: `espirito` — [OK] — autoconfiança e força de vontade; resiste a medo e ataques sobrenaturais — linha ~2056 de docs/swade_basico
- **Força** — id sugerido: `forca` — [OK] — poder físico; base do dano corpo a corpo e carga — linha ~2060 de docs/swade_basico
- **Vigor** — id sugerido: `vigor` — [OK] — resistência a fadiga/doença/veneno; base da Resistência — linha ~2065 de docs/swade_basico

`app/src/main/assets/geral_atributos.json` tem os 5 atributos com `min: 4` e descrições praticamente idênticas ao texto do livro. Todos [OK].

## Perícias

Criação: 12 pontos; 5 perícias básicas (Atletismo, Conhecimento Geral, Furtividade, Perceber, Persuadir) começam com d4 grátis; custo 1 ponto por dado até igualar o atributo vinculado, 2 pontos depois disso (linha ~4667). `pericias.json` tem 66 entradas totais (todas as 32 perícias do básico presentes, cada uma multiplicada por livro/variante); porém **nenhuma entrada de `pericias.json` tem campo `id`** (só `nome`), ao contrário dos demais assets — inconsistência de esquema a considerar, não um dado faltante.

- **Atirar** (Agilidade) — [OK] — combate à distância — linha ~2007
- **Atletismo** (Agilidade, básica) — [OK] — escalar, saltar, nadar, arremessar etc. — linha ~2014
- **Cavalgar** (Agilidade) — [OK] — montar e controlar montarias — linha ~2117
- **Ciência** (Astúcia) — [OK] — biologia, química, geologia etc. — linha ~2124
- **Ciência Estranha** (Astúcia) — [OK] — perícia arcana do Antecedente Arcano (Ciência Estranha) — linha ~2132
- **Conhecimento Acadêmico** (Astúcia) — [OK] — artes, história, literatura — linha ~2143
- **Conhecimento Batalha** (Astúcia) — [CONFERIR] — asset usa "Conhecimento de Batalha" (com "de"); nome do livro é "Conhecimento Batalha" — linha ~2151
- **Conhecimento Geral** (Astúcia, básica) — [OK] — linha ~2157
- **Conjurar** (Astúcia) — [OK] — perícia arcana do Antecedente Arcano (Magia) — linha ~2071
- **Consertar** (Astúcia) — [OK] — reparo mecânico/eletrônico — linha ~2076
- **Curar** (Astúcia) — [OK] — tratar ferimentos, doenças, ciência forense — linha ~2103
- **Dirigir** (Agilidade) — [OK] — veículos terrestres motorizados — linha ~2164
- **Eletrônica** (Astúcia) — [OK] — dispositivos complexos/especializados — linha ~2178
- **Fé** (Espírito) — [OK] — perícia arcana do Antecedente Arcano (Milagres) — linha ~2194
- **Foco** (Espírito) — [OK] — perícia arcana do Antecedente Arcano (Dom) — linha ~2198
- **Furtividade** (Agilidade, básica) — [OK] — esgueirar-se e esconder-se — linha ~2201
- **Hackear** (Astúcia) — [OK] — invasão de sistemas — linha ~2298
- **Idiomas** (Astúcia) — [OK] — fluência em idioma específico — linha ~2317
- **Intimidar** (Espírito) — [OK] — amedrontar oponentes — linha ~2428
- **Jogar** (Astúcia) — [OK] — jogos de azar — linha ~2477
- **Ladinagem** (Agilidade) — [OK] — arrombar, bater carteiras, armadilhas — linha ~2440
- **Lutar** (Agilidade) — [OK] — combate corpo a corpo — linha ~2594
- **Navegar** (Agilidade) — [OK] — pilotar embarcações — linha ~2600
- **Ocultismo** (Astúcia) — [OK] — conhecimento sobrenatural — linha ~2607
- **Perceber** (Astúcia, básica) — [OK] — percepção geral — linha ~2532
- **Performance** (Espírito) — [OK] — cantar, atuar, tocar instrumento — linha ~2548
- **Persuadir** (Espírito, básica) — [OK] — convencimento — linha ~2636
- **Pesquisar** (Astúcia) — [OK] — encontrar informação escrita — linha ~2732
- **Pilotar** (Agilidade) — [OK] — veículos 3D (aeronaves, naves) — linha ~2786
- **Provocar** (Astúcia) — [OK] — insultar/menosprezar (Desafio) — linha ~2795
- **Psiônicos** (Astúcia) — [OK] — perícia arcana do Antecedente Arcano (Psiônicos) — linha ~2814
- **Sobrevivência** (Astúcia) — [OK] — comida, água, abrigo, rastrear — linha ~2822

## Vantagens

139 vantagens do livro básico mapeadas (133 "normais" + Antecedente Arcano genérico + 5 subtipos), todas presentes em `vantagens.json` com `categoria`, `requisitos` e descrição. Nota de categorização: o JSON agrupa como `COMBATE` várias vantagens que o livro lista sob "Vantagens Estranhas" (Mãos Firmes, Nervos de Aço, Queixo de Ferro, Retirada, Rock and Roll!, Tiro Mortal, Varredura etc.) — não é um erro de dado, só uma taxonomia diferente da seção do livro; marcado [CONFERIR] apenas nesses casos.

### De Antecedente (24 + Antecedente Arcano)
- **Ambidestro** — `ambidestro` — [OK] — ignora -2 na mão inábil (N, Agi d8) — linha ~4833
- **Antecedente Arcano** — `antecedente_arcano` — [OK] — acesso aos 5 Antecedentes Arcanos do Cap. 5 — linha ~4835
- **Aristocrata** — `aristocrata` — [OK] — +2 Conhecimento Geral, rede de contatos com a elite — linha ~4838
- **Atraente** — `atraente` — [OK] — +1 Performance/Persuadir (N, Vig d6) — linha ~4839
- **Muito Atraente** — `muito_atraente` — [OK] — +2 Performance/Persuadir (requer Atraente) — linha ~4840
- **Brutamontes** — `brutamontes` — [OK] — Atletismo usa Força; melhora distância de arremesso — linha ~4842
- **Carismático** — `carismatico` — [OK] — rerrolagem grátis em Persuadir — linha ~4848
- **Corajoso** — `corajoso` — [OK] — +2 em testes de Medo, -2 na Tabela de Medo — linha ~4849
- **Cura Rápida** — `cura_rapida` — [OK] — +2 Vigor em cura natural — linha ~4850
- **Famoso** — `famoso` — [OK] — +1 Persuadir quando reconhecido; dobro de cachê — linha ~4852
- **Muito Famoso** — `muito_famoso` — [OK] — +2 Persuadir, 5x cachê (requer Famoso) — linha ~4856
- **Furioso** — `furioso` — [OK] — bônus de fúria ao ficar Abalado/Ferido — linha ~4858
- **Impulso** — `impulso` — [OK] — +2 ao rerrolar com Bene — linha ~4866
- **Ligeiro** — `ligeiro` — [OK] — Movimentação +2, corrida +1 tipo — linha ~4868
- **Linguista** — `linguista` — [OK] — d6 em idiomas = metade da Astúcia — linha ~4869
- **Musculoso** — `musculoso` — [OK] — Tamanho +1; Força maior p/ Sobrecarga e Força Mínima — linha ~4871
- **Prontidão** — `prontidao` — [OK] — +2 Perceber — linha ~4876
- **Rápido** — `rapido` — [OK] — descarta Cartas de Ação ≤5 — linha ~4877
- **Resistência Arcana** — `resistencia_arcana` — [OK] — -2 em perícias arcanas contra você; -2 dano mágico — linha ~4885
- **Resistência Arcana Aprimorada** — `resistencia_arcana_aprimorada` — [OK] — -4/-4 (requer Resistência Arcana) — linha ~4890
- **Rico** — `rico` — [OK] — 3x recursos iniciais, salário — linha ~4897
- **Podre de Rico** — `podre_de_rico` — [OK] — 5x recursos iniciais (requer Rico) — linha ~4899
- **Sorte** — `sorte` — [OK] — +1 Bene por sessão — linha ~4901
- **Sorte Grande** — `sorte_grande` — [OK] — +2 Benes (requer Sorte) — linha ~4902

### De Combate (32)
- **Arma Predileta** — `arma_predileta` — [OK] — +1 ataque/aparar com arma específica — linha ~4904
- **Arma Predileta Aprimorada** — `arma_predileta_aprimorada` — [OK] — bônus sobe para +2 — linha ~4912
- **Artista Marcial** — `artista_marcial` — [OK] — +1 Lutar Desarmado, dano d4 — linha ~4917
- **Guerreiro Marcial** — `guerreiro_marcial` — [OK] — +2 Lutar Desarmado, dado maior (requer Artista Marcial) — linha ~4922
- **Atacar Primeiro** — `atacar_primeiro` — [OK] — Ataque Livre quando oponente entra no alcance — linha ~4926
- **Atacar Primeiro Aprimorado** — `atacar_primeiro_aprimorado` — [OK] — até 3 oponentes — linha ~4929
- **Atirador** — `atirador` — [OK] — ignora 2 pts de penalidade de distância/cobertura — linha ~4936
- **Atirar com Duas Armas** — `atirar_com_duas_armas` — [OK] — rolagem extra com 2ª arma à distância — linha ~4943
- **Bloquear** — `bloquear` — [OK] — +1 Aparar, ignora 1 pt de Agrupar — linha ~4949
- **Bloquear Aprimorado** — `bloquear_aprimorado` — [OK] — +2 Aparar — linha ~4950
- **Brigão** — `brigao` — [OK] — Resistência +1, dano desarmado d4 — linha ~4952
- **Pugilista** — `pugilista` — [OK] — dado de dano/Resistência sobem (requer Brigão) — linha ~4961
- **Calculista** — `calculista` — [OK] — ignora até 2 pts penalidade em Cartas ≤5 — linha ~4963
- **Contra-Ataque** — `contra_ataque` — [OK] — Ataque Livre contra Lutar falho — linha ~4965
- **Contra-Ataque Aprimorado** — `contra_ataque_aprimorado` — [OK] — até 3 por rodada — linha ~4967
- **Corredor** — `corredor` — [OK] — ignora terreno acidentado, +2 Atletismo — linha ~4974
- **Disparo Rápido** — `disparo_rapido` — [OK] — CdT +1 com Atirar — linha ~4976
- **Disparo Rápido Aprimorado** — `disparo_rapido_aprimorado` — [OK] — CdT +1 em até 2 ataques — linha ~4979
- **Disparo Duplo** — `disparo_duplo` — [OK] — +1 acerto/dano ao disparar — linha ~4986
- **Duro de Matar** — `duro_de_matar` — [OK] — ignora penalidades por ferimento ao resistir a Sangrando — linha ~4989
- **Muito Duro de Matar** — `muito_duro_de_matar` — [OK] — rola dado extra ao ficar Incapacitado — linha ~4991
- **Esquiva** — `esquiva` — [OK] — -2 para acertar você à distância — linha ~4998
- **Esquiva Aprimorada** — `esquiva_aprimorada` — [OK] — +2 Evasão em ataques de área — linha ~4999
- **Finta** — `finta` — [OK] — Desafio de Lutar pode usar Astúcia do alvo — linha ~5001
- **Focado** — `focado` — [OK] — saca Carta de Ação extra em combate — linha ~5005
- **Extremamente Focado** — `extremamente_focado` — [OK] — 2 cartas extras (requer Focado) — linha ~5007
- **Frenesi** — `frenesi` — [OK] — 2º dado de Lutar em ataque corpo a corpo — linha ~5010
- **Frenesi Aprimorado** — `frenesi_aprimorado` — [OK] — 3º dado — linha ~5012
- **Golpe Poderoso** — `golpe_poderoso` — [OK] — dobra dano ao sacar Curinga — linha ~5015
- **Impiedoso** — `impiedoso` — [OK] — +2 dano ao rerrolar dano com Bene — linha ~5021
- **Instinto Assassino** — `instinto_assassino` — [OK] — rerrolagem grátis em Desafio iniciado por você — linha ~5022
- **Lutador Improvisador** — `lutador_improvisador` — [OK] — ignora -2 de armas improvisadas — linha ~5025
- **Lutar com Duas Armas** — `lutar_com_duas_armas` — [OK] — rolagem extra com 2ª arma corpo a corpo — linha ~5028

### Estranhas (livro) — mapeadas em `vantagens.json` sob a categoria `COMBATE` (12) e `ESTRANHAS` (8)
- **Campeão** — `campeao` — [OK] — +2 dano vs. sobrenaturais malignos — linha ~5035
- **Chi** — `chi` — [OK] — rerrola ataque, força rerrolagem inimiga ou +d6 desarmado (requer Guerreiro Marcial) — linha ~5037
- **Coragem Líquida** — `coragem_liquida` — [OK] — álcool aumenta Vigor, penaliza Agi/Ast — linha ~5042
- **Curandeiro** — `curandeiro` — [OK] — +2 em Curar — linha ~5047
- **Elo Animal** — `elo_animal` — [OK] — gasta Benes por animais controlados — linha ~5048
- **Noção do Perigo** — `nocao_do_perigo` — [OK] — +2 Perceber p/ emboscadas — linha ~5049
- **Senhor das Feras** — `senhor_das_feras` — [OK] — companhia animal — linha ~5052
- **Sucateiro** — `sucateiro` — [OK] — encontra item necessário 1x/encontro (requer Sorte) — linha ~5055
- **Mãos Firmes** — `maos_firmes` — [CONFERIR-categoria] — ignora penalidade de plataforma instável — linha ~5059
- **Matador de Gigantes** — `matador_de_gigantes` — [CONFERIR-categoria] — +1d6 dano vs. criaturas 3+ Tamanhos maiores — linha ~5061
- **Nervos de Aço** — `nervos_de_aco` — [CONFERIR-categoria] — ignora 1 nível de penalidade por ferimento — linha ~5064
- **Nervos de Aço Aprimorados** — `nervos_de_aco_aprimorados` — [CONFERIR-categoria] — ignora 2 níveis — linha ~5065
- **Queixo de Ferro** — `queixo_de_ferro` — [CONFERIR-categoria] — +2 Vigor/Absorção vs. nocaute — linha ~5069
- **Reflexos de Combate** — `reflexos_de_combate` — [CONFERIR-categoria] — +2 Espírito para recuperar de Abalado/Atordoado — linha ~5072
- **Retirada** — `retirada` — [CONFERIR-categoria] — sem ataque livre ao se retirar — linha ~5075
- **Retirada Aprimorada** — `retirada_aprimorada` — [CONFERIR-categoria] — até 3 oponentes — linha ~5077
- **Rock and Roll!** — `rock_and_roll` — [CONFERIR-categoria] — ignora penalidade de Recuo — linha ~5080
- **Tiro Mortal** — `tiro_mortal` — [CONFERIR-categoria] — dobra dano ao sacar Curinga — linha ~5082
- **Varredura** — `varredura` — [CONFERIR-categoria] — ataque em área com arma de 2 mãos — linha ~5089
- **Varredura Aprimorada** — `varredura_aprimorada` — [CONFERIR-categoria] — evita aliados — linha ~5095

### De Liderança (8)
- **Comando** — `comando` — [OK] — +1 recuperação de Extras no Raio de Comando — linha ~5100
- **Presença de Comando** — `presenca_de_comando` — [OK] — Raio de Comando 10 quadros (requer Comando) — linha ~5102
- **Estrategista** — `estrategista` — [OK] — Carta de Ação extra p/ aliados — linha ~5105
- **Mestre Estrategista** — `mestre_estrategista` — [OK] — 2 cartas extras (requer Estrategista) — linha ~5112
- **Fervor** — `fervor` — [OK] — +1 dano de Lutar para Extras no Raio — linha ~5115
- **Inspirar** — `inspirar` — [OK] — Conhec. Batalha como Suporte no Raio — linha ~5119
- **Líder Nato** — `lider_nato` — [OK] — Vantagens de Liderança aplicam-se a Cartas Selvagens — linha ~5123
- **Mantenham a Formação!** — `mantenham_a_formacao` — [OK] — +1 Resistência de Extras no Raio — linha ~5127

### De Poder (14)
- **Artífice** — `artifice` — [OK] — cria Dispositivos Arcanos — linha ~5133
- **Canalização** — `canalizacao` — [OK] — -1 custo de PP com ampliação — linha ~5134
- **Concentração** — `concentracao` — [OK] — dobra Duração de poderes não instantâneos — linha ~5136
- **Drenar a Alma** — `drenar_a_alma` — [OK] — recupera 5 PP por Fadiga — linha ~5137
- **Engenhoqueiro** — `engenhoqueiro` — [OK] — 3 PP para replicar outro poder (Ciência Estranha) — linha ~5139
- **Esforço Extra** — `esforco_extra` — [OK] — aumenta Foco pagando PP (Dom) — linha ~5146
- **Guerreiro Sagrado/Profano** — `guerreiro_sagrado_profano` — [OK] — bônus de Absorção por PP (Milagres) — linha ~5150
- **Mago** — `mago` — [OK] — 1 PP extra p/ mudar Manifestação (Magia) — linha ~5158
- **Mentalista** — `mentalista` — [OK] — +2 em resistidas de Psiônicos — linha ~5164
- **Novos Poderes** — `novos_poderes` — [OK] — aprende 2 novos poderes — linha ~5169
- **Pontos de Poder** — `pontos_de_poder` — [OK] — +5 PP (1x por Estágio) — linha ~5226
- **Recarga Rápida** — `recarga_rapida` — [OK] — recupera 10 PP/hora — linha ~5229
- **Recarga Rápida Aprimorada** — `recarga_rapida_aprimorada` — [OK] — recupera 20 PP/hora — linha ~5231
- **Surto de Poder** — `surto_de_poder` — [OK] — recupera 10 PP ao sacar Curinga — linha ~5236

### Profissionais (12)
- **Acrobata** — `acrobata` — [OK] — rerrolagem grátis em Atletismo — linha ~5174
- **Combatente Acrobata** — `combatente_acrobata` — [OK] — -1 para ser acertado (requer Acrobata) — linha ~5176
- **Ás** — `as` — [OK] — Benes para Absorver dano em veículo — linha ~5179
- **Assassino** — `assassino` — [OK] — +2 dano vs. Vulneráveis / habilita Finalização — linha ~5181
- **Erudito** — `erudito` — [OK] — +2 em perícias de "conhecimento" — linha ~5187
- **Investigador** — `investigador` — [OK] — +2 Pesquisar e certas rolagens de Perceber — linha ~5189
- **Ladrão** — `ladrao` — [OK] — +1 Ladinagem/Atletismo(escalar)/Furtividade urbana — linha ~5193
- **Mateiro** — `mateiro` — [OK] — +2 Sobrevivência/Furtividade em áreas selvagens — linha ~5199
- **McGyver** — `mcgyver` — [OK] — dispositivos improvisados a partir de sucata — linha ~5204
- **Pau Pra Toda Obra** — `pau_pra_toda_obra` — [OK] — d4/d6 temporário em uma perícia — linha ~5210
- **Senhor Conserta Tudo** — `senhor_conserta_tudo` — [OK] — +2 Consertar — linha ~5213
- **Soldado** — `soldado` — [OK] — Força maior p/ Sobrecarga; rerrola Vigor vs. perigos — linha ~5219

### Sociais (14)
- **Agitador** — `agitador` — [OK] — Desafio de Intimidar/Provocar em área — linha ~5248
- **Ameaçador** — `ameacador` — [OK] — +2 Intimidar — linha ~5252
- **Cativar o Ambiente** — `cativar_o_ambiente` — [OK] — 2º dado de Suporte (Performance/Persuadir) — linha ~5253
- **Cativar a Multidão** — `cativar_a_multidao` — [OK] — 3º dado (requer Cativar o Ambiente) — linha ~5258
- **Conexões** — `conexoes` — [OK] — contatos ajudam 1x/sessão — linha ~5264
- **Confiável** — `confiavel` — [OK] — rerrolagem grátis em Suporte — linha ~5265
- **Elevar o Moral** — `elevar_o_moral` — [OK] — remove Distraído/Vulnerável após Desafio — linha ~5266
- **Elo Comum** — `elo_comum` — [OK] — cede Benes livremente — linha ~5268
- **Humilhar** — `humilhar` — [OK] — rerrolagem grátis em Provocar — linha ~5269
- **Manha** — `manha` — [OK] — +2 Conhecimento Geral e contatos com criminalidade — linha ~5270
- **Obstinado** — `obstinado` — [OK] — +2 para resistir a Desafios de Astúcia/Espírito — linha ~5272
- **Vontade de Ferro** — `vontade_de_ferro` — [OK] — bônus aplica-se a poderes também — linha ~5273
- **Provocador** — `provocador` — [OK] — ampliação em Provocar "provoca" rivais — linha ~5279
- **Réplica** — `replica` — [OK] — ampliação ao resistir Provocar/Intimidar deixa alvo Distraído — linha ~5281

### Lendárias (9, requerem Estágio Lendário)
- **Duro na Queda** — `duro_na_queda` — [OK] — aguenta 4 Ferimentos — linha ~5285
- **Muito Duro na Queda** — `muito_duro_na_queda` — [OK] — aguenta 5 (requer Duro na Queda) — linha ~5288
- **Mestre de Arma** — `mestre_de_arma` — [OK] — Aparar +1, dano Lutar d8 — linha ~5294
- **Mestre das Armas** — `mestre_das_armas` — [OK] — Aparar +1, dano Lutar d10 — linha ~5297
- **Parceiro** — `parceiro` — [OK] — Carta Selvagem aliada — linha ~5303
- **Profissional** — `profissional` — [OK] — Característica e limite sobem 1 tipo — linha ~5304
- **Especialista** — `especialista` — [OK] — mais 1 tipo (requer Profissional) — linha ~5306
- **Mestre** — `mestre` — [OK] — Dado Selvagem d10 na Característica — linha ~5308
- **Seguidores** — `seguidores` — [OK] — 5 seguidores — linha ~5312

## Antecedente Arcano

Explicação do sistema em linha ~12418: cada tipo tem Perícia Arcana, Poderes Iniciais e Pontos de Poder. Confere em `geral_arcano_info.json` (`key`, `slots`, `pp`, `foco`) — todos os 5 batem exatamente com o texto.

- **Antecedente Arcano (Dom)** — id: `antecedente_arcano_dom` / `geral_arcano_info` key `DOM` — [OK] — Perícia Foco (Espírito), 1 poder inicial, 15 PP — linha ~12449
- **Antecedente Arcano (Milagres)** — id: `antecedente_arcano_milagres` / key `MILAGRES` — [OK] — Perícia Fé (Espírito), 3 poderes, 10 PP — linha ~12460
- **Antecedente Arcano (Psiônicos)** — id: `antecedente_arcano_psionicos` / key `PSIONICOS` — [OK] — Perícia Psiônicos (Astúcia), 3 poderes, 10 PP — linha ~12478
- **Antecedente Arcano (Ciência Estranha)** — id: `antecedente_arcano_ciencia_estranha` / key `CIENCIA ESTRANHA` — [OK] — Perícia Ciência Estranha (Astúcia), 2 poderes, 15 PP — linha ~12491
- **Antecedente Arcano (Magia)** — id: `antecedente_arcano_magia` / key `MAGIA` — [OK] — Perícia Conjurar (Astúcia), 3 poderes, 10 PP — linha ~12507

Regras adjacentes mapeadas mas não tabeladas linha a linha (mecânicas gerais, não itens de catálogo): Versatilidade/re-flavor de poderes (~12519), Múltiplos Antecedentes Arcanos (~12574), Manifestações e Ativação (~12623–12822), Modificadores de Poderes (~12787), Dispositivos Arcanos e sua criação (~12983–13029) — regra de item mágico/arcano, propositalmente **fora do escopo** desta tarefa.

## Complicações

57 complicações do livro básico, todas presentes em `complicacoes.json` com `severity` correspondente ("menor", "maior" ou "menor ou maior").

- **Almofadinha** — `almofadinha` — [OK] — Menor — -2 Intimidar — linha ~4690
- **Analfabeto** — `analfabeto` — [OK] — Menor — não lê/escreve — linha ~4691
- **Anêmico** — `anemico` — [OK] — Menor — -2 Vigor vs. Fadiga — linha ~4692
- **Arrogante** — `arrogante` — [OK] — Maior — desafia a maior ameaça — linha ~4693
- **Atrapalhado** — `atrapalhado` — [OK] — Maior — -2 Atletismo/Furtividade — linha ~4695
- **Boca Grande** — `boca_grande` — [OK] — Menor — não guarda segredo — linha ~4696
- **Cauteloso** — `cauteloso` — [OK] — Menor — planeja demais — linha ~4698
- **Cego** — `cego` — [OK] — Maior — -6 em ações visuais, Vantagem grátis — linha ~4699
- **Código de Honra** — `codigo_de_honra` — [OK] — Maior — age com nobreza — linha ~4701
- **Covarde** — `covarde` — [OK] — Maior — -2 Medo/resistir Intimidar — linha ~4702
- **Curioso** — `curioso` — [OK] — Maior — quer saber tudo — linha ~4703
- **Deficiente Auditivo** — `deficiente_auditivo` — [OK] — Menor/Maior — -4 Perceber sons — linha ~4704
- **Delirante** — `delirante` — [OK] — Menor/Maior — crença estranha — linha ~4706
- **Desagradável** — `desagradavel` — [OK] — Menor — -1 Persuadir — linha ~4708
- **Desastrado** — `desastrado` — [OK] — Menor — -2 dispositivos elétricos/mecânicos — linha ~4709
- **Desconfiado** — `desconfiado` — [OK] — Menor/Maior — aliados -2 ao dar Suporte — linha ~4710
- **Desejo de Morrer** — `desejo_de_morrer` — [OK] — Menor — quer morrer após tarefa épica — linha ~4712
- **Excesso de Confiança** — `excesso_de_confianca` — [OK] — Maior — acha que pode tudo — linha ~4713
- **Feio** — `feio` — [OK] — Menor/Maior — -1/-2 Persuadir — linha ~4714
- **Fobia** — `fobia` — [OK] — Menor/Maior — -1/-2 na presença do medo — linha ~4715
- **Forasteiro** — `forasteiro` — [OK] — Menor/Maior — -2 Persuadir, sem direitos legais — linha ~4717
- **Ganancioso** — `ganancioso` — [OK] — Menor/Maior — obsessão por riqueza — linha ~4720
- **Guiado** — `guiado` — [OK] — Menor/Maior — ações guiadas por crença — linha ~4721
- **Hábito** — `habito` — [OK] — Menor/Maior — vício, Fadiga por privação — linha ~4722
- **Heroico** — `heroico` — [OK] — Maior — sempre ajuda necessitados — linha ~4723
- **Hesitante** — `hesitante` — [OK] — Menor — saca 2 cartas de ação, fica com a menor — linha ~4724
- **Idoso** — `idoso` — [OK] — Maior — -1 Mov/Corrida/Agi/For/Vig, +5 pts perícia — linha ~4726
- **Impulsivo** — `impulsivo` — [OK] — Maior — age sem pensar — linha ~4728
- **Incrédulo** — `incredulo` — [OK] — Menor — não acredita no sobrenatural — linha ~4731
- **Inimigo** — `inimigo` — [OK] — Menor/Maior — nêmesis recorrente — linha ~4733
- **Invejoso** — `invejoso` — [OK] — Menor/Maior — cobiça o alheio — linha ~4734
- **Jovem** — `jovem` — [OK] — Menor/Maior — menos pontos de atributo/perícia, Benes extras — linha ~4735
- **Leal** — `leal` — [OK] — Menor — sempre leal a amigos — linha ~4737
- **Lento** — `lento` — [OK] — Menor/Maior — Mov -1/-2, sem Vantagem Ligeiro — linha ~4738
- **Língua Presa** — `lingua_presa` — [OK] — Maior — -1 Intimidar/Persuadir/Provocar — linha ~4741
- **Má Sorte** — `ma_sorte` — [OK] — Maior — 1 Bene a menos por sessão — linha ~4743
- **Mudo** — `mudo` — [OK] — Maior — não fala — linha ~4744
- **Não Sabe Nadar** — `nao_sabe_nadar` — [OK] — Menor — -2 nadar, custo triplo de Movimentação — linha ~4745
- **Obeso** — `obeso` — [OK] — Menor — Tamanho +1, Mov -1, corrida d4 — linha ~4747
- **Obrigação** — `obrigacao` — [OK] — Menor/Maior — 20h/40h semanais — linha ~4749
- **Pacifista** — `pacifista` — [OK] — Menor/Maior — só luta em legítima defesa / não luta — linha ~4750
- **Peculiaridade** — `peculiaridade` — [OK] — Menor — fraqueza menor persistente — linha ~4752
- **Pequeno** — `pequeno` — [OK] — Menor — Tamanho/Resistência -1 — linha ~4754
- **Pobreza** — `pobreza` — [OK] — Menor — metade dos recursos iniciais — linha ~4756
- **Procurado** — `procurado` — [OK] — Menor/Maior — procurado pelas autoridades — linha ~4757
- **Sanguinário** — `sanguinario` — [OK] — Maior — nunca faz prisioneiros — linha ~4758
- **Segredo** — `segredo` — [OK] — Menor/Maior — segredo obscuro — linha ~4759
- **Sem Escrúpulos** — `sem_escrupulos` — [OK] — Menor/Maior — faz o que for preciso — linha ~4760
- **Sem-Noção** — `sem_nocao` — [OK] — Maior — -1 Conhecimento Geral/Perceber — linha ~4761
- **Sensível** — `sensivel` — [OK] — Menor/Maior — -2/-4 resistir Provocar — linha ~4762
- **Teimoso** — `teimoso` — [OK] — Menor — raramente admite erros — linha ~4764
- **Um Braço Só** — `um_braco_so` — [OK] — Maior — -4 em tarefas de 2 mãos — linha ~4765
- **Um Olho Só** — `um_olho_so` — [OK] — Maior — -2 a 5+ quadros de distância — linha ~4766
- **Vergonha** — `vergonha` — [OK] — Menor/Maior — culpa de evento trágico — linha ~4767
- **Vingativo** — `vingativo` — [OK] — Menor/Maior — busca vingança — linha ~4768
- **Visão Ruim** — `visao_ruim` — [OK] — Menor/Maior — -1/-2 rolagens dependentes de visão — linha ~4770
- **Voto** — `voto` — [OK] — Menor/Maior — devoção a uma causa — linha ~4773

## Ancestralidades

As 10 ancestralidades jogáveis do livro básico, todas em `ancestralidades.json` com `livros: ["BASICO"]`, atributos/habilidades raciais batendo com o texto (ex.: Anões = Movimentação Reduzida + Resistente + Visão no Escuro; Humanos = Adaptável apenas). Também há a seção de exemplo "Celestiais/Guardiões" (ancestralidades customizadas construídas com a regra de Criação de Ancestralidades) já presente no asset como bônus.

- **Androides** — `ANDROIDES` — [OK] — Construto, Forasteiro, Pacifista, Voto — linha ~646
- **Anões** — `ANÕES` — [OK] — Movimentação Reduzida, Resistente, Visão no Escuro — linha ~685
- **Avianos** — `AVIANOS` — [OK] — Frágil, Movimentação Reduzida, Não Sabe Nadar, Sentidos Aguçados, Voo — linha ~731
- **Aquarianos** — `AQUARIANOS` — [OK] — Aquático, Dependência, Resistência, Visão no Escuro — linha ~757
- **Elfos** — `ELFOS` — [OK] — Ágil, Desastrado, Visão no Escuro — linha ~781
- **Meio-Elfos** — `MEIO-ELFOS` — [OK] — Forasteiro, Herança, Visão no Escuro — linha ~805
- **Humanos** — `HUMANOS` — [OK] — Adaptável (Vantagem Novato grátis) — linha ~830
- **Rakashanos** — `RAKASHANOS` — [OK] — Ágil, Inimigo Racial, Mordida/Garras, Não Sabe Nadar, Sanguinário, Visão no Escuro — linha ~868
- **Pequeninos** — `PEQUENINOS` — [OK] — Espirituoso, Movimentação Reduzida, Sorte, Tamanho -1 — linha ~888
- **Sáurios** — `SÁURIOS` — [OK] — Armadura +2, Forasteiro, Fraqueza Ambiental, Mordida, Sentidos Aguçados, Prontidão — linha ~966
- **Celestiais** (exemplo de ancestralidade customizada) — `CELESTIAIS` — [OK] — Desastrado, Antecedente Arcano (Milagres), Fé d6, Voo, Inimigo Racial (Demônios), Voto — linha ~1211
- **Guardiões** (exemplo de ancestralidade customizada) — `GUARDIÕES` — [OK] — Adaptável, Campeão, Vigoroso (Vig d6), Voto — linha ~1222

Regra de "Criando Ancestralidades" (ferramenta de Mestre para montar novas raças com pontos de habilidade racial, ~linha 976–1191) não foi tabelada entrada a entrada — ver seção "Fora de escopo" abaixo; os pontos/habilidades raciais individuais parecem estar cobertos por `basico_habilidades_raciais.json` (não auditado em detalhe nesta passada).

## Equipamento

Catálogo comprável do Cap. 2 mapeado em `equipamentos.json`: 32 grupos (`tipo`/`subtipo`) com `livros: ["BASICO"]`, somando **283 itens**. Conferência por amostragem (ex.: tabela "Armas à Distância — Medievais", linha ~5916) bateu exatamente em nome, dano, alcance, PA, Força Mínima, peso e custo (ex.: Arco Longo 15/30/60, 2d6, PA 1, For. Mín. d8, 1,5 kg, $300). Marcado [OK] em bloco; não foi feita conferência item a item das ~283 linhas por eficiência de tempo/contexto — ver nota final.

| Grupo do livro (linha aprox.) | Grupo no asset | Itens | Status |
|---|---|---|---|
| Animais e Arreios (~5561) | Equipamento Geral / Animais e Arreios | 4 | [OK] |
| Acessórios para Armas de Fogo (~5629) | Equipamento Geral / Acessórios para armas de fogo | 3 | [OK] |
| Equipamento de Aventura (~5566) | Equipamento Geral / Equipamento de Aventura | 32 | [OK] |
| Vestuário (~5616) | Equipamento Geral / Vestuário | 6 | [OK] |
| Computador e Eletrônicos / Vigilância (~5624, ~5638) | Eletrônicos / Computadores e Vigilância | 12 | [OK] |
| Comida (~5667) | Consumíveis / Comida | 4 | [OK] |
| Defesa Pessoal (~5675) | Armas Pessoais / Defesa Pessoal | 2 | [OK] |
| Munição (~5687) | Munição / Geral | 11 | [OK] |
| Armaduras Antigas e Medievais — tecido/couro (~5740) | Armaduras / Tecido e Couro | 7 | [OK] |
| Cota de Malha, Malha de Placas etc. (~5740–5761) | Armaduras / Medievais de Metal | 14 | [OK] |
| Armaduras Modernas/Futuristas (~5786–5833) | Armaduras / Modernas e Futuristas | 12 | [OK] |
| Escudos (~5839) | Escudos / Geral | 8 | [OK] |
| Armas Corpo a Corpo — Medievais (~5877) | Armas Corpo a Corpo / Medievais | 20 | [OK] |
| Armas Corpo a Corpo — Modernas/Futuristas (~5906) | Armas Corpo a Corpo / Modernas e Futuristas | 10 | [OK] |
| Armas à Distância — Medievais (arcos, bestas etc.) (~5913) | Armas à Distância / Medievais | 9 | [OK] |
| Armas à Distância — Modernas (arco composto) (~5932) | Armas à Distância / Modernas (Arcos) | 2 | [OK] |
| Armas de Pólvora Negra (~5953) | Armas de Fogo / Pólvora Negra | 5 | [OK] |
| Pistolas Modernas / Revólveres / Semi-Auto (~5966–5986) | Armas de Fogo / Pistolas Modernas | 8 | [OK] |
| Submetralhadoras (~5964) | Armas de Fogo / Submetralhadoras | 3 | [OK] |
| Rifles (ação-alavanca, ferrolho, assalto) (~5993–6019) | Armas de Fogo / Rifles | 9 | [OK] |
| Escopetas (~6030) | Armas de Fogo / Escopetas | 4 | [OK] |
| Metralhadoras (~6057) | Armas de Fogo / Metralhadoras | 7 | [OK] |
| Armas de Energia (Sci-Fi, sem seção própria no básico — futuristas) | Armas de Energia / Lasers (Sci-Fi) | 4 | [CONFERIR] |
| Canhões e Catapultas (~6109–6120) | Armas Especiais / Canhões e Catapultas | 3 | [OK] |
| Lança-Chamas, Minas, Granadas (~6132–6198) | Armas Especiais / Explosivos e Lança-Chamas | 10 | [OK] |
| Lança-Foguetes, Mísseis (~6215–6242) | Armas Pesadas / Lança-Foguetes e Mísseis | 9 | [OK] |
| Armas Veiculares (~6291) | Armas Veiculares / Canhões e Metralhadoras Montadas | 18 | [OK] |
| Veículos Terrestres Civis (~6340) | Veículos / Terrestres Civis | 11 | [OK] |
| Veículos Militares WWII (~6512) | Veículos / Militares (WWII) | 6 | [OK] |
| Veículos Militares Modernos/Futuristas (~6538–6558) | Veículos / Militares Modernos e Sci-Fi | 7 | [OK] |
| Aeronaves (~6580–6609) | Veículos / Aeronaves | 15 | [OK] |
| Embarcações (~6633) | Veículos / Embarcações | 8 | [OK] |

[CONFERIR] em "Armas de Energia / Lasers" apenas porque o livro básico não tem uma seção dedicada a laser com esse nome exato — vale conferir se esses 4 itens vieram de um adendo futurista do próprio básico (armas modernas/futuristas aparecem espalhadas nas tabelas de rifles/pistolas com a tag "futuristas") ou se foram herdados de outro sourcebook e só rotulados como BASICO por engano.

## Poderes

53 poderes do livro básico, todos em `poderes.json` (campos `estagio`, `pontosDePoder`, `distancia`, `duracao`, `manifestacoes`, `modificadores`). Conferência pela tabela "Sumário de Poderes" (linha ~14873–14998), que já lista Rank/PP/Distância/Duração/Resumo de cada um; valores batem com a amostra conferida (ex.: Adivinhação = Experiente, 5 PP).

- **Adivinhação** — `adivinhacao` — [OK] — E, 5 PP, Pessoal, 5 min — perguntas a entidades — linha ~14874
- **Ajuda** — `ajuda` — [OK] — N, 1 PP, Ast, Inst. — remove Fadiga/Estados negativos — linha ~14875
- **Amigo das Feras** — `amigo_das_feras` — [OK] — N, Esp. (variável), Ast, 10 min — controla animais — linha ~14877
- **Andar nas Paredes** — `andar_nas_paredes` — [OK] — N, 2 PP, Ast, 5 — anda em paredes — linha ~14879
- **Atordoar** — `atordoar` — [OK] — N, 2 PP, Ast, Inst. — alvo fica Atordoado — linha ~14884
- **Aumentar/Reduzir Característica** — `aumentar_reduzir_caracteristica` — [OK] — N, 3 PP, Ast, 5/Inst. — sobe/desce perícia ou atributo — linha ~14885
- **Banir** — `banir` — [OK] — V, 3 PP, Ast, Inst. — resistida por Espírito, bane entidades — linha ~14890
- **Barreira** — `barreira` — [OK] — E, 2 PP, Ast, 5 — parede Dureza 10 — linha ~14892
- **Campo de Dano** — `campo_de_dano` — [OK] — E, 4 PP, Ast, 5 — aura 2d4 de dano — linha ~14896
- **Cavar** — `cavar` — [OK] — N, 2 PP, Ast, 5 — cria túneis — linha ~14898
- **Cegar** — `cegar` — [OK] — N, 2 PP, Ast, Inst. — -2/-4 penalidade — linha ~14899
- **Confusão** — `confusao` — [OK] — N, 1 PP, Ast, Especial — Distraído e Vulnerável — linha ~14901
- **Conjurar Aliado** — `conjurar_aliado` — [OK] — N, Esp. (variável), Ast, 5 — conjura criatura aliada — linha ~14902
- **Crescimento/Encolhimento** — `crescimento_encolhimento` — [OK] — E, Esp. (variável), Ast, 5 — muda Tamanho — linha ~14904
- **Cura** — `cura` — [OK] — N, 2 PP, Toque, Inst. — remove Ferimentos recentes — linha ~14906
- **Dádiva do Guerreiro** — `dadiva_do_guerreiro` — [OK] — E, 4 PP, Ast, 5 — concede Vantagem de Combate — linha ~14908
- **Deflexão** — `deflexao` — [OK] — N, 3 PP, Ast, 5 — -2 em ataques contra o alvo — linha ~14910
- **Detectar/Ocultar Arcano** — `detectar_ocultar_arcano` — [OK] — N, 2 PP, Ast, Especial — detecta/oculta magia — linha ~14912
- **Devastação** — `devastacao` — [OK] — N, 2 PP, Ast, Inst. — área Distraída/arremessada — linha ~14915
- **Disfarce** — `disfarce` — [OK] — E, 2 PP, Ast, 10 min — alvo parece outra pessoa — linha ~14923
- **Dissipar** — `dissipar` — [OK] — N, 1 PP, Ast, Inst. — anula efeitos mágicos — linha ~14924
- **Drenar Pontos de Poder** — `drenar_pontos_de_poder` — [OK] — V, 2 PP, Ast, Inst. — drena d6 PP do alvo — linha ~14925
- **Elo Mental** — `elo_mental` — [OK] — N, 1 PP, Ast, 30 min — comunicação telepática a 1,5 km — linha ~14930
- **Empatia** — `empatia` — [OK] — N, 1 PP, Ast, 5 — +1/+2 em perícias sociais — linha ~14932
- **Enredar** — `enredar` — [OK] — N, 2 PP, Ast, Inst. — prende/Enreda — linha ~14936
- **Explosão** — `explosao` — [OK] — E, 3 PP, Ast x2, Inst. — 2d6 dano em área — linha ~14937
- **Falar Idioma** — `falar_idioma` — [OK] — N, 1 PP, Ast, 10 min — fala/entende idiomas — linha ~14939
- **Fantoche** — `fantoche` — [OK] — V, 3 PP, Ast, 5 — controla o alvo (resistida por Espírito) — linha ~14940
- **Ferir** — `ferir` — [OK] — N, 2 PP, Ast, 5 — +2/+4 dano de arma — linha ~14942
- **Iluminar/Obscurecer** — `iluminar_obscurecer` — [OK] — N, 2 PP, Ast, 10 min — cria/dissipa iluminação — linha ~14943
- **Ilusão** — `ilusao` — [OK] — N, 3 PP, Ast, 5 — imagens imaginárias — linha ~14945
- **Intangibilidade** — `intangibilidade` — [OK] — V, 5 PP, Ast, 5 — alvo incorpóreo — linha ~14946
- **Invisibilidade** — `invisibilidade` — [OK] — E, 5 PP, Ast, 5 — -4/-6 para ser afetado — linha ~14947
- **Leitura de Objeto** — `leitura_de_objeto` — [OK] — E, 2 PP, Toque, Especial — revela história do objeto — linha ~14948
- **Leitura Mental** — `leitura_mental` — [OK] — N, 2 PP, Ast, Inst. — lê a mente (resistida por Astúcia) — linha ~14953
- **Limpeza Mental** — `limpeza_mental` — [OK] — V, 3 PP, Ast, Inst. — remove/altera memórias — linha ~14955
- **Manipulação Elemental** — `manipulacao_elemental` — [OK] — N, 1 PP, Ast, 5 — manipula elementos básicos — linha ~14957
- **Medo** — `medo` — [OK] — N, 2 PP, Ast, Inst. — teste de Medo — linha ~14960
- **Morosidade/Velocidade** — `morosidade_velocidade` — [OK] — E, 2 PP, Ast, Inst./5 — altera Movimentação — linha ~14961
- **Mudança de Forma** — `mudanca_de_forma` — [OK] — N, Esp. (variável), Pessoal, 5 — assume outras formas — linha ~14963
- **Proteção** — `protecao` — [OK] — N, 1 PP, Ast, 5 — Armadura +2 — linha ~14966
- **Proteção Ambiental** — `protecao_ambiental` — [OK] — N, 2 PP, Ast, 1 hora — -2/-4 vs. ambientes hostis — linha ~14967
- **Proteção Arcana** — `protecao_arcana` — [OK] — N, 1 PP, Ast, 5 — reduz ataques/dano de conjuradores oponentes — linha ~14970
- **Raio** — `raio` — [OK] — N, 1 PP, Ast x2, Inst. — 2d6 à distância — linha ~14980
- **Rajada** — `rajada` — [OK] — N, 2 PP, Cone, Inst. — 2d6 em cone — linha ~14981
- **Ressurreição** — `ressurreicao` — [OK] — H, 30 PP, Toque, Inst. — traz mortos de volta — linha ~14983
- **Som/Silêncio** — `som_silencio` — [OK] — N, 1 PP, Ast x5/Ast, Inst./5 — cria/abafa som — linha ~14984
- **Sono** — `sono` — [OK] — E, 2 PP, Ast, 1 hora — adormece vítimas — linha ~14986
- **Telecinese** — `telecinese` — [OK] — E, 5 PP, Ast x2, 5 — move itens (Força d10/d12) — linha ~14987
- **Teleporte** — `teleporte` — [OK] — E, 2 PP, Ast, Inst. — teleporta até 12 quadros — linha ~14989
- **Visão Distante** — `visao_distante` — [OK] — E, 2 PP, Ast, 5 — vê a grandes distâncias — linha ~14991
- **Visão Sombria** — `visao_sombria` — [OK] — N, 1 PP, Ast, 1 hora — ignora penalidade de iluminação — linha ~14995
- **Voar** — `voar` — [OK] — V, 3 PP, Ast, 5 — Movimentação 12 voando — linha ~14997
- **Zumbi** — `zumbi` — [OK] — V, 3 PP, Ast, 1 hora — reergue e controla mortos-vivos — linha ~14998

## Criação de Personagem (regras)

Sumário completo em linha ~4649–4687 (bloco "Sumário da Criação de Personagem"), consistente com os detalhes espalhados no Cap. 1:

1. **Conceito** (~4650) — ideia geral da personagem.
2. **Ancestralidade** (~4652) — escolher e aplicar bônus/habilidades.
3. **Complicações** (~4655) — até 4 pontos (Maior=2, Menor=1); 2 pts = +1 atributo ou 1 Vantagem; 1 pt = +1 ponto de perícia ou 2x recursos iniciais.
4. **Atributos** (~4662) — começam d4, 5 pontos, 1 ponto/passo, limite d12 (salvo bônus racial).
5. **Perícias** (~4667) — 5 básicas grátis em d4; 12 pontos; 1 ponto/dado até igualar atributo, 2 pontos depois.
6. **Características Derivadas** (~4673) — Movimentação 6; Aparar = 2 + metade de Lutar; Resistência = 2 + metade de Vigor + Armadura.
7. **Vantagens** (~4681) — pontos de Complicação sobrando, 2 pts por Vantagem.
8. **Equipamento** (~4685) — até $500.

Regra de Progresso/Estágio (Novato 0–3, Experiente 4–7, Veterano 8–11, Heroico 12–15, Lendário 16+) descrita em ~4552–4648, relevante para gates de Vantagens Lendárias/Heroicas etc.

**Conferência de fórmula**: `DerivedAttributesCalculator.kt` (linha 30) calcula Aparar como `2 + (maxFightingRaw / 2)`, batendo com a regra do livro (2 + metade de Lutar). Nenhum hardcode divergente encontrado nessa amostra.

## Fora de escopo (não mapeado)

- **Cap. 3 — Regras de Combate** (linhas ~6693–9014): ações, ataques, dano, estados (Abalado, Ferido, Atordoado etc.), perseguições, batalhas em massa, conflito social, tabuleiros. Só mencionado onde citado por uma Vantagem/Perícia específica.
- **Cap. 4 — trechos de mestre**: Aliados como PNJs (~9014–9331 fora dos pontos de Progresso), Batalhas em Massa (~9181–9331), Conflito Social como minigame (~9331–9538), Medo/Perigos ambientais como tabelas de mestre (~9778–10379), Perseguições e Veículos em combate (~10379–11169), Tarefas Dramáticas, Riqueza/Negociação como regras de cena (~11962–12232), Viagem (~12287–12418) — todas regras de condução de cena/mestre, não de build de personagem.
- **Regras de Ambientação opcionais** (~11318–12417): Convicção, Herói Nunca Morre, Especialização de Perícias, Múltiplos Idiomas, Sem Pontos de Poder, Pura Sorte etc. — variantes de mesa que o Mestre liga/desliga, fora do escopo de "criação de personagem core".
- **Dispositivos Arcanos / criação de itens mágicos** (~12983–13029): sistema de criação de artefatos com Antecedente Arcano — explicitamente excluído pela tarefa.
- **Cap. 6 — Bestiário** (linhas ~15055–18149 até o fim): habilidades especiais de criaturas, tabela de Tamanho/Escala para monstros, construção de ameaças — bestiário de antagonistas, fora de escopo.
- **Criando Ancestralidades** (~976–1191): ferramenta de Mestre para montar novas raças (pontos de habilidade racial positivos/negativos); os 10 exemplos jogáveis do livro já foram mapeados acima. Os pontos individuais de habilidade racial (Ágil, Lento, Grande etc.) provavelmente correspondem a `basico_habilidades_raciais.json`, não auditado linha a linha nesta passada.
