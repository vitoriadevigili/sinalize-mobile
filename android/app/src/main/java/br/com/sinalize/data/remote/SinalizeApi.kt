package br.com.sinalize.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SinalizeApi {


    @GET("categorias")
    suspend fun listarCategorias(): List<CategoriaDto>

    @POST("categorias")
    suspend fun criarCategoria(@Body corpo: CategoriaRequestDto): CategoriaDto

    @PATCH("categorias/{id}")
    suspend fun editarCategoria(@Path("id") id: Long, @Body corpo: CategoriaRequestDto): CategoriaDto

    @DELETE("categorias/{id}")
    suspend fun excluirCategoria(@Path("id") id: Long)


    @GET("palavras")
    suspend fun listarPalavras(): List<PalavraDto>

    @POST("palavras")
    suspend fun criarPalavra(@Body corpo: PalavraRequestDto): PalavraDto

    @PATCH("palavras/{id}")
    suspend fun editarPalavra(@Path("id") id: Long, @Body corpo: PalavraRequestDto): PalavraDto

    @DELETE("palavras/{id}")
    suspend fun excluirPalavra(@Path("id") id: Long)

    @GET("palavras/aleatorias")
    suspend fun palavrasAleatorias(
        @Query("excluir") excluir: String,
        @Query("quantidade") quantidade: Int,
    ): List<PalavraResumoDto>


    @POST("quizzes")
    suspend fun criarQuiz(@Body corpo: QuizRequestDto): QuizDto

    @PATCH("quizzes/palavras/{quizPalavraId}/responder")
    suspend fun responder(@Path("quizPalavraId") quizPalavraId: Long, @Body corpo: RespostaRequestDto): QuizPalavraDto
}
