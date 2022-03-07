package ru.hse.factory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.hse.charset.HseshCharsets
import ru.hse.command.*
import ru.hse.environment.EnvironmentImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.IllegalArgumentException
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.*

class PipeFactoryTest {
    private val charset: Charset = HseshCharsets.default

    private fun createPipeFactory(): PipeFactory = PipeFactoryImpl()

    private fun createCommandFactory(): CommandFactory {
        val environment = EnvironmentImpl(EnvironmentImpl(null, System.getenv()))
        val factory = CommandFactoryImpl(environment)
        factory.registerCommand("echo") { EchoCommand(it) }
        factory.registerCommand("cat") { CatCommand(environment, it) }
        factory.registerCommand("exit") { ExitCommand() }
        factory.registerCommand("pwd") { PwdCommand(environment, it) }
        factory.registerCommand("wc") { WcCommand(environment, it) }
        return factory
    }

    private val pipeFactory = createPipeFactory()
    private val commandFactory = createCommandFactory()

    @Test
    fun `test correct not exit pipe one command`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val pipe = pipeFactory.create(listOf(echo))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct not exit pipe multiple commands`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val echoOutputSize = "3${lineSeparator()}".toByteArray(HseshCharsets.default).size
        val wc = commandFactory.create(listOf("wc"))
        val pipe = pipeFactory.create(listOf(echo, wc))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("      1       1       $echoOutputSize${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct not exit pipe multiple commands with input`() {
        val wc = commandFactory.create(listOf("wc"))
        val cat = commandFactory.create(listOf("cat"))
        val pipe = pipeFactory.create(listOf(wc, cat))
        val inputBytes = "123${lineSeparator()}".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("      1       1       ${inputBytes.size}${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct exit pipe multiple commands exit last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc"))
        val exit = commandFactory.create(listOf("exit"))
        val pipe = pipeFactory.create(listOf(echo, wc, exit))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertTrue(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Bye${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct exit pipe multiple commands exit not last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc"))
        val exit = commandFactory.create(listOf("exit"))
        val pipe = pipeFactory.create(listOf(echo, exit, wc))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertTrue(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Bye${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test incorrect exit pipe multiple commands exit last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val command = commandFactory.create(listOf("AoAoA"))
        val exit = commandFactory.create(listOf("exit"))
        val pipe = pipeFactory.create(listOf(echo, command, exit))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test incorrect exit pipe multiple commands exit not last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val exit = commandFactory.create(listOf("exit"))
        val command = commandFactory.create(listOf("AoAoA"))
        val pipe = pipeFactory.create(listOf(echo, exit, command))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertTrue(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Bye${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test incorrect not exit pipe multiple commands`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val pwd = commandFactory.create(listOf("pwd", "3"))
        val pipe = pipeFactory.create(listOf(echo, pwd))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("pwd: too many arguments${lineSeparator()}", error.toString(charset))
    }

    @Test
    fun `test creating pipe with empty commands`() {
        assertThrows<IllegalArgumentException> {
            pipeFactory.create(emptyList())
        }
    }
}
