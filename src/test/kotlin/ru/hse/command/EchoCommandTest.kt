package ru.hse.command

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Ignore
class EchoCommandTest {
    private fun createEchoCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
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
        assertEquals(expectedOutput, output.toString(StandardCharsets.UTF_8))
        assertEquals(0, error.size())
    }

    companion object {
        @JvmStatic
        fun echoData() = listOf(
            Arguments.of(listOf("Hello", "world", "!"), "Hello world !\n"),
            Arguments.of(listOf<String>(), "\n"),
            Arguments.of(listOf("", "", "!"), "  !\n"),
            Arguments.of(listOf("!", "", ""), "!  \n"),
            Arguments.of(listOf("", "", "!", "", ""), "  !  \n"),
        )
    }
}
