package ru.hse.parser

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import ru.hse.environment.EnvironmentImpl
import ru.hse.substitutor.SubstitutorImpl
import ru.hse.tokenizer.TokenizerImpl
import ru.hse.validator.VarNameValidatorImpl
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssignmentParserTest {
    private fun createParser(env: Map<String, String>): AssignmentParser {
        val varNameValidator = VarNameValidatorImpl()
        val tokenizer = TokenizerImpl()
        val environment = EnvironmentImpl(null, env)
        val substitutor = SubstitutorImpl(environment, varNameValidator)
        return AssignmentParser(varNameValidator, tokenizer, substitutor)
    }

    private val parser: AssignmentParser = createParser(
        mapOf(
            "a" to "3",
            "b" to "4"
        )
    )

    @ParameterizedTest
    @ValueSource(
        strings =
        [
            "a= 3",
            "a =3",
            "a = 3",
            "a=3\"",
            "a=3'",
            "a='3",
            "a=\"3",
            "a=3|",
            "a=3 3",
            "a=${'$'}b b",
            "a=\" 3 \"|' 3 '",
            "33=3",
            "a|=3",
            "=3",
            "echo 3",
            "cat | cat"
        ]
    )
    fun `test invalid assignment`(expression: String) {
        assertTrue(parser.tryParse(expression).isFailure)
    }

    @ParameterizedTest
    @MethodSource("validAssignmentData")
    fun `test valid assignment`(expression: String, expectedKey: String, expectedValue: String) {
        val res = parser.tryParse(expression)
        assertTrue(res.isSuccess)
        assertEquals(Pair(expectedKey, expectedValue), res.getOrThrow())
    }

    companion object {
        @JvmStatic
        fun validAssignmentData() = listOf(
            Arguments.of("a=3dF_", "a", "3dF_"),
            Arguments.of("a9a9a=${'$'}b", "a9a9a", "4"),
            Arguments.of("aR=\" = | ' ${'$'}b \"", "aR", " = | ' 4 "),
            Arguments.of("a__=' = | \" ${'$'}b '", "a__", " = | \" ${'$'}b "),
            Arguments.of("abc=3'3 3'3", "abc", "33 33"),
            Arguments.of("_3=\" 3 3 \"", "_3", " 3 3 "),
            Arguments.of("_a=' 3 3 '", "_a", " 3 3 "),
            Arguments.of("__=' ${'$'}b '\" ${'$'}b \"' 3 '", "__", " ${'$'}b  4  3 "),
            Arguments.of("a3=3'|'", "a3", "3|"),
            Arguments.of("AAA=3\"=\"", "AAA", "3="),
            Arguments.of("a=", "a", ""),
            Arguments.of("a=   ", "a", ""),
            Arguments.of("   a=3    ", "a", "3"),
            Arguments.of("a=${'$'}b ", "a", "4"),
            Arguments.of("a='${'$'}b='${'$'}b", "a", "${'$'}b=4"),
            Arguments.of("a=${'$'} ", "a", "${'$'}"),
            Arguments.of("a=${'$'}c", "a", ""),
        )
    }
}
