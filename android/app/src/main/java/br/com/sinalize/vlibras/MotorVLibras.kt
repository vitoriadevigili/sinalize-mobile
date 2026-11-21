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

/** Endereço da página do avatar, servida a partir de app/src/main/assets. */
private const val URL_VLIBRAS = "https://appassets.androidplatform.net/assets/vlibras-frame.html"

/** Se o avatar não carregar nesse tempo, o estado vira [EstadoVLibras.Erro] (com "Tentar novamente"). */
private const val TEMPO_MAXIMO_CARREGAMENTO_MS = 90_000L

/** Tamanho (em px) da WebView enquanto ela carrega escondida atrás da tela. */
private const val LARGURA_ESTACIONADA = 640
private const val ALTURA_ESTACIONADA = 360

sealed interface EstadoVLibras {
    data object Carregando : EstadoVLibras
    data object Pronto : EstadoVLibras
    data class Erro(val mensagem: String) : EstadoVLibras
}

/**
 * Avatar do VLibras, único para o app inteiro (fica no AppContainer).
 *
 * O personagem 3D demora para carregar (dezenas de segundos no emulador), então ele começa a carregar
 * assim que o app abre e fica pronto para quando a pessoa entrar no Quiz:
 * - enquanto ninguém está vendo, a WebView fica "estacionada" atrás do conteúdo da tela
 *   (presa à janela, para o navegador continuar carregando e desenhando o 3D);
 * - a tela do Quiz pede para [exibirEm] a sua área, e ao sair a WebView volta para o estacionamento.
 *
 * A página (assets/vlibras-frame.html) conversa com o app por window.sinalizar / window.repetir
 * e pela ponte AndroidBridge (onReady / onErro).
 */
class MotorVLibras(private val app: Context) {

    private val _estado = MutableStateFlow<EstadoVLibras>(EstadoVLibras.Carregando)
    val estado: StateFlow<EstadoVLibras> = _estado.asStateFlow()

    /** A WebView guarda este contexto; ele aponta para a Activity atual (ou para o app, se não houver). */
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

    /** Começa a carregar o avatar (se ainda não começou). */
    fun iniciar() {
        if (webView == null) criar()
    }

    /** Descarta o avatar atual e carrega de novo (botão "Tentar novamente"). */
    fun recarregar() {
        destruir()
        criar()
    }

    /** Pede para o avatar sinalizar [palavra]. Vazio não sinaliza nada. Se ainda estiver carregando, sinaliza ao ficar pronto. */
    fun sinalizar(palavra: String) {
        ultimaPalavra = palavra
        if (_estado.value == EstadoVLibras.Pronto && palavra.isNotBlank()) {
            executar("window.sinalizar(${JSONObject.quote(palavra)})")
        }
    }

    /** Sinaliza de novo a última palavra. */
    fun repetir() {
        if (_estado.value == EstadoVLibras.Pronto) executar("window.repetir()")
    }

    // ---- Onde a WebView fica ----

    /** Chamado no onCreate da Activity: prende a WebView atrás do conteúdo da tela, de forma invisível. */
    fun estacionar(activity: Activity) {
        contexto.baseContext = activity
        estacionamento = activity.findViewById(android.R.id.content)
        val wv = webView ?: return
        if (wv.parent == null) moverParaEstacionamento(wv)
    }

    /** Chamado no onDestroy da Activity: solta a WebView para não segurar a Activity antiga na memória. */
    fun liberar(activity: Activity) {
        if (contexto.baseContext !== activity) return
        webView?.let { (it.parent as? ViewGroup)?.removeView(it) }
        estacionamento = null
        contexto.baseContext = app
    }

    /** Mostra o avatar dentro de [container] (a área do avatar no Quiz). */
    fun exibirEm(container: FrameLayout) {
        val wv = webView ?: return
        if (wv.parent === container) return
        (wv.parent as? ViewGroup)?.removeView(wv)
        wv.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
        container.addView(wv, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
    }

    /** Tira o avatar de [container] e o devolve para o estacionamento. */
    fun devolverDe(container: FrameLayout) {
        val wv = webView ?: return
        if (wv.parent !== container) return
        container.removeView(wv)
        moverParaEstacionamento(wv)
    }

    private fun moverParaEstacionamento(wv: WebView) {
        val destino = estacionamento ?: return
        (wv.parent as? ViewGroup)?.removeView(wv)
        // Leitores de tela não devem encontrar o avatar escondido.
        wv.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        // Índice 0: fica por baixo do conteúdo do app (que é opaco), então não aparece.
        destino.addView(wv, 0, ViewGroup.LayoutParams(LARGURA_ESTACIONADA, ALTURA_ESTACIONADA))
    }

    // ---- Criação ----

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
        // Em builds de depuração, a página pode ser inspecionada pelo chrome://inspect do computador.
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

            // Ponte chamada pela página. Os métodos rodam numa thread do WebView, então o resultado
            // é repassado para a thread principal.
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

            // Mensagens do console da página vão para o Logcat (filtro "VLibras"), para diagnóstico.
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

                // Links dentro do widget não devem abrir outra página no lugar do avatar.
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                    request.isForMainFrame && request.url.toString() != URL_VLIBRAS
            }
        }
    }
}
