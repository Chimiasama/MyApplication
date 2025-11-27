// CriadorViewModel.kt
package com.example.swadebuilder.model

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.model.PersonagemSalvo
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.util.keyify

// ---- OBJETOS DE RETORNO ----
data class InvestCheck(val ok: Boolean, val motivoBloqueio: String? = null)
data class InvestResult(val ok: Boolean, val mensagem: String)

/**
 * ViewModel que gerencia o estado de criação de personagem.
 */
class CriadorViewModel : ViewModel() {

    val state = CriadorState()

    private val _feedbackMessages = mutableStateListOf<String>()
    val feedbackMessages: List<String> = _feedbackMessages

    fun clearFeedbackMessages() {
        _feedbackMessages.clear()
    }

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
        state.aplicarAncestralidade("HUMANOS", _feedbackMessages)

        if (state.modoSupers) {
            listaVantagens.firstOrNull { it.id == "superpoderes" }?.let { sp ->
                if (state.vantagensSelecionadas.none { it.id == "superpoderes" }) {
                    state.vantagensSelecionadas.add(sp)
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
        state.superInvestments.clear()
        state.superNivelCampanha = null
        state.usarSemPontosDePoder = false

        state.superPontosTotais = 0
        state.superPontosDisponiveis = 0
        state.superLimite = 0
        state.superLimitePorPoder = 0
        state.poderFavoritoId = null
        state.limiteDePoderDaCampanha = Int.MAX_VALUE

        state.faseSupersAtiva = false
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
        state.recalcularPontosAtributo(_feedbackMessages)

        listaPericias.forEach { per ->
            state.baseIncsPorPericia[per] = 0
            state.spCostStackPorPericia.getValue(per).clear()
            state.compCostStackPorPericia[per]?.clear()
        }
        state.rebuildAllPericiaStacks(_feedbackMessages)

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
        state.aplicarAncestralidade(salvo.ancestralidade, _feedbackMessages)

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

        state.poderFavoritoId = salvo.poderFavoritoId

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

        state.superInvestments.clear()
        state.superInvestments.addAll(salvo.superInvestments)

        state.pontosVantagem = salvo.pontosRestantes
        state.dinheiro       = salvo.dinheiro

        state.usarEspecializacoesDePericia = salvo.usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()
        state.especializacoesPorPericia.putAll(salvo.especializacoesPorPericia)

        state.recalcularPontosAtributo(_feedbackMessages)
        state.rebuildAllPericiaStacks(_feedbackMessages)
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
        return if (state.poderFavoritoId != null && state.poderFavoritoId == poderId)
            state.limiteFavorecido
        else
            state.limitePorPoderPadrao
    }

    fun definirPoderFavorecido(poderId: String?) {
        state.poderFavoritoId = poderId
    }

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
                // A lógica de steps foi removida daqui, será tratada na UI/ViewModel
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

            is PowerEffect.Generico -> {
                // Nenhuma validação extra necessária para poderes genéricos
            }
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

                // (b) refletir imediatamente no atributo visível:
                // +2 por step até d12; +1 por step acima de d12.
                val holder = state.valoresAtributos[key]
                if (holder != null) {
                    val antes = holder.intValue
                    repeat(efeito.steps.coerceAtLeast(0)) {
                        holder.intValue = if (holder.intValue < 12) {
                            (holder.intValue + 2).coerceAtMost(30)
                        } else {
                            (holder.intValue + 1).coerceAtMost(30)
                        }
                    }
                    _feedbackMessages.add("Atributo $key aumentado de d$antes para d${holder.intValue}.")
                }
            }

            is PowerEffect.SuperPericia -> {
                // O efeito visual é agora derivado da lista de investimentos,
                // então não é mais necessário aplicar diretamente aqui.
                // A atualização da UI será automática.
            }

            is PowerEffect.BonusArmadura -> {
                state.updateArmorFromPower((state.armorFromPower + efeito.value).coerceAtLeast(0))
                _feedbackMessages.add("Armadura aumentada em ${efeito.value}.")
            }

            is PowerEffect.BonusResistencia -> {
                state.updateBonusResFromPower((state.bonusResFromPower + efeito.value).coerceAtLeast(0))
                _feedbackMessages.add("Resistência aumentada em ${efeito.value}.")
            }

            is PowerEffect.BonusAparar -> {
                state.updateBonusPararFromPower((state.bonusPararFromPower + efeito.value).coerceAtLeast(0))
                _feedbackMessages.add("Aparar aumentado em ${efeito.value}.")
            }

            is PowerEffect.BonusMovimentacao -> {
                state.updateBonusMovimentacaoFromPower(
                    (state.bonusMovimentacaoFromPower + efeito.value).coerceAtLeast(0)
                )
                _feedbackMessages.add("Movimentação aumentada em ${efeito.value}.")
            }

            is PowerEffect.SuperVantagem -> {
                listaVantagens.firstOrNull { it.id == efeito.vantagemId }?.let { v ->
                    state.adicionarVantagemPorSuper(v)
                    _feedbackMessages.add("Vantagem ${v.nome} adicionada.")
                }
            }

            is PowerEffect.Generico -> {
                _feedbackMessages.add("${efeito.nome} adquirido.")
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
        val temFeiticaria = state.superInvestments.any { it.displayName.keyify() == "SUPERFEITICARIA" }
        val temCiencia    = state.superInvestments.any { it.displayName.keyify() == "SUPERCIENCIA" }

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

                // espelha a aplicação: -1 por step se > d12; -2 por step quando <= d12
                val holder = state.valoresAtributos[key]
                if (holder != null) {
                    val antes = holder.intValue
                    repeat(efeito.steps.coerceAtLeast(0)) {
                        holder.intValue = if (holder.intValue > 12) {
                            (holder.intValue - 1).coerceAtLeast(4)
                        } else {
                            (holder.intValue - 2).coerceAtLeast(4)
                        }
                    }
                    _feedbackMessages.add("Atributo $key reduzido de d$antes para d${holder.intValue}.")
                }
            }

            is PowerEffect.SuperPericia -> {
                val perObj = listaPericias.firstOrNull { it.nome.keyify() == efeito.periciaKey.keyify() }
                if (perObj != null) {
                    val baseRaw = state.rawTotal(perObj)
                    val incsAtuais = state.superInvestments
                        .mapNotNull { it.effect as? PowerEffect.SuperPericia }
                        .filter { it.periciaKey.equals(perObj.nome, ignoreCase = true) }
                        .sumOf { it.steps }
                    val incsDepois = (incsAtuais - efeito.steps).coerceAtLeast(0)
                    val rawDepois = state.applySuperStepsFrom(baseRaw, incsDepois)

                    val perKey = perObj.nome.keyify()
                    val bloqueio = motivoBloqueioRemocaoSuperPericia(perKey, rawDepois)
                    if (bloqueio != null) {
                        return InvestResult(false, bloqueio)
                    }
                }
            }

            is PowerEffect.BonusArmadura -> {
                state.updateArmorFromPower((state.armorFromPower - efeito.value).coerceAtLeast(0))
                _feedbackMessages.add("Armadura reduzida em ${efeito.value}.")
            }

            is PowerEffect.BonusResistencia -> {
                state.updateBonusResFromPower((state.bonusResFromPower - efeito.value).coerceAtLeast(0))
                _feedbackMessages.add("Resistência reduzida em ${efeito.value}.")
            }

            is PowerEffect.BonusAparar -> {
                state.updateBonusPararFromPower((state.bonusPararFromPower - efeito.value).coerceAtLeast(0))
                _feedbackMessages.add("Aparar reduzido em ${efeito.value}.")
            }

            is PowerEffect.BonusMovimentacao -> {
                state.updateBonusMovimentacaoFromPower(
                    (state.bonusMovimentacaoFromPower - efeito.value).coerceAtLeast(0)
                )
                _feedbackMessages.add("Movimentação reduzida em ${efeito.value}.")
            }

            is PowerEffect.SuperVantagem -> {
                listaVantagens.firstOrNull {
                    it.id.equals(efeito.vantagemId, ignoreCase = true)
                }?.let { v ->
                    state.removerVantagemPorSuper(v)
                    _feedbackMessages.add("Vantagem ${v.nome} removida.")
                }
            }
            is PowerEffect.Generico -> {
                _feedbackMessages.add("${efeito.nome} removido.")
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
    fun tentarInvestirSuper(investment: SuperInvestment): InvestResult {
        val check = canInvestInPower(
            poderId = investment.powerId,
            custo = investment.cost,
            efeito = investment.effect
        )
        if (!check.ok) {
            return InvestResult(false, check.motivoBloqueio ?: "Não foi possível investir.")
        }

        // Adiciona o investimento à lista principal do estado
        state.superInvestments.add(investment)

        return applyPowerInvestment(
            poderId = investment.powerId,
            custo = investment.cost,
            efeito = investment.effect
        )
    }

    /** Façade para desfazer o investimento feito. */
    fun desfazerInvestimentoSuper(investment: SuperInvestment): InvestResult {
        return revertPowerInvestment(
            poderId = investment.powerId,
            custo = investment.cost,
            efeito = investment.effect
        )
    }
}