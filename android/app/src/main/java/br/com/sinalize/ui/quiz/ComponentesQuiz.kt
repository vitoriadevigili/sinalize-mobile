package br.com.sinalize.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.R
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.CinzaPendente
import br.com.sinalize.ui.theme.VerdeAcerto
import br.com.sinalize.ui.theme.VermelhoErro

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BarraDeProgresso(resultados: List<Boolean?>, indiceAtual: Int) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        resultados.forEachIndexed { i, resultado ->
            val alvo = when {
                resultado == true -> VerdeAcerto
                resultado == false -> VermelhoErro
                i == indiceAtual -> AzulSinalize
                else -> CinzaPendente
            }
            val fundo by animateColorAsState(alvo, tween(300), label = "progresso")
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(fundo),
                contentAlignment = Alignment.Center,
            ) {
                when (resultado) {
                    true -> Icon(painterResource(R.drawable.ic_ant_check), "Acertou", Modifier.size(14.dp), Color.White)
                    false -> Icon(painterResource(R.drawable.ic_ant_close), "Errou", Modifier.size(14.dp), Color.White)
                    null -> Text("${i + 1}", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun Alternativas(
    opcoes: List<PalavraResumo>,
    idCorreto: Long,
    idSelecionado: Long?,
    respondido: Boolean,
    colunas: Int,
    onSelecionar: (PalavraResumo) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        opcoes.chunked(colunas).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                linha.forEach { opcao ->
                    val cor = when {
                        !respondido -> AzulSinalize
                        opcao.id == idCorreto -> VerdeAcerto
                        opcao.id == idSelecionado -> VermelhoErro
                        else -> AzulSinalize
                    }
                    val fundo by animateColorAsState(cor, tween(300), label = "alternativa")
                    val opacidade by animateFloatAsState(
                        if (respondido && cor == AzulSinalize) 0.6f else 1f, tween(300), label = "opacidade",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 60.dp)
                            .alpha(opacidade)
                            .clip(RoundedCornerShape(12.dp))
                            .background(fundo)
                            .clickable(enabled = !respondido) { onSelecionar(opcao) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            opcao.palavra,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                repeat(colunas - linha.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
