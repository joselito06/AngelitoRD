package com.example.angelitord.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.angelitord.models.User

/**
 * Componente de Avatar con iniciales del usuario
 */
@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    fontSize: Int = 16,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val initials = getInitials(name)

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Obtener iniciales del nombre
 */
private fun getInitials(name: String): String {
    if (name.isBlank()) return "?"

    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> {
            // Primera letra del nombre y primera letra del apellido
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        }
        else -> {
            // Primera y segunda letra del nombre
            val firstName = parts[0]
            if (firstName.length >= 2) {
                "${firstName[0].uppercaseChar()}${firstName[1].uppercaseChar()}"
            } else {
                firstName.first().uppercaseChar().toString()
            }
        }
    }
}

/**
 * Menú de usuario con avatar y opciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileMenu(
    user: User?,
    email: String,
    groupCount: Int,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Avatar clickeable
        IconButton(
            onClick = { showMenu = true }
        ) {
            if (user != null) {
                UserAvatar(
                    name = user.name,
                    size = 36,
                    fontSize = 14
                )
            } else {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Menú desplegable
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.width(280.dp)
        ) {
            // Header con información del usuario
            UserMenuHeader(
                name = user?.name ?: "Usuario",
                email = email,
                groupCount = groupCount
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Opción: Mi Perfil
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            "Mi Perfil",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Ver y editar información",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    onProfileClick()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            // Opción: Configuración (para futuro)
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            "Configuración",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Preferencias de la app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    onSettingsClick()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Opción: Cerrar Sesión
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            "Cerrar Sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Salir de tu cuenta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    onSignOut()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

/**
 * Header del menú con información del usuario
 */
@Composable
private fun UserMenuHeader(
    name: String,
    email: String,
    groupCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar grande
            UserAvatar(
                name = name,
                size = 56,
                fontSize = 22
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del usuario
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Estadísticas rápidas
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickStat(
                    icon = Icons.Default.Group,
                    value = groupCount.toString(),
                    label = "Grupos"
                )
                QuickStat(
                    icon = Icons.Default.CardGiftcard,
                    value = "🎁",
                    label = "Angelito"
                )
            }
        }
    }
}

/**
 * Componente de estadística rápida
 */
@Composable
private fun RowScope.QuickStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/**
 * Versión simplificada para TopBar
 */
@Composable
fun UserAvatarButton(
    user: User?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        if (user != null) {
            UserAvatar(
                name = user.name,
                size = 36,
                fontSize = 14
            )
        } else {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Perfil",
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Colores aleatorios para avatares (para variedad)
 */
object AvatarColors {
    val colors = listOf(
        Color(0xFF1976D2), // Azul
        Color(0xFF388E3C), // Verde
        Color(0xFFD32F2F), // Rojo
        Color(0xFFF57C00), // Naranja
        Color(0xFF7B1FA2), // Púrpura
        Color(0xFF0097A7), // Cian
        Color(0xFFC2185B), // Rosa
        Color(0xFF5D4037), // Marrón
        Color(0xFF455A64), // Gris Azulado
        Color(0xFF00897B)  // Verde Azulado
    )

    fun getColorForName(name: String): Color {
        val hash = name.hashCode()
        val index = Math.abs(hash % colors.size)
        return colors[index]
    }
}

/**
 * Avatar con color basado en el nombre (para variedad)
 */
@Composable
fun ColorfulUserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40,
    fontSize: Int = 16
) {
    val backgroundColor = AvatarColors.getColorForName(name)

    UserAvatar(
        name = name,
        modifier = modifier,
        size = size,
        fontSize = fontSize,
        backgroundColor = backgroundColor,
        textColor = Color.White
    )
}