package com.example.swadebuilder.model

/**
 * Uma linha de ataque sintetizada a partir de um Super Poder de combate (Ataque Corpo a
 * Corpo / Ataque de Longa Distância) — usada tanto no Resumo do personagem (dentro do
 * app) quanto nas tabelas de armas do PDF exportado, mas NUNCA vira um EquipamentoItem
 * de verdade: não aparece na aba/página de Equipamentos, só nas listas de "Ataques".
 */
data class AtaqueDeSuperPoder(
    val nome: String,
    val dano: String,
    val pa: String = "-",
    val alcance: String = "-",
    val cdt: String = "-",
    val notas: String = ""
)

private fun notasDe(vararg partes: String?): String =
    partes.filterNotNull().filter { it.isNotBlank() }.joinToString(" • ")

private fun paDe(mods: Map<String, Int>, chave: String): String =
    mods[chave]?.let { "${(it * 2).coerceAtMost(20)}" } ?: "-"

/**
 * Dado de Chifres/Garras/Mordida — cada um usa sua própria escala de custo pro mesmo
 * progresso d4 -> d6 -> 2d6 (ver super_poderes.json, modificadores de "Ataque Corpo a
 * Corpo"): Chifres e Mordida custam 1/2/4, Garras custa 2/3/5.
 */
private fun dadoNatural(valor: Int, escalaD6: Int, escala2d6: Int): String = when (valor) {
    escalaD6 -> "d6"
    escala2d6 -> "2d6"
    else -> "d4"
}

/**
 * Extrai as linhas de "Ataque Corpo a Corpo" investidas pelo jogador nos Super Poderes —
 * ver o texto oficial do poder (super_poderes.json): o nível base (0/2/4/6 SP) aumenta o
 * dano desarmado de For pra For+d6/+2d6/+3d6, e cada modificador de arma natural (Chifres/
 * Garras/Mordida) ou Arma Especial é uma "forma de ataque" própria, com seu próprio dado
 * e seu próprio Perfurante de Armadura (ver split em 5 modificadores no catálogo).
 */
fun List<SuperInvestment>.ataquesCorpoACorpoDeSuperPoderes(): List<AtaqueDeSuperPoder> {
    val linhas = mutableListOf<AtaqueDeSuperPoder>()
    for (inv in this) {
        if (inv.displayName != "Ataque Corpo a Corpo") continue
        val mods = inv.modifiers
        val baseDados = when (inv.baseCost) {
            2 -> 1
            4 -> 2
            6 -> 3
            else -> 0
        }
        val baseSufixo = if (baseDados > 0) "+${baseDados}d6" else ""

        val notasGerais = notasDe(
            "Arma Pesada".takeIf { "Arma Pesada" in mods },
            "Contingente/Ligado".takeIf { "Contingente/Ligado" in mods },
            "Alternável".takeIf { "Alternável" in mods },
            "Característica Alternativa (Dificuldade sempre é o Aparar do alvo)".takeIf { "Característica Alternativa" in mods },
            "Carga (+1d6 na próxima rolagem ao avançar)".takeIf { "Carga" in mods },
            "Esmagar (ignora Armadura de objetos/veículos, fica Vulnerável)".takeIf { "Esmagar" in mods },
            "Dispositivo".takeIf { mods.keys.any { it.startsWith("Dispositivo") } }
        )

        val paBase = paDe(mods, "Perfurante de Armadura (Socos/Chutes)")
        if (baseDados > 0 || paBase != "-" || notasGerais.isNotBlank()) {
            linhas.add(
                AtaqueDeSuperPoder(
                    nome = "Ataque Corpo a Corpo",
                    dano = "For$baseSufixo",
                    pa = paBase,
                    notas = notasGerais
                )
            )
        }

        mods["Chifres"]?.let { valor ->
            val die = dadoNatural(valor, escalaD6 = 2, escala2d6 = 4)
            linhas.add(
                AtaqueDeSuperPoder(
                    nome = "Ataque Corpo a Corpo (Chifres)",
                    dano = "For$baseSufixo+$die",
                    pa = paDe(mods, "Perfurante de Armadura (Chifres)"),
                    notas = "+4 de dano se mover 5+ quadros antes de acertar"
                )
            )
        }

        mods["Garras"]?.let { valor ->
            val die = dadoNatural(valor, escalaD6 = 3, escala2d6 = 5)
            linhas.add(
                AtaqueDeSuperPoder(
                    nome = "Ataque Corpo a Corpo (Garras)",
                    dano = "For$baseSufixo+$die",
                    pa = paDe(mods, "Perfurante de Armadura (Garras)"),
                    notas = "+2 em Atletismo (escalar)"
                )
            )
        }

        mods["Mordida"]?.let { valor ->
            val die = dadoNatural(valor, escalaD6 = 2, escala2d6 = 4)
            linhas.add(
                AtaqueDeSuperPoder(
                    nome = "Ataque Corpo a Corpo (Mordida)",
                    dano = "For$baseSufixo+$die",
                    pa = paDe(mods, "Perfurante de Armadura (Mordida)")
                )
            )
        }

        mods["Arma especial"]?.let { valor ->
            // Acumula com o nível base do poder (mesma "moeda" d6) até o teto de +5d6
            // total (ver texto oficial) — o dado da arma empunhada em si (ex.: For+d8 de
            // uma espada longa) não entra aqui porque o app não sabe qual arma é.
            val dadosExtra = when (valor) {
                2 -> 1; 4 -> 2; 6 -> 3; 8 -> 4; 10 -> 5; else -> 1
            }
            val totalDados = (baseDados + dadosExtra).coerceAtMost(5)
            val notasArma = notasDe(
                "combina com o dado da arma empunhada",
                "Conjunto (vale pra duas armas)".takeIf { "Conjunto" in mods },
                "Retornável".takeIf { "Retornável" in mods },
                "Arremesso de armas".takeIf { "Arremesso de armas" in mods }
            )
            linhas.add(
                AtaqueDeSuperPoder(
                    nome = "Ataque Corpo a Corpo (Arma Especial)",
                    dano = "For+${totalDados}d6",
                    pa = paDe(mods, "Perfurante de Armadura (Arma Especial)"),
                    notas = notasArma
                )
            )
        }
    }
    return linhas
}

/**
 * Extrai as linhas de "Ataque de Longa Distância" investidas nos Super Poderes — ver
 * texto oficial (super_poderes.json): o custo (3/6/9/12/15 SP) define o dano (2d6 a 6d6)
 * e o Alcance-padrão é 12/24/48, dobrado ou triplicado pelo modificador Distância.
 */
fun List<SuperInvestment>.ataquesADistanciaDeSuperPoderes(): List<AtaqueDeSuperPoder> {
    val linhas = mutableListOf<AtaqueDeSuperPoder>()
    for (inv in this) {
        if (inv.displayName != "Ataque de Longa Distância") continue
        val mods = inv.modifiers
        val dano = when (inv.baseCost) {
            3 -> "2d6"; 6 -> "3d6"; 9 -> "4d6"; 12 -> "5d6"; 15 -> "6d6"
            else -> "2d6"
        }
        val alcance = when (mods["Distância"]) {
            2 -> "24/48/96"
            4 -> "36/72/144"
            else -> "12/24/48"
        }
        val cdt = when (mods["Cadência de tiro"]) {
            3 -> "2"; 6 -> "3"; else -> "-"
        }
        val pa = paDe(mods, "Perfurante de armadura")
        val notas = notasDe(
            "usa a perícia escolhida ao comprar o poder (Atletismo/Foco/Atirar/Conjurar)",
            "Área (Modelo Médio de Explosão)".takeIf { mods["Área"] == 2 },
            "Área (Modelo Médio ou Grande de Explosão)".takeIf { mods["Área"] == 4 },
            "Cone".takeIf { "Cone" in mods },
            "Espalhar (+2 no ataque, risco a Espectadores)".takeIf { "Espalhar" in mods },
            "Letal (sem ataque não letal)".takeIf { "Letal" in mods },
            "Requer material por perto".takeIf { "Requer material" in mods },
            "Arma Pesada".takeIf { "Arma Pesada" in mods },
            "Alternável".takeIf { "Alternável" in mods },
            "Dispositivo".takeIf { "Dispositivo" in mods },
            "Limitação".takeIf { "Limitação" in mods },
            "Seletivo (ignora aliados na área)".takeIf { "Seletivo" in mods },
            "Carga (+1d6 na próxima rolagem ao carregar)".takeIf { "Carga" in mods }
        )
        linhas.add(
            AtaqueDeSuperPoder(
                nome = "Ataque de Longa Distância",
                dano = dano,
                pa = pa,
                alcance = alcance,
                cdt = cdt,
                notas = notas
            )
        )
    }
    return linhas
}
