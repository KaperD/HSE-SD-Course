package ru.hse.environment

import java.nio.file.Path

/**
 * Отвечает за переменные окружения и текущую рабочую директорию
 */
interface Environment {
    /**
     * Текущая рабочая директория
     */
    var workDirectory: Path

    /**
     * Установить значение для переменной
     * @param key имя переменной
     * @param value значение переменной
     */
    fun set(key: String, value: String)

    /**
     * Получить значение переменной
     * @param key имя переменной
     */
    fun get(key: String): String

    /**
     * Получить все переменные и их значения
     */
    fun getAll(): Map<String, String>
}
