package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.testExecutable
import ru.hse.utils.trimIndentCrossPlatform
import ru.hse.utils.trimMarginCrossPlatform
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.lang.System.lineSeparator
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class CatCommandTest {
    private fun createCatCommand(args: List<String>): Executable {
        return CatCommand(args)
    }

    @Test
    fun `test empty args`() {
        val cat = createCatCommand(emptyList())
        testExecutable(
            cat,
            input = "Hello ${lineSeparator()} World",
            expectedOutput = "Hello ${lineSeparator()} World",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test file not exist`() {
        val cat = createCatCommand(listOf("AoAoA"))
        testExecutable(
            cat,
            input = "",
            expectedOutput = "",
            expectedError = "cat: AoAoA: No such file or directory${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test file not exist multiple files`() {
        val cat = createCatCommand(listOf("AoAoA", "src/test/resources/cat.txt"))
        testExecutable(
            cat,
            input = "",
            expectedOutput = " Hello${lineSeparator()}",
            expectedError = "cat: AoAoA: No such file or directory${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test closed output`() {
        val cat = createCatCommand(listOf("src/test/resources/cat.txt"))
        val file = File.createTempFile("test", null)
        file.deleteOnExit()
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(file, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("cat: IO problem: Stream closed${lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test closed output with input`() {
        val cat = createCatCommand(emptyList())
        val file = File.createTempFile("test", null)
        file.deleteOnExit()
        file.writeBytes("Hello${lineSeparator()}".toByteArray(HseshCharsets.default))
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(file, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("cat: IO problem: Stream closed${lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test unreadable file`() {
        val unreadable = File("src/test/resources/unreadable.txt")
        unreadable.createNewFile()
        unreadable.deleteOnExit()
        if (!unreadable.setReadable(false)) {
            return
        }
        val cat = createCatCommand(listOf("src/test/resources/cat.txt", unreadable.path))
        testExecutable(
            cat,
            input = "",
            expectedOutput = " Hello${lineSeparator()}",
            expectedError = "cat: src/test/resources/unreadable.txt: Permission denied${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test not regular file`() {
        val cat = createCatCommand(listOf("src"))
        testExecutable(
            cat,
            input = "",
            expectedOutput = "",
            expectedError = "cat: src: Is not a regular file${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }

    @ParameterizedTest
    @MethodSource("catData")
    fun `test cat existing files`(args: List<String>, expectedOutput: String) {
        val cat = createCatCommand(args)
        testExecutable(
            cat,
            input = "",
            expectedOutput = expectedOutput,
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    companion object {
        @JvmStatic
        fun catData() = listOf(
            Arguments.of(
                listOf("src/test/resources/cat.txt"),
                """
                    | Hello
                    |
                """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf("src/test/resources/cat2.txt"),
                """
                    wor ld

                    !


                """.trimIndentCrossPlatform()
            ),
            Arguments.of(
                listOf(
                    "src/test/resources/cat.txt",
                    "src/test/resources/cat2.txt"
                ),
                """
                    | Hello
                    |wor ld
                    |
                    |!
                    |
                    |
                """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf(
                    "src/test/resources/cat2.txt",
                    "src/test/resources/cat.txt"
                ),
                """
                    |wor ld
                    |
                    |!
                    |
                    | Hello
                    |
                """.trimMarginCrossPlatform()
            ),
            Arguments.of(
                listOf(
                    "src/test/resources/cat.txt",
                    "src/test/resources/cat.txt",
                    "src/test/resources/cat.txt"
                ),
                """
                    | Hello
                    | Hello
                    | Hello
                    |
                """.trimMarginCrossPlatform()
            ),
        )
    }
}
