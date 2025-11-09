package com.example.angelitord.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.angelitord.models.AngelitoGroup
import com.example.angelitord.models.GroupStatus
import com.example.angelitord.models.User
import com.example.angelitord.utils.ShareHelper
import com.example.angelitord.viewmodel.GroupUiState
import com.example.angelitord.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    val group by viewModel.currentGroup.collectAsState()
    val members by viewModel.groupMembers.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showRemoveMemberDialog by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        viewModel.loadGroupDetails(groupId)
    }

    // Observar el estado
    LaunchedEffect(uiState) {
        when (uiState) {
            is GroupUiState.GroupDeleted -> {
                snackbarHostState.showSnackbar("Grupo eliminado exitosamente")
                viewModel.resetState()
                onNavigateBack()
            }
            is GroupUiState.MemberRemoved -> {
                snackbarHostState.showSnackbar("Miembro removido exitosamente")
                viewModel.resetState()
                // Si el usuario se salió, regresar
                if (showLeaveDialog) {
                    onNavigateBack()
                }
            }
            is GroupUiState.GroupLockToggled -> {
                val isLocked = (uiState as GroupUiState.GroupLockToggled).isLocked
                snackbarHostState.showSnackbar(
                    if (isLocked) "Grupo bloqueado" else "Grupo desbloqueado"
                )
                viewModel.resetState()
            }
            is GroupUiState.DrawCompleted -> {
                snackbarHostState.showSnackbar("¡Sorteo realizado exitosamente!")
                viewModel.resetState()
            }
            is GroupUiState.Error -> {
                snackbarHostState.showSnackbar(
                    (uiState as GroupUiState.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.groupName ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Menú
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menú")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Opciones del admin
                        if (group?.adminId == currentUserId) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (group?.isLocked == true) "Desbloquear Grupo" else "Bloquear Grupo")
                                },
                                onClick = {
                                    currentUserId?.let { viewModel.toggleGroupLock(groupId, it) }
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        if (group?.isLocked == true) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar Grupo") },
                                onClick = {
                                    showDeleteDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            )
                        } else {
                            // Opción para salir (no admin)
                            DropdownMenuItem(
                                text = { Text("Salir del Grupo") },
                                onClick = {
                                    showLeaveDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (group?.adminId == currentUserId && group?.status == GroupStatus.READY) {
                FloatingActionButton(
                    onClick = { viewModel.performDraw(groupId) }
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Realizar Sorteo")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        group?.let { currentGroup ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Información del grupo
                item {
                    GroupInfoCard(currentGroup)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Estado del grupo con indicador de bloqueo
                item {
                    GroupStatusCard(currentGroup, members.size)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Botones de acción para el admin
                if (currentGroup.adminId == currentUserId) {
                    item {
                        AdminActionsCard(
                            onInviteClick = { showShareDialog = true },
                            canDraw = currentGroup.status == GroupStatus.READY,
                            isLocked = currentGroup.isLocked
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Mi asignación (si ya se hizo el sorteo)
                if (currentGroup.status == GroupStatus.ASSIGNED && currentUserId != null) {
                    item {
                        MyAssignmentCard(
                            onClick = { viewModel.getMyAssignment(groupId, currentUserId) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Lista de miembros
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Miembros (${members.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentGroup.isLocked) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bloqueado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(members) { member ->
                    MemberCard(
                        member = member,
                        isAdmin = member.userId == currentGroup.adminId,
                        currentUserIsAdmin = currentGroup.adminId == currentUserId,
                        canRemove = currentGroup.status == GroupStatus.PENDING || currentGroup.status == GroupStatus.READY,
                        onRemove = { showRemoveMemberDialog = member.userId }
                    )
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Diálogo para compartir invitación
    if (showShareDialog && group != null) {
        ShareInvitationDialog(
            group = group!!,
            onDismiss = { showShareDialog = false },
            onShareOption = { option ->
                val adminName = members.find { it.userId == group!!.adminId }?.name ?: "Admin"
                when (option) {
                    ShareOption.WHATSAPP -> ShareHelper.shareViaWhatsApp(
                        context, null, group!!.groupName, adminName, groupId
                    )
                    ShareOption.EMAIL -> ShareHelper.shareViaEmail(
                        context, null, group!!.groupName, adminName, groupId
                    )
                    ShareOption.SMS -> ShareHelper.shareViaSMS(
                        context, null, group!!.groupName, adminName, groupId
                    )
                    ShareOption.TELEGRAM -> ShareHelper.shareViaTelegram(
                        context, group!!.groupName, adminName, groupId
                    )
                    ShareOption.OTHER -> ShareHelper.shareInvitation(
                        context, group!!.groupName, adminName, groupId
                    )
                    ShareOption.COPY -> ShareHelper.copyToClipboard(context, groupId)
                }
                showShareDialog = false
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        DeleteGroupDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                currentUserId?.let { userId ->
                    viewModel.deleteGroup(groupId, userId)
                }
                showDeleteDialog = false
            }
        )
    }

    // Diálogo para salir del grupo
    if (showLeaveDialog && currentUserId != null) {
        LeaveGroupDialog(
            onDismiss = { showLeaveDialog = false },
            onConfirm = {
                viewModel.removeMember(groupId, currentUserId, currentUserId)
                showLeaveDialog = false
            }
        )
    }

    // Diálogo para remover miembro
    showRemoveMemberDialog?.let { memberId ->
        val member = members.find { it.userId == memberId }
        if (member != null && currentUserId != null) {
            RemoveMemberDialog(
                memberName = member.name,
                onDismiss = { showRemoveMemberDialog = null },
                onConfirm = {
                    viewModel.removeMember(groupId, memberId, currentUserId)
                    showRemoveMemberDialog = null
                }
            )
        }
    }
}

@Composable
fun GroupInfoCard(group: AngelitoGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = group.groupName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (group.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            group.budget?.let { budget ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Presupuesto: RD$ ${budget.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            group.eventDate?.let { date ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(date)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun GroupStatusCard(group: AngelitoGroup, memberCount: Int) {
    val statusInfo = when (group.status) {
        GroupStatus.PENDING -> "⏳ Esperando más miembros" to
                "Se necesitan al menos 3 personas. Actualmente: $memberCount"
        GroupStatus.READY -> "✅ Listo para sortear" to
                "Ya hay suficientes personas. El administrador puede realizar el sorteo."
        GroupStatus.ASSIGNED -> "🎁 Angelitos asignados" to
                "El sorteo ya se realizó. ¡Revisa tu angelito!"
        GroupStatus.REVEALED -> "👀 Angelitos revelados" to
                "Los angelitos se han revelado."
        GroupStatus.COMPLETED -> "✨ Evento completado" to
                "El intercambio ha finalizado."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (group.status) {
                GroupStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                GroupStatus.READY -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = statusInfo.first,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = statusInfo.second,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AdminActionsCard(
    onInviteClick: () -> Unit,
    canDraw: Boolean,
    isLocked: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Acciones de Administrador",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onInviteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invitar Amigos")
            }

            if (isLocked) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "El grupo está bloqueado. Nadie más puede unirse.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyAssignmentCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎁 Tu Angelito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClick) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver mi angelito")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCard(
    member: User,
    isAdmin: Boolean,
    currentUserIsAdmin: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (isAdmin) {
                    Text(
                        text = "Administrador",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Botón para remover (solo admin puede ver y solo si no es el admin mismo)
            if (currentUserIsAdmin && !isAdmin && canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.RemoveCircle,
                        contentDescription = "Expulsar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

enum class ShareOption {
    WHATSAPP, EMAIL, SMS, TELEGRAM, OTHER, COPY
}

@Composable
fun ShareInvitationDialog(
    group: AngelitoGroup,
    onDismiss: () -> Unit,
    onShareOption: (ShareOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compartir Invitación") },
        text = {
            Column {
                ShareOptionButton(
                    icon = Icons.Default.Chat,
                    text = "WhatsApp",
                    onClick = { onShareOption(ShareOption.WHATSAPP) }
                )
                ShareOptionButton(
                    icon = Icons.Default.Email,
                    text = "Correo Electrónico",
                    onClick = { onShareOption(ShareOption.EMAIL) }
                )
                ShareOptionButton(
                    icon = Icons.Default.Message,
                    text = "SMS",
                    onClick = { onShareOption(ShareOption.SMS) }
                )
                ShareOptionButton(
                    icon = Icons.Default.Send,
                    text = "Telegram",
                    onClick = { onShareOption(ShareOption.TELEGRAM) }
                )
                ShareOptionButton(
                    icon = Icons.Default.ContentCopy,
                    text = "Copiar Código",
                    onClick = { onShareOption(ShareOption.COPY) }
                )
                ShareOptionButton(
                    icon = Icons.Default.Share,
                    text = "Otras Apps",
                    onClick = { onShareOption(ShareOption.OTHER) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ShareOptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DeleteGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Grupo") },
        text = {
            Column {
                Text("¿Estás seguro de que deseas eliminar este grupo?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Esta acción no se puede deshacer y se eliminará para todos los miembros.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
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
fun LeaveGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salir del Grupo") },
        text = {
            Column {
                Text("¿Estás seguro de que deseas salir de este grupo?")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No podrás volver a unirte a menos que el administrador te invite de nuevo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Salir")
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
fun RemoveMemberDialog(
    memberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Expulsar Miembro") },
        text = {
            Text("¿Estás seguro de que deseas expulsar a $memberName del grupo?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Expulsar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

