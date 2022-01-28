package ru.hse.tokenizer

interface Tokenizer {
    fun tokenize(expression: String): Result<List<String>>
}
