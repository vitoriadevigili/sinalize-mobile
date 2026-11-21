package br.com.sinalize.data.repository

import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.data.model.Quiz

interface CategoriaRepository {
    /** GET /categorias */
    suspend fun listar(): Result<List<Categoria>>

    /** POST /categorias */
    suspend fun criar(nome: String, descricao: String): Result<Categoria>

    /** PATCH /categorias/{id} */
    suspend fun editar(id: Long, nome: String, descricao: String): Result<Categoria>

    /** DELETE /categorias/{id} */
    suspend fun excluir(id: Long): Result<Unit>
}

interface PalavraRepository {
    /** GET /palavras */
    suspend fun listar(): Result<List<Palavra>>

    /** POST /palavras */
    suspend fun criar(palavra: String, categoriaIds: List<Long>): Result<Palavra>

    /** PATCH /palavras/{id} */
    suspend fun editar(id: Long, palavra: String, categoriaIds: List<Long>): Result<Palavra>

    /** DELETE /palavras/{id} */
    suspend fun excluir(id: Long): Result<Unit>

    /** GET /palavras/aleatorias?excluir=..&quantidade=.. */
    suspend fun aleatorias(excluir: List<Long>, quantidade: Int): Result<List<PalavraResumo>>
}

interface QuizRepository {
    /** POST /quizzes */
    suspend fun criar(categoriaIds: List<Long>): Result<Quiz>

    /** PATCH /quizzes/palavras/{id}/responder */
    suspend fun responder(quizPalavraId: Long, acertou: Boolean): Result<Unit>
}
