package com.example.angelitord.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelitord.models.AngelitoGroup
import com.example.angelitord.viewmodel.GroupUiState
import com.example.angelitord.viewmodel.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    group: AngelitoGroup,
    viewModel: GroupViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf(group.groupName) }
    var budget by remember { mutableStateOf(group.budget?.toInt()?.toString() ?: "") }
    var description by remember { mutableStateOf(group.description) }
    var eventDate by remember { mutableStateOf(group.eventDate) }
    var locationName by remember { mutableStateOf(group.locationName) }
    var showDatePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = eventDate
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GroupUiState.GroupUpdated -> {

                viewModel.resetState()
                onNavigateBack()
            }
            is GroupUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
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
                title = { Text("Editar Grupo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✏️ Modificar Información",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Nombre del grupo
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Nombre del Grupo") },
                leadingIcon = {
                    Icon(Icons.Default.Group, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                enabled = uiState !is GroupUiState.Loading
            )

            // Presupuesto
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it.filter { char -> char.isDigit() } },
                label = { Text("Presupuesto (Opcional)") },
                placeholder = { Text("Ej: 1000") },
                prefix = { Text("RD$ ") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                enabled = uiState !is GroupUiState.Loading
            )

            // Fecha del evento
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = uiState !is GroupUiState.Loading
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = eventDate?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "Seleccionar fecha del evento"
                )
            }

            // Lugar del evento
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Lugar del Evento (Opcional)") },
                placeholder = { Text("Ej: Casa de Juan, Plaza Central") },
                leadingIcon = {
                    Icon(Icons.Default.Place, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                enabled = uiState !is GroupUiState.Loading
            )

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (Opcional)") },
                placeholder = { Text("Detalles adicionales del evento") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 24.dp),
                maxLines = 4,
                enabled = uiState !is GroupUiState.Loading
            )

            // Botón guardar
            Button(
                onClick = {
                    viewModel.updateGroup(
                        groupId = group.groupId,
                        adminId = group.adminId,
                        groupName = groupName,
                        budget = budget.toDoubleOrNull(),
                        eventDate = eventDate,
                        description = description,
                        locationName = locationName,
                        locationLatitude = null, // Por ahora sin coordenadas
                        locationLongitude = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = groupName.isNotBlank() && uiState !is GroupUiState.Loading
            ) {
                if (uiState is GroupUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Puedes modificar la información del grupo en cualquier momento antes del intercambio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            eventDate = datePickerState.selectedDateMillis
                            showDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        Text(
                            text = "Seleccionar fecha del evento",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                )
            }
        }
    }
}