package br.com.sinalize.data.remote

import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.data.model.Quiz
import br.com.sinalize.data.repository.CategoriaRepository
import br.com.sinalize.data.repository.PalavraRepository
import br.com.sinalize.data.repository.QuizRepository
import kotlinx.serialization.json.Json


class RemoteCategoriaRepository(
    private val api: SinalizeApi,
    private val json: Json,
) : CategoriaRepository {

    override suspend fun listar(): Result<List<Categoria>> =
        chamarApi(json) { api.listarCategorias().map { it.paraModelo() } }

    override suspend fun criar(nome: String, descricao: String): Result<Categoria> =
        chamarApi(json) { api.criarCategoria(CategoriaRequestDto(nome, descricao)).paraModelo() }

    override suspend fun editar(id: Long, nome: String, descricao: String): Result<Categoria> =
        chamarApi(json) { api.editarCategoria(id, CategoriaRequestDto(nome, descricao)).paraModelo() }

    override suspend fun excluir(id: Long): Result<Unit> =
        chamarApi(json) { api.excluirCategoria(id) }
}

class RemotePalavraRepository(
    private val api: SinalizeApi,
    private val json: Json,
) : PalavraRepository {

    override suspend fun listar(): Result<List<Palavra>> =
        chamarApi(json) { api.listarPalavras().map { it.paraModelo() } }

    override suspend fun criar(palavra: String, categoriaIds: List<Long>): Result<Palavra> =
        chamarApi(json) { api.criarPalavra(PalavraRequestDto(palavra, categoriaIds)).paraModelo() }

    override suspend fun editar(id: Long, palavra: String, categoriaIds: List<Long>): Result<Palavra> =
        chamarApi(json) { api.editarPalavra(id, PalavraRequestDto(palavra, categoriaIds)).paraModelo() }

    override suspend fun excluir(id: Long): Result<Unit> =
        chamarApi(json) { api.excluirPalavra(id) }

    override suspend fun aleatorias(excluir: List<Long>, quantidade: Int): Result<List<PalavraResumo>> =
        chamarApi(json) {
            api.palavrasAleatorias(excluir = excluir.joinToString(","), quantidade = quantidade)
                .map { it.paraModelo() }
        }
}

class RemoteQuizRepository(
    private val api: SinalizeApi,
    private val json: Json,
) : QuizRepository {

    override suspend fun criar(categoriaIds: List<Long>): Result<Quiz> =
        chamarApi(json) { api.criarQuiz(QuizRequestDto(categoriaIds)).paraModelo() }

    override suspend fun responder(quizPalavraId: Long, acertou: Boolean): Result<Unit> =
        chamarApi(json) { api.responder(quizPalavraId, RespostaRequestDto(acertou)) }
}
