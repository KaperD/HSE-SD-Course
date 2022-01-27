package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

@Ignore
class WcCommandTest {
    private val charset: Charset = StandardCharsets.UTF_8

    private fun createWcCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
    }

    @Test
    fun `test empty args`() {
        val wc = createWcCommand(emptyList())
        assertFalse(wc.isExit())
        val input = ByteArrayInputStream("123 ы\n".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        assertEquals(0, wc.run(input, output, error))
        assertEquals("1 2 7", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test file not exist`() {
        val wc = createWcCommand(listOf("AoAoA"))
        assertFalse(wc.isExit())
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        assertNotEquals(0, wc.run(input, output, error))
        assertEquals(0, output.size())
        assertEquals("wc: AoAoA: No such file or directory\n", error.toString(charset))
    }

    @ParameterizedTest
    @MethodSource("wcData")
    fun `test wc existing files`(args: List<String>, expectedOutput: String) {
        val wc: Executable = createWcCommand(args)
        assertFalse(wc.isExit())
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        assertEquals(0, wc.run(input, output, error))
        assertEquals(expectedOutput, output.toString(StandardCharsets.UTF_8))
        assertEquals(0, error.size())
    }

    companion object {
        @JvmStatic
        fun wcData() = listOf(
            Arguments.of(listOf("wc.txt"), "1 2 9 wc.txt\n"),
            Arguments.of(listOf("wc2.txt"), "3 2 17 wc2.txt\n"),
            Arguments.of(listOf("wc.txt", "wc2.txt"), "1 2 9 wc.txt\n3 2 17 wc2.txt\n"),
            Arguments.of(listOf("wc2.txt", "wc.txt"), "3 2 17 wc2.txt\n1 2 9 wc.txt\n"),
            Arguments.of(listOf("wc.txt", "wc.txt", "wc.txt"), "1 2 9 wc.txt\n1 2 9 wc.txt\n1 2 9 wc.txt\n"),
        )
    }
}
