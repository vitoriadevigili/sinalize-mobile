package br.com.sinalize.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.R
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.BordaCampo
import br.com.sinalize.ui.theme.TextoEscuro
import br.com.sinalize.ui.theme.TextoSecundario

data class OpcaoSelecao(val id: Long, val rotulo: String)

private val FundoEtiqueta = Color(0x0F000000)
private val FundoOpcaoMarcada = Color(0xFFE6F0FF)
private val CorPlaceholder = Color(0xFFBFBFBF)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SeletorMultiplo(
    opcoes: List<OpcaoSelecao>,
    selecionadas: List<Long>,
    onAlternar: (Long) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecione",
    textoSemOpcoes: String = "Nenhuma opção disponível",
    habilitado: Boolean = true,
) {
    var aberto by remember { mutableStateOf(false) }
    val gerenciadorDeFoco = LocalFocusManager.current
    val teclado = LocalSoftwareKeyboardController.current
    var larguraPx by remember { mutableIntStateOf(0) }
    val larguraDp = with(LocalDensity.current) { larguraPx.toDp() }
    val giroDaSeta by animateFloatAsState(if (aberto) 180f else 0f, label = "seta")
    val escolhidas = opcoes.filter { it.id in selecionadas }

    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .onSizeChanged { larguraPx = it.width }
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, if (aberto) AzulSinalize else BordaCampo, RoundedCornerShape(6.dp))
                .clickable(enabled = habilitado) {
                    gerenciadorDeFoco.clearFocus()
                    teclado?.hide()
                    aberto = !aberto
                }
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (escolhidas.isEmpty()) {
                    Text(placeholder, color = CorPlaceholder, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        escolhidas.forEach { opcao ->
                            Etiqueta(opcao.rotulo, onRemover = { onAlternar(opcao.id) }, habilitado = habilitado)
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_ant_down),
                contentDescription = if (aberto) "Fechar lista" else "Abrir lista",
                tint = CorPlaceholder,
                modifier = Modifier.size(12.dp).rotate(giroDaSeta),
            )
        }

        DropdownMenu(
            expanded = aberto,
            onDismissRequest = { aberto = false },
            modifier = Modifier.width(larguraDp).heightIn(max = 280.dp),
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp),
        ) {
            if (opcoes.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(textoSemOpcoes, color = TextoSecundario) },
                    onClick = {},
                    enabled = false,
                )
            }
            opcoes.forEach { opcao ->
                val marcada = opcao.id in selecionadas
                DropdownMenuItem(
                    text = {
                        Text(
                            opcao.rotulo,
                            fontWeight = if (marcada) FontWeight.SemiBold else FontWeight.Normal,
                            color = TextoEscuro,
                        )
                    },
                    trailingIcon = if (marcada) {
                        { Icon(painterResource(R.drawable.ic_ant_check), null, Modifier.size(14.dp), AzulSinalize) }
                    } else null,
                    onClick = { onAlternar(opcao.id) },
                    modifier = Modifier.background(if (marcada) FundoOpcaoMarcada else Color.Transparent),
                    colors = MenuDefaults.itemColors(textColor = TextoEscuro),
                )
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, onRemover: () -> Unit, habilitado: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(FundoEtiqueta)
            .padding(start = 8.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(texto, fontSize = 14.sp, color = TextoEscuro, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(enabled = habilitado, onClick = onRemover),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ant_close),
                contentDescription = "Remover $texto",
                tint = TextoSecundario,
                modifier = Modifier.size(10.dp),
            )
        }
    }
}
