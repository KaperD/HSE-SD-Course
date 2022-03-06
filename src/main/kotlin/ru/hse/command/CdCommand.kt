package ru.hse.command

import ru.hse.environment.Environment
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.path.*

/**
 * cd [arg ] -- меняет текущую рабочую директорию
 * Если аргумента нет -- меняет директорию на домашнюю для пользователя
 * Если аргумент есть -- меняет рабочую директорию на новую
 */
class CdCommand(
    private val environment: Environment,
    private val args: List<String>
) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream
    ): ExecutionResult = when(args.size) {
        0 -> {
            environment.workDirectory = Path(System.getProperty("user.home"))
            ExecutionResult.success
        }
        1 -> when (environment.workDirectory.resolve(args[0]).isDirectory()) {
                true -> {
                    environment.workDirectory = environment.workDirectory.resolve(args[0])
                    ExecutionResult.success
                }
                false -> {
                    error.writeln("cd: ${args[0]} is not a directory")
                    ExecutionResult.fail
                }
            }
        else -> {
            error.writeln("cd: too many arguments")
            ExecutionResult.fail
        }
    }
}
