package ru.hse.parser

interface Parser<T> {
    fun parse(line: String): Result<T>
}
