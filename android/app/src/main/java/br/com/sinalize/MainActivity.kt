package br.com.sinalize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import br.com.sinalize.ui.navigation.SinalizeNavHost
import br.com.sinalize.ui.theme.SinalizeTheme

class MainActivity : ComponentActivity() {

    private val vlibras get() = (application as SinalizeApplication).container.vlibras

    /** Pedido da permissão de rede local (Android 17+, só no build de depuração; ver PermissaoRedeLocal). */
    private val pedirRedeLocal = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null && PermissaoRedeLocal.precisaPedir(this)) {
            pedirRedeLocal.launch(PermissaoRedeLocal.NOME)
        }
        enableEdgeToEdge()
        setContent {
            SinalizeTheme {
                SinalizeNavHost()
            }
        }
        // O avatar do VLibras demora para carregar: começa já, ainda na Home,
        // para estar pronto quando a pessoa entrar no Quiz.
        vlibras.estacionar(this)
        vlibras.iniciar()
    }

    override fun onDestroy() {
        vlibras.liberar(this)
        super.onDestroy()
    }
}
