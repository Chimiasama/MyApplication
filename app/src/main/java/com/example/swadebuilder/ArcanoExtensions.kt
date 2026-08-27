package com.example.swadebuilder

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.semAcentos

fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()

fun Vantagem.toArcanoKey(): String? {
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()

    val n = nome.normAAKey()

    // Fix for "Poderes Místicos": force MISTICO key even if choice (class) is present
    // to ensure fixed power lookup works correctly in CriadorState.
    if ("PODERES MISTICOS" in n || "FORCA SOMBRIA" in n) return "MISTICO"

    // Only use choice if this is actually the generic "Antecedente Arcano" edge.
    // Other edges like "Arma Predileta" use 'choice' for other purposes (e.g. Skill Name),
    // and should not return it as an Arcane Key.
    val isGenericAB = "ANTECEDENTE ARCANO" in n || id == "antecedente_arcano"

    if (isGenericAB && !choice.isNullOrBlank()) {
        val c = choice!!.normAAKey()
        // Map new (Básico) keys to standard keys
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

    return when {
        "MAGIA NEGRA" in n || "MAGIA DAS TREVAS" in n -> "FEITICEIRO"
        "(DOM" in n -> "DOM"
        "(MAGIA" in n -> "MAGIA"
        "(MILAGRES" in n -> "MILAGRES"
        ("(PSIONICOS" in n) -> "PSIONICOS"
        ("(CIENCIA ESTRANHA" in n) -> "CIENCIA ESTRANHA"
        "ABENCOADO" in n -> "ABENCOADO"
        "CIENTISTA LOUCO" in n -> "CIENTISTA LOUCO"
        "MESTRE DO CHI" in n -> "MESTRE DO CHI"
        "MASCATE" in n || "HUCKSTER" in n || "VIGARISTA" in n -> "MASCATE"
        "XAMA" in n -> "XAMA"
        "CANALIZAR CRISTAL" in n -> "CANALIZAR CRISTAL"
        "ALQUIMIA" in n -> "ALQUIMIA"
        "BARDO" in n -> "BARDO"
        "BRUXO" in n -> "BRUXO"
        "BRUXA" in n -> "BRUXA"
        "BRUXARIA" in n -> "FEITICARIA"
        "CLERIGO" in n -> "CLERIGO"
        "DIABOLISTA" in n -> "DIABOLISTA"
        "DRUIDA" in n -> "DRUIDA"
        "ELEMENTALISTA" in n -> "ELEMENTALISTA"
        "FEITICEIRO" in n -> "FEITICEIRO"
        "MAGO" in n -> "MAGO"
        "NECROMANTE" in n -> "NECROMANTE"
        "RITUALISTA" in n -> "RITUALISTA"
        "CLERIGO_PF" in n -> "CLERIGO_PF"
        "MILAGRES_PF" in n -> "MILAGRES_PF"
        "BARDO_PF" in n -> "BARDO_PF"
        "DRUIDA_PF" in n -> "DRUIDA_PF"
        "FEITICEIRO_PF" in n -> "FEITICEIRO_PF"
        "MAGO_PF" in n -> "MAGO_PF"
        "INVOCADOR DE DEMONIOS" in n -> "INVOCADOR DE DEMONIOS"
        "VIDENTE" in n -> "VIDENTE"
        "MISTICO" in n -> "MISTICO"
        "VODUISTA" in n || "VUDUISMO" in n -> "VODUISTA"
        "DEMONIO" in n -> "DEMONIO"
        // Antecedente Arcano Customizado (ver categoria "Antecedente Arcano" no
        // criador de conteúdo em SettingsDialog.kt): nenhum dos nomes oficiais
        // acima bateu, mas o padrão "ANTECEDENTE ARCANO (Nome)" ainda é
        // reconhecível — usa o nome entre parênteses como chave. Fica por
        // último de propósito, só serve de rede pra AAs que o jogador criou,
        // nunca compete com um nome oficial.
        isGenericAB && "(" in n && n.endsWith(")") -> n.substringAfter("(").removeSuffix(")").trim()
        else -> null
    }
}
