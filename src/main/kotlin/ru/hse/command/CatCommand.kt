package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.write
import java.io.*
import kotlin.io.path.Path
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

class CatCommand(private val arguments: List<String>) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        return when {
            arguments.isEmpty() -> processEmptyArgumentList(input, output, error)
            else -> processNotEmptyArgumentList(input, output, error)
        }
    }

    private fun processEmptyArgumentList(
        input: InputStream,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        try {
            input.transferTo(output)
        } catch (e: IOException) {
            error.writeIOError(e.message)
            return ExecutionResult.fail()
        }
        return ExecutionResult.success()
    }

    private fun processNotEmptyArgumentList(
        ignored: InputStream,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        var isSuccessful = true
        for (fileName in arguments) {
            val fileValidationResult = checkFile(fileName, error)
            if (!fileValidationResult) {
                isSuccessful = false
                continue
            }
            @Suppress("SwallowedException")
            try {
                File(fileName).inputStream().buffered().use {
                    it.transferTo(output)
                }
            } catch (e: SecurityException) {
                isSuccessful = false
                error.writePermissionDeniedError(fileName)
            } catch (e: FileNotFoundException) {
                isSuccessful = false
                error.writeIsNotFileError(fileName)
            } catch (e: IOException) {
                isSuccessful = false
                error.writeIOError(e.message)
            }
        }
        return if (isSuccessful) ExecutionResult.success() else ExecutionResult.fail()
    }

    private fun checkFile(fileName: String, error: OutputStream): Boolean {
        val pathToFile = Path(fileName)
        return when {
            pathToFile.notExists() -> {
                error.writeNotExistsError(fileName)
                false
            }
            !pathToFile.isRegularFile() -> {
                error.writeIsNotFileError(fileName)
                false
            }
            !pathToFile.isReadable() -> {
                error.writePermissionDeniedError(fileName)
                false
            }
            else -> true
        }
    }

    private fun OutputStream.writeIsNotFileError(fileName: String) {
        write("cat: $fileName: Is not a regular file\n")
    }

    private fun OutputStream.writeNotExistsError(fileName: String) {
        write("cat: $fileName: No such file or directory\n")
    }

    private fun OutputStream.writePermissionDeniedError(fileName: String) {
        write("cat: $fileName: Permission denied\n")
    }

    private fun OutputStream.writeIOError(message: String?) {
        write("cat: IO problem: $message\n")
    }
}
