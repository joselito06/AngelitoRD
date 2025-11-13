package com.example.angelitord.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.angelitord.models.EventLocation
import com.example.angelitord.ui.components.AppTopBar
import com.example.angelitord.ui.components.LocationDisplayCard
import com.example.angelitord.ui.components.OSMLocationPickerDialog
import com.example.angelitord.viewmodel.GroupUiState
import com.example.angelitord.viewmodel.GroupViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    viewModel: GroupViewModel,
    onGroupCreated: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var groupName by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // ✅ Estado para ubicación con OSM
    var eventLocation by remember { mutableStateOf<EventLocation?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val datePickerState = rememberDatePickerState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val eventLocationFlow = viewModel.locationModel.collectAsState(initial = null)


    // Observar el estado y navegar cuando se cree el grupo
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is GroupUiState.GroupCreated -> {
                viewModel.resetState()
                onGroupCreated(state.groupId) // Navegar automáticamente
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
            AppTopBar(
                title = "Crear Grupo de Angelito",
                onNavigationClick = onNavigateBack
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
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Nombre del grupo
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Nombre del Grupo") },
                placeholder = { Text("Ej: Angelito Navidad 2025") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                enabled = uiState !is GroupUiState.Loading
            )

            // Presupuesto (opcional)
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it.filter { char -> char.isDigit() } },
                label = { Text("Presupuesto (Opcional)") },
                placeholder = { Text("Ej: 1000") },
                prefix = { Text("RD$ ") },
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

            // Descripción
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (Opcional)") },
                placeholder = { Text("Ej: Intercambio de regalos navideños") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 24.dp),
                maxLines = 4,
                enabled = uiState !is GroupUiState.Loading
            )

            // ✅ UBICACIÓN CON OSM
            Column {
                Text(
                    text = "Ubicación del evento",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LocationDisplayCard(
                    location = eventLocation,
                    onEditClick = { showLocationPicker = true }
                )

                if (eventLocation != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { /*eventLocation = null*/ }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quitar ubicación")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón crear
            Button(
                onClick = {
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserId != null && groupName.isNotBlank()) {
                        viewModel.createGroup(
                            groupName = groupName,
                            adminId = currentUserId,
                            budget = budget.toDoubleOrNull(),
                            eventDate = eventDate,
                            description = description,
                            locationName = eventLocation!!.placeId,
                            locationLatitude = eventLocation!!.latitude,
                            locationLongitude = eventLocation!!.longitude,
                            locationAddress = eventLocation!!.address,
                            locationPlaceName = eventLocation!!.placeName
                        )
                    }
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
                    Text("Creando...")
                } else {
                    Text(
                        text = "Crear Grupo",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Información",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Serás el administrador del grupo\n" +
                                "• Podrás invitar amigos al grupo\n" +
                                "• Se necesitan mínimo 3 personas\n" +
                                "• El sorteo se realizará cuando estés listo",
                        style = MaterialTheme.typography.bodyMedium
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

        // ✅ Diálogo de ubicación con OSM
        if (showLocationPicker) {
            OSMLocationPickerDialog(
                initialLocation = eventLocation,
                onDismiss = { showLocationPicker = false },
                onLocationSelected = { location ->
                    eventLocation = location
                    showLocationPicker = false
                }
            )
        }
    }
}