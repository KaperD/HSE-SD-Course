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
    @MethodSource("lengthData")
    fun `test length of`(token: String, expectedLength: Int) {
        assertEquals(expectedLength, validator.lengthIn(token))
    }

    companion object {
        @JvmStatic
        fun lengthData() = listOf(
            Arguments.of("a", 1),
            Arguments.of("_aA3_aA3", 8),
            Arguments.of("_331", 4),
            Arguments.of(" A", 0),
            Arguments.of("A ", 1),
            Arguments.of("3aa", 0),
            Arguments.of("a|a", 1),
            Arguments.of("aa=a", 2),
            Arguments.of("a''a", 1),
        )
    }
}