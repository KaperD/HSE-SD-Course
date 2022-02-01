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
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.System.lineSeparator
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
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        var res = executor.execute("  b=0  ", null, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("0", environment.get("b"))

        res = executor.execute("  c='${'$'}b='${'$'}b  ", null, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("${'$'}b=0", environment.get("c"))
    }

    @Test
    fun `test pipe expressions not exit`() {
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = executor.execute(" echo  3  |cat| cat", null, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("3${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test pipe expressions not exit with input`() {
        val file = File.createTempFile("test", null)
        file.deleteOnExit()
        file.writeBytes("Hello".toByteArray(HseshCharsets.default))
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        val res = executor.execute(" cat  | cat | cat", file, output, error)
        assertEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals("Hello", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test pipe expressions exit`() {
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        var res = executor.execute(" echo  3  |exit| cat", null, output, error)
        assertEquals(0, res.exitCode)
        assertTrue(res.needExit)
        assertEquals("Bye${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())

        output.reset()
        res = executor.execute("exit", null, output, error)
        assertEquals(0, res.exitCode)
        assertTrue(res.needExit)
        assertEquals("Bye${lineSeparator()}", output.toString(HseshCharsets.default))
        assertEquals(0, error.size())
    }

    @Test
    fun `test wrong expression`() {
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        var res = executor.execute("echo 3 | echo '", null, output, error)
        assertNotEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals(0, output.size())
        assertEquals("Invalid expression for tokenization${lineSeparator()}", error.toString(HseshCharsets.default))

        error.reset()
        res = executor.execute("echo 3 | | cat", null, output, error)
        assertNotEquals(0, res.exitCode)
        assertFalse(res.needExit)
        assertEquals(0, output.size())
        assertEquals("There is empty command in pipe${lineSeparator()}", error.toString(HseshCharsets.default))
    }
}
