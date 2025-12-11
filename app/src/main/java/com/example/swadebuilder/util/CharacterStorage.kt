package com.example.swadebuilder.util

import android.content.Context
import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

object CharacterStorage {
    private const val SAVE_DIR = "personagens"

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

    private fun savesDirectory(context: Context): File {
        return File(context.filesDir, SAVE_DIR).apply { mkdirs() }
    }

    fun listSaves(context: Context): List<SaveEntry> {
        val dir = savesDirectory(context)
        return dir.listFiles()?.mapNotNull { file ->
            runCatching {
                val snap = json.decodeFromString<PersonagemSnapshot>(file.readText())
                SaveEntry(file.nameWithoutExtension, snap.nome, snap.timestamp)
            }.getOrNull()
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun load(context: Context, id: String): PersonagemSnapshot? {
        val file = File(savesDirectory(context), "$id.json")
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString<PersonagemSnapshot>(file.readText())
        }.getOrNull()
    }

    fun save(context: Context, snapshot: PersonagemSnapshot): SaveEntry {
        val dir = savesDirectory(context)
        val saveId = snapshot.id.ifBlank { UUID.randomUUID().toString() }
        val file = File(dir, "$saveId.json")
        file.writeText(json.encodeToString(snapshot))
        return SaveEntry(saveId, snapshot.nome, snapshot.timestamp)
    }

    fun delete(context: Context, id: String) {
        val file = File(savesDirectory(context), "$id.json")
        if (file.exists()) {
            file.delete()
        }
    }
}
