package ru.hse.command

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.executable.Executable
import ru.hse.testExecutable
import java.lang.System.lineSeparator

class ExitCommandTest {
    private fun createExitCommand(@Suppress("unused") args: List<String>): Executable = ExitCommand()

    @ParameterizedTest
    @MethodSource("exitData")
    fun `test exit`(args: List<String>) {
        val exit: Executable = createExitCommand(args)
        testExecutable(
            exit,
            input = "",
            expectedOutput = "Bye${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = true
        )
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
