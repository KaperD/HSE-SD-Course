package ru.hse.command

import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths

class PwdCommand(private val args: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        if (args.isNotEmpty()) {
            error.write("pwd: too many arguments\n".toByteArray(HseshCharsets.default))
            return ExecutionResult(1, false)
        }
        output.write(Paths.get("").toAbsolutePath().toString().toByteArray(HseshCharsets.default))
        output.write("\n".toByteArray(HseshCharsets.default))
        return ExecutionResult(0, false)
    }
}
