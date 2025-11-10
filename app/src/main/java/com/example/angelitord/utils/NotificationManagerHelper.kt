package com.example.angelitord.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.angelitord.R
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "angelito_channel"
        const val CHANNEL_NAME = "Notificaciones de Angelito"
        const val CHANNEL_DESCRIPTION = "Notificaciones sobre sorteos y actualizaciones de grupos"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Crear canal de notificaciones (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION

                // Aplicar configuraciones según preferencias
                enableVibration(preferencesManager.vibrationEnabled)

                if (!preferencesManager.soundEnabled) {
                    setSound(null, null)
                }
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Activar o desactivar notificaciones
     */
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferencesManager.notificationsEnabled = enabled

        if (enabled) {
            // Suscribirse a topic general
            FirebaseMessaging.getInstance().subscribeToTopic("all_users").await()
        } else {
            // Desuscribirse de todos los topics
            FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users").await()
        }

        // Actualizar canal
        createNotificationChannel()
    }

    /**
     * Configurar vibración
     */
    fun setVibrationEnabled(enabled: Boolean) {
        preferencesManager.vibrationEnabled = enabled
        createNotificationChannel()
    }

    /**
     * Configurar sonido
     */
    fun setSoundEnabled(enabled: Boolean) {
        preferencesManager.soundEnabled = enabled
        createNotificationChannel()
    }

    /**
     * Verificar si las notificaciones están habilitadas en el sistema
     */
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationManager.areNotificationsEnabled()
        } else {
            true
        }
    }

    /**
     * Mostrar notificación local de prueba
     */
    fun showTestNotification() {
        if (!preferencesManager.notificationsEnabled) return

        // Verificar permisos en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎉 Notificación de Prueba")
            .setContentText("Las notificaciones están funcionando correctamente")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("¡Excelente! Tu configuración de notificaciones está funcionando perfectamente. Recibirás notificaciones sobre sorteos, actualizaciones de grupos y más.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply {
                if (preferencesManager.vibrationEnabled) {
                    setVibrate(longArrayOf(0, 500, 250, 500))
                } else {
                    setVibrate(longArrayOf(0))
                }

                if (!preferencesManager.soundEnabled) {
                    setSound(null)
                }
            }
            .build()

        NotificationManagerCompat.from(context).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    /**
     * Suscribirse a un grupo específico
     */
    suspend fun subscribeToGroup(groupId: String) {
        if (preferencesManager.notificationsEnabled) {
            FirebaseMessaging.getInstance().subscribeToTopic("group_$groupId").await()
        }
    }

    /**
     * Desuscribirse de un grupo
     */
    suspend fun unsubscribeFromGroup(groupId: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("group_$groupId").await()
    }
}