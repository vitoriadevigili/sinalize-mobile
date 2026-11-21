package br.com.sinalize.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.sinalize.SinalizeApplication
import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.data.model.Quiz
import br.com.sinalize.data.repository.CategoriaRepository
import br.com.sinalize.data.repository.PalavraRepository
import br.com.sinalize.data.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FaseQuiz { SELECIONANDO, CARREGANDO, JOGANDO, FINALIZADO }

data class QuizUiState(
    val fase: FaseQuiz = FaseQuiz.SELECIONANDO,
    val categorias: List<Categoria> = emptyList(),
    val carregandoCategorias: Boolean = true,
    val selecionadas: List<Long> = emptyList(),
    val preparando: Boolean = false,
    /** Já tocou em "Começar", mas o avatar do VLibras ainda está carregando. */
    val aguardandoAvatar: Boolean = false,
    val quiz: Quiz? = null,
    val indice: Int = 0,
    val resultados: List<Boolean?> = emptyList(),
    val opcoes: List<PalavraResumo> = emptyList(),
    val idSelecionado: Long? = null,
    val respondido: Boolean = false,
    val erro: String? = null,
) {
    val total: Int get() = quiz?.quizPalavras?.size ?: 0
    val palavraCorreta: PalavraResumo? get() = quiz?.quizPalavras?.getOrNull(indice)?.palavra
    val acertos: Int get() = resultados.count { it == true }
}

fun mensagemPorDesempenho(acertos: Int, total: Int): String {
    val proporcao = if (total > 0) acertos.toDouble() / total else 0.0
    return when {
        proporcao == 1.0 -> "Perfeito!"
        proporcao >= 0.7 -> "Muito bem!"
        proporcao >= 0.4 -> "Foi quase!"
        else -> "Continue praticando!"
    }
}

class QuizViewModel(
    private val categoriaRepository: CategoriaRepository,
    private val palavraRepository: PalavraRepository,
    private val quizRepository: QuizRepository,
    private val esperaPreparoMs: Long = ESPERA_PREPARO_MS,
    private val esperaAvancoMs: Long = ESPERA_AVANCO_MS,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var tarefa: Job? = null

    /**
     * O quiz só começa quando o avatar terminou de carregar (ou falhou e mostra "Tentar novamente"),
     * para a primeira pergunta não aparecer sem o sinal. A tela informa isso com [definirAvatarDisponivel].
     */
    private val avatarDisponivel = MutableStateFlow(false)

    fun definirAvatarDisponivel(disponivel: Boolean) {
        avatarDisponivel.value = disponivel
    }

    init {
        carregarCategorias()
    }

    /** Busca as categorias (GET /categorias). Também é o "Tentar novamente" da janela de categorias. */
    fun carregarCategorias() {
        _uiState.update { it.copy(carregandoCategorias = true, erro = null) }
        viewModelScope.launch {
            val resultado = categoriaRepository.listar()
            _uiState.update {
                it.copy(
                    carregandoCategorias = false,
                    categorias = resultado.getOrDefault(it.categorias),
                    erro = resultado.exceptionOrNull()?.let { e -> e.message ?: "Não foi possível carregar as categorias." },
                )
            }
        }
    }

    fun alternarCategoria(id: Long) = _uiState.update {
        it.copy(selecionadas = if (id in it.selecionadas) it.selecionadas - id else it.selecionadas + id)
    }

    fun comecar() {
        val categoriaIds = _uiState.value.selecionadas
        if (categoriaIds.isEmpty() || _uiState.value.preparando) return
        tarefa?.cancel()
        tarefa = viewModelScope.launch {
            _uiState.update { it.copy(preparando = true, erro = null) }
            delay(esperaPreparoMs)
            if (!avatarDisponivel.value) {
                _uiState.update { it.copy(aguardandoAvatar = true) }
                avatarDisponivel.first { it }
            }
            _uiState.update { it.copy(preparando = false, aguardandoAvatar = false, fase = FaseQuiz.CARREGANDO) }

            quizRepository.criar(categoriaIds)
                .onSuccess { quiz ->
                    _uiState.update {
                        it.copy(
                            fase = FaseQuiz.JOGANDO,
                            quiz = quiz,
                            indice = 0,
                            resultados = List(quiz.quizPalavras.size) { null },
                        )
                    }
                    montarAlternativas()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(fase = FaseQuiz.SELECIONANDO, erro = e.message ?: "Não foi possível criar o quiz.")
                    }
                }
        }
    }

    private suspend fun montarAlternativas() {
        val correta = _uiState.value.palavraCorreta ?: return
        val erradas = palavraRepository.aleatorias(excluir = listOf(correta.id), quantidade = 3)
            .getOrDefault(emptyList())
        _uiState.update { it.copy(opcoes = (erradas + correta).shuffled()) }
    }

    fun responder(opcao: PalavraResumo) {
        val estado = _uiState.value
        val correta = estado.palavraCorreta ?: return
        val item = estado.quiz?.quizPalavras?.getOrNull(estado.indice) ?: return
        if (estado.respondido || estado.fase != FaseQuiz.JOGANDO) return

        val acertou = opcao.id == correta.id
        _uiState.update {
            it.copy(
                idSelecionado = opcao.id,
                respondido = true,
                resultados = it.resultados.toMutableList().also { lista -> lista[it.indice] = acertou },
            )
        }

        tarefa = viewModelScope.launch {
            launch { quizRepository.responder(item.id, acertou) }
            delay(esperaAvancoMs)
            if (estado.indice + 1 < estado.total) {
                _uiState.update {
                    it.copy(indice = it.indice + 1, opcoes = emptyList(), idSelecionado = null, respondido = false)
                }
                montarAlternativas()
            } else {
                _uiState.update { it.copy(fase = FaseQuiz.FINALIZADO) }
            }
        }
    }

    fun reiniciar() {
        tarefa?.cancel()
        _uiState.update {
            QuizUiState(
                categorias = it.categorias,
                carregandoCategorias = false,
                selecionadas = it.selecionadas,
            )
        }
    }

    fun erroExibido() = _uiState.update { it.copy(erro = null) }

    companion object {
        /**
         * Tempo mínimo do "Preparando o seu quiz...". Na web eram 3 s fixos (para dar tempo ao VLibras);
         * aqui o quiz espera o avatar de verdade, então basta um instante para a transição não piscar.
         */
        const val ESPERA_PREPARO_MS = 800L

        const val ESPERA_AVANCO_MS = 1_500L

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SinalizeApplication).container
                QuizViewModel(container.categoriaRepository, container.palavraRepository, container.quizRepository)
            }
        }
    }
}
