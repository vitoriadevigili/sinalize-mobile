package br.com.sinalize.data.model

data class UsuarioResumo(
    val id: Long,
    val nome: String,
)

data class CategoriaResumo(
    val id: Long,
    val nome: String,
    /** A API não manda a descrição nas categorias de uma palavra; aí fica vazia. */
    val descricao: String = "",
)

data class Categoria(
    val id: Long,
    val nome: String,
    val descricao: String,
    val criadoPor: UsuarioResumo? = null,
    val criadoEm: String? = null,
)

data class PalavraResumo(
    val id: Long,
    val palavra: String,
)

data class Palavra(
    val id: Long,
    val palavra: String,
    val categorias: List<CategoriaResumo> = emptyList(),
    val criadoPor: UsuarioResumo? = null,
    val criadoEm: String? = null,
)

data class QuizPalavra(
    val id: Long,
    val palavra: PalavraResumo,
    val acertou: Boolean? = null,
)

data class Quiz(
    val id: Long,
    val quizPalavras: List<QuizPalavra>,
)
