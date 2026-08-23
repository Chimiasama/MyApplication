package com.example.swadebuilder.model

/**
 * Representa um preset declarativo de módulo / cenário de criação de personagem.
 * Centraliza os padrões de regras ativas e metadados por cenário.
 */
data class CreationPreset(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val moduloKey: String,
    val isFullEditionOnly: Boolean = false,
    val defaultCartaSelvagem: Boolean = true,
    val defaultMaisPontosPericias: Boolean = true,
    val defaultNasceUmHeroi: Boolean = false,
    val defaultRegraFama: Boolean = false,
    val defaultRegraRiqueza: Boolean = false,
    val defaultRegraCosaNostra: Boolean = false,
    val defaultRegraMechas: Boolean = false,
    val defaultRegraCiberneticos: Boolean = false,
    val defaultGrandesResponsabilidades: Boolean = false,
    val defaultModoMonstro: Boolean = false,
    val defaultCompendioFantasia: Boolean = false,
    val defaultCompendioPathfinder: Boolean = false,
    val defaultCompendioDeadlands: Boolean = false,
    val defaultCompendioCrystalHeart: Boolean = false,
    val defaultCompendioArteDaGuerra: Boolean = false,
    val defaultCompendioCidadeSolVapor: Boolean = false,
    val defaultCompendioWiseguys: Boolean = false,
    val defaultCompendioHorror: Boolean = false,
    val defaultCompendioSciFi: Boolean = false,
    val defaultSuperPoderes: Boolean = false
) {
    companion object {
        val ALL_PRESETS: List<CreationPreset> = listOf(
            CreationPreset(
                id = "basico",
                titulo = "Livro Básico",
                subtitulo = "As regras do livro básico.",
                moduloKey = "BASICO",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = true
            ),
            CreationPreset(
                id = "fantasia",
                titulo = "Compêndio de Fantasia",
                subtitulo = "Raças, itens mágicos e regras de fantasia.",
                moduloKey = "FANTASIA",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = false,
                defaultCompendioFantasia = true
            ),
            CreationPreset(
                id = "scifi",
                titulo = "Compêndio de Ficção",
                subtitulo = "Tecnologia avançada, naves e cibernéticos.",
                moduloKey = "SCI_FI",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = true,
                defaultCompendioSciFi = true
            ),
            CreationPreset(
                id = "horror",
                titulo = "Compêndio de Horror",
                subtitulo = "Climas sombrios e criaturas aterrorizantes.",
                moduloKey = "HORROR",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = true,
                defaultCompendioHorror = true
            ),
            CreationPreset(
                id = "supers",
                titulo = "Superpoderes",
                subtitulo = "Seja um super-herói!",
                moduloKey = "SUPER",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = true,
                defaultSuperPoderes = true
            ),
            CreationPreset(
                id = "pathfinder",
                titulo = "Mundo Ancestral / Pathfinder",
                subtitulo = "Classes, ancestralidades e magia para alta fantasia.",
                moduloKey = "PATHFINDER",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = false,
                defaultCompendioPathfinder = true
            ),
            CreationPreset(
                id = "deadlands",
                titulo = "Deadlands",
                subtitulo = "Pistoleiros, revividos e o horror do Oeste.",
                moduloKey = "DEADLANDS",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = false,
                defaultCompendioDeadlands = true
            ),
            CreationPreset(
                id = "crystal_heart",
                titulo = "Crystal Heart",
                subtitulo = "Troque seu coração por uma pedra mágica.",
                moduloKey = "CRYSTAL_HEART",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = false,
                defaultCompendioCrystalHeart = true
            ),
            CreationPreset(
                id = "arte_da_guerra",
                titulo = "Arte da Guerra: Nova Era",
                subtitulo = "Ativa Chi, Tropos e equipamentos orientais.",
                moduloKey = "ARTE_DA_GUERRA",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = false,
                defaultNasceUmHeroi = true,
                defaultCompendioArteDaGuerra = true
            ),
            CreationPreset(
                id = "cidade_sol_vapor",
                titulo = "A Cidade do Sol a Vapor",
                subtitulo = "Estímulos vitorianos, vapor e tecnomagia.",
                moduloKey = "CIDADE_SOL_VAPOR",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = true,
                defaultCompendioCidadeSolVapor = true
            ),
            CreationPreset(
                id = "wiseguys",
                titulo = "Wiseguys",
                subtitulo = "Crime organizado moderno, conexões e esquemas.",
                moduloKey = "WISEGUYS",
                defaultCartaSelvagem = true,
                defaultMaisPontosPericias = true,
                defaultRegraRiqueza = true,
                defaultRegraCosaNostra = true,
                defaultCompendioWiseguys = true
            )
        )

        fun getById(id: String): CreationPreset {
            return ALL_PRESETS.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ALL_PRESETS.first()
        }
    }
}
