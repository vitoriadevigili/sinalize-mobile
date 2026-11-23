package br.com.sinalize.data.repository

import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.data.model.Quiz

interface CategoriaRepository {
    suspend fun listar(): Result<List<Categoria>>

    suspend fun criar(nome: String, descricao: String): Result<Categoria>

    suspend fun editar(id: Long, nome: String, descricao: String): Result<Categoria>

    suspend fun excluir(id: Long): Result<Unit>
}

interface PalavraRepository {
    suspend fun listar(): Result<List<Palavra>>

    suspend fun criar(palavra: String, categoriaIds: List<Long>): Result<Palavra>

    suspend fun editar(id: Long, palavra: String, categoriaIds: List<Long>): Result<Palavra>

    suspend fun excluir(id: Long): Result<Unit>

    suspend fun aleatorias(excluir: List<Long>, quantidade: Int): Result<List<PalavraResumo>>
}

interface QuizRepository {
    suspend fun criar(categoriaIds: List<Long>): Result<Quiz>

    suspend fun responder(quizPalavraId: Long, acertou: Boolean): Result<Unit>
}
