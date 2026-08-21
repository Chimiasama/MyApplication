package com.example.swadebuilder.util

import com.example.swadebuilder.model.CustomContentItem
import com.example.swadebuilder.model.CustomContentPackage
import com.example.swadebuilder.model.CustomContentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomContentManagerTest {

    private val manager = CustomContentManager()

    @Test
    fun testValidItemValidation() {
        val validItem = CustomContentItem(
            id = "custom:vantagem_magia_runica",
            name = "Magia Rúnica",
            type = CustomContentType.ADVANTAGE,
            description = "Permite conjurar magias gravando runas em objetos."
        )
        val result = manager.validateItem(validItem)
        assertTrue(result is CustomContentValidationResult.Valid)
    }

    @Test
    fun testInvalidNamespaceItemValidation() {
        val invalidItem = CustomContentItem(
            id = "vantagem_oficial",
            name = "Vantagem Oficial Falsa",
            type = CustomContentType.ADVANTAGE,
            description = "Sem namespace custom:"
        )
        val result = manager.validateItem(invalidItem)
        assertTrue(result is CustomContentValidationResult.Invalid)
        val invalid = result as CustomContentValidationResult.Invalid
        assertTrue(invalid.reason.contains("namespace"))
    }

    @Test
    fun testInvalidEmptyNameValidation() {
        val invalidItem = CustomContentItem(
            id = "fanmade:escudo_de_energia",
            name = "   ",
            type = CustomContentType.EQUIPMENT,
            description = "Escudo energético."
        )
        val result = manager.validateItem(invalidItem)
        assertTrue(result is CustomContentValidationResult.Invalid)
    }

    @Test
    fun testPackageExportAndImportRoundTrip() {
        val item = CustomContentItem(
            id = "custom:poder_teleporte_sombra",
            name = "Teleporte de Sombra",
            type = CustomContentType.POWER,
            description = "Permite mover-se instantaneamente entre sombras."
        )
        val pkg = CustomContentPackage(
            packageId = "pkg_magias_necro",
            packageName = "Pacote de Magias Negras",
            author = "Mestre da Mesa",
            items = listOf(item)
        )

        val jsonString = manager.exportPackageToJson(pkg)
        val importedResult = manager.importPackageFromJson(jsonString)

        assertTrue(importedResult.isSuccess)
        val importedPkg = importedResult.getOrThrow()
        assertEquals("pkg_magias_necro", importedPkg.packageId)
        assertEquals(1, importedPkg.items.size)
        assertEquals("custom:poder_teleporte_sombra", importedPkg.items.first().id)
    }

    @Test
    fun testImportInvalidPackageJsonFails() {
        val invalidJson = "{ \"packageId\": \"pkg_test\", \"packageName\": \"Test\", \"items\": [{ \"id\": \"invalid_id\", \"name\": \"Name\", \"type\": \"ADVANTAGE\", \"description\": \"Desc\" }] }"
        val result = manager.importPackageFromJson(invalidJson)
        assertTrue(result.isFailure)
    }
}
