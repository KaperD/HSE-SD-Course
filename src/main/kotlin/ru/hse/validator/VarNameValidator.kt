package ru.hse.validator

interface VarNameValidator {
    fun check(token: String): Boolean
    fun nameFromBeginningIn(token: String): String
}
