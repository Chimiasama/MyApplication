package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryUtilsTest {

    @Test
    fun `buildSummaryLines deduplicates skills`() {
        val listaPericias = listOf(
            Pericia("Atletismo", "AGILIDADE", true),
            Pericia("Atletismo", "AGILIDADE", true), // Duplicate
            Pericia("Lutar", "AGILIDADE", true)
        )
        val listaAncestralidades = emptyList<RacialModifier>()
        val listaTropos = emptyList<com.example.swadebuilder.model.Tropo>()
        val listaComplicacoes = emptyList<com.example.swadebuilder.model.Complicacao>()
        val listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        val mapaAtributosDisplay = listaAtributos.associateWith { it }
        val listaPoderes = emptyList<com.example.swadebuilder.model.Poder>()
        val arcanoInfo = emptyMap<String, Triple<Int, Int, String>>()

        val personagem = MeuPersonagem(
            nome = "Test Char",
            pericias = mapOf("Atletismo" to 4),
            ancestralidade = "Humano",
            celestialAAMilagresDesabilitado = false,
            vantagens = emptyList(),
            complicacoes = emptyList(),
            desvantagensRaciais = emptyList(),
            equipamentos = emptyList(),
            poderes = emptyMap(),
            dinheiro = 500,
            atributos = emptyMap(),
            pontosRestantes = 0
        )

        val lines = buildSummaryLines(
            personagem = personagem,
            allAdvantages = emptyList(),
            listaAncestralidades = listaAncestralidades,
            listaTropos = listaTropos,
            listaComplicacoes = listaComplicacoes,
            listaAtributos = listaAtributos,
            mapaAtributosDisplay = mapaAtributosDisplay,
            listaPericias = listaPericias,
            listaPoderes = listaPoderes,
            arcanoInfo = arcanoInfo
        )

        // Find lines starting with "Atletismo:"
        val atletismoLines = lines.filter { it.startsWith("Atletismo:") }

        // Should be exactly 1
        assertEquals("Should have exactly one line for Atletismo", 1, atletismoLines.size)
        assertEquals("Atletismo: d4", atletismoLines.first())
    }

    @Test
    fun `buildSummaryLines uses avianos ave de rapina traits in racial characteristics`() {
        val listaPericias = listOf(Pericia("Perceber", "ASTUCIA", true))
        val listaAncestralidades = listOf(
            RacialModifier(
                nome = "AVIANOS",
                habilidades = listOf(
                    RacialAbility("Frágil", ""),
                    RacialAbility("Movimentação Reduzida", ""),
                    RacialAbility("Não Sabe Nadar", ""),
                    RacialAbility("Sentidos Aguçados", ""),
                    RacialAbility("Voo", "")
                ),
                origem = "FC",
                especieId = "avianos"
            )
        )
        val listaTropos = emptyList<com.example.swadebuilder.model.Tropo>()
        val listaComplicacoes = listOf(
            com.example.swadebuilder.model.Complicacao(
                id = "habitante_de_gravidade_baixa",
                name = "HABITANTE DE GRAVIDADE ZERO/BAIXA",
                severity = "menor",
                description = "",
                origem = "SCIFI"
            )
        )
        val listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        val mapaAtributosDisplay = listaAtributos.associateWith { it }
        val listaPoderes = emptyList<com.example.swadebuilder.model.Poder>()
        val arcanoInfo = emptyMap<String, Triple<Int, Int, String>>()

        val personagem = MeuPersonagem(
            nome = "Ave",
            pericias = mapOf("Perceber" to 6),
            ancestralidade = "AVIANOS",
            celestialAAMilagresDesabilitado = false,
            vantagens = emptyList(),
            complicacoes = emptyList(),
            desvantagensRaciais = listOf(
                "HABITANTE DE GRAVIDADE ZERO/BAIXA",
                "FORMA ALIENÍGENA",
                "SENTIDOS AGUÇADOS (Olhos de Águia)"
            ),
            equipamentos = emptyList(),
            poderes = emptyMap(),
            dinheiro = 0,
            pontosRestantes = 0,
            atributos = emptyMap(),
            compendioSciFiAtivo = true
        )

        val lines = buildSummaryLines(
            personagem = personagem,
            allAdvantages = emptyList(),
            listaAncestralidades = listaAncestralidades,
            listaTropos = listaTropos,
            listaComplicacoes = listaComplicacoes,
            listaAtributos = listaAtributos,
            mapaAtributosDisplay = mapaAtributosDisplay,
            listaPericias = listaPericias,
            listaPoderes = listaPoderes,
            arcanoInfo = arcanoInfo
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertTrue(racialLine!!.contains("Habitante de Gravidade Zero/Baixa"))
        assertTrue(racialLine.contains("Forma Alienígena"))
        assertFalse(racialLine.contains("Frágil"))
        assertFalse(racialLine.contains("Não Sabe Nadar"))
        assertFalse(lines.any { it.startsWith("Anotações Raciais:") && it.contains("FORMA ALIEN") })
    }

    @Test
    fun `buildSummaryLines preserves raw casing for narrative racial notes`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Dwarf",
                ancestralidade = "ANÕES",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf("Anões Ciber: Combinar com o Mestre 2 pontos em habilidades negativas apropriadas ao cenário."),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioSciFiAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val annotations = lines.firstOrNull { it.startsWith("Anotações Raciais:") }
        assertEquals(
            "Anotações Raciais: Anões Ciber: Combinar com o Mestre 2 pontos em habilidades negativas apropriadas ao cenário.",
            annotations
        )
    }

    @Test
    fun `buildSummaryLines omits desastrado from elfos comunitario racial traits`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Elf",
                ancestralidade = "ELFOS",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf("TRANSTORNO DE SEPARAÇÃO"),
                vantagensRaciais = listOf("COMUNITÁRIO"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioSciFiAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "ELFOS",
                    origem = "FC",
                    opcoes = listOf("Básico", "Comunitário"),
                    habilidades = listOf(
                        RacialAbility("Ágil", ""),
                        RacialAbility("Desastrado", ""),
                        RacialAbility("Visão no Escuro", "")
                    ),
                    especieId = "elfos"
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertFalse(racialLine!!.contains("Desastrado"))
        assertTrue(racialLine.contains("Comunitário"))
    }




    @Test
    fun `buildSummaryLines preserva texto exato da anotacao de possessores energia`() {
        val texto = "Combine com o mestre de jogo para equilibrar com 4 pontos de habilidades negativas que façam sentido no cenário."
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Possessor",
                ancestralidade = "POSSESSORES",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf(texto),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioSciFiAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val annotations = lines.firstOrNull { it.startsWith("Anotações Raciais:") }
        assertEquals("Anotações Raciais: $texto", annotations)
    }


    @Test
    fun `buildSummaryLines mostra anotacao racial de quadroides habilidoso e sensivel maior`() {
        val texto = "Combine com o mestre de jogo para equilibrar com 1 ponto de habilidade negativa que faça sentido ao cenário."
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Quadroide",
                ancestralidade = "QUADROIDES",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf("SENSÍVEL (Maior)", texto),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioSciFiAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = listOf(
                com.example.swadebuilder.model.Complicacao(
                    id = "sensivel",
                    name = "SENSÍVEL",
                    severity = "Maior",
                    description = "",
                    origem = "SCI_FI"
                )
            ),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val complicacoesLine = lines.firstOrNull { it.contains("SENSÍVEL (Maior)", ignoreCase = true) }
        val annotations = lines.firstOrNull { it.startsWith("Anotações Raciais:") }
        assertTrue(complicacoesLine?.contains("SENSÍVEL (Maior)", ignoreCase = true) == true)
        // If there are multiple annotations (SENSÍVEL Maior is a complication, not annotation here, but just in case of mixup)
        // The expected text is just the second item in the list passed to character.
        // But joinToString(", ") will add a comma if there were other items.
        // Here desvantagensRaciais has 2 items: "SENSÍVEL (Maior)" and "Combine..."
        // SENSÍVEL (Maior) is filtered OUT of annotations because it is in listaComplicacoes.
        // So annotations list should only contain "Combine..."
        assertEquals("Anotações Raciais: $texto", annotations)
    }

    @Test
    fun `buildSummaryLines remove tamanho e movimento mais dois na gazela e mantem grande`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Gazela",
                ancestralidade = "CENTAUX",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                vantagensRaciais = listOf("MOVIMENTAÇÃO +2", "TAMANHO +2", "GRANDE", "ÓBVIO", "MOVIMENTAÇÃO +4"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioSciFiAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "CENTAUX",
                    origem = "FC",
                    opcoes = listOf("Padrão", "Gazela"),
                    habilidades = listOf(
                        RacialAbility("Estável", ""),
                        RacialAbility("Movimentação +2", ""),
                        RacialAbility("Tamanho +2", ""),
                        RacialAbility("Grande", ""),
                        RacialAbility("Óbvio", "")
                    ),
                    especieId = "centaux"
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertFalse(racialLine!!.contains("Movimentação +2"))
        assertFalse(racialLine.contains("Tamanho +2"))
        assertTrue(racialLine.contains("Movimentação +4"))
        assertTrue(racialLine.contains("Grande"))
    }

    @Test
    fun `summary deduplica vantagens com mesmo nome exibido`() {
        val personagem = MeuPersonagem(
            nome = "Oracle",
            ancestralidade = "ORÁCULOS",
            celestialAAMilagresDesabilitado = false,
            vantagens = listOf("poderes_misticos", "poderes_misticos"),
            complicacoes = emptyList(),
            desvantagensRaciais = emptyList(),
            equipamentos = emptyList(),
            poderes = emptyMap(),
            dinheiro = 0,
            pontosRestantes = 0,
            atributos = emptyMap(),
            pericias = emptyMap(),
            compendioSciFiAtivo = true
        )

        val lines = buildSummaryLines(
            personagem = personagem,
            allAdvantages = listOf(
                Vantagem(
                    id = "poderes_misticos",
                    nome = "Poderes Místicos",
                    categoria = Categoria.ANTECEDENTE,
                    origem = "SCI_FI",
                    requisitos = Requisito()
                )
            ),
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val joined = lines.joinToString("\n")
        val count = "Poderes Místicos".toRegex().findAll(joined).count()
        assertEquals(1, count)
    }

    @Test
    fun `buildSummaryLines aplica aparar mais um do signo garca para humanos adg`() {
        // Garça concede Aparar +1 por um traço REAL (id="APARAR", já resolvido
        // em habilidades[] por applyAncestryVariantAdjustments quando o Signo
        // está ativo — mesma injeção que qualquer outra Seleção usa), lido
        // pelo catálogo genérico (RacialTraitPointCatalog.ApararBonus) — não
        // mais um `if` checando o id do Signo. `signoAdgSelecionado` some do
        // fixture porque não é mais lido por calcAparar(); o que importa é a
        // raça já vir com o traço, como currentAncestryDef traria de verdade.
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Garca",
                pericias = mapOf("Lutar" to 4),
                ancestralidade = "HUMANOS",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "HUMANOS",
                    origem = "ARTE_DA_GUERRA",
                    especieId = "humano",
                    habilidades = listOf(
                        RacialAbility(
                            nome = "Aparar +1 (Garça)",
                            descricao = "",
                            id = "APARAR"
                        )
                    )
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        assertTrue(lines.contains("Aparar: 5"))
    }

    @Test
    fun `buildSummaryLines aplica aparar baixo de um traco racial estatico qualquer, nao so Garca`() {
        // Prova de que o mecanismo é genérico (funciona pra QUALQUER raça com
        // um traço ApararBonus, não só Garça): Tanukimimi tem "Aparar Baixo"
        // (id=APARAR_BAIXO, category=racial_trait_negative) — um achado real
        // desta migração, nunca coberto pelo `apararBaixoMod` (que só lê
        // desvantagensRaciais, e esse traço não é uma Complicação concedida).
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Tanuki",
                pericias = mapOf("Lutar" to 4),
                ancestralidade = "TANUKIMIMI",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "TANUKIMIMI",
                    origem = "ARTE_DA_GUERRA",
                    especieId = "tanukimimi",
                    habilidades = listOf(
                        RacialAbility(
                            nome = "Despretensiosos e Barrigudos (Aparar Baixo)",
                            descricao = "",
                            id = "APARAR_BAIXO",
                            category = "racial_trait_negative"
                        )
                    )
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        assertTrue(lines.contains("Aparar: 3"))
    }

    @Test
    fun `buildSummaryLines aplica modificador generico aparar da ancestralidade`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Parry",
                pericias = mapOf("Lutar" to 4),
                ancestralidade = "HUMANOS",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf("APARAR +1"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                atributos = emptyMap(),
                pontosRestantes = 0
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        assertTrue(lines.contains("Aparar: 5"))
    }

    @Test
    fun `buildSummaryLines oculta impulso em caracteristicas raciais de tanukimimi com pensamentos positivos`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Tanu",
                ancestralidade = "TANUKIMIMI",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                vantagensRaciais = listOf("Impulso"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            allAdvantages = listOf(
                Vantagem(
                    id = "impulso",
                    nome = "Impulso",
                    descricao = "",
                    categoria = Categoria.ANTECEDENTE,
                    requisitos = Requisito(observacoes = "Espírito d8")
                )
            ),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "TANUKIMIMI",
                    origem = "ARTE_DA_GUERRA",
                    habilidades = listOf(
                        RacialAbility("Pensamentos Positivos", ""),
                        RacialAbility("Lentos para Agir", "")
                    ),
                    especieId = "tanukimimi"
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertTrue(racialLine!!.contains("Pensamentos Positivos"))
        assertFalse(racialLine.contains("Impulso"))
    }

    @Test
    fun `buildSummaryLines mostra Forasteiro sem grau entre parenteses`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Terra",
                ancestralidade = "TERRACOTA",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf("FORASTEIRO (Menor)"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = listOf(
                com.example.swadebuilder.model.Complicacao(
                    id = "FORASTEIRO",
                    name = "FORASTEIRO",
                    severity = "menor",
                    description = "",
                    origem = "ARTE_DA_GUERRA"
                )
            ),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val complicacoesLine = lines.firstOrNull { it.startsWith("Complicações") }
        val detailsLine = lines.getOrNull((complicacoesLine?.let { lines.indexOf(it) } ?: -1) + 1)

        assertEquals("Forasteiro", detailsLine)
    }

    @Test
    fun `buildSummaryLines mostra furioso normalmente em feral (Insanidade virou dois tracos de verdade)`() {
        // Insanidade deixou de ser uma habilidade única que só mencionava
        // Furioso no texto — agora é Sanguinário (Complicação, habilidade
        // "Insanidade (Sanguinário)") mais Furioso (Vantagem real, concedida
        // via vantagensGratis). Não tem mais duplicata a esconder: Furioso
        // aparece normal, como qualquer Vantagem concedida por raça.
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Feral",
                ancestralidade = "FERAL",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                vantagensRaciais = listOf("FURIOSO"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            allAdvantages = listOf(
                Vantagem(
                    id = "furioso",
                    nome = "Furioso",
                    descricao = "",
                    categoria = Categoria.ANTECEDENTE,
                    requisitos = Requisito()
                )
            ),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "FERAL",
                    origem = "ARTE_DA_GUERRA",
                    habilidades = listOf(
                        RacialAbility("Insanidade (Sanguinário)", ""),
                        RacialAbility("Primitivo", "")
                    ),
                    especieId = "feral"
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertTrue(racialLine!!.contains("Insanidade (Sanguinário)"))
        assertTrue(racialLine.contains("Furioso"))
    }


    @Test
    fun `buildAncestralidadeDisplay humano adg usa formato sem signo ou signo sem do`() {
        val semSigno = buildAncestralidadeDisplay(
            MeuPersonagem(
                nome = "Hum",
                ancestralidade = "HUMANOS",
                signoAdgSelecionado = "Nenhum",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            ancestralidadeNomeBase = "Humano (Império San)"
        )

        val comSigno = buildAncestralidadeDisplay(
            MeuPersonagem(
                nome = "Hum",
                ancestralidade = "HUMANOS",
                signoAdgSelecionado = "Garça",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            ancestralidadeNomeBase = "Humano (Império San)"
        )

        assertEquals("Humano Sem Signo", semSigno)
        assertEquals("Humano Signo Garça", comSigno)
    }

    @Test
    fun `buildSummaryLines humano adg mostra caracteristicas raciais apenas com signo dinamico`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Hum",
                ancestralidade = "HUMANO (IMPÉRIO SAN)",
                signoAdgSelecionado = "Garça",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap(),
                compendioArteDaGuerraAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "Humano (Império San)",
                    origem = "ARTE_DA_GUERRA",
                    habilidades = listOf(
                        RacialAbility("Pontos de Perícia", ""),
                        RacialAbility("Adaptável ou Signo", ""),
                        RacialAbility("Signos de Nascença", "")
                    ),
                    especieId = "humano"
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val identidade = lines.firstOrNull { it.startsWith("Humano ") }
        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }

        assertEquals("Humano Signo Garça", identidade)
        assertEquals("Características Raciais: Signo Garça", racialLine)
    }

    @Test
    fun `buildSummaryLines mostra so o skin do traco racial que concede Vantagem, sem duplicar o nome da Vantagem`() {
        // Sáurios "Sentidos Aguçados" (id=PRONTIDAO, category=racial_edge) concede a
        // Vantagem real "Prontidão" — o id cru "PRONTIDAO" cai em vantagensRaciais
        // (mesmo mecanismo de RacialModifier.vantagensGratisEfetivas()), mas a linha
        // de "Características Raciais" deve mostrar só o skin "Sentidos Aguçados" UMA
        // vez, nunca "Sentidos Aguçados, Prontidão" — a Vantagem "Prontidão" já
        // aparece normalmente na seção de Vantagens, não precisa repetir aqui.
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Sáurio",
                ancestralidade = "SAURIOS",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                vantagensRaciais = listOf("PRONTIDAO"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap()
            ),
            allAdvantages = listOf(
                Vantagem(
                    id = "prontidao",
                    nome = "Prontidão",
                    descricao = "",
                    categoria = Categoria.COMBATE,
                    requisitos = Requisito()
                )
            ),
            listaAncestralidades = listOf(
                RacialModifier(
                    nome = "SAURIOS",
                    origem = "BASICO",
                    habilidades = listOf(
                        RacialAbility(
                            nome = "Sentidos Aguçados",
                            descricao = "Possuem sentidos aguçados, ganhando a Vantagem Prontidão.",
                            id = "PRONTIDAO",
                            category = "racial_edge"
                        )
                    ),
                    especieId = "saurios"
                )
            ),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertTrue(racialLine!!.contains("Sentidos Aguçados"))
        assertFalse("Não deveria repetir o nome da Vantagem concedida (Prontidão) numa entrada à parte", racialLine.contains("Prontidão"))
        assertEquals(1, "Sentidos Aguçados".toRegex().findAll(racialLine).count())
    }

    @Test
    fun `buildSummaryLines para transmorfos exibe Mudar de Forma em caracteristicas raciais e nao duplica Antecedente Arcano Dom`() {
        val transmorfos = RacialModifier(
            nome = "TRANSMORFOS",
            origem = "FANTASIA",
            habilidades = listOf(
                RacialAbility(
                    nome = "Carismático",
                    descricao = "",
                    id = "CARISMATICO",
                    category = "racial_edge"
                ),
                RacialAbility(
                    nome = "Mudar de Forma",
                    descricao = "",
                    id = "PODER_RACIAL",
                    category = "racial_trait_positive"
                )
            ),
            especieId = "transmorfos"
        )
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Transmorfo",
                ancestralidade = "TRANSMORFOS",
                celestialAAMilagresDesabilitado = false,
                vantagens = listOf("Carismático", "Antecedente Arcano (Dom)"),
                complicacoes = emptyList(),
                desvantagensRaciais = emptyList(),
                vantagensRaciais = listOf("carismatico", "antecedente_arcano_dom"),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                atributos = emptyMap(),
                pericias = emptyMap()
            ),
            allAdvantages = listOf(
                Vantagem(id = "carismatico", nome = "Carismático", descricao = "", categoria = Categoria.SOCIAIS, requisitos = Requisito()),
                Vantagem(id = "antecedente_arcano_dom", nome = "Antecedente Arcano (Dom)", descricao = "", categoria = Categoria.ANTECEDENTE, requisitos = Requisito())
            ),
            listaAncestralidades = listOf(transmorfos),
            listaTropos = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = emptyMap(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertTrue(racialLine!!.contains("Mudar de Forma"))
        assertFalse("Não deveria repetir Antecedente Arcano nas características raciais", racialLine.contains("Antecedente Arcano"))
    }

    @Test
    fun `buildSummaryLines uses skin names in lite mode even when modoOficialAtivo is true`() {
        org.junit.Assume.assumeFalse("Test only applies to Lite edition flavor", EditionConfig.isFullEdition)
        val advantages = listOf(
            Vantagem(
                id = "bom_companheiro",
                nome = "BOM COMPANHEIRO (Made Man)",
                originalName = "Made Man",
                categoria = Categoria.ANTECEDENTE,
                origem = "WISEGUYS",
                requisitos = Requisito()
            )
        )
        val complicacoes = listOf(
            com.example.swadebuilder.model.Complicacao(
                id = "procurado_wiseguys",
                name = "PROCURADO (Wiseguys)",
                originalName = "Wanted (Wiseguys)",
                severity = "menor",
                description = "",
                origem = "WISEGUYS"
            )
        )
        val eq = listOf(
            com.example.swadebuilder.model.EquipamentoItem(
                nome = "Pedra Fantasma",
                originalName = "Ghost Rock"
            )
        )

        val personagem = MeuPersonagem(
            nome = "Mafioso",
            ancestralidade = "HUMANOS",
            modoOficialAtivo = true, // Attempt to force official mode
            celestialAAMilagresDesabilitado = false,
            vantagens = listOf("bom_companheiro"),
            complicacoes = listOf("procurado_wiseguys"),
            desvantagensRaciais = emptyList(),
            equipamentos = eq,
            poderes = emptyMap(),
            dinheiro = 100,
            atributos = emptyMap(),
            pericias = emptyMap(),
            pontosRestantes = 0
        )

        val lines = buildSummaryLines(
            personagem = personagem,
            allAdvantages = advantages,
            listaAncestralidades = emptyList(),
            listaTropos = emptyList(),
            listaComplicacoes = complicacoes,
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val joined = lines.joinToString("\n")
        // In Lite edition, EditionConfig.isFullEdition is false.
        // Even with modoOficialAtivo = true, output MUST use skin terms (e.g. Máfia instead of Wiseguys/Made Man, Carvão Espectral instead of Pedra Fantasma/Ghost Rock).
        assertFalse("Should not display raw official name Made Man in Lite mode", joined.contains("Made Man"))
        assertFalse("Should not display Ghost Rock in Lite mode", joined.contains("Ghost Rock"))
        assertTrue("Should map Wiseguys to Máfia in Lite mode", joined.contains("Máfia") || joined.contains("Procurado"))
    }

}
