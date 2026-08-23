package com.example.swadebuilder.util

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class CustomStorageManagerTest {

    private lateinit var storageManager: CustomStorageManager
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        storageManager = CustomStorageManager()
        tempDir = File(System.getProperty("java.io.tmpdir"), "swade_test_custom_storage_${System.currentTimeMillis()}")
        tempDir.mkdirs()
    }

    @Test
    fun testAddAndDeleteCustomVantagemPerBook() {
        val adv = Vantagem(
            id = "custom:super_pulo",
            nome = "Super Pulo",
            categoria = com.example.swadebuilder.model.Categoria.COMBATE,
            origem = "FANTASIA",
            requisitos = Requisito()
        )

        storageManager.addVantagem(tempDir, "FANTASIA", adv)
        val loadedFantasia = storageManager.loadCustomContent(tempDir, "FANTASIA")
        assertEquals(1, loadedFantasia.vantagens.size)
        assertEquals("Super Pulo", loadedFantasia.vantagens.first().nome)

        // Ensure book isolation
        val loadedSciFi = storageManager.loadCustomContent(tempDir, "SCI_FI")
        assertTrue(loadedSciFi.vantagens.isEmpty())

        // Delete from FANTASIA
        storageManager.deleteVantagem(tempDir, "FANTASIA", "custom:super_pulo")
        val loadedAfterDelete = storageManager.loadCustomContent(tempDir, "FANTASIA")
        assertTrue(loadedAfterDelete.vantagens.isEmpty())
    }

    @Test
    fun testAddAndImportCustomEquipamentoCrossBook() {
        val equip = EquipamentoItem(
            nome = "Espada Laser",
            custo = JsonPrimitive(600),
            peso = JsonPrimitive(3),
            dano = JsonPrimitive("For+d12+5"),
            origem = "SCI_FI",
            subtipo = "Corpo a Corpo"
        )

        storageManager.addEquipamento(tempDir, "SCI_FI", equip)
        val sciFiContent = storageManager.loadCustomContent(tempDir, "SCI_FI")
        assertEquals(1, sciFiContent.equipamentos.size)

        // Import into FANTASIA
        val imported = storageManager.importItemFromAnotherBook(
            baseDir = tempDir,
            targetBookKey = "FANTASIA",
            sourceBookKey = "SCI_FI",
            itemType = "equipamento",
            itemIdOrName = "Espada Laser"
        )
        assertTrue(imported)

        val fantasiaContent = storageManager.loadCustomContent(tempDir, "FANTASIA")
        assertEquals(1, fantasiaContent.equipamentos.size)
        assertEquals("FANTASIA", fantasiaContent.equipamentos.first().origem)

        // Deleting from SCI_FI does NOT delete from FANTASIA
        storageManager.deleteEquipamento(tempDir, "SCI_FI", "Espada Laser")
        assertTrue(storageManager.loadCustomContent(tempDir, "SCI_FI").equipamentos.isEmpty())
        assertFalse(storageManager.loadCustomContent(tempDir, "FANTASIA").equipamentos.isEmpty())
    }

    @Test
    fun testCorruptedFileFallbackReturnsEmptyContent() {
        val corruptedFile = File(tempDir, "custom_content_CORRUPTED.json")
        corruptedFile.writeText("{ invalid json ...")

        val result = storageManager.loadCustomContent(tempDir, "CORRUPTED")
        assertEquals("CORRUPTED", result.bookKey)
        assertTrue(result.vantagens.isEmpty())
    }
}
