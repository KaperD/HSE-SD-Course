package ru.hse.executor

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.command.EchoCommand
import ru.hse.command.ExitCommand
import ru.hse.environment.EnvironmentImpl
import ru.hse.factory.CommandFactoryImpl
import ru.hse.factory.PipeFactoryImpl
import ru.hse.parser.AssignmentParser
import ru.hse.parser.PipeParser
import ru.hse.splitter.PipeSplitterImpl
import ru.hse.substitutor.SubstitutorImpl
import ru.hse.tokenizer.TokenizerImpl
import ru.hse.validator.VarNameValidatorImpl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ExpressionExecutorTest {
    private val environment = EnvironmentImpl(null, mapOf("a" to "3", "b" to "4"))

    private fun createExecutor(): ExpressionExecutor {
        val varNameValidator = VarNameValidatorImpl()
        val tokenizer = TokenizerImpl()
        val pipeSplitter = PipeSplitterImpl()
        val substitutor = SubstitutorImpl(environment, varNameValidator)
        val assignmentParser = AssignmentParser(varNameValidator, tokenizer, substitutor)
        val pipeParser = PipeParser(tokenizer, pipeSplitter, substitutor)
        val commandFactory = CommandFactoryImpl(environment)
        commandFactory.registerCommand("exit") { ExitCommand() }
        commandFactory.registerCommand("echo") { EchoCommand(it) }
        val pipeFactory = PipeFactoryImpl()
        return ExpressionExecutorImpl(assignmentParser, pipeParser, environment, commandFactory, pipeFactory)
    }

    private val executor = createExecutor()

    @Test
    fun `test assignment expressions`() {
        val input = ByteArrayInputStream(ByteArray(0))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        var res = executor.execute("  b=0  ", input, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("0", environment.get("b"))

        res = executor.execute("  c='${'$'}b='${'$'}b  ", input, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("${'$'}b=0", environment.get("c"))
    }

    @Test
    fun `test pipe expressions not exit`() {
        val input = InputStream.nullInputStream()
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = executor.execute(" echo  3  |cat| cat", input, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("3\n", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test pipe expressions not exit with input`() {
        val input = ByteArrayInputStream("Hello".toByteArray(HseshCharsets.default))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = executor.execute(" cat  | cat | cat", input, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("Hello", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test pipe expressions exit`() {
        val input = InputStream.nullInputStream()
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        var res = executor.execute(" echo  3  |exit| cat", input, output, error)
        assertEquals(0, res.exitCode)
        assertTrue(res.needExit)
        assertEquals("Bye\n", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())

        output.reset()
        res = executor.execute("exit", input, output, error)
        assertEquals(0, res.exitCode)
        assertTrue(res.needExit)
        assertEquals("Bye\n", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test wrong expression`() {
        val input = InputStream.nullInputStream()
        input.close()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        var res = executor.execute("echo 3 | echo '", input, output, error)
        assertNotEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals(0, output.size())
        assertEquals("Invalid expression for tokenization\n", error.toString(HseshCharsets.default))

        error.reset()
        res = executor.execute("echo 3 | | cat", input, output, error)
        assertNotEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals(0, output.size())
        assertEquals("There is empty command in pipe\n", error.toString(HseshCharsets.default))
    }
}
