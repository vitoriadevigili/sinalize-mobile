package br.com.sinalize.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CoresSinalize = lightColorScheme(
    primary = AzulSinalize,
    onPrimary = Color.White,
    primaryContainer = AzulSinalize,
    onPrimaryContainer = Color.White,
    secondary = AzulSinalize,
    onSecondary = Color.White,
    secondaryContainer = CinzaBotao,
    onSecondaryContainer = TextoEscuro,
    background = FundoPagina,
    onBackground = TextoEscuro,
    surface = Color.White,
    onSurface = TextoEscuro,
    surfaceVariant = CinzaBotao,
    onSurfaceVariant = TextoSecundario,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    outline = BordaCampo,
    outlineVariant = CinzaBotao,
    error = VermelhoExcluir,
)

/** O site é claro; o app segue a mesma identidade, sem tema escuro. */
@Composable
fun SinalizeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CoresSinalize, typography = Typography, content = content)
}
