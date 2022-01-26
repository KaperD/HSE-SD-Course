package ru.hse

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssignmentParserTest {
    private fun createParser(): AssignmentParser {
        TODO("Return object when it's ready")
    }

    private val validator: AssignmentParser = createParser()

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
            "a=3=",
            "a=3 3",
            "a=${'$'}b b",
            "a=\" 3 \"|' 3 '",
            "33=3",
            "a|=3",
            "a==3",
            "=3",
            "a="
        ]
    )
    fun `test invalid assignment`(expression: String) {
        assertTrue(validator.parse(expression).isFailure)
    }

    @ParameterizedTest
    @MethodSource("validAssignmentData")
    fun `test valid assignment`(expression: String, expectedKey: String, expectedValue: String) {
        val res = validator.parse(expression)
        assertTrue(res.isSuccess)
        assertEquals(Pair(expectedKey, expectedValue), res.getOrThrow())
    }

    companion object {
        @JvmStatic
        fun validAssignmentData() = listOf(
            Arguments.of("a=3dF_", "a", "3dF_"),
            Arguments.of("a9a9a=${'$'}b", "a9a9a", "${'$'}b"),
            Arguments.of("aR=\" = | ' ${'$'}b \"", "aR", "\" = | ' ${'$'}b \""),
            Arguments.of("a__=' = | \" ${'$'}b '", "a__", "' = | \" ${'$'}b '"),
            Arguments.of("abc=3'3 3'3", "abc", "3'3 3'3"),
            Arguments.of("_3=\" 3 3 \"", "_3", "\" 3 3 \""),
            Arguments.of("_a=' 3 3 '", "_a", "' 3 3 '"),
            Arguments.of("__=' ${'$'}b '\" ${'$'}b \"' 3 '", "__", "' ${'$'}b '\" ${'$'}b \"' 3 '"),
            Arguments.of("a3=3'|'", "a3", "3'|'"),
            Arguments.of("AAA=3\"=\"", "AAA", "3\"=\""),
        )
    }
}
