package ru.hse

interface VarNameValidator {
    fun check(token: String): Boolean
    fun lengthIn(token: String): Int
}