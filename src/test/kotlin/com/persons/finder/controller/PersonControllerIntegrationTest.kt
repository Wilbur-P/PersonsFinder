package com.persons.finder.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.persons.finder.entity.PersonEntity
import com.persons.finder.repository.PersonRepository
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop"
    ]
)
class PersonControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var personRepository: PersonRepository

    @BeforeEach
    fun setUp() {
        personRepository.deleteAll()
    }

    @Test
    fun `POST persons creates person and returns generated bio`() {
        val payload = mapOf(
            "name" to "Alice",
            "jobTitle" to "Backend Engineer",
            "hobbies" to listOf("Chess", "Hiking"),
            "location" to mapOf("latitude" to 37.7749, "longitude" to -122.4194)
        )

        mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$"))
                jsonPath("$.bio") { isString() }
            }
    }

    @Test
    fun `POST persons returns 400 on validation failure`() {
        val payload = mapOf(
            "name" to " ",
            "jobTitle" to "Backend Engineer",
            "hobbies" to listOf("Chess"),
            "location" to mapOf("latitude" to 37.7749, "longitude" to -122.4194)
        )

        mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Validation failed") }
                jsonPath("$.fieldErrors.name") { exists() }
            }
    }

    @Test
    fun `POST persons rejects unsafe name values`() {
        val payload = mapOf(
            "name" to "<script>alert(1)</script>",
            "jobTitle" to "Backend Engineer",
            "hobbies" to listOf("Chess"),
            "location" to mapOf("latitude" to 37.7749, "longitude" to -122.4194)
        )

        mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("name contains disallowed characters") }
            }
    }

    @Test
    fun `POST persons returns field error when location is missing`() {
        val payload = mapOf(
            "name" to "Alice",
            "jobTitle" to "Backend Engineer",
            "hobbies" to listOf("Chess")
        )

        mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Validation failed") }
                jsonPath("$.fieldErrors.location") { value("location is required") }
            }
    }

    @Test
    fun `POST persons returns field error when location latitude is missing`() {
        val payload = mapOf(
            "name" to "Alice",
            "jobTitle" to "Backend Engineer",
            "hobbies" to listOf("Chess"),
            "location" to mapOf("longitude" to -122.4194)
        )

        mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Validation failed") }
                jsonPath("$.fieldErrors[\"location.latitude\"]") { value("latitude is required") }
            }
    }

    @Test
    fun `POST persons returns field error when location longitude is missing`() {
        val payload = mapOf(
            "name" to "Alice",
            "jobTitle" to "Backend Engineer",
            "hobbies" to listOf("Chess"),
            "location" to mapOf("latitude" to 37.7749)
        )

        mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Validation failed") }
                jsonPath("$.fieldErrors[\"location.longitude\"]") { value("longitude is required") }
            }
    }

    @Test
    fun `PUT location updates existing person`() {
        val existing = personRepository.save(
            PersonEntity(
                id = "01H00000000000000000000010",
                name = "Bob",
                jobTitle = "Engineer",
                hobbiesCsv = "hiking",
                bio = "Bio",
                latitude = 10.0,
                longitude = 10.0
            )
        )

        val payload = mapOf("latitude" to 11.11, "longitude" to 22.22)

        mockMvc.put("/persons/${existing.id}/location") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(existing.id) }
                jsonPath("$.latitude") { value(11.11) }
                jsonPath("$.longitude") { value(22.22) }
            }
    }

    @Test
    fun `PUT location returns 404 for unknown person`() {
        val payload = mapOf("latitude" to 11.11, "longitude" to 22.22)

        mockMvc.put("/persons/01H00000000000000000000099/location") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message", containsString("was not found"))
            }
    }

    @Test
    fun `PUT location returns 400 for invalid ULID`() {
        val payload = mapOf("latitude" to 11.11, "longitude" to 22.22)

        mockMvc.put("/persons/not-a-ulid/location") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `PUT location returns field error when latitude is missing`() {
        val payload = mapOf("longitude" to 22.22)

        mockMvc.put("/persons/01H00000000000000000000099/location") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Validation failed") }
                jsonPath("$.fieldErrors.latitude") { value("latitude is required") }
            }
    }

    @Test
    fun `PUT location returns field error when longitude is missing`() {
        val payload = mapOf("latitude" to 11.11)

        mockMvc.put("/persons/01H00000000000000000000099/location") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Validation failed") }
                jsonPath("$.fieldErrors.longitude") { value("longitude is required") }
            }
    }

    @Test
    fun `GET nearby returns sorted people within radius`() {
        personRepository.saveAll(
            listOf(
                PersonEntity(
                    id = "01H00000000000000000000101",
                    name = "Near",
                    jobTitle = "Engineer",
                    hobbiesCsv = "chess",
                    bio = "Bio",
                    latitude = -36.8485,
                    longitude = 174.7733
                ),
                PersonEntity(
                    id = "01H00000000000000000000102",
                    name = "Middle",
                    jobTitle = "Designer",
                    hobbiesCsv = "music",
                    bio = "Bio",
                    latitude = -36.8485,
                    longitude = 174.8033
                ),
                PersonEntity(
                    id = "01H00000000000000000000103",
                    name = "Far",
                    jobTitle = "Writer",
                    hobbiesCsv = "surfing",
                    bio = "Bio",
                    latitude = -36.8485,
                    longitude = 175.1633
                )
            )
        )

        mockMvc.get("/persons/nearby") {
            param("latitude", "-36.8485")
            param("longitude", "174.7633")
            param("radiusKm", "10")
            param("limit", "10")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(2))
                jsonPath("$[0].id") { value("01H00000000000000000000101") }
                jsonPath("$[1].id") { value("01H00000000000000000000102") }
            }
    }

    @Test
    fun `GET nearby rejects radius above configured cap`() {
        mockMvc.get("/persons/nearby") {
            param("latitude", "0")
            param("longitude", "0")
            param("radiusKm", "51")
            param("limit", "10")
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("radiusKm must be <= 50.0") }
            }
    }

    @Test
    fun `GET nearby rejects limit above cap`() {
        mockMvc.get("/persons/nearby") {
            param("latitude", "0")
            param("longitude", "0")
            param("radiusKm", "10")
            param("limit", "201")
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("limit must be between 1 and 200") }
            }
    }

    @Test
    fun `error response does not leak stack traces or class names`() {
        val responseBody = mockMvc.post("/persons") {
            contentType = MediaType.APPLICATION_JSON
            content = "{\"name\":\"Alice\""
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Malformed JSON request") }
            }
            .andReturn()
            .response
            .contentAsString

        assertFalse(responseBody.contains("Exception"))
        assertFalse(responseBody.contains("com.persons.finder"))
    }
}
