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

    // androidx.security.crypto (MasterKey/EncryptedFile) foi descontinuada pelo Google sem um
    // substituto direto. Usada aqui só para LER retratos criptografados por versões antigas do
    // app — todo salvamento novo já é em texto puro (ver "1. Try Plaintext (Preferred)" abaixo).
    // Migrar arriscaria quebrar a leitura de retratos já salvos, então mantemos o uso legado
    // suprimindo o aviso em vez de trocar a implementação.
    @Suppress("DEPRECATION")
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

    @Suppress("DEPRECATION")
    suspend fun loadPortrait(
        context: Context,
        fileName: String,
        targetWidth: Int? = null
    ): Bitmap? = withContext(Dispatchers.IO) {
        val dir = portraitsDirectory(context)
        val file = try {
            SecurityUtils.getSafeChildFile(dir, fileName)
        } catch (e: Exception) {
            return@withContext null
        }

        if (!file.exists()) return@withContext null

        val options = if (targetWidth != null) {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            val opts = BitmapFactory.Options()
            var scale = 1
            if (boundsOptions.outWidth > targetWidth) {
                scale = boundsOptions.outWidth / targetWidth
            }
            opts.inSampleSize = scale
            opts
        } else null

        // 1. Try Plaintext (Preferred)
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            if (bitmap != null) return@withContext bitmap
        } catch (e: Exception) {
            // Ignore
        }

        // 2. Try Encrypted (Legacy)
        try {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                getMasterKey(context),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            val opts = if (targetWidth != null) {
                // First pass for bounds
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                encryptedFile.openFileInput().use { input ->
                    BitmapFactory.decodeStream(input, null, boundsOpts)
                }
                val finalOpts = BitmapFactory.Options()
                var scale = 1
                if (boundsOpts.outWidth > targetWidth) {
                    scale = boundsOpts.outWidth / targetWidth
                }
                finalOpts.inSampleSize = scale
                finalOpts
            } else null

            return@withContext encryptedFile.openFileInput().use { input ->
                BitmapFactory.decodeStream(input, null, opts)
            }
        } catch (e: Exception) {
            // Ignore
        }

        return@withContext null
    }

    suspend fun savePortrait(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        val dir = portraitsDirectory(context)
        val fileName = "portrait_${UUID.randomUUID()}${extensionFor(sourceUri, context)}"
        val destination = File(dir, fileName)

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                // Write directly to file (plaintext)
                destination.outputStream().use { output ->
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
