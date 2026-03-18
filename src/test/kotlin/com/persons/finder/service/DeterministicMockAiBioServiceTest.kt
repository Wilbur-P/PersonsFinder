package com.persons.finder.service

import com.persons.finder.service.impl.DeterministicMockAiBioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class DeterministicMockAiBioServiceTest {

    private val service = DeterministicMockAiBioService()

    @Test
    fun `returns deterministic bio for identical input`() {
        val input = BioGenerationInput(
            jobTitle = "Backend Engineer",
            hobbies = listOf("Hiking", "Chess")
        )

        val first = service.generateBio(input)
        val second = service.generateBio(input)

        assertEquals(first, second)
    }

    @Test
    fun `returns different bio when input changes`() {
        val first = service.generateBio(
            BioGenerationInput(
                jobTitle = "Backend Engineer",
                hobbies = listOf("Hiking", "Chess")
            )
        )
        val second = service.generateBio(
            BioGenerationInput(
                jobTitle = "Backend Engineer",
                hobbies = listOf("Hiking", "Cooking")
            )
        )

        assertNotEquals(first, second)
    }

    @Test
    fun `returns same bio regardless of hobby order`() {
        val first = service.generateBio(
            BioGenerationInput(
                jobTitle = "Backend Engineer",
                hobbies = listOf("Hiking", "Chess")
            )
        )
        val second = service.generateBio(
            BioGenerationInput(
                jobTitle = "Backend Engineer",
                hobbies = listOf("Chess", "Hiking")
            )
        )

        assertEquals(first, second)
    }

    @Test
    fun `structured field framing avoids delimiter ambiguity`() {
        val first = service.generateBio(
            BioGenerationInput(
                jobTitle = "Engineer|Chess",
                hobbies = listOf("Hiking")
            )
        )
        val second = service.generateBio(
            BioGenerationInput(
                jobTitle = "Engineer",
                hobbies = listOf("Chess|Hiking")
            )
        )

        assertNotEquals(first, second)
    }
}
