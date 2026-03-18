package com.persons.finder.service.impl

import com.persons.finder.service.AiBioService
import com.persons.finder.service.BioGenerationInput
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.springframework.stereotype.Service

@Service
class DeterministicMockAiBioService : AiBioService {

    private val tones = listOf("Curious", "Methodical", "Playful", "Pragmatic", "Inventive")
    private val verbs = listOf("explores", "collects ideas from", "enjoys", "keeps learning through", "recharges with")

    override fun generateBio(input: BioGenerationInput): String {
        val normalizedJobTitle = input.jobTitle.trim()
        val normalizedHobbies = input.hobbies.map { it.trim().lowercase() }.sorted()

        val hash = MessageDigest.getInstance("SHA-256")
            .digest(buildCanonicalSeed(normalizedJobTitle, normalizedHobbies).toByteArray(StandardCharsets.UTF_8))

        val tone = tones[(hash[0].toInt() and 0xFF) % tones.size]
        val verb = verbs[(hash[1].toInt() and 0xFF) % verbs.size]
        val hobbyPhrase = when (normalizedHobbies.size) {
            1 -> normalizedHobbies.first()
            else -> normalizedHobbies.take(2).joinToString(" and ")
        }
        val signature = hash.take(3).joinToString("") { "%02x".format(it) }

        return "$tone $normalizedJobTitle who $verb $hobbyPhrase. Signature $signature."
    }

    private fun buildCanonicalSeed(jobTitle: String, hobbies: List<String>): String {
        val encodedHobbies = hobbies.joinToString(separator = "") { hobby ->
            "hobby:${hobby.length}:$hobby;"
        }
        return "jobTitle:${jobTitle.length}:$jobTitle;hobbyCount:${hobbies.size};$encodedHobbies"
    }
}
