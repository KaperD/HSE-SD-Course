package ru.hse.command

import ru.hse.environment.Environment
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.write
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DefaultCommand(private var command: List<String>, private val environment: Environment) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        val process = startProcess(error) ?: return ExecutionResult.fail()
        return finishProcess(process, input, output, error)
    }

    private fun startProcess(error: OutputStream): Process? {
        val processBuilder = ProcessBuilder(command)
        processBuilder.environment().putAll(environment.getAll())
        return try {
            processBuilder.start()
        } catch (e: IOException) {
            error.write(e.message)
            null
        }
    }

    private fun finishProcess(
        process: Process,
        input: InputStream,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        if (!writeToProcess(process, input, error) || !readFromProcess(process, output, error)) {
            return ExecutionResult.fail()
        }
        return ExecutionResult(process.exitValue(), false)
    }

    private fun writeToProcess(process: Process, input: InputStream, error: OutputStream): Boolean {
        return try {
            input.transferTo(process.outputStream)
            process.outputStream.close()
            true
        } catch (e: IOException) {
            // Если сообщение "Stream closed" -- это значит, что команда не имеет входных данных
            if (e.message != "Stream closed") {
                error.write(e.message)
            }
            e.message == "Stream closed"
        }
    }

    private fun readFromProcess(process: Process, output: OutputStream, error: OutputStream): Boolean {
        return try {
            process.inputStream.transferTo(output)
            process.errorStream.transferTo(error)
            process.waitFor()
            true
        } catch (e: IOException) {
            error.write(e.message)
            false
        }
    }
}
