package ru.hse.command

import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.InputStream
import java.io.OutputStream

class ExitCommand : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        output.write("Bye\n".toByteArray(HseshCharsets.default))
        return ExecutionResult.success(true)
    }
}
