package ru.hse.factory

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import kotlin.test.*

@Ignore
class PipeFactoryTest {
    private val charset: Charset = HseshCharsets.default

    private fun createPipeFactory(): PipeFactory {
        TODO("Return object when it's ready")
    }

    private fun createCommandFactory(): CommandFactory {
        TODO("Return object when it's ready")
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
        assertEquals("3\n", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct not exit pipe multiple commands`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc"))
        val pipe = pipeFactory.create(listOf(echo, wc))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("1 1 4\n", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct not exit pipe multiple commands with input`() {
        val wc = commandFactory.create(listOf("wc"))
        val cat = commandFactory.create(listOf("cat"))
        val pipe = pipeFactory.create(listOf(wc, cat))
        val input = ByteArrayInputStream("123\n".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("1 1 4\n", output.toString(charset))
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
        assertEquals(0, output.size())
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
        assertEquals(0, output.size())
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
        assertEquals("Put here a message", error.toString(charset))
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
        assertEquals(0, output.size())
        assertEquals(0, error.size())
    }

    @Test
    fun `test incorrect not exit pipe multiple commands`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc", "3"))
        val pipe = pipeFactory.create(listOf(echo, wc))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pipe.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("pwd: too many arguments\n", error.toString(charset))
    }
}
