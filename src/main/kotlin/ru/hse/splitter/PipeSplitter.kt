package ru.hse.splitter

/**
 * Отвечает за разбиение пайпа на команды
 */
interface PipeSplitter {
    /**
     * Поделить список токенов на команды (команды в виде списка токенов)
     * Если не получилось, возвращает ошибку
     */
    fun split(tokens: List<String>): Result<List<List<String>>>
}
