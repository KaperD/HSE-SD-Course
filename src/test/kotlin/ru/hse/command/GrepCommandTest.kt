package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.System.lineSeparator
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

    @Test
    fun `test grep empty input`() {
        val grep = createGrepCommand(listOf("hello"))
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals(0, error.size())
    }

    @Test
    fun `test grep missing pattern`() {
        val grep = createGrepCommand(listOf("-i"))
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertTrue(error.toString(charset).contains("pattern is not specified"))
    }

    @Test
    fun `test grep wrong pattern`() {
        val grep = createGrepCommand(listOf("(hello"))
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertTrue(error.toString(charset).contains("Pattern syntax error"))
    }

    @Test
    fun `test grep missing number of lines`() {
        val grep = createGrepCommand(listOf("hello", "-A"))
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertTrue(error.toString(charset).contains("Parse error: Missing argument for option: A"))
    }

    @Test
    fun `test grep wrong string number of lines`() {
        val grep = createGrepCommand(listOf("hello", "-A", "9j"))
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertTrue(error.toString(charset).contains("Argument error: -A argument should be a number, but found 9j"))
    }

    @Test
    fun `test grep wrong int number of lines`() {
        val grep = createGrepCommand(listOf("hello", "-A", "-1"))
        val inputBytes = "".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertTrue(error.toString(charset).contains("Argument error: -A argument should be non negative, but found -1"))
    }

    @Test
    fun `test grep complex pattern`() {
        val grep = createGrepCommand(listOf("""(#(.|\n)*\w{3,})"""))
        val inputBytes = ("#" + "a".repeat(10000)).toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertTrue(error.toString(charset).contains("Match error: pattern is too complex or line is too big"))
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
                listOf("Hell", "-A", "1") to listOf(
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
