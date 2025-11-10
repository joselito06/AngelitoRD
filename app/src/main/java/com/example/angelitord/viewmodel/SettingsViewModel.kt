package com.example.angelitord.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Initial)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    /**
     * Cargar configuraciones guardadas
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val doc = firestore.collection("user_settings")
                    .document(userId)
                    .get()
                    .await()

                if (doc.exists()) {
                    _settings.value = AppSettings(
                        notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                        vibrationEnabled = doc.getBoolean("vibrationEnabled") ?: true,
                        soundEnabled = doc.getBoolean("soundEnabled") ?: true,
                        darkThemeEnabled = doc.getBoolean("darkThemeEnabled") ?: false,
                        analyticsEnabled = doc.getBoolean("analyticsEnabled") ?: true,
                        debugModeEnabled = doc.getBoolean("debugModeEnabled") ?: false
                    )
                }
            } catch (e: Exception) {
                // Si no existen configuraciones, usar las predeterminadas
                _settings.value = AppSettings()
            }
        }
    }

    /**
     * Guardar configuraciones en Firestore
     */
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val settingsMap = mapOf(
                    "notificationsEnabled" to _settings.value.notificationsEnabled,
                    "vibrationEnabled" to _settings.value.vibrationEnabled,
                    "soundEnabled" to _settings.value.soundEnabled,
                    "darkThemeEnabled" to _settings.value.darkThemeEnabled,
                    "analyticsEnabled" to _settings.value.analyticsEnabled,
                    "debugModeEnabled" to _settings.value.debugModeEnabled,
                    "lastUpdated" to System.currentTimeMillis()
                )

                firestore.collection("user_settings")
                    .document(userId)
                    .set(settingsMap)
                    .await()
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Error al guardar configuración")
            }
        }
    }

    /**
     * Actualizar configuración de notificaciones
     */
    fun updateNotificationsSetting(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)

        // Si se desactivan las notificaciones, desactivar también vibración y sonido
        if (!enabled) {
            _settings.value = _settings.value.copy(
                vibrationEnabled = false,
                soundEnabled = false
            )
        }

        saveSettings()
        _uiState.value = SettingsUiState.Success(
            if (enabled) "Notificaciones activadas" else "Notificaciones desactivadas"
        )
    }

    /**
     * Actualizar configuración de vibración
     */
    fun updateVibrationSetting(enabled: Boolean) {
        _settings.value = _settings.value.copy(vibrationEnabled = enabled)
        saveSettings()
        _uiState.value = SettingsUiState.Success(
            if (enabled) "Vibración activada" else "Vibración desactivada"
        )
    }

    /**
     * Actualizar configuración de sonido
     */
    fun updateSoundSetting(enabled: Boolean) {
        _settings.value = _settings.value.copy(soundEnabled = enabled)
        saveSettings()
        _uiState.value = SettingsUiState.Success(
            if (enabled) "Sonido activado" else "Sonido desactivado"
        )
    }

    /**
     * Actualizar tema oscuro
     */
    fun updateDarkThemeSetting(enabled: Boolean) {
        _settings.value = _settings.value.copy(darkThemeEnabled = enabled)
        saveSettings()
        _uiState.value = SettingsUiState.Success(
            if (enabled) "Tema oscuro activado" else "Tema claro activado"
        )
    }

    /**
     * Actualizar análisis
     */
    fun updateAnalyticsSetting(enabled: Boolean) {
        _settings.value = _settings.value.copy(analyticsEnabled = enabled)
        saveSettings()
        _uiState.value = SettingsUiState.Success(
            if (enabled) "Análisis activado" else "Análisis desactivado"
        )
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
                    "Caché limpiado: ${String.format("%.2f", sizeInMB)} MB liberados"
                )
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Error al limpiar caché")
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

                // 2. Eliminar configuraciones
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

                // 5. Eliminar cuenta de Firebase Auth
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
 * Data class para las configuraciones de la app
 */
data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val debugModeEnabled: Boolean = false
)

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