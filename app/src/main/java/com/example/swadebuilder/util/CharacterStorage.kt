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

            val metadata = decodeMetadataSafely(context, file, masterKey)
                ?: decodeSnapshotSafely(context, file, masterKey)
                    ?.takeIf { validateChecksum(it) }
                    ?.let {
                        MetadataSnapshot(
                            version = it.version,
                            id = it.id,
                            nome = it.nome,
                            timestamp = it.timestamp,
                            checksum = it.checksum
                        )
                    }
                ?: return@mapNotNull null

            SaveEntry(file.nameWithoutExtension, metadata.nome, metadata.timestamp)
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun decodeMetadataSafely(context: Context, file: File, masterKey: MasterKey): MetadataSnapshot? {
        // 1. Try Plaintext (Preferred)
        try {
            file.inputStream().use { input ->
                return json.decodeFromStream<MetadataSnapshot>(input)
            }
        } catch (e: Exception) {
            // If failed, fall through to encrypted check
        }

        // 2. Try Encrypted (Legacy)
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
            // Both failed
        }
        return null
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun decodeSnapshotSafely(context: Context, file: File, masterKey: MasterKey): PersonagemSnapshot? {
        // 1. Try Plaintext (Preferred)
        try {
            file.inputStream().use { input ->
                return json.decodeFromStream<PersonagemSnapshot>(input)
            }
        } catch (e: Exception) {
            // If failed, fall through to encrypted check
        }

        // 2. Try Encrypted (Legacy)
        try {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            return encryptedFile.openFileInput().use { input ->
                json.decodeFromStream<PersonagemSnapshot>(input)
            }
        } catch (e: Exception) {
            // Both failed
        }
        return null
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

            // 1. Try Plaintext (Preferred)
            try {
                snapshot = file.inputStream().use { input ->
                    json.decodeFromStream<PersonagemSnapshot>(input)
                }
            } catch (e: Exception) {
                // Ignore and try encrypted
            }

            // 2. Try Encrypted (Legacy) if not loaded yet
            if (snapshot == null) {
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
                } catch (e: Exception) {
                    // Ignore
                }
            }

            if (snapshot == null) {
                return@withContext LoadResult.Failure(
                    "Arquivo corrompido ou inválido. Tente salvar novamente."
                )
            }

            if (!validateChecksum(snapshot)) {
                return@withContext LoadResult.Failure(
                    "Falha na verificação de integridade do personagem."
                )
            }

            LoadResult.Success(snapshot)
        } catch (e: Exception) {
            LoadResult.Failure("Erro ao carregar personagem.")
        }
    }

    suspend fun save(context: Context, snapshot: PersonagemSnapshot): SaveEntry = withContext(Dispatchers.IO) {
        val saveId = snapshot.id.ifBlank { UUID.randomUUID().toString() }
        val file = getSafeFile(context, saveId)

        val snapshotForChecksum = snapshot.copy(id = saveId, checksum = null)
        val snapshotToSave = snapshotForChecksum.copy(checksum = checksumFor(snapshotForChecksum))

        val tempFile = File(file.parentFile, "${file.nameWithoutExtension}_temp.json")
        if (tempFile.exists()) tempFile.delete()

        // Write plaintext to temp file
        tempFile.outputStream().use { output ->
            val jsonString = json.encodeToString(snapshotToSave)
            output.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        // Atomic Rename
        if (file.exists()) {
            file.delete()
        }
        if (!tempFile.renameTo(file)) {
            // Try explicit copy and delete if rename fails
            try {
                tempFile.copyTo(file, overwrite = true)
                tempFile.delete()
            } catch (e: Exception) {
                throw IllegalStateException("Falha ao finalizar salvamento.")
            }
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
