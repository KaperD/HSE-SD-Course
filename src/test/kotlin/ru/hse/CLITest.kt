package ru.hse

import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

class CLITest {
    private val charset: Charset = StandardCharsets.UTF_8
    private fun createCLI(input: InputStream, output: OutputStream, error: OutputStream): CLI {
        TODO("Return object when it's ready")
    }

    @Test
    fun `test read line`() {
        val input = ByteArrayInputStream("Hello   \n   World\n".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        assertEquals(listOf("Hello   ", "   World"), listOf(cli.getLine(), cli.getLine()))
        assertEquals("> > ", output.toString(charset))
    }

    @Test
    fun `test read empty lines`() {
        val input = ByteArrayInputStream("\n\n\n".toByteArray(charset))
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
        cli.showMessage(" \n")
        cli.showMessage("World")
        assertEquals("Hello\n \n\nWorld\n", output.toString(charset))
    }

    @Test
    fun `test get input stream`() {
        val s = "Hello \n world"
        val input = ByteArrayInputStream(s.toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        assertEquals(s, String(cli.getInputStream().readAllBytes(), charset))
    }

    @Test
    fun `test get output stream`() {
        val s = "Hello \n world"
        val input = ByteArrayInputStream("".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        cli.getOutputStream().write(s.toByteArray(charset))
        assertEquals(s, output.toString(charset))
    }

    @Test
    fun `test get error stream`() {
        val s = "Hello \n world"
        val input = ByteArrayInputStream("".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val cli = createCLI(input, output, error)
        cli.getErrorStream().write(s.toByteArray(charset))
        assertEquals(s, error.toString(charset))
    }
}
