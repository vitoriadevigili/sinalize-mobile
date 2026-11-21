package br.com.sinalize.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/** Erro de uma chamada à API, já com uma mensagem pronta para mostrar ao usuário. */
class ErroApi(
    mensagem: String,
    val status: Int? = null,
    causa: Throwable? = null,
) : Exception(mensagem, causa)

/**
 * Executa uma chamada à API e devolve [Result]:
 * - sucesso: o valor;
 * - erro HTTP: a mensagem enviada pelo back-end (campo "message" do ErrorResponse; em erros de
 *   validação, as mensagens de cada campo);
 * - sem conexão: uma mensagem pedindo para verificar se o back-end está rodando.
 */
internal suspend fun <T> chamarApi(json: Json, chamada: suspend () -> T): Result<T> =
    try {
        Result.success(chamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(ErroApi(mensagemDoErroHttp(json, e), e.code(), e))
    } catch (e: IOException) {
        Result.failure(
            ErroApi(
                "Não foi possível conectar ao servidor. Verifique se o back-end está rodando e a conexão com a internet.",
                causa = e,
            )
        )
    } catch (e: SerializationException) {
        Result.failure(ErroApi("O servidor respondeu em um formato inesperado.", causa = e))
    }

internal fun mensagemDoErroHttp(json: Json, e: HttpException): String {
    val corpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
    val erro = corpo?.let { runCatching { json.decodeFromString<ErroDto>(it) }.getOrNull() }
    val porCampo = erro?.campos.orEmpty().map { it.mensagem }.distinct()
    return when {
        porCampo.isNotEmpty() -> porCampo.joinToString("\n")
        !erro?.message.isNullOrBlank() -> erro!!.message!!
        else -> mensagemPadrao(e.code())
    }
}

private fun mensagemPadrao(status: Int): String = when (status) {
    400 -> "Os dados enviados são inválidos."
    404 -> "Registro não encontrado."
    409 -> "A operação não pode ser concluída por causa de outros registros relacionados."
    in 500..599 -> "Erro no servidor. Tente novamente em instantes."
    else -> "Erro inesperado (HTTP $status)."
}
