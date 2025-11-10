package com.example.angelitord.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelitord.ui.components.AppTopBar
import com.example.angelitord.ui.components.WhatsApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndSupportScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Ayuda y Soporte",
                onNavigationClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Contacto rápido
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "¿Necesitas ayuda?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Estamos aquí para ayudarte. Contáctanos por cualquiera de estos medios.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Métodos de contacto
            Text(
                text = "Contáctanos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            ContactMethodCard(
                icon = Icons.Default.Email,
                title = "Email",
                subtitle = "soporte@angelitord.com",
                description = "Respuesta en 24-48 horas",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soporte@angelitord.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Soporte Angelito RD")
                    }
                    context.startActivity(intent)
                }
            )

            ContactMethodCard(
                icon = Icons.WhatsApp,
                title = "WhatsApp",
                subtitle = "+1 (809) 555-0123",
                description = "Lun-Vie: 9AM - 6PM",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/18095550123")
                    }
                    context.startActivity(intent)
                }
            )

            ContactMethodCard(
                icon = Icons.Default.Phone,
                title = "Teléfono",
                subtitle = "+1 (809) 555-0123",
                description = "Lun-Vie: 9AM - 6PM",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:+18095550123")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // FAQs
            Text(
                text = "Preguntas Frecuentes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            FAQItem(
                question = "¿Cómo creo un grupo?",
                answer = "Ve a la pantalla principal y toca el botón '+'. Ingresa el nombre del grupo, " +
                        "presupuesto (opcional), y fecha del evento. Luego invita a los participantes."
            )

            FAQItem(
                question = "¿Cómo realizo un sorteo?",
                answer = "Como administrador del grupo, ve a los detalles del grupo y toca 'Realizar Sorteo'. " +
                        "El sistema asignará aleatoriamente a cada participante su angelito."
            )

            FAQItem(
                question = "¿Puedo ver a quién le toca darme regalo?",
                answer = "No, esto arruinaría la sorpresa. Solo puedes ver a quién debes darle regalo tú. " +
                        "La magia del Angelito es la sorpresa."
            )

            FAQItem(
                question = "¿Puedo hacer otro sorteo si no me gusta el resultado?",
                answer = "Una vez realizado el sorteo, no se puede rehacer automáticamente. Esto mantiene " +
                        "la integridad del juego. Si hay un problema real, contacta a soporte."
            )

            FAQItem(
                question = "¿Cómo invito personas a mi grupo?",
                answer = "Puedes compartir el código del grupo o enviar invitaciones directamente desde " +
                        "la aplicación. Los invitados recibirán una notificación."
            )

            FAQItem(
                question = "¿Puedo editar un grupo después de crearlo?",
                answer = "Sí, como administrador puedes editar el nombre, presupuesto, fecha y descripción. " +
                        "También puedes agregar o remover participantes antes del sorteo."
            )

            FAQItem(
                question = "¿Puedo estar en varios grupos?",
                answer = "¡Sí! Puedes participar en tantos grupos como quieras. La app te mantiene " +
                        "organizado mostrándote todos tus grupos activos."
            )

            FAQItem(
                question = "¿La aplicación es gratis?",
                answer = "Sí, Angelito RD es completamente gratuita. Podrás crear grupos ilimitados " +
                        "y usar todas las funciones sin costo alguno."
            )

            FAQItem(
                question = "¿Mis datos están seguros?",
                answer = "Sí, todos tus datos están protegidos con Firebase y encriptación SSL. " +
                        "Lee nuestra Política de Privacidad para más detalles."
            )

            FAQItem(
                question = "¿Cómo elimino mi cuenta?",
                answer = "Ve a Configuración > Zona Peligrosa > Eliminar Cuenta. Esta acción es " +
                        "irreversible y eliminará todos tus datos permanentemente."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tutoriales
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Video Tutoriales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Aprende a usar la app paso a paso",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://www.youtube.com/@angelitord")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Tutoriales")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ContactMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FAQItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}