package br.com.sinalize.ui.quiz

import br.com.sinalize.data.fake.BancoEmMemoria
import br.com.sinalize.data.fake.FakeCategoriaRepository
import br.com.sinalize.data.fake.FakePalavraRepository
import br.com.sinalize.data.fake.FakeQuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun criarViewModel(avatarPronto: Boolean = true): QuizViewModel {
        val banco = BancoEmMemoria()
        return QuizViewModel(
            FakeCategoriaRepository(banco, atrasoMs = 0),
            FakePalavraRepository(banco, atrasoMs = 0),
            FakeQuizRepository(banco, atrasoMs = 0),
        ).also { it.definirAvatarDisponivel(avatarPronto) }
    }

    @Test
    fun `quiz so comeca depois que o avatar fica disponivel`() = runTest(dispatcher) {
        val vm = criarViewModel(avatarPronto = false)
        advanceUntilIdle()
        vm.alternarCategoria(1L)
        vm.comecar()
        advanceUntilIdle()

        assertEquals(FaseQuiz.SELECIONANDO, vm.uiState.value.fase)
        assertTrue(vm.uiState.value.preparando)
        assertTrue(vm.uiState.value.aguardandoAvatar)

        vm.definirAvatarDisponivel(true)
        advanceUntilIdle()
        assertEquals(FaseQuiz.JOGANDO, vm.uiState.value.fase)
        assertEquals(false, vm.uiState.value.aguardandoAvatar)
    }

    @Test
    fun `mensagem por desempenho segue as faixas da web`() {
        assertEquals("Perfeito!", mensagemPorDesempenho(5, 5))
        assertEquals("Muito bem!", mensagemPorDesempenho(4, 5))
        assertEquals("Foi quase!", mensagemPorDesempenho(2, 5))
        assertEquals("Continue praticando!", mensagemPorDesempenho(1, 5))
        assertEquals("Continue praticando!", mensagemPorDesempenho(0, 0))
    }

    @Test
    fun `jogo completo acertando tudo termina com 5 de 5`() = runTest(dispatcher) {
        val vm = criarViewModel()
        advanceUntilIdle()
        assertEquals(6, vm.uiState.value.categorias.size)

        vm.alternarCategoria(1L)
        vm.comecar()
        advanceUntilIdle()
        assertEquals(FaseQuiz.JOGANDO, vm.uiState.value.fase)

        repeat(vm.uiState.value.total) {
            val estado = vm.uiState.value
            assertEquals(4, estado.opcoes.size)
            assertTrue(estado.opcoes.any { it.id == estado.palavraCorreta!!.id })
            vm.responder(estado.palavraCorreta!!)
            advanceUntilIdle()
        }

        val final = vm.uiState.value
        assertEquals(FaseQuiz.FINALIZADO, final.fase)
        assertEquals(5, final.acertos)
        assertEquals(List(5) { true }, final.resultados)
    }

    @Test
    fun `responder errado marca falso e jogar novamente volta para a selecao`() = runTest(dispatcher) {
        val vm = criarViewModel()
        advanceUntilIdle()
        vm.alternarCategoria(3L)
        vm.comecar()
        advanceUntilIdle()

        val estado = vm.uiState.value
        val errada = estado.opcoes.first { it.id != estado.palavraCorreta!!.id }
        vm.responder(errada)
        assertEquals(false, vm.uiState.value.resultados[0])
        vm.responder(estado.palavraCorreta!!)
        assertEquals(false, vm.uiState.value.resultados[0])

        vm.reiniciar()
        assertEquals(FaseQuiz.SELECIONANDO, vm.uiState.value.fase)
        assertEquals(listOf(3L), vm.uiState.value.selecionadas)
    }
}
