package ru.hse.factory

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.hse.command.DefaultCommand
import ru.hse.command.EchoCommand
import ru.hse.command.ExitCommand
import ru.hse.environment.EnvironmentImpl
import kotlin.test.assertIs

class CommandFactoryTest {
    private fun createCommandFactory(): CommandFactory {
        return CommandFactoryImpl(EnvironmentImpl(null, mapOf("a" to "3")))
    }

    private val factory = createCommandFactory()

    @Test
    fun `test creating registered command`() {
//        factory.registerCommand("wc", { WcCommand(it) })
//        val wc = factory.create(listOf("wc", "file"))
//        assertIs<WcCommand>(wc)

        factory.registerCommand("exit") { ExitCommand() }
        val exit = factory.create(listOf("exit"))
        assertIs<ExitCommand>(exit)
    }

    @Test
    fun `test creating not registered command`() {
        val sleep = factory.create(listOf("sleep", "1"))
        assertIs<DefaultCommand>(sleep)
    }

    @Test
    fun `test registering existing command`() {
        factory.registerCommand("MyCommand") { ExitCommand() }
        assertThrows<IllegalStateException> { factory.registerCommand("MyCommand") { EchoCommand(it) } }
    }

    @Test
    fun `test registering command from no tokens`() {
        assertThrows<IllegalArgumentException> { factory.create(emptyList()) }
    }
}
