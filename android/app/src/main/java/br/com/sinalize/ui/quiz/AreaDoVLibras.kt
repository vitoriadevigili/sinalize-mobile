package br.com.sinalize.ui.quiz

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sinalize.R
import br.com.sinalize.ui.components.BotaoPrimario
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.vlibras.EstadoVLibras
import br.com.sinalize.vlibras.MotorVLibras

/**
 * Área do avatar no quiz (QuizPage.tsx): caixa 16:9 com cantos arredondados, com o VLibras dentro.
 * - "Carregando personagem 3D..." até o avatar ficar pronto;
 * - "Montando o seu quiz..." enquanto o quiz é criado;
 * - aviso com "Tentar novamente" se não der para carregar (ex.: sem internet);
 * - botão "Repetir" para ver o sinal de novo, durante as perguntas.
 * A altura é limitada para caber em celular deitado.
 */
@Composable
fun AreaDoVLibras(
    motor: MotorVLibras,
    palavra: String,
    alturaMaxima: Dp,
    montandoQuiz: Boolean,
    podeRepetir: Boolean,
) {
    val estado by motor.estado.collectAsStateWithLifecycle()

    // Envia a palavra ao avatar sempre que ela muda (se ainda estiver carregando, ele sinaliza ao ficar pronto).
    LaunchedEffect(palavra) { motor.sinalizar(palavra) }

    Box(
        modifier = Modifier
            .heightIn(max = alturaMaxima)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111)),
        contentAlignment = Alignment.Center,
    ) {
        AvatarVLibras(motor, Modifier.fillMaxSize())

        when (val atual = estado) {
            is EstadoVLibras.Erro -> Camada {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(atual.mensagem, color = Color.White, textAlign = TextAlign.Center, fontSize = 15.sp)
                    Spacer(Modifier.size(12.dp))
                    BotaoPrimario("Tentar novamente", onClick = motor::recarregar)
                }
            }
            EstadoVLibras.Carregando -> Camada { Carregando("Carregando personagem 3D...") }
            EstadoVLibras.Pronto -> when {
                montandoQuiz -> Camada(fundo = Color.Black.copy(alpha = 0.4f)) { Carregando("Montando o seu quiz...") }
                podeRepetir -> BotaoRepetir(
                    onClick = motor::repetir,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                )
            }
        }
    }
}

/** Mostra a WebView do avatar (que é do app inteiro) dentro desta área enquanto ela estiver na tela. */
@Composable
private fun AvatarVLibras(motor: MotorVLibras, modifier: Modifier) {
    val contexto = LocalContext.current
    val container = remember { FrameLayout(contexto) }
    DisposableEffect(motor, container) {
        motor.exibirEm(container)
        onDispose { motor.devolverDe(container) }
    }
    // Ao recarregar ("Tentar novamente") a WebView muda: o estado novo dispara a reexibição.
    val estado by motor.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado) { motor.exibirEm(container) }

    AndroidView(factory = { container }, modifier = modifier)
}

/** Botão "Repetir" sobre o avatar, no canto inferior direito. */
@Composable
private fun BotaoRepetir(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(AzulSinalize)
            .clickable(onClickLabel = "Repetir o sinal", onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(painterResource(R.drawable.ic_ant_reload), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Text("Repetir", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Camada(fundo: Color = Color(0xFF111111), conteudo: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(fundo), contentAlignment = Alignment.Center) { conteudo() }
}

@Composable
private fun Carregando(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
        Spacer(Modifier.size(12.dp))
        Text(texto, color = Color.White)
    }
}
