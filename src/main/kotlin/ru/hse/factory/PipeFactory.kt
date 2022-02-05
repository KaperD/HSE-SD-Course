package ru.hse.factory

import ru.hse.executable.Executable

/**
 * Отвечает за создание пайпов
 */
interface PipeFactory {
    /**
     *  Создать пайп из списка команд
     */
    fun create(commands: List<Executable>): Executable
}
