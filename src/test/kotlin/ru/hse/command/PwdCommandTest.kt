package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PwdCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createPwdCommand(args: List<String>): Executable = PwdCommand(args)

    @Test
    fun `test correct pwd call`() {
        val pwd = createPwdCommand(emptyList())
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = pwd.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(System.getProperty("user.dir") + "\n", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test incorrect pwd call`() {
        val pwd = createPwdCommand(listOf("some"))
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
}
