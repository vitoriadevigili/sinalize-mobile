package br.com.sinalize.ui.dicionario

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.sinalize.R
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.BordaCampo
import br.com.sinalize.ui.theme.CinzaBotao
import br.com.sinalize.ui.theme.TextoEscuro
import br.com.sinalize.ui.theme.TextoSecundario
import br.com.sinalize.ui.theme.VermelhoExcluir
import kotlinx.coroutines.launch

@Composable
fun SeletorDeAba(aba: Aba, onSelecionar: (Aba) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x0A000000))
            .padding(2.dp),
    ) {
        Aba.entries.forEach { opcao ->
            val selecionada = opcao == aba
            Surface(
                onClick = { onSelecionar(opcao) },
                shape = RoundedCornerShape(6.dp),
                color = if (selecionada) Color.White else Color.Transparent,
                shadowElevation = if (selecionada) 1.dp else 0.dp,
            ) {
                Text(
                    text = opcao.rotulo,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                    fontSize = 16.sp,
                    color = if (selecionada) TextoEscuro else Color(0xA6000000),
                )
            }
        }
    }
}

@Composable
fun CampoDeBusca(texto: String, onTextoAlterado: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = texto,
        onValueChange = onTextoAlterado,
        singleLine = true,
        textStyle = TextStyle(fontSize = 15.sp, color = TextoEscuro),
        cursorBrush = SolidColor(AzulSinalize),
        modifier = modifier,
        decorationBox = { campo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, BordaCampo, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.ic_ant_search),
                    contentDescription = null,
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (texto.isEmpty()) Text("Pesquisar", color = Color(0xFFBFBFBF), fontSize = 15.sp)
                    campo()
                }
                if (texto.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0x40000000))
                            .clickable { onTextoAlterado("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_ant_close),
                            contentDescription = "Limpar busca",
                            tint = Color.White,
                            modifier = Modifier.size(8.dp),
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun FiltroAlfabetico(letraSelecionada: Char?, onLetra: (Char) -> Unit) {
    val estado = rememberLazyListState()
    val escopo = rememberCoroutineScope()
    val passo = with(LocalDensity.current) { 100.dp.toPx() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        BotaoCircular(onClick = { escopo.launch { estado.animateScrollBy(-passo) } }) {
            Icon(painterResource(R.drawable.ic_ant_left), "Letras anteriores", Modifier.size(12.dp), tint = TextoEscuro)
        }
        LazyRow(
            state = estado,
            modifier = Modifier.weight(1f, fill = false).widthIn(max = 600.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(4.dp),
        ) {
            items(('A'..'Z').toList()) { letra ->
                val selecionada = letra == letraSelecionada
                val fundo by animateColorAsState(if (selecionada) AzulSinalize else CinzaBotao, label = "letra")
                BotaoCircular(onClick = { onLetra(letra) }, fundo = fundo) {
                    Text(
                        text = letra.toString(),
                        color = if (selecionada) Color.White else TextoEscuro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
        BotaoCircular(onClick = { escopo.launch { estado.animateScrollBy(passo) } }) {
            Icon(painterResource(R.drawable.ic_ant_right), "Próximas letras", Modifier.size(12.dp), tint = TextoEscuro)
        }
    }
}

@Composable
private fun BotaoCircular(
    onClick: () -> Unit,
    fundo: Color = CinzaBotao,
    conteudo: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.size(36.dp).clip(CircleShape).background(fundo).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { conteudo() }
}

@Composable
fun CardDoDicionario(
    item: ItemDicionario,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(AzulSinalize, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.nome,
            color = Color.White,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
        )
        IconeDeAcao(R.drawable.ic_ant_edit, "Editar ${item.nome}", Color.White, onEditar)
        IconeDeAcao(R.drawable.ic_ant_delete, "Excluir ${item.nome}", VermelhoExcluir, onExcluir)
    }
}

@Composable
private fun IconeDeAcao(icone: Int, descricao: String, cor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icone), contentDescription = descricao, tint = cor, modifier = Modifier.size(15.dp))
    }
}

@Composable
fun MensagemVazia(aba: Aba) {
    Text(
        text = if (aba == Aba.CATEGORIAS) "Nenhuma categoria encontrada com os filtros informados!"
        else "Nenhuma palavra encontrada com os filtros informados!",
        color = TextoSecundario,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 40.dp),
    )
}

