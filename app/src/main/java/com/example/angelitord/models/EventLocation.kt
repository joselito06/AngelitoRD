package com.example.angelitord.models

import com.google.firebase.firestore.GeoPoint

/**
 * Modelo de ubicación del evento
 */
data class EventLocation(
    val address: String = "",              // Dirección legible
    val latitude: Double = 0.0,            // Latitud
    val longitude: Double = 0.0,           // Longitud
    val placeName: String = "",            // Nombre del lugar (ej: "Casa de María")
    val placeId: String = ""               // Google Place ID (opcional)
) {
    /**
     * Convertir a GeoPoint para Firebase
     */
    fun toGeoPoint(): GeoPoint {
        return GeoPoint(latitude, longitude)
    }

    /**
     * Verificar si la ubicación es válida
     */
    fun isValid(): Boolean {
        return latitude != 0.0 && longitude != 0.0
    }

    companion object {
        /**
         * Crear desde GeoPoint de Firebase
         */
        fun fromGeoPoint(geoPoint: GeoPoint, address: String = "", placeName: String = ""): EventLocation {
            return EventLocation(
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                address = address,
                placeName = placeName
            )
        }

        /**
         * Ubicación por defecto (Santo Domingo, RD)
         */
        fun default(): EventLocation {
            return EventLocation(
                latitude = 18.4861,
                longitude = -69.9312,
                address = "Santo Domingo, República Dominicana",
                placeName = "Santo Domingo"
            )
        }
    }
}
