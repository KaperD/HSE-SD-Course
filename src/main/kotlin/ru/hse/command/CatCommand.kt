package ru.hse.command

import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.*
import kotlin.io.path.*

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
        ignored: OutputStream
    ): ExecutionResult {
        @Suppress("SwallowedException")
        try {
            input.transferTo(output)
        } catch (e: IOException) {
            return ExecutionResult.fail(true)
        }
        return ExecutionResult.success(false)
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
                File(fileName).inputStream().use {
                    it.transferTo(output)
                }
            } catch (e: SecurityException) {
                isSuccessful = false
                error.writePermissionDeniedError(fileName)
            } catch (e: FileNotFoundException) {
                isSuccessful = false
                error.writeIsNotFileError(fileName)
            } catch (e: IOException) {
                return ExecutionResult.fail(true)
            }
        }
        return if (isSuccessful) ExecutionResult.success(false) else ExecutionResult.fail(false)
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

    private fun OutputStream.writeError(message: String) {
        this.write(message.toByteArray(charset = HseshCharsets.default))
    }

    private fun OutputStream.writeIsNotFileError(fileName: String) {
        this.writeError("cat: $fileName: Is not a regular file\n")
    }

    private fun OutputStream.writeNotExistsError(fileName: String) {
        this.writeError("cat: $fileName: No such file or directory\n")
    }

    private fun OutputStream.writePermissionDeniedError(fileName: String) {
        this.writeError("cat: $fileName: Permission denied\n")
    }
}
