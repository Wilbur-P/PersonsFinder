package com.persons.finder.service

import com.persons.finder.exception.InvalidInputException
import org.springframework.stereotype.Service

@Service
class PromptSafetyService {

    data class SanitizedBioInput(
        val jobTitle: String,
        val hobbies: List<String>
    )

    private val allowedPattern = Regex("^[A-Za-z0-9 .,'&()\\-+/]{1,80}$")

    private val suspiciousPatterns = listOf(
        Regex("(?i)\\b(ignore|bypass|override)\\b.{0,40}\\b(instruction|prompt|rule|system)\\b"),
        Regex("(?i)\\b(system|assistant|developer)\\s*:"),
        Regex("(?i)```"),
        Regex("(?i)<\\s*script"),
        Regex("(?i)\\bact as\\b")
    )

    fun sanitizeForBio(jobTitle: String, hobbies: List<String>): SanitizedBioInput {
        val sanitizedJobTitle = normalizeWhitespace(jobTitle)
        validateField("jobTitle", sanitizedJobTitle, 80)

        if (hobbies.isEmpty() || hobbies.size > 8) {
            throw InvalidInputException("hobbies must contain between 1 and 8 values")
        }

        val sanitizedHobbies = hobbies.mapIndexed { index, hobby ->
            val normalized = normalizeWhitespace(hobby)
            validateField("hobbies[$index]", normalized, 40)
            normalized
        }

        return SanitizedBioInput(sanitizedJobTitle, sanitizedHobbies)
    }

    private fun validateField(fieldName: String, value: String, maxLength: Int) {
        if (value.isBlank()) {
            throw InvalidInputException("$fieldName must not be blank")
        }
        if (value.length > maxLength) {
            throw InvalidInputException("$fieldName must be <= $maxLength characters")
        }
        if (!allowedPattern.matches(value)) {
            throw InvalidInputException("$fieldName contains disallowed characters")
        }
        if (suspiciousPatterns.any { it.containsMatchIn(value) }) {
            throw InvalidInputException("$fieldName contains unsafe prompt-like instructions")
        }
    }

    private fun normalizeWhitespace(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }
}
