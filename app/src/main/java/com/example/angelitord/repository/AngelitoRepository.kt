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
            description = description
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
        val group = groupsCollection.document(groupId).get().await().toObject<AngelitoGroup>()
        if (group != null) {
            Result.success(group)
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
}