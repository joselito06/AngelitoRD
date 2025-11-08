package com.example.angelitord.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        // Observar cambios en el estado de autenticación
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    /**
     * Iniciar sesión con email y contraseña
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Validar inputs
                if (email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Por favor completa todos los campos")
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("Email inválido")
                    return@launch
                }

                // Iniciar sesión con Firebase
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()

                if (result.user != null) {
                    _authState.value = AuthState.Success("¡Bienvenido!")
                    _currentUser.value = result.user
                } else {
                    _authState.value = AuthState.Error("Error al iniciar sesión")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("password is invalid") == true ->
                            "Contraseña incorrecta"
                        e.message?.contains("no user record") == true ->
                            "Usuario no encontrado"
                        e.message?.contains("network") == true ->
                            "Error de conexión. Verifica tu internet"
                        else ->
                            "Error: ${e.message}"
                    }
                )
            }
        }
    }

    /**
     * Registrar nuevo usuario
     */
    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                // Validaciones
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    _authState.value = AuthState.Error("Por favor completa todos los campos")
                    return@launch
                }

                if (name.length < 3) {
                    _authState.value = AuthState.Error("El nombre debe tener al menos 3 caracteres")
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("Email inválido")
                    return@launch
                }

                if (password.length < 6) {
                    _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres")
                    return@launch
                }

                if (password != confirmPassword) {
                    _authState.value = AuthState.Error("Las contraseñas no coinciden")
                    return@launch
                }

                // Crear usuario en Firebase Auth
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

                if (result.user != null) {
                    val userId = result.user!!.uid

                    // Crear documento de usuario en Firestore
                    val user = hashMapOf(
                        "userId" to userId,
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(user)
                        .await()

                    _authState.value = AuthState.Success("¡Cuenta creada exitosamente!")
                    _currentUser.value = result.user
                } else {
                    _authState.value = AuthState.Error("Error al crear cuenta")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("email address is already in use") == true ->
                            "Este email ya está registrado"
                        e.message?.contains("network") == true ->
                            "Error de conexión. Verifica tu internet"
                        else ->
                            "Error: ${e.message}"
                    }
                )
            }
        }
    }

    /**
     * Cerrar sesión
     */
    fun signOut() {
        firebaseAuth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Initial
    }

    /**
     * Restablecer contraseña
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                if (email.isBlank()) {
                    _authState.value = AuthState.Error("Ingresa tu email")
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _authState.value = AuthState.Error("Email inválido")
                    return@launch
                }

                firebaseAuth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success("Email de recuperación enviado. Revisa tu bandeja de entrada")

            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error al enviar email: ${e.message}")
            }
        }
    }

    /**
     * Resetear el estado de autenticación
     */
    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}