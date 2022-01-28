package ru.hse.splitter

interface PipeSplitter {
    fun split(tokens: List<String>): Result<List<List<String>>>
}
