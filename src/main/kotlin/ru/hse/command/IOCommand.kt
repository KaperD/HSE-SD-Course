package ru.hse.command

import ru.hse.environment.Environment
import ru.hse.utils.writeln
import java.io.*
import kotlin.io.path.*

/**
 * Команда, которая будет работать с вводом/выводом
 */
interface IOCommand {
    /**
     * Имя команды, которое будет использоваться при выводе ошибок
     */
    val commandName: String

    /**
     * Безопасно выполнить действие block над открытым для чтения файла.
     * Выводит ошибку в error, если она случилась во время чтения
     * @param fileName путь до файла
     * @param error поток для вывода ошибок
     * @param block действие над открытым для чтения файлом, которое возвращает true, если всё прошло без ошибок
     * @return true, если чтение завершилось без ошибок
     */
    fun readFile(
        environment: Environment,
        fileName: String,
        error: OutputStream,
        block: (InputStream) -> Boolean
    ): Boolean {
        if (!checkFile(environment, fileName, error)) {
            return false
        }
        return try {
            environment.workDirectory.resolve(fileName).inputStream().buffered().use {
                block(it)
            }
        } catch (ignored: SecurityException) {
            error.writePermissionDeniedError(fileName)
            false
        } catch (ignored: FileNotFoundException) {
            error.writeIsNotFileError(fileName)
            false
        } catch (e: IOException) {
            error.writeIOError(e.message)
            false
        }
    }

    /**
     * Безопасно выполнить IO действия
     * Если во время исполнения произошло IOException, ошибка будет выведена в error
     * @param error поток для вывода ошибок
     * @param block IO действие, которое возвращает true, если всё прошло без ошибок
     * @return true, если выполнение завершилось без ошибок
     */
    fun safeIO(error: OutputStream, block: () -> Boolean): Boolean {
        return try {
            block()
        } catch (e: IOException) {
            error.writeIOError(e.message)
            false
        }
    }

    private fun checkFile(environment: Environment, fileName: String, error: OutputStream): Boolean {
        val pathToFile = environment.workDirectory.resolve(fileName)
        return when {
            pathToFile.notExists() -> {
                error.writeNotExistsError(fileName)
                false
            }
            !pathToFile.isRegularFile() -> {
                error.writeIsNotFileError(fileName)
                false
            }
            !pathToFile.isReadable() -> {
                error.writePermissionDeniedError(fileName)
                false
            }
            else -> true
        }
    }

    private fun OutputStream.writeIsNotFileError(fileName: String) {
        writeln("$commandName: $fileName: Is not a regular file")
    }

    private fun OutputStream.writeNotExistsError(fileName: String) {
        writeln("$commandName: $fileName: No such file or directory")
    }

    private fun OutputStream.writePermissionDeniedError(fileName: String) {
        writeln("$commandName: $fileName: Permission denied")
    }

    private fun OutputStream.writeIOError(message: String?) {
        writeln("$commandName: IO problem: $message")
    }
}
