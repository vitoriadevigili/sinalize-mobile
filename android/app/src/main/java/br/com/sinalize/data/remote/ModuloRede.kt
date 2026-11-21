package br.com.sinalize.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** Monta o cliente HTTP (OkHttp) e a API (Retrofit) apontando para [urlBase]. */
object ModuloRede {

    /** Ignora campos novos que o back-end venha a mandar, para o app não quebrar. */
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun criarApi(urlBase: String, logarRequisicoes: Boolean): SinalizeApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .apply {
                // Só em depuração: mostra as requisições e respostas no Logcat (filtro "okhttp").
                if (logarRequisicoes) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(urlBase)
            .client(cliente)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
            .build()
            .create(SinalizeApi::class.java)
    }
}
