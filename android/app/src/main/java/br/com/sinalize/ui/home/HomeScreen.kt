package br.com.sinalize.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.R
import br.com.sinalize.ui.components.ConteudoComRodape
import br.com.sinalize.ui.components.IconesDaBarraDeStatus
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.FundoPagina

@Composable
fun HomeScreen(onJogar: () -> Unit, onDicionario: () -> Unit) {
    var mostrandoSobre by rememberSaveable { mutableStateOf(false) }
    IconesDaBarraDeStatus(claros = false)

    val entrada = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrada.animateTo(1f, tween(600, easing = CubicBezierEasing(0.25f, 0.8f, 0.25f, 1f)))
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(FundoPagina)) {
        val paisagemBaixa = maxWidth > maxHeight && maxHeight < 500.dp
        val alturaTela = maxHeight

        ConteudoComRodape {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = if (paisagemBaixa) 16.dp else 32.dp)
                    .graphicsLayer {
                        alpha = entrada.value
                        translationY = (1f - entrada.value) * 20.dp.toPx()
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (paisagemBaixa) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(48.dp),
                    ) {
                        Logo(Modifier.heightIn(max = alturaTela * 0.5f))
                        BotoesDaHome(onJogar, onDicionario, onSobre = { mostrandoSobre = true }, compacto = true)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(16.dp))
                        Logo(Modifier.heightIn(max = alturaTela * 0.35f))
                        Spacer(Modifier.height(50.dp))
                        BotoesDaHome(onJogar, onDicionario, onSobre = { mostrandoSobre = true })
                    }
                }
            }
        }
    }

    if (mostrandoSobre) {
        SobreDialog(onFechar = { mostrandoSobre = false })
    }
}

@Composable
private fun Logo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.sinalize_logo),
        contentDescription = "Logo do Sinalize",
        modifier = modifier.widthIn(max = 420.dp).fillMaxWidth(),
    )
}

@Composable
private fun BotoesDaHome(onJogar: () -> Unit, onDicionario: () -> Unit, onSobre: () -> Unit, compacto: Boolean = false) {
    Column(
        modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compacto) 12.dp else 24.dp),
    ) {
        BotaoDaHome("Jogar", R.drawable.ic_ant_play_circle, onJogar, preenchido = true, compacto = compacto)
        BotaoDaHome("Dicionário", R.drawable.ic_ant_book, onDicionario, preenchido = true, compacto = compacto)
        BotaoDaHome("Sobre o Projeto", R.drawable.ic_ant_info_circle, onSobre, preenchido = false, compacto = compacto)
    }
}

@Composable
private fun BotaoDaHome(
    texto: String,
    @DrawableRes icone: Int,
    onClick: () -> Unit,
    preenchido: Boolean,
    compacto: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(if (compacto) 56.dp else 70.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 24.dp),
        border = if (preenchido) null else BorderStroke(1.dp, AzulSinalize),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (preenchido) AzulSinalize else Color.White,
            contentColor = if (preenchido) Color.White else AzulSinalize,
        ),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icone),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterStart).size(28.dp),
            )
            Text(
                text = texto,
                fontSize = if (preenchido) 22.sp else 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
