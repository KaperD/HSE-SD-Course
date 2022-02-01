package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.executable.Executable
import ru.hse.testExecutable
import java.lang.System.lineSeparator

class PwdCommandTest {
    private fun createPwdCommand(args: List<String>): Executable = PwdCommand(args)

    @Test
    fun `test correct pwd call`() {
        val pwd = createPwdCommand(emptyList())
        testExecutable(
            pwd,
            input = "",
            expectedOutput = System.getProperty("user.dir") + lineSeparator(),
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test incorrect pwd call`() {
        val pwd = createPwdCommand(listOf("some"))
        testExecutable(
            pwd,
            input = "",
            expectedOutput = "",
            expectedError = "pwd: too many arguments${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }
}
