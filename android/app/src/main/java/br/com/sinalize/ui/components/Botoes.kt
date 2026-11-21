package br.com.sinalize.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.R
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.BordaCampo
import br.com.sinalize.ui.theme.CinzaBotao
import br.com.sinalize.ui.theme.TextoEscuro
import br.com.sinalize.ui.theme.VermelhoExcluir

val CantoAntd = RoundedCornerShape(6.dp)

@Composable
fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cor: Color = AzulSinalize,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 40.dp),
        shape = CantoAntd,
        colors = ButtonDefaults.buttonColors(
            containerColor = cor,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFF5F5F5),
            disabledContentColor = Color(0x40000000),
        ),
    ) { Text(texto, fontSize = 15.sp) }
}

@Composable
fun BotaoSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp),
        shape = CantoAntd,
        border = androidx.compose.foundation.BorderStroke(1.dp, BordaCampo),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoEscuro),
    ) { Text(texto, fontSize = 15.sp) }
}

@Composable
fun BotaoCinza(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icone: Int? = null,
    cor: Color = CinzaBotao,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 36.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = contentPadding,
        elevation = null,
        colors = ButtonDefaults.buttonColors(containerColor = cor, contentColor = TextoEscuro),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icone != null) {
                Icon(painterResource(icone), contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun BotaoPilula(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = AzulSinalize, contentColor = Color.White),
    ) { Text(texto, fontSize = 16.sp) }
}

@Composable
fun ConfirmDialog(
    titulo: String,
    rotuloConfirmar: String,
    rotuloCancelar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    texto: String? = null,
    perigoso: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = RoundedCornerShape(8.dp),
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_ant_exclamation_circle),
                contentDescription = null,
                tint = Color(0xFFFAAD14),
                modifier = Modifier.size(28.dp),
            )
        },
        title = { Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
        text = texto?.let { { Text(it, fontSize = 15.sp) } },
        confirmButton = {
            BotaoPrimario(
                texto = rotuloConfirmar,
                onClick = onConfirmar,
                cor = if (perigoso) VermelhoExcluir else AzulSinalize,
            )
        },
        dismissButton = { BotaoSecundario(rotuloCancelar, onCancelar) },
    )
}
