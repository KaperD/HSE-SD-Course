package ru.hse.command

import ru.hse.environment.Environment
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.File
import java.io.IOException
import java.io.OutputStream

class DefaultCommand(private var command: List<String>, private val environment: Environment) : Executable {
    override fun run(file: File, output: OutputStream, error: OutputStream): ExecutionResult {
        val process = startProcess(ProcessBuilder.Redirect.from(file), error) ?: return ExecutionResult.fail()
        return finishProcess(process, output, error)
    }

    override fun runInheritInput(output: OutputStream, error: OutputStream): ExecutionResult {
        val process = startProcess(ProcessBuilder.Redirect.INHERIT, error) ?: return ExecutionResult.fail()
        return finishProcess(process, output, error)
    }

    private fun startProcess(source: ProcessBuilder.Redirect, error: OutputStream): Process? {
        val processBuilder = ProcessBuilder(command)
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
            return ExecutionResult.fail()
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
