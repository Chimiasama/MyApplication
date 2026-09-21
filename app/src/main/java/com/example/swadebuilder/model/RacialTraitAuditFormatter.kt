package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify

/**
 * Formata cada `RacialAbility` de uma raça numa linha de auditoria "crua": id/traitId, o
 * efeito mecânico resolvido (`RacialTraitPointCatalog.efeitoDe`) e a descrição OFICIAL do
 * catálogo de criação de raças (`basico_habilidades_raciais.json`, via `HabilidadeCriacao`) —
 * ignorando de propósito `RacialAbility.nome`/`descricao`/`descricaoLite`, que é onde mora a
 * reskinagem por raça (ex.: Draconianos "Mal-Humorado" reaproveitado como "Arrogante" via
 * `targetRef`, ou uma descrição narrativa reescrita pra combinar com o flavor da raça).
 *
 * Uso: alternar entre esta leitura "id puro" (sem skin) e a leitura normal (com skin, o
 * comportamento de sempre) na tela "Ver detalhes" de Ancestralidades — ver
 * `AppPreferences.loadModoAuditoriaIdPuro`/`AncestralidadesSection.kt`. Serve só pra auditoria
 * de quem mantém o app; não é (e não deve virar) algo visível pro jogador/criador de raça.
 */
object RacialTraitAuditFormatter {

    fun formatar(habilidades: List<RacialAbility>, catalogoOficial: List<HabilidadeCriacao>): List<String> {
        val catalogoPorId = catalogoOficial
            .filter { !it.id.isNullOrBlank() }
            .associateBy { it.id!!.keyify() }
        return habilidades.map { formatarUm(it, catalogoPorId) }
    }

    private fun formatarUm(hab: RacialAbility, catalogoPorId: Map<String, HabilidadeCriacao>): String {
        val idBruto = hab.resolvedTraitId()
        val chave = idBruto.keyify()
        val categoria = hab.category
        val pontos = hab.resolvedPontos()
        val pontosStr = if (pontos != 0) " · ${if (pontos > 0) "+" else ""}$pontos pts" else ""

        val cabecalho = buildString {
            append("[id=")
            append(idBruto.ifBlank { "(vazio)" })
            if (!hab.targetRef.isNullOrBlank()) append(" alvo=${hab.targetRef}")
            if (hab.vezes != 1) append(" x${hab.vezes}")
            if (hab.invisivel) append(" invisível")
            if (!categoria.isNullOrBlank()) append(" categoria=$categoria")
            append("]")
        }

        // Vantagem/Complicação concedida por GRANTED_EDGE/RACIAL_HINDRANCE (ou category
        // racial_edge/racial_hindrance): o conteúdo de verdade está em targetRef/nome, o id
        // em si é só um marcador de mecanismo — mostrar isso explícito em vez de tentar achar
        // "GRANTED_EDGE" no catálogo de traços (não é um traço, é um ponteiro pra Vantagem).
        if (categoria == "racial_edge" || chave == "GRANTED_EDGE") {
            val alvo = hab.targetRef ?: hab.id ?: hab.nome
            return "$cabecalho Vantagem Grátis concedida ao personagem: $alvo$pontosStr"
        }
        if (categoria == "racial_hindrance" || chave == "RACIAL_HINDRANCE") {
            val alvo = hab.targetRef ?: hab.nome
            val sev = hab.severity?.let { " ($it)" }.orEmpty()
            return "$cabecalho Complicação concedida ao personagem: $alvo$sev$pontosStr"
        }

        val entradaCatalogo = catalogoPorId[chave]
        val label = RacialTraitPointCatalog.LABEL[chave]
        val efeito = RacialTraitPointCatalog.efeitoDe(chave, hab.targetRef, hab.value)

        val definicao = when {
            entradaCatalogo != null ->
                "${entradaCatalogo.nome}: ${entradaCatalogo.descricao}"
            !label.isNullOrBlank() && efeito != RacialTraitEffect.Nenhum ->
                "$label — ${formatEfeito(efeito)} (sem entrada em basico_habilidades_raciais.json, só em RacialTraitPointCatalog)"
            !label.isNullOrBlank() ->
                "$label (sem entrada em basico_habilidades_raciais.json, só em RacialTraitPointCatalog)"
            efeito != RacialTraitEffect.Nenhum ->
                "${formatEfeito(efeito)} (sem LABEL nem catálogo — só o efeito mecânico bruto)"
            else ->
                "⚠ SEM CATÁLOGO nem efeito mecânico — possível sujeira de hardcode fora de RacialTraitPointCatalog (nome de exibição normal seria \"${hab.nome}\")"
        }

        return "$cabecalho $definicao$pontosStr"
    }

    private fun formatEfeito(efeito: RacialTraitEffect): String = when (efeito) {
        is RacialTraitEffect.AtributoStep -> "Atributo ${efeito.atributo} ${sinal(efeito.passos)} passo(s)"
        is RacialTraitEffect.PericiaStep -> "Perícia ${efeito.pericia} ${sinal(efeito.passos)} passo(s)"
        is RacialTraitEffect.ResistenciaBonus -> "Resistência ${sinal(efeito.valor)}"
        is RacialTraitEffect.PassoBonus -> "Movimentação ${sinal(efeito.valor)}"
        is RacialTraitEffect.ApararBonus -> "Aparar ${sinal(efeito.valor)}"
        is RacialTraitEffect.TamanhoBonus -> "Tamanho ${sinal(efeito.valor)}${if (efeito.minusculo) " (Diminuto)" else ""}"
        is RacialTraitEffect.ArmaduraBonus -> "Armadura +${efeito.valor}"
        is RacialTraitEffect.Composite -> efeito.efeitos.joinToString(" + ") { formatEfeito(it) }
        is RacialTraitEffect.PericiaPoolBonus -> "Pontos de Perícia ${sinal(efeito.valor)}"
        is RacialTraitEffect.AtributoPoolBonus -> "Pontos de Atributo ${sinal(efeito.valor)}"
        RacialTraitEffect.Nenhum -> "sem efeito numérico cadastrado"
    }

    private fun sinal(valor: Int): String = if (valor >= 0) "+$valor" else "$valor"
}
