package ru.hse.substitutor

/**
 * Отвечает за замену переменных и удаление внешних кавычек
 */
interface Substitutor {
    /**
     * Заменить в токене переменные на их значения и убрать кавычки
     */
    fun substitute(token: String): String
}
