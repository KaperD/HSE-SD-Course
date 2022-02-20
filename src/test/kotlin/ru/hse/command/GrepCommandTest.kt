package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
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
    fun `test grep success`() {
        for ((filename, results) in grepResults) {
            for ((resultArgs, resultLines) in results) {
                val allArgs = resultArgs.toMutableList()
                allArgs.add(filename)
                testGrepSuccess(allArgs, resultLines)
            }
        }
    }

    companion object {
        private val grepResults = mapOf(
            "src/test/resources/grep_short.txt" to mapOf(
                listOf("hello") to listOf("- What's up with you, hello? You look sad..."),
                listOf("Hello") to listOf("- Hi, Hello!", "- Hello, Hi..."),
                listOf("hello", "-i") to listOf(
                    "- Hi, Hello!",
                    "- Hello, Hi...",
                    "- What's up with you, hello? You look sad...",
                    "- You sure, heLLO? Maybe you want some cheesecake? I know one place, it's just across this street"
                ),
                listOf("Hell", "-A", "2") to listOf(
                    "- Hi, Hello!",
                    "- Hello, Hi...",
                    "- What's up with you, hello? You look sad...",
                ),
            ),

            "src/test/resources/grep_cyrillic.txt" to emptyMap(),

            "src/test/resources/grep_all_star.txt" to emptyMap(),

            "src/test/resources/grep with space.txt" to emptyMap()
        )
    }
}
