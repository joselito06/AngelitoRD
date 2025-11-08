package com.example.angelitord.models

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val fcmToken: String = "", // Para notificaciones push
    val createdAt: Long = System.currentTimeMillis()
)
