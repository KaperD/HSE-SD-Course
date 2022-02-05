package ru.hse.factory

import ru.hse.command.DefaultCommand
import ru.hse.environment.Environment
import ru.hse.executable.Executable
import java.lang.IllegalArgumentException
import java.util.function.Function

/**
 * Название команды — первый из переданных токенов
 * Если название команды зарегистрировано, то возвращает зарегистрированную реализацию
 * Иначе возвращает сущность, которая запустит другой процесс с переданным названием
 */
class CommandFactoryImpl(private val environment: Environment) : CommandFactory {
    private val commands = hashMapOf<String, Function<List<String>, Executable>>()

    override fun registerCommand(name: String, factory: Function<List<String>, Executable>) {
        if (commands.putIfAbsent(name, factory) != null) {
            throw IllegalStateException("The command $name is not registered for the first time")
        }
    }

    override fun create(tokens: List<String>): Executable {
        if (tokens.isEmpty()) {
            throw IllegalArgumentException("Cannot create a command from the empty list of tokens")
        }
        val commandArguments = if (tokens.size == 1) listOf() else tokens.drop(1)
        return commands[tokens.first()]?.apply(commandArguments) ?: DefaultCommand(tokens, environment)
    }
}
