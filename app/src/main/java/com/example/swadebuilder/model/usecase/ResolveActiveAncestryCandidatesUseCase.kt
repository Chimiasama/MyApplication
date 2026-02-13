package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ids.ModuleIds

class ResolveActiveAncestryCandidatesUseCase {

    data class Flags(
        val compendioFantasiaAtivo: Boolean,
        val compendioHorrorAtivo: Boolean,
        val compendioArteDaGuerraAtivo: Boolean,
        val compendioDeadlandsAtivo: Boolean,
        val compendioWiseguysAtivo: Boolean,
        val compendioCidadeSolVaporAtivo: Boolean,
        val compendioCrystalHeartAtivo: Boolean,
        val compendioSciFiAtivo: Boolean,
        val compendioPathfinderAtivo: Boolean
    )

    fun isOriginActive(origin: String, flags: Flags): Boolean {
        return when (origin.uppercase()) {
            ModuleIds.FANTASIA -> flags.compendioFantasiaAtivo
            ModuleIds.HORROR -> flags.compendioHorrorAtivo
            ModuleIds.ARTE_DA_GUERRA -> flags.compendioArteDaGuerraAtivo
            ModuleIds.DEADLANDS -> flags.compendioDeadlandsAtivo
            ModuleIds.WISEGUYS -> flags.compendioWiseguysAtivo
            ModuleIds.CIDADE_SOL_VAPOR -> flags.compendioCidadeSolVaporAtivo
            ModuleIds.CRYSTAL_HEART -> flags.compendioCrystalHeartAtivo
            ModuleIds.SCI_FI_ALIAS_FC, ModuleIds.SCI_FI_ALIAS_SCIFI, ModuleIds.SCI_FI -> flags.compendioSciFiAtivo
            else -> {
                if (origin.contains("TRILHADOR", ignoreCase = true) || origin.contains(ModuleIds.PATHFINDER, ignoreCase = true)) {
                    flags.compendioPathfinderAtivo
                } else {
                    true
                }
            }
        }
    }
}
