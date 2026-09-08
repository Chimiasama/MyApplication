package com.example.swadebuilder.util

import com.example.swadebuilder.toDiceString

/**
 * Regras de "Força Mínima" do livro básico (Cap. 2 "Equipamento"), lidas a partir dos
 * campos livres do catálogo (`dano`, `forcaMin`) e do raw-int de Força do personagem
 * (a mesma escala 4/6/8/10/12/13/14... usada em `valoresAtributos`/`Int.toDiceString()`).
 * Só matemática pura — quem aplica cada resultado (ModifierEngine, ResumoSection) decide
 * se vira um modificador de stat de verdade ou só uma nota exibida na ficha.
 */
object ForcaMinimaCalculator {

    private val DADO_REGEX = Regex("""d(\d+)(?:\+(\d+))?""", RegexOption.IGNORE_CASE)
    private val DANO_FORCA_REGEX = Regex("""(For|Str|Força)\s*\+\s*d(\d+)""", RegexOption.IGNORE_CASE)

    /** "d4".."d12+2"... -> raw-int (4,6,8,10,12,13,14...). Null se não reconhecer o formato. */
    fun paraRaw(dado: String): Int? {
        val m = DADO_REGEX.find(dado.trim()) ?: return null
        val base = m.groupValues[1].toIntOrNull() ?: return null
        val bonus = m.groupValues[2].toIntOrNull() ?: 0
        return base + bonus
    }

    /**
     * Índice sequencial de "passos de tipo de dado": d4=0, d6=1, d8=2, d10=3, d12=4,
     * d12+1=5, d12+2=6... — é a unidade que o livro usa em "cada diferença de tipo de
     * dado" (o incremento d12+N conta como mais um passo, igual a subir de d10 pra d12).
     */
    fun passo(raw: Int): Int = if (raw <= 12) (raw - 4) / 2 else 4 + (raw - 12)

    /**
     * Quantos passos a Força do personagem (`forcaRaw`) fica abaixo do mínimo exigido por
     * um item (`minimo`, ex.: "d8"). Zero se o item não tem mínimo cadastrado, se o texto
     * não é um dado reconhecível, ou se a Força já atende.
     */
    fun passosAbaixoDoMinimo(forcaRaw: Int, minimo: String?): Int {
        val minRaw = minimo?.let { paraRaw(it) } ?: return 0
        return (passo(minRaw) - passo(forcaRaw)).coerceAtLeast(0)
    }

    /**
     * Dano efetivo de uma arma corpo a corpo/arremesso (formato "For+dN"): o dado PRÓPRIO
     * da arma nunca passa do dado de Força de quem usa — regra incondicional, independente
     * de a arma ter uma "Força Mínima" cadastrada ("alguém com Força d4 pegar uma espada
     * longa (For+d8), a rolagem é de d4+d4 em vez de d4+d8"). Retorna null quando não há
     * capping a aplicar: o texto não casa com "For+dN", ou o dado da arma já é <= à Força.
     */
    fun danoLimitadoPelaForca(dano: String, forcaRaw: Int): String? {
        val m = DANO_FORCA_REGEX.find(dano) ?: return null
        val dadoArmaRaw = m.groupValues[2].toIntOrNull() ?: return null
        if (dadoArmaRaw <= forcaRaw) return null
        return dano.replaceRange(m.range, "${m.groupValues[1]}+${forcaRaw.toDiceString()}")
    }
}
