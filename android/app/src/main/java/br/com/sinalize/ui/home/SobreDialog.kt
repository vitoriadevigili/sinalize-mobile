package br.com.sinalize.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.R
import br.com.sinalize.ui.components.barraDeRolagem
import br.com.sinalize.ui.components.BotaoPilula
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.TextoEscuro

private const val REPOSITORIO = "https://github.com/vitoriadevigili/sinalize-mobile"

private val alunos =listOf(
    "Pedro Paoli Neto",
    "Luís Felipe dos Anjos de Carvalho",
    "Isadora Pimenta de Oliveira",
    "Larissa Colzani",
    "Vitória Carolina Devigili"
)

@Composable
fun SobreDialog(onFechar: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onFechar,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text("Sobre o Projeto", color = AzulSinalize, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        },
        text = {
            val rolagem = rememberScrollState()
            Column(Modifier.barraDeRolagem(rolagem).verticalScroll(rolagem).padding(end = 12.dp)) {
                Text(
                    text = buildAnnotatedString {
                        append("O software ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Sinalize") }
                        append(
                            " foi desenvolvido como proposta de projeto da matéria de Programação para Dispositivos Móveis, " +
                                "do curso de Tecnologia em Análise e Desenvolvimento de Sistemas, na UDESC."
                        )
                    },
                    fontSize = 16.sp,
                    textAlign = TextAlign.Justify,
                    color = TextoEscuro,
                )

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text(
                    "Alunos responsáveis pelo desenvolvimento do projeto:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextoEscuro,
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    alunos.forEach { Text(it, fontSize = 15.sp, color = TextoEscuro) }
                }

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                Text("Acesso ao repositório:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextoEscuro)
                Column(
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LinkGithub("Sinalize Mobile (Android + Spring Boot)") {
                        uriHandler.openUri(REPOSITORIO)
                    }
                }
            }
        },
        confirmButton = { BotaoPilula("Fechar", onFechar) },
    )
}

@Composable
private fun LinkGithub(texto: String, @DrawableRes icone: Int = R.drawable.ic_ant_github, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(icone), contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
        Text(texto, color = AzulSinalize, fontSize = 15.sp)
    }
}
