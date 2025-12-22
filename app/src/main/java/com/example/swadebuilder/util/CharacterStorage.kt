package com.example.swadebuilder.util

import android.content.Context
import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

object CharacterStorage {
    private const val SAVE_DIR = "personagens"

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    data class SaveEntry(
        val id: String,
        val nome: String,
        val timestamp: Long
    )

    private fun savesDirectory(context: Context): File {
        return File(context.filesDir, SAVE_DIR).apply { mkdirs() }
    }

    /**
     * Retorna um arquivo seguro, verificando se não há Path Traversal.
     * @throws SecurityException se o ID tentar sair do diretório de salvamento.
     */
    private fun getSafeFile(context: Context, id: String): File {
        val dir = savesDirectory(context)
        // Sanitização básica extra
        if (id.contains(File.separator) || id.contains("/") || id.contains("\\")) {
             throw IllegalArgumentException("ID de arquivo inválido: $id")
        }

        val file = File(dir, "$id.json")

        // Verificação canônica robusta (Path Traversal Protection)
        if (!file.canonicalPath.startsWith(dir.canonicalPath)) {
            throw SecurityException("Tentativa de Path Traversal detectada: $id")
        }

        return file
    }

    fun listSaves(context: Context): List<SaveEntry> {
        val dir = savesDirectory(context)
        return dir.listFiles()?.mapNotNull { file ->
            runCatching {
                val snap = json.decodeFromString<PersonagemSnapshot>(file.readText())
                SaveEntry(file.nameWithoutExtension, snap.nome, snap.timestamp)
            }.getOrNull()
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun load(context: Context, id: String): PersonagemSnapshot? {
        return try {
            val file = getSafeFile(context, id)
            if (!file.exists()) return null
            runCatching {
                json.decodeFromString<PersonagemSnapshot>(file.readText())
            }.getOrNull()
        } catch (e: Exception) {
            // Retorna null se houver erro de segurança ou IO, tratando como arquivo não encontrado/inválido
            null
        }
    }

    fun save(context: Context, snapshot: PersonagemSnapshot): SaveEntry {
        // Gera ID novo se estiver em branco
        val saveId = snapshot.id.ifBlank { UUID.randomUUID().toString() }

        // Obtém arquivo seguro
        val file = getSafeFile(context, saveId)

        file.writeText(json.encodeToString(snapshot))
        return SaveEntry(saveId, snapshot.nome, snapshot.timestamp)
    }

    fun delete(context: Context, id: String) {
        try {
            val file = getSafeFile(context, id)
            if (file.exists()) {
                // Tenta carregar antes de deletar pra pegar a imagem associada
                val snapshot = runCatching {
                    json.decodeFromString<PersonagemSnapshot>(file.readText())
                }.getOrNull()

                if (snapshot?.fotoCaminho != null) {
                    ImageStorage.deleteImage(context, snapshot.fotoCaminho)
                }

                file.delete()
            }
        } catch (e: Exception) {
            // Ignora erro se ID for inválido (nada a deletar)
        }
    }
}
