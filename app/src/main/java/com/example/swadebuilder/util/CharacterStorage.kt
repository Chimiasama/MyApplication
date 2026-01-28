package com.example.swadebuilder.util

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.MessageDigest
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

    @Serializable
    private data class MetadataSnapshot(
        val version: Int = 1,
        val id: String,
        val nome: String,
        val timestamp: Long,
        val checksum: String? = null
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

    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private fun checksumFor(snapshot: PersonagemSnapshot): String {
        val payload = json.encodeToString(snapshot.copy(checksum = null))
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun validateChecksum(snapshot: PersonagemSnapshot): Boolean {
        // For new save files (version 2+), checksum is mandatory to prevent tampering
        if (snapshot.version >= 2 && snapshot.checksum == null) {
            return false
        }
        val expected = snapshot.checksum ?: return true
        return expected == checksumFor(snapshot)
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun listSaves(context: Context): List<SaveEntry> = withContext(Dispatchers.IO) {
        val dir = savesDirectory(context)
        val masterKey = getMasterKey(context)

        dir.listFiles()?.mapNotNull { file ->
            if (file.length() > MAX_FILE_SIZE) return@mapNotNull null

            // Try decoding metadata, handling both encrypted and plaintext
            val snapshot = decodeMetadataSafely(context, file, masterKey) ?: return@mapNotNull null

            SaveEntry(file.nameWithoutExtension, snapshot.nome, snapshot.timestamp)
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun decodeMetadataSafely(context: Context, file: File, masterKey: MasterKey): MetadataSnapshot? {
        // 1. Try Encrypted
        try {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            return encryptedFile.openFileInput().use { input ->
                json.decodeFromStream<MetadataSnapshot>(input)
            }
        } catch (e: Exception) {
            // If failed (not encrypted or key mismatch), fall through to plaintext check
        }

        // 2. Try Plaintext (Legacy)
        return try {
            file.inputStream().use { input ->
                json.decodeFromStream<MetadataSnapshot>(input)
            }
        } catch (e: Exception) {
            null
        }
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
            var wasEncrypted = false

            // 1. Try Encrypted
            try {
                val encryptedFile = EncryptedFile.Builder(
                    context,
                    file,
                    getMasterKey(context),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()

                snapshot = encryptedFile.openFileInput().use { input ->
                    json.decodeFromStream<PersonagemSnapshot>(input)
                }
                wasEncrypted = true
            } catch (e: Exception) {
                // Ignore and try plaintext
            }

            // 2. Try Plaintext if not loaded yet
            if (snapshot == null) {
                try {
                    snapshot = file.inputStream().use { input ->
                        json.decodeFromStream<PersonagemSnapshot>(input)
                    }
                } catch (e: SerializationException) {
                    return@withContext LoadResult.Failure(
                        "Arquivo corrompido ou inválido. Tente salvar novamente."
                    )
                }
            }

            if (snapshot == null) return@withContext LoadResult.Failure("Erro desconhecido ao ler arquivo.")

            if (!validateChecksum(snapshot)) {
                return@withContext LoadResult.Failure(
                    "Falha na verificação de integridade do personagem."
                )
            }

            // Migration: If it was plaintext, save it as encrypted immediately
            if (!wasEncrypted) {
                save(context, snapshot)
            }

            LoadResult.Success(snapshot)
        } catch (e: Exception) {
            LoadResult.Failure("Erro ao carregar personagem.")
        }
    }

    suspend fun save(context: Context, snapshot: PersonagemSnapshot): SaveEntry = withContext(Dispatchers.IO) {
        val saveId = snapshot.id.ifBlank { UUID.randomUUID().toString() }
        val file = getSafeFile(context, saveId)

        val snapshotForChecksum = snapshot.copy(checksum = null)
        val snapshotToSave = snapshotForChecksum.copy(checksum = checksumFor(snapshotForChecksum))
        // We still encode to string to verify size/payload, but we write via stream
        // Actually, EncryptedFile gives an OutputStream. We should write directly to it.
        // But for atomic save, we need a temp file.

        val tempFile = File(file.parentFile, "${file.nameWithoutExtension}_temp.json")
        if (tempFile.exists()) tempFile.delete()

        val masterKey = getMasterKey(context)
        val encryptedTemp = EncryptedFile.Builder(
            context,
            tempFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        // Write encrypted to temp file
        encryptedTemp.openFileOutput().use { output ->
            // encoding directly to stream
            val jsonString = json.encodeToString(snapshotToSave)
            output.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        // Atomic Rename
        if (file.exists()) {
            file.delete()
        }
        if (!tempFile.renameTo(file)) {
            throw IllegalStateException("Falha ao finalizar salvamento atômico.")
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
            // Ignora erro se ID for inválido (nada a deletar)
        }
    }
}
