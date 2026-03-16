package com.persons.finder.entity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table

@Entity
@Table(
    name = "persons",
    indexes = [
        Index(name = "idx_persons_lat_lon", columnList = "latitude, longitude")
    ]
)
class PersonEntity(
    @Id
    @Column(nullable = false, length = 26, updatable = false)
    var id: String = "",

    @Column(nullable = false, length = 80)
    var name: String = "",

    @Column(name = "job_title", nullable = false, length = 80)
    var jobTitle: String = "",

    @Column(name = "hobbies_csv", nullable = false, length = 400)
    var hobbiesCsv: String = "",

    @Column(nullable = false, length = 512)
    var bio: String = "",

    @Column(nullable = false)
    var latitude: Double = 0.0,

    @Column(nullable = false)
    var longitude: Double = 0.0,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    @PrePersist
    fun onCreate() {
        val now = LocalDateTime.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
