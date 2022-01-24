package ru.hse

import java.io.InputStream
import java.io.OutputStream

interface Command {
    fun run(input: InputStream, output: OutputStream, error: OutputStream): Int
    fun isExit(): Boolean
}