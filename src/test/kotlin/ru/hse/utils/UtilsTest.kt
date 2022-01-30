package ru.hse.utils

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import java.io.ByteArrayOutputStream
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
        assertEquals("Hello\n", output.toString(HseshCharsets.default))
    }
}
