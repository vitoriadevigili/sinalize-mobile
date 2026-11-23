package br.com.sinalize.ui.dicionario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import br.com.sinalize.ui.theme.AzulRodape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.sinalize.R
import br.com.sinalize.ui.components.barraDeRolagem
import br.com.sinalize.ui.components.BotaoCinza
import br.com.sinalize.ui.components.BotaoPrimario
import br.com.sinalize.ui.components.ConfirmDialog
import br.com.sinalize.ui.components.Footer
import br.com.sinalize.ui.components.PaginaComHeader
import br.com.sinalize.ui.components.Responsivo
import br.com.sinalize.ui.theme.AzulSinalize
import br.com.sinalize.ui.theme.CinzaPendente
import br.com.sinalize.ui.theme.TextoEscuro

@Composable
fun DicionarioScreen(
    onVoltarAoMenu: () -> Unit,
    viewModel: DicionarioViewModel = viewModel(factory = DicionarioViewModel.Factory),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(estado.mensagem) {
        estado.mensagem?.let {
            snackbar.showSnackbar(it)
            viewModel.mensagemExibida()
        }
    }

    PaginaComHeader(onVoltarAoMenu = onVoltarAoMenu) {
        Box(Modifier.fillMaxSize()) {
            when {
                estado.carregando -> Carregando()
                estado.erro != null -> ErroAoCarregar(estado.erro!!, viewModel::carregar)
                else -> ConteudoDoDicionario(estado, viewModel)
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
        }
    }

    when (val dialogo = estado.dialogo) {
        is DialogoDicionario.EditarPalavra -> PalavraDialog(
            palavraEditada = dialogo.palavra,
            categorias = estado.categorias,
            salvando = estado.salvando,
            onCancelar = viewModel::fecharDialogo,
            onSalvar = { texto, ids -> viewModel.salvarPalavra(dialogo.palavra?.id, texto, ids) },
        )
        is DialogoDicionario.EditarCategoria -> CategoriaDialog(
            categoriaEditada = dialogo.categoria,
            salvando = estado.salvando,
            onCancelar = viewModel::fecharDialogo,
            onSalvar = { nome, descricao -> viewModel.salvarCategoria(dialogo.categoria?.id, nome, descricao) },
        )
        is DialogoDicionario.ConfirmarExclusao -> ConfirmDialog(
            titulo = if (dialogo.aba == Aba.PALAVRAS) "Deseja realmente excluir esta palavra?"
            else "Deseja realmente excluir esta categoria?",
            rotuloConfirmar = "Sim, excluir",
            rotuloCancelar = "Cancelar",
            perigoso = true,
            onConfirmar = viewModel::confirmarExclusao,
            onCancelar = viewModel::fecharDialogo,
        )
        null -> Unit
    }
}

@Composable
private fun ConteudoDoDicionario(estado: DicionarioUiState, viewModel: DicionarioViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val larguraTotal = maxWidth
        val margem: Dp = max(16.dp, (larguraTotal - Responsivo.LARGURA_MAXIMA_PAGINA) / 2)
        val colunas = Responsivo.colunasDoDicionario(larguraTotal - margem * 2)
        val estadoDaGrade = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Fixed(colunas),
            state = estadoDaGrade,
            modifier = Modifier
                .fillMaxSize()
                .barraDeRolagem(estadoDaGrade)
                .drawBehind {
                    val info = estadoDaGrade.layoutInfo
                    val ultimo = info.visibleItemsInfo.lastOrNull() ?: return@drawBehind
                    if (ultimo.index == info.totalItemsCount - 1) {
                        val fim = (ultimo.offset.y + ultimo.size.height).toFloat()
                        if (fim < size.height) {
                            drawRect(AzulRodape, topLeft = Offset(0f, fim), size = Size(size.width, size.height - fim))
                        }
                    }
                },
            contentPadding = PaddingValues(start = margem, end = margem, top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            linhaInteira { Cabecalho(estado, viewModel) }

            if (estado.filtrados.isEmpty()) {
                linhaInteira {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { MensagemVazia(estado.aba) }
                }
            }

            items(estado.visiveis, key = { "${estado.aba}-${it.id}" }) { item ->
                CardDoDicionario(
                    item = item,
                    onEditar = { viewModel.abrirEdicao(item.id) },
                    onExcluir = { viewModel.pedirExclusao(item.id) },
                )
            }

            if (estado.temMais) {
                linhaInteira {
                    Box(Modifier.fillMaxWidth().padding(top = 28.dp), contentAlignment = Alignment.Center) {
                        BotaoCinza(
                            texto = "Ver mais",
                            onClick = viewModel::verMais,
                            cor = CinzaPendente,
                            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            linhaInteira {
                Footer(Modifier.padding(top = 40.dp).requiredWidth(larguraTotal))
            }
        }
    }
}

private fun LazyGridScope.linhaInteira(conteudo: @Composable () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) { conteudo() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Cabecalho(estado: DicionarioUiState, viewModel: DicionarioViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SeletorDeAba(estado.aba, viewModel::selecionarAba)

        CampoDeBusca(
            texto = estado.busca,
            onTextoAlterado = viewModel::buscar,
            modifier = Modifier.padding(vertical = 18.dp).widthIn(max = 460.dp).fillMaxWidth(),
        )

        FiltroAlfabetico(estado.letra, viewModel::alternarLetra)

        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            BotaoCinza(
                texto = if (estado.aba == Aba.CATEGORIAS) "Criar categoria" else "Criar palavra",
                icone = R.drawable.ic_ant_plus,
                onClick = viewModel::abrirCriacao,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Exibindo ${estado.filtrados.size} de ${estado.total}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextoEscuro,
                )
                MenuOrdenar(estado.ordenacao, viewModel::ordenar)
            }
        }
    }
}

@Composable
private fun MenuOrdenar(atual: Ordenacao, onOrdenar: (Ordenacao) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    Box {
        BotaoCinza(texto = "Ordenar", icone = R.drawable.ic_ant_sort_ascending, onClick = { aberto = true })
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            Ordenacao.entries.forEach { opcao ->
                DropdownMenuItem(
                    text = {
                        Text(
                            opcao.rotulo,
                            color = if (opcao == atual) AzulSinalize else TextoEscuro,
                            fontWeight = if (opcao == atual) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onOrdenar(opcao)
                        aberto = false
                    },
                )
            }
        }
    }
}

@Composable
private fun Carregando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AzulSinalize)
    }
}

@Composable
private fun ErroAoCarregar(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(mensagem, color = TextoEscuro, fontSize = 16.sp)
        BotaoPrimario("Tentar novamente", onTentarNovamente)
    }
}
