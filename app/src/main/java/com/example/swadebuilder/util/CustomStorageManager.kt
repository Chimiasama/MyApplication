package com.example.swadebuilder.util

import android.content.Context
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

import com.example.swadebuilder.model.HabilidadeCriacao

@Serializable
data class BookCustomContent(
    val bookKey: String,
    val vantagens: List<Vantagem> = emptyList(),
    val complicacoes: List<Complicacao> = emptyList(),
    val equipamentos: List<EquipamentoItem> = emptyList(),
    val poderes: List<Poder> = emptyList(),
    val racas: List<RacialModifier> = emptyList(),
    val habilidadesRaciais: List<HabilidadeCriacao> = emptyList()
)

class CustomStorageManager(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true }
) {
    private fun getFile(baseDir: File, bookKey: String): File {
        val safeKey = bookKey.uppercase().trim().replace(Regex("[^A-Z0-9_]"), "_")
        return File(baseDir, "custom_content_$safeKey.json")
    }

    fun loadCustomContent(baseDir: File, bookKey: String): BookCustomContent {
        return runCatching {
            val file = getFile(baseDir, bookKey)
            if (!file.exists()) return BookCustomContent(bookKey = bookKey.uppercase())
            val text = file.readText()
            json.decodeFromString(BookCustomContent.serializer(), text)
        }.getOrElse { BookCustomContent(bookKey = bookKey.uppercase()) }
    }

    fun loadCustomContent(context: Context, bookKey: String): BookCustomContent {
        return loadCustomContent(context.filesDir, bookKey)
    }

    fun saveCustomContent(baseDir: File, content: BookCustomContent) {
        runCatching {
            val file = getFile(baseDir, content.bookKey)
            val text = json.encodeToString(BookCustomContent.serializer(), content)
            file.writeText(text)
        }
    }

    fun saveCustomContent(context: Context, content: BookCustomContent) {
        saveCustomContent(context.filesDir, content)
    }

    fun addVantagem(baseDir: File, bookKey: String, item: Vantagem) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(
            vantagens = (current.vantagens.filterNot { it.id == item.id } + item)
        )
        saveCustomContent(baseDir, updated)
    }

    fun addVantagem(context: Context, bookKey: String, item: Vantagem) {
        addVantagem(context.filesDir, bookKey, item)
    }

    fun deleteVantagem(baseDir: File, bookKey: String, itemId: String) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(vantagens = current.vantagens.filterNot { it.id == itemId })
        saveCustomContent(baseDir, updated)
    }

    fun deleteVantagem(context: Context, bookKey: String, itemId: String) {
        deleteVantagem(context.filesDir, bookKey, itemId)
    }

    fun addComplicacao(baseDir: File, bookKey: String, item: Complicacao) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(
            complicacoes = (current.complicacoes.filterNot { it.id == item.id } + item)
        )
        saveCustomContent(baseDir, updated)
    }

    fun addComplicacao(context: Context, bookKey: String, item: Complicacao) {
        addComplicacao(context.filesDir, bookKey, item)
    }

    fun deleteComplicacao(baseDir: File, bookKey: String, itemId: String) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(complicacoes = current.complicacoes.filterNot { it.id == itemId })
        saveCustomContent(baseDir, updated)
    }

    fun deleteComplicacao(context: Context, bookKey: String, itemId: String) {
        deleteComplicacao(context.filesDir, bookKey, itemId)
    }

    fun addEquipamento(baseDir: File, bookKey: String, item: EquipamentoItem) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(
            equipamentos = (current.equipamentos.filterNot { it.nome.equals(item.nome, ignoreCase = true) } + item)
        )
        saveCustomContent(baseDir, updated)
    }

    fun addEquipamento(context: Context, bookKey: String, item: EquipamentoItem) {
        addEquipamento(context.filesDir, bookKey, item)
    }

    fun deleteEquipamento(baseDir: File, bookKey: String, itemNome: String) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(equipamentos = current.equipamentos.filterNot { it.nome.equals(itemNome, ignoreCase = true) })
        saveCustomContent(baseDir, updated)
    }

    fun deleteEquipamento(context: Context, bookKey: String, itemNome: String) {
        deleteEquipamento(context.filesDir, bookKey, itemNome)
    }

    fun addPoder(baseDir: File, bookKey: String, item: Poder) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(
            poderes = (current.poderes.filterNot { it.id == item.id } + item)
        )
        saveCustomContent(baseDir, updated)
    }

    fun addPoder(context: Context, bookKey: String, item: Poder) {
        addPoder(context.filesDir, bookKey, item)
    }

    fun deletePoder(baseDir: File, bookKey: String, itemId: String) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(poderes = current.poderes.filterNot { it.id == itemId })
        saveCustomContent(baseDir, updated)
    }

    fun deletePoder(context: Context, bookKey: String, itemId: String) {
        deletePoder(context.filesDir, bookKey, itemId)
    }

    fun addRaca(baseDir: File, bookKey: String, item: RacialModifier) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(
            racas = (current.racas.filterNot { it.nome.equals(item.nome, ignoreCase = true) } + item)
        )
        saveCustomContent(baseDir, updated)
    }

    fun addRaca(context: Context, bookKey: String, item: RacialModifier) {
        addRaca(context.filesDir, bookKey, item)
    }

    fun deleteRaca(baseDir: File, bookKey: String, itemNome: String) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(racas = current.racas.filterNot { it.nome.equals(itemNome, ignoreCase = true) })
        saveCustomContent(baseDir, updated)
    }

    fun deleteRaca(context: Context, bookKey: String, itemNome: String) {
        deleteRaca(context.filesDir, bookKey, itemNome)
    }

    fun addHabilidadeRacial(baseDir: File, bookKey: String, item: HabilidadeCriacao) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(
            habilidadesRaciais = (current.habilidadesRaciais.filterNot { it.nome.equals(item.nome, ignoreCase = true) } + item)
        )
        saveCustomContent(baseDir, updated)
    }

    fun addHabilidadeRacial(context: Context, bookKey: String, item: HabilidadeCriacao) {
        addHabilidadeRacial(context.filesDir, bookKey, item)
    }

    fun deleteHabilidadeRacial(baseDir: File, bookKey: String, itemNome: String) {
        val current = loadCustomContent(baseDir, bookKey)
        val updated = current.copy(habilidadesRaciais = current.habilidadesRaciais.filterNot { it.nome.equals(itemNome, ignoreCase = true) })
        saveCustomContent(baseDir, updated)
    }

    fun deleteHabilidadeRacial(context: Context, bookKey: String, itemNome: String) {
        deleteHabilidadeRacial(context.filesDir, bookKey, itemNome)
    }

    fun importItemFromAnotherBook(baseDir: File, targetBookKey: String, sourceBookKey: String, itemType: String, itemIdOrName: String): Boolean {
        if (targetBookKey.equals(sourceBookKey, ignoreCase = true)) return false
        val sourceContent = loadCustomContent(baseDir, sourceBookKey)

        when (itemType.lowercase()) {
            "vantagem" -> {
                val item = sourceContent.vantagens.firstOrNull { it.id == itemIdOrName } ?: return false
                addVantagem(baseDir, targetBookKey, item.copy(origem = targetBookKey.uppercase()))
            }
            "complicação", "complicacao" -> {
                val item = sourceContent.complicacoes.firstOrNull { it.id == itemIdOrName } ?: return false
                addComplicacao(baseDir, targetBookKey, item.copy(origem = targetBookKey.uppercase()))
            }
            "equipamento" -> {
                val item = sourceContent.equipamentos.firstOrNull { it.nome.equals(itemIdOrName, ignoreCase = true) } ?: return false
                addEquipamento(baseDir, targetBookKey, item.copy(origem = targetBookKey.uppercase()))
            }
            "poder" -> {
                val item = sourceContent.poderes.firstOrNull { it.id == itemIdOrName } ?: return false
                addPoder(baseDir, targetBookKey, item.copy(origem = targetBookKey.uppercase()))
            }
            "raça", "raca" -> {
                val item = sourceContent.racas.firstOrNull { it.nome.equals(itemIdOrName, ignoreCase = true) } ?: return false
                addRaca(baseDir, targetBookKey, item.copy(origem = targetBookKey.uppercase()))
            }
            else -> return false
        }
        return true
    }

    fun importItemFromAnotherBook(context: Context, targetBookKey: String, sourceBookKey: String, itemType: String, itemIdOrName: String): Boolean {
        return importItemFromAnotherBook(context.filesDir, targetBookKey, sourceBookKey, itemType, itemIdOrName)
    }
}
