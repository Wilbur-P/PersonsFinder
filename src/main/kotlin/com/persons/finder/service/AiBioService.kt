package com.persons.finder.service

interface AiBioService {
    fun generateBio(jobTitle: String, hobbies: List<String>): String
}
