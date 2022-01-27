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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class CatCommandTest {
    private val charset: Charset = StandardCharsets.UTF_8

    private fun createCatCommand(args: List<String>): Executable {
        TODO("Return object when it's ready")
    }

    @Test
    fun `test empty args`() {
        val cat = createCatCommand(emptyList())
        assertFalse(cat.isExit())
        val input = ByteArrayInputStream("Hello \n World".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        assertEquals(0, cat.run(input, output, error))
        assertEquals("Hello \n World", output.toString(charset))
        assertEquals(0, error.size())
    }

    @Test
    fun `test file not exist`() {
        val cat = createCatCommand(listOf("AoAoA"))
        assertFalse(cat.isExit())
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        assertNotEquals(0, cat.run(input, output, error))
        assertEquals(0, output.size())
        assertEquals("cat: AoAoA: No such file or directory\n", error.toString(charset))
    }

    @ParameterizedTest
    @MethodSource("catData")
    fun `test cat existing files`(args: List<String>, expectedOutput: String) {
        val cat: Executable = createCatCommand(args)
        assertFalse(cat.isExit())
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        assertEquals(0, cat.run(input, output, error))
        assertEquals(expectedOutput, output.toString(StandardCharsets.UTF_8))
        assertEquals(0, error.size())
    }

    companion object {
        @JvmStatic
        fun catData() = listOf(
            Arguments.of(listOf("cat.txt"), " Hello\n"),
            Arguments.of(listOf("cat2.txt"), "wor ld\n \n!\n\n"),
            Arguments.of(listOf("cat.txt", "cat2.txt"), " Hello\nwor ld\n \n!\n\n"),
            Arguments.of(listOf("cat2.txt", "cat.txt"), "wor ld\n \n!\n\n Hello\n"),
            Arguments.of(listOf("cat.txt", "cat.txt", "cat.txt"), " Hello\n Hello\n Hello\n"),
        )
    }
}
