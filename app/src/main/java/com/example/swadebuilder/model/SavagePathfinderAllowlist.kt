package com.example.swadebuilder.model

val SAVAGE_PATHFINDER_ALLOWLIST = setOf(
    // A AllowList foi substituida pelo arquivo equipamentos_trilhador.json que contem
    // as entradas com custos e pesos especificos para o cenario.
    // Manter itens aqui duplicaria a exibicao (versao Basica + versao Pathfinder).
    // O filtro em EquipamentoSection permite automaticamente itens com origem FANTASIABUSCATRILHA.

    // Materiais Especiais (ja estao no JSON com origem correta, removidos daqui para evitar duplicidade se existirem no Basico)
    // "Adamante", "Madeira Negra", "Couro de Dragão", "Ferro Frio", "Mithral", "Prata Alquímica"
)

val SAVAGE_PATHFINDER_BLOCKED_SKILLS = setOf(
    "PSIONICOS",
    "ELETRONICA",
    "HACKEAR",
    "CIENCIA"
)
