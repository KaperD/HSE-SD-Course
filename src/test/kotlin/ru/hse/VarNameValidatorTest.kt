package ru.hse

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VarNameValidatorTest {
    private fun createValidator(): VarNameValidator {
        TODO("Return object when it's ready")
    }
    private val validator: VarNameValidator = createValidator()

    @ParameterizedTest
    @ValueSource(strings =
        [
            "a",
            "A",
            "_aA3_aA3",
            "Good3_3",
            "___",
            "_331",
            "small",
            "BIG",
            "BIG_small_0123456789"
        ]
    )
    fun `test valid names`(token: String) {
        assertTrue(validator.check(token))
    }

    @ParameterizedTest
    @ValueSource(strings =
        [
            "3",
            "123",
            "a=a",
            "a|a",
            "a a",
            "Space ",
            " Space",
            "3aaD",
            "a,",
            "'Bad'"
        ]
    )
    fun `test invalid names`(token: String) {
        assertFalse(validator.check(token))
    }

    @ParameterizedTest
    @MethodSource("nameFromBeginningData")
    fun `test name from beginning`(token: String, expectedName: String) {
        assertEquals(expectedName, validator.nameFromBeginningIn(token))
    }

    companion object {
        @JvmStatic
        fun nameFromBeginningData() = listOf(
            Arguments.of("a", "a"),
            Arguments.of("_aA3_aA3", "_aA3_aA3"),
            Arguments.of("_331", "_331"),
            Arguments.of("  A", ""),
            Arguments.of("A  ", "A"),
            Arguments.of("3aa", ""),
            Arguments.of("a|a", "a"),
            Arguments.of("aa=a", "aa"),
            Arguments.of("a''a", "a"),
            Arguments.of("   ", ""),
            Arguments.of("", ""),
        )
    }
}