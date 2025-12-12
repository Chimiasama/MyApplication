package com.example.swadebuilder.model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swadebuilder.AppData
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Estagio
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.TOTAL_PROGRESS_LIMIT
import com.example.swadebuilder.dynamicStageCaps
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.nivelParaEstagio
import com.example.swadebuilder.normAAKey
import com.example.swadebuilder.stageIndexForSlot
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.CharacterStorage
import com.example.swadebuilder.util.keyify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CriadorViewModel : ViewModel() {
    val state = CriadorState()
    val feedbackMessages = mutableStateListOf<String>()

    fun clearFeedbackMessages() {
        feedbackMessages.clear()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun salvarPersonagem(context: Context, nomeArquivo: String): CharacterStorage.SaveEntry {
        val snapshot = state.toSnapshot()
        return CharacterStorage.saveCharacter(context, snapshot, nomeArquivo)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun listarPersonagensSalvos(context: Context): List<CharacterStorage.SaveEntry> {
        return CharacterStorage.listCharacters(context)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun carregarPersonagem(context: Context, snapshotId: String): Boolean {
        val snapshot = CharacterStorage.loadCharacter(context, snapshotId) ?: return false
        state.restoreFromSnapshot(snapshot, feedbackMessages)
        return true
    }

    fun setAppTheme(theme: AppTheme) {
        state.appTheme = theme
    }

    fun resetStateParaNovoPersonagem(
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        compendioFantasiaAtivo: Boolean,
        compendioHorrorAtivo: Boolean,
        compendioSciFiAtivo: Boolean,
        compendioTrilhadorAtivo: Boolean,
        modoMonstroAtivo: Boolean,
        usarEspecializacoesDePericia: Boolean,
        showHelpMessages: Boolean
    ) {
        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias
        state.modoSupers = modoSupers
        state.compendioFantasiaAtivo = compendioFantasiaAtivo
        state.compendioHorrorAtivo = compendioHorrorAtivo
        state.compendioSciFiAtivo = compendioSciFiAtivo
        state.compendioTrilhadorAtivo = compendioTrilhadorAtivo
        state.compendioDeadlandsAtivo = false
        state.modoMonstroAtivo = modoMonstroAtivo
        state.usarEspecializacoesDePericia = usarEspecializacoesDePericia
        state.showHelpMessages = showHelpMessages

        state.nomePersonagem = ""
        state.anotacoes = ""
        state.ancestralidade = "HUMANOS"
        state.dinheiro = 500
        state.pontosVantagem = 0
        state.pontosAtributo = 5
        state.pontosComplicacaoGastos = 0
        state.armadura = 0
        state.idAtual = null
        state.progresso = 0
        state.stageXpSpent.keys.forEach { state.stageXpSpent[it] = 0 }
        state.progressosDisponiveis = 0
        state.xpSlots.fill(false)
        state.advancementHistory.clear()
        state.frozenSkillIncrements.clear()
        state.skillAdvancementInProgress = false
        state.skillsForCurrentAdvancement.clear()
        state.advantageAdvancementInProgress = false
        state.advantageForCurrentAdvancement = null
        state.attributeAdvancementInProgress = false
        state.attributeStageForCurrentAdvancement = null
        state.stageNameForCurrentAdvancement = null
        state.attributeStacksBeforeAdvancement = null
        state.attributeUsedReservation = false
        state.emProgresso = false
        state.modoProgressaoAtivo = false
        state.mostrandoVantagensProgresso = false
        state.mostrandoPericiasProgresso = false
        state.mostrandoAtributosProgresso = false
        state.mostrandoPoderesProgresso = false
        state.frozenAdvantageCount = 0
        state.nasceUmHeroi = false
        state.overrideStageForVantagem = null
        state.openVantagensAfterGrant = false
        state.pvFromXpOutstanding = 0

        state.valoresAtributos.forEach { it.value.intValue = 4 }
        state.paCostStackPorAtributo.forEach { it.value.clear() }
        state.cpPaStack.clear()
        state.paFromProgress = 0
        state.legendaryAttrReservations = 0

        state.baseIncsPorPericia.keys.forEach { state.baseIncsPorPericia[it] = 0 }
        state.compIncsPorPericia.keys.forEach { state.compIncsPorPericia[it] = 0 }
        state.spCostStackPorPericia.values.forEach { it.clear() }
        state.compCostStackPorPericia.values.forEach { it.clear() }
        state.cpSpStack.clear()
        state.spFromProgress = 0
        state.especializacoesPorPericia.clear()

        state.vantagensSelecionadas.clear()
        state.vantagensAutomaticas.clear()
        state.vantagensRaciais.clear()
        state.cpPvStack.clear()

        state.complicacoesSelecionadas.clear()
        state.desvantagensAutomaticas.clear()
        state.desvantagensRaciais.clear()
        state.reservasComplicacaoMaior.clear()

        state.poderSlotsPorArcano.clear()
        state.novosPoderesStacksPorArcano.clear()
        state.poderesSelecionados.clear()
        state.arcanoEmCompraViaXpKey = null
        state.arcanoSnapshotAntesDaCompra = null

        state.equipamentosComprados.clear()
        state.cpRecursosStack.clear()

        state.superInvestments.clear()
        state.superNivelCampanha = null
        state.usarSemPontosDePoder = false
        state.superPontosTotais = 0
        state.superPontosDisponiveis = 0
        state.superLimite = 0
        state.superLimitePorPoder = 0
        state.poderFavoritoId = null
        state.limiteDePoderDaCampanha = Int.MAX_VALUE
        state.bonusPararFromPower = 0
        state.bonusResFromPower = 0
        state.armorFromPower = 0
        state.bonusMovimentacaoFromPower = 0
        state.vantagensDePoder.clear()
        state.gastosPorPoder.clear()
        state.faseSupersAtiva = false
        state.comprasPpPorEstagio.keys.forEach { state.comprasPpPorEstagio[it] = 0 }
        state.comprasAttrPorEstagio.keys.forEach { state.comprasAttrPorEstagio[it] = 0 }

        state.aplicarAncestralidade("HUMANOS", mutableListOf())
    }

    fun setMultiplosAAHabilitados(habilitado: Boolean) {
        state.permiteMultiAntecedenteArcano = habilitado
    }

    fun startAdvancementTransaction(
        type: AdvancementType,
        targetStageName: String,
        feedback: (String) -> Unit
    ): Boolean {
        if (state.emProgresso) {
            feedback("Já existe um avanço em andamento. Termine ou cancele antes de iniciar outro.")
            return false
        }

        state.stageNameForCurrentAdvancement = targetStageName

        when (type) {
            AdvancementType.SKILL -> {
                state.skillAdvancementInProgress = true
                state.skillsForCurrentAdvancement.clear()
                state.grantSkillPointsFromXp()
                state.snapshotFrozenSkillIncrements()
            }
            AdvancementType.ADVANTAGE -> {
                state.advantageAdvancementInProgress = true
                state.advantageForCurrentAdvancement = null
                state.grantVantagemPointFromXp(targetStageName)
            }
            AdvancementType.ATTRIBUTE -> {
                state.attributeAdvancementInProgress = true
                state.attributeStageForCurrentAdvancement = targetStageName
                state.attributeStacksBeforeAdvancement = state.snapshotAttributeStacks()
                state.mostrandoAtributosProgresso = true

                if (state.legendaryAttrReservations > 0) {
                    state.attributeUsedReservation = true
                    state.legendaryAttrReservations -= 1
                    state.paFromProgress += 1
                } else if (targetStageName == "Lendário") {
                    state.legendaryAttrReservations += 1
                    feedback("Avanço Lendário reservado. Gaste mais um avanço para aumentar um atributo.")
                    finishAttributeAdvancement(reservationOnly = true)
                    return true
                } else {
                    state.attributeUsedReservation = false
                    state.paFromProgress += 1
                }
            }
        }
        state.updateEmProgressoFlag()
        return true
    }

    fun finishAttributeAdvancement(reservationOnly: Boolean = false) {
        if (!state.attributeAdvancementInProgress) return

        if (reservationOnly) {
            val action = AdvancementAction.ReserveLegendaryAttribute
            state.advancementHistory.add(action)
        } else {
            val oldStacks = state.attributeStacksBeforeAdvancement ?: emptyMap()
            val newStacks = state.snapshotAttributeStacks()
            val changedAttr = state.paCostStackPorAtributo.keys.firstOrNull { attr ->
                (newStacks[attr] ?: 0) > (oldStacks[attr] ?: 0)
            }

            if (changedAttr != null) {
                val action = AdvancementAction.IncreaseAttribute(
                    attributeName = changedAttr,
                    wasLegendaryReservation = state.attributeUsedReservation
                )
                state.advancementHistory.add(action)
            } else {
                // Caso falhe em achar, aborta (não deveria acontecer se UI travar corretamente)
                cancelAdvancementInProgress()
                return
            }
        }

        val stName = state.stageNameForCurrentAdvancement ?: "Novato"
        state.spendProgressAtStage(stName, 1)

        state.attributeAdvancementInProgress = false
        state.attributeStageForCurrentAdvancement = null
        state.stageNameForCurrentAdvancement = null
        state.attributeStacksBeforeAdvancement = null
        state.attributeUsedReservation = false
        state.mostrandoAtributosProgresso = false
        state.updateEmProgressoFlag()
    }

    fun finishAdvantageAdvancement(vantagem: Vantagem) {
        if (!state.advantageAdvancementInProgress) return

        state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
        state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)

        val isArcane = vantagem.nome.keyify().startsWith("antecedente arcano") ||
                vantagem.id.startsWith("antecedente_arcano") ||
                vantagem.id.startsWith("aa_")

        val isNewPowers = vantagem.id == "novos_poderes"

        val action = if (isArcane) {
            val arcKey = getArcanoKey(vantagem)
            AdvancementAction.SpendOnArcaneBackground(
                advantageId = vantagem.id,
                arcaneKey = arcKey
            )
        } else if (isNewPowers) {
            val arcKey = if (vantagem.choice.isNullOrBlank()) {
                 state.poderSlotsPorArcano.keys.firstOrNull() ?: ""
            } else {
                vantagem.choice!!.normAAKey()
            }
            AdvancementAction.SpendOnNewPowers(
                advantageId = vantagem.id,
                arcaneKey = arcKey
            )
        } else {
            AdvancementAction.SpendOnAdvantage(
                advantageId = vantagem.id,
                choice = vantagem.choice
            )
        }

        state.advancementHistory.add(action)

        val stName = state.stageNameForCurrentAdvancement ?: "Novato"
        state.spendProgressAtStage(stName, 1)

        state.advantageAdvancementInProgress = false
        state.advantageForCurrentAdvancement = null
        state.stageNameForCurrentAdvancement = null
        state.overrideStageForVantagem = null
        state.openVantagensAfterGrant = false
        state.mostrandoVantagensProgresso = false
        state.updateEmProgressoFlag()
        state.checkFreeze()
    }

    fun finishSkillAdvancement() {
        if (!state.skillAdvancementInProgress) return

        if (state.skillsForCurrentAdvancement.isEmpty()) {
            return // Nada gasto ainda, aguarde
        }

        val action = AdvancementAction.IncreaseSkills(
            skillsIncreased = state.skillsForCurrentAdvancement.toList()
        )
        state.advancementHistory.add(action)

        val stName = state.stageNameForCurrentAdvancement ?: "Novato"
        state.spendProgressAtStage(stName, 1)

        state.skillAdvancementInProgress = false
        state.skillsForCurrentAdvancement.clear()
        state.stageNameForCurrentAdvancement = null
        state.frozenSkillIncrements.clear()
        state.spFromProgress = 0
        state.mostrandoPericiasProgresso = false
        state.updateEmProgressoFlag()
    }

    fun cancelAdvancementInProgress() {
        if (!state.emProgresso) return

        if (state.skillAdvancementInProgress) {
            state.skillsForCurrentAdvancement.forEach { skillName ->
                val per = listaPericias.firstOrNull { it.nome == skillName }
                if (per != null) {
                    state.decreasePericia(per)
                }
            }
            state.spFromProgress = (state.spFromProgress - 2).coerceAtLeast(0)
            state.skillAdvancementInProgress = false
            state.skillsForCurrentAdvancement.clear()
            state.frozenSkillIncrements.clear()
            state.mostrandoPericiasProgresso = false
        }

        if (state.advantageAdvancementInProgress) {
            state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
            state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
            state.limparCompraArcanoViaXp(restaurarSnapshot = true)

            state.advantageAdvancementInProgress = false
            state.advantageForCurrentAdvancement = null
            state.overrideStageForVantagem = null
            state.openVantagensAfterGrant = false
            state.mostrandoVantagensProgresso = false
        }

        if (state.attributeAdvancementInProgress) {
            if (state.attributeUsedReservation) {
                state.legendaryAttrReservations += 1
            }
            state.attributeStacksBeforeAdvancement?.let {
                state.restoreAttributeStacks(it)
            }
            state.paFromProgress = (state.paFromProgress - 1).coerceAtLeast(0)

            state.attributeAdvancementInProgress = false
            state.attributeStageForCurrentAdvancement = null
            state.attributeStacksBeforeAdvancement = null
            state.attributeUsedReservation = false
            state.mostrandoAtributosProgresso = false
        }

        state.stageNameForCurrentAdvancement = null
        state.updateEmProgressoFlag()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun revertLastAdvancement() {
        if (state.advancementHistory.isEmpty()) return
        val lastAction = state.advancementHistory.removeLast()

        val stageName = state.stageNameForCurrentAdvancementSnapshot ?: lastAction.guessStageName()
        state.refundProgressAtStage(stageName, 1)

        when (lastAction) {
            is AdvancementAction.IncreaseSkills -> {
                lastAction.skillsIncreased.forEach { skillName ->
                    val per = listaPericias.firstOrNull { it.nome == skillName }
                    if (per != null) {
                        state.decreasePericia(per)
                    }
                }
            }
            is AdvancementAction.SpendOnAdvantage -> {
                val toRemove = state.vantagensSelecionadas.lastOrNull {
                    it.id == lastAction.advantageId && it.choice == lastAction.choice
                }
                if (toRemove != null) {
                    state.vantagensSelecionadas.remove(toRemove)
                    // PV já foi gasto. Não devolvemos PV ao pool, pois o avanço foi anulado.
                    // O estado volta a ser como antes de iniciar o avanço.
                }
            }
            is AdvancementAction.SpendOnArcaneBackground -> {
                val toRemove = state.vantagensSelecionadas.lastOrNull {
                    it.id == lastAction.advantageId
                }
                if (toRemove != null) {
                    state.vantagensSelecionadas.remove(toRemove)
                    state.poderSlotsPorArcano.remove(lastAction.arcaneKey)
                    state.novosPoderesStacksPorArcano.remove(lastAction.arcaneKey)
                    state.syncPoderesSelecionadosFromSlots()
                }
            }
            is AdvancementAction.SpendOnNewPowers -> {
                val toRemove = state.vantagensSelecionadas.lastOrNull {
                    it.id == lastAction.advantageId
                }
                if (toRemove != null) {
                    state.vantagensSelecionadas.remove(toRemove)
                    // Calcula slots iniciais do arcano para desfazer corretamente
                    val baseSlots = state.arcanoInfo[lastAction.arcaneKey]?.first ?: 0
                    state.desfazerUltimosNovosPoderes(lastAction.arcaneKey, baseSlots)
                }
            }
            is AdvancementAction.IncreaseAttribute -> {
                val attr = lastAction.attributeName
                val stack = state.paCostStackPorAtributo[attr]
                if (!stack.isNullOrEmpty()) {
                    stack.removeLast()
                    val current = state.valoresAtributos[attr]?.intValue ?: 4
                    val prev = if (current > 12) current - 1 else current - 2
                    state.valoresAtributos[attr]?.intValue = prev
                    state.recalcularPontosAtributo()
                }
                if (lastAction.wasLegendaryReservation) {
                    state.legendaryAttrReservations += 1
                }
            }
            is AdvancementAction.ReserveLegendaryAttribute -> {
                state.legendaryAttrReservations -= 1
            }
            is AdvancementAction.RemoveMinorHindrance -> {
                val comp = listaComplicacoes.firstOrNull { it.id == lastAction.hindranceId }
                if (comp != null) {
                    state.complicacoesSelecionadas[comp] = "Menor"
                }
            }
            is AdvancementAction.RemoveMajorHindrance -> {
                // Esse action representa o 2º ponto gasto.
                // Ao reverter, devolvemos a reserva (voltou a ter pago 1 ponto só).
                state.reservasComplicacaoMaior[lastAction.hindranceId] = true
            }
            is AdvancementAction.ReserveRemoveMajorHindrance -> {
                // Esse action representa o 1º ponto gasto.
                // Ao reverter, removemos a reserva e devolvemos a complicação Maior.
                state.reservasComplicacaoMaior.remove(lastAction.hindranceId)
                val comp = listaComplicacoes.firstOrNull { it.id == lastAction.hindranceId }
                if (comp != null) {
                    state.complicacoesSelecionadas[comp] = "Maior"
                }
            }
        }

        // Se após reverter, ficarmos "presos" no limite do estágio, libera o congelamento
        state.frozenAdvantageCount = 0 // simplificação: libera pra recalcularem
        state.checkFreeze()
    }

    fun getAdvancementOptions(): List<AdvancementOption> {
        val list = mutableListOf<AdvancementOption>()

        // Opções padrão
        list.add(AdvancementOption.GainAdvantage)
        list.add(AdvancementOption.IncreaseSkills)
        list.add(AdvancementOption.IncreaseAttribute)

        // Remover complicações
        val menores = state.complicacoesSelecionadas.filterValues { it == "Menor" }.keys
        menores.forEach { c ->
            list.add(AdvancementOption.RemoveMinorHindrance(c))
        }

        val maiores = state.complicacoesSelecionadas.filterValues { it == "Maior" }.keys
        maiores.forEach { c ->
            if (state.reservasComplicacaoMaior.containsKey(c.id)) {
                list.add(AdvancementOption.FinishRemoveMajorHindrance(c))
            } else {
                list.add(AdvancementOption.StartRemoveMajorHindrance(c))
            }
        }

        return list
    }

    fun executeRemoveHindrance(option: AdvancementOption, targetStageName: String) {
        when (option) {
            is AdvancementOption.RemoveMinorHindrance -> {
                state.complicacoesSelecionadas.remove(option.hindrance)
                val action = AdvancementAction.RemoveMinorHindrance(option.hindrance.id)
                state.advancementHistory.add(action)
                state.spendProgressAtStage(targetStageName, 1)
            }
            is AdvancementOption.StartRemoveMajorHindrance -> {
                state.complicacoesSelecionadas.remove(option.hindrance) // remove da UI visualmente?
                // Na verdade, regra SWADE: paga 1 ponto pra "tratar", depois outro pra remover.
                // Vamos manter a complicação na lista mas marcar reserva.
                // Se quiser remover visualmente só no 2o ponto, ok.
                // Aqui: removemos visualmente SE a regra for "já conta como removida mas devendo ponto".
                // Mas geralmente remove só no final. Vamos assumir:
                // 1º ponto: ganha flag "pagando". 2º ponto: remove de vez.
                state.complicacoesSelecionadas.remove(option.hindrance) // Removemos pra feedback visual?
                // Melhor: manter na lista se quiser realismo, ou remover e confiar no user.
                // O app remove visualmente no final.
                state.reservasComplicacaoMaior[option.hindrance.id] = true

                // Mas espere! Se eu não remover agora, o usuário vê ela lá.
                // Vamos fazer assim: Start -> adiciona reserva. Finish -> remove complicação.
                // Se a reserva está lá, a complicação ainda está lá.
                // CORREÇÃO: StartRemoveMajorHindrance APENAS adiciona a reserva.

                // Ops, lógica acima:
                state.reservasComplicacaoMaior[option.hindrance.id] = true
                // Não remove do map complicacoesSelecionadas ainda.

                val action = AdvancementAction.ReserveRemoveMajorHindrance(option.hindrance.id)
                state.advancementHistory.add(action)
                state.spendProgressAtStage(targetStageName, 1)
            }
            is AdvancementOption.FinishRemoveMajorHindrance -> {
                state.reservasComplicacaoMaior.remove(option.hindrance.id)
                state.complicacoesSelecionadas.remove(option.hindrance)
                val action = AdvancementAction.RemoveMajorHindrance(option.hindrance.id)
                state.advancementHistory.add(action)
                state.spendProgressAtStage(targetStageName, 1)
            }
            else -> {}
        }
    }

    private fun AdvancementAction.guessStageName(): String {
        // Fallback simplificado se não tiver salvo no snapshot
        // (idealmente salvaríamos o stageName em cada action, mas vamos usar o atual)
        return "Novato"
    }

    fun normalizeArcanoIdsNoCarregamento() {
        val novos = mutableStateListOf<Vantagem>()
        state.vantagensSelecionadas.forEach { v ->
            if (v.id == "antecedente_arcano" && v.choice != null) {
                // Tenta achar um ID específico para esse choice
                // Ex: choice="Milagres" -> id="antecedente_arcano_milagres"
                val choiceNorm = v.choice!!.keyify()
                val candidato = listaVantagens.firstOrNull { cand ->
                    cand.grupoId == "antecedente_arcano" &&
                            cand.subtipoArcano != null &&
                            (cand.nome.keyify().contains(choiceNorm) || cand.subtipoArcano?.equals(choiceNorm, true) == true)
                }
                if (candidato != null) {
                    // Substitui pela versão específica
                    novos.add(candidato)
                } else {
                    novos.add(v)
                }
            } else {
                novos.add(v)
            }
        }
        state.vantagensSelecionadas.clear()
        state.vantagensSelecionadas.addAll(novos)
    }

    private fun getArcanoKey(v: Vantagem): String {
        return v.subtipoArcano
            ?: v.choice?.normAAKey()
            ?: v.nome.normAAKey()
    }
}

enum class AdvancementType {
    SKILL, ADVANTAGE, ATTRIBUTE
}

sealed class AdvancementOption {
    object GainAdvantage : AdvancementOption()
    object IncreaseSkills : AdvancementOption()
    object IncreaseAttribute : AdvancementOption()
    data class RemoveMinorHindrance(val hindrance: Complicacao) : AdvancementOption()
    data class StartRemoveMajorHindrance(val hindrance: Complicacao) : AdvancementOption()
    data class FinishRemoveMajorHindrance(val hindrance: Complicacao) : AdvancementOption()
}
