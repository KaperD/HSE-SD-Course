package ru.hse.factory

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.command.CatCommand
import ru.hse.command.EchoCommand
import ru.hse.command.ExitCommand
import ru.hse.command.PwdCommand
import ru.hse.environment.EnvironmentImpl
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import kotlin.test.*

class CommandFactoryTest {
    private val charset: Charset = HseshCharsets.default

    private fun createCommandFactory(): CommandFactory {
        val factory = CommandFactoryImpl(EnvironmentImpl(null, mapOf()))
        factory.registerCommand("cat", ::CatCommand)
        factory.registerCommand("echo", ::EchoCommand)
        factory.registerCommand("pwd", ::PwdCommand)
        factory.registerCommand("exit") { ExitCommand() }
        return factory
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
        assertEquals(0, res.exitCode)
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
        assertEquals("Cannot run program \"AoAoA\": error=2, No such file or directory", error.toString(charset))
    }
}
