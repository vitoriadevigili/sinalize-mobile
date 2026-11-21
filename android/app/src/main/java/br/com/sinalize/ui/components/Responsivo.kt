package br.com.sinalize.ui.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Responsivo {
    val SM: Dp = 576.dp
    val MD: Dp = 768.dp
    val LG: Dp = 992.dp

    val LARGURA_MAXIMA_PAGINA: Dp = 1200.dp

    val LARGURA_MAXIMA_QUIZ: Dp = 760.dp

    fun colunasDoDicionario(largura: Dp): Int = when {
        largura < SM -> 1
        largura < MD -> 2
        largura < LG -> 3
        else -> 4
    }

    fun colunasDasAlternativas(largura: Dp): Int = if (largura < SM) 1 else 2

    fun ehCompacto(largura: Dp): Boolean = largura < 600.dp
}
