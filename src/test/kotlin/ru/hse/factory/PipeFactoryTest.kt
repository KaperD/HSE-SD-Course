package ru.hse.factory

import org.junit.jupiter.api.Test
import ru.hse.charset.HseshCharsets
import ru.hse.command.CatCommand
import ru.hse.command.EchoCommand
import ru.hse.command.ExitCommand
import ru.hse.command.PwdCommand
import ru.hse.environment.EnvironmentImpl
import ru.hse.testExecutable
import java.lang.System.lineSeparator
import java.nio.charset.Charset

class PipeFactoryTest {
    private val charset: Charset = HseshCharsets.default

    private fun createPipeFactory(): PipeFactory = PipeFactoryImpl()

    private fun createCommandFactory(): CommandFactory {
        val factory = CommandFactoryImpl(EnvironmentImpl(null))
        factory.registerCommand("echo") { EchoCommand(it) }
        factory.registerCommand("cat") { CatCommand(it) }
        factory.registerCommand("exit") { ExitCommand() }
        factory.registerCommand("pwd") { PwdCommand(it) }
//        factory.registerCommand("wc") { WcCommand(it) }
        return factory
    }

    private val pipeFactory = createPipeFactory()
    private val commandFactory = createCommandFactory()

    @Test
    fun `test correct not exit pipe one command`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val pipe = pipeFactory.create(listOf(echo))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "3${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test correct not exit pipe multiple commands`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc"))
        val pipe = pipeFactory.create(listOf(echo, wc))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "1 1 4${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test correct not exit pipe multiple commands with input`() {
        val wc = commandFactory.create(listOf("wc"))
        val cat = commandFactory.create(listOf("cat"))
        val pipe = pipeFactory.create(listOf(wc, cat))
        testExecutable(
            pipe,
            input = "123${lineSeparator()}",
            expectedOutput = "1 1 4${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test correct exit pipe multiple commands exit last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc"))
        val exit = commandFactory.create(listOf("exit"))
        val pipe = pipeFactory.create(listOf(echo, wc, exit))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "Bye${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = true
        )
    }

    @Test
    fun `test correct exit pipe multiple commands exit not last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val wc = commandFactory.create(listOf("wc"))
        val exit = commandFactory.create(listOf("exit"))
        val pipe = pipeFactory.create(listOf(echo, exit, wc))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "Bye${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = true
        )
    }

    @Test
    fun `test incorrect exit pipe multiple commands exit last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val cat = commandFactory.create(listOf("cat", "AoAoA"))
        val exit = commandFactory.create(listOf("exit"))
        val pipe = pipeFactory.create(listOf(echo, cat, exit))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "",
            expectedError = "cat: AoAoA: No such file or directory${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }

    @Test
    fun `test incorrect exit pipe multiple commands exit not last`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val exit = commandFactory.create(listOf("exit"))
        val command = commandFactory.create(listOf("AoAoA"))
        val pipe = pipeFactory.create(listOf(echo, exit, command))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "Bye${lineSeparator()}",
            expectedError = "",
            expectedIsZeroExitCode = true,
            expectedNeedExit = true
        )
    }

    @Test
    fun `test incorrect not exit pipe multiple commands`() {
        val echo = commandFactory.create(listOf("echo", "3"))
        val pwd = commandFactory.create(listOf("pwd", "3"))
        val pipe = pipeFactory.create(listOf(echo, pwd))
        testExecutable(
            pipe,
            input = "",
            expectedOutput = "",
            expectedError = "pwd: too many arguments${lineSeparator()}",
            expectedIsZeroExitCode = false,
            expectedNeedExit = false
        )
    }
}
