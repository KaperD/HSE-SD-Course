package ru.hse.tokenizer

/**
 * Отвечает за разбиение выражения на токены
 */
interface Tokenizer {
    /**
     * Разбивает выражение на токены
     * Если не получилось, возвращает ошибку
     */
    fun tryTokenize(expression: String): Result<List<String>>
}
