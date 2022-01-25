package ru.hse

interface Tokenizer {
    fun tokenize(expression: String): Result<List<String>>
}
