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
 *
 * "Exclusivo desta raça": id que só aparece nas habilidades de UMA raça em todo
 * `ancestralidades.json` (ver [calcularIdsExclusivos]) — normalmente um traço bem específico,
 * sem equivalente genérico no livro de criação (ex.: "Magia Gnômica" do Gnomo, "Carismático"
 * dos Transmorfos). Isso é DIFERENTE do campo `RacialAbility.invisivel`, que no código hoje
 * marca algo mais estreito — uma entrada sintética escondida da UI porque já aparece em outro
 * lugar (ex.: um `ATTRIBUTE_BOOST` espelhando um bônus já mostrado na aba de Atributos) ou um
 * ajuste de orçamento sem narrativa própria — não "traço de balanceamento exclusivo da raça".
 * As duas etiquetas aparecem juntas quando se aplicam, mas não são a mesma coisa.
 */
object RacialTraitAuditFormatter {

    /**
     * Ids usados por exatamente UMA raça em todo o catálogo, mapeados pro nome dessa raça.
     * Puramente estrutural (conta ocorrências em `habilidades[].id` por raça) — não tenta
     * adivinhar se existe lógica hardcoded em outro arquivo Kotlin amarrada a esse id (isso
     * exigiria buscar o id no resto do código-fonte, o que este objeto não faz).
     *
     * Ignora de propósito toda habilidade `category="racial_hindrance"`/`"racial_edge"`
     * (Vantagem/Complicação CONCEDIDA, não um traço em si — ver a doc da classe): esse tipo
     * de entrada sempre aponta pra um item JÁ existente no catálogo geral de Vantagens/
     * Complicações, então nunca é "traço sem equivalente genérico", mesmo quando só uma raça
     * hoje a concede automaticamente. Bug real relatado pelo usuário: Androides "Pacifista"
     * aparecia marcado "exclusivo-desta-raça" só porque nenhuma OUTRA raça concede Pacifista
     * de graça — mas a Complicação Pacifista em si é universal, qualquer personagem de
     * qualquer raça pode escolhê-la na aba Complicações. O `id` de uma entrada
     * racial_hindrance/racial_edge é só o marcador interno do mecanismo de concessão (ver
     * `RacialTraitAuditFormatter.formatarUm`), nunca uma alegação sobre quantas raças
     * compartilham o CONTEÚDO concedido.
     */
    fun calcularIdsExclusivos(todasAsRacas: List<RacialModifier>): Map<String, String> {
        val porId = mutableMapOf<String, MutableSet<String>>()
        todasAsRacas.forEach { raca ->
            raca.habilidades.forEach { hab ->
                if (hab.category == "racial_hindrance" || hab.category == "racial_edge") return@forEach
                val chave = hab.id?.keyify() ?: return@forEach
                porId.getOrPut(chave) { mutableSetOf() }.add(raca.nome)
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
        // Id CRU (hab.id, não resolvedTraitId()) pra achar LABEL/catálogo oficial: um traço
        // migrado pro par genérico ATTRIBUTE_BOOST/SKILL_BOOST (traitId parametrizado +
        // targetRef, id mantido só pra identidade/auditoria — ver migração de
        // Atributo/Perícia Aumentada) tem LABEL/entrada cadastrados sob o id ORIGINAL
        // ("SENTIDOS_AGUCADOS", "CAES_DE_GUARDA" etc.), não sob "ATTRIBUTE_BOOST"/
        // "SKILL_BOOST" — buscar por `chave` (resolvida) aqui perdia esse match e a linha de
        // auditoria caía no ramo "sem LABEL nem catálogo" mesmo com um label real cadastrado.
        // `chave` continua sendo o que decide o EFEITO mecânico (efeitoDe já sabe interpretar
        // o par genérico via targetRef/value) e o que aparece no cabeçalho (idBruto).
        val chaveLabel = (hab.id?.takeIf { it.isNotBlank() } ?: idBruto).keyify()
        val categoria = hab.category
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

        val entradaCatalogo = catalogoPorId[chaveLabel]
        val label = RacialTraitPointCatalog.LABEL[chaveLabel]
        val efeito = RacialTraitPointCatalog.efeitoDe(chave, hab.targetRef, hab.value)
        // Custo calibrado no catálogo interno (RacialTraitPointCatalog.CUSTOS) sem LABEL/
        // catálogo oficial: o padrão de um traço bem específico de uma raça, sem equivalente
        // genérico no livro de criação — não confundir com "sem definição nenhuma". Ver
        // RacialTraitPointCatalog.kt: "sem equivalente oficial... o custo é julgamento próprio
        // calibrado na mesma escala do catálogo oficial".
        val custoCatalogado = RacialTraitPointCatalog.CUSTOS[chaveLabel]

        val definicao = when {
            entradaCatalogo != null ->
                "${entradaCatalogo.nome}: ${entradaCatalogo.descricao}"
            !label.isNullOrBlank() && efeito != RacialTraitEffect.Nenhum ->
                "$label — ${formatEfeito(efeito)} (sem entrada em basico_habilidades_raciais.json, só em RacialTraitPointCatalog)"
            !label.isNullOrBlank() ->
                "$label (sem entrada em basico_habilidades_raciais.json, só em RacialTraitPointCatalog)"
            efeito != RacialTraitEffect.Nenhum ->
                "${formatEfeito(efeito)} (sem LABEL nem catálogo — só o efeito mecânico bruto)"
            custoCatalogado != null ->
                "Traço específico desta raça, sem nome genérico reaproveitável — custo calibrado no catálogo interno (${sinal(custoCatalogado)} pts), sem equivalente direto no livro de criação. Nome de exibição normal: \"${hab.nome}\""
            else ->
                "⚠ SEM CATÁLOGO, CUSTOS nem efeito mecânico — possível sujeira de hardcode fora de RacialTraitPointCatalog (nome de exibição normal seria \"${hab.nome}\")"
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
        is RacialTraitEffect.ChiReserveBonus -> "Reserva de Chi ${sinal(efeito.valor)}"
        RacialTraitEffect.Nenhum -> "sem efeito numérico cadastrado"
    }

    private fun sinal(valor: Int): String = if (valor >= 0) "+$valor" else "$valor"
}
