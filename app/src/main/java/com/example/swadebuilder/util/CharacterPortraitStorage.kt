package com.example.swadebuilder.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.UUID
import com.example.swadebuilder.util.CharacterStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CharacterPortraitStorage {
    private const val PORTRAIT_DIR = "portraits"
    private const val MAX_PORTRAIT_SIZE_BYTES = 5 * 1024 * 1024L // 5 MB

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

    suspend fun savePortrait(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = portraitsDirectory(context)
            val fileName = "portrait_${UUID.randomUUID()}${extensionFor(sourceUri, context)}"
            val destination = File(dir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output ->
                    var bytesCopied: Long = 0
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytes = input.read(buffer)
                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        if (bytesCopied > MAX_PORTRAIT_SIZE_BYTES) {
                            throw IOException("Imagem excede o limite de tamanho de 5MB.")
                        }
                        bytes = input.read(buffer)
                    }
                }
            } ?: return@withContext null

            fileName
        }.getOrNull()
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

        val file = File(portraitsDirectory(context), fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}
