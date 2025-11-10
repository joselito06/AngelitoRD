package com.example.angelitord.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("angelito_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_DARK_THEME_ENABLED = "dark_theme_enabled"
        private const val KEY_ANALYTICS_ENABLED = "analytics_enabled"
    }

    // Notificaciones
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    // Vibración
    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    // Sonido
    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()

    // Tema oscuro
    var darkThemeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME_ENABLED, value).apply()

    // Analytics
    var analyticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ANALYTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ANALYTICS_ENABLED, value).apply()

    /**
     * Cargar todas las configuraciones
     */
    fun getAllSettings(): Map<String, Boolean> {
        return mapOf(
            KEY_NOTIFICATIONS_ENABLED to notificationsEnabled,
            KEY_VIBRATION_ENABLED to vibrationEnabled,
            KEY_SOUND_ENABLED to soundEnabled,
            KEY_DARK_THEME_ENABLED to darkThemeEnabled,
            KEY_ANALYTICS_ENABLED to analyticsEnabled
        )
    }

    /**
     * Limpiar todas las preferencias
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}