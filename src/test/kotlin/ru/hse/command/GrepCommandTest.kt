package ru.hse.command

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.utils.trimIndentCrossPlatform
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
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
        assertTrue(error.toString(charset).contains("Pattern error"))
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

    @Test
    fun `test grep cyrillic case insensitive`() {
        val grep = createGrepCommand(listOf("-i", "минимальный"))
        val inputBytes = "Минимальный синтаксис grep".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Минимальный синтаксис grep${lineSeparator()}", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test unreadable file`() {
        val unreadable = File("src/test/resources/unreadable_for_grep.txt")
        unreadable.createNewFile()
        unreadable.deleteOnExit()
        if (!unreadable.setReadable(false)) {
            return
        }
        val grep = createGrepCommand(listOf("a", "src/test/resources/unreadable_for_grep.txt"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(
            "grep: src/test/resources/unreadable_for_grep.txt: Permission denied${lineSeparator()}",
            error.toString(charset)
        )
    }

    @Test
    fun `test -A flag with value bigger than the number of input lines`() {
        val grep = createGrepCommand(listOf("-A", "20", "There are"))
        val inputBytes = """
                             The 1st line
                             There are a few lines
                             ...
                             ...
                             ...
                             There are a few lines
                         """.trimIndentCrossPlatform().toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(
            """
                There are a few lines
                ...
                ...
                ...
                There are a few lines

            """.trimIndentCrossPlatform(),
            output.toString(charset)
        )
        assertEquals(0, error.size())
    }

    @Test
    fun `test -A flag integer overflow`() {
        val tooBigInt = "99999999999999999999999"
        val grep = createGrepCommand(listOf("-A", tooBigInt, "int"))
        val inputBytes = "integer overflow".toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(0, output.size())
        assertEquals(
            "grep: Argument error: -A argument should be a number, but found $tooBigInt${lineSeparator()}",
            error.toString(charset)
        )
    }

    @Test
    fun `test -w flag with regex like ^text$`() {
        val grep = createGrepCommand(listOf("-w", "^word$"))
        val inputBytes = """
                             other words word other words
                             other_words_word_other_words
                             ^word${'$'}
                         """.trimIndentCrossPlatform().toByteArray(charset)
        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = grep.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals(
            "".trimIndentCrossPlatform(),
            output.toString(charset)
        )
        assertEquals(0, error.size())
    }

    @Test
    fun `test grep without flags`() {
        val args = listOf("the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "they",
            "then",
            "either",
            "weather",
            "mother"
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with -w flag`() {
        val args = listOf("-w", "the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the"
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with -i flag`() {
        val args = listOf("-i", "the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "tHe",
            "they",
            "ThEy",
            "then",
            "ThEn",
            "either",
            "eItHeR",
            "weather",
            "wEaThEr",
            "mother",
            "MoThEr"
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with -A flag`() {
        val args = listOf("-A", "1", "the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "tHe",
            "they",
            "ThEy",
            "then",
            "ThEn",
            "either",
            "eItHeR",
            "weather",
            "wEaThEr",
            "mother",
            "MoThEr",
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with -w and -i flags`() {
        val args = listOf("-i",  "-w", "the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "tHe"
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with -w and -A flags`() {
        val args = listOf("-w", "-A", "1", "the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "tHe"
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with -i and -A flags`() {
        val args = listOf("-i", "-A", "1", "the", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "tHe",
            "This string does not contain th* word you are looking for",
            "they",
            "ThEy",
            "This string does not contain th* word you are looking for",
            "then",
            "ThEn",
            "This string does not contain th* word you are looking for",
            "either",
            "eItHeR",
            "This string does not contain th* word you are looking for",
            "weather",
            "wEaThEr",
            "This string does not contain th* word you are looking for",
            "mother",
            "MoThEr",
            "This string does not contain th* word you are looking for",
        )
        testGrepSuccess(args, result)
    }

    @Test
    fun `test grep with all flags`() {
        val args = listOf( "the", "-i",  "-w", "-A", "1", "src/test/resources/grep_flag_combinations.txt")
        val result = listOf(
            "the",
            "tHe",
            "This string does not contain th* word you are looking for"
        )
        testGrepSuccess(args, result)
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
            )
        )
    }
}
