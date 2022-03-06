package ru.hse.command

import ru.hse.environment.Environment
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ProcessBuilder.Redirect

/**
 * Отвечает за исполнение команд, неизвестных Hsesh, путём запуска другого процесса
 */
class DefaultCommand(private var command: List<String>, private val environment: Environment) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        return if (input === System.`in`) {
            runInherit(output, error)
        } else {
            runFromFile(input, output, error)
        }
    }

    private fun runInherit(output: OutputStream, error: OutputStream): ExecutionResult {
        val process = startProcess(Redirect.INHERIT, error) ?: return ExecutionResult.fail
        return finishProcess(process, output, error)
    }

    private fun runFromFile(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        val file = File.createTempFile("default_command", null)
        return try {
            file.outputStream().buffered().use {
                input.transferTo(it)
            }
            val process = startProcess(Redirect.from(file), error) ?: return ExecutionResult.fail
            finishProcess(process, output, error)
        } catch (e: IOException) {
            error.writeln(e.message)
            ExecutionResult.fail
        } finally {
            file.delete()
        }
    }

    private fun startProcess(source: Redirect, error: OutputStream): Process? {
        val processBuilder = ProcessBuilder(command).directory(environment.workDirectory.toFile())
        processBuilder.redirectInput(source)
        processBuilder.environment().putAll(environment.getAll())
        return try {
            processBuilder.start()
        } catch (e: IOException) {
            error.writeln(e.message)
            null
        }
    }

    private fun finishProcess(
        process: Process,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        if (!readFromProcess(process, output, error)) {
            return ExecutionResult.fail
        }
        return ExecutionResult(process.exitValue(), false)
    }

    private fun readFromProcess(process: Process, output: OutputStream, error: OutputStream): Boolean {
        return try {
            process.inputStream.transferTo(output)
            process.errorStream.transferTo(error)
            process.waitFor()
            true
        } catch (e: IOException) {
            error.writeln(e.message)
            false
        }
    }
}
