package com.persons.finder.repository

import com.persons.finder.entity.PersonEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PersonRepository : JpaRepository<PersonEntity, String> {
    fun findByLatitudeBetweenAndLongitudeBetween(
        latitudeMin: Double,
        latitudeMax: Double,
        longitudeMin: Double,
        longitudeMax: Double
    ): List<PersonEntity>
}
