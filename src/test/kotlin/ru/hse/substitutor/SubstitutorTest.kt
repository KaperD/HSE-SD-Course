package ru.hse.substitutor

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import ru.hse.validator.VarNameValidatorImpl
import ru.hse.environment.EnvironmentImpl
import kotlin.test.assertEquals

class SubstitutorTest {
    private fun createSubstitutor(vars: Map<String, String>): Substitutor {
        return SubstitutorImpl(EnvironmentImpl(EnvironmentImpl(null, vars)), VarNameValidatorImpl())
    }

    private val substitutor = createSubstitutor(
        mapOf(
            "a" to "3",
            "aa" to "5",
            "_aA3" to "4",
            "x" to ""
        )
    )

    @ParameterizedTest
    @MethodSource("substituteWithoutQuotationData")
    fun `test substitution without quotation`(token: String, expectedString: String) {
        assertEquals(expectedString, substitutor.substitute(token))
    }

    @ParameterizedTest
    @MethodSource("substituteWithDoubleQuotationData")
    fun `test substitution with double quotation`(token: String, expectedString: String) {
        assertEquals(expectedString, substitutor.substitute(token))
    }

    @ParameterizedTest
    @MethodSource("substituteWithSingleQuotationData")
    fun `test substitution with single quotation`(token: String, expectedString: String) {
        assertEquals(expectedString, substitutor.substitute(token))
    }

    @ParameterizedTest
    @MethodSource("substituteWithConcatenationData")
    fun `test substitution with concatenation`(token: String, expectedString: String) {
        assertEquals(expectedString, substitutor.substitute(token))
    }

    companion object {
        @JvmStatic
        fun substituteWithoutQuotationData() = listOf(
            Arguments.of("a", "a"),
            Arguments.of("${'$'}", "${'$'}"),
            Arguments.of("${'$'}3", "${'$'}3"),
            Arguments.of("${'$'}a", "3"),
            Arguments.of("${'$'}a${'$'}a", "33"),
            Arguments.of("${'$'}a=3", "3=3"),
            Arguments.of("${'$'}_aA3", "4"),
            Arguments.of("${'$'}aa", "5"),
            Arguments.of("${'$'}b", ""),
            Arguments.of("Hello${'$'}b", "Hello"),
            Arguments.of("Hello${'$'}x", "Hello"),
        )

        @JvmStatic
        fun substituteWithDoubleQuotationData() = listOf(
            Arguments.of("\"a\"", "a"),
            Arguments.of("\"${'$'}\"", "${'$'}"),
            Arguments.of("\"${'$'}3\"", "${'$'}3"),
            Arguments.of("\"${'$'}a\"", "3"),
            Arguments.of("\"${'$'}a${'$'}a\"", "33"),
            Arguments.of("\"${'$'}a=3\"", "3=3"),
            Arguments.of("\"${'$'}_aA3\"", "4"),
            Arguments.of("\"${'$'}aa\"", "5"),
            Arguments.of("\"${'$'}b\"", ""),
            Arguments.of("\"Hello${'$'}b\"", "Hello"),
            Arguments.of("\"Hello${'$'}x\"", "Hello"),
            Arguments.of("\" ${'$'}a = 12 / ${'$'}_aA3 \"", " 3 = 12 / 4 "),
            Arguments.of("\"'${'$'}a ' \"", "'3 ' "),
        )

        @JvmStatic
        fun substituteWithSingleQuotationData() = listOf(
            Arguments.of("'a'", "a"),
            Arguments.of("'${'$'}'", "${'$'}"),
            Arguments.of("'${'$'}3'", "${'$'}3"),
            Arguments.of("'${'$'}a'", "${'$'}a"),
            Arguments.of("'${'$'}a${'$'}a'", "${'$'}a${'$'}a"),
            Arguments.of("'${'$'}a=3'", "${'$'}a=3"),
            Arguments.of("'${'$'}_aA3'", "${'$'}_aA3"),
            Arguments.of("'${'$'}aa'", "${'$'}aa"),
            Arguments.of("'${'$'}b'", "${'$'}b"),
            Arguments.of("'Hello${'$'}b'", "Hello${'$'}b"),
            Arguments.of("'Hello${'$'}x'", "Hello${'$'}x"),
            Arguments.of("' ${'$'}a = 12 / ${'$'}_aA3 '", " ${'$'}a = 12 / ${'$'}_aA3 "),
            Arguments.of("'\"${'$'}a \"\" \" '", "\"${'$'}a \"\" \" "),
        )

        @JvmStatic
        fun substituteWithConcatenationData() = listOf(
            Arguments.of("${'$'}a'${'$'}a'\"${'$'}a\"", "3${'$'}a3"),
            Arguments.of("'${'$'}'a", "${'$'}a"),
            Arguments.of("${'$'}a=3", "3=3"),
            Arguments.of("${'$'}_aA3'  '${'$'}_aA3", "4  4"),
        )
    }
}
