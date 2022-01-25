package ru.hse

interface Parser<T> {
    fun parse(line: String): Result<T>
}