package com.persons.finder.dto.response

data class NearbyPersonResponse(
    val id: String,
    val bio: String,
    val distanceKm: Double
)
