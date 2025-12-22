package com.example.swadebuilder.util

import android.content.Context
import android.util.Log
import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
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

    suspend fun listSaves(context: Context): List<SaveEntry> = withContext(Dispatchers.IO) {
        val dir = savesDirectory(context)
        dir.listFiles()?.mapNotNull { file ->
            try {
                // Decodificação parcial ou completa segura
                val content = file.readText()
                val snap = json.decodeFromString<PersonagemSnapshot>(content)
                SaveEntry(file.nameWithoutExtension, snap.nome, snap.timestamp)
            } catch (e: Exception) {
                // Arquivo corrompido ou ilegível é ignorado na listagem
                null
            }
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    /**
     * Carrega um personagem do disco.
     * @throws SerializationException se o JSON estiver malformado.
     * @throws Exception para outros erros de I/O.
     */
    suspend fun load(context: Context, id: String): PersonagemSnapshot? = withContext(Dispatchers.IO) {
        try {
            val file = getSafeFile(context, id)
            if (!file.exists()) return@withContext null

            val content = file.readText()
            json.decodeFromString<PersonagemSnapshot>(content)
        } catch (e: SerializationException) {
            Log.e("CharacterStorage", "Erro de integridade ao decodificar JSON para ID $id", e)
            throw e // Repassa para o ViewModel tratar e exibir aviso amigável
        } catch (e: Exception) {
            Log.e("CharacterStorage", "Erro genérico ao carregar ID $id", e)
            null
        }
    }

    suspend fun save(context: Context, snapshot: PersonagemSnapshot): SaveEntry = withContext(Dispatchers.IO) {
        // Gera ID novo se estiver em branco
        val saveId = snapshot.id.ifBlank { UUID.randomUUID().toString() }
        val snapshotToSave = snapshot.copy(id = saveId)

        val targetFile = getSafeFile(context, saveId)
        val dir = savesDirectory(context)

        // 1. Gravar em arquivo temporário
        val tempFile = File(dir, "$saveId.json.tmp")

        try {
            val content = json.encodeToString(snapshotToSave)
            tempFile.writeText(content)

            // 2. Renomear atômico (ou delete + rename)
            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    throw java.io.IOException("Falha ao deletar arquivo antigo: ${targetFile.absolutePath}")
                }
            }

            if (!tempFile.renameTo(targetFile)) {
                throw java.io.IOException("Falha ao renomear arquivo temporário para oficial")
            }

        } catch (e: Exception) {
            // Limpeza em caso de erro
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e
        }

        SaveEntry(saveId, snapshotToSave.nome, snapshotToSave.timestamp)
    }

    suspend fun delete(context: Context, id: String) = withContext(Dispatchers.IO) {
        try {
            val file = getSafeFile(context, id)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Ignora erro se ID for inválido
        }
    }
}
