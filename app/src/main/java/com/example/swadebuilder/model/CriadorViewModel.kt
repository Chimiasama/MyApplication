// CriadorViewModel.kt
package com.example.swadebuilder.model

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.PurchasedPower
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.periciaStartRaw
import com.example.swadebuilder.util.keyify
import java.io.File
import java.io.FileOutputStream
import com.example.swadebuilder.SUPER_PODERES_RESTRITIVOS
import com.example.swadebuilder.MIN_RAW_RESTRITIVO
import com.example.swadebuilder.listaDeEstagios

// ---- EFEITOS POSSÍVEIS DE INVESTIMENTO DE SUPER ----
sealed class PowerEffect {
    data class SuperAtributo(val attrKey: String, val steps: Int) : PowerEffect()
    data class SuperPericia(val periciaId: String, val steps: Int) : PowerEffect()
    data class BonusArmadura(val value: Int) : PowerEffect()                 // armorFromPower += value
    data class BonusResistencia(val value: Int) : PowerEffect()              // bonusResFromPower += value
    data class BonusAparar(val value: Int) : PowerEffect()

    data class BonusMovimentacao(val value: Int) : PowerEffect()
    // bonusPararFromPower += value
    data class SuperVantagem(val vantagemId: String) : PowerEffect()
}

// ---- OBJETOS DE RETORNO ----
data class InvestCheck(val ok: Boolean, val motivoBloqueio: String? = null)
data class InvestResult(val ok: Boolean, val mensagem: String)

/**
 * ViewModel que gerencia o estado de criação de personagem.
 */
class CriadorViewModel : ViewModel() {

    val state = CriadorState()

    // === NOVO: toggle global (por enquanto via MainActivity) ===
    var multiplosAAHabilitados: Boolean = false
        private set

    fun setMultiplosAAHabilitados(enabled: Boolean) {
        multiplosAAHabilitados = enabled
    }

    private fun mapChoiceToArcanoId(choice: String?): String? {
        return when (choice?.trim()?.uppercase()) {
            "DOM"                -> "antecedente_arcano_dom"
            "MAGIA"              -> "antecedente_arcano_magia"
            "MILAGRES"           -> "antecedente_arcano_milagres"
            "PSIÔNICOS", "PSIONICOS" -> "antecedente_arcano_psionicos"
            "CIÊNCIA ESTRANHA", "CIENCIA ESTRANHA" -> "antecedente_arcano_ciencia_estranha"
            else -> null
        }
    }

    fun normalizeArcanoIdsNoCarregamento() {

        val convertidos = state.vantagensSelecionadas.map { v ->
            if (v.id == "antecedente_arcano" && v.choice != null) {
                val novoId = mapChoiceToArcanoId(v.choice)
                val novo = listaVantagens.find { it.id == novoId }
                novo ?: v
            } else v
        }
        state.vantagensSelecionadas.clear()
        state.vantagensSelecionadas.addAll(convertidos.distinctBy { it.id })
    }

    fun resetStateParaNovoPersonagem(
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        usarEspecializacoesDePericia: Boolean = false,
        grandesResponsabilidades: Boolean = false,
    ) {

        state.modoSupers = modoSupers
        state.modoSuperequip = modoSupers
        state.modoSuperComplicacoes = modoSupers
        state.grandesResponsabilidades = grandesResponsabilidades
        state.modoSuperComplicacoes = modoSupers

        state.idAtual = null
        state.nomePersonagem = ""
        state.anotacoes = ""

        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias
        state.usarEspecializacoesDePericia = usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()

        state.ancestralidade = "HUMANOS"
        state.vantagensSelecionadas.clear()
        state.complicacoesSelecionadas.clear()
        state.vantagensAutomaticas.clear()
        state.desvantagensAutomaticas.clear()
        state.aplicarAncestralidade("HUMANOS")

        if (state.modoSupers) {
            listaVantagens.firstOrNull { it.id == "superpoderes" }?.let { sp ->
                if (state.vantagensSelecionadas.none { it.id == "superpoderes" }) {
                    state.vantagensSelecionadas.add(sp)
                }
            }
    fun valorMovimentacao(): Int {
        val base = 6

        val racialPenalty =
            listaAncestralidadesJson
                .firstOrNull { it.nome.keyify() == state.ancestralidade }
                ?.desvantagens
                ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
                .takeIf { it == true }
                ?.let { 1 }
                ?: 0

        val idosoPenalty =
            state.complicacoesSelecionadas
                .filterKeys { it.id.keyify() == "IDOSO" }
                .isNotEmpty()
                .takeIf { it }
                ?.let { 1 }
                ?: 0

        val lentoPenalty = state.complicacoesSelecionadas
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
            state.complicacoesSelecionadas
                .filterKeys { it.id.keyify() == "OBESO" }
                .isNotEmpty()
                .takeIf { it }
                ?.let { 1 }
                ?: 0

        val ligeiroBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == "LIGEIRO" })
                2
            else
                0

        return (base
                - racialPenalty
                - idosoPenalty
                - lentoPenalty
                - obesoPenalty
                + ligeiroBonus
                + state.bonusMovimentacaoFromPower)
            .coerceAtLeast(0)
    }

    fun valorAparar(): Int {
        val perLutar = listaPericias.firstOrNull { it.nome.equals("Lutar", ignoreCase = true) }
        val lutarRaw = perLutar?.let { state.rawTotalComSupers(it) } ?: 0
        val base     = 2 + (lutarRaw / 2)

        val bloquearBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR" }) 1 else 0
        val bloquearAprimoradoBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == "BLOQUEAR APRIMORADO" }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + state.bonusPararFromPower
    }

    fun valorResistenciaBase(): Int {
        val vigorRaw = state.valoresAtributos["VIGOR"]?.intValue ?: 4
        val base     = 2 + (vigorRaw / 2)

        val bonusPos = if (state.vantagensAutomaticas.any { it.keyify() == "RESISTENCIA" }) 1 else 0
        val bonusNeg = if (state.desvantagensAutomaticas.any { it.keyify() == "FRAGIL" }) -1 else 0

        // Bônus de “Brigão / Pugilista” continua igual
        val brigaoBonus = state.vantagensSelecionadas
            .count { it.nome.keyify() in listOf("BRIGAO", "PUGILISTA") }

        // Em vez de recalcular tamanho aqui, usamos a função centralizada:
        val sizeRaw = valorTamanho()   // já inclui racial, OBESO, PEQUENO, MUSCULOSO, com clamp -1..+3

        return (base + bonusPos + bonusNeg + brigaoBonus + sizeRaw)
            .coerceAtLeast(0)
    }

    fun valorResistenciaFinal(): Int {
        return valorResistenciaBase() + state.bonusResFromPower
    }

    fun valorArmaduraEfetiva(): Int {
        val armorFromEquipment = state.armadura
        val melhorExterna = kotlin.math.max(state.armorFromPower, armorFromEquipment)
        return (melhorExterna + state.naturalArmorFromRace).coerceAtLeast(0)
    }

    fun valorTamanho(): Int {
        val desc = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == state.ancestralidade }
            ?.desvantagens
            ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }

        val racialSize = desc
            ?.substringAfter("TAMANHO")
            ?.trim()
            ?.toIntOrNull()
            ?: 0
        val obesoBonus =
            if (state.complicacoesSelecionadas.keys.any { it.id.keyify() == "OBESO" })
                1
            else
                0
        val pequenoPenalty =
            if (state.complicacoesSelecionadas.keys.any { it.id.keyify() == "PEQUENO" })
                -1
            else
                0
        val musculosoBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" })
                1
            else
                0
        val raw = racialSize + obesoBonus + pequenoPenalty + musculosoBonus
        return raw.coerceIn(-1, 3)
    }

    fun gastarPcParaVantagem(): Boolean {
        // custo de 2 PC para 1 PV
        if (state.pontosComplicacao - state.pontosComplicacaoGastos < 2) return false

        state.pontosComplicacaoGastos += 2
        state.cpPvStack.add(Unit)   // registra que 1 vantagem foi comprada
        state.pontosVantagem += 1
        return true
    }

    fun devolverPcDeVantagem() {
        if (state.cpPvStack.isNotEmpty()) {
            state.cpPvStack.removeLast()
            state.pontosComplicacaoGastos -= 2
            state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
        }
    }

    fun gastarPcParaAtributo(): Boolean {
        // Custo de 2 Pontos de Complicação para 1 aumento de atributo
        val custo = 2
        val disponivel = (state.pontosComplicacao - state.pontosComplicacaoGastos)

        if (disponivel < custo) return false

        state.cpPaStack.add("PB")              // registra 1 compra de atributo com PC
        state.pontosComplicacaoGastos += custo // soma 2 ao total gasto
        state.recalcularPontosAtributo()       // recalcula PA restantes e stacks

        return true
    }
    fun removerVantagemPorSuper(v: Vantagem) {
        state.vantagensSelecionadas.remove(v)
        state.vantagensDePoder.remove(v.id)
    }

    fun adicionarVantagemPorSuper(v: Vantagem): Boolean {
        if (v.categoria == Categoria.LENDARIAS) return false

        val progressoAnterior = state.overrideStageForVantagem
        state.overrideStageForVantagem = "Lendário"

        val permitido = state.podeSelecionar(v)
        state.overrideStageForVantagem = progressoAnterior

        if (!permitido) return false

        if (!state.vantagensSelecionadas.contains(v)) {
            state.vantagensSelecionadas += v
            state.vantagensDePoder += v.id
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

    /** Respeita o teto de mitigação por supers (clampa apenas a soma dos componentes de supers) */
    /** Facilita adicionar/remover efeitos de um PoderId no ledger */
    fun registrarGastoDePoder(poderId: String, custo: Int) {
        val atual = state.gastosPorPoder[poderId] ?: 0
        state.gastosPorPoder[poderId] = atual + custo
        state.superPontosDisponiveis = (state.superPontosTotais - state.gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun desfazerGastoDePoder(poderId: String, custo: Int) {
        val atual = (state.gastosPorPoder[poderId] ?: 0) - custo
        if (atual <= 0) state.gastosPorPoder.remove(poderId) else state.gastosPorPoder[poderId] = atual
        state.superPontosDisponiveis = (state.superPontosTotais - state.gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun updateBonusPararFromPower(value: Int) {
        state.bonusPararFromPower = value.coerceAtLeast(0)
    }

    fun updateBonusResFromPower(value: Int) {
        state.bonusResFromPower = value.coerceAtLeast(0)
    }

    fun updateArmorFromPower(value: Int) {
        state.armorFromPower = value.coerceAtLeast(0)
    }

    fun updateBonusMovimentacaoFromPower(value: Int) {
        state.bonusMovimentacaoFromPower = value.coerceAtLeast(0)
    }
    fun comprarSuperPoder(
        nome: String,
        custo: Int,
        baseCost: Int = custo,
        poderId: String = "sp_${nome.keyify()}",
        registrarNoLedger: Boolean = true
    ): Pair<Boolean, String> {

        // 0) Regra restritiva de perícia mínima (Superciência / Superfeitiçaria)
        val nomeKey = nome.keyify()
        val periciaReqKey = SUPER_PODERES_RESTRITIVOS[nomeKey]
        if (periciaReqKey != null) {
            val perReq = listaPericias.firstOrNull { it.nome.keyify() == periciaReqKey }
            val rawFinalReq = perReq?.let { state.rawTotalComSupers(it) } ?: 0

            if (rawFinalReq < MIN_RAW_RESTRITIVO) {
                val perNome = perReq?.nome ?: periciaReqKey
                return false to "Para comprar $nome, você precisa ter a perícia $perNome em d10 ou mais."
            }
        }

        // 1) Limite de quantidade de superpoderes
        if (state.superPoderesComprados.size >= state.superLimite) {
            return false to "Limite de superpoderes atingido (${state.superLimite})."
        }

        // 2) Saldo de SP
        if (state.superPontosDisponiveis < custo) {
            return false to "Sem saldo: precisa de $custo SP, tem ${state.superPontosDisponiveis}."
        }

        // 3) Compra normal
        state.superPoderesComprados.add(
            PurchasedPower(
                nome = nome,
                custo = custo,
                baseCost = baseCost,
                poderId = poderId
            )
        )

        if (registrarNoLedger) {
            registrarGastoDePoder(poderId, custo)
        }

        return true to "Superpoder adquirido."
    }

    fun removerSuperPoder(
        poder: PurchasedPower,
        desfazerNoLedger: Boolean = true
    ) {
        if (state.superPoderesComprados.remove(poder)) {
            if (desfazerNoLedger) {
                desfazerGastoDePoder(poder.poderId, poder.custo)
            }
        }
    }

    fun grantVantagemPointFromXp(stageName: String) {
        check(state.progressosDisponiveis > 0) { "Sem XP disponível." }

        state.stageXpSpent[stageName] = state.stageXpSpent.getValue(stageName) + 1
        state.progressosDisponiveis -= 1

        state.pontosVantagem += 1
        state.pvFromXpOutstanding += 1

        state.overrideStageForVantagem = stageName

        state.openVantagensAfterGrant = true
    }


    fun maxComprasPpAteAgora(): Int {
        return listaDeEstagios.indexOf(state.estagioAtual()) + 1
    }

    private fun selecionarPontosDePoder(v: Vantagem) {
        val estagio = state.estagioAtual().nome
        val totalFeitas = state.comprasPpPorEstagio.values.sum()

        if (totalFeitas >= maxComprasPpAteAgora()) return

        val feitasNoEstagio = state.comprasPpPorEstagio[estagio] ?: 0
        state.comprasPpPorEstagio[estagio] = feitasNoEstagio + 1

        val ganho = if (totalFeitas < 4) 5 else 2
        state.bonusPoderExtra += ganho

        state.vantagensSelecionadas += v
    }

    fun removerPontosDePoder(v: Vantagem) {
        if (!state.vantagensSelecionadas.remove(v)) return

        val totalAntes = state.comprasPpPorEstagio.values.sum()
        if (totalAntes == 0) return

        val estagio = state.estagioAtual().nome
        val feitas = state.comprasPpPorEstagio[estagio] ?: 0
        if (feitas > 0) {
            state.comprasPpPorEstagio[estagio] = feitas - 1
        } else {
            val fallback = state.comprasPpPorEstagio.entries.lastOrNull { it.value > 0 }
            fallback?.let {
                state.comprasPpPorEstagio[it.key] = it.value - 1
            }
        }

        val ganhoRemovido = if (totalAntes <= 4) 5 else 2
        state.bonusPoderExtra = (state.bonusPoderExtra - ganhoRemovido).coerceAtLeast(0)
    }

    fun comprarPontoDePoder(v: Vantagem) {
        if (!state.podeSelecionar(v)) return
        selecionarPontosDePoder(v)
        state.vantagensSelecionadas += v
    }
    fun applyVantagemDinheiro(v: Vantagem) {
        when (v.nome.trim().uppercase()) {
            "RICO"          -> state.dinheiro += 1000
            "PODRE DE RICO" -> state.dinheiro += 1500
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

        while (state.dinheiro < amount && state.equipamentosComprados.isNotEmpty()) {
            val eq = state.equipamentosComprados.removeAt(state.equipamentosComprados.lastIndex)
            val custo = (eq.custo as? kotlinx.serialization.json.JsonPrimitive)
                ?.content
                ?.toIntOrNull()
                ?: 0
            state.dinheiro += custo
        }

        state.dinheiro = (state.dinheiro - amount).coerceAtLeast(0)
    }
    fun registrarNovosPoderes(versionKey: String, escolhas: List<String>) {
        val pilha = state.novosPoderesStacksPorArcano.getOrPut(versionKey) { mutableListOf() }
        pilha.add(escolhas)
    }

    fun desfazerUltimosNovosPoderes(versionKey: String, initialSlots: Int) {
        val pilha = state.novosPoderesStacksPorArcano[versionKey] ?: return
        if (pilha.isEmpty()) return

        val ultima = pilha.removeLast()
        val slots = state.poderSlotsPorArcano[versionKey] ?: return

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
        state.poderesSelecionados.apply {
            clear()
            addAll(slots.filterNotNull())
        }
    }
        }

        state.equipamentosComprados.clear()

        state.pontosComplicacaoGastos = 0

        state.cpRecursosStack.clear()
        state.cpPaStack.clear()
        state.cpPvStack.clear()
        state.cpSpStack.clear()
        state.comprasPpPorEstagio.keys.forEach   { state.comprasPpPorEstagio[it] = 0 }
        state.comprasAttrPorEstagio.keys.forEach { state.comprasAttrPorEstagio[it] = 0 }
        state.paCostStackPorAtributo.values.forEach  { it.clear() }
        state.compCostStackPorPericia.values.forEach { it.clear() }
        state.spCostStackPorPericia.values.forEach   { it.clear() }
        state.poderSlotsPorArcano.clear()
        state.novosPoderesStacksPorArcano.clear()

        // ─────────────────────────────────────────────────────────────
        // RESET COMPLETO DE SUPERS – NÃO VAZAR ENTRE PERSONAGENS
        // ─────────────────────────────────────────────────────────────
        state.superPoderesComprados.clear()
        state.superNivelCampanha = null
        state.usarSemPontosDePoder = false

        state.superPontosTotais = 0
        state.superPontosDisponiveis = 0
        state.superLimite = 0
        state.superLimitePorPoder = 0
        state.idPoderFavorecido = null
        state.limiteDePoderDaCampanha = Int.MAX_VALUE

        state.faseSupersAtiva = false
        state.superAtributoIncs.clear()
        state.superPericiaIncs.clear()
        state.bonusPararFromPower = 0
        state.bonusResFromPower = 0
        state.armorFromPower = 0
        state.bonusMovimentacaoFromPower = 0
        state.vantagensDePoder.clear()
        state.gastosPorPoder.clear()
        state.naturalArmorFromRace = 0
        // ─────────────────────────────────────────────────────────────

        state.dinheiro = 500
        state.progresso = 0
        state.progressosDisponiveis = 0
        state.frozenAdvCount = 0
        state.emProgresso = false

        state.valoresAtributos.forEach { (_, holder) -> holder.intValue = 4 }
        state.recalcularPontosAtributo()

        listaPericias.forEach { per ->
            state.baseIncsPorPericia[per] = 0
            state.spCostStackPorPericia.getValue(per).clear()
            state.compCostStackPorPericia[per]?.clear()
        }
        state.rebuildAllPericiaStacks()

        state.pontosVantagem =
            if (state.vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0
    }

    fun loadFromSalvo(
        salvo: PersonagemSalvo,
        categoriasBasico: List<EquipamentoCategoria>,
        categoriasSuper:  List<EquipamentoCategoria>
    ) {
        // Reinicia o estado com as flags corretas vindas do save
        resetStateParaNovoPersonagem(
            cartaSelvagem = salvo.cartaSelvagem,
            maisPontosPericias = salvo.maisPontosPericias,
            modoSupers = salvo.modoSupers,
            usarEspecializacoesDePericia = salvo.usarEspecializacoesDePericia
        )

        // Demais flags de modo (mantêm comportamento de telas/filtros)
        state.modoSuperequip        = salvo.modoSuperequip
        state.modoSuperComplicacoes = salvo.modoSuperComplicacoes
        state.usarSemPontosDePoder  = salvo.semPontosDePoder

        // Identidade, nome e anotações
        state.idAtual        = salvo.id
        state.nomePersonagem = salvo.nome
        state.anotacoes      = salvo.anotacoes

        // 2) Ancestralidade e flags gerais — APLICAR ANTES dos atributos/perícias
        state.maisPontosPericias = salvo.maisPontosPericias
        state.cartaSelvagem      = salvo.cartaSelvagem
        state.heroisSemArmadura  = salvo.heroisSemArmadura
        state.ancestralidade     = salvo.ancestralidade
        state.aplicarAncestralidade(salvo.ancestralidade)

        state.cpPaStack.clear()
        repeat(salvo.cpPaCount) { state.cpPaStack.add("PA") }

        state.cpPvStack.clear()
        repeat(salvo.cpPvCount) { state.cpPvStack.add(Unit) }

        state.cpSpStack.clear()
        repeat(salvo.cpSpCount) { state.cpSpStack.add(Unit) }

        state.cpRecursosStack.clear()
        repeat(salvo.cpRecursosCount) { state.cpRecursosStack.add(Unit) }

        state.pontosComplicacaoGastos = (state.cpPaStack.size * 2) +
                (state.cpPvStack.size * 2) +
                (state.cpSpStack.size * 1) +
                (state.cpRecursosStack.size * 1)

        state.valoresAtributos.forEach { (key, holder) ->
            holder.intValue = salvo.atributos[key] ?: 4
        }
        val desiredPericias: Map<Pericia, Int> = listaPericias.associateWith { per ->
            salvo.pericias[per.nome] ?: state.rawTotal(per)
        }
        state.rebuildPericias(desiredPericias)

        state.vantagensSelecionadas.clear()
        val mapPorId   = listaVantagens.associateBy { it.id }
        val mapPorNome = listaVantagens.associateBy { it.nome.trim().uppercase() }

        salvo.vantagens.forEach { saved ->
            val trimmed = saved.trim()
            val byId = mapPorId[trimmed]
            if (byId != null) {
                state.vantagensSelecionadas.add(byId)
            } else {
                val byName = mapPorNome[trimmed.uppercase()]
                if (byName != null) {
                    state.vantagensSelecionadas.add(byName)
                }
            }
        }

        state.complicacoesSelecionadas.clear()
        salvo.complicacoes.forEach { compId ->
            listaComplicacoes.find { it.id == compId }?.let { comp ->
                // Por default, restaura como “Menor”
                state.complicacoesSelecionadas[comp] = "Menor"
            }
        }

        state.equipamentosComprados.clear()
        val todasCategorias = (categoriasBasico + categoriasSuper)
        val mapaItensPorNome = todasCategorias
            .flatMap { it.itens }
            .associateBy { it.nome.trim().uppercase() }

        salvo.equipamentos.forEach { nomeSalvo ->
            mapaItensPorNome[nomeSalvo.trim().uppercase()]?.let { item ->
                state.equipamentosComprados.add(item)
            }
        }

        state.poderSlotsPorArcano.clear()
        salvo.poderes.forEach { (arcano, poderesLista) ->
            val capacidade = arcanoInfo[arcano]?.first ?: 0
            state.poderSlotsPorArcano[arcano] = mutableStateListOf<String?>().apply {
                repeat(capacidade) { idx -> add(poderesLista.getOrNull(idx)) }
            }
        }

        state.idPoderFavorecido = salvo.idPoderFavorecido

        state.superAtributoIncs.clear()
        state.superAtributoIncs.putAll(salvo.superAtributoIncs)

        state.superPericiaIncs.clear()
        state.superPericiaIncs.putAll(salvo.superPericiaIncs)

        state.updateBonusPararFromPower(salvo.bonusPararFromPower)
        state.updateBonusResFromPower (salvo.bonusResFromPower)
        state.updateArmorFromPower    (salvo.armorFromPower)
        state.updateBonusMovimentacaoFromPower(salvo.bonusMovimentacaoFromPower)

        state.vantagensDePoder.clear()
        state.vantagensDePoder.addAll(salvo.vantagensDePoder)

        state.gastosPorPoder.clear()
        state.gastosPorPoder.putAll(salvo.gastosPorPoder)

        state.limiteDePoderDaCampanha = salvo.limiteDePoderDaCampanha

        val totalSupers = salvo.superPontosTotais
        state.superPontosTotais = totalSupers

        if (salvo.modoSupers && totalSupers > 0) {

            val nivel = (totalSupers / 15).coerceIn(1, 5)
            state.superNivelCampanha = nivel

            val limite = 5 * nivel
            state.superLimite        = limite
            state.superLimitePorPoder = limite

            val gastos = state.gastosPorPoder.values.sum()
            state.superPontosDisponiveis = (totalSupers - gastos).coerceAtLeast(0)

            state.faseSupersAtiva = true
        } else {
            state.superNivelCampanha   = null
            state.superPontosDisponiveis = 0
            state.superLimite          = 0
            state.superLimitePorPoder  = 0
            state.faseSupersAtiva      = false
        }

        state.superPoderesComprados.clear()
        salvo.superpoderesComprados.forEach { nomePoder ->
            state.superPoderesComprados.add(
                PurchasedPower(
                    nome     = nomePoder,
                    custo    = 0,
                    baseCost = 0,
                    poderId  = "sp_${nomePoder.keyify()}"
                )
            )
        }

        state.pontosVantagem = salvo.pontosRestantes
        state.dinheiro       = salvo.dinheiro

        state.usarEspecializacoesDePericia = salvo.usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()
        state.especializacoesPorPericia.putAll(salvo.especializacoesPorPericia)

        state.recalcularPontosAtributo()
        state.rebuildAllPericiaStacks()
        normalizeArcanoIdsNoCarregamento()
        if (state.modoSupers) {
            listaVantagens.firstOrNull { it.id == "superpoderes" }?.let { sp ->
                if (state.vantagensSelecionadas.none { it.id == "superpoderes" }) {
                    state.vantagensSelecionadas.add(sp)
                }
            }
        }
    }

    fun perPowerLimit(poderId: String): Int {
        return if (state.idPoderFavorecido != null && state.idPoderFavorecido == poderId)
            state.limiteFavorecido
        else
            state.limitePorPoderPadrao
    }

    /** Cap genérico de atributo final por campanha/racial/estágio. */
    @Suppress("unused_parameter")
    fun atributoCapFinal(attrKey: String): Int = 12

    /** Quantos "steps" de super já foram aplicados a um atributo. */
    @Suppress("unused")
    fun currentAttrSteps(attrKey: String): Int = state.superAtributoIncs[attrKey] ?: 0

    /** Quantos "steps" de super já foram aplicados a uma perícia (por id/keyify). */
    @Suppress("unused")
    fun currentPericiaSteps(periciaId: String): Int = state.superPericiaIncs[periciaId] ?: 0

    /** Busca o raw final de atributo com supers (para conferência de cap). */
    @Suppress("unused")
    fun atributoRawComSupers(attrKey: String): Int = state.atributoRawComSupers(attrKey)

    /** Verifica se a perícia é Lutar (para efeitos que impactam Aparar). */
    @Suppress("unused")
    fun periciaIdEhLutar(periciaId: String): Boolean {
        return periciaId.trim().equals("LUTAR", ignoreCase = true) ||
                periciaId.trim().equals("lutar", ignoreCase = true)
    }

    /** Máximo racial/campanha para um atributo (não é alterado por Superatributo). */
    @Suppress("unused")
    fun maxAtributoRacial(attrKey: String): Int = state.atributoMaxRaw(attrKey)

    /** Verifica se o personagem possui a vantagem “Atributo Lendário” aplicável ao atributo alvo. */
    @Suppress("unused")
    fun possuiAtributoLendarioPara(attrKey: String): Boolean {
        val alvo = attrKey.trim().uppercase()
        return state.vantagensSelecionadas.any { v ->
            val idOk   = v.id.contains("atributo_lendario", ignoreCase = true)
            val nomeOk = v.nome.contains("Atributo Lend", ignoreCase = true) // tolera variações
            if (!(idOk || nomeOk)) return@any false

            // Se a sua implementação usa choice (ex.: “Atributo Lendário (Agilidade)”),
            // validamos a escolha; se não houver choice, consideramos que vale pra qualquer:
            val choice = v.choice?.trim()?.uppercase()
            (choice == null || choice == alvo)
        }
    }

    /**
     * Pode subir o atributo por Super? Respeita:
     * - Máximo racial/campanha (state.atributoMaxRaw)
     * - Bloqueio acima de d12 sem “Atributo Lendário”
     * - Não “salta” o bloqueio de Lendário: se cruzar 12, precisa possuir a vantagem
     */
    fun podeSubirAtributoPorSuper(attrKey: String, steps: Int): InvestCheck {
        if (steps == 0) return InvestCheck(true)

        val atualRaw = state.atributoRawComSupers(attrKey)          // base + supers já aplicados
        val alvoRaw  = state.applySuperStepsFrom(atualRaw, steps)   // simula steps corretamente

        val tetoTecnico = 30
        if (alvoRaw > tetoTecnico) {
            return InvestCheck(false, "Limite técnico de atributo excedido ($tetoTecnico).")
        }
        return InvestCheck(true)
    }

    /**
     * Valida se é possível investir "custo" pontos em um poder específico com um "efeito".
     * - saldo
     * - limite por poder (favorecido vs padrão)
     * - teto combinado de supers (armadura/resistência) pós-compra
     * - regras de tipo (perícia, atributo, vantagem)
     */
    fun canInvestInPower(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestCheck {
        // 1) Saldo de Pontos de Super (SP)
        if (custo <= 0) return InvestCheck(false, "Custo inválido.")
        if (state.superPontosDisponiveis < custo) {
            return InvestCheck(false, "Sem saldo: precisa de $custo, tem ${state.superPontosDisponiveis}.")
        }

        // 2) Limite Individual de CUSTO (quantos SP gastei neste poder)
        val jaGastoNestePoder = state.gastosPorPoder[poderId] ?: 0
        val limiteIndividual = perPowerLimit(poderId)
        if (jaGastoNestePoder + custo > limiteIndividual) {
            val falta = (jaGastoNestePoder + custo) - limiteIndividual
            return InvestCheck(false, "Limite de gasto neste poder excedido em $falta (limite: $limiteIndividual).")
        }

        // 3) Limite Compartilhado de CUSTO (Armadura + Resistência)
        // Regra: a soma de SP gastos em Resistência e Armadura não pode ultrapassar o Limite de Poder da campanha.
        if (poderId == "sp_armor" || poderId == "sp_res") {
            val gastosArmor = state.gastosPorPoder["sp_armor"] ?: 0
            val gastosRes   = state.gastosPorPoder["sp_res"] ?: 0
            val shareAtual  = gastosArmor + gastosRes
            val shareLimite = state.limiteDePoderDaCampanha
            val shareDepois = shareAtual + custo

            if (shareDepois > shareLimite) {
                val excedeu = shareDepois - shareLimite
                return InvestCheck(
                    ok = false,
                    motivoBloqueio = "Limite compartilhado de Armadura+Resistência excedido em $excedeu (gasto previsto: $shareDepois / limite $shareLimite)."
                )
            }
        }

        // 4) Checagens específicas de outros efeitos
        when (efeito) {
            is PowerEffect.SuperAtributo -> {
                val valida = podeSubirAtributoPorSuper(efeito.attrKey, efeito.steps)
                if (!valida.ok) return valida
            }

            is PowerEffect.SuperPericia -> {
                val k = efeito.periciaId
                val stepsAtuais = state.superPericiaIncs[k] ?: 0
                val stepsFinais = stepsAtuais + efeito.steps
                if (stepsFinais < 0) {
                    return InvestCheck(false, "Redução inválida na perícia ${efeito.periciaId}.")
                }
            }

            // Armadura/Resistência: não há checagem adicional aqui,
            // o limite compartilhado já foi tratado acima pelo custo.
            is PowerEffect.BonusArmadura    -> { /* nada extra */ }
            is PowerEffect.BonusResistencia -> { /* nada extra */ }

            is PowerEffect.BonusAparar -> { /* ok */ }

            is PowerEffect.BonusMovimentacao -> { /* ok */ }

            is PowerEffect.SuperVantagem -> {
                val vant = listaVantagens.firstOrNull {
                    it.id.equals(efeito.vantagemId, ignoreCase = true)
                } ?: return InvestCheck(false, "Vantagem não encontrada: ${efeito.vantagemId}.")

                // NÃO permitir comprar de novo se já tiver a vantagem de qualquer forma
                if (state.vantagensSelecionadas.any { it.id == vant.id }) {
                    return InvestCheck(false, "Você já possui a vantagem ${vant.nome}.")
                }

                // valida requisitos ignorando Estágio (simula “Lendário” para não travar pelo estágio)
                val progressoAnterior = state.overrideStageForVantagem
                state.overrideStageForVantagem = "Lendário"
                val permitido = state.podeSelecionar(vant)
                state.overrideStageForVantagem = progressoAnterior

                if (!permitido) {
                    return InvestCheck(false, "Requisitos não atendidos para a vantagem (exceto Estágio).")
                }
            }
        }

        return InvestCheck(true, null)
    }

    /**
     * Aplica o investimento no ledger e atualiza derivados.
     * NÃO chama validação; chame canInvestInPower antes.
     */
    fun applyPowerInvestment(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestResult {
        // 1) registra o gasto no ledger
        state.registrarGastoDePoder(poderId, custo)

        // 2) aplica o efeito em estado observável
        when (efeito) {
            is PowerEffect.SuperAtributo -> {
                val key = efeito.attrKey.uppercase().trim()

                // (a) steps acumulados
                val atualSteps = state.superAtributoIncs[key] ?: 0
                state.superAtributoIncs[key] = (atualSteps + efeito.steps).coerceAtLeast(0)

                // (b) refletir imediatamente no atributo visível:
                // +2 por step até d12; +1 por step acima de d12.
                val holder = state.valoresAtributos[key]
                if (holder != null) {
                    repeat(efeito.steps.coerceAtLeast(0)) {
                        holder.intValue = if (holder.intValue < 12) {
                            (holder.intValue + 2).coerceAtMost(30)
                        } else {
                            (holder.intValue + 1).coerceAtMost(30)
                        }
                    }
                }
            }

            is PowerEffect.SuperPericia -> {
                val k = efeito.periciaId
                val atual = state.superPericiaIncs[k] ?: 0
                state.superPericiaIncs[k] = (atual + efeito.steps).coerceAtLeast(0)
            }

            is PowerEffect.BonusArmadura -> {
                state.updateArmorFromPower((state.armorFromPower + efeito.value).coerceAtLeast(0))
            }

            is PowerEffect.BonusResistencia -> {
                state.updateBonusResFromPower((state.bonusResFromPower + efeito.value).coerceAtLeast(0))
            }

            is PowerEffect.BonusAparar -> {
                state.updateBonusPararFromPower((state.bonusPararFromPower + efeito.value).coerceAtLeast(0))
            }

            is PowerEffect.BonusMovimentacao -> {
                state.updateBonusMovimentacaoFromPower(
                    (state.bonusMovimentacaoFromPower + efeito.value).coerceAtLeast(0)
                )
            }

            is PowerEffect.SuperVantagem -> {
                listaVantagens.firstOrNull { it.id == efeito.vantagemId }?.let { v ->
                    state.adicionarVantagemPorSuper(v)
                }
            }
        }

        // 3) derivados de perícia / etc.
        state.rebuildAllPericiaStacks()
        // IMPORTANTE: NÃO recalcular atributos básicos aqui,
        // para não “somar de novo” os supers nem mexer na etapa de criação com PAs.

        return InvestResult(true, "Investimento aplicado.")
    }

    // ===== NOVO: bloqueio de remoção de Superperícia ligada a superpoderes restritivos =====
    private fun motivoBloqueioRemocaoSuperPericia(perKey: String, rawDepois: Int): String? {
        val temFeiticaria = state.superPoderesComprados.any { it.nome.keyify() == "SUPERFEITICARIA" }
        val temCiencia    = state.superPoderesComprados.any { it.nome.keyify() == "SUPERCIENCIA" }

        if (temFeiticaria && perKey == "OCULTISMO" && rawDepois < 10) {
            return "Primeiro remova o superpoder Superfeitiçaria."
        }
        if (temCiencia && perKey == "CIENCIAS" && rawDepois < 10) {
            return "Primeiro remova o superpoder Superciência."
        }
        return null
    }

    fun revertPowerInvestment(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestResult {
        // desfaz o gasto no ledger
        state.desfazerGastoDePoder(poderId, custo)

        when (efeito) {
            is PowerEffect.SuperAtributo -> {
                val key = efeito.attrKey.uppercase().trim()
                val atual = state.superAtributoIncs[key] ?: 0
                state.superAtributoIncs[key] = (atual - efeito.steps).coerceAtLeast(0)
                if (state.superAtributoIncs[key] == 0) state.superAtributoIncs.remove(key)

                // espelha a aplicação: -1 por step se > d12; -2 por step quando <= d12
                val holder = state.valoresAtributos[key]
                if (holder != null) {
                    repeat(efeito.steps.coerceAtLeast(0)) {
                        holder.intValue = if (holder.intValue > 12) {
                            (holder.intValue - 1).coerceAtLeast(4)
                        } else {
                            (holder.intValue - 2).coerceAtLeast(4)
                        }
                    }
                }
            }

            is PowerEffect.SuperPericia -> {
                val k = efeito.periciaId
                val stepsToRemove = efeito.steps.coerceAtLeast(0)

                // 0) Simula quanto a perícia ficaria após remover esses steps
                val perObj = listaPericias.firstOrNull { it.nome.keyify() == k.keyify() }
                if (perObj != null && stepsToRemove > 0) {
                    val baseRaw       = state.rawTotal(perObj)
                    val stepsAtuais   = state.superPericiaIncs[k] ?: 0
                    val stepsDepois   = (stepsAtuais - stepsToRemove).coerceAtLeast(0)
                    val rawDepois     = state.applySuperStepsFrom(baseRaw, stepsDepois)

                    val perKey = perObj.nome.keyify()
                    val bloqueio = motivoBloqueioRemocaoSuperPericia(perKey, rawDepois)
                    if (bloqueio != null) {
                        // Não desfaz no ledger, não altera nada
                        return InvestResult(false, bloqueio)
                    }
                }

                // 1) Se passou, desfaz normalmente
                val atual = state.superPericiaIncs[k] ?: 0
                state.superPericiaIncs[k] = (atual - efeito.steps).coerceAtLeast(0)
                if (state.superPericiaIncs[k] == 0) state.superPericiaIncs.remove(k)
            }

            is PowerEffect.BonusArmadura -> {
                state.updateArmorFromPower((state.armorFromPower - efeito.value).coerceAtLeast(0))
            }

            is PowerEffect.BonusResistencia -> {
                state.updateBonusResFromPower((state.bonusResFromPower - efeito.value).coerceAtLeast(0))
            }

            is PowerEffect.BonusAparar -> {
                state.updateBonusPararFromPower((state.bonusPararFromPower - efeito.value).coerceAtLeast(0))
            }

            is PowerEffect.BonusMovimentacao -> {
                state.updateBonusMovimentacaoFromPower(
                    (state.bonusMovimentacaoFromPower - efeito.value).coerceAtLeast(0)
                )
            }

            is PowerEffect.SuperVantagem -> {
                listaVantagens.firstOrNull {
                    it.id.equals(efeito.vantagemId, ignoreCase = true)
                }?.let { v ->
                    state.removerVantagemPorSuper(v)
                }
            }
        }

        // Atualiza apenas derivados que dependem de supers / perícias
        state.rebuildAllPericiaStacks()
        // De novo: nada de recalcular atributos de criação aqui.

        return InvestResult(true, "Investimento revertido.")
    }

    /**
     * Função genérica "façade" para a UI: tenta investir e retorna mensagem pronta.
     * Use um poderId estável por alvo (ex.: "sp_pericia_LUTAR", "sp_attr_FORCA", "sp_armor").
     */
    fun tentarInvestirSuper(poderId: String, custo: Int, efeito: PowerEffect): InvestResult {
        val check = canInvestInPower(poderId = poderId, custo = custo, efeito = efeito)
        if (!check.ok) {
            return InvestResult(false, check.motivoBloqueio ?: "Não foi possível investir.")
        }
        return applyPowerInvestment(poderId = poderId, custo = custo, efeito = efeito)
    }

    /** Façade para desfazer o investimento feito. */
    fun desfazerInvestimentoSuper(poderId: String, custo: Int, efeito: PowerEffect): InvestResult {
        return revertPowerInvestment(poderId = poderId, custo = custo, efeito = efeito)
    }

    @Suppress("unused")
    fun iniciarFaseSupers(nivelRoman: String) {
        val total = when (nivelRoman.trim().uppercase()) {
            "I" -> 15
            "II" -> 30
            "III" -> 45
            "IV" -> 60
            "V" -> 75
            else -> 15
        }
        state.superPontosTotais = total
        state.superPontosDisponiveis = total - state.gastosPorPoder.values.sum()
        state.faseSupersAtiva = true
        state.emProgresso = false
    }

    @Suppress("unused")
    fun iniciarFaseProgresso() {
        // Só permite iniciar progresso quando terminou supers
        if (state.superPontosTotais > 0 && state.superPontosDisponiveis == 0) {
            state.emProgresso = true
        }
    }

    fun salvarEExibirFichaPdf(context: Context, dadosDoPersonagem: MeuPersonagem) {
        val pdfFile = File(context.getExternalFilesDir(null), "ficha_preenchida.pdf")

        gerarFichaEmPdf(pdfFile, dadosDoPersonagem)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Nenhum app de PDF encontrado.", Toast.LENGTH_SHORT).show()
        }
    }

    fun buildSummaryLines(personagem: MeuPersonagem): List<String> {
        val lines = mutableListOf<String>()

        val ancestralidadeNome: String = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
            ?.nome ?: personagem.ancestralidade

        val vantagensNomeKey: List<String> = listaVantagens
            .filter { it.id in personagem.vantagens }
            .map { it.nome.keyify() }

        fun temComp(key: String): Boolean =
            personagem.complicacoes.any { it.keyify() == key }

        fun racialSize(): Int =
            listaAncestralidadesJson
                .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
                ?.desvantagens
                ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }
                ?.substringAfter("TAMANHO")
                ?.trim()
                ?.toIntOrNull()
                ?: 0

        fun tamanhoTotal(): Int {
            val base = racialSize()
            val obesoBonus = if (temComp("OBESO")) 1 else 0
            val pequenoPenalty = if (temComp("PEQUENO")) -1 else 0
            return base + obesoBonus + pequenoPenalty
        }

        fun resistenciaBase(): Int {
            val vigorRaw = personagem.atributos["VIGOR"] ?: 4
            val base = 2 + (vigorRaw / 2)

            val bonusPos =
                if (vantagensNomeKey.any { it == "RESISTENCIA" }) 1 else 0
            val bonusNeg =
                if (personagem.complicacoes.any { it.keyify() == "FRAGIL" }) -1 else 0

            val brigaoBonus = vantagensNomeKey.count { it in listOf("BRIGAO", "PUGILISTA") }

            return (base + bonusPos + bonusNeg + brigaoBonus + tamanhoTotal())
                .coerceAtLeast(0)
        }

        fun resistenciaFinal(): Int =
            resistenciaBase() + personagem.bonusResFromPower

        fun calcMovimento(): Int {
            val base = 6

            val racialPenalty =
                listaAncestralidadesJson
                    .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
                    ?.desvantagens
                    ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
                    .takeIf { it == true }
                    ?.let { 1 }
                    ?: 0

            val lentoPenalty = if (temComp("LENTO")) 1 else 0
            val idosoPenalty = if (temComp("IDOSO")) 1 else 0
            val obesoPenalty = if (temComp("OBESO")) 1 else 0
            val ligeiroBonus =
                if (vantagensNomeKey.any { it == "LIGEIRO" }) 2 else 0

            return (
                    base
                            - racialPenalty
                            - lentoPenalty
                            - idosoPenalty
                            - obesoPenalty
                            + ligeiroBonus
                            + personagem.bonusMovimentacaoFromPower
                    ).coerceAtLeast(0)
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

        fun calcAparar(): Int {
            val lutarRawBase = personagem.pericias["Lutar"] ?: 0
            val lutarStepsFromSupers = personagem.superPericiaIncs["LUTAR"] ?: 0
            val lutarComSupers = applySuperStepsFrom(lutarRawBase, lutarStepsFromSupers)

            val base = 2 + (lutarComSupers / 2)

            val bloquearBonus =
                if (vantagensNomeKey.any { it == "BLOQUEAR" }) 1 else 0
            val bloquearAprimoradoBonus =
                if (vantagensNomeKey.any { it == "BLOQUEAR APRIMORADO" }) 1 else 0

            return base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusPararFromPower
        }

        fun calcArmaduraEfetiva(): Int {
            return personagem.armorFromPower.coerceAtLeast(0)
        }

        val aparar = calcAparar()
        val resFinal = resistenciaFinal()
        val tamanho = tamanhoTotal()
        val mov = calcMovimento()
        val armadura = calcArmaduraEfetiva()
        val resistenciaTexto =
            if (armadura > 0) "${resFinal}(${armadura})" else resFinal.toString()

        lines += "Identidade"
        lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
        lines += "Ancestralidade: $ancestralidadeNome"
        lines += ""

        lines += "Atributos derivados"
        lines += "Aparar: $aparar"
        lines += "Resistência: $resistenciaTexto"
        lines += "Tamanho: $tamanho"
        lines += "Movimento: $mov"
        if (armadura > 0) {
            lines += "Armadura: $armadura"
        }
        lines += ""

        lines += "Atributos"
        lines += listaAtributos.joinToString(", ") { attrKey ->
            val label = mapaAtributosDisplay[attrKey] ?: attrKey
            val valor = personagem.atributos[attrKey] ?: 4
            "$label d$valor"
        }
        lines += ""

        val periciasParaMostrar = listaPericias.filter { per ->
            per.basica || (personagem.pericias[per.nome] ?: 0) >
                    periciaStartRaw(personagem.ancestralidade, per)
        }

        lines += "Perícias"
        if (periciasParaMostrar.isEmpty()) {
            lines += "– Nenhuma"
        } else {
            periciasParaMostrar.forEach { per ->
                val raw = personagem.pericias[per.nome] ?: 0
                lines += "• ${per.nome} d$raw"
            }
        }
        lines += ""

        lines += "Recursos & Equipamentos"
        lines += "Dinheiro restante: ${personagem.dinheiro}"
        if (personagem.equipamentos.isEmpty()) {
            lines += "Equipamentos: – Nenhum"
        } else {
            lines += "Equipamentos:"
            personagem.equipamentos.forEach { eq ->
                lines += "• ${eq.nome}"
            }
        }
        lines += ""

        lines += "Vantagens"
        if (personagem.vantagens.isEmpty()) {
            lines += "– Nenhuma"
        } else {
            val nomesVantagens = listaVantagens
                .filter { it.id in personagem.vantagens }
                .map { it.nome }
            lines += nomesVantagens.joinToString(", ")
        }
        lines += ""

        lines += "Complicações"
        lines += if (personagem.complicacoes.isEmpty()) {
            "– Nenhuma"
        } else {
            personagem.complicacoes.joinToString(", ")
        }
        lines += ""

        if (personagem.poderes.isNotEmpty()) {
            lines += "Poderes arcanos"
            personagem.poderes.forEach { (arcanoKey, lista) ->
                val label = arcanoKey
                    .lowercase()
                    .replace('_', ' ')
                    .replaceFirstChar { it.titlecase() }

                lines += if (lista.isEmpty()) {
                    "• $label: – nenhum poder escolhido"
                } else {
                    "• $label: ${lista.joinToString(", ")}"
                }
            }
            lines += ""
        }

        if (personagem.modoSupers &&
            (personagem.superPontosTotais > 0 || personagem.gastosPorPoder.isNotEmpty())
        ) {
            lines += "Superpoderes"

            if (personagem.gastosPorPoder.isEmpty()) {
                lines += "– Nenhum superpoder registrado"
            } else {
                personagem.gastosPorPoder.forEach { (poderId, custo) ->
                    lines += "• $poderId: $custo SP"
                }
            }

            lines += "Superpontos: ${personagem.superPontosTotais} (disponíveis: ${personagem.superPontosDisponiveis})"
            lines += "Limite por poder: ${personagem.limitePorPoderPadrao}"
            lines += ""
        }

        if (personagem.anotacoes.isNotBlank()) {
            lines += "Anotações"
            personagem.anotacoes
                .lines()
                .forEach { linha -> lines += linha }
        }

        return lines
    }

    fun gerarFichaEmPdf(destino: File, personagem: MeuPersonagem) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

        val marginLeft   = 40f
        val marginRight  = 40f
        val marginTop    = 50f
        val marginBottom = 40f

        val paint = Paint().apply { textSize = 12f }
        val fm = paint.fontMetrics
        val lineHeight = fm.descent - fm.ascent + fm.leading

        var page = doc.startPage(pageInfo)
        var canvas = page.canvas
        var y = marginTop

        fun newPage() {
            doc.finishPage(page)
            page = doc.startPage(pageInfo)
            canvas = page.canvas
            y = marginTop
        }

        fun drawWrapped(text: String) {
            var start = 0
            val maxWidth = pageInfo.pageWidth - marginLeft - marginRight
            while (start < text.length) {
                val count = paint.breakText(text, start, text.length, true, maxWidth, null)
                val line = text.substring(start, start + count)
                if (y + lineHeight > pageInfo.pageHeight - marginBottom) {
                    newPage()
                }
                canvas.drawText(line, marginLeft, y, paint)
                y += lineHeight
                start += count
            }
        }

        val titlePaint = Paint(paint).apply {
            textSize = 16f
            isFakeBoldText = true
        }
        val title = "Ficha de ${personagem.nome}"

        val titleFm = titlePaint.fontMetrics
        val titleHeight = titleFm.descent - titleFm.ascent + titleFm.leading
        if (y + titleHeight > pageInfo.pageHeight - marginBottom) {
            newPage()
        }
        canvas.drawText(title, marginLeft, y, titlePaint)
        y += titleHeight + 12f

        val lines = buildSummaryLines(personagem)
        for (linha in lines) {
            drawWrapped(linha)
        }

        doc.finishPage(page)
        FileOutputStream(destino).use { out ->
            doc.writeTo(out)
        }
        doc.close()
    }
}