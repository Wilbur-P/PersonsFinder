package com.persons.finder.service

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import org.springframework.stereotype.Component

@Component
class DistanceCalculator {

    companion object {
        private const val EARTH_RADIUS_KM = 6371.0088
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val originLat = Math.toRadians(lat1)
        val targetLat = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) +
            cos(originLat) * cos(targetLat) * sin(dLon / 2).pow(2.0)

        return 2 * EARTH_RADIUS_KM * asin(min(1.0, sqrt(a)))
    }

    fun latitudeDelta(radiusKm: Double): Double {
        return radiusKm / 111.0
    }

    fun longitudeDelta(radiusKm: Double, latitude: Double): Double {
        val latitudeRadians = Math.toRadians(latitude)
        val cosValue = abs(cos(latitudeRadians))
        if (cosValue < 1e-12) {
            return 180.0
        }
        return (radiusKm / (111.320 * cosValue)).coerceAtMost(180.0)
    }
}
