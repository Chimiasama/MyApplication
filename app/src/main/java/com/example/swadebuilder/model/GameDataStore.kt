package com.example.swadebuilder.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.SuperPoder

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
}
