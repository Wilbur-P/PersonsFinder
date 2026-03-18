package com.persons.finder.benchmark

import com.persons.finder.entity.PersonEntity
import com.persons.finder.repository.PersonRepository
import com.persons.finder.service.PersonService
import javax.persistence.EntityManager
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:file:./build/benchmark-db/personsfinder;AUTO_SERVER=FALSE;DB_CLOSE_ON_EXIT=FALSE"
    ]
)
@Tag("benchmark")
class NearbyBenchmarkTest {

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var personService: PersonService

    @Autowired
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun setup() {
        personRepository.deleteAll()
    }

    @Test
    fun benchmarkNearbySearchWithOneMillionRecords() {
        val totalRecords = 1_000_000
        val batchSize = 10_000
        val random = Random(42)
        val batch = ArrayList<PersonEntity>(batchSize)

        val seedMs = measureTimeMillis {
            for (i in 1..totalRecords) {
                batch.add(
                    PersonEntity(
                        id = i.toString().padStart(26, '0'),
                        name = "Person $i",
                        jobTitle = "Engineer",
                        hobbiesCsv = "running,reading",
                        bio = "Benchmark bio",
                        latitude = -90.0 + random.nextDouble() * 180.0,
                        longitude = -180.0 + random.nextDouble() * 360.0
                    )
                )

                if (batch.size == batchSize) {
                    personRepository.saveAll(batch)
                    personRepository.flush()
                    entityManager.clear()
                    batch.clear()
                }
            }

            if (batch.isNotEmpty()) {
                personRepository.saveAll(batch)
                personRepository.flush()
                entityManager.clear()
                batch.clear()
            }
        }

        val timings = mutableListOf<Long>()
        repeat(5) {
            val elapsed = measureTimeMillis {
                personService.findNearby(
                    latitude = 0.0,
                    longitude = 0.0,
                    radiusKm = 10.0,
                    limit = 50
                )
            }
            timings += elapsed
        }

        val sorted = timings.sorted()
        val p50 = sorted[sorted.size / 2]
        val p95 = sorted[((sorted.size - 1) * 95) / 100]

        println("Seeded records: $totalRecords in ${seedMs}ms")
        println("Nearby timings(ms): $timings")
        println("p50=${p50}ms p95=${p95}ms")
    }
}
