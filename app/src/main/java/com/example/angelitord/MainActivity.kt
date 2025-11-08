package com.example.angelitord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.angelitord.models.AngelitoGroup
import com.example.angelitord.models.GroupStatus
import com.example.angelitord.ui.screens.CreateGroupScreen
import com.example.angelitord.ui.screens.GroupDetailScreen
import com.example.angelitord.ui.screens.JoinGroupScreen
import com.example.angelitord.ui.screens.LoginScreen
import com.example.angelitord.ui.screens.SignUpScreen
import com.example.angelitord.ui.theme.AngelitoRDTheme
import com.example.angelitord.viewmodel.AuthViewModel
import com.example.angelitord.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AngelitoRDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AngelitoApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AngelitoApp(
    authViewModel: AuthViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determinar la pantalla inicial basada en el estado de autenticación
    val startDestination = if (currentUser != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Pantallas de Autenticación
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                viewModel = authViewModel,
                onSignUpSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Pantallas Principales (requieren autenticación)
        composable("home") {
            HomeScreenWithScaffold(
                groupViewModel = groupViewModel,
                authViewModel = authViewModel,
                onCreateGroupClick = {
                    navController.navigate("create_group")
                },
                onJoinGroupClick = {
                    navController.navigate("join_group")
                },
                onGroupClick = { groupId ->
                    navController.navigate("group_detail/$groupId")
                }
            )
        }

        composable("create_group") {
            CreateGroupScreen(
                viewModel = groupViewModel,
                onGroupCreated = { groupId ->
                    navController.navigate("group_detail/$groupId") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable("join_group") {
            JoinGroupScreen(
                viewModel = groupViewModel,
                onGroupJoined = { groupId ->
                    navController.navigate("group_detail/$groupId") {
                        popUpTo("home")
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("group_detail/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                viewModel = groupViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenWithScaffold(
    groupViewModel: GroupViewModel,
    authViewModel: AuthViewModel,
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onGroupClick: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Angelito RD") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                            contentDescription = "Menú"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cerrar Sesión") },
                            onClick = {
                                authViewModel.signOut()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    androidx.compose.material.icons.Icons.Default.ExitToApp,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showOptionsDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Opciones")
            }
        }
    ) { padding ->
        HomeScreen(
            viewModel = groupViewModel,
            onGroupClick = onGroupClick,
            modifier = Modifier.padding(padding)
        )

        // Diálogo de opciones: Crear o Unirse
        if (showOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showOptionsDialog = false },
                title = { Text("¿Qué deseas hacer?") },
                text = {
                    Column {
                        ElevatedCard(
                            onClick = {
                                showOptionsDialog = false
                                onCreateGroupClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Crear Grupo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = "Serás el administrador",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ElevatedCard(
                            onClick = {
                                showOptionsDialog = false
                                onJoinGroupClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Unirse a Grupo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ingresa el código compartido",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showOptionsDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    viewModel: GroupViewModel,
    onGroupClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val groups by viewModel.userGroups.collectAsState()
    var groupToDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            viewModel.loadUserGroups(userId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (groups.isEmpty()) {
            EmptyGroupsScreen(modifier = modifier)
        } else {
            GroupsList(
                groups = groups,
                onGroupClick = onGroupClick,
                onDeleteGroup = { groupId ->
                    groupToDelete = groupId
                },
                modifier = modifier
            )
        }

        // Diálogo de confirmación para eliminar
        groupToDelete?.let { groupId ->
            AlertDialog(
                onDismissRequest = { groupToDelete = null },
                title = { Text("Eliminar Grupo") },
                text = {
                    androidx.compose.foundation.layout.Column {
                        Text("¿Estás seguro de que deseas eliminar este grupo?")
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Esta acción no se puede deshacer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentUser?.uid?.let { userId ->
                                viewModel.deleteGroup(groupId, userId)
                            }
                            groupToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { groupToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyGroupsScreen(modifier: Modifier = Modifier) {
    // TODO: Implementar pantalla vacía mejorada
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎁",
                style = MaterialTheme.typography.displayLarge
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(16.dp)
            )
            Text(
                text = "No tienes grupos aún",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "¡Crea uno usando el botón +!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GroupsList(
    groups: List<AngelitoGroup>,
    onGroupClick: (String) -> Unit,
    onDeleteGroup: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = groups.size,
            key = { index -> groups[index].groupId }
        ) { index ->
            val group = groups[index]
            val isAdmin = group.adminId == currentUserId

            SwipeToDeleteGroupCard(
                group = group,
                isAdmin = isAdmin,
                onClick = { onGroupClick(group.groupId) },
                onDelete = { onDeleteGroup(group.groupId) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteGroupCard(
    group: AngelitoGroup,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    if (isAdmin) {
                        onDelete()
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = isAdmin,
        backgroundContent = {
            val backgroundColor = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> androidx.compose.ui.graphics.Color.Transparent
            }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) {
        GroupCard(group = group, onClick = onClick, isAdmin = isAdmin)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: AngelitoGroup,
    onClick: () -> Unit,
    isAdmin: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (group.status) {
                GroupStatus.ASSIGNED -> MaterialTheme.colorScheme.tertiaryContainer
                GroupStatus.READY -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.groupName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isAdmin) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${group.members.size} miembros",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "•",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = when (group.status) {
                        GroupStatus.PENDING -> "⏳ Esperando"
                        GroupStatus.READY -> "✅ Listo"
                        GroupStatus.ASSIGNED -> "🎁 Sorteado"
                        GroupStatus.REVEALED -> "👀 Revelado"
                        GroupStatus.COMPLETED -> "✨ Completado"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (group.eventDate != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "📅 ${java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(group.eventDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}




