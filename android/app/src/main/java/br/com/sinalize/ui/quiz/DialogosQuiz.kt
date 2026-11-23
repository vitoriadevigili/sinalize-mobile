package br.com.sinalize.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.sinalize.R
import br.com.sinalize.data.model.Categoria
import br.com.sinalize.ui.components.barraDeRolagem
import br.com.sinalize.ui.components.BotaoPilula
import br.com.sinalize.ui.components.BotaoPrimario
import br.com.sinalize.ui.components.BotaoSecundario
import br.com.sinalize.ui.components.OpcaoSelecao
import br.com.sinalize.ui.components.SeletorMultiplo
import br.com.sinalize.ui.theme.AzulRodape
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.TextoEscuro
import br.com.sinalize.ui.theme.TextoSecundario
import br.com.sinalize.ui.theme.VerdeAcerto
import br.com.sinalize.ui.theme.VermelhoErro

@Composable
fun SelecaoDeCategoriasDialog(
    categorias: List<Categoria>,
    carregando: Boolean,
    selecionadas: List<Long>,
    preparando: Boolean,
    onAlternar: (Long) -> Unit,
    aguardandoAvatar: Boolean = false,
    mensagemErro: String? = null,
    onTentarNovamente: () -> Unit = {},
    onCancelar: () -> Unit,
    onComecar: () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!preparando) onCancelar() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AzulRodape),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
            ) {
                val rolagem = rememberScrollState()
                Column(Modifier.padding(24.dp).barraDeRolagem(rolagem).verticalScroll(rolagem).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Escolha as categorias do quiz",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextoEscuro,
                            modifier = Modifier.weight(1f),
                        )
                        if (!preparando) {
                            Box(
                                Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onCancelar),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(painterResource(R.drawable.ic_ant_close), "Fechar", Modifier.size(14.dp), TextoSecundario)
                            }
                        }
                    }

                    if (preparando) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CircularProgressIndicator(color = AzulSinalize)
                            Text(
                                "Preparando o seu quiz...",
                                color = AzulSinalize,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                            if (aguardandoAvatar) {
                                Text(
                                    "Aguardando o personagem 3D terminar de carregar.",
                                    color = TextoSecundario,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    } else {
                        Text(
                            "Selecione uma ou mais categorias para montar o seu quiz.",
                            color = TextoSecundario,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                        )
                        if (mensagemErro != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(mensagemErro, color = VermelhoErro, fontSize = 14.sp, textAlign = TextAlign.Center)
                                if (categorias.isEmpty() && !carregando) {
                                    BotaoSecundario("Tentar novamente", onTentarNovamente, Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                        if (carregando) {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AzulSinalize, modifier = Modifier.size(28.dp))
                            }
                        } else {
                            SeletorMultiplo(
                                opcoes = categorias.map { OpcaoSelecao(it.id, it.nome) },
                                selecionadas = selecionadas,
                                onAlternar = onAlternar,
                                placeholder = "Selecione",
                                textoSemOpcoes = "Nenhuma categoria cadastrada.",
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        ) {
                            BotaoSecundario("Cancelar", onCancelar)
                            BotaoPrimario("Começar", onComecar, enabled = selecionadas.isNotEmpty())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultadoDialog(
    acertos: Int,
    total: Int,
    onJogarNovamente: () -> Unit,
    onVoltar: () -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    mensagemPorDesempenho(acertos, total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextoEscuro,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "$acertos / $total corretas",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 24.dp, bottom = 28.dp)
                        .clip(CircleShape)
                        .background(VerdeAcerto)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotaoPilula("Jogar novamente", onJogarNovamente, Modifier.width(220.dp))
                    BotaoPilula("Voltar", onVoltar, Modifier.width(220.dp))
                }
            }
        }
    }
}
