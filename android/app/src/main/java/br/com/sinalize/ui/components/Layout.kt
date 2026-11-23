package br.com.sinalize.ui.components

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import br.com.sinalize.R
import br.com.sinalize.ui.theme.AzulRodape
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.FundoPagina

@Composable
fun PaginaComHeader(
    onVoltarAoMenu: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var confirmandoSaida by rememberSaveable { mutableStateOf(false) }

    BackHandler { confirmandoSaida = true }
    IconesDaBarraDeStatus(claros = true)

    Column(Modifier.fillMaxSize().background(FundoPagina)) {
        HeaderBar(onMenu = { confirmandoSaida = true })
        content()
    }

    if (confirmandoSaida) {
        ConfirmDialog(
            titulo = "Deseja voltar ao menu principal?",
            texto = "Ao sair, o progresso desta sessão será perdido.",
            rotuloConfirmar = "Sair",
            rotuloCancelar = "Continuar",
            perigoso = true,
            onConfirmar = {
                confirmandoSaida = false
                onVoltarAoMenu()
            },
            onCancelar = { confirmandoSaida = false },
        )
    }
}

@Composable
fun HeaderBar(onMenu: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compacto = Responsivo.ehCompacto(maxWidth) || maxHeight < 500.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp)
                .background(AzulSinalize)
                .statusBarsPadding()
                .height(if (compacto) 64.dp else 90.dp)
                .padding(horizontal = if (compacto) 12.dp else 30.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onMenu, shape = CircleShape) {
                Icon(
                    painter = painterResource(R.drawable.ic_ant_home),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Menu", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.sinalize_logo_white),
                contentDescription = "Logo do Sinalize",
                modifier = Modifier.height(if (compacto) 48.dp else 80.dp).alpha(0.95f),
            )
        }
    }
}

@Composable
fun Footer(modifier: Modifier = Modifier) {
    val telaBaixa = LocalConfiguration.current.screenHeightDp < 500
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compacto = Responsivo.ehCompacto(maxWidth)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulRodape)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = if (telaBaixa) 10.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (telaBaixa) 2.dp else 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.udesc_logo),
                    contentDescription = "Logo da UDESC",
                    modifier = Modifier
                        .size(if (telaBaixa) 36.dp else if (compacto) 52.dp else 67.dp)
                        .alpha(0.9f),
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Sinalize") }
                        append(" • Programação para Dispositivos Móveis")
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                )
            }
            Text(
                text = "UDESC - Centro de Ciências Tecnológicas",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun ConteudoComRodape(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val alturaVisivel = maxHeight
        val rolagem = rememberScrollState()
        Column(Modifier.fillMaxSize().barraDeRolagem(rolagem).verticalScroll(rolagem)) {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(min = alturaVisivel),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.fillMaxWidth(), content = content)
                Footer()
            }
        }
    }
}

@Composable
fun IconesDaBarraDeStatus(claros: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val orientacao = LocalConfiguration.current.orientation
    DisposableEffect(claros, orientacao) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !claros
        }
        onDispose { }
    }
}
