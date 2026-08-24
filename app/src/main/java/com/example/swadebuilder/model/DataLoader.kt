package com.example.swadebuilder.model

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.example.swadebuilder.util.CustomCrystalHeartStorage
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Loads JSON game data from assets into global variables.
 * Refactored for Lazy Loading.
 */
object DataLoader {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    // Cache for loaded file content (FileName -> Any)
    // Stores List<T> or specific wrapper types (AtributoList, PericiaList)
    private val dataCache = mutableMapOf<String, Any>()

    private data class ModuleFile(val fileName: String, val originOverride: String? = null)

    // --- Module Definitions ---

    // Equipamentos vivem em um único arquivo consolidado (equipamentos.json), com cada
    // categoria marcada por "livros" (quais livros a enxergam), em vez de um arquivo por
    // livro. Isso elimina a necessidade de uma lista de módulos e de regras especiais por
    // livro (ex.: o antigo caso especial do Crystal Heart) — a visibilidade é resolvida
    // diretamente em updateActiveModules() a partir de equipVisibleOrigins.
    @Serializable
    private data class EquipamentoCategoriaFonte(
        val tipo: String,
        val subtipo: String,
        val subsubtipo: String? = null,
        val livros: List<String>,
        val itens: List<EquipamentoItem>
    )

    // Perícias vivem em um único arquivo consolidado (pericias.json). Diferente do
    // equipamentos.json, aqui a maioria das perícias é idêntica entre livros (mesmo
    // atributo, mesma regra de "básica", mesma descrição), então cada registro carrega a
    // lista de livros que a possuem — sem duplicar o mesmo conteúdo 10 vezes. Onde um livro
    // diverge de verdade (ex.: Crystal Heart reescreve a descrição de quase toda perícia, ou
    // "Lutar" é básica só em Crystal Heart), esse livro fica com seu próprio registro.
    @Serializable
    private data class PericiaFonte(
        val nome: String,
        val atributo: String = "",
        val basica: Boolean = false,
        val descricao: String? = null,
        val livros: List<String>
    )

    private val advantageModules = listOf(
        ModuleFile("fantasia_vantagens.json", originOverride = "FANTASIA"),
        ModuleFile("horror_vantagens.json", originOverride = "HORROR"),
        ModuleFile("scifi_vantagens.json", originOverride = "SCI_FI"),
        ModuleFile("crystal_vantagens.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("super_vantagens.json", originOverride = "SUPER"),
        ModuleFile("wiseguys_vantagens.json", originOverride = "WISEGUYS"),
        ModuleFile("adg_vantagens.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("sol_vapor_vantagens.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("deadlands_vantagens.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_vantagens.json", originOverride = "PATHFINDER"),
        ModuleFile("basico_vantagens.json")
    )

    private val complicationModules = listOf(
        ModuleFile("fantasia_complicacoes.json", originOverride = "FANTASIA"),
        ModuleFile("horror_complicacoes.json", originOverride = "HORROR"),
        ModuleFile("scifi_complicacoes.json", originOverride = "SCI_FI"),
        ModuleFile("super_complicacoes.json", originOverride = "SUPER"),
        ModuleFile("wiseguys_complicacoes.json", originOverride = "WISEGUYS"),
        ModuleFile("crystal_complicacoes.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("adg_complicacoes.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("sol_vapor_complicacoes.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("deadlands_complicacoes.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_complicacoes.json", originOverride = "PATHFINDER"),
        ModuleFile("basico_complicacoes.json")
    )

    // Ancestralidades vivem em um único arquivo consolidado (ancestralidades.json). Ao
    // contrário de Perícias, quase todo nome de raça compartilhado entre livros tem dados
    // DIFERENTES de propósito (ex.: "Anões" do Sci-Fi tem variantes Ciber que o Básico não
    // tem) — nenhum dos 14 nomes repetidos entre livros é idêntico campo a campo. Por isso,
    // como em Equipamentos, cada registro carrega apenas o(s) livro(s) exatos aos quais
    // pertence, sem fundir raças com conteúdo diferente.
    @Serializable
    private data class RacialModifierFonte(
        val id: String? = null,
        val nome: String,
        val originalName: String? = null,
        val originalDescription: String? = null,
        val descricao: String? = null,
        val atributos: Map<String, Int>,
        val pericias: Map<String, Int>,
        val vantagensGratis: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val habilidades: List<RacialAbility> = emptyList(),
        val movimentacao: Int = 0,
        val tags: List<String> = emptyList(),
        val opcoes: List<String> = emptyList(),
        val livros: List<String>
    )

    private val powerModules = listOf(
        ModuleFile("fantasia_poderes.json", originOverride = "FANTASIA"),
        ModuleFile("scifi_poderes.json", originOverride = "SCI_FI"),
        ModuleFile("horror_poderes.json", originOverride = "HORROR"),
        ModuleFile("deadlands_poderes.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_poderes.json", originOverride = "PATHFINDER"),
        ModuleFile("crystal_poderes.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("sol_vapor_poderes.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("wiseguys_poderes.json", originOverride = "WISEGUYS"),
        ModuleFile("adg_poderes.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("adg_tecnicas_chi.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("super_poderes_base.json", originOverride = "SUPER"),
        ModuleFile("basico_poderes.json")
    )

    // --- Loading Logic ---

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.loadAndMerge(
        modules: List<ModuleFile>,
        activeKeys: Set<String>,
        noinline transform: (T, String?) -> T = { item, _ -> item }
    ): List<T> {
        return modules.filter {
            val key = it.originOverride?.uppercase() ?: "BASICO"
            key in activeKeys
        }.flatMap { module ->
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut(module.fileName) {
                try {
                    open(module.fileName).use { input ->
                        json.decodeFromStream<List<T>>(input)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "SWADE_DEBUG",
                        "[DataLoader] falha ao carregar ${module.fileName} (originOverride=${module.originOverride}): ${e::class.simpleName}: ${e.message}",
                        e
                    )
                    emptyList<T>()
                }
            } as List<T>

            cached.map { item ->
                if (module.originOverride != null) transform(item, module.originOverride) else item
            }
        }
    }

    private var loadedArcanoInfoList: List<ArcanoInfo> = emptyList()

    @OptIn(ExperimentalSerializationApi::class)
    
    fun loadCore(context: Context): GameDataSnapshot {
        return updateActiveModules(context, setOf("BASICO"))
    }

    @OptIn(ExperimentalSerializationApi::class)
    
    fun updateActiveModules(context: Context, activeModules: Set<String>): GameDataSnapshot {
        val keys = activeModules + "BASICO" // Always include basic
        val assets = context.assets

        val replacementBookKeys = setOf(
            "PATHFINDER",
            "DEADLANDS",
            "CRYSTAL_HEART",
            "ARTE_DA_GUERRA",
            "CIDADE_SOL_VAPOR",
            "WISEGUYS"
        )
        // O Básico só é substituído quando exatamente um livro autônomo está ativo sozinho
        // (regra real de mesa: aquele livro passa a ser o "corebook" da mesa). Quando mais de
        // uma origem não-básica está ativa ao mesmo tempo — hoje isso só acontece no Modo Livre,
        // que ativa todos os livros simultaneamente — não há um único "substituto", então o
        // Básico continua disponível junto com tudo, como já é o contrato de getActiveOrigins().
        val nonBasicActiveKeys = keys - "BASICO"
        val shouldReplaceBasico = nonBasicActiveKeys.size == 1 && nonBasicActiveKeys.first() in replacementBookKeys

        // 1. Equipamentos
        // Livros de cenário autônomos (ex.: Crystal Heart, Deadlands) trazem seu próprio
        // catálogo de equipamentos, coerente com o gênero (sem viaturas/armas modernas fora
        // de contexto), e não herdam o Básico. Livros companheiros (Fantasia, Horror, Sci-Fi,
        // Supers) somam o próprio conteúdo ao Básico. No Modo Livre (mais de uma origem não-
        // básica ativa ao mesmo tempo) tudo fica visível.
        val equipVisibleOrigins = if (shouldReplaceBasico) nonBasicActiveKeys else keys

        @Suppress("UNCHECKED_CAST")
        val equipCategoriasFonte = dataCache.getOrPut("equipamentos.json") {
            runCatching {
                loadJsonAsset<List<EquipamentoCategoriaFonte>>(context, "equipamentos.json")
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar equipamentos.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<EquipamentoCategoriaFonte>

        val allEquip = equipCategoriasFonte.mapNotNull { cat ->
            if (cat.livros.none { it in equipVisibleOrigins }) return@mapNotNull null
            EquipamentoCategoria(
                tipo = cat.tipo,
                subtipo = cat.subtipo,
                subsubtipo = cat.subsubtipo,
                origem = cat.livros.first(),
                itens = cat.itens
            )
        }
        val localListaEquipamentos = allEquip.flatMap { it.itens }

        val localEquipamentoCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
            }
        )
        val localSuperequipCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true) ?: false
            }
        )

        // 2. Crystal Hearts
        val localListaCoracoesCrystal = if ("CRYSTAL_HEART" in keys) {
            @Suppress("UNCHECKED_CAST")
            val hearts = dataCache.getOrPut("crystal_coracoes.json") {
                runCatching {
                    assets.open("crystal_coracoes.json")
                        .use { input -> json.decodeFromStream<List<CrystalHeart>>(input) }
                }.getOrElse { emptyList<CrystalHeart>() }
            } as List<CrystalHeart>
            val customHearts = CustomCrystalHeartStorage.load(context)
            (hearts + customHearts).distinctBy { it.id }
        } else {
            emptyList()
        }

        // 3. Super Poderes
        val localListaSuperPoderes = if ("SUPER" in keys) {
            @Suppress("UNCHECKED_CAST")
            val supers = dataCache.getOrPut("super_poderes.json") {
                runCatching {
                    assets.open("super_poderes.json")
                        .use { input -> json.decodeFromStream<List<SuperPoder>>(input) }
                }.getOrElse { emptyList<SuperPoder>() }
            } as List<SuperPoder>
            supers
        } else {
            emptyList()
        }

        // 4. Arcano Info (Always load core)
        @Suppress("UNCHECKED_CAST")
        val arcanoList = dataCache.getOrPut("geral_arcano_info.json") {
            runCatching {
                assets.open("geral_arcano_info.json")
                    .use { input -> json.decodeFromStream<List<ArcanoInfo>>(input) }
            }.getOrElse { emptyList<ArcanoInfo>() }
        } as List<ArcanoInfo>

        loadedArcanoInfoList = arcanoList
        // arcanoInfo removed from global write, handled in snapshot

        // 5. Atributos (Always load core)
        val atributosData = dataCache.getOrPut("geral_atributos.json") {
            runCatching {
                loadJsonAsset<AtributoList>(context, "geral_atributos.json")
            }.getOrElse { AtributoList(emptyList()) }
        } as AtributoList

        val localListaAtributos = atributosData.atributos.map { it.nome.keyify() }
        val localMapaAtributosDisplay = atributosData.atributos.associate { it.nome.keyify() to it.nome }

        // 6. Pericias
        // Mesma regra de visibilidade dos equipamentos: livro autônomo vê só o próprio
        // conteúdo, livro companheiro soma ao Básico, Modo Livre vê tudo.
        val skillVisibleOrigins = if (shouldReplaceBasico) nonBasicActiveKeys else keys

        @Suppress("UNCHECKED_CAST")
        val periciasFonte = dataCache.getOrPut("pericias.json") {
            runCatching {
                loadJsonAsset<List<PericiaFonte>>(context, "pericias.json")
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar pericias.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<PericiaFonte>

        val todasPericiasJson = periciasFonte.flatMap { fonte ->
            fonte.livros.filter { it in skillVisibleOrigins }.map { livro ->
                PericiaJson(
                    nome = fonte.nome,
                    atributo = fonte.atributo,
                    basica = fonte.basica,
                    origem = livro,
                    descricao = fonte.descricao
                )
            }
        }

        val rawPericias = todasPericiasJson.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica,
                origem   = pj.origem,
                descricao = pj.descricao
            )
        }

        val localListaPericias = rawPericias
        val localMapaPericias = localListaPericias.associateBy { it.nome.keyify() }

        val localMapaAtributosDescricao = atributosData.atributos.associate {
            it.nome.keyify() to (it.descricao ?: "")
        }

        // 7. Vantagens
        val advantagesToLoad = if (shouldReplaceBasico) {
            advantageModules.filter { it.fileName != "basico_vantagens.json" }
        } else {
            advantageModules
        }

        val todasVantagens = assets.loadAndMerge<Vantagem>(advantagesToLoad, keys) { item, override ->
             if (override != null) item.copy(origem = override) else item
        }

        val localListaVantagens = buildList {
            addAll(todasVantagens)

            if (shouldReplaceBasico && none { it.id == "antecedente_arcano" }) {
                @Suppress("UNCHECKED_CAST")
                val basicoVantagens = dataCache.getOrPut("basico_vantagens.json") {
                    runCatching {
                        assets.open("basico_vantagens.json")
                            .use { input -> json.decodeFromStream<List<Vantagem>>(input) }
                    }.getOrElse { emptyList<Vantagem>() }
                } as List<Vantagem>

                basicoVantagens
                    .firstOrNull { it.id == "antecedente_arcano" }
                    ?.let { add(it) }
            }
        }

        if ("CIDADE_SOL_VAPOR" in keys) {
            val steamAll = todasVantagens.filter { canonicalOriginKey(it.origem) == "CIDADE_SOL_VAPOR" }
            Log.d(
                "SWADE_DEBUG",
                "[DataLoader] keys=$keys, shouldReplaceBasico=$shouldReplaceBasico, " +
                    "vantagens_total=${todasVantagens.size}, sol_vapor_total=${steamAll.size}"
            )
            steamAll.take(20).forEach { vant ->
                Log.d(
                    "SWADE_DEBUG",
                    "[DataLoader] sol_vapor id=${vant.id}, origem=${vant.origem}, nome=${vant.nomeExibicao}"
                )
            }
        }

        // 8. Tropos e Complicações
        val adgTropos = if ("ARTE_DA_GUERRA" in keys) {
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut("adg_tropos.json") {
                runCatching { loadJsonAsset<List<Tropo>>(context, "adg_tropos.json") }.getOrElse { emptyList<Tropo>() }
            } as List<Tropo>
            cached
        } else emptyList()

        val chTropos = if ("CRYSTAL_HEART" in keys) {
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut("crystal_tropos.json") {
                runCatching { loadJsonAsset<List<Tropo>>(context, "crystal_tropos.json") }.getOrElse { emptyList<Tropo>() }
            } as List<Tropo>
            cached
        } else emptyList()

        val localListaTropos = adgTropos + chTropos

        val complicationModulesToLoad = if (shouldReplaceBasico) {
            complicationModules.filter { it.fileName != "basico_complicacoes.json" }
        } else {
            complicationModules
        }

        val localListaComplicacoes = assets.loadAndMerge<Complicacao>(complicationModulesToLoad, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        // 9. Ancestralidades
        // Mesma regra de visibilidade das demais categorias: livro autônomo vê só o próprio
        // conteúdo, livro companheiro soma ao Básico, Modo Livre vê tudo.
        val ancestryVisibleOrigins = if (shouldReplaceBasico) nonBasicActiveKeys else keys

        @Suppress("UNCHECKED_CAST")
        val ancestriasFonte = dataCache.getOrPut("ancestralidades.json") {
            runCatching {
                loadJsonAsset<List<RacialModifierFonte>>(context, "ancestralidades.json")
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar ancestralidades.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<RacialModifierFonte>

        val localListaAncestralidadesJson = ancestriasFonte.flatMap { fonte ->
            fonte.livros.filter { it in ancestryVisibleOrigins }.map { livro ->
                RacialModifier(
                    id = fonte.id,
                    nome = fonte.nome,
                    originalName = fonte.originalName,
                    originalDescription = fonte.originalDescription,
                    descricao = fonte.descricao,
                    atributos = fonte.atributos,
                    pericias = fonte.pericias,
                    vantagensGratis = fonte.vantagensGratis,
                    desvantagens = fonte.desvantagens,
                    habilidades = fonte.habilidades,
                    origem = livro,
                    movimentacao = fonte.movimentacao,
                    tags = fonte.tags,
                    opcoes = fonte.opcoes
                )
            }
        }

        // 10. Monstros
        val localListaMonstroTemplates = if ("HORROR" in keys) {
            @Suppress("UNCHECKED_CAST")
            val monstros = dataCache.getOrPut("horror_monstros.json") {
                runCatching {
                    assets.open("horror_monstros.json")
                        .use { input -> json.decodeFromStream<List<MonstroTemplate>>(input) }
                }.getOrElse { emptyList<MonstroTemplate>() }
            } as List<MonstroTemplate>
            monstros
        } else {
            emptyList()
        }

        // 11. Mapas Raciais
        val localRacialAttrMinMap = localListaAncestralidadesJson.associate { rm ->
            val m = rm.atributos
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        val localRacialSkillStartMap = localListaAncestralidadesJson.associate { rm ->
            val m = rm.pericias
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        // 12. Regras de Criação de Raça (Unused mostly but cached)
        // Kept for consistency if needed later

        // 13. Poderes
        val powerModulesToLoad = if (shouldReplaceBasico) {
            powerModules.filter { it.fileName != "basico_poderes.json" }
        } else {
            powerModules
        }
        val todosPoderes = assets.loadAndMerge<Poder>(powerModulesToLoad, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        val localListaPoderes = todosPoderes

        // 14. Custom Local Content per active book
        val customStorageManager = com.example.swadebuilder.util.CustomStorageManager()
        val customVantagens = mutableListOf<Vantagem>()
        val customComplicacoes = mutableListOf<Complicacao>()
        val customEquipamentos = mutableListOf<EquipamentoItem>()
        val customPoderes = mutableListOf<Poder>()
        val customRacas = mutableListOf<RacialModifier>()

        keys.forEach { bookKey ->
            val customData = customStorageManager.loadCustomContent(context, bookKey)
            customVantagens += customData.vantagens
            customComplicacoes += customData.complicacoes
            customEquipamentos += customData.equipamentos
            customPoderes += customData.poderes
            customRacas += customData.racas
        }

        val mergedVantagens = (localListaVantagens + customVantagens).distinctBy { it.id }
        val mergedComplicacoes = (localListaComplicacoes + customComplicacoes).distinctBy { it.id }
        val mergedEquipamentos = (localListaEquipamentos + customEquipamentos).distinctBy { it.nome.keyify() }
        val mergedPoderes = (localListaPoderes + customPoderes).distinctBy { it.id }
        val mergedAncestralidades = (localListaAncestralidadesJson + customRacas).distinctBy { it.nome.keyify() }

        // Inject custom equipment into categories so they appear in EquipamentoSection
        val updatedEquipamentoCategorias = if (customEquipamentos.isNotEmpty()) {
            val categorizedCustoms = customEquipamentos.groupBy { it.subtipo ?: "Equipamento Geral" }
            val existingTypes = localEquipamentoCategorias.map { it.subtipo to it }.toMap().toMutableMap()
            categorizedCustoms.forEach { (subtipo, items) ->
                val existing = existingTypes[subtipo]
                if (existing != null) {
                    existingTypes[subtipo] = existing.copy(itens = (existing.itens + items).distinctBy { it.nome.keyify() })
                } else {
                    existingTypes[subtipo] = EquipamentoCategoria(
                        tipo = "EQUIPAMENTO GERAL",
                        subtipo = subtipo,
                        origem = "CUSTOM",
                        itens = items
                    )
                }
            }
            existingTypes.values.toList()
        } else {
            localEquipamentoCategorias
        }

        return GameDataSnapshot(
            listaComplicacoes = mergedComplicacoes,
            listaCoracoesCrystal = localListaCoracoesCrystal,
            listaAncestralidadesJson = mergedAncestralidades,
            listaMonstroTemplates = localListaMonstroTemplates,
            racialAttrMinMap = localRacialAttrMinMap,
            racialSkillStartMap = localRacialSkillStartMap,
            listaAtributos = localListaAtributos,
            mapaAtributosDisplay = localMapaAtributosDisplay,
            listaPericias = localListaPericias,
            mapaPericias = localMapaPericias,
            mapaAtributosDescricao = localMapaAtributosDescricao,
            listaVantagens = mergedVantagens,
            listaPoderes = mergedPoderes,
            listaTropos = localListaTropos,
            listaEquipamentos = mergedEquipamentos,
            equipamentoCategorias = updatedEquipamentoCategorias,
            superequipCategorias = localSuperequipCategorias,
            listaSuperPoderes = localListaSuperPoderes,
            arcanoInfo = loadedArcanoInfoList
        )
    }

    private fun deduplicarEquipamentoCategorias(
        categorias: List<EquipamentoCategoria>
    ): List<EquipamentoCategoria> {
        return categorias.map { categoria ->
            val itensDeduplicados = categoria.itens.distinctBy { equipamentoKey(it) }
            if (itensDeduplicados.size == categoria.itens.size) {
                categoria
            } else {
                categoria.copy(itens = itensDeduplicados)
            }
        }
    }

    private fun equipamentoKey(item: EquipamentoItem): String = listOfNotNull(
        item.nome.keyify(),
        item.custo?.toString(),
        item.peso?.toString(),
        item.origem?.keyify(),
        item.subtipo?.keyify(),
        item.subsubtipo?.keyify(),
        item.forcaMin?.toString(),
        item.armadura?.toString(),
        item.aparar?.toString(),
        item.observacoes?.toString(),
        item.dano?.toString(),
        item.pa?.toString(),
        item.cdt?.toString(),
        item.distancia?.toString(),
        item.tiros?.toString(),
        item.tamanho?.toString(),
        item.manobrabilidade?.toString(),
        item.velMaxima?.toString(),
        item.resistencia?.toString(),
        item.tripulacao?.toString(),
        item.pmf?.toString(),
        item.malfuncionamento?.toString(),
        item.tensao?.toString(),
        item.mods_slots?.toString()
    ).joinToString("|")
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> loadJsonAsset(context: Context, fileName: String): T {
    val json = Json { ignoreUnknownKeys = true }
    return context.assets.open(fileName).use { input ->
        json.decodeFromStream(input)
    }
}
