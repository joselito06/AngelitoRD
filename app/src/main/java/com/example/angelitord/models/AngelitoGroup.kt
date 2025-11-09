package com.example.angelitord.models

import com.google.firebase.firestore.DocumentId


data class AngelitoGroup(
    @DocumentId
    val groupId: String = "",
    val groupName: String = "",
    val adminId: String = "", // Usuario creador/administrador
    val members: List<String> = emptyList(), // Lista de IDs de usuarios
    val status: GroupStatus = GroupStatus.PENDING,
    val assignments: Map<String, String> = emptyMap(), // gifter -> receiver
    val budget: Double? = null,
    val eventDate: Long? = null,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val revealedAt: Long? = null, // Cuando se revelen los angelitos
    val isLocked: Boolean = false, // Si está bloqueado no pueden unirse más personas
    val locationName: String = "", // Nombre del lugar (ej: "Casa de Juan")
    val locationLatitude: Double? = null, // Latitud para el mapa
    val locationLongitude: Double? = null // Longitud para el mapa
)

enum class GroupStatus {
    PENDING,      // Esperando más miembros
    READY,        // Listo para sortear (min 3 miembros)
    ASSIGNED,     // Ya se hizo el sorteo
    REVEALED,     // Se revelaron los angelitos
    COMPLETED     // Evento finalizado
}