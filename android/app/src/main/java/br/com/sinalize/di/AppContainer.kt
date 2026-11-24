package br.com.sinalize.di

import android.content.Context
import br.com.sinalize.BuildConfig
import br.com.sinalize.data.remote.ModuloRede
import br.com.sinalize.data.remote.RemoteCategoriaRepository
import br.com.sinalize.data.remote.RemotePalavraRepository
import br.com.sinalize.data.remote.RemoteQuizRepository
import br.com.sinalize.data.repository.CategoriaRepository
import br.com.sinalize.data.repository.PalavraRepository
import br.com.sinalize.data.repository.QuizRepository
import br.com.sinalize.vlibras.MotorVLibras

class AppContainer(contexto: Context) {

    private val api = ModuloRede.criarApi(BuildConfig.API_BASE_URL, logarRequisicoes = BuildConfig.DEBUG)

    val categoriaRepository: CategoriaRepository = RemoteCategoriaRepository(api, ModuloRede.json)
    val palavraRepository: PalavraRepository = RemotePalavraRepository(api, ModuloRede.json)
    val quizRepository: QuizRepository = RemoteQuizRepository(api, ModuloRede.json)

    val vlibras = MotorVLibras(contexto.applicationContext)
}
