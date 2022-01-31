package ru.hse.cli

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.utils.trimMarginCrossPlatform
import ru.hse.utils.write
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.assertEquals

class CLITest {
    private val charset: Charset = HseshCharsets.default

    private fun createCLI(input: InputStream, output: OutputStream, error: OutputStream): CLI {
        return CLIImpl(input, output, error)
    }

    @Test
    fun `test read line`() {
        val input = ByteArrayInputStream("Hello   ${lineSeparator()}   World${lineSeparator()}".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        assertEquals(listOf("Hello   ", "   World"), listOf(cli.getLine(), cli.getLine()))
        assertEquals("> > ", output.toString(charset))
    }

    @Test
    fun `test read empty lines`() {
        val input = ByteArrayInputStream(
            """
                |
                |
                |
            """.trimMarginCrossPlatform().toByteArray(charset)
        )
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        assertEquals(listOf("", "", ""), listOf(cli.getLine(), cli.getLine(), cli.getLine()))
        assertEquals("> > > ", output.toString(charset))
    }

    @Test
    fun `test show message`() {
        val input = ByteArrayInputStream("".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        cli.showMessage("Hello")
        cli.showMessage(" ${lineSeparator()}")
        cli.showMessage("World")
        assertEquals(
            "Hello${lineSeparator()} ${lineSeparator()}${lineSeparator()}World${lineSeparator()}",
            output.toString(charset)
        )
    }

    @Test
    fun `test get input stream`() {
        val s = "Hello ${lineSeparator()} world"
        val input = ByteArrayInputStream(s.toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        assertEquals(s, String(cli.inputStream.readAllBytes(), charset))
    }

    @Test
    fun `test get output stream`() {
        val s = "Hello ${lineSeparator()} world"
        val input = ByteArrayInputStream("".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        cli.outputStream.write(s)
        assertEquals(s, output.toString(charset))
    }

    @Test
    fun `test get error stream`() {
        val s = "Hello ${lineSeparator()} world"
        val input = ByteArrayInputStream("".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        cli.errorStream.write(s)
        assertEquals(s, error.toString(charset))
    }
}
