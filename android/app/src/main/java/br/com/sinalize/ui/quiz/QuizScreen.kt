package br.com.sinalize.ui.quiz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.sinalize.SinalizeApplication
import br.com.sinalize.ui.components.ConteudoComRodape
import br.com.sinalize.vlibras.EstadoVLibras
import br.com.sinalize.ui.components.PaginaComHeader
import br.com.sinalize.ui.components.Responsivo

@Composable
fun QuizScreen(
    onVoltarAoMenu: () -> Unit,
    viewModel: QuizViewModel = viewModel(factory = QuizViewModel.Factory),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // Avatar do VLibras (já vem carregando desde a Home). O quiz só começa quando ele estiver disponível.
    val motor = (LocalContext.current.applicationContext as SinalizeApplication).container.vlibras
    val estadoAvatar by motor.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estadoAvatar) {
        viewModel.definirAvatarDisponivel(estadoAvatar != EstadoVLibras.Carregando)
    }

    // Na escolha de categorias o erro aparece dentro da janela; nas outras fases, num aviso (snackbar).
    LaunchedEffect(estado.erro, estado.fase) {
        if (estado.fase == FaseQuiz.SELECIONANDO) return@LaunchedEffect
        estado.erro?.let {
            snackbar.showSnackbar(it)
            viewModel.erroExibido()
        }
    }

    PaginaComHeader(onVoltarAoMenu = onVoltarAoMenu) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val alturaDoVideo = maxHeight * 0.55f
            ConteudoComRodape {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    BoxWithConstraints(
                        Modifier
                            .widthIn(max = Responsivo.LARGURA_MAXIMA_QUIZ)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                    ) {
                        val colunas = Responsivo.colunasDasAlternativas(maxWidth)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (estado.quiz != null && estado.fase != FaseQuiz.SELECIONANDO) {
                                BarraDeProgresso(estado.resultados, estado.indice)
                            }

                            // Como na web: o avatar só sinaliza durante o jogo (e repete a última no resultado).
                            val jogandoOuFim = estado.fase == FaseQuiz.JOGANDO || estado.fase == FaseQuiz.FINALIZADO
                            AreaDoVLibras(
                                motor = motor,
                                palavra = if (jogandoOuFim) estado.palavraCorreta?.palavra.orEmpty() else "",
                                alturaMaxima = alturaDoVideo,
                                montandoQuiz = estado.fase == FaseQuiz.CARREGANDO,
                                podeRepetir = estado.fase == FaseQuiz.JOGANDO,
                            )
                            Spacer(Modifier.height(24.dp))

                            val correta = estado.palavraCorreta
                            if (estado.fase == FaseQuiz.JOGANDO && correta != null && estado.opcoes.isNotEmpty()) {
                                Alternativas(
                                    opcoes = estado.opcoes,
                                    idCorreto = correta.id,
                                    idSelecionado = estado.idSelecionado,
                                    respondido = estado.respondido,
                                    colunas = colunas,
                                    onSelecionar = viewModel::responder,
                                )
                            }
                        }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }

    if (estado.fase == FaseQuiz.SELECIONANDO) {
        SelecaoDeCategoriasDialog(
            categorias = estado.categorias,
            carregando = estado.carregandoCategorias,
            mensagemErro = estado.erro,
            onTentarNovamente = viewModel::carregarCategorias,
            selecionadas = estado.selecionadas,
            preparando = estado.preparando,
            aguardandoAvatar = estado.aguardandoAvatar,
            onAlternar = viewModel::alternarCategoria,
            onCancelar = onVoltarAoMenu,
            onComecar = viewModel::comecar,
        )
    }

    if (estado.fase == FaseQuiz.FINALIZADO) {
        ResultadoDialog(
            acertos = estado.acertos,
            total = estado.total,
            onJogarNovamente = viewModel::reiniciar,
            onVoltar = onVoltarAoMenu,
        )
    }
}
