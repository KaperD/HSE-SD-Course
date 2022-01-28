package ru.hse.cli

import ru.hse.charset.HseshCharsets
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

class CLIImpl(
    private val input: InputStream,
    private val output: OutputStream,
    private val error: OutputStream
) : CLI {
    private val newLineChar: Int = "\n".toByteArray(HseshCharsets.default)[0].toInt()

    override fun getLine(): String {
        output.write("> ".toByteArray(HseshCharsets.default))
        val builder = ByteArrayOutputStream()
        var c = input.read()
        while (c != newLineChar) {
            builder.write(c)
            c = input.read()
        }
        return builder.toString(HseshCharsets.default)
    }

    override fun showMessage(message: String) {
        output.write(message.toByteArray(HseshCharsets.default))
        output.write(newLineChar)
    }

    override fun getInputStream(): InputStream = input

    override fun getOutputStream(): OutputStream = output

    override fun getErrorStream(): OutputStream = error
}
