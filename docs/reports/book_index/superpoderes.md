# Índice de Criação de Personagem — Compêndio de Superpoderes

Fonte: `docs/swade_superpoderes` (~15732 linhas, edição brasileira RetroPunk). Este livro descreve um sistema **paralelo e independente** ao de Antecedente Arcano: super-heróis e supervilões não escolhem Magia, Milagres, Psiônicos etc. — em vez disso, compram a Vantagem de Antecedente **Superpoderes** (p. 14), que concede "Pontos de Superpoder" (PSP) definidos pelo Nível de Poder da campanha (I a V). Esses pontos compram **Super Poderes** listados no Capítulo Quatro (92 poderes distintos, cada um com custo base — fixo, por nível, ou com tabela própria — e uma lista de Modificadores/Efeitos específicos que alteram o custo). O livro é explícito: "Pontos de Superpoder não são os mesmos pontos usados em Antecedentes Arcanos". Além do sistema de poderes, o livro traz Complicações e Vantagens novas típicas do gênero (identidade secreta, fraquezas, parceiros, etc.) e um capítulo de Equipamento temático (armas de pulso, armaduras, veículos de superequipe). **Não existe** no livro um sistema formal de "Origem" com categorias fixas (Nascido, Alienígena, Mutante, Artefato, Treinado, Experimento...) — isso é tratado de forma puramente narrativa (ver seção "Ancestralidades/Origem" abaixo).

Verificação feita contra `app/src/main/assets/super_poderes.json`, `vantagens.json`, `complicacoes.json`, `equipamentos.json` e `geral_arcano_info.json` (tag de livro `SUPER`). **Resultado geral: cobertura praticamente completa** — os 92 Super Poderes, as 6 Vantagens novas e as 15 Complicações novas identificadas no texto já estão todos cadastrados. As únicas lacunas reais são os **Modificadores Universais de Poder** (regra transversal, não amarrada a um poder específico) e a mecânica de **Conjunto de Poderes** (formas alternativas), que não existem como dado estruturado em nenhum arquivo.

## Sistema de Origem

O Capítulo 1 ("Criando Personagens", a partir da linha 434) não define categorias fechadas de origem. O texto diz textualmente: "Supers, por padrão, são humanos, e por isso têm a habilidade racial Adaptável [...] Porém, ignore a opção Superpoderes listada nas Habilidades Raciais Positivas. Em vez disso, use as regras listadas aqui" (linha ~468-472). Ou seja:

- Qualquer raça jogável do Savage Worlds Edição Aventura (ou de outro Compêndio aprovado pelo Mestre) pode ser usada como base do super-herói; não há uma lista fechada de "origens" tipo Mutante/Alienígena/Artefato.
- A "origem" da personagem (nasceu com poderes, foi mutado, é alienígena, usa um artefato, foi treinado etc.) é só justificativa narrativa (Manifestação) escolhida livremente para cada Super Poder comprado — cada poder no Capítulo 4 já lista sugestões de "Manifestações" (ex.: Absorção sugere "Forma amorfa, controle de energia ou matéria, desmaterialização, magia").
- Origens específicas ficam a cargo de Complicações (**Forma Alienígena**, **Transformação**) e do modificador de poder **Dispositivo** (para heróis "Artefato"/gadget), não de um template de raça dedicado.
- [OK, confirmado em 2026-08-31] — o app não implementa (nem deveria implementar) categorias fechadas de "Origem do super-herói" (Nascido/Alienígena/Mutante/Artefato/Treinado/Experimento) como feature própria, e isso está correto: confirmado que o livro realmente não define esse sistema, tratando origem como escolha puramente narrativa de Manifestação. Nenhuma ação necessária — não é conteúdo faltante.

## Sistema de Pontos de Poder

- **Vantagem Superpoderes** (Antecedente, Novato) — destrava o sistema. Concede PSP conforme o Nível de Poder da campanha (escolha do Mestre) e deve ser gratuita na maioria das campanhas. [OK] `app/src/main/assets/vantagens.json` id `superpoderes` (livro `SUPER`) — descrição já inclui a tabela completa abaixo. Linha ~892-975.

| Nível de Poder | Pontos de Superpoder | Limite de Poder |
|---|---|---|
| I | 15 | 5 |
| II | 30 | 10 |
| III | 45 | 15 |
| IV | 60 | 20 |
| V | 75 | 25 |

- **Limite de Poder**: nenhum super poder pode custar (após modificadores) mais que o Limite de Poder da campanha. Linha ~938.
- **Vantagem O Melhor que Há** (Antecedente, Novato) — dobra o Limite de Poder relativo (metade dos PSP totais em vez de 1/3). [OK] `vantagens.json` id `o_melhor_que_ha` (livro `SUPER`). Linha ~975.
- **Estrelas Ascendentes** (regra opcional, não é Vantagem/Complicação) — personagens começam com 1/3 a 2/3 dos PSP do Nível de Poder e ganham o resto com Progressos ao longo da campanha. Linha ~914. [FALTA] — regra de progressão de campanha, não mapeada em dado estruturado (baixa prioridade: é orientação de Mestre sobre distribuição de pontos ao longo do tempo, não uma opção de ficha).
- **Conjunto de Poderes** ("Power Sets" — formas alternativas com poderes totalmente diferentes, ex.: detetive que vira lobisomem) — cada conjunto adicional custa PSP fixo por Nível de Poder e reduz proporcionalmente o Limite de Poder de cada conjunto. Linha ~3200-3260.

| Nível de Poder | Custo do 2º+ Conjunto | Limite de Poder por Conjunto |
|---|---|---|
| I | 3 PSP | 4 |
| II | 5 PSP | 8 |
| III | 8 PSP | 12 |
| IV | 10 PSP | 16 |
| V | 12 PSP | 20 |

  [FALTA] — id sugerido: `conjunto_de_poderes` — mecânica de build (múltiplas listas de poder por personagem/forma) não representada em nenhum JSON; hoje só é citada em texto livre dentro da descrição do poder Mudança de Forma.
- **Treinamento** (recompensa de campanha, não de criação de personagem em si) — Mestre pode conceder +5 PSP após um grande desafio de campanha. Linha ~2457. Fora do escopo de ficha inicial — mencionado aqui só como contexto.

## Super Poderes

92 poderes no Capítulo Quatro (linhas ~3375–7900), todos com custo em PSP e lista própria de Modificadores/Efeitos. **Todos os 92 já estão cadastrados em `app/src/main/assets/super_poderes.json`** (contagem exata: 92 registros no arquivo = 92 títulos no Sumário do livro), com `custoBase`, `manifestacoes`, `descricao`/`descricaoLite` e `modificadores`/`modificadoresLite` completos — inclusive os poderes com custo "por nível" já vêm com a progressão expandida (ex.: Armadura 1..37, Resistência 1..37). Nomenclatura tem pequenas variações de capitalização/grafia entre livro e JSON (ex. "Não dorme" vs. "Não Dorme", "Controlar Máquinas" vs. "Controle de Máquinas", "Precisão mortal" vs. "Precisão Mortal") — cosmético, sem impacto funcional.

- **Absorção** (2 PSP) — id: `absorcao` — [OK] super_poderes.json — Absorve/neutraliza/redireciona um Tipo de Poder específico; rolagem gratuita de Foco no lugar de Absorção normal — linha ~3375
- **Ações Adicionais** (5/10/15 PSP) — id: `acoes_adicionais` — [OK] super_poderes.json — Permite realizar múltiplas ações por turno sem penalidade de Multi-Ação — linha ~3414
- **Alcance** (1–3 PSP) — id: `alcance` — [OK] super_poderes.json — Membros elásticos concedem Alcance +1 a +3 em ataques corpo a corpo — linha ~3493
- **Andar nas Paredes** (1 PSP) — id: `andar_nas_paredes` — [OK] super_poderes.json — Permite escalar/andar em superfícies verticais e tetos — linha ~3501
- **Anular** (3 PSP) — id: `anular` — [OK] super_poderes.json — Rolagem de Foco força o alvo a testar Espírito ou perder um poder/Habilidade temporariamente — linha ~3519
- **Aparar** (1/2/3/4/5 PSP) — id: `aparar` — [OK] super_poderes.json — +1 ao Aparar por compra, até +5 — linha ~3601
- **Aquático** (1/2 PSP) — id: `aquatico` — [OK] super_poderes.json — Torna a personagem semi ou totalmente aquática — linha ~3633
- **Armadura** (1/nível, 2 armadura por compra) — id: `armadura` — [OK] super_poderes.json — Armadura natural, não acumula com armaduras vestidas — linha ~3651
- **Ataque Corpo a Corpo** (0/2/4/6 PSP) — id: `ataque_corpo_a_corpo` — [OK] super_poderes.json — Aumenta o dano corpo a corpo além do dano de Força — linha ~3716
- **Ataque de Longa Distância** (3/6/9/12/15 PSP, tabela de dano 2d6–6d6) — id: `ataque_de_longa_distancia` — [OK] super_poderes.json — Ataque à distância com Alcance 12/24/48 — linha ~3921
- **Atordoar** (3 PSP) — id: `atordoar` — [OK] super_poderes.json — Foco resistido por Vigor deixa o alvo Atordoado — linha ~4028
- **Aumentar/Reduzir Característica** (2 PSP) — id: `aumentar_reduzir_caracteristica` — [OK] super_poderes.json — Aumenta ou reduz um atributo/perícia alvo — linha ~4055
- **Balançar** (2 PSP) — id: `balancar` — [OK] super_poderes.json — Deslocamento por cordas/teias com Movimentação 12 — linha ~4126
- **Bônus de Perícia** (2/4 PSP) — id: `bonus_de_pericia` — [OK] super_poderes.json — Excelência sobre-humana numa perícia específica — linha ~4163
- **Camaleão** (3 PSP) — id: `camaleao` — [OK] super_poderes.json — Copia a aparência de outra criatura de Tamanho próximo — linha ~4182
- **Campo de Dano** (3/5/10 PSP) — id: `campo_de_dano` — [OK] super_poderes.json — Aura/campo que dana quem chega perto — linha ~4234
- **Campo de Força** (1/nível) — id: `campo_de_forca` — [OK] super_poderes.json — Camada de energia protetora, reduz dano recebido — linha ~4275
- **Cavar** (1 PSP) — id: `cavar` — [OK] super_poderes.json — Movimentação subterrânea — linha ~4322
- **Cegar** (2 PSP) — id: `cegar` — [OK] super_poderes.json — Foco mira um alvo específico para cegá-lo — linha ~4370
- **Companheiro Animal** (3+Tamanho PSP) — id: `companheiro_animal` — [OK] super_poderes.json — Concede um companheiro animal Carta Selvagem — linha ~4403
- **Construto** (8 PSP) — id: `construto` — [OK] super_poderes.json — Personagem é autômato/ciborgue/robô — linha ~4471
- **Controle de Máquinas** (Especial) — id: `controlar_maquinas` — [OK] super_poderes.json (nome no JSON: "Controlar Máquinas") — Controla mentalmente máquinas elétricas a 12 quadros — linha ~4745
- **Controle Animal** (Especial) — id: `controle_de_animal` — [OK] super_poderes.json (nome no JSON: "Controle de Animal") — Controla e comunica-se com animais nativos — linha ~4510
- **Controle de Clima** (7 PSP) — id: `controle_de_clima` — [OK] super_poderes.json — Chama/dissipa tempestades, chuva, calor, neve — linha ~4587
- **Controle de Energia** (5 PSP) — id: `controle_de_energia` — [OK] super_poderes.json — Manipula um Tipo de Poder de energia escolhido — linha ~4660
- **Controle de Matéria** (5 PSP) — id: `controle_de_materia` — [OK] super_poderes.json — Manipula um Tipo de Poder de matéria escolhido — linha ~4812
- **Controle Mental** (5 PSP) — id: `controle_mental` — [OK] super_poderes.json — Domina mente e corpo de um alvo — linha ~4889
- **Crescimento** (3/nível) — id: `crescimento` — [OK] super_poderes.json — Aumenta o Tamanho da personagem — linha ~4971
- **Curar** (3 PSP) — id: `curar` — [OK] super_poderes.json — Cura a si mesmo ou outros a 6 quadros — linha ~5042
- **Decompor** (2 PSP) — id: `decompor` — [OK] super_poderes.json — Apodrece/destrói o alvo — linha ~5117
- **Destemido** (2 PSP) — id: `destemido` — [OK] super_poderes.json — Imunidade total a testes de Medo — linha ~5155
- **Duplicação** (4/nível) — id: `duplicacao` — [OK] super_poderes.json — Gera cópias de si mesmo — linha ~5221
- **Empurrar** (1 PSP) — id: `empurrar` — [OK] super_poderes.json — Afasta oponentes à distância — linha ~5194
- **Encolhimento** (4 PSP) — id: `encolhimento` — [OK] super_poderes.json — Reduz o Tamanho em até 2 pontos — linha ~5272
- **Enredar** (3 PSP) — id: `enredar` — [OK] super_poderes.json — Aprisiona oponentes (cordas, teias, energia) — linha ~5318
- **Escanear** (2 PSP) — id: `escanear` — [OK] super_poderes.json — Detecta alvos/objetos de um Tipo de Poder escolhido — linha ~5393
- **Escudo Mental** (1 PSP) — id: `escudo_mental` — [OK] super_poderes.json — Resistência a leitura/controle mental — linha ~5435
- **Espacial** (2 PSP) — id: `espacial` — [OK] super_poderes.json — Sobrevive no espaço e debaixo d'água — linha ~5460
- **Esquiva** (1/2/3/4/5 PSP) — id: `esquiva` — [OK] super_poderes.json — Penalidade a ataques de longa distância contra a personagem — linha ~5541
- **Explodir** (2 PSP) — id: `explodir` — [OK] super_poderes.json — Detona o próprio corpo e se reconstitui — linha ~5493
- **Falar Idioma** (1 PSP) — id: `falar_idioma` — [OK] super_poderes.json — Compreende/fala qualquer idioma — linha ~5525
- **Forma Alternativa** (2 PSP) — id: `forma_alternativa` — [OK] super_poderes.json — Corpo permanentemente feito de matéria/energia específica — linha ~5587
- **Furacão** (3 PSP) — id: `furacao` — [OK] super_poderes.json — Gera ciclone de ar/energia/matéria — linha ~5655
- **Gênio** (2 PSP) — id: `genio` — [OK] super_poderes.json — Rerrolagem gratuita em testes de Astúcia — linha ~5744
- **Ilusão** (4 PSP) — id: `ilusao` — [OK] super_poderes.json — Cria imagens/sons ilusórios — linha ~5756
- **Imune a Doenças/Venenos** (1/2 PSP) — id: `imune_a_doencas_venenos` — [OK] super_poderes.json — Imunidade a veneno e/ou doença — linha ~5828
- **Infecção** (2 PSP) — id: `infeccao` — [OK] super_poderes.json — Transmite infecção debilitante — linha ~5899
- **Intangibilidade** (5 PSP) — id: `intangibilidade` — [OK] super_poderes.json — Imune a ataques físicos, atravessa objetos — linha ~5883
- **Interface** (2 PSP) — id: `interface` — [OK] super_poderes.json — Conecta-se diretamente a equipamentos eletrônicos — linha ~6006
- **Invisibilidade** (8 PSP) — id: `invisibilidade` — [OK] super_poderes.json — Torna-se difícil de perceber — linha ~6032
- **Leitura de Objeto** (2 PSP) — id: `leitura_de_objeto` — [OK] super_poderes.json — Psicometria: impressões de um objeto — linha ~6084
- **Leitura Mental** (3 PSP) — id: `leitura_mental` — [OK] super_poderes.json — Capta pensamentos superficiais de um alvo — linha ~6126
- **Lentidão** (2 PSP) — id: `lentidao` — [OK] super_poderes.json — Reduz a Movimentação de um alvo — linha ~6203
- **Má Sorte** (4 PSP) — id: `ma_sorte` — [OK] super_poderes.json — Adversários próximos acumulam azar — linha ~6221
- **Mal Funcionamento** (3 PSP) — id: `mau_funcionamento` — [OK] super_poderes.json (nome no JSON: "Mau Funcionamento") — Faz tecnologia falhar a distância — linha ~6247
- **Medo** (2 PSP) — id: `medo` — [OK] super_poderes.json — Incute terror em oponentes — linha ~6292
- **Membros Extras** (2/nível) — id: `membros_extras` — [OK] super_poderes.json — Membros/braços preênseis adicionais — linha ~6328
- **Mimetismo** (1/nível) — id: `mimetismo` — [OK] super_poderes.json — Copia superpoderes de outras pessoas — linha ~6355
- **Morto-Vivo** (8 PSP) — id: `morto_vivo` — [OK] super_poderes.json (nome no JSON: "Morto-vivo") — Traços de morto-vivo (vampiro, zumbi etc.) — linha ~6432
- **Movimentação** (2 PSP) — id: `movimentacao` — [OK] super_poderes.json — Aumenta a Movimentação e corrida — linha ~6516
- **Mudança de Forma** (2/nível) — id: `mudanca_de_forma` — [OK] super_poderes.json — Transforma-se em animais naturais — linha ~6527
- **Não Come** (1 PSP) — id: `nao_come` — [OK] super_poderes.json — Reduz necessidade de alimentação — linha ~6597
- **Não Dorme** (1 PSP) — id: `nao_dorme` — [OK] super_poderes.json (nome no JSON: "Não dorme") — Reduz necessidade de sono — linha ~6613
- **Não Envelhece** (1 PSP) — id: `nao_envelhece` — [OK] super_poderes.json (nome no JSON: "Não envelhece") — Imune aos efeitos do tempo — linha ~6621
- **Não Respira** (1 PSP) — id: `nao_respira` — [OK] super_poderes.json (nome no JSON: "Não respira") — Segura a respiração por até 15 min — linha ~6639
- **Obscurecer** (4 PSP) — id: `obscurecer` — [OK] super_poderes.json — Campo de escuridão/neblina/fumaça — linha ~6649
- **Perceptivo** (1/nível) — id: `perceptivo` — [OK] super_poderes.json — Ignora penalidade de ataque por nível — linha ~6691
- **Possessão** (5 PSP) — id: `possessao` — [OK] super_poderes.json — Transfere consciência para outro corpo — linha ~6717
- **Precisão Mortal** (2 PSP) — id: `precisao_mortal` — [OK] super_poderes.json (nome no JSON: "Precisão mortal") — +d6 de dano com armas de fogo/arco/besta — linha ~6764
- **Reflexos Aprimorados** (3 PSP) — id: `reflexos_aprimorados` — [OK] super_poderes.json — Agilidade excepcional para escapar de perigo — linha ~6781
- **Regeneração** (2/5/10 PSP) — id: `regeneracao` — [OK] super_poderes.json — Cura Ferimentos rapidamente — linha ~6806
- **Resistência** (1/nível) — id: `resistencia` — [OK] super_poderes.json — +1 à Resistência por compra — linha ~6892
- **Resistência Ambiental** (1 PSP) — id: `resistencia_ambiental` — [OK] super_poderes.json — Resistência natural a um Tipo de Poder — linha ~6851
- **Robusto** (2 PSP) — id: `robusto` — [OK] super_poderes.json — Segundo Abalado seguido não vira Ferimento — linha ~6926
- **Salto** (1-8 PSP) — id: `salto` — [OK] super_poderes.json — Saltos extraordinários — linha ~6933
- **Sem Órgãos Vitais** (1 PSP) — id: `sem_orgaos_vitais` — [OK] super_poderes.json — Ataques Localizados não causam dano extra — linha ~6992
- **Sentidos Aprimorados** (Especial, 1 PSP por opção) — id: `sentidos_aprimorados` — [OK] super_poderes.json — Escuta, infravisão etc., cada opção 1 ponto — linha ~7000
- **Servos** (2 por servo) — id: `servos` — [OK] super_poderes.json — Concede um grupo de seguidores — linha ~7055
- **Superatributo** (2/nível) — id: `superatributo` — [OK] super_poderes.json — Eleva um atributo além do limite racial — linha ~7132
- **Superciência** (4 PSP) — id: `superciencia` — [OK] super_poderes.json — Inventa gadgets avançados na hora — linha ~7164
- **Superfeitiçaria** (4 PSP) — id: `superfeiticaria` — [OK] super_poderes.json — Canaliza magia arcana/divina como super poder — linha ~7197
- **Superperícia** (1/nível) — id: `superpericia` — [OK] super_poderes.json — Compra/eleva uma perícia além do teto normal — linha ~7233
- **Supervantagem** (2/nível) — id: `supervantagem` — [OK] super_poderes.json — Concede uma Vantagem escolhida, ignorando Requisitos de Estágio — linha ~7251
- **Telecinese** (3 PSP) — id: `telecinese` — [OK] super_poderes.json — Move objetos/criaturas com a mente — linha ~7261
- **Telepatia** (2 PSP) — id: `telepatia` — [OK] super_poderes.json — Comunicação mental a 24 quadros — linha ~7311
- **Teleporte** (2 PSP) — id: `teleporte` — [OK] super_poderes.json — Desaparece/reaparece a até 12 quadros — linha ~7344
- **Terremoto** (2 PSP) — id: `terremoto` — [OK] super_poderes.json — Abala o solo, desequilibra alvos na área — linha ~7451
- **Transmissão** (2 PSP) — id: `transmissao` — [OK] super_poderes.json — Capta sinais de rádio/TV/internet a distância — linha ~7499
- **Veículo** (1 PSP) — id: `veiculo` — [OK] super_poderes.json — Veículo pessoal sob medida — linha ~7537
- **Velocidade** (3-17 PSP) — id: `velocidade` — [OK] super_poderes.json — Deslocamento em velocidade extrema — linha ~7692
- **Veneno** (2 PSP) — id: `veneno` — [OK] super_poderes.json — Debilita/neutraliza com toxina — linha ~7795
- **Voo** (2-18 PSP) — id: `voo` — [OK] super_poderes.json — Capacidade de voar — linha ~7839

## Modificadores/Efeitos Genéricos

O "Sumário de Modificadores Universais de Poder" (linha ~3388-3400) lista os modificadores que se aplicam a **qualquer** Super Poder, além dos modificadores específicos já embutidos em cada poder no JSON:

- **Alternável (+1)** — permite ter várias versões do mesmo poder, cada uma custando igual ou menos que o custo base. Linha ~3040.
- **Arma Pesada (+1)** — ataque passa a ignorar Armadura Pesada corretamente (conta como Arma Pesada). Linha ~3064.
- **Característica Alternativa (+1)** — troca a Característica de ativação padrão (normalmente Foco) por outra perícia que faça sentido narrativo. Linha ~3075.
- **Vinculado/Conectado (0/+2)** — poder só ativa (Vinculado) ou pode ativar tanto sozinho quanto (Conectado, +2) junto de outra ação bem-sucedida. Linha ~3122.
- **Dispositivo (-1/-2)** — desconto para poder vir de um item removível (vestível: -1; segurado na mão: -2); nunca funciona para outra personagem. Linha ~3255.
- **Distância/Alcance (+2/+4)** — dobra (+2) ou triplica (+4) o Alcance do poder. Linha ~3031.
- **Especial (?)** — modificador livre, negociado com o Mestre, tipicamente na faixa de +/-2. Linha ~3287.
- **Limitação (-1/-2)** — desconto por restrição de uso (rara: -1; comum/metade das vezes: -2). Linha ~3374.
- **Forte/Poderoso (+1)** — aumenta em +1d6 a distância de Projetar causada pelo poder. Linha ~3300.
- **Seletivo (+1)** — permite escolher quais alvos afetar dentro de uma área de efeito. Linha ~3403.
- **Conjunto de Poderes** (ver Sistema de Pontos de Poder acima) — não é um modificador de custo por ponto, mas uma opção de build alternativa.

[FALTA] — nenhum desses 9 modificadores universais está representado como entrada própria em nenhum JSON (eles não aparecem nas listas `modificadores`/`modificadoresLite` de cada poder — essas listas trazem só os modificadores específicos daquele poder). Ids sugeridos, caso o app queira um catálogo à parte de modificadores aplicáveis a qualquer Super Poder: `mod_alternavel`, `mod_arma_pesada`, `mod_caracteristica_alternativa`, `mod_vinculado_conectado`, `mod_dispositivo`, `mod_alcance_poder`, `mod_especial`, `mod_limitacao`, `mod_forte_poderoso`, `mod_seletivo`.

## Vantagens

6 Vantagens novas específicas de campanha de supers (linhas ~883-1050), todas já cadastradas em `vantagens.json` com `livros: ["SUPER"]`:

- **Superpoderes** (Antecedente, Novato) — id: `superpoderes` — [OK] vantagens.json — Concede Pontos de Superpoder para comprar Super Poderes (ver tabela acima) — linha ~892
- **O Melhor que Há** (Antecedente, Novato) — id: `o_melhor_que_ha` — [OK] vantagens.json — Dobra o Limite de Poder relativo (1/2 dos PSP em vez de 1/3) — linha ~975
- **Aguenta o Tranco** (Combate; Experiente, Queixo de Ferro, Vigor d10+) — id: `aguenta_o_tranco` — [OK] vantagens.json — Rerrolagem gratuita de Absorção ou de Vigor contra Golpe Nocauteador — linha ~990
- **Líder de Equipe** (Liderança; Experiente, Comando, Elo Comum, Líder Nato) — id: `lider_de_equipe` — [OK] vantagens.json — Aliados no Raio de Comando trocam Benes como se todos tivessem Elo Comum — linha ~1001
- **Dupla Dinâmica** (Social; Novato, Espírito d8+) — id: `dupla_dinamica` — [OK] vantagens.json — Bônus de Ataque Combinado com um parceiro escolhido soma-se também ao dano — linha ~1013
- **Parceiro** (Social; releitura da Vantagem Parceiro core para Estágio Veterano) — id: `parceiro` — [OK] vantagens.json (já existe cadastro geral de Parceiro; versão SUPER também tagueada) — Parceiro ganha PSP igual ao Limite de Poder do mentor — linha ~1029

## Complicações

15 Complicações novas/adaptadas para campanhas de supers (linhas ~507-895), todas já cadastradas em `complicacoes.json`:

- **Anulação de Poder (Menor/Maior)** — id: `anulacao_de_poder` — [OK] complicacoes.json (livro SUPER) — Exposição a um gatilho remove todos os superpoderes — linha ~507
- **Aparência Distinta (Menor)** — id: `aparencia_distinta` — [OK] complicacoes.json (livro SUPER) — Aparência incomum torna a personagem fácil de identificar/rastrear — linha ~522
- **Dependência (Maior)** — id: `dependencia` — [OK, resolvido em 2026-08-31] — confirmado que o texto do livro (linha 535) é praticamente idêntico ao da entrada já tagueada `HORROR` (que já menciona "o super deve consumir..." e a nota "esta é a versão da Habilidade Racial Negativa"). Adicionada uma cópia com `livros: ["SUPER"]` — mesmo padrão de "uma entrada por livro em que o conteúdo é reimpresso" usado no resto do catálogo.
- **Dependente (Menor/Maior)** — id: `dependente` — [OK, resolvido em 2026-08-31] — a versão já cadastrada como `SCI_FI` é semelhante mas não idêntica (usa "Extra Resiliente" em vez de "Carta Selvagem de Estágio Novato sem poderes"). Criada uma cópia própria com `livros: ["SUPER"]` usando o texto exato do livro (linha 600) em vez de reaproveitar o texto do Sci-Fi — mesma Complicação, redação específica desta edição.
- **Doença Terminal (Menor/Maior)** — id: `doenca_terminal` — [OK] complicacoes.json (livro SUPER) — Doença incurável, penalidades em Vigor contra Fadiga — linha ~625
- **Forma Alienígena (Maior)** — id: `forma_alienigena` — [OK] complicacoes.json (livro SUPER) — Corpo completamente não-humano, -4 em Cura — linha ~658
- **Fraqueza Ambiental (Menor)** — id: `fraqueza_ambiental` — [OK] complicacoes.json (livro SUPER) — Vulnerabilidade a um Tipo de Poder específico (+4 dano, -4 Resistência) — linha ~683
- **Idealista (Menor)** — id: `idealista` — [OK] complicacoes.json (livro SUPER) — Visão de mundo preto-no-branco causa dilemas morais — linha ~699
- **Identidade Secreta (Maior)** — id: `identidade_secreta` — [OK] complicacoes.json (livro SUPER) — Alter-ego civil só conhecido por poucos — linha ~732
- **Imprudente (Maior)** — id: `imprudente` — [OK] complicacoes.json (livro SUPER) — Falha Crítica ao ativar poder causa efeito descontrolado/dano colateral — linha ~737
- **Megalomaníaco (Maior)** — id: `megalomaniaco` — [OK] complicacoes.json (livro SUPER) — Precisa estar no comando; penalidades a Suporte e Comando — linha ~756
- **Monólogo (Maior)** — id: `monologo` — [OK] complicacoes.json (livro SUPER, grafado "MONOLÓGO" no dado) — Vilania obriga a discursar em vez de agir — linha ~806
- **Sinistro (Menor)** — id: `sinistro` — [OK] complicacoes.json (livro SUPER) — Provocado por qualquer Provocar bem-sucedida — linha ~824
- **Transformação (Menor/Maior)** — id: `transformacao` — [OK] complicacoes.json (livro SUPER) — Precisa de gatilho/transformação para acessar poderes — linha ~839
- **Vulnerabilidade (Menor/Maior)** — id: `vulnerabilidade` — [OK] complicacoes.json (livro SUPER) — Exposição a substância causa Distraído ou Fadiga — linha ~873

## Ancestralidades/Templates

Não há Ancestralidades/raças jogáveis dedicadas neste livro. O texto reafirma que super-heróis usam as raças padrão do Savage Worlds Edição Aventura (o padrão default é Humano/Adaptável) e tratam alienígena/mutante/etc. apenas como Manifestação narrativa de poderes comprados ou como a Complicação **Forma Alienígena** — não como um bloco de Habilidades Raciais próprio. Nenhuma ação necessária; nada a cadastrar como raça.

## Equipamento

O Capítulo Dois (linhas ~1099-1474) lista equipamento temático de super-herói: escudos/capas especiais, armas corpo a corpo com modificadores (Arma de Energia, Arma Poderosa, Atordoar), armaduras específicas de supers, armas de pulso ("blaster"), armas especiais anti-super (anuladora, atordoante, de rede, lança-espuma) e veículos de superequipe. **Amostragem verificada em `equipamentos.json`** (326 itens com tag `SUPER`) — todos os itens abaixo já estão presentes:

- **Escudo Energético** — [OK] equipamentos.json — Aparar +2, rolagem gratuita de Absorção — linha ~1182
- **Arpéu Automático** — [OK] equipamentos.json — Concede o poder balançar via dispositivo — linha ~1199
- **Binóculos / Comunicadores / Mochila a Jato / Granada de Fumaça** — [OK] equipamentos.json — linhas ~1203/1208/1211/1219
- **Bastão / Bastão Anulador / Garras de Punho / Malho de Duas Mãos / Manoplas / Rede (Balanceada)** — [OK] equipamentos.json — armas corpo a corpo temáticas — linhas ~1273-1289
- **Armadura Corporal / Pesada / de Combate / de Combate Pesada / Elmo Balístico / Collant** — [OK] equipamentos.json — linhas ~1317-1332
- **Mod: Arma de Energia / Arma Poderosa / Atordoar / Corrente-Vibrolâmina / Lâmina Molecular** — [OK] equipamentos.json — modificadores de arma — linha ~1394+
- **Canhão / Gatling / Pistola / Rifle / Rifle de Assalto / Rifle Sniper / SMG "de Pulso"** — [OK] equipamentos.json — armas de energia — linha ~1210-1394
- **Balas de Borracha / Mata Capas** — [OK] equipamentos.json — munição especial — linha ~1394
- **Arma Anuladora / Arma Atordoante / Arma de Rede / Granada Atordoante / Lança Espuma** — [OK] equipamentos.json — armas especiais anti-super — linha ~1394-1421
- **Moto Chieftain / Jato Peregrine Jump / Juggernaut VBTP / Submarino de Ataque Mako / Tanque de Batalha Grizzly** — [OK] equipamentos.json — veículos de superequipe — linha ~1432-1474 (nota: são recursos tipicamente de equipe/base, mas o dado já existe como item comprável individualmente)
- Tabela de **Super Força e Sobrecarga** (peso levantável por dado de Força, d4 a d12+15) — linha ~1099-1150 — é regra de resolução de força, não um item de equipamento; não se aplica cadastro em `equipamentos.json`.

## Fora de escopo

Conteúdo lido e conscientemente excluído deste índice por não servir à criação de ficha individual de jogador:

- **Capítulo 3 — Regras de Ambientação** (linhas ~2024-2874): Combate de Quadrinhos, Ataques Combinados, Projetar, Proezas de Poderes, Armas Improvisadas Grandes, Sinergia (e Sinergia Ambiental), Morte e Derrota, Sem Golpe de Misericórdia, Grandes Responsabilidades, Mega Destruição, Nunca se Renda, Vem na Mão, Treinamento (regra de recompensa de campanha), Convicção Vilanesca, Estilos de Supers ("Quatro Cores", Cósmico, Sombrio, Fantasia, Horror, Ultraviolento, De Nada a Herói) e Níveis de Poder I-V (já resumidos na tabela de Superpoderes acima) — são regras de mesa/combate e conselhos de campanha para o Mestre, não escolhas de ficha.
- **Bases/QG** (linhas ~1474-1869, "Passo 1: Vantagem" a "Passo 4: Melhorias", Encontros, exemplo "Barragem da Patrulha do Destino") — recurso de grupo/equipe, explicitamente excluído pelo escopo da tarefa.
- **Capítulo Cinco — Galeria de Vilões** (linhas ~8205-14174 e a partir de ~14174): dezenas de fichas de vilões e NPCs modelo (Atlante, Legião Ômega, Punho Carmesim, Guarda do Destino, Agarrão, Alquimista, etc.) — bestiário de antagonistas de Mestre, não conteúdo de criação de personagem jogável.
- **Conversão Necessary Evil / V'Sori** (a partir de ~14174, incluindo "Armas Veiculares V'Sori" ~15331) — conteúdo de cenário/conversão para campanha específica, fora do escopo de ficha genérica.
- Sumário de Poderes (linha ~7922, índice remissivo de página) e Créditos/Sumário geral (linhas 1-419) — material de referência editorial, sem conteúdo de regras.
