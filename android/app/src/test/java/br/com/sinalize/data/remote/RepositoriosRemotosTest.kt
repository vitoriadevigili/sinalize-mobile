package br.com.sinalize.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RepositoriosRemotosTest {

    private lateinit var servidor: MockWebServer
    private lateinit var palavras: RemotePalavraRepository
    private lateinit var categorias: RemoteCategoriaRepository
    private lateinit var quizzes: RemoteQuizRepository

    @Before
    fun setUp() {
        servidor = MockWebServer().apply { start() }
        val api = ModuloRede.criarApi(servidor.url("/").toString(), logarRequisicoes = false)
        palavras = RemotePalavraRepository(api, ModuloRede.json)
        categorias = RemoteCategoriaRepository(api, ModuloRede.json)
        quizzes = RemoteQuizRepository(api, ModuloRede.json)
    }

    @After
    fun tearDown() {
        runCatching { servidor.shutdown() }
    }

    private fun responder(status: Int, corpo: String = "") {
        servidor.enqueue(
            MockResponse().setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(corpo)
        )
    }

    @Test
    fun `lista palavras lendo o JSON em snake_case`() = runTest {
        responder(
            200,
            """
            [{"id":1,"palavra":"Laranja","criado_em":"2026-11-21T19:30:00","atualizado_em":null,
              "criado_por":{"id":1,"nome":"Administrador"},"atualizado_por":null,
              "categorias":[{"id":5,"nome":"Alimentos"},{"id":3,"nome":"Cores"}]}]
            """.trimIndent(),
        )

        val lista = palavras.listar().getOrThrow()

        assertEquals("GET /palavras", servidor.takeRequest().let { "${it.method} ${it.path}" })
        assertEquals("Laranja", lista.single().palavra)
        assertEquals(listOf("Alimentos", "Cores"), lista.single().categorias.map { it.nome })
        assertEquals("Administrador", lista.single().criadoPor?.nome)
    }

    @Test
    fun `cria palavra enviando categoria_ids`() = runTest {
        responder(201, """{"id":9,"palavra":"Uva","categorias":[{"id":5,"nome":"Alimentos"}]}""")

        val criada = palavras.criar("Uva", listOf(5L)).getOrThrow()

        val requisicao = servidor.takeRequest()
        assertEquals("POST /palavras", "${requisicao.method} ${requisicao.path}")
        assertEquals("""{"palavra":"Uva","categoria_ids":[5]}""", requisicao.body.readUtf8())
        assertEquals(9L, criada.id)
    }

    @Test
    fun `sorteia aleatorias com excluir e quantidade na URL`() = runTest {
        responder(200, """[{"id":2,"palavra":"Gato"}]""")

        palavras.aleatorias(excluir = listOf(1L, 7L), quantidade = 3).getOrThrow()

        assertEquals("/palavras/aleatorias?excluir=1%2C7&quantidade=3", servidor.takeRequest().path)
    }

    @Test
    fun `cria quiz e le quiz_palavras`() = runTest {
        responder(
            201,
            """{"id":3,"criado_em":"2026-11-21T19:30:00","usuario":{"id":1,"nome":"Administrador"},
               "quiz_palavras":[{"id":10,"acertou":null,"respondido_em":null,"palavra":{"id":4,"palavra":"Azul"}}]}""",
        )

        val quiz = quizzes.criar(listOf(3L)).getOrThrow()

        assertEquals("""{"categoria_ids":[3]}""", servidor.takeRequest().body.readUtf8())
        assertEquals("Azul", quiz.quizPalavras.single().palavra.palavra)
    }

    @Test
    fun `responder envia PATCH com acertou`() = runTest {
        responder(200, """{"id":10,"acertou":true,"respondido_em":"2026-11-21T19:31:00","palavra":{"id":4,"palavra":"Azul"}}""")

        quizzes.responder(10L, true).getOrThrow()

        val requisicao = servidor.takeRequest()
        assertEquals("PATCH /quizzes/palavras/10/responder", "${requisicao.method} ${requisicao.path}")
        assertEquals("""{"acertou":true}""", requisicao.body.readUtf8())
    }

    @Test
    fun `excluir aceita 204 sem corpo`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(204))

        assertTrue(categorias.excluir(4L).isSuccess)
        assertEquals("DELETE /categorias/4", servidor.takeRequest().let { "${it.method} ${it.path}" })
    }

    @Test
    fun `erro de validacao mostra as mensagens dos campos`() = runTest {
        responder(
            400,
            """{"timestamp":"2026-11-21T19:30:00Z","status":400,"error":"Bad Request",
               "message":"Há campos inválidos.","path":"/palavras",
               "campos":[{"campo":"categoria_ids","mensagem":"A palavra deve estar associada a pelo menos uma categoria."}]}""",
        )

        val erro = palavras.criar("Uva", emptyList()).exceptionOrNull() as ErroApi

        assertEquals(400, erro.status)
        assertEquals("A palavra deve estar associada a pelo menos uma categoria.", erro.message)
    }

    @Test
    fun `erro de regra de negocio mostra a message do back-end`() = runTest {
        responder(409, """{"status":409,"error":"Conflict","message":"Já existe uma categoria com esse nome.","path":"/categorias"}""")

        val erro = categorias.criar("Cores", "x").exceptionOrNull() as ErroApi

        assertEquals(409, erro.status)
        assertEquals("Já existe uma categoria com esse nome.", erro.message)
    }

    @Test
    fun `erro sem corpo usa mensagem padrao pelo status`() = runTest {
        servidor.enqueue(MockResponse().setResponseCode(500))

        val erro = categorias.listar().exceptionOrNull() as ErroApi

        assertEquals("Erro no servidor. Tente novamente em instantes.", erro.message)
    }

    @Test
    fun `sem conexao pede para verificar o back-end`() = runTest {
        servidor.shutdown()

        val erro = categorias.listar().exceptionOrNull() as ErroApi

        assertTrue(erro.message!!.startsWith("Não foi possível conectar ao servidor."))
    }
}
