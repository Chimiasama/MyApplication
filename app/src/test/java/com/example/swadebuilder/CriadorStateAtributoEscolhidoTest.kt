package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.registry.AncestryVariantRegistry
import com.example.swadebuilder.util.keyify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Escolha de atributo (TARGET_ATTRIBUTE_OR_SKILL) migrada pro sistema de
 * Seleção — mesmo padrão de Herança/Signo (ver CriadorStateSignoDeNascencaTest),
 * mas o "efeito mecânico" é o próprio atributo escolhido, não um pacote de
 * traços fixo por opção (ver AncestryVariantRegistry.meioOrc()/feral() e a
 * Seleção aninhada de humanos() Sci-Fi). Estes testes fixam que o traço real
 * (traitId=ATTRIBUTE_BOOST + targetRef=o atributo escolhido) reflete
 * `humanoMineradorAtributo` corretamente pras 3 raças que compartilham esse
 * campo, e que o default de cada raça (quando nada foi escolhido ainda)
 * corresponde ao que existia antes desta migração.
 */
class CriadorStateAtributoEscolhidoTest {

    private fun atributosPadrao() = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")

    private fun snapshotCom(vararg racas: RacialModifier): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas.toList(),
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = atributosPadrao(),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = emptyList<Pericia>(),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList<Vantagem>(),
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    private fun meioOrc() = RacialModifier(
        nome = "MEIO-ORCS",
        habilidades = listOf(RacialAbility(nome = "Endurecido", descricao = "", id = "ENDURECIDO")),
        origem = "FANTASIA"
    )

    private fun feral() = RacialModifier(
        nome = "Feral",
        habilidades = listOf(RacialAbility(nome = "Primitivo", descricao = "", id = "PRIMITIVO")),
        origem = "ARTE_DA_GUERRA"
    )

    private fun humanosSciFi() = RacialModifier(
        nome = "HUMANOS",
        habilidades = emptyList(),
        origem = "SCI_FI",
        opcoes = listOf("Padrão", "Baixa Gravidade", "Minerador")
    )

    private fun humanoPathfinder() = RacialModifier(
        nome = "Humano",
        habilidades = listOf(
            RacialAbility(nome = "Adaptável", descricao = "", id = "ADAPTAVEL"),
            RacialAbility(nome = "Flexibilidade", descricao = "", id = "FLEXIBILIDADE", traitId = "FLEXIBILIDADE")
        ),
        origem = "PATHFINDER"
    )

    private fun meioElfoPathfinder() = RacialModifier(
        nome = "Meio-Elfo",
        habilidades = listOf(
            RacialAbility(nome = "Flexibilidade", descricao = "", id = "FLEXIBILIDADE", traitId = "FLEXIBILIDADE")
        ),
        origem = "PATHFINDER"
    )

    @Test
    fun `meio-orc sem escolha usa o default Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioOrc()))
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "MEIO-ORCS"
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Força"))
    }

    @Test
    fun `meio-orc com Forca escolhida sobe Forca e nao Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioOrc()))
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "MEIO-ORCS"
        state.selecionarHumanoMineradorAtributo("Força")

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `feral sem escolha usa o default Forca`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(feral()))
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "Feral"
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Agilidade"))
    }

    @Test
    fun `feral com Agilidade escolhida sobe Agilidade`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(feral()))
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "Feral"
        state.selecionarHumanoMineradorAtributo("Agilidade")

        assertEquals(6, state.atributoMinRaw("Agilidade"))
        assertEquals(4, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `humano sci-fi minerador sem escolha usa o default Forca`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanosSciFi()))
        state.compendioSciFiAtivo = true
        state.ancestralidade = "HUMANOS"
        state.selecionarScifiVariant("Minerador")
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `humano sci-fi minerador com Vigor escolhido sobe Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanosSciFi()))
        state.compendioSciFiAtivo = true
        state.ancestralidade = "HUMANOS"
        state.selecionarScifiVariant("Minerador")
        state.selecionarHumanoMineradorAtributo("Vigor")

        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Força"))
    }

    @Test
    fun `humano sci-fi baixa gravidade nao ganha bonus de forca ou vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanosSciFi()))
        state.compendioSciFiAtivo = true
        state.ancestralidade = "HUMANOS"
        state.selecionarScifiVariant("Baixa Gravidade")

        assertEquals(4, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(6, state.atributoMinRaw("Agilidade"))
    }

    @Test
    fun `trocar de meio-orc Vigor para Forca nao deixa Vigor vazando`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioOrc()))
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "MEIO-ORCS"
        state.selecionarHumanoMineradorAtributo("Vigor")
        assertEquals(6, state.atributoMinRaw("Vigor"))

        state.selecionarHumanoMineradorAtributo("Força")

        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(6, state.atributoMinRaw("Força"))
    }

    @Test
    fun `humano pathfinder com flexibilidade sem escolha usa default Agilidade`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanoPathfinder()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Humano"
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Agilidade"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `humano pathfinder com flexibilidade com Vigor escolhido sobe Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanoPathfinder()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Humano"
        state.selecionarHumanoMineradorAtributo("Vigor")

        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Agilidade"))
    }

    @Test
    fun `meio-elfo pathfinder com flexibilidade com Espirito escolhido sobe Espirito`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioElfoPathfinder()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Meio-Elfo"
        state.selecionarHumanoMineradorAtributo("Espírito")

        assertEquals(6, state.atributoMinRaw("Espírito"))
        assertEquals(4, state.atributoMinRaw("Agilidade"))
    }

    @Test
    fun `flexibilidade possui opcoes de atributo nao vazias no registro`() {
        val config = AncestryVariantRegistry.get("HUMANO", "PATHFINDER")
        assertNotNull(config)
        val selection = config!!.selecoes.firstOrNull { it.marcadorTraitId == "FLEXIBILIDADE" }
        assertNotNull(selection)
        assertFalse(selection!!.targetOptions.isNullOrEmpty())
        assertTrue(selection.targetOptions!!.contains("Vigor"))
    }

    @Test
    fun `humano pathfinder com flexibilidade sobe atributo minimo para d6 mas NAO eleva o teto maximo para d12+1`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanoPathfinder()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Humano"
        state.selecionarHumanoMineradorAtributo("Vigor")

        // Inicia em d6
        assertEquals(6, state.atributoMinRaw("Vigor"))
        // O teto máximo permanece d12 (12) em vez de elevar para d12+1 (13)
        assertEquals(12, state.atributoMaxRaw("Vigor"))
    }

    @Test
    fun `simulacao completa de selecao humano pathfinder aumentando agilidade mantem teto maximo em d12`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanoPathfinder()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Humano"

        // 1. Seleciona Agilidade na Flexibilidade
        state.selecionarHumanoMineradorAtributo("Agilidade")

        // 2. Verifica piso e teto inicial
        assertEquals(6, state.atributoMinRaw("Agilidade"))
        assertEquals(12, state.atributoMaxRaw("Agilidade"))
        assertEquals(12, state.atributoMaxRawNaCriacao("Agilidade"))

        // 3. Simula avanço de pontos na criação (d6 -> d8 -> d10 -> d12)
        state.pontosAtributo = 5
        val stack = state.paCostStackPorAtributo.getValue("AGILIDADE")

        // d6 -> d8 (8)
        stack.add(1)
        state.valoresAtributos["AGILIDADE"]!!.intValue = 8
        state.pontosAtributo--

        // d8 -> d10 (10)
        stack.add(1)
        state.valoresAtributos["AGILIDADE"]!!.intValue = 10
        state.pontosAtributo--

        // d10 -> d12 (12)
        stack.add(1)
        state.valoresAtributos["AGILIDADE"]!!.intValue = 12
        state.pontosAtributo--

        // 4. Confirma que o valor atual atingiu d12 e que o teto continua d12
        assertEquals(12, state.valoresAtributos["AGILIDADE"]!!.intValue)
        assertEquals(12, state.atributoMaxRawNaCriacao("AGILIDADE"))

        // 5. Confirma que a tentativa de subir além de d12 na criação é bloqueada (nextRaw 13 > maxRaw 12)
        val nextRaw = 12 + 1
        val maxRaw = state.atributoMaxRawNaCriacao("AGILIDADE")
        assertTrue("Aumento além de d12 deve ser bloqueado", nextRaw > maxRaw)
    }

    @Test
    fun `deduplicacao de opcoes de atributo por keyify nao produz entradas duplicadas sem acento`() {
        val targetOptions = listOf("Agilidade", "Astúcia", "Espírito", "Força", "Vigor")
        val listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")

        val map = LinkedHashMap<String, String>()
        targetOptions.forEach { opt ->
            map[opt.keyify()] = opt
        }
        listaAtributos.forEach { attr ->
            val key = attr.keyify()
            if (key !in map) {
                map[key] = attr
            }
        }

        val result = map.values.toList()
        assertEquals(5, result.size)
        assertEquals(listOf("Agilidade", "Astúcia", "Espírito", "Força", "Vigor"), result)
    }
}
