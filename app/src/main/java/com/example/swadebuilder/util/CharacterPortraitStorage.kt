package com.example.swadebuilder.util

import android.content.Context
import android.net.Uri
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

    fun savePortrait(context: Context, sourceUri: Uri): String? {
        return runCatching {
            val dir = portraitsDirectory(context)
            val fileName = "portrait_${UUID.randomUUID()}${extensionFor(sourceUri, context)}"
            val destination = File(dir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            fileName
        }.getOrNull()
    }
}
