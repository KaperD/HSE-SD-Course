package ru.hse.executor

import ru.hse.executable.ExecutionResult
import java.io.File
import java.io.OutputStream

interface ExpressionExecutor {
    fun execute(expression: String, file: File?, output: OutputStream, error: OutputStream): ExecutionResult
}
