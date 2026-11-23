package br.com.sinalize.vlibras

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.MutableContextWrapper
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.webkit.WebViewAssetLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

private const val URL_VLIBRAS = "https://appassets.androidplatform.net/assets/vlibras-frame.html"

private const val TEMPO_MAXIMO_CARREGAMENTO_MS = 90_000L

private const val LARGURA_ESTACIONADA = 640
private const val ALTURA_ESTACIONADA = 360

sealed interface EstadoVLibras {
    data object Carregando : EstadoVLibras
    data object Pronto : EstadoVLibras
    data class Erro(val mensagem: String) : EstadoVLibras
}

class MotorVLibras(private val app: Context) {

    private val _estado = MutableStateFlow<EstadoVLibras>(EstadoVLibras.Carregando)
    val estado: StateFlow<EstadoVLibras> = _estado.asStateFlow()

    private val contexto = MutableContextWrapper(app)
    private val principal = Handler(Looper.getMainLooper())
    private var webView: WebView? = null
    private var estacionamento: ViewGroup? = null
    private var ultimaPalavra = ""

    private val tempoEsgotado = Runnable {
        if (_estado.value == EstadoVLibras.Carregando) {
            _estado.value = EstadoVLibras.Erro("O personagem 3D está demorando para carregar.")
        }
    }

    fun iniciar() {
        if (webView == null) criar()
    }

    fun recarregar() {
        destruir()
        criar()
    }

    fun sinalizar(palavra: String) {
        ultimaPalavra = palavra
        if (_estado.value == EstadoVLibras.Pronto && palavra.isNotBlank()) {
            executar("window.sinalizar(${JSONObject.quote(palavra)})")
        }
    }

    fun repetir() {
        if (_estado.value == EstadoVLibras.Pronto) executar("window.repetir()")
    }


    fun estacionar(activity: Activity) {
        contexto.baseContext = activity
        estacionamento = activity.findViewById(android.R.id.content)
        val wv = webView ?: return
        if (wv.parent == null) moverParaEstacionamento(wv)
    }

    fun liberar(activity: Activity) {
        if (contexto.baseContext !== activity) return
        webView?.let { (it.parent as? ViewGroup)?.removeView(it) }
        estacionamento = null
        contexto.baseContext = app
    }

    fun exibirEm(container: FrameLayout) {
        val wv = webView ?: return
        if (wv.parent === container) return
        (wv.parent as? ViewGroup)?.removeView(wv)
        wv.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        container.addView(wv, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
    }

    fun devolverDe(container: FrameLayout) {
        val wv = webView ?: return
        if (wv.parent !== container) return
        container.removeView(wv)
        moverParaEstacionamento(wv)
    }

    private fun moverParaEstacionamento(wv: WebView) {
        val destino = estacionamento ?: return
        (wv.parent as? ViewGroup)?.removeView(wv)
        wv.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        destino.addView(wv, 0, ViewGroup.LayoutParams(LARGURA_ESTACIONADA, ALTURA_ESTACIONADA))
    }


    private fun criar() {
        _estado.value = EstadoVLibras.Carregando
        val wv = criarWebView(
            aoFicarPronto = {
                principal.removeCallbacks(tempoEsgotado)
                Log.d("VLibras", "avatar pronto")
                _estado.value = EstadoVLibras.Pronto
                if (ultimaPalavra.isNotBlank()) sinalizar(ultimaPalavra)
            },
            aoDarErro = { mensagem ->
                if (_estado.value != EstadoVLibras.Pronto) _estado.value = EstadoVLibras.Erro(mensagem)
            },
        )
        webView = wv
        wv.loadUrl(URL_VLIBRAS)
        principal.postDelayed(tempoEsgotado, TEMPO_MAXIMO_CARREGAMENTO_MS)
        moverParaEstacionamento(wv)
    }

    private fun destruir() {
        principal.removeCallbacks(tempoEsgotado)
        webView?.let { wv ->
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.stopLoading()
            wv.removeJavascriptInterface("AndroidBridge")
            wv.destroy()
        }
        webView = null
    }

    private fun executar(js: String) {
        webView?.evaluateJavascript(js, null)
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    private fun criarWebView(aoFicarPronto: () -> Unit, aoDarErro: (String) -> Unit): WebView {
        val depuravel = app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        WebView.setWebContentsDebuggingEnabled(depuravel)

        val carregadorDeAssets = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(app))
            .build()

        return WebView(contexto).apply {
            setBackgroundColor(Color.parseColor("#111111"))
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onReady(@Suppress("UNUSED_PARAMETER") valor: String) {
                        principal.post(aoFicarPronto)
                    }

                    @JavascriptInterface
                    fun onErro(mensagem: String) {
                        principal.post { aoDarErro(mensagem) }
                    }
                },
                "AndroidBridge",
            )

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(mensagem: ConsoleMessage): Boolean {
                    if (depuravel) {
                        Log.d("VLibras", "${mensagem.messageLevel()}: ${mensagem.message()} (${mensagem.sourceId()}:${mensagem.lineNumber()})")
                    }
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                    carregadorDeAssets.shouldInterceptRequest(request.url)

                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    if (request.isForMainFrame) {
                        aoDarErro("Não foi possível carregar o VLibras. Verifique a conexão com a internet.")
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                    request.isForMainFrame && request.url.toString() != URL_VLIBRAS
            }
        }
    }
}
