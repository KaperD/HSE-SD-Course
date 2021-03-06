package ru.hse.command

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.System.lineSeparator
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EchoCommandTest {
    private fun createEchoCommand(args: List<String>): Executable {
        return EchoCommand(args)
    }

    @ParameterizedTest
    @MethodSource("echoData")
    fun `test valid assignment`(args: List<String>, expectedOutput: String) {
        val echo: Executable = createEchoCommand(args)
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = echo.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expectedOutput, output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    companion object {
        @JvmStatic
        fun echoData() = listOf(
            Arguments.of(listOf("Hello", "world", "!"), "Hello world !${lineSeparator()}"),
            Arguments.of(listOf<String>(), lineSeparator()),
            Arguments.of(listOf("", "", "!"), "  !${lineSeparator()}"),
            Arguments.of(listOf("!", "", ""), "!  ${lineSeparator()}"),
            Arguments.of(listOf("", "", "!", "", ""), "  !  ${lineSeparator()}"),
        )
    }
}
