package com.example.angelitord.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Utilidad para compartir invitaciones a grupos de Angelito
 * usando intents de Android (open source)
 */
object ShareHelper {

    /**
     * Generar el mensaje de invitación
     */
    private fun generateInvitationMessage(
        groupName: String,
        adminName: String,
        groupId: String
    ): String {
        return """
            🎁 ¡Te han invitado a un Angelito! 🎁
            
            $adminName te invita a participar en el grupo "$groupName"
            
            Descarga Angelito RD y únete con el código:
            $groupId
            
            🎄 ¡Será divertido! 🎄
        """.trimIndent()
    }

    /**
     * Compartir invitación por cualquier app (chooser genérico)
     */
    fun shareInvitation(
        context: Context,
        groupName: String,
        adminName: String,
        groupId: String
    ) {
        val message = generateInvitationMessage(groupName, adminName, groupId)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "Invitación a Angelito RD")
        }

        context.startActivity(
            Intent.createChooser(intent, "Compartir invitación por...")
        )
    }

    /**
     * Compartir específicamente por WhatsApp
     */
    fun shareViaWhatsApp(
        context: Context,
        phoneNumber: String? = null,
        groupName: String,
        adminName: String,
        groupId: String
    ) {
        val message = generateInvitationMessage(groupName, adminName, groupId)

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = if (phoneNumber != null) {
                    // Enviar a un contacto específico
                    Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
                } else {
                    // Abrir WhatsApp para elegir contacto
                    Uri.parse("https://wa.me/?text=${Uri.encode(message)}")
                }
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si WhatsApp no está instalado, usar el chooser genérico
            shareInvitation(context, groupName, adminName, groupId)
        }
    }

    /**
     * Compartir por Email
     */
    fun shareViaEmail(
        context: Context,
        email: String? = null,
        groupName: String,
        adminName: String,
        groupId: String
    ) {
        val message = generateInvitationMessage(groupName, adminName, groupId)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            if (email != null) {
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            }
            putExtra(Intent.EXTRA_SUBJECT, "Invitación a Angelito RD - $groupName")
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si no hay app de email, usar el chooser genérico
            shareInvitation(context, groupName, adminName, groupId)
        }
    }

    /**
     * Compartir por SMS
     */
    fun shareViaSMS(
        context: Context,
        phoneNumber: String? = null,
        groupName: String,
        adminName: String,
        groupId: String
    ) {
        val message = generateInvitationMessage(groupName, adminName, groupId)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = if (phoneNumber != null) {
                Uri.parse("sms:$phoneNumber")
            } else {
                Uri.parse("sms:")
            }
            putExtra("sms_body", message)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareInvitation(context, groupName, adminName, groupId)
        }
    }

    /**
     * Compartir por Telegram
     */
    fun shareViaTelegram(
        context: Context,
        groupName: String,
        adminName: String,
        groupId: String
    ) {
        val message = generateInvitationMessage(groupName, adminName, groupId)

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://t.me/share/url?url=${Uri.encode("")}&text=${Uri.encode(message)}")
                setPackage("org.telegram.messenger")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si Telegram no está instalado, usar el chooser genérico
            shareInvitation(context, groupName, adminName, groupId)
        }
    }

    /**
     * Copiar código al portapapeles
     */
    fun copyToClipboard(context: Context, groupId: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Código de Angelito", groupId)
        clipboard.setPrimaryClip(clip)
    }
}