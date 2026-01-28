package com.example.swadebuilder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

object CharacterPortraitStorage {
    private const val PORTRAIT_DIR = "portraits"
    private const val MAX_PORTRAIT_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB

    private fun portraitsDirectory(context: Context): File {
        return File(context.filesDir, PORTRAIT_DIR).apply { mkdirs() }
    }

    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private fun extensionFor(uri: Uri, context: Context): String {
        val mime = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.ROOT)
        return when {
            mime.contains("png") -> ".png"
            mime.contains("webp") -> ".webp"
            mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
            else -> ".jpg"
        }
    }

    suspend fun loadPortrait(context: Context, fileName: String): Bitmap? = withContext(Dispatchers.IO) {
        val dir = portraitsDirectory(context)
        val file = try {
            SecurityUtils.getSafeChildFile(dir, fileName)
        } catch (e: Exception) {
            return@withContext null
        }

        if (!file.exists()) return@withContext null

        // 1. Try Encrypted
        try {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                getMasterKey(context),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            return@withContext encryptedFile.openFileInput().use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            // Ignore, try plaintext
        }

        // 2. Try Plaintext (Legacy)
        return@withContext try {
             BitmapFactory.decodeFile(file.absolutePath)?.also { bitmap ->
                 // Migration: Re-save encrypted
                 // We need to write back the bitmap as encrypted.
                 // This is complex because we need to know the format (PNG/JPG).
                 // We can infer from extension.
                 // Since we have the bitmap, we can compress it back.
                 // Note: this might lose quality for JPG.
                 // Alternative: Read bytes of plaintext file and write to temp encrypted, then rename.
                 migrateToEncrypted(context, file)
             }
        } catch (e: Exception) {
            null
        }
    }

    private fun migrateToEncrypted(context: Context, file: File) {
        try {
            val tempFile = File(file.parentFile, "${file.name}_temp")
            val masterKey = getMasterKey(context)
            val encryptedTemp = EncryptedFile.Builder(
                context,
                tempFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedTemp.openFileOutput().use { output ->
                file.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            if (file.delete()) {
                tempFile.renameTo(file)
            } else {
                tempFile.delete()
            }
        } catch (e: Exception) {
            // Migration failed, keep plaintext
        }
    }

    suspend fun savePortrait(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        val dir = portraitsDirectory(context)
        val fileName = "portrait_${UUID.randomUUID()}${extensionFor(sourceUri, context)}"
        val destination = File(dir, fileName)

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val masterKey = getMasterKey(context)
                val encryptedFile = EncryptedFile.Builder(
                    context,
                    destination,
                    masterKey,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()

                encryptedFile.openFileOutput().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var totalBytes = 0L
                    var bytesRead = input.read(buffer)
                    while (bytesRead >= 0) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                        if (totalBytes > MAX_PORTRAIT_SIZE_BYTES) {
                            throw java.io.IOException("Imagem excede o limite de 10MB")
                        }
                        bytesRead = input.read(buffer)
                    }
                }
            } ?: return@withContext null

            // Validate that the file is actually an image
            // We need to read it back via EncryptedFile to validate
            val isValid = try {
                 loadPortrait(context, fileName) != null
            } catch (e: Exception) { false }

            if (!isValid) {
                if (destination.exists()) destination.delete()
                return@withContext null
            }

            fileName
        } catch (e: Exception) {
            if (destination.exists()) {
                destination.delete()
            }
            null
        }
    }

    suspend fun deleteIfUnused(
        context: Context,
        fileName: String,
        excludingSaveIds: Set<String> = emptySet()
    ) = withContext(Dispatchers.IO) {
        val referenced = CharacterStorage.listSaves(context).any { entry ->
            if (entry.id in excludingSaveIds) {
                return@any false
            }
            when (val result = CharacterStorage.load(context, entry.id)) {
                is CharacterStorage.LoadResult.Success ->
                    result.snapshot.selecoes.retratoFileName == fileName
                else -> false
            }
        }

        if (referenced) return@withContext

        try {
            val file = SecurityUtils.getSafeChildFile(portraitsDirectory(context), fileName)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Ignora se o arquivo for inválido ou tentar path traversal
        }
    }
}
