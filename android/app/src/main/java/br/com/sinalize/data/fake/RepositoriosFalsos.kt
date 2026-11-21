package br.com.sinalize.data.fake

import br.com.sinalize.data.model.Categoria
import br.com.sinalize.data.model.Palavra
import br.com.sinalize.data.model.PalavraResumo
import br.com.sinalize.data.model.Quiz
import br.com.sinalize.data.model.QuizPalavra
import br.com.sinalize.data.repository.CategoriaRepository
import br.com.sinalize.data.repository.PalavraRepository
import br.com.sinalize.data.repository.QuizRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/*
 * Implementações em memória dos repositórios, com dados ilustrativos.
 * As alterações (criar, editar, excluir) valem enquanto o app estiver aberto.
 * O pequeno atraso imita o tempo de resposta da rede, para os estados de carregamento aparecerem.
 */

/** Estado compartilhado entre os repositórios falsos (palavras referenciam categorias). */
class BancoEmMemoria(
    categorias: List<Categoria> = DadosIlustrativos.categorias,
    palavras: List<Palavra> = DadosIlustrativos.palavras,
) {
    val categorias = MutableStateFlow(categorias)
    val palavras = MutableStateFlow(palavras)
    private var proximoId = 1000L

    fun novoId(): Long = proximoId++
}

private const val ATRASO_SIMULADO_MS = 300L

class FakeCategoriaRepository(
    private val banco: BancoEmMemoria,
    private val atrasoMs: Long = ATRASO_SIMULADO_MS,
) : CategoriaRepository {

    override suspend fun listar(): Result<List<Categoria>> {
        delay(atrasoMs)
        return Result.success(banco.categorias.value)
    }

    override suspend fun criar(nome: String, descricao: String): Result<Categoria> {
        delay(atrasoMs)
        if (banco.categorias.value.any { it.nome.equals(nome.trim(), ignoreCase = true) }) {
            return Result.failure(IllegalStateException("Já existe uma categoria com esse nome."))
        }
        val nova = Categoria(banco.novoId(), nome.trim(), descricao.trim(), DadosIlustrativos.administrador)
        banco.categorias.update { it + nova }
        return Result.success(nova)
    }

    override suspend fun editar(id: Long, nome: String, descricao: String): Result<Categoria> {
        delay(atrasoMs)
        val atual = banco.categorias.value.firstOrNull { it.id == id }
            ?: return Result.failure(NoSuchElementException("Categoria não encontrada."))
        val editada = atual.copy(nome = nome.trim(), descricao = descricao.trim())
        banco.categorias.update { lista -> lista.map { if (it.id == id) editada else it } }
        // Mantém o resumo da categoria atualizado nas palavras que a usam.
        banco.palavras.update { lista ->
            lista.map { p -> p.copy(categorias = p.categorias.map { if (it.id == id) editada.resumo() else it }) }
        }
        return Result.success(editada)
    }

    override suspend fun excluir(id: Long): Result<Unit> {
        delay(atrasoMs)
        banco.categorias.update { lista -> lista.filterNot { it.id == id } }
        banco.palavras.update { lista ->
            lista.map { p -> p.copy(categorias = p.categorias.filterNot { it.id == id }) }
        }
        return Result.success(Unit)
    }
}

class FakePalavraRepository(
    private val banco: BancoEmMemoria,
    private val atrasoMs: Long = ATRASO_SIMULADO_MS,
) : PalavraRepository {

    override suspend fun listar(): Result<List<Palavra>> {
        delay(atrasoMs)
        return Result.success(banco.palavras.value)
    }

    override suspend fun criar(palavra: String, categoriaIds: List<Long>): Result<Palavra> {
        delay(atrasoMs)
        if (banco.palavras.value.any { it.palavra.equals(palavra.trim(), ignoreCase = true) }) {
            return Result.failure(IllegalStateException("Essa palavra já está cadastrada."))
        }
        val nova = Palavra(
            id = banco.novoId(),
            palavra = palavra.trim(),
            categorias = resumos(categoriaIds),
            criadoPor = DadosIlustrativos.administrador,
        )
        banco.palavras.update { it + nova }
        return Result.success(nova)
    }

    override suspend fun editar(id: Long, palavra: String, categoriaIds: List<Long>): Result<Palavra> {
        delay(atrasoMs)
        val atual = banco.palavras.value.firstOrNull { it.id == id }
            ?: return Result.failure(NoSuchElementException("Palavra não encontrada."))
        val editada = atual.copy(palavra = palavra.trim(), categorias = resumos(categoriaIds))
        banco.palavras.update { lista -> lista.map { if (it.id == id) editada else it } }
        return Result.success(editada)
    }

    override suspend fun excluir(id: Long): Result<Unit> {
        delay(atrasoMs)
        banco.palavras.update { lista -> lista.filterNot { it.id == id } }
        return Result.success(Unit)
    }

    override suspend fun aleatorias(excluir: List<Long>, quantidade: Int): Result<List<PalavraResumo>> {
        delay(atrasoMs)
        val sorteadas = banco.palavras.value
            .filterNot { it.id in excluir }
            .shuffled()
            .take(quantidade)
            .map { PalavraResumo(it.id, it.palavra) }
        return Result.success(sorteadas)
    }

    private fun resumos(ids: List<Long>) =
        banco.categorias.value.filter { it.id in ids }.map { it.resumo() }
}

class FakeQuizRepository(
    private val banco: BancoEmMemoria,
    private val atrasoMs: Long = ATRASO_SIMULADO_MS,
) : QuizRepository {

    override suspend fun criar(categoriaIds: List<Long>): Result<Quiz> {
        delay(atrasoMs)
        val sorteadas = banco.palavras.value
            .filter { p -> p.categorias.any { it.id in categoriaIds } }
            .shuffled()
            .take(TAMANHO_QUIZ)
        if (sorteadas.isEmpty()) {
            return Result.failure(IllegalStateException("As categorias escolhidas não têm palavras."))
        }
        val quiz = Quiz(
            id = banco.novoId(),
            quizPalavras = sorteadas.map { QuizPalavra(banco.novoId(), PalavraResumo(it.id, it.palavra)) },
        )
        return Result.success(quiz)
    }

    override suspend fun responder(quizPalavraId: Long, acertou: Boolean): Result<Unit> {
        delay(atrasoMs)
        return Result.success(Unit)
    }

    companion object {
        const val TAMANHO_QUIZ = 5
    }
}
