package ru.hse

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.charset.HseshCharsets
import ru.hse.cli.CLIImpl
import ru.hse.command.CatCommand
import ru.hse.command.EchoCommand
import ru.hse.command.ExitCommand
import ru.hse.command.PwdCommand
import ru.hse.environment.EnvironmentImpl
import ru.hse.executor.ExpressionExecutorImpl
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
import java.io.OutputStream
import kotlin.test.assertEquals

class HseshApplicationTest {
    private fun createApplication(input: InputStream, output: OutputStream, error: OutputStream): HseshApplication {
        val varNameValidator = VarNameValidatorImpl()
        val tokenizer = TokenizerImpl()
        val pipeSplitter = PipeSplitterImpl()
        val environment = EnvironmentImpl(EnvironmentImpl(null, System.getenv()))
        val substitutor = SubstitutorImpl(environment, varNameValidator)
        val assignmentParser = AssignmentParser(varNameValidator, tokenizer, substitutor)
        val pipeParser = PipeParser(tokenizer, pipeSplitter, substitutor)
        val commandFactory = CommandFactoryImpl(environment)
        commandFactory.registerCommand("exit") { ExitCommand() }
        commandFactory.registerCommand("echo") { EchoCommand(it) }
        commandFactory.registerCommand("cat") { CatCommand(it) }
        commandFactory.registerCommand("pwd") { PwdCommand(it) }
        val pipeFactory = PipeFactoryImpl()
        val expressionExecutor = ExpressionExecutorImpl(
            assignmentParser,
            pipeParser,
            environment,
            commandFactory,
            pipeFactory
        )
        val cli = CLIImpl(input, output, error)
        return HseshApplication(cli, expressionExecutor)
    }

    @ParameterizedTest
    @MethodSource("validAssignmentData")
    fun `test application`(input: String, output: String, error: String) {
        val inputStream = ByteArrayInputStream(input.toByteArray(HseshCharsets.default))
        val outputStream = ByteArrayOutputStream()
        val errorStream = ByteArrayOutputStream()
        val application = createApplication(inputStream, outputStream, errorStream)
        application.run()
        assertEquals(output, outputStream.toString(HseshCharsets.default))
        assertEquals(error, errorStream.toString(HseshCharsets.default))
    }

    companion object {
        @JvmStatic
        fun validAssignmentData() = listOf(
            Arguments.of(
                """
                    echo 3
                    echo 4 | cat
                    echo 3 | sleep 0
                    exit

                """.trimIndent(),
                """
                    > 3
                    > 4
                    > > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    echo 3
                    echo   4|cat
                    echo '3'|sleep 0
                    exit
                """.trimIndent(),
                """
                    > 3
                    > 4
                    > > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    echo 3


                    exit

                """.trimIndent(),
                """
                    > 3
                    > > > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    x=ec
                    y=ho
                    ${'$'}x${'$'}y 3
                    exit

                """.trimIndent(),
                """
                    > > > 3
                    > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    FILE=src/test/resources/file' 'with" "whitespace" in "name.txt
                    cat ${'$'}FILE
                    exit

                """.trimIndent(),
                """
                    > > Hello
                    > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    a=3
                    a=4
                    echo ${'$'}a
                    exit

                """.trimIndent(),
                """
                    > > > 4
                    > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    echo 3 | exit | echo 3

                """.trimIndent(),
                """
                    > Bye

                """.trimIndent(),
                ""
            ),
            Arguments.of(
                """
                    echo 3' | exit | echo 3
                    exit

                """.trimIndent(),
                """
                    > > Bye

                """.trimIndent(),
                "Invalid expression for tokenization\n"
            ),
            Arguments.of(
                """
                    pwd 3 | exit
                    exit

                """.trimIndent(),
                """
                    > > Bye

                """.trimIndent(),
                "pwd: too many arguments\n"
            ),
            Arguments.of(
                """
                    echo 3 | exit |
                    exit

                """.trimIndent(),
                """
                    > > Bye

                """.trimIndent(),
                "There is empty command in pipe\n"
            ),
        )
    }
}
