package com.example.angelitord.repository

import com.example.angelitord.models.AngelitoGroup
import com.example.angelitord.models.GroupStatus
import com.example.angelitord.models.User
import com.example.angelitord.utils.AngelitoAssigner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AngelitoRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val groupsCollection = firestore.collection("angelito_groups")
    private val usersCollection = firestore.collection("users")

    /**
     * Crear un nuevo grupo de angelito
     */
    suspend fun createGroup(
        groupName: String,
        adminId: String,
        budget: Double? = null,
        eventDate: Long? = null,
        description: String = ""
    ): Result<String> = try {
        val group = AngelitoGroup(
            groupName = groupName,
            adminId = adminId,
            members = listOf(adminId),
            status = GroupStatus.PENDING,
            budget = budget,
            eventDate = eventDate,
            description = description,
            isLocked = false
        )

        val documentRef = groupsCollection.add(group).await()
        Result.success(documentRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Agregar miembro al grupo
     */
    suspend fun addMemberToGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (group.isLocked) {
                return Result.failure(Exception("Este grupo está bloqueado y no acepta nuevos miembros"))
            }

            if (group.members.contains(userId)) {
                return Result.failure(Exception("El usuario ya está en el grupo"))
            }

            val updatedMembers = group.members + userId
            val newStatus = if (updatedMembers.size >= 3) GroupStatus.READY else GroupStatus.PENDING

            groupRef.update(
                mapOf(
                    "members" to updatedMembers,
                    "status" to newStatus
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remover miembro del grupo (admin o el mismo usuario)
     */
    suspend fun removeMemberFromGroup(groupId: String, userId: String, requesterId: String): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            // Verificar permisos: admin o el mismo usuario
            if (requesterId != group.adminId && requesterId != userId) {
                return Result.failure(Exception("No tienes permiso para remover este miembro"))
            }

            // No permitir que el admin se salga
            if (userId == group.adminId) {
                return Result.failure(Exception("El administrador no puede salir del grupo. Debes eliminarlo."))
            }

            if (!group.members.contains(userId)) {
                return Result.failure(Exception("El usuario no está en el grupo"))
            }

            // Si ya se hizo el sorteo, no permitir salir
            if (group.status == GroupStatus.ASSIGNED || group.status == GroupStatus.REVEALED || group.status == GroupStatus.COMPLETED) {
                return Result.failure(Exception("No puedes salir después de que se ha realizado el sorteo"))
            }

            val updatedMembers = group.members - userId

            // Si quedan menos de 3 miembros, cambiar status a PENDING
            val newStatus = if (updatedMembers.size >= 3) group.status else GroupStatus.PENDING

            groupRef.update(
                mapOf(
                    "members" to updatedMembers,
                    "status" to newStatus
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Bloquear/Desbloquear grupo (solo admin)
     */
    suspend fun toggleGroupLock(groupId: String, adminId: String): Result<Boolean> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (group.adminId != adminId) {
                return Result.failure(Exception("Solo el administrador puede bloquear/desbloquear el grupo"))
            }

            val newLockedState = !group.isLocked

            groupRef.update("isLocked", newLockedState).await()

            Result.success(newLockedState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Realizar el sorteo de angelitos
     */
    suspend fun performDraw(groupId: String): Result<Map<String, String>> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (group.status != GroupStatus.READY) {
                return Result.failure(Exception("El grupo no está listo para el sorteo"))
            }

            if (group.members.size < 3) {
                return Result.failure(Exception("Se necesitan al menos 3 miembros"))
            }

            // Generar asignaciones
            val assignments = AngelitoAssigner.assignAngelitos(group.members)

            // Actualizar en Firebase
            groupRef.update(
                mapOf(
                    "assignments" to assignments,
                    "status" to GroupStatus.ASSIGNED
                )
            ).await()

            Result.success(assignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener la asignación de un usuario específico
     */
    suspend fun getMyAssignment(groupId: String, userId: String): Result<User?> {
        return try {
            val group = groupsCollection.document(groupId).get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (group.status != GroupStatus.ASSIGNED) {
                return Result.failure(Exception("Aún no se han asignado los angelitos"))
            }

            val receiverId = group.assignments[userId]
            if (receiverId == null) {
                return Result.failure(Exception("No tienes asignación en este grupo"))
            }

            val receiver = usersCollection.document(receiverId).get().await().toObject<User>()
            Result.success(receiver)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener todos los grupos donde el usuario es miembro
     */
    suspend fun getUserGroups(userId: String): Result<List<AngelitoGroup>> = try {
        val groups = groupsCollection
            .whereArrayContains("members", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject<AngelitoGroup>() }

        Result.success(groups)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtener detalles de un grupo
     */
    suspend fun getGroupDetails(groupId: String): Result<AngelitoGroup> = try {
        val doc = groupsCollection.document(groupId).get().await()
        val group = doc.toObject<AngelitoGroup>()?.copy(groupId = doc.id)

        if (group != null) {
            Result.success(group.copy(groupId = doc.id))
        } else {
            Result.failure(Exception("Grupo no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Obtener información de usuarios del grupo
     */
    suspend fun getGroupMembers(memberIds: List<String>): Result<List<User>> = try {
        val users = memberIds.mapNotNull { userId ->
            usersCollection.document(userId).get().await().toObject<User>()
        }
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Eliminar grupo (solo el admin puede)
     */
    suspend fun deleteGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            // Verificar que el usuario sea el administrador
            if (group.adminId != userId) {
                return Result.failure(Exception("Solo el administrador puede eliminar el grupo"))
            }

            // Eliminar el grupo
            groupRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Disolver sorteo (reiniciar el grupo al estado READY o PENDING)
     */
    suspend fun dissolveDraw(groupId: String, adminId: String): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (group.adminId != adminId) {
                return Result.failure(Exception("Solo el administrador puede disolver el sorteo"))
            }

            if (group.status != GroupStatus.ASSIGNED && group.status != GroupStatus.REVEALED) {
                return Result.failure(Exception("No hay sorteo para disolver"))
            }

            // Calcular nuevo status
            val newStatus = if (group.members.size >= 3) GroupStatus.READY else GroupStatus.PENDING

            groupRef.update(
                mapOf(
                    "assignments" to emptyMap<String, String>(),
                    "status" to newStatus,
                    "revealedAt" to null
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Editar información del grupo
     */
    suspend fun updateGroup(
        groupId: String,
        adminId: String,
        groupName: String,
        budget: Double?,
        eventDate: Long?,
        description: String,
        locationName: String,
        locationLatitude: Double?,
        locationLongitude: Double?
    ): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)
            val group = groupRef.get().await().toObject<AngelitoGroup>()

            if (group == null) {
                return Result.failure(Exception("Grupo no encontrado"))
            }

            if (group.adminId != adminId) {
                return Result.failure(Exception("Solo el administrador puede editar el grupo"))
            }

            val updates = mutableMapOf<String, Any?>(
                "groupName" to groupName,
                "description" to description,
                "locationName" to locationName
            )

            // Solo actualizar si no son null o si se quiere limpiar
            if (budget != null) updates["budget"] = budget
            if (eventDate != null) updates["eventDate"] = eventDate
            if (locationLatitude != null) updates["locationLatitude"] = locationLatitude
            if (locationLongitude != null) updates["locationLongitude"] = locationLongitude

            groupRef.update(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener información del usuario
     */
    suspend fun getUserInfo(userId: String): Result<User> = try {
        val user = usersCollection.document(userId).get().await().toObject<User>()
        if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Actualizar información del usuario
     */
    suspend fun updateUserInfo(userId: String, name: String): Result<Unit> = try {
        usersCollection.document(userId).update("name", name).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}