package ru.hse.command

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.executable.Executable
import ru.hse.testExecutable
import java.lang.System.lineSeparator

class EchoCommandTest {
    private fun createEchoCommand(args: List<String>): Executable {
        return EchoCommand(args)
    }

    @ParameterizedTest
    @MethodSource("echoData")
    fun `test echo`(args: List<String>, expectedOutput: String) {
        val echo: Executable = createEchoCommand(args)
        testExecutable(
            echo,
            input = "",
            expectedOutput = expectedOutput,
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
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
