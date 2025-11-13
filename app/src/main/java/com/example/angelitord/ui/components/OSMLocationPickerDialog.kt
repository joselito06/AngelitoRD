package com.example.angelitord.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.angelitord.R
import com.example.angelitord.models.EventLocation
import com.google.android.gms.location.LocationServices
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Diálogo para seleccionar ubicación con OpenStreetMap
 * 100% GRATIS - Sin API Key necesaria
 */
@Composable
fun OSMLocationPickerDialog(
    initialLocation: EventLocation? = null,
    onDismiss: () -> Unit,
    onLocationSelected: (EventLocation) -> Unit
) {
    val context = LocalContext.current

    // Estado
    var selectedPosition by remember {
        mutableStateOf(
            GeoPoint(
                initialLocation?.latitude ?: 18.4861,  // Santo Domingo
                initialLocation?.longitude ?: -69.9312
            )
        )
    }

    var address by remember { mutableStateOf(initialLocation?.address ?: "") }
    var placeName by remember { mutableStateOf(initialLocation?.placeName ?: "") }
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Verificar permisos
    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Launcher para pedir permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }

                    Text(
                        text = "Seleccionar Ubicación",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = {
                            val location = EventLocation(
                                latitude = selectedPosition.latitude,
                                longitude = selectedPosition.longitude,
                                address = address.ifBlank {
                                    "${selectedPosition.latitude}, ${selectedPosition.longitude}"
                                },
                                placeName = placeName
                            )
                            onLocationSelected(location)
                            onDismiss()
                        }
                    ) {
                        Text("Guardar")
                    }
                }

                // Campos de dirección
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Nombre del lugar") },
                        placeholder = { Text("Ej: Casa de María") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        placeholder = { Text("Ej: Calle Principal #123") },
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // Coordenadas actuales
                    Text(
                        text = "Coordenadas: ${String.format("%.6f", selectedPosition.latitude)}, " +
                                "${String.format("%.6f", selectedPosition.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instrucciones
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mantén presionado en el mapa para seleccionar la ubicación",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mapa OSM
                var mapView: MapView? by remember { mutableStateOf(null) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)

                                // Configurar zoom
                                controller.setZoom(15.0)
                                controller.setCenter(selectedPosition)

                                // Mantener referencia al MapView para usarla en el callback
                                val currentMapView = this

                                // Crear marcador inicial
                                val initialMarker = Marker(this).apply {
                                    position = selectedPosition
                                    title = placeName.ifBlank { "Ubicación del evento" }
                                    snippet = address.ifBlank { null }

                                    // Ícono del marcador
                                    try {
                                        icon = ResourcesCompat.getDrawable(
                                            resources,
                                            R.drawable.ic_location_pin,
                                            null
                                        )
                                    } catch (e: Exception) {
                                        // Usar ícono por defecto
                                    }
                                }

                                // ✅ CORREGIDO: Usar MapEventsOverlay para detectar toques
                                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                        // Tap simple - no hacer nada
                                        return false
                                    }

                                    override fun longPressHelper(p: GeoPoint?): Boolean {
                                        // Toque largo - actualizar posición
                                        p?.let { geoPoint ->
                                            selectedPosition = geoPoint

                                            // Limpiar todos los overlays excepto el MapEventsOverlay
                                            val overlaysList = currentMapView.overlays.toList()
                                            currentMapView.overlays.clear()

                                            // Re-agregar solo el MapEventsOverlay
                                            overlaysList.filterIsInstance<MapEventsOverlay>()
                                                .firstOrNull()?.let {
                                                    currentMapView.overlays.add(it)
                                                }

                                            // Crear nuevo marcador
                                            val newMarker = Marker(currentMapView).apply {
                                                position = geoPoint
                                                title = placeName.ifBlank { "Ubicación seleccionada" }
                                                snippet = address.ifBlank { null }

                                                try {
                                                    icon = ResourcesCompat.getDrawable(
                                                        resources,
                                                        R.drawable.ic_location_pin,
                                                        null
                                                    )
                                                } catch (e: Exception) {
                                                    // Usar ícono por defecto
                                                }
                                            }
                                            currentMapView.overlays.add(newMarker)
                                            currentMapView.invalidate()
                                        }
                                        return true
                                    }
                                })

                                // Agregar overlays en orden: MapEventsOverlay primero, luego el marcador
                                overlays.add(mapEventsOverlay)
                                overlays.add(initialMarker)

                                mapView = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Marcador central visual (cruz)
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de ubicación actual
                    OutlinedButton(
                        onClick = {
                            if (hasLocationPermission) {
                                val fusedLocationClient = LocationServices
                                    .getFusedLocationProviderClient(context)

                                try {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            selectedPosition = GeoPoint(it.latitude, it.longitude)
                                            mapView?.controller?.apply {
                                                setCenter(selectedPosition)
                                                setZoom(17.0)
                                            }

                                            // Actualizar marcador
                                            mapView?.overlays?.let { overlays ->
                                                // Mantener el MapEventsOverlay
                                                val mapEvents = overlays.firstOrNull { it is MapEventsOverlay }
                                                overlays.clear()
                                                mapEvents?.let { overlays.add(it) }

                                                val marker = Marker(mapView).apply {
                                                    position = selectedPosition
                                                    title = placeName.ifBlank { "Mi ubicación" }
                                                    try {
                                                        icon = ResourcesCompat.getDrawable(
                                                            context.resources,
                                                            R.drawable.ic_location_pin,
                                                            null
                                                        )
                                                    } catch (e: Exception) {
                                                        // Usar ícono por defecto
                                                    }
                                                }
                                                overlays.add(marker)
                                            }
                                            mapView?.invalidate()
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    // Sin permisos
                                }
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mi Ubicación")
                    }

                    // Botón de centrar mapa
                    OutlinedButton(
                        onClick = {
                            mapView?.controller?.apply {
                                setCenter(selectedPosition)
                                setZoom(15.0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Centrar")
                    }
                }

                // Nota sobre privacidad
                Text(
                    text = "🌍 Usando OpenStreetMap - Datos abiertos y gratuitos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}