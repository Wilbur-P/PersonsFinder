package com.persons.finder.dto.response

data class NearbyPersonResponse(
    val id: String,
    val name: String,
    val jobTitle: String,
    val bio: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double
)
