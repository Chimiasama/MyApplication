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
}
