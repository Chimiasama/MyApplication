package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import kotlinx.serialization.Serializable

@Serializable
data class MechaCustomizacoes(
    val blindagem_extra: Int = 0,
    val anotacoes: String = ""
)

@Serializable
data class MechaModItem(
    val id: String,
    val nome: String,
    val categoria: String = "",
    val mods_cost: Int = 0,
    val max_uses: Int = 1,
    // Livro: alguns Modificadores custam "Metade do Tam." (arredondado pra cima) ou "Tam." (cheio)
    // em vez de um número fixo de MODs. 0 = sem escala, usa mods_cost fixo; 1 = igual ao Tamanho
    // do chassi; 2 = metade do Tamanho (arredondado pra cima).
    val escala_tamanho_divisor: Int = 0,
    // Preço de tabela do livro (ex.: "$5K × Tam."). Só informativo/registro — o app não desconta
    // dinheiro por isso.
    val custo: String = "",
    val descricao: String = "",
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null
) {
    fun exibido(): MechaModItem =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this

    fun custoResolvido(tamanhoChassi: Int): Int =
        if (escala_tamanho_divisor > 0) {
            val divisor = escala_tamanho_divisor
            ((tamanhoChassi + divisor - 1) / divisor).coerceAtLeast(1)
        } else {
            mods_cost
        }
}

@Serializable
data class MechaWeaponItem(
    val id: String,
    val nome: String,
    val mods_cost: Int = 1,
    // Preço de tabela do livro (ex.: "$5K"). Só informativo/registro — o app não desconta dinheiro por isso.
    val custo: String = "",
    val descricao: String = "",
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null
) {
    fun exibido(): MechaWeaponItem =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}

@Serializable
data class MechaItem(
    val id: String,
    val nome: String,
    val categoria_chassi: String = "Grande",
    val tamanho: Int = 4,
    val manobrabilidade: Int = 0,
    val vel_maxima: Int = 8,
    val resistencia_base: Int = 15,
    val armadura_base: Int = 20,
    val ferimentos: Int = 4,
    val forca: String = "d12+4",
    val energia_dias: Int = 5,
    val mod_pontos_max: Int = 12,
    // Limite de Armadura Extra somado à armadura_base, conforme a categoria do chassi (livro).
    // 0 = sem limite cadastrado (não bloqueia).
    val armadura_extra_max: Int = 0,
    // Preço de tabela do chassi (ex.: "$1.750K"). Só informativo/registro — o app não desconta dinheiro por isso.
    val custo: String = "",
    val mods_instalados: List<MechaModItem> = emptyList(),
    val sistemas_instalados: List<String> = emptyList(),
    val armas_equipadas: List<String> = emptyList(),
    // Armas de Mecha criadas na hora pelo jogador (fora do catálogo oficial do livro), com
    // Mods cost próprio — ver CreateCustomMechaWeaponDialog em MechasSection.kt. Substituem
    // o antigo campo de texto livre "Arma Personalizada", que aceitava qualquer string em
    // armas_equipadas sem nenhum custo em MODs associado (o Mecha "ganhava" armas de graça).
    val armasCustomizadas: List<MechaWeaponItem> = emptyList(),
    val customizacoes: MechaCustomizacoes = MechaCustomizacoes()
) {
    // Livro (Estruturas de Mechas): a Armadura Máxima que o chassi aceita depende só da categoria
    // (Grande 20 / Enorme 30 / Colossal 40), não do Tamanho exato dentro da categoria. Quando
    // `armadura_extra_max` não é definido no catálogo (0), derivamos da categoria — cobre também
    // Mechas customizados criados do zero, que só preenchem `categoria_chassi`.
    fun armaduraExtraMaximaResolvida(): Int =
        if (armadura_extra_max > 0) armadura_extra_max else when (categoria_chassi.trim().lowercase()) {
            "grande" -> 20
            "enorme" -> 30
            "colossal" -> 40
            else -> 0
        }
}

@Serializable
data class CiberneticoItem(
    val id: String,
    val nome: String,
    val strain_custo: Int = 0,
    // Limite de compras do mesmo implante (livro: "Máximo"). 99 = sem limite prático (livro usa "I").
    val max_uses: Int = 99,
    // Preço de tabela do livro (ex.: "$5K"). Só informativo/registro — o app não desconta dinheiro por isso.
    val custo: String = "",
    val efeito: String = "",
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val efeitoLite: String? = null,
    val modificacoes: List<String> = emptyList()
) {
    fun exibido(): CiberneticoItem =
        if (!EditionConfig.isFullEdition && !efeitoLite.isNullOrBlank()) copy(efeito = efeitoLite) else this
}

@Serializable
data class MechaCatalogWrapper(
    val mechas: List<MechaItem> = emptyList()
)

@Serializable
data class CiberneticoCatalogWrapper(
    val ciberneticos: List<CiberneticoItem> = emptyList()
)

@Serializable
data class MechaModCatalogWrapper(
    val modificadores: List<MechaModItem> = emptyList()
)

@Serializable
data class MechaWeaponCatalogWrapper(
    val armas: List<MechaWeaponItem> = emptyList()
)
