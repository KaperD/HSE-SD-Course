package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.*

/**
 * cat [file ...] — конкатенирует содержимое файлов и выводит его в поток вывода
 * Если список файлов пуст, то переводит весь поток ввода в поток вывода
 */
class CatCommand(private val arguments: List<String>) : IOCommand, Executable {
    override val commandName: String = "cat"

    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        return when {
            arguments.isEmpty() -> processEmptyArgumentList(input, output, error)
            else -> processNotEmptyArgumentList(output, error)
        }
    }

    private fun processEmptyArgumentList(
        input: InputStream,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        return if (safeIO(error) { input.transferTo(output); true }) {
            ExecutionResult.success
        } else {
            ExecutionResult.fail
        }
    }

    private fun processNotEmptyArgumentList(
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        var isSuccessful = true
        for (fileName in arguments) {
            if (!readFile(fileName, error) { it.transferTo(output); true }) {
                isSuccessful = false
            }
        }
        return if (isSuccessful) ExecutionResult.success else ExecutionResult.fail
    }
}
