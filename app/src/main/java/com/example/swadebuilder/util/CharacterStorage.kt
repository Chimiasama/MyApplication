package com.example.swadebuilder.util

import android.content.Context
import java.io.File

object CharacterStorage {
    private const val DIR_NAME = "saved_characters"

    private fun getDir(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun listSavedFiles(context: Context): List<File> {
        val dir = getDir(context)
        return dir.listFiles { file -> file.extension == "json" }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun saveFile(context: Context, filename: String, content: String) {
        val dir = getDir(context)
        val file = File(dir, if (filename.endsWith(".json")) filename else "$filename.json")
        file.writeText(content)
    }

    fun readFile(context: Context, filename: String): String {
        val dir = getDir(context)
        val file = File(dir, filename)
        return file.readText()
    }

    fun deleteFile(context: Context, filename: String) {
        val dir = getDir(context)
        val file = File(dir, filename)
        if (file.exists()) {
            file.delete()
        }
    }
}
