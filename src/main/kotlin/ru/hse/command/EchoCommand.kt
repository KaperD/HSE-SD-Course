package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.write
import java.io.InputStream
import java.io.OutputStream

class EchoCommand(private val args: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        output.write(args.joinToString(separator = " ", postfix = "\n"))
        return ExecutionResult.success()
    }
}
