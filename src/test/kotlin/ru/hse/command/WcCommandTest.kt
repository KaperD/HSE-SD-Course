package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.executable.Executable
import ru.hse.testExecutable
import ru.hse.utils.trimIndentCrossPlatform
import java.lang.System.lineSeparator
import kotlin.test.Ignore

@Ignore
class WcCommandTest {
    private fun createWcCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
    }

    @Test
    fun `test empty args`() {
        val wc = createWcCommand(emptyList())
        testExecutable(
            wc,
            input = "123 ы${lineSeparator()}",
            expectedOutput = "1 2 7${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test empty args empty input`() {
        val wc = createWcCommand(emptyList())
        testExecutable(
            wc,
            input = "",
            expectedOutput = "0 0 0${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test file not exist`() {
        val wc = createWcCommand(listOf("AoAoA"))
        testExecutable(
            wc,
            input = "",
            expectedOutput = "",
            expectedError = "wc: AoAoA: No such file or directory${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }

    @ParameterizedTest
    @MethodSource("wcData")
    fun `test wc existing files`(args: List<String>, expectedOutput: String) {
        val wc: Executable = createWcCommand(args)
        testExecutable(
            wc,
            input = "",
            expectedOutput = expectedOutput,
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    companion object {
        @JvmStatic
        fun wcData() = listOf(
            Arguments.of(
                listOf("wc.txt"),
                "1 2 9 wc.txt${lineSeparator()}"
            ),
            Arguments.of(
                listOf("wc2.txt"),
                "3 2 17 wc2.txt${lineSeparator()}"
            ),
            Arguments.of(
                listOf("wc.txt", "wc2.txt"),
                """
                    1 2 9 wc.txt
                    3 2 17 wc2.txt
                    4 4 26 total
                """.trimIndentCrossPlatform()
            ),
            Arguments.of(
                listOf("wc2.txt", "wc.txt"),
                """
                    3 2 17 wc2.txt
                    1 2 9 wc.txt
                    4 4 26 total
                """.trimIndentCrossPlatform()
            ),
            Arguments.of(
                listOf("wc.txt", "wc.txt", "wc.txt"),
                """
                    1 2 9 wc.txt
                    1 2 9 wc.txt
                    1 2 9 wc.txt
                    3 6 27 total
                """.trimIndentCrossPlatform()
            ),
            Arguments.of(
                listOf("wc.txt", "AoAoA", "wc.txt"),
                """
                    1 2 9 wc.txt
                    wc: AoAoA: No such file or directory
                    1 2 9 wc.txt
                    2 4 18 total
                """.trimIndentCrossPlatform()
            ),
        )
    }
}
