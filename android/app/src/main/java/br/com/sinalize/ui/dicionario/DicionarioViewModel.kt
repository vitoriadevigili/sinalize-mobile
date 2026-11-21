package br.com.sinalize.ui.dicionario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.sinalize.SinalizeApplication
import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.repository.CategoriaRepository
import br.com.sinalize.data.repository.PalavraRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ITENS_POR_PAGINA = 24

sealed interface DialogoDicionario {
    data class EditarPalavra(val palavra: Palavra?) : DialogoDicionario
    data class EditarCategoria(val categoria: Categoria?) : DialogoDicionario
    data class ConfirmarExclusao(val aba: Aba, val id: Long) : DialogoDicionario
}

data class DicionarioUiState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val palavras: List<Palavra> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val aba: Aba = Aba.PALAVRAS,
    val busca: String = "",
    val letra: Char? = null,
    val ordenacao: Ordenacao = Ordenacao.AZ,
    val limite: Int = ITENS_POR_PAGINA,
    val dialogo: DialogoDicionario? = null,
    val salvando: Boolean = false,
    val mensagem: String? = null,
) {
    private val itensDaAba: List<ItemDicionario>
        get() = when (aba) {
            Aba.PALAVRAS -> palavras.map { ItemDicionario(it.id, it.palavra) }
            Aba.CATEGORIAS -> categorias.map { ItemDicionario(it.id, it.nome) }
        }

    val filtrados: List<ItemDicionario> by lazy { filtrarEOrdenar(itensDaAba, busca, letra, ordenacao) }
    val visiveis: List<ItemDicionario> get() = filtrados.take(limite)

    val total: Int get() = itensDaAba.size
    val temMais: Boolean get() = filtrados.size > limite
}

class DicionarioViewModel(
    private val palavraRepository: PalavraRepository,
    private val categoriaRepository: CategoriaRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DicionarioUiState())
    val uiState: StateFlow<DicionarioUiState> = _uiState.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        _uiState.update { it.copy(carregando = true, erro = null) }
        viewModelScope.launch {
            val palavras = async { palavraRepository.listar() }
            val categorias = async { categoriaRepository.listar() }
            val rPalavras = palavras.await()
            val rCategorias = categorias.await()
            val falha = rPalavras.exceptionOrNull() ?: rCategorias.exceptionOrNull()
            _uiState.update {
                if (falha != null) {
                    it.copy(carregando = false, erro = falha.message ?: "Não foi possível carregar o dicionário.")
                } else {
                    it.copy(
                        carregando = false,
                        palavras = rPalavras.getOrThrow(),
                        categorias = rCategorias.getOrThrow(),
                    )
                }
            }
        }
    }

    fun selecionarAba(aba: Aba) = _uiState.update {
        it.copy(aba = aba, letra = null, busca = "", limite = ITENS_POR_PAGINA)
    }

    fun buscar(texto: String) = _uiState.update { it.copy(busca = texto, limite = ITENS_POR_PAGINA) }

    fun alternarLetra(letra: Char) = _uiState.update {
        it.copy(letra = if (it.letra == letra) null else letra, limite = ITENS_POR_PAGINA)
    }

    fun ordenar(ordenacao: Ordenacao) = _uiState.update { it.copy(ordenacao = ordenacao) }

    fun verMais() = _uiState.update { it.copy(limite = it.limite + ITENS_POR_PAGINA) }

    fun abrirCriacao() = _uiState.update {
        it.copy(
            dialogo = when (it.aba) {
                Aba.PALAVRAS -> DialogoDicionario.EditarPalavra(null)
                Aba.CATEGORIAS -> DialogoDicionario.EditarCategoria(null)
            }
        )
    }

    fun abrirEdicao(id: Long) = _uiState.update { estado ->
        val dialogo = when (estado.aba) {
            Aba.PALAVRAS -> estado.palavras.firstOrNull { it.id == id }?.let { DialogoDicionario.EditarPalavra(it) }
            Aba.CATEGORIAS -> estado.categorias.firstOrNull { it.id == id }?.let { DialogoDicionario.EditarCategoria(it) }
        }
        estado.copy(dialogo = dialogo)
    }

    fun pedirExclusao(id: Long) = _uiState.update {
        it.copy(dialogo = DialogoDicionario.ConfirmarExclusao(it.aba, id))
    }

    fun fecharDialogo() = _uiState.update { it.copy(dialogo = null) }

    fun mensagemExibida() = _uiState.update { it.copy(mensagem = null) }

    fun salvarPalavra(id: Long?, palavra: String, categoriaIds: List<Long>) = executar(
        sucesso = if (id == null) "Palavra adicionada." else "Palavra atualizada.",
    ) {
        if (id == null) palavraRepository.criar(palavra, categoriaIds).map { }
        else palavraRepository.editar(id, palavra, categoriaIds).map { }
    }

    fun salvarCategoria(id: Long?, nome: String, descricao: String) = executar(
        sucesso = if (id == null) "Categoria adicionada." else "Categoria atualizada.",
    ) {
        if (id == null) categoriaRepository.criar(nome, descricao).map { }
        else categoriaRepository.editar(id, nome, descricao).map { }
    }

    fun confirmarExclusao() {
        val dialogo = _uiState.value.dialogo as? DialogoDicionario.ConfirmarExclusao ?: return
        when (dialogo.aba) {
            Aba.PALAVRAS -> executar("Palavra excluída.") { palavraRepository.excluir(dialogo.id) }
            Aba.CATEGORIAS -> executar("Categoria excluída.") { categoriaRepository.excluir(dialogo.id) }
        }
    }

    private fun executar(sucesso: String, operacao: suspend () -> Result<Unit>) {
        if (_uiState.value.salvando) return
        _uiState.update { it.copy(salvando = true) }
        viewModelScope.launch {
            val resultado = operacao()
            val palavras = palavraRepository.listar().getOrNull()
            val categorias = categoriaRepository.listar().getOrNull()
            _uiState.update {
                it.copy(
                    salvando = false,
                    dialogo = if (resultado.isSuccess) null else it.dialogo,
                    palavras = palavras ?: it.palavras,
                    categorias = categorias ?: it.categorias,
                    mensagem = resultado.fold(
                        onSuccess = { sucesso },
                        onFailure = { e -> e.message ?: "Não foi possível concluir a operação." },
                    ),
                )
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SinalizeApplication).container
                DicionarioViewModel(container.palavraRepository, container.categoriaRepository)
            }
        }
    }
}
