package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify

/**
 * Custo em pontos de cada traço racial (entradas de `habilidades[]` em
 * ancestralidades.json), indexado pelo mesmo id estável usado em
 * `RacialAbility.id`. Positivo = pontos que o traço custa pra "comprar" ao
 * montar uma Variante custom de raça; negativo = pontos que o traço devolve
 * ao orçamento por ser uma desvantagem — mesma lógica de
 * `AnaoCiberNegativeTrait.custo`, generalizada pra qualquer raça em vez de só
 * o catálogo curado dos Anões Ciber.
 *
 * Curadoria manual, traço a traço (não é fórmula por categoria): cada valor
 * foi julgado lendo a descrição real do traço, usando como âncora de
 * calibração os custos já aprovados em AnaoCiberNegativeTrait (ex.: Frágil =
 * -1, Tamanho -1 = -1, Transtorno de Separação = -2) e a severidade oficial
 * de Complicação quando o traço é uma (Menor = -1, Maior = -2). Escala usada
 * pros demais casos:
 *   +1  bônus pequeno/situacional (perícia começando em d4/d6, bônus estreito
 *       a um único tipo de rolagem, utilidade narrativa menor)
 *   +2  um passo de atributo (d4->d6), uma Vantagem concedida de graça, uma
 *       arma natural (For+d4), Resistência/Aparar/Armadura +1
 *   +3  efeito forte: Voo, Resistência +2, ou um pacote com vários efeitos
 *       combinados (ex.: pacote de morto-vivo/construto)
 *   +4  dois passos de atributo (d4->d8) ou pacote muito forte
 *   -1  Complicação Menor equivalente, ou uma única penalidade pontual leve
 *   -2  Complicação Maior equivalente, um passo de atributo pra baixo, ou
 *       penalidade combinada/mais severa
 *   -3/-4  pacote de desvantagens combinadas (mais de uma Complicação junto)
 *
 * Alguns ids ficam com custo 0 de propósito: são placeholders de Seleção, não
 * um efeito único e fixo — o jogador escolhe entre várias opções dentro do
 * próprio traço (ex.: "Dons da Natureza" do Umvee escolhe 1 de 6; "Signos de
 * Nascença" escolhe 1 de 13) e é a opção resolvida que carrega efeito
 * mecânico de verdade. Contar pontos no placeholder também contaria de novo
 * no traço injetado pela escolha — Terracota/Umvee/Elementais já são
 * resolvidos assim em AncestryVariantRegistry.
 */
object RacialTraitPointCatalog {

    val CUSTOS: Map<String, Int> = mapOf(
        "ACAO_ADICIONAL" to 2, // ignora 2 pts de penalidade de ações múltiplas
        "ACOES_ADICIONAIS" to 2, // mesmo efeito de Ação Adicional, outro id
        "ADAPTAVEL" to 2, // Vantagem Novato grátis à escolha
        "ADAPTAVEL_OU_SIGNO" to 2, // alternativa ao Adaptável (mesmo efeito)
        "AGIL" to 2, // Agilidade d4->d6
        "ALMOFADINHA" to -1, // Complicação (sem severidade explícita, trato como Menor)
        "ALTA_TECNOLOGIA" to -2, // Complicação Maior
        "ANALFABETO" to -1, // Complicação Menor
        "ANCESTRALIDADE_INFAME" to -2, // -2 Persuadir + reações sempre Não Cooperativas (categorizado positivo no JSON, mas o efeito é negativo)
        "ANDAR_NAS_PAREDES" to 1, // mobilidade situacional (superfícies verticais)
        "ANTECEDENTE_ARCANO_DEMONIO" to 4, // AA completo + poder inicial + 3 poderes extra + 10 PP
        "ANTECEDENTE_ARCANO_MILAGRES" to 3, // AA completo (Milagres)
        "APARAR" to 2, // Aparar +2
        "APARAR_BAIXO" to -2, // Aparar -2
        "APTIDAO_COM_PEDRAS" to 1, // Perceber +2 situacional (achar armadilhas/portas)
        "AQUATICO" to 2, // Movimentação completa na água, não se afoga
        "ARISCOS" to -2, // -2 Provocar (resistida) e -2 Intimidar
        "ARMADURA_2" to 2, // Armadura +2
        "ARMA_DE_SOPRO" to 3, // ataque de área 2d6, Evadível
        "ARROGANTE" to -2, // Complicação Maior
        "ARTICULACOES_LIMITADAS" to -1, // Movimentação 5, dado de corrida d4
        "AR_INTERNO" to 2, // não respira, imune a toxina inalada, não se afoga/sufoca
        "ASTUCIA" to 2, // Astúcia d4->d6
        "ASTUTO" to 2, // Astúcia d4->d6
        "ATRAENTE" to 2, // Vantagem Atraente grátis
        "AVERSAO_ANIMAL" to -1, // animais evitam + -2 pra controlar/montar animais
        "AZARADO" to -1, // Complicação Menor
        "BAIXA_TECNOLOGIA" to -2, // Complicação Maior
        "BEBEDOR_DE_SANGUE" to 1, // cura natural extra 1x/sessão, condicional
        "BOCA_GRANDE" to -1, // Complicação Menor
        "BOM_CONSELHEIRO" to -1, // Complicação Peculiaridade (Menor)
        "BRINCALHAO" to 1, // Provocar d4 (perícia básica)
        "BRINCANDO_COM_O_DESTINO" to 1, // Jogar d6 (perícia)
        "BRUTAL" to -1, // -1 Persuadir
        "CABECADA" to 2, // arma natural For+d4 (cabeçada dos Drakens)
        "CABECAS_DURAS" to -1, // -1 Astúcia
        "CABECA_DURA" to -1, // -1 Astúcia (Elementais)
        "CAES_DE_GUARDA" to 1, // Perceber d6 inicial
        "CALCULISTA" to 2, // Vantagem Calculista grátis
        "CAMPEAO" to 2, // Vantagem Campeão grátis
        "CANINOS" to 2, // arma natural For+d4 + Prender/Enredar
        "CARISMATICO" to 2, // Vantagem Carismático grátis
        "CASCA" to 1, // habilidade narrativa de nicho (possessores)
        "CASCOS" to 2, // arma natural For+d4
        "CHIFRES" to 2, // arma natural For+d4 (+4 se corrida)
        "CHI_REDUZIDO" to -1, // -1 na Reserva de Chi inicial
        "CIBER_RESISTENCIA" to -1, // Complicação Menor
        "CIRCUITOS_DE_ASIMOV" to -2, // Complicação Maior (Pacifista)
        "CODIGO_DE_HONRA" to -2, // Complicação Maior
        "COMUNITARIO" to 2, // +2 Espírito perto de outros da espécie
        "CONHECIMENTO_GERAL" to 1, // Conhecimento Geral d6 inicial
        "CONSTITUICAO_DE_FERRO" to 1, // +1 resistir veneno / recuperar de poderes
        "CONSTRUTO" to 3, // pacote: +2 recuperar Abalado, não respira, ignora 1 nível de Ferimento, imune veneno/doença (com a contrapartida de não curar naturalmente)
        "COVARDE" to -2, // Complicação Maior
        "CURIOSO" to -2, // Complicação Maior
        "DEFINIDO_PELO_OFICIO" to 1, // 1 perícia à escolha d6 inicial
        "DEPENDENCIA" to -2, // precisa de água ou sofre Fadiga progressiva até morrer
        "DEPENDENCIA_ATMOSFERICA" to -1, // dependência atmosférica (severidade não qualificada no JSON)
        "DESAGRADAVEL" to -1, // Complicação Menor
        "DESAJEITADO" to -2, // -2 Atletismo e Furtividade
        "DESASTRADO" to -1, // Complicação Menor
        "DESPRETENSIOSOS_E_BARRIGUDOS" to -2, // -1 Aparar, -1 Movimentação, corrida d4 (três penalidades)
        "DICAS_CULTURAIS" to 1, // Convenção d6 inicial
        "DIGESTAO_GLORIOSA" to 1, // imune a náusea/veneno/doença por ingestão
        "DIMINUTO" to 2, // Tamanho -4 com benefício de Modificadores de Escala (o JSON já trata como líquido positivo)
        "DONS_DA_NATUREZA" to 0, // placeholder de Seleção (Umvee/Feral escolhem 1 de 6 dons; o dom resolvido é que pontua)
        "DURAO" to 2, // Vigor d4->d6
        "EM_FORMA" to 2, // Vigor d4->d6
        "ENDURECIDO" to 2, // Força ou Vigor d4->d6 (escolha)
        "ESGUIOS" to -2, // -1 Resistência e -1 Vigor (duas penalidades)
        "ESPACIAL" to 2, // não respira, ignora descompressão/radiação
        "ESPIRITUAL" to 2, // Espírito d4->d6
        "ESPIRITUOSO" to 2, // Espírito d4->d6
        "ESQUISITICES" to -2, // duas Complicações Menores (Hábito + Peculiaridade)
        "ESTAVEL" to 1, // ignora penalidade de Terreno Difícil
        "EXCESSIVAMENTE_DETALHISTAS" to -1, // Complicação Cauteloso (Menor)
        "FE" to 1, // Fé d4->d6 (perícia)
        "FELIZES_POR_NATUREZA" to 2, // Espírito d4->d6
        "FEROCIDADE_ORC" to 2, // Resistência +1
        "FLEXIBILIDADE" to 2, // 1 atributo à escolha d4->d6
        "FOBIA" to -1, // Complicação Menor
        "FORASTEIRO" to -2, // Complicação Maior
        "FORCA_SOBRENATURAL" to 2, // Força d4->d6
        "FORMATO_CORPORAL_INCOMUM" to -1, // não usa roupas/armaduras/móveis padrão
        "FORMA_DE_ENERGIA" to 2, // pacote defensivo forte, mas não usa armadura/arma sem traje (substitui Forte na Seleção de elemento dos Elementais)
        "FORMA_INCOMUM" to -1, // não usa montarias/armaduras/itens não adaptados
        "FORTE" to 2, // Força d4->d6
        "FORTUNA_DA" to 2, // Bene extra por sessão
        "FRACO" to -1, // -1 Força e dano corpo a corpo
        "FRAGIL" to -1, // Resistência -1 (âncora: AnaoCiberNegativeTrait.fragil)
        "FRAQUEZA_AMBIENTAL" to -1, // -4 resistir efeito ambiental / +4 dano dele (âncora: AnaoCiberNegativeTrait.fraqueza_ambiental)
        "GANANCIOSO" to -1, // Complicação Menor
        "GARRAS" to 2, // arma natural For+d4, PA 2, +2 Atletismo (escalar)
        "GELATINOSO" to 2, // meio dano de queda/colisão, sem dano extra de Ataque Localizado, atravessa aberturas
        "GRANDE" to -1, // -2 Características com equipamento não adaptado
        "GUIADO" to -2, // Complicação Maior
        "HERANCA" to 2, // Vantagem Novato grátis OU Agilidade d4->d6 (escolha)
        "HERANCA_MISTA" to 2, // Vantagem Novato extra
        "IMPULSIVO" to -1, // Complicação Menor
        "IMUNE_A_DOENCAS_E_VENENOS" to 2, // imune a doença e veneno
        "INCAPAZ_DE_FALAR" to -2, // Complicação Maior
        "INFRAVISAO" to 1, // reduz pela metade penalidade de pouca luz contra alvos de calor/frio
        "INIMIGO_ANCESTRAL" to -1, // -2 Persuadir + hostilidade com cultura rival (âncora: Inimigo Racial)
        "INIMIGO_RACIAL" to -1, // idem
        "INIMIGO_RACIAL_DEMONIOS_E_DIABOS" to -1, // idem, escopo mais estreito
        "INSANIDADE" to 0, // concede Furioso (Vantagem) e Sanguinário (Complicação Maior) juntos - se cancelam no saldo
        "INTEGRADO_A_NATUREZA" to 1, // Sobrevivência d6 inicial
        "INTELIGENCIA" to 2, // Astúcia d4->d6
        "INTIMIDANTE" to 1, // Intimidar com teto ampliado (d12+1)
        "LEAL" to -1, // Complicação Leal (Menor)
        "LENTO" to -1, // -1 Movimentação, corrida d4
        "LIMITACOES_TECNICAS" to -1, // não aprende/canaliza Técnicas de Chi
        "MAGIA_ELFICA" to 1, // rerrolagem pra resistir a poderes
        "MAGIA_GNOMICA" to 2, // conjura Truques com PP próprio
        "MAL_HUMORADO" to -2, // Complicação Arrogante (efetivamente Maior)
        "MATILHA" to -1, // sujeito à Complicação Leal
        "MEMBROS_EXTRAS" to 2, // braços extras: +1 Agrupar com várias armas, +1 Atletismo (escalar/lutar)
        "MENTE_DE_COLMEIA" to -3, // duas Complicações juntas: Guiado (Maior) + Leal
        "MENTE_PRIMITIVA" to -1, // Astúcia travada em d6 na criação
        "METADE_CARNE" to 0, // só esclarece que ainda precisa comer/descansar (contraponto narrativo de Metade Construto, sem efeito mecânico próprio)
        "METADE_CONSTRUTO" to 3, // +3 Resistência, não respira, imune veneno/doença, sem dano extra de Ataque Localizado (com a contrapartida de não curar naturalmente)
        "MORDIDA" to 2, // arma natural For+d4
        "MORDIDAGARRAS" to 2, // arma natural For+d4
        "MORDIDA_GARRAS" to 2, // arma natural For+d4
        "MORDIDA_OU_GARRA" to 2, // arma natural For+d4
        "MORDIDA_VENENOSA" to 2, // mordida aplica veneno Moderado em Abalado/Ferido
        "MORTO_VIVO" to 3, // pacote morto-vivo: Resistência +2, +2 recuperar Abalado, sem dano extra de Ataque Localizado, ignora 1 nível de Ferimento, não respira, imune veneno/doença
        "MOVIMENTACAO" to 3, // Movimentação 10, corrida d10 (mesmo delta de Movimentação +4)
        "MOVIMENTACAO_2" to 2, // Movimentação +2, corrida +1 dado
        "MOVIMENTACAO_4" to 3, // Movimentação 10, corrida d10
        "MOVIMENTACAO_REDUZIDA" to -1, // -1 Movimentação, corrida reduzida um tipo
        "MUDAR_DE_FORMA" to 3, // Antecedente Arcano (Dom) completo + poder de disfarce como ação livre limitada
        "MUITO_FORTE" to 4, // Força d4->d8 (dois passos)
        "MUITO_RESISTENTE" to 4, // Vigor d4->d8 (dois passos)
        "NAO_PODE_CURAR" to -1, // não faz teste de cura natural
        "NAO_SABE_NADAR" to -1, // Complicação Menor
        "NATURALMENTE_SOBRENATURAL" to 1, // Ocultismo d4 inicial
        "NATUREZA_DIABOLICA" to 1, // +1 Intimidar
        "NERVOS_DE_ACO" to 2, // Vantagem Nervos de Aço grátis
        "NOCAO_DO_PERIGO" to 2, // Vantagem Noção do Perigo grátis
        "OBSESSIVOS" to 1, // 1 perícia de Astúcia à escolha d4 inicial
        "OBVIO" to -1, // -1 Furtividade
        "OPCAO_MAGICA" to 2, // acesso a escolher AA (Demônio) como Vantagem Novato
        "PACIFISTA" to -2, // Complicação Maior
        "PENSAMENTOS_POSITIVOS" to 2, // Vantagem Impulso grátis
        "PEQUENOS" to -1, // Tamanho/Resistência -1
        "PERICIAS_BASICAS_REDUZIDAS" to -2, // não começa com Conhecimento Geral, Persuadir nem Furtividade
        "PESFIRMES" to 1, // Atletismo d4->d6
        "PONTOS_DE_PERICIA" to 2, // +3 pontos de perícia iniciais
        "POUCO_IMPONENTE" to -1, // Complicação Almofadinha
        "PREPARADO" to 1, // 1 perícia da lista d4 inicial
        "PRIMITIVO" to 2, // Agilidade, Força ou Vigor (escolha) d4->d6
        "PROGRAMADO" to -2, // Complicação Maior
        "PRONTIDAO" to 2, // Vantagem Prontidão grátis
        "RAPIDO" to 2, // Vantagem Rápido grátis
        "REDUCAO_DE_SONO" to 1, // precisa de metade do sono
        "REFLEXOS_DE_COMBATE" to 3, // Vantagem Reflexos de Combate grátis + +2 Espírito recuperar Abalado
        "RESISTENCIA" to 2, // Resistência +1
        "RESISTENCIA_1" to 2, // Resistência +1
        "RESISTENCIA_2" to 3, // Resistência +2
        "RESISTENCIA_AMBIENTAL" to 2, // +4 resistir efeito do elemento ancestral / -4 dano dele
        "RESISTENCIA_AO_FRIO" to 2, // +4 resistir frio / -4 dano de frio
        "RESISTENCIA_NATURAL" to 2, // imune a veneno e doença
        "RESISTENTE" to 2, // Vigor d4->d6
        "ROBO" to 3, // pacote robô: não respira, imune doença/veneno, ignora descompressão/radiação (com a contrapartida de precisar ser consertado, não curado)
        "ROBUSTO" to 2, // Vigor d4->d6
        "RUDE" to -2, // -2 Persuadir
        "SANGUE_FRIO" to -2, // -1 Agilidade/Força/Vigor após tempo no frio (três atributos, situacional)
        "SANGUINARIO" to -2, // Complicação Maior
        "SEGREDO" to -2, // Complicação Maior
        "SEM_ESCRUPULOS" to -2, // Complicação Maior
        "SEM_INSTRUCAO" to -1, // -1 Astúcia
        "SEM_NOCAO" to -1, // -1 Conhecimento Geral e -1 Perceber
        "SEM_ORGAOS_VITAIS" to 1, // sem dano extra de Ataque Localizado
        "SEM_SANGUE" to 2, // estabiliza automaticamente ao Sangrar
        "SENSIBILIDADE_A_LUZ_SOLAR" to -2, // Estado Distraído sob luz solar sem proteção
        "SENSIVEL" to -2, // Complicação Maior
        "SENTIDOS_AGUCADOS" to 1, // Perceber d4->d6
        "SENTIDOS_APRIMORADOS" to 1, // Perceber d4->d6
        "SENTIDOS_APURADOS" to 1, // Perceber d4->d6
        "SIGNOS_DE_NASCENCA" to 0, // placeholder de Seleção (Humano Império San escolhe 1 de 13 signos; o signo resolvido é que pontua)
        "SOCIALMENTE_SOFISTICADOS" to 2, // Vantagem Cativar o Ambiente grátis
        "SOLIDO_COMO_ROCHA" to 2, // Vigor d4->d6
        "SORRATEIRO" to 1, // Furtividade d4->d6
        "SORTE" to 2, // Bene extra por sessão
        "SUCATEIRO" to 2, // Vantagem Sucateiro grátis
        "SUSPEITOSO" to -1, // Complicação Menor
        "TAMANHO_3" to 3, // Resistência +3
        "TAMANHO_MAIS_1" to 2, // Resistência +1
        "TAMANHO_MAIS_2" to 3, // Resistência +2
        "TAMANHO_MENOS_1" to -1, // Tamanho/Resistência -1 (âncora: AnaoCiberNegativeTrait.tamanho_menos_1)
        "TELEPATIA" to 2, // comunicação mental em 24m com qualquer ser inteligente
        "TRANSTORNO_DE_SEPARACAO" to -2, // -2 Espírito quando sozinho (âncora: AnaoCiberNegativeTrait.transtorno_separacao)
        "TRAPALHOES_TRAVESSOS" to 1, // Furtividade d4->d6
        "TREINADOS_PARA_A_GUERRA" to -1, // -4 Conhecimento Geral (uma perícia só)
        "TRIPAS_RESISTENTE" to 2, // +1 em várias rolagens (Medo, Absorção, resistir veneno/náusea)
        "VELOCIDADE_DA_LEBRE" to 2, // Movimentação +2, corrida +1 dado
        "VIGOROSO" to 2, // Vigor d4->d6
        "VIGOROSOS" to 2, // Resistência +1
        "VISAO_DE_360" to 1, // ignora 1 pt de bônus de Agrupar contra si
        "VISAO_NA_PENUMBRA" to 1, // ignora penalidade de Penumbra/Escuridão
        "VISAO_NO_ESCURO" to 1, // ignora penalidade de Penumbra/Escuridão
        "VISAO_TOTAL_NO_ESCURO" to 2, // ignora penalidade de iluminação até 20m
        "VOO" to 3, // voa com Movimentação 12
        "VOTO" to -2 // Complicação Maior
    )

    /** Custo em pontos do traço, pelo id (0 se não estiver no catálogo). */
    fun custoDe(id: String?): Int = id?.let { CUSTOS[it.keyify()] } ?: 0
}
