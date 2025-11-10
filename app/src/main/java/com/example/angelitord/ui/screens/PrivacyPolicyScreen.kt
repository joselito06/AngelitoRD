package com.example.angelitord.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelitord.ui.components.AppTopBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lastUpdated = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "DO"))
        .format(Date())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Política de Privacidad",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = "Última actualización: $lastUpdated",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Tu privacidad es importante",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Esta política describe cómo recopilamos, usamos y protegemos tu información personal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrivacySection(
                title = "1. Información que Recopilamos",
                content = """
                    1.1. Información de Cuenta:
                    • Nombre completo
                    • Dirección de correo electrónico
                    • Foto de perfil (opcional)
                    
                    1.2. Información de Uso:
                    • Grupos que creas o a los que te unes
                    • Interacciones dentro de la aplicación
                    • Fecha y hora de uso
                    
                    1.3. Información Técnica:
                    • Tipo de dispositivo
                    • Sistema operativo
                    • Dirección IP
                    • Identificador del dispositivo
                    
                    1.4. No recopilamos:
                    • Información financiera
                    • Información sensible personal
                    • Contactos del dispositivo (sin tu permiso)
                """.trimIndent()
            )

            PrivacySection(
                title = "2. Cómo Usamos tu Información",
                content = """
                    Usamos tu información para:
                    
                    2.1. Proporcionar el servicio:
                    • Crear y gestionar tu cuenta
                    • Facilitar los sorteos de Angelito
                    • Enviar notificaciones relevantes
                    
                    2.2. Mejorar la aplicación:
                    • Analizar patrones de uso
                    • Identificar y corregir errores
                    • Desarrollar nuevas funcionalidades
                    
                    2.3. Comunicarnos contigo:
                    • Responder consultas de soporte
                    • Enviar actualizaciones importantes
                    • Solicitar retroalimentación
                    
                    2.4. Seguridad:
                    • Prevenir fraudes
                    • Proteger contra uso indebido
                    • Cumplir con requisitos legales
                """.trimIndent()
            )

            PrivacySection(
                title = "3. Compartir Información",
                content = """
                    3.1. No vendemos tu información personal a terceros.
                    
                    3.2. Compartimos información limitada con:
                    
                    • Firebase/Google Cloud: Para almacenamiento y autenticación
                    • Servicios de análisis: Datos anónimos para mejorar la app
                    
                    3.3. Podemos compartir información si:
                    • Lo requiere la ley
                    • Es necesario proteger nuestros derechos
                    • Tienes tu consentimiento explícito
                    
                    3.4. Dentro de grupos:
                    • Tu nombre y foto son visibles para otros miembros
                    • Los administradores pueden ver tu email
                """.trimIndent()
            )

            PrivacySection(
                title = "4. Seguridad de los Datos",
                content = """
                    4.1. Medidas de seguridad:
                    • Cifrado SSL/TLS en todas las comunicaciones
                    • Almacenamiento seguro en Firebase
                    • Autenticación protegida
                    • Acceso restringido a datos
                    
                    4.2. Tu responsabilidad:
                    • Mantener tu contraseña segura
                    • No compartir tu cuenta
                    • Cerrar sesión en dispositivos compartidos
                    
                    4.3. Ningún sistema es 100% seguro. Te notificaremos si hay una brecha de seguridad que afecte tus datos.
                """.trimIndent()
            )

            PrivacySection(
                title = "5. Tus Derechos",
                content = """
                    Tienes derecho a:
                    
                    5.1. Acceder a tus datos:
                    • Ver qué información tenemos sobre ti
                    • Solicitar una copia de tus datos
                    
                    5.2. Corregir tus datos:
                    • Actualizar información incorrecta
                    • Modificar tu perfil
                    
                    5.3. Eliminar tus datos:
                    • Borrar tu cuenta desde Configuración
                    • Solicitar eliminación completa
                    
                    5.4. Controlar el uso:
                    • Desactivar notificaciones
                    • Limitar recopilación de datos
                    • Exportar tus datos
                    
                    5.5. Para ejercer estos derechos, contacta con soporte@angelitord.com
                """.trimIndent()
            )

            PrivacySection(
                title = "6. Retención de Datos",
                content = """
                    6.1. Mantenemos tus datos mientras tu cuenta esté activa.
                    
                    6.2. Después de eliminar tu cuenta:
                    • Datos personales: Eliminados en 30 días
                    • Datos anónimos: Pueden retenerse para análisis
                    • Datos requeridos por ley: Se mantienen según regulaciones
                    
                    6.3. Puedes solicitar eliminación inmediata contactándonos.
                """.trimIndent()
            )

            PrivacySection(
                title = "7. Cookies y Tecnologías Similares",
                content = """
                    7.1. Usamos tecnologías para:
                    • Mantener tu sesión activa
                    • Recordar preferencias
                    • Analizar uso de la aplicación
                    
                    7.2. Puedes gestionar estas preferencias en la configuración de tu dispositivo.
                """.trimIndent()
            )

            PrivacySection(
                title = "8. Privacidad de Menores",
                content = """
                    8.1. La aplicación está diseñada para usuarios de 13 años en adelante.
                    
                    8.2. No recopilamos intencionalmente información de menores de 13 años.
                    
                    8.3. Si descubrimos que un menor de 13 años ha proporcionado información, la eliminaremos inmediatamente.
                    
                    8.4. Los padres pueden contactarnos para revisar o eliminar información de sus hijos.
                """.trimIndent()
            )

            PrivacySection(
                title = "9. Cambios a esta Política",
                content = """
                    9.1. Podemos actualizar esta política ocasionalmente.
                    
                    9.2. Te notificaremos de cambios significativos mediante:
                    • Notificación en la aplicación
                    • Correo electrónico
                    • Mensaje al iniciar sesión
                    
                    9.3. La fecha de última actualización aparece al inicio de esta política.
                """.trimIndent()
            )

            PrivacySection(
                title = "10. Contacto",
                content = """
                    Para preguntas sobre privacidad:
                    
                    Email: privacidad@angelitord.com
                    Soporte: soporte@angelitord.com
                    
                    En la app: Configuración > Ayuda y Soporte
                    
                    Responderemos dentro de 48 horas hábiles.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛡️ Compromiso de Privacidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nos comprometemos a proteger tu privacidad y mantener tus datos seguros. " +
                                "Nunca venderemos tu información personal a terceros.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacySection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}