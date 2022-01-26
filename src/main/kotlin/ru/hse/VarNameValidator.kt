package ru.hse

interface VarNameValidator {
    fun check(token: String): Boolean
    fun nameFromBeginningIn(token: String): String
}
