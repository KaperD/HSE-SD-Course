package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.InputStream
import java.io.OutputStream

class EchoCommand(private val args: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        output.writeln(args.joinToString(separator = " "))
        return ExecutionResult.success()
    }
}
