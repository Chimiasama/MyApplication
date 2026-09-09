# Auditoria — Modificadores Universais de Poder em `super_poderes.json`

## Metodologia

1. O texto-fonte foi lido em `docs/swade_superpoderes` (linhas ~2990–3410, "Modificadores Universais de Poder", e o quadro-resumo em ~3333–3351). O resumo fornecido na tarefa foi conferido contra o texto verbatim e está correto; os valores de custo, texto e exclusões usados abaixo seguem o livro.
2. Os 92 poderes de `app/src/main/assets/super_poderes.json` foram lidos por completo (campos `nome`, `custoBase`, `modificadores`, `descricao`, `manifestacoes`, `nivelRepeticoes`).
3. Para cada um dos 92 poderes e cada um dos 8 modificadores universais em escopo (excluindo **Especial**, tratado por outra funcionalidade do app), foi feita a classificação:
   - **já presente** — o poder já tem modificador nomeado equivalente (não reportado individualmente).
   - **não aplicável** — não faz sentido mecânico para aquele poder (não reportado individualmente, apenas contado).
   - **já coberto pelo texto-base** (princípio de deduplicação do dono do produto) — reportado na seção final "De-dup".
   - **lacuna genuína** — reportado nas tabelas por modificador, com o texto JSON exato recomendado.
4. Onde o próprio texto de um poder já menciona um modificador universal por nome (ex.: "Isso não pode ser combinado com os modificadores Alcance e Área", ou "use o modificador Seletivo para evitar afetar aliados") mas esse modificador **não aparece** no array `modificadores` daquele poder, isso foi tratado como evidência forte de lacuna genuína (o texto já pressupõe que o modificador exista como opção), não como deduplicação — a deduplicação só se aplica quando o efeito do modificador **já ocorre automaticamente**, sem custo, no próprio mecanismo do poder (ex.: Empurrar/Furacão já causam Projetar por conta própria).
5. Os textos JSON recomendados seguem o formato `"Nome (+custo/-custo): descrição"` exigido pelo parser de custo do app (tudo antes do primeiro `:`, com o grupo de parênteses de custos ao final).

Nenhuma alteração foi feita em `super_poderes.json` — este é um relatório de auditoria apenas.

---

## 1. Alternável (+1)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Ataque de Longa Distância | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste ataque (variando ao menos a Manifestação e o Tipo de Poder), cada uma custando o mesmo ou menos que o custo original, podendo alternar entre elas quando desejar."` | É o próprio exemplo do livro-base para este modificador (heroína elemental com ataques de água/fogo/ar/terra), mas o poder de ataque de longa distância "padrão" do sistema não o lista. |
| Atordoar | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (elétrico, tóxico, mental, sônico etc.), cada uma custando o mesmo ou menos que o custo original."` | Poder de status com Tipo de Poder variável — encaixe natural para versões temáticas alternativas. |
| Anular | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder, cada uma custando o mesmo ou menos que o custo original."` | Um "anulador" poderia ter versão anti-magia e versão anti-tecnologia com o mesmo custo. |
| Cegar | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (luz, areia, controle mental etc.), cada uma custando o mesmo ou menos que o custo original."` | Diferentes fontes de cegueira já listadas nas Manifestações se encaixam como versões alternáveis. |
| Decompor | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (ferrugem, ácido, podridão etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações distintas do próprio poder já sugerem versões alternáveis. |
| Enredar | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (teias, correntes, vinhas etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações variadas do poder se encaixam bem como versões alternáveis. |
| Infecção | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder, cada uma custando o mesmo ou menos que o custo original."` | Pragas temáticas diferentes (viral, mágica, nanotecnológica) cabem como versões alternáveis. |
| Medo | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder, cada uma custando o mesmo ou menos que o custo original."` | Fontes de medo diferentes (visual, sônica, feromônios) cabem como versões alternáveis. |
| Veneno | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder, cada uma custando o mesmo ou menos que o custo original."` | Toxinas diferentes (paralisante, corrosiva, nervosa) cabem como versões alternáveis. |
| Campo de Dano | `"Alternável (+1): Permite adquirir versões temáticas alternativas desta aura (fogo, gelo, espinhos etc.), cada uma custando o mesmo ou menos que o custo original."` | Auras de elementos diferentes são candidatas naturais a versões alternáveis. |
| Empurrar | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (vento, telecinese, força bruta etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações plausíveis diferentes para o mesmo efeito de empurrão. |
| Furacão | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (ar, fogo, destroços etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações de vórtice diferentes se encaixam como versões alternáveis. |
| Controle de Energia | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder com combinações de modificadores diferentes, cada uma custando o mesmo ou menos que o custo original."` | "Tipo de poder adicional" amplia os tipos manipulados na mesma compra, mas não permite builds de modificadores totalmente diferentes por versão — Alternável cobre essa lacuna. |
| Controle de Matéria | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder com combinações de modificadores diferentes, cada uma custando o mesmo ou menos que o custo original."` | Mesmo raciocínio de Controle de Energia. |
| Ilusão | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (visual, sonora, holográfica de alta tecnologia etc.), cada uma custando o mesmo ou menos que o custo original."` | Diferentes naturezas de ilusão cabem como versões alternáveis. |
| Obscurecer | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (escuridão, neblina, fumaça etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes do próprio poder cabem como versões alternáveis. |
| Explodir | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (explosão química, radioativa, de energia pura etc.), cada uma custando o mesmo ou menos que o custo original."` | Diferentes naturezas de explosão cabem como versões alternáveis. |
| Controle Mental | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (hipnose, vodu, tecnologia etc.), cada uma custando o mesmo ou menos que o custo original."` | Diferentes "vetores" de controle mental cabem como versões alternáveis. |
| Telecinese | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (magia, gravidade, psiônica etc.), cada uma custando o mesmo ou menos que o custo original."` | Diferentes fontes de força mental cabem como versões alternáveis. |
| Teleporte | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (fumaça, magia, discos tecnológicos etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes do próprio poder cabem como versões alternáveis. |
| Intangibilidade | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (energia, neblina, fantasmagoria etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Invisibilidade | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (alteração celular, camuflagem tecnológica etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Camaleão | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (forma maleável, kit de disfarce de alta tecnologia etc.), cada uma custando o mesmo ou menos que o custo original."` | As próprias duas Manifestações listadas já sugerem duas versões alternáveis plausíveis. |
| Encolhimento | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (pílulas, magia, metabolismo mutante etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Terremoto | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (vibração sônica, geológica etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Lentidão | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (desacelerar o tempo, gelo escorregadio etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Mau Funcionamento | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (pulso eletromagnético, maldição etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Leitura Mental | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (concentração, brilho místico, hipnotismo etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Possessão | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (troca de alma, fantasma etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Aumentar/Reduzir Característica | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder, cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes (karma, nanotecnologia, manipulação mental) cabem como versões alternáveis. |
| Controlar Máquinas | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (tecnocinese, integração eletrônica etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Controle de Animal | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (encantamentos, dispositivos tecnológicos etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |
| Controle de Clima | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder, cada uma custando o mesmo ou menos que o custo original."` | Diferentes fenômenos climáticos já sugerem versões temáticas alternáveis. |
| Escanear | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (sentidos animais, sensores de alta tecnologia etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes com Tipos de Poder distintos cabem como versões alternáveis. |
| Leitura de Objeto | `"Alternável (+1): Permite adquirir versões temáticas alternativas deste poder (impressões psíquicas, contato com fantasmas etc.), cada uma custando o mesmo ou menos que o custo original."` | Manifestações diferentes cabem como versões alternáveis. |

**Total nesta seção: 35.** Esta é a categoria com maior volume e a mais subjetiva — recomenda-se revisão humana antes de aplicar em massa (ver observação final).

---

## 2. Arma Pesada (+1)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Ataque de Longa Distância | `"Arma Pesada (+1): O ataque conta como uma Arma Pesada. Armaduras marcadas com Arma Pesada ignoram dano de quaisquer ataques que não tenham a marcação Arma Pesada."` | É o ataque à distância "padrão" do sistema e não tem nenhuma forma de ignorar Armadura Pesada — lacuna clara frente a Ataque Corpo a Corpo, que já possui este modificador. |
| Campo de Dano | `"Arma Pesada (+1): O dano causado pelo campo conta como Arma Pesada. Armaduras marcadas com Arma Pesada ignoram dano de quaisquer ataques que não tenham a marcação Arma Pesada."` | A aura causa dano direto por rolagem (2d6–4d6), mas nada permite ignorar Armadura Pesada dos alvos adjacentes. |
| Explodir | `"Arma Pesada (+1): O dano da explosão conta como Arma Pesada. Armaduras marcadas com Arma Pesada ignoram dano de quaisquer ataques que não tenham a marcação Arma Pesada."` | Causa 3d10–5d10 de dano por rolagem, mas nenhum modificador atual permite ignorar Armadura Pesada. |
| Furacão | `"Arma Pesada (+1): Só disponível com o modificador Dano. O dano causado conta como Arma Pesada."` | O modificador opcional Dano (+2) já adiciona uma rolagem de dano direto; falta como ignorar Armadura Pesada nela. |
| Enredar | `"Arma Pesada (+1): Só disponível com o modificador Mortal. O dano causado conta como Arma Pesada."` | O modificador opcional Mortal já causa dano direto (2d6/3d6); falta como ignorar Armadura Pesada nesse dano. |

**Total nesta seção: 5.**

---

## 3. Característica Alternativa (+1)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Atordoar | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | É citado no próprio livro-base como exemplo de poder que aceita Característica Alternativa em combinação com Contingente/Dispositivo, mas o modificador não está na lista. |
| Anular | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Um "anulador" poderia usar Ocultismo ou Persuadir em vez de Foco. |
| Aumentar/Reduzir Característica | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Persuadir (liderança) ou Curar (energia vital) em vez de Foco. |
| Cegar | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Atirar (jato de areia) em vez de Foco. |
| Controlar Máquinas | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Reparar/Eletrônica em vez de Foco. |
| Controle de Animal | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Sobrevivência em vez de Foco. |
| Controle de Clima | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Ocultismo em vez de Foco. |
| Controle Mental | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Persuadir/Intimidar em vez de Foco. |
| Curar | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar a perícia Curar em vez de Foco (o próprio texto universal cita justamente Curar como exemplo de Característica Alternativa). |
| Decompor | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Ocultismo (maldição) em vez de Foco. |
| Duplicação | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Ocultismo/Conhecimento Arcano em vez de Foco. |
| Empurrar | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Atletismo (rajada física) em vez de Foco. |
| Enredar | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Atletismo (arremesso de rede) em vez de Foco. |
| Ilusão | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Persuadir/Enganar em vez de Foco. |
| Infecção | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Sobrevivência em vez de Foco. |
| Intangibilidade | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | O ataque de fase (rolagem de Foco contra Atletismo/Vigor do alvo) poderia usar Espírito em vez de Foco. |
| Leitura de Objeto | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Ocultismo em vez de Foco. |
| Leitura Mental | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Persuadir em vez de Foco. |
| Lentidão | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Atirar (jato de gelo) em vez de Foco. |
| Mau Funcionamento | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Eletrônica/Reparar em vez de Foco. |
| Medo | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Intimidar em vez de Foco. |
| Mimetismo | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Conhecimento (Ciência) em vez de Foco. |
| Mudança de Forma | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Sobrevivência/Ocultismo em vez de Foco. |
| Possessão | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Espírito/Ocultismo em vez de Foco. |
| Teleporte | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Conjurar/Atletismo em vez de Foco. |
| Veneno | `"Característica Alternativa (+1): É possível usar uma perícia diferente de Foco para ativar este poder, desde que faça sentido narrativo e conte com aprovação do Mestre."` | Poderia usar Sobrevivência em vez de Foco. |

**Total nesta seção: 26.**

---

## 4. Vinculado/Conectado (0/+2)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Atordoar | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Citado explicitamente no livro-base como exemplo de poder que aceita Contingente (Vinculado/Conectado) atrelado a um ataque de longa distância. |
| Enredar | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | É o próprio exemplo do livro-base (flecha com rede Vinculada a um ataque de longa distância). |
| Cegar | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Encaixe natural como efeito secundário de outro ataque (ex.: um soco que também cega). |
| Decompor | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Conectado a um ataque corpo a corpo (toque decompositor). |
| Infecção | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Vinculado a uma mordida ou arranhão de Ataque Corpo a Corpo. |
| Veneno | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Mesma lógica de Infecção — encaixe natural em uma mordida ou lâmina envenenada. |
| Medo | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Vinculado a uma aparência intimidadora ativada junto de outro ataque. |
| Mau Funcionamento | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Conectado a um ataque de longa distância (ex.: pulso eletromagnético disparado junto com um tiro). |
| Anular | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Vinculado a um ataque corpo a corpo (toque anulador). |
| Aumentar/Reduzir Característica | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Conectado a outro poder ativado (ex.: Sanguessuga disparando automaticamente). |
| Leitura Mental | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Vinculada a um ataque bem-sucedido (leitura instantânea ao acertar). |
| Lentidão | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Conectada a um ataque de longa distância. |
| Possessão | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | Pode ser Vinculada a um ataque corpo a corpo (toque possessivo). |
| Controle Mental | `"Vinculado/Conectado (0/+2): De graça (Vinculado), este poder só ativa junto com uma ação primária bem-sucedida (como um ataque ou outro poder). Por +2 pontos (Conectado), também pode ser ativado sozinho. Limitado a uma vez por ação, usando a rolagem da ação primária."` | O livro cita Controle Mental como poder "primário" ao qual outros (ex.: decompor) se conectam, mas o próprio Controle Mental também pode ser Vinculado a uma ação diferente (ex.: um olhar hipnótico ativado junto de um ataque). |

**Total nesta seção: 14.**

---

## 5. Dispositivo (-1/-2)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Armadura | `"Dispositivo (-1/-2): O poder vem de um item removível. Reduza o custo em 1 se for algo vestido ou difícil de remover (ex.: parte de armadura, elmo), ou em 2 se for algo segurado na mão e fácil de perder ou desarmar. Nunca funciona para outro personagem."` | É o próprio exemplo do livro-base para o desconto de -1 ("uma parte de armadura"), mas falta na lista deste poder. |
| Campo de Força | `"Dispositivo (-1/-2): O poder vem de um item removível. Reduza o custo em 1 se for algo vestido ou difícil de remover (ex.: cinto de campo de força), ou em 2 se for algo segurado na mão e fácil de perder ou desarmar. Nunca funciona para outro personagem."` | A própria lista de Manifestações já cita "cinto de campo de força" — exatamente o exemplo do livro para Dispositivo. |
| Ataque de Longa Distância | `"Dispositivo (-1/-2): O poder vem de um item removível. Reduza o custo em 1 se for algo vestido ou difícil de remover, ou em 2 se for algo segurado na mão e fácil de perder ou desarmar (ex.: uma arma high tech). Nunca funciona para outro personagem."` | Manifestações incluem "armas high tech" — encaixe óbvio para o desconto de Dispositivo. |
| Balançar | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: arma de cabos). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação "arma de cabos" é um gancho/dispositivo clássico, mas falta o desconto de Dispositivo. |
| Falar Idioma | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: tradutor eletrônico). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "Dispositivos" explicitamente, mas o modificador não está na lista. |
| Anular | `"Dispositivo (-1/-2): O poder vem de um item removível. Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "Dispositivos" explicitamente. |
| Camaleão | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: kit de disfarce de alta tecnologia). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "kits de disfarce de alta tecnologia". |
| Controlar Máquinas | `"Dispositivo (-1/-2): O poder vem de um item removível. Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Encaixe natural com uma luva/controlador tecnocinético removível. |
| Controle de Animal | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: dispositivo tecnológico). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "dispositivos tecnológicos". |
| Escanear | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: sensores de alta tecnologia). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "sensores de alta tecnologia". |
| Espacial | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: traje espacial). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestações incluem "traje espacial" e "bugigangas" — item clássico removível. |
| Interface | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: dispositivo de hacking). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestações incluem laptops/ciborgues — dispositivo de hacking plausível. |
| Invisibilidade | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: traje furtivo). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "traje furtivo". |
| Transmissão | `"Dispositivo (-1/-2): O poder vem de um item removível. Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "dispositivos de alta tecnologia" explicitamente. |
| Superatributo | `"Dispositivo (-1/-2): Só disponível para versões baseadas em equipamento (ex.: armadura energizada). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação "armadura energizada (Força)" sugere um exosqueleto removível. |
| Crescimento | `"Dispositivo (-1/-2): O poder vem de um item removível ou consumível (ex.: pílulas de crescimento). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestações incluem "robô" e "pílulas de crescimento". |
| Aumentar/Reduzir Característica | `"Dispositivo (-1/-2): O poder vem de um item removível (ex.: nanotecnologia portátil). Reduza o custo em 1 se difícil de remover, ou em 2 se fácil de perder ou desarmar. Nunca funciona para outro personagem."` | Manifestação já cita "nanotecnologia". |
| Ataque Corpo a Corpo | `"Dispositivo (-1/-2): Pode ser adquirido separadamente para cada forma de ataque (Mordida, Garras, Arma Especial etc.), nunca reduzindo o custo daquela forma abaixo de 1. Nunca funciona para outro personagem."` | O próprio texto do poder já explica como aplicar Dispositivo a cada forma de ataque separadamente, mas o modificador não aparece na lista de modificadores do poder. |

**Total nesta seção: 18.**

---

## 6. Distância/Alcance (+2/+4)

*(Não se aplica ao poder Camaleão, cujo "Requer toque" desconta o custo por copiar aparência sem que exista uma Distância base a dobrar/triplicar — ver seção de não-aplicáveis.)*

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Ataque de Longa Distância | `"Distância (+2/+4): Dobra a Distância deste poder (de 12/24/48 para 24/48/96) por +2 pontos, ou a triplica (36/72/144) por +4."` | Poder de ataque à distância "padrão" do sistema não possui nenhuma forma de estender seu Alcance base. |
| Anular | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | O próprio modificador Requer Toque do poder já cita "Alcance" como incompatível, confirmando que deveria existir como opção — mas não está na lista. |
| Atordoar | `"Distância (+2/+4): Dobra ou triplica a Distância deste poder."` | Requer Toque do poder já cita Alcance como excludente, confirmando a lacuna. |
| Aumentar/Reduzir Característica | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Requer Toque cita Alcance como incompatível, mas o modificador não está listado. |
| Cegar | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Poder tem Distância fixa de 12 quadros sem nenhuma forma de estendê-la. |
| Controlar Máquinas | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Requer Toque cita Alcance como incompatível. |
| Controle de Animal | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Requer Toque cita Alcance e Invocável como incompatíveis, confirmando a lacuna. |
| Controle Mental | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Requer Toque cita Alcance como incompatível. |
| Curar | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Requer Toque cita Alcance como incompatível; poder de cura à distância sem forma de estendê-la. |
| Decompor | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Enredar | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Mesmo raciocínio. |
| Infecção | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Leitura de Objeto | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Leitura Mental | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Mau Funcionamento | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Mesmo raciocínio. |
| Medo | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Mimetismo | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Possessão | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Veneno | `"Distância (+2/+4): Dobra a Distância deste poder (de 6 para 12 quadros) por +2 pontos, ou a triplica (18 quadros) por +4."` | Mesmo raciocínio. |
| Lentidão | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Poder de longo alcance sem nenhuma forma de estendê-lo. |
| Controle de Clima | `"Distância (+2/+4): Dobra a distância em que a "tempestade" de combate pode ser conjurada (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | A distância de invocação da tempestade de combate não pode ser estendida atualmente. |
| Controle de Energia | `"Distância (+2/+4): Dobra a distância em que o campo de energia pode ser conjurado (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | A distância de invocação do campo de energia não pode ser estendida atualmente. |
| Controle de Matéria | `"Distância (+2/+4): Dobra a distância em que o campo de matéria pode ser conjurado (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | Mesmo raciocínio de Controle de Energia. |
| Telecinese | `"Distância (+2/+4): Dobra a Distância deste poder (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | O alcance fixo do poder não pode ser estendido atualmente. |
| Furacão | `"Distância (+2/+4): Dobra a distância em que o furacão pode ser conjurado (de 12 para 24 quadros) por +2 pontos, ou a triplica (36 quadros) por +4."` | A distância de invocação não pode ser estendida atualmente; o modificador Cone é opcional/alternativo (não obrigatório), então Distância continua aplicável à forma padrão. |

**Total nesta seção: 25.**

---

## 7. Limitação (-1/-2)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Ataque de Longa Distância | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Nenhum modificador de restrição situacional está disponível hoje para este ataque "padrão" do sistema. |
| Anular | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição de tipo (ex.: não afeta magia, ou só afeta tecnologia). |
| Atordoar | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Citado no próprio livro-base como exemplo de poder combinável com restrições situacionais. |
| Aumentar/Reduzir Característica | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | O próprio texto do poder já descreve como aplicar esta Limitação (só aumentar OU só diminuir; ou só afetar perícias/atributos), mas o modificador não está na lista de opções do poder. |
| Cegar | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição (ex.: não funciona contra criaturas sem visão). |
| Decompor | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição de material (ex.: não afeta metal, ou só afeta orgânicos). |
| Enredar | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição (ex.: não funciona debaixo d'água). |
| Infecção | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição (ex.: não afeta mortos-vivos ou construtos). |
| Medo | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição (ex.: só funciona à noite). |
| Veneno | `"Limitação (-1/-2): Uma restrição rara ao uso do poder reduz seu custo em 1 ponto; uma restrição comum (que impeça seu uso em metade das vezes) reduz o custo em 2."` | Poderia ter restrição (ex.: não afeta construtos/mortos-vivos). |

**Total nesta seção: 10.** Nota: Limitação é, por natureza, o modificador mais dependente de contexto de campanha/conceito de personagem (na prática, quase qualquer poder ativo poderia justificá-lo). A lista acima foi deliberadamente restrita ao núcleo de poderes de ataque/status de curto alcance onde o encaixe é mais óbvio e consistente entre si; não é uma lista exaustiva de todo poder que teoricamente poderia ganhar uma Limitação.

---

## 8. Forte/Poderoso (+1/+2)

**Nenhuma lacuna genuína encontrada.** Este modificador universal só se aplica a poderes cujo próprio efeito causa Projetar (a regra de nocaute/arremesso do SWADE). Os únicos dois poderes do arquivo que causam Projetar como parte do próprio texto-base são **Empurrar** e **Furacão**, e ambos já têm seu próprio "Forte (+2)" nomeado cobrindo o mesmo território mecânico — ver seção de De-dup abaixo para o raciocínio completo (é o exemplo de referência fornecido pelo dono do produto). Nenhum outro poder do arquivo causa Projetar em sua própria descrição-base, então não há mais candidatos.

---

## 9. Seletivo (+1)

| Poder | Texto JSON recomendado | Justificativa breve |
|---|---|---|
| Campo de Força | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados ou oponentes indesejados."` | Confirmado pelo dono do produto — o próprio texto do poder já sugere "Adquira Seletivo para excluir oponentes!", mas o modificador não está na lista. |
| Empurrar | `"Seletivo (+1): É possível escolher quais alvos afetar dentro do Modelo de Cone, ignorando aliados indesejados."` | O próprio texto do poder já recomenda "use o modificador Seletivo para evitar afetar aliados" — mesma lacuna textual confirmada do Campo de Força, mas o modificador não está na lista. |
| Anular | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro de uma área, permite não anular os poderes dos aliados. |
| Ataque de Longa Distância | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro do Modelo de Explosão comprado via Área, permite poupar aliados. |
| Atordoar | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Mesmo raciocínio. |
| Campo de Dano | `"Seletivo (+1): É possível escolher quais alvos adjacentes afetar, ignorando aliados indesejados."` | Permite não atordoar/queimar aliados adjacentes à aura. |
| Cegar | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite poupar aliados da cegueira. |
| Controle de Energia | `"Seletivo (+1): É possível escolher quais alvos afetar dentro do campo, ignorando aliados indesejados."` | O efeito de Dano/Distrair da área pode afetar aliados sem este modificador. |
| Controle de Matéria | `"Seletivo (+1): É possível escolher quais alvos afetar dentro do campo, ignorando aliados indesejados."` | Mesmo raciocínio de Controle de Energia. |
| Decompor | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos ou objetos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite poupar objetos/aliados. |
| Enredar | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite não enredar aliados. |
| Furacão | `"Seletivo (+1): É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite não empurrar/distrair aliados. |
| Infecção | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite poupar aliados do contágio. |
| Mau Funcionamento | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais dispositivos afetar dentro do Modelo, ignorando os de aliados."` | Dentro da área, permite poupar os próprios dispositivos/aliados eletrônicos. |
| Medo | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite poupar aliados do teste de Medo. |
| Veneno | `"Seletivo (+1): Só disponível com o modificador Área. É possível escolher quais alvos afetar dentro do Modelo, ignorando aliados indesejados."` | Dentro da área, permite poupar aliados. |
| Controle de Clima | `"Seletivo (+1): É possível escolher quais alvos afetar dentro do modelo da tempestade de combate, ignorando aliados."` | O efeito Distrair da tempestade afeta todos dentro do modelo, incluindo aliados, sem opção de exclusão. |
| Terremoto | `"Seletivo (+1): É possível escolher quais alvos afetar dentro da área de efeito, ignorando aliados."` | O tremor abala todos na área, incluindo aliados, sem opção de exclusão. |
| Explodir | `"Seletivo (+1): É possível escolher quais alvos afetar dentro do Modelo Grande de Explosão, ignorando aliados."` | A explosão afeta todos ao redor, incluindo aliados, sem opção de exclusão. |

**Total nesta seção: 19.**

---

## De-dup — casos limítrofes revisados

Casos em que um modificador universal **não** foi recomendado porque o próprio texto-base do poder já concede o efeito equivalente, sem custo adicional (princípio "se o texto já aplica algo que seria coberto por um modificador não precisa por"):

| Poder | Modificador não recomendado | Raciocínio |
|---|---|---|
| Empurrar | Forte/Poderoso (+1/+2) | O poder já causa Projetar como efeito inerente da sua própria descrição-base (1d6/2d6 quadros); é o exemplo-base fornecido pelo dono do produto. |
| Furacão | Forte/Poderoso (+1/+2) | Mesma razão — Projetar (2d6/3d6 quadros) já é parte do efeito-base do poder, mesmo que o "Forte (+2)" já listado no poder não inclua textualmente o +1d6 extra do modificador universal. |
| Absorção | Alternável (+1) | O próprio poder já tem o modificador "Tipos Adicionais de Poder", que permite customizar quais Tipos de Poder são absorvidos dentro da mesma compra — cobrindo a mesma necessidade de variação que Alternável supriria. |
| Absorção | Característica Alternativa (+1) | O texto-base já substitui Espírito por Foco na rolagem de Absorção ("usando Foco em vez de Espírito") — a troca de Característica que o modificador concederia já está embutida de graça na regra do próprio poder. |
| Ataque de Longa Distância | Característica Alternativa (+1) | O texto-base já deixa a critério do jogador escolher livremente qual perícia ativa o ataque no momento da compra ("A heroína ou herói decide que Característica seu ataque usa ao adquirir o poder") — não há necessidade de pagar por algo que já é gratuito e nativo do poder. |
| Telecinese | Característica Alternativa (+1) | O texto-base já define seu próprio mecanismo bespoke de seleção de Característica (usar sempre a menor entre Foco e a perícia física relevante) — um modificador genérico de troca de Característica conflitaria/duplicaria essa regra própria. |
| Companheiro Animal | Alternável (+1) | O texto-base já autoriza comprar o poder novamente para criar companheiros diferentes ("VÁRIOS COMPANHEIROS"), cumprindo a mesma função de Alternável sem exigir o modificador. |
| Servos | Alternável (+1) | Mesma razão — "VÁRIOS SERVOS" já permite conjuntos temáticos diferentes via recompra direta do poder. |
| Mudança de Forma | Alternável (+1) | O próprio texto redireciona quem deseja formas mais exóticas/variadas para o sistema de Conjunto de Poderes ("Quem deseja mudanças mais exóticas deve criar cada forma como um Conjunto de Poderes"), então Alternável não é a ferramenta indicada pelo livro para este poder. |
| Ilusão | Seletivo (+1) | O sub-modificador "Distração" já poderoso permite, à vontade de quem criou a ilusão, afetar apenas inimigos dentro da área ("Se quem a criou desejar, inimigos na área de efeito ficam Distraídos") — já entrega seleção de alvo para o efeito prejudicial relevante. |

---

## Não aplicável (contagem, não listado exaustivamente)

Para os 92 poderes × 8 modificadores em escopo (736 combinações), a maioria foi classificada como "não aplicável" por incompatibilidade mecânica óbvia (ex.: Seletivo em poderes sem efeito de área/modelo; Arma Pesada em poderes que não causam dano por rolagem; Distância em poderes sem Distância própria, com toque obrigatório, ou com Modelo de Cone obrigatório; Vinculado/Conectado, Característica Alternativa e Limitação em poderes sem rolagem/evento de ativação — ex.: Não Envelhece, que o próprio livro exclui explicitamente destes dois últimos; Dispositivo em poderes onde a ideia de item removível não faz sentido narrativo básico, como identidades raciais permanentes). Essas combinações somam a grande maioria das 736 possibilidades e não foram listadas linha a linha por não agregarem valor de auditoria.

Exclusões explícitas do livro aplicadas: **Veículo** nunca pode ter Dispositivo (regra explícita do próprio poder); nenhum poder sem rolagem de ativação recebeu Vinculado/Conectado, Característica Alternativa ou Limitação.

---

## Resumo final

- **Total de recomendações (lacunas genuínas):** 152
  - Alternável: 35
  - Característica Alternativa: 26
  - Distância/Alcance: 25
  - Seletivo: 19
  - Dispositivo: 18
  - Vinculado/Conectado: 14
  - Limitação: 10
  - Arma Pesada: 5
  - Forte/Poderoso: 0
- **Poderes com 0 modificadores novos recomendados:** 43 de 92
- **Poderes com 1 ou mais modificadores novos recomendados:** 49 de 92
- **Casos de de-dup (já cobertos por texto-base) revisados:** 10
- **Poderes com o maior número de lacunas:** Anular e Enredar (7 cada); Ataque de Longa Distância, Atordoar, Aumentar/Reduzir Característica, Cegar, Decompor, Infecção, Medo e Veneno (6 cada) — todos poderes de ataque/status de curto-médio alcance com "Requer toque" já presente, o que expôs consistentemente a mesma família de lacunas (Distância, Vinculado/Conectado, Característica Alternativa, Limitação, Seletivo, Alternável).

### Pontos que merecem sanity-check humano antes de aplicar ao JSON

1. **Alternável** é, de longe, a categoria mais subjetiva — qualquer poder de compra única pode, em tese, justificá-la, então a lista de 35 reflete um corte de julgamento (poderes de efeito/ataque temático) e não uma regra mecânica rígida. Recomenda-se revisão humana antes de aplicar em massa.
2. **Característica Alternativa** — as perícias alternativas sugeridas nas justificativas (Ocultismo, Persuadir, Sobrevivência etc.) são exemplos ilustrativos de "faz sentido narrativo", não uma lista fechada; o texto JSON recomendado é propositalmente genérico para não engessar a escolha do jogador.
3. **Limitação** foi deliberadamente sub-representada (só 10 itens) por ser o modificador mais dependente de conceito de personagem/campanha — a lista real de poderes elegíveis é maior na teoria, mas listar todos non-Special-like seria pouco útil.
4. **Distância/Alcance** e **Seletivo** têm a base mais sólida de evidência (o próprio texto de cada poder já menciona o modificador por nome como excludente/recomendado, mas ele está ausente da lista) — estes são os dois grupos com maior confiança de que a omissão é um bug de dados, não uma escolha deliberada.
