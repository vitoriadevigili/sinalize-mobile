package br.com.sinalize

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissaoRedeLocal {

    const val NOME = "android.permission.ACCESS_LOCAL_NETWORK"
    private const val API_ANDROID_17 = 37

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
