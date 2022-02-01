package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.environment.EnvironmentImpl
import ru.hse.testExecutable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class DefaultCommandTest {
    private val environment = EnvironmentImpl(null)

    @Test
    fun `test creating not registered command and run correct without input`() {
        val echo = DefaultCommand(listOf("echo", "3", "3"), environment)
        testExecutable(
            echo,
            input = "",
            expectedOutput = "3 3${System.lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test creating not registered command and run correct with input`() {
        val cat = DefaultCommand(listOf("cat"), environment)
        testExecutable(
            cat,
            input = "123${System.lineSeparator()}",
            expectedOutput = "123${System.lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test creating not registered command and run correct with input but command don't need it`() {
        val echo = DefaultCommand(listOf("echo", "3"), environment)
        testExecutable(
            echo,
            input = "123${System.lineSeparator()}",
            expectedOutput = "3${System.lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test creating not registered command and run with invalid input`() {
        val cat = DefaultCommand(listOf("cat"), environment)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(File("AoAoA"), output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run with invalid output`() {
        val echo = DefaultCommand(listOf("echo", "3"), environment)
        val file = File.createTempFile("test", null)
        file.deleteOnExit()
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = echo.run(file, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run incorrect`() {
        val sleep = DefaultCommand(listOf("sleep", "-"), environment)
        val file = File.createTempFile("test", null)
        file.deleteOnExit()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = sleep.run(file, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test creating not existing command`() {
        val command = DefaultCommand(listOf("AoAoA", "3"), environment)
        val file = File.createTempFile("test", null)
        file.deleteOnExit()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = command.run(file, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertNotEquals(0, error.size())
    }

    @Test
    fun `test creating not registered command and run correct with System in as input`() {
        val wc = DefaultCommand(listOf("echo", "3"), environment)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.runInheritInput(output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("3${System.lineSeparator()}", output.toString())
        assertEquals(0, error.size())
    }
}
