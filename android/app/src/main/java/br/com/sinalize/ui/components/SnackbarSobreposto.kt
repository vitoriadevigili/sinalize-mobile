package br.com.sinalize.ui.components

import android.graphics.Rect
import android.view.View
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
fun SnackbarSobreposto(estado: SnackbarHostState) {
    val janela = LocalView.current.rootView
    val posicao = remember(janela) { RodapeDaTela(janela) }
    Popup(
        popupPositionProvider = posicao,
        properties = PopupProperties(focusable = false, clippingEnabled = false),
    ) {
        SnackbarHost(estado)
    }
}

private class RodapeDaTela(private val janela: View) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val tela = Rect().also(janela::getWindowVisibleDisplayFrame)
        val origem = IntArray(2).also(janela::getLocationOnScreen)
        return IntOffset(
            x = tela.left + (tela.width() - popupContentSize.width) / 2 - origem[0],
            y = tela.bottom - popupContentSize.height - origem[1],
        )
    }
}
