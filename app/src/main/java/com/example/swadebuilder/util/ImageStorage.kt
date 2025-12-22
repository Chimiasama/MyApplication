package com.example.swadebuilder.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object ImageStorage {
    private const val IMAGES_DIR = "images"

    private fun getImagesDir(context: Context): File {
        return File(context.filesDir, IMAGES_DIR).apply { mkdirs() }
    }

    /**
     * Copia a imagem da URI para o diretório interno do app e retorna o nome do arquivo.
     * Gera um nome único para evitar conflitos.
     */
    fun saveImage(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val uniqueName = "img_${UUID.randomUUID()}.jpg"
            val destFile = File(getImagesDir(context), uniqueName)

            inputStream.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            uniqueName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retorna o arquivo de imagem salvo internamente.
     */
    fun getImageFile(context: Context, fileName: String?): File? {
        if (fileName.isNullOrBlank()) return null
        val file = File(getImagesDir(context), fileName)
        return if (file.exists()) file else null
    }

    /**
     * Exclui uma imagem salva.
     */
    fun deleteImage(context: Context, fileName: String?) {
        if (fileName.isNullOrBlank()) return
        try {
            val file = File(getImagesDir(context), fileName)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
