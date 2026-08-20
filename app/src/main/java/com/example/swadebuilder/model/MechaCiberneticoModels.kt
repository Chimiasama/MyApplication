package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class MechaCustomizacoes(
    val blindagem_extra: Int = 0,
    val propulsores: Boolean = false,
    val anotacoes: String = ""
)

@Serializable
data class MechaModItem(
    val id: String,
    val nome: String,
    val categoria: String = "",
    val mods_cost: Int = 0,
    val max_uses: Int = 1,
    val descricao: String = ""
)

@Serializable
data class MechaWeaponItem(
    val id: String,
    val nome: String,
    val mods_cost: Int = 1,
    val descricao: String = ""
)

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
    val mods_instalados: List<MechaModItem> = emptyList(),
    val sistemas_instalados: List<String> = emptyList(),
    val armas_equipadas: List<String> = emptyList(),
    val customizacoes: MechaCustomizacoes = MechaCustomizacoes()
)

@Serializable
data class CiberneticoItem(
    val id: String,
    val nome: String,
    val strain_custo: Int = 0,
    val efeito: String = "",
    val modificacoes: List<String> = emptyList()
)

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
