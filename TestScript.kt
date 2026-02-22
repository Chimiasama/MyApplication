import com.example.swadebuilder.util.toFancyTitleCase
import com.example.swadebuilder.util.keyify

fun main() {
    val input = "HABITANTE DE GRAVIDADE ZERO/BAIXA"
    println("Input: $input")
    println("Formatted: '${input.toFancyTitleCase()}'")

    val input2 = "Habitante de Gravidade Zero/Baixa"
    println("Input2: $input2")
    println("Formatted2: '${input2.toFancyTitleCase()}'")

    println("Keyify check: 'AVIANOS'.keyify() == '${"AVIANOS".keyify()}'")
}
