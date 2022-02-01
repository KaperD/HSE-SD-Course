package ru.hse

import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun testExecutable(
    executable: Executable,
    input: String,
    expectedOutput: String,
    expectedError: String,
    expectedIsZeroExitCode: Boolean,
    expectedNeedExit: Boolean
) {
    val file = File.createTempFile("test", null)
    file.deleteOnExit()
    file.writeBytes(input.toByteArray(HseshCharsets.default))
    val output = ByteArrayOutputStream()
    val error = ByteArrayOutputStream()
    val res = executable.run(file, output, error)
    if (expectedIsZeroExitCode) {
        assertEquals(0, res.exitCode)
    } else {
        assertNotEquals(0, res.exitCode)
    }
    assertEquals(expectedOutput, output.toString(HseshCharsets.default))
    assertEquals(expectedError, error.toString(HseshCharsets.default))
    assertEquals(expectedNeedExit, res.needExit)
}
