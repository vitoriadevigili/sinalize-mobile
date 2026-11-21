package br.com.sinalize

import android.app.Application
import br.com.sinalize.di.AppContainer

class SinalizeApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
