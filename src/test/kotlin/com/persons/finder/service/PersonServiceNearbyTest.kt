package com.persons.finder.service

import com.persons.finder.entity.PersonEntity
import com.persons.finder.repository.PersonRepository
import com.persons.finder.service.impl.DeterministicMockAiBioService
import com.persons.finder.service.impl.PersonServiceImpl
import com.persons.finder.util.UlidGenerator
import java.security.SecureRandom
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock

class PersonServiceNearbyTest {

    @Test
    fun `findNearby filters by radius and sorts by distance`() {
        val repository = mock(PersonRepository::class.java)

        val closest = PersonEntity(
            id = "01H00000000000000000000001",
            name = "Closest",
            jobTitle = "Engineer",
            hobbiesCsv = "chess",
            bio = "Bio",
            latitude = 0.0,
            longitude = 0.01
        )

        val second = PersonEntity(
            id = "01H00000000000000000000002",
            name = "Second",
            jobTitle = "Engineer",
            hobbiesCsv = "hiking",
            bio = "Bio",
            latitude = 0.0,
            longitude = 0.03
        )

        val outsideRadius = PersonEntity(
            id = "01H00000000000000000000003",
            name = "Far",
            jobTitle = "Engineer",
            hobbiesCsv = "music",
            bio = "Bio",
            latitude = 1.0,
            longitude = 1.0
        )

        given(
            repository.findByLatitudeBetweenAndLongitudeBetween(
                anyDouble(),
                anyDouble(),
                anyDouble(),
                anyDouble()
            )
        ).willReturn(listOf(second, outsideRadius, closest))

        val service = PersonServiceImpl(
            personRepository = repository,
            aiBioService = DeterministicMockAiBioService(),
            promptSafetyService = PromptSafetyService(),
            distanceCalculator = DistanceCalculator(),
            ulidGenerator = UlidGenerator(SecureRandom()),
            maxRadiusKm = 50.0,
            defaultLimit = 50,
            maxLimit = 200
        )

        val results = service.findNearby(
            latitude = 0.0,
            longitude = 0.0,
            radiusKm = 5.0,
            limit = 10
        )

        assertEquals(listOf(closest.id, second.id), results.map { it.id })
    }
}
