// CriadorViewModel.kt
package com.example.swadebuilder.model

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.adicionarVantagemPorSuper
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.removerVantagemPorSuper
import com.example.swadebuilder.util.keyify

// ---- EFEITOS POSSÍVEIS DE INVESTIMENTO DE SUPER ----
sealed class PowerEffect {
    data class SuperAtributo(val attrKey: String, val steps: Int) : PowerEffect()
    data class SuperPericia(val periciaId: String, val steps: Int) : PowerEffect()
    data class BonusArmadura(val value: Int) : PowerEffect()                 // armorFromPower += value
    data class BonusResistencia(val value: Int) : PowerEffect()              // bonusResFromPower += value
    data class BonusAparar(val value: Int) : PowerEffect()                   // bonusPararFromPower += value
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

    // === NOVO: compatibilidade ao carregar saves antigos ===
    fun normalizeArcanoIdsNoCarregamento() {
        // 1) converte AA base + choice em AA específico
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
        grandesResponsabilidades: Boolean = false, // ← NOVO
    ) {

        state.modoSupers = modoSupers
        state.modoSuperequip = modoSupers
        state.modoSuperComplicacoes = modoSupers
        state.grandesResponsabilidades = grandesResponsabilidades
        state.modoSuperComplicacoes = modoSupers

        state.idAtual = null
        state.nomePersonagem = ""
        state.anotacoes = ""              // ← **ESSENCIAL** pra não vazar texto de outro personagem

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

        state.equipamentosComprados.clear()

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
        state.modoSuperequip = salvo.modoSuperequip
        state.modoSuperComplicacoes = salvo.modoSuperComplicacoes

        // Identidade, nome e anotações
        state.idAtual        = salvo.id
        state.nomePersonagem = salvo.nome
        state.anotacoes      = salvo.anotacoes      // ← recupera as anotações salvas

        // 1) Atributos e perícias
        state.valoresAtributos.forEach { (key, holder) ->
            holder.intValue = salvo.atributos[key] ?: 4
        }
        val desiredPericias: Map<Pericia, Int> = listaPericias.associateWith { per ->
            salvo.pericias[per.nome] ?: state.rawTotal(per)
        }
        state.rebuildPericias(desiredPericias)

        // 2) Ancestralidade e flags gerais
        state.maisPontosPericias = salvo.maisPontosPericias
        state.cartaSelvagem      = salvo.cartaSelvagem
        state.heroisSemArmadura  = salvo.heroisSemArmadura
        state.ancestralidade     = salvo.ancestralidade
        state.aplicarAncestralidade(salvo.ancestralidade)

        // 3) Vantagens — prioriza ID; fallback por nome (case-insensitive)
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

        // 4) Complicações — já são IDs no formato atual
        state.complicacoesSelecionadas.clear()
        salvo.complicacoes.forEach { compId ->
            listaComplicacoes.find { it.id == compId }?.let { comp ->
                // Por default, restaura como “Menor”
                state.complicacoesSelecionadas[comp] = "Menor"
            }
        }

        // 5) Equipamentos — busca em BÁSICO + SUPER (corrige sumiço pós-load)
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

        // 6) Poderes arcanos (slots por AA)
        state.poderSlotsPorArcano.clear()
        salvo.poderes.forEach { (arcano, poderesLista) ->
            val capacidade = arcanoInfo[arcano]?.first ?: 0
            state.poderSlotsPorArcano[arcano] = mutableStateListOf<String?>().apply {
                repeat(capacidade) { idx -> add(poderesLista.getOrNull(idx)) }
            }
        }

        // 8) Dinheiro, pontos, especializações
        state.pontosVantagem = salvo.pontosRestantes
        state.dinheiro       = salvo.dinheiro

        state.usarEspecializacoesDePericia = salvo.usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()
        state.especializacoesPorPericia.putAll(salvo.especializacoesPorPericia)

        // 9) Recalcular derivados conforme seu fluxo atual
        state.recalcularPontosAtributo()
        state.rebuildAllPericiaStacks()
        normalizeArcanoIdsNoCarregamento()
    }

    // ---------------- SUPERS: VALIDAÇÃO E INVESTIMENTO ----------------
    /**
     * Limite por poder: se for o favorecido, usa limiteFavorecido; senão, limitePorPoderPadrao.
     * Observação: o favorecido é comparado por 'poderId'.
     */
    fun perPowerLimit(poderId: String): Int {
        return if (state.idPoderFavorecido != null && state.idPoderFavorecido == poderId)
            state.limiteFavorecido
        else
            state.limitePorPoderPadrao
    }

    /**
     * Retorna a soma prevista (após compra) de Armadura+Resistência vindas de SUPERS apenas.
     * Obs.: NÃO inclui "Heróis sem Armadura" (bônus global de cenário) e nem armadura de equipamento.
     */
    fun projectedMitigacaoSupersSum(afterArmorFromPower: Int, afterBonusResFromPower: Int): Int {
        return (afterArmorFromPower.coerceAtLeast(0) + afterBonusResFromPower.coerceAtLeast(0))
    }

    /** Cap genérico de atributo final por campanha/racial/estágio. */
    @Suppress("unused_parameter")
    fun atributoCapFinal(attrKey: String): Int = 12

    /** Quantos "steps" de super já foram aplicados a um atributo. */
    @Suppress("unused")
    fun currentAttrSteps(attrKey: String): Int = state.superAtributoIncs[attrKey] ?: 0

    /** Quantos "steps" de super já foram aplicados a uma perícia (por id/keyify). */
    fun currentPericiaSteps(periciaId: String): Int = state.superPericiaIncs[periciaId] ?: 0

    /** Busca o raw final de atributo com supers (para conferência de cap). */
    fun atributoRawComSupers(attrKey: String): Int = state.atributoRawComSupers(attrKey)

    /** Verifica se a perícia é Lutar (para efeitos que impactam Aparar). */
    @Suppress("unused")
    fun periciaIdEhLutar(periciaId: String): Boolean {
        return periciaId.trim().equals("LUTAR", ignoreCase = true) ||
                periciaId.trim().equals("lutar", ignoreCase = true)
    }

    /** Máximo racial/campanha para um atributo (não é alterado por Superatributo). */
    fun maxAtributoRacial(attrKey: String): Int = state.atributoMaxRaw(attrKey)

    /** Verifica se o personagem possui a vantagem “Atributo Lendário” aplicável ao atributo alvo. */
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
    // CriadorViewModel.kt — SUBSTITUA a função inteira
    fun canInvestInPower(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestCheck {
        // 1) saldo
        if (custo <= 0) return InvestCheck(false, "Custo inválido.")
        if (state.superPontosDisponiveis < custo) {
            return InvestCheck(false, "Sem saldo: precisa de $custo, tem ${state.superPontosDisponiveis}.")
        }

        // 2) limite por poder (individual)
        val jaGastoNestePoder = state.gastosPorPoder[poderId] ?: 0
        val limiteIndividual = perPowerLimit(poderId)
        if (jaGastoNestePoder + custo > limiteIndividual) {
            val falta = (jaGastoNestePoder + custo) - limiteIndividual
            return InvestCheck(false, "Limite deste poder excedido em $falta (limite: $limiteIndividual).")
        }

        // 2.b) LIMITE COMPARTILHADO: Armadura + Resistência (em pontos INVESTIDOS)
        if (poderId == "sp_armor" || poderId == "sp_res") {
            val gastosArmor = state.gastosPorPoder["sp_armor"] ?: 0
            val gastosRes   = state.gastosPorPoder["sp_res"]   ?: 0
            val totalApos   = gastosArmor + gastosRes + custo
            val limiteShare = state.superLimitePorPoder

            if (totalApos > limiteShare) {
                val restante = (limiteShare - (gastosArmor + gastosRes)).coerceAtLeast(0)
                return InvestCheck(
                    false,
                    "Limite compartilhado de Armadura+Resistência atingido (resta $restante de $limiteShare)."
                )
            }
        }

        // 3) checagens específicas por efeito — **sem alterar estado**
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

            is PowerEffect.BonusArmadura -> {
                val afterArmor = state.armorFromPower + efeito.value
                val afterRes   = state.bonusResFromPower
                val soma       = projectedMitigacaoSupersSum(afterArmor, afterRes)
                if (soma > state.limiteDePoderDaCampanha) {
                    return InvestCheck(false, "Teto combinado de mitigação excedido: $soma > ${state.limiteDePoderDaCampanha}.")
                }
            }

            is PowerEffect.BonusResistencia -> {
                val afterArmor = state.armorFromPower
                val afterRes   = state.bonusResFromPower + efeito.value
                val soma       = projectedMitigacaoSupersSum(afterArmor, afterRes)
                if (soma > state.limiteDePoderDaCampanha) {
                    return InvestCheck(false, "Teto combinado de mitigação excedido: $soma > ${state.limiteDePoderDaCampanha}.")
                }
            }

            is PowerEffect.BonusAparar -> { /* ok */ }

            is PowerEffect.SuperVantagem -> {
                val vant = listaVantagens.firstOrNull { it.id == efeito.vantagemId }
                    ?: return InvestCheck(false, "Vantagem não encontrada: ${efeito.vantagemId}.")
                // valida requisitos ignorando Estágio (simula “Lendário” para não travar pelo estágio)
                val progressoAnterior = state.overrideStageForVantagem
                state.overrideStageForVantagem = "Lendário"
                val permitido = state.podeSelecionar(vant)
                state.overrideStageForVantagem = progressoAnterior
                if (!permitido) return InvestCheck(false, "Requisitos não atendidos para a vantagem (exceto Estágio).")
            }
        }

        return InvestCheck(true, null)
    }

    /**
     * Aplica o investimento no ledger e atualiza derivados.
     * NÃO chama validação; chame canInvestInPower antes.
     */
    // CriadorViewModel.kt — SUBSTITUA a função inteira
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

                // (b) **refletir imediatamente** no atributo visível:
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

            is PowerEffect.SuperVantagem -> {
                listaVantagens.firstOrNull { it.id == efeito.vantagemId }?.let { v ->
                    state.adicionarVantagemPorSuper(v)
                }
            }
        }

        // 3) derivados
        state.rebuildAllPericiaStacks()
        state.recalcularPontosAtributo()

        return InvestResult(true, "Investimento aplicado.")
    }

    /**
     * Reverte um investimento (desfaz no ledger e atualiza derivados).
     * Use o mesmo 'efeito' que foi aplicado, com valores em módulo correspondente.
     */
    // CriadorViewModel.kt — SUBSTITUA a função inteira
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

            is PowerEffect.SuperVantagem -> {
                listaVantagens.firstOrNull { it.id == efeito.vantagemId }?.let { v ->
                    state.removerVantagemPorSuper(v)
                }
            }
        }

        state.rebuildAllPericiaStacks()
        state.recalcularPontosAtributo()

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

    fun iniciarFaseProgresso() {
        // Só permite iniciar progresso quando terminou supers
        if (state.superPontosTotais > 0 && state.superPontosDisponiveis == 0) {
            state.emProgresso = true
        }
    }
}
