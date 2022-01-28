package ru.hse.factory

import org.junit.jupiter.api.Test
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.test.*

@Ignore
class CommandFactoryTest {
    private val charset: Charset = StandardCharsets.UTF_8

    private fun createCommandFactory(): CommandFactory {
        TODO("Return object when it's ready")
    }

    private val factory = createCommandFactory()

    @Test
    fun `test creating registered command`() {
//        factory.registerCommand("wc", { WcCommand(it) })
        val cat = factory.create(listOf("wc", "file"))
        assertIs<Executable>(cat) // Заменить на WcCommand

        //        factory.registerCommand("exit", { ExitCommand(it) })
        val exit = factory.create(listOf("exit"))
        assertIs<Executable>(exit) // Заменить на ExitCommand
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
        assertEquals("3 3\n", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run correct with input`() {
        val wc = factory.create(listOf("cat"))
        val input = ByteArrayInputStream("123\n".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("123\n", output.toString())
        assertEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run incorrect`() {
        val pwd = factory.create(listOf("pwd", "3"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pwd.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("pwd: too many arguments\n", error.toString(charset))
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
        assertEquals("Put here a message", error.toString(charset))
    }
}
