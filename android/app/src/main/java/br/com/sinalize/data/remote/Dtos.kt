package br.com.sinalize.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable
data class UsuarioResumoDto(
    val id: Long,
    val nome: String,
)

@Serializable
data class CategoriaResumoDto(
    val id: Long,
    val nome: String,
)

@Serializable
data class CategoriaDto(
    val id: Long,
    val nome: String,
    val descricao: String,
    @SerialName("criado_em") val criadoEm: String? = null,
    @SerialName("atualizado_em") val atualizadoEm: String? = null,
    @SerialName("criado_por") val criadoPor: UsuarioResumoDto? = null,
    @SerialName("atualizado_por") val atualizadoPor: UsuarioResumoDto? = null,
)

@Serializable
data class PalavraResumoDto(
    val id: Long,
    val palavra: String,
)

@Serializable
data class PalavraDto(
    val id: Long,
    val palavra: String,
    @SerialName("criado_em") val criadoEm: String? = null,
    @SerialName("atualizado_em") val atualizadoEm: String? = null,
    @SerialName("criado_por") val criadoPor: UsuarioResumoDto? = null,
    @SerialName("atualizado_por") val atualizadoPor: UsuarioResumoDto? = null,
    val categorias: List<CategoriaResumoDto> = emptyList(),
)

@Serializable
data class QuizPalavraDto(
    val id: Long,
    val acertou: Boolean? = null,
    @SerialName("respondido_em") val respondidoEm: String? = null,
    val palavra: PalavraResumoDto,
)

@Serializable
data class QuizDto(
    val id: Long,
    @SerialName("criado_em") val criadoEm: String? = null,
    val usuario: UsuarioResumoDto? = null,
    @SerialName("quiz_palavras") val quizPalavras: List<QuizPalavraDto> = emptyList(),
)

@Serializable
data class ErroDto(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null,
    val path: String? = null,
    val campos: List<CampoErroDto> = emptyList(),
)

@Serializable
data class CampoErroDto(
    val campo: String,
    val mensagem: String,
)


@Serializable
data class CategoriaRequestDto(
    val nome: String,
    val descricao: String,
)

@Serializable
data class PalavraRequestDto(
    val palavra: String,
    @SerialName("categoria_ids") val categoriaIds: List<Long>,
)

@Serializable
data class QuizRequestDto(
    @SerialName("categoria_ids") val categoriaIds: List<Long>,
)

@Serializable
data class RespostaRequestDto(
    val acertou: Boolean,
)
