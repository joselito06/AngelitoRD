package com.example.angelitord.models


data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val debugModeEnabled: Boolean = false
)