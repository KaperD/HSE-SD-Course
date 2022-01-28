package ru.hse.command

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createFile
import kotlin.io.path.deleteExisting
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class CatCommandTest {
    private val charset: Charset = HseshCharsets.default

    private fun createCatCommand(args: List<String>): Executable {
        return CatCommand(args)
    }

    @Test
    fun `test empty args`() {
        val cat = createCatCommand(emptyList())

        val input = ByteArrayInputStream("Hello \n World".toByteArray(charset))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertEquals(0, res.exitCode)
        assertEquals("Hello \n World", output.toString(charset))
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
        assertEquals("cat: AoAoA: No such file or directory\n", error.toString(charset))
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
        assertEquals(" Hello\n", output.toString(charset))
        assertEquals("cat: AoAoA: No such file or directory\n", error.toString(charset))
    }

    @Test
    fun `test unreadable file`() {
        val unreadable = Path.of("src/test/resources/unreadable.txt")
            .createFile(PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("-wx-wx-wx")))
        val cat = createCatCommand(listOf("src/test/resources/cat.txt", "src/test/resources/unreadable.txt"))
        val input = ByteArrayInputStream(ByteArray(0))
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = cat.run(input, output, error)
        assertFalse(res.needExit)
        assertNotEquals(0, res.exitCode)
        assertEquals(" Hello\n", output.toString(charset))
        assertEquals("cat: src/test/resources/unreadable.txt: Permission denied\n", error.toString(charset))
        unreadable.deleteExisting()
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
        assertEquals("cat: src: Is not a regular file\n", error.toString(charset))
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
            Arguments.of(listOf("src/test/resources/cat.txt"), " Hello\n"),
            Arguments.of(listOf("src/test/resources/cat2.txt"), "wor ld\n\n!\n\n"),
            Arguments.of(listOf(
                "src/test/resources/cat.txt",
                "src/test/resources/cat2.txt"
            ), " Hello\nwor ld\n\n!\n\n"),
            Arguments.of(listOf(
                "src/test/resources/cat2.txt",
                "src/test/resources/cat.txt"
            ), "wor ld\n\n!\n\n Hello\n"),
            Arguments.of(listOf(
                "src/test/resources/cat.txt",
                "src/test/resources/cat.txt",
                "src/test/resources/cat.txt"
            ), " Hello\n Hello\n Hello\n"),
        )
    }
}
