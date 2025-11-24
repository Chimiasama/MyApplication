package com.example.swadebuilder

sealed class SelectionError {
    data object None : SelectionError()

    sealed class VantagemError : SelectionError() {
        data object LimitePontosPoderEstagio : VantagemError()
        data object MultiplosArcanosDesabilitado : VantagemError()
        data object JaSelecionada : VantagemError()
        data object ArcanoComEscolhaJaSelecionado : VantagemError()
        data object JaSelecionadaComEspecializacao : VantagemError()
        data object RequerProfissionalComEspecializacao : VantagemError()
        data object NenhumAtributoOuPericiaNoMaximo : VantagemError()
        data class AtributoNaoEstaNoMaximo(val attr: String) : VantagemError()
        data class PericiaNaoEstaNoMaximo(val pericia: String) : VantagemError()
        data class RequerEstagio(val estagio: String) : VantagemError()
        data object RequerVantagensPrevias : VantagemError()
        data object LimiteComprasPontosPoder : VantagemError()
        data class MaximoSelecoes(val max: Int) : VantagemError()
        data object ComEscolhaJaSelecionada : VantagemError()
        data class RequerAtributo(val attr: String, val valor: Int) : VantagemError()
        data object RequerUmaDasPericias : VantagemError()
        data class RequerPericia(val pericia: String, val valor: Int) : VantagemError()
        data object RequerUmaDasPericiasOpcionais : VantagemError()
        data object RequerCartaSelvagem : VantagemError()
        data object ConflitoComplicacao : VantagemError()
        data object ConflitoPobreza : VantagemError()
    }

    sealed class SuperPoderError : SelectionError() {
        data class RequerPericia(val poder: String, val pericia: String) : SuperPoderError()
        data class LimiteAtingido(val limite: Int) : SuperPoderError()
        data class SemSaldo(val custo: Int, val saldo: Int) : SuperPoderError()
    }
}
