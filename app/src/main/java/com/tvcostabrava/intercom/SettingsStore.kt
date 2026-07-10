package com.tvcostabrava.intercom

import android.content.Context

/** Guarda la URL del servidor de senializacion para no tener que recompilar la app. */
object SettingsStore {
    private const val PREFS_NAME = "intercom_settings"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_THEME = "theme"
    private const val THEME_LIGHT = "light"
    private const val THEME_DARK = "dark"
    private const val KEY_ROLE = "role"

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

    /** true = Oscuro (por defecto), false = Claro. */
    fun isDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, THEME_DARK) != THEME_LIGHT
    }

    fun setDarkTheme(context: Context, dark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, if (dark) THEME_DARK else THEME_LIGHT)
            .apply()
    }

    /** Rol/departamento fijo del usuario (null = todavia no elegido). Es su canal por defecto. */
    fun getRole(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ROLE, null)
    }

    fun setRole(context: Context, role: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role)
            .apply()
    }
}
