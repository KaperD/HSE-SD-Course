package ru.hse.command

import ru.hse.utils.writeln
import java.io.*
import kotlin.io.path.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

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
     * @param block действие над открытым для чтения файлом
     * @return true, если чтение завершилось без ошибок
     */
    fun readFile(fileName: String, error: OutputStream, block: (InputStream) -> Unit): Boolean {
        if (!checkFile(fileName, error)) {
            return false
        }
        @Suppress("SwallowedException")
        return try {
            File(fileName).inputStream().buffered().use {
                block(it)
            }
            true
        } catch (e: SecurityException) {
            error.writePermissionDeniedError(fileName)
            false
        } catch (e: FileNotFoundException) {
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
     * @param block IO действие
     * @return true, если выполнение завершилось без ошибок
     */
    fun safeIO(error: OutputStream, block: () -> Unit): Boolean {
        return try {
            block()
            true
        } catch (e: IOException) {
            error.writeIOError(e.message)
            false
        }
    }

    private fun checkFile(fileName: String, error: OutputStream): Boolean {
        val pathToFile = Path(fileName)
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
