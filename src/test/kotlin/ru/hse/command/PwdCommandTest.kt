package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

@Ignore
class PwdCommandTest {
    private fun createPwdCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
    }

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
        assertEquals(System.getProperty("user.dir") + "\n", output.toString(StandardCharsets.UTF_8))
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
        assertEquals("pwd: too many arguments\n", error.toString(StandardCharsets.UTF_8))
    }
}
