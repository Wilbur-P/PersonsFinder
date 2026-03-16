package com.persons.finder.dto.request

import javax.validation.Valid
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class CreatePersonRequest(
    @field:NotBlank(message = "name is required")
    @field:Size(max = 80, message = "name must be <= 80 characters")
    val name: String,
    @field:NotBlank(message = "jobTitle is required")
    @field:Size(max = 80, message = "jobTitle must be <= 80 characters")
    val jobTitle: String,
    @field:NotEmpty(message = "hobbies must contain at least one value")
    @field:Size(max = 8, message = "hobbies must contain at most 8 values")
    val hobbies: List<@NotBlank(message = "hobbies cannot contain blank values") @Size(max = 40, message = "hobby must be <= 40 characters") String>,
    @field:Valid
    @field:NotNull(message = "location is required")
    val location: LocationRequest
)

data class LocationRequest(
    @field:DecimalMin(value = "-90.0", message = "latitude must be >= -90")
    @field:DecimalMax(value = "90.0", message = "latitude must be <= 90")
    val latitude: Double,
    @field:DecimalMin(value = "-180.0", message = "longitude must be >= -180")
    @field:DecimalMax(value = "180.0", message = "longitude must be <= 180")
    val longitude: Double
)
