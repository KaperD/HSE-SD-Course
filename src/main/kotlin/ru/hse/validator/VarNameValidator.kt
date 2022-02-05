package ru.hse.validator

/**
 * Отвечает за имена переменных
 */
interface VarNameValidator {
    /**
     * Проверить, является ли токен корректным именем переменной
     */
    fun check(token: String): Boolean

    /**
     * Найти корректное имя переменной, начиная с начала строки
     */
    fun nameFromBeginningIn(token: String): String
}
