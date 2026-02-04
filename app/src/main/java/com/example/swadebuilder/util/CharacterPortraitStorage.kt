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
import java.io.IOException
import java.util.Locale
import java.util.UUID

object CharacterPortraitStorage {
    private const val PORTRAIT_DIR = "portraits"
    private const val MAX_PORTRAIT_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB
    private const val MAX_PORTRAIT_DIMENSION = 1024

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

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        if (width <= maxDimension && height <= maxDimension) return 1
        var inSampleSize = 1
        var halfWidth = width / 2
        var halfHeight = height / 2
        while (halfWidth / inSampleSize >= maxDimension && halfHeight / inSampleSize >= maxDimension) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun decodeScaledBitmapFromBytes(data: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_PORTRAIT_DIMENSION)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    suspend fun loadPortrait(context: Context, fileName: String): Bitmap? = withContext(Dispatchers.IO) {
        val dir = portraitsDirectory(context)
        val file = try {
            SecurityUtils.getSafeChildFile(dir, fileName)
        } catch (e: Exception) {
            return@withContext null
        }

        if (!file.exists()) return@withContext null

        // 1. Try Plaintext (Preferred)
        try {
            val data = file.readBytes()
            val bitmap = decodeScaledBitmapFromBytes(data)
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

            return@withContext encryptedFile.openFileInput().use { input ->
                val data = input.readBytes()
                decodeScaledBitmapFromBytes(data)
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
            val data = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val bytes = input.readBytes()
                if (bytes.size > MAX_PORTRAIT_SIZE_BYTES) {
                    throw IOException("Imagem excede o limite de 10MB")
                }
                bytes
            } ?: return@withContext null

            val bitmap = decodeScaledBitmapFromBytes(data) ?: return@withContext null
            val format = if (fileName.endsWith(".png")) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            destination.outputStream().use { output ->
                if (!bitmap.compress(format, 85, output)) {
                    throw IOException("Falha ao salvar retrato.")
                }
            }

            // Validate that the file is actually an image via loader
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
