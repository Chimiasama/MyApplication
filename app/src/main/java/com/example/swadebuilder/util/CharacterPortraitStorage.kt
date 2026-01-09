package com.example.swadebuilder.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

object CharacterPortraitStorage {
    private const val PORTRAIT_DIR = "portraits"

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
                    input.copyTo(output)
                }
            } ?: return@withContext null

            // Validate that the file is actually an image
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(destination.absolutePath, options)

            if (options.outWidth == -1 || options.outHeight == -1) {
                if (destination.exists()) destination.delete()
                return@withContext null
            }

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
