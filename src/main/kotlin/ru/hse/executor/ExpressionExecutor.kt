package ru.hse.executor

import ru.hse.executable.ExecutionResult
import java.io.InputStream
import java.io.OutputStream

interface ExpressionExecutor {
    fun execute(expression: String, input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult
}
