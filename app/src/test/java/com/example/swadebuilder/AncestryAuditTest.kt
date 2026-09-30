package com.example.swadebuilder

import com.example.swadebuilder.model.HabilidadeCriacao
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitAuditFormatter
import com.example.swadebuilder.util.keyify
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

/** Gera o relatório auditoria_tracos_exclusivos.md em docs/ (v3). */
class AncestryAuditTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun findAssetFile(fileName: String): File {
        val candidates = listOf(
            File("src/main/assets/$fileName"),
            File("app/src/main/assets/$fileName")
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("File $fileName not found in candidates: ${candidates.map { it.absolutePath }}")
    }

    private fun findProjectRootDir(): File {
        var dir: File? = File(".").absoluteFile
        while (dir != null) {
            if (File(dir, "settings.gradle.kts").exists() || File(dir, "settings.gradle").exists()) {
                return dir
            }
            dir = dir.parentFile
        }
        return File(".").absoluteFile
    }

    @Test
    fun generateExclusiveTraitsAuditReport() {
        val ancestralidadesFile = findAssetFile("ancestralidades.json")
        val habilidadesFile = findAssetFile("basico_habilidades_raciais.json")

        val racas = json.decodeFromString<List<RacialModifier>>(ancestralidadesFile.readText())
        val catalogoOficial = json.decodeFromString<List<HabilidadeCriacao>>(habilidadesFile.readText())

        val exclusivos = RacialTraitAuditFormatter.calcularIdsExclusivos(racas)

        val reportLines = mutableListOf<String>()
        reportLines.add("# Relatório de Auditoria de Traços Raciais e Exclusividades\n")
        reportLines.add("Este relatório sintetiza a varredura automática realizada sobre todas as ancestralidades em `ancestralidades.json` comparadas com o catálogo oficial (`basico_habilidades_raciais.json`).\n")

        val exclusiveEntries = mutableListOf<String>()
        val outOfCatalogEntries = mutableListOf<String>()

        fun isExcecao(raca: RacialModifier): Boolean {
            val livro = raca.origem.uppercase()
            val racaKey = raca.nome.keyify()
            val racaIdKey = raca.id?.keyify().orEmpty()

            // Exceções solicitadas pelo usuário: Pathfinder, Cidade do Sol a Vapor, Meio-Elfos, Meio-Orcs
            if (livro.contains("PATHFINDER") || racaIdKey.contains("PATHFINDER")) return true
            if (livro.contains("CIDADE_SOL_VAPOR") || livro.contains("CSV") || racaIdKey.contains("CSV")) return true
            if (racaKey.contains("MEIO ELFO") || racaIdKey.contains("MEIO_ELFO")) return true
            if (racaKey.contains("MEIO ORC") || racaIdKey.contains("MEIO_ORC")) return true

            return false
        }

        racas.forEach { raca ->
            val linhasFormatadas = RacialTraitAuditFormatter.formatar(
                habilidades = raca.habilidades,
                catalogoOficial = catalogoOficial,
                idsExclusivos = exclusivos
            )

            val racaNome = raca.nome.ifBlank { raca.id ?: "Desconhecida" }
            val racaId = raca.id?.let { " [$it]" }.orEmpty()
            val livro = raca.origem.ifBlank { "BASICO" }

            linhasFormatadas.forEachIndexed { index, linha ->
                val hab = raca.habilidades.getOrNull(index)
                if (!isExcecao(raca)) {
                    if (linha.contains("exclusivo-desta-raça")) {
                        exclusiveEntries.add("- **${racaNome}**${racaId} ($livro) — Traço: **${hab?.nome}** | `id=${hab?.id}` | `traitId=${hab?.traitId}` | Audit: `$linha`")
                    }
                    if (linha.contains("[Regra Única da Raça / Fora do Catálogo]")) {
                        outOfCatalogEntries.add("- **${racaNome}**${racaId} ($livro) — Traço: **${hab?.nome}** | `id=${hab?.id}` | `traitId=${hab?.traitId}` | Audit: `$linha`")
                    }
                }
            }
        }

        reportLines.add("## 1. Traços Identificados como Exclusivos de Uma Raça (`exclusivo-desta-raça`)\n")
        if (exclusiveEntries.isEmpty()) {
            reportLines.add("Nenhum traço exclusivo encontrado.\n")
        } else {
            reportLines.addAll(exclusiveEntries.distinct())
            reportLines.add("")
        }

        reportLines.add("## 2. Traços Identificados como Fora do Catálogo (`[Regra Única da Raça / Fora do Catálogo]`)\n")
        if (outOfCatalogEntries.isEmpty()) {
            reportLines.add("Nenhum traço fora do catálogo encontrado.\n")
        } else {
            reportLines.addAll(outOfCatalogEntries.distinct())
            reportLines.add("")
        }

        reportLines.add("## 3. Auditoria Detalhada por Ancestralidade\n")
        racas.forEach { raca ->
            val racaNome = raca.nome.ifBlank { raca.id ?: "Desconhecida" }
            val racaId = raca.id?.let { " [id=$it]" }.orEmpty()
            val livro = raca.origem.ifBlank { "BASICO" }
            reportLines.add("### ${racaNome}${racaId} ($livro)")

            val linhasFormatadas = RacialTraitAuditFormatter.formatar(
                habilidades = raca.habilidades,
                catalogoOficial = catalogoOficial,
                idsExclusivos = exclusivos
            )

            linhasFormatadas.forEach { linha ->
                reportLines.add("- $linha")
            }
            reportLines.add("")
        }

        val projectRoot = findProjectRootDir()
        val docsDir = File(projectRoot, "docs")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
        }
        val outputFile = File(docsDir, "auditoria_tracos_exclusivos.md")
        outputFile.writeText(reportLines.joinToString("\n"))

        println("Auditoria salva com sucesso em: ${outputFile.absolutePath}")
    }
}
