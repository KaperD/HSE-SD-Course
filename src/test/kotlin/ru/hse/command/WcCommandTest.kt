package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.utils.trimIndentCrossPlatform
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

@Ignore
class WcCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createWcCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
    }

    @Test
    fun `test empty args`() {
        val wc = createWcCommand(emptyList())
        val input = ByteArrayInputStream("123 ы${lineSeparator()}".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("1 2 7${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test empty args empty input`() {
        val wc = createWcCommand(emptyList())
        val input = ByteArrayInputStream("".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("0 0 0${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test file not exist`() {
        val wc = createWcCommand(listOf("AoAoA"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("wc: AoAoA: No such file or directory${lineSeparator()}", error.toString(charset))
    }

    @ParameterizedTest
    @MethodSource("wcData")
    fun `test wc existing files`(args: List<String>, expectedOutput: String) {
        val wc: Executable = createWcCommand(args)
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expectedOutput, output.toString(charset))
        assertEquals(0, error.size())
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
