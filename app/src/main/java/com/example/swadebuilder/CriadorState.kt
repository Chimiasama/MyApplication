package com.example.swadebuilder

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.swadebuilder.model.AdvantageSnapshot
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.ComplicacaoSnapshot
import com.example.swadebuilder.model.EquipFilter
import com.example.swadebuilder.model.EquipSuperType
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.ModifierEngine
import com.example.swadebuilder.model.ModifierTarget
import com.example.swadebuilder.model.PersonagemSnapshot
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.SnapshotAtributos
import com.example.swadebuilder.model.SnapshotFlags
import com.example.swadebuilder.model.SnapshotPericias
import com.example.swadebuilder.model.SnapshotProgresso
import com.example.swadebuilder.model.SnapshotRecursos
import com.example.swadebuilder.model.SnapshotSelecoes
import com.example.swadebuilder.model.SnapshotSupers
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.VantFilter
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

enum class TabStyle { ICONES, TEXTO }

class CriadorState {
    var appTheme by mutableStateOf(AppTheme.DEFAULT)
    var mostrarIdentificadorLivro by mutableStateOf(true)
    var estiloAbas by mutableStateOf(TabStyle.TEXTO)
    var mostrarDescricaoHome by mutableStateOf(true)
    var hapticStrength by mutableIntStateOf(DEFAULT_HAPTIC_STRENGTH)
    var soundVolume by mutableIntStateOf(DEFAULT_SOUND_VOLUME)
    var showSystemMessages by mutableStateOf(true)
    var modoSupers by mutableStateOf(false)
    var compendioFantasiaAtivo by mutableStateOf(false)
    var compendioHorrorAtivo by mutableStateOf(false)
    var compendioSciFiAtivo by mutableStateOf(false)
    var compendioScifiMechasCiberneticosAtivo by mutableStateOf(false)
    var compendioPathfinderAtivo by mutableStateOf(false)
    var compendioDeadlandsAtivo by mutableStateOf(false)
    var compendioCrystalHeartAtivo by mutableStateOf(false)
    var compendioArteDaGuerraAtivo by mutableStateOf(false)
    var compendioCidadeSolVaporAtivo by mutableStateOf(false)
    var compendioWiseguysAtivo by mutableStateOf(false)
    var optRegraRiqueza by mutableStateOf(false)
    var optRegraCosaNostra by mutableStateOf(false)
    var optRegraFama by mutableStateOf(false)
    var modoOficialAtivo by mutableStateOf(false)
    var modoMonstroAtivo by mutableStateOf(false)
    var tipoMonstroSelecionado by mutableStateOf<String?>(null)
    var grandesResponsabilidades by mutableStateOf(false)
    var signoAdgSelecionado by mutableStateOf<String?>(null)
    var protagonistaRollTecnicas by mutableStateOf<Int?>(null)
    var protagonistaRollPericia by mutableStateOf<Int?>(null)
    var protagonistaRollVantagem by mutableStateOf<Int?>(null)
    var protagonistaRollQualidade by mutableStateOf<Int?>(null)
    var protagonistaRollHabilidade by mutableStateOf<Int?>(null)
    var protagonistaPericiasEscolhidas by mutableStateOf<List<String>>(emptyList())
    var protagonistaPericiasPaixao by mutableStateOf<List<String>>(emptyList())
    var protagonistaBonusPv by mutableStateOf(false)
    var artistaMarcialJutsuOpcao by mutableStateOf(ARTISTA_MARCIAL_JUTSU_D6)
    var artistaMarcialPotencialFisico by mutableStateOf<String?>(null)
    val artistaMarcialTecnicasSelecionadas = mutableStateListOf<String>()
    var buXistaCaminhoSelecionado by mutableStateOf<String?>(null)
    var elementalistaElementoSelecionado by mutableStateOf<String?>(null)
    var descendenteElementalSelecionado by mutableStateOf<String?>(null)
    var gnomoPericiaEscolhida by mutableStateOf<String?>(null)
    var signoSerpentePericiaEscolhida by mutableStateOf("Jogar")
    var dominioClerigoSelecionado by mutableStateOf<String?>(null)
    var dominioClerigoPathfinderSelecionado by mutableStateOf<String?>(null)

    fun getActiveModuleKeys(): Set<String> {
        val keys = mutableSetOf<String>()
        if (compendioFantasiaAtivo) keys.add("FANTASIA")
        if (compendioHorrorAtivo) keys.add("HORROR")
        if (compendioSciFiAtivo) keys.add("SCI_FI")
        if (compendioPathfinderAtivo) keys.add("PATHFINDER")
        if (compendioDeadlandsAtivo) keys.add("DEADLANDS")
        if (compendioCrystalHeartAtivo) keys.add("CRYSTAL_HEART")
        if (compendioArteDaGuerraAtivo) keys.add("ARTE_DA_GUERRA")
        if (compendioCidadeSolVaporAtivo) keys.add("CIDADE_SOL_VAPOR")
        if (compendioWiseguysAtivo) keys.add("WISEGUYS")
        if (modoSupers) keys.add("SUPER")
        return keys
    }

    fun getMonstroSelecionado(): com.example.swadebuilder.model.MonstroTemplate? {
        if (!modoMonstroAtivo || tipoMonstroSelecionado == null) return null
        return listaMonstroTemplates.firstOrNull { it.id == tipoMonstroSelecionado }
    }

    fun aplicarTipoMonstro(novoId: String?) {
        tipoMonstroSelecionado = novoId
        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
    }

    fun getAncestralidadeDef(name: String): com.example.swadebuilder.model.RacialModifier? {
        val key = name.keyify()
        val candidates = listaAncestralidadesJson.filter { it.nome.keyify() == key }
        if (candidates.isEmpty()) return null
        if (candidates.size == 1) return candidates.first()

        val activeCandidates = candidates.filter { item ->
            val origin = item.origem.uppercase()
            when (origin) {
                "FANTASIA" -> compendioFantasiaAtivo
                "HORROR" -> compendioHorrorAtivo
                "ARTE_DA_GUERRA" -> compendioArteDaGuerraAtivo
                "DEADLANDS" -> compendioDeadlandsAtivo
                "WISEGUYS" -> compendioWiseguysAtivo
                "CIDADE_SOL_VAPOR" -> compendioCidadeSolVaporAtivo
                "CRYSTAL_HEART" -> compendioCrystalHeartAtivo
                "FC", "SCIFI" -> compendioSciFiAtivo
                else -> {
                    if (origin.contains("TRILHADOR") || origin.contains("PATHFINDER")) compendioPathfinderAtivo
                    else true // BASICO or others
                }
            }
        }

        if (activeCandidates.isEmpty()) return candidates.firstOrNull()

        return activeCandidates.maxByOrNull { getOriginPriority(it.origem) }
    }

    fun isAttributeRankLimitReached(): Boolean {
        val stageIndex = currentProgressStageIndex()
        val lendarioIndex = listaDeEstagios.indexOfFirst { it.nome.equals("Lendário", ignoreCase = true) }
            .takeIf { it >= 0 } ?: listaDeEstagios.lastIndex
        val totalAttrPurchases = comprasAttrPorEstagio.values.sum()
        val baseAllowance = (stageIndex + 1).coerceAtMost(lendarioIndex)
        val remainingBaseAttrs = (baseAllowance - totalAttrPurchases).coerceAtLeast(0)
        return remainingBaseAttrs <= 0
    }

    fun isAttributeFreeForMonster(attr: String): Boolean {
        if (!modoMonstroAtivo) return false
        val key = attr.keyify()
        return key == "AGILIDADE" || key == "FORCA" || key == "VIGOR"
    }

    val vantagensAutomaticasDoSigno = mutableStateListOf<String>()
    val vantagensAutomaticasDoElemento = mutableStateListOf<String>()
    val vantagensAutomaticasDoPotencialFisico = mutableStateListOf<String>()

    val fixedPowersByArcano = mapOf(
        "ABENCOADO" to listOf("simbolo_sagrado"),
        "MESTRE DO CHI" to listOf("deflexao"),
        "BARDO" to listOf("aumentar_reduzir_caracteristica", "som_silencio"),
        "CLERIGO" to listOf("cura", "santuario"),
        "DIABOLISTA" to listOf("banir", "devastacao", "conjurar_aliado"),
        "DRUIDA" to listOf("amigo_das_feras", "protecao_ambiental", "mudanca_de_forma"),
        "ELEMENTALISTA" to listOf("manipulacao_elemental", "protecao_ambiental"),
        "ILUSIONISTA" to listOf("ilusao", "iluminar_obscurecer", "som_silencio"),
        "INVOCADOR" to listOf("amigo_das_feras", "aumentar_reduzir_caracteristica", "conjurar_aliado"),
        "MAGO" to listOf("detectar_ocultar_arcano", "dissipar", "trancar_destrancar"),
        "NECROMANTE" to listOf("detectar_ocultar_arcano", "dissipar", "zumbi"),
        "XAMA" to listOf("protecao_arcana", "ajuda"),
        "MISTICO_BARBARO" to listOf("aumentar_reduzir_caracteristica", "ferir", "morosidade_velocidade"),
        "MISTICO_GUERREIRO" to listOf("aumentar_reduzir_caracteristica", "ferir", "protecao"),
        "MISTICO_LADRAO" to listOf("andar_nas_paredes", "aumentar_reduzir_caracteristica", "trancar_destrancar", "visao_sombria"),
        "MISTICO_MONGE" to listOf("aumentar_reduzir_caracteristica", "deflexao", "ferir"),
        "MISTICO_PALADINO" to listOf("aumentar_reduzir_caracteristica", "cura", "ferir", "protecao", "santuario"),
        "MISTICO_PATRULHEIRO" to listOf("amigo_das_feras", "aumentar_reduzir_caracteristica", "enredar", "visao_distante")
    )

    fun isFixedPower(arcanoKey: String, powerId: String?): Boolean {
        if (powerId == null) return false
        val fixedList = fixedPowersByArcano[arcanoKey.normAAKey()] ?: return false
        return fixedList.contains(powerId)
    }

    companion object {
        fun getOriginPriority(origin: String?): Int {
            val o = origin?.uppercase() ?: "BASICO"
            return when {
                o == "HORROR" -> 1000
                o == "FANTASIA" -> 900
                o == "ARTE_DA_GUERRA" -> 800
                o == "DEADLANDS" -> 800
                o == "WISEGUYS" -> 800
                o == "CIDADE_SOL_VAPOR" -> 800
                o.contains("TRILHADOR") || o.contains("PATHFINDER") -> 800
                o == "FC" || o == "SCIFI" || o == "SCI_FI" -> 800
                o == "CRYSTAL_HEART" -> 800
                o == "BASICO" -> 0
                else -> 100
            }
        }

        const val BASE_SP_POOL = 15
        const val DEFAULT_HAPTIC_STRENGTH = 70
        const val DEFAULT_SOUND_VOLUME = 70
        const val ARTISTA_MARCIAL_JUTSU_D6 = "D6"
        const val ARTISTA_MARCIAL_JUTSU_D4_D4 = "D4_D4"
        val SIGNOS_ADG = listOf(
            "Nenhum", "Basabasa", "Boi", "Tigre", "Lebre", "Garça", "Serpente",
            "Dragão", "Kirin", "Macaco", "Raposa", "Lobo", "Tartaruga", "Urso"
        )
        val SIGNOS_ADG_DESC = mapOf(
            "Nenhum" to "Sem signo de nascença. Você mantém os benefícios de Humano Adaptável (15 pontos de perícia e 1 PV).",
            "Basabasa" to "Aqueles que nasceram no primeiro mês sob o signo de Basabasa geralmente são indivíduos honestos e ambiciosos, conhecidos por uma beleza sobrenatural. Tal alinhamento celestial é ofuscado por uma oscilação de humores excêntricos. Começam as coisas com entusiasmo e logo perdem o interesse, tornando-se voláteis. Um Basabasa tem a Vantagem Atraente e escolhe na criação do personagem entre adicionar +1 às rolagens de Provocar ou Intimidar contra alvos que se sintam atraídos ou desprezem o Herói.",
            "Boi" to "Aqueles que nasceram sob o signo do Boi são grandes e imponentes, conhecidos por serem diretos e persistentes. Falhas comuns incluem teimosia, franqueza excessiva e inabilidade em expressar emoções. Um Boi recebe +1 em rolagens em Atletismo quando utilizado em situações que podem exigir Força (como escalar ou nadar). Caso o personagem possua a Vantagem Brutamontes, esse benefício se aplica a todas as rolagens de Atletismo. Além disso, este benefício aumenta a Força em um tipo de dado e aumenta seu limite máximo no atributo em d12+1.",
            "Tigre" to "A herança do signo do Tigre faz com que se tornem destemidos e precisos, realizando atos cavalheirescos dignos de respeito enquanto assumem a liderança. Tigres são naturalmente temperamentais. Em um papel de liderança ou posição de autoridade, tomarão decisões para obter o melhor resultado possível, sem considerar o efeito sobre os outros. Um Tigre tem um alcance de comando de +4 quadros, adiciona +1 nas rolagens de Medo e subtrai 1 dos resultados da Tabela de Medo (isso acumula com a Vantagem Corajoso).",
            "Lebre" to "Heróis nascidos sob o signo da Lebre exibem qualidades gentis, amáveis e compassivas, com um toque de modéstia. Lebres podem demonstrar características comportamentais de sonhar acordado, escapismo, falta de perspectiva ou timidez em interações sociais. Uma Lebre tem um toque natural (Cura d6) e, com os suprimentos médicos adequados, pode gastar um Bene para tratar um Ferimento com horas ou dias (até 4 dias), como se estivesse sendo tratada dentro da Hora de Ouro. Um personagem pode se beneficiar desse tratamento uma única vez por aventura.",
            "Garça" to "Nascidos com o signo da Garça, os indivíduos buscam uma vida de perfeito equilíbrio entre os altos e baixos que ela oferece. Uma Garça é agraciada com graça em seus movimentos e se destaca contra adversários em todas as formas. A Garça possui a fraqueza da insegurança e depende muito dos outros em momentos de dúvida. As garças recebem +1 em Aparar, d4 em Acrobacia e aumentam Atletismo em um tipo de dado.",
            "Serpente" to "Muitos veem a serpente como astuta e sorrateira, no entanto, o signo da Serpente é um símbolo de sabedoria e mantém um alto nível de astúcia. Serpentes são consideradas sensíveis e emotivas, a maioria é talentosa nas artes. Com essa sensibilidade vem a hesitação e pequenos surtos de leve paranoia. Uma Serpente começa com Jogar d6 ou Performance d6. Usando Jogar, uma Serpente adiciona +1 ao total da diferença se vencer e -1 ao total da diferença se perder. Usando Performance para captação de recursos, altera a porcentagem para 30% e 40% com um sucesso.",
            "Dragão" to "Nascidos sob o signo do Dragão, os indivíduos são respeitados por serem animados, pacientes e sábios em sua experiência. Muitos dos melhores estrategistas da história são do signo do Dragão. Os dragões tendem a odiar hipocrisia, fofocas e calúnias, e desprezam ser usados ou controlados pelos outros. O Dragão aumenta seu Espírito em um tipo de dado e aumenta seu máximo neste atributo para d12+1. Dragões se beneficiam de +1 em rolagens de Conhecimento Geral quando estão em situações desconhecidas.",
            "Kirin" to "O nascimento de um Kirin coincide com o final da estação de verão à medida que se aproxima o outono, representando um tempo de coleta e colheita. Um Kirin é proativo e percebe a malícia dos outros por meio de ações independentes. No entanto, um Kirin tende a fazer o que é necessário por conta própria, desconfiando que os outros cumpram suas obrigações. Um Kirin precisa de um incentivo a mais para prosseguir, começando com +1 em sua Reserva de Chi e uma Bene adicional em cada sessão.",
            "Macaco" to "O signo do Macaco está associado ao ser cheio de vida, de raciocínio rápido e versátil. Um Macaco é conhecido por tirar o máximo de qualquer situação, mas muitas vezes olha com menosprezo àqueles que não aprendem rapidamente. Muitas vezes, um temperamento impetuoso será a causa das ações de um Macaco. Com este signo de nascença, a Astúcia de um Macaco aumenta em um tipo de dado e seu máximo em aumenta d12+1. Um Macaco rola d4+1 nas perícias não treinadas baseadas em Astúcia, este bônus não se aplica ao dado selvagem.",
            "Raposa" to "Dizem que a Raposa possui uma intuição incrível. Capaz de ler situações sociais e saber exatamente o que as outras pessoas precisam ouvir. Isso não quer dizer que a Raposa seja falsa, é uma demonstração de habilidade e grande cuidado em aspectos de \"Manter as Aparências\". Traição e confiança são preocupações comuns de uma Raposa, levando-a a questionar a lealdade e a amizade de outros. Uma Raposa começa com a Vantagem Elevar a Moral e recebe +1 em Persuadir e nas rolagens da Tabela de Reação.",
            "Lobo" to "Um lobo é um animal social que se sente em casa quando pertence a uma matilha, assim como é verdadeiro para aqueles nascidos sob o signo do Lobo. Um Lobo exibe risos, alegria e comportamento solidário entre amigos, preferindo estar em companhia a sobreviver sozinho. Um Lobo pode sobreviver sozinho, mas prospera dentro de um grupo. Um Lobo começa com as Vantagens Elo Comum e adiciona +1 nas rolagens da Tabela de Reação para Reação Inicial..",
            "Tartaruga" to "Uma Tartaruga de casca dura é vista como lenta e covarde pelos outros, no entanto, uma Tartaruga possui mais longevidade, paciência e consciência do que aqueles que estão à sua volta. Hesitações na hora de tomar decisões frequentemente fazem uma Tartaruga perder oportunidades. Nascer sob o signo da Tartaruga concede +1 à Resistência. Aqueles que tentarem realizar a manobra “Finalização” em uma Tartaruga recebem -1 nas rolagens de ataque e dano na tentativa.",
            "Urso" to "Nascido no inverno, um Urso é considerado focado nas necessidades de sobrevivência. Na verdade, um Urso é centrado na família e focado na sobrevivência de cada membro. Isso pode significar que um Urso seja isolacionista e indiferente àqueles que não conhece. Por essa razão, o Vigor de um Urso aumenta em um tipo de dado e seu máximo aumenta para d12+1. Ursos reduzem a penalidade recebida de Exausto para -1 em vez de -2."
        )
    }
    var maisPontosPericias by mutableStateOf(true)
    var cartaSelvagem       by mutableStateOf(true)
    var dinheiro by mutableIntStateOf(500)
    var requisicao by mutableIntStateOf(1)
    val carteiraPathfinder = mutableStateMapOf("PL" to 0, "PO" to 0, "PP" to 0, "PC" to 0)

    fun updateTotalPathfinderMoney() {
        if (!compendioPathfinderAtivo) return
        val pl = carteiraPathfinder["PL"] ?: 0
        val po = carteiraPathfinder["PO"] ?: 0
        val pp = carteiraPathfinder["PP"] ?: 0
        val pc = carteiraPathfinder["PC"] ?: 0
        dinheiro = (pl * 1000) + (po * 100) + (pp * 10) + pc
    }

    fun addPathfinderMoney(amountInCopper: Int) {
        if (amountInCopper <= 0) return

        // Strategy: Maximize PO (Gold), avoid PL (Platinum).
        // 1 PO = 100 CP. 1 PP = 10 CP.

        var remaining = amountInCopper

        // Add PO
        val poToAdd = remaining / 100
        remaining %= 100

        // Add PP
        val ppToAdd = remaining / 10
        remaining %= 10

        // Add PC
        val pcToAdd = remaining

        if (poToAdd > 0) carteiraPathfinder["PO"] = (carteiraPathfinder["PO"] ?: 0) + poToAdd
        if (ppToAdd > 0) carteiraPathfinder["PP"] = (carteiraPathfinder["PP"] ?: 0) + ppToAdd
        if (pcToAdd > 0) carteiraPathfinder["PC"] = (carteiraPathfinder["PC"] ?: 0) + pcToAdd

        updateTotalPathfinderMoney()
    }

    fun spendPathfinderMoney(amountInCopper: Int): Boolean {
        if (amountInCopper <= 0) return true // No cost
        updateTotalPathfinderMoney() // Ensure sync
        if (dinheiro < amountInCopper) return false

        var costRemaining = amountInCopper

        // 1. Spend PC
        val currentPC = carteiraPathfinder["PC"] ?: 0
        if (currentPC >= costRemaining) {
            carteiraPathfinder["PC"] = currentPC - costRemaining
            updateTotalPathfinderMoney()
            return true
        } else {
            // Spend all PC
            carteiraPathfinder["PC"] = 0
            costRemaining -= currentPC
        }

        // 2. Spend PP (1 PP = 10 PC)
        // Need X CP.
        // 1 PP covers 10 CP.
        // We need ceil(costRemaining / 10.0) PPs.
        val neededPP = (costRemaining + 9) / 10
        val currentPP = carteiraPathfinder["PP"] ?: 0

        if (currentPP >= neededPP) {
            carteiraPathfinder["PP"] = currentPP - neededPP
            val change = (neededPP * 10) - costRemaining
            if (change > 0) {
                carteiraPathfinder["PC"] = (carteiraPathfinder["PC"] ?: 0) + change
            }
            updateTotalPathfinderMoney()
            return true
        } else {
            // Spend all PP
            carteiraPathfinder["PP"] = 0
            costRemaining -= (currentPP * 10)
        }

        // 3. Spend PO (1 PO = 100 PC)
        val neededPO = (costRemaining + 99) / 100
        val currentPO = carteiraPathfinder["PO"] ?: 0

        if (currentPO >= neededPO) {
            carteiraPathfinder["PO"] = currentPO - neededPO
            val changeTotal = (neededPO * 100) - costRemaining
            // Change needs to be broken down into PP and PC
            val changePP = changeTotal / 10
            val changePC = changeTotal % 10

            if (changePP > 0) carteiraPathfinder["PP"] = (carteiraPathfinder["PP"] ?: 0) + changePP
            if (changePC > 0) carteiraPathfinder["PC"] = (carteiraPathfinder["PC"] ?: 0) + changePC

            updateTotalPathfinderMoney()
            return true
        } else {
            // Spend all PO
            carteiraPathfinder["PO"] = 0
            costRemaining -= (currentPO * 100)
        }

        // 4. Spend PL (1 PL = 1000 PC)
        val neededPL = (costRemaining + 999) / 1000
        val currentPL = carteiraPathfinder["PL"] ?: 0

        if (currentPL >= neededPL) {
            carteiraPathfinder["PL"] = currentPL - neededPL
            val changeTotal = (neededPL * 1000) - costRemaining

            // Change breakdown (PO, PP, PC)
            val changePO = changeTotal / 100
            val rem1 = changeTotal % 100
            val changePP = rem1 / 10
            val changePC = rem1 % 10

            if (changePO > 0) carteiraPathfinder["PO"] = (carteiraPathfinder["PO"] ?: 0) + changePO
            if (changePP > 0) carteiraPathfinder["PP"] = (carteiraPathfinder["PP"] ?: 0) + changePP
            if (changePC > 0) carteiraPathfinder["PC"] = (carteiraPathfinder["PC"] ?: 0) + changePC

            updateTotalPathfinderMoney()
            return true
        }

        // Should not reach here if dinheiro check passed
        return false
    }

    fun compactPathfinderMoney() {
        updateTotalPathfinderMoney()
        var remaining = dinheiro

        val pl = remaining / 1000
        remaining %= 1000
        val po = remaining / 100
        remaining %= 100
        val pp = remaining / 10
        val pc = remaining % 10

        carteiraPathfinder["PL"] = pl
        carteiraPathfinder["PO"] = po
        carteiraPathfinder["PP"] = pp
        carteiraPathfinder["PC"] = pc
    }

    var famaManual by mutableIntStateOf(0)
    val poderesSelecionados = mutableStateListOf<String>()
    val manifestacoesPoderes = mutableStateMapOf<String, String>()
    val equipamentosComprados = mutableStateListOf<EquipamentoItem>()
    private val _maxedTraits = mutableStateListOf<String>()
    val maxedTraits: List<String> get() = _maxedTraits
    var idAtual by mutableStateOf<String?>(null)

    // UI State Persistence
    var vantSearchQuery by mutableStateOf("")
    val vantSelectedCategories = mutableStateListOf<Categoria>()
    var vantFilter by mutableStateOf(VantFilter())

    var equipSearchQuery by mutableStateOf("")
    val equipSelectedSuperTypes = mutableStateListOf<EquipSuperType>()
    var equipFilter by mutableStateOf(EquipFilter())
    val equipExpandedTypes = mutableStateMapOf<String, Boolean>()
    var equipSectionFilters = mutableStateMapOf<EquipSuperType, Set<String>>()

    var anotacoes by mutableStateOf("")
    var portraitFileName by mutableStateOf<String?>(null)
    // expandirRetrato mantido para compatibilidade, mas o UI deve usar portraitScaleType
    var expandirRetrato by mutableStateOf(false)
    var portraitScaleType by mutableStateOf("CROP") // CROP, FIT
    var portraitAlignment by mutableStateOf("CENTER") // TOP, CENTER, BOTTOM

    var coracaoCrystalSelecionado by mutableStateOf<com.example.swadebuilder.model.CrystalHeart?>(null)

    val comprasPpPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    val superInvestments = mutableStateListOf<SuperInvestment>()
    var superNivelCampanha by mutableStateOf<Int?>(null)
    var usarSemPontosDePoder by mutableStateOf(false)

    var superPontosTotais by mutableIntStateOf(0)
    var superPontosDisponiveis by mutableIntStateOf(0)
    var superLimite by mutableIntStateOf(0)
    var superLimitePorPoder by mutableIntStateOf(0)
    var poderFavoritoId by mutableStateOf<String?>(null)
    val oMelhorQueHaSelecionada by derivedStateOf {
        vantagensSelecionadas.any { it.id == "o_melhor_que_ha" }
    }
    val limitePorPoderPadrao: Int
        get() = kotlin.math.floor(superPontosTotais / 3.0).toInt()
    val limiteFavorecido: Int
        get() = kotlin.math.ceil(superPontosTotais / 2.0).toInt()
    var limiteDePoderDaCampanha by mutableIntStateOf(Int.MAX_VALUE)

    var faseSupersAtiva by mutableStateOf(false)

    var bonusApararFromPower by mutableIntStateOf(0)
    var bonusResFromPower  by mutableIntStateOf(0)
    var armorFromPower     by mutableIntStateOf(0)

    var bonusMovimentacaoFromPower by mutableIntStateOf(0)

    val vantagensDePoder   = mutableStateSetOf<String>()
    val gastosPorPoder     = mutableStateMapOf<String, Int>()
    var naturalArmorFromRace by mutableIntStateOf(0)
    var soldadoCargaAtivo by mutableStateOf(true)

    // PROMPT 3: Transtornos Gratuitos (Horror Mode)
    val transtornos: SnapshotStateList<Complicacao> = mutableStateListOf()
    // PROMPT 5: Notas de Perícia (Sci-Fi / Wiseguys)
    val notasPericia: SnapshotStateMap<String, String> = mutableStateMapOf()

    val origemPersonagem: String?
        get() = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == ancestralidade }
            ?.origem
            ?.uppercase()

    val usaRiqueza: Boolean
        get() = optRegraRiqueza || origemPersonagem == "WISEGUYS" || optRegraCosaNostra

    val usaRequisicao: Boolean
        get() = compendioCrystalHeartAtivo || origemPersonagem == "CRYSTAL_HEART"

    var riquezaModifier by mutableIntStateOf(0)

    val dadoRiqueza: Int
        get() {
            var die = 6
            val hasPodreDeRico = vantagensSelecionadas.any { it.nome.keyify() == "PODRE DE RICO" }
            val hasRico = vantagensSelecionadas.any { it.nome.keyify() == "RICO" }
            val hasPobreza = complicacoesSelecionadas.keys.any { it.id.keyify() == "POBREZA" }

            die = when {
                hasPodreDeRico -> 10
                hasRico -> 8
                else -> 6
            }

            if (hasPobreza) {
                die = 4
            }

            val steps = listOf(4, 6, 8, 10, 12)
            val baseIndex = steps.indexOf(die).takeIf { it >= 0 } ?: 1
            val finalIndex = (baseIndex + riquezaModifier).coerceIn(0, steps.lastIndex)

            return steps[finalIndex]
        }

    fun aplicarRegrasWiseguys() {
        if (!optRegraCosaNostra) return

        // Conexões (Máfia)
        val conexoes = listaVantagens.firstOrNull { it.id == "conexoes" }
        if (conexoes != null) {
            val v = conexoes.copy()
            v.choice = "Máfia"
            // Verifica se já não tem essa vantagem com essa escolha
            val jaTem = vantagensSelecionadas.any { it.id == "conexoes" && it.choice.equals("Máfia", ignoreCase = true) }
            if (!jaTem) {
                vantagensSelecionadas.add(v)
                // Marca como automática para não ser removível ou custar pontos
                vantagensAutomaticas.add(v.nome)
            }
        }

        // Obrigação (Maior) - Servir a Máfia
        val obrigacao = listaComplicacoes.firstOrNull { it.id == "obrigacao" }
        if (obrigacao != null) {
            val jaTem = complicacoesSelecionadas.keys.any { it.id == "obrigacao" }
            if (!jaTem) {
                // Adiciona como Maior
                complicacoesSelecionadas[obrigacao] = "Maior"
                desvantagensAutomaticas.add(obrigacao.name)
                // Nota: O texto "servir a Máfia" idealmente iria nas anotações ou como um custom field,
                // mas a estrutura de Complicacao é rígida. Vamos assumir que o usuário entende pelo contexto Wiseguys.
                if (!anotacoes.contains("Obrigação: Servir a Máfia")) {
                    anotacoes += "\n• Obrigação: Servir a Máfia"
                }
            }
        }
    }

    fun adicionarVantagemCavaleiro(vant: Vantagem, armorChoice: String) {
        adicionarVantagem(vant)

        val targets = mutableListOf<String>()
        targets.add("Cavalo de Guerra")
        targets.add("Lança")
        targets.add("Espada Longa")
        targets.add("Escudo (Médio)")

        if (armorChoice.contains("Completa", true)) {
            targets.add("Armadura de Placas")
        } else {
            targets.add("Cota de Malha")
        }

        fun findItem(target: String): EquipamentoItem? {
            val tKey = target.keyify()
            listaEquipamentos.firstOrNull { it.nome.keyify() == tKey }?.let { return it }

            if (tKey.contains("CAVALO")) {
                return listaEquipamentos.firstOrNull {
                    val k = it.nome.keyify()
                    k.contains("CAVALO") && k.contains("GUERRA")
                }
            }
            if (tKey.contains("LANCA")) {
                listaEquipamentos.firstOrNull { it.nome.equals("Lança", true) }?.let { return it }
                return listaEquipamentos.firstOrNull { it.nome.keyify().contains("LANCA") }
            }
            if (tKey.contains("ESCUDO")) {
                return listaEquipamentos.firstOrNull {
                    val k = it.nome.keyify()
                    k.contains("ESCUDO") && k.contains("MEDIO")
                }
            }
            return null
        }

        targets.forEach { t ->
            val itemProto = findItem(t)
            if (itemProto != null) {
                val newItem = itemProto.copy(
                    custo = kotlinx.serialization.json.JsonPrimitive(0),
                    origemGrant = "CAVALEIRO"
                )
                equipamentosComprados.add(newItem)
            }
        }
    }

    fun valorMovimentacao(): Int {
        val base = 6
        val mods = ModifierEngine.sum(this, ModifierTarget.PACE)
        return (base + mods).coerceAtLeast(0)
    }

    fun totalTensaoEquipamentos(): Int = totalTensaoCibernetica()

    fun totalTensaoCibernetica(): Int =
        equipamentosComprados.sumOf { it.tensao ?: 0 }

    fun totalTensaoAtual(): Int = totalTensaoCibernetica()

    fun valorLimiteTensao(): Pair<Int, Int> {
        val espirito = valoresAtributos["ESPIRITO"]?.intValue ?: 4
        val vigor = valoresAtributos["VIGOR"]?.intValue ?: 4

        // Base = half the die type of lower attribute (Standard: Die/2)
        // Or if we treat die value as the number: (e.g. d6 -> 6). 6/2 = 3.
        // My intValue stores 4, 6, 8, 10, 12. So directly dividing by 2 works.
        val lower = minOf(espirito, vigor)
        var baseLimit = lower / 2
        var maxLimit = baseLimit * 2 // Implied by 3->6 example

        // Modifiers
        val hasCibertolerancia = vantagensSelecionadas.any { it.nome.keyify() == "CIBERTOLERANCIA" }
        val hasCibersamurai = vantagensSelecionadas.any { it.nome.keyify() == "CIBERSAMURAI" }
        val hasCiborgue = vantagensSelecionadas.any { it.nome.keyify() == "CIBORGUE" }
        val hasCiberResistencia = complicacoesSelecionadas.keys.any { it.id.keyify() == "CIBER_RESISTENCIA" }

        if (hasCibertolerancia) {
            baseLimit += 2
            maxLimit += 2
        }
        if (hasCibersamurai) {
            baseLimit += 2
            maxLimit += 2
        }
        if (hasCiborgue) {
            baseLimit += 4
            maxLimit += 4
        }
        if (hasCiberResistencia) {
            baseLimit -= 2
        }

        return baseLimit.coerceAtLeast(0) to maxLimit.coerceAtLeast(0)
    }

    fun totalSlotsMecha(): Int =
        equipamentosComprados.sumOf { (it.mods_slots as? JsonPrimitive)?.content?.toIntOrNull() ?: 0 }

    fun isPersonagemRobotico(): Boolean {
        val ancestral = listaAncestralidadesJson.firstOrNull { it.nome.keyify() == ancestralidade }
        val nomeKey = (ancestral?.nome ?: ancestralidade).keyify()
        val robotByName = listOf("ANDROID", "CONSTRUTO", "CONSTRUCTO").any { nomeKey.contains(it) }
        val robotBySkill = ancestral?.habilidades?.any { it.nome.keyify() == "MODIFICACOES" } == true
        val robotByAdvantage = ancestral?.vantagensGratis?.any { it.keyify() == "CONSTRUTO" } == true

        return robotByName || robotBySkill || robotByAdvantage
    }

    fun limiteModsRoboticos(): Int {
        val baseSlots = (valorTamanho() + 1).coerceAtLeast(1)
        val bonusSlots = vantagensSelecionadas.count {
            val nomeKey = it.nome.keyify()
            nomeKey.contains("MODS") && nomeKey.contains("ROBOT")
        }
        return (baseSlots + bonusSlots).coerceAtLeast(0)
    }

    fun valorAparar(): Int {
        val perLutar = mapaPericias["LUTAR"]
        val perJutsu = mapaPericias["JUTSU"]
        val lutarRaw = perLutar?.let { rawTotalComSupers(it) } ?: 0
        val jutsuRaw = perJutsu?.let { rawTotalComSupers(it) } ?: 0
        val melhorLuta = maxOf(lutarRaw, jutsuRaw)
        val base     = 2 + (melhorLuta / 2)

        val mods = ModifierEngine.sum(this, ModifierTarget.PARRY)

        return base + mods
    }

    // Engine Delegation
    fun tamanhoExibido(): Int = ModifierEngine.sizeDisplay(this)
    fun tamanhoParaResistencia(): Int = ModifierEngine.sizeForToughness(this)
    fun resistenciaBase(): Int = ModifierEngine.toughnessBase(this)

    fun valorResistenciaBase(): Int {
        // Agora delega para o engine
        // O engine já inclui bônus de poder, mas se quisermos manter "Base" vs "Final"
        // para outras lógicas, teríamos que ter cuidado.
        // O prompt pede para usar engine.
        return resistenciaBase()
    }

    fun valorResistenciaFinal(): Int {
        // resistenciaBase() do engine j inclui bonusResFromPower (via TOUGHNESS_FLAT)
        return resistenciaBase()
    }

    fun calculaAtaqueDesarmado(): Pair<String, String> {
        val modifiers = mutableListOf<String>()
        var steps = 0

        // Check Martial Artist (id: artista_marcial)
        if (vantagensSelecionadas.any { it.id == "artista_marcial" }) {
            modifiers.add("Artista Marcial")
            steps++
        }

        // Check Brawler (id: brigao)
        if (vantagensSelecionadas.any { it.id == "brigao" }) {
            modifiers.add("Brigão")
            steps++
        }

        // Check Claws (Garra)
        val ancestry = listaAncestralidadesJson.firstOrNull { it.nome.keyify() == ancestralidade }
        val hasRacialClaws = ancestry?.habilidades?.any { it.nome.keyify().contains("GARRA") } == true ||
                ancestry?.vantagensGratis?.any { it.keyify().contains("GARRA") } == true

        // Check Edge Claws
        val hasEdgeClaws = vantagensSelecionadas.any { it.nome.keyify().contains("GARRA") }

        if (hasRacialClaws || hasEdgeClaws) {
            modifiers.add("Garra")
            steps++
        }

        val damageStr = if (steps == 0) {
            "For"
        } else {
            // Base die for natural weapon is d4. Steps increase it: d4->d6->d8->d10->d12->d12+1...
            val dieType = when (steps) {
                1 -> "d4"
                2 -> "d6"
                3 -> "d8"
                4 -> "d10"
                5 -> "d12"
                else -> "d12+${steps-5}"
            }
            "For+$dieType"
        }

        return damageStr to modifiers.joinToString(", ")
    }

    fun extrairArmasNaturais(): List<EquipamentoItem> {
        val weapons = mutableListOf<EquipamentoItem>()
        val ancestralidadeObj = listaAncestralidadesJson.firstOrNull { it.nome.keyify() == ancestralidade }
            ?: return emptyList()

        val keywords = listOf("Garras", "Mordida", "Chifres", "Cascos")
        val sources = ancestralidadeObj.vantagensGratis + ancestralidadeObj.habilidades.map { it.nome }

        // Helper to find description for a keyword
        fun findDesc(keyword: String): String {
            // 1. Try Ability (Habilidade Racial)
            val hab = ancestralidadeObj.habilidades.find { it.nome.contains(keyword, ignoreCase = true) }
            if (hab != null) return hab.descricao

            // 2. Try Free Edge (Vantagem Grátis)
            // If the keyword is in vantagensGratis, we try to look up the edge definition in the global list
            if (ancestralidadeObj.vantagensGratis.any { it.contains(keyword, ignoreCase = true) }) {
                val edge = listaVantagens.firstOrNull {
                    it.nome.contains(keyword, ignoreCase = true)
                }
                if (edge != null) return edge.descricao
            }
            return ""
        }

        // Check for Martial Artist / Brawler (used for upgrading damage)
        val hasMartialArtist = vantagensSelecionadas.any { it.id == "artista_marcial" }
        val hasBrawler = vantagensSelecionadas.any { it.id == "brigao" }

        // Helper to upgrade die type string (e.g. "For+d4" -> "For+d6")
        fun upgradeDie(dmg: String): String {
            val dieMap = listOf("d4", "d6", "d8", "d10", "d12")
            val match = Regex("""d(\d+)""").find(dmg) ?: return dmg
            val currentDie = match.value
            val index = dieMap.indexOf(currentDie)
            if (index != -1 && index < dieMap.lastIndex) {
                return dmg.replace(currentDie, dieMap[index + 1])
            }
            if (index == -1 && !dmg.contains("+d")) {
                // Handle plain "For" -> "For+d4" case if passed here, though usually handled explicitly
                return "$dmg+d4"
            }
            return dmg
        }

        // Monster Natural Weapons
        getMonstroSelecionado()?.let { monstro ->
            monstro.habilidades.forEach { hab ->
                val nomeKey = hab.nome.keyify()
                if (nomeKey.contains("GARRA") || nomeKey.contains("MORDIDA") || nomeKey.contains("CHIFRE") || nomeKey.contains("CASCO")) {
                    val dmgRegex = Regex("""(For|Str|Força|Strength)(\s*\+\s*)?d\d+""", RegexOption.IGNORE_CASE)
                    var dmgMatch = dmgRegex.find(hab.descricao)?.value?.replace(" ", "") ?: "For+d4"

                    if (nomeKey.contains("GARRA") && (hasMartialArtist || hasBrawler)) {
                        dmgMatch = upgradeDie(dmgMatch)
                    }

                    weapons.add(
                        EquipamentoItem(
                            nome = hab.nome,
                            dano = JsonPrimitive(dmgMatch),
                            distancia = JsonPrimitive("Toque"),
                            peso = JsonPrimitive(0),
                            custo = JsonPrimitive(0),
                            observacoes = JsonPrimitive("Monstro")
                        )
                    )
                }
            }
        }

        // Parse logic
        keywords.forEach { key ->
            val matchedSource = sources.firstOrNull { it.contains(key, ignoreCase = true) }

            if (matchedSource != null) {
                var desc = findDesc(key)
                if (desc.isBlank()) {
                    desc = matchedSource
                }

                // Regex to find damage like "For+d4", "Str+d4", "For+d6", allowing for spaces
                val dmgRegex = Regex("""(For|Str|Força|Strength)(\s*\+\s*)?d\d+""", RegexOption.IGNORE_CASE)
                val paRegex = Regex("""PA\s*\d+""", RegexOption.IGNORE_CASE)

                var dmgMatch = dmgRegex.find(desc)?.value?.replace(" ", "") ?: "For+d4"
                val paMatch = paRegex.find(desc)?.value?.replace("PA", "", ignoreCase = true)?.trim()?.toIntOrNull() ?: 0

                // Apply scaling to "Garras" if Martial Artist or Brawler is present
                if (key.equals("Garras", ignoreCase = true)) {
                    if (hasMartialArtist || hasBrawler) {
                        dmgMatch = upgradeDie(dmgMatch)
                    }
                }

                var finalName = key
                if (matchedSource.contains("(") && matchedSource.contains(")")) {
                    val prefix = matchedSource.substringBefore("(").trim()
                    if (prefix.isNotBlank() && !prefix.equals(key, ignoreCase = true)) {
                        finalName = prefix.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    }
                }

                weapons.add(
                    EquipamentoItem(
                        nome = finalName,
                        dano = JsonPrimitive(dmgMatch),
                        pa = if (paMatch > 0) JsonPrimitive(paMatch) else null,
                        distancia = JsonPrimitive("Toque"),
                        peso = JsonPrimitive(0),
                        custo = JsonPrimitive(0)
                    )
                )
            }
        }

        // Always add "Ataque Natural" (Unarmed) using central logic
        val (unarmedDmg, unarmedNotes) = calculaAtaqueDesarmado()
        weapons.add(
            EquipamentoItem(
                nome = "Ataque Natural",
                dano = JsonPrimitive(unarmedDmg),
                distancia = JsonPrimitive("Toque"),
                peso = JsonPrimitive(0),
                custo = JsonPrimitive(0),
                observacoes = if (unarmedNotes.isNotBlank()) JsonPrimitive(unarmedNotes) else null
            )
        )

        return weapons
    }

    fun valorChi(): Int {
        return reservaChi
    }

    fun valorDominio(): Int {
        val espiritoRaw = valoresAtributos["ESPIRITO"]?.intValue ?: 4
        // Domínio inicial para ressuscitados é geralmente o dado de Espírito
        return espiritoRaw
    }

    fun valorArmaduraEfetiva(): Int {
        // Agora usa o Engine para somar armadura de equipamentos (filtrando Mechas)
        // A variável 'armadura' permanece como fallback ou armadura base manual se houver
        val armorFromEquipment = ModifierEngine.sum(this, ModifierTarget.ARMOR)
        kotlin.math.max(armorFromPower, armorFromEquipment)
        // 'armadura' variável de estado ainda pode ser usada se setada manualmente por raças (ex: Saurios)
        // Mas Saurios setam naturalArmorFromRace = 2 e armadura = 0 no código atual.
        // Se houver algum caso de uso para 'armadura' (variável), ela deveria ser somada?
        // No código original: val armorFromEquipment = armadura.
        // Assumimos que 'armadura' state var era SÓ para equipamento ou manual override.
        // Se o Engine já pega equipment, e 'armadura' é 0 na maioria dos casos, ok.
        // Se 'armadura' for usada para outra coisa, precisamos somar ou max.
        // Vamos somar 'armadura' (state var) com o do Engine por segurança,
        // caso algum sistema legado use 'armadura' para "Armadura Mágica Permanente" não listada em itens.
        val totalEquipmentArmor = armorFromEquipment + armadura

        val bestArmor = kotlin.math.max(armorFromPower, totalEquipmentArmor)
        return (bestArmor + naturalArmorFromRace).coerceAtLeast(0)
    }

    fun valorTamanho(): Int = tamanhoExibido()

    fun valorFama(): Int {
        // Base 0 + Modifiers (Traits) + Manual
        // TODO: Add traits logic if needed later
        return famaManual
    }

    // PROMPT 2: Brawny (Brutamontes) Carga calculation
    fun valorCargaMaxima(): Float {
        val strengthRaw = valoresAtributos["FORCA"]?.intValue ?: 4
        val hasSoldado = vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }
        val hasMusculoso = vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
        // PROMPT 2: "Brawny treats Strength as one die type higher"
        val hasBrutamontes = vantagensSelecionadas.any {
            val nk = it.nome.keyify()
            nk == "BRUTAMONTES" || nk == "BRAWNY"
        }

        var effectiveStrength = strengthRaw

        // Brawny logic: treat as one die higher
        if (hasBrutamontes) {
             effectiveStrength = if (effectiveStrength < 12) effectiveStrength + 2 else effectiveStrength + 1
        }

        // Soldier logic: treat as one die higher (if active)
        if (hasSoldado && soldadoCargaAtivo) {
             effectiveStrength = if (effectiveStrength < 12) effectiveStrength + 2 else effectiveStrength + 1
        }

        val bonusCapacity = if (hasMusculoso) 10f else 0f
        val baseLimit = if (effectiveStrength >= 4) ((effectiveStrength - 2) / 2) * 10f else 0f
        return baseLimit + bonusCapacity
    }

    fun gastarPcParaRecursos(): Boolean {
        if (pontosComplicacao - pontosComplicacaoGastos < 1) return false
        pontosComplicacaoGastos += 1
        cpRecursosStack.add(Unit)
        dinheiro += if (compendioPathfinderAtivo) 60000 else 500
        return true
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun devolverPcDeRecursos() {
        if (cpRecursosStack.isNotEmpty()) {
            cpRecursosStack.removeLast()
            pontosComplicacaoGastos -= 1
            val amount = if (compendioPathfinderAtivo) 60000 else 500
            dinheiro = (dinheiro - amount).coerceAtLeast(0)
        }
    }

    fun gastarPcParaVantagem(): Boolean {
        // custo de 2 PC para 1 PV
        if (pontosComplicacao - pontosComplicacaoGastos < 2) return false

        pontosComplicacaoGastos += 2
        cpPvStack.add(Unit)   // registra que 1 vantagem foi comprada
        pontosVantagem += 1
        return true
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun devolverPcDeVantagem() {
        if (cpPvStack.isNotEmpty()) {
            cpPvStack.removeLast()
            pontosComplicacaoGastos -= 2

            val removedAdvantage = if (pontosVantagem == 0) {
                removerUltimaVantagemCompradaComPv()
            } else false

            if (removedAdvantage) {
                pontosVantagem += 1 // devolve o PV gasto pela vantagem removida
            }

            pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
        }
    }

    fun gastarPcParaAtributo(): Boolean {
        // Custo de 2 Pontos de Complicação para 1 aumento de atributo
        val custo = 2
        val disponivel = (pontosComplicacao - pontosComplicacaoGastos)

        if (disponivel < custo) return false

        cpPaStack.add("PB")              // registra 1 compra de atributo com PC
        pontosComplicacaoGastos += custo // soma 2 ao total gasto
        recalcularPontosAtributo()       // recalcula PA restantes e stacks

        return true
    }

    fun isPathfinderEligible(v: Vantagem): Boolean {
        if (!compendioPathfinderAtivo) return false
        return when (v.categoria) {
            Categoria.CLASSE, Categoria.PROFISSIONAL, Categoria.ANTECEDENTE -> true
            else -> false
        }
    }

    fun isVantagemAutomatica(v: Vantagem): Boolean {
        val key = v.nome.substringBefore("(").trim().keyify()
        val autoKeys = (vantagensAutomaticas + vantagensRaciais)
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()

        // Also check raw IDs in vantagensRaciais/Automaticas because some JSONs use IDs directly
        // like "aa_agente_syn" which doesn't match the name "ANTECEDENTE ARCANO"
        val autoIds = (vantagensAutomaticas + vantagensRaciais).toSet()

        return key in autoKeys ||
                v.id in autoIds ||
                v.id in vantagensAutomaticasDoTropo ||
                v.id in vantagensAutomaticasDoProtagonista ||
                v.id in vantagensAutomaticasDoSigno ||
                v.id in vantagensAutomaticasDoElemento ||
                v.id in vantagensAutomaticasDoPotencialFisico ||
                (v.id == "conexoes" && v.choice?.equals("Máfia", ignoreCase = true) == true)
    }

    fun isProtagonistaEligible(v: Vantagem): Boolean {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_protagonista") return false
        return when (protagonistaRollVantagem) {
            1 -> v.categoria == Categoria.CHI
            2 -> v.categoria == Categoria.ESTRANHAS
            3 -> v.categoria == Categoria.ANTECEDENTE
            4 -> v.categoria == Categoria.COMBATE
            5 -> v.categoria == Categoria.SOCIAIS
            6 -> true
            8 -> true
            else -> false
        }
    }

    val protagonistaSlotAvailable: Boolean by derivedStateOf {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_protagonista") false
        else {
            vantagensSlotProtagonista.isEmpty()
        }
    }

    val pathfinderSlotAvailable: Boolean by derivedStateOf {
        if (!compendioPathfinderAtivo) false
        else {
            vantagensSelecionadas.none { vant ->
                isPathfinderEligible(vant) && !isVantagemAutomatica(vant)
            }
        }
    }

    private fun Vantagem.isBrutamontes(): Boolean {
        val idKey = id.keyify()
        val nameKey = nome.keyify()
        return idKey == "BRUTAMONTES" || idKey == "BRAWNY" || nameKey == "BRUTAMONTES" || nameKey == "BRAWNY"
    }

    fun comprarVantagem(v: Vantagem, onFeedback: (String) -> Unit = {}): Boolean {
        // Special case: Power Points
        val isPowerPoint = v.nome.contains("Pontos de Poder", true) || v.nomeExibicao.contains("Pontos de Poder", true)

        if (isPowerPoint) {
            if (!podeSelecionar(v)) return false
            comprarPontoDePoder(v)
            onFeedback("Vantagem ${v.nome} (Pontos de Poder) adicionada.")
            return true
        }

        // Standard Advantage
        val isFreePathfinder = pathfinderSlotAvailable && isPathfinderEligible(v)
        val isFreeProtagonista = protagonistaSlotAvailable && isProtagonistaEligible(v)

        if (!isFreePathfinder && !isFreeProtagonista && pontosVantagem <= 0) return false // No points

        applyVantagemDinheiro(v)
        adicionarVantagem(v)

        if (isFreePathfinder) {
            onFeedback("Vantagem ${v.nome} adicionada (Slot de Classe Gratuito).")
        } else if (isFreeProtagonista) {
            vantagensSlotProtagonista.add(v.id)
            onFeedback("Vantagem ${v.nome} adicionada (Slot de Protagonista Gratuito).")
        } else {
            pontosVantagem--
            onFeedback("Vantagem ${v.nome} adicionada.")
        }

        val enforcePoolLimit = !v.isBrutamontes()
        rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)

        return true
    }

    fun venderVantagem(v: Vantagem, onFeedback: (String) -> Unit = {}) {
        val isPowerPoint = v.nome.contains("Pontos de Poder", true) || v.nomeExibicao.contains("Pontos de Poder", true)

        if (isPowerPoint) {
            removerPontosDePoder(v)
            pontosVantagem++
            rebuildAllPericiaStacks(enforcePoolLimit = true)
            onFeedback("Vantagem ${v.nome} removida.")
            return
        }

        if (v.id in vantagensAutomaticasDoProtagonista) {
            onFeedback("Vantagem automática do Protagonista (use a rolagem do tropo para alterar).")
            return
        }

        // Standard Advantage
        val wasEligible = isPathfinderEligible(v)
        val wasProtagonistaEligible = v.id in vantagensSlotProtagonista

        removeVantagemDinheiro(v)
        removerVantagem(v)

        var shouldRefund = true

        if (wasEligible && compendioPathfinderAtivo) {
            // Count remaining eligible edges that are NOT automatic
            val remainingEligiblePurchased = vantagensSelecionadas.count {
                isPathfinderEligible(it) && !isVantagemAutomatica(it)
            }
            // If we have ZERO purchased eligible edges left, then we removed the one that was occupying the free slot.
            // No refund.
            if (remainingEligiblePurchased == 0) {
                shouldRefund = false
            }
        }
        if (wasProtagonistaEligible && compendioArteDaGuerraAtivo) {
            vantagensSlotProtagonista.remove(v.id)
            shouldRefund = false
        }

        if (shouldRefund) {
            pontosVantagem++
        }

        if (v.id == "o_melhor_que_ha") {
            poderFavoritoId = null
        }

        val enforcePoolLimit = !v.isBrutamontes()
        rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
        onFeedback(if (shouldRefund) "Vantagem removida (+1 PV)." else "Vantagem gratuita removida (Slot liberado).")
    }

    fun adicionarVantagem(v: Vantagem) {
        vantagensSelecionadas.add(v)

        v.toArcanoKey()?.let { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()

            // Initialize slots for ANY Arcane Background, not just fixed ones
            val slots = poderSlotsPorArcano.getOrPut(arcKey) {
                val count = getSlotsCountForArcano(arcKey)
                mutableStateListOf<String?>().apply { repeat(count) { add(null) } }
            }

            val effectiveKey = if (arcKey == "MISTICO" && !v.choice.isNullOrBlank()) {
                "MISTICO_${v.choice!!.normAAKey()}"
            } else {
                arcKey
            }

            fixedPowersByArcano[effectiveKey]?.let { fixedList ->
                // Ensure slots size
                // Fix: Mystic Powers might have more fixed powers than the default slot count
                val requiredSize = getEffectiveSlotsCountForArcano(arcKey)
                while (slots.size < requiredSize) slots.add(null)

                fixedList.forEachIndexed { index, powerId ->
                    if (index < slots.size) {
                        slots[index] = powerId
                    }
                }
            }
            syncPoderesSelecionadosFromSlots()
        }

        if (v.id == "escolhido") {
            val inimigo = listaComplicacoes.firstOrNull { it.id == "inimigo" }
            if (inimigo != null) {
                if (complicacoesSelecionadas.keys.none { it.id == "inimigo" }) {
                    complicacoesSelecionadas[inimigo] = "Maior"
                    desvantagensAutomaticas.add(inimigo.name)
                    if (!anotacoes.contains("Inimigo (Maior) adicionado por Escolhido")) {
                        anotacoes += "\n• Inimigo (Maior) adicionado automaticamente pela Vantagem Escolhido."
                    }
                }
            }
        }
    }

    fun removerVantagem(v: Vantagem) {
        vantagensSelecionadas.remove(v)

        // Safety check for Mystic Powers cleanup
        if (v.nome.normAAKey().contains("PODERES MISTICOS")) {
            val anyMystic = vantagensSelecionadas.any { it.nome.normAAKey().contains("PODERES MISTICOS") }
            if (!anyMystic) {
                poderSlotsPorArcano.remove("MISTICO")
                novosPoderesStacksPorArcano.remove("MISTICO")
                syncPoderesSelecionadosFromSlots()
            }
        }

        val arcKey = v.toArcanoKey()?.normAAKey()
        if (arcKey != null) {
            val remainingWithSameKey = vantagensSelecionadas.any { it.toArcanoKey()?.normAAKey() == arcKey }
            if (!remainingWithSameKey) {
                poderSlotsPorArcano.remove(arcKey)
                novosPoderesStacksPorArcano.remove(arcKey)
                syncPoderesSelecionadosFromSlots()
            }
        }

        if (v.nome.keyify() == "CAVALEIRO") {
            equipamentosComprados.removeAll { it.origemGrant == "CAVALEIRO" }
        }

        if (v.id == "escolhido") {
            val inimigo = listaComplicacoes.firstOrNull { it.id == "inimigo" }
            if (inimigo != null) {
                // Remove apenas se estiver marcado como automático
                if (desvantagensAutomaticas.contains(inimigo.name)) {
                    complicacoesSelecionadas.remove(inimigo)
                    desvantagensAutomaticas.remove(inimigo.name)

                    // Remove a anotação se presente
                    val note = "\n• Inimigo (Maior) adicionado automaticamente pela Vantagem Escolhido."
                    if (anotacoes.contains(note)) {
                        anotacoes = anotacoes.replace(note, "")
                    }
                }
            }
        }
    }

    fun removerVantagemPorSuper(v: Vantagem) {
        vantagensSelecionadas.remove(v)
        vantagensDePoder.remove(v.id)
    }

    fun adicionarVantagemPorSuper(v: Vantagem): Boolean {
        if (v.categoria == Categoria.LENDARIAS) return false

        val progressoAnterior = overrideStageForVantagem
        overrideStageForVantagem = "Lendário"

        val permitido = podeSelecionar(v)
        overrideStageForVantagem = progressoAnterior

        if (!permitido) return false

        if (!vantagensSelecionadas.contains(v)) {
            vantagensSelecionadas += v
            vantagensDePoder += v.id
            return true
        }
        return false
    }

    fun applySuperStepsFrom(rawStart: Int, steps: Int): Int {
        var raw = rawStart
        var remaining = steps.coerceAtLeast(0)

        if (raw <= 0 && remaining > 0) {
            raw = 4
            remaining -= 1
        }

        repeat(remaining) {
            raw += if (raw < 12) 2 else 1
        }

        return raw
    }

    fun atributoRawComSupers(attrKey: String): Int {
        return valoresAtributos[attrKey]?.intValue ?: 4
    }

    /** Respeita o teto de mitigação por supers (clampa apenas a soma dos componentes de supers) */
    /** Facilita adicionar/remover efeitos de um PoderId no ledger */
    fun registrarGastoDePoder(poderId: String, custo: Int) {
        val atual = gastosPorPoder[poderId] ?: 0
        gastosPorPoder[poderId] = atual + custo
        superPontosDisponiveis = (superPontosTotais - gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun desfazerGastoDePoder(poderId: String, custo: Int) {
        val atual = (gastosPorPoder[poderId] ?: 0) - custo
        if (atual <= 0) gastosPorPoder.remove(poderId) else gastosPorPoder[poderId] = atual
        superPontosDisponiveis = (superPontosTotais - gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun updateBonusApararFromPower(value: Int) {
        bonusApararFromPower = value.coerceAtLeast(0)
    }

    fun updateBonusResFromPower(value: Int) {
        bonusResFromPower = value.coerceAtLeast(0)
    }

    fun updateArmorFromPower(value: Int) {
        armorFromPower = value.coerceAtLeast(0)
    }

    fun updateBonusMovimentacaoFromPower(value: Int) {
        bonusMovimentacaoFromPower = value.coerceAtLeast(0)
    }

    fun rawTotalComSupers(per: Pericia): Int {
        val base = rawTotal(per)
        val incs = superInvestments
            .map { it.effect }
            .filterIsInstance<PowerEffect.SuperPericia>()
            .filter { it.periciaKey.keyify() == per.nome.keyify() }
            .sumOf { it.steps }
        return applySuperStepsFrom(base, incs)
    }

    var regraMultiplosIdiomas by mutableStateOf(false)

    private val idiomaSlotRegex = Regex("^Idiomas\\s+(\\d+)$", RegexOption.IGNORE_CASE)
    private val idiomasExtras = mutableStateListOf<Pericia>()

    // PROMPT 5: Jutsu Skill Logic (Arte da Guerra)
    private val jutsuSlotRegex = Regex("^Jutsu\\s+(\\d+)$", RegexOption.IGNORE_CASE)
    private val jutsuExtras = mutableStateListOf<Pericia>()

    // Computed property for basic filtering before injecting dynamic slots (Idioms, Jutsu)
    val periciasFiltradasPorCompendio: List<Pericia> by derivedStateOf {
            val activeOrigins = getActiveOrigins()
            val filteredByOrigin = listaPericias.filter {
                val o = (it.origem ?: "").ifBlank { "BASICO" }.uppercase().semAcentos()
                o in activeOrigins
            }

            // Unify duplicates by Key, choosing the most relevant source
            val unifiedList = filteredByOrigin.groupBy { it.nome.keyify() }
                .mapNotNull { (_, group) ->
                    group.maxByOrNull { CriadorState.getOriginPriority(it.origem) }
                }

            if (compendioPathfinderAtivo) {
                // If Pathfinder active:
                val forbiddenIds = setOf("ELETRONICA", "FOCO", "HACKEAR", "PSIONICOS", "IDIOMAS")
                unifiedList.filter { per ->
                    val key = per.nome.keyify()
                    key !in forbiddenIds && per.origem != "ARTE_DA_GUERRA"
                }
            } else if (compendioArteDaGuerraAtivo) {
                // If AdG active:
                // 1. Remove standard skills that don't exist in AdG
                val forbiddenIds = setOf(
                    "HACKEAR", "FE", "ELETRONICA", "CIENCIA ESTRANHA", "PSIONICOS", "CONJURAR", "CIENCIA_ESTRANHA"
                )

                unifiedList.filter { per ->
                    val key = per.nome.keyify()

                    // Exclude specific forbidden skills
                    if (key in forbiddenIds) return@filter false

                    // Special case for FOCO:
                    // If we have an AdG version (origem="ARTE_DA_GUERRA"), use that.
                    // If it's the standard Foco (no origem or BASICO), exclude it IF we have an AdG replacement.
                    // Actually, simpler: if per.nome is FOCO, only keep if it is the AdG version.
                    if (key == "FOCO") {
                        per.origem == "ARTE_DA_GUERRA"
                    } else if (key == "TRANSICAO") {
                        // Transição is exclusive to Elementalista trope
                        tropoSelecionado?.id == "tropo_elementalista"
                    } else {
                        // Keep other AdG skills
                        if (per.origem == "ARTE_DA_GUERRA") return@filter true
                        // Keep standard skills (unless forbidden above or handled by replacement)
                        true
                    }
                }
            } else if (compendioWiseguysAtivo) {
                // If Wiseguys active:
                // Filter out specific arcane skills
                val forbiddenForWiseguys = setOf(
                    "FE", "PSIONICOS", "CIENCIA ESTRANHA", "CONJURAR", "CIENCIA_ESTRANHA", "OCULTISMO", "FOCO"
                )

                val filtered = unifiedList.filter { per ->
                    val key = per.nome.keyify()
                    if (key in forbiddenForWiseguys) return@filter false
                    // Also filter AdG skills if AdG is not active
                    per.origem != "ARTE_DA_GUERRA"
                }

                val lei = Pericia("Lei", "ASTUCIA", false, "WISEGUYS")
                if (!baseIncsPorPericia.containsKey(lei)) {
                    ensurePericiaEntry(lei)
                }
                (filtered + lei).sortedBy { it.nome }
            } else {
                // If neither AdG nor Wiseguys specific filtering is active:
                // Hide any skill marked with ARTE_DA_GUERRA
                unifiedList.filter { per ->
                    per.origem != "ARTE_DA_GUERRA"
                }.sortedBy { it.nome }
            }
        }

    fun isPericiaBasicaEfetiva(per: Pericia): Boolean {
        if (!per.basica) return false
        if (compendioFantasiaAtivo && ancestralidade.keyify() == "GOLENS") {
            val key = per.nome.keyify()
            if (key == "CONHECIMENTO GERAL" || key == "PERSUADIR" || key == "FURTIVIDADE") {
                return false
            }
        }
        return true
    }

    fun periciaStartRaw(anc: String, per: Pericia): Int {
        val ancKey = anc.keyify()
        val perKey = per.nome.keyify()

        val defaultBase = if (per.basica) {
            if (compendioFantasiaAtivo && ancKey == "GOLENS" && (perKey == "CONHECIMENTO GERAL" || perKey == "PERSUADIR" || perKey == "FURTIVIDADE")) 0 else 4
        } else {
            0
        }

        val base = racialSkillStartMap[ancKey]?.get(perKey) ?: defaultBase

        var modifiedBase = base

        // Monster Bonus
        getMonstroSelecionado()?.let { monstro ->
            val monsterKey = per.nome.keyify()
            val bonusEntry = monstro.atributos_bonus.entries.firstOrNull {
                it.key.keyify() == monsterKey
            }
            if (bonusEntry != null) {
                // Mapping: 1 -> d4 (4), 2 -> d6 (6), 3 -> d8 (8), etc.
                val steps = bonusEntry.value
                val bonusRaw = when(steps) {
                    1 -> 4
                    2 -> 6
                    3 -> 8
                    4 -> 10
                    5 -> 12
                    else -> 4
                }
                modifiedBase = maxOf(modifiedBase, bonusRaw)
            }
        }

        // Arte da Guerra - Signos (only for Humans)
        if (compendioArteDaGuerraAtivo && ancKey.contains("HUMANO")) {
            val sign = signoAdgSelecionado
            if (sign != null) {
                // Lebre: Cura d6
                if (sign.equals("Lebre", ignoreCase = true) && perKey == "CURAR") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                // Garça: Acrobacia d4, Atletismo +1 die type (from base)
                if (sign.equals("Garça", ignoreCase = true)) {
                    if (perKey == "ACROBACIA") modifiedBase = maxOf(modifiedBase, 4)
                    if (perKey == "ATLETISMO") modifiedBase = maxOf(modifiedBase, 6) // Base d4 -> d6
                }
                // Serpente: Jogar OR Performance d6
                if (sign.equals("Serpente", ignoreCase = true)) {
                    val chosen = signoSerpentePericiaEscolhida.keyify()
                    if (perKey == chosen) {
                        modifiedBase = maxOf(modifiedBase, 6)
                    }
                }
                // Macaco: Unskilled d4+1 (Not represented in start raw)
            }
        }

        // Arte da Guerra - Protagonista
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_protagonista") {
            val pericias = protagonistaPericiasDoTropo()
            if (perKey in pericias) {
                modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        // Arte da Guerra - Bu Xista
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_buxista") {
            if (perKey == "CONVENCAO" || perKey == "OCULTISMO") {
                modifiedBase = if (modifiedBase > 0) {
                    maxOf(modifiedBase, applySuperStepsFrom(modifiedBase, 1))
                } else {
                    maxOf(modifiedBase, 4)
                }
            }
        }

        // Arte da Guerra - Artista Marcial (Jutsu inicial)
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_artista_marcial") {
            val slotIndex = jutsuSlotIndex(per)
            if (slotIndex != null) {
                when (artistaMarcialJutsuOpcao) {
                    ARTISTA_MARCIAL_JUTSU_D6 -> if (slotIndex == 1) {
                        modifiedBase = maxOf(modifiedBase, 6)
                    }
                    ARTISTA_MARCIAL_JUTSU_D4_D4 -> if (slotIndex == 2) {
                        modifiedBase = maxOf(modifiedBase, 4)
                    }
                }
            }
        }

        // Arte da Guerra - Tropos
        if (compendioArteDaGuerraAtivo) {
            tropoSelecionado?.let { tropo ->
                val bonusMap = tropo.periciasGratuitas.mapKeys {
                    val k = it.key.keyify()
                    if (k == "JUTSU") "LUTAR" else k
                }
                val bonus = bonusMap[perKey]
                if (bonus != null) {
                    modifiedBase = maxOf(modifiedBase, bonus)
                }
            }
        }

        // Gnomo Buscatrilha - Obsessivos (d4 em perícia de Astúcia à escolha)
        if (compendioPathfinderAtivo && ancKey.contains("GNOMO") && ancKey.contains("PATHFINDER")) {
            val chosen = gnomoPericiaEscolhida?.keyify()
            if (chosen != null && perKey == chosen) {
                modifiedBase = maxOf(modifiedBase, 4)
            }
        }

        // Fantasia: Antecedente Arcano concede d4 na perícia (se não tiver)
        if (compendioFantasiaAtivo || compendioHorrorAtivo || compendioPathfinderAtivo) {
            val absVantages = vantagensSelecionadas.filter { it.toArcanoKey() != null }

            // Pathfinder: Apenas o SEGUNDO (ou posteriores) Antecedente Arcano concede a perícia d4 grátis.
            // O primeiro (classe principal) deve ser comprado com pontos.
            val absToConsider = if (compendioPathfinderAtivo) {
                if (absVantages.size > 1) absVantages.drop(1) else emptyList()
            } else {
                absVantages
            }

            absToConsider.forEach { vant ->
                val abKey = vant.toArcanoKey()?.normAAKey()
                if (abKey != null && arcanoInfo.containsKey(abKey)) {
                    val info = arcanoInfo[abKey]
                    if (info != null && info.third.keyify() == perKey) {
                        modifiedBase = maxOf(modifiedBase, 4)
                    }
                }
            }
        }

        return modifiedBase
    }

    private val _periciasComIdiomas: List<Pericia> by derivedStateOf {
        // Use the filtered list as base instead of raw global listaPericias
        val baseList = periciasFiltradasPorCompendio

        val idiomaBase = idiomaBasePericia()
        if (idiomaBase == null) {
            periciasComJutsu(baseList) // fallback if no idioms
        } else {
            val extrasOrdenados = idiomasExtras.sortedBy { idiomaSlotIndex(it) ?: Int.MAX_VALUE }

            // Combine base list + idioms first
            val listWithIdioms = buildList {
                baseList.forEach { per ->
                    add(per)
                    if (per == idiomaBase) {
                        addAll(extrasOrdenados)
                    }
                }
            }

            // Then inject Jutsu extras
            // Base skill is "Lutar". If Arte da Guerra is active, we treat "Lutar" as "Jutsu".
            // The extras should appear after "Lutar".
            val lutarBase = listWithIdioms.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) }

            if (lutarBase != null && compendioArteDaGuerraAtivo) {
                val jutsuExtrasOrdenados = jutsuExtras.sortedBy { jutsuSlotIndex(it) ?: Int.MAX_VALUE }
                buildList {
                    listWithIdioms.forEach { per ->
                        add(per)
                        if (per == lutarBase) {
                            addAll(jutsuExtrasOrdenados)
                        }
                    }
                }
            } else {
                listWithIdioms
            }
        }
    }

    fun periciasComIdiomas(): List<Pericia> = _periciasComIdiomas

    // Helper used above if Idiomas base is missing (unlikely but safe)
    private fun periciasComJutsu(baseList: List<Pericia> = periciasFiltradasPorCompendio): List<Pericia> {
        val lutarBase = baseList.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) }
            ?: return baseList

        if (!compendioArteDaGuerraAtivo) return baseList

        val jutsuExtrasOrdenados = jutsuExtras.sortedBy { jutsuSlotIndex(it) ?: Int.MAX_VALUE }
        return buildList {
            baseList.forEach { per ->
                add(per)
                if (per == lutarBase) {
                    addAll(jutsuExtrasOrdenados)
                }
            }
        }
    }

    fun isIdiomaPericia(per: Pericia): Boolean =
        per.nome.equals("Idiomas", ignoreCase = true) || idiomaSlotRegex.matches(per.nome)

    fun idiomaSlotIndex(per: Pericia): Int? {
        return if (per.nome.equals("Idiomas", ignoreCase = true)) {
            1
        } else {
            idiomaSlotRegex.find(per.nome)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun idiomaSlotIndexFromName(name: String): Int? {
        return if (name.equals("Idiomas", ignoreCase = true)) {
            1
        } else {
            idiomaSlotRegex.find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    // Jutsu Logic Implementation
    fun isJutsuPericia(per: Pericia): Boolean {
        if (!compendioArteDaGuerraAtivo) return false
        return per.nome.equals("LUTAR", ignoreCase = true) || jutsuSlotRegex.matches(per.nome)
    }

    fun jutsuSlotIndex(per: Pericia): Int? {
        return if (per.nome.equals("LUTAR", ignoreCase = true)) {
            1
        } else {
            jutsuSlotRegex.find(per.nome)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun jutsuSlotIndexFromName(name: String): Int? {
        return if (name.equals("LUTAR", ignoreCase = true)) {
            1
        } else {
            jutsuSlotRegex.find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun jutsuSlotName(index: Int): String = "Jutsu $index"

    private fun ensureJutsuSlotCount(totalSlots: Int) {
        val base = listaPericias.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) } ?: return
        val desired = totalSlots.coerceAtLeast(1)
        while (jutsuExtras.size < desired - 1) {
             val nextIndex = (jutsuExtras.mapNotNull { jutsuSlotIndex(it) }.maxOrNull() ?: 1) + 1
             val novo = Pericia(nome = jutsuSlotName(nextIndex), atributo = base.atributo, basica = false) // Extras aren't basic
             jutsuExtras.add(novo)
             ensurePericiaEntry(novo)
        }
    }

    private fun trimJutsuSlots(desiredSlots: Int) {
        val desired = desiredSlots.coerceAtLeast(1)
        while (jutsuExtras.size > desired - 1) {
            val ultimo = jutsuExtras.maxByOrNull { jutsuSlotIndex(it) ?: 0 } ?: break
            if (rawTotal(ultimo) > 0 || compIncsPorPericia.getValue(ultimo) > 0) break
            jutsuExtras.remove(ultimo)
            baseIncsPorPericia.remove(ultimo)
            compIncsPorPericia.remove(ultimo)
            compCostStackPorPericia.remove(ultimo)
            spCostStackPorPericia.remove(ultimo)
            notasPericia.remove(ultimo.nome)
        }
    }

    fun syncJutsuSlots() {
        if (!compendioArteDaGuerraAtivo) {
            return
        }
        val lutarBase = listaPericias.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) } ?: return
        ensurePericiaEntry(lutarBase)

        val allJutsu = listOf(lutarBase) + jutsuExtras
        val jutsusComprados = allJutsu.count { rawTotal(it) > 0 }

        val desiredSlots = if (jutsusComprados > 0) jutsusComprados + 1 else 1

        ensureJutsuSlotCount(desiredSlots)
        trimJutsuSlots(desiredSlots)
    }

    private fun ensureJutsuSlotsFromSnapshot(keys: Set<String>) {
        val maxIndex = keys.mapNotNull { jutsuSlotIndexFromName(it) }.maxOrNull() ?: 1
        if (maxIndex > 1) {
            ensureJutsuSlotCount(maxIndex)
        }
    }


    fun idiomaDefaultLabel(per: Pericia): String {
        val idx = idiomaSlotIndex(per) ?: 1
        return "Idioma $idx"
    }

    private fun idiomaSlotName(index: Int): String = "Idiomas $index"

    private fun idiomaBasePericia(): Pericia? =
        listaPericias.firstOrNull { it.nome.keyify() == "IDIOMAS" }

    private fun idiomaSlotsOrdenados(): List<Pericia> {
        val base = idiomaBasePericia() ?: return emptyList()
        return listOf(base) + idiomasExtras.sortedBy { idiomaSlotIndex(it) ?: Int.MAX_VALUE }
    }

    fun ensureAllPericiasRegistered() {
        listaPericias.forEach { ensurePericiaEntry(it) }
    }

    fun ensureAllAtributosRegistered() {
        listaAtributos.forEach { nome ->
            if (!valoresAtributos.containsKey(nome)) {
                valoresAtributos[nome] = mutableIntStateOf(4)
            }
            if (!paCostStackPorAtributo.containsKey(nome)) {
                paCostStackPorAtributo[nome] = mutableListOf()
            }
        }
    }

    private fun ensurePericiaEntry(per: Pericia) {
        if (!baseIncsPorPericia.containsKey(per)) baseIncsPorPericia[per] = 0
        if (!compIncsPorPericia.containsKey(per)) compIncsPorPericia[per] = 0
        if (!compCostStackPorPericia.containsKey(per)) compCostStackPorPericia[per] = mutableListOf()
        if (!spCostStackPorPericia.containsKey(per)) spCostStackPorPericia[per] = mutableStateListOf()
    }

    private fun ensureIdiomaSlotCount(totalSlots: Int) {
        val base = idiomaBasePericia() ?: return
        val desired = totalSlots.coerceAtLeast(1)
        while (idiomasExtras.size < desired - 1) {
            val nextIndex = (idiomasExtras.mapNotNull { idiomaSlotIndex(it) }.maxOrNull() ?: 1) + 1
            val novo = Pericia(nome = idiomaSlotName(nextIndex), atributo = base.atributo, basica = base.basica)
            idiomasExtras.add(novo)
            ensurePericiaEntry(novo)
        }
    }

    private fun trimIdiomaSlots(desiredSlots: Int) {
        val desired = desiredSlots.coerceAtLeast(1)
        while (idiomasExtras.size > desired - 1) {
            val ultimo = idiomasExtras.maxByOrNull { idiomaSlotIndex(it) ?: 0 } ?: break
            if (rawTotal(ultimo) > 0 || compIncsPorPericia.getValue(ultimo) > 0) break
            idiomasExtras.remove(ultimo)
            baseIncsPorPericia.remove(ultimo)
            compIncsPorPericia.remove(ultimo)
            compCostStackPorPericia.remove(ultimo)
            spCostStackPorPericia.remove(ultimo)
            notasPericia.remove(ultimo.nome)
        }
    }

    private fun ensureIdiomaSlotsFromSnapshot(keys: Set<String>) {
        val maxIndex = keys.mapNotNull { idiomaSlotIndexFromName(it) }.maxOrNull() ?: 1
        if (maxIndex > 1) {
            ensureIdiomaSlotCount(maxIndex)
        }
    }

    fun syncIdiomaSlots() {
        val idiomaBase = idiomaBasePericia() ?: return
        ensurePericiaEntry(idiomaBase)
        val idiomasComprados = idiomaSlotsOrdenados().count { rawTotal(it) > 0 }
        val linguistaCount = linguistaLanguageCount()
        val filled = maxOf(linguistaCount, idiomasComprados)
        val desiredSlots = if (filled > 0) filled + 1 else 1
        ensureIdiomaSlotCount(desiredSlots)
        trimIdiomaSlots(desiredSlots)
    }

    private fun linguistaLanguageCount(): Int {
        val hasLinguista = vantagensSelecionadas.any { it.id == "linguista" }
        val astuciaRaw = valoresAtributos["ASTUCIA"]?.intValue ?: 0
        return when {
            regraMultiplosIdiomas && hasLinguista -> astuciaRaw
            regraMultiplosIdiomas -> astuciaRaw / 2
            hasLinguista -> astuciaRaw / 2
            else -> 0
        }
    }

    fun linguistaMinRawFor(pericia: Pericia): Int {
        val count = linguistaLanguageCount()
        if (count <= 0 || !isIdiomaPericia(pericia)) return 0
        val idx = idiomaSlotIndex(pericia) ?: return 0
        return if (idx <= count) 6 else 0
    }

    private fun stepsToReach(per: Pericia, targetRaw: Int): Int {
        var curr = periciaStartRaw(ancestralidade, per)
        var steps = 0
        while (curr < targetRaw) {
            curr = if (curr == 0) 4 else curr + 2
            steps++
        }
        return steps
    }

    private fun syncLinguistaIdiomas() {
        val idiomaBase = idiomaBasePericia() ?: return
        ensurePericiaEntry(idiomaBase)
        val linguistaCount = linguistaLanguageCount()
        if (linguistaCount <= 0) {
            idiomaSlotsOrdenados().forEach { per ->
                compIncsPorPericia[per] = 0
            }
            syncIdiomaSlots()
            return
        }

        ensureIdiomaSlotCount(linguistaCount)
        val slots = idiomaSlotsOrdenados()
        slots.forEachIndexed { index, per ->
            if (index < linguistaCount) {
                val totalSteps = stepsToReach(per, 6)
                val baseSteps = baseIncsPorPericia.getValue(per)
                val freeSteps = (totalSteps - baseSteps).coerceAtLeast(0)
                compIncsPorPericia[per] = freeSteps
                if (notasPericia[per.nome].isNullOrBlank()) {
                    notasPericia[per.nome] = idiomaDefaultLabel(per)
                }
            } else {
                compIncsPorPericia[per] = 0
            }
        }

        syncIdiomaSlots()
    }

    var pvFromXpOutstanding by mutableIntStateOf(0)
    var overrideStageForVantagem by mutableStateOf<String?>(null)
    var openVantagensAfterGrant by mutableStateOf(false)
    var superPoderEmFoco by mutableStateOf<String?>(null)

    var arcanoEmCompraViaXpKey by mutableStateOf<String?>(null)
    var arcanoSnapshotAntesDaCompra: List<String?>? = null

    var ancestralidadeEmFoco by mutableStateOf<String?>(null)

    fun removerSuperPoder(
        poder: SuperInvestment,
        desfazerNoLedger: Boolean = true
    ) {
        if (superInvestments.remove(poder)) {
            if (desfazerNoLedger) {
                desfazerGastoDePoder(poder.powerId, poder.cost)
            }
        }
    }

    fun grantVantagemPointFromXp(stageName: String) {
        pontosVantagem += 1
        pvFromXpOutstanding += 1

        overrideStageForVantagem = stageName

        openVantagensAfterGrant = true
        mostrandoVantagensProgresso = true
    }

    fun grantSkillPointsFromXp() {
        spFromProgress += 2
        mostrandoPericiasProgresso = true
    }


    fun maxComprasPpAteAgora(): Int {
        return listaDeEstagios.indexOf(estagioAtual()) + 1
    }

    private fun selecionarPontosDePoder(v: Vantagem) {
        val estagio = estagioAtual().nome
        val totalFeitas = comprasPpPorEstagio.values.sum()

        if (totalFeitas >= maxComprasPpAteAgora()) return

        val feitasNoEstagio = comprasPpPorEstagio[estagio] ?: 0
        comprasPpPorEstagio[estagio] = feitasNoEstagio + 1

        val ganho = if (totalFeitas < 4) 5 else 2
        bonusPoderExtra += ganho

        vantagensSelecionadas += v
    }

    fun removerPontosDePoder(v: Vantagem, estagioOverride: String? = null) {
        if (!vantagensSelecionadas.remove(v)) return

        val totalAntes = comprasPpPorEstagio.values.sum()
        if (totalAntes == 0) return

        val estagio = estagioOverride ?: estagioAtual().nome
        val feitas = comprasPpPorEstagio[estagio] ?: 0
        if (feitas > 0) {
            comprasPpPorEstagio[estagio] = feitas - 1
        } else {
            val fallback = comprasPpPorEstagio.entries.lastOrNull { it.value > 0 }
            fallback?.let {
                comprasPpPorEstagio[it.key] = it.value - 1
            }
        }

        val ganhoRemovido = if (totalAntes <= 4) 5 else 2
        bonusPoderExtra = (bonusPoderExtra - ganhoRemovido).coerceAtLeast(0)
    }

    private fun removerUltimaVantagemCompradaComPv(): Boolean {
        val autoKeys = (vantagensAutomaticas + vantagensRaciais)
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()
        val autoIds = vantagensAutomaticasDoTropo.toSet()

        val candidate = vantagensSelecionadas
            .asReversed()
            .firstOrNull { vant ->
                val key = vant.nome.substringBefore("(").trim().keyify()
                key !in autoKeys && vant.id !in autoIds
            }
            ?: return false

        removeVantagemDinheiro(candidate)
        removerVantagem(candidate)
        if (candidate.id == "o_melhor_que_ha") {
            poderFavoritoId = null
        }

        rebuildAllPericiaStacks()
        return true
    }

    fun comprarPontoDePoder(v: Vantagem) {
        if (!podeSelecionar(v)) return
        selecionarPontosDePoder(v)
        vantagensSelecionadas += v
    }

    val comprasAttrPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    fun identifyMaxedTraits() {
        _maxedTraits.clear()

        listaAtributos.forEach { attrKey ->
            val current    = valoresAtributos[attrKey]?.intValue ?: return@forEach
            val maxAllowed = atributoMaxRaw(attrKey)
            if (current == maxAllowed) {
                _maxedTraits.add(attrKey)
            }
        }

        periciasComIdiomas().forEach { per ->
            val current    = rawTotal(per)
            val maxAllowed = periciaCapRaw(per)
            if (current == maxAllowed) {
                _maxedTraits.add(per.nome.keyify())
            }
        }
    }

    fun applyVantagemDinheiro(v: Vantagem) {
        if (usaRiqueza) return
        dinheiro += when (v.nome.trim().uppercase()) {
            "RICO" -> if (compendioArteDaGuerraAtivo) 2000 else 1000
            "PODRE DE RICO" -> 1500
            else -> 0
        }
    }

    fun removeVantagemDinheiro(vant: Vantagem) {
        if (usaRiqueza) return
        val key = vant.nome.trim().uppercase()
        val amount = when (key) {
            "RICO" -> if (compendioArteDaGuerraAtivo) 2000 else 1000
            "PODRE DE RICO" -> 1500
            else -> 0
        }
        if (amount <= 0) return

        while (dinheiro < amount && equipamentosComprados.isNotEmpty()) {
            val eq = equipamentosComprados.removeAt(equipamentosComprados.lastIndex)
            val custo = (eq.custo as? kotlinx.serialization.json.JsonPrimitive)
                ?.content
                ?.toIntOrNull()
                ?: 0
            dinheiro += custo
        }

        dinheiro = (dinheiro - amount).coerceAtLeast(0)
    }

    val minAttrPorVantagem by derivedStateOf {
        val resultado = mutableMapOf<String, Int>()
        vantagensSelecionadas.forEach { vant ->
            vant.requisitos.atributoMin.forEach { (atributo, valorMin) ->
                val atual = resultado[atributo]
                if (atual == null || valorMin > atual) {
                    resultado[atributo] = valorMin
                }
            }
        }
        resultado.toMap()
    }

    val minPericiaPorVantagem: Map<Pericia, Int> by derivedStateOf {
        vantagensSelecionadas.flatMap { vant ->
            val obrigatorias = vant.requisitos.periciaMin   // se for null, vira um Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    getBestPericia(nomeRaw)
                        ?.let { per -> per to min }
                }

            val opcionais = vant.requisitos.periciaMinOpcional   // se null, vira Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    getBestPericia(nomeRaw)
                        ?.let { per -> per to min }
                }

            val fav = run {
                val choiceSnapshot = vant.choice
                if (
                    vant.nome.trim().equals("Arma Predileta", ignoreCase = true)
                    && choiceSnapshot != null
                ) {
                    getBestPericia(choiceSnapshot)
                        ?.let { per -> listOf(per to 8) }
                        .orEmpty()
                } else {
                    emptyList()
                }
            }

            obrigatorias + opcionais + fav
        }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, listaMinimos) ->
                listaMinimos.maxOrNull() ?: 0
            }
    }

    private val incompatibilidades: Map<String, Set<String>> = mapOf(
        "LENTO"   to setOf("LIGEIRO"),
        "LIGEIRO" to setOf("LENTO"),
        "OBESO"      to setOf("MUSCULOSO"),
        "MUSCULOSO"  to setOf("OBESO"),
        "POBREZA"        to setOf("RICO", "PODRE DE RICO"),
        "RICO"           to setOf("POBREZA"),
        "PODRE DE RICO"  to setOf("POBREZA"),
        "ESCOLHIDO"      to setOf("INIMIGO"),
        "INIMIGO"        to setOf("ESCOLHIDO")
    )

    fun mensagemConflitoParaVantagem(vantagem: Vantagem): String? {
        val key = vantagem.nome.keyify()
        val compsConfl = incompatibilidades[key] ?: return null
        val conflito = complicacoesSelecionadas.keys.firstOrNull { comp ->
            comp.id.keyify() in compsConfl
        }
        return conflito?.let { "Remova ${it.name} para pegar ${vantagem.nome}." }
    }

    fun mensagemConflitoParaComplicacao(complicacao: Complicacao): String? {
        val key = complicacao.id.keyify()
        val vantConfl = incompatibilidades[key] ?: return null
        val conflito = vantagensSelecionadas.firstOrNull { vant ->
            vant.nome.keyify() in vantConfl
        }
        return conflito?.let { "Remova ${it.nome} para pegar ${complicacao.name}." }
    }

    val poderSlotsPorArcano = mutableStateMapOf<String, SnapshotStateList<String?>>()

    val novosPoderesStacksPorArcano = mutableStateMapOf<String, MutableList<List<String>>>()

    fun registrarNovosPoderes(versionKey: String, escolhas: List<String>) {
        val pilha = novosPoderesStacksPorArcano.getOrPut(versionKey) { mutableListOf() }
        pilha.add(escolhas)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun desfazerUltimosNovosPoderes(versionKey: String, initialSlots: Int) {
        val pilha = novosPoderesStacksPorArcano[versionKey] ?: return
        if (pilha.isEmpty()) return

        val ultima = pilha.removeLast()
        val slots = poderSlotsPorArcano[versionKey] ?: return

        ultima.forEach { poderId ->
            val idx = slots.indexOfLast { it == poderId }
            if (idx >= 0) slots[idx] = null
        }

        val extrasAinda = pilha.sumOf { it.size }
        val tamanhoMinimo = (initialSlots + extrasAinda).coerceAtLeast(initialSlots)

        while (slots.size > tamanhoMinimo && slots.lastOrNull() == null) {
            slots.removeLast()
        }

        syncPoderesSelecionadosFromSlots()
    }

    fun syncPoderesSelecionadosFromSlots() {
        poderesSelecionados.apply {
            clear()
            addAll(poderSlotsPorArcano.values.flatMap { it.filterNotNull() })
        }
    }

    fun iniciarCompraArcanoViaXp(arcKeyRaw: String) {
        val arcKey = arcKeyRaw.normAAKey()
        arcanoEmCompraViaXpKey = arcKey
        arcanoSnapshotAntesDaCompra = poderSlotsPorArcano[arcKey]?.toList()

        // Usa a contagem dinâmica que inclui Novos Poderes, em vez de apenas o valor inicial
        val totalSlots = getSlotsCountForArcano(arcKey)
        val slots = poderSlotsPorArcano.getOrPut(arcKey) { mutableStateListOf() }
        while (slots.size < totalSlots) { slots.add(null) }

        mostrandoPoderesProgresso = true
    }

    fun restoreArcanoSlots(arcKey: String, snapshot: List<String?>?) {
        if (snapshot == null) {
            poderSlotsPorArcano.remove(arcKey)
            novosPoderesStacksPorArcano.remove(arcKey)
        } else {
            poderSlotsPorArcano[arcKey] = mutableStateListOf<String?>().apply { addAll(snapshot) }
        }
        syncPoderesSelecionadosFromSlots()
    }

    fun limparCompraArcanoViaXp(restaurarSnapshot: Boolean) {
        arcanoEmCompraViaXpKey?.let { arcKey ->
            if (restaurarSnapshot) {
                restoreArcanoSlots(arcKey, arcanoSnapshotAntesDaCompra)
            }
        }
        arcanoEmCompraViaXpKey = null
        arcanoSnapshotAntesDaCompra = null
        mostrandoPoderesProgresso = false
    }

    fun getSlotsCountForArcano(arcKey: String): Int {
        val base = arcanoInfo[arcKey]?.first ?: 0
        var bonusSlots = 0

        vantagensSelecionadas
            .filter { it.id == "novos_poderes" }
            .forEach { vant ->
                val choice = vant.choice
                if (choice.isNullOrBlank()) {
                    // Legacy or Single AB: counts for this one if it's the only one or default
                    // If we have multiple ABs and blank choice, it might be ambiguous,
                    // but usually means it belongs to the primary/only one.
                    // To be safe, if blank, we assume it adds +2 if this is the only AB?
                    // Or we let the new logic handle it.
                    // For now, if blank, +2 (Standard behavior)
                    bonusSlots += 2
                } else {
                    if (choice.contains("&")) {
                        // Split logic: "Key1 & Key2"
                        // Normalize each part individually
                        if (choice.split("&").any { it.normAAKey() == arcKey }) {
                            bonusSlots += 1
                        }
                    } else {
                        // Single target
                        if (choice.normAAKey() == arcKey) {
                            bonusSlots += 2
                        }
                    }
                }
            }

        val bonusTecnicas = if (arcKey == "MESTRE DO CHI") tecnicasIniciaisFromTropo else 0
        return base + bonusSlots + bonusTecnicas
    }

    fun getEffectiveSlotsCountForArcano(arcKey: String): Int {
        val baseCount = getSlotsCountForArcano(arcKey)
        val arcKeyNorm = arcKey.normAAKey()

        val vant = vantagensSelecionadas.find { it.toArcanoKey()?.normAAKey() == arcKeyNorm }

        if (vant != null && arcKeyNorm == "MISTICO" && !vant.choice.isNullOrBlank()) {
            val effectiveKey = "MISTICO_${vant.choice!!.normAAKey()}"
            val fixedList = fixedPowersByArcano[effectiveKey]
            if (fixedList != null) {
                return maxOf(baseCount, fixedList.size)
            }
        }
        return baseCount
    }

    fun arcanoCompraPendente(): Boolean {
        val arcKey = arcanoEmCompraViaXpKey ?: return false
        val required = getSlotsCountForArcano(arcKey)
        if (required == 0) return false
        val slots = poderSlotsPorArcano[arcKey] ?: return true
        val filled = slots.count { it != null }
        return filled < required
    }

    var permiteMultiAntecedenteArcano by mutableStateOf(false)
    var usarEspecializacoesDePericia by mutableStateOf(false)

    val especializacoesPorPericia: SnapshotStateMap<String, com.example.swadebuilder.model.EspecializacoesDto> = mutableStateMapOf()

    var bonusPoderExtra by mutableIntStateOf(0)

    var obesoBonusSize by mutableIntStateOf(0)
    var obesoMalusMov by mutableIntStateOf(0)

    var idosoBonusSp by mutableIntStateOf(0)

    var jovemAutoPequeno by mutableStateOf(false)

    private var jovemMalusPa by mutableIntStateOf(0)
    private var jovemMalusSp by mutableIntStateOf(0)

    fun syncFromCPRefund(pa: Boolean = false, sp: Boolean = false, feedbackMessages: MutableList<String>) {
        if (pa) recalcularPontosAtributo(feedbackMessages)
        if (sp) rebuildAllPericiaStacks(feedbackMessages)
    }

    val cpPaStack       = mutableStateListOf<String>()  // você já trocou pra add("PB")
    var paFromProgress by mutableIntStateOf(0)
    var legendaryAttrReservations by mutableIntStateOf(0)
    val cpSpStack       = mutableStateListOf<Unit>()
    var spFromProgress by mutableIntStateOf(0)
    val cpPvStack       = mutableStateListOf<Unit>()
    val cpRecursosStack = mutableStateListOf<Unit>()

    private val totalSpPool: Int
        get() {
            // PROMPT: Arte da Guerra skill points adjustment
            if (compendioArteDaGuerraAtivo) {
                // If AdG active:
                // Base: 12 points
                // Humans with "Nenhum" sign: +3 points (15 total)
                // Ignore "maisPontosPericias" checkbox
                val isHuman = ancestralidade.keyify().contains("HUMANO")
                val base = 12 + if (isHuman && signoAdgSelecionado.equals("Nenhum", ignoreCase = true)) 3 else 0
                return (base + cpSpStack.size + spFromProgress + idosoBonusSp - jovemMalusSp).coerceAtLeast(0)
            } else {
                // Standard Logic
                val base = if (maisPontosPericias) BASE_SP_POOL else (BASE_SP_POOL - 3)
                return (base + cpSpStack.size + spFromProgress + idosoBonusSp - jovemMalusSp)
                    .coerceAtLeast(0)
            }
        }

    val pontosPericia by derivedStateOf {
        val used = spCostStackPorPericia.values.sumOf { it.sum() } +
                compCostStackPorPericia.values.sumOf { it.sum() }
        totalSpPool - used
    }

    val tecnicasIniciaisFromTropo by derivedStateOf {
        val base = tropoSelecionado?.tecnicasIniciais ?: 0
        if (tropoSelecionado?.id == "tropo_protagonista") {
            base + tecnicasIniciaisProtagonista()
        } else {
            base
        }
    }

    val reservaChi by derivedStateOf {
        // PROMPT: Chi = 2 + (Spirit/2) + bonuses
        val espiritoRaw = valoresAtributos["ESPIRITO"]?.intValue ?: 0
        val racialPenalty = if (ancestralidade.keyify() == "TERRACOTA") 1 else 0
        val bonusFromChiEdges = vantagensSelecionadas.count { it.categoria == Categoria.CHI }
        val bonusFromTropo = if (compendioArteDaGuerraAtivo) tecnicasIniciaisFromTropo else 0
        val bonusFromSign = if (compendioArteDaGuerraAtivo && ancestralidade.keyify().contains("HUMANO") && signoAdgSelecionado?.equals("Kirin", ignoreCase = true) == true) 1 else 0

        // Base 2 added as requested
        val baseChi = if (compendioArteDaGuerraAtivo) 2 else 0

        (baseChi + espiritoRaw / 2 - racialPenalty + bonusFromChiEdges + bonusFromTropo + bonusFromSign).coerceAtLeast(0)
    }

    var nomePersonagem by mutableStateOf("")

    var progresso by mutableIntStateOf(0)
    fun estagioAtual(): Estagio {
        return listaDeEstagios.first { progresso in it.minProgress .. it.maxProgress }
    }

    private fun effectiveProgressoParaVantagens(): Int {
        val stName = overrideStageForVantagem ?: return progresso
        val st = listaDeEstagios.firstOrNull { it.nome.equals(stName, ignoreCase = true) }
        return st?.minProgress ?: progresso
    }

    private fun currentProgressStageIndex(): Int {
        var firstOpen = -1

        listaDeEstagios.forEachIndexed { idx, st ->
            val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
            val cap = if (idx < listaDeEstagios.lastIndex) {
                st.maxProgress - prevMax
            } else {
                (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
            }
            val spentHere = stageXpSpent[st.nome] ?: 0
            if (spentHere < cap && firstOpen == -1) {
                firstOpen = idx
            }
        }

        return if (firstOpen >= 0) firstOpen else listaDeEstagios.lastIndex
    }

    var ancestralidade by mutableStateOf("HUMANOS")
    var celestialAAMilagresDesabilitado by mutableStateOf(false)
    var meioElfoAgil by mutableStateOf(false)
    var meioOrcForca by mutableStateOf(false)

    var tropoSelecionado by mutableStateOf<Tropo?>(null)
    val vantagensAutomaticasDoTropo = mutableStateListOf<String>()
    val vantagensAutomaticasDoProtagonista = mutableStateListOf<String>()
    val vantagensSlotProtagonista = mutableStateListOf<String>()

    val vantagensAutomaticas = mutableStateListOf<String>()
    val vantagensRaciais = mutableStateListOf<String>()
    val desvantagensRaciais = mutableStateListOf<String>()

    var pontosVantagem by mutableIntStateOf(0)

    val desvantagensAutomaticas = mutableStateListOf<String>()

    var frozenAdvantageCount by mutableIntStateOf(0)

    var pontosAtributo by mutableIntStateOf(5)

    var armadura by mutableIntStateOf(0)

    var nasceUmHeroi by mutableStateOf(false)

    val valoresAtributos = mutableStateMapOf<String, androidx.compose.runtime.MutableIntState>().apply {
        listaAtributos.forEach { put(it, mutableIntStateOf(4)) }
    }

    val complicacoesSelecionadas: SnapshotStateMap<Complicacao, String?> = mutableStateMapOf()
    val reservasComplicacaoMaior: SnapshotStateMap<String, Boolean> = mutableStateMapOf()

    val pontosComplicacao: Int
        get() {
            val autoKeys = desvantagensAutomaticas
                .map { it.substringBefore("(").trim().keyify() }
                .toSet()

            var total = 0
            var temMaior = false

            for ((comp, tipo) in complicacoesSelecionadas) {
                if (comp.id.keyify() in autoKeys) continue
                // PROMPT 3: Ignora complicações (Transtornos) ganhos em progresso para cálculo de PC
                if (transtornos.any { it.id == comp.id }) continue

                when (tipo) {
                    "Maior" -> { total += 2; temMaior = true }
                    "Menor" -> { total += 1 }
                }
            }

            val teto = if (grandesResponsabilidades && temMaior) 6 else 4
            return minOf(total, teto)
        }

    val vantagensSelecionadas      = mutableStateListOf<Vantagem>()

    // controla quais categorias da seção de Vantagens estão expandidas
    val categoriasVantagensExpandidas: SnapshotStateMap<Categoria, Boolean> =
        mutableStateMapOf<Categoria, Boolean>().apply {
            Categoria.entries.forEach { this[it] = false }
        }

    val sectionsExpanded: SnapshotStateMap<MainSection, Boolean> =
        mutableStateMapOf<MainSection, Boolean>().apply {
            MainSection.entries.forEach { this[it] = false }
            // Opcional: deixar algumas abertas por padrão
            this[MainSection.RESUMO] = true
        }

    fun toggleSection(section: MainSection) {
        val current = sectionsExpanded[section] ?: false
        sectionsExpanded[section] = !current
    }

    // guarda o nome da vantagem que está em foco (usada ao voltar da tela de detalhes)
    var vantagemEmFoco by mutableStateOf<String?>(null)

    fun temAntecedenteArcano(): Boolean {
        return vantagensSelecionadas.any { it.toArcanoKey() != null } ||
            (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_elementalista")
    }

    fun getBestPericia(nome: String): Pericia? {
        val key = nome.keyify()
        return periciasComIdiomas().firstOrNull { it.nome.keyify() == key }
            ?: mapaPericias[key]
    }

    fun podeRemoverPoderDoSlot(poderId: String): Pair<Boolean, String?> {
        val normalizedId = poderId.replace('_', ' ').keyify()
        val linkedAdvantage = vantagensSelecionadas.firstOrNull { vant ->
            if (vant.id == "poder_favorito" && !vant.choice.isNullOrBlank()) {
                val choiceKey = vant.choice!!.replace('_', ' ').keyify()
                choiceKey == normalizedId
            } else {
                false
            }
        }

        if (linkedAdvantage != null) {
            return false to "Remova a vantagem Poder Favorito antes de devolver o poder ${linkedAdvantage.choice}."
        }
        return true to null
    }

    fun podeSelecionarComplicacao(complicacao: Complicacao): Pair<Boolean, String?> {
        if (complicacao.id == "talisma" && !temAntecedenteArcano()) {
            return false to "Talismã requer um Antecedente Arcano."
        }

        if (compendioCrystalHeartAtivo) {
            val forbidden = setOf(
                "incredulo", "ganancioso", "analfabeto", "pobreza",
                "forasteiro", "inimigo", "lento", "procurado", "um_braco_so",
                "obrigacao" // Block generic obligation to favor specific ones if needed, or keeping it?
                // Prompt said "Normalmente não pode ser escolhida... mas alguns casos raros...".
                // If blocked, users can't take it. Maybe I should NOT block obligacao?
                // But text says "Normalmente não pode ser escolhida".
                // And points to "Dependente".
                // I'll block it to force setting compliance, assuming "rare cases" are handled by GM override or using Dependente.
                // Re-reading: "Veja também a nova Complicação, Dependente."
                // I'll block standard 'obrigacao' since description is generic.
            )
            if (complicacao.id.keyify() in forbidden) {
                return false to "Não utilizada em Crystal Heart (ou substituída por versão específica)."
            }
        }

        return true to null
    }

    fun podeRemoverComplicacao(comp: Complicacao, tipo: String? = null): Pair<Boolean, String?> {
        // Locked check
        if (criacaoBasicaCongelada && !modoProgressaoAtivo) return false to "Criação finalizada."

        // Automatic checks
        val autoKeys = desvantagensAutomaticas.map { it.substringBefore("(").trim().keyify() }.toSet()
        if (comp.id.keyify() in autoKeys) return false to "Complicação automática (Racial ou de Cenário)."

        // Young check
        if (comp.id == "pequeno" && jovemAutoPequeno) return false to "Adicionado automaticamente por Jovem (Maior)."

        // Knight Check
        val currentType = tipo ?: complicacoesSelecionadas[comp]
        if (comp.id.keyify() == "OBRIGACAO" && currentType == "Maior") {
             if (vantagensSelecionadas.any { it.nome.keyify() == "CAVALEIRO" }) {
                 return false to "Remova a vantagem Cavaleiro para remover esta Obrigação."
             }
        }

        // Points check
        val cost = if (currentType == "Maior") 2 else 1
        if (!modoProgressaoAtivo && pontosComplicacaoGastos > pontosComplicacao - cost) {
             return false to "Pontos em uso. Desfaça compras para liberar."
        }

        return true to null
    }

    fun podeRemoverVantagem(vantagem: Vantagem): Pair<Boolean, String?> {
        if (vantagem.id in vantagensAutomaticasDoProtagonista) {
            return false to "Vantagem automática do Protagonista."
        }
        if (vantagem.toArcanoKey() != null) {
            val temOutro = vantagensSelecionadas.any { it != vantagem && it.toArcanoKey() != null }
            if (!temOutro) {
                val temTalisma = complicacoesSelecionadas.keys.any { it.id == "talisma" }
                if (temTalisma) {
                    return false to "Remova a complicação Talismã antes de remover o Antecedente Arcano."
                }
            }
        }
        return true to null
    }

    fun podeSelecionar(v: Vantagem): Boolean {
        val key = v.nome.keyify()

        // Crystal Heart Blocks
        if (compendioCrystalHeartAtivo) {
            val forbiddenIds = setOf(
                "campeao", "chi", "linguista", "resistencia_arcana", "resistencia_arcana_aprimorada",
                "rico", "podre_de_rico",
                "aristocrata", "arma_predileta", "comando", "conexoes",
                // "antecedente_arcano" generic block handled below in generic logic usually,
                // but explicit block helps if logic is complex.
                "antecedente_arcano"
            )
            val vKey = v.id.keyify()

            if (vKey in forbiddenIds) return false

            // Block Power Edges unless Crystal Heart specific
            if (v.categoria == Categoria.PODER && v.origem != "CRYSTAL_HEART") {
                return false
            }
        }

        // Regra: "Mago" do básico oculto se Fantasia ativo
        if (compendioFantasiaAtivo && v.id == "mago") return false

        // Regra: Antecedentes Arcanos que não existem em Pathfinder (Ciência Estranha, Psiônicos, Dom)
        if (compendioPathfinderAtivo) {
            val forbiddenIds = setOf(
                "antecedente_arcano_ciencia_estranha",
                "antecedente_arcano_psionicos",
                "antecedente_arcano_dom"
            )
            if (v.id in forbiddenIds) return false
        }

        // 0) Exclusividade de Classe/Prestígio (Buscatrilha)
        if (vantagensSelecionadas.classeExclusivaBloqueada(v)) return false

        // 1) Regra especial: O MELHOR QUE HÁ
        if (key == "o_melhor_que_ha") {
            if (emProgresso) return false
            if (superInvestments.isEmpty()) return false
        }

        // 1a) Regra especial: CAVALEIRO (Fantasia)
        if (key == "CAVALEIRO") {
            val hasObligation = complicacoesSelecionadas.entries.any { (k, v) ->
                k.id.keyify() == "OBRIGACAO" && v == "Maior"
            }
            if (!hasObligation) return false
        }

        // 2) Pontos de Poder por estágio
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalFeitas = comprasPpPorEstagio.values.sum()
            val maxPermitidas = maxComprasPpAteAgora()
            if (totalFeitas >= maxPermitidas) return false
        }

        // 2a) Vantagens exclusivas de Ressuscitado exigem ter a vantagem-base
        if (v.categoria == Categoria.ATORMENTADO) {
            val temRessuscitado = vantagensSelecionadas.any { it.id == "atormentado" }
            if (!temRessuscitado) return false
        }

        // 3) Antecedente Arcano e multi-arcano
        if (key.startsWith("ANTECEDENTE ARCANO")) {
            if (compendioCrystalHeartAtivo) {
                // Allows only "Antecedente Arcano: Canalizar Cristal" which has ID "aa_agente_syn"
                if (v.id == "aa_agente_syn") return true
                return false
            }

            if (!permiteMultiAntecedenteArcano && !compendioFantasiaAtivo && !compendioHorrorAtivo && !compendioPathfinderAtivo) {
                val anyArcano = vantagensSelecionadas.any { it.nome.keyify().startsWith("ANTECEDENTE ARCANO") }
                if (anyArcano && vantagensSelecionadas.none { it.nome.keyify() == key }) {
                    return false
                }
            } else {
                val jaTemMesmoId = vantagensSelecionadas.any { it.id == v.id }
                if (jaTemMesmoId) return false
                if (v.id == "antecedente_arcano" && v.choice != null) {
                    val jaTemMesmaChoice = vantagensSelecionadas.any {
                        it.id == "antecedente_arcano" && it.choice?.keyify() == v.choice?.keyify()
                    }
                    if (jaTemMesmaChoice) return false
                }
            }
        }

        // 4) PROFISSIONAL / ESPECIALISTA
        if (key == "profissional" || key == "especialista") {
            val choiceSeguro = v.choice

            if (v.requiresChoice && choiceSeguro != null) {
                val already = vantagensSelecionadas.any {
                    it.nome.keyify() == key &&
                            it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (already) return false
            }

            if (key == "especialista" && choiceSeguro != null) {
                val profExist = vantagensSelecionadas.any {
                    it.id == "profissional" && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (!profExist) return false
            }

            if (choiceSeguro == null) {
                val anyMaxAttr = listaAtributos.any { a ->
                    valoresAtributos[a]!!.intValue == atributoMaxRaw(a)
                }
                val anyMaxPer = periciasComIdiomas().any { p ->
                    rawTotal(p) == periciaCapRaw(p)
                }
                return anyMaxAttr || anyMaxPer
            }

            val choiceKey = choiceSeguro.keyify()
            return if (listaAtributos.contains(choiceKey)) {
                valoresAtributos[choiceKey]!!.intValue == atributoMaxRaw(choiceKey)
            } else {
                val per = getBestPericia(choiceKey) ?: return false
                rawTotal(per) == periciaCapRaw(per)
            }
        }

        // 5) Estágio mínimo (respeita Nasce um Herói)
        val ignorarEstagioPorNasce = (nasceUmHeroi && !emProgresso && pvFromXpOutstanding == 0)
        if (!ignorarEstagioPorNasce) {
            val estagioRequerido = listaDeEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
            if (estagioRequerido != null) {
                val estagioAtual = overrideStageForVantagem?.let { stageName ->
                    listaDeEstagios.firstOrNull { it.nome.equals(stageName, ignoreCase = true) }
                } ?: estagioAtual()

                if (listaDeEstagios.indexOf(estagioAtual) < listaDeEstagios.indexOf(estagioRequerido)) {
                    return false
                }
            }
        }

        // 6) Vantagens prévias
        if (v.requisitos.vantagensPrevias.isNotEmpty()) {
            val faltam = v.requisitos.vantagensPrevias.any { prevId ->
                when (prevId) {
                    "antecedente_arcano", "antecedente_arcano:*" -> {
                        vantagensSelecionadas.none { poss ->
                            poss.id.startsWith("antecedente_arcano_") ||
                                    (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                        }
                    }
                    else -> {
                        vantagensSelecionadas.none { poss ->
                            poss.id == prevId
                        }
                    }
                }
            }
            if (faltam) return false
        }

        // 7) PPs de novo (segurança extra)
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalCompras = comprasPpPorEstagio.values.sum()
            val limite = maxComprasPpAteAgora()
            if (totalCompras >= limite) return false
        }
        else if (v.limiteCompra != "infinito" && v.maxSelections > 0) {
            val ja = vantagensSelecionadas.count { it.id.keyify() == v.id.keyify() }
            if (ja >= v.maxSelections) return false
        }

        // 8) Evita repetir a MESMA choice em vantagens com escolha
        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = vantagensSelecionadas.any {
                it.id == v.id && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }

        // 9) Estágio alternativo (tabela nivelParaEstagio)
        nivelParaEstagio[v.requisitos.estagio]?.let { estReqObj2 ->
            if (estReqObj2.minProgress > effectiveProgressoParaVantagens()) return false
        }

        // 10) Atributos mínimos
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                val attrKey = mapaAtributosDisplay.keys.firstOrNull {
                    it.equals(chaveNorm, ignoreCase = true)
                } ?: chaveNorm
                val atual = valoresAtributos[attrKey]?.intValue ?: return false
                atual < min
            }) return false

        // 11) Perícias mínimas obrigatórias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atendeUma = periciaMinMap.any { (perNome, minRaw) ->
                val per = getBestPericia(perNome)
                per != null && rawTotal(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = getBestPericia(perNome) ?: return@any false
                    rawTotal(per) < minRaw
                }) {
                return false
            }
        }

        // 12) Perícias mínimas opcionais (qualquer uma)
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            if (v.vinculadoPericia && !v.choice.isNullOrBlank()) {
                val choiceKey = v.choice!!.keyify()
                val matchEntry = periciaMinOpcMap.entries.firstOrNull { it.key.keyify() == choiceKey }
                if (matchEntry == null) return false
                val per = getBestPericia(choiceKey) ?: return false
                if (rawTotal(per) < matchEntry.value) return false
            } else {
                val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                    val per = getBestPericia(perNome)
                    per != null && rawTotal(per) >= minRaw
                }
                if (!atendeUmaOpc) return false
            }
        }

        // 13) Exige Carta Selvagem?
        if (v.requisitos.exigeCS && !cartaSelvagem) return false

        // 13a) Tags Raciais
        if (v.requisitos.tags.isNotEmpty()) {
            val ancDef = getAncestralidadeDef(ancestralidade)
            if (ancDef == null || !ancDef.tags.containsAll(v.requisitos.tags)) {
                return false
            }
        }

        // 13b) Tiro Duplo Aprimorado
        if (v.id == "tiro_duplo_aprimorado") {
            val base = vantagensSelecionadas.firstOrNull { it.id == "tiro_duplo" }
            if (base == null) return false
            val choice = base.choice
            if (choice.isNullOrBlank()) return false
            val skill = getBestPericia(choice) ?: return false
            if (rawTotal(skill) < 10) return false
        }

        // 13c) Template Monstruoso
        if (v.requisitos.templatesRequired.isNotEmpty()) {
            val selected = tipoMonstroSelecionado
            if (selected == null || selected !in v.requisitos.templatesRequired) {
                return false
            }
        }

        // 14) Conflitos com complicações (Lento x Ligeiro, etc.)
        val compsConfl = incompatibilidades[key] ?: emptySet()
        val vantKey = v.nome.trim().uppercase()
        if (vantKey == "RICO" || vantKey == "PODRE DE RICO") {
            val tenhoPobreza = complicacoesSelecionadas.keys.any {
                it.id.trim().uppercase() == "POBREZA"
            }
            if (tenhoPobreza) return false
        }
        if (complicacoesSelecionadas.keys
                .map { it.id.keyify() }
                .any { it in compsConfl }
        ) return false

        return true
    }

    var pontosComplicacaoGastos by mutableIntStateOf(0)
    val baseIncsPorPericia = mutableStateMapOf<Pericia, Int>().also { m -> listaPericias.forEach { m[it] = 0 } }
    private val compIncsPorPericia = mutableStateMapOf<Pericia, Int>().also { m -> listaPericias.forEach { m[it] = 0 } }
    val compCostStackPorPericia = mutableStateMapOf<Pericia, MutableList<Int>>().also { m ->
        listaPericias.forEach { m[it] = mutableListOf() }
    }
    val paCostStackPorAtributo = mutableStateMapOf<String, MutableList<Int>>().also { m ->
        listaAtributos.forEach { m[it] = mutableListOf() }
    }
    val spCostStackPorPericia = mutableStateMapOf<Pericia, SnapshotStateList<Int>>().also { m ->
        listaPericias.forEach { m[it] = mutableStateListOf() }
    }

    fun rebuildPericias(desiredRaw: Map<Pericia, Int>) {
        syncLinguistaIdiomas()
        val poolSize = totalSpPool // Updated getter usage
        var cumulativeCost = 0

        periciasComIdiomas().forEach { per ->

            val cap = periciaCapRaw(per)
            val target = (desiredRaw[per] ?: rawTotal(per)).coerceAtMost(cap)

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var curr = periciaStartRaw(ancestralidade, per)
            var freeSteps = compIncsPorPericia.getValue(per)

            while (curr < target && cumulativeCost < poolSize) {
                val next = when {
                    curr == 0 -> 4
                    curr < 12 -> curr + 2
                    else      -> curr + 1
                }

                val attrKey = atributoBaseParaPericia(per)
                val cost    = if (next <= valoresAtributos[attrKey]!!.intValue) 1 else 2
                if (cumulativeCost + cost > poolSize) break

                if (freeSteps > 0) {
                    freeSteps -= 1
                } else {
                    stack.add(cost)
                    baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                    cumulativeCost += cost
                }
                curr = next
            }
        }
    }

    fun decreasePericia(per: Pericia) {
        val spStack = spCostStackPorPericia.getValue(per)
        val idx = spStack.indexOfLast { it > 0 }
        if (idx >= 0) {
            spStack.removeAt(idx)
            val newIncs = baseIncsPorPericia.getValue(per) - 1
            baseIncsPorPericia[per] = newIncs

            if (newIncs == 0) {
                spStack.clear()
                especializacoesPorPericia.remove(per.nome)
                notasPericia.remove(per.nome)
            }

            if (skillAdvancementInProgress) {
                skillsForCurrentAdvancement.remove(per.nome)
            }
        }

        if (isIdiomaPericia(per)) {
            syncIdiomaSlots()
        }
        if (isJutsuPericia(per)) {
            syncJutsuSlots()
        }
    }

    private fun atributoBaseRacial(a: String): Int {
        // Fix: Use keyified ancestry to match DataLoader map keys
        val base = racialAttrMinMap[ancestralidade.keyify()]?.get(a.keyify()) ?: 4

        var modifiedBase = base

        // Monster Bonus
        getMonstroSelecionado()?.let { monstro ->
            val attrKey = a.keyify()
            val bonusEntry = monstro.atributos_bonus.entries.firstOrNull {
                it.key.keyify() == attrKey
            }
            if (bonusEntry != null) {
                // Steps: 1 -> d6, 2 -> d8. Base is d4 (4).
                val steps = bonusEntry.value
                var monsterBase = 4
                repeat(steps) {
                    monsterBase = if (monsterBase < 12) monsterBase + 2 else monsterBase + 1
                }
                modifiedBase = maxOf(modifiedBase, monsterBase)
            }
        }

        // Meio-Elfo Ágil
        if (a.keyify() == "AGILIDADE" && meioElfoAgil) {
            modifiedBase = maxOf(modifiedBase, 6)
        }

        // Meio-Orc: Escolha entre Vigor d6 ou Força d6
        if (ancestralidade.equals("MEIO-ORCS", ignoreCase = true)) {
            if (a.keyify() == "VIGOR") {
                modifiedBase = if (meioOrcForca) 4 else 6
            }
            if (a.keyify() == "FORCA") {
                modifiedBase = if (meioOrcForca) 6 else 4
            }
        }

        // Descendente Elemental (Terra)
        if (ancestralidade.keyify() == "DESCENDENTE ELEMENTAL" || ancestralidade.keyify() == "DESC_ELEMENTAL") {
            if (descendenteElementalSelecionado.equals("Terra", ignoreCase = true) && a.keyify() == "VIGOR") {
                modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        // Arte da Guerra - Signos (only for Humans)
        if (compendioArteDaGuerraAtivo && ancestralidade.keyify().contains("HUMANO")) {
            val sign = signoAdgSelecionado
            val attrKey = a.keyify()
            if (sign != null) {
                if (sign.equals("Boi", ignoreCase = true) && attrKey == "FORCA") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                if (sign.equals("Dragão", ignoreCase = true) && attrKey == "ESPIRITO") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                if (sign.equals("Macaco", ignoreCase = true) && attrKey == "ASTUCIA") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                if (sign.equals("Urso", ignoreCase = true) && attrKey == "VIGOR") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
            }
        }

        // Arte da Guerra - Protagonista (Qualidades de Herói)
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_protagonista") {
            val attrKey = a.keyify()
            when (protagonistaRollQualidade) {
                2 -> if (attrKey == "ASTUCIA") modifiedBase = maxOf(modifiedBase, 6)
                4 -> if (attrKey == "FORCA") modifiedBase = maxOf(modifiedBase, 6)
                6 -> if (attrKey == "ESPIRITO") modifiedBase = maxOf(modifiedBase, 6)
                8 -> if (attrKey == "AGILIDADE") modifiedBase = maxOf(modifiedBase, 6)
                10 -> if (attrKey == "VIGOR") modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        return modifiedBase
    }

    fun atributoMinRaw(a: String): Int =
        atributoBaseRacial(a)

    fun atributoMaxRaw(a: String): Int {
        val minRaw = atributoMinRaw(a)

        var extras = ((minRaw - 4).coerceAtLeast(0) / 2)
        if (a.keyify() == "AGILIDADE" && meioElfoAgil) {
            extras += 1
        }
        val baseCap = 12 + extras

        val chave = a.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        var finalCap = baseCap + (profCount + espCount) * 2

        // Limite de Força por Tamanho (Diminutos/Pequenos)
        // Se Tamanho <= -2 (Pequeno/Muito Pequeno): Força Máxima = d8.
        // Se Tamanho <= -3 (Muito Pequeno/Minúsculo): Força Máxima = d6.
        // Se Tamanho <= -4 (Minúsculo/Diminuto): Força Máxima = d4.
        if (chave == "FORCA") {
            val rawSize = ModifierEngine.sizeRawDisplay(this)
            val sizeCap = when {
                rawSize <= -4 -> 4 // d4
                rawSize == -3 -> 6 // d6
                rawSize == -2 -> 8 // d8
                else -> 100 // Sem limite por tamanho
            }
            if (sizeCap < finalCap) {
                finalCap = sizeCap
            }
        }

        return finalCap
    }

    fun periciaCapRaw(per: Pericia): Int {
        // PROMPT: AdG allows exceeding d12 during creation
        if (compendioArteDaGuerraAtivo) return 20 // Effectively high cap for creation

        val startRaw = periciaStartRaw(ancestralidade, per)

        // Half-Orc Buscatrilha Intimidate Exception (starts d4 but gets cap increase)
        val isHalfOrcIntimidate = compendioPathfinderAtivo &&
                ancestralidade.keyify().contains("MEIO-ORC") &&
                ancestralidade.keyify().contains("PATHFINDER") &&
                per.nome.keyify() == "INTIMIDAR"

        val baseCap = if (startRaw >= 6 || isHalfOrcIntimidate) 13 else 12

        val chave = per.nome.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        return baseCap + (profCount + espCount) * 2
    }

    fun rawTotal(per: Pericia): Int {
        val startRaw     = periciaStartRaw(ancestralidade, per)
        val normalIncs   = baseIncsPorPericia.getValue(per)
        val complicsIncs = compIncsPorPericia.getValue(per)
        val totalIncs    = normalIncs + complicsIncs

        if (startRaw == 0 && totalIncs == 0) return 0

        val (startForSteps, steps) = if (startRaw == 0) {
            4 to (totalIncs - 1).coerceAtLeast(0)
        } else {
            startRaw to totalIncs.coerceAtLeast(0)
        }

        return applySuperStepsFrom(startForSteps, steps)
    }

    fun aplicarAncestralidade(anc: String, feedbackMessages: MutableList<String>) {
        val prevAnc = ancestralidade

        val prevAncDef = getAncestralidadeDef(prevAnc)
        val wasHumano = (prevAnc == "HUMANOS" || prevAncDef?.vantagensGratis?.any { it.keyify() == "ADAPTAVEL" } == true)

        val ancDef = getAncestralidadeDef(anc)
        val vaiSerHumano = (anc == "HUMANOS" || ancDef?.vantagensGratis?.any { it.keyify() == "ADAPTAVEL" } == true)

        val paAntes = pontosAtributo
        val spAntes = pontosPericia
        val pvAntes = pontosVantagem

        // Mapeia as vantagens raciais gratuitas da ancestralidade ANTERIOR
        val prevFreeKeys: Set<String> =
            (vantagensAutomaticas.toSet() +
                    when (prevAnc) {
                        "SAURIOS"    -> setOf("Sentidos Aguçados", "Prontidão")
                        "PEQUENINOS" -> setOf("Sorte")
            "CELESTIAIS" -> setOf("ANTECEDENTE ARCANO MILAGRES", "ANTECEDENTE ARCANO (MILAGRES)")
                        else         -> emptySet()
                    }
                    ).map { it.keyify() }
                .toSet()

        // --- Ajuste do +1 PV de HUMANOS (sem apagar tudo e respeitando pré-requisitos) ---

        if (wasHumano && !vaiSerHumano) {
            // Helper: vantagem é racial gratuita da raça anterior?
            fun isRacialFree(v: Vantagem): Boolean =
                v.nome.keyify() in prevFreeKeys

            // Helper: vantagem é pré-requisito de outra?
            fun isUsedAsPrereq(v: Vantagem): Boolean {
                v.id
                return vantagensSelecionadas.any { other ->
                    other != v && other.requisitos.vantagensPrevias.any { prevId ->
                        when (prevId) {
                            "antecedente_arcano",
                            "antecedente_arcano:*" -> {
                                other.id.startsWith("antecedente_arcano_") ||
                                        (other.id == "antecedente_arcano" && !other.choice.isNullOrBlank())
                            }
                            else -> other.id == prevId
                        }
                    }
                }
            }

            // Candidatos a serem removidos para "pagar" o edge grátis de humano:
            // - não raciais
            // - não são pré-requisito de outra
            // - não são vantagens de PODER (superpoderes)
            // - não são vantagens de cenário automáticas (Superpoderes, Canalizar Cristal, Conexões Máfia)
            val candidatos = vantagensSelecionadas.filter { v ->
                val isScenarioEdge = v.id == "superpoderes" ||
                        v.id == "agente_syn" ||
                        v.id == "aa_agente_syn" ||
                        (v.id == "conexoes" && v.choice?.equals("Máfia", ignoreCase = true) == true)

                !isRacialFree(v) &&
                        !isUsedAsPrereq(v) &&
                        !isScenarioEdge &&
                        !v.categoria.name.equals("PODER", ignoreCase = true)
            }

            if (candidatos.isNotEmpty()) {
                // Remove só UMA vantagem (a última adquirida, por simplicidade)
                val toRemove = candidatos.last()
                vantagensSelecionadas.remove(toRemove)
                feedbackMessages.add("Vantagem ${toRemove.nome} removida para compensar a troca de Ancestralidade.")
            } else {
                // Não sobrou nada "seguro" pra remover → ajusta só o pool de PV
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            }
        } else if (!wasHumano && vaiSerHumano) {
            // Entrando em HUMANOS → ganha 1 PV racial
            pontosVantagem += 1
        }

        // --- Ajuste de atributos pela nova raça ---

        periciasComIdiomas().associateWith { rawTotal(it) }

        val newAttrMods = racialAttrMinMap[anc] ?: emptyMap()

        listaAtributos.forEach { nome ->
            val st     = valoresAtributos[nome]!!
            val newMin = newAttrMods[nome] ?: 4

            val extras = ((newMin - 4).coerceAtLeast(0) / 2)
            val newMax = 12 + extras

            val stack = paCostStackPorAtributo.getValue(nome)
            var raw   = newMin
            var appliedSteps = 0

            repeat(stack.size) {
                val candidate = if (raw < 12) raw + 2 else raw + 1
                if (candidate > newMax) {
                    return@repeat
                }
                raw = candidate
                appliedSteps++
            }

            if (appliedSteps < stack.size) {
                val removidos = stack.size - appliedSteps
                repeat(removidos) {
                    stack.removeAt(stack.lastIndex)
                }
                feedbackMessages.add("$removidos ponto(s) de atributo devolvido(s) de $nome.")
            }

            st.intValue = raw
        }

        // Troca efetiva da ancestralidade
        ancestralidade = anc
        if (compendioArteDaGuerraAtivo && anc.keyify().contains("HUMANO")) {
            if (signoAdgSelecionado == null) {
                selecionarSigno("Nenhum")
            }
        } else if (signoAdgSelecionado != null) {
            selecionarSigno(null)
        }
        celestialAAMilagresDesabilitado = (anc == "CELESTIAIS" && modoSupers)
        if (anc != "MEIO-ELFOS") {
            meioElfoAgil = false
        }
        if (anc != "MEIO-ORCS") {
            meioOrcForca = false
        }
        if (anc.keyify() != "DESCENDENTE ELEMENTAL" && anc.keyify() != "DESC_ELEMENTAL") {
            selecionarDescendenteElemental(null)
        }
        if (!anc.keyify().contains("GNOMO")) {
            selecionarPericiaGnomo(null)
        }

        // --- Vantagens / desvantagens raciais ---

        // Remove APENAS as vantagens raciais automáticas da raça anterior
        if (prevFreeKeys.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.nome.keyify() in prevFreeKeys }
        }

        desvantagensAutomaticas.clear()
        vantagensAutomaticas.clear()
        vantagensRaciais.clear()
        desvantagensRaciais.clear()

        getAncestralidadeDef(anc)?.let { rm ->
            desvantagensAutomaticas.addAll(rm.desvantagens)
            vantagensAutomaticas.addAll(rm.vantagensGratis)
            vantagensRaciais.addAll(rm.vantagensGratis)
            desvantagensRaciais.addAll(rm.desvantagens)
        }

        naturalArmorFromRace = 0

        // Generic Logic for Edges listed in vantagesGratis strings
        getAncestralidadeDef(anc)?.vantagensGratis?.forEach { featString ->
            val featKey = featString.keyify()
            val edge = listaVantagens.firstOrNull { it.nome.keyify() == featKey || it.id == featString || it.id.keyify() == featKey }
            if (edge != null && vantagensSelecionadas.none { it.id == edge.id }) {
                vantagensSelecionadas.add(edge)
            }
        }

        when (anc) {
            "SAURIOS" -> {
                // Prontidão is handled by the generic logic above now, but keeping this doesn't hurt (idempotent).
                // "Sentidos Aguçados" removal is handled earlier.
                naturalArmorFromRace = 2
                armadura = 0
            }
            "PEQUENINOS" -> {
                // "Sorte" vem do JSON. Apenas garantimos a Vantagem mecânica.
                listaVantagens.firstOrNull { it.nome.equals("Sorte", ignoreCase = true) }
                    ?.let {
                        if (vantagensSelecionadas.none { sel -> sel.id == it.id }) {
                            vantagensSelecionadas.add(it)
                        }
                    }
                // "Sorte" já está em vantagensGratis do JSON, então não adicionamos strings duplicadas.
                listaVantagens.firstOrNull { it.nome.equals("Espirituoso", ignoreCase = true) }
                    ?.let {
                        if (vantagensSelecionadas.none { sel -> sel.id == it.id }) {
                            vantagensSelecionadas.add(it)
                        }
                    }

                if (desvantagensRaciais.none { it.contains("Tamanho", ignoreCase = true) }) {
                    desvantagensRaciais.add("Tamanho -1")
                }
                if (desvantagensRaciais.none { it.contains("Movimentação Reduzida", ignoreCase = true) }) {
                    desvantagensRaciais.add("Movimentação Reduzida")
                }
                armadura = 0
            }
            "CELESTIAIS" -> {
                val aaMilagres = listaVantagens.firstOrNull {
                    it.id == "antecedente_arcano_milagres"
                }
                if (aaMilagres != null && vantagensSelecionadas.none { it.id == aaMilagres.id }) {
                    vantagensSelecionadas.add(aaMilagres)
                }
                vantagensAutomaticas.add("ANTECEDENTE ARCANO (MILAGRES)")
                armadura = 0
            }
            "HUMANO (WISEGUYS)".keyify() -> {
                val conexoesMafia = listaVantagens.firstOrNull {
                    it.nome.equals("Conexões (Máfia)", ignoreCase = true)
                }
                if (conexoesMafia != null && vantagensSelecionadas.none { it.nome.equals("Conexões (Máfia)", ignoreCase = true) }) {
                    vantagensSelecionadas.add(conexoesMafia)
                }
            }
            "DESCENDENTE ELEMENTAL" -> {
                if (descendenteElementalSelecionado == null) {
                    selecionarDescendenteElemental("Água")
                } else {
                    // Re-apply to ensure consistency
                    val current = descendenteElementalSelecionado
                    descendenteElementalSelecionado = null
                    selecionarDescendenteElemental(current)
                }
                armadura = 0
            }
            else -> {
                armadura = 0
            }
        }

        // --- Complicações raciais automáticas ---

        val oldAutoKeys = getAncestralidadeDef(prevAnc)
            ?.desvantagens
            ?.map { it.substringBefore("(").trim().keyify() }
            ?.toSet()
            ?: emptySet()

        complicacoesSelecionadas.keys
            .filter { it.id.keyify() in oldAutoKeys }
            .forEach { complicacoesSelecionadas.remove(it) }

        val autoBaseKeys = desvantagensAutomaticas
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()

        listaComplicacoes
            .filter { it.id.keyify() in autoBaseKeys }
            .groupBy { it.id.keyify() }
            .forEach { (_, variants) ->
                val comp = variants.maxByOrNull { getOriginPriority(it.origem) } ?: variants.first()

                val hasMenor = desvantagensAutomaticas.any {
                    it.substringBefore("(").trim().keyify() == comp.id.keyify()
                            && it.contains("Menor", ignoreCase = true)
                }
                val hasMaior = desvantagensAutomaticas.any {
                    it.substringBefore("(").trim().keyify() == comp.id.keyify()
                            && it.contains("Maior", ignoreCase = true)
                }

                when {
                    hasMaior -> complicacoesSelecionadas[comp] = "Maior"
                    hasMenor -> complicacoesSelecionadas[comp] = "Menor"
                    else -> complicacoesSelecionadas[comp] = "Menor"
                }
            }

        // Recalcula pontos de atributo/perícias após o ajuste racial
        recalcularPontosAtributo(feedbackMessages)
        rebuildAllPericiaStacks(feedbackMessages)

        val paDepois = pontosAtributo
        val spDepois = pontosPericia
        val pvDepois = pontosVantagem

        if (paDepois > paAntes) feedbackMessages.add("${paDepois - paAntes} ponto(s) de atributo devolvido(s).")
        if (spDepois > spAntes) feedbackMessages.add("${spDepois - spAntes} ponto(s) de perícia devolvido(s).")
        if (pvDepois > pvAntes) feedbackMessages.add("${pvDepois - pvAntes} ponto(s) de vantagem devolvido(s).")

        // Validar requisitos das vantagens existentes
        var changed = true
        while (changed) {
            changed = false
            val iterator = vantagensSelecionadas.iterator()
            while (iterator.hasNext()) {
                val v = iterator.next()

                val autoKeys = (vantagensAutomaticas + vantagensRaciais)
                    .map { it.substringBefore("(").trim().keyify() }
                    .toSet()

                val autoIds = (vantagensAutomaticas + vantagensRaciais).toSet()

                if (v.nome.substringBefore("(").trim().keyify() in autoKeys) continue
                if (v.id in autoIds) continue
                if (v.id in vantagensAutomaticasDoTropo) continue
                if (v.id == "conexoes" && v.choice?.equals("Máfia", ignoreCase = true) == true) continue

                if (!atendeRequisitosMantidos(v)) {
                    iterator.remove()
                    removeVantagemDinheiro(v)
                    pontosVantagem++
                    feedbackMessages.add("Vantagem '${v.nome}' removida (requisitos não atendidos).")
                    changed = true
                }
            }
        }
        if (pontosVantagem != pvDepois) {
            rebuildAllPericiaStacks(feedbackMessages)
        }
    }

    private fun atendeRequisitosMantidos(v: Vantagem): Boolean {
        // Estágio
        val estagioRequerido = listaDeEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
        if (estagioRequerido != null) {
            val atual = estagioAtual()
            if (listaDeEstagios.indexOf(atual) < listaDeEstagios.indexOf(estagioRequerido)) return false
        }

        // Prévias
        if (v.requisitos.vantagensPrevias.isNotEmpty()) {
            val faltam = v.requisitos.vantagensPrevias.any { prevId ->
                when (prevId) {
                    "antecedente_arcano", "antecedente_arcano:*" -> {
                        vantagensSelecionadas.none { poss ->
                            poss.id.startsWith("antecedente_arcano_") ||
                                    (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                        }
                    }
                    else -> vantagensSelecionadas.none { it.id == prevId }
                }
            }
            if (faltam) return false
        }

        // Atributos
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                val attrKey = mapaAtributosDisplay.keys.firstOrNull { it.equals(chaveNorm, ignoreCase = true) } ?: chaveNorm
                (valoresAtributos[attrKey]?.intValue ?: 0) < min
            }) return false

        // Perícias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atende = periciaMinMap.any { (nome, min) ->
                val p = getBestPericia(nome) ?: return@any false
                rawTotal(p) >= min
            }
            if (!atende) return false
        } else {
            if (periciaMinMap.any { (nome, min) ->
                    val p = getBestPericia(nome) ?: return@any false
                    rawTotal(p) < min
                }) return false
        }

        // Opcionais
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            if (v.vinculadoPericia && !v.choice.isNullOrBlank()) {
                val choiceKey = v.choice!!.keyify()
                val matchEntry = periciaMinOpcMap.entries.firstOrNull { it.key.keyify() == choiceKey }
                if (matchEntry == null) return false
                val per = getBestPericia(choiceKey) ?: return false
                if (rawTotal(per) < matchEntry.value) return false
            } else {
                val atende = periciaMinOpcMap.any { (nome, min) ->
                    val p = getBestPericia(nome) ?: return@any false
                    rawTotal(p) >= min
                }
                if (!atende) return false
            }
        }

        // Tags
        if (v.requisitos.tags.isNotEmpty()) {
            val ancDef = getAncestralidadeDef(ancestralidade)
            if (ancDef == null || !ancDef.tags.containsAll(v.requisitos.tags)) return false
        }

        // Template Monstruoso
        if (v.requisitos.templatesRequired.isNotEmpty()) {
            val selected = tipoMonstroSelecionado
            if (selected == null || selected !in v.requisitos.templatesRequired) {
                return false
            }
        }

        // Tiro Duplo Aprimorado
        if (v.id == "tiro_duplo_aprimorado") {
            val base = vantagensSelecionadas.firstOrNull { it.id == "tiro_duplo" } ?: return false
            val choice = base.choice
            if (choice.isNullOrBlank()) return false
            val skill = getBestPericia(choice) ?: return false
            if (rawTotal(skill) < 10) return false
        }

        return true
    }

    fun spendProgressAcrossStages(n: Int) {
        var remaining = n
        reachedStages().mapIndexed { idx, est -> idx to est }.forEach { (idx, est) ->
            if (remaining == 0) return@forEach
            val cap   = dynamicStageCaps[idx]
            val spent = stageXpSpent.getValue(est.nome)
            val avail = (cap - spent).coerceAtLeast(0)
            val use   = avail.coerceAtMost(remaining)
            if (use > 0) {
                stageXpSpent[est.nome] = spent + use
                remaining -= use
            }
        }
        recomputeAvailableProgress()
    }

    fun spendProgressAtStage(stageName: String, n: Int) {
        val current = stageXpSpent.getValue(stageName)
        stageXpSpent[stageName] = current + n
        recomputeAvailableProgress()
    }

    fun refundProgressAcrossStages(n: Int) {
        var remaining = n
        reachedStages()
            .mapIndexed { idx, est -> idx to est }
            .asReversed()
            .forEach { (_, est) ->
                if (remaining == 0) return@forEach
                val spent = stageXpSpent.getValue(est.nome)
                if (spent > 0) {
                    val refund = spent.coerceAtMost(remaining)
                    stageXpSpent[est.nome] = spent - refund
                    remaining -= refund
                }
            }
        recomputeAvailableProgress()
    }

    fun refundProgressAtStage(stageName: String, n: Int) {
        val current = stageXpSpent.getValue(stageName)
        stageXpSpent[stageName] = (current - n).coerceAtLeast(0)
        recomputeAvailableProgress()
    }

    fun recomputeAvailableProgress() {
        val totalSpent = stageXpSpent.values.sum()
        val availableByProgress = (progresso - totalSpent).coerceAtLeast(0)

        val remainingStageCapacity = reachedStages()
            .sumOf { stage ->
                val stageIndex = listaDeEstagios.indexOf(stage)
                val cap = dynamicStageCaps[stageIndex]
                val spentHere = stageXpSpent.getValue(stage.nome)
                (cap - spentHere).coerceAtLeast(0)
            }

        progressosDisponiveis = kotlin.math.min(availableByProgress, remainingStageCapacity)
    }

    fun checkFreeze() {
        val idx = currentProgressStageIndex()
        val est = listaDeEstagios[idx]
        val cap = dynamicStageCaps[idx]
        val spent = stageXpSpent.getValue(est.nome)
        if (spent == cap) {
            frozenAdvantageCount = vantagensSelecionadas.size
        }
    }

    val isAdgLockedMode: Boolean
        get() = compendioArteDaGuerraAtivo && tropoSelecionado == null

    fun isSectionEnabled(section: MainSection): Boolean {
        if (modoProgressaoAtivo) return true
        if (!compendioArteDaGuerraAtivo) return true

        return if (tropoSelecionado == null) {
            // "Locked Mode" (No Trope selected yet):
            // Can see Summary, Ancestry, and Trope selection.
            // Other tabs are disabled.
            when (section) {
                MainSection.RESUMO, MainSection.ANCESTRALIDADES, MainSection.TROPOS -> true
                else -> false
            }
        } else {
            // "Unlocked Mode" (Trope selected):
            // Ancestry is now LOCKED (disabled).
            // Trope is ENABLED (to change back to 'None').
            // All other tabs are ENABLED.
            when (section) {
                MainSection.ANCESTRALIDADES -> false
                else -> true
            }
        }
    }

    // PROMPT 1: Explicit calculation: (Current Step - Racial Base Step)
    private fun calcularPontosAtributoRestantes(): Int {
        var usados = 0

        for (nome in listaAtributos) {
            val atual = valoresAtributos[nome]!!.intValue
            val base = atributoBaseRacial(nome)
            val stackSize = paCostStackPorAtributo[nome]?.size ?: 0
            val valorSemBonus = applySuperStepsFrom(base, stackSize)
            val valorParaCusto = if (atual > valorSemBonus) valorSemBonus else atual

            // PROMPT 1: Explicit calculation: (Current Step - Racial Base Step)
            // Steps count: d4=0, d6=1, d8=2, d10=3, d12=4
            // Since we store values as (4, 6, 8, 10, 12), we can iterate or calculate directly.
            // The previous loop logic was: loop from base to current, incrementing cost.
            // This IS the correct logic for "cost is investment above racial base".
            // Refactoring to be clearer/more explicit if needed, but the loop is robust for d12+ handling.

            var cur = base
            while (cur < valorParaCusto) {
                cur += if (cur < 12) 2 else 1
                usados += 1
            }
        }

        val isPathfinderHuman = compendioPathfinderAtivo &&
                (ancestralidade.equals("Humano (Pathfinder)", ignoreCase = true) ||
                        ancestralidade.equals("Humano (Pathfinder)", ignoreCase = true))
        val isPathfinderHalfElf = compendioPathfinderAtivo &&
                (ancestralidade.keyify().contains("MEIO-ELFO") && ancestralidade.keyify().contains("PATHFINDER"))

        val basePoints = if (isPathfinderHuman || isPathfinderHalfElf) 6 else 5

        return (basePoints + cpPaStack.size + paFromProgress - jovemMalusPa) - usados
    }

    private fun artistaMarcialPotencialFisicoAttrKey(): String? {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_artista_marcial") return null
        return when (artistaMarcialPotencialFisico?.keyify()) {
            "AGILIDADE" -> "AGILIDADE"
            "FORCA" -> "FORCA"
            "VIGOR" -> "VIGOR"
            else -> null
        }
    }

    private fun shouldApplyArtistaMarcialBonus(attrKey: String): Boolean {
        val selected = artistaMarcialPotencialFisicoAttrKey() ?: return false
        if (selected != attrKey) return false
        val base = atributoBaseRacial(attrKey)
        val stackSize = paCostStackPorAtributo[attrKey]?.size ?: 0
        val valueFromStack = applySuperStepsFrom(base, stackSize)
        return valueFromStack < atributoMaxRaw(attrKey)
    }

    fun recalcularPontosAtributo(feedbackMessages: MutableList<String> = mutableListOf()) {

        // Ensure current values meet the new racial base (e.g. if Sign increased base from d4 to d6)
        listaAtributos.forEach { attrKey ->
            val min = atributoBaseRacial(attrKey)
            val state = valoresAtributos[attrKey]
            val stack = paCostStackPorAtributo[attrKey] ?: emptyList()

            var newValue = min
            repeat(stack.size) {
                newValue = if (newValue < 12) newValue + 2 else newValue + 1
            }

            if (state != null) {
                state.intValue = newValue
            }
        }

        listaAtributos.forEach { attrKey ->
            val state = valoresAtributos[attrKey] ?: return@forEach
            if (shouldApplyArtistaMarcialBonus(attrKey)) {
                val max = atributoMaxRaw(attrKey)
                state.intValue = minOf(applySuperStepsFrom(state.intValue, 1), max)
            }
        }

        pontosAtributo = calcularPontosAtributoRestantes()

        trimAttributeStacks(feedbackMessages)

        rebuildAllPericiaStacks(feedbackMessages)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun selecionarSigno(novoSigno: String?) {
        if (signoAdgSelecionado == novoSigno) return

        val isHumanAdg = compendioArteDaGuerraAtivo && ancestralidade.keyify().contains("HUMANO")
        if (compendioArteDaGuerraAtivo &&
            signoAdgSelecionado.equals("Nenhum", ignoreCase = true) &&
            !novoSigno.equals("Nenhum", ignoreCase = true)
        ) {
            if (pontosVantagem > 0) {
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            } else {
                removerUltimaVantagemCompradaComPv()
            }
        }

        // 1. Remove old edges from previous sign
        if (vantagensAutomaticasDoSigno.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoSigno }
            vantagensAutomaticasDoSigno.clear()
        }

        signoAdgSelecionado = novoSigno

        // 2. Add new edges
        if (novoSigno != null) {
            val edgesToAdd = mutableListOf<String>()
            when (novoSigno) {
                "Basabasa" -> edgesToAdd.add("atraente")
                "Raposa" -> edgesToAdd.add("elevar_a_moral")
                "Lobo" -> edgesToAdd.add("elo_comum")
            }

            edgesToAdd.forEach { edgeId ->
                val vant = listaVantagens.firstOrNull { it.id == edgeId }
                if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
                    vantagensSelecionadas.add(vant)
                    vantagensAutomaticasDoSigno.add(vant.id)
                }
            }
        }

        if (isHumanAdg && novoSigno.equals("Nenhum", ignoreCase = true)) {
            pontosVantagem += 1
        }

        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
    }

    private fun syncArtistaMarcialPotencialFisico() {
        if (vantagensAutomaticasDoPotencialFisico.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoPotencialFisico }
            vantagensAutomaticasDoPotencialFisico.clear()
        }

        if (tropoSelecionado?.id != "tropo_artista_marcial") return

        val edgeId = when (artistaMarcialPotencialFisico?.keyify()) {
            "AGILIDADE" -> "esquiva"
            "FORCA" -> "bloquear"
            "VIGOR" -> "reflexos_de_combate"
            else -> null
        } ?: return

        val vant = listaVantagens.firstOrNull { it.id == edgeId }
        if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
            vantagensSelecionadas.add(vant)
            vantagensAutomaticasDoPotencialFisico.add(vant.id)
        }
    }

    fun atualizarArtistaMarcialJutsuOpcao(novaOpcao: String) {
        if (artistaMarcialJutsuOpcao == novaOpcao) return
        artistaMarcialJutsuOpcao = novaOpcao
        rebuildAllPericiaStacks()
        syncJutsuSlots()
    }

    fun atualizarArtistaMarcialPotencialFisico(novoPotencial: String?) {
        if (artistaMarcialPotencialFisico == novoPotencial) return
        artistaMarcialPotencialFisico = novoPotencial
        syncArtistaMarcialPotencialFisico()
        recalcularPontosAtributo()
    }

    fun toggleArtistaMarcialTecnica(tecnica: String) {
        if (artistaMarcialTecnicasSelecionadas.contains(tecnica)) {
            artistaMarcialTecnicasSelecionadas.remove(tecnica)
            return
        }
        if (artistaMarcialTecnicasSelecionadas.size >= 3) return
        artistaMarcialTecnicasSelecionadas.add(tecnica)
    }

    fun selecionarPericiaGnomo(pericia: String?) {
        if (gnomoPericiaEscolhida == pericia) return
        gnomoPericiaEscolhida = pericia
        rebuildAllPericiaStacks()
    }

    fun updateProtagonistaRollTecnicas(value: Int?) {
        if (protagonistaRollTecnicas == value) return
        protagonistaRollTecnicas = value?.coerceIn(1, 4)
        syncMestreDoChiSlots()
    }

    fun updateProtagonistaRollPericia(value: Int?) {
        if (protagonistaRollPericia == value) return
        protagonistaRollPericia = value?.coerceIn(1, 6)
        if (protagonistaRollPericia != 1) {
            protagonistaPericiasEscolhidas = emptyList()
        }
        rebuildAllPericiaStacks()
    }

    fun updateProtagonistaRollVantagem(value: Int?) {
        if (protagonistaRollVantagem == value) return
        protagonistaRollVantagem = value?.coerceIn(1, 8)
    }

    fun updateProtagonistaRollQualidade(value: Int?) {
        if (protagonistaRollQualidade == value) return
        protagonistaRollQualidade = value?.coerceIn(1, 10)
        if (protagonistaRollQualidade != 7) {
            protagonistaPericiasPaixao = emptyList()
        }
        atualizarProtagonistaAutoVantagens()
        recalcularPontosAtributo()
    }

    fun updateProtagonistaRollHabilidade(value: Int?) {
        if (protagonistaRollHabilidade == value) return
        protagonistaRollHabilidade = value?.coerceIn(1, 12)
        syncProtagonistaBonusPv()
    }

    fun updateProtagonistaPericiasEscolhidas(value: List<String>) {
        protagonistaPericiasEscolhidas = value
        rebuildAllPericiaStacks()
    }

    fun updateProtagonistaPericiasPaixao(value: List<String>) {
        protagonistaPericiasPaixao = value
        rebuildAllPericiaStacks()
    }

    private fun tecnicasIniciaisProtagonista(): Int {
        return when (protagonistaRollTecnicas) {
            1 -> 1
            2 -> 2
            3 -> 2
            4 -> 3
            else -> 0
        }
    }

    private fun protagonistaPericiasDoTropo(): Set<String> {
        val roll = protagonistaRollPericia ?: return emptySet()
        val paixaoAtiva = protagonistaRollQualidade == 7
        val paixaoList = if (paixaoAtiva) protagonistaPericiasPaixao else emptyList()
        return if (roll == 1) {
            (protagonistaPericiasEscolhidas + paixaoList)
                .map { it.keyify() }
                .toSet()
        } else {
            val pericia = when (roll) {
                2 -> "Atletismo"
                3 -> "Conhecimento Geral"
                4 -> "Perceber"
                5 -> "Persuadir"
                6 -> "Furtividade"
                else -> null
            }
            val baseList = buildList {
                if (pericia != null) add(pericia)
                addAll(paixaoList)
            }
            baseList.map { it.keyify() }.toSet()
        }
    }

    private fun atualizarProtagonistaAutoVantagens() {
        if (vantagensAutomaticasDoProtagonista.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoProtagonista }
            vantagensAutomaticasDoProtagonista.clear()
        }

        val qualidade = protagonistaRollQualidade ?: return
        val edgesToAdd = when (qualidade) {
            1 -> listOf("corajoso", "elevar_a_moral")
            3 -> listOf("confiavel", "comando")
            else -> emptyList()
        }

        edgesToAdd.forEach { edgeId ->
            val vant = listaVantagens.firstOrNull { it.id == edgeId }
            if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
                vantagensSelecionadas.add(vant)
                vantagensAutomaticasDoProtagonista.add(vant.id)
            }
        }
    }

    private fun syncProtagonistaBonusPv() {
        val shouldHaveBonus = compendioArteDaGuerraAtivo &&
            tropoSelecionado?.id == "tropo_protagonista" &&
            protagonistaRollHabilidade == 4

        if (shouldHaveBonus && !protagonistaBonusPv) {
            pontosVantagem += 1
            protagonistaBonusPv = true
        } else if (!shouldHaveBonus && protagonistaBonusPv) {
            if (pontosVantagem > 0) {
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            } else {
                removerUltimaVantagemCompradaComPv()
            }
            protagonistaBonusPv = false
        }
    }

    private fun syncMestreDoChiSlots() {
        rebuildAllPericiaStacks()
        poderSlotsPorArcano["MESTRE DO CHI"]?.let { slots ->
            val required = getSlotsCountForArcano("MESTRE DO CHI")
            while (slots.size < required) slots.add(null)
            while (slots.size > required && slots.lastOrNull() == null) {
                slots.removeLast()
            }
            syncPoderesSelecionadosFromSlots()
        }
    }

    fun selecionarDescendenteElemental(novoElemento: String?) {
        if (descendenteElementalSelecionado == novoElemento) return

        // 1. Remove old edges
        if (vantagensAutomaticasDoElemento.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoElemento }
            vantagensAutomaticasDoElemento.clear()
        }

        descendenteElementalSelecionado = novoElemento

        // 2. Add new edges
        if (novoElemento != null) {
            val edgesToAdd = mutableListOf<String>()
            when (novoElemento) {
                "Ar" -> edgesToAdd.add("ar_interno")
                "Água" -> edgesToAdd.add("aquatico")
                "Fogo" -> edgesToAdd.add("rapido")
                "Terra" -> edgesToAdd.add("solido_como_rocha")
            }

            edgesToAdd.forEach { edgeId ->
                val vant = listaVantagens.firstOrNull { it.id == edgeId || it.nome.keyify() == edgeId.keyify() }
                if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
                    vantagensSelecionadas.add(vant)
                    vantagensAutomaticasDoElemento.add(vant.id)
                }
            }
        }

        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun selecionarTropo(novoTropo: Tropo?) {
        if (tropoSelecionado?.id == novoTropo?.id) return

        if (vantagensAutomaticasDoTropo.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoTropo }
            vantagensAutomaticasDoTropo.clear()
        }
        if (vantagensAutomaticasDoPotencialFisico.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoPotencialFisico }
            vantagensAutomaticasDoPotencialFisico.clear()
        }
        if (vantagensAutomaticasDoProtagonista.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoProtagonista }
            vantagensAutomaticasDoProtagonista.clear()
        }
        if (tropoSelecionado?.id != "tropo_artista_marcial") {
            artistaMarcialTecnicasSelecionadas.clear()
        }
        if (tropoSelecionado?.id != "tropo_buxista") {
            buXistaCaminhoSelecionado = null
        }
        if (tropoSelecionado?.id != "tropo_elementalista") {
            elementalistaElementoSelecionado = null
        }

        tropoSelecionado = novoTropo

        if (novoTropo != null) {
            novoTropo.ganhaAoComprar.forEach { vantId ->
                val vant = listaVantagens.firstOrNull { it.id == vantId } ?: return@forEach
                if (vantagensSelecionadas.none { it.id == vant.id }) {
                    vantagensSelecionadas += vant
                    vantagensAutomaticasDoTropo += vant.id
                }
            }

            // PROMPT: Equipamentos de Tropo
            // Se encontrar equipamento com ID "kit_[nome_tropo_limpo]", adiciona.
            // ID trope example: tropo_samurai -> clean: samurai -> kit_samurai
            if (compendioArteDaGuerraAtivo) {
                val suffix = novoTropo.id.removePrefix("tropo_")

                // Remove previous kits?
                // Better to assume user manages inventory, but if switching tropes repeatedly it might clutter.
                // For now, just add.
                val kitItem = listaEquipamentos.firstOrNull {
                    val key = it.nome.keyify()
                    key == "kit_$suffix" || key == "kit_de_$suffix"
                }
                if (kitItem != null) {
                    equipamentosComprados.add(kitItem)
                }
            }
            if (novoTropo.id != "tropo_protagonista") {
                protagonistaRollTecnicas = null
                protagonistaRollPericia = null
                protagonistaRollVantagem = null
                protagonistaRollQualidade = null
                protagonistaRollHabilidade = null
                protagonistaPericiasEscolhidas = emptyList()
                protagonistaPericiasPaixao = emptyList()
                vantagensSlotProtagonista.clear()
                syncProtagonistaBonusPv()
            } else {
                atualizarProtagonistaAutoVantagens()
                syncProtagonistaBonusPv()
            }
            if (novoTropo.id == "tropo_artista_marcial" && artistaMarcialPotencialFisico == null) {
                artistaMarcialPotencialFisico = "Agilidade"
            }
            syncArtistaMarcialPotencialFisico()
            if (novoTropo.id != "tropo_artista_marcial") {
                artistaMarcialTecnicasSelecionadas.clear()
            }
            if (novoTropo.id == "tropo_buxista" && buXistaCaminhoSelecionado == null) {
                buXistaCaminhoSelecionado = "Equilibrado"
            }
            if (novoTropo.id == "tropo_elementalista" && elementalistaElementoSelecionado == null) {
                elementalistaElementoSelecionado = "Fogo"
            }
        } else {
            protagonistaRollTecnicas = null
            protagonistaRollPericia = null
            protagonistaRollVantagem = null
            protagonistaRollQualidade = null
            protagonistaRollHabilidade = null
            protagonistaPericiasEscolhidas = emptyList()
            protagonistaPericiasPaixao = emptyList()
            vantagensSlotProtagonista.clear()
            syncProtagonistaBonusPv()
            syncArtistaMarcialPotencialFisico()
            artistaMarcialTecnicasSelecionadas.clear()
            buXistaCaminhoSelecionado = null
            elementalistaElementoSelecionado = null
        }

        syncMestreDoChiSlots()
        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
        syncJutsuSlots()
    }

    private fun trimAttributeStacks(feedbackMessages: MutableList<String> = mutableListOf()) {

        while (pontosAtributo < 0) {
            val entry = paCostStackPorAtributo
                .entries
                .firstOrNull { it.value.isNotEmpty() }
                ?: break

            val nomeAttr = entry.key
            val stack    = entry.value

            stack.removeAt(stack.size - 1)

            val base = atributoBaseRacial(nomeAttr)

            val atual = valoresAtributos[nomeAttr]!!.intValue

            val novo = if (atual > 12) atual - 1 else atual - 2
            valoresAtributos[nomeAttr]!!.intValue = novo.coerceAtLeast(base)

            feedbackMessages.add("Atributo $nomeAttr reduzido para d${novo.coerceAtLeast(base)} para compensar pontos.")

            pontosAtributo = calcularPontosAtributoRestantes()
        }
    }

    fun applyYoungMinor() {
        jovemAutoPequeno = false
        jovemMalusPa = 1
        jovemMalusSp = 2
        recalcularPontosAtributo()
    }

    fun applyYoungMajor(pequComp: Complicacao) {
        jovemAutoPequeno = true
        jovemMalusPa = 2
        jovemMalusSp = 2
        desvantagensAutomaticas.add(pequComp.id.substringBefore("(").trim())
        complicacoesSelecionadas[pequComp] = "Menor"
        recalcularPontosAtributo()
    }

    fun removeYoung(pequComp: Complicacao) {
        jovemAutoPequeno = false
        jovemMalusPa = 0
        jovemMalusSp = 0
        desvantagensAutomaticas.remove(pequComp.id.substringBefore("(").trim())
        complicacoesSelecionadas.remove(pequComp)
        recalcularPontosAtributo()
    }

    var emProgresso by mutableStateOf(false)
    var modoProgressaoAtivo by mutableStateOf(false)
    var mostrandoVantagensProgresso by mutableStateOf(false)
    var mostrandoPericiasProgresso by mutableStateOf(false)
    var mostrandoAtributosProgresso by mutableStateOf(false)
    var mostrandoPoderesProgresso by mutableStateOf(false)
    val frozenSkillIncrements = mutableStateMapOf<String, Int>()

    // Novas variáveis para rastrear o avanço de perícias
    var skillAdvancementInProgress by mutableStateOf(false)
    val skillsForCurrentAdvancement = mutableStateListOf<String>()

    // Novas variáveis para rastrear o avanço de vantagens
    var advantageAdvancementInProgress by mutableStateOf(false)
    var advantageForCurrentAdvancement by mutableStateOf<String?>(null)

    // Avanço de atributos
    var attributeAdvancementInProgress by mutableStateOf(false)
    var attributeStageForCurrentAdvancement by mutableStateOf<String?>(null)
    var stageNameForCurrentAdvancement by mutableStateOf<String?>(null)
    var attributeStacksBeforeAdvancement by mutableStateOf<Map<String, Int>?>(null)
    var attributeUsedReservation by mutableStateOf(false)

    val advancementHistory = mutableStateListOf<com.example.swadebuilder.model.AdvancementAction>()

    fun updateEmProgressoFlag() {
        emProgresso =
            skillAdvancementInProgress ||
                    advantageAdvancementInProgress ||
                    attributeAdvancementInProgress
    }

    fun snapshotFrozenSkillIncrements() {
        frozenSkillIncrements.clear()
        baseIncsPorPericia.forEach { (pericia, incs) ->
            frozenSkillIncrements[pericia.nome] = incs
        }
    }

    fun increasePericiaFromAdvancement(per: Pericia, cost: Int) {
        if (skillAdvancementInProgress) {
            skillsForCurrentAdvancement.add(per.nome)
        }
        baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
        spCostStackPorPericia.getValue(per).add(cost)
        if (isIdiomaPericia(per)) {
            if (notasPericia[per.nome].isNullOrBlank()) {
                notasPericia[per.nome] = idiomaDefaultLabel(per)
            }
            syncIdiomaSlots()
        }
        if (isJutsuPericia(per)) {
            syncJutsuSlots()
        }
    }

    fun baseCreationComplete(): Boolean = pontosAtributo == 0 &&
            pontosPericia == 0 &&
            pontosVantagem == 0 &&
            (pontosComplicacao - pontosComplicacaoGastos).coerceAtLeast(0) == 0

    fun creationComplete(): Boolean {
        // "Ficha básica completa": todos os pontos iniciais foram distribuídos.
        // Em campanha supers, também exige ter zerado os Pontos de Super.
        val supersProntos = !modoSupers || (superPontosTotais > 0 && superPontosDisponiveis == 0)

        return baseCreationComplete() && supersProntos
    }

    val stageXpSpent: SnapshotStateMap<String, Int> = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    var progressosDisponiveis by mutableIntStateOf(0)

    val xpSlots = mutableStateListOf<Boolean>().apply {
        repeat(20) { add(false) }
    }

    private fun reachedStages(): List<Estagio> =
        listaDeEstagios.filter { progresso >= it.minProgress }

    fun atributoRawBaseSemSupers(attrKey: String): Int {
        val key = attrKey.uppercase().trim()
        val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
        val baseMin = mods[key] ?: 4

        // Quantos "steps" base foram comprados na criação
        val stepsBase = paCostStackPorAtributo[key]?.size ?: 0

        var raw = baseMin
        repeat(stepsBase) {
            raw += if (raw < 12) 2 else 1
        }
        return raw
    }

    fun snapshotAttributeStacks(): Map<String, Int> =
        paCostStackPorAtributo.mapValues { (_, stack) -> stack.size }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun restoreAttributeStacks(snapshot: Map<String, Int>) {
        listaAtributos.forEach { attr ->
            val stack = paCostStackPorAtributo.getValue(attr)
            val target = snapshot[attr] ?: 0
            while (stack.size > target) {
                stack.removeLast()
                val current = valoresAtributos[attr]!!.intValue
                val prev = if (current > 12) current - 1 else current - 2
                valoresAtributos[attr]!!.intValue = prev
            }
        }
        recalcularPontosAtributo()
    }

    fun rebuildAllPericiaStacks(
        feedbackMessages: MutableList<String> = mutableListOf(),
        enforcePoolLimit: Boolean = true
    ) {
        if (modoProgressaoAtivo) return

        syncLinguistaIdiomas()
        syncJutsuSlots()

        var cumulativeCost = 0
        val pool = totalSpPool

        periciasComIdiomas().forEach { per ->

            val desiredRaw = rawTotal(per)
            val cap       = periciaCapRaw(per)
            val minRaw    = maxOf(if (isPericiaBasicaEfetiva(per)) 4 else 0, linguistaMinRawFor(per))

            var target = desiredRaw.coerceIn(minRaw, cap)

            fun costFor(tgt: Int): Int {
                var curr = periciaStartRaw(ancestralidade, per)
                var freeSteps = compIncsPorPericia.getValue(per)
                var sum  = 0
                while (curr < tgt) {
                    val next     = if (curr == 0) 4 else curr + 2
                    val attrKey  = atributoBaseParaPericia(per)

                    // >>> AQUI: atributo para custo ignora supers enquanto estiver na fase supers de criação
                    val attrRawForCost =
                        if (faseSupersAtiva && !emProgresso) {
                            atributoRawBaseSemSupers(attrKey)
                        } else {
                            valoresAtributos[attrKey]!!.intValue
                        }

                    val stepCost = if (next <= attrRawForCost) 1 else 2
                    if (freeSteps > 0) {
                        freeSteps -= 1
                    } else {
                        sum += stepCost
                    }
                    curr = next
                }
                return sum
            }

            var cost = costFor(target)

            if (enforcePoolLimit && cost > 0 && cumulativeCost + cost > pool) {
                feedbackMessages.add("Perícia ${per.nome} reduzida para d$target para compensar pontos.")
            }
            while (enforcePoolLimit && cumulativeCost + cost > pool) {
                target = (target - 2).coerceAtLeast(minRaw)
                cost   = costFor(target)
            }

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var currRaw = periciaStartRaw(ancestralidade, per)
            var freeSteps = compIncsPorPericia.getValue(per)
            while (currRaw < target) {
                val next     = if (currRaw == 0) 4 else currRaw + 2
                val attrKey  = atributoBaseParaPericia(per)

                // >>> MESMA REGRA AQUI
                val attrRawForCost =
                    if (faseSupersAtiva && !emProgresso) {
                        atributoRawBaseSemSupers(attrKey)
                    } else {
                        valoresAtributos[attrKey]!!.intValue
                    }

                val stepCost = if (next <= attrRawForCost) 1 else 2
                if (freeSteps > 0) {
                    freeSteps -= 1
                } else {
                    stack.add(stepCost)
                    baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                }
                currRaw = next
            }

            cumulativeCost += cost
        }
    }

    fun toSnapshot(): PersonagemSnapshot {
        val snapshotId = idAtual ?: UUID.randomUUID().toString()

        return PersonagemSnapshot(
            version = 2,
            id = snapshotId,
            nome = nomePersonagem,
            timestamp = System.currentTimeMillis(),
            appTheme = appTheme.name,
            hapticStrength = hapticStrength,
            soundVolume = soundVolume,
            anotacoes = anotacoes,
            flags = SnapshotFlags(
                cartaSelvagem = cartaSelvagem,
                maisPontosPericias = maisPontosPericias,
                modoSupers = modoSupers,
                compendioFantasiaAtivo = compendioFantasiaAtivo,
                compendioHorrorAtivo = compendioHorrorAtivo,
                compendioSciFiAtivo = compendioSciFiAtivo,
                compendioScifiMechasCiberneticosAtivo = compendioScifiMechasCiberneticosAtivo,
                compendioPathfinderAtivo = compendioPathfinderAtivo,
                compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                compendioWiseguysAtivo = compendioWiseguysAtivo,
                optRegraFama = optRegraFama,
                modoOficialAtivo = modoOficialAtivo,
                modoMonstroAtivo = modoMonstroAtivo,
                tipoMonstroSelecionado = tipoMonstroSelecionado,
                usarEspecializacoesDePericia = usarEspecializacoesDePericia,
                grandesResponsabilidades = grandesResponsabilidades,
                nasceUmHeroi = nasceUmHeroi,
                soldadoCargaAtivo = soldadoCargaAtivo,
                permiteMultiAntecedenteArcano = permiteMultiAntecedenteArcano,
                meioElfoAgil = meioElfoAgil,
                meioOrcForca = meioOrcForca,
                celestialAAMilagresDesabilitado = celestialAAMilagresDesabilitado,
                jovemAutoPequeno = jovemAutoPequeno,
                jovemMalusPa = jovemMalusPa,
                jovemMalusSp = jovemMalusSp,
                idosoBonusSp = idosoBonusSp,
                obesoBonusSize = obesoBonusSize,
                obesoMalusMov = obesoMalusMov,
                bonusPoderExtra = bonusPoderExtra,
                optRegraRiqueza = optRegraRiqueza,
                optRegraCosaNostra = optRegraCosaNostra
            ),
            recursos = SnapshotRecursos(
                dinheiro = dinheiro,
                requisicao = requisicao,
                pontosVantagem = pontosVantagem,
                pontosAtributo = pontosAtributo,
                pontosComplicacaoGastos = pontosComplicacaoGastos,
                famaManual = famaManual,
                paFromProgress = paFromProgress,
                spFromProgress = spFromProgress,
                legendaryAttrReservations = legendaryAttrReservations,
                cpPaStack = cpPaStack.toList(),
                cpSpStack = cpSpStack.map { 1 },
                cpPvStack = cpPvStack.map { 1 },
                cpRecursosStack = cpRecursosStack.map { 1 },
                riquezaModifier = riquezaModifier,
                carteiraPathfinder = carteiraPathfinder.toMap()
            ),
            atributos = SnapshotAtributos(
                ancestralidade = ancestralidade,
                valoresAtributos = valoresAtributos.mapValues { it.value.intValue },
                paCostStackPorAtributo = paCostStackPorAtributo.mapValues { it.value.toList() }
            ),
            pericias = SnapshotPericias(
                baseIncsPorPericia = baseIncsPorPericia.mapKeys { it.key.nome },
                compIncsPorPericia = compIncsPorPericia.mapKeys { it.key.nome },
                spCostStackPorPericia = spCostStackPorPericia.mapKeys { it.key.nome }.mapValues { it.value.toList() },
                compCostStackPorPericia = compCostStackPorPericia.mapKeys { it.key.nome }.mapValues { it.value.toList() },
                especializacoesPorPericia = especializacoesPorPericia.toMap(),
                // PROMPT 5: Persist Skill Notes
                notasPericia = notasPericia.toMap()
            ),
            selecoes = SnapshotSelecoes(
                vantagens = vantagensSelecionadas.map { AdvantageSnapshot(it.id, it.choice) },
                vantagensAutomaticas = vantagensAutomaticas.toList(),
                vantagensRaciais = vantagensRaciais.toList(),
                desvantagensAutomaticas = desvantagensAutomaticas.toList(),
                desvantagensRaciais = desvantagensRaciais.toList(),
                complicacoesSelecionadas = complicacoesSelecionadas.map { (comp, nivel) ->
                    ComplicacaoSnapshot(comp.id, nivel)
                },
                // PROMPT 3: Transtornos
                transtornos = transtornos.map { ComplicacaoSnapshot(it.id, null) },
                reservasComplicacaoMaior = reservasComplicacaoMaior.toMap(),
                poderesSelecionados = poderesSelecionados.toList(),
                manifestacoesPoderes = manifestacoesPoderes.toMap(),
                poderSlotsPorArcano = poderSlotsPorArcano.mapValues { it.value.toList() },
                novosPoderesStacksPorArcano = novosPoderesStacksPorArcano
                    .mapValues { (_, pilhas) -> pilhas.map { it.toList() } },
                arcanoEmCompraViaXpKey = arcanoEmCompraViaXpKey,
                arcanoSnapshotAntesDaCompra = arcanoSnapshotAntesDaCompra,
                equipamentosComprados = equipamentosComprados.toList(),
                coracaoCrystalId = coracaoCrystalSelecionado?.id,
                tropoSelecionadoId = tropoSelecionado?.id,
                vantagensTropoAutomaticas = vantagensAutomaticasDoTropo.toList(),
                tecnicasIniciaisTropo = tecnicasIniciaisFromTropo,
                retratoFileName = portraitFileName,
                expandirRetrato = expandirRetrato,
                portraitScaleType = portraitScaleType,
                portraitAlignment = portraitAlignment,
                signoAdgSelecionado = signoAdgSelecionado,
                artistaMarcialJutsuOpcao = artistaMarcialJutsuOpcao,
                artistaMarcialPotencialFisico = artistaMarcialPotencialFisico,
                artistaMarcialTecnicasSelecionadas = artistaMarcialTecnicasSelecionadas.toList(),
                buXistaCaminhoSelecionado = buXistaCaminhoSelecionado,
                elementalistaElementoSelecionado = elementalistaElementoSelecionado,
                protagonistaRollTecnicas = protagonistaRollTecnicas,
                protagonistaRollPericia = protagonistaRollPericia,
                protagonistaRollVantagem = protagonistaRollVantagem,
                protagonistaRollQualidade = protagonistaRollQualidade,
                protagonistaRollHabilidade = protagonistaRollHabilidade,
                protagonistaPericiasEscolhidas = protagonistaPericiasEscolhidas,
                protagonistaPericiasPaixao = protagonistaPericiasPaixao,
                protagonistaSlotAdvantageIds = vantagensSlotProtagonista.toList(),
                gnomoPericiaEscolhida = gnomoPericiaEscolhida,
                dominioClerigoSelecionado = dominioClerigoSelecionado,
                dominioClerigoPathfinderSelecionado = dominioClerigoPathfinderSelecionado
            ),
            progresso = SnapshotProgresso(
                progresso = progresso,
                progressosDisponiveis = progressosDisponiveis,
                stageXpSpent = stageXpSpent.toMap(),
                xpSlots = xpSlots.toList(),
                advancementHistory = advancementHistory.toList(),
                frozenSkillIncrements = frozenSkillIncrements.toMap(),
                skillAdvancementInProgress = skillAdvancementInProgress,
                skillsForCurrentAdvancement = skillsForCurrentAdvancement.toList(),
                advantageAdvancementInProgress = advantageAdvancementInProgress,
                advantageForCurrentAdvancement = advantageForCurrentAdvancement,
                attributeAdvancementInProgress = attributeAdvancementInProgress,
                attributeStageForCurrentAdvancement = attributeStageForCurrentAdvancement,
                stageNameForCurrentAdvancement = stageNameForCurrentAdvancement,
                attributeStacksBeforeAdvancement = attributeStacksBeforeAdvancement,
                attributeUsedReservation = attributeUsedReservation,
                overrideStageForVantagem = overrideStageForVantagem,
                emProgresso = emProgresso,
                modoProgressaoAtivo = modoProgressaoAtivo,
                mostrandoVantagensProgresso = mostrandoVantagensProgresso,
                mostrandoPericiasProgresso = mostrandoPericiasProgresso,
                mostrandoAtributosProgresso = mostrandoAtributosProgresso,
                mostrandoPoderesProgresso = mostrandoPoderesProgresso,
                frozenAdvantageCount = frozenAdvantageCount,
                stageNameForCurrentAdvancementSnapshot = stageNameForCurrentAdvancement
            ),
            supers = SnapshotSupers(
                superInvestments = superInvestments.toList(),
                superNivelCampanha = superNivelCampanha,
                usarSemPontosDePoder = usarSemPontosDePoder,
                superPontosTotais = superPontosTotais,
                superPontosDisponiveis = superPontosDisponiveis,
                superLimite = superLimite,
                superLimitePorPoder = superLimitePorPoder,
                poderFavoritoId = poderFavoritoId,
                limiteDePoderDaCampanha = limiteDePoderDaCampanha,
                bonusApararFromPower = bonusApararFromPower,
                bonusResFromPower = bonusResFromPower,
                armorFromPower = armorFromPower,
                bonusMovimentacaoFromPower = bonusMovimentacaoFromPower,
                vantagensDePoder = vantagensDePoder.toList(),
                gastosPorPoder = gastosPorPoder.toMap(),
                faseSupersAtiva = faseSupersAtiva,
                comprasPpPorEstagio = comprasPpPorEstagio.toMap(),
                comprasAttrPorEstagio = comprasAttrPorEstagio.toMap(),
                superPontosDisponiveisFlag = superPontosDisponiveis > 0
            )
        )
    }

    fun restoreFromSnapshot(snapshot: PersonagemSnapshot, feedbackMessages: MutableList<String>) {
        val flags = snapshot.flags

        cartaSelvagem = flags.cartaSelvagem
        maisPontosPericias = flags.maisPontosPericias
        modoSupers = flags.modoSupers
        compendioFantasiaAtivo = flags.compendioFantasiaAtivo
        compendioHorrorAtivo = flags.compendioHorrorAtivo
        compendioSciFiAtivo = flags.compendioSciFiAtivo
        compendioScifiMechasCiberneticosAtivo = flags.compendioScifiMechasCiberneticosAtivo
        compendioPathfinderAtivo = flags.compendioPathfinderAtivo
        compendioDeadlandsAtivo = flags.compendioDeadlandsAtivo
        compendioCrystalHeartAtivo = flags.compendioCrystalHeartAtivo
        compendioArteDaGuerraAtivo = flags.compendioArteDaGuerraAtivo
        compendioCidadeSolVaporAtivo = flags.compendioCidadeSolVaporAtivo
        compendioWiseguysAtivo = flags.compendioWiseguysAtivo
        optRegraFama = flags.optRegraFama
        optRegraRiqueza = flags.optRegraRiqueza
        optRegraCosaNostra = flags.optRegraCosaNostra
        modoOficialAtivo = flags.modoOficialAtivo
        modoMonstroAtivo = flags.modoMonstroAtivo
        usarEspecializacoesDePericia = flags.usarEspecializacoesDePericia
        grandesResponsabilidades = flags.grandesResponsabilidades

        idAtual = snapshot.id
        nomePersonagem = snapshot.nome
        anotacoes = snapshot.anotacoes
        appTheme = AppTheme.valueOf(snapshot.appTheme)
        portraitFileName = snapshot.selecoes.retratoFileName
        expandirRetrato = snapshot.selecoes.expandirRetrato
        portraitScaleType = snapshot.selecoes.portraitScaleType
        portraitAlignment = snapshot.selecoes.portraitAlignment

        // Flags adicionais
        nasceUmHeroi = flags.nasceUmHeroi
        soldadoCargaAtivo = flags.soldadoCargaAtivo
        permiteMultiAntecedenteArcano = flags.permiteMultiAntecedenteArcano
        meioElfoAgil = flags.meioElfoAgil
        meioOrcForca = flags.meioOrcForca
        celestialAAMilagresDesabilitado = flags.celestialAAMilagresDesabilitado
        jovemAutoPequeno = flags.jovemAutoPequeno
        jovemMalusPa = flags.jovemMalusPa
        jovemMalusSp = flags.jovemMalusSp
        idosoBonusSp = flags.idosoBonusSp
        obesoBonusSize = flags.obesoBonusSize
        obesoMalusMov = flags.obesoMalusMov
        bonusPoderExtra = flags.bonusPoderExtra
        tipoMonstroSelecionado = flags.tipoMonstroSelecionado
        signoAdgSelecionado = snapshot.selecoes.signoAdgSelecionado
        artistaMarcialJutsuOpcao = snapshot.selecoes.artistaMarcialJutsuOpcao ?: ARTISTA_MARCIAL_JUTSU_D6
        artistaMarcialPotencialFisico = snapshot.selecoes.artistaMarcialPotencialFisico
        artistaMarcialTecnicasSelecionadas.clear()
        artistaMarcialTecnicasSelecionadas.addAll(snapshot.selecoes.artistaMarcialTecnicasSelecionadas)
        buXistaCaminhoSelecionado = snapshot.selecoes.buXistaCaminhoSelecionado
        elementalistaElementoSelecionado = snapshot.selecoes.elementalistaElementoSelecionado
        protagonistaRollTecnicas = snapshot.selecoes.protagonistaRollTecnicas
        protagonistaRollPericia = snapshot.selecoes.protagonistaRollPericia
        protagonistaRollVantagem = snapshot.selecoes.protagonistaRollVantagem
        protagonistaRollQualidade = snapshot.selecoes.protagonistaRollQualidade
        protagonistaRollHabilidade = snapshot.selecoes.protagonistaRollHabilidade
        protagonistaPericiasEscolhidas = snapshot.selecoes.protagonistaPericiasEscolhidas
        protagonistaPericiasPaixao = snapshot.selecoes.protagonistaPericiasPaixao
        vantagensSlotProtagonista.clear()
        vantagensSlotProtagonista.addAll(snapshot.selecoes.protagonistaSlotAdvantageIds)
        gnomoPericiaEscolhida = snapshot.selecoes.gnomoPericiaEscolhida
        dominioClerigoSelecionado = snapshot.selecoes.dominioClerigoSelecionado
        dominioClerigoPathfinderSelecionado = snapshot.selecoes.dominioClerigoPathfinderSelecionado

        dinheiro = snapshot.recursos.dinheiro
        requisicao = snapshot.recursos.requisicao
        famaManual = snapshot.recursos.famaManual
        pontosVantagem = snapshot.recursos.pontosVantagem
        pontosComplicacaoGastos = snapshot.recursos.pontosComplicacaoGastos
        paFromProgress = snapshot.recursos.paFromProgress
        spFromProgress = snapshot.recursos.spFromProgress
        legendaryAttrReservations = snapshot.recursos.legendaryAttrReservations

        cpPaStack.apply { clear(); addAll(snapshot.recursos.cpPaStack) }
        cpSpStack.apply { clear(); repeat(snapshot.recursos.cpSpStack.size) { add(Unit) } }
        cpPvStack.apply { clear(); repeat(snapshot.recursos.cpPvStack.size) { add(Unit) } }
        cpRecursosStack.apply { clear(); repeat(snapshot.recursos.cpRecursosStack.size) { add(Unit) } }
        riquezaModifier = snapshot.recursos.riquezaModifier

        carteiraPathfinder.clear()
        if (snapshot.recursos.carteiraPathfinder.isNotEmpty()) {
            carteiraPathfinder.putAll(snapshot.recursos.carteiraPathfinder)
        } else if (compendioPathfinderAtivo && snapshot.recursos.dinheiro > 0) {
            val pl = snapshot.recursos.dinheiro / 1000
            var rem = snapshot.recursos.dinheiro % 1000
            val po = rem / 100
            rem %= 100
            val pp = rem / 10
            val pc = rem % 10
            carteiraPathfinder["PL"] = pl
            carteiraPathfinder["PO"] = po
            carteiraPathfinder["PP"] = pp
            carteiraPathfinder["PC"] = pc
        }

        aplicarAncestralidade(snapshot.atributos.ancestralidade, feedbackMessages)

        paCostStackPorAtributo.forEach { (attr, stack) ->
            stack.clear()
            stack.addAll(snapshot.atributos.paCostStackPorAtributo[attr].orEmpty())
            val base = racialAttrMinMap[snapshot.atributos.ancestralidade]?.get(attr) ?: 4
            valoresAtributos[attr]!!.intValue = applySuperStepsFrom(base, stack.size)
        }
        pontosAtributo = snapshot.recursos.pontosAtributo

        especializacoesPorPericia.clear()
        ensureIdiomaSlotsFromSnapshot(snapshot.pericias.baseIncsPorPericia.keys)

        // --- Restore Loop ---
        periciasComIdiomas().forEach { per ->
            baseIncsPorPericia[per] = snapshot.pericias.baseIncsPorPericia[per.nome] ?: 0
            compIncsPorPericia[per] = snapshot.pericias.compIncsPorPericia[per.nome] ?: 0

            spCostStackPorPericia.getValue(per).apply {
                clear()
                addAll(snapshot.pericias.spCostStackPorPericia[per.nome].orEmpty())
            }
            compCostStackPorPericia.getValue(per).apply {
                clear()
                addAll(snapshot.pericias.compCostStackPorPericia[per.nome].orEmpty())
            }

            snapshot.pericias.especializacoesPorPericia[per.nome]?.let { dto ->
                especializacoesPorPericia[per.nome] = dto
            }
        }

        // Restore Jutsu
        ensureJutsuSlotsFromSnapshot(snapshot.pericias.baseIncsPorPericia.keys)

        // Second pass for Jutsu Extras that were just added
        periciasComIdiomas().forEach { per ->
            if (!baseIncsPorPericia.containsKey(per)) {
                baseIncsPorPericia[per] = snapshot.pericias.baseIncsPorPericia[per.nome] ?: 0
                compIncsPorPericia[per] = snapshot.pericias.compIncsPorPericia[per.nome] ?: 0
                ensurePericiaEntry(per)

                spCostStackPorPericia.getValue(per).apply {
                    clear()
                    addAll(snapshot.pericias.spCostStackPorPericia[per.nome].orEmpty())
                }
                compCostStackPorPericia.getValue(per).apply {
                    clear()
                    addAll(snapshot.pericias.compCostStackPorPericia[per.nome].orEmpty())
                }
            }
        }

        notasPericia.clear()
        notasPericia.putAll(snapshot.pericias.notasPericia ?: emptyMap())
        syncIdiomaSlots()
        syncJutsuSlots()


        vantagensSelecionadas.clear()
        snapshot.selecoes.vantagens.forEach { snap ->
            listaVantagens.firstOrNull { it.id == snap.id }?.let { vant ->
                val newVant = vant.copy()
                newVant.choice = snap.choice
                vantagensSelecionadas.add(newVant)
            }
        }

        vantagensAutomaticas.apply { clear(); addAll(snapshot.selecoes.vantagensAutomaticas) }
        vantagensRaciais.apply { clear(); addAll(snapshot.selecoes.vantagensRaciais) }
        desvantagensAutomaticas.apply { clear(); addAll(snapshot.selecoes.desvantagensAutomaticas) }
        desvantagensRaciais.apply { clear(); addAll(snapshot.selecoes.desvantagensRaciais) }

        complicacoesSelecionadas.clear()
        snapshot.selecoes.complicacoesSelecionadas.forEach { compSnap ->
            listaComplicacoes.firstOrNull { it.id == compSnap.id }?.let { comp ->
                complicacoesSelecionadas[comp] = compSnap.nivel
            }
        }
        reservasComplicacaoMaior.clear()
        reservasComplicacaoMaior.putAll(snapshot.selecoes.reservasComplicacaoMaior)

        // PROMPT 3: Restore Transtornos
        transtornos.clear()
        snapshot.selecoes.transtornos?.forEach { compSnap ->
            listaComplicacoes.firstOrNull { it.id == compSnap.id }?.let { comp ->
                transtornos.add(comp)
            }
        }

        equipamentosComprados.apply { clear(); addAll(snapshot.selecoes.equipamentosComprados) }

        tropoSelecionado = snapshot.selecoes.tropoSelecionadoId?.let { id ->
            listaTropos.firstOrNull { it.id == id }
        }
        vantagensAutomaticasDoTropo.apply {
            clear()
            addAll(snapshot.selecoes.vantagensTropoAutomaticas)
        }
        vantagensAutomaticasDoProtagonista.clear()
        if (tropoSelecionado?.id == "tropo_protagonista") {
            atualizarProtagonistaAutoVantagens()
        }
        protagonistaBonusPv = tropoSelecionado?.id == "tropo_protagonista" &&
            protagonistaRollHabilidade == 4
        syncArtistaMarcialPotencialFisico()

        poderSlotsPorArcano.clear()
        snapshot.selecoes.poderSlotsPorArcano.forEach { (key, slots) ->
            poderSlotsPorArcano[key] = mutableStateListOf<String?>().apply { addAll(slots) }
        }
        novosPoderesStacksPorArcano.clear()
        snapshot.selecoes.novosPoderesStacksPorArcano.forEach { (key, pilhas) ->
            novosPoderesStacksPorArcano[key] = pilhas.map { it.toMutableList() }.toMutableList()
        }
        poderesSelecionados.apply { clear(); addAll(snapshot.selecoes.poderesSelecionados) }
        manifestacoesPoderes.apply {
            clear()
            putAll(snapshot.selecoes.manifestacoesPoderes)
        }
        arcanoEmCompraViaXpKey = snapshot.selecoes.arcanoEmCompraViaXpKey
        arcanoSnapshotAntesDaCompra = snapshot.selecoes.arcanoSnapshotAntesDaCompra

        progresso = snapshot.progresso.progresso
        progressosDisponiveis = snapshot.progresso.progressosDisponiveis
        stageXpSpent.keys.forEach { stage -> stageXpSpent[stage] = snapshot.progresso.stageXpSpent[stage] ?: 0 }
        xpSlots.apply { clear(); addAll(snapshot.progresso.xpSlots) }
        advancementHistory.apply { clear(); addAll(snapshot.progresso.advancementHistory) }

        frozenSkillIncrements.clear()
        frozenSkillIncrements.putAll(snapshot.progresso.frozenSkillIncrements)

        skillAdvancementInProgress = snapshot.progresso.skillAdvancementInProgress
        skillsForCurrentAdvancement.apply { clear(); addAll(snapshot.progresso.skillsForCurrentAdvancement) }
        advantageAdvancementInProgress = snapshot.progresso.advantageAdvancementInProgress
        advantageForCurrentAdvancement = snapshot.progresso.advantageForCurrentAdvancement
        attributeAdvancementInProgress = snapshot.progresso.attributeAdvancementInProgress
        attributeStageForCurrentAdvancement = snapshot.progresso.attributeStageForCurrentAdvancement
        stageNameForCurrentAdvancement = snapshot.progresso.stageNameForCurrentAdvancement
        attributeStacksBeforeAdvancement = snapshot.progresso.attributeStacksBeforeAdvancement
        attributeUsedReservation = snapshot.progresso.attributeUsedReservation
        overrideStageForVantagem = snapshot.progresso.overrideStageForVantagem
        emProgresso = snapshot.progresso.emProgresso
        modoProgressaoAtivo = snapshot.progresso.modoProgressaoAtivo
        mostrandoVantagensProgresso = snapshot.progresso.mostrandoVantagensProgresso
        mostrandoPericiasProgresso = snapshot.progresso.mostrandoPericiasProgresso
        mostrandoAtributosProgresso = snapshot.progresso.mostrandoAtributosProgresso
        mostrandoPoderesProgresso = snapshot.progresso.mostrandoPoderesProgresso
        frozenAdvantageCount = snapshot.progresso.frozenAdvantageCount

        superInvestments.apply { clear(); addAll(snapshot.supers.superInvestments) }
        superNivelCampanha = snapshot.supers.superNivelCampanha
        usarSemPontosDePoder = snapshot.supers.usarSemPontosDePoder
        superPontosTotais = snapshot.supers.superPontosTotais
        superPontosDisponiveis = snapshot.supers.superPontosDisponiveis
        superLimite = snapshot.supers.superLimite
        superLimitePorPoder = snapshot.supers.superLimitePorPoder
        poderFavoritoId = snapshot.supers.poderFavoritoId
        limiteDePoderDaCampanha = snapshot.supers.limiteDePoderDaCampanha
        bonusApararFromPower = snapshot.supers.bonusApararFromPower
        bonusResFromPower = snapshot.supers.bonusResFromPower
        armorFromPower = snapshot.supers.armorFromPower
        bonusMovimentacaoFromPower = snapshot.supers.bonusMovimentacaoFromPower
        vantagensDePoder.apply { clear(); addAll(snapshot.supers.vantagensDePoder) }
        gastosPorPoder.apply { clear(); putAll(snapshot.supers.gastosPorPoder) }
        faseSupersAtiva = snapshot.supers.faseSupersAtiva

        comprasPpPorEstagio.keys.forEach { comprasPpPorEstagio[it] = snapshot.supers.comprasPpPorEstagio[it] ?: 0 }
        comprasAttrPorEstagio.keys.forEach { comprasAttrPorEstagio[it] = snapshot.supers.comprasAttrPorEstagio[it] ?: 0 }

        snapshot.selecoes.coracaoCrystalId?.let { cid ->
            coracaoCrystalSelecionado = listaCoracoesCrystal.find { it.id == cid }
        }

        trimAttributeStacks(feedbackMessages)
        rebuildAllPericiaStacks(feedbackMessages)
        updateEmProgressoFlag()
        syncPoderesSelecionadosFromSlots()
    }
}
