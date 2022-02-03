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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class WcCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createWcCommand(args: List<String>): Executable = WcCommand(args)

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

    @Test
    fun `test one not existing file`() {
        val wc = createWcCommand(listOf("src/test/resources/wc.txt", "AoAoA", "src/test/resources/wc.txt"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(
            """
                1 2 9 src/test/resources/wc.txt
                1 2 9 src/test/resources/wc.txt
                2 4 18 total

            """.trimIndentCrossPlatform(),
            output.toString(charset)
        )
        assertEquals("wc: AoAoA: No such file or directory${lineSeparator()}", error.toString(charset))
    }

    @ParameterizedTest
    @MethodSource("wcData")
    fun `test wc existing corrct files`(args: List<String>, expectedOutput: String) {
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
        private const val file1 = "src/test/resources/wc.txt"
        private const val file2 = "src/test/resources/wc2.txt"

        @JvmStatic
        fun wcData() = listOf(
            Arguments.of(
                listOf(file1),
                "1 2 9 src/test/resources/wc.txt${lineSeparator()}"
            ),
            Arguments.of(
                listOf(file2),
                "3 2 17 src/test/resources/wc2.txt${lineSeparator()}"
            ),
            Arguments.of(
                listOf(file1, file2),
                """
                    1 2 9 src/test/resources/wc.txt
                    3 2 17 src/test/resources/wc2.txt
                    4 4 26 total

                """.trimIndentCrossPlatform()
            ),
            Arguments.of(
                listOf(file2, file1),
                """
                    3 2 17 src/test/resources/wc2.txt
                    1 2 9 src/test/resources/wc.txt
                    4 4 26 total

                """.trimIndentCrossPlatform()
            ),
            Arguments.of(
                listOf(file1, file1, file1),
                """
                    1 2 9 src/test/resources/wc.txt
                    1 2 9 src/test/resources/wc.txt
                    1 2 9 src/test/resources/wc.txt
                    3 6 27 total

                """.trimIndentCrossPlatform()
            ),
        )
    }
}
