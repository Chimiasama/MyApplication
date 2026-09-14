package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.GrupoAlternativo
import com.example.swadebuilder.model.GrupoMinimo
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePrerequisiteUseCaseTest {

    private val useCase = ValidatePrerequisiteUseCase()

    private fun vantagem(id: String, requisitos: Requisito = Requisito(), choice: String? = null) =
        Vantagem(id = id, nome = id, categoria = Categoria.SOCIAIS, origem = "BASICO", requisitos = requisitos, choice = choice)

    private fun input(
        vantagem: Vantagem,
        vantagensSelecionadas: List<Vantagem> = emptyList(),
        complicacoesSelecionadas: Collection<Complicacao> = emptyList(),
        pericias: List<Pericia> = emptyList(),
        totais: Map<String, Int> = emptyMap()
    ) = ValidatePrerequisiteUseCase.Input(
        vantagem = vantagem,
        vantagensSelecionadas = vantagensSelecionadas,
        complicacoesSelecionadas = complicacoesSelecionadas,
        pericias = pericias,
        rawTotalPericia = { p -> totais[p.nome] ?: 0 }
    )

    // --- grupoMinimo: "pelo menos N destas opções" (Bando de Guerra / Ordem-Unida) ---

    @Test
    fun `grupoMinimo bloqueia quando tem menos opcoes do grupo do que o minimo`() {
        val bandoDeGuerra = vantagem(
            "bando_de_guerra",
            Requisito(
                vantagensPrevias = listOf("comando", "seguidores"),
                grupoMinimo = GrupoMinimo(
                    opcoes = listOf(
                        "presenca_de_comando", "estrategista", "mestre_estrategista",
                        "fervor", "inspirar", "lider_nato", "mantenham_a_formacao"
                    ),
                    minimo = 2
                )
            )
        )
        val selecionadas = listOf(
            vantagem("comando"), vantagem("seguidores"), vantagem("inspirar") // só 1 do grupo
        )
        assertFalse(useCase.execute(input(bandoDeGuerra, vantagensSelecionadas = selecionadas)))
    }

    @Test
    fun `grupoMinimo libera quando tem pelo menos N opcoes do grupo, alem da lista fixa`() {
        val bandoDeGuerra = vantagem(
            "bando_de_guerra",
            Requisito(
                vantagensPrevias = listOf("comando", "seguidores"),
                grupoMinimo = GrupoMinimo(
                    opcoes = listOf(
                        "presenca_de_comando", "estrategista", "mestre_estrategista",
                        "fervor", "inspirar", "lider_nato", "mantenham_a_formacao"
                    ),
                    minimo = 2
                )
            )
        )
        val selecionadas = listOf(
            vantagem("comando"), vantagem("seguidores"), vantagem("inspirar"), vantagem("fervor")
        )
        assertTrue(useCase.execute(input(bandoDeGuerra, vantagensSelecionadas = selecionadas)))
    }

    @Test
    fun `grupoMinimo nao dispensa a lista fixa de vantagens_previas`() {
        val bandoDeGuerra = vantagem(
            "bando_de_guerra",
            Requisito(
                vantagensPrevias = listOf("comando", "seguidores"),
                grupoMinimo = GrupoMinimo(
                    opcoes = listOf("inspirar", "fervor"),
                    minimo = 2
                )
            )
        )
        // Tem as duas do grupo, mas falta "seguidores" da lista fixa.
        val selecionadas = listOf(vantagem("comando"), vantagem("inspirar"), vantagem("fervor"))
        assertFalse(useCase.execute(input(bandoDeGuerra, vantagensSelecionadas = selecionadas)))
    }

    // --- gruposAlternativos: "isto OU aquilo" (Pathfinder: AA ou Poderes Místicos) ---

    @Test
    fun `gruposAlternativos libera com antecedente arcano generico`() {
        val v = vantagem(
            "concentracao",
            Requisito(
                gruposAlternativos = listOf(
                    GrupoAlternativo(vantagens = listOf("ANTECEDENTE_ARCANO")),
                    GrupoAlternativo(vantagens = listOf("poderes_misticos"))
                )
            )
        )
        val selecionadas = listOf(vantagem("antecedente_arcano_mago"))
        assertTrue(useCase.execute(input(v, vantagensSelecionadas = selecionadas)))
    }

    @Test
    fun `gruposAlternativos libera so com poderes misticos, sem nenhum antecedente arcano`() {
        val v = vantagem(
            "concentracao",
            Requisito(
                gruposAlternativos = listOf(
                    GrupoAlternativo(vantagens = listOf("ANTECEDENTE_ARCANO")),
                    GrupoAlternativo(vantagens = listOf("poderes_misticos"))
                )
            )
        )
        val selecionadas = listOf(vantagem("poderes_misticos"))
        assertTrue(useCase.execute(input(v, vantagensSelecionadas = selecionadas)))
    }

    @Test
    fun `gruposAlternativos bloqueia sem nenhuma das duas alternativas`() {
        val v = vantagem(
            "concentracao",
            Requisito(
                gruposAlternativos = listOf(
                    GrupoAlternativo(vantagens = listOf("ANTECEDENTE_ARCANO")),
                    GrupoAlternativo(vantagens = listOf("poderes_misticos"))
                )
            )
        )
        assertFalse(useCase.execute(input(v, vantagensSelecionadas = emptyList())))
    }

    @Test
    fun `gruposAlternativos aceita alternativa so de pericias (Irmandade das Seis Chaves)`() {
        val v = vantagem(
            "irmandade_das_seis_chaves",
            Requisito(
                gruposAlternativos = listOf(
                    GrupoAlternativo(vantagens = listOf("magomecanico")),
                    GrupoAlternativo(pericias = mapOf("Consertar" to 10, "Ciência" to 10))
                )
            )
        )
        val consertar = Pericia(nome = "Consertar", atributo = "AGILIDADE", basica = false)
        val ciencia = Pericia(nome = "Ciência", atributo = "ASTUCIA", basica = false)
        val ok = useCase.execute(
            input(
                v,
                pericias = listOf(consertar, ciencia),
                totais = mapOf("Consertar" to 10, "Ciência" to 10)
            )
        )
        assertTrue(ok)
    }

    @Test
    fun `gruposAlternativos rejeita alternativa de pericias quando falta uma delas`() {
        val v = vantagem(
            "irmandade_das_seis_chaves",
            Requisito(
                gruposAlternativos = listOf(
                    GrupoAlternativo(vantagens = listOf("magomecanico")),
                    GrupoAlternativo(pericias = mapOf("Consertar" to 10, "Ciência" to 10))
                )
            )
        )
        val consertar = Pericia(nome = "Consertar", atributo = "AGILIDADE", basica = false)
        val ciencia = Pericia(nome = "Ciência", atributo = "ASTUCIA", basica = false)
        // Ciência só em d8, não em d10 — não deveria satisfazer nem essa alternativa nem a outra.
        val ok = useCase.execute(
            input(
                v,
                pericias = listOf(consertar, ciencia),
                totais = mapOf("Consertar" to 10, "Ciência" to 8)
            )
        )
        assertFalse(ok)
    }

    @Test
    fun `entradas sem grupoMinimo nem gruposAlternativos continuam se comportando como antes`() {
        val v = vantagem("sorte_grande", Requisito(vantagensPrevias = listOf("sorte")))
        assertFalse(useCase.execute(input(v, vantagensSelecionadas = emptyList())))
        assertTrue(useCase.execute(input(v, vantagensSelecionadas = listOf(vantagem("sorte")))))
    }
}
