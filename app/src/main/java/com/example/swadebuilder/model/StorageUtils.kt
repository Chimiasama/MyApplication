package com.example.swadebuilder.model

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
private fun custoParaPenalidadeTexto(custo: String): String {
    val clean = custo.trim()

    // Números simples (ex.: "3")
    clean.toIntOrNull()?.let { base ->
        val pen = (base + 1) / 2  // ceil(base/2)
        return "-$pen"
    }

    // Prefixos + e faixas (ex.: "+2/+3")
    if (clean.contains("/")) {
        val parts = clean.split("/")
        val mapped = parts.map { p ->
            val n = p.replace("+","").trim().toIntOrNull()
            n?.let { "-${(it+1)/2}" } ?: "—"
        }
        return mapped.joinToString("/")
    }

    // Sufixo "+" (ex.: "2+")
    if (clean.endsWith("+")) {
        val n = clean.removeSuffix("+").toIntOrNull()
        return n?.let { "-${(it+1)/2}+" } ?: "—"
    }

    // Prefixo "+" (ex.: "+1")
    if (clean.startsWith("+")) {
        val n = clean.removePrefix("+").toIntOrNull()
        return n?.let { "-${(it+1)/2}" } ?: "—"
    }

    // Casos textuais ("Especial", "—", vazio)
    return "—"
}