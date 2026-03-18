package com.persons.finder.service

import com.persons.finder.exception.InvalidInputException
import java.text.Normalizer
import org.springframework.stereotype.Service

@Service
class PromptSafetyService {

    private val allowedPattern = Regex("^[A-Za-z0-9 .,'&()\\-+/]{1,80}$")
    private val controlOrInvisibleChars = Regex("[\\p{Cc}\\p{Cf}]")

    private val suspiciousPatterns = listOf(
        Regex("(?i)\\b(ignore|bypass|override|forget)\\b.{0,50}\\b(instruction|prompt|rule|system|guardrail)\\b"),
        Regex("(?i)\\b(system|assistant|developer|user)\\s*:"),
        Regex("(?i)```"),
        Regex("(?i)<\\s*script"),
        Regex("(?i)\\b(act|pretend) as\\b")
    )

    fun sanitizeForBio(jobTitle: String, hobbies: List<String>): BioGenerationInput {
        val sanitizedJobTitle = normalizeInput(jobTitle)
        validateField("jobTitle", sanitizedJobTitle, 80)

        if (hobbies.isEmpty() || hobbies.size > 8) {
            throw InvalidInputException("hobbies must contain between 1 and 8 values")
        }

        val sanitizedHobbies = hobbies.mapIndexed { index, hobby ->
            val normalized = normalizeInput(hobby)
            validateField("hobbies[$index]", normalized, 40)
            normalized
        }

        return BioGenerationInput(
            jobTitle = sanitizedJobTitle,
            hobbies = sanitizedHobbies
        )
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
        if (suspiciousPatterns.any { it.containsMatchIn(value) } || containsObfuscatedPromptInjection(value)) {
            throw InvalidInputException("$fieldName contains unsafe prompt-like instructions")
        }
    }

    private fun containsObfuscatedPromptInjection(value: String): Boolean {
        val canonical = value.lowercase().replace(Regex("[^a-z0-9]"), "")
        return canonical.contains("ignoreinstructions") ||
            canonical.contains("ignoreallinstructions") ||
            canonical.contains("systemprompt") ||
            canonical.contains("assistantrole") ||
            canonical.contains("developerprompt") ||
            canonical.contains("bypassguardrails") ||
            canonical.contains("forgetsafetyrules")
    }

    private fun normalizeInput(value: String): String {
        val unicodeNormalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
        return unicodeNormalized
            .replace(controlOrInvisibleChars, "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
