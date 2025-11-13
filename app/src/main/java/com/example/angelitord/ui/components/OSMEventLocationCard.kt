package com.example.angelitord.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.core.content.res.ResourcesCompat
import com.example.angelitord.R
import com.example.angelitord.models.EventLocation
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Card que muestra mapa OSM de la ubicación del evento
 */
@Composable
fun OSMEventLocationCard(
    location: EventLocation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = location.placeName.ifBlank { "Ubicación del Evento" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (location.address.isNotBlank()) {
                            Text(
                                text = location.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${String.format("%.6f", location.latitude)}, " +
                                    "${String.format("%.6f", location.longitude)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Botón para abrir en app de mapas
                IconButton(
                    onClick = {
                        openInMapsApp(context, location)
                    }
                ) {
                    Icon(
                        Icons.Default.Directions,
                        contentDescription = "Abrir en Maps",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Mapa OSM (solo lectura)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(false) // Solo vista, no interactivo

                            val geoPoint = GeoPoint(location.latitude, location.longitude)

                            controller.setZoom(15.0)
                            controller.setCenter(geoPoint)

                            // Agregar marcador
                            val marker = Marker(this).apply {
                                position = geoPoint
                                title = location.placeName
                                snippet = location.address

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
                            overlays.add(marker)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay para hacer el mapa clickeable
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.01f),
                    onClick = {
                        openInMapsApp(context, location)
                    }
                ) {}
            }

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copiar dirección
                OutlinedButton(
                    onClick = {
                        copyToClipboard(context, location)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar")
                }

                // Compartir ubicación
                OutlinedButton(
                    onClick = {
                        shareLocation(context, location)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }
            }

            // Nota de OSM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌍 Mapa de OpenStreetMap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Abrir ubicación en app de mapas del usuario
 */
private fun openInMapsApp(context: Context, location: EventLocation) {
    // Intentar abrir en Google Maps primero
    val gmmIntentUri = Uri.parse(
        "geo:${location.latitude},${location.longitude}" +
                "?q=${location.latitude},${location.longitude}" +
                "(${location.placeName.ifBlank { "Evento" }})"
    )
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
    mapIntent.setPackage("com.google.android.apps.maps")

    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // Si no tiene Google Maps, abrir en navegador
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.openstreetmap.org/?mlat=${location.latitude}&mlon=${location.longitude}#map=15/${location.latitude}/${location.longitude}")
        )
        context.startActivity(browserIntent)
    }
}

/**
 * Copiar dirección al portapapeles
 */
private fun copyToClipboard(context: Context, location: EventLocation) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
            as android.content.ClipboardManager

    val text = if (location.address.isNotBlank()) {
        location.address
    } else {
        "${location.latitude}, ${location.longitude}"
    }

    val clip = android.content.ClipData.newPlainText("Dirección", text)
    clipboard.setPrimaryClip(clip)

    android.widget.Toast.makeText(
        context,
        "Dirección copiada",
        android.widget.Toast.LENGTH_SHORT
    ).show()
}

/**
 * Compartir ubicación
 */
private fun shareLocation(context: Context, location: EventLocation) {
    val shareText = buildString {
        append("📍 ${location.placeName.ifBlank { "Ubicación del evento" }}\n")
        if (location.address.isNotBlank()) {
            append("${location.address}\n")
        }
        append("\nVer en mapa:\n")
        append("https://www.openstreetmap.org/?mlat=${location.latitude}&mlon=${location.longitude}#map=15/${location.latitude}/${location.longitude}")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Ubicación: ${location.placeName}")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir ubicación"))
}