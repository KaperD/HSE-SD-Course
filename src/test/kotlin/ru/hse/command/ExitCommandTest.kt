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
import kotlin.test.assertTrue

@Ignore
class ExitCommandTest {
    private fun createExitCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
    }

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
        assertEquals("Bye\n", output.toString(StandardCharsets.UTF_8))
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
