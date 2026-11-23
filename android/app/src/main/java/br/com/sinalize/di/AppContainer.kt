package br.com.sinalize.di

import android.content.Context
import br.com.sinalize.BuildConfig
import br.com.sinalize.data.fake.BancoEmMemoria
import br.com.sinalize.data.fake.FakeCategoriaRepository
import br.com.sinalize.data.fake.FakePalavraRepository
import br.com.sinalize.data.fake.FakeQuizRepository
import br.com.sinalize.data.remote.ModuloRede
import br.com.sinalize.data.remote.RemoteCategoriaRepository
import br.com.sinalize.data.remote.RemotePalavraRepository
import br.com.sinalize.data.remote.RemoteQuizRepository
import br.com.sinalize.data.repository.CategoriaRepository
import br.com.sinalize.data.repository.PalavraRepository
import br.com.sinalize.data.repository.QuizRepository
import br.com.sinalize.vlibras.MotorVLibras

class AppContainer(contexto: Context) {

    val categoriaRepository: CategoriaRepository
    val palavraRepository: PalavraRepository
    val quizRepository: QuizRepository

    init {
        if (BuildConfig.USAR_DADOS_ILUSTRATIVOS) {
            val banco = BancoEmMemoria()
            categoriaRepository = FakeCategoriaRepository(banco)
            palavraRepository = FakePalavraRepository(banco)
            quizRepository = FakeQuizRepository(banco)
        } else {
            val api = ModuloRede.criarApi(BuildConfig.API_BASE_URL, logarRequisicoes = BuildConfig.DEBUG)
            val json = ModuloRede.json
            categoriaRepository = RemoteCategoriaRepository(api, json)
            palavraRepository = RemotePalavraRepository(api, json)
            quizRepository = RemoteQuizRepository(api, json)
        }
    }

    val vlibras = MotorVLibras(contexto.applicationContext)
}
