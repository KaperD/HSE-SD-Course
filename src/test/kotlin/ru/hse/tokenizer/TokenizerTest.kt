package ru.hse.tokenizer

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenizerTest {
    private fun createTokenizer(): Tokenizer = TokenizerImpl()

    private val tokenizer: Tokenizer = createTokenizer()

    @ParameterizedTest
    @MethodSource("correctExpressionsData")
    fun `test parse correct expressions`(expression: String, expectedTokens: List<String>) {
        val res = tokenizer.tryTokenize(expression)
        assertTrue(res.isSuccess)
        assertEquals(expectedTokens, res.getOrThrow())
    }

    @ParameterizedTest
    @MethodSource("ignoresSpacesData")
    fun `test parse ignores spaces`(expression: String, expectedTokens: List<String>) {
        val res = tokenizer.tryTokenize(expression)
        assertTrue(res.isSuccess)
        assertEquals(expectedTokens, res.getOrThrow())
    }

    @ParameterizedTest
    @MethodSource("wrongQuotationData")
    fun `test parse incorrect expressions wrong quotation`(expression: String) {
        val res = tokenizer.tryTokenize(expression)
        assertTrue(res.isFailure)
        assertTrue(res.toString().contains("Invalid expression for tokenization"))
    }

    companion object {
        @JvmStatic
        fun correctExpressionsData() = listOf(
            Arguments.of("", listOf<String>()),
            Arguments.of("a", listOf("a")),
            Arguments.of("a|a", listOf("a", "|", "a")),
            Arguments.of("'a'|\"a\"", listOf("'a'", "|", "\"a\"")),
            Arguments.of("3' '3 3\" \"3 '3 '\"3\"", listOf("3' '3", "3\" \"3", "'3 '\"3\"")),
            Arguments.of("|||", listOf("|", "|", "|")),
            Arguments.of("echo 3 | wc | wc", listOf("echo", "3", "|", "wc", "|", "wc")),
            Arguments.of("cat ${'$'}FILE", listOf("cat", "${'$'}FILE")),
            Arguments.of("'  '", listOf("'  '")),
            Arguments.of("\"  \"", listOf("\"  \"")),
            Arguments.of("' ${'$'}a ' \" ${'$'}a \"", listOf("' ${'$'}a '", "\" ${'$'}a \"")),
            Arguments.of("' = + | \" ${'$'} {}[]() '", listOf("' = + | \" ${'$'} {}[]() '")),
            Arguments.of("\" = + | ' ${'$'} {}[]() \"", listOf("\" = + | ' ${'$'} {}[]() \"")),
        )

        @JvmStatic
        fun ignoresSpacesData() = listOf(
            Arguments.of("    ", listOf<String>()),
            Arguments.of("  a", listOf("a")),
            Arguments.of("a  ", listOf("a")),
            Arguments.of("  a  ", listOf("a")),
            Arguments.of("  a  b|    c ", listOf("a", "b", "|", "c")),
        )

        @JvmStatic
        fun wrongQuotationData() = listOf(
            Arguments.of("' a"),
            Arguments.of("a '"),
            Arguments.of("a'"),
            Arguments.of("'a"),
            Arguments.of("\" a"),
            Arguments.of("a \""),
            Arguments.of("a\""),
            Arguments.of("\"a"),
            Arguments.of("' a \""),
            Arguments.of("\" a '"),
            Arguments.of("\" ' \" ' \""),
            Arguments.of("' \" ' \" '"),
        )
    }
}
