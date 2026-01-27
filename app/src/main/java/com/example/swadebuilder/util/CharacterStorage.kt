package com.example.swadebuilder.util

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.IOException
import java.util.UUID

object CharacterStorage {
    private const val SAVE_DIR = "personagens"
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5 MB

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

    sealed class LoadResult {
        data class Success(val snapshot: PersonagemSnapshot) : LoadResult()
        data class Failure(val message: String) : LoadResult()
        data object NotFound : LoadResult()
    }

    private fun savesDirectory(context: Context): File {
        return File(context.filesDir, SAVE_DIR).apply { mkdirs() }
    }

    /**
     * Retorna um arquivo seguro, verificando se não há Path Traversal.
     * @throws SecurityException se o ID tentar sair do diretório de salvamento.
     */
    private fun getSafeFile(context: Context, id: String): File {
        return SecurityUtils.getSafeChildFile(savesDirectory(context), "$id.json")
    }

    private fun getEncryptedFile(context: Context, file: File): EncryptedFile {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun listSaves(context: Context): List<SaveEntry> = withContext(Dispatchers.IO) {
        val dir = savesDirectory(context)
        dir.listFiles()?.mapNotNull { file ->
            if (file.length() > MAX_FILE_SIZE) return@mapNotNull null

            // Try encrypted first
            var snapshot: PersonagemSnapshot? = null
            try {
                val encryptedFile = getEncryptedFile(context, file)
                encryptedFile.openFileInput().use { input ->
                    snapshot = json.decodeFromStream<PersonagemSnapshot>(input)
                }
            } catch (e: Exception) {
                // Fallback to plain text (legacy)
                try {
                    file.inputStream().use { input ->
                        snapshot = json.decodeFromStream<PersonagemSnapshot>(input)
                    }
                } catch (e2: Exception) {
                    return@mapNotNull null
                }
            }

            snapshot?.let {
                SaveEntry(file.nameWithoutExtension, it.nome, it.timestamp)
            }
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun load(context: Context, id: String): LoadResult = withContext(Dispatchers.IO) {
        try {
            val file = getSafeFile(context, id)
            if (!file.exists()) return@withContext LoadResult.NotFound
            if (file.length() > MAX_FILE_SIZE) {
                return@withContext LoadResult.Failure("Arquivo excede o limite de tamanho.")
            }

            var snapshot: PersonagemSnapshot? = null
            var migrated = false

            // 1. Try Encrypted Load
            try {
                val encryptedFile = getEncryptedFile(context, file)
                encryptedFile.openFileInput().use { input ->
                    snapshot = json.decodeFromStream<PersonagemSnapshot>(input)
                }
            } catch (e: Exception) {
                // 2. Fallback: Try Plain Text Load
                try {
                    file.inputStream().use { input ->
                        snapshot = json.decodeFromStream<PersonagemSnapshot>(input)
                    }
                    migrated = true
                } catch (e2: Exception) {
                    return@withContext LoadResult.Failure("Arquivo corrompido ou falha de integridade.")
                }
            }

            if (snapshot == null) return@withContext LoadResult.Failure("Erro desconhecido ao ler arquivo.")

            // 3. Auto-Migrate if needed
            if (migrated) {
                try {
                    save(context, snapshot!!)
                } catch (e: Exception) {
                    // Proceed even if migration save fails (rare)
                }
            }

            LoadResult.Success(snapshot!!)
        } catch (e: Exception) {
            LoadResult.Failure("Erro ao carregar personagem: ${e.message}")
        }
    }

    suspend fun save(context: Context, snapshot: PersonagemSnapshot): SaveEntry = withContext(Dispatchers.IO) {
        val saveId = snapshot.id.ifBlank { UUID.randomUUID().toString() }
        val file = getSafeFile(context, saveId)
        val tempFile = File(file.parentFile, "${file.name}.tmp")

        // Clean up stale temp file
        if (tempFile.exists()) tempFile.delete()

        val encryptedTemp = getEncryptedFile(context, tempFile)

        try {
            encryptedTemp.openFileOutput().use { output ->
                output.write(json.encodeToString(snapshot).toByteArray())
            }

            // Atomic replace
            if (file.exists()) {
                if (!file.delete()) throw IOException("Falha ao substituir arquivo antigo.")
            }
            if (!tempFile.renameTo(file)) {
                throw IOException("Falha ao renomear arquivo temporário.")
            }
        } catch (e: IOException) {
            if (tempFile.exists()) tempFile.delete()
            throw IllegalStateException("Falha ao salvar personagem criptografado.", e)
        }

        SaveEntry(saveId, snapshot.nome, snapshot.timestamp)
    }

    suspend fun delete(context: Context, id: String) = withContext(Dispatchers.IO) {
        try {
            val file = getSafeFile(context, id)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Ignora erro
        }
    }
}
