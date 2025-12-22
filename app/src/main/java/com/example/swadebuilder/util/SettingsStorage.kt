package com.example.swadebuilder.util

import android.content.Context

object SettingsStorage {
    private const val PREF_FILE = "swadebuilder_settings"
    private const val KEY_LIVRO_SWADE = "livro_swade"
    private const val KEY_LIVRO_PATHFINDER = "livro_pathfinder"
    private const val KEY_CARTA_SELVAGEM = "carta_selvagem"
    private const val KEY_MAIS_PONTOS_PERICIAS = "mais_pontos_pericias"
    private const val KEY_MULTI_ANTECEDENTE_ARCANO = "multi_antecedente_arcano"
    private const val KEY_ESPECIALIZACAO_PER = "especializacao_per"
    private const val KEY_HEROIS_SEM_ARMADURA = "herois_sem_armadura"
    private const val KEY_MULTIPLOS_IDIOMAS = "multiplos_idiomas"
    private const val KEY_NASCE_UM_HEROI = "nasce_um_heroi"
    private const val KEY_SEM_PONTOS_PODER = "sem_pontos_poder"
    private const val KEY_MODO_SUPERS = "modo_supers"
    private const val KEY_GRANDES_RESPONSABILIDADES = "grandes_responsabilidades"
    private const val KEY_COMPENDIO_FANTASIA = "compendio_fantasia"
    private const val KEY_COMPENDIO_HORROR = "compendio_horror"
    private const val KEY_COMPENDIO_SCIFI = "compendio_scifi"
    private const val KEY_COMPENDIO_BUSCATRILHA = "compendio_buscatrilha"
    private const val KEY_COMPENDIO_DEADLANDS = "compendio_deadlands"
    private const val KEY_COMPENDIO_CRYSTAL_HEART = "compendio_crystal_heart"
    private const val KEY_COMPENDIO_ARTE_GUERRA = "compendio_arte_guerra"
    private const val KEY_COMPENDIO_CIDADE_SOL_VAPOR = "compendio_cidade_sol_vapor"
    private const val KEY_COMPENDIO_WISEGUYS = "compendio_wiseguys"
    private const val KEY_MODO_MONSTRO = "modo_monstro"

    private var appContext: Context? = null

    data class EditionSettings(
        val livroSwade: Boolean = true,
        val livroPathfinder: Boolean = false,
        val cartaSelvagem: Boolean = true,
        val maisPontosPericias: Boolean = true,
        val multiAntecedenteArcano: Boolean = false,
        val especializacaoPer: Boolean = false,
        val heroisSemArmadura: Boolean = false,
        val multiplosIdiomas: Boolean = false,
        val nasceUmHeroi: Boolean = false,
        val semPontosPoder: Boolean = false,
        val modoSupers: Boolean = false,
        val grandesResponsabilidades: Boolean = false,
        val compendioFantasia: Boolean = false,
        val compendioHorror: Boolean = false,
        val compendioSciFi: Boolean = false,
        val compendioBuscatrilha: Boolean = false,
        val compendioDeadlands: Boolean = false,
        val compendioCrystalHeart: Boolean = false,
        val compendioArteDaGuerra: Boolean = false,
        val compendioCidadeSolVapor: Boolean = false,
        val compendioWiseguys: Boolean = false,
        val modoMonstro: Boolean = false
    )

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun loadEditionSettings(context: Context = requireNotNull(appContext)): EditionSettings {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return EditionSettings(
            livroSwade = prefs.getBoolean(KEY_LIVRO_SWADE, true),
            livroPathfinder = prefs.getBoolean(KEY_LIVRO_PATHFINDER, false),
            cartaSelvagem = prefs.getBoolean(KEY_CARTA_SELVAGEM, true),
            maisPontosPericias = prefs.getBoolean(KEY_MAIS_PONTOS_PERICIAS, true),
            multiAntecedenteArcano = prefs.getBoolean(KEY_MULTI_ANTECEDENTE_ARCANO, false),
            especializacaoPer = prefs.getBoolean(KEY_ESPECIALIZACAO_PER, false),
            heroisSemArmadura = prefs.getBoolean(KEY_HEROIS_SEM_ARMADURA, false),
            multiplosIdiomas = prefs.getBoolean(KEY_MULTIPLOS_IDIOMAS, false),
            nasceUmHeroi = prefs.getBoolean(KEY_NASCE_UM_HEROI, false),
            semPontosPoder = prefs.getBoolean(KEY_SEM_PONTOS_PODER, false),
            modoSupers = prefs.getBoolean(KEY_MODO_SUPERS, false),
            grandesResponsabilidades = prefs.getBoolean(KEY_GRANDES_RESPONSABILIDADES, false),
            compendioFantasia = prefs.getBoolean(KEY_COMPENDIO_FANTASIA, false),
            compendioHorror = prefs.getBoolean(KEY_COMPENDIO_HORROR, false),
            compendioSciFi = prefs.getBoolean(KEY_COMPENDIO_SCIFI, false),
            compendioBuscatrilha = prefs.getBoolean(KEY_COMPENDIO_BUSCATRILHA, false),
            compendioDeadlands = prefs.getBoolean(KEY_COMPENDIO_DEADLANDS, false),
            compendioCrystalHeart = prefs.getBoolean(KEY_COMPENDIO_CRYSTAL_HEART, false),
            compendioArteDaGuerra = prefs.getBoolean(KEY_COMPENDIO_ARTE_GUERRA, false),
            compendioCidadeSolVapor = prefs.getBoolean(KEY_COMPENDIO_CIDADE_SOL_VAPOR, false),
            compendioWiseguys = prefs.getBoolean(KEY_COMPENDIO_WISEGUYS, false),
            modoMonstro = prefs.getBoolean(KEY_MODO_MONSTRO, false)
        )
    }

    fun loadEditionSettingsOrNull(): EditionSettings? {
        val context = appContext ?: return null
        return loadEditionSettings(context)
    }

    fun saveEditionSettings(
        settings: EditionSettings,
        context: Context = requireNotNull(appContext)
    ) {
        context
            .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_LIVRO_SWADE, settings.livroSwade)
            .putBoolean(KEY_LIVRO_PATHFINDER, settings.livroPathfinder)
            .putBoolean(KEY_CARTA_SELVAGEM, settings.cartaSelvagem)
            .putBoolean(KEY_MAIS_PONTOS_PERICIAS, settings.maisPontosPericias)
            .putBoolean(KEY_MULTI_ANTECEDENTE_ARCANO, settings.multiAntecedenteArcano)
            .putBoolean(KEY_ESPECIALIZACAO_PER, settings.especializacaoPer)
            .putBoolean(KEY_HEROIS_SEM_ARMADURA, settings.heroisSemArmadura)
            .putBoolean(KEY_MULTIPLOS_IDIOMAS, settings.multiplosIdiomas)
            .putBoolean(KEY_NASCE_UM_HEROI, settings.nasceUmHeroi)
            .putBoolean(KEY_SEM_PONTOS_PODER, settings.semPontosPoder)
            .putBoolean(KEY_MODO_SUPERS, settings.modoSupers)
            .putBoolean(KEY_GRANDES_RESPONSABILIDADES, settings.grandesResponsabilidades)
            .putBoolean(KEY_COMPENDIO_FANTASIA, settings.compendioFantasia)
            .putBoolean(KEY_COMPENDIO_HORROR, settings.compendioHorror)
            .putBoolean(KEY_COMPENDIO_SCIFI, settings.compendioSciFi)
            .putBoolean(KEY_COMPENDIO_BUSCATRILHA, settings.compendioBuscatrilha)
            .putBoolean(KEY_COMPENDIO_DEADLANDS, settings.compendioDeadlands)
            .putBoolean(KEY_COMPENDIO_CRYSTAL_HEART, settings.compendioCrystalHeart)
            .putBoolean(KEY_COMPENDIO_ARTE_GUERRA, settings.compendioArteDaGuerra)
            .putBoolean(KEY_COMPENDIO_CIDADE_SOL_VAPOR, settings.compendioCidadeSolVapor)
            .putBoolean(KEY_COMPENDIO_WISEGUYS, settings.compendioWiseguys)
            .putBoolean(KEY_MODO_MONSTRO, settings.modoMonstro)
            .apply()
    }
}
