package com.persons.finder.controller

import com.persons.finder.dto.request.CreatePersonRequest
import com.persons.finder.dto.request.UpdateLocationRequest
import com.persons.finder.dto.response.LocationUpdatedResponse
import com.persons.finder.dto.response.NearbyPersonResponse
import com.persons.finder.dto.response.PersonCreatedResponse
import com.persons.finder.service.PersonService
import javax.validation.Valid
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/persons")
@Validated
class PersonController(
    private val personService: PersonService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createPerson(@Valid @RequestBody request: CreatePersonRequest): PersonCreatedResponse {
        return personService.createPerson(request)
    }

    @PutMapping("/{id}/location")
    fun updateLocation(
        @PathVariable
        @Pattern(regexp = "^[0-9A-HJKMNP-TV-Z]{26}$", message = "id must be a valid ULID")
        id: String,
        @Valid @RequestBody request: UpdateLocationRequest
    ): LocationUpdatedResponse {
        return personService.updateLocation(id, request)
    }

    @GetMapping("/nearby")
    fun getNearby(
        @RequestParam
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90")
        latitude: Double,
        @RequestParam
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180")
        longitude: Double,
        @RequestParam
        @DecimalMin(value = "0.000001", inclusive = true, message = "radiusKm must be > 0")
        radiusKm: Double,
        @RequestParam(required = false)
        limit: Int?
    ): List<NearbyPersonResponse> {
        return personService.findNearby(latitude, longitude, radiusKm, limit)
    }
}
