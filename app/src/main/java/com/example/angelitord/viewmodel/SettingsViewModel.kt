package com.example.angelitord.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angelitord.models.AppSettings
import com.example.angelitord.utils.NotificationManagerHelper
import com.example.angelitord.utils.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesManager: PreferencesManager,
    private val notificationManager: NotificationManagerHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Initial)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Cargar configuraciones desde SharedPreferences Y Firestore
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                // 1. Primero cargar desde SharedPreferences (inmediato)
                _settings.value = AppSettings(
                    notificationsEnabled = preferencesManager.notificationsEnabled,
                    vibrationEnabled = preferencesManager.vibrationEnabled,
                    soundEnabled = preferencesManager.soundEnabled,
                    darkThemeEnabled = preferencesManager.darkThemeEnabled,
                    analyticsEnabled = preferencesManager.analyticsEnabled,
                    debugModeEnabled = false
                )

                // 2. Sincronizar con Firestore (background)
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val doc = firestore.collection("user_settings")
                    .document(userId)
                    .get()
                    .await()

                if (doc.exists()) {
                    // Si hay configuraciones en Firestore, actualizarlas localmente
                    val firestoreSettings = AppSettings(
                        notificationsEnabled = doc.getBoolean("notificationsEnabled")
                            ?: preferencesManager.notificationsEnabled,
                        vibrationEnabled = doc.getBoolean("vibrationEnabled")
                            ?: preferencesManager.vibrationEnabled,
                        soundEnabled = doc.getBoolean("soundEnabled")
                            ?: preferencesManager.soundEnabled,
                        darkThemeEnabled = doc.getBoolean("darkThemeEnabled")
                            ?: preferencesManager.darkThemeEnabled,
                        analyticsEnabled = doc.getBoolean("analyticsEnabled")
                            ?: preferencesManager.analyticsEnabled,
                        debugModeEnabled = false
                    )

                    // Actualizar SharedPreferences con los datos de Firestore
                    preferencesManager.notificationsEnabled = firestoreSettings.notificationsEnabled
                    preferencesManager.vibrationEnabled = firestoreSettings.vibrationEnabled
                    preferencesManager.soundEnabled = firestoreSettings.soundEnabled
                    preferencesManager.darkThemeEnabled = firestoreSettings.darkThemeEnabled
                    preferencesManager.analyticsEnabled = firestoreSettings.analyticsEnabled

                    _settings.value = firestoreSettings
                } else {
                    // Si no hay configuraciones en Firestore, guardar las locales
                    saveSettingsToFirestore()
                }
            } catch (e: Exception) {
                // En caso de error, usar las configuraciones locales
                android.util.Log.e("SettingsViewModel", "Error loading settings: ${e.message}")
            }
        }
    }

    /**
     * Guardar configuraciones en Firestore
     */
    private fun saveSettingsToFirestore() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val settingsMap = mapOf(
                    "notificationsEnabled" to _settings.value.notificationsEnabled,
                    "vibrationEnabled" to _settings.value.vibrationEnabled,
                    "soundEnabled" to _settings.value.soundEnabled,
                    "darkThemeEnabled" to _settings.value.darkThemeEnabled,
                    "analyticsEnabled" to _settings.value.analyticsEnabled,
                    "lastUpdated" to System.currentTimeMillis()
                )

                firestore.collection("user_settings")
                    .document(userId)
                    .set(settingsMap)
                    .await()

                android.util.Log.d("SettingsViewModel", "Settings saved to Firestore successfully")
            } catch (e: Exception) {
                android.util.Log.e("SettingsViewModel", "Error saving to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Actualizar configuración de notificaciones
     */
    fun updateNotificationsSetting(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // 1. Actualizar SharedPreferences PRIMERO
                preferencesManager.notificationsEnabled = enabled

                // 2. Configurar Firebase Messaging
                notificationManager.setNotificationsEnabled(enabled)

                // 3. Si se desactivan las notificaciones, desactivar también vibración y sonido
                if (!enabled) {
                    preferencesManager.vibrationEnabled = false
                    preferencesManager.soundEnabled = false
                }

                // 4. Actualizar estado local
                _settings.value = _settings.value.copy(
                    notificationsEnabled = enabled,
                    vibrationEnabled = if (enabled) _settings.value.vibrationEnabled else false,
                    soundEnabled = if (enabled) _settings.value.soundEnabled else false
                )

                // 5. Guardar en Firestore (background)
                saveSettingsToFirestore()

                // 6. Verificar permisos del sistema
                if (enabled && !notificationManager.areNotificationsEnabled()) {
                    _uiState.value = SettingsUiState.Error(
                        "Las notificaciones están bloqueadas en la configuración del sistema. " +
                                "Por favor, actívalas en Configuración > Aplicaciones > Angelito RD"
                    )
                } else {
                    _uiState.value = SettingsUiState.Success(
                        if (enabled) "Notificaciones activadas ✓" else "Notificaciones desactivadas"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    "Error al actualizar notificaciones: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualizar configuración de vibración
     */
    fun updateVibrationSetting(enabled: Boolean) {
        viewModelScope.launch {
            // 1. Actualizar SharedPreferences
            preferencesManager.vibrationEnabled = enabled
            notificationManager.setVibrationEnabled(enabled)

            // 2. Actualizar estado local
            _settings.value = _settings.value.copy(vibrationEnabled = enabled)

            // 3. Guardar en Firestore
            saveSettingsToFirestore()

            _uiState.value = SettingsUiState.Success(
                if (enabled) "Vibración activada ✓" else "Vibración desactivada"
            )
        }
    }

    /**
     * Actualizar configuración de sonido
     */
    fun updateSoundSetting(enabled: Boolean) {
        viewModelScope.launch {
            // 1. Actualizar SharedPreferences
            preferencesManager.soundEnabled = enabled
            notificationManager.setSoundEnabled(enabled)

            // 2. Actualizar estado local
            _settings.value = _settings.value.copy(soundEnabled = enabled)

            // 3. Guardar en Firestore
            saveSettingsToFirestore()

            _uiState.value = SettingsUiState.Success(
                if (enabled) "Sonido activado ✓" else "Sonido desactivado"
            )
        }
    }

    /**
     * Actualizar tema oscuro
     */
    fun updateDarkThemeSetting(enabled: Boolean) {
        Log.d("SettingsViewModel", "updateDarkThemeSetting called with: $enabled")

        // 1. Actualizar SharedPreferences
        preferencesManager.darkThemeEnabled = enabled

        // 2. Actualizar estado local
        _settings.value = _settings.value.copy(darkThemeEnabled = enabled)

        // 3. Guardar en Firestore
        //saveSettingsToFirestore()

        _uiState.value = SettingsUiState.Success(
            if (enabled) "Tema oscuro activado ✓"
            else "Tema claro activado ✓"
        )

        /*viewModelScope.launch {

            // 1. Actualizar SharedPreferences
            preferencesManager.darkThemeEnabled = enabled

            // 2. Actualizar estado local
            _settings.value = _settings.value.copy(darkThemeEnabled = enabled)

            // 3. Guardar en Firestore
            saveSettingsToFirestore()

            _uiState.value = SettingsUiState.Success(
                if (enabled) "Tema oscuro activado ✓"
                else "Tema claro activado ✓"
            )
        }*/
    }

    /**
     * Actualizar análisis
     */
    fun updateAnalyticsSetting(enabled: Boolean) {
        viewModelScope.launch {
            // 1. Actualizar SharedPreferences
            preferencesManager.analyticsEnabled = enabled

            // TODO: Configurar Firebase Analytics
            // FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enabled)

            // 2. Actualizar estado local
            _settings.value = _settings.value.copy(analyticsEnabled = enabled)

            // 3. Guardar en Firestore
            saveSettingsToFirestore()

            _uiState.value = SettingsUiState.Success(
                if (enabled) "Análisis activado ✓" else "Análisis desactivado"
            )
        }
    }

    /**
     * Probar notificaciones
     */
    fun testNotification() {
        if (!preferencesManager.notificationsEnabled) {
            _uiState.value = SettingsUiState.Error(
                "Las notificaciones están desactivadas. Por favor, actívalas primero."
            )
            return
        }

        try {
            notificationManager.showTestNotification()
            _uiState.value = SettingsUiState.Success(
                "Notificación de prueba enviada ✓"
            )
        } catch (e: Exception) {
            _uiState.value = SettingsUiState.Error(
                "Error al enviar notificación: ${e.message}"
            )
        }
    }

    /**
     * Limpiar caché de la aplicación
     */
    fun clearCache(context: Context) {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                val cacheSize = calculateDirectorySize(cacheDir)

                cacheDir.deleteRecursively()

                val sizeInMB = cacheSize / (1024.0 * 1024.0)
                _uiState.value = SettingsUiState.Success(
                    "Caché limpiado: ${String.format("%.2f", sizeInMB)} MB liberados ✓"
                )
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Error al limpiar caché: ${e.message}")
            }
        }
    }

    /**
     * Calcular tamaño de un directorio
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size: Long = 0
        if (directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    calculateDirectorySize(file)
                } else {
                    file.length()
                }
            }
        } else {
            size = directory.length()
        }
        return size
    }

    /**
     * Eliminar cuenta del usuario
     */
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _uiState.value = SettingsUiState.Loading

                val user = firebaseAuth.currentUser
                if (user == null) {
                    _uiState.value = SettingsUiState.Error("Usuario no autenticado")
                    return@launch
                }

                val userId = user.uid

                // 1. Eliminar datos del usuario en Firestore
                firestore.collection("users")
                    .document(userId)
                    .delete()
                    .await()

                // 2. Eliminar configuraciones de usuario
                firestore.collection("user_settings")
                    .document(userId)
                    .delete()
                    .await()

                // 3. Eliminar grupos donde es admin
                val adminGroups = firestore.collection("angelito_groups")
                    .whereEqualTo("adminId", userId)
                    .get()
                    .await()

                adminGroups.documents.forEach { doc ->
                    doc.reference.delete().await()
                }

                // 4. Remover de grupos donde es miembro
                val memberGroups = firestore.collection("angelito_groups")
                    .whereArrayContains("members", userId)
                    .get()
                    .await()

                memberGroups.documents.forEach { doc ->
                    val currentMembers = doc.get("members") as? List<*> ?: emptyList<String>()
                    val updatedMembers = currentMembers.filterNot { it == userId }
                    doc.reference.update("members", updatedMembers).await()
                }

                // 5. Limpiar preferencias locales
                preferencesManager.clearAll()

                // 6. Eliminar cuenta de Firebase Auth
                user.delete().await()

                _uiState.value = SettingsUiState.AccountDeleted
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    "Error al eliminar cuenta: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Initial
    }
}

/**
 * Estados de la UI de configuración
 */
sealed class SettingsUiState {
    object Initial : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
    object AccountDeleted : SettingsUiState()
}