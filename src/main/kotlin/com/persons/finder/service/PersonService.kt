package com.persons.finder.service

import com.persons.finder.dto.request.CreatePersonRequest
import com.persons.finder.dto.request.UpdateLocationRequest
import com.persons.finder.dto.response.LocationUpdatedResponse
import com.persons.finder.dto.response.NearbyPersonResponse
import com.persons.finder.dto.response.PersonCreatedResponse

interface PersonService {
    fun createPerson(request: CreatePersonRequest): PersonCreatedResponse
    fun updateLocation(id: String, request: UpdateLocationRequest): LocationUpdatedResponse
    fun findNearby(latitude: Double, longitude: Double, radiusKm: Double, limit: Int?): List<NearbyPersonResponse>
}
