package ru.hse.splitter

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipeSplitterTest {
    private fun createSplitter(): PipeSplitter {
        return PipeSplitterImpl()
    }

    private val splitter: PipeSplitter = createSplitter()

    @ParameterizedTest
    @MethodSource("correctTokensData")
    fun `test correct tokens split`(tokens: List<String>, expectedLists: List<List<String>>) {
        val res = splitter.trySplit(tokens)
        assertTrue(res.isSuccess)
        assertEquals(expectedLists, res.getOrThrow())
    }

    @ParameterizedTest
    @MethodSource("wrongTokensData")
    fun `test wrong tokens split`(tokens: List<String>) {
        val res = splitter.trySplit(tokens)
        assertTrue(res.isFailure)
    }

    companion object {
        @JvmStatic
        fun correctTokensData() = listOf(
            Arguments.of(listOf<String>(), listOf<List<String>>()),
            Arguments.of(listOf("echo", "3"), listOf(listOf("echo", "3"))),
            Arguments.of(listOf("echo", "3", "|", "wc"), listOf(listOf("echo", "3"), listOf("wc"))),
            Arguments.of(
                listOf("echo", "3", "|", "wc", "|", "wc"),
                listOf(listOf("echo", "3"), listOf("wc"), listOf("wc"))
            ),
        )

        @JvmStatic
        fun wrongTokensData() = listOf(
            Arguments.of(listOf("|")),
            Arguments.of(listOf("echo", "3", "|")),
            Arguments.of(listOf("|", "echo", "3")),
            Arguments.of(listOf("echo", "3", "|", "wc", "|")),
            Arguments.of(listOf("echo", "3", "|", "|", "wc")),
        )
    }
}
