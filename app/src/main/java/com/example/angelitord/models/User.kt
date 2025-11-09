package com.example.angelitord.models

import com.google.firebase.firestore.DocumentId

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val fcmToken: String = "", // Para notificaciones push
    val createdAt: Long = System.currentTimeMillis()
)
