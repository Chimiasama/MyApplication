package com.example.myapplication.model

import android.content.Context
import kotlinx.serialization.json.Json
import java.io.File

object StorageUtils {
    private fun fileNameFromKey(key: String): String =
        "personagem_${key}.json"

    fun salvarPersonagem(context: Context, personagem: PersonagemSalvo) {
        val file = File(context.filesDir, fileNameFromKey(personagem.id))
        val jsonText = Json.encodeToString(personagem)
        file.writeText(jsonText)
    }

    fun carregarPersonagem(context: Context, nomeArquivo: String): PersonagemSalvo? {
        val file = File(context.filesDir, fileNameFromKey(nomeArquivo))
        if (!file.exists()) return null
        val jsonText = file.readText()
        return Json.decodeFromString<PersonagemSalvo>(jsonText)
    }

    fun listarPersonagens(context: Context): List<Pair<String, String>> {
        return context.filesDir
            .listFiles { file ->
                file.name.startsWith("personagem_") && file.name.endsWith(".json")
            }
            ?.mapNotNull { file ->
                runCatching {
                    val jsonText = file.readText()
                    val personagem = Json.decodeFromString<PersonagemSalvo>(jsonText)
                    val key = file.name
                        .removePrefix("personagem_")
                        .removeSuffix(".json")
                    Pair(personagem.nome, key)
                }.getOrNull()
            } ?: emptyList()
    }

    fun deletarPersonagem(context: Context, nomeArquivo: String) {
        val file = File(context.filesDir, fileNameFromKey(nomeArquivo))
        if (file.exists()) file.delete()
    }
}
