package ru.hse

interface PipeSplitter {
    fun split(tokens: List<String>): Result<List<List<String>>>
}