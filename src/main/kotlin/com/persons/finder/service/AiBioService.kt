package com.persons.finder.service

interface AiBioService {
    fun generateBio(input: BioGenerationInput): String
}
