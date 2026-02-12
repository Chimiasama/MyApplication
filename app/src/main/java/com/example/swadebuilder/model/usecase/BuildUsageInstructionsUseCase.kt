package com.example.swadebuilder.model.usecase

class BuildUsageInstructionsUseCase {

    data class Input(
        val compendioFantasiaAtivo: Boolean,
        val compendioHorrorAtivo: Boolean,
        val compendioSciFiAtivo: Boolean,
        val compendioPathfinderAtivo: Boolean,
        val compendioDeadlandsAtivo: Boolean,
        val compendioCrystalHeartAtivo: Boolean,
        val compendioArteDaGuerraAtivo: Boolean,
        val compendioCidadeSolVaporAtivo: Boolean,
        val compendioWiseguysAtivo: Boolean,
        val modoSupers: Boolean,
        val modoMonstroAtivo: Boolean,
        val pathfinderLabel: String,
        val supersBookLabel: String,
        val monsterBookLabel: String
    )

    fun execute(input: Input): String {
        val activeBooks = buildList {
            add("Básico (sempre ativo)")
            if (input.compendioFantasiaAtivo) add("Compêndio Fantasia")
            if (input.compendioHorrorAtivo) add("Compêndio Horror")
            if (input.compendioSciFiAtivo) add("Compêndio Ficção Científica")
            if (input.compendioPathfinderAtivo) add("Compêndio ${input.pathfinderLabel}")
            if (input.compendioDeadlandsAtivo) add("Compêndio Deadlands")
            if (input.compendioCrystalHeartAtivo) add("Compêndio Crystal Heart")
            if (input.compendioArteDaGuerraAtivo) add("Compêndio Arte da Guerra")
            if (input.compendioCidadeSolVaporAtivo) add("Compêndio Cidade do Sol a Vapor")
            if (input.compendioWiseguysAtivo) add("Compêndio Wiseguys")
            if (input.modoSupers) add(input.supersBookLabel)
            if (input.modoMonstroAtivo) add(input.monsterBookLabel)
        }

        return buildString {
            appendLine("Compêndios ativos:")
            activeBooks.forEach { appendLine("• $it") }
            appendLine()
            appendLine("Dica: para ativar/desativar compêndios, use Configurações → Novo personagem.")
        }.trim()
    }
}
