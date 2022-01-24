package ru.hse

interface PipeSplitter {
    fun split(tokens: List<String>): List<List<String>>
}