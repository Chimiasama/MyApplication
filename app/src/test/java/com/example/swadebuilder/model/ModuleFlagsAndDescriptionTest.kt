package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.ids.ModuleIds
import com.example.swadebuilder.util.toEditionDisplayName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleFlagsAndDescriptionTest {

    private fun dummyFlags(
        modoSupers: Boolean = false,
        compendioFantasiaAtivo: Boolean = false,
        compendioHorrorAtivo: Boolean = false,
        compendioSciFiAtivo: Boolean = false,
        compendioPathfinderAtivo: Boolean = false,
        compendioDeadlandsAtivo: Boolean = false,
        compendioCrystalHeartAtivo: Boolean = false,
        compendioArteDaGuerraAtivo: Boolean = false,
        compendioCidadeSolVaporAtivo: Boolean = false,
        compendioWiseguysAtivo: Boolean = false,
        modoLivre: Boolean = false
    ): SnapshotFlags {
        val baseFlags = CriadorState().toSnapshot().flags
        return baseFlags.copy(
            modoSupers = modoSupers,
            compendioFantasiaAtivo = compendioFantasiaAtivo,
            compendioHorrorAtivo = compendioHorrorAtivo,
            compendioSciFiAtivo = compendioSciFiAtivo,
            compendioPathfinderAtivo = compendioPathfinderAtivo,
            compendioDeadlandsAtivo = compendioDeadlandsAtivo,
            compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
            compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
            compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
            compendioWiseguysAtivo = compendioWiseguysAtivo,
            modoLivre = modoLivre
        )
    }

    @Test
    fun `moduleKeysFromFlags retorna conjunto correto de modulos`() {
        val vm = CriadorViewModel()

        // Null flags -> Empty set (Básico)
        assertTrue(vm.moduleKeysFromFlags(null).isEmpty())

        // Default flags -> Empty set
        assertTrue(vm.moduleKeysFromFlags(dummyFlags()).isEmpty())

        // Pathfinder
        val pathfinderKeys = vm.moduleKeysFromFlags(dummyFlags(compendioPathfinderAtivo = true))
        assertEquals(setOf(ModuleIds.PATHFINDER), pathfinderKeys)

        // Supers + Sci-Fi
        val superSciFiKeys = vm.moduleKeysFromFlags(dummyFlags(modoSupers = true, compendioSciFiAtivo = true))
        assertEquals(setOf(ModuleIds.SCI_FI, ModuleIds.SUPER), superSciFiKeys)

        // Modo Livre -> Todos os módulos
        val freeModeKeys = vm.moduleKeysFromFlags(dummyFlags(modoLivre = true))
        assertTrue(freeModeKeys.containsAll(listOf(
            ModuleIds.FANTASIA, ModuleIds.HORROR, ModuleIds.SCI_FI,
            ModuleIds.PATHFINDER, ModuleIds.DEADLANDS, ModuleIds.CRYSTAL_HEART,
            ModuleIds.ARTE_DA_GUERRA, ModuleIds.CIDADE_SOL_VAPOR,
            ModuleIds.WISEGUYS, ModuleIds.SUPER
        )))
    }

    @Test
    fun `getModuleNamesDescription formata corretamente titulos dos livros e compendios`() {
        val vm = CriadorViewModel()

        val expectedBasico = "Básico".toEditionDisplayName()
        val expectedPathfinder = "Pathfinder".toEditionDisplayName()
        val expectedSupers = "Superpoderes".toEditionDisplayName()
        val expectedDeadlands = "Deadlands".toEditionDisplayName()
        val expectedFantasia = "Fantasia".toEditionDisplayName()
        val expectedHorror = "Horror".toEditionDisplayName()

        // Null ou Básico
        assertEquals(expectedBasico, vm.getModuleNamesDescription(null))
        assertEquals(expectedBasico, vm.getModuleNamesDescription(dummyFlags()))

        // Livros/Compêndios Individuais
        assertEquals(expectedPathfinder, vm.getModuleNamesDescription(dummyFlags(compendioPathfinderAtivo = true)))
        assertEquals(expectedSupers, vm.getModuleNamesDescription(dummyFlags(modoSupers = true)))
        assertEquals(expectedDeadlands, vm.getModuleNamesDescription(dummyFlags(compendioDeadlandsAtivo = true)))

        // Múltiplos Compêndios
        val descMultiple = vm.getModuleNamesDescription(
            dummyFlags(modoSupers = true, compendioFantasiaAtivo = true, compendioHorrorAtivo = true)
        )
        assertEquals("$expectedSupers, $expectedFantasia, $expectedHorror", descMultiple)
    }
}
