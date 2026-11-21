package br.com.sinalize.data.fake

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositoriosFalsosTest {

    private val banco = BancoEmMemoria()
    private val palavras = FakePalavraRepository(banco, atrasoMs = 0)
    private val categorias = FakeCategoriaRepository(banco, atrasoMs = 0)
    private val quizzes = FakeQuizRepository(banco, atrasoMs = 0)

    @Test
    fun `aleatorias nunca devolve a palavra excluida`() = runTest {
        repeat(50) {
            val sorteadas = palavras.aleatorias(excluir = listOf(1L), quantidade = 3).getOrThrow()
            assertEquals(3, sorteadas.size)
            assertTrue(sorteadas.none { it.id == 1L })
        }
    }

    @Test
    fun `quiz tem ate 5 palavras so das categorias escolhidas`() = runTest {
        val cores = 3L
        val quiz = quizzes.criar(listOf(cores)).getOrThrow()
        assertEquals(FakeQuizRepository.TAMANHO_QUIZ, quiz.quizPalavras.size)
        val idsDeCores = banco.palavras.value.filter { p -> p.categorias.any { it.id == cores } }.map { it.id }
        assertTrue(quiz.quizPalavras.all { it.palavra.id in idsDeCores })
    }

    @Test
    fun `criar palavra repetida falha`() = runTest {
        assertTrue(palavras.criar("azul", emptyList()).isFailure)
    }

    @Test
    fun `editar categoria atualiza o nome dentro das palavras`() = runTest {
        categorias.editar(3L, "Cores básicas", "Nomes das cores.").getOrThrow()
        val azul = palavras.listar().getOrThrow().first { it.palavra == "Azul" }
        assertEquals("Cores básicas", azul.categorias.single().nome)
    }

    @Test
    fun `excluir categoria remove o vinculo das palavras`() = runTest {
        categorias.excluir(3L).getOrThrow()
        val laranja = palavras.listar().getOrThrow().first { it.palavra == "Laranja" }
        assertFalse(laranja.categorias.any { it.id == 3L })
        assertEquals(listOf(5L), laranja.categorias.map { it.id })
    }
}
