package ru.hse.pipe

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.*

/**
 * Пайп, в котором данные между командами передаются через файлы
 */
class FilePipe(private val commands: List<Executable>) : Executable {
    init {
        assert(commands.isNotEmpty())
    }

    override fun run(file: File, output: OutputStream, error: OutputStream): ExecutionResult {
        return runInternal(file, output, error)
    }

    override fun runInheritInput(output: OutputStream, error: OutputStream): ExecutionResult {
        return runInternal(null, output, error)
    }

    private fun runInternal(file: File?, output: OutputStream, error: OutputStream): ExecutionResult {
        return if (commands.size == 1) {
            runOne(commands.first(), file, output, error)
        } else {
            runMultiple(file, output, error)
        }
    }

    private fun runMultiple(file: File?, output: OutputStream, error: OutputStream): ExecutionResult {
        val pipeInputFile = File.createTempFile("pipeFile", null)
        var pipeOutputFile = File.createTempFile("pipeFile", null)
        var currentInputFile = file

        try {
            for ((i, command) in commands.dropLast(1).withIndex()) {
                val res = pipeOutputFile.outputStream().buffered().use {
                    runOne(command, currentInputFile, it, error)
                }
                if (res.exitCode != 0 || res.needExit) {
                    if (res.needExit) {
                        pipeOutputFile.inputStream().buffered().transferTo(output)
                    }
                    return res
                }
                val t = currentInputFile
                currentInputFile = pipeOutputFile
                pipeOutputFile = if (i == 0) pipeInputFile else t!!
            }
            return runOne(commands.last(), currentInputFile, output, error)
        } finally {
            pipeOutputFile.delete()
            pipeInputFile.delete()
        }
    }

    private fun runOne(command: Executable, file: File?, output: OutputStream, error: OutputStream): ExecutionResult {
        return if (file != null) {
            command.run(file, output, error)
        } else {
            command.runInheritInput(output, error)
        }
    }
}
