package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.environment.EnvironmentImpl
import ru.hse.executable.Executable
import ru.hse.utils.trimIndentCrossPlatform
import ru.hse.utils.trimMarginCrossPlatform
import java.io.*
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class CatCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createCatCommand(args: List<String>): Executable {
        val environment = EnvironmentImpl(EnvironmentImpl(null, System.getenv()))
        return CatCommand(environment, args)
    }

    @Test
    fun `test empty args`() {
        val cat = createCatCommand(emptyList())

        val input = ByteArrayInputStream("Hello ${lineSeparator()} World".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Hello ${lineSeparator()} World", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test file not exist`() {
        val cat = createCatCommand(listOf("AoAoA"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("cat: AoAoA: No such file or directory${lineSeparator()}", error.toString(charset))
    }

    @Test
    fun `test file not exist multiple files`() {
        val cat = createCatCommand(listOf("AoAoA", "src/test/resources/cat.txt"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(" Hello${lineSeparator()}", output.toString(charset))
        assertEquals("cat: AoAoA: No such file or directory${lineSeparator()}", error.toString(charset))
    }

    @Test
    fun `test closed output`() {
        val cat = createCatCommand(listOf("src/test/resources/cat.txt"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals("cat: IO problem: Stream closed${lineSeparator()}", error.toString(HseshCharsets.default))
    }

    @Test
    fun `test closed output with input`() {
        val cat = createCatCommand(emptyList())
        val input = ByteArrayInputStream("Hello${lineSeparator()}".toByteArray(HseshCharsets.default))
        val output = OutputStream.nullOutputStream()
        output.close()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
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
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(" Hello${lineSeparator()}", output.toString(charset))
        assertEquals(
            "cat: src/test/resources/unreadable.txt: Permission denied${lineSeparator()}",
            error.toString(charset)
        )
    }

    @Test
    fun `test not regular file`() {
        val cat = createCatCommand(listOf("src"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals("cat: src: Is not a regular file${lineSeparator()}", error.toString(charset))
    }

    @ParameterizedTest
    @MethodSource("catData")
    fun `test cat existing files`(args: List<String>, expectedOutput: String) {
        val cat: Executable = createCatCommand(args)
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(expectedOutput, output.toString(charset))
        assertEquals(0, error.size())
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
