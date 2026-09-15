package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.usecase.ValidateCustomCategoryPrerequisiteUseCase
import com.example.swadebuilder.model.usecase.ValidatePrerequisiteUseCase
import com.example.swadebuilder.model.usecase.ValidateRequirementsUseCase
import com.example.swadebuilder.model.usecase.ValidateScenarioRulesUseCase
import com.example.swadebuilder.util.keyify

/**
 * Validates prerequisites for selecting Advantages (Edges) durante o fluxo de Progresso (XP) —
 * usado só por ProgressosDialog.strictRequirementsOk(). Delega pras mesmas classes de
 * validação usadas na criação de personagem (ValidateScenarioRulesUseCase/
 * ValidateRequirementsUseCase/ValidatePrerequisiteUseCase/
 * ValidateCustomCategoryPrerequisiteUseCase) sempre que a regra já tem uma "fonte única de
 * verdade" lá, em vez de reimplementá-la aqui de novo — reimplementar já causou divergência
 * real mais de uma vez (auditoria Rodada 8: Ressuscitado/Atormentado verificado com um id que
 * não existe em nenhuma Vantagem, sempre bloqueando; Especialista/perícias mínimas ignorando a
 * substituição de Jutsu da Arte da Guerra; bônus de Liderança do Samurai ausente;
 * grupoMinimo/gruposAlternativos e as regras de cenário do Crystal Heart nunca checados aqui).
 * O que sobra abaixo são as regras específicas desta tela (limite "uma vez por Estágio" e
 * afins, que dependem do Estágio sendo comprado) e os itens sem equivalente de criação.
 */
object RequirementValidator {

    private val validateScenarioRulesUseCase = ValidateScenarioRulesUseCase()
    private val validateRequirementsUseCase = ValidateRequirementsUseCase()
    private val validatePrerequisiteUseCase = ValidatePrerequisiteUseCase()
    private val validateCustomCategoryPrerequisiteUseCase = ValidateCustomCategoryPrerequisiteUseCase()

    fun canSelect(v: Vantagem, state: CriadorState): Boolean {
        val key = v.nome.keyify()

        // 0) Regras de cenário (Crystal Heart: lista de proibidos + só Antecedente Arcano
        // Canalizar Cristal + só Vantagens de Poder do próprio cenário; Cidade do Sol a Vapor:
        // exclusividade do Antecedente Arcano Demônio; Fantasia: bloqueio de "Mago";
        // Pathfinder: Antecedentes Arcanos substituídos) — mesma fonte da criação. Faltava
        // por completo aqui; só a parte "só Canalizar Cristal" tinha uma cópia local (removida
        // abaixo, redundante com esta chamada).
        if (!validateScenarioRulesUseCase.execute(
                ValidateScenarioRulesUseCase.Input(
                    vantagem = v,
                    ancestralidade = state.ancestralidade,
                    compendioCrystalHeartAtivo = state.compendioCrystalHeartAtivo,
                    compendioFantasiaAtivo = state.compendioFantasiaAtivo,
                    compendioPathfinderAtivo = state.compendioPathfinderAtivo
                )
            )) return false

        // 1) Regra especial: O MELHOR QUE HÁ
        if (v.id == Constants.ID_THE_BEST_THERE_IS) {
            if (state.emProgresso) return false
            if (state.superInvestments.isEmpty()) return false
        }

        // 1a) Regra especial: CAVALEIRO (Fantasia) — exige Obrigação (Maior)
        if (v.id == "cavaleiro") {
            val hasObligation = state.complicacoesSelecionadas.entries.any { (comp, grau) ->
                comp.id == "obrigacao" && grau == "Maior"
            }
            if (!hasObligation) return false
        }

        // 1b) Regra especial: ASSASSINO IMPIEDOSO (Deadlands/Wiseguys) — exige Sem Escrúpulos
        // (Maior). Faltava por completo aqui (só existia pro fluxo de criação).
        if (v.id == "assassino_impiedoso") {
            val hasSemEscrupulosMaior = state.complicacoesSelecionadas.entries.any { (comp, grau) ->
                comp.id == "sem_escrupulos" && grau == "Maior"
            }
            if (!hasSemEscrupulosMaior) return false
        }

        // 2) "Uma vez por Estágio" (Pontos de Poder, Pontos de Chi, Presa, Poder do Sangue,
        // Vontade Sombria etc.) — SEM acumular Estágios pulados: teto de 1 por Estágio,
        // olhando só o Estágio atual (nunca uma soma cumulativa de Estágios anteriores),
        // exceto Pontos de Poder no Lendário, que não tem teto (mas só vale 2 em vez de 5
        // a partir da 2ª compra lá — ver CriadorState.selecionarPontosDePoder). Duplicado de
        // CriadorState/ValidatePowerPointsLimitUseCase aqui porque esta tela é quem sabe qual
        // Estágio está sendo comprado agora, ao contrário do fluxo de criação.
        if (v.nome.contains(Constants.EDGE_POWER_POINTS, ignoreCase = true)) {
            val feitasNoEstagio = state.comprasPpPorEstagio[state.estagioAtual().nome] ?: 0
            if (feitasNoEstagio >= state.maxComprasPpNesteEstagio()) return false
        } else if (v.limiteCompra == "uma_vez_por_estagio") {
            val feitasNoEstagio = state.comprasEstagioPorVantagem[v.id]?.get(state.estagioAtual().nome) ?: 0
            if (feitasNoEstagio >= 1) return false
        }

        // 2a) Vantagens exclusivas de Ressuscitado (categoria ATORMENTADO) exigem ter a
        // Vantagem-base "Atormentado" primeiro. O id verificado aqui estava errado
        // (Constants.ID_RESSUSCITADO = "ressuscitado", que não corresponde a nenhuma
        // Vantagem do catálogo) — isso fazia esta checagem bloquear SEMPRE qualquer
        // Vantagem ATORMENTADO durante Progresso, mesmo com "Atormentado" já selecionada.
        if (v.categoria == Categoria.ATORMENTADO) {
            val temAtormentado = state.vantagensSelecionadas.any { it.id == "atormentado" }
            if (!temAtormentado) return false
        }

        // 3) Antecedente Arcano e multi-arcano (a exclusividade do Crystal Heart pra só
        // Canalizar Cristal já foi resolvida no item 0, acima — não precisa mais de checagem
        // própria aqui).
        if (key.startsWith(Constants.EDGE_ARCANE_BACKGROUND)) {
            if (!state.permiteMultiplosAntecedentesArcanos) {
                val anyArcano = state.vantagensSelecionadas.any { it.nome.keyify().startsWith(Constants.EDGE_ARCANE_BACKGROUND) }
                if (anyArcano && state.vantagensSelecionadas.none { it.nome.keyify() == key }) {
                    return false
                }
            } else {
                val jaTemMesmoId = state.vantagensSelecionadas.any { it.id == v.id }
                if (jaTemMesmoId) return false
                if (v.id == Constants.ID_AA_PREFIX && v.choice != null) {
                    val jaTemMesmaChoice = state.vantagensSelecionadas.any {
                        it.id == Constants.ID_AA_PREFIX && it.choice?.keyify() == v.choice?.keyify()
                    }
                    if (jaTemMesmaChoice) return false
                }
            }
        }

        // 4) PROFISSIONAL / ESPECIALISTA
        if (v.id == Constants.ID_PROFISSIONAL || v.id == Constants.ID_ESPECIALISTA) {
            val choiceSeguro = v.choice

            if (v.requiresChoice && choiceSeguro != null) {
                val already = state.vantagensSelecionadas.any {
                    it.id == v.id &&
                            it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (already) return false
            }

            if (v.id == Constants.ID_ESPECIALISTA && choiceSeguro != null) {
                val profExist = state.vantagensSelecionadas.any {
                    it.id == Constants.ID_PROFISSIONAL && it.choice?.keyify() == choiceSeguro.keyify()
                }
                if (!profExist) return false
            }

            if (choiceSeguro == null) {
                val anyMaxAttr = state.listaAtributos.any { a ->
                    state.valoresAtributos[a]!!.intValue == state.atributoMaxRaw(a)
                }
                val anyMaxPer = state.periciasComIdiomas().any { p ->
                    state.rawTotal(p) == state.periciaCapRaw(p)
                }
                return anyMaxAttr || anyMaxPer
            }

            val choiceKey = choiceSeguro.keyify()
            return if (state.listaAtributos.contains(choiceKey)) {
                state.valoresAtributos[choiceKey]!!.intValue == state.atributoMaxRaw(choiceKey)
            } else {
                // getBestPericia, não mapaPericias direto: na Arte da Guerra, um requisito de
                // "Lutar" pode estar satisfeito pela melhor categoria de Jutsu do personagem,
                // não só pelo slot base "Lutar" (a versão anterior usava mapaPericias direto e
                // ignorava isso, bloqueando Especialista/Profissional em Lutar indevidamente).
                val per = state.getBestPericia(choiceKey) ?: return false
                state.rawTotal(per) == state.periciaCapRaw(per)
            }
        }

        // 5) Estágio mínimo — respeita Nasce um Herói (exceto no Lendário — a regra opcional
        // nunca libera esse Estágio) e o bônus de Liderança do Samurai da Arte da Guerra
        // (Conhecimento de Batalha d8+ dispensa o Estágio mínimo pra Vantagens de
        // Liderança); esse bônus do Samurai estava ausente aqui antes, bloqueando essas
        // compras por Estágio durante Progresso mesmo quando o personagem já tinha o direito.
        val ehLendario = listaDeEstagios.lastOrNull()?.nome?.equals(v.requisitos.estagio, ignoreCase = true) == true
        val ignorarEstagioPorNasce = (state.nasceUmHeroi && !state.emProgresso && state.pvFromXpOutstanding == 0 && !ehLendario)
        val ignorarEstagioPorSamurai = state.compendioArteDaGuerraAtivo &&
            state.tropoSelecionado?.id == "tropo_samurai" &&
            v.categoria == Categoria.LIDERANCA &&
            state.getBestPericia("Conhecimento de Batalha")?.let { state.rawTotal(it) >= 8 } == true
        if (!ignorarEstagioPorNasce && !ignorarEstagioPorSamurai) {
            val estagioRequerido = listaDeEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
            if (estagioRequerido != null) {
                val estagioAtual = state.overrideStageForVantagem?.let { stageName ->
                    listaDeEstagios.firstOrNull { it.nome.equals(stageName, ignoreCase = true) }
                } ?: state.estagioAtual()

                if (listaDeEstagios.indexOf(estagioAtual) < listaDeEstagios.indexOf(estagioRequerido)) {
                    return false
                }
            }
        }

        // 6) Pré-requisitos: lista fixa de vantagens/complicações prévias (E), "pelo menos N
        // destas opções" (grupoMinimo — ex.: Bando de Guerra) e "isto OU aquilo"
        // (gruposAlternativos — ex.: Antecedente Arcano OU Poderes Místicos; Pontos de Poder;
        // Drenar a Alma). Delega pra ValidatePrerequisiteUseCase, a mesma classe da criação:
        // até a Rodada 7 esta função reimplementava só a lista fixa, sem grupoMinimo nem
        // gruposAlternativos — qualquer Vantagem usando um dos dois nunca tinha esse
        // pré-requisito checado aqui.
        if (!validatePrerequisiteUseCase.execute(
                ValidatePrerequisiteUseCase.Input(
                    vantagem = v,
                    vantagensSelecionadas = state.vantagensSelecionadas,
                    complicacoesSelecionadas = state.complicacoesSelecionadas.keys,
                    pericias = state.periciasComIdiomas(),
                    rawTotalPericia = { state.rawTotal(it) },
                    getBestPericia = { state.getBestPericia(it) },
                    valoresAtributos = state.valoresAtributos.mapValues { it.value.intValue }
                )
            )) return false

        // 6b) Pré-requisito por Categoria Customizada (ex.: qualquer Vantagem de "Pacto
        // Menor" libera "Pacto Maior", pra campanhas próprias) — ausente aqui antes.
        if (!validateCustomCategoryPrerequisiteUseCase.execute(v, state.vantagensSelecionadas)) return false

        // 7) Limite de Compra genérico (maxSelections) — Pontos de Poder e as demais
        // "uma vez por Estágio" já foram checadas no item 2 (e teriam retornado false antes
        // de chegar aqui); esse ramo só cobre o `limite_compra` comum (ex.: "uma_vez").
        if (v.limiteCompra != "infinito" && v.limiteCompra != "uma_vez_por_estagio" &&
            !v.nome.contains(Constants.EDGE_POWER_POINTS, ignoreCase = true) && v.maxSelections > 0
        ) {
            val ja = state.vantagensSelecionadas.count { it.id == v.id }
            if (ja >= v.maxSelections) return false
        }

        // 8) Evita repetir a MESMA choice em vantagens com escolha
        val choiceSeguro2 = v.choice
        if (v.requiresChoice && choiceSeguro2 != null) {
            val repetida = state.vantagensSelecionadas.any {
                it.id == v.id && it.choice == choiceSeguro2
            }
            if (repetida) return false
        }

        // 9) Estágio alternativo (tabela nivelParaEstagio) — usa o Progresso do Estágio em
        // compra retroativa (overrideStageForVantagem) quando presente, senão o Progresso
        // atual; equivalente a CriadorState.effectiveProgressoParaVantagens() (privada lá).
        nivelParaEstagio[v.requisitos.estagio]?.let { estReqObj2 ->
            val stName = state.overrideStageForVantagem ?: ""
            val prog = if (stName.isNotEmpty()) {
                listaDeEstagios.firstOrNull { it.nome.equals(stName, ignoreCase = true) }?.minProgress ?: state.progresso
            } else state.progresso

            if (estReqObj2.minProgress > prog) return false
        }

        // 10-13) Atributos mínimos, perícias mínimas (obrigatórias e opcionais — respeitando
        // qual opção foi de fato escolhida em Vantagens vinculadas, ex.: Arma Predileta/
        // Atirador/Tiro Mortal), Carta Selvagem, Tags Raciais e Template Monstruoso — delega
        // pra ValidateRequirementsUseCase, a mesma classe da criação. A versão anterior
        // reimplementava isso à mão usando mapaPericias direto (ignorando a substituição de
        // Jutsu da Arte da Guerra — mesmo problema do item 4), sem a checagem de qual opção
        // foi escolhida em perícia mínima opcional vinculada, e sem checar Tags
        // Raciais/Template Monstruoso (que nem existiam aqui).
        if (!validateRequirementsUseCase.execute(
                ValidateRequirementsUseCase.Input(
                    vantagem = v,
                    valoresAtributos = state.valoresAtributos.mapValues { it.value.intValue },
                    pericias = state.periciasComIdiomas(),
                    rawTotalPericia = { state.rawTotal(it) },
                    ancestralidadeDef = state.currentAncestryDef,
                    tipoMonstroSelecionado = state.tipoMonstroSelecionado,
                    cartaSelvagem = state.cartaSelvagem,
                    getBestPericia = { state.getBestPericia(it) }
                )
            )) return false

        // 13b) Tiro Duplo Aprimorado — exige Tiro Duplo com a perícia associada em d10+
        if (v.id == "tiro_duplo_aprimorado") {
            val base = state.vantagensSelecionadas.firstOrNull { it.id == "tiro_duplo" }
            if (base == null) return false
            val choice = base.choice
            if (choice.isNullOrBlank()) return false
            val skill = state.getBestPericia(choice) ?: return false
            if (state.rawTotal(skill) < 10) return false
        }

        // 14) Conflitos com complicações
        val compsConfl = IncompatibilityRules.complicacoesIncompativeisCom(v.id)
        return state.complicacoesSelecionadas.keys.none { it.id in compsConfl }
    }
}
