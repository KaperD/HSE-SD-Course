package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.System.lineSeparator
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExitCommandTest {
    private fun createExitCommand(): Executable = ExitCommand()

    @Test
    fun `test exit`() {
        val exit: Executable = createExitCommand()
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = exit.run(input, output, error)
        assertTrue(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Bye${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }
}
