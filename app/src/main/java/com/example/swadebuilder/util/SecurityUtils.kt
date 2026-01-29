package com.example.swadebuilder.util

import java.io.File

object SecurityUtils {
    /**
     * Retorna um arquivo filho dentro do diretório pai, garantindo que não haja Path Traversal.
     * @throws SecurityException se o arquivo resultante estiver fora do diretório pai.
     * @throws IllegalArgumentException se o nome do arquivo for inválido (contiver separadores).
     */
    fun getSafeChildFile(parentDir: File, fileName: String): File {
        if (fileName.contains(File.separator) || fileName.contains("/") || fileName.contains("\\")) {
             throw IllegalArgumentException("Nome de arquivo inválido: $fileName")
        }

        val file = File(parentDir, fileName)

        // Verificação canônica robusta (Path Traversal Protection)
        // Garante que o pai do arquivo canônico é EXATAMENTE o diretório pai canônico.
        if (file.canonicalFile.parentFile != parentDir.canonicalFile) {
            throw SecurityException("Tentativa de Path Traversal detectada: $fileName")
        }

        return file
    }

    private val INVALID_FILENAME_CHARS = Regex("[^a-zA-Z0-9._-]")

    /**
     * Sanitiza uma string para ser usada como nome de arquivo seguro.
     * Remove caracteres especiais, limita o tamanho e garante que não seja vazio.
     */
    fun sanitizeFilename(name: String): String {
        // Substitui caracteres inválidos por sublinhado
        val safeName = name.replace(INVALID_FILENAME_CHARS, "_")
        // Limita a 50 caracteres para evitar problemas em sistemas de arquivo
        val truncated = safeName.take(50)
        return truncated.ifBlank { "personagem_sem_nome" }
    }

    /**
     * Verifica se o nome de arquivo fornecido é válido e seguro.
     * Permite apenas letras, números, ponto, sublinhado e hífen.
     */
    fun isValidFilename(name: String): Boolean {
        if (name.isBlank() || name.length > 50) return false
        return !INVALID_FILENAME_CHARS.containsMatchIn(name)
    }

    /**
     * Sanitiza texto geral removendo caracteres de controle que podem causar problemas em exportações (PDF/JSON).
     */
    fun sanitizeText(input: String): String {
        return input.filter { !it.isISOControl() || it == '\n' || it == '\r' || it == '\t' }
    }
}
