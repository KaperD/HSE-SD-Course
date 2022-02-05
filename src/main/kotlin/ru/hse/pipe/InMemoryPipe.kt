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
    init {
        if (commands.isEmpty()) {
            throw IllegalArgumentException("Commands for pipe should not be empty")
        }
    }

    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        var currentInput: InputStream = input
        val inMemoryOutput = ByteArrayOutputStream()
        for (command in commands.dropLast(1)) {
            val res = command.run(currentInput, inMemoryOutput, error)
            if (res.exitCode != 0 || res.needExit) {
                if (res.needExit) {
                    output.write(inMemoryOutput.toByteArray())
                }
                return res
            }
            currentInput = ByteArrayInputStream(inMemoryOutput.toByteArray())
            inMemoryOutput.reset()
        }
        return commands.last().run(currentInput, output, error)
    }
}
