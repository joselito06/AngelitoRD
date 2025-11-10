package com.example.angelitord.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

/**
 * TopAppBar personalizado para Angelito RD con logo estilizado
 * Automáticamente usa blanco en modo claro y negro en modo oscuro
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconDescription: String = "Volver",
    actions: @Composable () -> Unit = {},
    useStyledTitle: Boolean = true
) {
    TopAppBar(
        title = {
            if (useStyledTitle) {
                StyledTitle(text = title)
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconDescription
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * TopAppBar principal con logo animado de Angelito RD
 * Usar para la pantalla principal/home
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarWithLogo(
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    navigationIconDescription: String = "Menú",
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            AngelitoLogo(animated = true)
        },
        navigationIcon = {
            if (onNavigationClick != null && navigationIcon != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconDescription
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * TopAppBar con color primario y logo simple
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBarPrimary(
    title: String? = null,
    showLogo: Boolean = false,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationIconDescription: String = "Volver",
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            when {
                showLogo -> AngelitoLogoSimple()
                title != null -> Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                else -> {}
            }
        },
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationIconDescription
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * Ejemplo de uso:
 *
 * Scaffold(
 *     topBar = {
 *         AppTopBar(
 *             title = "Configuración",
 *             onNavigationClick = { navController.popBackStack() }
 *         )
 *     }
 * ) { padding ->
 *     // Contenido
 * }
 *
 * O con acciones:
 *
 * AppTopBar(
 *     title = "Mi Grupo",
 *     onNavigationClick = { navController.popBackStack() },
 *     actions = {
 *         IconButton(onClick = { /* editar */ }) {
 *             Icon(Icons.Default.Edit, contentDescription = "Editar")
 *         }
 *     }
 * )
 */