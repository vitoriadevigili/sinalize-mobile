package br.com.sinalize.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.dp

private val LARGURA = 4.dp
private val MARGEM = 2.dp
private val ALTURA_MINIMA = 32.dp

fun Modifier.barraDeRolagem(
    estado: ScrollState,
    cor: Color = Color(0x99808080),
): Modifier = drawWithContent {
    drawContent()
    if (estado.maxValue <= 0 || estado.maxValue == Int.MAX_VALUE) return@drawWithContent
    val visivel = size.height
    val total = visivel + estado.maxValue
    desenharBarra(
        fracaoVisivel = visivel / total,
        progresso = estado.value.toFloat() / estado.maxValue,
        cor = cor,
    )
}

fun Modifier.barraDeRolagem(
    estado: LazyGridState,
    cor: Color = Color(0x99808080),
): Modifier = drawWithContent {
    drawContent()
    if (!estado.canScrollForward && !estado.canScrollBackward) return@drawWithContent
    val info = estado.layoutInfo
    val total = info.totalItemsCount
    val visiveis = info.visibleItemsInfo.size
    if (total == 0 || visiveis == 0) return@drawWithContent
    val fracaoVisivel = (visiveis.toFloat() / total).coerceIn(0.05f, 1f)
    val progresso = when {
        !estado.canScrollForward -> 1f
        !estado.canScrollBackward -> 0f
        else -> (estado.firstVisibleItemIndex.toFloat() / (total - visiveis).coerceAtLeast(1)).coerceIn(0f, 1f)
    }
    desenharBarra(fracaoVisivel, progresso, cor)
}

private fun ContentDrawScope.desenharBarra(fracaoVisivel: Float, progresso: Float, cor: Color) {
    val largura = LARGURA.toPx()
    val altura = (size.height * fracaoVisivel).coerceAtLeast(ALTURA_MINIMA.toPx()).coerceAtMost(size.height)
    val topo = (size.height - altura) * progresso
    drawRoundRect(
        color = cor,
        topLeft = Offset(size.width - largura - MARGEM.toPx(), topo),
        size = Size(largura, altura),
        cornerRadius = CornerRadius(largura / 2),
    )
}
