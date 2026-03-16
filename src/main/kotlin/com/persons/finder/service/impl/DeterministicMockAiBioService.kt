package com.persons.finder.service.impl

import com.persons.finder.service.AiBioService
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.springframework.stereotype.Service

@Service
class DeterministicMockAiBioService : AiBioService {

    private val tones = listOf("Curious", "Methodical", "Playful", "Pragmatic", "Inventive")
    private val verbs = listOf("explores", "collects ideas from", "enjoys", "keeps learning through", "recharges with")

    override fun generateBio(jobTitle: String, hobbies: List<String>): String {
        val normalizedJobTitle = jobTitle.trim()
        val normalizedHobbies = hobbies.map { it.trim().lowercase() }.sorted()

        val seed = "$normalizedJobTitle|${normalizedHobbies.joinToString("|")}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(seed.toByteArray(StandardCharsets.UTF_8))

        val tone = tones[(hash[0].toInt() and 0xFF) % tones.size]
        val verb = verbs[(hash[1].toInt() and 0xFF) % verbs.size]
        val hobbyPhrase = when (normalizedHobbies.size) {
            1 -> normalizedHobbies.first()
            else -> normalizedHobbies.take(2).joinToString(" and ")
        }
        val signature = hash.take(3).joinToString("") { "%02x".format(it) }

        return "$tone $normalizedJobTitle who $verb $hobbyPhrase. Signature $signature."
    }
}
