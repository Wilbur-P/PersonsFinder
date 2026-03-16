package com.persons.finder.service

import com.persons.finder.service.impl.DeterministicMockAiBioService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class DeterministicMockAiBioServiceTest {

    private val service = DeterministicMockAiBioService()

    @Test
    fun `returns deterministic bio for identical input`() {
        val first = service.generateBio("Backend Engineer", listOf("Hiking", "Chess"))
        val second = service.generateBio("Backend Engineer", listOf("Hiking", "Chess"))

        assertEquals(first, second)
    }

    @Test
    fun `returns different bio when input changes`() {
        val first = service.generateBio("Backend Engineer", listOf("Hiking", "Chess"))
        val second = service.generateBio("Backend Engineer", listOf("Hiking", "Cooking"))

        assertNotEquals(first, second)
    }
}
