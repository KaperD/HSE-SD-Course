package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.environment.Environment
import ru.hse.environment.EnvironmentImpl
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class CdCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createCdCommand(args: List<String>): Pair<Executable, Environment> {
        val environment = EnvironmentImpl(null)
        return CdCommand(environment, args) to environment
    }

    @Test
    fun `test correct cd call empty arguments`() {
        val (cd, env) = createCdCommand(emptyList())
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cd.run(input, output, error)
        val expected = Path(System.getProperty("user.home"))
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expected, env.workDirectory)
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct cd call simple argument`() {
        val (cd, env) = createCdCommand(listOf("src/main/kotlin"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cd.run(input, output, error)
        val expected = Path("src/main/kotlin").toRealPath()
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expected, env.workDirectory)
        assertEquals(0, error.size())
    }

    @Test
    fun `test correct cd call i'll be back`() {
        val environment = EnvironmentImpl(null)
        val cd = CdCommand(environment, listOf(".."))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        cd.run(input, output, error)
        val newCd = CdCommand(environment, listOf("HSE-SD-Course"))
        val res = newCd.run(input, output, error)
        val expected = Path("").toRealPath()
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expected, environment.workDirectory.toRealPath())
        assertEquals(0, error.size())
    }

    @Test
    fun `test incorrect cd call too many args`() {
        val (cd, env) = createCdCommand(listOf("src", "main"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cd.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(env.workDirectory.toRealPath(), Path("").toRealPath())
        assertNotEquals(0, res.exitCode)
        assertEquals("cd: too many arguments\n", error.toString(charset))
    }

    @Test
    fun `test incorrect cd call no such directory`() {
        val (cd, env) = createCdCommand(listOf("МируМир/"))
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cd.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(env.workDirectory.toRealPath(), Path("").toRealPath())
        assertNotEquals(0, res.exitCode)
        assertEquals("cd: no such directory: МируМир/\n", error.toString(charset))
    }
}
