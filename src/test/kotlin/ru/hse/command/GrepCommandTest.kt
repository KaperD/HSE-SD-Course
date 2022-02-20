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
import kotlin.text.lines

class GrepCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createGrepCommand(args: List<String>): Executable = GrepCommand(args)

    private fun testGrepSuccess(
        args: List<String>,
        matchedStrings: List<String>
    ) {
        val grep = createGrepCommand(args)
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(
            matchedStrings.joinToString(lineSeparator(), postfix = lineSeparator()),
            output.toString(charset)
        )
        assertEquals(0, error.size())
    }

    @Test
    fun `test without flags`() = testGrepSuccess(
        listOf(fileShort, "hello"),
        listOf("- What's up with you, hello? You look sad...")
    )

    companion object {
        private val fileShort = "src/test/resources/grep_short.txt"
        private val fileCyrillic = "src/test/resources/grep_cyrillic.txt"
        private val fileAllStar = "src/test/resources/grep_all_star.txt"
        private val fileWithSpace = "src/test/resources/grep with space.txt"
    }
}
