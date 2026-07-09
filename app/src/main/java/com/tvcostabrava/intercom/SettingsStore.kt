package com.tvcostabrava.intercom

import android.content.Context

/** Guarda la URL del servidor de senializacion para no tener que recompilar la app. */
object SettingsStore {
    private const val PREFS_NAME = "intercom_settings"
    private const val KEY_SERVER_URL = "server_url"

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, "") ?: ""
    }

    fun setServerUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SERVER_URL, url)
            .apply()
    }
}
