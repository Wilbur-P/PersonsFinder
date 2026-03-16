package com.persons.finder.exception

class PersonNotFoundException(id: String) : RuntimeException("Person with id '$id' was not found")
