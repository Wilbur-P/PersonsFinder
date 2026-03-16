package com.persons.finder.service

import com.persons.finder.exception.RateLimitExceededException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RateLimiterService(
    @Value("\${app.rate-limit.max-requests:120}")
    private val maxRequests: Int,
    @Value("\${app.rate-limit.window-seconds:60}")
    private val windowSeconds: Long
) {

    private data class Counter(
        val windowStartEpochSecond: Long,
        val requests: AtomicInteger
    )

    private val counters = ConcurrentHashMap<String, Counter>()

    fun checkLimit(endpoint: String, clientId: String) {
        val now = Instant.now().epochSecond
        val key = "$endpoint|$clientId"

        counters.compute(key) { _, existing ->
            if (existing == null || now - existing.windowStartEpochSecond >= windowSeconds) {
                Counter(now, AtomicInteger(1))
            } else {
                val current = existing.requests.incrementAndGet()
                if (current > maxRequests) {
                    throw RateLimitExceededException()
                }
                existing
            }
        }

        if (counters.size > 5000) {
            counters.entries.removeIf { now - it.value.windowStartEpochSecond > windowSeconds * 2 }
        }
    }

    fun clearAll() {
        counters.clear()
    }
}
