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
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

class CriadorState {
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

    val superPoderesComprados = mutableStateListOf<PurchasedPower>()
    var superNivelCampanha by mutableStateOf<Int?>(null)
    var usarSemPontosDePoder by mutableStateOf(false)

    var superPontosTotais by mutableIntStateOf(0)
    var superPontosDisponiveis by mutableIntStateOf(0)
    var superLimite by mutableIntStateOf(0)
    var superLimitePorPoder by mutableIntStateOf(0)
    var idPoderFavorecido by mutableStateOf<String?>(null)
    val limitePorPoderPadrao: Int
        get() = kotlin.math.floor(superPontosTotais / 3.0).toInt()
    val limiteFavorecido: Int
        get() = kotlin.math.ceil(superPontosTotais / 2.0).toInt()
    var limiteDePoderDaCampanha by mutableIntStateOf(Int.MAX_VALUE)

    var faseSupersAtiva by mutableStateOf(false)

    val superAtributoIncs = mutableStateMapOf<String, Int>()
    val superPericiaIncs = mutableStateMapOf<String, Int>()
    var bonusPararFromPower by mutableIntStateOf(0)
    var bonusResFromPower  by mutableIntStateOf(0)
    var armorFromPower     by mutableIntStateOf(0)

    var bonusMovimentacaoFromPower by mutableIntStateOf(0)

    val vantagensDePoder   = mutableStateSetOf<String>()
    val gastosPorPoder     = mutableStateMapOf<String, Int>()
    var naturalArmorFromRace by mutableIntStateOf(0)

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

    fun syncFromCPRefund(pa: Boolean = false, sp: Boolean = false) {
        if (pa) recalcularPontosAtributo()
        if (sp) rebuildAllPericiaStacks()
    }

    val cpPaStack       = mutableStateListOf<String>()  // você já trocou pra add("PB")
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
        val caps = listaDeEstagios.mapIndexed { idx, st ->
            val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
            if (idx < listaDeEstagios.lastIndex)
                st.maxProgress - prevMax
            else
                (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
        }
        val firstOpen = caps.indexOfFirst { cap ->
            val nome = listaDeEstagios[caps.indexOf(cap)].nome
            (stageXpSpent[nome] ?: 0) < cap
        }
        return if (firstOpen >= 0) firstOpen else listaDeEstagios.lastIndex
    }

    var ancestralidade by mutableStateOf("HUMANOS")

    val vantagensAutomaticas = mutableStateListOf<String>()

    var pontosVantagem by mutableIntStateOf(0)

    val desvantagensAutomaticas = mutableStateListOf<String>()

    var frozenAdvCount by mutableIntStateOf(0)

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

        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalFeitas = comprasPpPorEstagio.values.sum()
            val maxPermitidas = maxComprasPpAteAgora()
            if (totalFeitas >= maxPermitidas) return false
        }

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

        val ignorarEstagioPorNasce =
            (nasceUmHeroi && !emProgresso && pvFromXpOutstanding == 0)
        if (!ignorarEstagioPorNasce) {

            listaDeEstagios
                .firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
                ?.let { estReqObj ->
                    if (effectiveProgressoParaVantagens() < estReqObj.minProgress) return false
                }
        }

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

        if (v.nome.contains("Pontos de Poder", ignoreCase = true)) {
            val totalCompras = comprasPpPorEstagio.values.sum()
            val limite = maxComprasPpAteAgora()
            if (totalCompras >= limite) return false
        }

        else if (v.maxSelections > 0) {
            val ja = vantagensSelecionadas.count { it.id == v.id }
            if (ja >= v.maxSelections) return false
        }

        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = vantagensSelecionadas.any {
                it.id == v.id && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }

        nivelParaEstagio[v.requisitos.estagio]?.let { estReqObj2 ->
            if (estReqObj2.minProgress > effectiveProgressoParaVantagens()) return false
        }

        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                valoresAtributos[chaveNorm]?.intValue?.let { it < min } != false
            }) return false

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

        if (v.requisitos.exigeCS && !cartaSelvagem) return false

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

        val extras = ((minRaw - 4).coerceAtLeast(0) / 2)
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

    fun aplicarAncestralidade(anc: String) {
        val prevAnc = ancestralidade

        if (prevAnc == "HUMANOS" && anc != "HUMANOS") {
            if (vantagensSelecionadas.isNotEmpty()) {
                vantagensSelecionadas.removeAt(vantagensSelecionadas.lastIndex)
            } else {
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            }
        } else if (prevAnc != "HUMANOS" && anc == "HUMANOS") {
            pontosVantagem += 1
        }

        val desiredRaw = listaPericias.associateWith { rawTotal(it) }

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
                repeat(stack.size - appliedSteps) {
                    stack.removeAt(stack.lastIndex)
                }
            }

            st.intValue = raw
        }

        ancestralidade = anc

        val prevFree = vantagensAutomaticas.toSet() +
                when (prevAnc) {
                    "SAURIOS"    -> setOf("Sentidos Aguçados", "Prontidão")
                    "PEQUENINOS" -> setOf("Sorte")
                    else         -> emptySet()
                }

        vantagensSelecionadas.removeAll { it.nome in prevFree }
        desvantagensAutomaticas.clear()
        vantagensAutomaticas.clear()

        listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == anc }
            ?.let { rm ->
                desvantagensAutomaticas.addAll(rm.desvantagens)
                vantagensAutomaticas.addAll(rm.vantagensGratis)
            }

        val keepFreeKeys = vantagensAutomaticas.map { it.keyify() }.toSet()
        vantagensSelecionadas.removeAll { sel ->
            sel.nome.keyify() !in keepFreeKeys
        }

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
                armadura = 0
            }
            "CELESTIAIS" -> {
                listaVantagens
                    .firstOrNull { it.nome.equals("ANTECEDENTE ARCANO MILAGRES", ignoreCase = true) }
                    ?.let {
                        vantagensSelecionadas.add(it)
                    }
                vantagensAutomaticas.add("ANTECEDENTE ARCANO MILAGRES")
                armadura = 0
            }
            else -> {
                armadura = 0
            }
        }

        pontosVantagem = if (vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0

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

                val grau = when (comp.severity.lowercase()) {
                    "both"  -> if (hasMenor) "Menor" else "Maior"
                    "menor" -> "Menor"
                    "maior" -> "Maior"
                    else    -> "Menor"
                }

                complicacoesSelecionadas[comp] = grau
            }

        if (modoSupers) {
            listaVantagens.firstOrNull { it.id == "superpoderes" }?.let { sp ->
                if (vantagensSelecionadas.none { it.id == "superpoderes" }) {
                    vantagensSelecionadas.add(sp)
                }
            }
        }

        rebuildPericias(desiredRaw)

        recalcularPontosAtributo()
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
        val totalSpent = stageXpSpent.values.sum()
        progressosDisponiveis = (progresso - totalSpent).coerceAtLeast(0)
    }

    fun checkFreeze() {
        val idx = currentProgressStageIndex()
        val est = listaDeEstagios[idx]
        val cap = dynamicStageCaps[idx]
        val spent = stageXpSpent.getValue(est.nome)
        if (spent == cap) {
            frozenAdvCount = vantagensSelecionadas.size
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

        return (5 + cpPaStack.size - jovemMalusPa) - usados
    }

    fun recalcularPontosAtributo() {

        pontosAtributo = calcularPontosAtributoRestantes()

        trimAttributeStacks()

        rebuildAllPericiaStacks()
    }

    private fun trimAttributeStacks() {

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

    fun creationComplete(): Boolean =
        pontosAtributo == 0 &&
                pontosPericia == 0 &&
                pontosVantagem == 0 &&
                (pontosComplicacao - pontosComplicacaoGastos).coerceAtLeast(0) == 0


    val stageXpSpent: SnapshotStateMap<String, Int> = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    var progressosDisponiveis by mutableIntStateOf(0)

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

    fun rebuildAllPericiaStacks() {
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
