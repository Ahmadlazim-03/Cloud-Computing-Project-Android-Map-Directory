package com.mapdir.app.util

import android.location.Location

/**
 * Utility for calculating distance between two geographic coordinates.
 * Uses Haversine formula (same as Android Location.distanceBetween).
 */
object DistanceCalculator {
    private const val EARTH_RADIUS_M = 6371000.0

    /**
     * Calculate distance between two points using Haversine formula.
     * @param lat1 Latitude of first point (degrees)
     * @param lon1 Longitude of first point (degrees)
     * @param lat2 Latitude of second point (degrees)
     * @param lon2 Longitude of second point (degrees)
     * @return Distance in meters
     */
    fun haversineDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return EARTH_RADIUS_M * c
    }

    /**
     * Calculate distance from Location to a latitude/longitude point.
     * @param from Source Location object
     * @param destLat Destination latitude
     * @param destLon Destination longitude
     * @return Distance in meters
     */
    fun distanceTo(from: Location, destLat: Double, destLon: Double): Double {
        return haversineDistance(from.latitude, from.longitude, destLat, destLon)
    }

    /**
     * Format distance to human-readable string.
     * Formats as km if >= 1000m, otherwise as meters.
     */
    fun formatDistance(distanceM: Double?): String {
        return when {
            distanceM == null -> "N/A"
            distanceM >= 1000 -> String.format("%.1f km", distanceM / 1000)
            else -> String.format("%.0f m", distanceM)
        }
    }
}
