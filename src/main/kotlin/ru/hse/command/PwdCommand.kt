package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Paths

class PwdCommand(private val args: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        if (args.isNotEmpty()) {
            error.writeln("pwd: too many arguments")
            return ExecutionResult.fail()
        }
        output.writeln(Paths.get("").toAbsolutePath().toString())
        return ExecutionResult.success()
    }
}
