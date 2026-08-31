package com.example.swadebuilder.model

import android.content.Context
import android.util.Log
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.util.CustomCrystalHeartStorage
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
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

    private fun EquipamentoItem.comObservacoesExibidas(): EquipamentoItem =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) {
            copy(observacoes = JsonPrimitive(descricaoLite!!))
        } else this

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
        // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
        val descricaoLite: String? = null,
        val livros: List<String>,
        val id: String = ""
    )

    // Vantagens vivem em um único arquivo consolidado (vantagens.json), com cada registro
    // marcado por "livros". Vantagem tem muitos campos (grupoId, subtipoArcano, choiceOptions,
    // maxSelections, etc.) e pode ganhar novos com o tempo, então em vez de espelhar cada
    // campo em um tipo "Fonte" paralelo (arriscado — um campo esquecido silenciosamente vira
    // valor padrão), o arquivo é lido como JSON genérico: remove-se apenas "livros" e injeta-
    // se "origem", e o restante do objeto é decodificado direto pelo parser real de Vantagem.
    private fun vantagemFromRaw(raw: JsonObject, origin: String): Vantagem {
        val content = raw.toMutableMap()
        content.remove("livros")
        // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
        // Cai para "descricao" enquanto não for escrito, e nunca é usado na Full edition.
        val descricaoLite = (content.remove("descricaoLite") as? JsonPrimitive)?.takeIf { it.isString }?.content
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) {
            content["descricao"] = JsonPrimitive(descricaoLite)
        }
        content["origem"] = JsonPrimitive(origin)
        return json.decodeFromJsonElement(Vantagem.serializer(), JsonObject(content))
    }

    private fun livrosDe(raw: JsonObject): List<String> =
        (raw["livros"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content } ?: emptyList()

    // Complicações vivem em um único arquivo consolidado (complicacoes.json). Como em
    // Equipamentos/Ancestralidades/Poderes, quase nenhum id compartilhado entre livros tem
    // dados idênticos, então cada registro carrega apenas o(s) livro(s) exatos aos quais
    // pertence.
    @Serializable
    private data class ComplicacaoFonte(
        val id: String,
        val name: String,
        val originalName: String? = null,
        val originalDescription: String? = null,
        val severity: String,
        val description: String,
        // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
        val descricaoLite: String? = null,
        val observacoes: String = "",
        @kotlinx.serialization.SerialName("vantagens_previas")
        val vantagensPrevias: List<String> = emptyList(),
        val livros: List<String>
    ) {
        fun descricaoExibida(): String =
            if (!EditionConfig.isFullEdition) descricaoLite?.takeIf { it.isNotBlank() } ?: description else description
    }

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
        // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
        val descricaoLite: String? = null,
        val atributos: Map<String, Int>,
        val pericias: Map<String, Int>,
        val vantagensGratis: List<String> = emptyList(),
        val desvantagens: List<String> = emptyList(),
        val habilidades: List<RacialAbility> = emptyList(),
        val movimentacao: Int = 0,
        val tags: List<String> = emptyList(),
        val opcoes: List<String> = emptyList(),
        val livros: List<String>
    ) {
        fun descricaoExibida(): String? =
            if (!EditionConfig.isFullEdition) descricaoLite?.takeIf { it.isNotBlank() } ?: descricao else descricao
    }

    private fun RacialAbility.exibida(): RacialAbility =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite!!) else this

    // Poderes vivem em um único arquivo consolidado (poderes.json). Como em Equipamentos e
    // Ancestralidades, cada nome de poder compartilhado entre livros tem dados diferentes
    // (descrições e modificadores próprios por cenário), então cada registro carrega apenas
    // o(s) livro(s) exatos aos quais pertence.
    //
    // A consolidação também corrigiu um bug de dados grave: os poderes exclusivos de
    // scifi_poderes.json, horror_poderes.json e deadlands_poderes.json usavam campos errados
    // ("custo"/"alcance" em vez de "pontosDePoder"/"distancia") e não tinham "id". Como esses
    // campos são obrigatórios em Poder, a desserialização do arquivo INTEIRO falhava e o
    // DataLoader silenciosamente descartava todos os poderes daquele livro (não só os
    // quebrados) — ou seja, Sci-Fi e Horror perdiam os poderes exclusivos deles, e Deadlands
    // (que não herda o Básico) ficava sem NENHUM poder disponível. Os 51 registros afetados
    // foram corrigidos (campos renomeados, id gerado a partir do nome) antes da consolidação.
    @Serializable
    private data class PoderFonte(
        val id: String,
        val nome: String,
        val estagio: String,
        @Serializable(with = StringOrIntSerializer::class)
        val pontosDePoder: String,
        val distancia: String,
        val duracao: String,
        val manifestacoes: List<String> = emptyList(),
        val descricao: String,
        // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
        val descricaoLite: String? = null,
        val modificadores: List<Modificador> = emptyList(),
        val livros: List<String>
    )

    private fun PoderFonte.descricaoExibida(): String =
        if (!EditionConfig.isFullEdition) descricaoLite?.takeIf { it.isNotBlank() } ?: descricao else descricao

    // --- Loading Logic ---

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
                itens = cat.itens.map { it.comObservacoesExibidas() }
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
            (hearts.map { it.exibido() } + customHearts).distinctBy { it.id }
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
            supers.map { it.exibido() }
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
                // Na edição Lite, mostra o resumo genérico quando ele já foi escrito; enquanto
                // não for, cai para a descrição original (sem regressão visual).
                val descricao = if (!EditionConfig.isFullEdition) {
                    fonte.descricaoLite ?: fonte.descricao
                } else {
                    fonte.descricao
                }
                PericiaJson(
                    nome = fonte.nome,
                    atributo = fonte.atributo,
                    basica = fonte.basica,
                    origem = livro,
                    descricao = descricao,
                    id = fonte.id
                )
            }
        }

        val rawPericias = todasPericiasJson.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica,
                origem   = pj.origem,
                descricao = pj.descricao,
                id = pj.id
            )
        }

        val localListaPericias = rawPericias
        val localMapaPericias = localListaPericias.associateBy { it.nome.keyify() }

        // Na edição Lite, mostra o resumo genérico (sem reproduzir o texto do livro original)
        // quando ele já foi escrito; enquanto não for, cai para a descrição original.
        val localMapaAtributosDescricao = atributosData.atributos.associate {
            val texto = if (!EditionConfig.isFullEdition) {
                it.descricaoLite ?: it.descricao
            } else {
                it.descricao
            }
            it.nome.keyify() to (texto ?: "")
        }

        // 7. Vantagens
        val advantageVisibleOrigins = if (shouldReplaceBasico) nonBasicActiveKeys else keys

        @Suppress("UNCHECKED_CAST")
        val vantagensRawJson = dataCache.getOrPut("vantagens.json") {
            runCatching {
                assets.open("vantagens.json").use { input -> json.decodeFromStream<List<JsonObject>>(input) }
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar vantagens.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<JsonObject>

        val todasVantagens = vantagensRawJson.flatMap { raw ->
            livrosDe(raw).filter { it in advantageVisibleOrigins }.map { livro -> vantagemFromRaw(raw, livro) }
        }

        val localListaVantagens = buildList {
            addAll(todasVantagens)

            if (shouldReplaceBasico && none { it.id == "antecedente_arcano" }) {
                vantagensRawJson
                    .firstOrNull { raw -> raw["id"]?.let { (it as? JsonPrimitive)?.content } == "antecedente_arcano" && "BASICO" in livrosDe(raw) }
                    ?.let { add(vantagemFromRaw(it, "BASICO")) }
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
            cached.map { it.exibido() }
        } else emptyList()

        val chTropos = if ("CRYSTAL_HEART" in keys) {
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut("crystal_tropos.json") {
                runCatching { loadJsonAsset<List<Tropo>>(context, "crystal_tropos.json") }.getOrElse { emptyList<Tropo>() }
            } as List<Tropo>
            cached.map { it.exibido() }
        } else emptyList()

        val localListaTropos = adgTropos + chTropos

        val complicationVisibleOrigins = if (shouldReplaceBasico) nonBasicActiveKeys else keys

        @Suppress("UNCHECKED_CAST")
        val complicacoesFonte = dataCache.getOrPut("complicacoes.json") {
            runCatching {
                loadJsonAsset<List<ComplicacaoFonte>>(context, "complicacoes.json")
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar complicacoes.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<ComplicacaoFonte>

        val localListaComplicacoes = complicacoesFonte.flatMap { fonte ->
            fonte.livros.filter { it in complicationVisibleOrigins }.map { livro ->
                Complicacao(
                    id = fonte.id,
                    name = fonte.name,
                    originalName = fonte.originalName,
                    originalDescription = fonte.originalDescription,
                    severity = fonte.severity,
                    description = fonte.descricaoExibida(),
                    origem = livro,
                    observacoes = fonte.observacoes,
                    vantagensPrevias = fonte.vantagensPrevias
                )
            }
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
                    descricao = fonte.descricaoExibida(),
                    atributos = fonte.atributos,
                    pericias = fonte.pericias,
                    vantagensGratis = fonte.vantagensGratis,
                    desvantagens = fonte.desvantagens,
                    habilidades = fonte.habilidades.map { it.exibida() },
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
            monstros.map { it.exibido() }
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
        val powerVisibleOrigins = if (shouldReplaceBasico) nonBasicActiveKeys else keys

        @Suppress("UNCHECKED_CAST")
        val poderesFonte = dataCache.getOrPut("poderes.json") {
            runCatching {
                loadJsonAsset<List<PoderFonte>>(context, "poderes.json")
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar poderes.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<PoderFonte>

        val localListaPoderes = poderesFonte.flatMap { fonte ->
            fonte.livros.filter { it in powerVisibleOrigins }.map { livro ->
                Poder(
                    id = fonte.id,
                    nome = fonte.nome,
                    origem = livro,
                    estagio = fonte.estagio,
                    pontosDePoder = fonte.pontosDePoder,
                    distancia = fonte.distancia,
                    duracao = fonte.duracao,
                    manifestacoes = fonte.manifestacoes,
                    descricao = fonte.descricaoExibida(),
                    modificadores = fonte.modificadores
                )
            }
        }

        // 14. Custom Local Content per active book
        val customStorageManager = com.example.swadebuilder.util.CustomStorageManager()
        val customVantagens = mutableListOf<Vantagem>()
        val customComplicacoes = mutableListOf<Complicacao>()
        val customEquipamentos = mutableListOf<EquipamentoItem>()
        val customPoderes = mutableListOf<Poder>()
        val customSuperPoderes = mutableListOf<SuperPoder>()
        val customRacas = mutableListOf<RacialModifier>()
        val customVariantesRaciais = mutableListOf<CustomAncestryVariant>()

        // TAG_GERAL sempre entra, além dos livros realmente ativos: é onde fica
        // o conteúdo customizado que o jogador marcou como "Geral" na criação,
        // pra aparecer em qualquer combinação de livros.
        (keys + com.example.swadebuilder.util.TAG_GERAL).forEach { bookKey ->
            val customData = customStorageManager.loadCustomContent(context, bookKey)
            customVantagens += customData.vantagens
            customComplicacoes += customData.complicacoes
            customEquipamentos += customData.equipamentos
            customPoderes += customData.poderes
            customSuperPoderes += customData.superPoderes
            customRacas += customData.racas
            customVariantesRaciais += customData.variantesRaciais
        }

        // Usa distinctByOriginPriority (não distinctBy simples) porque um mesmo id/nome pode
        // existir em mais de um livro ativo ao mesmo tempo (Modo Livre, ou um livro
        // companheiro somado ao Básico) com conteúdo DIFERENTE por livro — distinctBy() ficaria
        // com a primeira ocorrência do arquivo (normalmente a do Básico), descartando em
        // silêncio a versão mais específica do outro livro.
        val mergedVantagens = (localListaVantagens + customVantagens).distinctByOriginPriority({ it.origem }, { it.id })
        val mergedComplicacoes = (localListaComplicacoes + customComplicacoes).distinctByOriginPriority({ it.origem }, { it.id })
        val mergedEquipamentos = (localListaEquipamentos + customEquipamentos).distinctByOriginPriority({ it.origem }, { it.nome.keyify() })
        val mergedPoderes = (localListaPoderes + customPoderes).distinctByOriginPriority({ it.origem }, { it.id })
        val mergedAncestralidades = (localListaAncestralidadesJson + customRacas).distinctByOriginPriority({ it.origem }, { it.nome.keyify() })
        // Só mescla Super Poderes customizados quando SUPER está ativo — mesmo
        // gate que já vale pro catálogo oficial (super_poderes.json só carrega
        // com "SUPER" em keys), já que o traço só faz sentido junto com o
        // Antecedente Arcano (Super Poderes) desse cenário.
        val mergedSuperPoderes = if ("SUPER" in keys) {
            (localListaSuperPoderes + customSuperPoderes).distinctBy { it.nome.keyify() }
        } else {
            localListaSuperPoderes
        }

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
            listaSuperPoderes = mergedSuperPoderes,
            arcanoInfo = loadedArcanoInfoList,
            listaVariantesRaciaisCustom = customVariantesRaciais
        )
    }

    // Usado por telas que precisam do catálogo de poderes de um livro específico
    // independentemente de qual livro está ativo no momento (ex.: a tela de Poderes mostra
    // as opções de um Antecedente Arcano concedido por uma vantagem de origem X, mesmo que o
    // personagem tenha outra origem ativa). Substitui a antiga leitura direta de
    // "${livro}_poderes.json" arquivo por arquivo — que quebrou quando os arquivos por livro
    // foram consolidados em poderes.json, e que já vinha com um bug próprio: as chaves do
    // mapa eram geradas a partir do nome do arquivo (ex.: "SCIFI", "CRYSTAL", "ARTE DA
    // GUERRA" com espaço) enquanto a busca usa a forma canônica (ex.: "SCI_FI",
    // "CRYSTAL_HEART", "ARTE_DA_GUERRA" com underscore) — a maioria das buscas por origem
    // nunca batia com o mapa. Aqui as chaves já nascem no formato canônico usado por
    // powerAssetOriginKey().
    fun poderesPorOrigem(context: Context): Map<String, List<Poder>> {
        @Suppress("UNCHECKED_CAST")
        val fonte = dataCache.getOrPut("poderes.json") {
            runCatching {
                loadJsonAsset<List<PoderFonte>>(context, "poderes.json")
            }.getOrElse { e ->
                Log.e("SWADE_DEBUG", "[DataLoader] falha ao carregar poderes.json: ${e::class.simpleName}: ${e.message}", e)
                emptyList()
            }
        } as List<PoderFonte>

        val map = mutableMapOf<String, MutableList<Poder>>()
        fonte.forEach { f ->
            f.livros.forEach { livroRaw ->
                val livro = powerAssetOriginKey(livroRaw)
                map.getOrPut(livro) { mutableListOf() }.add(
                    Poder(
                        id = f.id,
                        nome = f.nome,
                        origem = livro,
                        estagio = f.estagio,
                        pontosDePoder = f.pontosDePoder,
                        distancia = f.distancia,
                        duracao = f.duracao,
                        manifestacoes = f.manifestacoes,
                        descricao = f.descricaoExibida(),
                        modificadores = f.modificadores
                    )
                )
            }
        }
        return map
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
