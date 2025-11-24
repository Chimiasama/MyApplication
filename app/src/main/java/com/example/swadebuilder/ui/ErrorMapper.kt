package com.example.swadebuilder.ui

import android.content.Context
import com.example.swadebuilder.R
import com.example.swadebuilder.SelectionError

fun mapErrorToString(context: Context, error: SelectionError): String {
    return when (error) {
        is SelectionError.None -> ""
        is SelectionError.VantagemError.LimitePontosPoderEstagio -> context.getString(R.string.vantagem_error_limite_pontos_poder_estagio)
        is SelectionError.VantagemError.MultiplosArcanosDesabilitado -> context.getString(R.string.vantagem_error_multiplos_arcanos_desabilitado)
        is SelectionError.VantagemError.JaSelecionada -> context.getString(R.string.vantagem_error_ja_selecionada)
        is SelectionError.VantagemError.ArcanoComEscolhaJaSelecionado -> context.getString(R.string.vantagem_error_arcano_com_escolha_ja_selecionado)
        is SelectionError.VantagemError.JaSelecionadaComEspecializacao -> context.getString(R.string.vantagem_error_ja_selecionada_com_especializacao)
        is SelectionError.VantagemError.RequerProfissionalComEspecializacao -> context.getString(R.string.vantagem_error_requer_profissional_com_especializacao)
        is SelectionError.VantagemError.NenhumAtributoOuPericiaNoMaximo -> context.getString(R.string.vantagem_error_nenhum_atributo_ou_pericia_no_maximo)
        is SelectionError.VantagemError.AtributoNaoEstaNoMaximo -> context.getString(R.string.vantagem_error_atributo_nao_esta_no_maximo, error.attr)
        is SelectionError.VantagemError.PericiaNaoEstaNoMaximo -> context.getString(R.string.vantagem_error_pericia_nao_esta_no_maximo, error.pericia)
        is SelectionError.VantagemError.RequerEstagio -> context.getString(R.string.vantagem_error_requer_estagio, error.estagio)
        is SelectionError.VantagemError.RequerVantagensPrevias -> context.getString(R.string.vantagem_error_requer_vantagens_previas)
        is SelectionError.VantagemError.LimiteComprasPontosPoder -> context.getString(R.string.vantagem_error_limite_compras_pontos_poder)
        is SelectionError.VantagemError.MaximoSelecoes -> context.getString(R.string.vantagem_error_maximo_selecoes, error.max)
        is SelectionError.VantagemError.ComEscolhaJaSelecionada -> context.getString(R.string.vantagem_error_com_escolha_ja_selecionada)
        is SelectionError.VantagemError.RequerAtributo -> context.getString(R.string.vantagem_error_requer_atributo, error.attr, error.valor)
        is SelectionError.VantagemError.RequerUmaDasPericias -> context.getString(R.string.vantagem_error_requer_uma_das_pericias)
        is SelectionError.VantagemError.RequerPericia -> context.getString(R.string.vantagem_error_requer_pericia, error.pericia, error.valor)
        is SelectionError.VantagemError.RequerUmaDasPericiasOpcionais -> context.getString(R.string.vantagem_error_requer_uma_das_pericias_opcionais)
        is SelectionError.VantagemError.RequerCartaSelvagem -> context.getString(R.string.vantagem_error_requer_carta_selvagem)
        is SelectionError.VantagemError.ConflitoComplicacao -> context.getString(R.string.vantagem_error_conflito_complicacao)
        is SelectionError.VantagemError.ConflitoPobreza -> context.getString(R.string.vantagem_error_conflito_pobreza)

        is SelectionError.SuperPoderError.RequerPericia -> context.getString(R.string.super_poder_error_requer_pericia, error.poder, error.pericia)
        is SelectionError.SuperPoderError.LimiteAtingido -> context.getString(R.string.super_poder_error_limite_atingido, error.limite)
        is SelectionError.SuperPoderError.SemSaldo -> context.getString(R.string.super_poder_error_sem_saldo, error.custo, error.saldo)
        else -> "Erro desconhecido"
    }
}
