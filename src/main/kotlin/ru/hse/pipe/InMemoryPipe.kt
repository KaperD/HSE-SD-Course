package ru.hse.pipe

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Пайп, в котором данные между командами передаются через оперативную память
 */
class InMemoryPipe(private val commands: List<Executable>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        var currentInput: InputStream = input
        val inMemoryOutput = ByteArrayOutputStream()
        for ((i, command) in commands.withIndex()) {
            val isLastCommand = i == commands.lastIndex
            val currentOutput = if (isLastCommand) output else inMemoryOutput
            val res = command.run(currentInput, currentOutput, error)
            if (res.exitCode != 0 || res.needExit || isLastCommand) {
                if (res.needExit && !isLastCommand) {
                    output.write(inMemoryOutput.toByteArray())
                }
                return res
            }
            currentInput = ByteArrayInputStream(inMemoryOutput.toByteArray())
            inMemoryOutput.reset()
        }
        throw IllegalStateException("Should not reach here")
    }
}
