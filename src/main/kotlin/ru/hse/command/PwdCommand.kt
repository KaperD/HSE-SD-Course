package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.write
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths

class PwdCommand(private val args: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        if (args.isNotEmpty()) {
            error.write("pwd: too many arguments\n")
            return ExecutionResult.fail()
        }
        output.write(Paths.get("").toAbsolutePath().toString())
        output.write("\n")
        return ExecutionResult.success()
    }
}
