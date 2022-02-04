package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.utils.trimMarginCrossPlatform
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class WcCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createWcCommand(args: List<String>) = WcCommand(args, 7)

    @Test
    fun `test empty args`() {
        val wc = createWcCommand(emptyList())
        val inputBytes = "123 ы${lineSeparator()}".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("      1       2       ${inputBytes.size}${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test empty args empty input`() {
        val wc = createWcCommand(emptyList())
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("      0       0       ${inputBytes.size}${lineSeparator()}", output.toString(charset))
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
        val wc = createWcCommand(listOf(file1, "AoAoA", file1))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(
            """
               |       1       2       $file1BytesSize $file1
               |       1       2       $file1BytesSize $file1
               |       2       4      ${2 * file1BytesSize} total

            """.trimMarginCrossPlatform(),
            output.toString(charset)
        )
        assertEquals("wc: AoAoA: No such file or directory${lineSeparator()}", error.toString(charset))
    }

    @Test
    fun `test closed output`() {
        val cat = createWcCommand(listOf(file1))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("wc: IO problem: Stream closed${lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test closed output with input`() {
        val cat = createWcCommand(emptyList())
        val input = ByteArrayInputStream("Hello${lineSeparator()}".toByteArray(HseshCharsets.default))
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("wc: IO problem: Stream closed${lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test unreadable file`() {
        val unreadable = File("src/test/resources/unreadable1.txt")
        unreadable.createNewFile()
        unreadable.deleteOnExit()
        if (!unreadable.setReadable(false)) {
            return
        }
        val cat = createWcCommand(listOf(file1, unreadable.path))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("       1       2       $file1BytesSize $file1${lineSeparator()}", output.toString(charset))
        assertEquals(
            "wc: ${unreadable.path}: Permission denied${lineSeparator()}",
            error.toString(charset)
        )
    }

    @Test
    fun `test not regular file`() {
        val cat = createWcCommand(listOf("src"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("wc: src: Is not a regular file${lineSeparator()}", error.toString(charset))
    }

    @ParameterizedTest
    @MethodSource("wcData")
    fun `test wc existing correct files`(args: List<String>, expectedOutput: String) {
        val wc: Executable = createWcCommand(args)
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = wc.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode, error.toString(HseshCharsets.default))
        assertEquals(expectedOutput, output.toString(charset))
        assertEquals(0, error.size())
    }

    companion object {
        val file1: String = File("src/test/resources/wc.txt").path
        val file1BytesSize = File(file1).readBytes().size
        private val file2 = File("src/test/resources/wc2.txt").path
        private val file2BytesSize = File(file2).readBytes().size

        @JvmStatic
        fun wcData() = listOf(
            Arguments.of(
                listOf(file1),
                """
                    |       1       2       $file1BytesSize $file1

               """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf(file2),
                """
                    |       3       2      $file2BytesSize $file2

                """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf("src/test/resources/wc.txt", "src/test/resources/wc2.txt"),
                """
                   |       1       2       $file1BytesSize $file1
                   |       3       2      $file2BytesSize $file2
                   |       4       4      ${file1BytesSize + file2BytesSize} total

                """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf("src/test/resources/wc2.txt", "src/test/resources/wc.txt"),
                """
                   |       3       2      $file2BytesSize $file2
                   |       1       2       $file1BytesSize $file1
                   |       4       4      ${file1BytesSize + file2BytesSize} total

                """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf("src/test/resources/wc.txt", "src/test/resources/wc.txt", "src/test/resources/wc.txt"),
                """
                   |       1       2       $file1BytesSize $file1
                   |       1       2       $file1BytesSize $file1
                   |       1       2       $file1BytesSize $file1
                   |       3       6      ${3 * file1BytesSize} total

                """.trimMarginCrossPlatform()

            ),
        )
    }
}
