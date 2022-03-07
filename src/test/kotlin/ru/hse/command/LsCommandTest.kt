package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.environment.EnvironmentImpl
import ru.hse.executable.Executable
import ru.hse.utils.writeln
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class LsCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createLsCommand(args: List<String>): Executable {
        val environment = EnvironmentImpl(null)
        return LsCommand(environment, args)
    }

    @Test
    fun `test correct ls call empty arguments`() {
        val ls = createLsCommand(emptyList())
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = ls.run(input, output, error)
        val expected = Path("").listDirectoryEntries()
            .filter {it.name[0] != '.'}
            .joinToString(separator = System.lineSeparator(), postfix = System.lineSeparator())
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expected, output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct ls call dir argument`() {
        val ls = createLsCommand(listOf("src/main/kotlin/ru/hse/"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = ls.run(input, output, error)
        val expected = Path("src/main/kotlin/ru/hse/").listDirectoryEntries()
            .map {it.name}
            .filter {it[0] != '.'}
            .joinToString(separator = System.lineSeparator(), postfix = System.lineSeparator())
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expected, output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct ls call file argument`() {
        val ls = createLsCommand(listOf("src/main/kotlin/ru/hse/command/LsCommand.kt"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = ls.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        val expected = ByteArrayOutputStream()
        expected.writeln("LsCommand.kt")
        assertEquals(expected.toString(charset), output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct ls call regex argument`() {
        val ls = createLsCommand(listOf("*.bat"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = ls.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        val expected = ByteArrayOutputStream()
        expected.writeln("gradlew.bat")
        assertEquals(expected.toString(charset), output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test incorrect ls call many args`() {
        val ls = createLsCommand(listOf("main/", "*"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = ls.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(1, res.exitCode)
        val expected = ByteArrayOutputStream()
        expected.writeln("ls: too many arguments")
        assertEquals(expected.toString(charset), error.toString(charset))
    }

    @Test
    fun `test incorrect ls call no such file`() {
        val ls = createLsCommand(listOf("/Миру-Мир.txt"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = ls.run(input, output, error)
        assertFalse(res.needExit)
        val expected = ByteArrayOutputStream()
        expected.writeln("ls: Миру-Мир.txt: No such file or directory")
        assertEquals(expected.toString(charset), error.toString(charset))
    }
}
