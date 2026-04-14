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
            appendLine("### Como usar o app")
            appendLine("• Navegue pelas abas na parte inferior (Características, Perícias, Vantagens, etc.) para montar seu personagem.")
            appendLine("• Os pontos de construção são calculados e validados automaticamente.")
            appendLine("• Na aba Resumo, você pode revisar todas as escolhas e exportar a ficha final em PDF.")
            appendLine()

            val hasSpecialInstructions = input.compendioPathfinderAtivo ||
                                         input.compendioCrystalHeartAtivo ||
                                         input.modoSupers ||
                                         input.compendioSciFiAtivo ||
                                         input.modoMonstroAtivo

            if (hasSpecialInstructions) {
                appendLine("### Instruções específicas dos compêndios ativos")
                if (input.compendioPathfinderAtivo) appendLine("• Pathfinder: As Classes de Prestígio funcionam como Vantagens.")
                if (input.compendioCrystalHeartAtivo) appendLine("• Crystal Heart: A instalação de Corações e Cristais afeta os dados de seus atributos e perícias automaticamente.")
                if (input.modoSupers) appendLine("• Supers: Ajuste o Nível de Campanha. Seus Pontos de Super Poderes podem ser gastos na aba de Poderes.")
                if (input.compendioSciFiAtivo) appendLine("• Ficção Científica: A instalação de Implantes Cibernéticos é limitada pela Tensão Máxima do personagem.")
                if (input.modoMonstroAtivo) appendLine("• Monstros: Os limites normais de pontos são ignorados para permitir a criação livre de criaturas personalizadas.")
                appendLine()
            }

            appendLine("### Compêndios ativos")
            activeBooks.forEach { appendLine("• $it") }
            appendLine()
            appendLine("Dica: para ativar/desativar compêndios, use Configurações → Novo personagem.")
        }.trim()
    }
}
