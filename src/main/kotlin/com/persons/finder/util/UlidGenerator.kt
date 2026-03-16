package com.persons.finder.util

import java.security.SecureRandom
import org.springframework.stereotype.Component

@Component
class UlidGenerator(
    private val secureRandom: SecureRandom = SecureRandom()
) {

    fun nextUlid(timestampMillis: Long = System.currentTimeMillis()): String {
        require(timestampMillis >= 0) { "timestampMillis must be >= 0" }

        val result = CharArray(26)
        var time = timestampMillis

        for (index in 9 downTo 0) {
            result[index] = ENCODING[(time and 31).toInt()]
            time = time ushr 5
        }

        val randomBytes = ByteArray(10)
        secureRandom.nextBytes(randomBytes)

        var currentValue = 0
        var currentBits = 0
        var outputIndex = 10

        for (byteValue in randomBytes) {
            currentValue = (currentValue shl 8) or (byteValue.toInt() and 0xFF)
            currentBits += 8

            while (currentBits >= 5) {
                currentBits -= 5
                result[outputIndex++] = ENCODING[(currentValue shr currentBits) and 31]
            }
        }

        if (currentBits > 0) {
            result[outputIndex++] = ENCODING[(currentValue shl (5 - currentBits)) and 31]
        }

        while (outputIndex < 26) {
            result[outputIndex++] = ENCODING[secureRandom.nextInt(32)]
        }

        return String(result)
    }

    companion object {
        private val ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray()
        private val ULID_REGEX = Regex("^[0-9A-HJKMNP-TV-Z]{26}$")

        fun isValid(value: String): Boolean = ULID_REGEX.matches(value)
    }
}
