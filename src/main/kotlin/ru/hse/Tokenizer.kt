package ru.hse

interface Tokenizer {
    fun parse(expression: String): List<String>
}