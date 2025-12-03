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
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

class CriadorState {
    var appTheme by mutableStateOf(AppTheme.DEFAULT)
    var showHelpMessages by mutableStateOf(false)
    var modoSupers by mutableStateOf(false)
    var modoSuperComplicacoes by mutableStateOf(false)
    var modoSuperequip by mutableStateOf(false)
    var grandesResponsabilidades by mutableStateOf(false)
    companion object { const val BASE_SP_POOL = 15 }
    var maisPontosPericias by mutableStateOf(true)
    var cartaSelvagem       by mutableStateOf(true)
    var dinheiro by mutableIntStateOf(500)
    val poderesSelecionados = mutableStateListOf<String>()
    val equipamentosComprados = mutableStateListOf<EquipamentoItem>()
    var heroisSemArmadura by mutableStateOf(false)
    private val _maxedTraits = mutableStateListOf<String>()
    val maxedTraits: List<String> get() = _maxedTraits
    var idAtual by mutableStateOf<String?>(null)

    var anotacoes by mutableStateOf("")

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

    var bonusPararFromPower by mutableIntStateOf(0)
    var bonusResFromPower  by mutableIntStateOf(0)
    var armorFromPower     by mutableIntStateOf(0)

    var bonusMovimentacaoFromPower by mutableIntStateOf(0)

    val vantagensDePoder   = mutableStateSetOf<String>()
    val gastosPorPoder     = mutableStateMapOf<String, Int>()
    var naturalArmorFromRace by mutableIntStateOf(0)

    fun valorMovimentacao(): Int {
        val base = 6

        val racialPenalty =
            listaAncestralidadesJson
                .firstOrNull { it.nome.keyify() == ancestralidade }
                ?.desvantagens
                ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
                .takeIf { it == true }
                ?.let { 1 }
                ?: 0

        val idosoPenalty =
            complicacoesSelecionadas
                .filterKeys { it.id.keyify() == "IDOSO" }
                .isNotEmpty()
                .takeIf { it }
                ?.let { 1 }
                ?: 0

        val lentoPenalty = complicacoesSelecionadas
            .entries
            .firstOrNull { it.key.id.keyify() == "LENTO" }
            ?.let { (_, grau) ->
                when (grau) {
                    "Menor" -> 1
                    "Maior" -> 2
                    else    -> 0
                }
            }
            ?: 0

        val obesoPenalty =
            complicacoesSelecionadas
                .filterKeys { it.id.keyify() == "OBESO" }
                .isNotEmpty()
                .takeIf { it }
                ?.let { 1 }
                ?: 0

        val ligeiroBonus =
            if (vantagensSelecionadas.any { it.nome.keyify() == "LIGEIRO" })
                2
            else
                0

        return (base
                - racialPenalty
                - idosoPenalty
                - lentoPenalty
                - obesoPenalty
                + ligeiroBonus
                + bonusMovimentacaoFromPower)
            .coerceAtLeast(0)
    }

    fun valorAparar(): Int {
        val perLutar = listaPericias.firstOrNull { it.nome.equals("Lutar", ignoreCase = true) }
        val lutarRaw = perLutar?.let { rawTotalComSupers(it) } ?: 0
        val base     = 2 + (lutarRaw / 2)

        val bloquearBonus =
            if (vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR" }) 1 else 0
        val bloquearAprimoradoBonus =
            if (vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR APRIMORADO" }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + bonusPararFromPower
    }

    fun valorResistenciaBase(): Int {
        val vigorRaw = valoresAtributos["VIGOR"]?.intValue ?: 4
        val base     = 2 + (vigorRaw / 2)

        val bonusPos = if (vantagensAutomaticas.any { it.keyify() == "RESISTENCIA" }) 1 else 0
        val bonusNeg = if (desvantagensAutomaticas.any { it.keyify() == "FRAGIL" }) -1 else 0

        // Bônus de “Brigão / Pugilista” continua igual
        val brigaoBonus = vantagensSelecionadas
            .count { it.nome.keyify() in listOf("BRIGAO", "PUGILISTA") }

        // Em vez de recalcular tamanho aqui, usamos a função centralizada:
        val sizeRaw = valorTamanho()   // já inclui racial, OBESO, PEQUENO, MUSCULOSO, com clamp -1..+3

        return (base + bonusPos + bonusNeg + brigaoBonus + sizeRaw)
            .coerceAtLeast(0)
    }

    fun valorResistenciaFinal(): Int {
        return valorResistenciaBase() + bonusResFromPower
    }

    fun valorArmaduraEfetiva(): Int {
        val armorFromEquipment = armadura
        val melhorExterna = kotlin.math.max(armorFromPower, armorFromEquipment)
        return (melhorExterna + naturalArmorFromRace).coerceAtLeast(0)
    }

    fun valorTamanho(): Int {
        val desc = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == ancestralidade }
            ?.desvantagens
            ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }

        val racialSize = desc
            ?.substringAfter("TAMANHO")
            ?.trim()
            ?.toIntOrNull()
            ?: 0
        val obesoBonus =
            if (complicacoesSelecionadas.keys.any { it.id.keyify() == "OBESO" })
                1
            else
                0
        val pequenoPenalty =
            if (complicacoesSelecionadas.keys.any { it.id.keyify() == "PEQUENO" })
                -1
            else
                0
        val musculosoBonus =
            if (vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" })
                1
            else
                0
        val raw = racialSize + obesoBonus + pequenoPenalty + musculosoBonus
        return raw.coerceIn(-1, 3)
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

    fun updateBonusPararFromPower(value: Int) {
        bonusPararFromPower = value.coerceAtLeast(0)
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
            .filter { it.periciaKey.equals(per.nome, ignoreCase = true) }
            .sumOf { it.steps }
        return applySuperStepsFrom(base, incs)
    }

    var regraMultiplosIdiomas by mutableStateOf(false)

    var pvFromXpOutstanding by mutableIntStateOf(0)
    var overrideStageForVantagem by mutableStateOf<String?>(null)
    var openVantagensAfterGrant by mutableStateOf(false)
    var superPoderEmFoco by mutableStateOf<String?>(null)

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
        //This is a hack to add 2 skill points
        cpSpStack.add(Unit)
        cpSpStack.add(Unit)
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

    fun removerPontosDePoder(v: Vantagem) {
        if (!vantagensSelecionadas.remove(v)) return

        val totalAntes = comprasPpPorEstagio.values.sum()
        if (totalAntes == 0) return

        val estagio = estagioAtual().nome
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

        listaPericias.forEach { per ->
            val current    = rawTotal(per)
            val maxAllowed = periciaCapRaw(per)
            if (current == maxAllowed) {
                _maxedTraits.add(per.nome.keyify())
            }
        }
    }

    fun applyVantagemDinheiro(v: Vantagem) {
        when (v.nome.trim().uppercase()) {
            "RICO"          -> dinheiro += 1000
            "PODRE DE RICO" -> dinheiro += 1500
        }
    }

    fun removeVantagemDinheiro(vant: Vantagem) {
        val key = vant.nome.trim().uppercase()
        val amount = when (key) {
            "RICO"          -> 1000
            "PODRE DE RICO" -> 1500
            else            -> 0
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
                    val chaveNorm = nomeRaw.uppercase().semAcentos().trim()
                    listaPericias
                        .firstOrNull { it.nome.uppercase().semAcentos() == chaveNorm }
                        ?.let { per -> per to min }
                }

            val opcionais = vant.requisitos.periciaMinOpcional   // se null, vira Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    val chaveNorm = nomeRaw.uppercase().semAcentos().trim()
                    listaPericias
                        .firstOrNull { it.nome.uppercase().semAcentos() == chaveNorm }
                        ?.let { per -> per to min }
                }

            val fav = run {
                val choiceSnapshot = vant.choice
                if (
                    vant.nome.trim().equals("Arma Predileta", ignoreCase = true)
                    && choiceSnapshot != null
                ) {
                    val key = choiceSnapshot.uppercase().semAcentos().trim()
                    listaPericias
                        .firstOrNull { it.nome.uppercase().semAcentos() == key }
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

    fun atributoBaseParaPericia(per: Pericia): String {
        return if (per.nome.equals("Atletismo", ignoreCase = true)
            && vantagensSelecionadas.any { it.nome.keyify() == "BRUTAMONTES" }
        ) {
            "FORCA"
        } else {
            per.atributo
        }
    }

    private val incompatibilidades: Map<String, Set<String>> = mapOf(
        "LENTO"   to setOf("LIGEIRO"),
        "LIGEIRO" to setOf("LENTO"),
        "OBESO"      to setOf("MUSCULOSO"),
        "MUSCULOSO"  to setOf("OBESO"),
        "POBREZA"        to setOf("RICO", "PODRE DE RICO"),
        "RICO"           to setOf("POBREZA"),
        "PODRE DE RICO"  to setOf("POBREZA")
    )

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

        // Atualiza a lista plana
        poderesSelecionados.apply {
            clear()
            addAll(slots.filterNotNull())
        }
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
    val cpPvStack       = mutableStateListOf<Unit>()
    val cpRecursosStack = mutableStateListOf<Unit>()

    private val totalSpPool: Int
        get() {
            val base = if (maisPontosPericias) BASE_SP_POOL else BASE_SP_POOL - 3
            return (base + cpSpStack.size + idosoBonusSp - jovemMalusSp)
                .coerceAtLeast(0)
        }

    val pontosPericia by derivedStateOf {
        val used = spCostStackPorPericia.values.sumOf { it.sum() } +
                compCostStackPorPericia.values.sumOf { it.sum() }
        totalSpPool - used
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

    val vantagensAutomaticas = mutableStateListOf<String>()
    val desvantagensRaciais = mutableStateListOf<String>()

    var pontosVantagem by mutableIntStateOf(0)

    val desvantagensAutomaticas = mutableStateListOf<String>()

    var frozenAdvantageCount by mutableIntStateOf(0)

    var pontosAtributo by mutableIntStateOf(5)

    var armadura by mutableIntStateOf(0)

    var nasceUmHeroi by mutableStateOf(false)

    val valoresAtributos = listaAtributos.associateWith { mutableIntStateOf(4) }

    val complicacoesSelecionadas: SnapshotStateMap<Complicacao, String?> = mutableStateMapOf()

    val pontosComplicacao: Int
        get() {
            val autoKeys = desvantagensAutomaticas
                .map { it.substringBefore("(").trim().keyify() }
                .toSet()

            var total = 0
            var temMaior = false

            for ((comp, tipo) in complicacoesSelecionadas) {
                if (comp.id.keyify() in autoKeys) continue
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

    // guarda o nome da vantagem que está em foco (usada ao voltar da tela de detalhes)
    var vantagemEmFoco by mutableStateOf<String?>(null)

    fun podeSelecionar(v: Vantagem): Boolean {
        val key = v.nome.keyify()

        // 1) Regra especial: O MELHOR QUE HÁ
        if (key == "o_melhor_que_ha") {
            if (emProgresso) return false
            if (superInvestments.isEmpty()) return false
        }

        // 2) Pontos de Poder por estágio
        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalFeitas = comprasPpPorEstagio.values.sum()
            val maxPermitidas = maxComprasPpAteAgora()
            if (totalFeitas >= maxPermitidas) return false
        }

        // 3) Antecedente Arcano e multi-arcano
        if (key.startsWith("antecedente arcano")) {
            if (!permiteMultiAntecedenteArcano) {
                val anyArcano = vantagensSelecionadas.any { it.nome.keyify().startsWith("antecedente arcano") }
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
                val anyMaxPer = listaPericias.any { p ->
                    rawTotal(p) == periciaCapRaw(p)
                }
                return anyMaxAttr || anyMaxPer
            }

            val choiceKey = choiceSeguro.keyify()
            return if (listaAtributos.contains(choiceKey)) {
                valoresAtributos[choiceKey]!!.intValue == atributoMaxRaw(choiceKey)
            } else {
                val per = listaPericias.first { it.nome.keyify() == choiceKey }
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
        // >>> AQUI estava o problema: cap geral por maxSelections, ignorando limite_compra "infinito"
        else if (v.limiteCompra != "infinito" && v.maxSelections > 0) {
            val ja = vantagensSelecionadas.count { it.id == v.id }
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
                valoresAtributos[chaveNorm]?.intValue?.let { it < min } != false
            }) return false

        // 11) Perícias mínimas obrigatórias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atendeUma = periciaMinMap.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull {
                    it.nome.equals(perNome, ignoreCase = true)
                }
                per != null && rawTotal(per) >= minRaw
            }
            if (!atendeUma) return false
        } else {
            if (periciaMinMap.any { (perNome, minRaw) ->
                    val per = listaPericias.firstOrNull {
                        it.nome.equals(perNome, ignoreCase = true)
                    } ?: return@any false
                    rawTotal(per) < minRaw
                }) {
                return false
            }
        }

        // 12) Perícias mínimas opcionais (qualquer uma)
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            val atendeUmaOpc = periciaMinOpcMap.any { (perNome, minRaw) ->
                val per = listaPericias.firstOrNull {
                    it.nome.equals(perNome, ignoreCase = true)
                }
                per != null && rawTotal(per) >= minRaw
            }
            if (!atendeUmaOpc) return false
        }

        // 13) Exige Carta Selvagem?
        if (v.requisitos.exigeCS && !cartaSelvagem) return false

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
        val poolSize = BASE_SP_POOL + cpSpStack.size
        var cumulativeCost = 0

        listaPericias.forEach { per ->

            val cap = periciaCapRaw(per)
            val target = desiredRaw.getValue(per).coerceAtMost(cap)

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var curr = periciaStartRaw(ancestralidade, per)

            while (curr < target && cumulativeCost < poolSize) {
                val next = when {
                    curr == 0 -> 4
                    curr < 12 -> curr + 2
                    else      -> curr + 1
                }

                val attrKey = atributoBaseParaPericia(per)
                val cost    = if (next <= valoresAtributos[attrKey]!!.intValue) 1 else 2
                if (cumulativeCost + cost > poolSize) break

                stack.add(cost)
                baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                cumulativeCost += cost
                curr = next
            }
        }
    }

    fun decreasePericia(per: Pericia) {
        val spStack = spCostStackPorPericia.getValue(per)
        val idx = spStack.indexOfLast { it > 0 }
        if (idx >= 0) {
            spStack.removeAt(idx)
            baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) - 1

        }
    }

    fun atributoMinRaw(a: String): Int =
        racialAttrMinMap[ancestralidade]?.get(a) ?: 4

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

        return baseCap + (profCount + espCount) * 2
    }

    fun periciaCapRaw(per: Pericia): Int {
        val startRaw = periciaStartRaw(ancestralidade, per)

        val baseCap = if (startRaw >= 6) 13 else 12

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
        val wasHumano = (prevAnc == "HUMANOS")
        val vaiSerHumano = (anc == "HUMANOS")

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
            val candidatos = vantagensSelecionadas.filter { v ->
                !isRacialFree(v) &&
                        !isUsedAsPrereq(v) &&
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

        listaPericias.associateWith { rawTotal(it) }

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
        celestialAAMilagresDesabilitado = (anc == "CELESTIAIS" && modoSupers)
        if (anc != "MEIO-ELFOS") {
            meioElfoAgil = false
        }

        // --- Vantagens / desvantagens raciais ---

        // Remove APENAS as vantagens raciais automáticas da raça anterior
        if (prevFreeKeys.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.nome.keyify() in prevFreeKeys }
        }

        desvantagensAutomaticas.clear()
        vantagensAutomaticas.clear()
        desvantagensRaciais.clear()

        listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == anc }
            ?.let { rm ->
                desvantagensAutomaticas.addAll(rm.desvantagens)
                vantagensAutomaticas.addAll(rm.vantagensGratis)
            }

        // NÃO tem mais "raças levam pra lista completa":
        // Removemos o bloco que apagava tudo que não fosse vantagem automática.

        when (anc) {
            "SAURIOS" -> {
                listaVantagens.firstOrNull { it.nome.equals("Sentidos Aguçados", ignoreCase = true) }
                    ?.let { vantagensSelecionadas.add(it) }
                listaVantagens.firstOrNull { it.nome.equals("Prontidão", ignoreCase = true) }
                    ?.let { vantagensSelecionadas.add(it) }
                vantagensAutomaticas.add("Prontidão")
                armadura = 2
            }
            "PEQUENINOS" -> {
                listaVantagens.firstOrNull { it.nome.equals("Sorte", ignoreCase = true) }
                    ?.let { vantagensSelecionadas.add(it) }
                vantagensAutomaticas.add("Sorte")
                desvantagensRaciais.add("Tamanho -1")
                desvantagensRaciais.add("Movimentação Reduzida")
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
            else -> {
                armadura = 0
            }
        }

        // IMPORTANTE:
        // Removido: pontosVantagem = if (vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0
        // Agora o pool de PV não é mais zerado/redefinido ao trocar de raça.
        // Ele só é ajustado pelo bloco de HUMANO acima.

        // --- Complicações raciais automáticas ---

        val oldAutoKeys = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == prevAnc }
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
            .forEach { comp ->
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

    private fun calcularPontosAtributoRestantes(): Int {
        val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
        var usados = 0

        for (nome in listaAtributos) {
            val atual = valoresAtributos[nome]!!.intValue
            val base  = mods[nome] ?: 4

            var cur = base
            while (cur < atual) {
                cur += if (cur < 12) 2 else 1
                usados += 1
            }
        }

        return (5 + cpPaStack.size + paFromProgress - jovemMalusPa) - usados
    }

    fun recalcularPontosAtributo(feedbackMessages: MutableList<String> = mutableListOf()) {

        pontosAtributo = calcularPontosAtributoRestantes()

        trimAttributeStacks(feedbackMessages)

        rebuildAllPericiaStacks(feedbackMessages)
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

            val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
            val base = mods[nomeAttr] ?: 4

            val atual = valoresAtributos[nomeAttr]!!.intValue

            val novo = if (atual > 12) atual - 1 else atual - 2
            valoresAtributos[nomeAttr]!!.intValue = novo.coerceAtLeast(base)

            feedbackMessages?.add("Atributo $nomeAttr reduzido para d${novo.coerceAtLeast(base)} para compensar pontos.")

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
    val criacaoBasicaCongelada: Boolean
        get() = emProgresso
    var modoProgressaoAtivo by mutableStateOf(false)
    var mostrandoVantagensProgresso by mutableStateOf(false)
    var mostrandoPericiasProgresso by mutableStateOf(false)
    var mostrandoAtributosProgresso by mutableStateOf(false)
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

    fun increasePericiaFromAdvancement(per: Pericia, cost: Int) {
        if (skillAdvancementInProgress) {
            skillsForCurrentAdvancement.add(per.nome)
        }
        baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
        spCostStackPorPericia.getValue(per).add(cost)
    }

    fun creationComplete(): Boolean {
        // "Ficha básica completa": todos os pontos iniciais foram distribuídos.
        // Em modo supers, NÃO amarramos isso aos pontos de super – eles são uma fase à parte.
        return pontosAtributo == 0 &&
                pontosPericia == 0 &&
                pontosVantagem == 0 &&
                (pontosComplicacao - pontosComplicacaoGastos).coerceAtLeast(0) == 0
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

    fun rebuildAllPericiaStacks(feedbackMessages: MutableList<String> = mutableListOf()) {
        var cumulativeCost = 0
        val pool = totalSpPool

        listaPericias.forEach { per ->

            val desiredRaw = rawTotal(per)
            val cap       = periciaCapRaw(per)
            val minRaw    = if (per.basica) 4 else 0

            var target = desiredRaw.coerceIn(minRaw, cap)

            fun costFor(tgt: Int): Int {
                var curr = periciaStartRaw(ancestralidade, per)
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
                    sum += stepCost
                    curr = next
                }
                return sum
            }

            var cost = costFor(target)

            if (cost > 0 && cumulativeCost + cost > pool) {
                feedbackMessages?.add("Perícia ${per.nome} reduzida para d$target para compensar pontos.")
            }
            while (cumulativeCost + cost > pool) {
                target = (target - 2).coerceAtLeast(minRaw)
                cost   = costFor(target)
            }

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var currRaw = periciaStartRaw(ancestralidade, per)
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
                stack.add(stepCost)
                baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                currRaw = next
            }

            cumulativeCost += cost
        }
    }
}
