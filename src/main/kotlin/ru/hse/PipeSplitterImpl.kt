package ru.hse

class PipeSplitterImpl : PipeSplitter {
    override fun split(tokens: List<String>): Result<List<List<String>>> {
        if (!validatePipe(tokens)) {
            return Result.failure(RuntimeException("There is empty command in pipe"))
        }

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

        if (tokenAccumulator.isNotEmpty()) {
            splitPipe.add(tokenAccumulator)
        }

        return Result.success(splitPipe)
    }

    private fun validatePipe(tokens: List<String>): Boolean =
        tokens.isEmpty() ||
            tokens.zipWithNext().none { it == PIPE_SPLIT_TOKEN to PIPE_SPLIT_TOKEN } &&
            tokens.last() != PIPE_SPLIT_TOKEN && tokens.first() != PIPE_SPLIT_TOKEN

    companion object {
        const val PIPE_SPLIT_TOKEN = "|"
    }
}
