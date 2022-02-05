package ru.hse.factory

import ru.hse.executable.Executable
import java.util.function.Function

/**
 * Отвечает за создание команд
 */
interface CommandFactory {
    /**
     * Зарегистрировать команду hsesh
     * @param name имя регистрируемой команды
     * @param factory позволяет создать команду из списка её аргументов
     */
    fun registerCommand(name: String, factory: Function<List<String>, Executable>)

    /**
     * Создать команду из списка токенов
     * @param tokens токены для создания команды.
     *               Первый токен -- название зарегестрированной команды, остальные -- её аргументы.
     *               Если команда с данным именем не была зарегестрирована, создаётся DefaultCommand
     * @return созданная команда
     */
    fun create(tokens: List<String>): Executable
}
