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
 * `AppPreferences.loadModoAuditoriaIdPuro`/`AncestralidadesSection.kt`.
 */
object RacialTraitAuditFormatter {

    fun calcularIdsExclusivos(todasAsRacas: List<RacialModifier>): Map<String, String> {
        val porId = mutableMapOf<String, MutableSet<String>>()
        todasAsRacas.forEach { raca ->
            raca.habilidades.forEach { hab ->
                val cat = hab.resolvedCategory()
                if (cat == "racial_hindrance" || cat == "racial_edge") return@forEach
                val idBruto = hab.resolvedTraitId()
                val chave = idBruto.keyify()
                if (chave.isBlank()) return@forEach

                // Traços que pertencem ao catálogo oficial das tabelas dos livros (ex.: AQUATICO, CONSTRUTO, ARMADURA)
                // são genéricos da tabela de regras e nunca "exclusivos de uma raça".
                val chaveLabel = (hab.id?.takeIf { it.isNotBlank() } ?: idBruto).keyify()
                if (RacialTraitPointCatalog.ehDoCatalogo(chaveLabel, hab.targetRef)) return@forEach

                val especieChave = raca.especieId ?: raca.id ?: raca.nome
                porId.getOrPut(chave) { mutableSetOf() }.add(especieChave)
            }
        }
        return porId.filterValues { it.size == 1 }.mapValues { it.value.first() }
    }

    fun formatar(
        habilidades: List<RacialAbility>,
        catalogoOficial: List<HabilidadeCriacao>,
        idsExclusivos: Map<String, String> = emptyMap(),
        allVantagens: List<Vantagem> = emptyList()
    ): List<String> {
        val catalogoPorId = catalogoOficial
            .filter { !it.id.isNullOrBlank() }
            .associateBy { it.id!!.keyify() }
        return habilidades.map { formatarUm(it, catalogoPorId, idsExclusivos, allVantagens) }
    }

    private fun formatarUm(
        hab: RacialAbility,
        catalogoPorId: Map<String, HabilidadeCriacao>,
        idsExclusivos: Map<String, String>,
        allVantagens: List<Vantagem>
    ): String {
        val idBruto = hab.resolvedTraitId()
        val chave = idBruto.keyify()
        val chaveLabel = (hab.id?.takeIf { it.isNotBlank() } ?: idBruto).keyify()
        val categoria = hab.resolvedCategory()
        val pontos = hab.resolvedPontos(allVantagens)
        val pontosStr = if (pontos != 0) " · ${if (pontos > 0) "+" else ""}$pontos pts" else ""
        val exclusivoDe = idsExclusivos[chave]

        val cabecalho = buildString {
            append("[id=")
            append(idBruto.ifBlank { "(vazio)" })
            if (!hab.targetRef.isNullOrBlank()) append(" alvo=${hab.targetRef}")
            if (hab.vezes != 1) append(" x${hab.vezes}")
            if (hab.invisivel) append(" invisível(UI)")
            if (!categoria.isNullOrBlank()) append(" categoria=$categoria")
            if (exclusivoDe != null) append(" exclusivo-desta-raça")
            append("]")
        }

        val normNome = hab.nome.keyify().replace("_", " ")
        val normChave = chaveLabel.keyify().replace("_", " ")
        val normAlvo = hab.targetRef?.keyify()?.replace("_", " ").orEmpty()

        if (categoria == "racial_edge" || chave == "GRANTED_EDGE") {
            val alvo = hab.targetRef ?: hab.id ?: hab.nome
            val skinStr = if (normNome.isNotBlank() && normNome != normAlvo && normNome != "GRANTED EDGE") " Skin: \"${hab.nome}\"" else ""
            return "$cabecalho Vantagem Grátis concedida ao personagem: $alvo$skinStr$pontosStr"
        }
        if (categoria == "racial_hindrance" || chave == "RACIAL_HINDRANCE") {
            val alvo = hab.targetRef ?: hab.nome
            val sev = hab.severity?.let { " ($it)" }.orEmpty()
            val skinStr = if (normNome.isNotBlank() && normNome != normAlvo && normNome != "RACIAL HINDRANCE") " Skin: \"${hab.nome}\"" else ""
            return "$cabecalho Complicação concedida ao personagem: $alvo$sev$skinStr$pontosStr"
        }

        val entradaCatalogo = catalogoPorId[chaveLabel]
        val label = RacialTraitPointCatalog.LABEL[chaveLabel]
        val efeito = RacialTraitPointCatalog.efeitoDe(chave, hab.targetRef, hab.value)
        val custoCatalogado = RacialTraitPointCatalog.CUSTOS[chaveLabel]
        val ehCatalogo = RacialTraitPointCatalog.ehDoCatalogo(chaveLabel, hab.targetRef) || entradaCatalogo != null

        val catalogTag = if (ehCatalogo) "[Catálogo Oficial]" else "[Regra Única da Raça / Fora do Catálogo]"

        val normLabel = label?.keyify()?.replace("_", " ").orEmpty()
        val normCatalogoNome = entradaCatalogo?.nome?.keyify()?.replace("_", " ").orEmpty()

        val skinStr = if (
            normNome.isNotBlank() &&
            normNome != normChave &&
            normNome != normAlvo &&
            normNome != normLabel &&
            normNome != normCatalogoNome
        ) " Skin: \"${hab.nome}\"" else ""

        val definicao = when {
            entradaCatalogo != null ->
                "${entradaCatalogo.nome}: ${entradaCatalogo.descricao}"
            !label.isNullOrBlank() && efeito != RacialTraitEffect.Nenhum ->
                "$label — ${formatEfeito(efeito)}"
            !label.isNullOrBlank() ->
                label
            efeito != RacialTraitEffect.Nenhum ->
                formatEfeito(efeito)
            custoCatalogado != null ->
                "Traço específico desta raça, sem nome genérico reaproveitável — custo calibrado no catálogo interno (${sinal(custoCatalogado)} pts)."
            else ->
                "⚠ SEM CATÁLOGO, CUSTOS nem efeito mecânico — possível sujeira fora de RacialTraitPointCatalog"
        }

        return "$cabecalho $catalogTag $definicao$skinStr$pontosStr"
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
        is RacialTraitEffect.ChiReserveBonus -> "Reserva de Chi ${sinal(efeito.valor)}"
        RacialTraitEffect.Nenhum -> "sem efeito numérico cadastrado"
    }

    private fun sinal(valor: Int): String = if (valor >= 0) "+$valor" else "$valor"
}
