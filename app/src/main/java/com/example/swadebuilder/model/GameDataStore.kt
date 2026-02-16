package com.example.swadebuilder.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Store transitório para Fase 1: centraliza leitura preferencial do snapshot
 * com fallback explícito para globais legadas.
 */
class GameDataStore {
    private var snapshot by mutableStateOf<GameDataSnapshot?>(null)

    fun updateSnapshot(newSnapshot: GameDataSnapshot) {
        snapshot = newSnapshot
    }

    fun currentSnapshot(): GameDataSnapshot? = snapshot

    fun pericias(fallback: List<Pericia>): List<Pericia> =
        snapshot?.listaPericias ?: fallback

    fun vantagens(fallback: List<Vantagem>): List<Vantagem> =
        snapshot?.listaVantagens ?: fallback

    fun complicacoes(fallback: List<Complicacao>): List<Complicacao> =
        snapshot?.listaComplicacoes ?: fallback

    fun coracoesCrystal(fallback: List<CrystalHeart>): List<CrystalHeart> =
        snapshot?.listaCoracoesCrystal ?: fallback

    fun periciasMap(fallback: Map<String, Pericia>): Map<String, Pericia> =
        snapshot?.mapaPericias ?: fallback

    fun withUpdatedCoracoesCrystal(coracoes: List<CrystalHeart>) {
        snapshot = snapshot?.copy(listaCoracoesCrystal = coracoes)
    }

    fun superPoderes(fallback: List<SuperPoder>): List<SuperPoder> =
        snapshot?.listaSuperPoderes ?: fallback

    // Phase 9: Accessors for migration, initially returning empty list if snapshot is null
    fun getPericias(): List<Pericia> = snapshot?.listaPericias ?: emptyList()
    fun getVantagens(): List<Vantagem> = snapshot?.listaVantagens ?: emptyList()
    fun getComplicacoes(): List<Complicacao> = snapshot?.listaComplicacoes ?: emptyList()
    fun getAncestralidades(): List<RacialModifier> = snapshot?.listaAncestralidadesJson ?: emptyList()
    fun getAtributos(): List<String> = snapshot?.listaAtributos ?: emptyList()
    fun getEquipamentos(): List<EquipamentoItem> = snapshot?.listaEquipamentos ?: emptyList()
    fun getEquipamentoCategorias(): List<EquipamentoCategoria> = snapshot?.equipamentoCategorias ?: emptyList()
    fun getSuperequipCategorias(): List<EquipamentoCategoria> = snapshot?.superequipCategorias ?: emptyList()
    fun getSuperPoderes(): List<SuperPoder> = snapshot?.listaSuperPoderes ?: emptyList()
    fun getPoderes(): List<Poder> = snapshot?.listaPoderes ?: emptyList()
    fun getTropos(): List<Tropo> = snapshot?.listaTropos ?: emptyList()
    fun getMonstroTemplates(): List<MonstroTemplate> = snapshot?.listaMonstroTemplates ?: emptyList()
    fun getRacialAttrMinMap(): Map<String, Map<String, Int>> = snapshot?.racialAttrMinMap ?: emptyMap()
    fun getRacialSkillStartMap(): Map<String, Map<String, Int>> = snapshot?.racialSkillStartMap ?: emptyMap()
    fun getMapaAtributosDisplay(): Map<String, String> = snapshot?.mapaAtributosDisplay ?: emptyMap()
    fun getMapaPericias(): Map<String, Pericia> = snapshot?.mapaPericias ?: emptyMap()
    fun getPericiasMap(): Map<String, Pericia> = snapshot?.mapaPericias ?: emptyMap()
    fun getCoracoesCrystal(): List<CrystalHeart> = snapshot?.listaCoracoesCrystal ?: emptyList()

    fun getArcanoInfoMap(): Map<String, Triple<Int, Int, String>> {
        return snapshot?.arcanoInfo?.associate {
            it.key.uppercase().trim() to Triple(it.slots, it.pp, it.foco)
        } ?: emptyMap()
    }
}
