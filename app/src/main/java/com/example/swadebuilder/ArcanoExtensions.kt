package com.example.swadebuilder

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.semAcentos

fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()

/**
 * Chave do Antecedente Arcano que esta Vantagem identifica/pertence, se
 * houver. `subtipoArcano` é a fonte de verdade — dado de catálogo
 * (vantagens.json), preenchido pra toda vantagem-de-AA e vantagem
 * ligada-a-AA conhecida (ex.: "Poderes Favorecidos (Mago)" -> "MAGO"),
 * exatamente pra não precisar adivinhar por texto do nome aqui.
 *
 * Só sobra lógica em código pros dois casos que não dá pra pré-computar
 * num campo estático do catálogo:
 * - O "Antecedente Arcano" genérico (id `antecedente_arcano`, sem sufixo no
 *   nome): o jogador escolhe o AA na hora de criar o personagem, então a
 *   chave só existe na escolha (`choice`) daquela cópia da vantagem, nunca
 *   no catálogo.
 * - Antecedente Arcano Customizado (criado pelo mestre no conteúdo
 *   customizado, ver SettingsDialog.kt): não existe no catálogo pra ganhar
 *   `subtipoArcano`, mas segue o padrão "ANTECEDENTE ARCANO (Nome)" — extrai
 *   o nome entre parênteses como chave.
 */
fun Vantagem.toArcanoKey(): String? {
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()

    val n = nome.normAAKey()

    // Só usa 'choice' se esta for de fato a vantagem genérica "Antecedente
    // Arcano" — outras vantagens (ex.: "Arma Predileta") usam 'choice' pra
    // outra coisa (nome de perícia etc.) e não devem virar chave de AA.
    val isGenericAB = "ANTECEDENTE ARCANO" in n || id == "antecedente_arcano"

    if (isGenericAB && !choice.isNullOrBlank()) {
        val c = choice!!.normAAKey()
        // Mapeia as chaves novas (Básico) pras chaves padrão.
        return when {
            "DOM BASICO" in c -> "DOM"
            "MAGIA BASICO" in c -> "MAGIA"
            "MILAGRES BASICO" in c -> "MILAGRES"
            "PSIONICOS BASICO" in c -> "PSIONICOS"
            "CIENCIA ESTRANHA BASICO" in c -> "CIENCIA ESTRANHA"
            "VUDUISMO" in c -> "VODUISTA"
            else -> c
        }
    }

    if (isGenericAB && "(" in n && n.endsWith(")")) {
        return n.substringAfter("(").removeSuffix(")").trim()
    }

    return null
}
