package ru.hse.parser

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore
class PipeParserTest {
    private fun createParser(env: Collection<Pair<String, String>>): PipeParser {
        TODO("Return object when it's ready")
    }
    private val parser: PipeParser = createParser(
        listOf(
            Pair("a", "3"),
            Pair("s", " "),
            Pair("v", "|"),
            Pair("x", "ex"),
            Pair("y", "it"),
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
        assertTrue(parser.parse(expression).isFailure)
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
        assertTrue(parser.parse(expression).isFailure)
    }

    @ParameterizedTest
    @MethodSource("correctPipeData")
    fun `test correct pipe`(expression: String, expected: List<List<String>>) {
        val res = parser.parse(expression)
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
            Arguments.of("echo 3 | echo \"${'$'}a' '${'$'}a\"", listOf(listOf("echo", "3"), listOf("echo", "3 3"))),
            Arguments.of("echo 3 | echo\"3\"", listOf(listOf("echo", "3"), listOf("echo3"))),
            Arguments.of("echo 3 ${'$'}v wc", listOf(listOf("echo", "3", "|", "wc"))),
            Arguments.of("${'$'}x${'$'}y", listOf(listOf("exit"))),
        )
    }
}
