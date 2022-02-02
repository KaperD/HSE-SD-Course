package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.environment.EnvironmentImpl
import java.io.*
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class DefaultCommandTest {
    private val charset: Charset = HseshCharsets.default
    private val environment = EnvironmentImpl(null)

    private fun createCommand(args: List<String>): DefaultCommand {
        return DefaultCommand(args, environment)
    }

    @Test
    fun `test run correct without input`() {
        val echo = createCommand(listOf("echo", "3", "3"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = echo.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3 3${System.lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test run correct with input`() {
        val cat = createCommand(listOf("cat"))
        val input = ByteArrayInputStream("123${System.lineSeparator()}".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("123${System.lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test run correct with input but command don't need it`() {
        val echo = createCommand(listOf("echo", "3"))
        val input = ByteArrayInputStream("123${System.lineSeparator()}".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = echo.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3${System.lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test run with invalid input`() {
        val cat = createCommand(listOf("cat"))
        val input = object : InputStream() {
            override fun read(): Int {
                throw IOException("Hi")
            }
        }
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("Hi${System.lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test run with invalid output`() {
        val cat = createCommand(listOf("cat"))
        val input = ByteArrayInputStream("123${System.lineSeparator()}".toByteArray(charset))
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test run incorrect`() {
        val sleep = createCommand(listOf("sleep", "-"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = sleep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test run not existing command`() {
        val command = createCommand(listOf("AoAoA", "3"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = command.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test run correct with System in as input`() {
        val wc = createCommand(listOf("echo", "3"))
        val input = System.`in`
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3${System.lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }
}
