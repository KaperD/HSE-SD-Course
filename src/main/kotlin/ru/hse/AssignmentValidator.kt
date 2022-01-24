package ru.hse

import java.util.*

interface AssignmentValidator {
    fun check(expression: String): Optional<Pair<String, String>>
}