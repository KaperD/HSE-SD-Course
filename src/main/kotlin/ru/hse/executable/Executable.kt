package ru.hse.executable

import java.io.InputStream
import java.io.OutputStream

interface Executable {
    fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult
}
