package br.com.sinalize

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Android 17+ (API 37): para um app acessar endereços da rede local — como o back-end de
 * desenvolvimento em 10.0.2.2 (emulador) ou no IP do computador — o usuário precisa permitir
 * "dispositivos na rede local". Sem isso, as conexões são descartadas e terminam em tempo esgotado.
 *
 * A permissão só é declarada no build de depuração (src/debug/AndroidManifest.xml), porque em
 * produção o app falaria com um servidor público em HTTPS, que não precisa dela.
 */
object PermissaoRedeLocal {

    const val NOME = "android.permission.ACCESS_LOCAL_NETWORK"
    private const val API_ANDROID_17 = 37

    /** true quando é preciso pedir a permissão ao usuário (Android 17+, declarada e ainda não concedida). */
    fun precisaPedir(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < API_ANDROID_17) return false
        if (!declaradaNoManifest(contexto)) return false
        return ContextCompat.checkSelfPermission(contexto, NOME) != PackageManager.PERMISSION_GRANTED
    }

    private fun declaradaNoManifest(contexto: Context): Boolean = runCatching {
        val info = contexto.packageManager.getPackageInfo(contexto.packageName, PackageManager.GET_PERMISSIONS)
        info.requestedPermissions.orEmpty().contains(NOME)
    }.getOrDefault(false)
}
