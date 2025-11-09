package com.example.angelitord.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angelitord.models.User
import com.example.angelitord.repository.UnitOfWork
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UnitOfWork,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()

    private val _groupCount = MutableStateFlow(0)
    val groupCount: StateFlow<Int> = _groupCount.asStateFlow()

    /**
     * Cargar información del usuario
     */
    fun loadUserInfo(userId: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            repository.angelitoRepository.getUserInfo(userId)
                .onSuccess { user ->
                    _userInfo.value = user
                    _uiState.value = ProfileUiState.Success
                    loadGroupCount(userId)
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "Error al cargar perfil")
                }
        }
    }

    /**
     * Cargar cantidad de grupos del usuario
     */
    private fun loadGroupCount(userId: String) {
        viewModelScope.launch {
            repository.angelitoRepository.getUserGroups(userId)
                .onSuccess { groups ->
                    _groupCount.value = groups.size
                }
        }
    }

    /**
     * Actualizar nombre del usuario
     */
    fun updateUserName(userId: String, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank() || newName.length < 3) {
                _uiState.value = ProfileUiState.Error("El nombre debe tener al menos 3 caracteres")
                return@launch
            }

            _uiState.value = ProfileUiState.Loading

            repository.angelitoRepository.updateUserInfo(userId, newName)
                .onSuccess {
                    _userInfo.value = _userInfo.value?.copy(name = newName)
                    _uiState.value = ProfileUiState.UpdateSuccess("Nombre actualizado correctamente")
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "Error al actualizar nombre")
                }
        }
    }

    /**
     * Cambiar contraseña
     */
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                // Validaciones
                if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                    _uiState.value = ProfileUiState.Error("Por favor completa todos los campos")
                    return@launch
                }

                if (newPassword.length < 6) {
                    _uiState.value = ProfileUiState.Error("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }

                if (newPassword != confirmPassword) {
                    _uiState.value = ProfileUiState.Error("Las contraseñas no coinciden")
                    return@launch
                }

                _uiState.value = ProfileUiState.Loading

                val user = firebaseAuth.currentUser
                if (user == null) {
                    _uiState.value = ProfileUiState.Error("Usuario no autenticado")
                    return@launch
                }

                // Reautenticar con la contraseña actual
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
                    user.email ?: "",
                    currentPassword
                )

                user.reauthenticate(credential).await()

                // Cambiar contraseña
                user.updatePassword(newPassword).await()

                _uiState.value = ProfileUiState.UpdateSuccess("Contraseña actualizada correctamente")
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    when {
                        e.message?.contains("password is invalid") == true ->
                            "Contraseña actual incorrecta"
                        e.message?.contains("network") == true ->
                            "Error de conexión. Verifica tu internet"
                        else ->
                            "Error: ${e.message}"
                    }
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Initial
    }
}

sealed class ProfileUiState {
    object Initial : ProfileUiState()
    object Loading : ProfileUiState()
    object Success : ProfileUiState()
    data class UpdateSuccess(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}