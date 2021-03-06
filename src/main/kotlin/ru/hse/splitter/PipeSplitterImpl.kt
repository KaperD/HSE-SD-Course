package ru.hse.splitter

import ru.hse.utils.failure

/**
 * Делит список токенов по токену |
 */
class PipeSplitterImpl : PipeSplitter {
    override fun trySplit(tokens: List<String>): Result<List<List<String>>> {
        val splitPipe = mutableListOf<List<String>>()
        var tokenAccumulator: MutableList<String> = mutableListOf()

        for (token in tokens) {
            if (token == PIPE_SPLIT_TOKEN) {
                splitPipe.add(tokenAccumulator)
                tokenAccumulator = mutableListOf()
            } else {
                tokenAccumulator.add(token)
            }
        }

        if (tokens.isNotEmpty()) {
            splitPipe.add(tokenAccumulator)
        }

        if (splitPipe.any { it.isEmpty() }) {
            return failure("There is empty command in pipe")
        }

        return Result.success(splitPipe)
    }

    companion object {
        const val PIPE_SPLIT_TOKEN = "|"
    }
}
