package ru.hse.executable

import java.io.File
import java.io.OutputStream

interface Executable {
    fun run(file: File, output: OutputStream, error: OutputStream): ExecutionResult
    fun runInheritInput(output: OutputStream, error: OutputStream): ExecutionResult
}
