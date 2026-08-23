package com.example.swadebuilder.util

import com.example.swadebuilder.model.PersonagemSnapshot
import kotlinx.serialization.json.Json

/**
 * Gerenciador de backup e exportação/importação segura de fichas em formato JSON.
 */
object CharacterBackupManager {

    private val json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    sealed class ImportResult {
        data class Success(val snapshot: PersonagemSnapshot) : ImportResult()
        data class Failure(val reason: String) : ImportResult()
    }

    fun exportBackupJson(snapshot: PersonagemSnapshot): String {
        return json.encodeToString(snapshot)
    }

    fun importBackupJson(jsonString: String): ImportResult {
        if (jsonString.isBlank()) {
            return ImportResult.Failure("Arquivo de backup está vazio.")
        }
        return try {
            val snapshot = json.decodeFromString<PersonagemSnapshot>(jsonString)

            if (snapshot.version < 1) {
                return ImportResult.Failure("Versão do arquivo de backup incompatível (${snapshot.version}).")
            }

            if (snapshot.nome.isBlank()) {
                return ImportResult.Failure("O arquivo de backup não possui um nome de personagem válido.")
            }

            ImportResult.Success(snapshot)
        } catch (e: Exception) {
            ImportResult.Failure("Erro ao importar arquivo JSON de backup: ${e.localizedMessage ?: "formato inválido"}")
        }
    }
}
