package ru.hse.command

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExitCommandTest {
    private fun createExitCommand(@Suppress("unused") args: List<String>): Executable = ExitCommand()

    @ParameterizedTest
    @MethodSource("exitData")
    fun `test exit`(args: List<String>) {
        val exit: Executable = createExitCommand(args)
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = exit.run(input, output, error)
        assertTrue(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Bye\n", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    companion object {
        @JvmStatic
        fun exitData() = listOf(
            Arguments.of(listOf<String>()),
            Arguments.of(listOf("")),
            Arguments.of(listOf("Hello", "exit")),
        )
    }
}
