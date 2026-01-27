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

    private fun extensionFor(uri: Uri, context: Context): String {
        val mime = context.contentResolver.getType(uri).orEmpty().lowercase(Locale.ROOT)
        return when {
            mime.contains("png") -> ".png"
            mime.contains("webp") -> ".webp"
            mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
            else -> ".jpg"
        }
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

    suspend fun savePortrait(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        val dir = portraitsDirectory(context)
        val fileName = "portrait_${UUID.randomUUID()}${extensionFor(sourceUri, context)}"
        val destination = File(dir, fileName)

        try {
            val encryptedFile = getEncryptedFile(context, destination)

            // Write encrypted
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
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

            // Validate that the file is actually an image by trying to decode it
            try {
                encryptedFile.openFileInput().use { input ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(input, null, options)
                    if (options.outWidth == -1 || options.outHeight == -1) {
                        throw java.io.IOException("Arquivo não é uma imagem válida")
                    }
                }
            } catch (e: Exception) {
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

    suspend fun loadPortrait(context: Context, fileName: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val dir = portraitsDirectory(context)
            val file = SecurityUtils.getSafeChildFile(dir, fileName)
            if (!file.exists()) return@withContext null

            // 1. Try Encrypted
            try {
                val encryptedFile = getEncryptedFile(context, file)
                encryptedFile.openFileInput().use { input ->
                    return@withContext BitmapFactory.decodeStream(input)
                }
            } catch (e: Exception) {
                // 2. Fallback: Try Plain Text
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        // 3. Migrate: Overwrite with encrypted version
                        try {
                            val bytes = file.readBytes()
                            val tempFile = File(file.parentFile, "${file.name}.tmp")
                            if (tempFile.exists()) tempFile.delete()

                            val encryptedTemp = getEncryptedFile(context, tempFile)
                            encryptedTemp.openFileOutput().use { output ->
                                output.write(bytes)
                            }

                            if (file.delete()) {
                                tempFile.renameTo(file)
                            } else {
                                tempFile.delete()
                            }
                        } catch (e3: Exception) {
                            // Migration failed, but we have the bitmap
                        }
                        return@withContext bitmap
                    }
                } catch (e2: Exception) {
                    // Ignore
                }
            }
            null
        } catch (e: Exception) {
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
