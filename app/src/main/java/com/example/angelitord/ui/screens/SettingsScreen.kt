package com.example.angelitord.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelitord.ui.components.AppTopBar
import com.example.angelitord.viewmodel.SettingsUiState
import com.example.angelitord.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToHelp: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Debug: Imprimir cada vez que cambian las configuraciones
    LaunchedEffect(settings) {
        android.util.Log.d("SettingsScreen", "Settings changed: $settings")
    }

    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SettingsUiState.Success -> {
                snackbarHostState.showSnackbar(
                    state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetState()
            }
            is SettingsUiState.Error -> {
                snackbarHostState.showSnackbar(
                    state.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetState()
            }
            is SettingsUiState.AccountDeleted -> {
                // Navegar al login cuando se elimine la cuenta
                viewModel.resetState()
                onNavigateBack() // Esto cerrará la sesión
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Configuración",
                onNavigationClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // SECCIÓN: NOTIFICACIONES
            SectionHeader(
                icon = Icons.Default.Notifications,
                title = "Notificaciones"
            )

            SettingsSwitchItem(
                icon = Icons.Default.NotificationsActive,
                title = "Notificaciones Push",
                subtitle = "Recibir notificaciones de sorteos y eventos",
                checked = settings.notificationsEnabled,
                onCheckedChange = { viewModel.updateNotificationsSetting(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Vibration,
                title = "Vibración",
                subtitle = "Vibrar al recibir notificaciones",
                checked = settings.vibrationEnabled,
                onCheckedChange = { viewModel.updateVibrationSetting(it) },
                enabled = settings.notificationsEnabled
            )

            SettingsSwitchItem(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "Sonido",
                subtitle = "Reproducir sonido en notificaciones",
                checked = settings.soundEnabled,
                onCheckedChange = { viewModel.updateSoundSetting(it) },
                enabled = settings.notificationsEnabled
            )
            if(settings.notificationsEnabled){
                SettingsClickableItem(
                    icon = Icons.Default.Send,
                    title = "Probar Notificación",
                    subtitle = "Enviar una notificación de prueba",
                    onClick = {
                        //viewModel.testNotification()
                              },
                    //enabled = settings.notificationsEnabled
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: APARIENCIA
            SectionHeader(
                icon = Icons.Default.Palette,
                title = "Apariencia"
            )

            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Tema Oscuro",
                subtitle = "Usar tema oscuro en la aplicación",
                checked = settings.darkThemeEnabled,
                onCheckedChange = { viewModel.updateDarkThemeSetting(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: PRIVACIDAD
            /*SectionHeader(
                icon = Icons.Default.Security,
                title = "Privacidad y Seguridad"
            )

            SettingsSwitchItem(
                icon = Icons.Default.Analytics,
                title = "Análisis de Uso",
                subtitle = "Ayúdanos a mejorar la app compartiendo datos de uso",
                checked = settings.analyticsEnabled,
                onCheckedChange = { viewModel.updateAnalyticsSetting(it) }
            )

            SettingsClickableItem(
                icon = Icons.Default.Lock,
                title = "Privacidad",
                subtitle = "Administrar configuración de privacidad",
                onClick = { /* TODO: Navegar a pantalla de privacidad */ }
            )

            Spacer(modifier = Modifier.height(24.dp))*/

            // SECCIÓN: ALMACENAMIENTO
            SectionHeader(
                icon = Icons.Default.Storage,
                title = "Almacenamiento"
            )

            SettingsClickableItem(
                icon = Icons.Default.CleaningServices,
                title = "Limpiar Caché",
                subtitle = "Liberar espacio eliminando archivos temporales",
                onClick = { showClearCacheDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: ACERCA DE
            SectionHeader(
                icon = Icons.Default.Info,
                title = "Acerca de"
            )

            SettingsClickableItem(
                icon = Icons.Default.Info,
                title = "Acerca de Angelito RD",
                subtitle = "Versión 1.0.0",
                onClick = onNavigateToAbout
            )

            SettingsClickableItem(
                icon = Icons.Default.Description,
                title = "Términos y Condiciones",
                subtitle = "Lee nuestros términos de servicio",
                onClick = onNavigateToTerms
            )

            SettingsClickableItem(
                icon = Icons.Default.PrivacyTip,
                title = "Política de Privacidad",
                subtitle = "Cómo manejamos tus datos",
                onClick = onNavigateToPrivacy
            )

            SettingsClickableItem(
                icon = Icons.AutoMirrored.Filled.Help,
                title = "Ayuda y Soporte",
                subtitle = "¿Necesitas ayuda?",
                onClick = onNavigateToHelp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SECCIÓN: ZONA PELIGROSA
            SectionHeader(
                icon = Icons.Default.Warning,
                title = "Zona Peligrosa",
                color = MaterialTheme.colorScheme.error
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    SettingsClickableItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Eliminar Cuenta",
                        subtitle = "Eliminar permanentemente tu cuenta y todos tus datos",
                        onClick = { showDeleteAccountDialog = true },
                        iconTint = MaterialTheme.colorScheme.error,
                        titleColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Información de depuración (solo en desarrollo)
            if (settings.debugModeEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🔧 Modo Depuración",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Build: Debug\nVersión: 1.0.0\nSDK: ${android.os.Build.VERSION.SDK_INT}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }

    // Diálogo: Limpiar Caché
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            icon = {
                Icon(
                    Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Limpiar Caché") },
            text = {
                Text("¿Deseas eliminar los archivos temporales? Esto liberará espacio pero puede hacer que la app sea más lenta temporalmente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCache(context)
                        showClearCacheDialog = false
                    }
                ) {
                    Text("Limpiar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo: Eliminar Cuenta
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                viewModel.deleteAccount()
                showDeleteAccountDialog = false
            }
        )
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Eliminar Cuenta Permanentemente",
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text("Esta acción es IRREVERSIBLE y eliminará:")
                Spacer(modifier = Modifier.height(12.dp))

                DeletionWarningItem("• Todos tus grupos")
                DeletionWarningItem("• Tus datos personales")
                DeletionWarningItem("• Tu historial de intercambios")
                DeletionWarningItem("• Todas tus configuraciones")

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No podrás recuperar tu cuenta después de eliminarla",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar Mi Cuenta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeletionWarningItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}