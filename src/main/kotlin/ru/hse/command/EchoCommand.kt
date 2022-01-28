package ru.hse.command

import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.InputStream
import java.io.OutputStream

class EchoCommand(private val args: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        output.write(args.joinToString(separator = " ", postfix = "\n").toByteArray(HseshCharsets.default))
        return ExecutionResult.success(false)
    }
}
