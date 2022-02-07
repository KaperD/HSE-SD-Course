package ru.hse.utils

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import java.io.ByteArrayOutputStream
import java.lang.System.lineSeparator
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun `test write to output stream`() {
        val output = ByteArrayOutputStream()
        output.write(null as String?)
        assertEquals(0, output.size())
        output.write("Hello")
        assertEquals("Hello", output.toString(HseshCharsets.default))
    }

    @Test
    fun `test writeln to output stream`() {
        val output = ByteArrayOutputStream()
        output.writeln(null as String?)
        assertEquals(0, output.size())
        output.writeln("Hello")
        assertEquals("Hello${lineSeparator()}", output.toString(HseshCharsets.default))
    }

    @Test
    fun `test trim margin cross platform`() {
        val str = """
            | Hello
            |
        """
        assertEquals(" Hello${lineSeparator()}", str.trimMarginCrossPlatform())
    }

    @Test
    fun `test trim indent cross platform`() {
        val str = """
            Hello

        """
        assertEquals("Hello${lineSeparator()}", str.trimIndentCrossPlatform())
    }
}
