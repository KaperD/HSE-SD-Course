package ru.hse.command

import ru.hse.charset.HseshCharsets
import ru.hse.environment.Environment
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class DefaultCommand(private var command: List<String>, private val environment: Environment) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        val processBuilder = ProcessBuilder(command)
        processBuilder.environment().putAll(environment.getAll())
        val process: Process?
        try {
            process = processBuilder.start()
        } catch (e: IOException) {
            error.write(e.message?.toByteArray(HseshCharsets.default) ?: "".toByteArray(HseshCharsets.default))
            return ExecutionResult.fail(false)
        }
        @Suppress("SwallowedException")
        try {
            input.transferTo(process.outputStream)
            process.outputStream.close()
            process.inputStream.transferTo(output)
            process.errorStream.transferTo(error)
        } catch (e: IOException) {
            return ExecutionResult.fail(true)
        }
        process.waitFor()
        return ExecutionResult(process.exitValue(), false)
    }
}
