package com.example.swadebuilder.util

import android.content.Context
import com.example.swadebuilder.model.CrystalHeart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

object CustomCrystalHeartStorage {
    private const val FILE_NAME = "custom_crystal_hearts.json"
    private const val MAX_FILE_SIZE = 256 * 1024L
    private const val MAX_HEARTS = 50
    private const val MAX_NAME_LENGTH = 60
    private const val MAX_TEXT_LENGTH = 500
    private const val MAX_POWER_LENGTH = 200
    private const val MAX_POWERS = 10

    private val validStages = setOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário")

    private val json = Json {
        encodeDefaults = true
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private fun storageFile(context: Context): File {
        return SecurityUtils.getSafeChildFile(context.filesDir, FILE_NAME)
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    fun load(context: Context): List<CrystalHeart> {
        val file = storageFile(context)
        if (!file.exists() || file.length() > MAX_FILE_SIZE) return emptyList()

        val hearts = runCatching {
            file.inputStream().use { input ->
                json.decodeFromStream<List<CrystalHeart>>(input)
            }
        }.getOrElse { emptyList() }

        return hearts.mapNotNull { sanitize(it) }.distinctBy { it.id }.take(MAX_HEARTS)
    }

    fun saveCustomHeart(context: Context, heart: CrystalHeart): CrystalHeart? {
        val sanitized = sanitize(heart)
        val current = load(context).toMutableList()
        val existingIndex = current.indexOfFirst { it.id == sanitized.id }
        if (existingIndex >= 0) {
            current[existingIndex] = sanitized
        } else {
            current.add(sanitized)
        }
        saveAll(context, current.take(MAX_HEARTS))
        return sanitized
    }

    fun deleteCustomHeart(context: Context, heartId: String): Boolean {
        val current = load(context).toMutableList()
        val removed = current.removeAll { it.id == heartId }
        if (removed) {
            saveAll(context, current)
        }
        return removed
    }

    private fun saveAll(context: Context, hearts: List<CrystalHeart>) {
        val file = storageFile(context)
        file.outputStream().use { output ->
            output.write(json.encodeToString(hearts).toByteArray())
        }
    }

    private fun sanitize(heart: CrystalHeart): CrystalHeart {
        val safeName = SecurityUtils.sanitizeText(heart.nome)
            .trim()
            .take(MAX_NAME_LENGTH)
            .ifBlank { "Coração Personalizado" }

        val stage = if (validStages.contains(heart.estagio)) heart.estagio else "Novato"

        val passiva = SecurityUtils.sanitizeText(heart.habilidadePassiva.orEmpty())
            .trim()
            .take(MAX_TEXT_LENGTH)
            .ifBlank { null }

        val complicacao = SecurityUtils.sanitizeText(heart.complicacaoInerente.orEmpty())
            .trim()
            .take(MAX_TEXT_LENGTH)
            .ifBlank { null }

        val descricao = SecurityUtils.sanitizeText(heart.descricao.orEmpty())
            .trim()
            .take(MAX_TEXT_LENGTH)
            .ifBlank { null }

        val poderes = heart.poderes.map { poder ->
            SecurityUtils.sanitizeText(poder)
                .trim()
                .take(MAX_POWER_LENGTH)
        }.filter { it.isNotBlank() }
            .distinct()
            .take(MAX_POWERS)

        return heart.copy(
            nome = safeName,
            estagio = stage,
            habilidadePassiva = passiva,
            poderes = poderes,
            complicacaoInerente = complicacao,
            origem = "CRYSTAL_HEART",
            descricao = descricao,
            custom = true,
            placeholder = false
        )
    }
}
