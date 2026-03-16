package com.persons.finder.dto.response

import java.time.LocalDateTime

data class LocationUpdatedResponse(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val updatedAt: LocalDateTime
)
