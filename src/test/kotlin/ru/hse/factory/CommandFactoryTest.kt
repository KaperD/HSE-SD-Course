package ru.hse.factory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.hse.charset.HseshCharsets
import ru.hse.command.EchoCommand
import ru.hse.command.ExitCommand
import ru.hse.environment.EnvironmentImpl
import java.io.*
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.*

class CommandFactoryTest {
    private val charset: Charset = HseshCharsets.default

    private fun createCommandFactory(): CommandFactory {
        return CommandFactoryImpl(EnvironmentImpl(null, mapOf("a" to "3")))
    }

    private val factory = createCommandFactory()

    @Test
    fun `test creating registered command`() {
//        factory.registerCommand("wc", { WcCommand(it) })
//        val wc = factory.create(listOf("wc", "file"))
//        assertIs<WcCommand>(wc)

        factory.registerCommand("exit") { ExitCommand() }
        val exit = factory.create(listOf("exit"))
        assertIs<ExitCommand>(exit)
    }

    @Test
    fun `test creating not registered command and run correct without input`() {
        val echo = factory.create(listOf("echo", "3", "3"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = echo.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3 3${lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run correct with input`() {
        val wc = factory.create(listOf("cat"))
        val input = ByteArrayInputStream("123${lineSeparator()}".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("123${lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run correct with input but command don't need it`() {
        val wc = factory.create(listOf("echo", "3"))
        val input = ByteArrayInputStream("123${lineSeparator()}".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3${lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run with invalid input`() {
        val cat = factory.create(listOf("cat"))
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
        assertEquals("Hi${lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test creating not registered command and run with invalid output`() {
        val cat = factory.create(listOf("cat"))
        val input = ByteArrayInputStream("123${lineSeparator()}".toByteArray(charset))
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run incorrect`() {
        val pwd = factory.create(listOf("sleep", "-"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pwd.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test creating not existing command`() {
        val command = factory.create(listOf("AoAoA", "3"))
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
    fun `test creating not registered command and run correct with System in as input`() {
        val wc = factory.create(listOf("echo", "3"))
        val input = System.`in`
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3${lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test registering existing command`() {
        factory.registerCommand("MyCommand") { ExitCommand() }
        assertThrows<IllegalStateException> { factory.registerCommand("MyCommand") { EchoCommand(it) } }
    }

    @Test
    fun `test registering command from no tokens`() {
        assertThrows<IllegalArgumentException> { factory.create(emptyList()) }
    }
}
