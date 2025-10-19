package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.model.Vantagem
import com.example.myapplication.util.loadJsonAsset

object VantagensRepository {
    private var cache: List<Vantagem>? = null

    /**
     * Retorna a lista de todas as vantagens (desserializadas de vantange​ns.json).
     * Se já foi carregada uma vez, retorna a versão em cache.
     */
    fun getAll(context: Context): List<Vantagem> {
        if (cache == null) {
            // loadJsonAsset<> é a função de extensão que você já usa em Outros lugares
            cache = context.loadJsonAsset("Vantagens.json")
        }
        return cache!!
    }
}
