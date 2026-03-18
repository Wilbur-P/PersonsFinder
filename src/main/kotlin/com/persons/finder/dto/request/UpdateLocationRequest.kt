package com.persons.finder.dto.request

import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotNull

data class UpdateLocationRequest(
    @field:NotNull(message = "latitude is required")
    @field:DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @field:DecimalMax(value = "90.0", message = "latitude must be <= 90")
    val latitude: Double?,
    @field:NotNull(message = "longitude is required")
    @field:DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @field:DecimalMax(value = "180.0", message = "longitude must be <= 180")
    val longitude: Double?
)
