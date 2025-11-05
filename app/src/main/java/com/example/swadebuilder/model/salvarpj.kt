package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PersonagemSalvo(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,

    // Mantemos o mesmo nome de campo para compatibilidade.
    // A partir de agora GRAVE IDs de vantagens aqui. Em saves antigos pode haver nomes.
    val vantagens: List<String>,

    // Já eram IDs (pela sua MainActivity) — mantemos.
    val complicacoes: List<String>,

    // Equipamentos por nome (como já estava).
    val equipamentos: List<String>,

    // Poderes arcanos (slots por chave de AA) — já existia.
    val poderes: Map<String, List<String>>,

    val dinheiro: Int,
    val pontosRestantes: Int,
    val maisPontosPericias: Boolean,
    val cartaSelvagem: Boolean,
    val heroisSemArmadura: Boolean = false,
    val semPontosDePoder: Boolean = false,
    // --- Especializações (já existentes) ---
    val usarEspecializacoesDePericia: Boolean = false,
    val especializacoesPorPericia: Map<String, EspecializacoesDto> = emptyMap(),

    // --- NOVOS CAMPOS (SUPERS) ---
    // Flags de modo para restaurar telas e filtros corretamente:
    val modoSupers: Boolean = false,
    val modoSuperequip: Boolean = false,
    val modoSuperComplicacoes: Boolean = false,

    // Snapshot simples dos superpoderes comprados (por nome).
    // Mesmo que sua UI calcule slots/PP, manter este espelho ajuda a reexibir corretamente.
    val superpoderesComprados: List<String> = emptyList()
)

@Serializable
data class EspecializacoesDto(
    val principal: String? = null,
    val lista: List<String> = emptyList()
)
