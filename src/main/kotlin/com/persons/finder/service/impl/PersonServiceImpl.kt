package com.persons.finder.service.impl

import com.persons.finder.dto.request.CreatePersonRequest
import com.persons.finder.dto.request.UpdateLocationRequest
import com.persons.finder.dto.response.LocationUpdatedResponse
import com.persons.finder.dto.response.NearbyPersonResponse
import com.persons.finder.dto.response.PersonCreatedResponse
import com.persons.finder.entity.PersonEntity
import com.persons.finder.exception.InvalidInputException
import com.persons.finder.exception.PersonNotFoundException
import com.persons.finder.repository.PersonRepository
import com.persons.finder.service.AiBioService
import com.persons.finder.service.DistanceCalculator
import com.persons.finder.service.PersonService
import com.persons.finder.service.PromptSafetyService
import com.persons.finder.util.UlidGenerator
import java.text.Normalizer
import java.util.LinkedHashMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PersonServiceImpl(
    private val personRepository: PersonRepository,
    private val aiBioService: AiBioService,
    private val promptSafetyService: PromptSafetyService,
    private val distanceCalculator: DistanceCalculator,
    private val ulidGenerator: UlidGenerator,
    @Value("\${app.nearby.max-radius-km:50}")
    private val maxRadiusKm: Double,
    @Value("\${app.nearby.default-limit:50}")
    private val defaultLimit: Int,
    @Value("\${app.nearby.max-limit:200}")
    private val maxLimit: Int
) : PersonService {

    private val allowedNamePattern = Regex("^[\\p{L}][\\p{L} .'-]{0,79}$")
    private val controlOrInvisibleChars = Regex("[\\p{Cc}\\p{Cf}]")

    @Transactional
    override fun createPerson(request: CreatePersonRequest): PersonCreatedResponse {
        val sanitizedBioInput = promptSafetyService.sanitizeForBio(request.jobTitle, request.hobbies)
        val bio = aiBioService.generateBio(sanitizedBioInput)

        val person = PersonEntity(
            id = ulidGenerator.nextUlid(),
            name = sanitizeName(request.name),
            jobTitle = sanitizedBioInput.jobTitle,
            hobbiesCsv = sanitizedBioInput.hobbies.joinToString(","),
            bio = bio,
            latitude = request.location.latitude,
            longitude = request.location.longitude
        )

        val created = personRepository.save(person)
        return PersonCreatedResponse(id = created.id, bio = created.bio)
    }

    @Transactional
    override fun updateLocation(id: String, request: UpdateLocationRequest): LocationUpdatedResponse {
        validateUlid(id)

        val person = personRepository.findById(id)
            .orElseThrow { PersonNotFoundException(id) }

        person.latitude = request.latitude
        person.longitude = request.longitude

        val updated = personRepository.save(person)
        return LocationUpdatedResponse(
            id = updated.id,
            latitude = updated.latitude,
            longitude = updated.longitude,
            updatedAt = updated.updatedAt
        )
    }

    @Transactional(readOnly = true)
    override fun findNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double,
        limit: Int?
    ): List<NearbyPersonResponse> {
        validateCoordinates(latitude, longitude)
        validateRadius(radiusKm)

        val requestedLimit = limit ?: defaultLimit
        if (requestedLimit < 1 || requestedLimit > maxLimit) {
            throw InvalidInputException("limit must be between 1 and $maxLimit")
        }

        val latitudeDelta = distanceCalculator.latitudeDelta(radiusKm)
        val longitudeDelta = distanceCalculator.longitudeDelta(radiusKm, latitude)

        val latitudeMin = (latitude - latitudeDelta).coerceAtLeast(-90.0)
        val latitudeMax = (latitude + latitudeDelta).coerceAtMost(90.0)
        val longitudeMin = longitude - longitudeDelta
        val longitudeMax = longitude + longitudeDelta

        val candidates = fetchCandidates(latitudeMin, latitudeMax, longitudeMin, longitudeMax)

        return candidates
            .map { entity ->
                val distance = distanceCalculator.haversineKm(
                    latitude,
                    longitude,
                    entity.latitude,
                    entity.longitude
                )
                entity to distance
            }
            .filter { (_, distance) -> distance <= radiusKm }
            .sortedBy { (_, distance) -> distance }
            .take(requestedLimit)
            .map { (entity, distance) ->
                NearbyPersonResponse(
                    id = entity.id,
                    name = entity.name,
                    jobTitle = entity.jobTitle,
                    bio = entity.bio,
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    distanceKm = distance
                )
            }
    }

    private fun fetchCandidates(
        latitudeMin: Double,
        latitudeMax: Double,
        longitudeMin: Double,
        longitudeMax: Double
    ): List<PersonEntity> {
        if (longitudeMin >= -180.0 && longitudeMax <= 180.0) {
            return personRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitudeMin,
                latitudeMax,
                longitudeMin,
                longitudeMax
            )
        }

        val deduplicated = LinkedHashMap<String, PersonEntity>()

        if (longitudeMin < -180.0) {
            personRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitudeMin,
                latitudeMax,
                longitudeMin + 360.0,
                180.0
            ).forEach { deduplicated[it.id] = it }

            personRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitudeMin,
                latitudeMax,
                -180.0,
                longitudeMax
            ).forEach { deduplicated[it.id] = it }
        } else {
            personRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitudeMin,
                latitudeMax,
                longitudeMin,
                180.0
            ).forEach { deduplicated[it.id] = it }

            personRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitudeMin,
                latitudeMax,
                -180.0,
                longitudeMax - 360.0
            ).forEach { deduplicated[it.id] = it }
        }

        return deduplicated.values.toList()
    }

    private fun validateRadius(radiusKm: Double) {
        if (radiusKm <= 0) {
            throw InvalidInputException("radiusKm must be > 0")
        }
        if (radiusKm > maxRadiusKm) {
            throw InvalidInputException("radiusKm must be <= $maxRadiusKm")
        }
    }

    private fun validateCoordinates(latitude: Double, longitude: Double) {
        if (latitude !in -90.0..90.0) {
            throw InvalidInputException("latitude must be between -90 and 90")
        }
        if (longitude !in -180.0..180.0) {
            throw InvalidInputException("longitude must be between -180 and 180")
        }
    }

    private fun validateUlid(id: String) {
        if (!UlidGenerator.isValid(id)) {
            throw InvalidInputException("id must be a valid ULID")
        }
    }

    private fun sanitizeName(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFKC)
            .replace(controlOrInvisibleChars, "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.isBlank()) {
            throw InvalidInputException("name must not be blank")
        }
        if (normalized.length > 80) {
            throw InvalidInputException("name must be <= 80 characters")
        }
        if (!allowedNamePattern.matches(normalized)) {
            throw InvalidInputException("name contains disallowed characters")
        }

        return normalized
    }
}
