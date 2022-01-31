package ru.hse.cli

import ru.hse.charset.HseshCharsets
import ru.hse.utils.write
import ru.hse.utils.writeln
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream

class CLIImpl(
    override val inputStream: InputStream,
    override val outputStream: OutputStream,
    override val errorStream: OutputStream
) : CLI {
    private val reader = BufferedReader(inputStream.reader(HseshCharsets.default))

    override fun getLine(): String {
        outputStream.write("> ")
        return reader.readLine() ?: ""
    }

    override fun showMessage(message: String) {
        outputStream.writeln(message)
    }
}
