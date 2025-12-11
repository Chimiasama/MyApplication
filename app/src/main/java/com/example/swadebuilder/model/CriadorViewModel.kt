// CriadorViewModel.kt
package com.example.swadebuilder.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.normAAKey
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.util.CharacterStorage
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

    fun setAppTheme(theme: com.example.swadebuilder.ui.theme.AppTheme) {
        state.appTheme = theme
    }

    fun salvarPersonagem(context: Context, nomePersonalizado: String? = null): CharacterStorage.SaveEntry {
        val nome = (nomePersonalizado?.takeIf { it.isNotBlank() } ?: state.nomePersonagem)
            .ifBlank { "Personagem" }

        val snapshot = state.toSnapshot().copy(nome = nome)
        val entry = CharacterStorage.save(context, snapshot)
        state.idAtual = entry.id
        _feedbackMessages.add("Personagem salvo: ${entry.nome}")
        return entry
    }

    fun listarPersonagensSalvos(context: Context): List<CharacterStorage.SaveEntry> {
        return CharacterStorage.listSaves(context)
    }

    fun carregarPersonagem(context: Context, saveId: String): Boolean {
        val snapshot = CharacterStorage.load(context, saveId) ?: return false
        clearFeedbackMessages()
        val flags = snapshot.flags
        resetStateParaNovoPersonagem(
            cartaSelvagem = flags.cartaSelvagem,
            maisPontosPericias = flags.maisPontosPericias,
            modoSupers = flags.modoSupers,
            compendioFantasiaAtivo = flags.compendioFantasiaAtivo,
            compendioHorrorAtivo = flags.compendioHorrorAtivo,
            modoMonstroAtivo = flags.modoMonstroAtivo,
            usarEspecializacoesDePericia = flags.usarEspecializacoesDePericia,
            grandesResponsabilidades = flags.grandesResponsabilidades,
            showHelpMessages = snapshot.showHelpMessages
        )
        state.restoreFromSnapshot(snapshot, _feedbackMessages)
        state.idAtual = saveId
        return true
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
        compendioFantasiaAtivo: Boolean,
        compendioHorrorAtivo: Boolean = false,
        modoMonstroAtivo: Boolean = false,
        usarEspecializacoesDePericia: Boolean = false,
        grandesResponsabilidades: Boolean = false,
        showHelpMessages: Boolean = false
    ) {

        state.showHelpMessages = showHelpMessages
        state.modoSupers = modoSupers
        state.compendioFantasiaAtivo = compendioFantasiaAtivo
        state.compendioHorrorAtivo = compendioHorrorAtivo
        state.modoMonstroAtivo = modoMonstroAtivo
        state.tipoMonstroSelecionado = if (modoMonstroAtivo) "anjo" else null
        state.modoSuperequip = modoSupers
        state.modoSuperComplicacoes = modoSupers
        state.grandesResponsabilidades = grandesResponsabilidades
        state.modoSuperComplicacoes = modoSupers
        state.soldadoCargaAtivo = true

        state.idAtual = null
        state.nomePersonagem = ""
        state.anotacoes = ""

        state.tipoMonstroSelecionado = if (modoMonstroAtivo) "anjo" else null

        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias
        state.usarEspecializacoesDePericia = usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()

        state.ancestralidade = "HUMANOS"
        state.vantagensSelecionadas.clear()
        state.complicacoesSelecionadas.clear()
        state.reservasComplicacaoMaior.clear()
        state.vantagensAutomaticas.clear()
        state.desvantagensAutomaticas.clear()
        state.vantagemEmFoco = null
        state.categoriasVantagensExpandidas.keys.forEach { cat ->
            state.categoriasVantagensExpandidas[cat] = false
        }
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
        state.paFromProgress = 0
        state.spFromProgress = 0
        state.legendaryAttrReservations = 0
        state.cpPvStack.clear()
        state.cpSpStack.clear()
        state.comprasPpPorEstagio.keys.forEach   { state.comprasPpPorEstagio[it] = 0 }
        state.comprasAttrPorEstagio.keys.forEach { state.comprasAttrPorEstagio[it] = 0 }
        state.paCostStackPorAtributo.values.forEach  { it.clear() }
        state.compCostStackPorPericia.values.forEach { it.clear() }
        state.spCostStackPorPericia.values.forEach   { it.clear() }
        state.poderSlotsPorArcano.clear()
        state.novosPoderesStacksPorArcano.clear()
        state.attributeAdvancementInProgress = false
        state.attributeStageForCurrentAdvancement = null
        state.attributeStacksBeforeAdvancement = null
        state.attributeUsedReservation = false
        state.stageNameForCurrentAdvancement = null
        state.overrideStageForVantagem = null
        state.arcanoEmCompraViaXpKey = null
        state.arcanoSnapshotAntesDaCompra = null
        state.mostrandoPoderesProgresso = false
        state.mostrandoAtributosProgresso = false

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
        state.stageXpSpent.keys.forEach { state.stageXpSpent[it] = 0 }
        state.xpSlots.fill(false)
        state.frozenAdvantageCount = 0
        state.advancementHistory.clear()
        state.emProgresso = false
        state.modoProgressaoAtivo = false
        state.mostrandoVantagensProgresso = false
        state.mostrandoPericiasProgresso = false
        state.frozenSkillIncrements.clear()

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
            is PowerEffect.Generico -> {
                // Nenhuma validação extra necessária para poderes genéricos
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

    fun startSkillAdvancement(slotIndex: Int, stageName: String) {
        if (state.progressosDisponiveis >= 1) {
            state.progresso++
            state.spendProgressAtStage(stageName, 1)
            state.stageNameForCurrentAdvancement = stageName
            state.xpSlots[slotIndex] = true
            state.skillAdvancementInProgress = true
            state.skillsForCurrentAdvancement.clear()
            state.grantSkillPointsFromXp()
            state.updateEmProgressoFlag()
        }
    }


    fun finishSkillAdvancement() {
        if (state.skillAdvancementInProgress) {
            val skills = state.skillsForCurrentAdvancement.toList()
            val stageName = state.stageNameForCurrentAdvancement ?: state.estagioAtual().nome
            val skillValuesSnapshot = skills.associateWith { skillName ->
                val pericia = listaPericias.firstOrNull { it.nome == skillName }
                pericia?.let { state.rawTotal(it) }
            }.filterValues { it != null }.mapValues { it.value!! }
            state.advancementHistory.add(
                AdvancementAction.SpendOnSkills(
                    skillsIncreased = skills,
                    recordedSkillValues = skillValuesSnapshot,
                    stageName = stageName
                )
            )
            state.skillAdvancementInProgress = false
            state.skillsForCurrentAdvancement.clear()
            state.stageNameForCurrentAdvancement = null
            if (state.modoProgressaoAtivo) {
                state.snapshotFrozenSkillIncrements()
            }
            state.updateEmProgressoFlag()
            state.mostrandoPericiasProgresso = false
        }
    }

    fun startAdvantageAdvancement(slotIndex: Int, est: String) {
        if (state.progressosDisponiveis >= 1) {
            state.progresso++
            state.spendProgressAtStage(est, 1)
            state.stageNameForCurrentAdvancement = est
            state.xpSlots[slotIndex] = true
            state.advantageAdvancementInProgress = true
            state.advantageForCurrentAdvancement = null
            state.mostrandoPoderesProgresso = false
            state.arcanoEmCompraViaXpKey = null
            state.arcanoSnapshotAntesDaCompra = null
            state.grantVantagemPointFromXp(est)
            state.updateEmProgressoFlag()
        }
    }

    fun finishAdvantageAdvancement() {
        if (state.advantageAdvancementInProgress) {
            if (state.arcanoCompraPendente()) return
            val advantageId = state.advantageForCurrentAdvancement
            if (advantageId != null) {
                val stageName = state.stageNameForCurrentAdvancement ?: state.estagioAtual().nome
                state.advancementHistory.add(
                    AdvancementAction.SpendOnAdvantage(
                        advantageId = advantageId,
                        stageName = stageName,
                        arcanoKey = state.arcanoEmCompraViaXpKey,
                        previousArcanoSlots = state.arcanoSnapshotAntesDaCompra
                    )
                )
                if (state.pvFromXpOutstanding > 0) {
                    state.pvFromXpOutstanding--
                }
            }
            state.advantageAdvancementInProgress = false
            state.advantageForCurrentAdvancement = null
            state.stageNameForCurrentAdvancement = null
            state.limparCompraArcanoViaXp(restaurarSnapshot = false)
            state.updateEmProgressoFlag()
            state.mostrandoVantagensProgresso = false
        }
    }

    fun selectAdvantageForAdvancement(vantagem: Vantagem) {
        if (state.advantageAdvancementInProgress) {
            if (state.advantageForCurrentAdvancement != null) {
                val currentAdvantageId = state.advantageForCurrentAdvancement!!
                val currentAdvantage = state.vantagensSelecionadas.find { it.id == currentAdvantageId }
                if (currentAdvantage != null) {
                    val currentArcKey = currentAdvantage.toArcanoKey()?.normAAKey()
                    if (currentArcKey != null && currentArcKey == state.arcanoEmCompraViaXpKey) {
                        state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                    }

                    if (currentAdvantage.nome.contains("Pontos de Poder", true)) {
                        state.removerPontosDePoder(currentAdvantage)
                    } else {
                        state.removeVantagemDinheiro(currentAdvantage)
                        state.vantagensSelecionadas.remove(currentAdvantage)
                    }
                    state.pontosVantagem++
                }
            }

            if (vantagem.nome.contains("Pontos de Poder", true)) {
                state.comprarPontoDePoder(vantagem)
            } else {
                state.applyVantagemDinheiro(vantagem)
                // Se for "Novos Poderes" e temos múltiplos arcanos habilitados,
                // seria ideal que 'vantagem' já viesse com choice definida.
                // Aqui vamos assumir que se não vier, e tivermos 1 arcano, aplicamos a ele.
                // Mas 'vantagem' é data class vinda do clique.
                state.vantagensSelecionadas.add(vantagem)
            }
            state.pontosVantagem--
            state.advantageForCurrentAdvancement = vantagem.id

            // Check if it's "Novos Poderes" to trigger the flow
            if (vantagem.id == "novos_poderes") {
                // Find target arcane background
                // 1. Try choice if set
                val choiceKey = advantageArcaneKey(vantagem)
                // 2. If not, try to find the first existing arcane background
                val arcKey = choiceKey ?: state.vantagensSelecionadas
                    .mapNotNull { it.toArcanoKey()?.normAAKey() }
                    .firstOrNull()

                if (arcKey != null) {
                    state.iniciarCompraArcanoViaXp(arcKey)
                } else {
                    state.limparCompraArcanoViaXp(restaurarSnapshot = false)
                }
            } else {
                vantagem.toArcanoKey()?.let { arcKey ->
                    state.iniciarCompraArcanoViaXp(arcKey)
                } ?: state.limparCompraArcanoViaXp(restaurarSnapshot = false)
            }

            state.rebuildAllPericiaStacks()
        }
    }

    private fun advantageArcaneKey(v: Vantagem): String? {
        return v.choice?.normAAKey()
    }

    fun startAttributeAdvancement(
        slotIndex: Int,
        stageName: String,
        consumesLegendaryReservation: Boolean
    ) {
        if (state.progressosDisponiveis >= 1) {
            state.progresso++
            state.spendProgressAtStage(stageName, 1)
            state.stageNameForCurrentAdvancement = stageName
            state.attributeStageForCurrentAdvancement = stageName
            state.xpSlots[slotIndex] = true

            if (consumesLegendaryReservation) {
                state.legendaryAttrReservations =
                    (state.legendaryAttrReservations - 1).coerceAtLeast(0)
            }
            state.attributeUsedReservation = consumesLegendaryReservation

            state.paFromProgress += 1
            state.recalcularPontosAtributo()

            state.attributeAdvancementInProgress = true
            state.attributeStacksBeforeAdvancement = state.snapshotAttributeStacks()
            state.mostrandoAtributosProgresso = true
            state.updateEmProgressoFlag()
        }
    }

    fun reserveLegendaryAttribute(slotIndex: Int, stageName: String) {
        if (state.progressosDisponiveis >= 1 && state.legendaryAttrReservations == 0) {
            state.progresso++
            state.spendProgressAtStage(stageName, 1)
            state.xpSlots[slotIndex] = true
            state.legendaryAttrReservations += 1
            state.advancementHistory.add(
                AdvancementAction.ReserveLegendaryAttribute(stageName = stageName)
            )
            state.recomputeAvailableProgress()
        }
    }

    fun finishAttributeAdvancement() {
        if (state.attributeAdvancementInProgress) {
            val before = state.attributeStacksBeforeAdvancement ?: emptyMap()
            val increases = mutableListOf<String>()
            state.paCostStackPorAtributo.forEach { (attr, stack) ->
                val diff = stack.size - (before[attr] ?: 0)
                repeat(diff.coerceAtLeast(0)) { increases.add(attr) }
            }

            val stageName = state.attributeStageForCurrentAdvancement ?: state.estagioAtual().nome
            var reservationAvailable = state.attributeUsedReservation
            increases.forEach { attr ->
                val usedReservation = reservationAvailable
                if (reservationAvailable) reservationAvailable = false
                val prev = state.comprasAttrPorEstagio[stageName] ?: 0
                state.comprasAttrPorEstagio[stageName] = prev + 1
                state.advancementHistory.add(
                    AdvancementAction.IncreaseAttribute(
                        attributeName = attr,
                        usedLegendaryReservation = usedReservation,
                        stageName = stageName,
                        progressCost = 1
                    )
                )
            }

            state.attributeAdvancementInProgress = false
            state.attributeStageForCurrentAdvancement = null
            state.stageNameForCurrentAdvancement = null
            state.attributeStacksBeforeAdvancement = null
            state.attributeUsedReservation = false
            state.mostrandoAtributosProgresso = false
            state.recomputeAvailableProgress()
            state.checkFreeze()
            state.updateEmProgressoFlag()
            state.mostrandoAtributosProgresso = false
        }
    }

    fun cancelAdvancementInProgress() {
        // Roda apenas se houver um avanço em andamento para ser cancelado.
        if (
            !state.skillAdvancementInProgress &&
            !state.advantageAdvancementInProgress &&
            !state.attributeAdvancementInProgress
        ) {
            return
        }

        val stageName = state.stageNameForCurrentAdvancement
            ?: state.attributeStageForCurrentAdvancement
            ?: state.estagioAtual().nome

        val lastUsedIndex = state.xpSlots.indexOfLast { it }
        if (lastUsedIndex != -1) {
            state.xpSlots[lastUsedIndex] = false
            state.progresso--
            state.refundProgressAtStage(stageName, 1)
        }

        if (state.skillAdvancementInProgress) {
            state.spFromProgress = (state.spFromProgress - 2).coerceAtLeast(0)
            state.rebuildAllPericiaStacks()
        }

        if (state.advantageAdvancementInProgress) {
            state.advantageForCurrentAdvancement?.let { advId ->
                state.vantagensSelecionadas.firstOrNull { it.id == advId }?.let { vant ->
                    val arcKey = vant.toArcanoKey()?.normAAKey()
                    // Se a vantagem for um antecedente arcano sendo comprado OU
                    // se for uma vantagem que acionou o fluxo de compra de poderes (ex: Novos Poderes)
                    if (state.arcanoEmCompraViaXpKey != null) {
                        state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                    } else if (arcKey != null && arcKey == state.arcanoEmCompraViaXpKey) {
                        state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                    }

                    if (vant.nome.contains("Pontos de Poder", true)) {
                        state.removerPontosDePoder(vant)
                    } else {
                        state.removeVantagemDinheiro(vant)
                        state.vantagensSelecionadas.remove(vant)
                    }
                    state.pontosVantagem++
                }
            }
            state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
            state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
        }

        if (state.attributeAdvancementInProgress) {
            state.paFromProgress = (state.paFromProgress - 1).coerceAtLeast(0)
            state.attributeStacksBeforeAdvancement?.let { state.restoreAttributeStacks(it) }
            if (state.attributeUsedReservation) {
                state.legendaryAttrReservations += 1
            }
            state.attributeUsedReservation = false
            state.attributeStageForCurrentAdvancement = null
            state.attributeStacksBeforeAdvancement = null
            state.mostrandoAtributosProgresso = false
        }

        state.skillAdvancementInProgress = false
        state.advantageAdvancementInProgress = false
        state.attributeAdvancementInProgress = false
        state.advantageForCurrentAdvancement = null
        state.stageNameForCurrentAdvancement = null
        state.mostrandoPericiasProgresso = false
        state.mostrandoVantagensProgresso = false
        state.mostrandoPoderesProgresso = false
        state.updateEmProgressoFlag()
    }

    fun revertLastAdvancement() {
        if (state.advancementHistory.isEmpty()) return

        val lastAction = state.advancementHistory.removeLast()
        val stageName = lastAction.stageName

        // Reverte o slot de XP e o contador de progresso
        val lastUsedIndex = state.xpSlots.indexOfLast { it }
        if (lastUsedIndex != -1) {
            state.xpSlots[lastUsedIndex] = false
            state.progresso--
        }

        when (lastAction) {
            is AdvancementAction.SpendOnAdvantage -> {
                // Reverte o gasto E a concessão do ponto de vantagem
                val advantage = state.vantagensSelecionadas.firstOrNull { it.id == lastAction.advantageId }
                if (advantage != null) {
                    state.vantagensSelecionadas.remove(advantage)
                }
                lastAction.arcanoKey?.let { arcKey ->
                    state.restoreArcanoSlots(arcKey, lastAction.previousArcanoSlots)
                }
                state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
                state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
                state.frozenAdvantageCount = state.vantagensSelecionadas.size
            }
            is AdvancementAction.IncreaseAttribute -> {
                val stack = state.paCostStackPorAtributo[lastAction.attributeName]
                if (stack != null && stack.isNotEmpty()) {
                    stack.removeLast()
                    val current = state.valoresAtributos[lastAction.attributeName]!!.intValue
                    val prevRaw = if (current > 12) current - 1 else current - 2
                    state.valoresAtributos[lastAction.attributeName]!!.intValue = prevRaw
                    val prev = state.comprasAttrPorEstagio[lastAction.stageName] ?: 0
                    if (prev > 0) {
                        state.comprasAttrPorEstagio[lastAction.stageName] = prev - 1
                    }
                    state.paFromProgress = (state.paFromProgress - 1).coerceAtLeast(0)
                    state.recalcularPontosAtributo()
                    if (lastAction.usedLegendaryReservation) {
                        state.legendaryAttrReservations += 1
                    }
                }
            }
            is AdvancementAction.SpendOnSkills -> {
                // Reverte o gasto dos pontos de perícia
                lastAction.skillsIncreased.forEach { skillName ->
                    val skill = listaPericias.firstOrNull { it.nome == skillName }
                    if (skill != null) {
                        state.decreasePericia(skill)
                    }
                }
                // Reverte a concessão dos pontos de perícia
                state.spFromProgress = (state.spFromProgress - 2).coerceAtLeast(0)

                state.rebuildAllPericiaStacks()
            }
            is AdvancementAction.RemoveHindrance -> {
                val hindrance = listaComplicacoes.first { it.id == lastAction.hindranceId }
                when (lastAction.changeType) {
                    HindranceChangeType.RESERVATION -> {
                        state.reservasComplicacaoMaior.remove(hindrance.id)
                    }

                    HindranceChangeType.REDUCE_TO_MINOR -> {
                        val previous = lastAction.previousLevel ?: "Maior"
                        state.complicacoesSelecionadas[hindrance] = previous
                    }

                    HindranceChangeType.REMOVE -> {
                        val previous = lastAction.previousLevel
                        if (previous != null) {
                            state.complicacoesSelecionadas[hindrance] = previous
                        }
                        if (lastAction.usedReservation) {
                            state.reservasComplicacaoMaior[hindrance.id] = true
                        }
                    }
                }
            }
            is AdvancementAction.ReserveLegendaryAttribute -> {
                state.legendaryAttrReservations =
                    (state.legendaryAttrReservations - 1).coerceAtLeast(0)
            }
        }
        // Devolve o ponto de avanço ao "pool"
        state.refundProgressAtStage(stageName, lastAction.progressCost)
        if (state.modoProgressaoAtivo) {
            state.snapshotFrozenSkillIncrements()
        }
        state.updateEmProgressoFlag()
    }
}
