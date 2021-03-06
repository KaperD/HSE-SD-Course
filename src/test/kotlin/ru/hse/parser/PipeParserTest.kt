package ru.hse.parser

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import ru.hse.environment.EnvironmentImpl
import ru.hse.splitter.PipeSplitterImpl
import ru.hse.substitutor.SubstitutorImpl
import ru.hse.tokenizer.TokenizerImpl
import ru.hse.validator.VarNameValidatorImpl
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipeParserTest {
    private fun createParser(env: Map<String, String>): PipeParser {
        return PipeParser(
            TokenizerImpl(),
            PipeSplitterImpl(),
            SubstitutorImpl(EnvironmentImpl(null, env), VarNameValidatorImpl())
        )
    }

    private val parser: PipeParser = createParser(
        mapOf(
            "a" to "3",
            "s" to " ",
            "v" to "|",
            "x" to "ex",
            "y" to "it",
        )
    )

    @ParameterizedTest
    @ValueSource(
        strings =
        [
            "echo '",
            "echo \"",
            "echo 3 | echo '3",
            "echo 3 | echo \"3",
            "echo 3 | echo '3 | wc",
            "echo 3 | echo \"3 | wc",
            "echo '3 | echo \"3'\" | echo '",
            "echo \"3 | echo '3\"' | echo \"",
        ]
    )
    fun `test invalid pipe missing quotation`(expression: String) {
        assertTrue(parser.tryParse(expression).isFailure)
    }

    @ParameterizedTest
    @ValueSource(
        strings =
        [
            "echo 3 |   ",
            "|  echo 3",
            "echo 3 || echo 3",
            "echo 3 |  | echo 3",
            "echo 3 | echo 3 | echo 3 |",
            "| echo 3 | echo 3 | echo 3",
        ]
    )
    fun `test invalid pipe empty command`(expression: String) {
        assertTrue(parser.tryParse(expression).isFailure)
    }

    @ParameterizedTest
    @MethodSource("correctPipeData")
    fun `test correct pipe`(expression: String, expected: List<List<String>>) {
        val res = parser.tryParse(expression)
        assertTrue(res.isSuccess)
        assertEquals(expected, res.getOrThrow())
    }

    companion object {
        @JvmStatic
        fun correctPipeData() = listOf(
            Arguments.of("  echo   3  ", listOf(listOf("echo", "3"))),
            Arguments.of("echo   '${'$'}a '  ", listOf(listOf("echo", "${'$'}a "))),
            Arguments.of("echo   \"${'$'}a\"  ", listOf(listOf("echo", "3"))),
            Arguments.of("echo${'$'}s=3  ", listOf(listOf("echo =3"))),
            Arguments.of("a = 3  ", listOf(listOf("a", "=", "3"))),
            Arguments.of("echo 3 | wc ", listOf(listOf("echo", "3"), listOf("wc"))),
            Arguments.of("echo 3 | echo ${'$'}a' '${'$'}a", listOf(listOf("echo", "3"), listOf("echo", "3 3"))),
            Arguments.of("echo 3 | echo ${'$'}a${'$'}s${'$'}a", listOf(listOf("echo", "3"), listOf("echo", "3 3"))),
            Arguments.of("echo 3 | echo \"${'$'}a' '${'$'}a\"", listOf(listOf("echo", "3"), listOf("echo", "3' '3"))),
            Arguments.of("echo 3 | echo\"3\"", listOf(listOf("echo", "3"), listOf("echo3"))),
            Arguments.of("echo 3 ${'$'}v wc", listOf(listOf("echo", "3", "|", "wc"))),
            Arguments.of("${'$'}x${'$'}y", listOf(listOf("exit"))),
        )
    }
}
