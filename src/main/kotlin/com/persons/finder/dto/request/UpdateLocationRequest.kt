package com.persons.finder.dto.request

import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin

data class UpdateLocationRequest(
    @field:DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @field:DecimalMax(value = "90.0", message = "latitude must be <= 90")
    val latitude: Double,
    @field:DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @field:DecimalMax(value = "180.0", message = "longitude must be <= 180")
    val longitude: Double
)
