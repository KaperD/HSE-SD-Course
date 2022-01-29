package ru.hse.cli

import ru.hse.charset.HseshCharsets
import ru.hse.utils.write
import ru.hse.utils.writeln
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class CLIImpl(
    override val inputStream: InputStream,
    override val outputStream: OutputStream,
    override val errorStream: OutputStream
) : CLI {
    private val newLineChar: Int = "\n".toByteArray(HseshCharsets.default)[0].toInt()

    override fun getLine(): String {
        outputStream.write("> ")
        val builder = ByteArrayOutputStream()
        var c = inputStream.read()
        while (c != newLineChar) {
            builder.write(c)
            c = inputStream.read()
        }
        return builder.toString(HseshCharsets.default)
    }

    override fun showMessage(message: String) {
        outputStream.writeln(message)
    }
}
