package ru.hse

class TokenizerImpl : Tokenizer {
    private val validToken = "(\\||('[^']*'|\"[^\"]*\"|[^'\"|\\s]+)+)\\s*".toRegex()
    private val failure: Result<List<String>> = Result.failure(RuntimeException("Invalid expression for tokenization"))

    override fun tokenize(expression: String): Result<List<String>> {
        val trimmedExpression = expression.trim()

        var startIndex = 0
        val result = mutableListOf<String>()
        while (startIndex < trimmedExpression.length) {
            val match = validToken.find(trimmedExpression, startIndex)

            if (match == null || match.range.first != startIndex) {
                return failure
            }

            result.add(match.groupValues[1])
            startIndex = match.range.last + 1
        }
        return Result.success(result)
    }
}
