package com.example.angelitord.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.angelitord.models.AngelitoGroup
import com.example.angelitord.models.User
import com.example.angelitord.repository.UnitOfWork
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: UnitOfWork
) : ViewModel() {
    private val _uiState = MutableStateFlow<GroupUiState>(GroupUiState.Initial)
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    private val _userGroups = MutableStateFlow<List<AngelitoGroup>>(emptyList())
    val userGroups: StateFlow<List<AngelitoGroup>> = _userGroups.asStateFlow()

    private val _currentGroup = MutableStateFlow<AngelitoGroup?>(null)
    val currentGroup: StateFlow<AngelitoGroup?> = _currentGroup.asStateFlow()

    private val _groupMembers = MutableStateFlow<List<User>>(emptyList())
    val groupMembers: StateFlow<List<User>> = _groupMembers.asStateFlow()

    /**
     * Crear un nuevo grupo
     */
    fun createGroup(
        groupName: String,
        adminId: String,
        budget: Double? = null,
        eventDate: Long? = null,
        description: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.createGroup(groupName, adminId, budget, eventDate, description)
                .onSuccess { groupId ->
                    _uiState.value = GroupUiState.GroupCreated(groupId)
                    loadUserGroups(adminId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    /**
     * Agregar miembro al grupo
     */
    fun addMember(groupId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.addMemberToGroup(groupId, userId)
                .onSuccess {
                    _uiState.value = GroupUiState.MemberAdded
                    loadGroupDetails(groupId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al agregar miembro")
                }
        }
    }

    /**
     * Remover miembro del grupo
     */
    fun removeMember(groupId: String, userId: String, requesterId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.removeMemberFromGroup(groupId, userId, requesterId)
                .onSuccess {
                    _uiState.value = GroupUiState.MemberRemoved
                    loadGroupDetails(groupId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al remover miembro")
                }
        }
    }

    /**
     * Bloquear/Desbloquear grupo
     */
    fun toggleGroupLock(groupId: String, adminId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.toggleGroupLock(groupId, adminId)
                .onSuccess { isLocked ->
                    _uiState.value = GroupUiState.GroupLockToggled(isLocked)
                    loadGroupDetails(groupId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al cambiar estado del grupo")
                }
        }
    }

    /**
     * Realizar el sorteo de angelitos
     */
    fun performDraw(groupId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.performDraw(groupId)
                .onSuccess { assignments ->
                    _uiState.value = GroupUiState.DrawCompleted(assignments)
                    loadGroupDetails(groupId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al realizar sorteo")
                }
        }
    }

    /**
     * Obtener mi asignación
     */
    fun getMyAssignment(groupId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.getMyAssignment(groupId, userId)
                .onSuccess { receiver ->
                    _uiState.value = GroupUiState.AssignmentRetrieved(receiver)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al obtener asignación")
                }
        }
    }

    /**
     * Cargar grupos del usuario
     */
    fun loadUserGroups(userId: String) {
        viewModelScope.launch {
            repository.angelitoRepository.getUserGroups(userId)
                .onSuccess { groups ->
                    _userGroups.value = groups
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al cargar grupos")
                }
        }
    }

    /**
     * Cargar detalles de un grupo
     */
    fun loadGroupDetails(groupId: String) {
        viewModelScope.launch {
            repository.angelitoRepository.getGroupDetails(groupId)
                .onSuccess { group ->
                    _currentGroup.value = group
                    loadGroupMembers(group.members)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al cargar grupo")
                }
        }
    }

    /**
     * Cargar miembros del grupo
     */
    private fun loadGroupMembers(memberIds: List<String>) {
        viewModelScope.launch {
            repository.angelitoRepository.getGroupMembers(memberIds)
                .onSuccess { members ->
                    _groupMembers.value = members
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al cargar miembros")
                }
        }
    }

    /**
     * Eliminar grupo (solo admin)
     */
    fun deleteGroup(groupId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.deleteGroup(groupId, userId)
                .onSuccess {
                    _uiState.value = GroupUiState.GroupDeleted
                    loadUserGroups(userId) // Recargar lista de grupos
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al eliminar grupo")
                }
        }
    }

    /**
     * Disolver sorteo (solo admin)
     */
    fun dissolveDraw(groupId: String, adminId: String) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.dissolveDraw(groupId, adminId)
                .onSuccess {
                    _uiState.value = GroupUiState.DrawDissolved
                    loadGroupDetails(groupId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al disolver sorteo")
                }
        }
    }

    /**
     * Editar grupo (solo admin)
     */
    fun updateGroup(
        groupId: String,
        adminId: String,
        groupName: String,
        budget: Double?,
        eventDate: Long?,
        description: String,
        locationName: String,
        locationLatitude: Double?,
        locationLongitude: Double?
    ) {
        viewModelScope.launch {
            _uiState.value = GroupUiState.Loading

            repository.angelitoRepository.updateGroup(
                groupId, adminId, groupName, budget, eventDate,
                description, locationName, locationLatitude, locationLongitude
            )
                .onSuccess {
                    _uiState.value = GroupUiState.GroupUpdated
                    loadGroupDetails(groupId)
                }
                .onFailure { error ->
                    _uiState.value = GroupUiState.Error(error.message ?: "Error al actualizar grupo")
                }
        }
    }

    fun resetState() {
        _uiState.value = GroupUiState.Initial
    }
}

sealed class GroupUiState {
    object Initial : GroupUiState()
    object Loading : GroupUiState()
    data class GroupCreated(val groupId: String) : GroupUiState()
    object MemberAdded : GroupUiState()
    object MemberRemoved : GroupUiState()
    data class GroupLockToggled(val isLocked: Boolean) : GroupUiState()
    data class DrawCompleted(val assignments: Map<String, String>) : GroupUiState()
    object DrawDissolved : GroupUiState()
    object GroupUpdated : GroupUiState()
    data class AssignmentRetrieved(val receiver: User?) : GroupUiState()
    object GroupDeleted : GroupUiState()
    data class Error(val message: String) : GroupUiState()
}